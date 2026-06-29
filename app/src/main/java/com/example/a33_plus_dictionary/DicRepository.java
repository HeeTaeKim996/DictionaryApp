package com.example.a33_plus_dictionary;

import android.app.Activity;
import android.content.ContentResolver;
import android.content.ContentValues;
import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.os.Environment;
import android.provider.MediaStore;

import androidx.activity.ComponentActivity;
import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;

import java.io.BufferedInputStream;
import java.io.DataInputStream;
import java.io.DataOutput;
import java.io.DataOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.ArrayList;
import java.util.function.Consumer;
import java.util.zip.ZipEntry;
import java.util.zip.ZipInputStream;
import java.util.zip.ZipOutputStream;

public class DicRepository
{
    private static DicRepository _instance = new DicRepository();

    private DicRepository()
    {
    }

    private ArrayList<String> dicNames;
    private File rootFile;

    private CacheInfo cacheInfo;
    private File cacheFile;


    public static DicRepository Instance()
    {
        return _instance;
    }

    public DicData Initialize(Context context)
    {
        rootFile = context.getFilesDir();


        InitializeCache();

        // TEMP REMOVE ALL DICS
//        DeleteAllDataFromRootFile();

        UpdateFiles();

        return LoadAnyData();
    }

    public DicData LoadAnyData()
    {
        if (cacheInfo.lastVisitedDicName.equals("") == false)
        {
            for (String file : dicNames)
            {
                if (file.equals(cacheInfo.lastVisitedDicName))
                {
                    return LoadData(file);
                }
            }

            cacheInfo.lastVisitedDicName = "";
        }

        if (dicNames.size() > 0)
        {
            String firstDicName = dicNames.get(0);
            return LoadData(firstDicName);

        }
        return new DicData("BaseData", null);
    }

    private void UpdateFiles()
    {
        File[] tempFiles = rootFile.listFiles();
        dicNames = new ArrayList<String>();
        if (tempFiles != null)
        {
            for (File file : tempFiles)
            {
                if (file.isFile())
                {
                    String fileName = file.getName();
                    if (fileName.endsWith(".dic"))
                    {
                        fileName = fileName.substring(0, fileName.length() - 4);
                        dicNames.add(fileName);
                    }
                }
            }
        }
    }

    public ArrayList<String> GetDicNames()
    {
        return dicNames;
    }


    public void SaveDicData(DicData dicData)
    {
        SaveData(dicData.dicName, dicData.data);

    }

    // V_1_1
    private void SaveData(String name, ArrayList<SPair> pairs)
    {
        File savedFile = new File(rootFile, name + ".dic");

        try (DataOutputStream dos = new DataOutputStream(new FileOutputStream(savedFile)))
        {
            dos.writeInt(AppConfig.GetVersionByInt());

            dos.writeInt(pairs.size());
            for (SPair pair : pairs)
            {
                dos.writeUTF(pair.word);
                dos.writeUTF(pair.meaning);
            }

            dos.flush();


        } catch (IOException e)
        {
            e.printStackTrace();
        }

        UpdateFiles();
    }

    public DicData LoadData(String name)
    {
        File loadedFile = new File(rootFile, name + ".dic");
        try (DataInputStream dis = new DataInputStream(new FileInputStream(loadedFile)))
        {
            int version = dis.readInt();
            if (version == 11)
            {
                return LoadData_1_1__(name, dis);
            }
        } catch (IOException e)
        {
            e.printStackTrace();
        }

        return null;
    }

    public DicData LoadData_1_1__(String name, DataInputStream dis)
    {
        try
        {
            int size = dis.readInt();
            ArrayList<SPair> pairs = new ArrayList<SPair>(size); // size() 확보가 아닌, 공간 확보
            for (int i = 0; i < size; i++)
            {
                String word = dis.readUTF();
                String meaning = dis.readUTF();
                pairs.add(new SPair(word, meaning));
            }

            return new DicData(name, pairs);
        } catch (Exception e)
        {
            e.printStackTrace();
        }

        return null;
    }

    private void DeleteAllDataFromRootFile()
    {
        File[] allFiles = rootFile.listFiles();
        if (allFiles != null)
        {
            for (File file : allFiles)
            {
                if (file.isFile())
                {
                    file.delete();
                }
            }
        }

        dicNames.clear();
        cacheFile.delete();
    }

    public boolean DeleteDic(String dicName)
    {
        for (int i = 0; i < dicNames.size(); i++)
        {
            String name = dicNames.get(i);
            if (dicName.equals(name))
            {
                File deletedFile = new File(rootFile, dicName + ".dic");
                if (deletedFile.exists())
                {
                    deletedFile.delete();
                    dicNames.remove(i);
                    return true;
                }

                return false;
            }
        }
        return false;
    }

    public boolean ChangeDicName(String fromName, String toName)
    {
        for (int i = 0; i < dicNames.size(); i++)
        {
            String name = dicNames.get(i);
            if (fromName.equals(name))
            {
                File fromFile = new File(rootFile, fromName + ".dic");
                if (fromFile.exists())
                {
                    File toFile = new File(rootFile, toName + ".dic");

                    if (fromFile.renameTo(toFile))
                    {
                        dicNames.set(i, toName);
                        return true;
                    } else
                    {
                        return false;
                    }
                }
            }
            ;
        }

        return false;
    }


    private void InitializeCache()
    {
        cacheInfo = new CacheInfo();
        cacheFile = new File(rootFile, "cache.cac");

        if (cacheFile.exists())
        {
            try (DataInputStream dis = new DataInputStream((new FileInputStream(cacheFile))))
            {
                int version = dis.readInt();
                if (version == 11)
                {
                    ReadCache_v_1_1__(dis);
                }
            } catch (IOException e)
            {
                e.printStackTrace();
            }
        }
    }

    private void ReadCache_v_1_1__(DataInputStream dis)
    {
        try
        {
            cacheInfo.lastVisitedDicName = dis.readUTF();
        } catch (IOException e)
        {
            e.printStackTrace();
        }
    }


    private void DeleteCacheData()
    {
        File tempFile = new File(rootFile, "cache.cac");
        if (tempFile.exists())
        {
            tempFile.delete();
        }
    }


    public void UpdateCacheInfo_LastVisitedDicName(String dicName)
    {
        if (cacheInfo.lastVisitedDicName.equals(dicName)) return;

        cacheInfo.lastVisitedDicName = dicName;
        try (DataOutputStream dos = new DataOutputStream((new FileOutputStream(cacheFile))))
        {
            dos.writeInt(AppConfig.GetVersionByInt());
            dos.writeUTF(cacheInfo.lastVisitedDicName);
        } catch (IOException e)
        {
            e.printStackTrace();
        }
    }


    public void ExportDataToZip(Context context)
    {
        ContentValues values = new ContentValues();
        values.put(MediaStore.MediaColumns.DISPLAY_NAME, "Dictionary_data.zip");    // 파일 이름
        values.put(MediaStore.MediaColumns.MIME_TYPE, "application/zip");           // 파일 형식
        values.put(MediaStore.MediaColumns.RELATIVE_PATH, Environment.DIRECTORY_DOWNLOADS);
        // 다운로드 파일 에 저장

        ContentResolver resolver = context.getContentResolver();
        Uri uri = resolver.insert(MediaStore.Downloads.EXTERNAL_CONTENT_URI, values);

        if (uri == null) return;

        UpdateFiles();

        try (OutputStream targetOutputStream = resolver.openOutputStream(uri);)
        {
            try (ZipOutputStream zos = new ZipOutputStream(targetOutputStream))
            {
                ZipEntry sigEntry = new ZipEntry("Dic.sig");
                zos.putNextEntry(sigEntry);
                zos.closeEntry();

                byte[] buffer = new byte[4096];
                Consumer<File> AddDataToZip = (File file) ->
                {
                    if (file.exists() == false) return;

                    ZipEntry zipEntry = new ZipEntry(file.getName());
                    try
                    {
                        zos.putNextEntry(zipEntry);

                        try (FileInputStream fis = new FileInputStream(file))
                        {
                            int length;
                            while ((length = fis.read(buffer)) > 0)
                            {
                                // ((1) 버퍼의 (2) 오프셋부터, (3) 길이만큼의 (1) 버퍼 데이터를 zos에 write
                                zos.write(buffer, 0, length);
                            }
                        } catch (IOException e)
                        {
                            e.printStackTrace();
                        }
                        zos.closeEntry();
                    } catch (IOException e)
                    {
                        e.printStackTrace();
                    }
                };

                for (String dicName : dicNames)
                {
                    File file = new File(rootFile, dicName + ".dic");
                    AddDataToZip.accept(file);
                }

                zos.flush();
            } catch (IOException e)
            {
                e.printStackTrace();
            }
        } catch (IOException e)
        {
            e.printStackTrace();
        }
    }


    public void ImportFromZip(Context context, Consumer<DicData> callback)
    {
        ComponentActivity activity = (ComponentActivity) context;

        ActivityResultLauncher<Intent> filePickerLauncher = activity.getActivityResultRegistry()
                .register(
                        "custom_file_picker_key",
                        new ActivityResultContracts.StartActivityForResult(),
                        result ->
                        {
                            if(result.getResultCode() == Activity.RESULT_OK
                                    && result.getData() != null)
                            {
                                Uri fileUri = result.getData().getData();
                                callback.accept(ImportFromZip_ReadCode(context, fileUri));
                            }
                        });

        Intent intent = new Intent(Intent.ACTION_OPEN_DOCUMENT);
        intent.addCategory(Intent.CATEGORY_OPENABLE);
        intent.setType("application/zip");

        filePickerLauncher.launch(intent);
    }

    private DicData ImportFromZip_ReadCode(Context context, Uri uri)
    {
        try
        {
            InputStream inputStream = context.getContentResolver().openInputStream(uri);
            BufferedInputStream bufferedInputStream = new BufferedInputStream(inputStream);
            ZipInputStream zipInputStream = new ZipInputStream(bufferedInputStream);

            
            // 기존 데이터 모두 삭제
            DeleteAllDataFromRootFile();

            byte[] buffer = new byte[4096];
            ZipEntry entry;
            boolean bHasSignature = false;
            while((entry = zipInputStream.getNextEntry()) != null)
            {
                String fileName = entry.getName();
                if(fileName.equals("Dic.sig"))
                {
                    bHasSignature = true;
                    continue;
                }
                File file = new File(rootFile, fileName);

                try(FileOutputStream fos = new FileOutputStream(file))
                {
                    int length;
                    while((length = zipInputStream.read(buffer)) > 0)
                    {
                        fos.write(buffer, 0, length);
                    }
                    fos.flush();
                }
                catch (IOException e) { e.printStackTrace(); }

                zipInputStream.closeEntry();
            }

            if(bHasSignature == false)
            {
                // Wrong Zip File
                DeleteAllDataFromRootFile();
                return null;
            }
            return Initialize(context);
        }
        catch (IOException e) { e.printStackTrace(); }

        return LoadAnyData();
    }

}


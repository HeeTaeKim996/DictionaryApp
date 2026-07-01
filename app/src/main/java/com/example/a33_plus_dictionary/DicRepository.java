package com.example.a33_plus_dictionary;

import android.app.Activity;
import android.content.ContentResolver;
import android.content.ContentValues;
import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.os.Environment;
import android.provider.MediaStore;
import android.util.Log;

import androidx.activity.ComponentActivity;
import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;

import java.io.BufferedInputStream;
import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.TreeSet;
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


    private File rootFile;
    private File metRootFile;
    private File dicRootFile;

    private CacheInfo cacheInfo;
    private File cacheFile;

    File initFile;
    File itemFile;

    private ArrayList<DicInfo> dicInfos;
    private TreeSet<String> items;
    private HashMap<String, ArrayList<DicInfo>> mappedInfos;


    public static DicRepository Instance()
    {
        return _instance;
    }

    public DicData Initialize(Context context)
    {
        rootFile = context.getFilesDir();
        metRootFile = new File(rootFile, "met");
        dicRootFile = new File(rootFile, "dic");
        initFile = new File(rootFile, "Init.ini");
        itemFile = new File(rootFile, "Item.dat");

        if (dicRootFile.exists() == false)
        {
            dicRootFile.mkdir();
        }
        if (metRootFile.exists() == false)
        {
            metRootFile.mkdir();
        }

        dicInfos = new ArrayList<DicInfo>();

        items = new TreeSet<String>();
        items.add("None");

        mappedInfos = new HashMap<String, ArrayList<DicInfo>>();


//        DeleteAllFilesFromRootFile();

        InitializeData();

//        Debug_ShowAllRootFiles(context);

        return LoadAnyData();
    }

    public DicData LoadAnyData()
    {
        if (cacheInfo.lastVisitedDicName.equals("") == false)
        {
            for (DicInfo dicInfo : dicInfos)
            {
                String dicName = dicInfo.dicName;
                if (dicName.equals(cacheInfo.lastVisitedDicName))
                {
                    return LoadData(dicInfo);
                }
            }

            cacheInfo.lastVisitedDicName = "";
        }

        if (dicInfos.size() > 0)
        {
            DicInfo firstInfo = dicInfos.get(0);
            return LoadData(firstInfo);

        }
        DicInfo dicInfo = new DicInfo();
        dicInfo.dicName = "BaseData";
        AddInfo(dicInfo);
        SaveMetaData(dicInfo);
        return new DicData(dicInfo, null);
    }


    private void InitializeData()
    {
        LoadInitData();
        LoadItemData();
        InitializeDicInfos();
        InitializeCache();
    }

    private void SaveInitData()
    {
        try(DataOutputStream dos = new DataOutputStream(new FileOutputStream(initFile)))
        {
            dos.writeInt(AppConfig.GetVersionByInt());


        }
        catch(IOException e) { e.printStackTrace(); }
    }

    private void LoadInitData()
    {
        try(DataInputStream dis = new DataInputStream(new FileInputStream(initFile)))
        {
            int version = dis.readInt();


        }
        catch(IOException e) { e.printStackTrace(); }
    }




    private void SaveItemData()
    {
        try(DataOutputStream dos = new DataOutputStream(new FileOutputStream(itemFile)))
        {
            dos.writeInt(AppConfig.GetVersionByInt());

            ArrayList<String> itemString = GetItems();
            dos.writeInt(itemString.size());

            for(String s : itemString)
            {
                dos.writeUTF(s);
            }
        }
        catch (IOException e) { e.printStackTrace(); }
    }
    private  void LoadItemData()
    {
        items.clear();
        items.add("None");

        try(DataInputStream dis = new DataInputStream(new FileInputStream(itemFile)))
        {
            int version = dis.readInt();

            int size = dis.readInt();
            while(size-- > 0)
            {
                String item = dis.readUTF();
                items.add(item);
                if(mappedInfos.containsKey(item) == false)
                {
                    mappedInfos.put(item, new ArrayList<DicInfo>());
                }

            }
        }
        catch(IOException e) { e.printStackTrace(); }
    }



    private void InitializeDicInfos()
    {
        File[] metFiles = metRootFile.listFiles();
        if (metFiles != null)
        {
            for (File metFile : metFiles)
            {
                if (metFile.isFile())
                {
                    String fileName = metFile.getName();
                    if (fileName.endsWith(".met"))
                    {
                        fileName = fileName.substring(0, fileName.length() - 4);
                        DicInfo dicInfo = new DicInfo();
                        dicInfo.dicName = fileName;

                        try (DataInputStream dis = new DataInputStream(new FileInputStream(metFile)))
                        {
                            int version = dis.readInt();
                            dicInfo.item = dis.readUTF();
                            dicInfo.aimDate = dis.readInt();
                            dicInfo.aimIndex = dis.readByte();

                        } catch (IOException e)
                        {
                            e.printStackTrace();
                        }

                        AddInfo(dicInfo);
                    }
                }
            }
        }
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

                cacheInfo.lastVisitedDicName = dis.readUTF();
                cacheInfo.currItem = dis.readUTF();

            } catch (IOException e)
            {
                e.printStackTrace();
            }
        }
    }


    public ArrayList<DicInfo> GetDicInfos()
    {
        if(cacheInfo.currItem == "All")
        {
            return dicInfos;
        }

        if(mappedInfos.containsKey(cacheInfo.currItem) == false)
        {
            mappedInfos.put(cacheInfo.currItem, new ArrayList<DicInfo>());
        }


        return mappedInfos.get(cacheInfo.currItem);
    }

    public void ChangeCurrItem(String item)
    {
        cacheInfo.currItem = item;
        SaveCacheData();
    }



    public void ChangeItem(DicInfo dicInfo, String newItem)
    {
        items.add(newItem);

        ArrayList<DicInfo> itemInfos = mappedInfos.get(dicInfo.item);
        itemInfos.remove(dicInfo);

        if (mappedInfos.containsKey(newItem) == false)
        {
            mappedInfos.put(newItem, new ArrayList<DicInfo>());
        }
        mappedInfos.get(newItem).add(dicInfo);

        dicInfo.item = newItem;

        SaveMetaData(dicInfo);
    }

    public void UpdateMetaData(DicInfo dicInfo)
    {
        SaveMetaData(dicInfo);
    }

    private void SaveMetaData(DicInfo dicInfo)
    {
        File metFile = MakeMetFile(dicInfo.dicName);

        try (DataOutputStream dos = new DataOutputStream(new FileOutputStream(metFile)))
        {
            dos.writeInt(AppConfig.GetVersionByInt());

            dos.writeUTF(dicInfo.item);
            dos.writeInt(dicInfo.aimDate);
            dos.writeByte(dicInfo.aimIndex);

            dos.flush();
        } catch (IOException e)
        {
            e.printStackTrace();
        }
    }


    public void UpdateDicData(DicData dicData)
    {
        SaveDicData(dicData.dicInfo, dicData.data);
    }

    private void SaveDicData(DicInfo dicInfo, ArrayList<SPair> pairs)
    {
        File savedFile = MakeDicFile(dicInfo.dicName);

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
    }



    private File MakeDicFile(String dicName)
    {
        return new File(dicRootFile, dicName + ".dic");
    }

    private File MakeMetFile(String dicName)
    {
        return new File(metRootFile, dicName + ".met");
    }


    public DicData MakeNewDic(String newDicName, String item)
    {
        for (DicInfo dicInfo : dicInfos)
        {
            if (dicInfo.dicName.equals(newDicName))
            {
                return null;
            }
        }

        DicInfo newInfo = new DicInfo();
        newInfo.dicName = newDicName;
        newInfo.item = item;

        DicData newData = new DicData(newInfo, null);

        AddInfo(newInfo);

        SaveMetaData(newInfo);
        SaveDicData(newInfo, newData.data);

        return newData;
    }

    private void AddInfo(DicInfo dicInfo)
    {
        dicInfos.add(dicInfo);

        items.add(dicInfo.item);

        if (mappedInfos.containsKey(dicInfo.item) == false)
        {
            mappedInfos.put(dicInfo.item, new ArrayList<DicInfo>());
        }
        mappedInfos.get(dicInfo.item).add(dicInfo);
    }

    private boolean DeleteInfo(DicInfo dicInfo)
    {
        if (dicInfos.remove(dicInfo) == false) return false;

        ArrayList<DicInfo> itemInfos = mappedInfos.get(dicInfo.item);
        itemInfos.remove(dicInfo);

        return true;
    }


    public DicData LoadData(DicInfo dicInfo)
    {
        File loadedFile = MakeDicFile(dicInfo.dicName);
        try (DataInputStream dis = new DataInputStream(new FileInputStream(loadedFile)))
        {
            int version = dis.readInt();

            return LoadData(dicInfo, dis);

        } catch (IOException e)
        {
            e.printStackTrace();
        }

        return new DicData(dicInfo, new ArrayList<SPair>());
    }

    public DicData LoadData(DicInfo dicInfo, DataInputStream dis)
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


            return new DicData(dicInfo, pairs);
        } catch (Exception e)
        {
            e.printStackTrace();
        }


        return new DicData(dicInfo, new ArrayList<SPair>());
    }

    private void DeleteAllFilesFromRootFile()
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


        allFiles = metRootFile.listFiles();
        if (allFiles != null)
        {
            for (File file : allFiles)
            {
                file.delete();
            }
        }

        allFiles = dicRootFile.listFiles();
        if (allFiles != null)
        {
            for (File file : allFiles)
            {
                file.delete();

            }
        }

        dicInfos.clear();
        mappedInfos.clear();

        items.clear();
        items.add("None");

        cacheInfo = new CacheInfo();
    }

    public boolean DeleteDic(DicInfo dicInfo)
    {
        if (DeleteInfo(dicInfo) == false)
        {
            return false;
        }

        File deletedFile = MakeMetFile(dicInfo.dicName);
        if (deletedFile.exists())
        {
            deletedFile.delete();
        }

        deletedFile = MakeDicFile(dicInfo.dicName);
        if (deletedFile.exists())
        {
            deletedFile.delete();

        }

        return true;
    }

    public boolean ChangeDicName(DicInfo changingInfo, String toName)
    {
        for (DicInfo dicInfo : dicInfos)
        {
            if (dicInfo.equals(changingInfo))
            {
                File fromFile = MakeDicFile(dicInfo.dicName);
                if (fromFile.exists())
                {
                    File toFile = MakeDicFile(toName);

                    if (fromFile.renameTo(toFile))
                    {
                        dicInfo.dicName = toName;
                        return true;
                    } else
                    {
                        return false;
                    }
                }
            }
        }

        return false;
    }

    public void UpdateLastVisitedDicName(String dicName)
    {
        if (cacheInfo.lastVisitedDicName.equals(dicName)) return;
        cacheInfo.lastVisitedDicName = dicName;

        SaveCacheData();
    }


    private void SaveCacheData()
    {
        try (DataOutputStream dos = new DataOutputStream((new FileOutputStream(cacheFile))))
        {
            dos.writeInt(AppConfig.GetVersionByInt());
            dos.writeUTF(cacheInfo.lastVisitedDicName);
            dos.writeUTF(cacheInfo.currItem);
        } catch (IOException e)
        {
            e.printStackTrace();
        }
    }


    public ArrayList<String> GetItems()
    {
        return new ArrayList<String>(items);
    }

    public void AddItem(String newItem)
    {
        if(items.add(newItem))
        {
            if(mappedInfos.containsKey(newItem) == false)
            {
                mappedInfos.put(newItem, new ArrayList<DicInfo>());
            }

            SaveItemData();
        }
    }

    public String GetCurrItem()
    {
        if(items.contains(cacheInfo.currItem) == false)
        {
            cacheInfo.currItem = "All";
        }

        return cacheInfo.currItem;
    }

    public boolean ChangeItemName(String fromName, String toName)
    {
        if(toName.equals("All"))    return false;
        if(items.contains(toName))  return false;

        ArrayList<DicInfo> itemInfos = mappedInfos.get(fromName);
        for(DicInfo dicInfo : itemInfos)
        {
            dicInfo.item = toName;
            SaveMetaData(dicInfo);
        }

        items.add(toName);
        mappedInfos.put(toName, itemInfos);

        items.remove(fromName);
        mappedInfos.remove(fromName);

        SaveItemData();

        return true;
    }

    public void DeleteItem(String deletedName)
    {
        ArrayList<DicInfo> itemInfos = mappedInfos.get(deletedName);
        for(DicInfo dicInfo : itemInfos)
        {
            dicInfo.item = "None";
            SaveMetaData(dicInfo);
        }

        if(mappedInfos.containsKey("None") == false)
        {
            // Should not happen. mappedInfos Must Have None Item
            android.os.Debug.waitForDebugger();
        }
        ArrayList<DicInfo> nonInfo = mappedInfos.get("None");
        nonInfo.addAll(itemInfos);

        items.remove(deletedName);
        mappedInfos.remove(deletedName);

        SaveItemData();
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

        InitializeDicInfos();

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

                for (DicInfo dicInfo : dicInfos)
                {
                    String dicName = dicInfo.dicName;
                    File file = MakeDicFile(dicInfo.dicName);
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
                            if (result.getResultCode() == Activity.RESULT_OK
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
            DeleteAllFilesFromRootFile();

            byte[] buffer = new byte[4096];
            ZipEntry entry;
            boolean bHasSignature = false;
            while ((entry = zipInputStream.getNextEntry()) != null)
            {
                String fileName = entry.getName();
                if (fileName.equals("Dic.sig"))
                {
                    bHasSignature = true;
                    continue;
                }
                File file = MakeDicFile(fileName);

                try (FileOutputStream fos = new FileOutputStream(file))
                {
                    int length;
                    while ((length = zipInputStream.read(buffer)) > 0)
                    {
                        fos.write(buffer, 0, length);
                    }
                    fos.flush();
                } catch (IOException e)
                {
                    e.printStackTrace();
                }

                zipInputStream.closeEntry();
            }

            if (bHasSignature == false)
            {
                // Wrong Zip File
                DeleteAllFilesFromRootFile();
                return null;
            }
            return Initialize(context);
        } catch (IOException e)
        {
            e.printStackTrace();
        }

        return LoadAnyData();
    }


    private void Debug_ShowAllRootFiles(Context context)
    {
        String temp = ".\nLOG\n\n";
        File[] files = rootFile.listFiles();
        for (File file : files)
        {
            temp += file.toString() + "\n";
        }

        temp += "\nMet\n";
        files = metRootFile.listFiles();
        for (File file : files)
        {
            temp += file.toString() + "\n";
        }

        temp += "\nDic\n";
        files = dicRootFile.listFiles();
        for (File file : files)
        {
            temp += file.toString() + "\n";
        }

        Log.d("ShowRootFiles", temp);
    }
}


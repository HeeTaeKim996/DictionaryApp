package com.example.a33_plus_dictionary;

import android.content.Context;

import androidx.constraintlayout.solver.Cache;

import java.io.DataInput;
import java.io.DataInputStream;
import java.io.DataOutput;
import java.io.DataOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.ArrayList;

public class DicRepository
{
    private static DicRepository _instance = new DicRepository();
    private DicRepository(){}

    private ArrayList<String> files;
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

        // TODO TEMP
//        RemoveAllRepository();

        UpdateFiles();

        return LoadAnyData();
    }

    public DicData LoadAnyData()
    {
        if(cacheInfo.lastVisitedDicName.equals("") == false)
        {
            for(String file : files)
            {
                if(file.equals(cacheInfo.lastVisitedDicName))
                {
                    return LoadData(file);
                }
            }

            cacheInfo.lastVisitedDicName = "";
        }

        if(files.size() > 0)
        {
            String firstDicName = files.get(0);
            return LoadData(firstDicName);

        }
        return new DicData("BaseData", null);
    }

    private void UpdateFiles()
    {
        File[] tempFiles = rootFile.listFiles();
        files = new ArrayList<String>();
        if(tempFiles != null)
        {
            for(File file : tempFiles)
            {
                if(file.isFile())
                {
                    String fileName = file.getName();
                    if(fileName.endsWith(".dic"))
                    {
                        fileName = fileName.substring(0, fileName.length() -4);
                        files.add(fileName);
                    }
                }
            }
        }
    }

    public ArrayList<String> GetDicNames()
    {
        return files;
    }



    public void SaveDicData(DicData dicData)
    {
        SaveData(dicData.dicName, dicData.data);

    }

    // V_1_1
    private void SaveData(String name, ArrayList<SPair> pairs)
    {
        File savedFile = new File(rootFile, name + ".dic");

        try(DataOutputStream dos = new DataOutputStream(new FileOutputStream(savedFile)))
        {
            dos.writeInt(AppConfig.GetVersionByInt());

            dos.writeInt(pairs.size());
            for(SPair pair : pairs)
            {
                dos.writeUTF(pair.word);
                dos.writeUTF(pair.meaning);
            }

            dos.flush();


        }
        catch(IOException e)
        {
            e.printStackTrace();
        }

        UpdateFiles();
    }

    public DicData LoadData(String name)
    {
        File loadedFile = new File(rootFile, name + ".dic");
        try(DataInputStream dis = new DataInputStream(new FileInputStream(loadedFile)))
        {
            int version = dis.readInt();
            if(version == 11)
            {
                return LoadData_1_1(name, dis);
            }
        }
        catch(IOException e)
        {
            e.printStackTrace();
        }

        return null;
    }

    public DicData LoadData_1_1(String name, DataInputStream dis)
    {
        try
        {
            int size = dis.readInt();
            ArrayList<SPair> pairs = new ArrayList<SPair>(size); // size() 확보가 아닌, 공간 확보
            for(int i = 0; i < size; i++)
            {
                String word = dis.readUTF();
                String meaning = dis.readUTF();
                pairs.add(new SPair(word, meaning));
            }

            return new DicData(name, pairs);
        }
        catch(Exception e)
        {
            e.printStackTrace();
        }

        return null;
    }

    private void RemoveAllRepository()
    {
        File[] files = rootFile.listFiles();
        if(files != null)
        {
            for(File file : files)
            {
                if(file.isFile())
                {
                    file.delete();
                }
            }
        }
    }

    public boolean DeleteDic(String dicName)
    {
        for(int i = 0; i < files.size(); i++)
        {
            String name = files.get(i);
            if(dicName.equals(name))
            {
                File deletedFile = new File(rootFile, dicName + ".dic");
                if(deletedFile.exists())
                {
                    deletedFile.delete();
                    files.remove(i);
                    return true;
                }

                return false;
            }
        }
        return false;
    }

    public boolean ChangeDicName(String fromName, String toName)
    {
        for(int i = 0; i < files.size(); i++)
        {
            String name = files.get(i);
            if(fromName.equals(name))
            {
                File fromFile = new File(rootFile, fromName + ".dic");
                if(fromFile.exists())
                {
                    File toFile = new File(rootFile, toName + ".dic");

                    if(fromFile.renameTo(toFile))
                    {
                        files.set(i, toName);
                        return true;
                    }
                    else
                    {
                        return false;
                    }
                }
            };
        }

        return false;
    }

    private void InitializeCache()
    {
        cacheInfo = new CacheInfo();
        cacheFile = new File(rootFile, "cache.cac");
        if(cacheFile.exists())
        {
            try(DataInputStream dis = new DataInputStream((new FileInputStream(cacheFile))))
            {
                cacheInfo.lastVisitedDicName = dis.readUTF();
            }
            catch(IOException e)
            {
                e.printStackTrace();
            }
        }
    }

    public void UpdateCacheInfo_LastVisitedDicName(String dicName)
    {
        if(cacheInfo.lastVisitedDicName.equals(dicName)) return;

        cacheInfo.lastVisitedDicName = dicName;
        try(DataOutputStream dos = new DataOutputStream((new FileOutputStream(cacheFile))))
        {
            dos.writeUTF(cacheInfo.lastVisitedDicName);
        }
        catch(IOException e)
        {
            e.printStackTrace();
        }
    }
}


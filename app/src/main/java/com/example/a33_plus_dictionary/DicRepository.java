package com.example.a33_plus_dictionary;

import android.content.Context;

import java.io.DataInputStream;
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



    public static DicRepository Instance()
    {
        return _instance;
    }

    public DicData Initialize(Context context)
    {
        rootFile = context.getFilesDir();

        // TODO TEMP
//        RemoveAllRepository();

        UpdateFiles();

        return LoadAnyData();
    }

    public DicData LoadAnyData()
    {
        // TODO : 최신 처리했던 DicName 을 로그로 남긴 후, 해당 DicName 이 파일에 있다면 최우선으로 불러옴
        if(files.size() > 0)
        {
            String tempFirst = files.get(0);
            return LoadData(tempFirst);

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

    private void SaveData(String name, ArrayList<SPair> pairs)
    {
        File savedFile = new File(rootFile, name + ".dic");

        try(DataOutputStream dos = new DataOutputStream(new FileOutputStream(savedFile)))
        {
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

}

package com.example.a33_plus_dictionary;

import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.app.AlertDialog;

import android.content.Context;
import android.net.Uri;
import android.os.Bundle;
import android.provider.ContactsContract;
import android.provider.UserDictionary;
import android.text.style.BulletSpan;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.TextView;

import com.example.a33_plus_dictionary.databinding.ActivityMainBinding;
import com.example.a33_plus_dictionary.databinding.AddWordBinding;
import com.example.a33_plus_dictionary.databinding.ChangeStringBinding;
import com.example.a33_plus_dictionary.databinding.DataConfigureBinding;
import com.example.a33_plus_dictionary.databinding.DatasBinding;
import com.example.a33_plus_dictionary.databinding.EditBinding;
import com.example.a33_plus_dictionary.databinding.ReviseWordBinding;
import com.example.a33_plus_dictionary.databinding.WordsBinding;
import com.example.a33_plus_dictionary.databinding.YesOrNoBinding;

import androidx.appcompat.app.AlertDialog.Builder;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Random;

public class MainActivity extends AppCompatActivity
{
    ActivityMainBinding activityMainBinding;
    DicData dicData;

    int tempInt = 0;

    @Override
    protected void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);

        activityMainBinding = ActivityMainBinding.inflate(getLayoutInflater());
        setContentView(activityMainBinding.getRoot());



        // TEMP
        String fileName = DicDataManager.Initialize(MainActivity.this);

        if(fileName != "")
        {
            File directory = MainActivity.this.getFilesDir();
            File savedFile = new File(directory, fileName + ".dic");

            dicData = new DicData(savedFile);
        }
        else
        {
            dicData = new DicData("BaseDic");
        }




        // 기본 버튼 세팅
        activityMainBinding.addButton.setOnClickListener(new View.OnClickListener()
        {
            @Override
            public void onClick(View view)
            {
                AddWordBinding addWordBinding = AddWordBinding.inflate(getLayoutInflater());
                AlertDialog dialog = new Builder(MainActivity.this)
                        .setView(addWordBinding.getRoot()).create();
                dialog.show();

                addWordBinding.reviseWordButtonCancel.setOnClickListener(new View.OnClickListener()
                {
                    @Override
                    public void onClick(View view)
                    {
                        dialog.dismiss();
                    }
                });

                addWordBinding.addWordButtonAdd.setOnClickListener(new View.OnClickListener()
                {
                    @Override
                    public void onClick(View view)
                    {
                        String word = addWordBinding.wordText.getText().toString();
                        String meaning = addWordBinding.meaningText.getText().toString();
                        SPair sPair = new SPair(word, meaning);

                        dicData.AddData(sPair);

                        dialog.dismiss();
                    }
                });
            }
        });


        activityMainBinding.buttonShake.setOnClickListener(new View.OnClickListener()
        {
            @Override
            public void onClick(View view)
            {
                dicData.ShakePos();
                dicData.DisplayAll();
            }
        });

        activityMainBinding.buttonSave.setOnClickListener(new View.OnClickListener()
        {
            @Override
            public void onClick(View view)
            {
                dicData.SaveData();
            }
        });

        activityMainBinding.buttonSeeDataManager.setOnClickListener(new View.OnClickListener()
        {
            @Override
            public void onClick(View view)
            {
                DicDataManager.ShowXml(MainActivity.this,
                        activityMainBinding.getRoot());
            }
        });

    }



























































    class WordsItem
    {
        public WordsBinding wordsBinding;
        private boolean bOn = true;
        private String word;
        private String meaning;
        private int pos;

        public WordsItem(Context context, ViewGroup parent, String word, String meaning,
                         int InPos)
        {
            wordsBinding = WordsBinding.inflate(LayoutInflater.from(context), parent,
                    false);

            wordsBinding.word.setText(word);
            wordsBinding.meaning.setText(meaning);

            this.word = word;
            this.meaning = meaning;
            this.pos = InPos;

            wordsBinding.getRoot().setOnClickListener(new View.OnClickListener()
            {
                @Override
                public void onClick(View view)
                {
                    if(bOn)
                    {
                        wordsBinding.meaning.setText("");
                    }
                    else
                    {
                        wordsBinding.meaning.setText(meaning);
                    }

                    bOn = !bOn;
                }
            });

            wordsBinding.getRoot().setOnLongClickListener(new View.OnLongClickListener()
            {
                @Override
                public boolean onLongClick(View view)
                {
                    // TEMP DELETE
//                    parent.removeView(wordsBinding.getRoot());

                    EditBinding editBinding = EditBinding.inflate(getLayoutInflater());
                    AlertDialog dialog = new Builder(MainActivity.this)
                            .setView(editBinding.getRoot()).create();
                    dialog.show();

                    editBinding.buttonExit.setOnClickListener(new View.OnClickListener()
                    {
                        @Override
                        public void onClick(View view)
                        {
                            dialog.dismiss();
                        }
                    });

                    editBinding.buttonDelete.setOnClickListener(new View.OnClickListener()
                    {
                        @Override
                        public void onClick(View view)
                        {
                            dicData.RemoveData(WordsItem.this);

                            dialog.dismiss();
                        }
                    });

                    editBinding.buttonRevise.setOnClickListener(new View.OnClickListener()
                    {
                        @Override
                        public void onClick(View view)
                        {
                            ReviseWordBinding rwBinding = ReviseWordBinding.inflate(getLayoutInflater());
                            AlertDialog rwDialog = new Builder(MainActivity.this)
                                    .setView(rwBinding.getRoot()).create();
                            rwDialog.show();

                            rwBinding.wordText.setText(WordsItem.this.word);
                            rwBinding.meaningText.setText(WordsItem.this.meaning);

                            rwBinding.reviseWordButtonCancel.setOnClickListener(new View.OnClickListener()
                            {
                                @Override
                                public void onClick(View view)
                                {
                                    rwDialog.dismiss();
                                    dialog.dismiss();
                                }
                            });

                            rwBinding.reviseWordButtonAdd.setOnClickListener(new View.OnClickListener()
                            {
                                @Override
                                public void onClick(View view)
                                {
                                    SPair revisedPair = new SPair(rwBinding.wordText.getText().toString(),
                                            rwBinding.meaningText.getText().toString());
                                    dicData.ReviseData(revisedPair, WordsItem.this);

                                    rwDialog.dismiss();
                                    dialog.dismiss();
                                }
                            });
                        }
                    });


                    // true 로 리턴해야, 이벤트를 소비하여 일반 클릭 이 추가로 발생하는 것을 방지
                    return true;
                }
            });

            parent.addView(wordsBinding.getRoot());
        }
    }



































    /*------------------------------
                DicData
     -----------------------------*/
    class SPair
    {
        public SPair(String InWord, String InMeaning)
        {
            word = InWord;
            meaning = InMeaning;
        }

        String word;
        String meaning;
    }

    class DicData
    {
        private String dicName;
        private ArrayList<SPair> data;
        private ArrayList<Integer> poser;

        public DicData()
        {
            data = new ArrayList<SPair>();
            poser = new ArrayList<Integer>();
        }

        public DicData(String newName)
        {
            this();
            dicName = newName;
        }

        public DicData(File file)
        {
            this();
            try(DataInputStream dis = new DataInputStream(new FileInputStream(file)))
            {
                dicName = dis.readUTF();
                int size = dis.readInt();
                while(size-- > 0)
                {
                    String word = dis.readUTF();
                    String meaning = dis.readUTF();

                    SPair sPair = new SPair(word, meaning);
                    AddData(sPair);
                }

                DisplayAll();
            }
            catch(IOException e)
            {
                e.printStackTrace();
            }
        }

        public void SaveData()
        {
            File directory = MainActivity.this.getFilesDir();
            File file = new File(directory, dicName + ".dic");

            try(DataOutputStream dos = new DataOutputStream(new FileOutputStream(file)))
            {
                dos.writeUTF(dicName);
                dos.writeInt(data.size());
                for(int i = 0; i < data.size(); i++)
                {
                    SPair sPair = data.get(i);
                    dos.writeUTF(sPair.word);
                    dos.writeUTF(sPair.meaning);
                }

                dos.flush();
            }
            catch(IOException e)
            {
                e.printStackTrace();
            }
        }


        public void AddData(SPair sPair)
        {
            poser.add(data.size());

            WordsItem wordsItem = new WordsItem(MainActivity.this,
                    activityMainBinding.buttonContainer, sPair.word, sPair.meaning,
                    data.size());

            data.add(sPair);
        }

        public void RemoveData(WordsItem wordsItem)
        {
            int dataPos = poser.get(wordsItem.pos);
            data.remove(dataPos);
            for(int i = 0; i < poser.size(); i++)
            {
                int currPos = poser.get(i);
                if(currPos > dataPos)
                {
                    poser.set(i, currPos - 1);
                }
            }

            poser.remove(wordsItem.pos);

            // 추후 최적화 필요해보이지만, 우선은 리셋하고 다시 진열
            DisplayAll();
        }

        public void ReviseData(SPair revisedPair, WordsItem wordsItem)
        {
            int dataPos = poser.get(wordsItem.pos);
            data.set(dataPos, revisedPair);

            wordsItem.word = revisedPair.word;
            wordsItem.meaning = revisedPair.meaning;

            wordsItem.wordsBinding.word.setText(revisedPair.word);
            wordsItem.wordsBinding.meaning.setText(revisedPair.meaning);
        }

        public void ShakePos()
        {
            Random rand = new Random();
            int bound = poser.size();

            if(bound <= 0) return;

            for(int i = 0; i < bound; i++)
            {
                int shakePos = rand.nextInt(bound);
                int first = poser.get(i);
                int second = poser.get(shakePos);

                poser.set(i, second);
                poser.set(shakePos, first);
            }
        }

        public void DisplayAll()
        {
            activityMainBinding.buttonContainer.removeAllViews();

            for(int i = 0; i < poser.size(); i++)
            {
                int index = poser.get(i);
                SPair sPair = data.get(index);

                WordsItem wordsITem = new WordsItem(MainActivity.this,
                        activityMainBinding.buttonContainer, sPair.word, sPair.meaning,
                        i);
            }
        }
    }


































































































    static class DicDataManager
    {
        private static ArrayList<String> dicNames;
        private static DatasBinding datasBinding;
        private static String currDic = "";

        public static String Initialize(Context context)
        {
            dicNames = new ArrayList<String>();

            File directory = context.getFilesDir();
            File[] files = directory.listFiles();

            if(files != null)
            {
                for(File file : files)
                {
                    if(file.isFile())
                    {
                        String fileName = file.getName();
                        if(fileName.endsWith(".dic"))
                        {
                            fileName = fileName.substring(0, fileName.length() - 4);
                            dicNames.add(fileName);
                        }
                    }
                }
            }

            if(dicNames.size() > 0)
            {
                currDic = dicNames.get(0);
                return currDic;
            }
            return "";
        }

        public static void ShowXml(Context context, ViewGroup parent)
        {
            datasBinding = DatasBinding.inflate(LayoutInflater.from(context), parent,
                    false);
            AlertDialog datasDialog = new Builder(context)
                    .setView(datasBinding.getRoot()).create();
            datasDialog.show();

            datasBinding.textCurrentDataName.setText(currDic);
            datasBinding.buttonExit.setOnClickListener(new View.OnClickListener()
            {
                @Override
                public void onClick(View view)
                {
                    datasDialog.dismiss();
                }
            });


            for(String dicName : dicNames)
            {
                TextView textView = new TextView(context);
                textView.setText(dicName);
                textView.setClickable(true);
                textView.setOnClickListener(new View.OnClickListener()
                {
                    @Override
                    public void onClick(View view)
                    {
                        ChangeCurrentDic(dicName);
                        datasDialog.dismiss();
                    }
                });

                textView.setOnLongClickListener(new View.OnLongClickListener()
                {
                    @Override
                    public boolean onLongClick(View view)
                    {
                        DataConfigureBinding configBinding = DataConfigureBinding.inflate(
                                LayoutInflater.from(context));
                        AlertDialog configDialog= new Builder(context)
                            .setView(configBinding.getRoot()).create();
                        configDialog.show();

                        configBinding.buttonCancel.setOnClickListener(new View.OnClickListener()
                        {
                            @Override
                            public void onClick(View view)
                            {
                                configDialog.dismiss();
                            }
                        });
                        configBinding.buttonDelete.setOnClickListener(new View.OnClickListener()
                        {
                            @Override
                            public void onClick(View view)
                            {
                                YesOrNoBinding ysBinding = YesOrNoBinding.inflate(
                                        LayoutInflater.from(context));
                                AlertDialog ysDialog = new Builder(context)
                                        .setView(ysBinding.getRoot()).create();
                                ysDialog.show();

                                ysBinding.textInform.setText("삭제하겠습니까?");
                                ysBinding.yes.setOnClickListener(new View.OnClickListener()
                                {
                                    @Override
                                    public void onClick(View view)
                                    {
                                        DeleteDic(dicName);
                                        ysDialog.dismiss();
                                        configDialog.dismiss();
                                    }
                                });
                                ysBinding.no.setOnClickListener(new View.OnClickListener()
                                {
                                    @Override
                                    public void onClick(View view)
                                    {
                                        ysDialog.dismiss();
                                    }
                                });
                            }
                        });
                        configBinding.buttonChageName.setOnClickListener(new View.OnClickListener()
                        {
                            @Override
                            public void onClick(View view)
                            {
                                ChangeStringBinding changeStringBinding = ChangeStringBinding.inflate(
                                        LayoutInflater.from(context));
                                Builder changeStringBuilder = new Builder(context);
                                changeStringBuilder.setView(changeStringBinding.getRoot());
                                AlertDialog changeStringDialog = changeStringBuilder.create();
                                changeStringDialog.show();

                                changeStringBinding.editText.setText(dicName);
                                changeStringBinding.buttonOk.setOnClickListener(new View.OnClickListener()
                                {
                                    @Override
                                    public void onClick(View view)
                                    {
                                        ChangeDicName(dicName);
                                        changeStringDialog.dismiss();
                                    }
                                });
                                changeStringBinding.buttonCancel.setOnClickListener(new View.OnClickListener()
                                {
                                    @Override
                                    public void onClick(View view)
                                    {
                                        changeStringDialog.dismiss();
                                    }
                                });


                            }
                        });

                        return true;
                    }
                });

                datasBinding.nameContainer.addView(textView);
            }
        }



        public static void ChangeCurrentDic(String dicName)
        {
            // TODO
        }

        public static void ChangeDicName(String dicName)
        {
            // TODO
        }

        public static void DeleteDic(String dicName)
        {
            // TODO
        }


    }


    public void MakeNewDic(String dicName)
    {
        DicData newDic = new DicData(dicName);
        dicData = newDic;
        dicData.SaveData();

        
    }
}
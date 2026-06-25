package com.example.a33_plus_dictionary;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.Button;

import com.example.a33_plus_dictionary.databinding.DicReposBinding;
import com.example.a33_plus_dictionary.databinding.MainInterfaceBinding;
import com.example.a33_plus_dictionary.databinding.OneStringBinding;
import com.example.a33_plus_dictionary.databinding.TwoStringBinding;
import com.example.a33_plus_dictionary.databinding.WordBinding;
import com.example.a33_plus_dictionary.databinding.WordLongClickBinding;

import androidx.appcompat.app.AlertDialog;

import java.util.ArrayList;
import java.util.Random;

public class MainInterface
{
    private MainInterfaceBinding mainBinding;
    private DicData dicData;

    private enum ShowState
    {
        HideMeaning,
        HideWord
    };
    private ShowState showState = ShowState.HideMeaning;


    public void Initialize(Context context)
    {
        mainBinding = MainInterfaceBinding.inflate(LayoutInflater.from(context));


        dicData = DicRepository.Instance().Initialize(context);


        mainBinding.textViewDicName.setText(dicData.dicName);
        mainBinding.buttonAdd.setOnClickListener(this::SetAddButton);
        mainBinding.buttonShake.setOnClickListener(this::OnShakeButtonClicked);
        mainBinding.buttonShowDictionaries.setOnClickListener(v->
        {
            OnShowDictionariesButtonClicked(v.getContext());
        });

        Interface_CleanAndShowAllWords(context);
    }

    public View GetRootView()
    {
        if(mainBinding == null) return null;

        return mainBinding.getRoot();
    }








    private void SetAddButton(View view)
    {
        Context context = view.getContext();
        TwoStringBinding binding = TwoStringBinding.inflate(LayoutInflater.from(context));
        AlertDialog dialog = new AlertDialog.Builder(context)
                .setView(binding.getRoot()).create();
        dialog.show();

        binding.buttonOk.setOnClickListener(v->
        {
            SPair sPair = new SPair(binding.editTextWord.getText().toString(),
                    binding.editTextMeaning.getText().toString());
            AddWord(context, sPair);
            dialog.dismiss();
        });
        binding.buttonCancel.setOnClickListener(v->{ dialog.dismiss(); });
    }



    /*------------------------
               Add
    ------------------------*/
    private void AddWord(Context context, SPair sPair)
    {
        dicData.data.add(sPair);
        int pos = dicData.shakePos.size();
        dicData.shakePos.add(pos);

        Interface_AddWord(context, pos, sPair);

        // SaveData
        DicRepository.Instance().SaveDicData(dicData);
    }

    private void Interface_CleanAndShowAllWords(Context context)
    {
        mainBinding.wordsLine.removeAllViews();

        for(int i = 0; i < dicData.data.size(); i++)
        {
            int pos = dicData.shakePos.get(i);
            Interface_AddWord(context, i, dicData.data.get(pos));
        }
    }


    private void Interface_AddWord(Context context, int pos, SPair pair)
    {
        WordBinding wordBinding = WordBinding.inflate(LayoutInflater.from(context));

        wordBinding.textPos.setText(String.valueOf(pos + 1));

        String first;
        String second;
        if(showState == ShowState.HideMeaning)
        {
          first = pair.word;
          second = pair.meaning;
        }
        else
        {
            first = pair.meaning;
            second = pair.word;
        }
        wordBinding.textFirst.setText(first);
        wordBinding.textSecond.setText(second);

        wordBinding.getRoot().setOnClickListener(v-> {
            SetOnWordClicked(v, wordBinding, second);});
        wordBinding.getRoot().setOnLongClickListener(v->
        {
            SetOnWordLongClicked(v, wordBinding, pos, pair);
            return true;
        });


        mainBinding.wordsLine.addView(wordBinding.getRoot());
    }


    private void SetOnWordClicked(View wordView, WordBinding wordBinding, String second)
    {
        if(wordBinding.textSecond.getText() == "")
        {
            wordBinding.textSecond.setText(second);
        }
        else
        {
            wordBinding.textSecond.setText("");
        }
    }


    private void SetOnWordLongClicked(View wordView, WordBinding wordBinding, int pos, SPair pair)
    {
        Context context = wordView.getContext();
        WordLongClickBinding binding = WordLongClickBinding.inflate(LayoutInflater.from(context));
        AlertDialog dialog = new AlertDialog.Builder(context).setView(binding.getRoot())
                .create();
        dialog.show();

        binding.textWord.setText(pair.word);
        binding.buttonCancel.setOnClickListener(v->{ dialog.dismiss();});
        binding.buttonEdit.setOnClickListener(v-> {
            SetOnWordEditButtonClicked(wordView, wordBinding, pos, pair);
            dialog.dismiss();
        });
        binding.buttonDelete.setOnClickListener(v->
        {
            OnDeleteButtonClicked(wordView, pos);
            dialog.dismiss();
        });
    }

    private void SetOnWordEditButtonClicked(View wordView, WordBinding wordBinding, int pos,
                                            SPair pair)
    {
        Context context = wordView.getContext();
        TwoStringBinding binding = TwoStringBinding.inflate(LayoutInflater.from(context));
        AlertDialog dialog = new AlertDialog.Builder(context).setView(binding.getRoot())
                .create();
        dialog.show();

        binding.editTextWord.setText(pair.word);
        binding.editTextMeaning.setText(pair.meaning);
        binding.buttonCancel.setOnClickListener(v->{ dialog.dismiss();});
        binding.buttonOk.setOnClickListener(v->
        {
            SPair newPair = new SPair(binding.editTextWord.getText().toString(),
                    binding.editTextMeaning.getText().toString());
            EditWord(wordView, wordBinding, pos, newPair);
            dialog.dismiss();
        });
    }



    private void EditWord(View wordView, WordBinding wordBinding, int pos, SPair pair)
    {
        int index = dicData.shakePos.get(pos);
        dicData.data.set(index, pair);

        Interface_EditWord(wordView, wordBinding, pos, pair);

        // SaveData
        DicRepository.Instance().SaveDicData(dicData);
    }
    private void Interface_EditWord(View wordView, WordBinding wordBinding, int pos, SPair pair)
    {
        String first;
        String second;
        if(showState == ShowState.HideMeaning)
        {
            first = pair.word;
            second = pair.meaning;
        }
        else
        {
            first = pair.meaning;
            second = pair.word;
        }

        wordBinding.textFirst.setText(first);
        wordBinding.textSecond.setText(second);
        wordBinding.getRoot().setOnClickListener(v->
        {
            SetOnWordClicked(wordView, wordBinding, second);
        });
        wordBinding.getRoot().setOnLongClickListener(v->
        {
            SetOnWordLongClicked(wordView, wordBinding, pos, pair);
            return true;
        });
    }


    private void OnShakeButtonClicked(View view)
    {
        int bound = dicData.shakePos.size();
        if(bound <= 0) return;

        Random random = new Random();

        for(int i = 0; i < bound; i++)
        {
            int shakePos = random.nextInt(bound);
            int first = dicData.shakePos.get(i);
            int second = dicData.shakePos.get(shakePos);

            dicData.shakePos.set(i, second);
            dicData.shakePos.set(shakePos, first);
        }

        Interface_CleanAndShowAllWords(view.getContext());
    }


    private void OnDeleteButtonClicked(View wordView, int pos)
    {
        // Address DicData
        int index = dicData.shakePos.get(pos);
        dicData.data.remove(index);
        for(int i = 0; i < dicData.shakePos.size(); i++)
        {
            int currIndex = dicData.shakePos.get(i);
            if(currIndex > index)
            {
                dicData.shakePos.set(i, currIndex - 1);
            }
        }

        dicData.shakePos.remove(pos);

        // Address Interface
        mainBinding.wordsLine.removeView(wordView);
        for(int i = pos; i < mainBinding.wordsLine.getChildCount(); i++)
        {
            View childView = mainBinding.wordsLine.getChildAt(i);
            WordBinding childBinding = WordBinding.bind(childView);
            childBinding.textPos.setText(String.valueOf(i + 1));
        }

        // SaveData
        DicRepository.Instance().SaveDicData(dicData);
    }






    private void OnShowDictionariesButtonClicked(Context context)
    {
        ArrayList<String> dicNames = DicRepository.Instance().GetDicNames();

        DicReposBinding binding = DicReposBinding.inflate(LayoutInflater.from(context));
        AlertDialog dialog = new AlertDialog.Builder(context).setView(binding.getRoot())
                .create();
        dialog.show();

        for(String dicName : dicNames)
        {
            Button dicButton = new Button(context);
            dicButton.setText(dicName);
            dicButton.setOnClickListener(v->
            {
                ChangeDic(v, dicName);
                dialog.dismiss();
            });
            binding.dicNames.addView(dicButton);
            dicButton.setOnLongClickListener(v->
            {
                OnDicLongClicked(v, dicName, dialog);
                return true;
            });

        }
        binding.buttonExit.setOnClickListener(v->{ dialog.dismiss();});
        binding.buttonAddDic.setOnClickListener(v->
        {
            OnMakeNewDicButtonClicked(v);
            dialog.dismiss();
        });
    }

    private void OnMakeNewDicButtonClicked(View view)
    {
        Context context = view.getContext();
        OneStringBinding binding = OneStringBinding.inflate(LayoutInflater.from(context));
        AlertDialog dialog = new AlertDialog.Builder(context).setView(binding.getRoot())
                .create();
        dialog.show();
        binding.buttonCancel.setOnClickListener(v->{ dialog.dismiss();});
        binding.buttonOk.setOnClickListener(v->
        {
            if(MakeNewDic(view, binding.editText.getText().toString()) == false)
            {
                DebugHelper.Instance().ShowInformInterface(context, null,
                        "새로운 사전 만들기 실패");
            }
            dialog.dismiss();
        });
    }

    private void ChangeDic(View view, String dicName)
    {
        if(dicName == dicData.dicName) return;
        Context context = view.getContext();


        dicData = DicRepository.Instance().LoadData(dicName);
        Interface_CleanAndShowAllWords(context);
        mainBinding.textViewDicName.setText(dicName);
    }

    private boolean MakeNewDic(View view, String dicName)
    {
        Context context = view.getContext();
        if(dicName == "") return false;

        ArrayList<String> dicNames = DicRepository.Instance().GetDicNames();
        for(String name : dicNames)
        {
            if(dicName == name) return false;
        }

        DicRepository.Instance().SaveDicData(dicData);

        dicData = new DicData(dicName, null);
        DicRepository.Instance().SaveDicData(dicData);

        Interface_CleanAndShowAllWords(context);
        mainBinding.textViewDicName.setText(dicData.dicName);
        return true;
    }


    private void OnDicLongClicked(View view, String dicName, AlertDialog dicReposDialog)
    {
        Context context = view.getContext();
        WordLongClickBinding binding = WordLongClickBinding.inflate(LayoutInflater.from(context));
        AlertDialog dialog = new AlertDialog.Builder(context).setView(binding.getRoot())
                .create();
        dialog.show();

        binding.textWord.setText(dicName);
        binding.buttonCancel.setOnClickListener(v->{dialog.dismiss();});
        binding.buttonEdit.setOnClickListener(v->
        {
            DebugHelper.Instance().ShowInformInterface(context, null, 
                    "아직 처리 못함");
            dialog.dismiss();
        });
        binding.buttonDelete.setOnClickListener(v->
        {
            OnDicDeleteButtonClicked(v, dicName, dicReposDialog);
            dialog.dismiss();
        });
    }
    private void OnDicDeleteButtonClicked(View view, String dicName,
                                          AlertDialog dicReposDialog)
    {
        Context context = view.getContext();
        boolean isCurrentDic = (dicName.equals(dicData.dicName));
        if(DicRepository.Instance().DeleteDic(dicName))
        {
            dicReposDialog.dismiss();
            OnShowDictionariesButtonClicked(view.getContext());

            if(isCurrentDic)
            {
                dicData = DicRepository.Instance().LoadAnyData();
                Interface_CleanAndShowAllWords(context);
                mainBinding.textViewDicName.setText(dicData.dicName);
            }
        }
    }

}

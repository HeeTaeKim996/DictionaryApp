package com.example.a33_plus_dictionary;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.view.inputmethod.InputMethodManager;
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

    private boolean isWordHide = false;

    public void Initialize(Context context)
    {
        mainBinding = MainInterfaceBinding.inflate(LayoutInflater.from(context));


        dicData = DicRepository.Instance().Initialize(context);


        mainBinding.textViewDicName.setText(dicData.dicName);
        mainBinding.buttonAdd.setOnClickListener(this::SetAddButton);
        mainBinding.buttonShake.setOnClickListener(this::OnShakeButtonClicked);
        mainBinding.buttonShowDictionaries.setOnClickListener(v ->
        {
            OnShowDictionariesButtonClicked(v.getContext());
        });
        mainBinding.buttonMode.setOnClickListener(v ->
        {
            OnModeChangeButtonClicked(v.getContext());
        });
        mainBinding.buttonHideAll.setOnClickListener(v ->
        {
            OnHideAllButtonClicked(v.getContext());
        });
        mainBinding.buttonShowAll.setOnClickListener(v ->
        {
            OnShowAllButtonClicked(v.getContext());
        });

        Interface_CleanAndShowAllWords(context);
    }

    public View GetRootView()
    {
        if (mainBinding == null) return null;

        return mainBinding.getRoot();
    }


    private void SetAddButton(View view)
    {
        Context context = view.getContext();
        TwoStringBinding binding = TwoStringBinding.inflate(LayoutInflater.from(context));
        AlertDialog dialog = new AlertDialog.Builder(context)
                .setView(binding.getRoot()).create();
        dialog.show();

        binding.editTextWord.requestFocus();
        Window window = dialog.getWindow();
        if(window != null)
        {
            window.setSoftInputMode(
                    WindowManager.LayoutParams.SOFT_INPUT_STATE_ALWAYS_VISIBLE);
        }


        binding.buttonOk.setOnClickListener(v ->
        {
            SPair sPair = new SPair(binding.editTextWord.getText().toString(),
                    binding.editTextMeaning.getText().toString());
            AddWord(context, sPair);
            dialog.dismiss();
        });
        binding.buttonCancel.setOnClickListener(v ->
        {
            dialog.dismiss();
        });
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

        for (int i = 0; i < dicData.data.size(); i++)
        {
            int index = dicData.shakePos.get(i);
            Interface_AddWord(context, i, dicData.data.get(index));
        }
    }


    private void Interface_AddWord(Context context, int pos, SPair pair)
    {
        WordBinding wordBinding = WordBinding.inflate(LayoutInflater.from(context));

        wordBinding.textPos.setText(String.valueOf(pos + 1));

        wordBinding.textFirst.setText(pair.word);
        wordBinding.textSecond.setText(pair.meaning);

        wordBinding.getRoot().setOnClickListener(v ->
        {
            SetOnWordClicked(v, wordBinding, pos);
        });
        wordBinding.getRoot().setOnLongClickListener(v ->
        {
            SetOnWordLongClicked(v, wordBinding, pos, pair);
            return true;
        });


        mainBinding.wordsLine.addView(wordBinding.getRoot());
    }


    private void SetOnWordClicked(View wordView, WordBinding wordBinding, int pos)
    {
        if(isWordHide == false)
        {
            if (wordBinding.textSecond.getText().equals(""))
            {
                ShowWord(wordBinding, pos);
            }
            else
            {
                HideWord(wordBinding);
            }
        }
        else
        {
            if(wordBinding.textFirst.getText().equals(""))
            {
                ShowWord(wordBinding, pos);
            }
            else
            {
                HideWord(wordBinding);
            }
        }
    }

    private void HideWord(WordBinding wordBinding)
    {
        if(isWordHide == false)
        {
            wordBinding.textSecond.setText("");
        }
        else
        {
            wordBinding.textFirst.setText("");
        }
    }

    private void ShowWord(WordBinding wordBinding, int pos)
    {
        SPair pair = dicData.data.get(dicData.shakePos.get(pos));
        if(isWordHide == false)
        {
            wordBinding.textSecond.setText(pair.meaning);
        }
        else
        {
            wordBinding.textFirst.setText(pair.word);
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
        binding.buttonCancel.setOnClickListener(v ->
        {
            dialog.dismiss();
        });
        binding.buttonEdit.setOnClickListener(v ->
        {
            SetOnWordEditButtonClicked(wordView, wordBinding, pos, pair);
            dialog.dismiss();
        });
        binding.buttonDelete.setOnClickListener(v ->
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
        binding.editTextWord.requestFocus();

        Window window = dialog.getWindow();
        if(window != null)
        {
            window.setSoftInputMode(
                    WindowManager.LayoutParams.SOFT_INPUT_STATE_ALWAYS_VISIBLE);
        }

        binding.editTextMeaning.setText(pair.meaning);
        binding.buttonCancel.setOnClickListener(v ->
        {
            dialog.dismiss();
        });
        binding.buttonOk.setOnClickListener(v ->
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
        wordBinding.textFirst.setText(pair.word);
        wordBinding.textSecond.setText(pair.meaning);
        wordBinding.getRoot().setOnClickListener(v ->
        {
            SetOnWordClicked(wordView, wordBinding, pos);
        });
        wordBinding.getRoot().setOnLongClickListener(v ->
        {
            SetOnWordLongClicked(wordView, wordBinding, pos, pair);
            return true;
        });
    }


    private void OnShakeButtonClicked(View view)
    {
        int bound = dicData.shakePos.size();
        if (bound <= 0) return;

        Random random = new Random();

        for (int i = 0; i < bound; i++)
        {
            int shakePos = random.nextInt(bound);
            int first = dicData.shakePos.get(i);
            int second = dicData.shakePos.get(shakePos);

            dicData.shakePos.set(i, second);
            dicData.shakePos.set(shakePos, first);
        }

        mainBinding.wordsLine.removeAllViews();
        Context context = view.getContext();
        for(int i = 0; i < dicData.data.size(); i++)
        {
            int index = dicData.shakePos.get(i);
            WordBinding wordBinding = WordBinding.inflate(LayoutInflater.from(context));

            SPair pair = dicData.data.get(index);
            wordBinding.textPos.setText(String.valueOf(i + 1));
            if(isWordHide == false)
            {
                wordBinding.textFirst.setText(pair.word);
                wordBinding.textSecond.setText("");
            }
            else
            {
                wordBinding.textFirst.setText("");
                wordBinding.textSecond.setText(pair.meaning);
            }

            int pos = i;
            wordBinding.getRoot().setOnClickListener(v->
            {
                SetOnWordClicked(v, wordBinding, pos);
            });
            wordBinding.getRoot().setOnLongClickListener(v->
            {
                SetOnWordLongClicked(v, wordBinding, pos, pair);
                return true;
            });

            mainBinding.wordsLine.addView(wordBinding.getRoot());
        }
    }


    private void OnDeleteButtonClicked(View wordView, int pos)
    {
        // Address DicData
        int index = dicData.shakePos.get(pos);
        dicData.data.remove(index);
        for (int i = 0; i < dicData.shakePos.size(); i++)
        {
            int currIndex = dicData.shakePos.get(i);
            if (currIndex > index)
            {
                dicData.shakePos.set(i, currIndex - 1);
            }
        }

        dicData.shakePos.remove(pos);

        // Address Interface
        mainBinding.wordsLine.removeView(wordView);
        for (int i = pos; i < mainBinding.wordsLine.getChildCount(); i++)
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

        for (String dicName : dicNames)
        {
            Button dicButton = new Button(context);
            dicButton.setAllCaps(false);
            dicButton.setText(dicName);
            dicButton.setOnClickListener(v ->
            {
                ChangeDic(v, dicName);
                dialog.dismiss();
            });
            binding.dicNames.addView(dicButton);
            dicButton.setOnLongClickListener(v ->
            {
                OnDicLongClicked(v, dicName, dialog);
                return true;
            });

        }
        binding.buttonExit.setOnClickListener(v ->
        {
            dialog.dismiss();
        });
        binding.buttonAddDic.setOnClickListener(v ->
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

        binding.editText.requestFocus();
        Window window = dialog.getWindow();
        if(window != null)
        {
            window.setSoftInputMode(
                    WindowManager.LayoutParams.SOFT_INPUT_STATE_ALWAYS_VISIBLE);
        }
        binding.buttonCancel.setOnClickListener(v ->
        {
            dialog.dismiss();
        });
        binding.buttonOk.setOnClickListener(v ->
        {
            if (MakeNewDic(view, binding.editText.getText().toString()) == false)
            {
                DebugHelper.Instance().ShowInformInterface(context, null,
                        "새로운 사전 만들기 실패");
            }
            dialog.dismiss();
        });
    }

    private void ChangeDic(View view, String dicName)
    {
        if (dicName.equals(dicData.dicName)) return;
        Context context = view.getContext();


        dicData = DicRepository.Instance().LoadData(dicName);

        SetModeDefault();
        Interface_CleanAndShowAllWords(context);
        mainBinding.textViewDicName.setText(dicName);
        DicRepository.Instance().UpdateCacheInfo_LastVisitedDicName(dicName);
    }

    private boolean MakeNewDic(View view, String dicName)
    {
        if (dicName.equals("")) return false;

        Context context = view.getContext();

        ArrayList<String> dicNames = DicRepository.Instance().GetDicNames();
        for (String name : dicNames)
        {
            if (dicName.equals(name)) return false;
        }

        DicRepository.Instance().SaveDicData(dicData);

        dicData = new DicData(dicName, null);
        DicRepository.Instance().SaveDicData(dicData);

        SetModeDefault();
        Interface_CleanAndShowAllWords(context);
        mainBinding.textViewDicName.setText(dicName);
        DicRepository.Instance().UpdateCacheInfo_LastVisitedDicName(dicName);
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
        binding.buttonCancel.setOnClickListener(v ->
        {
            dialog.dismiss();
        });
        binding.buttonDelete.setOnClickListener(v ->
        {
            OnDicDeleteButtonClicked(v, dicName, dicReposDialog);
            dialog.dismiss();
        });
        binding.buttonEdit.setOnClickListener(v ->
        {
            OnDicNameChangeButtonClicked(view, dicName, dicReposDialog);
            dialog.dismiss();
        });
    }

    private void OnDicDeleteButtonClicked(View view, String dicName,
                                          AlertDialog dicReposDialog)
    {
        Context context = view.getContext();
        boolean isCurrentDic = dicName.equals(dicData.dicName);
        if (DicRepository.Instance().DeleteDic(dicName))
        {
            dicReposDialog.dismiss();
            OnShowDictionariesButtonClicked(context);

            if (isCurrentDic)
            {
                dicData = DicRepository.Instance().LoadAnyData();
                Interface_CleanAndShowAllWords(context);
                mainBinding.textViewDicName.setText(dicData.dicName);
            }
        }
    }

    private void OnDicNameChangeButtonClicked(View view, String dickName,
                                              AlertDialog dicReposeDialog)
    {
        Context context = view.getContext();
        OneStringBinding binding = OneStringBinding.inflate(LayoutInflater.from(context));
        AlertDialog dialog = new AlertDialog.Builder(context).setView(binding.getRoot())
                .create();
        dialog.show();

        binding.editText.setText(dickName);
        binding.editText.requestFocus();
        Window window = dialog.getWindow();
        if(window != null)
        {
            window.setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_STATE_ALWAYS_VISIBLE);
        }

        binding.buttonOk.setOnClickListener(v ->
        {
            ChangeDicName(view, dickName, binding.editText.getText().toString(),
                    dicReposeDialog);
            dialog.dismiss();
        });
        binding.buttonCancel.setOnClickListener(v ->
        {
            dialog.dismiss();
        });
    }

    private void ChangeDicName(View view, String fromName, String toName,
                               AlertDialog dicReposeDialog)
    {
        if (toName.equals("")) return;

        Context context = view.getContext();
        boolean isCurrentDic = fromName.equals(dicData.dicName);
        if (DicRepository.Instance().ChangeDicName(fromName, toName))
        {
            dicReposeDialog.dismiss();
            OnShowDictionariesButtonClicked(context);

            if (isCurrentDic)
            {
                dicData.dicName = toName;
                Interface_CleanAndShowAllWords(context);
                mainBinding.textViewDicName.setText(toName);
                DicRepository.Instance().UpdateCacheInfo_LastVisitedDicName(toName);
            }
        }
    }


    private void OnModeChangeButtonClicked(Context context)
    {
        OnShowAllButtonClicked(context);
        isWordHide = !isWordHide;
        OnHideAllButtonClicked(context);

        if (isWordHide == false)
        {
            mainBinding.buttonMode.setText("단어 모드");
        } else
        {
            mainBinding.buttonMode.setText("뜻 모드");
        }
    }

    private void SetModeDefault()
    {
        isWordHide = false;
        mainBinding.buttonMode.setText("단어 모드");
    }

    private void OnHideAllButtonClicked(Context context)
    {
        int size = mainBinding.wordsLine.getChildCount();
        for (int i = 0; i < size; i++)
        {
            View child = mainBinding.wordsLine.getChildAt(i);
            WordBinding binding = WordBinding.bind(child);

            HideWord(binding);
        }
    }

    private void OnShowAllButtonClicked(Context context)
    {
        int size = mainBinding.wordsLine.getChildCount();
        for (int i = 0; i < size; i++)
        {
            View child = mainBinding.wordsLine.getChildAt(i);
            WordBinding binding = WordBinding.bind(child);

            ShowWord(binding, i);
        }
    }

}

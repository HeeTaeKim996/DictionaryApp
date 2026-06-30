package com.example.a33_plus_dictionary;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.LinearLayout;

import com.example.a33_plus_dictionary.databinding.DicReposBinding;
import com.example.a33_plus_dictionary.databinding.DicReposeAddDicBinding;
import com.example.a33_plus_dictionary.databinding.MainInterfaceBinding;
import com.example.a33_plus_dictionary.databinding.OneStringBinding;
import com.example.a33_plus_dictionary.databinding.OthersBinding;
import com.example.a33_plus_dictionary.databinding.OthersItemReviseBinding;
import com.example.a33_plus_dictionary.databinding.TwoStringBinding;
import com.example.a33_plus_dictionary.databinding.VerticalButtonsBinding;
import com.example.a33_plus_dictionary.databinding.WordBinding;
import com.example.a33_plus_dictionary.databinding.WordLongClickBinding;

import androidx.appcompat.app.AlertDialog;

import java.util.ArrayList;
import java.util.Random;
import java.util.function.Consumer;

public class MainInterface
{
    private MainInterfaceBinding mainBinding;
    private DicData dicData;

    private boolean isWordHide = false;

    public void Initialize(Context context)
    {
        mainBinding = MainInterfaceBinding.inflate(LayoutInflater.from(context));


        dicData = DicRepository.Instance().Initialize(context);


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
        mainBinding.buttonOthers.setOnClickListener(v ->
        {
            OnOthersButtonClicked(v.getContext());
        });
        mainBinding.buttonItem.setOnClickListener(v->
        {
            OnButtonItemClicked(context);
        });

        UpdateAllMainInterface(context);
    }

    public View GetRootView()
    {
        if (mainBinding == null) return null;

        return mainBinding.getRoot();
    }



    private void UpdateAllMainInterface(Context context)
    {
        SetModeDefault();
        Interface_CleanAndShowAllWords(context);
        mainBinding.textViewDicName.setText(dicData.dicInfo.dicName);
        DicRepository.Instance().UpdateCacheInfo(dicData.dicInfo.dicName);
        mainBinding.buttonItem.setText(dicData.dicInfo.item);
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
        if (window != null)
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





    private void AddWord(Context context, SPair sPair)
    {
        dicData.data.add(sPair);
        int pos = dicData.shakePos.size();
        dicData.shakePos.add(pos);

        Interface_AddWord(context, pos, sPair, false);

        // SaveData
        DicRepository.Instance().UpdateDicData(dicData);
    }

    private void Interface_CleanAndShowAllWords(Context context)
    {
        mainBinding.wordsLine.removeAllViews();

        for (int i = 0; i < dicData.data.size(); i++)
        {
            int index = dicData.shakePos.get(i);
            Interface_AddWord(context, i, dicData.data.get(index), false);
        }
    }




    private void Interface_AddWord(Context context, int pos, SPair pair,
                                   boolean bhideWord)
    {
        WordBinding wordBinding = WordBinding.inflate(LayoutInflater.from(context));

        wordBinding.textPos.setText(String.valueOf(pos + 1));


        wordBinding.textFirst.setText(pair.word);
        wordBinding.textSecond.setText(pair.meaning);


        if (bhideWord)
        {
            if (isWordHide == false)
            {
                wordBinding.textSecond.setVisibility(View.INVISIBLE);
            } else
            {
                wordBinding.textFirst.setVisibility(View.INVISIBLE);
            }
        }


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
        if (isWordHide == false)
        {
            if (wordBinding.textSecond.getVisibility() == View.INVISIBLE)
            {
                ShowWord(wordBinding);
            } else
            {
                HideWord(wordBinding);
            }
        } else
        {
            if (wordBinding.textFirst.getVisibility() == View.INVISIBLE)
            {
                ShowWord(wordBinding);
            } else
            {
                HideWord(wordBinding);
            }
        }
    }

    private void HideWord(WordBinding wordBinding)
    {
        if (isWordHide == false)
        {
            wordBinding.textSecond.setVisibility(View.INVISIBLE);
        } else
        {
            wordBinding.textFirst.setVisibility(View.INVISIBLE);
        }
    }

    private void ShowWord(WordBinding wordBinding)
    {
        if (isWordHide == false)
        {
            wordBinding.textSecond.setVisibility(View.VISIBLE);
        } else
        {
            wordBinding.textFirst.setVisibility(View.VISIBLE);
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
        if (window != null)
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
        DicRepository.Instance().UpdateDicData(dicData);
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
        for (int i = 0; i < dicData.data.size(); i++)
        {
            int index = dicData.shakePos.get(i);
            SPair pair = dicData.data.get(index);

            Interface_AddWord(context, i, pair, true);
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
        DicRepository.Instance().UpdateDicData(dicData);
    }


    private void OnShowDictionariesButtonClicked(Context context)
    {


        DicReposBinding binding = DicReposBinding.inflate(LayoutInflater.from(context));
        AlertDialog dialog = new AlertDialog.Builder(context).setView(binding.getRoot())
                .create();
        dialog.show();


        String currItemString = DicRepository.Instance().GetCurrItem();
        binding.buttonItem.setText(currItemString);

        UpdateDicRepos_DicNamesLayout(context, binding, dialog);

        binding.buttonExit.setOnClickListener(v ->
        {
            dialog.dismiss();
        });
        binding.buttonAddDic.setOnClickListener(v ->
        {
            OnMakeNewDicButtonClicked(v);
            dialog.dismiss();
        });


        binding.buttonItem.setOnClickListener(v->
        {
            OnDicReposItemButtonClicked(context, binding, dialog);
        });


    }

    private void UpdateDicRepos_DicNamesLayout(Context context, DicReposBinding dicReposBinding,
                                               AlertDialog dicReposDialog)
    {
        dicReposBinding.dicNames.removeAllViews();

        ArrayList<DicInfo> dicInfos = DicRepository.Instance().GetDicInfos();
        for (DicInfo dicInfo : dicInfos)
        {
            Button dicButton = new Button(context);
            dicButton.setAllCaps(false);
            dicButton.setText(dicInfo.dicName);
            dicButton.setOnClickListener(v ->
            {
                ChangeDic(v, dicInfo);
                dicReposDialog.dismiss();
            });
            dicReposBinding.dicNames.addView(dicButton);
            dicButton.setOnLongClickListener(v ->
            {
                OnDicLongClicked(v, dicInfo, dicReposDialog);
                return true;
            });

        }
    }


    private void OnMakeNewDicButtonClicked(View view)
    {
        Context context = view.getContext();
        DicReposeAddDicBinding binding = DicReposeAddDicBinding
                .inflate(LayoutInflater.from(context));
        AlertDialog dialog = new AlertDialog.Builder(context).setView(binding.getRoot())
                .create();
        dialog.show();

        binding.editText.requestFocus();
        Window window = dialog.getWindow();
        if (window != null)
        {
            window.setSoftInputMode(
                    WindowManager.LayoutParams.SOFT_INPUT_STATE_ALWAYS_VISIBLE);
        }

        binding.buttonItem.setText("None");
        binding.buttonItem.setOnClickListener(v->
        {
            OnMakeNewDicButtonClicked_SetItemButton(context, binding);
        });
        binding.buttonCancel.setOnClickListener(v ->
        {
            dialog.dismiss();
        });
        binding.buttonOk.setOnClickListener(v ->
        {
            if (MakeNewDic(view, binding.editText.getText().toString(),
                    binding.buttonItem.getText().toString())
                    == false)
            {
                DebugHelper.Instance().ShowInformInterface(context,
                        "새로운 사전 만들기 실패");
            }
            dialog.dismiss();
        });
    }
    private void OnMakeNewDicButtonClicked_SetItemButton(Context context,
                                                         DicReposeAddDicBinding addDicBinding)
    {
        VerticalButtonsBinding binding = VerticalButtonsBinding
                .inflate(LayoutInflater.from(context));
        AlertDialog dialog = new AlertDialog.Builder(context)
                .setView(binding.getRoot()).create();
        dialog.show();

        binding.buttonExit.setOnClickListener(v->{dialog.dismiss();});

        ArrayList<String> items = DicRepository.Instance().GetItems();
        for(String item : items)
        {
            Button button = new Button(context);

            button.setAllCaps(false);
            LinearLayout.LayoutParams params = new LinearLayout.LayoutParams(
                    LinearLayout.LayoutParams.WRAP_CONTENT,
                    LinearLayout.LayoutParams.WRAP_CONTENT
            );
            button.setLayoutParams(params);
            button.setText(item);

            button.setOnClickListener(v->
            {
                addDicBinding.buttonItem.setText(item);
                dialog.dismiss();
            });

            binding.layoutButtons.addView(button);
        }
    }

    // @@@ 추후 dicName 받지 말고, DicInfo 를 받아 처리하자
    private void ChangeDic(View view, DicInfo dicInfo)
    {
        if (dicInfo.dicName.equals(dicData.dicInfo.dicName)) return;
        Context context = view.getContext();


        dicData = DicRepository.Instance().LoadData(dicInfo);

        UpdateAllMainInterface(context);
    }

    private boolean MakeNewDic(View view, String dicName, String item)
    {
        if (dicName.equals("")) return false;

        Context context = view.getContext();


        DicData newData = DicRepository.Instance().MakeNewDic(dicName, item);
        if(newData == null) return false;

        DicRepository.Instance().UpdateDicData(dicData);

        dicData = newData;
        UpdateAllMainInterface(context);
        return true;
    }




    private void OnDicLongClicked(View view, DicInfo dicInfo, AlertDialog dicReposDialog)
    {
        Context context = view.getContext();
        WordLongClickBinding binding = WordLongClickBinding.inflate(LayoutInflater.from(context));
        AlertDialog dialog = new AlertDialog.Builder(context).setView(binding.getRoot())
                .create();
        dialog.show();

        binding.textWord.setText(dicInfo.dicName);
        binding.buttonCancel.setOnClickListener(v ->
        {
            dialog.dismiss();
        });
        binding.buttonDelete.setOnClickListener(v ->
        {
            OnDicDeleteButtonClicked(v, dicInfo, dicReposDialog);
            dialog.dismiss();
        });
        binding.buttonEdit.setOnClickListener(v ->
        {
            OnDicNameChangeButtonClicked(view, dicInfo, dicReposDialog);
            dialog.dismiss();
        });
    }

    private void OnDicDeleteButtonClicked(View view, DicInfo dicInfo,
                                          AlertDialog dicReposDialog)
    {
        Context context = view.getContext();
        boolean isCurrentDic = dicInfo.dicName.equals(dicData.dicInfo.dicName);
        if (DicRepository.Instance().DeleteDic(dicInfo))
        {
            dicReposDialog.dismiss();
            OnShowDictionariesButtonClicked(context);

            if (isCurrentDic)
            {
                dicData = DicRepository.Instance().LoadAnyData();
                UpdateAllMainInterface(context);
            }
        }
    }

    private void OnDicNameChangeButtonClicked(View view, DicInfo dicInfo,
                                              AlertDialog dicReposeDialog)
    {
        Context context = view.getContext();
        OneStringBinding binding = OneStringBinding.inflate(LayoutInflater.from(context));
        AlertDialog dialog = new AlertDialog.Builder(context).setView(binding.getRoot())
                .create();
        dialog.show();

        binding.editText.setText(dicInfo.dicName);
        binding.editText.requestFocus();
        Window window = dialog.getWindow();
        if (window != null)
        {
            window.setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_STATE_ALWAYS_VISIBLE);
        }

        binding.buttonOk.setOnClickListener(v ->
        {
            ChangeDicName(view, dicInfo, binding.editText.getText().toString(),
                    dicReposeDialog);
            dialog.dismiss();
        });
        binding.buttonCancel.setOnClickListener(v ->
        {
            dialog.dismiss();
        });
    }

    private void ChangeDicName(View view, DicInfo dicInfo, String toName,
                               AlertDialog dicReposeDialog)
    {
        if (toName.equals("")) return;

        Context context = view.getContext();
        boolean isCurrentDic = dicInfo.dicName.equals(dicData.dicInfo.dicName);
        if (DicRepository.Instance().ChangeDicName(dicInfo, toName))
        {
            dicReposeDialog.dismiss();
            OnShowDictionariesButtonClicked(context);

            if (isCurrentDic)
            {
                UpdateAllMainInterface(context);
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

            ShowWord(binding);
        }
    }


    private void OnOthersButtonClicked(Context context)
    {
        OthersBinding binding = OthersBinding.inflate(LayoutInflater.from(context));
        AlertDialog dialog = new AlertDialog.Builder(context)
                .setView(binding.getRoot()).create();
        dialog.show();

        binding.buttonExit.setOnClickListener(v ->
        {
            dialog.dismiss();
        });
        binding.buttonExportZip.setOnClickListener(v ->
        {
            DicRepository.Instance().ExportDataToZip(context);
            dialog.dismiss();
        });
        binding.buttonImportZip.setOnClickListener(v ->
        {
            Consumer<Boolean> consumer = (Boolean b) ->
            {
                if (b)
                {
                    Consumer<DicData> onImportFromZipCompleted = (DicData newDic) ->
                    {
                        if (newDic == null)
                        {
                            DebugHelper.Instance().ShowInformInterface(context,
                                    "데이터를 불러오는 데 실패했습니다.\n올바른 Zip 파일인지"
                                            + " 확인 바랍니다.");
                            return;
                        }
                        dicData = newDic;
                        UpdateAllMainInterface(context);
                    };

                    DicRepository.Instance().ImportFromZip(context, onImportFromZipCompleted);
                }
            };

            DebugHelper.Instance().ShowInformOKInterface(context,
                    "실행시 기존 데이터가 모두 삭제되고,\n zip 에서 추출한 데이터로 모두 대체됩니다"
                            + "\n진행하겠습니까?", consumer);

            dialog.dismiss();
        });
        binding.buttonEditItem.setOnClickListener(v->
        {
            OnOthers_EditItemtsButtonClicked(context);
            dialog.dismiss();
        });

    }


    private  void OnOthers_EditItemtsButtonClicked(Context context)
    {
        VerticalButtonsBinding binding = VerticalButtonsBinding
                .inflate(LayoutInflater.from(context));
        AlertDialog dialog = new AlertDialog.Builder(context).setView(binding.getRoot())
                .create();
        dialog.show();

        binding.buttonExit.setText("아이템 추가");
        binding.buttonExit.setOnClickListener(v->
        {
            OnAddItemButtonClicked(context);
            dialog.dismiss();
        });

        ArrayList<String> items = DicRepository.Instance().GetItems();
        for(int i = 0; i < items.size(); i++)
        {
            int pos = i;
            String itemName = items.get(i);

            Button button = new Button(context);
            button.setAllCaps(false);
            LinearLayout.LayoutParams params = new LinearLayout.LayoutParams(
                    LinearLayout.LayoutParams.WRAP_CONTENT,
                    LinearLayout.LayoutParams.WRAP_CONTENT
            );
            button.setLayoutParams(params);
            button.setText(itemName);
            button.setOnLongClickListener(v->
            {
                OnItemButtonLongedClicked(context, itemName, binding, dialog);
                return true;
            });

            binding.layoutButtons.addView(button);
        }
    }

    private void OnAddItemButtonClicked(Context context)
    {
        OneStringBinding binding = OneStringBinding
                .inflate(LayoutInflater.from(context));
        AlertDialog dialog = new AlertDialog.Builder(context)
                .setView(binding.getRoot()).create();
        dialog.show();

        binding.buttonCancel.setOnClickListener(v->{ dialog.dismiss();});
        binding.buttonOk.setOnClickListener(v->
        {

            String editText = binding.editText.getText().toString();
            if(editText.equals("All") == false)
            {
                DicRepository.Instance().AddItem(editText);
            }
            else
            {
                DebugHelper.Instance().ShowInformInterface(context,
                        "All 이라는 이름으로 Item 을 추가할 수 없습니다");
            }
            dialog.dismiss();
        });
    }

    private void OnItemButtonLongedClicked(Context context, String itemName,
                                         VerticalButtonsBinding parentBinding,
                                         AlertDialog parentDialog)
    {
        OthersItemReviseBinding binding = OthersItemReviseBinding
                .inflate(LayoutInflater.from(context));
        AlertDialog dialog = new AlertDialog.Builder(context)
                .setView(binding.getRoot()).create();
        dialog.show();

        binding.editText.setText(itemName);

        binding.buttonCancel.setOnClickListener(v->{dialog.dismiss();});
        binding.buttonEdit.setOnClickListener(v->
        {
            String changedName = binding.editText.getText().toString();
            if(itemName.equals(changedName) == false)
            {
                boolean isCurrDicChanged = dicData.dicInfo.item.equals(itemName);
                if(DicRepository.Instance().ChangeItemName(itemName, changedName) == false)
                {
                    DebugHelper.Instance().ShowInformInterface(context,"실패");
                    return;
                }
                if(isCurrDicChanged)
                {
                    UpdateAllMainInterface(context);
                }
            }

            parentDialog.dismiss();
            dialog.dismiss();
        });
        binding.buttonDelete.setOnClickListener(v->
        {
            boolean isCurrDicChanged = dicData.dicInfo.item == itemName;
            DicRepository.Instance().DeleteItem(itemName);
            if(isCurrDicChanged)
            {
                UpdateAllMainInterface(context);
            }

            parentDialog.dismiss();
            dialog.dismiss();
        });
    }








    private  void OnButtonItemClicked(Context context)
    {
        ArrayList<String> items = DicRepository.Instance().GetItems();
        ShowChangeDicItemButtonInterface(context, items, dicData.dicInfo);
    }

    private void ShowChangeDicItemButtonInterface(Context context, ArrayList<String> buttonNames,
                                                  DicInfo dicInfo)
    {
        VerticalButtonsBinding binding = VerticalButtonsBinding
                .inflate(LayoutInflater.from(context));
        AlertDialog dialog = new AlertDialog.Builder(context)
                .setView(binding.getRoot()).create();
        dialog.show();

        binding.buttonExit.setOnClickListener(v->{ dialog.dismiss();});

        for(int i = 0; i < buttonNames.size(); i++)
        {
            int pos = i;

            Button button = new Button(context);
            button.setAllCaps(false);
            LinearLayout.LayoutParams params = new LinearLayout.LayoutParams(
                    LinearLayout.LayoutParams.WRAP_CONTENT,
                    LinearLayout.LayoutParams.WRAP_CONTENT
            );
            button.setLayoutParams(params);
            button.setText(buttonNames.get(i));
            button.setOnClickListener(v->
            {
                ChangeDicItem(context, dicInfo, buttonNames.get(pos));
                dialog.dismiss();
            });

            binding.layoutButtons.addView(button);
        }
    }

    private void ChangeDicItem(Context context, DicInfo dicInfo, String newItem)
    {
        if(dicData.dicInfo.equals(dicInfo) == false)    return;
        if(dicData.dicInfo.item.equals(newItem))        return;

        DicRepository.Instance().ChangeItem(dicInfo, newItem);
        mainBinding.buttonItem.setText(newItem);
    }

    private void OnDicReposItemButtonClicked(Context contex, DicReposBinding dicReposBinding,
                                             AlertDialog dicReposDialog)
    {
        VerticalButtonsBinding binding = VerticalButtonsBinding
                .inflate(LayoutInflater.from(contex));
        AlertDialog dialog = new AlertDialog.Builder(contex).setView(binding.getRoot())
                .create();
        dialog.show();

        binding.buttonExit.setOnClickListener(v->{dialog.dismiss();});

        ArrayList<String> items = DicRepository.Instance().GetItems();

        Consumer<String> AddButton = (String item) ->
        {
            Button button = new Button(contex);
            button.setAllCaps(false);
            LinearLayout.LayoutParams params = new LinearLayout.LayoutParams(
                    LinearLayout.LayoutParams.WRAP_CONTENT,
                    LinearLayout.LayoutParams.WRAP_CONTENT
            );
            button.setLayoutParams(params);
            button.setText(item);
            button.setOnClickListener(v->
            {
                ChangeCurrItemButtonClicked(contex, dicReposBinding, dicReposDialog, item);
                dialog.dismiss();
            });

            binding.layoutButtons.addView(button);
        };

        AddButton.accept("All");

        for(String item : items)
        {
            AddButton.accept(item);
        }
    }

    private void ChangeCurrItemButtonClicked(Context context, DicReposBinding dicReposBinding,
                                             AlertDialog dicReposDialog, String newItem)
    {
        if(newItem.equals(DicRepository.Instance().GetCurrItem())) return;

        DicRepository.Instance().ChangeCurrItem(newItem);
        dicReposBinding.buttonItem.setText(newItem);

        UpdateDicRepos_DicNamesLayout(context, dicReposBinding, dicReposDialog);
    }

}

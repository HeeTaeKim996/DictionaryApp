package com.example.a33_plus_dictionary;

import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.app.AlertDialog;

import android.content.Context;
import android.net.Uri;
import android.os.Bundle;
import android.provider.UserDictionary;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;

import com.example.a33_plus_dictionary.databinding.ActivityMainBinding;
import com.example.a33_plus_dictionary.databinding.AddWordBinding;
import com.example.a33_plus_dictionary.databinding.EditBinding;
import com.example.a33_plus_dictionary.databinding.ReviseWordBinding;
import com.example.a33_plus_dictionary.databinding.WordsBinding;

import androidx.appcompat.app.AlertDialog.Builder;

public class MainActivity extends AppCompatActivity
{
    ActivityMainBinding activityMainBinding;

    int tempInt = 0;

    @Override
    protected void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);

        activityMainBinding = ActivityMainBinding.inflate(getLayoutInflater());
        setContentView(activityMainBinding.getRoot());

        OnAddButtonClicked onAddButtonClicked = new OnAddButtonClicked();
        activityMainBinding.addButton.setOnClickListener(onAddButtonClicked);
    }

    class OnAddButtonClicked implements View.OnClickListener
    {
        @Override
        public void onClick(View view)
        {
            AddWordBinding addWordBinding = AddWordBinding.inflate(getLayoutInflater());
            Builder builder = new Builder(MainActivity.this);
            builder.setView(addWordBinding.getRoot());

            AlertDialog dialog = builder.create();
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

                    WordsItem wordsItem = new WordsItem(MainActivity.this,
                            activityMainBinding.buttonContainer, word, meaning);

                    dialog.dismiss();
                }
            });
        }
    }

    class WordsItem
    {
        private WordsBinding wordsBinding;
        private boolean bOn = true;
        private String word;
        private String meaning;

        public WordsItem(Context context, ViewGroup parent, String word, String meaning)
        {
            wordsBinding = WordsBinding.inflate(LayoutInflater.from(context), parent, false);

            wordsBinding.word.setText(word);
            wordsBinding.meaning.setText(meaning);

            this.word = word;
            this.meaning = meaning;


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
                    Builder builder = new Builder(MainActivity.this);
                    builder.setView(editBinding.getRoot());

                    AlertDialog dialog = builder.create();
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
                            parent.removeView(wordsBinding.getRoot());
                            dialog.dismiss();
                        }
                    });

                    editBinding.buttonRevise.setOnClickListener(new View.OnClickListener()
                    {
                        @Override
                        public void onClick(View view)
                        {
                            ReviseWordBinding rwBinding = ReviseWordBinding.inflate(getLayoutInflater());
                            Builder rwBuilder = new Builder(MainActivity.this);
                            rwBuilder.setView(rwBinding.getRoot());

                            AlertDialog rwDialog = rwBuilder.create();
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
                                    WordsItem.this.word = rwBinding.wordText.getText().toString();
                                    WordsItem.this.meaning = rwBinding.meaningText.getText().toString();

                                    wordsBinding.word.setText(WordsItem.this.word);
                                    wordsBinding.meaning.setText(WordsItem.this.meaning);

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




}
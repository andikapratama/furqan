package com.pratamalabs.furqan;

import android.app.AlertDialog;
import android.app.Dialog;
import android.content.DialogInterface;
import android.os.Bundle;
import android.support.v4.app.DialogFragment;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import com.pratamalabs.furqan.events.GoToEvent;
import com.pratamalabs.furqan.models.Surah;
import com.pratamalabs.furqan.repository.FurqanDao;
import com.pratamalabs.furqan.services.EventBus;

import org.androidannotations.annotations.Bean;
import org.androidannotations.annotations.EFragment;
import org.apache.commons.lang3.StringUtils;

import butterknife.ButterKnife;

/**
 * Created by KatulSomin on 28/09/2014.
 */
@EFragment
public class GoToDialog extends DialogFragment {

    @Bean
    FurqanDao dao;

    @Bean
    EventBus bus;

    Surah mSurah;
    EditText editTextVerse;
    EditText editTextSurah;
    TextView textViewSurahName;
    boolean clearing = false;

    public GoToDialog() {
        // Empty constructor required for DialogFragment
    }

    public static GoToDialog newInstance(int surahNo, int verseNo) {
        GoToDialog frag = new GoToDialog_();
        Bundle args = new Bundle();
        args.putInt(Constants.SURAH_NUMBER, surahNo);
        args.putInt(Constants.VERSE_NUMBER, verseNo);
        frag.setArguments(args);
        return frag;
    }

    public void clear() {
        clearing = true;
        textViewSurahName.setText("Pick a surah first..");
        editTextSurah.setText("");
        editTextVerse.setText("");
        editTextVerse.setEnabled(false);
        clearing = false;
    }

    @Override
    public Dialog onCreateDialog(Bundle savedInstanceState) {
        int surahNo = getArguments().getInt(Constants.SURAH_NUMBER) - 1;
        AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
        // Get the layout inflater, and inflate the dialog's view (which contains the EditText)
        LayoutInflater inflater = getActivity().getLayoutInflater();
        View dialogView = inflater.inflate(R.layout.number_dialog, null);

        // Keep a reference to the EditText you can use when the user clicks the button
        editTextVerse = ButterKnife.findById(dialogView, R.id.editTextVerse);
        editTextSurah = ButterKnife.findById(dialogView, R.id.editTextSurah);
        textViewSurahName = ButterKnife.findById(dialogView, R.id.surahName);
        textViewSurahName.setTextColor(editTextSurah.getHintTextColors());

        if (surahNo < 0) {
            editTextSurah.requestFocus();
            editTextVerse.setEnabled(false);
        } else {
            mSurah = dao.getAllSurah().get(surahNo);
            textViewSurahName.setText(mSurah.getName());
            editTextVerse.requestFocus();
            editTextSurah.setText(String.valueOf(mSurah.getNo()));
        }

        editTextSurah.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {

            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {

            }

            @Override
            public void afterTextChanged(Editable s) {
                if (clearing) return;
                String text = s.toString();

                if (StringUtils.isBlank(text)) {
                    clear();
                    return;
                }

                int no = 0;
                try {
                    no = Integer.parseInt(text);
                    if (no > 114) {
                        editTextSurah.setText("114");
                        no = 114;
                    } else if (no < 0) {
                        no = 1;
                        editTextSurah.setText("1");
                    }

                    no = no - 1;
                    mSurah = dao.getAllSurah().get(no);
                    textViewSurahName.setText(mSurah.getName());
                    editTextVerse.setEnabled(true);
                } catch (Exception e) {
                    clear();
                }
            }
        });
        editTextVerse.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {

            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {

            }

            @Override
            public void afterTextChanged(Editable s) {
                if (clearing) return;
                String text = s.toString();
                if (StringUtils.isBlank(text)) {
                    return;
                } else {
                    int no = Integer.parseInt(text) - 1;
                    if (mSurah.getVerseCount() <= no) {
                        editTextVerse.setText(String.valueOf(mSurah.getVerseCount()));
                    } else if (no < 0) {
                        editTextVerse.setText("1");
                    }
                }
            }
        });

        // Inflate and set the layout for the dialog
        builder.setView(dialogView)
                .setTitle("Jump to..")
                        // Add action buttons
                .setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {

                    }
                })
                .setPositiveButton(R.string.go, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int id) {
                        // The user clicked 'ok' or 'sign-in' - now you can get the text from the EditText
                        String verseText = editTextVerse.getText().toString();
                        String surahText = editTextSurah.getText().toString();
                        int surahNo = -1;
                        int verseNo = -1;
                        try {
                            surahNo = Integer.parseInt(surahText);
                            verseNo = Integer.parseInt(verseText);
                            bus.post(new GoToEvent(surahNo, verseNo));
                        } catch (Exception e) {
                            Toast.makeText(getActivity(), "The destination is invalid", Toast.LENGTH_LONG);
                        }
                    }
                });
        // Build & show the dialog
        return builder.create();
    }
}

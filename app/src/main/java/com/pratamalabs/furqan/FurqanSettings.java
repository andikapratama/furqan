package com.pratamalabs.furqan;

import android.app.AlertDialog;
import android.app.Dialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.SharedPreferences;
import android.graphics.Typeface;
import android.preference.PreferenceManager;
import android.util.Pair;

import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;
import com.pratamalabs.furqan.models.Recitation;
import com.pratamalabs.furqan.models.Translation;
import com.pratamalabs.furqan.repository.FurqanDao;
import com.pratamalabs.furqan.services.Utils;

import org.androidannotations.annotations.AfterInject;
import org.androidannotations.annotations.Background;
import org.androidannotations.annotations.Bean;
import org.androidannotations.annotations.EBean;
import org.androidannotations.annotations.RootContext;

import java.io.IOException;
import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.List;

/**
 * Created by pratamalabs on 14/6/13.
 */

@EBean(scope = EBean.Scope.Singleton)
public class FurqanSettings {

    public static final String ARABIC_TYPEFACE = "arabic_typeface";
    public static final String TEXT_SIZE = "text_size";
    public static final String ARABIC_SIZE = "arabic_size";
    @Bean
    FurqanDao dao;

    @RootContext
    Context context;
    String typefaceKeyCache;
    //    private Map<String, Typeface> typefaceChoice;
    Typeface typeface;
    private List<Translation> selectedTranslations = new ArrayList<Translation>();
    private List<Recitation> recitations;

    @Background
    @AfterInject
    public void init() {

        try {
            Gson gson = new Gson();
            Type listType = new TypeToken<ArrayList<Recitation>>() {
            }.getType();
            recitations = gson.fromJson(Utils.stringFromInputStream(context.getAssets().open("recitations.js")), listType);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public List<String> getAvailableRecitationsName() {
        List<String> recitationsText = new ArrayList<>();
        for (Recitation recitation : recitations) {
            recitationsText.add(recitation.getTitle());
        }
        return recitationsText;
    }

    public void showRecitationsDialog(Context context) {
        showRecitationsDialog(context, null);
    }

    public void showRecitationsDialog(final Context context, final DialogInterface.OnClickListener listener) {
        List<String> recitationsText = getAvailableRecitationsName();
        Dialog dialog = new AlertDialog.Builder(context)
                .setTitle("Recitations")
                .setSingleChoiceItems(recitationsText.toArray(new String[recitationsText.size()]), PreferenceManager.getDefaultSharedPreferences(context).getInt("recitation", 0), new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        PreferenceManager.getDefaultSharedPreferences(context).edit().putInt("recitation", which).commit();
                        if (listener != null) {
                            listener.onClick(dialog, which);
                        }
                    }
                })
                .setPositiveButton("Ok", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {

                    }
                }).create();
        dialog.show();
    }

    public Recitation getSelectedRecitation() {
        return recitations.get(PreferenceManager.getDefaultSharedPreferences(context).getInt("recitation", 0));
    }

    public float getArabicTextSize() {
        return Float.valueOf(PreferenceManager.getDefaultSharedPreferences(context).getString(ARABIC_SIZE, "36"));
    }

    public void setGlobalLastRead(int surahNo, int verseNo) {
        SharedPreferences.Editor editor = PreferenceManager.getDefaultSharedPreferences(context).edit();
        editor.putInt("lastReadVerseNo", verseNo);
        editor.putInt("lastReadSurahNo", surahNo);
        editor.apply();
    }

    public void setSurahLastRead(int surahNo, int verseNo) {
        SharedPreferences.Editor editor = PreferenceManager.getDefaultSharedPreferences(context).edit();
        editor.putInt("lastRead" + String.valueOf(surahNo), verseNo);
        editor.apply();
    }

    public int getSurahLastRead(int surahNo) {
        SharedPreferences sharedPreferences = PreferenceManager.getDefaultSharedPreferences(context);
        int verseNo = sharedPreferences.getInt("lastRead" + String.valueOf(surahNo), 1);
        return verseNo;
    }

    public Pair<Integer, Integer> getGlobalLastRead() {
        SharedPreferences sharedPreferences = PreferenceManager.getDefaultSharedPreferences(context);
        int verseNo = sharedPreferences.getInt("lastReadVerseNo", 1);
        int surahNo = sharedPreferences.getInt("lastReadSurahNo", 1);
        return Pair.create(surahNo, verseNo);
    }

    public void setSurahTag(int key, String tag) {
        PreferenceManager.getDefaultSharedPreferences(context.getApplicationContext()).edit().putString(String.valueOf(key), tag).apply();
    }

    public String getSurahTag(int key) {
        return PreferenceManager.getDefaultSharedPreferences(context.getApplicationContext()).getString(String.valueOf(key), "");
    }

    public void setTranslationFold(String key, boolean visible) {
        PreferenceManager.getDefaultSharedPreferences(context.getApplicationContext()).edit().putBoolean(key, visible).apply();
    }

    public void setTranslationFold(SharedPreferences preference, String key, boolean visible) {
        preference.edit().putBoolean(key, visible).apply();
    }

    public boolean getTranslationFold(String key) {
        return PreferenceManager.getDefaultSharedPreferences(context.getApplicationContext()).getBoolean(key, false);
    }

    public float getTextSize() {
        return Float.valueOf(PreferenceManager.getDefaultSharedPreferences(context).getString(TEXT_SIZE, "16"));
    }

    public Typeface getArabicTypeface() {
        String typefaceKey = PreferenceManager.getDefaultSharedPreferences(context).getString(ARABIC_TYPEFACE, "ScheherazadeRegOT.ttf");
        if (!typefaceKey.equals(typefaceKeyCache)) {
            //this upgrades the old version which uses a different key
            if (!typefaceKey.endsWith(".ttf")) {
                //default is scheherazade.
                typefaceKey = "ScheherazadeRegOT.ttf";
            }
            typeface = Typeface.createFromAsset(context.getAssets(), typefaceKey);
        }
        return typeface;
    }

    public void refreshTranslations() {
        selectedTranslations = dao.getAvailableTranslations();
    }

    public synchronized List<Translation> getSelectedTranslations() {
        if (selectedTranslations.size() == 0) {
            refreshTranslations();
        }
        return selectedTranslations;
    }
}

package com.pratamalabs.furqan.models;

import java.util.ArrayList;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

/**
 * Created by pratamalabs on 9/6/13.
 */
public class Verse {
    final Map<Translation, String> translations;
    final String arabicText;
    final int number;
    final int surahNo;

    String note = "";

    private List<Translation> translationItems;

    public Verse(int surahNo, int no, String arabicText, String note) {
        this.number = no;
        this.surahNo = surahNo;
        translations = new LinkedHashMap();
        this.arabicText = arabicText;
        this.note = note;
    }

    ;

    public List<Translation> getTranslationItems() {
        if (translationItems == null) {
            translationItems = new ArrayList<Translation>(translations.keySet());
            Collections.sort(translationItems);
        }
        return translationItems;
    }

    public int getSurahNo() {
        return surahNo;
    }

    public Map<Translation, String> getTranslations() {
        return translations;
    }

    public int getNumber() {
        return number;
    }

    public String getArabicText() {
        return arabicText;
    }

    public String getNote() {
        return note == null ? "" : note;
    }

    public void setNote(String note) {
        this.note = note;
    }
}

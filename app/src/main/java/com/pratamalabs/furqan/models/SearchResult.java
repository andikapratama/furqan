package com.pratamalabs.furqan.models;

/**
 * Created by pratamalabs on 1/10/13.
 */
public class SearchResult {

    final String text;
    final String translationName;
    final int number;
    final int surahNo;

    public SearchResult(String text, int number, int surahNo, String translationName) {
        this.text = text;
        this.number = number;
        this.surahNo = surahNo;
        this.translationName = translationName;
    }

    public String getText() {
        return text;
    }

    public int getNumber() {
        return number;
    }

    public int getSurahNo() {
        return surahNo;
    }

    public String getTranslationName() {
        return translationName;
    }
}

package com.pratamalabs.furqan.models;

/**
 * Created by pratamalabs on 1/9/13.
 */
public class TranslationData {
    final public int id;
    final public int verseNo;
    final public int surahNo;
    final public String translation;

    public TranslationData(int id, int verseNo, int surahNo, String translation) {
        this.id = id;
        this.verseNo = verseNo;
        this.surahNo = surahNo;
        this.translation = translation;
    }
}

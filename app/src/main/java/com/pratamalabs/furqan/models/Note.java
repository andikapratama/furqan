package com.pratamalabs.furqan.models;

import org.apache.commons.lang3.StringUtils;

/**
 * Created by KatulSomin on 25/05/2014.
 */
public class Note implements FilterableItem {
    final int number;
    final Surah surah;
    final String note;

    public Note(int number, Surah surah, String note) {
        this.number = number;
        this.surah = surah;
        this.note = note;
    }

    public int getNumber() {
        return number;
    }

    public int getSurahNo() {
        return surah.getNo();
    }

    public String getSurahName() {
        return surah.getName();
    }

    public String getText() {
        return note;
    }


    @Override
    public boolean isFound(CharSequence keyword) {
        return StringUtils.containsIgnoreCase(note, keyword);
    }
}

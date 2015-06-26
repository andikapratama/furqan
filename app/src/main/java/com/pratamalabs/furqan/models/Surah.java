package com.pratamalabs.furqan.models;

import org.apache.commons.lang3.StringUtils;

/**
 * Created by pratamalabs on 9/6/13.
 */
public class Surah implements FilterableItem {
    final int no;
    final String name;
    final String translationName;
    final String arabicName;
    final String type;
    final int verseCount;

    public Surah(int no, String name, String translationName, String arabicName, String type, int verseCount) {
        this.no = no;
        this.name = name;
        this.translationName = translationName;
        this.arabicName = arabicName;
        this.type = type;
        this.verseCount = verseCount;
    }

    public String getTranslationName() {
        return translationName;
    }

    public int getNo() {
        return no;
    }

    public String getName() {
        return name;
    }

    public int getVerseCount() {
        return verseCount;
    }

    public String getArabicName() {
        return arabicName;
    }

    public String getType() {
        return type;
    }

    @Override
    public boolean isFound(CharSequence keyword) {
        return StringUtils.containsIgnoreCase(name, keyword) ||
                StringUtils.containsIgnoreCase(translationName, keyword) ||
                StringUtils.containsIgnoreCase(String.valueOf(no), keyword) ||
                StringUtils.containsIgnoreCase(arabicName, keyword);
    }
}

package com.pratamalabs.furqan.events;

/**
 * Created by KatulSomin on 01/09/2014.
 */
public class ShareVerseEvent {
    public final int verseNo;
    public final int surahNo;

    public ShareVerseEvent(int surahNo, int verseNo) {
        this.verseNo = verseNo;
        this.surahNo = surahNo;
    }
}

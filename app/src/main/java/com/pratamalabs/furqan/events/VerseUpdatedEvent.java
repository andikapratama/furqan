package com.pratamalabs.furqan.events;

/**
 * Created by KatulSomin on 08/09/2014.
 */
public class VerseUpdatedEvent {
    public final int surahNo;
    public final int verseNo;

    public VerseUpdatedEvent(int surahNo, int verseNo) {
        this.surahNo = surahNo;
        this.verseNo = verseNo;
    }

}

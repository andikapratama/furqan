package com.pratamalabs.furqan.events;

/**
 * Created by KatulSomin on 28/09/2014.
 */
public class GoToEvent {
    public final int surahNo;
    public final int verseNo;

    public GoToEvent(int surahNo, int verseNo) {
        this.surahNo = surahNo;
        this.verseNo = verseNo;
    }
}

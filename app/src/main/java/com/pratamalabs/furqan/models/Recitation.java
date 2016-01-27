package com.pratamalabs.furqan.models;

/**
 * Created by KatulSomin on 15/10/2014.
 */
public class Recitation {
    public final String subfolder;
    public final String name;
    public final String bitrate;
    public boolean downloaded;
    public int downloadedVerseCount;

    public Recitation(String subfolder, String name, String bitrate) {
        this.subfolder = subfolder;
        this.name = name;
        this.bitrate = bitrate;
    }

    public String getTitle() {
        return String.format("%s - %s", name, bitrate);
    }

    public Boolean isComplete() {
        return 6236 <= downloadedVerseCount;
    }

    public double downloadedPercentage() {
        return downloadedVerseCount / 6236.0;
    }
}

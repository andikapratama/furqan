package com.pratamalabs.furqan.models;

/**
 * Created by KatulSomin on 15/10/2014.
 */
public class Recitation {
    public final String subfolder;
    public final String name;
    public final String bitrate;

    public Recitation(String subfolder, String name, String bitrate) {
        this.subfolder = subfolder;
        this.name = name;
        this.bitrate = bitrate;
    }

    public String getTitle() {
        return String.format("%s - %s", name, bitrate);
    }
}

package com.pratamalabs.furqan.events;

/**
 * Created by KatulSomin on 15/10/2014.
 */
public class VersePlayerEvent {

    public final Type type;

    public VersePlayerEvent(Type type) {
        this.type = type;
    }

    public enum Type {Play, Stop, Error}
}

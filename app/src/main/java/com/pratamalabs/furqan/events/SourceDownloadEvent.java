package com.pratamalabs.furqan.events;

import com.pratamalabs.furqan.models.Source;

/**
 * Created by KatulSomin on 14/10/2014.
 */
public class SourceDownloadEvent {
    public final Source source;

    public SourceDownloadEvent(Source source) {
        this.source = source;
    }
}

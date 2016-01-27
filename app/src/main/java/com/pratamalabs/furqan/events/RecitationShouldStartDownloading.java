package com.pratamalabs.furqan.events;

import com.pratamalabs.furqan.models.Recitation;

/**
 * Created by andikapratama on 22/01/16.
 */
public class RecitationShouldStartDownloading {
    public final Recitation recitation;

    public RecitationShouldStartDownloading(Recitation recitation) {
        this.recitation = recitation;
    }
}

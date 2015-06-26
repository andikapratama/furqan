package com.pratamalabs.furqan;

import android.app.Application;
import android.view.ViewConfiguration;

import java.lang.reflect.Field;

/**
 * Created by pratamalabs on 13/6/13.
 */
public class FurqanApp extends Application {

    @Override
    public void onCreate() {
        super.onCreate();

        //hack to always show menu overflow
        try {
            ViewConfiguration config = ViewConfiguration.get(this);
            Field menuKeyField = ViewConfiguration.class.getDeclaredField("sHasPermanentMenuKey");

            if (menuKeyField != null) {
                menuKeyField.setAccessible(true);
                menuKeyField.setBoolean(config, false);
            }
        } catch (Exception e) {
            // presumably, not relevant
        }
    }
}


package com.pratamalabs.furqan.services;

import android.content.Context;
import android.content.res.Configuration;
import android.os.Environment;
import android.util.Log;
import android.view.inputmethod.InputMethodManager;
import android.widget.EditText;

import com.pratamalabs.furqan.FurqanApp;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;

/**
 * Created by pratamalabs on 31/7/13.
 */
public class Utils {

    static final SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss Z");

    static public Date dateFromString(String str) {
        Date date = null;
        try {
            date = dateFormat.parse(str);
        } catch (ParseException e) {
            e.printStackTrace();
        }
        return date;
    }

    static public void hideKeyboard(Context context, EditText editText) {
        InputMethodManager imm = (InputMethodManager) context.getSystemService(Context.INPUT_METHOD_SERVICE);
        imm.hideSoftInputFromWindow(editText.getWindowToken(), 0);
    }

    public static String stringFromInputStream(InputStream is) {
        BufferedReader r = new BufferedReader(new InputStreamReader(is));
        StringBuilder total;
        String line;
        try {
            total = new StringBuilder(is.available());
            while ((line = r.readLine()) != null) {
                total.append(line);
            }
        } catch (IOException e) {
            e.printStackTrace();
            return null;
        }
        return total.toString();
    }

    static public boolean writeToFile(String data) {
        File downloadDir = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS);
        File file = new File(downloadDir, "furqan-notes.fson");

        FileOutputStream out = null;
        try {
            out = new FileOutputStream(file);
            out.write(data.getBytes());
            out.flush();
            out.close();
            return true;
        } catch (IOException e) {
            Log.e("Exception", "File write failed: " + e.toString());
            return false;
        }
    }


    static public String readFromFile(Context context, String file) {

        String ret = "";

        try {
            InputStream inputStream = context.openFileInput(file);

            if (inputStream != null) {
                InputStreamReader inputStreamReader = new InputStreamReader(inputStream);
                BufferedReader bufferedReader = new BufferedReader(inputStreamReader);
                String receiveString;
                StringBuilder stringBuilder = new StringBuilder();

                while ((receiveString = bufferedReader.readLine()) != null) {
                    stringBuilder.append(receiveString);
                }

                inputStream.close();
                ret = stringBuilder.toString();
            }
        } catch (FileNotFoundException e) {
            Log.e("furqan", "File not found: " + e.toString());
        } catch (IOException e) {
            Log.e("furqan", "Can not read file: " + e.toString());
        }

        return ret;
    }

    static public String getVerseRecitationUrl(String reciter, int SurahNo, int verseNo) {

        File folder = new File(FurqanApp.instance.getExternalFilesDir(null), "/" + reciter);
        final File verse = new File(folder, String.format("%s%s.mp3", Utils.leadingZeros(SurahNo), Utils.leadingZeros(verseNo)));

        if (verse.exists()) {
            return verse.getAbsolutePath();
        }

        return String.format("http://www.everyayah.com/data/%s/%s%s.mp3",
                reciter,
                Utils.leadingZeros(SurahNo),
                Utils.leadingZeros(verseNo));
    }

    static public String leadingZeros(int number) {
        if (number < 10) {
            return String.format("00%d", number);
        } else if (number < 100) {
            return String.format("0%d", number);
        } else {
            return String.valueOf(number);
        }
    }

    static public boolean isLandscape(Context context) {
        return context.getResources().getConfiguration().orientation == Configuration.ORIENTATION_LANDSCAPE;
    }

}

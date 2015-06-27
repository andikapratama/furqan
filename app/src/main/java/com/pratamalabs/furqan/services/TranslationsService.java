package com.pratamalabs.furqan.services;

import android.content.ContentResolver;
import android.content.Context;
import android.net.Uri;
import android.text.TextUtils;
import android.widget.Toast;

import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;
import com.pratamalabs.furqan.models.Note;
import com.pratamalabs.furqan.models.TranslationData;
import com.pratamalabs.furqan.repository.FurqanDao;

import org.androidannotations.annotations.Bean;
import org.androidannotations.annotations.EBean;
import org.apache.commons.io.FileUtils;
import org.apache.commons.lang3.StringUtils;

import java.io.BufferedReader;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.List;

/**
 * Created by pratamalabs on 13/6/13.
 */
@EBean(scope = EBean.Scope.Singleton)
public class TranslationsService {

    @Bean
    FurqanDao dao;

    public boolean backupNotesToExternalDevice() {
        List<Note> notes = dao.getNotes();
        Gson gson = new Gson();
        String json = gson.toJson(notes);
        return Utils.writeToFile(json);
    }


    public void importNotes(Context context, Uri data) {
        final String scheme = data.getScheme();

        if (ContentResolver.SCHEME_FILE.equals(scheme)) {
            try {
                ContentResolver cr = context.getContentResolver();
                InputStream is = cr.openInputStream(data);
                if (is == null) return;

                BufferedReader reader = new BufferedReader(new InputStreamReader(is));

                Type listType = new TypeToken<ArrayList<Note>>() {
                }.getType();
                Gson gson = new Gson();
                List<Note> notes = gson.fromJson(reader, listType);
                importNotes(notes);
                Toast.makeText(context, "Notes succesfully imported!", Toast.LENGTH_LONG).show();

            } catch (Exception e) {
                e.printStackTrace();
                Toast.makeText(context, "File content invalid", Toast.LENGTH_LONG).show();
            }
        }
    }

    public void importNotes(List<Note> notes) {
        for (Note note : notes) {
            dao.setNote(note);
        }
    }

    public boolean saveTanzilTranslationResourceAndEnableIt(int id, File file) {
        long startTime = System.currentTimeMillis();


        boolean result = true;
        List<String> lines;
        try {
            lines = FileUtils.readLines(file);
        } catch (IOException e) {
            e.printStackTrace();
            result = false;
            return result;
        }

        List<TranslationData> datas = new ArrayList<>(lines.size());

        for (String line : lines) {
            if (line.isEmpty() || line.startsWith("#"))
                continue;

            String[] value = TextUtils.split(line, "\\|");
            if (value.length < 3)
                continue;
            String SuraNo = value[0];
            String verseNo = value[1];
            String translation = value[2];
            if (!StringUtils.isBlank(translation)) {
                datas.add(new TranslationData(id, Integer.valueOf(verseNo), Integer.valueOf(SuraNo), translation));
            }
        }

        dao.insertTranslationDataRecordBulk(datas);

        dao.updateSourceStatus(id, "enabled");
        long endTime = System.currentTimeMillis();

        System.out.println("That took " + (endTime - startTime) + " milliseconds");

        return result;
    }
}

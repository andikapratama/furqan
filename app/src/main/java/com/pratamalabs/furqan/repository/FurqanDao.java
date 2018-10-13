package com.pratamalabs.furqan.repository;

import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteStatement;

import com.crashlytics.android.Crashlytics;
import com.pratamalabs.furqan.FurqanApp;
import com.pratamalabs.furqan.models.Note;
import com.pratamalabs.furqan.models.SearchResult;
import com.pratamalabs.furqan.models.Source;
import com.pratamalabs.furqan.models.Surah;
import com.pratamalabs.furqan.models.Translation;
import com.pratamalabs.furqan.models.TranslationData;
import com.pratamalabs.furqan.models.Verse;

import org.androidannotations.annotations.AfterInject;
import org.androidannotations.annotations.EBean;
import org.apache.commons.lang3.StringUtils;

import java.sql.Date;
import java.util.ArrayList;
import java.util.List;

/**
 * Created by pratamalabs on 11/6/13.
 */


@EBean(scope = EBean.Scope.Singleton)
public class FurqanDao {

    public SQLiteDatabase mDatabase;



    public static FurqanDao get() {
        return FurqanDao_.getInstance_(FurqanApp.instance);
    }

    List<Surah> surahsCache;

    @AfterInject
    public void init() {
        FurqanDatabase mSQLite = FurqanDatabase.Companion.get();
        mDatabase = mSQLite.getWritableDatabase();
    }

    public synchronized List<SearchResult> searchNotesContaining(String query) {

        List<SearchResult> results = new ArrayList();
        Cursor cursor = mDatabase.rawQuery("SELECT SuraId,VerseId,Note FROM Notes WHERE Note LIKE '%' || ? || '%' ORDER BY SuraId,VerseId", new String[]{query});

        int suraId = cursor.getColumnIndex("SuraId");
        int verseId = cursor.getColumnIndex("VerseId");
        int verseText = cursor.getColumnIndex("Note");

        while (cursor.moveToNext()) {
            results.add(new SearchResult(
                    cursor.getString(verseText),
                    cursor.getInt(verseId),
                    cursor.getInt(suraId),
                    "Note"
            ));
        }
        cursor.close();

        return results;
    }

    public synchronized List<SearchResult> searchFurqanContaining(String query) {
        List<SearchResult> list = new ArrayList<SearchResult>();
        list.addAll(searchNotesContaining(query));
        list.addAll(searchTranslationsContaining(query));
        return list;
    }

    public synchronized List<SearchResult> searchTranslationsContaining(String query) {

        List<SearchResult> results = new ArrayList();
        Cursor cursor = mDatabase.rawQuery("SELECT td.SuraId,td.VerseId,td.VerseText,s.Name FROM TranslationData td LEFT JOIN Sources s on td.TranslationId = s.Id WHERE s.Status = 'enabled' AND td.VerseText LIKE '%' || ? || '%' ORDER BY td.SuraId,td.VerseId", new String[]{query});

        int suraId = cursor.getColumnIndex("SuraId");
        int verseId = cursor.getColumnIndex("VerseId");
        int verseText = cursor.getColumnIndex("VerseText");
        int name = cursor.getColumnIndex("Name");

        while (cursor.moveToNext()) {
            results.add(new SearchResult(
                    cursor.getString(verseText),
                    cursor.getInt(verseId),
                    cursor.getInt(suraId),
                    cursor.getString(name)
            ));
        }
        cursor.close();

        return results;
    }

    public synchronized List<Note> getNotes() {

        List<Note> results = new ArrayList();
        Cursor cursor = mDatabase.rawQuery("SELECT td.SuraId,td.VerseId,td.Note FROM Notes td", null);

        int suraId = cursor.getColumnIndex("SuraId");
        int verseId = cursor.getColumnIndex("VerseId");
        int note = cursor.getColumnIndex("Note");

        while (cursor.moveToNext()) {
            results.add(new Note(
                    cursor.getInt(verseId),
                    getAllSurah().get(cursor.getInt(suraId) - 1),
                    cursor.getString(note)
            ));
        }
        cursor.close();

        return results;
    }

    public synchronized List<Source> getAvailableSources() {
        List<Source> sources = new ArrayList();
        Cursor cursor = mDatabase.rawQuery("SELECT * FROM Sources ORDER BY Language,Ordering", null);

        int id = cursor.getColumnIndex("Id");
        int type = cursor.getColumnIndex("Type");
        int name = cursor.getColumnIndex("Name");
        int author = cursor.getColumnIndex("Author");
        int downloadLink = cursor.getColumnIndex("DownloadLink");
        int updateLink = cursor.getColumnIndex("UpdateLink");
        int lastModifiedDate = cursor.getColumnIndex("LastModifiedDate");
        int language = cursor.getColumnIndex("Language");
        int providerName = cursor.getColumnIndex("ProviderName");
        int status = cursor.getColumnIndex("Status");
        int order = cursor.getColumnIndex("Ordering");

        while (cursor.moveToNext()) {
            Source source = new Source(
                    cursor.getInt(id),
                    cursor.getString(type),
                    cursor.getString(name),
                    cursor.getString(author),
                    cursor.getString(downloadLink),
                    cursor.getString(updateLink),
                    Date.valueOf(cursor.getString(lastModifiedDate)),
                    cursor.getString(language),
                    cursor.getString(providerName),
                    cursor.getString(status),
                    cursor.getInt(order)
            );
            sources.add(source);
        }
        cursor.close();
        return sources;
    }

    public void setSourceOrder(int id, int order) {
        mDatabase.execSQL("UPDATE Sources SET Ordering = ? WHERE Id = ?"
                , new String[]{
                String.valueOf(order),
                String.valueOf(id)});
    }

    public void moveSourcesOrder(int idfrom, int from, int to) {

        if (from == to) {
            mDatabase.execSQL("UPDATE Sources SET Ordering = ? WHERE Id = ?"
                    , new String[]{
                    String.valueOf(to),
                    String.valueOf(idfrom)});
            return;
        }

        if (from < to) {
            int move = -1;

            mDatabase.execSQL("UPDATE Sources SET Ordering = (Ordering + (?)) WHERE Ordering > ? AND Ordering <= ?"
                    , new String[]{
                    String.valueOf(move),
                    String.valueOf(from),
                    String.valueOf(to)});
        } else {
            int move = 1;

            mDatabase.execSQL("UPDATE Sources SET Ordering = (Ordering + (?)) WHERE Ordering >= ? AND Ordering < ?"
                    , new String[]{
                    String.valueOf(move),
                    String.valueOf(to),
                    String.valueOf(from)});
        }

        mDatabase.execSQL("UPDATE Sources SET Ordering = ? WHERE Id = ?"
                , new String[]{
                String.valueOf(to),
                String.valueOf(idfrom)});

//        List<Source> hasil = getAvailableSources();
//        hasil.size();
    }

    public synchronized void deleteTranslationData(int id) {
        mDatabase.execSQL("DELETE FROM TranslationData where TranslationId = ?"
                , new String[]{
                String.valueOf(id)});
    }

    public synchronized void updateSourceStatus(int id, String status) {
        mDatabase.execSQL("UPDATE Sources SET Status = ? where Id = ?"
                , new String[]{
                status,
                String.valueOf(id)});
    }

    public void deleteNote(int suraId, int verseId) {
        mDatabase.execSQL("DELETE FROM Notes WHERE SuraId = ? AND VerseId = ?"
                , new String[]{
                String.valueOf(suraId),
                String.valueOf(verseId)});
    }

    public void setNote(Note note) {
        setNote(note.getSurahNo(), note.getNumber(), note.getText());
    }

    public void setNote(int suraId, int verseId, String note) {
        if (StringUtils.isBlank(note)) {
            deleteNote(suraId, verseId);
        } else {
            mDatabase.execSQL("INSERT OR REPLACE INTO Notes(SuraId,VerseId,Note) VALUES (?,?,?)"
                    , new String[]{
                    String.valueOf(suraId),
                    String.valueOf(verseId),
                    String.valueOf(note)});
        }
    }

    public synchronized void insertTranslationDataRecordBulk(Iterable<TranslationData> datas) {
        final SQLiteStatement statement = mDatabase.compileStatement("INSERT INTO TranslationData(TranslationId,SuraId,VerseId, VerseText) VALUES(?,?,?,?)");
        try {
            mDatabase.beginTransaction();

            for (TranslationData data : datas) {
                statement.clearBindings();
                statement.bindLong(1, data.id);
                statement.bindLong(2, data.surahNo);
                statement.bindLong(3, data.verseNo);
                statement.bindString(4, data.translation);
                statement.executeInsert();
            }

            mDatabase.setTransactionSuccessful();
        } catch (Exception e) {
            Crashlytics.logException(e);
        } finally {
            mDatabase.endTransaction();
        }
    }

    public List<Translation> getAvailableTranslations() {
        List<Translation> translations = new ArrayList();
        Cursor cursor = mDatabase.rawQuery("SELECT Id,Name,Language,LastModifiedDate,Ordering,TanzilId FROM Sources WHERE Type = 'Translation' AND Status = 'enabled' ORDER BY Ordering", null);

        int id = cursor.getColumnIndex("Id");
        int name = cursor.getColumnIndex("Name");
        int language = cursor.getColumnIndex("Language");
        int date = cursor.getColumnIndex("LastModifiedDate");
        int order = cursor.getColumnIndex("Ordering");
        int tanzilId = cursor.getColumnIndex("TanzilId");

        while (cursor.moveToNext()) {
            Translation translation = new Translation(
                    cursor.getInt(order),
                    cursor.getString(tanzilId),
                    Date.valueOf(cursor.getString(date)),
                    cursor.getString(language),
                    cursor.getString(name),
                    cursor.getInt(id)
            );
            translations.add(translation);
        }
        cursor.close();
        return translations;
    }

    public int getSourceId() {
        Cursor cursor = mDatabase.rawQuery("SELECT max(Id) FROM Sources", null);
        int max = 0;
        while (cursor.moveToNext()) {
            max = cursor.getInt(0);
        }
        cursor.close();
        return max;
    }

    public void addSource(Source source) {
        mDatabase.execSQL("INSERT OR REPLACE INTO Sources(Id,Type,Name,Author,DownloadLink,UpdateLink,LastModifiedDate,Language,ProviderName,Status,Ordering) VALUES(" +
                "?,?,?,?,?,?,?,?,?,?,?" +
                ")", new String[]{
                String.valueOf(source.id),
                source.type,
                source.name,
                source.author,
                source.downloadLink,
                source.updateLink,
                source.lastModifiedDate.toString(),
                source.language,
                source.providerName,
                source.status,
                String.valueOf(source.getOrder())
        });
    }

    public String getNote(int surahNo, int verseNo) {
        Cursor cursor = mDatabase.rawQuery("SELECT Note FROM Notes WHERE VerseId = ? AND SuraID = ? ",
                new String[]{
                        String.valueOf(verseNo),
                        String.valueOf(surahNo)
                }
        );

        String note = "";

        if (cursor.moveToNext()) {
            note = cursor.getString(0);
        }

        cursor.close();

        return note;
    }

    public void addTranslationToVerse(Verse verse, Translation translation) {
        Cursor cursor = mDatabase.rawQuery("SELECT VerseText FROM TranslationData WHERE TranslationId = ? AND VerseId = ? AND SuraId = ? ",
                new String[]{
                        String.valueOf(translation.getId()),
                        String.valueOf(verse.getNumber()),
                        String.valueOf(verse.getSurahNo())
                }
        );

        if (cursor.moveToNext()) {
            String txt = cursor.getString(0);
            if (!StringUtils.isBlank(txt)) {
                verse.getTranslations().put(translation, cursor.getString(0));
            }
        }

        cursor.close();
    }

    public String getArabicText(int surahNo, int verseNo) {
        Cursor cursor = mDatabase.rawQuery("SELECT VerseText FROM ArabicText WHERE VerseId = ? AND SuraId = ? ",
                new String[]{
                        String.valueOf(verseNo),
                        String.valueOf(surahNo)
                }
        );

        String arabicText = "";

        if (cursor.moveToNext()) {
            arabicText = cursor.getString(0);
        }

        cursor.close();
        return arabicText;
    }

    public Verse getVerse(int surahNo, int verseNo, Iterable<Translation> translations) {
        Verse verse = new Verse(surahNo, verseNo, getArabicText(surahNo, verseNo), getNote(surahNo, verseNo));

        for (Translation translation : translations) {
            this.addTranslationToVerse(verse, translation);
        }

        return verse;
    }

    public Surah getSurah(int surahNo) {
        Surah surah = null;
        Cursor cursor = mDatabase.rawQuery("SELECT Id,TransliterationName,TranslationName,VerseCount,ArabicName,Type FROM Surah WHERE Id = ?", new String[]{String.valueOf(surahNo)});
        if (cursor.moveToNext()) {
            surah = new Surah(surahNo,
                    cursor.getString(cursor.getColumnIndex("TransliterationName")),
                    cursor.getString(cursor.getColumnIndex("TranslationName")),
                    cursor.getString(cursor.getColumnIndex("ArabicName")),
                    cursor.getString(cursor.getColumnIndex("Type")),
                    cursor.getInt(cursor.getColumnIndex("VerseCount"))
            );
        }
        cursor.close();
        return surah;
    }

    public synchronized List<Surah> getAllSurah() {
        if (surahsCache == null) {

            List<Surah> surahs = new ArrayList();
            Cursor cursor = mDatabase.rawQuery("SELECT Id,TransliterationName,TranslationName,VerseCount,ArabicName,Type FROM Surah ORDER BY Id ASC", new String[]{});
            while (cursor.moveToNext()) {
                surahs.add(new Surah(cursor.getInt(cursor.getColumnIndex("Id")),
                        cursor.getString(cursor.getColumnIndex("TransliterationName")),
                        cursor.getString(cursor.getColumnIndex("TranslationName")),
                        cursor.getString(cursor.getColumnIndex("ArabicName")),
                        cursor.getString(cursor.getColumnIndex("Type")),
                        cursor.getInt(cursor.getColumnIndex("VerseCount"))
                ));
            }
            cursor.close();
            surahsCache = surahs;
        }
        return surahsCache;
    }

    public synchronized void deleteSourcesData(int id) {

        try {
            mDatabase.beginTransaction();
            deleteTranslationData(id);
            updateSourceStatus(id, "notdownloaded");
            mDatabase.setTransactionSuccessful();
        } finally {
            mDatabase.endTransaction();
        }
    }

    public Surah getSurahNo(int surahNo) {
        return getAllSurah().get(surahNo - 1);
    }
}

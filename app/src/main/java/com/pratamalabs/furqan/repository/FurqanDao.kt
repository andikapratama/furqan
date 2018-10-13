package com.pratamalabs.furqan.repository

import android.database.sqlite.SQLiteDatabase
import com.crashlytics.android.Crashlytics
import com.pratamalabs.furqan.models.*
import org.apache.commons.lang3.StringUtils
import java.sql.Date
import java.util.*

/**
 * Created by pratamalabs on 11/6/13.
 */


object FurqanDao {

    lateinit var mDatabase: SQLiteDatabase

    internal var surahsCache: List<Surah>? = null

    val notes: List<Note>
        @Synchronized get() {

            val results = mutableListOf<Note>()
            val cursor = mDatabase.rawQuery("SELECT td.SuraId,td.VerseId,td.Note FROM Notes td", null)

            val suraId = cursor.getColumnIndex("SuraId")
            val verseId = cursor.getColumnIndex("VerseId")
            val note = cursor.getColumnIndex("Note")

            while (cursor.moveToNext()) {
                results.add(Note(
                        cursor.getInt(verseId),
                        allSurah[cursor.getInt(suraId) - 1],
                        cursor.getString(note)
                ))
            }
            cursor.close()

            return results
        }

    val availableSources: List<Source>
        @Synchronized get() {
            val sources = mutableListOf<Source>()
            val cursor = mDatabase.rawQuery("SELECT * FROM Sources ORDER BY Language,Ordering", null)

            val id = cursor.getColumnIndex("Id")
            val type = cursor.getColumnIndex("Type")
            val name = cursor.getColumnIndex("Name")
            val author = cursor.getColumnIndex("Author")
            val downloadLink = cursor.getColumnIndex("DownloadLink")
            val updateLink = cursor.getColumnIndex("UpdateLink")
            val lastModifiedDate = cursor.getColumnIndex("LastModifiedDate")
            val language = cursor.getColumnIndex("Language")
            val providerName = cursor.getColumnIndex("ProviderName")
            val status = cursor.getColumnIndex("Status")
            val order = cursor.getColumnIndex("Ordering")

            while (cursor.moveToNext()) {
                val source = Source(
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
                )
                sources.add(source)
            }
            cursor.close()
            return sources
        }

    val availableTranslations: List<Translation>
        get() {
            val translations = mutableListOf<Translation>()
            val cursor = mDatabase.rawQuery("SELECT Id,Name,Language,LastModifiedDate,Ordering,TanzilId FROM Sources WHERE Type = 'Translation' AND Status = 'enabled' ORDER BY Ordering", null)

            val id = cursor.getColumnIndex("Id")
            val name = cursor.getColumnIndex("Name")
            val language = cursor.getColumnIndex("Language")
            val date = cursor.getColumnIndex("LastModifiedDate")
            val order = cursor.getColumnIndex("Ordering")
            val tanzilId = cursor.getColumnIndex("TanzilId")

            while (cursor.moveToNext()) {
                val translation = Translation(
                        cursor.getInt(order),
                        cursor.getString(tanzilId),
                        Date.valueOf(cursor.getString(date)),
                        cursor.getString(language),
                        cursor.getString(name),
                        cursor.getInt(id)
                )
                translations.add(translation)
            }
            cursor.close()
            return translations
        }

    val sourceId: Int
        get() {
            val cursor = mDatabase.rawQuery("SELECT max(Id) FROM Sources", null)
            var max = 0
            while (cursor.moveToNext()) {
                max = cursor.getInt(0)
            }
            cursor.close()
            return max
        }

    val allSurah: List<Surah>
        @Synchronized get() {
            if (surahsCache == null) {

                val surahs = mutableListOf<Surah>()
                val cursor = mDatabase.rawQuery("SELECT Id,TransliterationName,TranslationName,VerseCount,ArabicName,Type FROM Surah ORDER BY Id ASC", arrayOf())
                while (cursor.moveToNext()) {
                    surahs.add(Surah(cursor.getInt(cursor.getColumnIndex("Id")),
                            cursor.getString(cursor.getColumnIndex("TransliterationName")),
                            cursor.getString(cursor.getColumnIndex("TranslationName")),
                            cursor.getString(cursor.getColumnIndex("ArabicName")),
                            cursor.getString(cursor.getColumnIndex("Type")),
                            cursor.getInt(cursor.getColumnIndex("VerseCount"))
                    ))
                }
                cursor.close()
                surahsCache = surahs
            }
            return surahsCache!!
        }

    init {
        val mSQLite = FurqanDatabase.get()
        mDatabase = mSQLite.writableDatabase
    }

    @Synchronized
    fun searchNotesContaining(query: String): List<SearchResult> {

        val results = mutableListOf<SearchResult>()
        val cursor = mDatabase.rawQuery("SELECT SuraId,VerseId,Note FROM Notes WHERE Note LIKE '%' || ? || '%' ORDER BY SuraId,VerseId", arrayOf(query))

        val suraId = cursor.getColumnIndex("SuraId")
        val verseId = cursor.getColumnIndex("VerseId")
        val verseText = cursor.getColumnIndex("Note")

        while (cursor.moveToNext()) {
            results.add(SearchResult(
                    cursor.getString(verseText),
                    cursor.getInt(verseId),
                    cursor.getInt(suraId),
                    "Note"
            ))
        }
        cursor.close()

        return results
    }

    @Synchronized
    fun searchFurqanContaining(query: String): List<SearchResult> {
        val list = ArrayList<SearchResult>()
        list.addAll(searchNotesContaining(query))
        list.addAll(searchTranslationsContaining(query))
        return list
    }

    @Synchronized
    fun searchTranslationsContaining(query: String): List<SearchResult> {

        val results = mutableListOf<SearchResult>()
        val cursor = mDatabase.rawQuery("SELECT td.SuraId,td.VerseId,td.VerseText,s.Name FROM TranslationData td LEFT JOIN Sources s on td.TranslationId = s.Id WHERE s.Status = 'enabled' AND td.VerseText LIKE '%' || ? || '%' ORDER BY td.SuraId,td.VerseId", arrayOf(query))

        val suraId = cursor.getColumnIndex("SuraId")
        val verseId = cursor.getColumnIndex("VerseId")
        val verseText = cursor.getColumnIndex("VerseText")
        val name = cursor.getColumnIndex("Name")

        while (cursor.moveToNext()) {
            results.add(SearchResult(
                    cursor.getString(verseText),
                    cursor.getInt(verseId),
                    cursor.getInt(suraId),
                    cursor.getString(name)
            ))
        }
        cursor.close()

        return results
    }

    fun setSourceOrder(id: Int, order: Int) {
        mDatabase.execSQL("UPDATE Sources SET Ordering = ? WHERE Id = ?", arrayOf(order.toString(), id.toString()))
    }

    fun moveSourcesOrder(idfrom: Int, from: Int, to: Int) {

        if (from == to) {
            mDatabase.execSQL("UPDATE Sources SET Ordering = ? WHERE Id = ?", arrayOf(to.toString(), idfrom.toString()))
            return
        }

        if (from < to) {
            val move = -1

            mDatabase.execSQL("UPDATE Sources SET Ordering = (Ordering + (?)) WHERE Ordering > ? AND Ordering <= ?", arrayOf(move.toString(), from.toString(), to.toString()))
        } else {
            val move = 1

            mDatabase.execSQL("UPDATE Sources SET Ordering = (Ordering + (?)) WHERE Ordering >= ? AND Ordering < ?", arrayOf(move.toString(), to.toString(), from.toString()))
        }

        mDatabase.execSQL("UPDATE Sources SET Ordering = ? WHERE Id = ?", arrayOf(to.toString(), idfrom.toString()))

        //        List<Source> hasil = getAvailableSources();
        //        hasil.size();
    }

    @Synchronized
    fun deleteTranslationData(id: Int) {
        mDatabase.execSQL("DELETE FROM TranslationData where TranslationId = ?", arrayOf(id.toString()))
    }

    @Synchronized
    fun updateSourceStatus(id: Int, status: String) {
        mDatabase.execSQL("UPDATE Sources SET Status = ? where Id = ?", arrayOf(status, id.toString()))
    }

    fun deleteNote(suraId: Int, verseId: Int) {
        mDatabase.execSQL("DELETE FROM Notes WHERE SuraId = ? AND VerseId = ?", arrayOf(suraId.toString(), verseId.toString()))
    }

    fun setNote(note: Note) {
        setNote(note.surahNo, note.number, note.text)
    }

    fun setNote(suraId: Int, verseId: Int, note: String) {
        if (StringUtils.isBlank(note)) {
            deleteNote(suraId, verseId)
        } else {
            mDatabase.execSQL("INSERT OR REPLACE INTO Notes(SuraId,VerseId,Note) VALUES (?,?,?)", arrayOf(suraId.toString(), verseId.toString(), note))
        }
    }

    @Synchronized
    fun insertTranslationDataRecordBulk(datas: Iterable<TranslationData>) {
        val statement = mDatabase.compileStatement("INSERT INTO TranslationData(TranslationId,SuraId,VerseId, VerseText) VALUES(?,?,?,?)")
        try {
            mDatabase.beginTransaction()

            for (data in datas) {
                statement.clearBindings()
                statement.bindLong(1, data.id.toLong())
                statement.bindLong(2, data.surahNo.toLong())
                statement.bindLong(3, data.verseNo.toLong())
                statement.bindString(4, data.translation)
                statement.executeInsert()
            }

            mDatabase.setTransactionSuccessful()
        } catch (e: Exception) {
            Crashlytics.logException(e)
        } finally {
            mDatabase.endTransaction()
        }
    }

    fun addSource(source: Source) {
        mDatabase.execSQL("INSERT OR REPLACE INTO Sources(Id,Type,Name,Author,DownloadLink,UpdateLink,LastModifiedDate,Language,ProviderName,Status,Ordering) VALUES(" +
                "?,?,?,?,?,?,?,?,?,?,?" +
                ")", arrayOf(source.id.toString(), source.type, source.name, source.author, source.downloadLink, source.updateLink, source.lastModifiedDate.toString(), source.language, source.providerName, source.status, source.order.toString()))
    }

    fun getNote(surahNo: Int, verseNo: Int): String {
        val cursor = mDatabase.rawQuery("SELECT Note FROM Notes WHERE VerseId = ? AND SuraID = ? ",
                arrayOf(verseNo.toString(), surahNo.toString())
        )

        var note = ""

        if (cursor.moveToNext()) {
            note = cursor.getString(0)
        }

        cursor.close()

        return note
    }

    fun addTranslationToVerse(verse: Verse, translation: Translation) {
        val cursor = mDatabase.rawQuery("SELECT VerseText FROM TranslationData WHERE TranslationId = ? AND VerseId = ? AND SuraId = ? ",
                arrayOf(translation.id.toString(), verse.number.toString(), verse.surahNo.toString())
        )

        if (cursor.moveToNext()) {
            val txt = cursor.getString(0)
            if (!StringUtils.isBlank(txt)) {
                verse.translations[translation] = cursor.getString(0)
            }
        }

        cursor.close()
    }

    fun getArabicText(surahNo: Int, verseNo: Int): String {
        val cursor = mDatabase.rawQuery("SELECT VerseText FROM ArabicText WHERE VerseId = ? AND SuraId = ? ",
                arrayOf(verseNo.toString(), surahNo.toString())
        )

        var arabicText = ""

        if (cursor.moveToNext()) {
            arabicText = cursor.getString(0)
        }

        cursor.close()
        return arabicText
    }

    fun getVerse(surahNo: Int, verseNo: Int, translations: Iterable<Translation>): Verse {
        val verse = Verse(surahNo, verseNo, getArabicText(surahNo, verseNo), getNote(surahNo, verseNo))

        for (translation in translations) {
            this.addTranslationToVerse(verse, translation)
        }

        return verse
    }

    fun getSurah(surahNo: Int): Surah? {
        var surah: Surah? = null
        val cursor = mDatabase.rawQuery("SELECT Id,TransliterationName,TranslationName,VerseCount,ArabicName,Type FROM Surah WHERE Id = ?", arrayOf(surahNo.toString()))
        if (cursor.moveToNext()) {
            surah = Surah(surahNo,
                    cursor.getString(cursor.getColumnIndex("TransliterationName")),
                    cursor.getString(cursor.getColumnIndex("TranslationName")),
                    cursor.getString(cursor.getColumnIndex("ArabicName")),
                    cursor.getString(cursor.getColumnIndex("Type")),
                    cursor.getInt(cursor.getColumnIndex("VerseCount"))
            )
        }
        cursor.close()
        return surah
    }

    @Synchronized
    fun deleteSourcesData(id: Int) {

        try {
            mDatabase.beginTransaction()
            deleteTranslationData(id)
            updateSourceStatus(id, "notdownloaded")
            mDatabase.setTransactionSuccessful()
        } finally {
            mDatabase.endTransaction()
        }
    }

    fun getSurahNo(surahNo: Int): Surah {
        return allSurah[surahNo - 1]
    }
}

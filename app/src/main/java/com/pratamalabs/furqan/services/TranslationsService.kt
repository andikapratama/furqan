package com.pratamalabs.furqan.services

import android.content.ContentResolver
import android.content.Context
import android.net.Uri
import android.text.TextUtils
import android.widget.Toast
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import com.pratamalabs.furqan.models.Note
import com.pratamalabs.furqan.models.TranslationData
import com.pratamalabs.furqan.repository.FurqanDao
import org.apache.commons.io.FileUtils
import org.apache.commons.lang3.StringUtils
import java.io.BufferedReader
import java.io.File
import java.io.IOException
import java.io.InputStreamReader
import java.util.*

/**
 * Created by pratamalabs on 13/6/13.
 */
object TranslationsService {

    internal var dao = FurqanDao

    fun backupNotesToExternalDevice(): Boolean {
        val notes = dao.notes
        val gson = Gson()
        val json = gson.toJson(notes)
        return Utils.writeToFile(json)
    }


    fun importNotes(context: Context, data: Uri) {
        val scheme = data.scheme

        if (ContentResolver.SCHEME_FILE == scheme) {
            try {
                val cr = context.contentResolver
                val `is` = cr.openInputStream(data) ?: return

                val reader = BufferedReader(InputStreamReader(`is`))

                val listType = object : TypeToken<ArrayList<Note>>() {

                }.type
                val gson = Gson()
                val notes = gson.fromJson<List<Note>>(reader, listType)
                importNotes(notes)
                Toast.makeText(context, "Notes succesfully imported!", Toast.LENGTH_LONG).show()

            } catch (e: Exception) {
                e.printStackTrace()
                Toast.makeText(context, "File content invalid", Toast.LENGTH_LONG).show()
            }

        }
    }

    fun importNotes(notes: List<Note>) {
        for (note in notes) {
            dao.setNote(note)
        }
    }

    fun saveTanzilTranslationResourceAndEnableIt(id: Int, file: File): Boolean {
        val startTime = System.currentTimeMillis()


        var result = true
        val lines: List<String>
        try {
            lines = FileUtils.readLines(file)
        } catch (e: IOException) {
            e.printStackTrace()
            result = false
            return result
        }

        val datas = ArrayList<TranslationData>(lines.size)

        for (line in lines) {
            if (line.isEmpty() || line.startsWith("#"))
                continue

            val value = TextUtils.split(line, "\\|")
            if (value.size < 3)
                continue
            val SuraNo = value[0]
            val verseNo = value[1]
            val translation = value[2]
            if (!StringUtils.isBlank(translation)) {
                datas.add(TranslationData(id, Integer.valueOf(verseNo), Integer.valueOf(SuraNo), translation))
            }
        }

        dao.insertTranslationDataRecordBulk(datas)

        dao.updateSourceStatus(id, "enabled")
        val endTime = System.currentTimeMillis()

        println("That took " + (endTime - startTime) + " milliseconds")

        return result
    }
}

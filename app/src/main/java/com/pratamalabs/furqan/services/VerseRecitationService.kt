package com.pratamalabs.furqan.services

import android.app.ProgressDialog
import android.content.Context
import android.os.Handler
import android.widget.Toast
import com.koushikdutta.async.future.FutureCallback
import com.koushikdutta.ion.Ion
import com.pratamalabs.furqan.Utilities
import com.pratamalabs.furqan.models.Recitation
import com.pratamalabs.furqan.models.Surah
import com.pratamalabs.furqan.repository.FurqanDao
import org.apache.commons.io.FileUtils
import org.jetbrains.anko.coroutines.experimental.bg
import java.io.*

/**
 * Created by andikapratama on 26/07/15.
 */
object VerseRecitationService {

    internal var dao = FurqanDao

    open fun downloadFullRecitation(context: Context,
                                    recitation: Recitation,
                                    stopped: ValueHolder<Boolean>,
                                    progressCallback: ProgressDialog,
                                    callback: FutureCallback<Boolean>) {
        bg {
            val folder = File(context.getExternalFilesDir(null), "/" + recitation.subfolder)
            if (!folder.exists()) {
                folder.mkdir()
            }
            downloadVerseRecitation(context, dao.allSurah, recitation, stopped, 1, 1, 0, progressCallback, callback)
        }


    }

    open fun deleteFullRecitation(context: Context,
                                  recitation: Recitation,
                                  callback: FutureCallback<Boolean>) {
        bg {
            val folder = File(context.getExternalFilesDir(null), "/" + recitation.subfolder)
            try {
                FileUtils.deleteDirectory(folder)
                callback.onCompleted(null, true)
            } catch (e: IOException) {
                callback.onCompleted(e, false)
                e.printStackTrace()
            }
        }
    }

    fun downloadVerseRecitation(context: Context,
                                surahList: List<Surah>,
                                recitation: Recitation,
                                stopped: ValueHolder<Boolean>,
                                SurahNo: Int,
                                verseNo: Int,
                                progress: Int,
                                progressCallback: ProgressDialog,
                                callback: FutureCallback<Boolean>) {

        var nSurah = SurahNo
        var nVerse = verseNo + 1

        val currentSurah = surahList[SurahNo - 1]
        if (currentSurah.verseCount <= verseNo) {
            nSurah = SurahNo + 1
            nVerse = 1
        }

        val nextSurah = nSurah
        val nextVerse = nVerse

        val nextProgress = progress + 1


        val folder = File(context.getExternalFilesDir(null), "/" + recitation.subfolder)
        val verse = File(folder, String.format("%s%s.mp3", Utils.leadingZeros(SurahNo), Utils.leadingZeros(verseNo)))
        //skip to the next one
        if (verse.exists()) {
            progressCallback.progress = nextProgress

            if (stopped.value) {
                callback.onCompleted(null, false)
            }

            if (surahList.size < nextSurah) {
                //terminate condition;
                callback.onCompleted(null, true)
            } else {
                val mainHandler = Handler(context.mainLooper)

                val myRunnable = Runnable { downloadVerseRecitation(context, surahList, recitation, stopped, nextSurah, nextVerse, nextProgress, progressCallback, callback) }
                mainHandler.post(myRunnable)
            }
            return
        }

        if (stopped.value) {
            callback.onCompleted(null, false)
        }


        Ion.with(context).load(Utils.getVerseRecitationUrl(recitation.subfolder, SurahNo, verseNo))
                .progress { downloaded, total ->
                    val primaryProgress = progressCallback.progress
                    val intermediary = primaryProgress + (downloaded.toFloat() / total.toFloat() * (6236 - primaryProgress)).toInt()
                    progressCallback.secondaryProgress = intermediary
                }
                .write(context.getFileStreamPath("zip-" + System.currentTimeMillis() + ".zip"))
                .setCallback(FutureCallback { e, result ->
                    if (e != null) {
                        Toast.makeText(context, "Error downloading $recitation", Toast.LENGTH_LONG).show()
                        callback.onCompleted(e, false)
                        return@FutureCallback
                    }

                    var `in`: InputStream? = null
                    var out: OutputStream? = null
                    try {

                        `in` = FileInputStream(result)
                        out = FileOutputStream(verse)

                        // Transfer bytes from in to out
                        val buf = ByteArray(1024)
                        var len = `in`.read(buf)
                        while (len > 0) {
                            len = `in`.read(buf)
                            out.write(buf, 0, len)
                        }
                        result.delete()
                    } catch (e1: IOException) {
                        e1.printStackTrace()
                        callback.onCompleted(e1, false)
                    } finally {
                        Utilities.closeQuietly(`in`, out)
                    }

                    progressCallback.progress = nextProgress

                    if (surahList.size < nextSurah) {
                        //terminate condition;
                        callback.onCompleted(e, true)
                    } else {
                        downloadVerseRecitation(context, surahList, recitation, stopped, nextSurah, nextVerse, nextProgress, progressCallback, callback)
                    }
                })
    }
}

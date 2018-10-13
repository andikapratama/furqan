package com.pratamalabs.furqan.services;

import android.app.ProgressDialog;
import android.content.Context;
import android.os.Handler;
import android.widget.Toast;

import com.koushikdutta.async.future.FutureCallback;
import com.koushikdutta.ion.Ion;
import com.koushikdutta.ion.ProgressCallback;
import com.pratamalabs.furqan.FurqanApp;
import com.pratamalabs.furqan.Utilities;
import com.pratamalabs.furqan.models.Recitation;
import com.pratamalabs.furqan.models.Surah;
import com.pratamalabs.furqan.repository.FurqanDao;

import org.androidannotations.annotations.Background;
import org.androidannotations.annotations.EBean;
import org.apache.commons.io.FileUtils;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.List;

/**
 * Created by andikapratama on 26/07/15.
 */
@EBean(scope = EBean.Scope.Singleton)
public class VerseRecitationService {

    FurqanDao dao = FurqanDao.get();


    public static VerseRecitationService get() {
        return VerseRecitationService_.getInstance_(FurqanApp.instance);
    }

    @Background
    public void downloadFullRecitation(final Context context,
                                       final Recitation recitation,
                                       final ValueHolder<Boolean> stopped,
                                       final ProgressDialog progressCallback,
                                       final FutureCallback<Boolean> callback) {

        File folder = new File(context.getExternalFilesDir(null), "/" + recitation.subfolder);
        if (!folder.exists()) {
            folder.mkdir();
        }
        downloadVerseRecitation(context, dao.getAllSurah(), recitation, stopped, 1, 1, 0, progressCallback, callback);

    }

    @Background
    public void deleteFullRecitation(final Context context,
                                     final Recitation recitation,
                                     final FutureCallback<Boolean> callback) {

        File folder = new File(context.getExternalFilesDir(null), "/" + recitation.subfolder);
        try {
            FileUtils.deleteDirectory(folder);
            callback.onCompleted(null, true);
        } catch (IOException e) {
            callback.onCompleted(e, false);
            e.printStackTrace();
        }

    }

    public void downloadVerseRecitation(final Context context,
                                        final List<Surah> surahList,
                                        final Recitation recitation,
                                        final ValueHolder<Boolean> stopped,
                                        final int SurahNo,
                                        final int verseNo,
                                        final int progress,
                                        final ProgressDialog progressCallback,
                                        final FutureCallback<Boolean> callback) {

        int nSurah = SurahNo;
        int nVerse = verseNo + 1;

        Surah currentSurah = surahList.get(SurahNo - 1);
        if (currentSurah.getVerseCount() <= verseNo) {
            nSurah = SurahNo + 1;
            nVerse = 1;
        }

        final int nextSurah = nSurah;
        final int nextVerse = nVerse;

        final int nextProgress = progress + 1;


        File folder = new File(context.getExternalFilesDir(null), "/" + recitation.subfolder);
        final File verse = new File(folder, String.format("%s%s.mp3", Utils.leadingZeros(SurahNo), Utils.leadingZeros(verseNo)));
        //skip to the next one
        if (verse.exists()) {
            progressCallback.setProgress(nextProgress);

            if (stopped.value) {
                callback.onCompleted(null, false);
            }

            if (surahList.size() < nextSurah) {
                //terminate condition;
                callback.onCompleted(null, true);
            } else {
                Handler mainHandler = new Handler(context.getMainLooper());

                Runnable myRunnable = new Runnable() {
                    @Override
                    public void run() {
                        downloadVerseRecitation(context, surahList, recitation, stopped, nextSurah, nextVerse, nextProgress, progressCallback, callback);
                    }
                };
                mainHandler.post(myRunnable);
            }
            return;
        }

        if (stopped.value) {
            callback.onCompleted(null, false);
        }


        Ion.with(context).load(Utils.getVerseRecitationUrl(recitation.subfolder, SurahNo, verseNo))
                .progress(new ProgressCallback() {
                    @Override
                    public void onProgress(long downloaded, long total) {
                        int primaryProgress = progressCallback.getProgress();
                        int intermediary = primaryProgress + (int) ((((float) downloaded) / ((float) total)) * (6236 - primaryProgress));
                        progressCallback.setSecondaryProgress(intermediary);
                    }
                })
                .write(context.getFileStreamPath("zip-" + System.currentTimeMillis() + ".zip"))
                .setCallback(new FutureCallback<File>() {
                    @Override
                    public void onCompleted(Exception e, File result) {
                        if (e != null) {
                            Toast.makeText(context, "Error downloading " + recitation, Toast.LENGTH_LONG).show();
                            callback.onCompleted(e, false);
                            return;
                        }

                        InputStream in = null;
                        OutputStream out = null;
                        try {

                            in = new FileInputStream(result);
                            out = new FileOutputStream(verse);

                            // Transfer bytes from in to out
                            byte[] buf = new byte[1024];
                            int len;
                            while ((len = in.read(buf)) > 0) {
                                out.write(buf, 0, len);
                            }
                            result.delete();
                        } catch (IOException e1) {
                            e1.printStackTrace();
                            callback.onCompleted(e1, false);
                        } finally {
                            Utilities.closeQuietly(in, out);
                        }

                        progressCallback.setProgress(nextProgress);

                        if (surahList.size() < nextSurah) {
                            //terminate condition;
                            callback.onCompleted(e, true);
                        } else {
                            downloadVerseRecitation(context, surahList, recitation, stopped, nextSurah, nextVerse, nextProgress, progressCallback, callback);
                        }


                    }
                });
    }

}

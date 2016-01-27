package com.pratamalabs.furqan.services;

import android.content.Context;
import android.media.AudioManager;
import android.media.MediaPlayer;
import android.net.wifi.WifiManager;
import android.os.PowerManager;
import android.support.v4.app.Fragment;

import com.pratamalabs.furqan.FurqanSettings;
import com.pratamalabs.furqan.events.VersePlayerEvent;
import com.pratamalabs.furqan.models.Recitation;

import org.androidannotations.annotations.AfterInject;
import org.androidannotations.annotations.Bean;
import org.androidannotations.annotations.EFragment;

import java.io.IOException;

/**
 * Created by KatulSomin on 14/10/2014.
 */

@EFragment
public class VersePlayerFragment extends Fragment {

    @Bean
    FurqanSettings settings;

    @Bean
    EventBus bus;
    MediaPlayer mediaPlayer;
    WifiManager.WifiLock wifiLock;
    boolean isPlaying;
    int pendingSurahNo = 0;
    int pendingVerseNo = 0;

    public synchronized void stopRecitation() {
        stopRecitation(false);
    }

    public synchronized void stopRecitation(boolean finished) {
        isPlaying = false;
        if (mediaPlayer == null)
            return;

        mediaPlayer.stop();
        mediaPlayer.reset();

        if (wifiLock.isHeld()) {
            wifiLock.release();
        }

        if (finished) {
            bus.post(new VersePlayerEvent(VersePlayerEvent.Type.Finished));
        } else {
            bus.post(new VersePlayerEvent(VersePlayerEvent.Type.Stop));
        }
    }

    public synchronized void release() {
        if (mediaPlayer == null)
            return;
        stopRecitation();
        mediaPlayer.release();
        mediaPlayer = null;
    }

    @Override
    public void onDestroy() {
        release();
        super.onDestroy();
    }

    private synchronized void initMediaPlayer(Context context) {
        mediaPlayer = new MediaPlayer();
        mediaPlayer.setAudioStreamType(AudioManager.STREAM_MUSIC);
        mediaPlayer.setWakeMode(context, PowerManager.PARTIAL_WAKE_LOCK);

        mediaPlayer.setOnErrorListener(new MediaPlayer.OnErrorListener() {
            @Override
            public boolean onError(MediaPlayer mediaPlayer, int i, int i2) {
                stopRecitation();
                bus.post(new VersePlayerEvent(VersePlayerEvent.Type.Error));
                return false;
            }
        });

        wifiLock = ((WifiManager) context.getSystemService(Context.WIFI_SERVICE))
                .createWifiLock(WifiManager.WIFI_MODE_FULL, "mylock");
    }

    public synchronized boolean isPlaying() {
        return isPlaying;
    }

    @AfterInject
    public void afterInject() {
        if (pendingVerseNo > 0) {
            playRecitation(getActivity(), pendingSurahNo, pendingVerseNo);
        }
    }

    public synchronized void playRecitation(Context context, int SurahNo, int verseNo) {
        if (bus == null) {
            pendingSurahNo = SurahNo;
            pendingVerseNo = verseNo;
            return;
        }
        if (mediaPlayer != null) {
            stopRecitation();
        } else {
            initMediaPlayer(context);
        }

        isPlaying = true;
        bus.post(new VersePlayerEvent(VersePlayerEvent.Type.Play));
        try {
            Recitation selectedRecitations = settings.getSelectedRecitation();
            wifiLock.acquire();
            mediaPlayer.setDataSource(Utils.getVerseRecitationUrl(selectedRecitations.subfolder, SurahNo, verseNo));
        } catch (IOException e) {
            e.printStackTrace();
        }
        mediaPlayer.setOnPreparedListener(new MediaPlayer.OnPreparedListener() {

            @Override
            public void onPrepared(MediaPlayer mp) {
                mp.start();
            }
        });
        mediaPlayer.setOnCompletionListener(new MediaPlayer.OnCompletionListener() {
            @Override
            public void onCompletion(MediaPlayer mediaPlayer) {
                stopRecitation(true);
            }
        });
        mediaPlayer.prepareAsync();
    }


}

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

    private String leadingZeros(int number) {
        if (number < 10) {
            return String.format("00%d", number);
        } else if (number < 100) {
            return String.format("0%d", number);
        } else {
            return String.valueOf(number);
        }
    }

    public synchronized void stopRecitation() {
        isPlaying = false;
        bus.post(new VersePlayerEvent(VersePlayerEvent.Type.Stop));
        if (mediaPlayer == null)
            return;

        mediaPlayer.stop();
        mediaPlayer.reset();

        if (wifiLock.isHeld()) {
            wifiLock.release();
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

    public synchronized void playRecitation(Context context, int SurahNo, int verseNo) {
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
            mediaPlayer.setDataSource(String.format("http://www.everyayah.com/data/%s/%s%s.mp3",
                    selectedRecitations.subfolder,
                    leadingZeros(SurahNo),
                    leadingZeros(verseNo)));
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
                stopRecitation();
            }
        });
        mediaPlayer.prepareAsync();
    }


}

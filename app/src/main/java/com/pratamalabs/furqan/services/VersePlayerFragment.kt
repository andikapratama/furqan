package com.pratamalabs.furqan.services

import android.content.Context
import android.media.AudioManager
import android.media.MediaPlayer
import android.net.wifi.WifiManager
import android.os.Bundle
import android.os.PowerManager
import android.support.v4.app.Fragment
import com.pratamalabs.furqan.FurqanSettings
import com.pratamalabs.furqan.events.VersePlayerEvent
import java.io.IOException

/**
 * Created by KatulSomin on 14/10/2014.
 */

open class VersePlayerFragment : Fragment() {

    internal var settings = FurqanSettings.get()

    internal var bus = EventBus
    internal var mediaPlayer: MediaPlayer? = null
    internal lateinit var wifiLock: WifiManager.WifiLock
    @get:Synchronized
    var isPlaying: Boolean = false
        internal set
    internal var pendingSurahNo = 0
    internal var pendingVerseNo = 0

    @Synchronized
    fun stopRecitation() {
        stopRecitation(false)
    }

    @Synchronized
    fun stopRecitation(finished: Boolean) {
        isPlaying = false
        if (mediaPlayer == null)
            return

        mediaPlayer!!.stop()
        mediaPlayer!!.reset()

        if (wifiLock.isHeld) {
            wifiLock.release()
        }

        if (finished) {
            bus!!.post(VersePlayerEvent(VersePlayerEvent.Type.Finished))
        } else {
            bus!!.post(VersePlayerEvent(VersePlayerEvent.Type.Stop))
        }
    }

    @Synchronized
    fun release() {
        if (mediaPlayer == null)
            return
        stopRecitation()
        mediaPlayer!!.release()
        mediaPlayer = null
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        if (pendingVerseNo > 0) {
            playRecitation(activity, pendingSurahNo, pendingVerseNo)
        }
    }

    override fun onDestroy() {
        release()
        super.onDestroy()
    }

    @Synchronized
    private fun initMediaPlayer(context: Context?) {
        mediaPlayer = MediaPlayer()
        mediaPlayer!!.setAudioStreamType(AudioManager.STREAM_MUSIC)
        mediaPlayer!!.setWakeMode(context, PowerManager.PARTIAL_WAKE_LOCK)

        mediaPlayer!!.setOnErrorListener { mediaPlayer, i, i2 ->
            stopRecitation()
            bus!!.post(VersePlayerEvent(VersePlayerEvent.Type.Error))
            false
        }

        wifiLock = (context!!.getSystemService(Context.WIFI_SERVICE) as WifiManager)
                .createWifiLock(WifiManager.WIFI_MODE_FULL, "mylock")
    }

    @Synchronized
    fun playRecitation(context: Context?, SurahNo: Int, verseNo: Int) {
        if (bus == null) {
            pendingSurahNo = SurahNo
            pendingVerseNo = verseNo
            return
        }
        if (mediaPlayer != null) {
            stopRecitation()
        } else {
            initMediaPlayer(context)
        }

        isPlaying = true
        bus!!.post(VersePlayerEvent(VersePlayerEvent.Type.Play))
        try {
            val selectedRecitations = settings.selectedRecitation
            wifiLock.acquire()
            mediaPlayer!!.setDataSource(Utils.getVerseRecitationUrl(selectedRecitations.subfolder, SurahNo, verseNo))
        } catch (e: IOException) {
            e.printStackTrace()
        }

        mediaPlayer!!.setOnPreparedListener { mp -> mp.start() }
        mediaPlayer!!.setOnCompletionListener { stopRecitation(true) }
        mediaPlayer!!.prepareAsync()
    }


}

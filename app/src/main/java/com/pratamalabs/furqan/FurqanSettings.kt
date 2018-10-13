package com.pratamalabs.furqan

import android.annotation.SuppressLint
import android.app.AlertDialog
import android.content.Context
import android.content.DialogInterface
import android.content.SharedPreferences
import android.graphics.Typeface
import android.preference.PreferenceManager
import android.util.Pair
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import com.koushikdutta.async.future.FutureCallback
import com.pratamalabs.furqan.events.RecitationsFinishedLoadingEvent
import com.pratamalabs.furqan.models.Recitation
import com.pratamalabs.furqan.models.Translation
import com.pratamalabs.furqan.repository.FurqanDao
import com.pratamalabs.furqan.services.EventBus
import com.pratamalabs.furqan.services.Utils
import org.jetbrains.anko.coroutines.experimental.bg
import java.io.File
import java.io.IOException
import java.util.*

@SuppressLint("StaticFieldLeak")
/**
 * Created by pratamalabs on 14/6/13.
 */

object FurqanSettings {

    internal var dao = FurqanDao

    internal var eventBus = EventBus

    internal var context: Context = FurqanApp.instance
    internal var typefaceKeyCache: String? = null
    //    private Map<String, Typeface> typefaceChoice;
    internal var typeface: Typeface? = null
    private var selectedTranslations: List<Translation> = ArrayList()
    private val recitations = ArrayList<Recitation>()

    val availableRecitationsName: List<String>
        get() {
            val recitationsText = ArrayList<String>()
            for (recitation in recitations) {
                recitationsText.add(recitation.title)
            }
            return recitationsText
        }

    val selectedRecitation: Recitation
        get() = recitations[PreferenceManager.getDefaultSharedPreferences(context).getInt("recitation", 0)]

    val arabicTextSize: Float
        get() = java.lang.Float.valueOf(PreferenceManager.getDefaultSharedPreferences(context).getString(ARABIC_SIZE, "36"))

    val globalLastRead: Pair<Int, Int>
        get() {
            val sharedPreferences = PreferenceManager.getDefaultSharedPreferences(context)
            val verseNo = sharedPreferences.getInt("lastReadVerseNo", 1)
            val surahNo = sharedPreferences.getInt("lastReadSurahNo", 1)
            return Pair.create(surahNo, verseNo)
        }

    val textSize: Float
        get() = java.lang.Float.valueOf(PreferenceManager.getDefaultSharedPreferences(context).getString(TEXT_SIZE, "16"))

    //this upgrades the old version which uses a different key
    //default is scheherazade.
    val arabicTypeface: Typeface
        get() {
            var typefaceKey = PreferenceManager.getDefaultSharedPreferences(context).getString(ARABIC_TYPEFACE, "ScheherazadeRegOT.ttf")
            if (typefaceKey != typefaceKeyCache) {
                if (!typefaceKey!!.endsWith(".ttf")) {
                    typefaceKey = "ScheherazadeRegOT.ttf"
                }
                typeface = Typeface.createFromAsset(context.assets, typefaceKey)
            }
            return typeface!!
        }

    fun getRecitations(): List<Recitation> {
        return recitations
    }

    open fun refreshRecitation(callback: FutureCallback<Boolean>?) {
        bg {
            try {
                val gson = Gson()
                val listType = object : TypeToken<ArrayList<Recitation>>() {

                }.type
                if (recitations.size == 0) {
                    recitations.addAll(gson.fromJson<Any>(Utils.stringFromInputStream(context.assets.open("recitations.js")), listType) as Collection<Recitation>)
                }
                val folder = context.getExternalFilesDir(null)
                val subfolders = HashMap<String, File>()
                for (subFolder in folder!!.listFiles()) {
                    subfolders[subFolder.name] = subFolder
                }

                for (recitation in recitations) {
                    val subfolder = subfolders[recitation.subfolder]
                    if (subfolder != null) {
                        recitation.downloaded = true
                        recitation.downloadedVerseCount = subfolder.listFiles().size
                    } else {
                        recitation.downloaded = false
                        recitation.downloadedVerseCount = 0
                    }
                }

                eventBus.post(RecitationsFinishedLoadingEvent())
                callback?.onCompleted(null, true)
            } catch (e: IOException) {
                e.printStackTrace()
                callback?.onCompleted(e, false)
            }
        }
    }

    fun showRecitationsDownloadDialog(context: Context, listener: DialogInterface.OnClickListener?) {
        val recitationsText = availableRecitationsName
        val dialog = AlertDialog.Builder(context)
                .setTitle("Recitations")
                .setSingleChoiceItems(recitationsText.toTypedArray(), PreferenceManager.getDefaultSharedPreferences(context).getInt("recitation", 0)) { dialog, which ->
                    PreferenceManager.getDefaultSharedPreferences(context).edit().putInt("recitation", which).commit()
                    listener?.onClick(dialog, which)
                }
                .setPositiveButton("Ok") { dialog, which -> }.create()
        dialog.show()
    }

    @JvmOverloads
    fun showRecitationsDialog(context: Context, listener: DialogInterface.OnClickListener? = null) {
        val recitationsText = availableRecitationsName
        val dialog = AlertDialog.Builder(context)
                .setTitle("Recitations")
                .setSingleChoiceItems(recitationsText.toTypedArray(), PreferenceManager.getDefaultSharedPreferences(context).getInt("recitation", 0)) { dialog, which ->
                    PreferenceManager.getDefaultSharedPreferences(context).edit().putInt("recitation", which).commit()
                    listener?.onClick(dialog, which)
                }
                .setPositiveButton("Ok") { dialog, which -> }.create()
        dialog.show()
    }

    fun setGlobalLastRead(surahNo: Int, verseNo: Int) {
        val editor = PreferenceManager.getDefaultSharedPreferences(context).edit()
        editor.putInt("lastReadVerseNo", verseNo)
        editor.putInt("lastReadSurahNo", surahNo)
        editor.apply()
    }

    fun setSurahLastRead(surahNo: Int, verseNo: Int) {
        val editor = PreferenceManager.getDefaultSharedPreferences(context).edit()
        editor.putInt("lastRead" + surahNo.toString(), verseNo)
        editor.apply()
    }

    fun getSurahLastRead(surahNo: Int): Int {
        val sharedPreferences = PreferenceManager.getDefaultSharedPreferences(context)
        return sharedPreferences.getInt("lastRead" + surahNo.toString(), 1)
    }

    fun setSurahTag(key: Int, tag: String) {
        PreferenceManager.getDefaultSharedPreferences(context.applicationContext).edit().putString(key.toString(), tag).apply()
    }

    fun getSurahTag(key: Int): String {
        return PreferenceManager.getDefaultSharedPreferences(context.applicationContext).getString(key.toString(), "")
    }

    fun setTranslationFold(key: String, visible: Boolean) {
        PreferenceManager.getDefaultSharedPreferences(context.applicationContext).edit().putBoolean(key, visible).apply()
    }

    fun setTranslationFold(preference: SharedPreferences, key: String, visible: Boolean) {
        preference.edit().putBoolean(key, visible).apply()
    }

    fun getTranslationFold(key: String): Boolean {
        return PreferenceManager.getDefaultSharedPreferences(context.applicationContext).getBoolean(key, false)
    }

    fun refreshTranslations() {
        selectedTranslations = dao.availableTranslations
    }

    @Synchronized
    fun getSelectedTranslations(): List<Translation> {
        if (selectedTranslations.size == 0) {
            refreshTranslations()
        }
        return selectedTranslations
    }
    init {
        refreshRecitation(null)
    }

    val ARABIC_TYPEFACE = "arabic_typeface"
    val TEXT_SIZE = "text_size"
    val ARABIC_SIZE = "arabic_size"
}

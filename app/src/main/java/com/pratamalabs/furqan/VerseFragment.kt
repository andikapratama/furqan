package com.pratamalabs.furqan

/**
 * Created by pratamalabs on 17/9/13.
 */

import android.app.AlertDialog
import android.content.Intent
import android.content.SharedPreferences
import android.os.Bundle
import android.preference.PreferenceManager
import android.support.v4.app.Fragment
import android.text.Html
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.AdapterView
import android.widget.ListView
import com.pratamalabs.furqan.events.ShareVerseEvent
import com.pratamalabs.furqan.events.VerseUpdatedEvent
import com.pratamalabs.furqan.models.Translation
import com.pratamalabs.furqan.models.Verse
import com.pratamalabs.furqan.repository.FurqanDao
import com.pratamalabs.furqan.services.EventBus
import com.squareup.otto.Subscribe
import kotlinx.coroutines.experimental.android.UI
import kotlinx.coroutines.experimental.async
import org.apache.commons.lang3.StringUtils
import org.jetbrains.anko.coroutines.experimental.bg
import java.util.*

/**
 * A dummy fragment representing a section of the app, but that simply
 * displays dummy text.
 */
class VerseFragment : Fragment() {

    internal var dao = FurqanDao.get()
    internal var settings = FurqanSettings.get()
    internal var bus = EventBus.get()

    internal val preference: SharedPreferences by lazy {
        PreferenceManager.getDefaultSharedPreferences(activity!!.applicationContext)
    }
    private var surahNo: Int = 0
    private var verseNo: Int = 0
    private var mverse: Verse? = null
    private var listView: ListView? = null
    private var adapter: VerseListAdapter? = null
    internal var listener: SharedPreferences.OnSharedPreferenceChangeListener = SharedPreferences.OnSharedPreferenceChangeListener { sharedPreferences, s ->
        if (keys.contains(s)) {
            adapter!!.notifyDataSetChanged()
        }
    }

    override fun onResume() {
        super.onResume()
        if (adapter != null) {
            adapter!!.notifyDataSetChanged()
        }
        bus.register(this)
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        preference.registerOnSharedPreferenceChangeListener(listener)
    }

    override fun onDestroy() {
        preference.unregisterOnSharedPreferenceChangeListener(listener)
        super.onDestroy()
    }

    override fun onPause() {
        super.onPause()
        bus.unregister(this)
    }

    internal fun loadVerse() {

        async(UI) {

            val loadFirst = bg {
                mverse = dao.getVerse(surahNo, verseNo, settings.selectedTranslations)
                for (translation in settings.selectedTranslations) {
                    keys.add(translation.tanzilId)
                }
            }
            loadFirst.await()

            adapter = VerseListAdapter(mverse, activity, settings, dao, bus)
            listView!!.adapter = adapter
        }
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?,
                              savedInstanceState: Bundle?): View? {
        val rootView = inflater.inflate(R.layout.fragment_main_dummy, container, false)
        listView = rootView.findViewById<View>(R.id.listView) as ListView
        surahNo = arguments!!.getInt(Constants.SURAH_NUMBER, 1)
        verseNo = arguments!!.getInt(Constants.VERSE_NUMBER, 1)

        loadVerse()

        listView!!.onItemClickListener = AdapterView.OnItemClickListener { adapterView, view, i, l ->
            val viewHolder = VerseListAdapter.ViewHolder(view)
            val trans = adapterView.getItemAtPosition(i) as Translation

            if (viewHolder.isFolded) {
                viewHolder.setFold(i == 0, false)
            } else {
                viewHolder.setFold(i == 0, true)
            }
            settings.setTranslationFold(preference, if (i == 0) "arabic" else trans.tanzilId.toString(), viewHolder.isFolded)
        }

        return rootView
    }

    @Subscribe
    fun onVerseUpdated(event: VerseUpdatedEvent) {
        if (event.surahNo != surahNo || event.verseNo != verseNo)
            return
        loadVerse()
    }


    @Subscribe
    fun onShareVerse(event: ShareVerseEvent) {
        if (event.surahNo != surahNo || event.verseNo != verseNo || mverse == null)
            return

        val names = ArrayList<String>(mverse!!.translations.size + 2)
        names.add("Arabic")
        for (translation in mverse!!.translations.keys) {
            names.add(translation.translator)
        }
        val hasNote = !StringUtils.isBlank(mverse!!.note)
        if (hasNote) {
            names.add("Note")
        }

        val dialog = AlertDialog.Builder(activity)
                .setItems(names.toTypedArray()) { dialog, which ->
                    val textToShare: String
                    if (which == 0) {
                        textToShare = String.format("'%s', %s %d:%d", mverse!!.arabicText, dao.getSurahNo(surahNo).name, surahNo, verseNo)
                    } else if (hasNote && names.size == which + 1) {
                        textToShare = String.format("'%s', Notes on %s %d:%d", mverse!!.note, dao.getSurahNo(surahNo).name, surahNo, verseNo)
                    } else {
                        val translation = settings.selectedTranslations[which - 1]
                        val text = Html.fromHtml(mverse!!.translations[translation]).toString()
                        textToShare = String.format("'%s', %s, on %s %d:%d", text, translation.translator, dao.getSurahNo(surahNo).name, surahNo, verseNo)
                    }
                    val sendIntent = Intent()
                    sendIntent.action = Intent.ACTION_SEND
                    sendIntent.putExtra(Intent.EXTRA_TEXT, textToShare)
                    sendIntent.type = "text/plain"
                    startActivity(Intent.createChooser(sendIntent, resources.getText(R.string.send_to)))
                }
                .create()
        dialog.show()
    }

    companion object {
        val keys: MutableSet<String> = Utilities.newHashSet(FurqanSettings.ARABIC_SIZE,
                FurqanSettings.ARABIC_TYPEFACE,
                "arabic",
                FurqanSettings.TEXT_SIZE)
    }

}

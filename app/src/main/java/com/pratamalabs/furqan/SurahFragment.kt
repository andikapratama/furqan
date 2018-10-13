package com.pratamalabs.furqan

import android.app.AlertDialog
import android.content.Intent
import android.os.Bundle
import android.support.v4.app.ListFragment
import android.text.Editable
import android.text.TextWatcher
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.AdapterView
import android.widget.Filterable
import android.widget.ListView
import com.pratamalabs.furqan.events.GoToEvent
import com.pratamalabs.furqan.models.Surah
import com.pratamalabs.furqan.repository.FurqanDao
import com.pratamalabs.furqan.services.EventBus
import com.squareup.otto.Subscribe
import kotlinx.android.synthetic.main.list_filter.*
import kotlinx.coroutines.experimental.android.UI
import kotlinx.coroutines.experimental.async
import org.jetbrains.anko.coroutines.experimental.bg
import java.util.*

/**
 * Created by pratamalabs on 30/6/13.
 */
open class SurahFragment : ListFragment() {

    internal var dao = FurqanDao.get()

    internal var settings = FurqanSettings.get()


    internal var bus = EventBus

    internal lateinit var adapter: SurahListAdapter

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        val view = inflater.inflate(R.layout.list_filter, container, false)
        return view
    }

    override fun onListItemClick(l: ListView?, v: View?, position: Int, id: Long) {
        super.onListItemClick(l, v, position, id)
        val intent = Intent(activity, VerseActivity::class.java)
        intent.putExtra(VerseActivity.SURAH_NUMBER, id.toInt())
        startActivity(intent)
    }


    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        init()

        filterEditText.addTextChangedListener(object : TextWatcher {
            override fun beforeTextChanged(s: CharSequence, start: Int, count: Int, after: Int) {

            }

            override fun onTextChanged(s: CharSequence, start: Int, before: Int, count: Int) {

            }

            override fun afterTextChanged(s: Editable) {
                val text = filterEditText.text.toString().toLowerCase(Locale.getDefault())
                if (listAdapter is Filterable) {
                    (listAdapter as Filterable).filter.filter(text)
                }
            }
        })
    }


    //    @Background
    internal open fun loadSurah() {
        async(UI) {
            bg {
                adapter = SurahListAdapter(activity, dao.allSurah, settings)
            }.await()
            listAdapter = adapter
        }
    }


    //    @AfterViews
    internal fun init() {
        loadSurah()
        listView.onItemLongClickListener = AdapterView.OnItemLongClickListener { adapterView, view, n, l ->
            val alertDialog = AlertDialog.Builder(activity)
                    .setItems(arrayOf("Open", "Tag as 'Read'", "Tag as 'Memorized'", "Clear Tag")) { dialogInterface, i ->
                        val surah = adapterView.getItemAtPosition(n) as Surah

                        when (i) {
                            0 -> onListItemClick(listView, view, n, l)
                            1 -> {
                                settings.setSurahTag(surah.no, "read")
                                adapter.notifyDataSetChanged()
                            }
                            2 -> {
                                settings.setSurahTag(surah.no, "memorized")
                                adapter.notifyDataSetChanged()
                            }
                            3 -> {
                                settings.setSurahTag(surah.no, "")
                                adapter.notifyDataSetChanged()
                            }
                        }
                    }
                    .create()
            alertDialog.show()
            false
        }
        bus.register(this)
    }

    override fun onDestroy() {
        bus.unregister(this)
        super.onDestroy()
    }

    @Subscribe
    fun onGoTo(event: GoToEvent) {
        val intent = Intent(activity, VerseActivity::class.java)
        intent.putExtra(VerseActivity.SURAH_NUMBER, event.surahNo)
        intent.putExtra(VerseActivity.VERSE_NUMBER, event.verseNo)
        startActivity(intent)
    }



}

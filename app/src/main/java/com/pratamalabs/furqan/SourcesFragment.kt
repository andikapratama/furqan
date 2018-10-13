package com.pratamalabs.furqan

import android.app.AlertDialog
import android.app.ProgressDialog
import android.os.Bundle
import android.support.v4.app.ListFragment
import android.text.Editable
import android.text.TextWatcher
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.BaseAdapter
import android.widget.Filterable
import android.widget.ListView
import android.widget.Toast
import com.koushikdutta.async.future.FutureCallback
import com.koushikdutta.ion.Ion
import com.pratamalabs.furqan.events.SourceDownloadEvent
import com.pratamalabs.furqan.events.SourcesUpdatedEvent
import com.pratamalabs.furqan.models.Source
import com.pratamalabs.furqan.repository.FurqanDao
import com.pratamalabs.furqan.services.EventBus
import com.pratamalabs.furqan.services.TranslationsService
import com.pratamalabs.furqan.views.dragsortlistview.DragSortListView
import com.squareup.otto.Subscribe
import kotlinx.android.synthetic.main.sources_activity.*
import kotlinx.coroutines.experimental.android.UI
import kotlinx.coroutines.experimental.async
import org.jetbrains.anko.coroutines.experimental.bg
import java.io.File
import java.util.*
import java.util.concurrent.Future

/**
 * Created by pratamalabs on 23/7/13.
 */

open class SourcesFragment : ListFragment() {

    internal var dao = FurqanDao.get()
    internal var settings = FurqanSettings.get()
    internal var service = TranslationsService

    internal var bus = EventBus


    internal var downloading: Future<File>? = null

    internal var progressDialog: ProgressDialog? = null
    internal var isStarted = false
    private var adapter: BaseAdapter? = null

    @Subscribe
    fun updateSourceList(event: SourcesUpdatedEvent) {
        loadSources()
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        val view = inflater!!.inflate(R.layout.sources_activity, container, false)
        return view
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        loadSources()

        filterEditText!!.visibility = View.GONE
        filterDividerLine!!.visibility = View.GONE

        filterEditText!!.addTextChangedListener(object : TextWatcher {
            override fun beforeTextChanged(s: CharSequence, start: Int, count: Int, after: Int) {

            }

            override fun onTextChanged(s: CharSequence, start: Int, before: Int, count: Int) {

            }

            override fun afterTextChanged(s: Editable) {
                val text = filterEditText!!.text.toString().toLowerCase(Locale.getDefault())
                if (listAdapter is Filterable) {
                    (listAdapter as Filterable).filter.filter(text)
                }
            }
        })
    }

    internal open fun loadSources() {
        async(UI) {
            bg {
                val arrayList = dao.availableSources
                adapter = SourceListAdapter(activity, arrayList, dao, bus, settings)
            }.await()

            if (isStarted) {
                listAdapter = adapter
                val list = listView
                val controller = SourceSectionController(list, (adapter as SourceListAdapter?)!!)
                list.setDropListener(adapter as SourceListAdapter?)
                list.setFloatViewManager(controller)
                list.setOnTouchListener(controller)
                list.setOnDragListener { v, event ->
                    controller.setmDivPos((adapter as SourceListAdapter).divPosition)
                    false
                }

                val text = filterEditText!!.text.toString().toLowerCase(Locale.getDefault())
                if (listAdapter is Filterable) {
                    (listAdapter as Filterable).filter.filter(text)
                }
            }
        }
    }

    override fun getListView(): DragSortListView {
        return super.getListView() as DragSortListView
    }

    override fun onStart() {
        super.onStart()
        bus.register(this)
        isStarted = true
    }

    override fun onResume() {
        super.onResume()
        settings.refreshTranslations()
    }

    override fun onStop() {
        bus.unregister(this)
        super.onStop()
    }


    internal fun resetDownload() {
        // cancel any pending upload
        downloading!!.cancel(true)
        downloading = null
        // reset the ui
        try {
            if (progressDialog != null)
                progressDialog!!.dismiss()
        } catch (ex: Exception) {
            //empty
        }

    }

    private fun downloadAndEnable(source: Source) {
        if (downloading != null && !downloading!!.isCancelled) {
            resetDownload()
            return
        }

        progressDialog = ProgressDialog.show(activity, "Downloading...", source.name, false, false
        ) { Toast.makeText(activity, "Cancelled download of " + source.name, Toast.LENGTH_LONG).show() }

        downloading = Ion.with(activity!!)
                .load(source.downloadLink)
                .progressDialog(progressDialog)
                .write(activity!!.getFileStreamPath("zip-" + System.currentTimeMillis() + ".zip"))
                .setCallback(FutureCallback { e, result ->
                    resetDownload()
                    if (e != null) {
                        Toast.makeText(activity, "Error downloading " + source.name, Toast.LENGTH_LONG).show()
                        return@FutureCallback
                    }
                    val success = service.saveTanzilTranslationResourceAndEnableIt(source.id, result)
                    result.delete()
                    if (success) {
                        Toast.makeText(activity, source.name + " added", Toast.LENGTH_LONG).show()
                    } else {
                        Toast.makeText(activity, "Error downloading " + source.name, Toast.LENGTH_LONG).show()
                        return@FutureCallback
                    }
                    loadSources()
                })
    }

    @Subscribe
    fun onSourceDownload(event: SourceDownloadEvent) {
        confirmDownload(event.source)
    }

    private fun confirmDownload(source: Source) {
        if (source.status == "notdownloaded") {
            AlertDialog.Builder(activity)
                    .setTitle("Add translation")
                    .setMessage("Download the source and enable it?")
                    .setNegativeButton("Cancel", null)
                    .setPositiveButton("Yes") { dialogInterface, i -> downloadAndEnable(source) }
                    .show()
        }
    }

    override fun onListItemClick(l: ListView?, v: View?, position: Int, id: Long) {
        super.onListItemClick(l, v, position, id)
        val source = adapter!!.getItem(position) as Source
        confirmDownload(source)
    }

}

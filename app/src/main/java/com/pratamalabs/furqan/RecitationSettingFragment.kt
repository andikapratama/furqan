package com.pratamalabs.furqan

import android.app.AlertDialog
import android.app.ProgressDialog
import android.content.DialogInterface
import android.os.Bundle
import android.support.v4.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.AdapterView
import android.widget.Toast
import com.pratamalabs.furqan.events.RecitationDeleteCache
import com.pratamalabs.furqan.events.RecitationShouldStartDownloading
import com.pratamalabs.furqan.services.EventBus
import com.pratamalabs.furqan.services.ValueHolder
import com.pratamalabs.furqan.services.VerseRecitationService
import com.squareup.otto.Subscribe
import kotlinx.android.synthetic.main.fragment_recitation_setting.*
import kotlinx.coroutines.experimental.android.UI
import kotlinx.coroutines.experimental.async

/**
 * A placeholder fragment containing a simple view.
 */

open class RecitationSettingFragment : Fragment() {

    internal var settings = FurqanSettings.get()

    internal var eventBus = EventBus

    internal var recitationService = VerseRecitationService.get()

    internal var adapter: RecitationListAdapter? = null


    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        init()
    }

    fun init() {
        eventBus.register(this)
        adapter = recitationListView.adapter as? RecitationListAdapter
        if (adapter == null) {
            adapter = RecitationListAdapter(this.activity, eventBus, settings, recitationService)
            recitationListView.adapter = adapter
            recitationListView.onItemClickListener = AdapterView.OnItemClickListener { adapterView, view, i, l -> }
        }
    }

    @Subscribe
    fun onRecitationStartDownload(e: RecitationShouldStartDownloading) {
        val stopped = ValueHolder.from(false)
        val recitation = e.recitation

        val context = this.activity ?: return
        val pd = ProgressDialog(context)
        pd.setProgressStyle(ProgressDialog.STYLE_HORIZONTAL)
        pd.setMessage(recitation.title)
        pd.setTitle("Downloading")
        pd.setCancelable(true)
        pd.isIndeterminate = false
        pd.setButton(DialogInterface.BUTTON_NEUTRAL, "Pause") { dialogInterface, i -> stopped.value = true }
        pd.setButton(DialogInterface.BUTTON_NEGATIVE, "Cancel & Delete") { dialogInterface, i -> stopped.value = true }
        pd.setCanceledOnTouchOutside(false)
        pd.setOnCancelListener { stopped.value = true }
        pd.max = 6236
        pd.show()


        recitationService.downloadFullRecitation(context, e.recitation, stopped, pd) { e, result ->
            if (result!!) {
                Toast.makeText(context, recitation.title + " downloaded Successfully", Toast.LENGTH_LONG).show()
            } else if (e != null) {
                Toast.makeText(context, recitation.title + " download fails! Error: " + e.message, Toast.LENGTH_LONG).show()
            } else {
                Toast.makeText(context, recitation.title + " download paused", Toast.LENGTH_LONG).show()
            }
            settings.refreshRecitation { e, result -> refreshList(pd) }
        }
    }

    open fun refreshList(pd: ProgressDialog) {
        async(UI) {
            pd.dismiss()
            activity!!.runOnUiThread {
                adapter!!.recitations = settings.recitations
                adapter!!.notifyDataSetChanged()
            }
        }
    }

    @Subscribe
    fun onRecitationDelete(e: RecitationDeleteCache) {

        if (this.activity == null) return
        AlertDialog.Builder(this.activity)
                .setTitle("Confirm Delete")
                .setMessage("Delete the recitation?")
                .setNegativeButton("Cancel", null)
                .setPositiveButton("Yes") { dialogInterface, i ->
                    val pd = ProgressDialog.show(activity, "", "Deleting..", true, false, null)
                    recitationService.deleteFullRecitation(activity, e.recitation) { e, result -> settings.refreshRecitation { e, result -> refreshList(pd) } }
                }
                .show()
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?,
                              savedInstanceState: Bundle?): View? {
        return inflater.inflate(R.layout.fragment_recitation_setting, container, false)
    }
}

package com.pratamalabs.furqan;

import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ListView;
import android.widget.Toast;

import com.koushikdutta.async.future.FutureCallback;
import com.pratamalabs.furqan.events.RecitationDeleteCache;
import com.pratamalabs.furqan.events.RecitationShouldStartDownloading;
import com.pratamalabs.furqan.models.Recitation;
import com.pratamalabs.furqan.services.EventBus;
import com.pratamalabs.furqan.services.ValueHolder;
import com.pratamalabs.furqan.services.VerseRecitationService;
import com.squareup.otto.Subscribe;

import org.androidannotations.annotations.AfterViews;
import org.androidannotations.annotations.Bean;
import org.androidannotations.annotations.EFragment;
import org.androidannotations.annotations.UiThread;
import org.androidannotations.annotations.ViewById;

/**
 * A placeholder fragment containing a simple view.
 */

@EFragment(R.layout.fragment_recitation_setting)
public class RecitationSettingFragment extends Fragment {

    @ViewById(R.id.recitationListView)
    ListView recitationListView;

    FurqanSettings settings = FurqanSettings.get();

    EventBus eventBus = EventBus.get();

    @Bean
    VerseRecitationService recitationService;

    RecitationListAdapter adapter;

    public RecitationSettingFragment() {
    }

    @AfterViews
    public void init() {
        eventBus.register(this);
        adapter = (RecitationListAdapter) recitationListView.getAdapter();
        if (adapter == null) {
            adapter = new RecitationListAdapter(this.getActivity(), eventBus, settings, recitationService);
            recitationListView.setAdapter(adapter);
            recitationListView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
                @Override
                public void onItemClick(AdapterView<?> adapterView, View view, int i, long l) {

                }
            });
        }
    }

    @Subscribe
    public void onRecitationStartDownload(RecitationShouldStartDownloading e) {
        final ValueHolder<Boolean> stopped = ValueHolder.from(false);
        final Recitation recitation = e.recitation;

        final Context context = this.getActivity();
        if (context == null) return;
        final ProgressDialog pd = new ProgressDialog(context);
        pd.setProgressStyle(ProgressDialog.STYLE_HORIZONTAL);
        pd.setMessage(recitation.getTitle());
        pd.setTitle("Downloading");
        pd.setCancelable(true);
        pd.setIndeterminate(false);
        pd.setButton(DialogInterface.BUTTON_NEUTRAL, "Pause", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialogInterface, int i) {
                stopped.value = true;
            }
        });
        pd.setButton(DialogInterface.BUTTON_NEGATIVE, "Cancel & Delete", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialogInterface, int i) {
                stopped.value = true;
            }
        });
        pd.setCanceledOnTouchOutside(false);
        pd.setOnCancelListener(new DialogInterface.OnCancelListener() {
            @Override
            public void onCancel(DialogInterface dialogInterface) {
                stopped.value = true;
            }
        });
        pd.setMax(6236);
        pd.show();


        recitationService.downloadFullRecitation(context, e.recitation, stopped, pd, new FutureCallback<Boolean>() {
            @Override
            public void onCompleted(Exception e, Boolean result) {
                if (result) {
                    Toast.makeText(context, recitation.getTitle() + " downloaded Successfully", Toast.LENGTH_LONG).show();
                } else if (e != null) {
                    Toast.makeText(context, recitation.getTitle() + " download fails! Error: " + e.getMessage(), Toast.LENGTH_LONG).show();
                } else {
                    Toast.makeText(context, recitation.getTitle() + " download paused", Toast.LENGTH_LONG).show();
                }
                settings.refreshRecitation(new FutureCallback<Boolean>() {
                    @Override
                    public void onCompleted(Exception e, Boolean result) {
                        refreshList(pd);
                    }
                });
            }
        });
    }

    @UiThread
    public void refreshList(ProgressDialog pd) {
        pd.dismiss();
        getActivity().runOnUiThread(new Runnable() {
            @Override
            public void run() {
                adapter.recitations = settings.getRecitations();
                adapter.notifyDataSetChanged();
            }
        });
    }

    @Subscribe
    public void onRecitationDelete(final RecitationDeleteCache e) {

        if (this.getActivity() == null) return;
        new AlertDialog.Builder(this.getActivity())
                .setTitle("Confirm Delete")
                .setMessage("Delete the recitation?")
                .setNegativeButton("Cancel", null)
                .setPositiveButton("Yes", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialogInterface, int i) {
                        final ProgressDialog pd = ProgressDialog.show(getActivity(), "", "Deleting..", true, false, null);
                        recitationService.deleteFullRecitation(getActivity(), e.recitation, new FutureCallback<Boolean>() {
                            @Override
                            public void onCompleted(Exception e, Boolean result) {
                                settings.refreshRecitation(new FutureCallback<Boolean>() {
                                    @Override
                                    public void onCompleted(Exception e, Boolean result) {
                                        refreshList(pd);
                                    }
                                });
                            }
                        });
                    }
                })
                .show();
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_recitation_setting, container, false);
    }
}

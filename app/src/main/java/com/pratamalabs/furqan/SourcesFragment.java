package com.pratamalabs.furqan;

import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.content.DialogInterface;
import android.os.Bundle;
import android.support.v4.app.ListFragment;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.DragEvent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.EditText;
import android.widget.Filterable;
import android.widget.ListView;
import android.widget.Toast;

import com.koushikdutta.async.future.FutureCallback;
import com.koushikdutta.ion.Ion;
import com.pratamalabs.furqan.events.SourceDownloadEvent;
import com.pratamalabs.furqan.events.SourcesUpdatedEvent;
import com.pratamalabs.furqan.models.Source;
import com.pratamalabs.furqan.repository.FurqanDao;
import com.pratamalabs.furqan.services.EventBus;
import com.pratamalabs.furqan.services.TranslationsService;
import com.pratamalabs.furqan.views.dragsortlistview.DragSortListView;
import com.squareup.otto.Subscribe;

import org.androidannotations.annotations.Background;
import org.androidannotations.annotations.Bean;
import org.androidannotations.annotations.EFragment;
import org.androidannotations.annotations.UiThread;

import java.io.File;
import java.util.List;
import java.util.Locale;
import java.util.concurrent.Future;

import butterknife.ButterKnife;
import butterknife.InjectView;

/**
 * Created by pratamalabs on 23/7/13.
 */

@EFragment
public class SourcesFragment extends ListFragment {

    @Bean
    FurqanDao dao;
    @Bean
    FurqanSettings settings;
    @Bean
    TranslationsService service;
    @Bean
    EventBus bus;
    @InjectView(R.id.filterEditText)
    EditText filterEditText;

    @InjectView(R.id.filterDividerLine)
    View filterDividerLine;

    Future<File> downloading;

    ProgressDialog progressDialog;
    boolean isStarted = false;
    private BaseAdapter adapter;

    @Subscribe
    public void updateSourceList(SourcesUpdatedEvent event) {
        loadSources();
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.sources_activity, container, false);
        ButterKnife.inject(this, view);

        loadSources();

        filterEditText.setVisibility(View.GONE);
        filterDividerLine.setVisibility(View.GONE);

        filterEditText.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {

            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {

            }

            @Override
            public void afterTextChanged(Editable s) {
                String text = filterEditText.getText().toString().toLowerCase(Locale.getDefault());
                if (getListAdapter() instanceof Filterable) {
                    ((Filterable) getListAdapter()).getFilter().filter(text);
                }
            }
        });

        return view;
    }

    @Background
    void loadSources() {
        List<Source> arrayList = dao.getAvailableSources();
        adapter = new SourceListAdapter(getActivity(), arrayList, dao, bus, settings);
        refreshList();
    }

    @Override
    public DragSortListView getListView() {
        return (DragSortListView) super.getListView();
    }

    @UiThread(propagation = UiThread.Propagation.REUSE)
    public void refreshList() {
        if (!isStarted)
            return;

        setListAdapter(adapter);
        DragSortListView list = getListView();
        final SourceSectionController controller = new SourceSectionController(list, (SourceListAdapter) adapter);
        list.setDropListener((SourceListAdapter) adapter);
        list.setFloatViewManager(controller);
        list.setOnTouchListener(controller);
        list.setOnDragListener(new View.OnDragListener() {
            @Override
            public boolean onDrag(View v, DragEvent event) {
                controller.setmDivPos(((SourceListAdapter) adapter).getDivPosition());
                return false;
            }
        });

        String text = filterEditText.getText().toString().toLowerCase(Locale.getDefault());
        if (getListAdapter() instanceof Filterable) {
            ((Filterable) getListAdapter()).getFilter().filter(text);
        }
    }

    @Override
    public void onStart() {
        super.onStart();
        bus.register(this);
        isStarted = true;
    }

    @Override
    public void onResume() {
        super.onResume();
        settings.refreshTranslations();
    }

    @Override
    public void onStop() {
        bus.unregister(this);
        super.onStop();
    }


    void resetDownload() {
// cancel any pending upload
        downloading.cancel(true);
        downloading = null;
// reset the ui
        try {
            if (progressDialog != null)
                progressDialog.dismiss();
        } catch (Exception ex) {
            //empty
        }
    }

    private void downloadAndEnable(final Source source) {
        if (downloading != null && !downloading.isCancelled()) {
            resetDownload();
            return;
        }

        progressDialog = ProgressDialog.show(getActivity(), "Downloading...", source.name, false, false, new DialogInterface.OnCancelListener() {
                    @Override
                    public void onCancel(DialogInterface dialog) {
                        Toast.makeText(getActivity(), "Cancelled download of " + source.name, Toast.LENGTH_LONG).show();
                    }
                }
        );

        downloading = Ion.with(getActivity())
                .load(source.downloadLink)
                .progressDialog(progressDialog)
                .write(getActivity().getFileStreamPath("zip-" + System.currentTimeMillis() + ".zip"))
                .setCallback(new FutureCallback<File>() {
                    @Override
                    public void onCompleted(Exception e, File result) {
                        resetDownload();
                        if (e != null) {
                            Toast.makeText(getActivity(), "Error downloading " + source.name, Toast.LENGTH_LONG).show();
                            return;
                        }
                        service.saveTanzilTranslationResourceAndEnableIt(source.id, result);
                        result.delete();
                        Toast.makeText(getActivity(), source.name + " added", Toast.LENGTH_LONG).show();
                        loadSources();
                    }
                });
    }

    @Subscribe
    public void onSourceDownload(SourceDownloadEvent event) {
        confirmDownload(event.source);
    }

    private void confirmDownload(final Source source) {
        if (source.status.equals("notdownloaded")) {
            new AlertDialog.Builder(getActivity())
                    .setTitle("Add translation")
                    .setMessage("Download the source and enable it?")
                    .setNegativeButton("Cancel", null)
                    .setPositiveButton("Yes", new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialogInterface, int i) {
                            downloadAndEnable(source);
                        }
                    })
                    .show();
        }
    }

    @Override
    public void onListItemClick(ListView l, View v, int position, long id) {
        super.onListItemClick(l, v, position, id);
        final Source source = (Source) adapter.getItem(position);
        confirmDownload(source);
    }

}

package com.pratamalabs.furqan;

import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.support.v4.app.ListFragment;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.EditText;
import android.widget.Filterable;
import android.widget.ListView;

import com.pratamalabs.furqan.events.GoToEvent;
import com.pratamalabs.furqan.models.Surah;
import com.pratamalabs.furqan.repository.FurqanDao;
import com.pratamalabs.furqan.services.EventBus;
import com.squareup.otto.Subscribe;

import org.androidannotations.annotations.AfterViews;
import org.androidannotations.annotations.Background;
import org.androidannotations.annotations.Bean;
import org.androidannotations.annotations.EFragment;
import org.androidannotations.annotations.UiThread;

import java.util.Locale;

import butterknife.ButterKnife;
import butterknife.InjectView;

/**
 * Created by pratamalabs on 30/6/13.
 */
@EFragment
public class SurahFragment extends ListFragment {

    @Bean
    FurqanDao dao;

    @Bean
    FurqanSettings settings;

    @InjectView(R.id.filterEditText)
    EditText filterEditText;

    @Bean
    EventBus bus;

    SurahListAdapter adapter;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.list_filter, container, false);
        ButterKnife.inject(this, view);

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


    @Override
    public void onListItemClick(ListView l, View v, int position, long id) {
        super.onListItemClick(l, v, position, id);
        Intent intent = new Intent(getActivity(), VerseActivity_.class);
        intent.putExtra(VerseActivity.SURAH_NUMBER, (int) id);
        startActivity(intent);
    }


    @Override
    public void onViewCreated(View view, Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
    }


    @Background
    void loadSurah() {
        adapter = new SurahListAdapter(getActivity(), dao.getAllSurah(), settings);
        showSurah();
    }

    @UiThread
    void showSurah() {
        setListAdapter(adapter);
    }

    @AfterViews
    void init() {
        loadSurah();
        getListView().setOnItemLongClickListener(new AdapterView.OnItemLongClickListener() {
            @Override
            public boolean onItemLongClick(final AdapterView<?> adapterView, final View view, final int n, final long l) {
                AlertDialog alertDialog = new AlertDialog.Builder(getActivity())
                        .setItems(new String[]{"Open", "Tag as 'Read'", "Tag as 'Memorized'", "Clear Tag"}, new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialogInterface, int i) {
                                Surah surah = (Surah) adapterView.getItemAtPosition(n);

                                switch (i) {
                                    case 0:
                                        onListItemClick(getListView(), view, n, l);
                                        break;
                                    case 1:
                                        settings.setSurahTag(surah.getNo(), "read");
                                        adapter.notifyDataSetChanged();
                                        break;
                                    case 2:
                                        settings.setSurahTag(surah.getNo(), "memorized");
                                        adapter.notifyDataSetChanged();
                                        break;
                                    case 3:
                                        settings.setSurahTag(surah.getNo(), "");
                                        adapter.notifyDataSetChanged();
                                        break;
                                }
                            }
                        })
                        .create();
                alertDialog.show();
                return false;
            }
        });
        bus.register(this);
    }

    @Override
    public void onDestroy() {
        bus.unregister(this);
        super.onDestroy();
    }

    @Subscribe
    public void onGoTo(GoToEvent event) {
        Intent intent = new Intent(getActivity(), VerseActivity_.class);
        intent.putExtra(VerseActivity.SURAH_NUMBER, event.surahNo);
        intent.putExtra(VerseActivity.VERSE_NUMBER, event.verseNo);
        startActivity(intent);
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }


}

package com.pratamalabs.furqan;

/**
 * Created by pratamalabs on 17/9/13.
 */

import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.support.v4.app.Fragment;
import android.text.Html;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ListView;

import com.pratamalabs.furqan.events.ShareVerseEvent;
import com.pratamalabs.furqan.events.VerseUpdatedEvent;
import com.pratamalabs.furqan.models.Translation;
import com.pratamalabs.furqan.models.Verse;
import com.pratamalabs.furqan.repository.FurqanDao;
import com.pratamalabs.furqan.services.EventBus;
import com.squareup.otto.Subscribe;

import org.androidannotations.annotations.AfterViews;
import org.androidannotations.annotations.Background;
import org.androidannotations.annotations.Bean;
import org.androidannotations.annotations.EFragment;
import org.androidannotations.annotations.UiThread;
import org.apache.commons.lang3.StringUtils;

import java.util.ArrayList;
import java.util.Set;

/**
 * A dummy fragment representing a section of the app, but that simply
 * displays dummy text.
 */
@EFragment
public class VerseFragment extends Fragment {
    public static final Set<String> keys = Utilities.newHashSet(FurqanSettings.ARABIC_SIZE,
            FurqanSettings.ARABIC_TYPEFACE,
            "arabic",
            FurqanSettings.TEXT_SIZE);
    @Bean
    FurqanDao dao;
    @Bean
    FurqanSettings settings;
    @Bean
    EventBus bus;
    SharedPreferences preference;
    private int surahNo;
    private int verseNo;
    private Verse mverse;
    private ListView listView;
    private VerseListAdapter adapter;
    SharedPreferences.OnSharedPreferenceChangeListener listener = new SharedPreferences.OnSharedPreferenceChangeListener() {
        @Override
        public void onSharedPreferenceChanged(SharedPreferences sharedPreferences, String s) {
            if (keys.contains(s)) {
                adapter.notifyDataSetChanged();
            }
        }
    };

    @Override
    public void onResume() {
        super.onResume();
        settings.setGlobalLastRead(surahNo, verseNo);
        settings.setSurahLastRead(surahNo, verseNo);
        if(adapter!=null){
            adapter.notifyDataSetChanged();
        }
        bus.register(this);
    }

    @AfterViews
    void init() {
        preference.registerOnSharedPreferenceChangeListener(listener);
    }

    @Override
    public void onDestroy() {
        preference.unregisterOnSharedPreferenceChangeListener(listener);
        super.onDestroy();
    }

    @Override
    public void onPause() {
        super.onPause();
        bus.unregister(this);
    }

    @Background
    void loadVerse() {
        mverse = dao.getVerse(surahNo, verseNo, settings.getSelectedTranslations());
        for (Translation translation : settings.getSelectedTranslations()) {
            keys.add(translation.getTanzilId());
        }
        showVerse();
    }

    @UiThread(propagation = UiThread.Propagation.REUSE)
    void showVerse() {
        adapter = new VerseListAdapter(mverse, getActivity(), settings, dao, bus);
        listView.setAdapter(adapter);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        preference = PreferenceManager.getDefaultSharedPreferences(getActivity().getApplicationContext());
        View rootView = inflater.inflate(R.layout.fragment_main_dummy, container, false);
        listView = (ListView) rootView.findViewById(R.id.listView);
        surahNo = getArguments().getInt(Constants.SURAH_NUMBER, 1);
        verseNo = getArguments().getInt(Constants.VERSE_NUMBER, 1);

        loadVerse();

        listView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> adapterView, View view, int i, long l) {
                VerseListAdapter.ViewHolder viewHolder = new VerseListAdapter.ViewHolder(view);
                Translation trans = (Translation) adapterView.getItemAtPosition(i);

                if (viewHolder.isFolded()) {
                    viewHolder.setFold(i == 0, false);
                } else {
                    viewHolder.setFold(i == 0, true);
                }
                settings.setTranslationFold(preference, i == 0 ? "arabic" : String.valueOf(trans.getTanzilId()), viewHolder.isFolded());
            }
        });

        return rootView;
    }

    @Subscribe
    public void onVerseUpdated(VerseUpdatedEvent event) {
        if (event.surahNo != surahNo || event.verseNo != verseNo)
            return;
        loadVerse();
    }


    @Subscribe
    public void onShareVerse(ShareVerseEvent event) {
        if (event.surahNo != surahNo || event.verseNo != verseNo || mverse == null)
            return;

        final ArrayList<String> names = new ArrayList<String>(mverse.getTranslations().size() + 2);
        names.add("Arabic");
        for (Translation translation : mverse.getTranslations().keySet()) {
            names.add(translation.getTranslator());
        }
        final boolean hasNote = !StringUtils.isBlank(mverse.getNote());
        if (hasNote) {
            names.add("Note");
        }

        AlertDialog dialog = new AlertDialog.Builder(getActivity())
                .setItems(names.toArray(new String[names.size()]), new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        String textToShare;
                        if (which == 0) {
                            textToShare = String.format("'%s', %s %d:%d", mverse.getArabicText(), dao.getSurahNo(surahNo).getName(), surahNo, verseNo);
                        } else if (hasNote && names.size() == (which + 1)) {
                            textToShare = String.format("'%s', Notes on %s %d:%d", mverse.getNote(), dao.getSurahNo(surahNo).getName(), surahNo, verseNo);
                        } else {
                            Translation translation = settings.getSelectedTranslations().get(which - 1);
                            String text = Html.fromHtml(mverse.getTranslations().get(translation)).toString();
                            textToShare = String.format("'%s', %s, on %s %d:%d", text, translation.getTranslator(), dao.getSurahNo(surahNo).getName(), surahNo, verseNo);
                        }
                        Intent sendIntent = new Intent();
                        sendIntent.setAction(Intent.ACTION_SEND);
                        sendIntent.putExtra(Intent.EXTRA_TEXT, textToShare);
                        sendIntent.setType("text/plain");
                        startActivity(Intent.createChooser(sendIntent, getResources().getText(R.string.send_to)));
                    }
                })
                .create();
        dialog.show();
    }

}

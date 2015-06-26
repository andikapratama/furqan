package com.pratamalabs.furqan;

import android.content.Intent;
import android.os.Build;
import android.os.Bundle;
import android.support.v4.app.ListFragment;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.EditText;
import android.widget.Filterable;
import android.widget.ListView;

import com.pratamalabs.furqan.models.Note;
import com.pratamalabs.furqan.repository.FurqanDao;

import org.androidannotations.annotations.Background;
import org.androidannotations.annotations.Bean;
import org.androidannotations.annotations.EFragment;
import org.androidannotations.annotations.UiThread;

import java.util.List;
import java.util.Locale;

import butterknife.ButterKnife;
import butterknife.InjectView;

/**
 * Created by KatulSomin on 11/08/2014.
 */
@EFragment
public class NotesFragment extends ListFragment {

    @Bean
    FurqanDao dao;

    @InjectView(R.id.filterEditText)
    EditText filterEditText;

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
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    @Background
    void loadNotes() {
        List<Note> results = dao.getNotes();
        showNotes(results);
    }

    @UiThread
    void showNotes(List<Note> notes) {
        if (notes.size() == 0) {
            String[] emptyList = new String[]{"You have no notes.."};
            setListAdapter(new ArrayAdapter<String>(getActivity(), android.R.layout.simple_list_item_1, emptyList));
        } else {
            setListAdapter(new NoteListAdapter(getActivity(), notes, dao));
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN_MR1) {
                getListView().setTextAlignment(View.TEXT_ALIGNMENT_CENTER);
            }
        }
    }

    @Override
    public void onResume() {
        super.onResume();
        loadNotes();
    }

    @Override
    public void onListItemClick(ListView l, View v, int position, long id) {
        Object item = l.getAdapter().getItem(position);
        if (item instanceof Note) {
            Note note = (Note) item;
            Intent intent = new Intent(getActivity(), VerseActivity_.class);
            intent.putExtra(VerseActivity.SURAH_NUMBER, note.getSurahNo());
            intent.putExtra(VerseActivity.VERSE_NUMBER, note.getNumber());
            startActivity(intent);
        }
        super.onListItemClick(l, v, position, id);
    }
}

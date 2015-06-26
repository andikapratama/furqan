package com.pratamalabs.furqan;

import android.app.SearchManager;
import android.content.Context;
import android.content.Intent;
import android.os.AsyncTask;
import android.os.Build;
import android.os.Bundle;
import android.support.v4.app.NavUtils;
import android.support.v7.app.ActionBarActivity;
import android.support.v7.widget.SearchView;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.inputmethod.InputMethodManager;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.ListView;

import com.pratamalabs.furqan.models.SearchResult;
import com.pratamalabs.furqan.repository.FurqanDao;

import org.androidannotations.annotations.AfterViews;
import org.androidannotations.annotations.Bean;
import org.androidannotations.annotations.EActivity;
import org.androidannotations.annotations.ViewById;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by pratamalabs on 1/10/13.
 */
@EActivity(R.layout.plain_list)
public class SearchResultActivity extends ActionBarActivity {

    @Bean
    FurqanDao dao;

    @ViewById(R.id.search_list)
    ListView list;

    List<SearchResult> results = new ArrayList();
    SearchView searchView;
    String query;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    @AfterViews
    void init(){
        handleIntent(getIntent());
//        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        list.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> adapterView, View view, int i, long l) {

                if (results.size() > i) {
                    SearchResult sr = results.get(i);
                    Intent intent = new Intent(SearchResultActivity.this, VerseActivity_.class);
                    intent.putExtra(VerseActivity.SURAH_NUMBER, sr.getSurahNo());
                    intent.putExtra(VerseActivity.VERSE_NUMBER, sr.getNumber());
                    startActivity(intent);
                }
            }
        });
    }

    /**
     * Callback function
     */
    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        /** Create an option menu from res/menu/items.xml */

        getMenuInflater().inflate(R.menu.search, menu);

        MenuItem mi = menu.findItem(R.id.search);
        searchView =
                (SearchView) mi.getActionView();

        mi.expandActionView();
        searchView.setQuery(query, false);
        InputMethodManager imm = (InputMethodManager) getSystemService(Context.INPUT_METHOD_SERVICE);
        imm.hideSoftInputFromWindow(searchView.getWindowToken(), 0);
        searchView.clearFocus();
        searchView.setIconified(false);
        searchView.setOnQueryTextListener(new SearchView.OnQueryTextListener() {
            @Override
            public boolean onQueryTextSubmit(String query) {
                search(query);
                return true;
            }

            @Override
            public boolean onQueryTextChange(String newText) {
                return false;
            }
        });
        return super.onCreateOptionsMenu(menu);
    }

    @Override
    protected void onNewIntent(Intent intent) {
        handleIntent(intent);
    }

    private void search(final String query) {
        this.query = query;
        String[] emptyList = new String[]{"Searching.."};
        list.setAdapter(new ArrayAdapter<String>(this, android.R.layout.simple_list_item_1, emptyList));

        new AsyncTask<Void, Void, Void>() {
            @Override
            protected Void doInBackground(Void... voids) {
                results = dao.searchFurqanContaining(query);
                return null;
            }

            @Override
            protected void onPostExecute(Void aVoid) {
                if (results.size() == 0) {
                    String[] emptyList = new String[]{"No result found."};
                    list.setAdapter(new ArrayAdapter<String>(SearchResultActivity.this, android.R.layout.simple_list_item_1, emptyList));
                } else {
                    list.setAdapter(new SearchResultAdapter(SearchResultActivity.this, results, query, dao));
                    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN_MR1) {
                        list.setTextAlignment(View.TEXT_ALIGNMENT_CENTER);
                    }
                }
                super.onPostExecute(aVoid);
            }
        }.execute();
        //use the query to search your data somehow
    }

    private void handleIntent(Intent intent) {
        if (Intent.ACTION_SEARCH.equals(intent.getAction())) {
            search(intent.getStringExtra(SearchManager.QUERY));
        }
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            // Respond to the action bar's Up/Home button
            case android.R.id.home:
                NavUtils.navigateUpFromSameTask(this);
                return true;
        }
        return super.onOptionsItemSelected(item);
    }

}

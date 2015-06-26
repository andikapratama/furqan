package com.pratamalabs.furqan;

import android.app.ActionBar;
import android.app.FragmentTransaction;
import android.app.SearchManager;
import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentPagerAdapter;
import android.support.v4.view.ViewPager;
import android.support.v7.app.ActionBarActivity;
import android.support.v7.widget.SearchView;
import android.util.Pair;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;

import com.astuetz.PagerSlidingTabStrip;
import com.crashlytics.android.Crashlytics;
import com.pratamalabs.furqan.services.TranslationsService;
import com.pratamalabs.furqan.services.Utils;

import org.androidannotations.annotations.AfterViews;
import org.androidannotations.annotations.Bean;
import org.androidannotations.annotations.EActivity;
import org.androidannotations.annotations.ViewById;

import java.util.Locale;

@EActivity(R.layout.activity_start)
public class StartActivity extends ActionBarActivity implements ActionBar.TabListener {

    /**
     * The {@link android.support.v4.view.PagerAdapter} that will provide
     * fragments for each of the sections. We use a
     * {@link FragmentPagerAdapter} derivative, which will keep every
     * loaded fragment in memory. If this becomes too memory intensive, it
     * may be best to switch to a
     * {@link android.support.v13.app.FragmentStatePagerAdapter}.
     */
    SectionsPagerAdapter mSectionsPagerAdapter;

    /**
     * The {@link ViewPager} that will host the section contents.
     */
    @ViewById(R.id.pager)
    ViewPager mViewPager;

    @ViewById(R.id.tabs)
    PagerSlidingTabStrip tabStrip;

    @Bean
    FurqanSettings settings;

    @Bean
    TranslationsService service;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        Crashlytics.start(this);
    }

    @AfterViews
    void init() {
        if (mViewPager.getAdapter() == null) {
            mSectionsPagerAdapter = new SectionsPagerAdapter(getSupportFragmentManager());
            mViewPager.setAdapter(mSectionsPagerAdapter);
            tabStrip.setViewPager(mViewPager);
            int themeColor = getResources().getColor(android.R.color.holo_blue_dark);
            tabStrip.setIndicatorColor(themeColor);

            mViewPager.setOffscreenPageLimit(2);
        } else {
            mSectionsPagerAdapter = (SectionsPagerAdapter) mViewPager.getAdapter();
        }

        if (Utils.isLandscape(this)) {
            tabStrip.setVisibility(View.GONE);

            final ActionBar actionBar = getActionBar();
            actionBar.setNavigationMode(ActionBar.NAVIGATION_MODE_TABS);
            mViewPager.setOnPageChangeListener(new ViewPager.SimpleOnPageChangeListener() {
                @Override
                public void onPageSelected(int position) {
                    actionBar.setSelectedNavigationItem(position);
                }
            });

            // For each of the sections in the app, add a tab to the action bar.
            for (int i = 0; i < mSectionsPagerAdapter.getCount(); i++) {
                actionBar.addTab(
                        actionBar.newTab()
                                .setText(mSectionsPagerAdapter.getPageTitle(i))
                                .setTabListener(this)
                );
            }
        } else {

            tabStrip.setVisibility(View.VISIBLE);
        }
        Uri data = getIntent().getData();
        if (data != null) {
            getIntent().setData(null);
            try {
                importData(data);
            } catch (Exception e) {
                // warn user about bad data here
                finish();
            }
        }
    }

    private void importData(Uri data) {
        service.importNotes(this, data);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.main, menu);

        // Associate searchable configuration with the SearchView
        SearchManager searchManager =
                (SearchManager) getSystemService(Context.SEARCH_SERVICE);
        SearchView searchView =
                (SearchView) menu.findItem(R.id.search).getActionView();
        searchView.setSearchableInfo(
                searchManager.getSearchableInfo(getComponentName()));


        return true;
    }


    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle item selection
        Intent intent;
        switch (item.getItemId()) {
            case R.id.action_settings:

                intent = new Intent(this, SettingsActivity_.class);
                startActivityForResult(intent, 0);
                return true;
            case R.id.action_lastread:
                intent = new Intent(this, VerseActivity_.class);
                Pair<Integer, Integer> pair = settings.getGlobalLastRead();
                intent.putExtra(VerseActivity.SURAH_NUMBER, pair.first);
                intent.putExtra(VerseActivity.VERSE_NUMBER, pair.second - 1);
                startActivity(intent);
                return true;

            case R.id.action_goto:
                GoToDialog dialog = GoToDialog.newInstance(-1, mViewPager.getCurrentItem() + 1);
                dialog.show(getSupportFragmentManager(), "goto");
                return true;
            default:
                return super.onOptionsItemSelected(item);
        }
    }

    @Override
    public void onTabSelected(ActionBar.Tab tab, FragmentTransaction fragmentTransaction) {
        // When the given tab is selected, switch to the corresponding page in
        // the ViewPager.
        mViewPager.setCurrentItem(tab.getPosition());
    }

    @Override
    public void onTabUnselected(ActionBar.Tab tab, FragmentTransaction fragmentTransaction) {
    }

    @Override
    public void onTabReselected(ActionBar.Tab tab, FragmentTransaction fragmentTransaction) {
    }

    /**
     * A {@link FragmentPagerAdapter} that returns a fragment corresponding to
     * one of the sections/tabs/pages.
     */
    public class SectionsPagerAdapter extends FragmentPagerAdapter {

        public SectionsPagerAdapter(FragmentManager fm) {
            super(fm);
        }

        @Override
        public Fragment getItem(int position) {
            switch (position) {
                case 1:
                    return new NotesFragment_();
                case 2:
                    return new SourcesFragment_();
                default:
                    return new SurahFragment_();
            }
        }

        @Override
        public int getCount() {
            // Show 3 total pages.
            return 3;
        }

        @Override
        public CharSequence getPageTitle(int position) {
            Locale l = Locale.getDefault();
            switch (position) {
                case 0:
                    return getString(R.string.title_section1);
                case 1:
                    return getString(R.string.title_section2);
                case 2:
                    return getString(R.string.title_section3);
            }
            return null;
        }
    }
}

package com.pratamalabs.furqan;

import android.app.ActionBar;
import android.app.FragmentTransaction;
import android.app.SearchManager;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentPagerAdapter;
import android.support.v4.app.FragmentStatePagerAdapter;
import android.support.v4.view.ViewPager;
import android.support.v7.app.ActionBarActivity;
import android.support.v7.widget.SearchView;
import android.util.TypedValue;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.Toast;

import com.astuetz.PagerSlidingTabStrip;
import com.pratamalabs.furqan.events.GoToEvent;
import com.pratamalabs.furqan.events.ShareVerseEvent;
import com.pratamalabs.furqan.events.VersePlayerEvent;
import com.pratamalabs.furqan.models.Surah;
import com.pratamalabs.furqan.repository.FurqanDao;
import com.pratamalabs.furqan.services.EventBus;
import com.pratamalabs.furqan.services.VersePlayerFragment;
import com.pratamalabs.furqan.services.VersePlayerFragment_;
import com.squareup.otto.Subscribe;

import org.androidannotations.annotations.Bean;
import org.androidannotations.annotations.EActivity;

@EActivity
public class VerseActivity extends ActionBarActivity implements ActionBar.TabListener {

    public static final String SURAH_NUMBER = "surah_number";
    public static final String VERSE_NUMBER = "verse_number";
    /**
     * The {@link android.support.v4.view.PagerAdapter} that will provide
     * fragments for each of the sections. We use a
     * {@link FragmentPagerAdapter} derivative, which
     * will keep every loaded fragment in memory. If this becomes too memory
     * intensive, it may be best to switch to a
     * {@link FragmentStatePagerAdapter}.
     */
    SectionsPagerAdapter mSectionsPagerAdapter;
    Surah mSurah;

    @Bean
    FurqanDao dao;
    @Bean
    FurqanSettings settings;

    @Bean
    EventBus bus;

    VersePlayerFragment player;

    ViewPager mViewPager;
    private int currentColor = 0xFF666666;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        int themeColor = getResources().getColor(android.R.color.holo_blue_dark);

        int surahNo = getIntent().getExtras().getInt(SURAH_NUMBER, 1);
        int verseNo = getIntent().getExtras().getInt(VERSE_NUMBER, 0) - 1;

        if (verseNo < 0) {
            verseNo = settings.getSurahLastRead(surahNo) - 2;
        }

        player = (VersePlayerFragment) getSupportFragmentManager().findFragmentByTag("player");
        if (player == null) {
            player = new VersePlayerFragment_();
            player.setRetainInstance(true);
            getSupportFragmentManager().beginTransaction().add(player, "player").commit();
        }

        mSurah = dao.getSurah(surahNo);

        this.setTitle(mSurah.getName());


        // Create the adapter that will return a fragment for each of the three
        // primary sections of the app.
        mSectionsPagerAdapter = new SectionsPagerAdapter(getSupportFragmentManager());

        // Set up the ViewPager with the sections adapter.
        mViewPager = (ViewPager) findViewById(R.id.pager);
        mViewPager.setAdapter(mSectionsPagerAdapter);

        final int pageMargin = (int) TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, 4, getResources()
                .getDisplayMetrics());
        mViewPager.setPageMargin(pageMargin);
        mViewPager.setCurrentItem(verseNo);

        // Bind the widget to the adapter
        PagerSlidingTabStrip tabs = (PagerSlidingTabStrip) findViewById(R.id.tabs);
        tabs.setViewPager(mViewPager);
        tabs.setIndicatorColor(themeColor);

        settings.refreshTranslations();
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.verse, menu);

        MenuItem play = menu.findItem(R.id.action_play);
        MenuItem stop = menu.findItem(R.id.action_stop);
        if (player.isPlaying()) {
            play.setVisible(false);
            stop.setVisible(true);
        } else {
            play.setVisible(true);
            stop.setVisible(false);
        }

        // Associate searchable configuration with the SearchView
        SearchManager searchManager =
                (SearchManager) getSystemService(Context.SEARCH_SERVICE);
        SearchView searchView =
                (SearchView) menu.findItem(R.id.search).getActionView();
        searchView.setSearchableInfo(
                searchManager.getSearchableInfo(getComponentName()));


        return true;
    }

    @Subscribe
    public void onGoTo(GoToEvent event) {
        if (mSurah.getNo() == event.surahNo) {
            mViewPager.setCurrentItem(event.verseNo - 1);
        } else {
            Intent intent = new Intent(this, VerseActivity_.class);
            intent.putExtra(VerseActivity.SURAH_NUMBER, event.surahNo);
            intent.putExtra(VerseActivity.VERSE_NUMBER, event.verseNo);
            startActivity(intent);
        }

    }

    @Subscribe
    public void onVersePlayer(VersePlayerEvent event) {
        if (event.type == VersePlayerEvent.Type.Error) {
            Toast.makeText(this, "Error streaming the recitation. Check your connection and try again", Toast.LENGTH_LONG).show();
        }
        invalidateOptionsMenu();
    }

    @Override
    protected void onStart() {
        bus.register(this);
        super.onStart();
    }

    @Override
    protected void onStop() {
        bus.unregister(this);
        super.onStop();
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
            case R.id.action_goto:
                GoToDialog dialog = GoToDialog.newInstance(mSurah.getNo(), mViewPager.getCurrentItem() + 1);
                dialog.show(getSupportFragmentManager(), "goto");
                return true;
            case R.id.action_play:
                player.playRecitation(this, mSurah.getNo(), mViewPager.getCurrentItem() + 1);
                return true;
            case R.id.action_stop:
                player.stopRecitation();
                return true;
            case R.id.action_recitation_choice:
                settings.showRecitationsDialog(this);
                return true;
            case R.id.action_share:
                bus.post(new ShareVerseEvent(mSurah.getNo(), mViewPager.getCurrentItem() + 1));
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
    public class SectionsPagerAdapter extends FragmentStatePagerAdapter {

        public SectionsPagerAdapter(FragmentManager fm) {
            super(fm);
        }

        @Override
        public Fragment getItem(int position) {
            // getItem is called to instantiate the fragment for the given page.
            // Return a VerseFragment (defined as a static inner class
            // below) with the page number as its lone argument.
            Fragment fragment = new VerseFragment_();
            Bundle args = new Bundle();
            args.putInt(Constants.SURAH_NUMBER, mSurah.getNo());
            args.putInt(Constants.VERSE_NUMBER, position + 1);
            fragment.setArguments(args);
            return fragment;
        }

        @Override
        public int getCount() {
            return mSurah.getVerseCount();
        }

        @Override
        public CharSequence getPageTitle(int position) {
            return String.valueOf(position + 1);
        }
    }
}

package com.pratamalabs.furqan

import android.app.ActionBar
import android.app.FragmentTransaction
import android.app.SearchManager
import android.content.Context
import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.support.v4.app.Fragment
import android.support.v4.app.FragmentManager
import android.support.v4.app.FragmentPagerAdapter
import android.support.v4.view.ViewPager
import android.support.v7.app.AppCompatActivity
import android.support.v7.widget.SearchView
import android.view.Menu
import android.view.MenuItem
import android.view.View
import com.crashlytics.android.Crashlytics
import com.pratamalabs.furqan.services.TranslationsService
import com.pratamalabs.furqan.services.Utils
import io.fabric.sdk.android.Fabric
import kotlinx.android.synthetic.main.activity_start.*
import java.util.*

class StartActivity : AppCompatActivity(), ActionBar.TabListener {

    /**
     * The [android.support.v4.view.PagerAdapter] that will provide
     * fragments for each of the sections. We use a
     * [FragmentPagerAdapter] derivative, which will keep every
     * loaded fragment in memory. If this becomes too memory intensive, it
     * may be best to switch to a
     * [android.support.v13.app.FragmentStatePagerAdapter].
     */
    internal lateinit var mSectionsPagerAdapter: SectionsPagerAdapter

    /**
     * The [ViewPager] that will host the section contents.
     */

    internal var settings = FurqanSettings.get()

    internal var service: TranslationsService? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        setContentView(R.layout.activity_start)

        Fabric.with(this, Crashlytics())
        init()
    }

    internal fun init() {
        if (pager.adapter == null) {
            tabs.shouldExpand = true
            mSectionsPagerAdapter = SectionsPagerAdapter(supportFragmentManager)
            pager.adapter = mSectionsPagerAdapter
            tabs.setViewPager(pager)
            val themeColor = resources.getColor(android.R.color.holo_blue_dark)
            tabs.indicatorColor = themeColor

            pager.offscreenPageLimit = 2
        } else {
            mSectionsPagerAdapter = pager.adapter as SectionsPagerAdapter
        }

        if (Utils.isLandscape(this)) {
            tabs.visibility = View.GONE

            val actionBar = actionBar
            actionBar!!.navigationMode = ActionBar.NAVIGATION_MODE_TABS
            pager.setOnPageChangeListener(object : ViewPager.SimpleOnPageChangeListener() {
                override fun onPageSelected(position: Int) {
                    actionBar.setSelectedNavigationItem(position)
                }
            })

            // For each of the sections in the app, add a tab to the action bar.
            for (i in 0 until mSectionsPagerAdapter.count) {
                actionBar.addTab(
                        actionBar.newTab()
                                .setText(mSectionsPagerAdapter.getPageTitle(i))
                                .setTabListener(this)
                )
            }
        } else {

            tabs.visibility = View.VISIBLE
        }
        val data = intent.data
        if (data != null) {
            intent.data = null
            try {
                importData(data)
            } catch (e: Exception) {
                // warn user about bad data here
                finish()
            }

        }
    }

    private fun importData(data: Uri) {
        service!!.importNotes(this, data)
    }

    override fun onCreateOptionsMenu(menu: Menu): Boolean {
        // Inflate the menu; this adds items to the action bar if it is present.
        menuInflater.inflate(R.menu.main, menu)

        // Associate searchable configuration with the SearchView
        val searchManager = getSystemService(Context.SEARCH_SERVICE) as SearchManager
        val searchView = menu.findItem(R.id.search).actionView as SearchView
        searchView.setSearchableInfo(
                searchManager.getSearchableInfo(componentName))


        return true
    }


    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        // Handle item selection
        val intent: Intent
        when (item.itemId) {
            R.id.action_settings -> {

                intent = Intent(this, SettingsActivity::class.java)
                startActivityForResult(intent, 0)
                return true
            }
            R.id.action_recitation_cache -> {

                intent = Intent(this, RecitationSetting::class.java)
                startActivityForResult(intent, 0)
                return true
            }
            R.id.action_lastread -> {
                intent = Intent(this, VerseActivity::class.java)
                val pair = settings.globalLastRead
                intent.putExtra(VerseActivity.SURAH_NUMBER, pair.first)
                intent.putExtra(VerseActivity.VERSE_NUMBER, pair.second - 1)
                startActivity(intent)
                return true
            }

            R.id.action_goto -> {
                val dialog = GoToDialog.newInstance(-1, pager.currentItem + 1)
                dialog.show(supportFragmentManager, "goto")
                return true
            }
            else -> return super.onOptionsItemSelected(item)
        }
    }

    override fun onTabSelected(tab: ActionBar.Tab, fragmentTransaction: FragmentTransaction) {
        // When the given tab is selected, switch to the corresponding page in
        // the ViewPager.
        pager.currentItem = tab.position
    }

    override fun onTabUnselected(tab: ActionBar.Tab, fragmentTransaction: FragmentTransaction) {}

    override fun onTabReselected(tab: ActionBar.Tab, fragmentTransaction: FragmentTransaction) {}

    /**
     * A [FragmentPagerAdapter] that returns a fragment corresponding to
     * one of the sections/tabs/pages.
     */
    inner class SectionsPagerAdapter(fm: FragmentManager) : FragmentPagerAdapter(fm) {

        override fun getItem(position: Int): Fragment {
            when (position) {
                1 -> return NotesFragment()
                2 -> return SourcesFragment()
                else -> return SurahFragment()
            }
        }

        override fun getCount(): Int {
            // Show 3 total pages.
            return 3
        }

        override fun getPageTitle(position: Int): CharSequence? {
            val l = Locale.getDefault()
            when (position) {
                0 -> return getString(R.string.title_section1)
                1 -> return getString(R.string.title_section2)
                2 -> return getString(R.string.title_section3)
            }
            return null
        }
    }
}

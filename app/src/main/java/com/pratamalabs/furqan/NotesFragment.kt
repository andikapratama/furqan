package com.pratamalabs.furqan

import android.content.Intent
import android.os.Build
import android.os.Bundle
import android.support.v4.app.ListFragment
import android.text.Editable
import android.text.TextWatcher
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ArrayAdapter
import android.widget.Filterable
import android.widget.ListView
import com.pratamalabs.furqan.models.Note
import com.pratamalabs.furqan.repository.FurqanDao
import kotlinx.android.synthetic.main.list_filter.*
import kotlinx.coroutines.experimental.android.UI
import kotlinx.coroutines.experimental.async
import org.jetbrains.anko.coroutines.experimental.bg
import java.util.*

/**
 * Created by KatulSomin on 11/08/2014.
 */
open class NotesFragment : ListFragment() {

    internal var dao = FurqanDao

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        val view = inflater.inflate(R.layout.list_filter, container, false)

        return view
    }

    override fun onStart() {
        super.onStart()

        filterEditText.addTextChangedListener(object : TextWatcher {
            override fun beforeTextChanged(s: CharSequence, start: Int, count: Int, after: Int) {

            }

            override fun onTextChanged(s: CharSequence, start: Int, before: Int, count: Int) {

            }

            override fun afterTextChanged(s: Editable) {
                val text = filterEditText!!.text.toString().toLowerCase(Locale.getDefault())
                if (listAdapter is Filterable) {
                    (listAdapter as Filterable).filter.filter(text)
                }
            }
        })
    }

    internal open fun loadNotes() {
        async(UI) {
            val notes = bg { dao.notes}.await()

            if (notes.size == 0) {
                val emptyList = arrayOf("You have no notes..")
                listAdapter = ArrayAdapter(activity!!, android.R.layout.simple_list_item_1, emptyList)
            } else {
                listAdapter = NoteListAdapter(activity, notes, dao)
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN_MR1) {
                    listView.textAlignment = View.TEXT_ALIGNMENT_CENTER
                }
            }
        }
    }


    override fun onResume() {
        super.onResume()
        loadNotes()
    }

    override fun onListItemClick(l: ListView?, v: View?, position: Int, id: Long) {
        val item = l!!.adapter.getItem(position)
        if (item is Note) {
            val intent = Intent(activity, VerseActivity::class.java)
            intent.putExtra(VerseActivity.SURAH_NUMBER, item.surahNo)
            intent.putExtra(VerseActivity.VERSE_NUMBER, item.number)
            startActivity(intent)
        }
        super.onListItemClick(l, v, position, id)
    }
}

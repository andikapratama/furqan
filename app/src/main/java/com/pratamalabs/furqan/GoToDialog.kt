package com.pratamalabs.furqan

import android.app.AlertDialog
import android.app.Dialog
import android.os.Bundle
import android.support.v4.app.DialogFragment
import android.text.Editable
import android.text.TextWatcher
import android.widget.EditText
import android.widget.TextView
import android.widget.Toast
import butterknife.ButterKnife
import com.pratamalabs.furqan.events.GoToEvent
import com.pratamalabs.furqan.models.Surah
import com.pratamalabs.furqan.repository.FurqanDao
import com.pratamalabs.furqan.services.EventBus
import org.apache.commons.lang3.StringUtils

/**
 * Created by KatulSomin on 28/09/2014.
 */
open class GoToDialog : DialogFragment() {

    internal var dao = FurqanDao.get()

    internal var bus = EventBus

    internal lateinit var mSurah: Surah
    internal lateinit var editTextVerse: EditText
    internal lateinit var editTextSurah: EditText
    internal lateinit var textViewSurahName: TextView
    internal var clearing = false

    fun clear() {
        clearing = true
        textViewSurahName.text = "Pick a surah first.."
        editTextSurah.setText("")
        editTextVerse.setText("")
        editTextVerse.isEnabled = false
        clearing = false
    }

    override fun onCreateDialog(savedInstanceState: Bundle?): Dialog {
        val surahNo = arguments!!.getInt(Constants.SURAH_NUMBER) - 1
        val builder = AlertDialog.Builder(activity)
        // Get the layout inflater, and inflate the dialog's view (which contains the EditText)
        val inflater = activity!!.layoutInflater
        val dialogView = inflater.inflate(R.layout.number_dialog, null)

        // Keep a reference to the EditText you can use when the user clicks the button
        editTextVerse = ButterKnife.findById(dialogView, R.id.editTextVerse)
        editTextSurah = ButterKnife.findById(dialogView, R.id.editTextSurah)
        textViewSurahName = ButterKnife.findById(dialogView, R.id.surahName)
        textViewSurahName.setTextColor(editTextSurah.hintTextColors)

        if (surahNo < 0) {
            editTextSurah.requestFocus()
            editTextVerse.isEnabled = false
        } else {
            mSurah = dao.allSurah[surahNo]
            textViewSurahName.text = mSurah.name
            editTextVerse.requestFocus()
            editTextSurah.setText(mSurah.no.toString())
        }

        editTextSurah.addTextChangedListener(object : TextWatcher {
            override fun beforeTextChanged(s: CharSequence, start: Int, count: Int, after: Int) {

            }

            override fun onTextChanged(s: CharSequence, start: Int, before: Int, count: Int) {

            }

            override fun afterTextChanged(s: Editable) {
                if (clearing) return
                val text = s.toString()

                if (StringUtils.isBlank(text)) {
                    clear()
                    return
                }

                var no = 0
                try {
                    no = Integer.parseInt(text)
                    if (no > 114) {
                        editTextSurah.setText("114")
                        no = 114
                    } else if (no < 0) {
                        no = 1
                        editTextSurah.setText("1")
                    }

                    no = no - 1
                    mSurah = dao.allSurah[no]
                    textViewSurahName.text = mSurah.name
                    editTextVerse.isEnabled = true
                } catch (e: Exception) {
                    clear()
                }

            }
        })
        editTextVerse.addTextChangedListener(object : TextWatcher {
            override fun beforeTextChanged(s: CharSequence, start: Int, count: Int, after: Int) {

            }

            override fun onTextChanged(s: CharSequence, start: Int, before: Int, count: Int) {

            }

            override fun afterTextChanged(s: Editable) {
                if (clearing) return
                val text = s.toString()
                if (StringUtils.isBlank(text)) {
                    return
                } else {
                    val no = Integer.parseInt(text) - 1
                    if (mSurah.verseCount <= no) {
                        editTextVerse.setText(mSurah.verseCount.toString())
                    } else if (no < 0) {
                        editTextVerse.setText("1")
                    }
                }
            }
        })

        // Inflate and set the layout for the dialog
        builder.setView(dialogView)
                .setTitle("Jump to..")
                // Add action buttons
                .setNegativeButton("Cancel") { dialog, which -> }
                .setPositiveButton(R.string.go) { dialog, id ->
                    // The user clicked 'ok' or 'sign-in' - now you can get the text from the EditText
                    val verseText = editTextVerse.text.toString()
                    val surahText = editTextSurah.text.toString()
                    var surahNo = -1
                    var verseNo = -1
                    try {
                        surahNo = Integer.parseInt(surahText)
                        verseNo = Integer.parseInt(verseText)
                        bus.post(GoToEvent(surahNo, verseNo))
                    } catch (e: Exception) {
                        Toast.makeText(activity, "The destination is invalid", Toast.LENGTH_LONG)
                    }
                }
        // Build & show the dialog
        return builder.create()
    }

    companion object {

        fun newInstance(surahNo: Int, verseNo: Int): GoToDialog {
            val frag = GoToDialog()
            val args = Bundle()
            args.putInt(Constants.SURAH_NUMBER, surahNo)
            args.putInt(Constants.VERSE_NUMBER, verseNo)
            frag.arguments = args
            return frag
        }
    }
}// Empty constructor required for DialogFragment

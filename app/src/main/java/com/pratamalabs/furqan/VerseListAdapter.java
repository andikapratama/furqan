/**
 * Created by pratamalabs on 25/6/13.
 */

package com.pratamalabs.furqan;

import android.app.Activity;
import android.text.Html;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;

import com.pratamalabs.furqan.models.Translation;
import com.pratamalabs.furqan.models.Verse;
import com.pratamalabs.furqan.repository.FurqanDao;
import com.pratamalabs.furqan.services.EventBus;

import org.apache.commons.lang3.StringUtils;

import butterknife.ButterKnife;
import butterknife.InjectView;
import butterknife.Optional;


public class VerseListAdapter extends BaseAdapter {

    public static final int ARABIC = 0;
    public static final int NOTE = 1;
    public static final int TRANSLATION = 2;
    Verse mVerse;
    Activity context;
    FurqanSettings settings;
    FurqanDao dao;
    EventBus eventBus;

    public VerseListAdapter(Verse mVerse, Activity context, FurqanSettings settings, FurqanDao dao, EventBus eventBus) {
        this.mVerse = mVerse;
        this.context = context;
        this.settings = settings;
        this.dao = dao;
        this.eventBus = eventBus;
    }

    @Override
    public int getViewTypeCount() {
        return 3;
    }

    @Override
    public int getItemViewType(int position) {
        if (position == 0) {
            return ARABIC;
        } else if (getCount() - 1 == position) {
            return NOTE;
        } else {
            return TRANSLATION;
        }
    }

    @Override
    public int getCount() {
        return mVerse.getTranslations().size() + 2;
    }

    @Override
    public Translation getItem(int i) {
        return i == 0 ? null : mVerse.getTranslationItems().get(i - 1);
    }

    @Override
    public long getItemId(int i) {
        return i;
    }

    View produceViewForType(int type) {
        LayoutInflater inflater = context.getLayoutInflater();
        View rowView;
        if (type == NOTE) {
            rowView = inflater.inflate(R.layout.verse_note_row, null);
        } else {
            rowView = inflater.inflate(R.layout.verse_row, null);
        }

        final ViewHolder viewHolder = new ViewHolder(rowView);

        rowView.setTag(viewHolder);
        return rowView;
    }

    public void saveNote(String s) {
        dao.setNote(mVerse.getSurahNo(), mVerse.getNumber(), s);
//        eventBus.post(new VerseUpdatedEvent(mVerse.getSurahNo(), mVerse.getNumber()));
    }

    @Override
    public View getView(int i, View convertView, ViewGroup viewGroup) {

        View rowView = convertView;
        int type = getItemViewType(i);

        if (rowView == null) {
            rowView = produceViewForType(type);
        }

        final ViewHolder holder = (ViewHolder) rowView.getTag();

        if (type == ARABIC) {
            holder.dataText.setTypeface(settings.getArabicTypeface());
            holder.dataText.setTextSize(settings.getArabicTextSize());
            holder.sourceText.setText("");

            ImageView imageView = (ImageView) rowView.findViewById(R.id.imageCollapsible);
            imageView.setVisibility(View.GONE);
            holder.padding.setVisibility(View.VISIBLE);
            holder.sourceText.setVisibility(View.GONE);
            holder.dataText.setText(Html.fromHtml(mVerse.getArabicText()));

            holder.dataText.setTextSize(settings.getArabicTextSize());
            holder.dataText.setTypeface(settings.getArabicTypeface());

            holder.setFold(true, settings.getTranslationFold("arabic"));

            return rowView;
        } else if (type == NOTE) {
            holder.sourceText.setText("Notes");
            if (!mVerse.getNote().equals("")) {
                holder.noteEditText.setText(mVerse.getNote());
            }
            holder.noteEditText.setTextSize(settings.getTextSize());

            holder.noteEditText.setOnFocusChangeListener(new View.OnFocusChangeListener() {
                @Override
                public void onFocusChange(View v, boolean hasFocus) {
                    String input;
                    EditText editText;

                    if (!hasFocus) {
                        editText = (EditText) v;
                        input = editText.getText().toString();
                        saveNote(input);
                    }
                }
            });
            return rowView;
        }

        Translation translation = getItem(i);
        String text = StringUtils.trimToEmpty(mVerse.getTranslations().get(translation));
        holder.dataText.setText(Html.fromHtml(text));
        holder.dataText.setTextSize(settings.getTextSize());
        holder.sourceText.setText(translation.getTranslator());
        holder.padding.setVisibility(View.GONE);

        holder.setFold(false, settings.getTranslationFold(String.valueOf(translation.getTanzilId())));

        return rowView;
    }

    public static class ViewHolder {
        @Optional
        @InjectView(R.id.noteEditText)
        public EditText noteEditText;

        @InjectView(R.id.sourceLabel)
        public TextView sourceText;

        @Optional
        @InjectView(R.id.verseText)
        public TextView dataText;

        @InjectView(R.id.imageCollapsible)
        public ImageView imageView;

        @Optional
        @InjectView(R.id.optional_arabic_padding)
        public View padding;

        public ViewHolder(View view) {
            ButterKnife.inject(this, view);
        }

        public boolean isFolded() {
            return dataText.getVisibility() == View.GONE;
        }

        public void setFold(boolean isArabic, boolean fold) {
            if (fold) {
                dataText.setVisibility(View.GONE);
                imageView.setImageResource(R.drawable.navigation_expand);
                if (isArabic) {
                    sourceText.setText("Arabic Text");
                    padding.setVisibility(View.GONE);
                    imageView.setVisibility(View.VISIBLE);
                    sourceText.setVisibility(View.VISIBLE);
                }
            } else {
                dataText.setVisibility(View.VISIBLE);
                imageView.setImageResource(R.drawable.navigation_collapse);

                if (isArabic) {
                    sourceText.setText("");
                    padding.setVisibility(View.VISIBLE);
                    imageView.setVisibility(View.GONE);
                    sourceText.setVisibility(View.GONE);
                }
            }
        }
    }
}


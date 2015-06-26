package com.pratamalabs.furqan;

import android.app.Activity;
import android.text.Html;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.Filter;
import android.widget.Filterable;
import android.widget.TextView;

import com.pratamalabs.furqan.models.Surah;

import org.apache.commons.lang3.StringUtils;

import java.util.ArrayList;
import java.util.List;

import butterknife.ButterKnife;
import butterknife.InjectView;

/**
 * Created by pratamalabs on 30/6/13.
 */
public class SurahListAdapter extends BaseAdapter implements Filterable {

    List<Surah> originalSurahs;
    List<Surah> surahs;
    Activity context;
    FurqanSettings settings;

    public SurahListAdapter(Activity context, List<Surah> surahs, FurqanSettings settings) {
        this.context = context;
        this.settings = settings;
        this.originalSurahs = surahs;
        this.surahs = surahs;
    }

    @Override
    public int getCount() {
        return surahs.size();
    }

    @Override
    public Surah getItem(int i) {
        return surahs.get(i);
    }

    @Override
    public long getItemId(int i) {
        return getItem(i).getNo();
    }

    @Override
    public Filter getFilter() {
        return new Filter() {
            @Override
            protected FilterResults performFiltering(CharSequence constraint) {
                List<Surah> filtered;
                if (StringUtils.isBlank(constraint)) {
                    filtered = originalSurahs;
                } else {
                    filtered = new ArrayList<Surah>();
                    for (Surah surah : originalSurahs) {
                        if (surah.isFound(constraint)) {
                            filtered.add(surah);
                        }
                    }
                }

                FilterResults results = new FilterResults();
                results.values = filtered;
                results.count = filtered.size();
                return results;
            }

            @Override
            protected void publishResults(CharSequence constraint, FilterResults results) {

                surahs = (List<Surah>) results.values;
                notifyDataSetChanged();
            }
        };
    }

    @Override
    public View getView(int i, View convertView, ViewGroup viewGroup) {

        View rowView = convertView;
        if (rowView == null) {
            rowView = context.getLayoutInflater().inflate(R.layout.sura_row, null);
            ViewHolder viewHolder = new ViewHolder(rowView);
            viewHolder.arabicTitle.setTypeface(settings.getArabicTypeface());
            viewHolder.arabicTitle.setTextSize(settings.getArabicTextSize());
            rowView.setTag(viewHolder);
        }

        ViewHolder holder = (ViewHolder) rowView.getTag();
        Surah surah = getItem(i);
        holder.additionalInfo.setText(surah.getVerseCount() + " verses, " + surah.getType());
        holder.transliterationTitle.setText(surah.getNo() + ". " + Html.fromHtml(surah.getName()));
        holder.translationTitle.setText(surah.getTranslationName());
        holder.arabicTitle.setText(surah.getArabicName());

        String tag = settings.getSurahTag(surah.getNo());
        if (StringUtils.isBlank(tag)) {
            holder.readTag.setVisibility(View.GONE);
        } else {
            holder.readTag.setVisibility(View.VISIBLE);
            if ("read".equals(tag)) {
                holder.readTag.setBackgroundResource(R.drawable.btn_small_green_normal);
                holder.readTag.setText("read");
            } else {
                holder.readTag.setBackgroundResource(R.drawable.btn_small_orange_normal);
                holder.readTag.setText("memorized");
            }
        }

        return rowView;
    }

    static class ViewHolder {
        @InjectView(R.id.arabicTitle)
        TextView arabicTitle;
        @InjectView(R.id.latinTitle)
        TextView transliterationTitle;
        @InjectView(R.id.translationTitle)
        TextView translationTitle;
        @InjectView(R.id.additionalInfo)
        TextView additionalInfo;
        @InjectView(R.id.read_tag)
        TextView readTag;

        public ViewHolder(View view) {
            ButterKnife.inject(this, view);
        }
    }
}

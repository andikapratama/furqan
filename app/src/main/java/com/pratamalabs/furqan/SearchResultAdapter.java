package com.pratamalabs.furqan;

import android.app.Activity;
import android.text.Html;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.TextView;

import com.pratamalabs.furqan.models.SearchResult;
import com.pratamalabs.furqan.repository.FurqanDao;

import java.util.ArrayList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import butterknife.ButterKnife;
import butterknife.InjectView;

/**
 * Created by pratamalabs on 2/10/13.
 */
public class SearchResultAdapter extends BaseAdapter {


    List<SearchResult> results;
    Activity context;
    String query;
    Pattern pattern;

    FurqanDao dao;

    public SearchResultAdapter(Activity context, List<SearchResult> results, String query, FurqanDao dao) {
        this.context = context;
        this.results = results;
        this.query = query;
        this.dao = dao;
        pattern = Pattern.compile("(?i)" + query);
    }

    @Override
    public int getCount() {
        return results.size();
    }

    @Override
    public SearchResult getItem(int i) {
        return results.get(i);
    }

    @Override
    public long getItemId(int i) {
        return i;
    }

    @Override
    public View getView(int i, View convertView, ViewGroup viewGroup) {
        View rowView = convertView;
        if (rowView == null) {
            LayoutInflater inflater = context.getLayoutInflater();
            rowView = inflater.inflate(R.layout.search_row, null);
            ViewHolder viewHolder = new ViewHolder(rowView);
            rowView.setTag(viewHolder);
        }

        SearchResult item = getItem(i);

        ViewHolder holder = (ViewHolder) rowView.getTag();
        holder.surahTitle.setText(dao.getAllSurah().get(item.getSurahNo()).getName() + " " + String.valueOf(item.getNumber()) + " - " + item.getTranslationName());
        String text = item.getText();
        Matcher matcher = pattern.matcher(text);
        List<String> replacement = new ArrayList();

        // Check all occurance
        while (matcher.find()) {
            String match = text.substring(matcher.start(), matcher.end());
            replacement.add(match);
        }

        for (String replace : replacement) {
            text = text.replace(replace, "<b>" + replace + "</b>");
        }

        holder.verseTitle.setText(Html.fromHtml(text));
        return rowView;
    }

    static class ViewHolder {
        @InjectView(R.id.surahTextView)
        public TextView surahTitle;
        @InjectView(R.id.verseTextView)
        public TextView verseTitle;

        public ViewHolder(View view) {
            ButterKnife.inject(this, view);
        }
    }
}

package com.pratamalabs.furqan;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.Filter;
import android.widget.Filterable;
import android.widget.TextView;

import com.pratamalabs.furqan.models.Note;
import com.pratamalabs.furqan.repository.FurqanDao;

import org.apache.commons.lang3.StringUtils;

import java.util.ArrayList;
import java.util.List;

import butterknife.ButterKnife;
import butterknife.InjectView;

/**
 * Created by KatulSomin on 11/08/2014.
 */
public class NoteListAdapter extends BaseAdapter implements Filterable {

    public final List<Note> originalNotes;
    public final Context context;
    public final FurqanDao dao;
    public List<Note> notes;

    public NoteListAdapter(Context context, List<Note> notes, FurqanDao dao) {
        this.originalNotes = notes;
        this.notes = notes;
        this.context = context;
        this.dao = dao;
    }

    @Override
    public int getCount() {
        return notes.size();
    }

    @Override
    public Note getItem(int position) {
        return notes.get(position);
    }

    @Override
    public long getItemId(int position) {
        return 0;
    }

    @Override
    public Filter getFilter() {
        return new Filter() {
            @Override
            protected FilterResults performFiltering(CharSequence constraint) {
                List<Note> filtered;
                if (StringUtils.isBlank(constraint)) {
                    filtered = originalNotes;
                } else {
                    filtered = new ArrayList<Note>();
                    for (Note note : originalNotes) {
                        if (note.isFound(constraint)) {
                            filtered.add(note);
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

                notes = (List<Note>) results.values;
                notifyDataSetChanged();
            }
        };
    }

    @Override
    public View getView(int i, View convertView, ViewGroup viewGroup) {
        View rowView = convertView;
        if (rowView == null) {
            LayoutInflater inflater = (LayoutInflater) context
                    .getSystemService(Context.LAYOUT_INFLATER_SERVICE);
            rowView = inflater.inflate(R.layout.note_history_row, null);
            ViewHolder viewHolder = new ViewHolder(rowView);
            rowView.setTag(viewHolder);
        }

        Note item = getItem(i);

        ViewHolder holder = (ViewHolder) rowView.getTag();
        holder.surahTitle.setText(item.getSurahName() + " " + String.valueOf(item.getNumber()));
        holder.verseTitle.setText(item.getText());
        return rowView;
    }

    static class ViewHolder {
        @InjectView(R.id.surahTextView)
        public TextView surahTitle;
        @InjectView(R.id.noteTextView)
        public TextView verseTitle;

        public ViewHolder(View view) {
            ButterKnife.inject(this, view);
        }
    }
}

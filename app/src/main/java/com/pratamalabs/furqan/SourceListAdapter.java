package com.pratamalabs.furqan;

import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.text.Html;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.ImageView;
import android.widget.TextView;

import com.pratamalabs.furqan.events.SourceDownloadEvent;
import com.pratamalabs.furqan.events.SourcesUpdatedEvent;
import com.pratamalabs.furqan.models.Source;
import com.pratamalabs.furqan.repository.FurqanDao;
import com.pratamalabs.furqan.services.EventBus;
import com.pratamalabs.furqan.views.dragsortlistview.DragSortListView;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import butterknife.ButterKnife;
import butterknife.InjectView;

/**
 * Created by pratamalabs on 31/7/13.
 */
public class SourceListAdapter extends BaseAdapter implements DragSortListView.DropListener {

    public static final String NOTDOWNLOADED = "notdownloaded";
    Context context;
    List<Source> originalSources;
    List<Source> downloadedSources;
    List<Source> availableSources;
    FurqanDao dao;
    FurqanSettings settings;
    EventBus bus;
    private int mDivPos;

    public SourceListAdapter(final Context context, final List<Source> objects, FurqanDao dao, EventBus bus, FurqanSettings settings) {
        this.context = context;
        this.bus = bus;
        this.dao = dao;
        this.settings = settings;
        this.originalSources = objects;
        setupSourceSection(objects);
    }

    private void updateSorting() {
        boolean changed = false;
        for (int i = 0; i < downloadedSources.size(); i++) {
            Source source = downloadedSources.get(i);
            if (source.getOrder() != i) {
                source.setOrder(i);
                dao.setSourceOrder(source.id, i);
                changed = true;
            }
        }
        if (changed) {
            settings.refreshTranslations();
        }
    }

    private void setupSourceSection(List<Source> objects) {
        downloadedSources = new ArrayList<>();
        availableSources = new ArrayList<>();
        for (Source source : objects) {
            if (source.getStatus().equals(NOTDOWNLOADED)) {
                availableSources.add(source);
            } else {
                downloadedSources.add(source);
            }
        }
        Collections.sort(downloadedSources);
        updateSorting();
        mDivPos = downloadedSources.size() + 1;
    }

    @Override
    public int getCount() {
        return downloadedSources.size() + availableSources.size() + 2;
    }

    @Override
    public long getItemId(int i) {
        return 0;
    }

    @Override
    public Object getItem(int i) {
        if (i == 0) {
            return "Downloaded";
        }
        if (i == mDivPos) {
            return "Available";
        }
        if (i < mDivPos) {
            return downloadedSources.get(i - 1);
        }
        if (i > mDivPos) {
            return availableSources.get(i - (1 + mDivPos));
        }
        return null;
    }

    @Override
    public boolean isEnabled(int position) {
        return position != mDivPos && position != 0;
    }

    public int getDivPosition() {
        return mDivPos;
    }

    @Override
    public int getViewTypeCount() {
        return 2;
    }


    @Override
    public int getItemViewType(int position) {
        if (position == 0 || position == mDivPos) {
            return 0;
        } else {
            return 1;
        }
    }

    @Override
    public boolean areAllItemsEnabled() {
        return false;
    }

    @Override
    public void drop(int from, int to) {
        if (from != to) {
            //adjust for header
            from -= 1;
            to -= 1;

            Source item = downloadedSources.get(from);

            downloadedSources.remove(item);
            downloadedSources.add(to, item);

            updateSorting();
            notifyDataSetChanged();
            //list.moveCheckState(from, to);

        }

    }

//    @Override
//    public Filter getFilter() {
//
//        return new Filter() {
//            @Override
//            protected FilterResults performFiltering(CharSequence constraint) {
//                List<Source> filtered;
//                if (StringUtils.isBlank(constraint)) {
//                    filtered = originalSources;
//                } else {
//                    filtered = new ArrayList<Source>();
//                    for (Source source : originalSources) {
//                        if (source.isFound(constraint)) {
//                            filtered.add(source);
//                        }
//                    }
//                }
//                FilterResults results = new FilterResults();
//                results.values = filtered;
//                results.count = filtered.size();
//                return results;
//            }
//
//            @Override
//            protected void publishResults(CharSequence constraint, FilterResults results) {
//                setupSourceSection((List<Source>) results.values);
//                notifyDataSetChanged();
//            }
//        };
//    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {

        Object item = getItem(position);
        if (getItemViewType(position) == 0) {
            LayoutInflater inflater = (LayoutInflater) context
                    .getSystemService(Context.LAYOUT_INFLATER_SERVICE);
            View sectionView = inflater.inflate(R.layout.sources_section_div, null);
            TextView sectionTitle = (TextView) sectionView.findViewById(R.id.sectionTextView);
            sectionTitle.setText((String) item);
            return sectionView;
        } else {
            View rowView = convertView;
            if (rowView == null) {
                LayoutInflater inflater = (LayoutInflater) context
                        .getSystemService(Context.LAYOUT_INFLATER_SERVICE);
                rowView = inflater.inflate(R.layout.source_row, null);
                final ViewHolder viewHolder = new ViewHolder(rowView);

                viewHolder.statusToggle.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
                    @Override
                    public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                        if (!viewHolder.source.getStatus().equals(NOTDOWNLOADED)) {
                            viewHolder.source.setStatus(isChecked ? "enabled" : "disabled");
                            dao.updateSourceStatus(viewHolder.source.id,
                                    viewHolder.source.getStatus());
                        }
                    }
                });

                viewHolder.deleteButton.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(final View view) {
                        new AlertDialog.Builder(context)
                                .setTitle("Confirm Delete")
                                .setMessage("Delete the source data? you can always redownload it later.")
                                .setNegativeButton("Cancel", null)
                                .setPositiveButton("Yes", new DialogInterface.OnClickListener() {
                                    @Override
                                    public void onClick(DialogInterface dialogInterface, int i) {
                                        if (!viewHolder.source.getStatus().equals(NOTDOWNLOADED)) {
                                            dao.deleteSourcesData(viewHolder.source.id);
                                            bus.post(new SourcesUpdatedEvent());
                                        } else {
                                            bus.post(new SourceDownloadEvent(viewHolder.source));
                                        }
                                    }
                                })
                                .show();
                    }
                });

                rowView.setTag(viewHolder);
            }
            ViewHolder holder = (ViewHolder) rowView.getTag();
            Source source = (Source) item;
            holder.source = source;
            holder.langLabel.setText(source.language);
            holder.sourceLabel.setText(Html.fromHtml(source.name));

            if (!source.getStatus().equals(NOTDOWNLOADED)) {
                holder.statusToggle.setVisibility(View.VISIBLE);
                holder.deleteButton.setImageResource(R.drawable.content_discard);
                holder.deleteButton.setClickable(true);
                holder.drag_holder.setVisibility(View.VISIBLE);
                holder.statusToggle.setChecked(source.status.equals("enabled"));
            } else {
                holder.statusToggle.setVisibility(View.GONE);
                holder.deleteButton.setImageResource(R.drawable.ic_action_download);
                holder.deleteButton.setClickable(false);
                holder.drag_holder.setVisibility(View.GONE);
            }
            return rowView;
        }
    }

    static class ViewHolder {
        @InjectView(R.id.languageLabel)
        public TextView langLabel;

        @InjectView(R.id.sourceLabel)
        public TextView sourceLabel;

        @InjectView(R.id.toggleButton)
        public CheckBox statusToggle;

        @InjectView(R.id.imageButton)
        public ImageView deleteButton;

        @InjectView(R.id.drag_handle)
        public View drag_holder;
        public Source source;

        public ViewHolder(View view) {
            ButterKnife.inject(this, view);
        }
    }
}

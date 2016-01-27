package com.pratamalabs.furqan;

import android.app.Activity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageButton;
import android.widget.TextView;

import com.pratamalabs.furqan.events.RecitationDeleteCache;
import com.pratamalabs.furqan.events.RecitationShouldStartDownloading;
import com.pratamalabs.furqan.models.Recitation;
import com.pratamalabs.furqan.services.EventBus;
import com.pratamalabs.furqan.services.VerseRecitationService;

import java.util.List;

import butterknife.ButterKnife;
import butterknife.InjectView;

/**
 * Created by andikapratama on 05/09/15.
 */
public class RecitationListAdapter extends BaseAdapter {

    public List<Recitation> recitations;

    FurqanSettings settings;
    Activity context;
    EventBus eventBus;
    VerseRecitationService reciteService;

    public RecitationListAdapter(Activity context, EventBus eventBus, FurqanSettings settings, VerseRecitationService recitationService) {
        this.settings = settings;
        this.context = context;
        this.eventBus = eventBus;
        this.reciteService = recitationService;
        recitations = settings.getRecitations();
    }

    @Override
    public int getCount() {
        return recitations.size();
    }

    @Override
    public Recitation getItem(int i) {
        return recitations.get(i);
    }

    @Override
    public long getItemId(int i) {
        return getItem(i).hashCode();
    }

    @Override
    public View getView(int i, View view, ViewGroup viewGroup) {
        LayoutInflater inflater = context.getLayoutInflater();
        View rowView = view;
        if (rowView == null) {
            rowView = inflater.inflate(R.layout.recitation_item, null);
            rowView.setTag(new ViewHolder(rowView));
        }
        ViewHolder viewHolder = (ViewHolder) rowView.getTag();

        final Recitation recitation = recitations.get(i);


        if (recitation.isComplete()) {
            viewHolder.recitationStatus.setText("100% Downloaded, offline ready");
        } else if (recitation.downloaded) {

            viewHolder.recitationStatus.setText(
                    String.format("%d%% Downloaded", ((int) (recitation.downloadedPercentage() * 100)))
            );
        } else {
            viewHolder.recitationStatus.setText("Not Downloaded, online only");
        }

        viewHolder.downloadPauseButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                eventBus.post(new RecitationShouldStartDownloading(recitation));
            }
        });

        viewHolder.deleteButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                eventBus.post(new RecitationDeleteCache(recitation));
            }
        });

        viewHolder.recitationName.setText(recitation.getTitle());


        return rowView;
    }

    public static class ViewHolder {

        @InjectView(R.id.recitation_name)
        public TextView recitationName;

        @InjectView(R.id.recitation_status)
        public TextView recitationStatus;

        @InjectView(R.id.delete_button)
        public ImageButton deleteButton;

        @InjectView(R.id.download_pause_button)
        public ImageButton downloadPauseButton;


        public ViewHolder(View view) {
            ButterKnife.inject(this, view);
        }

    }
}

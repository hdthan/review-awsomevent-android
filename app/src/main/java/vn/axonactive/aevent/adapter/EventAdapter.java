package vn.axonactive.aevent.adapter;

import android.content.Context;
import android.content.Intent;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.TextView;

import com.squareup.picasso.Callback;
import com.squareup.picasso.Picasso;

import java.text.SimpleDateFormat;
import java.util.List;

import vn.axonactive.aevent.BuildConfig;
import vn.axonactive.aevent.R;
import vn.axonactive.aevent.activity.EventDetailActivity;
import vn.axonactive.aevent.api.RestfulAPI;
import vn.axonactive.aevent.model.Event;
import vn.axonactive.aevent.util.DataStorage;

/**
 * Created by dangducnam on 1/10/17.
 */

public class EventAdapter extends RecyclerView.Adapter {

    private final int VIEW_ITEM = 1;
    private final int VIEW_PROGRESS_BAR = 0;

    private Context context;

    private List<Event> mEvents;

    public EventAdapter(List<Event> mEvents) {
        this.mEvents = mEvents;

    }

    @Override
    public int getItemViewType(int position) {

        return mEvents.get(position) != null ? VIEW_ITEM : VIEW_PROGRESS_BAR;

    }

    @Override
    public RecyclerView.ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {

        RecyclerView.ViewHolder viewHolder = null;

        context = parent.getContext();

        if (viewType == VIEW_ITEM) {
            View inflatedView = LayoutInflater.from(context)
                    .inflate(R.layout.event_row, parent, false);
            viewHolder = new EventViewHolder(context, inflatedView);
        } else if (viewType == VIEW_PROGRESS_BAR) {
            View inflatedView = LayoutInflater.from(context)
                    .inflate(R.layout.progressbar_item, parent, false);
            viewHolder = new ProgressBarViewHolder(inflatedView);
        }


        return viewHolder;
    }

    @Override
    public void onBindViewHolder(RecyclerView.ViewHolder holder, int position) {

        if (holder instanceof EventViewHolder) {
            Event event = mEvents.get(position);
            ((EventViewHolder) holder).bindEvent(event);
        } else {
            ((ProgressBarViewHolder) holder).setLoading(true);
        }


    }

    public void showLoading() {
        mEvents.add(null);

        notifyItemInserted(mEvents.size() - 1);


    }

    public void hideLoading() {
        if (mEvents.size() > 1) {
            mEvents.remove(mEvents.size() - 1);
            notifyItemRemoved(mEvents.size());
        }
    }

    @Override
    public int getItemCount() {

        if (mEvents == null) {
            return 0;
        }

        return mEvents.size();
    }

    private static class ProgressBarViewHolder extends RecyclerView.ViewHolder {

        ProgressBar mProgressBar;

        ProgressBarViewHolder(View itemView) {
            super(itemView);

            mProgressBar = (ProgressBar) itemView.findViewById(R.id.progressBar);
            mProgressBar.setIndeterminate(true);

        }

        void setLoading(boolean loading) {
            mProgressBar.setIndeterminate(loading);
        }

    }

    private class EventViewHolder extends RecyclerView.ViewHolder {

        private Context context;

        private ImageView mImage;
        private TextView mDate;
        private TextView mTitle;
        private TextView mLocation;
        private ProgressBar mProgressBar;

        private Event mEvent;

        EventViewHolder(Context context, View v) {
            super(v);

            this.context = context;

            mImage = (ImageView) v.findViewById(R.id.image);
            mDate = (TextView) v.findViewById(R.id.date);
            mTitle = (TextView) v.findViewById(R.id.title);
            mLocation = (TextView) v.findViewById(R.id.location);
            mProgressBar = (ProgressBar) v.findViewById(R.id.progressBar);

            v.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    Context context = itemView.getContext();
                    Intent showEventDetailIntent = new Intent(context, EventDetailActivity.class);
                    DataStorage.id = mEvent.getId();
                    context.startActivity(showEventDetailIntent);
                }
            });
        }

        void bindEvent(Event event) {

            mEvent = event;

            String urlImage = BuildConfig.API_URL + mEvent.getImageCover() + "/cover";

            Picasso.with(context)
                    .load(urlImage)
                    .placeholder(R.drawable.image_cover_default)
                    .into(mImage, new Callback() {
                        @Override
                        public void onSuccess() {
                            mProgressBar.setVisibility(View.GONE);
                        }

                        @Override
                        public void onError() {
                            mProgressBar.setVisibility(View.GONE);
                        }
                    });

            SimpleDateFormat format = new SimpleDateFormat(context.getString(R.string.date_time_format));

            mDate.setText(format.format(mEvent.getStartDate()));
            mTitle.setText(mEvent.getTitle());
            mLocation.setText(mEvent.getLocation());
        }
    }
}



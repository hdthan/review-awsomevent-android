package vn.axonactive.aevent.adapter;

import android.app.Dialog;
import android.content.Context;
import android.content.Intent;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AlertDialog;
import android.support.v7.widget.CardView;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.GridView;
import android.widget.ImageButton;
import android.widget.TextView;

import com.github.vipulasri.timelineview.TimelineView;

import java.util.List;

import vn.axonactive.aevent.R;
import vn.axonactive.aevent.activity.SpeakerDetailActivity;
import vn.axonactive.aevent.model.OrderStatus;
import vn.axonactive.aevent.model.TimeLineModel;
import vn.axonactive.aevent.model.TopicSpeaker;
import vn.axonactive.aevent.util.DateTimeUtil;

/**
 * Created by ltphuc on 3/10/2017.
 */

public class TimeLineAdapter extends RecyclerView.Adapter<TimeLineAdapter.TimeLineViewHolder> {

    private Context mContext;

    private List<TimeLineModel> mTimeLines;

    public TimeLineAdapter(List<TimeLineModel> mTimeLines) {
        this.mTimeLines = mTimeLines;
    }

    @Override
    public TimeLineViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {

        mContext = parent.getContext();

        View inflatedView = LayoutInflater.from(mContext)
                .inflate(R.layout.item_timeline, parent, false);

        return new TimeLineViewHolder(inflatedView, viewType);
    }

    @Override
    public int getItemViewType(int position) {
        return TimelineView.getTimeLineViewType(position, getItemCount());
    }


    @Override
    public void onBindViewHolder(TimeLineViewHolder holder, int position) {

        holder.bindTimeLine(mTimeLines.get(position));

    }

    @Override
    public int getItemCount() {
        return mTimeLines.size();
    }

    class TimeLineViewHolder extends RecyclerView.ViewHolder {

        private TextView mTvTitle;
        private TextView mTvDescription;
        private TextView mTvStartTime;
        private TextView mTvDate;
        private TimelineView mTimelineView;
        private CardView mCardView;

        TimeLineViewHolder(View view, int viewType) {
            super(view);

            mTvTitle = (TextView) view.findViewById(R.id.text_title);
            mTvDescription = (TextView) view.findViewById(R.id.text_des);
            mTvStartTime = (TextView) view.findViewById(R.id.text_start_time);
            mTvDate = (TextView) view.findViewById(R.id.text_date);
            mCardView = (CardView) view.findViewById(R.id.card_view);

            mTimelineView = (TimelineView) view.findViewById(R.id.time_marker);
            mTimelineView.initLine(viewType);
        }

        void bindTimeLine(final TimeLineModel timeLine) {

            if (timeLine.getStatus() == OrderStatus.INACTIVE) {
                mTimelineView.setMarker(ContextCompat.getDrawable(mContext, R.drawable.ic_marker));
            } else if (timeLine.getStatus() == OrderStatus.ACTIVE) {
                mTimelineView.setMarker(ContextCompat.getDrawable(mContext, R.drawable.ic_marker_active));
            } else {
                mTimelineView.setMarker(ContextCompat.getDrawable(mContext, R.drawable.ic_marker_inactive));
            }

            mTvTitle.setText(timeLine.getTitle());

            if (timeLine.isInvisibleDate()) {
                mTvDate.setVisibility(View.GONE);
            } else {
                mTvDate.setText(DateTimeUtil.parseDateTime(timeLine.getStartTime(), "EEE, MMM dd"));
                mTvDate.setVisibility(View.VISIBLE);
            }

            if (timeLine.isInvisibleTime()) {
                mTvStartTime.setVisibility(View.GONE);
            } else {
                mTvStartTime.setText(DateTimeUtil.parseDateTime(timeLine.getStartTime(), "h:mm a"));
                mTvStartTime.setVisibility(View.VISIBLE);
            }

            mTvDescription.setText(DateTimeUtil.getLengthTime(timeLine.getStartTime(), timeLine.getEndTime()) + ", " + timeLine.getLocation());

            mCardView.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {

                    String description = timeLine.getDescription();

                    if (description == null || "".equals(description.trim())) {
                        return;
                    }

                    final AlertDialog.Builder builder = new AlertDialog.Builder(mContext);

                    LayoutInflater inflater = (LayoutInflater) mContext.getSystemService(Context.LAYOUT_INFLATER_SERVICE);

                    View view = inflater.inflate(R.layout.topic_detail_dialog, null);
                    builder.setView(view);

                    final Dialog dialog = builder.create();

                    TextView tvTitle = (TextView) view.findViewById(R.id.title);
                    ImageButton btnCancel = (ImageButton) view.findViewById(R.id.btn_cancel);
                    TextView tvDes = (TextView) view.findViewById(R.id.txt_des);

                    tvTitle.setText(timeLine.getTitle());

                    final List<TopicSpeaker> speakers = timeLine.getTopicSpeakers();

                    GridView gridView = (GridView) view.findViewById(R.id.grid_view);

                    gridView.setAdapter(new TopicSpeakerAdapter(mContext, speakers));

                    gridView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
                        @Override
                        public void onItemClick(AdapterView<?> adapterView, View view, int i, long l) {

                            Intent intent = new Intent(mContext, SpeakerDetailActivity.class);
                            intent.putExtra("speaker", speakers.get(i).getSpeaker());

                            mContext.startActivity(intent);

                        }
                    });


                    tvDes.setText(description);

                    btnCancel.setOnClickListener(new View.OnClickListener() {
                        @Override
                        public void onClick(View v) {
                            dialog.dismiss();
                        }
                    });

                    dialog.show();
                }
            });

        }

    }
}

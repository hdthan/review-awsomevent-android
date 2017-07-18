package vn.axonactive.aevent_organizer.adapter;

import android.app.Dialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.SharedPreferences;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AlertDialog;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import com.github.vipulasri.timelineview.TimelineView;

import java.util.Date;
import java.util.List;

import es.dmoral.toasty.Toasty;
import okhttp3.MediaType;
import okhttp3.RequestBody;
import okhttp3.ResponseBody;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;
import retrofit2.Retrofit;
import retrofit2.converter.scalars.ScalarsConverterFactory;
import vn.axonactive.aevent_organizer.R;
import vn.axonactive.aevent_organizer.api.EnrollmentEndpointInterface;
import vn.axonactive.aevent_organizer.api.RestfulAPI;
import vn.axonactive.aevent_organizer.model.Notification;
import vn.axonactive.aevent_organizer.model.OrderStatus;
import vn.axonactive.aevent_organizer.model.TimeLineModel;
import vn.axonactive.aevent_organizer.util.DataStorage;
import vn.axonactive.aevent_organizer.util.DateTimeUtil;
import vn.axonactive.aevent_organizer.util.GlobalBus;

/**
 * Created by ltphuc on 3/10/2017.
 */

public class TimeLineAdapter extends RecyclerView.Adapter<TimeLineAdapter.TimeLineViewHolder> {

    private Context mContext;

    private List<TimeLineModel> mTimeLines;

    public TimeLineAdapter(List<TimeLineModel> timeLines) {
        this.mTimeLines = timeLines;
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

    class TimeLineViewHolder extends RecyclerView.ViewHolder implements View.OnClickListener {

        private TextView mTvTitle;
        private TextView mTvDescription;
        private TextView mTvStartTime;
        private TextView mTvDate;
        private TimelineView mTimelineView;
        private Button mBtnCancel;
        private Button mBtnUpcoming;
        private ProgressBar mProgressBar;

        private TimeLineModel mTimeLine;

        TimeLineViewHolder(View view, int viewType) {
            super(view);

            mTvTitle = (TextView) view.findViewById(R.id.text_title);
            mTvDescription = (TextView) view.findViewById(R.id.text_des);
            mTvStartTime = (TextView) view.findViewById(R.id.text_start_time);
            mTvDate = (TextView) view.findViewById(R.id.text_date);

            mBtnUpcoming = (Button) view.findViewById(R.id.btn_upcoming);
            mBtnCancel = (Button) view.findViewById(R.id.btn_cancel);

            mProgressBar = (ProgressBar) view.findViewById(R.id.progressBar);

            mTimelineView = (TimelineView) view.findViewById(R.id.time_marker);
            mTimelineView.initLine(viewType);
        }

        void bindTimeLine(TimeLineModel timeLine) {

            mTimeLine = timeLine;

            if (mTimeLine.getStatus() != OrderStatus.COMPLETED) {
                mBtnUpcoming.setOnClickListener(this);
                mBtnCancel.setOnClickListener(this);
            }

            if (timeLine.getStatus() == OrderStatus.INACTIVE) {
                mTimelineView.setMarker(ContextCompat.getDrawable(mContext, R.drawable.ic_marker));
            } else if (timeLine.getStatus() == OrderStatus.ACTIVE) {
                mTimelineView.setMarker(ContextCompat.getDrawable(mContext, R.drawable.ic_marker_active));
            } else {
                mTimelineView.setMarker(ContextCompat.getDrawable(mContext, R.drawable.ic_marker_inactive));
            }

            mTvTitle.setText(mTimeLine.getTitle());

            if (mTimeLine.isInvisibleDate()) {
                mTvDate.setVisibility(View.GONE);
            } else {
                mTvDate.setText(DateTimeUtil.parseDateTime(mTimeLine.getStartTime(), "EEE, MMM dd"));
                mTvDate.setVisibility(View.VISIBLE);
            }

            if (mTimeLine.isInvisibleTime()) {
                mTvStartTime.setVisibility(View.GONE);
            } else {
                mTvStartTime.setText(DateTimeUtil.parseDateTime(mTimeLine.getStartTime(), "h:mm a"));
                mTvStartTime.setVisibility(View.VISIBLE);
            }

            mTvDescription.setText(DateTimeUtil.getLengthTime(mTimeLine.getStartTime(), mTimeLine.getEndTime()) + ", " + mTimeLine.getLocation());
        }

        private void showConfirmDialog(final int actionId) {

            String message = mContext.getString(R.string.confirm_msg_broadcast_upcoming);

            if (actionId == R.id.btn_cancel) {
                message = mContext.getString(R.string.confirm_msg_broadcast_cancel);
            }

            AlertDialog.Builder builder = new AlertDialog.Builder(mContext);
            builder.setMessage(message)
                    .setPositiveButton("Yes", new DialogInterface.OnClickListener() {
                        public void onClick(DialogInterface dialog, int id) {

                            RestfulAPI restfulAPI = new RestfulAPI(ScalarsConverterFactory.create());

                            Retrofit retrofit = restfulAPI.getRestClient();

                            EnrollmentEndpointInterface apiService = retrofit.create(EnrollmentEndpointInterface.class);

                            SharedPreferences prefs = mContext.getSharedPreferences(DataStorage.APP_PREFS, Context.MODE_PRIVATE);
                            String token = prefs.getString(DataStorage.TOKEN, null);


                            if (token != null) {

                                mProgressBar.setVisibility(View.VISIBLE);

                                long type = 0;
                                if (actionId == R.id.btn_upcoming) {
                                    type = 1;
                                }

                                String body = "{\"topicId\": " + mTimeLine.getId() + ", \"type\": " + type + "}";

                                final RequestBody requestBody = RequestBody.create(MediaType.parse("application/json"), body);


                                Call<ResponseBody> call = apiService.sendReminder(token, requestBody);

                                call.enqueue(new Callback<ResponseBody>() {
                                    @Override
                                    public void onResponse(Call<ResponseBody> call, final Response<ResponseBody> response) {
                                        if (response != null) {

                                            int code = response.code();

                                            if (code == 200) {

                                                String title = actionId == R.id.btn_cancel ? "Canceled" : "Upcoming";

                                                GlobalBus.getBus().post(new Notification(0, title, mTimeLine.getTitle(), new Date()));
                                                Toasty.success(mContext, "Successfully", Toast.LENGTH_SHORT, true).show();
                                            } else {
                                                Toasty.error(mContext, mContext.getString(R.string.err_unknown), Toast.LENGTH_SHORT, true).show();
                                            }

                                            mProgressBar.setVisibility(View.GONE);
                                        }
                                    }

                                    @Override
                                    public void onFailure(Call<ResponseBody> call, Throwable t) {
                                        Toasty.info(mContext, mContext.getString(R.string.err_network_connection), Toast.LENGTH_SHORT, true).show();
                                        mProgressBar.setVisibility(View.GONE);
                                    }
                                });
                            }

                        }
                    })
                    .setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
                        public void onClick(DialogInterface dialog, int id) {
                        }
                    });
            Dialog dialog = builder.create();
            dialog.show();

        }

        @Override
        public void onClick(View v) {
            int id = v.getId();

            showConfirmDialog(id);
        }
    }

}

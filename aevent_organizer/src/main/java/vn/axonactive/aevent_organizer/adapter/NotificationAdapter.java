package vn.axonactive.aevent_organizer.adapter;

import android.content.Context;
import android.content.SharedPreferences;
import android.graphics.Color;
import android.os.Handler;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.TextView;

import java.net.HttpURLConnection;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import okhttp3.ResponseBody;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;
import retrofit2.Retrofit;
import vn.axonactive.aevent_organizer.R;
import vn.axonactive.aevent_organizer.api.NotificationEndpointInterface;
import vn.axonactive.aevent_organizer.api.RestfulAPI;
import vn.axonactive.aevent_organizer.model.Notification;
import vn.axonactive.aevent_organizer.util.DataStorage;
import vn.axonactive.aevent_organizer.util.DateTimeUtil;

/**
 * Created by ltphuc on 3/14/2017.
 */

public class NotificationAdapter extends RecyclerView.Adapter<NotificationAdapter.NotificationViewHolder> {

    private Context mContext;

    private List<Notification> mNotifications;
    private List<Notification> mPendingNotifications;

    private Handler handler = new Handler();
    private HashMap<Long, Runnable> pendingRunnable = new HashMap<>();

    private static final int PENDING_REMOVAL_TIMEOUT = 3000;

    public NotificationAdapter(List<Notification> notifications) {
        this.mNotifications = notifications;
        mPendingNotifications = new ArrayList<>();
    }

    @Override
    public NotificationViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {

        mContext = parent.getContext();

        View inflatedView = LayoutInflater.from(mContext)
                .inflate(R.layout.noti_row, parent, false);

        return new NotificationViewHolder(inflatedView, viewType);
    }

    @Override
    public void onBindViewHolder(NotificationViewHolder holder, int position) {
        holder.bindNotification(mNotifications.get(position));
    }

    @Override
    public int getItemCount() {
        return mNotifications != null ? mNotifications.size() : 0;
    }

    class NotificationViewHolder extends RecyclerView.ViewHolder {

        private Notification mNotification;

        private TextView mTvTitle;
        private TextView mTvContent;
        private TextView mTvSendTime;
        private Button mBtnUndo;
        private Button mBtnDel;
        private View mViewContent;

        public NotificationViewHolder(View view, int viewType) {
            super(view);

            mTvTitle = (TextView) view.findViewById(R.id.txt_title);
            mTvContent = (TextView) view.findViewById(R.id.txt_content);
            mTvSendTime = (TextView) view.findViewById(R.id.txt_send_time);
            mViewContent = view.findViewById(R.id.view_content);
            mBtnDel = (Button) view.findViewById(R.id.btn_del);
            mBtnUndo = (Button) view.findViewById(R.id.btn_undo);
        }

        void bindNotification(final Notification notification) {

            mNotification = notification;

            if (mPendingNotifications.contains(notification)) {
                mViewContent.setBackgroundColor(Color.parseColor("#E91E63"));
                mTvTitle.setVisibility(View.INVISIBLE);
                mTvContent.setVisibility(View.INVISIBLE);
                mTvSendTime.setVisibility(View.INVISIBLE);

                mBtnUndo.setVisibility(View.VISIBLE);
                mBtnDel.setVisibility(View.VISIBLE);

                mBtnUndo.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {

                        long id = notification.getId();

                        Runnable pendingRemovalRunnable = pendingRunnable.get(id);
                        pendingRunnable.remove(id);

                        if (pendingRemovalRunnable != null)
                            handler.removeCallbacks(pendingRemovalRunnable);
                        mPendingNotifications.remove(notification);
                        // this will rebind the row in "normal" state
                        notifyItemChanged(mNotifications.indexOf(notification));
                    }
                });

                mBtnDel.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {

                        long id = notification.getId();

                        Runnable pendingRemovalRunnable = pendingRunnable.get(id);
                        pendingRunnable.remove(id);

                        if (pendingRemovalRunnable != null)
                            handler.removeCallbacks(pendingRemovalRunnable);
                        mPendingNotifications.remove(notification);
                        // this will rebind the row in "normal" state
                        remove(mNotifications.indexOf(notification));
                    }
                });

            } else {
                mViewContent.setBackgroundColor(Color.parseColor("#FFFFFF"));
                mTvTitle.setVisibility(View.VISIBLE);
                mTvContent.setVisibility(View.VISIBLE);
                mTvSendTime.setVisibility(View.VISIBLE);
                mBtnUndo.setVisibility(View.GONE);
                mBtnDel.setVisibility(View.GONE);

                mTvTitle.setText("".equals(mNotification.getTitle())? "No title" : mNotification.getTitle());
                mTvContent.setText(mNotification.getContent());

                mTvSendTime.setText(DateTimeUtil.getLengthTime(notification.getSendDate()));
            }

        }

    }

    public void pendingRemoval(int position) {

        final Notification notification = mNotifications.get(position);

        if (!mPendingNotifications.contains(notification)) {
            mPendingNotifications.add(notification);

            notifyItemChanged(position);

            Runnable pendingRemovalRunnable = new Runnable() {
                @Override
                public void run() {
                    remove(mNotifications.indexOf(notification));
                }
            };

            handler.postDelayed(pendingRemovalRunnable, PENDING_REMOVAL_TIMEOUT);
            pendingRunnable.put(notification.getId(), pendingRemovalRunnable);
        }
    }

    public void remove(int position) {

        Notification notification = mNotifications.get(position);

        if (mPendingNotifications.contains(notification)) {
            mPendingNotifications.remove(notification);
        }
        if (mNotifications.contains(notification)) {
            mNotifications.remove(position);
            notifyItemRemoved(position);
        }

        //call api to delete
        deleteNotification(notification.getId());
    }

    public boolean isPendingRemoval(int position) {
        Notification notification = mNotifications.get(position);
        return mPendingNotifications.contains(notification);
    }

    private void deleteNotification(long id) {

        RestfulAPI restfulAPI = new RestfulAPI();

        Retrofit retrofit = restfulAPI.getRestClient();

        NotificationEndpointInterface apiService = retrofit.create(NotificationEndpointInterface.class);

        SharedPreferences prefs = mContext.getSharedPreferences(DataStorage.APP_PREFS, Context.MODE_PRIVATE);
        String token = prefs.getString(DataStorage.TOKEN, null);

        if (token != null) {
            Call<ResponseBody> call = apiService.deleteNotificationById(token, id);
            call.enqueue(new Callback<ResponseBody>() {
                @Override
                public void onResponse(Call<ResponseBody> call, final Response<ResponseBody> response) {
                    if (response != null) {
                        if (response.code() == HttpURLConnection.HTTP_NO_CONTENT) {
                        } else {
                        }
                    }
                }

                @Override
                public void onFailure(Call<ResponseBody> call, Throwable t) {
                }
            });
        }

    }

}

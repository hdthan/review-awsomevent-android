package vn.axonactive.aevent.adapter;

import android.content.Context;
import android.content.Intent;
import android.graphics.Color;
import android.os.Handler;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ProgressBar;
import android.widget.TextView;

import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;

import vn.axonactive.aevent.R;
import vn.axonactive.aevent.activity.EventDetailActivity;
import vn.axonactive.aevent.model.NotificationModel;
import vn.axonactive.aevent.service.MyFirebaseMessagingService;
import vn.axonactive.aevent.sqlite.NotificationDataSource;
import vn.axonactive.aevent.util.BadgeUtil;
import vn.axonactive.aevent.util.DataStorage;
import vn.axonactive.aevent.util.DateTimeUtil;

/**
 * Created by ltphuc on 3/7/2017.
 */

public class NotificationAdapter extends RecyclerView.Adapter<RecyclerView.ViewHolder> {

    private final int VIEW_ITEM = 1;
    private final int VIEW_PROGRESS_BAR = 0;

    private Context context;

    private List<NotificationModel> mNotifications;
    private List<NotificationModel> mPendingNotifications;

    private Handler handler = new Handler();
    private HashMap<Long, Runnable> pendingRunnable = new HashMap<>();


    private static final int PENDING_REMOVAL_TIMEOUT = 3000;

    public NotificationAdapter(List<NotificationModel> notifications) {
        this.mNotifications = notifications;
        this.mPendingNotifications = new ArrayList<>();
    }

    @Override
    public int getItemViewType(int position) {

        return mNotifications.get(position) != null ? VIEW_ITEM : VIEW_PROGRESS_BAR;

    }

    @Override
    public RecyclerView.ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {


        context = parent.getContext();

        RecyclerView.ViewHolder viewHolder = null;

        if (viewType == VIEW_ITEM) {
            View inflatedView = LayoutInflater.from(context)
                    .inflate(R.layout.noti_row, parent, false);
            viewHolder = new NotificationViewHolder(context, inflatedView);
        } else if (viewType == VIEW_PROGRESS_BAR) {
            View inflatedView = LayoutInflater.from(context)
                    .inflate(R.layout.progressbar_item, parent, false);
            viewHolder = new ProgressBarViewHolder(inflatedView);
        }


        return viewHolder;

    }

    @Override
    public void onBindViewHolder(RecyclerView.ViewHolder holder, int position) {

        if (holder instanceof NotificationViewHolder) {
            NotificationModel notification = mNotifications.get(position);
            ((NotificationViewHolder) holder).bindNotification(notification);
        } else {
            ((TicketAdapter.ProgressBarViewHolder) holder).setLoading(true);
        }

    }

    @Override
    public int getItemCount() {
        return mNotifications.size();
    }

    public void showLoading() {
        mNotifications.add(null);

        notifyItemInserted(mNotifications.size() - 1);


    }

    public void hideLoading() {
        if (mNotifications.size() > 1) {
            mNotifications.remove(mNotifications.size() - 1);
            notifyItemRemoved(mNotifications.size());
        }
    }

    private class NotificationViewHolder extends RecyclerView.ViewHolder {

        private Context context;
        private TextView mTxtTitle;
        private TextView mTxtContent;
        private TextView mTxtSubTitle;
        private TextView mTxtRevTime;
        private Button mBtnUndo;
        private Button mBtnDel;
        private View mViewContent;

        private NotificationModel mNotification;

        NotificationViewHolder(final Context context, View v) {

            super(v);

            this.context = context;

            mTxtTitle = (TextView) v.findViewById(R.id.txt_title);
            mTxtContent = (TextView) v.findViewById(R.id.txt_content);
            mTxtSubTitle = (TextView) v.findViewById(R.id.txt_sub_title);
            mTxtRevTime = (TextView) v.findViewById(R.id.txt_rev_time);
            mBtnDel = (Button) v.findViewById(R.id.btn_del);
            mBtnUndo = (Button) v.findViewById(R.id.btn_undo);
            mViewContent = v.findViewById(R.id.view_content);

            v.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {

                    DataStorage.id = mNotification.getEventId();

                    int topicId = mNotification.getTopicId();

                    NotificationDataSource dataSource = new NotificationDataSource(context);
                    dataSource.open();
                    dataSource.updateNotification(mNotification.getId(), 0);
                    dataSource.close();
                    MyFirebaseMessagingService.decrementNumberOfNotification();
                    BadgeUtil.setBadge(context, MyFirebaseMessagingService.getNumberOfNotification());

                    Intent intent = new Intent(context, EventDetailActivity.class);
                    intent.putExtra("topicId", topicId);
                    intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
                    context.startActivity(intent);
                }
            });

        }

        void bindNotification(final NotificationModel notification) {

            mNotification = notification;

            if (mPendingNotifications.contains(notification)) {
                mViewContent.setBackgroundColor(Color.parseColor("#E91E63"));
                mTxtTitle.setVisibility(View.INVISIBLE);
                if ("".equals(notification.getSubTitle())) {
                    mTxtSubTitle.setVisibility(View.GONE);
                } else {
                    mTxtSubTitle.setVisibility(View.INVISIBLE);
                }
                mTxtContent.setVisibility(View.INVISIBLE);
                mTxtRevTime.setVisibility(View.INVISIBLE);
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
                mTxtTitle.setVisibility(View.VISIBLE);
                mTxtSubTitle.setVisibility(View.VISIBLE);
                mTxtContent.setVisibility(View.VISIBLE);
                mTxtRevTime.setVisibility(View.VISIBLE);
                mBtnUndo.setVisibility(View.GONE);
                mBtnDel.setVisibility(View.GONE);

                // set data here
                mTxtTitle.setText(mNotification.getTitle());
                mTxtContent.setText(mNotification.getContent());

                if ("".equals(mNotification.getSubTitle())) {
                    mTxtSubTitle.setVisibility(View.GONE);
                } else {
                    mTxtSubTitle.setVisibility(View.VISIBLE);
                    mTxtSubTitle.setText(mNotification.getSubTitle());
                }

                mTxtRevTime.setText(DateTimeUtil.getLengthTime(new Date(mNotification.getRevTime())));

                if (mNotification.getUnread() == 1) {
                    mTxtTitle.setTextColor(Color.parseColor("#F57C00"));
                }
            }

        }
    }

    private class ProgressBarViewHolder extends RecyclerView.ViewHolder {

        ProgressBar mProgressBar;

        ProgressBarViewHolder(View itemView) {
            super(itemView);

            mProgressBar = (ProgressBar) itemView.findViewById(R.id.progressBar);
            mProgressBar.setIndeterminate(true);

        }

        public void setLoading(boolean loading) {
            mProgressBar.setIndeterminate(loading);
        }

    }


    public void pendingRemoval(int position) {

        final NotificationModel notification = mNotifications.get(position);

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

    private void remove(int position) {

        NotificationModel notification = mNotifications.get(position);

        if (mPendingNotifications.contains(notification)) {
            mPendingNotifications.remove(notification);
        }
        if (mNotifications.contains(notification)) {
            mNotifications.remove(position);
            notifyItemRemoved(position);
        }

        //delete in sqlite db
        NotificationDataSource dataSource = new NotificationDataSource(context);
        dataSource.open();
        dataSource.deleteNotification(notification.getId());
        dataSource.close();

    }

    public boolean isPendingRemoval(int position) {
        NotificationModel notification = mNotifications.get(position);
        return mPendingNotifications.contains(notification);
    }
}

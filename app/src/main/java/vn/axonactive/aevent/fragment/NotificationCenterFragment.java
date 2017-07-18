package vn.axonactive.aevent.fragment;

import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.PorterDuff;
import android.graphics.drawable.Drawable;
import android.graphics.drawable.GradientDrawable;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.content.ContextCompat;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.helper.ItemTouchHelper;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import java.util.ArrayList;
import java.util.List;

import es.dmoral.toasty.Toasty;
import vn.axonactive.aevent.R;
import vn.axonactive.aevent.activity.MainActivity;
import vn.axonactive.aevent.adapter.NotificationAdapter;
import vn.axonactive.aevent.model.NotificationModel;
import vn.axonactive.aevent.service.MyFirebaseMessagingService;
import vn.axonactive.aevent.sqlite.NotificationDataSource;
import vn.axonactive.aevent.util.BadgeUtil;

/**
 * Created by ltphuc on 3/7/2017.
 */

public class NotificationCenterFragment extends Fragment implements MainActivity.OnPostDataRealTimeListener {

    private RecyclerView mRecyclerView;
    private LinearLayoutManager mLinearLayoutManager;
    private ProgressBar mProgressBar;
    private TextView mTvStatus;

    private NotificationDataSource dataSource;

    private NotificationAdapter mAdapter;

    private List<NotificationModel> mNotifications;

    private MainActivity activity;

    public NotificationCenterFragment() {

    }

    public static NotificationCenterFragment newInstance() {
        NotificationCenterFragment fragment = new NotificationCenterFragment();
        Bundle args = new Bundle();
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
        super.onCreateOptionsMenu(menu, inflater);
        menu.clear();
        inflater.inflate(R.menu.menu_notification, menu);

        MenuItem miClear = menu.findItem(R.id.action_clear);
        if (miClear != null) {
            miClear.setOnMenuItemClickListener(new MenuItem.OnMenuItemClickListener() {
                @Override
                public boolean onMenuItemClick(MenuItem item) {

                    if (mNotifications.size() > 0) {

                        NotificationDataSource dataSource = new NotificationDataSource(getContext());
                        dataSource.open();

                        dataSource.clearNotification();
                        dataSource.close();

                        mNotifications.clear();
                        mAdapter.notifyDataSetChanged();
                    } else {
                        Toasty.normal(getContext(), "No item to delete", Toast.LENGTH_SHORT).show();
                    }
                    return true;
                }
            });
        }

    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        setHasOptionsMenu(true);

        activity = (MainActivity) getActivity();

        View view = inflater.inflate(R.layout.fragment_notification_center, container, false);

        mRecyclerView = (RecyclerView) view.findViewById(R.id.recyclerView);
        mLinearLayoutManager = new LinearLayoutManager(getContext());
        mRecyclerView.setLayoutManager(mLinearLayoutManager);

        mProgressBar = (ProgressBar) view.findViewById(R.id.progressBar);
        mProgressBar.setIndeterminate(true);
        mTvStatus = (TextView) view.findViewById(R.id.txt_status);

        mNotifications = new ArrayList<>();
        mAdapter = new NotificationAdapter(mNotifications);

        mRecyclerView.setAdapter(mAdapter);

        dataSource = new NotificationDataSource(getContext());

        activity.addOnPostDataRealTimeListener(this);

        setUpItemTouchHelper();

        return view;
    }

    @Override
    public void onDataChange(NotificationModel notification) {
        mNotifications.add(0, notification);
        mAdapter.notifyItemInserted(0);
        mLinearLayoutManager.scrollToPosition(0);
        cleanBadge();
        mTvStatus.setVisibility(View.GONE);
    }

    @Override
    public void onResume() {
        super.onResume();
        loadData();
        cleanBadge();
    }

    private void cleanBadge() {
        MyFirebaseMessagingService.clearNumberOfNotification();
        BadgeUtil.setBadge(activity, 0);
        activity.setBadgeNotification(0);
    }

    private void loadData() {

        mNotifications.clear();

        dataSource.open();

        mNotifications.addAll(dataSource.getAllNotifications());

        mAdapter.notifyDataSetChanged();

        dataSource.close();

        if (mNotifications.size() == 0) {
            mTvStatus.setText("No notification.");
            mTvStatus.setVisibility(View.VISIBLE);
        } else {
            mTvStatus.setVisibility(View.GONE);
        }

    }

    private void setUpItemTouchHelper() {

        ItemTouchHelper.SimpleCallback simpleItemTouchCallback = new ItemTouchHelper.SimpleCallback(0, ItemTouchHelper.LEFT) {

            // we want to cache these and not allocate anything repeatedly in the onChildDraw method
            GradientDrawable background;
            Drawable xMark;
            int xMarkMargin;
            int marginTop;
            int marginStart;
            int xMarkSize;
            float radius;
            boolean initiated;

            private void init() {
                background = new GradientDrawable();
                background.setColor(Color.parseColor("#E91E63"));
                radius = getContext().getResources().getDimension(R.dimen.radius);
                background.setCornerRadius(radius);
                xMark = ContextCompat.getDrawable(getContext(), R.drawable.ic_archive);
                xMark.setColorFilter(Color.WHITE, PorterDuff.Mode.SRC_ATOP);
                xMarkMargin = (int) getContext().getResources().getDimension(R.dimen.ic_clear_margin);
                xMarkSize = (int) getContext().getResources().getDimension(R.dimen.icon_size);
                marginTop = (int) getContext().getResources().getDimension(R.dimen.card_view_margin_top);
                marginStart = (int) getContext().getResources().getDimension(R.dimen.card_view_margin_start);
                initiated = true;
            }

            // not important, we don't want drag & drop
            @Override
            public boolean onMove(RecyclerView recyclerView, RecyclerView.ViewHolder viewHolder, RecyclerView.ViewHolder target) {
                return false;
            }

            @Override
            public int getSwipeDirs(RecyclerView recyclerView, RecyclerView.ViewHolder viewHolder) {
                int position = viewHolder.getAdapterPosition();
                if (mAdapter.isPendingRemoval(position)) {
                    return 0;
                }
                return super.getSwipeDirs(recyclerView, viewHolder);
            }

            @Override
            public void onSwiped(RecyclerView.ViewHolder viewHolder, int swipeDir) {
                int swipedPosition = viewHolder.getAdapterPosition();
                mAdapter.pendingRemoval(swipedPosition);

            }

            @Override
            public void onChildDraw(Canvas c, RecyclerView recyclerView, RecyclerView.ViewHolder viewHolder, float dX, float dY, int actionState, boolean isCurrentlyActive) {
                View itemView = viewHolder.itemView;

                // not sure why, but this method get's called for view holder that are already swiped away
                if (viewHolder.getAdapterPosition() == -1) {
                    // not interested in those
                    return;
                }

                if (!initiated) {
                    init();
                }

                // draw red background
                background.setBounds(itemView.getRight() + (int) dX + marginStart, itemView.getTop() + marginTop, itemView.getRight() - marginStart, itemView.getBottom() - marginTop);
                background.draw(c);

                // draw x mark
                int itemHeight = itemView.getBottom() - itemView.getTop();
                int intrinsicWidth = xMark.getIntrinsicWidth();
                int intrinsicHeight = xMark.getIntrinsicWidth();

                int xMarkLeft = itemView.getRight() - xMarkMargin - intrinsicWidth;
                int xMarkRight = itemView.getRight() - xMarkMargin;
                int xMarkTop = itemView.getTop() + (itemHeight - intrinsicHeight) / 2;
                int xMarkBottom = xMarkTop + intrinsicHeight;
                xMark.setBounds(xMarkLeft, xMarkTop, xMarkRight, xMarkBottom);

                if (dX < -(xMarkSize + xMarkMargin * 2)) {
                    xMark.draw(c);
                }

                super.onChildDraw(c, recyclerView, viewHolder, dX, dY, actionState, isCurrentlyActive);
            }

        };
        ItemTouchHelper mItemTouchHelper = new ItemTouchHelper(simpleItemTouchCallback);
        mItemTouchHelper.attachToRecyclerView(mRecyclerView);
    }
}

package vn.axonactive.aevent_organizer.fragment;

import android.app.Dialog;
import android.content.Context;
import android.content.SharedPreferences;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.PorterDuff;
import android.graphics.drawable.Drawable;
import android.graphics.drawable.GradientDrawable;
import android.os.Bundle;
import android.support.design.widget.FloatingActionButton;
import android.support.v4.app.Fragment;
import android.support.v4.content.ContextCompat;
import android.support.v4.widget.SwipeRefreshLayout;
import android.support.v7.app.AlertDialog;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.helper.ItemTouchHelper;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import com.squareup.otto.Subscribe;

import java.net.HttpURLConnection;
import java.util.ArrayList;
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
import vn.axonactive.aevent_organizer.adapter.NotificationAdapter;
import vn.axonactive.aevent_organizer.api.EnrollmentEndpointInterface;
import vn.axonactive.aevent_organizer.api.NotificationEndpointInterface;
import vn.axonactive.aevent_organizer.api.RestfulAPI;
import vn.axonactive.aevent_organizer.model.Notification;
import vn.axonactive.aevent_organizer.util.DataStorage;
import vn.axonactive.aevent_organizer.util.GlobalBus;

/**
 * Created by ltphuc on 3/9/2017.
 */

public class BroadcastFragment extends Fragment implements View.OnClickListener {

    private long eventId;

    private Dialog dialog;

    private RecyclerView mRecyclerView;
    private LinearLayoutManager mLinearLayoutManager;
    private NotificationAdapter mAdapter;
    private ProgressBar mProgressBar;
    private TextView mTvStatus;
    private SwipeRefreshLayout mSwipeRefreshLayout;

    private List<Notification> mNotifications;

    public BroadcastFragment() {

    }

    public static BroadcastFragment newInstance() {
        BroadcastFragment fragment = new BroadcastFragment();
        Bundle args = new Bundle();
        fragment.setArguments(args);
        return fragment;
    }


    void onItemsLoadComplete() {
        mSwipeRefreshLayout.setRefreshing(false);
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        eventId = getArguments().getLong("eventId");

        mNotifications = new ArrayList<>();

        setHasOptionsMenu(true);
        View view = inflater.inflate(R.layout.fragment_broadcast, container, false);

        FloatingActionButton mBtnSendMessage = (FloatingActionButton) view.findViewById(R.id.btn_send_message);
        mBtnSendMessage.setOnClickListener(this);

        mRecyclerView = (RecyclerView) view.findViewById(R.id.recyclerView);

        mProgressBar = (ProgressBar) view.findViewById(R.id.progressBar);
        mProgressBar.setIndeterminate(true);

        mTvStatus = (TextView) view.findViewById(R.id.txt_status);

        mLinearLayoutManager = new LinearLayoutManager(getContext());

        mRecyclerView.setLayoutManager(mLinearLayoutManager);

        mSwipeRefreshLayout = (SwipeRefreshLayout) view.findViewById(R.id.swipeRefreshLayout);

        mSwipeRefreshLayout.setOnRefreshListener(new SwipeRefreshLayout.OnRefreshListener() {
            @Override
            public void onRefresh() {
                refreshItems();
            }
        });

        mAdapter = new NotificationAdapter(mNotifications);
        mRecyclerView.setAdapter(mAdapter);

        setUpItemTouchHelper();

        return view;
    }

    @Override
    public void onResume() {
        super.onResume();
        getNotifications();
    }

    @Subscribe
    public void revNotificationDataFromTimeLine(Notification notification) {

        mNotifications.add(0, notification);
        mAdapter.notifyItemInserted(0);
        mLinearLayoutManager.scrollToPosition(0);

        mTvStatus.setVisibility(View.GONE);

    }

    private boolean updateNotifications() {

        boolean success = false;
        RestfulAPI restfulAPI = new RestfulAPI();

        Retrofit retrofit = restfulAPI.getRestClient();

        NotificationEndpointInterface apiService = retrofit.create(NotificationEndpointInterface.class);

        SharedPreferences prefs = getContext().getSharedPreferences(DataStorage.APP_PREFS, Context.MODE_PRIVATE);
        String token = prefs.getString(DataStorage.TOKEN, null);

        if (token != null) {
            Call<List<Notification>> call = apiService.getNotificationsByEventId(token, eventId);
            call.enqueue(new Callback<List<Notification>>() {
                @Override
                public void onResponse(Call<List<Notification>> call, final Response<List<Notification>> response) {
                    if (response != null) {
                        if (response.code() == HttpURLConnection.HTTP_OK) {

                            mNotifications.clear();

                            mNotifications.addAll(response.body());

                            if (mNotifications.size() == 0) {
                                String status = "No notifications";
                                mTvStatus.setText(status);
                                mTvStatus.setVisibility(View.VISIBLE);
                            } else {
                                mTvStatus.setVisibility(View.GONE);
                            }

                            mAdapter.notifyDataSetChanged();

                        } else {
                            Toasty.error(getContext(), getString(R.string.err_unknown), Toast.LENGTH_LONG, true).show();
                        }
                    }
                }

                @Override
                public void onFailure(Call<List<Notification>> call, Throwable t) {
                    Toasty.error(getContext(), getString(R.string.err_network_connection), Toast.LENGTH_LONG, true).show();
                }
            });

            success = true;
        }

        return success;
    }


    private void getNotifications() {

        mProgressBar.setVisibility(View.VISIBLE);
        if (updateNotifications()) {
            mProgressBar.setVisibility(View.GONE);
        }
    }

    private void refreshItems() {

        if (updateNotifications()) {
            onItemsLoadComplete();
        }
    }

    private void clearNotification() {

        mProgressBar.setVisibility(View.VISIBLE);

        RestfulAPI restfulAPI = new RestfulAPI();

        Retrofit retrofit = restfulAPI.getRestClient();

        NotificationEndpointInterface apiService = retrofit.create(NotificationEndpointInterface.class);

        SharedPreferences prefs = getContext().getSharedPreferences(DataStorage.APP_PREFS, Context.MODE_PRIVATE);
        String token = prefs.getString(DataStorage.TOKEN, null);

        if (token != null) {
            Call<ResponseBody> call = apiService.clearNotificationByEventId(token, eventId);
            call.enqueue(new Callback<ResponseBody>() {
                @Override
                public void onResponse(Call<ResponseBody> call, final Response<ResponseBody> response) {
                    if (response != null) {
                        if (response.code() == HttpURLConnection.HTTP_NO_CONTENT) {

                            mNotifications.clear();

                            if (mNotifications.size() == 0) {
                                mTvStatus.setText("No notification");
                                mTvStatus.setVisibility(View.VISIBLE);
                            } else {
                                mTvStatus.setVisibility(View.GONE);
                            }

                            mAdapter = new NotificationAdapter(mNotifications);
                            mRecyclerView.setAdapter(mAdapter);

                            Toasty.success(getContext(), "Successful").show();

                        } else {
                            Toasty.error(getContext(), getString(R.string.err_unknown), Toast.LENGTH_LONG, true).show();
                        }
                    }
                    mProgressBar.setVisibility(View.GONE);
                }

                @Override
                public void onFailure(Call<ResponseBody> call, Throwable t) {
                    mProgressBar.setVisibility(View.GONE);
                    Toasty.error(getContext(), getString(R.string.err_network_connection), Toast.LENGTH_LONG, true).show();
                }
            });
        }
    }

    @Override
    public void onStart() {
        super.onStart();
        GlobalBus.getBus().register(this);
    }

    @Override
    public void onStop() {
        super.onStop();
        GlobalBus.getBus().unregister(this);
    }


    @Override
    public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
        super.onCreateOptionsMenu(menu, inflater);
        menu.clear();
        inflater.inflate(R.menu.menu_clear_noti, menu);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {

        if (item.getItemId() == R.id.action_clear) {
            if (mNotifications.size() > 0) {
                clearNotification();
            } else {
                Toasty.error(getContext(), "No notification to delete", Toast.LENGTH_SHORT, false).show();
            }
        }

        return super.onOptionsItemSelected(item);
    }

    @Override
    public void onClick(View v) {
        if (v.getId() == R.id.btn_send_message) {

            AlertDialog.Builder builder = new AlertDialog.Builder(getContext());

            LayoutInflater inflater = getActivity().getLayoutInflater();

            View view = inflater.inflate(R.layout.dialog_broadcast, null);

            final Button btnSend = (Button) view.findViewById(R.id.btn_send_message);
            Button btnCancel = (Button) view.findViewById(R.id.btn_cancel);
            ImageButton btnClose = (ImageButton) view.findViewById(R.id.btn_close);
            final EditText editMessage = (EditText) view.findViewById(R.id.edt_message);
            final ProgressBar progressBar = (ProgressBar) view.findViewById(R.id.progressBar);
            final EditText edtTitle = (EditText) view.findViewById(R.id.edit_title);
            final TextView tvCountMessage = (TextView) view.findViewById(R.id.txt_count_message);
            final TextView tvCountTitle = (TextView) view.findViewById(R.id.txt_count_title);

            builder.setView(view);

            dialog = builder.create();

            dialog.show();

            edtTitle.addTextChangedListener(new TextWatcher() {
                @Override
                public void beforeTextChanged(CharSequence s, int start, int count, int after) {

                }

                @Override
                public void onTextChanged(CharSequence s, int start, int before, int count) {
                    tvCountTitle.setText(s.length() + "/100");
                }

                @Override
                public void afterTextChanged(Editable s) {

                }
            });

            editMessage.addTextChangedListener(new TextWatcher() {
                @Override
                public void beforeTextChanged(CharSequence s, int start, int count, int after) {

                }

                @Override
                public void onTextChanged(CharSequence s, int start, int before, int count) {
                    tvCountMessage.setText(s.length() + "/250");
                    if (s.length() > 0) {
                        btnSend.setBackgroundResource(R.drawable.button_border_color);
                        btnSend.setTextColor(ContextCompat.getColor(getContext(), R.color.colorPrimary));
                        btnSend.setEnabled(true);
                    } else {
                        btnSend.setBackgroundResource(R.drawable.button_border_transparent);
                        btnSend.setTextColor(Color.parseColor("#BDBDBD"));
                        btnSend.setEnabled(false);
                    }
                }

                @Override
                public void afterTextChanged(Editable s) {

                }
            });

            btnCancel.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    dialog.dismiss();
                }
            });

            btnClose.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    dialog.dismiss();
                }
            });


            btnSend.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {

                    final String message = editMessage.getText().toString();
                    final String title = edtTitle.getText().toString();

                    progressBar.setVisibility(View.VISIBLE);

                    RestfulAPI restfulAPI = new RestfulAPI(ScalarsConverterFactory.create());

                    Retrofit retrofit = restfulAPI.getRestClient();

                    EnrollmentEndpointInterface apiService = retrofit.create(EnrollmentEndpointInterface.class);

                    SharedPreferences prefs = getContext().getSharedPreferences(DataStorage.APP_PREFS, Context.MODE_PRIVATE);
                    String token = prefs.getString(DataStorage.TOKEN, null);

                    if (token != null) {

                        String body = "{\"eventId\": " + eventId + ", \"title\": " + "\"" + title + "\"" + ", \"message\": " + "\"" + message + "\"" + "}";

                        final RequestBody requestBody = RequestBody.create(MediaType.parse("application/json"), body);


                        Call<ResponseBody> call = apiService.sendMessage(token, requestBody);

                        call.enqueue(new Callback<ResponseBody>() {
                            @Override
                            public void onResponse(Call<ResponseBody> call, final Response<ResponseBody> response) {
                                if (response != null) {

                                    int code = response.code();

                                    if (code == 200) {
                                        mNotifications.add(0, new Notification(title, message, new Date()));
                                        mAdapter.notifyItemInserted(0);
                                        mLinearLayoutManager.scrollToPosition(0);
                                        Toasty.success(getContext(), "Send message successfully", Toast.LENGTH_SHORT, true).show();

                                        if (mNotifications.size() == 0) {
                                            mTvStatus.setText("No notification");
                                            mTvStatus.setVisibility(View.VISIBLE);
                                        } else {
                                            mTvStatus.setVisibility(View.GONE);
                                        }

                                    } else {
                                        Toasty.error(getContext(), "Send message fail", Toast.LENGTH_SHORT, true).show();
                                    }
                                    progressBar.setVisibility(View.GONE);
                                    dialog.dismiss();
                                }
                            }

                            @Override
                            public void onFailure(Call<ResponseBody> call, Throwable t) {
                                progressBar.setVisibility(View.GONE);
                                Toasty.error(getContext(), "Send message fail", Toast.LENGTH_SHORT, true).show();
                                dialog.dismiss();
                            }
                        });
                    }

                }
            });
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

                // not sure why, but this method gets called for view holder that are already swiped away
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

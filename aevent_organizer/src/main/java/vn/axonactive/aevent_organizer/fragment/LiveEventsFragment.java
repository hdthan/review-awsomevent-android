package vn.axonactive.aevent_organizer.fragment;

import android.content.SharedPreferences;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.widget.SwipeRefreshLayout;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import java.net.HttpURLConnection;
import java.util.ArrayList;
import java.util.List;

import es.dmoral.toasty.Toasty;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;
import retrofit2.Retrofit;
import vn.axonactive.aevent_organizer.R;
import vn.axonactive.aevent_organizer.adapter.EventAdapter;
import vn.axonactive.aevent_organizer.api.EventEndpointInterface;
import vn.axonactive.aevent_organizer.api.RestfulAPI;
import vn.axonactive.aevent_organizer.listener.OnReceivedDataListener;
import vn.axonactive.aevent_organizer.model.Event;
import vn.axonactive.aevent_organizer.model.EventExpand;
import vn.axonactive.aevent_organizer.util.DataStorage;

import static android.content.Context.MODE_PRIVATE;

/**
 * Created by dtnhat on 1/16/2017.
 */

public class LiveEventsFragment extends Fragment {

    private RecyclerView mRecyclerView;
    private LinearLayoutManager mLinearLayoutManager;
    private EventAdapter mAdapter;
    private SwipeRefreshLayout mSwipeRefreshLayout;
    private ProgressBar mProgressBar;
    private TextView mTvStatus;

    private List<EventExpand> mEvents;

    private boolean isLoading = true;
    int pastVisibleItems, visibleItemCount, totalItemCount;

    private static final int NUMBER_ITEM_FIRST_LOAD = 20;
    private static final int NUMBER_ITEM_LOAD_MORE = 5;

    public LiveEventsFragment() {
    }

    public static LiveEventsFragment newInstance() {
        LiveEventsFragment fragment = new LiveEventsFragment();
        Bundle args = new Bundle();
        fragment.setArguments(args);
        return fragment;
    }

    private void refreshItems() {

        this.getLiveEvents(NUMBER_ITEM_FIRST_LOAD, new OnReceivedDataListener() {
            @Override
            public void onSuccess(Object data) {
                mEvents = new ArrayList<>();
                mEvents.addAll((List<EventExpand>) data);

                if (mEvents.size() == 0) {
                    mTvStatus.setVisibility(View.VISIBLE);
                } else {
                    mTvStatus.setVisibility(View.GONE);
                }

                mAdapter = new EventAdapter(mEvents);

                mRecyclerView.setAdapter(mAdapter);

                onItemsLoadComplete();
            }

            @Override
            public void onFailure() {
                Toasty.info(getContext(), getString(R.string.msg_status_error), Toast.LENGTH_SHORT, true).show();
                onItemsLoadComplete();
            }

            @Override
            public void onStart() {
            }
        });
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

        View view = inflater.inflate(R.layout.fragment_live_events, container, false);

        mSwipeRefreshLayout = (SwipeRefreshLayout) view.findViewById(R.id.swipeRefreshLayout);

        mSwipeRefreshLayout.setOnRefreshListener(new SwipeRefreshLayout.OnRefreshListener() {
            @Override
            public void onRefresh() {
                refreshItems();
            }
        });

        mRecyclerView = (RecyclerView) view.findViewById(R.id.recyclerView);
        mProgressBar = (ProgressBar) view.findViewById(R.id.progressBar);
        mProgressBar.setIndeterminate(true);
        mProgressBar.setVisibility(View.VISIBLE);
        mTvStatus = (TextView) view.findViewById(R.id.txt_status);

        mLinearLayoutManager = new LinearLayoutManager(getContext());
        mRecyclerView.setLayoutManager(mLinearLayoutManager);

        this.getLiveEvents(NUMBER_ITEM_FIRST_LOAD, new OnReceivedDataListener() {
            @Override
            public void onSuccess(Object data) {
                mEvents = new ArrayList<>();
                mEvents.addAll((List<EventExpand>) data);

                if (mEvents.size() == 0) {
                    mTvStatus.setText(getString(R.string.msg_status_empty));
                    mTvStatus.setVisibility(View.VISIBLE);
                } else {
                    mTvStatus.setVisibility(View.GONE);
                }

                mAdapter = new EventAdapter(mEvents);

                mRecyclerView.setAdapter(mAdapter);
                mProgressBar.setVisibility(View.GONE);
            }

            @Override
            public void onFailure() {
                mTvStatus.setText(getString(R.string.msg_status_error));
                mTvStatus.setVisibility(View.VISIBLE);
                mProgressBar.setVisibility(View.GONE);
            }

            @Override
            public void onStart() {
                mProgressBar.setVisibility(View.VISIBLE);
            }
        });

        mRecyclerView.addOnScrollListener(new RecyclerView.OnScrollListener() {
            @Override
            public void onScrollStateChanged(RecyclerView recyclerView, int newState) {
                super.onScrollStateChanged(recyclerView, newState);
            }

            @Override
            public void onScrolled(RecyclerView recyclerView, int dx, int dy) {
                super.onScrolled(recyclerView, dx, dy);

                if (dy > 0) {
                    visibleItemCount = mLinearLayoutManager.getChildCount();
                    totalItemCount = mLinearLayoutManager.getItemCount();
                    pastVisibleItems = mLinearLayoutManager.findFirstVisibleItemPosition();

                    if (isLoading) {
                        if ((visibleItemCount + pastVisibleItems) >= totalItemCount) {
                            isLoading = false;
                            loadMoreData();
                        }
                    }
                }

            }
        });

        return view;
    }

    private void loadMoreData() {

        this.getLiveEvents(mEvents.size() + NUMBER_ITEM_LOAD_MORE, new OnReceivedDataListener() {
            @Override
            public void onSuccess(Object data) {

                mAdapter.hideLoading();

                List<EventExpand> events = (List<EventExpand>) data;

                if (events.size() > mEvents.size()) {
                    for (int i = mEvents.size(); i < events.size(); i++) {
                        mEvents.add(events.get(i));
                        mAdapter.notifyItemInserted(mEvents.size());
                    }
                }

                isLoading = true;

            }

            @Override
            public void onFailure() {
                Toasty.info(getContext(), getString(R.string.err_network_connection), Toast.LENGTH_SHORT, true).show();
                mAdapter.hideLoading();
                isLoading = true;
            }

            @Override
            public void onStart() {
                mAdapter.showLoading();
            }
        });

    }

    private void getLiveEvents(int num, final OnReceivedDataListener listener) {

        SharedPreferences prefs = getContext().getSharedPreferences(DataStorage.APP_PREFS, MODE_PRIVATE);
        String token = prefs.getString(DataStorage.TOKEN, null);

        if (token != null) {
            listener.onStart();

            RestfulAPI restfulAPI = new RestfulAPI();

            Retrofit retrofit = restfulAPI.getRestClient();

            EventEndpointInterface apiService =
                    retrofit.create(EventEndpointInterface.class);
            Call<List<EventExpand>> call = apiService.getMyLiveEvents(token, num);
            call.enqueue(new Callback<List<EventExpand>>() {
                @Override
                public void onResponse(Call<List<EventExpand>> call, Response<List<EventExpand>> response) {
                    if (response.body() != null) {

                        int statusCode = response.code();

                        if (statusCode == HttpURLConnection.HTTP_OK) {
                            listener.onSuccess(response.body());
                        } else {
                            listener.onFailure();
                        }
                    }

                }

                @Override
                public void onFailure(Call<List<EventExpand>> call, Throwable t) {
                    listener.onFailure();
                }
            });
        }

    }


}

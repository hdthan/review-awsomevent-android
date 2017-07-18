package vn.axonactive.aevent.fragment;

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

import java.util.ArrayList;
import java.util.List;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;
import retrofit2.Retrofit;
import vn.axonactive.aevent.R;
import vn.axonactive.aevent.adapter.EventAdapter;
import vn.axonactive.aevent.api.EventEndpointInterface;
import vn.axonactive.aevent.api.RestfulAPI;
import vn.axonactive.aevent.listener.OnReceivedDataListener;
import vn.axonactive.aevent.model.Event;
import vn.axonactive.aevent.util.DataStorage;

import static android.content.Context.MODE_PRIVATE;

public class UpComingEventFragment extends Fragment {

    private RecyclerView mRecyclerView;
    private LinearLayoutManager mLinearLayoutManager;
    private EventAdapter mAdapter;
    private SwipeRefreshLayout mSwipeRefreshLayout;

    private ProgressBar mProgressBar;
    private TextView mTvStatus;

    private List<Event> mEvents;


    private boolean isLoading = true;
    int pastVisibleItems, visibleItemCount, totalItemCount;

    private static final int NUMBER_ITEM_FIRST_LOAD = 20;
    private static final int NUMBER_ITEM_LOAD_MORE = 5;

    public UpComingEventFragment() {
    }

    public static UpComingEventFragment newInstance() {
        UpComingEventFragment fragment = new UpComingEventFragment();
        Bundle args = new Bundle();
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }


    private void refreshItems() {

        this.getEvents(new OnReceivedDataListener() {
            @Override
            public void onSuccess(Object data) {
                mEvents.clear();
                mEvents.addAll((List<Event>) data);

                mAdapter.notifyDataSetChanged();

                if (mEvents.size() == 0) {
                    mTvStatus.setText(getString(R.string.msg_status_empty));
                    mTvStatus.setVisibility(View.VISIBLE);
                } else {
                    mTvStatus.setVisibility(View.GONE);
                }

                onItemsLoadComplete();
            }

            @Override
            public void onFailure() {
                Toast.makeText(getContext(), getString(R.string.msg_status_error), Toast.LENGTH_SHORT).show();
                onItemsLoadComplete();
            }

            @Override
            public void onStart() {
            }
        }, NUMBER_ITEM_FIRST_LOAD);
    }

    void onItemsLoadComplete() {
        mSwipeRefreshLayout.setRefreshing(false);
    }

    private void getEvents(final OnReceivedDataListener listener, int num) {

        SharedPreferences prefs = getContext().getSharedPreferences(DataStorage.APP_PREFS, MODE_PRIVATE);
        String token = prefs.getString(DataStorage.TOKEN, null);

        if (token != null) {
            listener.onStart();

            RestfulAPI restfulAPI = new RestfulAPI();

            Retrofit retrofit = restfulAPI.getRestClient();

            EventEndpointInterface apiService =
                    retrofit.create(EventEndpointInterface.class);
            Call<List<Event>> call = apiService.getEvents(num);
            call.enqueue(new Callback<List<Event>>() {
                @Override
                public void onResponse(Call<List<Event>> call, Response<List<Event>> response) {
                    if (response.body() != null) {
                        if (response.code() == 200) {
                            listener.onSuccess(response.body());
                        } else {
                            listener.onFailure();
                        }
                    }
                }

                @Override
                public void onFailure(Call<List<Event>> call, Throwable t) {
                    listener.onFailure();
                }
            });
        }

    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {

        View view = inflater.inflate(R.layout.fragment_up_coming_event, container, false);

        mSwipeRefreshLayout = (SwipeRefreshLayout) view.findViewById(R.id.swipeRefreshLayout);

        mSwipeRefreshLayout.setOnRefreshListener(new SwipeRefreshLayout.OnRefreshListener() {
            @Override
            public void onRefresh() {
                refreshItems();
            }
        });

        mRecyclerView = (RecyclerView) view.findViewById(R.id.recyclerView);
        mLinearLayoutManager = new LinearLayoutManager(getContext());
        mRecyclerView.setLayoutManager(mLinearLayoutManager);

        mProgressBar = (ProgressBar) view.findViewById(R.id.progressBar);
        mProgressBar.setIndeterminate(true);
        mProgressBar.setVisibility(View.VISIBLE);
        mTvStatus = (TextView) view.findViewById(R.id.txt_status);

        mEvents = new ArrayList<>();
        mAdapter = new EventAdapter(mEvents);
        mRecyclerView.setAdapter(mAdapter);

        this.getEvents(new OnReceivedDataListener() {
            @Override
            public void onSuccess(Object data) {

                mEvents.clear();

                mEvents.addAll((List<Event>) data);
                mAdapter.notifyDataSetChanged();

                if (mEvents.size() == 0) {
                    mTvStatus.setText(getString(R.string.msg_status_empty));
                    mTvStatus.setVisibility(View.VISIBLE);
                } else {
                    mTvStatus.setVisibility(View.GONE);
                }

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

        }, NUMBER_ITEM_FIRST_LOAD);

        mRecyclerView.addOnScrollListener(new RecyclerView.OnScrollListener() {
            @Override
            public void onScrollStateChanged(RecyclerView recyclerView, int newState) {
                super.onScrollStateChanged(recyclerView, newState);
            }

            @Override
            public void onScrolled(RecyclerView recyclerView, int dx, int dy) {
                super.onScrolled(recyclerView, dx, dy);

                if (dy > 0) //check for scroll down
                {
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

        getEvents(new OnReceivedDataListener() {
            @Override
            public void onSuccess(Object data) {

                mAdapter.hideLoading();

                List<Event> events = (List<Event>) data;

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
                Toast.makeText(getContext(), getString(R.string.msg_status_error), Toast.LENGTH_SHORT).show();
                isLoading = true;
                mAdapter.hideLoading();
            }

            @Override
            public void onStart() {
                mAdapter.showLoading();
            }
        }, mEvents.size() + NUMBER_ITEM_LOAD_MORE);

    }


}

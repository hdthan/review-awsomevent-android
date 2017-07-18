package vn.axonactive.aevent.fragment;

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

import java.net.HttpURLConnection;
import java.util.ArrayList;
import java.util.List;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;
import retrofit2.Retrofit;
import vn.axonactive.aevent.R;
import vn.axonactive.aevent.adapter.SpeakerAdapter;
import vn.axonactive.aevent.api.EventEndpointInterface;
import vn.axonactive.aevent.api.RestfulAPI;
import vn.axonactive.aevent.listener.OnReceivedDataListener;
import vn.axonactive.aevent.model.Speaker;
import vn.axonactive.aevent.util.DataStorage;

public class SpeakerFragment extends Fragment {

    private RecyclerView mRecyclerView;
    private LinearLayoutManager mLinearLayoutManager;
    private SwipeRefreshLayout mSwipeRefreshLayout;
    private ProgressBar mProgressBar;

    private SpeakerAdapter mAdapter;
    private TextView mTvComingSoon;

    private List<Speaker> mSpeakers;

    public SpeakerFragment() {

    }

    public static SpeakerFragment newInstance() {
        SpeakerFragment fragment = new SpeakerFragment();
        Bundle args = new Bundle();
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {

        mSpeakers = new ArrayList<>();

        View view = inflater.inflate(R.layout.fragment_speaker, container, false);

        mRecyclerView = (RecyclerView) view.findViewById(R.id.recyclerView);
        mTvComingSoon = (TextView) view.findViewById(R.id.txt_coming_soon);
        mProgressBar = (ProgressBar) view.findViewById(R.id.progressBar);

        mSwipeRefreshLayout = (SwipeRefreshLayout) view.findViewById(R.id.swipeRefreshLayout);

        mSwipeRefreshLayout.setOnRefreshListener(new SwipeRefreshLayout.OnRefreshListener() {
            @Override
            public void onRefresh() {
                refreshItems();
            }
        });

        mLinearLayoutManager = new LinearLayoutManager(getContext());

        mRecyclerView.setLayoutManager(mLinearLayoutManager);

        mAdapter = new SpeakerAdapter(mSpeakers);

        mRecyclerView.setAdapter(mAdapter);

        this.getSpeaker(new OnReceivedDataListener() {
            @Override
            public void onSuccess(Object data) {

                loadData((List<Speaker>) data);

                mProgressBar.setVisibility(View.GONE);
            }

            @Override
            public void onFailure() {

                mProgressBar.setVisibility(View.GONE);
            }

            @Override
            public void onStart() {
                mProgressBar.setVisibility(View.VISIBLE);
            }
        });

        return view;
    }

    private void refreshItems() {

        this.getSpeaker(new OnReceivedDataListener() {
            @Override
            public void onSuccess(Object data) {

                loadData((List<Speaker>) data);

                onItemsLoadComplete();
            }

            @Override
            public void onFailure() {
                onItemsLoadComplete();
            }

            @Override
            public void onStart() {

            }
        });

    }

    private void loadData(List<Speaker> speakers) {
        mSpeakers.clear();

        mSpeakers.addAll(speakers);

        mAdapter.notifyDataSetChanged();

        if (mSpeakers.size() == 0) {
            mTvComingSoon.setVisibility(View.VISIBLE);
        } else {
            mTvComingSoon.setVisibility(View.GONE);
        }
    }

    void onItemsLoadComplete() {
        mSwipeRefreshLayout.setRefreshing(false);
    }

    private void getSpeaker(final OnReceivedDataListener listener) {


        listener.onStart();

        RestfulAPI restfulAPI = new RestfulAPI();
        Retrofit retrofit = restfulAPI.getRestClient();

        EventEndpointInterface apiService = retrofit.create(EventEndpointInterface.class);

        Call<List<Speaker>> call = apiService.getSpeaker(DataStorage.id);

        call.enqueue(new Callback<List<Speaker>>() {
            @Override
            public void onResponse(Call<List<Speaker>> call, Response<List<Speaker>> response) {

                int statusCode = response.code();

                if (statusCode == HttpURLConnection.HTTP_OK) {
                    listener.onSuccess(response.body());
                } else {
                    listener.onFailure();
                }

            }

            @Override
            public void onFailure(Call<List<Speaker>> call, Throwable t) {
                listener.onFailure();
            }
        });

    }

}

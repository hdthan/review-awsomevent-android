package vn.axonactive.aevent.fragment;

import android.os.Bundle;
import android.support.annotation.Nullable;
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
import java.util.Date;
import java.util.List;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;
import retrofit2.Retrofit;
import vn.axonactive.aevent.R;
import vn.axonactive.aevent.adapter.TimeLineAdapter;
import vn.axonactive.aevent.api.EventEndpointInterface;
import vn.axonactive.aevent.api.RestfulAPI;
import vn.axonactive.aevent.listener.OnReceivedDataListener;
import vn.axonactive.aevent.model.TimeLineModel;
import vn.axonactive.aevent.model.Topic;
import vn.axonactive.aevent.util.DataStorage;
import vn.axonactive.aevent.util.DateTimeUtil;

/**
 * Created by ltphuc on 3/10/2017.
 */

public class TimeLineFragment extends Fragment {

    private RecyclerView mRecyclerView;
    private LinearLayoutManager mLinearLayoutManager;
    private SwipeRefreshLayout mSwipeRefreshLayout;
    private TimeLineAdapter mAdapter;
    private TextView mTvComingSoon;
    private ProgressBar mProgressBar;

    private List<TimeLineModel> mTimeLines;

    private int pos = -1;

    public TimeLineFragment() {

    }

    public static TimeLineFragment newInstance() {
        TimeLineFragment fragment = new TimeLineFragment();
        Bundle args = new Bundle();
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {

        View view = inflater.inflate(R.layout.fragment_time_line, container, false);

        mTimeLines = new ArrayList<>();

        mSwipeRefreshLayout = (SwipeRefreshLayout) view.findViewById(R.id.swipeRefreshLayout);

        mSwipeRefreshLayout.setOnRefreshListener(new SwipeRefreshLayout.OnRefreshListener() {
            @Override
            public void onRefresh() {
                refreshItems();
            }
        });

        mRecyclerView = (RecyclerView) view.findViewById(R.id.recyclerView);
        mTvComingSoon = (TextView) view.findViewById(R.id.txt_coming_soon);
        mProgressBar = (ProgressBar) view.findViewById(R.id.progressBar);

        mLinearLayoutManager = new LinearLayoutManager(getContext());

        mRecyclerView.setLayoutManager(mLinearLayoutManager);

        mAdapter = new TimeLineAdapter(mTimeLines);

        mRecyclerView.setAdapter(mAdapter);

        this.getTopics(new OnReceivedDataListener() {
            @Override
            public void onSuccess(Object data) {

                loadData((List<Topic>) data);

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

    private void loadData(List<Topic> topics) {

        mTimeLines.clear();

        mTimeLines.addAll(getTimeLine(topics));

        if (pos != -1) {
            mLinearLayoutManager.scrollToPositionWithOffset(pos, 20);
        }

        mAdapter.notifyDataSetChanged();

        if (mTimeLines.size() == 0) {
            mTvComingSoon.setVisibility(View.VISIBLE);
        } else {
            mTvComingSoon.setVisibility(View.GONE);
        }
    }

    private void getTopics(final OnReceivedDataListener listener) {

        listener.onStart();

        RestfulAPI restfulAPI = new RestfulAPI();
        Retrofit retrofit = restfulAPI.getRestClient();

        EventEndpointInterface apiService = retrofit.create(EventEndpointInterface.class);

        Call<List<Topic>> call = apiService.getTopic(DataStorage.id);

        call.enqueue(new Callback<List<Topic>>() {
            @Override
            public void onResponse(Call<List<Topic>> call, Response<List<Topic>> response) {

                int statusCode = response.code();

                if (statusCode == HttpURLConnection.HTTP_OK) {
                    listener.onSuccess(response.body());
                } else {
                    listener.onFailure();
                }

            }

            @Override
            public void onFailure(Call<List<Topic>> call, Throwable t) {
                listener.onFailure();
            }
        });

    }

    private void refreshItems() {

        this.getTopics(new OnReceivedDataListener() {
            @Override
            public void onSuccess(Object data) {

                loadData((List<Topic>) data);

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

    void onItemsLoadComplete() {
        mSwipeRefreshLayout.setRefreshing(false);
    }

    private List<TimeLineModel> getTimeLine(List<Topic> topics) {

        List<TimeLineModel> timeLines = new ArrayList<>();

        boolean invisibleDate = false;
        boolean invisibleTime = false;

        int size = topics.size();

        int topicId = getArguments().getInt("topicId");

        for (int i = 0; i < size; i++) {

            Topic t = topics.get(i);

            if (topicId == t.getId()) {
                pos = i;
            }

            Date currentStartTime = t.getStartTime();

            TimeLineModel timeLine = new TimeLineModel(t.getTitle(), t.getLocation(), t.getStartTime(), t.getEndTime(), t.getDescription());
            timeLine.setInvisibleDate(invisibleDate);
            timeLine.setInvisibleTime(invisibleTime);
            timeLine.setTopicSpeakers(t.getTopicSpeakers());
            timeLines.add(timeLine);

            if (i < size - 1) {

                Date nextStartTime = topics.get(i + 1).getStartTime();

                String currentDate = DateTimeUtil.parseDateTime(currentStartTime, "dd-MM-yyyy");
                String currentTime = DateTimeUtil.parseDateTime(currentStartTime, "hh:mm");
                String nextDate = DateTimeUtil.parseDateTime(nextStartTime, "dd-MM-yyyy");
                String nextTime = DateTimeUtil.parseDateTime(nextStartTime, "hh:mm");

                invisibleDate = currentDate.equals(nextDate);
                invisibleTime = invisibleDate && currentTime.equals(nextTime);

            }

        }

        return timeLines;

    }
}

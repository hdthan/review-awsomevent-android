package vn.axonactive.aevent_organizer.fragment;

import android.content.Context;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;
import retrofit2.Retrofit;
import vn.axonactive.aevent_organizer.R;
import vn.axonactive.aevent_organizer.adapter.TimeLineAdapter;
import vn.axonactive.aevent_organizer.api.RestfulAPI;
import vn.axonactive.aevent_organizer.api.TopicEndpointInterface;
import vn.axonactive.aevent_organizer.model.TimeLineModel;
import vn.axonactive.aevent_organizer.model.Topic;
import vn.axonactive.aevent_organizer.util.DataStorage;
import vn.axonactive.aevent_organizer.util.DateTimeUtil;
import vn.axonactive.aevent_organizer.util.GlobalBus;

/**
 * Created by ltphuc on 3/10/2017.
 */

public class TimeLineFragment extends Fragment {

    private RecyclerView mRecyclerView;
    private LinearLayoutManager mLinearLayoutManager;
    private TimeLineAdapter mAdapter;
    private ProgressBar mProgressBar;
    private TextView mTvStatus;

    private long eventId;

    public TimeLineFragment() {

    }

    public static TimeLineFragment newInstance() {
        TimeLineFragment fragment = new TimeLineFragment();
        Bundle args = new Bundle();
        fragment.setArguments(args);
        return fragment;
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
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    @Override
    public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
        super.onCreateOptionsMenu(menu, inflater);
        menu.clear();
    }

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {

        setHasOptionsMenu(true);
        eventId = getArguments().getLong("eventId");

        View view = inflater.inflate(R.layout.fragment_time_line, container, false);

        mRecyclerView = (RecyclerView) view.findViewById(R.id.recyclerView);

        mProgressBar = (ProgressBar) view.findViewById(R.id.progressBar);
        mProgressBar.setIndeterminate(true);
        mProgressBar.setVisibility(View.VISIBLE);
        mTvStatus = (TextView) view.findViewById(R.id.txt_status);

        mLinearLayoutManager = new LinearLayoutManager(getContext());

        mRecyclerView.setLayoutManager(mLinearLayoutManager);

        getTopics();

        return view;
    }

    private void getTopics() {

        RestfulAPI restfulAPI = new RestfulAPI();

        Retrofit retrofit = restfulAPI.getRestClient();

        TopicEndpointInterface apiService = retrofit.create(TopicEndpointInterface.class);

        SharedPreferences prefs = getContext().getSharedPreferences(DataStorage.APP_PREFS, Context.MODE_PRIVATE);
        String token = prefs.getString(DataStorage.TOKEN, null);

        if (token != null) {
            Call<List<Topic>> call = apiService.getTopicsByEventId(token, eventId);
            call.enqueue(new Callback<List<Topic>>() {
                @Override
                public void onResponse(Call<List<Topic>> call, final Response<List<Topic>> response) {
                    if (response != null) {
                        if (response.code() == 200) {
                            getTimeLine(response.body());
                        } else {
                            Toast.makeText(getContext(), "Error here", Toast.LENGTH_LONG).show();
                        }
                    }
                    mProgressBar.setVisibility(View.GONE);
                }

                @Override
                public void onFailure(Call<List<Topic>> call, Throwable t) {
                    mProgressBar.setVisibility(View.GONE);
                }
            });
        }

    }

    private void getTimeLine(List<Topic> topics) {

        List<TimeLineModel> timeLines = new ArrayList<>();

        boolean invisibleDate = false;
        boolean invisibleTime = false;

        int size = topics.size();

        if (size == 0) {
            mTvStatus.setVisibility(View.VISIBLE);
        } else {
            mTvStatus.setVisibility(View.GONE);
        }

        for (int i = 0; i < size; i++) {

            Topic t = topics.get(i);

            Date currentStartTime = t.getStartTime();

            TimeLineModel timeLine = new TimeLineModel(t.getId(), t.getTitle(), t.getLocation(), t.getStartTime(), t.getEndTime());
            timeLine.setInvisibleDate(invisibleDate);
            timeLine.setInvisibleTime(invisibleTime);
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

        mAdapter = new TimeLineAdapter(timeLines);

        mRecyclerView.setAdapter(mAdapter);
    }
}

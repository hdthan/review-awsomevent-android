package vn.axonactive.aevent_organizer.fragment;

import android.content.ActivityNotFoundException;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.support.design.widget.FloatingActionButton;
import android.support.v4.app.Fragment;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ProgressBar;
import android.widget.SearchView;
import android.widget.TextView;
import android.widget.Toast;

import com.google.firebase.database.ChildEventListener;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

import java.net.HttpURLConnection;
import java.text.Normalizer;
import java.util.ArrayList;
import java.util.List;

import es.dmoral.toasty.Toasty;
import okhttp3.MediaType;
import okhttp3.RequestBody;
import okhttp3.ResponseBody;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;
import retrofit2.Retrofit;
import vn.axonactive.aevent_organizer.R;
import vn.axonactive.aevent_organizer.activity.ScanQrCodeActivity;
import vn.axonactive.aevent_organizer.adapter.ParticipantAdapter;
import vn.axonactive.aevent_organizer.api.EventEndpointInterface;
import vn.axonactive.aevent_organizer.api.RestfulAPI;
import vn.axonactive.aevent_organizer.model.Enrollment;
import vn.axonactive.aevent_organizer.model.Participant;
import vn.axonactive.aevent_organizer.model.Statistic;
import vn.axonactive.aevent_organizer.model.User;
import vn.axonactive.aevent_organizer.util.DataStorage;
import vn.axonactive.aevent_organizer.util.GlobalBus;

import static android.app.Activity.RESULT_OK;


public class CheckInFragment extends Fragment implements SearchView.OnQueryTextListener {

    private RecyclerView mRecyclerView;
    private LinearLayoutManager mLinearLayoutManager;
    private ParticipantAdapter mAdapter;
    private FloatingActionButton mScan;
    private SearchView mSearchView;

    private ProgressBar mProgressBar;
    private TextView mTvStatus;

    private List<Participant> mParticipants;

    private int checkIn;
    private int total;

    private long eventId;

    private FirebaseDatabase mDatabase;
    private DatabaseReference mEventRef;
    private ChildEventListener mListener;

    public CheckInFragment() {
    }

    public static CheckInFragment newInstance() {
        CheckInFragment fragment = new CheckInFragment();
        Bundle args = new Bundle();
        fragment.setArguments(args);
        return fragment;
    }

    private void sendStatisticDataToActivity(Statistic statistic) {
        GlobalBus.getBus().post(statistic);
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
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        eventId = getArguments().getLong("eventId");
        mDatabase = FirebaseDatabase.getInstance();
        mEventRef = mDatabase.getReference("events").child(eventId + "");
    }

    private void getParticipants() {

        total = 0;
        checkIn = 0;

        SharedPreferences preferences = getContext().getSharedPreferences(DataStorage.APP_PREFS, Context.MODE_PRIVATE);
        String token = preferences.getString(DataStorage.TOKEN, null);

        if (token != null) {
            RestfulAPI restfulAPI = new RestfulAPI();
            Retrofit retrofit = restfulAPI.getRestClient();
            final EventEndpointInterface apiService = retrofit.create(EventEndpointInterface.class);
            Call<List<Enrollment>> call = apiService.getListParticipant(eventId, token);

            call.enqueue(new Callback<List<Enrollment>>() {
                @Override
                public void onResponse(Call<List<Enrollment>> call, Response<List<Enrollment>> response) {

                    int statusCode = response.code();

                    if (statusCode == HttpURLConnection.HTTP_OK) {

                        List<Enrollment> enrollments = response.body();

                        for (Enrollment enrollment : enrollments) {

                            User user = enrollment.getUser();

                            Participant p = new Participant(user.getId(), enrollment.getCheckIn(), enrollment.getAuthorizationCode(), user.getEmail(), user.getFullName(), user.getPhone());
                            mParticipants.add(p);

                            checkIn += p.getCheck();
                        }

                        total = mParticipants.size();
                        if (total == 0) {
                            mTvStatus.setVisibility(View.VISIBLE);
                            mTvStatus.setText(getString(R.string.msg_status_empty));
                        } else {
                            mTvStatus.setVisibility(View.GONE);
                        }

                        mAdapter.notifyDataSetChanged();

                        mListener = new ChildEventListener() {
                            @Override
                            public void onChildAdded(DataSnapshot dataSnapshot, String s) {

                                long userId = Long.parseLong(dataSnapshot.getKey());

                                for (Participant p : mParticipants) {
                                    if (p.getUserId() == userId) {
                                        return;
                                    }
                                }

                                Participant participant = dataSnapshot.getValue(Participant.class);
                                participant.setUserId(userId);

                                mParticipants.add(0, participant);
                                mAdapter.notifyItemInserted(0);

                                total++;
                                checkIn += participant.getCheck();

                                sendStatisticDataToActivity(new Statistic(checkIn, total));
                            }

                            @Override
                            public void onChildChanged(DataSnapshot dataSnapshot, String s) {

                                long userId = Long.parseLong(dataSnapshot.getKey());

                                Participant participant = dataSnapshot.getValue(Participant.class);
                                participant.setUserId(userId);

                                for (Participant p : mParticipants) {

                                    if (p.getUserId() == userId) {

                                        if (p.getCheck() != participant.getCheck()) {
                                            p.setCheck(participant.getCheck());
                                            mAdapter.notifyDataSetChanged();
                                            if (participant.getCheck() == 1) {
                                                checkIn++;
                                            } else {
                                                checkIn--;
                                            }
                                            sendStatisticDataToActivity(new Statistic(checkIn, total));
                                        }

                                        break;
                                    }

                                }
                            }

                            @Override
                            public void onChildRemoved(DataSnapshot dataSnapshot) {

                                long userId = Long.parseLong(dataSnapshot.getKey());

                                Participant participant = dataSnapshot.getValue(Participant.class);
                                participant.setUserId(userId);

                                for (int i = 0; i < mParticipants.size(); i++) {

                                    Participant p = mParticipants.get(i);

                                    if (p.getUserId() == userId) {

                                        total--;

                                        checkIn -= p.getCheck();

                                        mParticipants.remove(i);
                                        mAdapter.notifyDataSetChanged();
                                        sendStatisticDataToActivity(new Statistic(checkIn, total));
                                        break;
                                    }

                                }

                            }

                            @Override
                            public void onChildMoved(DataSnapshot dataSnapshot, String s) {

                            }

                            @Override
                            public void onCancelled(DatabaseError databaseError) {

                            }
                        };

                        mEventRef.addChildEventListener(mListener);

                        //when load all data
                        sendStatisticDataToActivity(new Statistic(checkIn, total));

                    } else {
                        mTvStatus.setVisibility(View.VISIBLE);
                        mTvStatus.setText(getString(R.string.msg_status_empty));
                    }

                    mProgressBar.setVisibility(View.GONE);

                }

                @Override
                public void onFailure(Call<List<Enrollment>> call, Throwable t) {
                    mTvStatus.setVisibility(View.VISIBLE);
                    mTvStatus.setText(getString(R.string.msg_status_error));
                    mProgressBar.setVisibility(View.GONE);
                }
            });
        }
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {


        View view = inflater.inflate(R.layout.fragment_event_check_in, container, false);

        mSearchView = (SearchView) view.findViewById(R.id.searchView);
        mSearchView.setOnQueryTextListener(this);

        mRecyclerView = (RecyclerView) view.findViewById(R.id.participant_recyclerView);
        mLinearLayoutManager = new LinearLayoutManager(getActivity());
        mRecyclerView.setLayoutManager(mLinearLayoutManager);

        mProgressBar = (ProgressBar) view.findViewById(R.id.progressBar);
        mTvStatus = (TextView) view.findViewById(R.id.tv_status);

        mParticipants = new ArrayList<>();

        mAdapter = new ParticipantAdapter(mParticipants, eventId);

        mRecyclerView.setAdapter(mAdapter);

        getParticipants();

        //scan qr code
        mScan = (FloatingActionButton) view.findViewById(R.id.scan);

        mScan.setOnClickListener(
                new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        Bundle bundle = new Bundle();
                        bundle.putLong("eventId", eventId);
                        Intent intent = new Intent(getContext(), ScanQrCodeActivity.class);
                        intent.putExtras(bundle);
                        startActivity(intent);
                    }
                }
        );

        return view;
    }


    @Override
    public void onDestroyView() {
        super.onDestroyView();
        mEventRef.removeEventListener(mListener);
    }


    private List<Participant> filter(List<Participant> participants, String queryText) {

        queryText = queryText.toLowerCase();

        List<Participant> filterParticipants = new ArrayList<>();

        for (Participant participant : participants) {

            boolean check = false;

            if (participant.getFullName() != null) {
                String fullName = participant.getFullName().toLowerCase();

                fullName = Normalizer.normalize(fullName, Normalizer.Form.NFD);

                fullName = fullName.replaceAll("[^a-zA-Z ]", "");

                check = fullName.contains(queryText);
            }

            if (!check && participant.getEmail() != null) {
                String email = participant.getEmail();

                int index = email.indexOf('@');

                if (index > 0) {
                    email = email.substring(0, index);

                    check = email.contains(queryText);
                }
            }

            if (!check && participant.getPhone() != null) {

                String phone = participant.getPhone();

                check = phone.contains(queryText);

            }

            if (check) {
                filterParticipants.add(participant);
            }
        }

        return filterParticipants;

    }

    @Override
    public boolean onQueryTextSubmit(String query) {
        return false;
    }

    @Override
    public boolean onQueryTextChange(String newText) {

        if (mParticipants != null && mParticipants.size() > 0) {
            newText = newText.trim();

            final List<Participant> filteredModelList = filter(mParticipants, newText);

            mAdapter.setFilter(filteredModelList);
        }
        return true;
    }
}

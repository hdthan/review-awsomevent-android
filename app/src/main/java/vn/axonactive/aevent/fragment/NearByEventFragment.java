package vn.axonactive.aevent.fragment;

import android.Manifest;
import android.content.IntentSender;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v4.app.ActivityCompat;
import android.support.v4.app.Fragment;
import android.support.v4.content.ContextCompat;
import android.support.v4.widget.SwipeRefreshLayout;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.appindexing.AppIndex;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.common.api.PendingResult;
import com.google.android.gms.common.api.ResultCallback;
import com.google.android.gms.common.api.Status;
import com.google.android.gms.location.LocationRequest;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.location.LocationSettingsRequest;
import com.google.android.gms.location.LocationSettingsResult;
import com.google.android.gms.location.LocationSettingsStatusCodes;

import java.net.HttpURLConnection;
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
import vn.axonactive.aevent.util.GPSTracker;

import static android.content.Context.MODE_PRIVATE;

public class NearByEventFragment extends Fragment {

    private RecyclerView mRecyclerView;
    private LinearLayoutManager mLinearLayoutManager;
    private EventAdapter mAdapter;
    private SwipeRefreshLayout mSwipeRefreshLayout;
    private ProgressBar mProgressBar;
    private TextView mTvStatus;

    private List<Event> mEvents;

    private GPSTracker gpsTracker;

    private boolean isLoading = true;
    int pastVisibleItems, visibleItemCount, totalItemCount;

    static final Integer LOCATION = 0x1;
    static final Integer GPS_SETTINGS = 0x7;

    GoogleApiClient client;
    LocationRequest mLocationRequest;
    PendingResult<LocationSettingsResult> result;

    private static final int NUMBER_ITEM_FIRST_LOAD = 20;
    private static final int NUMBER_ITEM_LOAD_MORE = 5;

    public NearByEventFragment() {
    }

    public static NearByEventFragment newInstance() {
        NearByEventFragment fragment = new NearByEventFragment();
        Bundle args = new Bundle();
        fragment.setArguments(args);
        return fragment;
    }

    private void getNearByEvent(final OnReceivedDataListener listener, int num) {

        SharedPreferences prefs = getContext().getSharedPreferences(DataStorage.APP_PREFS, MODE_PRIVATE);
        String token = prefs.getString(DataStorage.TOKEN, null);
        if (token != null) {

            gpsTracker = new GPSTracker(getContext());

            if (gpsTracker.canGetLocation()) {

                double latitude = gpsTracker.getLatitude();
                double longitude = gpsTracker.getLongitude();

                listener.onStart();

                RestfulAPI restfulAPI = new RestfulAPI();

                Retrofit retrofit = restfulAPI.getRestClient();

                EventEndpointInterface apiService =
                        retrofit.create(EventEndpointInterface.class);
                Call<List<Event>> call = apiService.getNearByEvent(latitude, longitude, num);

                call.enqueue(new Callback<List<Event>>() {
                    @Override
                    public void onResponse(Call<List<Event>> call, Response<List<Event>> response) {
                        if (response.body() != null) {
                            if (response.code() == HttpURLConnection.HTTP_OK) {
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

            } else {
                gpsTracker.showSettingsAlert();
            }
        }

    }

    private void refreshItems() {

        this.getNearByEvent(new OnReceivedDataListener() {
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

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {

        View view = inflater.inflate(R.layout.fragment_near_by_event, container, false);

        client = new GoogleApiClient.Builder(getContext())
                .addApi(AppIndex.API)
                .addApi(LocationServices.API)
                .build();

        askForPermission(Manifest.permission.ACCESS_FINE_LOCATION, LOCATION);

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

        this.getNearByEvent(new OnReceivedDataListener() {
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

    @Override
    public void onStart() {
        super.onStart();
        client.connect();
    }

    @Override
    public void onStop() {
        super.onStop();
        client.disconnect();
    }

    private void askForGPS() {
        mLocationRequest = LocationRequest.create();
        mLocationRequest.setPriority(LocationRequest.PRIORITY_HIGH_ACCURACY);
        mLocationRequest.setInterval(30 * 1000);
        mLocationRequest.setFastestInterval(5 * 1000);
        LocationSettingsRequest.Builder builder = new LocationSettingsRequest.Builder().addLocationRequest(mLocationRequest);
        builder.setAlwaysShow(true);
        result = LocationServices.SettingsApi.checkLocationSettings(client, builder.build());
        result.setResultCallback(new ResultCallback<LocationSettingsResult>() {
            @Override
            public void onResult(LocationSettingsResult result) {
                final Status status = result.getStatus();
                switch (status.getStatusCode()) {
                    case LocationSettingsStatusCodes.SUCCESS:
                        break;
                    case LocationSettingsStatusCodes.RESOLUTION_REQUIRED:
                        try {
                            status.startResolutionForResult(getActivity(), GPS_SETTINGS);
                        } catch (IntentSender.SendIntentException e) {

                        }
                        break;
                    case LocationSettingsStatusCodes.SETTINGS_CHANGE_UNAVAILABLE:
                        break;
                }
            }
        });
    }

    private void loadMoreData() {

        getNearByEvent(new OnReceivedDataListener() {
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

    @Override
    public void onPause() {
        super.onPause();
        if (gpsTracker != null) {
            gpsTracker.stopUsingGPS();
        }
    }

    @Override
    public void onResume() {
        super.onResume();

        if (gpsTracker != null && !gpsTracker.isGPSEnabled) {
            gpsTracker = new GPSTracker(getContext());
        }

    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);

        if (ActivityCompat.checkSelfPermission(getContext(), permissions[0]) == PackageManager.PERMISSION_GRANTED) {
            switch (requestCode) {
                case 1:
                    askForGPS();
                    break;
            }

            //Permission granted
        } else {
            //Permission denied
        }
    }

    private void askForPermission(String permission, Integer requestCode) {
        if (ContextCompat.checkSelfPermission(getContext(), permission) != PackageManager.PERMISSION_GRANTED) {

            if (ActivityCompat.shouldShowRequestPermissionRationale(getActivity(), permission)) {
                ActivityCompat.requestPermissions(getActivity(), new String[]{permission}, requestCode);
            } else {
                ActivityCompat.requestPermissions(getActivity(), new String[]{permission}, requestCode);
            }
        } else {
            //Permission already granted
        }
    }

}

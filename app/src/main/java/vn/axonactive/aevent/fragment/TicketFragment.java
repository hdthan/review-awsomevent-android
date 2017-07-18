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

import es.dmoral.toasty.Toasty;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;
import retrofit2.Retrofit;
import vn.axonactive.aevent.R;
import vn.axonactive.aevent.adapter.TicketAdapter;
import vn.axonactive.aevent.api.EnrollmentEndpointInterface;
import vn.axonactive.aevent.api.RestfulAPI;
import vn.axonactive.aevent.listener.OnReceivedDataListener;
import vn.axonactive.aevent.model.Enrollment;
import vn.axonactive.aevent.sqlite.EnrollmentDataSource;
import vn.axonactive.aevent.util.DataStorage;

import static android.content.Context.MODE_PRIVATE;

/**
 * Created by Dell on 2/12/2017.
 */

public class TicketFragment extends Fragment {

    private String ticketType = "up-coming";

    private RecyclerView mRecyclerView;
    private LinearLayoutManager mLinearLayoutManager;
    private TicketAdapter mAdapter;
    private SwipeRefreshLayout mSwipeRefreshLayout;

    private ProgressBar mProgressBar;
    private TextView mTvStatus;

    private List<Enrollment> mEnrollments;

    private boolean isLoading = true;
    int pastVisibleItems, visibleItemCount, totalItemCount;

    private EnrollmentDataSource dataSource;

    private static final int NUMBER_ITEM_FIRST_LOAD = 20;
    private static final int NUMBER_ITEM_LOAD_MORE = 5;

    public TicketFragment() {

    }

    public void setTicketType(String ticketType) {
        this.ticketType = ticketType;
    }

    public static TicketFragment newInstance(String ticketType) {


        TicketFragment fragment = new TicketFragment();
        fragment.setTicketType(ticketType);
        Bundle args = new Bundle();
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {

        View view = inflater.inflate(R.layout.fragment_ticket, container, false);

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

        dataSource = new EnrollmentDataSource(getContext());

        mEnrollments = new ArrayList<>();
        mAdapter = new TicketAdapter(mEnrollments);
        mRecyclerView.setAdapter(mAdapter);

        this.getTickets(NUMBER_ITEM_FIRST_LOAD, new OnReceivedDataListener() {
            @Override
            public void onSuccess(Object data) {

                mEnrollments.clear();
                mEnrollments.addAll((List<Enrollment>) data);
                mAdapter.notifyDataSetChanged();

                if (mEnrollments.size() == 0) {
                    mTvStatus.setText(getString(R.string.msg_status_empty));
                    mTvStatus.setVisibility(View.VISIBLE);
                } else {
                    mTvStatus.setVisibility(View.GONE);
                }

                mProgressBar.setVisibility(View.GONE);

                if ("up-coming".equals(ticketType)) {
                    dataSource.open();
                    dataSource.addEnrollment(mEnrollments);
                    dataSource.close();
                }
            }

            @Override
            public void onFailure() {

                if ("up-coming".equals(ticketType)) {

                    dataSource.open();

                    mEnrollments.addAll(dataSource.getAllEnrollment());
                    mAdapter.notifyDataSetChanged();

                    if (mEnrollments.size() == 0) {
                        mTvStatus.setText(getString(R.string.msg_status_empty));
                        mTvStatus.setVisibility(View.VISIBLE);
                    } else {
                        mTvStatus.setVisibility(View.GONE);
                    }
                }else{
                    mTvStatus.setText(getString(R.string.msg_status_error));
                    mTvStatus.setVisibility(View.VISIBLE);
                }

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

        getTickets(mEnrollments.size() + NUMBER_ITEM_LOAD_MORE, new OnReceivedDataListener() {
            @Override
            public void onSuccess(Object data) {

                mAdapter.hideLoading();

                List<Enrollment> enrollments = (List<Enrollment>) data;

                dataSource.open();

                if (enrollments.size() > mEnrollments.size()) {
                    for (int i = mEnrollments.size(); i < enrollments.size(); i++) {
                        mEnrollments.add(enrollments.get(i));
                        mAdapter.notifyItemInserted(mEnrollments.size());
                        if ("up-coming".equals(ticketType)) {
                            dataSource.addEnrollment(enrollments.get(i));
                        }
                    }
                }

                dataSource.close();

                isLoading = true;

            }

            @Override
            public void onFailure() {
                mAdapter.hideLoading();
                Toasty.normal(getContext(), getString(R.string.msg_status_error)).show();
                isLoading = true;
            }

            @Override
            public void onStart() {
                mAdapter.showLoading();
            }
        });

    }

    private void refreshItems() {

        this.getTickets(NUMBER_ITEM_FIRST_LOAD, new OnReceivedDataListener() {
            @Override
            public void onSuccess(Object data) {

                mEnrollments.clear();

                mEnrollments.addAll((List<Enrollment>) data);

                if (mEnrollments.size() == 0) {
                    mTvStatus.setText(getString(R.string.msg_status_empty));
                    mTvStatus.setVisibility(View.VISIBLE);
                } else {
                    mTvStatus.setVisibility(View.GONE);
                }

                if ("up-coming".equals(ticketType)) {
                    dataSource.open();
                    dataSource.addEnrollment(mEnrollments);
                    dataSource.close();
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
        });
    }

    void onItemsLoadComplete() {
        mSwipeRefreshLayout.setRefreshing(false);
    }

    private void getTickets(int num, final OnReceivedDataListener listener) {

        SharedPreferences prefs = getContext().getSharedPreferences(DataStorage.APP_PREFS, MODE_PRIVATE);
        String token = prefs.getString(DataStorage.TOKEN, null);
        if (token != null) {
            listener.onStart();

            RestfulAPI restfulAPI = new RestfulAPI();

            Retrofit retrofit = restfulAPI.getRestClient();

            EnrollmentEndpointInterface apiService =
                    retrofit.create(EnrollmentEndpointInterface.class);


            Call<List<Enrollment>> call = null;

            if (ticketType.equals("up-coming")) {
                call = apiService.getUpcomingTicket(token, num);
            } else if (ticketType.equals("passing")) {
                call = apiService.getPassingTicket(token, num);
            }

            call.enqueue(new Callback<List<Enrollment>>() {
                @Override
                public void onResponse(Call<List<Enrollment>> call, Response<List<Enrollment>> response) {
                    if (response.body() != null) {
                        listener.onSuccess(response.body());
                    } else {
                    }

                }

                @Override
                public void onFailure(Call<List<Enrollment>> call, Throwable t) {
                    listener.onFailure();
                }
            });
        }

    }

}

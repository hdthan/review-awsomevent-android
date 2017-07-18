package vn.axonactive.aevent.activity;

import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.drawable.Drawable;
import android.net.Uri;
import android.os.Bundle;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.TabLayout;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentPagerAdapter;
import android.support.v4.content.res.ResourcesCompat;
import android.support.v4.view.ViewPager;
import android.support.v7.widget.Toolbar;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Toast;

import com.facebook.CallbackManager;
import com.facebook.share.model.ShareLinkContent;
import com.facebook.share.widget.ShareDialog;
import com.getkeepsafe.taptargetview.TapTarget;
import com.getkeepsafe.taptargetview.TapTargetSequence;

import java.io.IOException;
import java.net.HttpURLConnection;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.List;

import jonathanfinerty.once.Once;
import okhttp3.ResponseBody;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;
import retrofit2.Retrofit;
import retrofit2.converter.scalars.ScalarsConverterFactory;
import vn.axonactive.aevent.BuildConfig;
import vn.axonactive.aevent.R;
import vn.axonactive.aevent.api.EnrollmentEndpointInterface;
import vn.axonactive.aevent.api.EventEndpointInterface;
import vn.axonactive.aevent.api.RestfulAPI;
import vn.axonactive.aevent.fragment.DescriptionFragment;
import vn.axonactive.aevent.fragment.GMapFragment;
import vn.axonactive.aevent.fragment.SpeakerFragment;
import vn.axonactive.aevent.fragment.SponsorFragment;
import vn.axonactive.aevent.fragment.TimeLineFragment;
import vn.axonactive.aevent.model.Event;
import vn.axonactive.aevent.util.DataStorage;


public class EventDetailActivity extends BaseActivity {

    private TabLayout tabLayout;
    private ViewPager viewPager;
    private FloatingActionButton mAction;
    private int type = -1;
    private String token = null;
    private RestfulAPI restfulAPI;

    private CallbackManager callbackManager;
    private ShareDialog shareDialog;

    private String showEventDetailTour = "showEventDetailTour";
    private String showJoinEvent = "showJoinEvent";

    private int topicId = -1;

    @Override
    protected void onResume() {
        super.onResume();
        checkType();
    }

    private void checkType() {

        SharedPreferences prefs = getSharedPreferences(DataStorage.APP_PREFS, MODE_PRIVATE);
        token = prefs.getString(DataStorage.TOKEN, null);

        if (token != null) {

            Retrofit retrofit = restfulAPI.getRestClient();

            EventEndpointInterface apiService =
                    retrofit.create(EventEndpointInterface.class);

            Call<ResponseBody> call = apiService.checkJoin(token, DataStorage.id);

            call.enqueue(new Callback<ResponseBody>() {
                @Override
                public void onResponse(Call<ResponseBody> call, Response<ResponseBody> response) {
                    if (response != null) {
                        if (response.code() == HttpURLConnection.HTTP_OK) {
                            try {
                                type = Integer.parseInt(response.body().string());
                                if (type == 0) {
                                    mAction.setVisibility(View.VISIBLE);
                                    mAction.setImageResource(R.drawable.ic_add);
                                    boolean done = Once.beenDone(Once.THIS_APP_INSTALL, showJoinEvent);               //If this is the first install then run onboarding
                                    if (!done) {
                                        initOnBoardingTutorialJoin();
                                        Once.markDone(showJoinEvent);
                                    }
                                } else if (type == 2) {
                                    mAction.setVisibility(View.VISIBLE);
                                    mAction.setImageResource(R.drawable.ic_ticket);
                                    boolean beenDone = Once.beenDone(Once.THIS_APP_INSTALL, showEventDetailTour);               //If this is the first install then run onboarding
                                    if (!beenDone) {
                                        initOnBoardingTutorial();
                                        Once.markDone(showEventDetailTour);
                                    }
                                }
                            } catch (IOException | NumberFormatException e) {
                                e.printStackTrace();
                            }
                        }
                    }
                }

                @Override
                public void onFailure(Call<ResponseBody> call, Throwable t) {

                }
            });

        }

    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        topicId = getIntent().getIntExtra("topicId", -1);

        facebookSDKInitialize();
        setContentView(R.layout.activity_event_detail_main);

        final Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);

        restfulAPI = new RestfulAPI(ScalarsConverterFactory.create());

        viewPager = (ViewPager) findViewById(R.id.viewpager);

        tabLayout = (TabLayout) findViewById(R.id.tabs);
        tabLayout.setVisibility(View.GONE);

        mAction = (FloatingActionButton) this.findViewById(R.id.btnAction);

        getEventDetail();
        checkType();

        shareDialog = new ShareDialog(this);

        mAction.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                if (type == 0) {
                    Intent intent = new Intent(EventDetailActivity.this, JoinEventActivity.class);
                    startActivity(intent);
                } else if (type == 2) {

                    if (token != null) {
                        showLoadingDialog();

                        Retrofit retrofit = restfulAPI.getRestClient();

                        EnrollmentEndpointInterface apiService =
                                retrofit.create(EnrollmentEndpointInterface.class);

                        Call<ResponseBody> call = apiService.getQRCode(token, DataStorage.currentEvent.getId());

                        call.enqueue(new Callback<ResponseBody>() {
                            @Override
                            public void onResponse(Call<ResponseBody> call, Response<ResponseBody> response) {
                                hideLoadingDialog();
                                if (response != null) {
                                    try {
                                        Intent intent = new Intent(EventDetailActivity.this, TicketActivity.class);
                                        intent.putExtra("url", response.body().string());
                                        intent.putExtra("title", DataStorage.currentEvent.getTitle());
                                        startActivity(intent);
                                    } catch (IOException e) {
                                        e.printStackTrace();
                                    }
                                }
                            }

                            @Override
                            public void onFailure(Call<ResponseBody> call, Throwable t) {
                                hideLoadingDialog();
                            }
                        });

                    }
                }
            }
        });
    }


    private void initOnBoardingTutorial() {                                                         //TODO DAN: Refactor

        Drawable ticket = ResourcesCompat.getDrawable(getResources(), R.drawable.ic_ticket, null);

        TapTargetSequence sequence = new TapTargetSequence(this)                                    //initializes and starts the onboarding process
                .targets(
                        TapTarget.forView(this.findViewById(R.id.btnAction),
                                "Show ticket",
                                "Tap here to display ticket and check-in")
                                .icon(ticket)
                );

        sequence.start();
    }

    private void initOnBoardingTutorialJoin() {

        Drawable join = ResourcesCompat.getDrawable(getResources(), R.drawable.ic_add, null);

        TapTargetSequence sequence = new TapTargetSequence(this)                                    //initializes and starts the onboarding process
                .targets(
                        TapTarget.forView(this.findViewById(R.id.btnAction),
                                "Join event",
                                "Tap here to join the event")
                                .icon(join)
                );
        sequence.start();
    }


    // Initialize the facebook sdk and then callback manager will handle the login responses.
    protected void facebookSDKInitialize() {
        callbackManager = CallbackManager.Factory.create();
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {

        getMenuInflater().inflate(R.menu.menu_event_detail, menu);
        return true;

    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {

        int itemId = item.getItemId();

        switch (itemId) {
            case R.id.action_share: {

                if (ShareDialog.canShow(ShareLinkContent.class)) {

                    Event event = DataStorage.currentEvent;

                    String link = BuildConfig.API_URL + "/event/" + event.getId();

                    String linkImage = BuildConfig.API_URL + event.getImageCover() + "/cover";

                    String location = event.getLocation();
                    SimpleDateFormat dateFormat = new SimpleDateFormat("dd/MM/yyyy");
                    String date = dateFormat.format(event.getStartDate());

                    String description = String.format("Location: %s. Date: %s", location, date);

                    ShareLinkContent linkContent = new ShareLinkContent.Builder()
                            .setContentTitle(event.getTitle())
                            .setImageUrl(Uri.parse(linkImage))
                            .setContentDescription(description)
                            .setContentUrl(Uri.parse(link))
                            .build();

                    shareDialog.show(linkContent);
                }
                return super.onOptionsItemSelected(item);
            }
            default: {
                return super.onOptionsItemSelected(item);
            }
        }

    }

    @Override
    protected void onActivityResult(final int requestCode, final int resultCode, final Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        callbackManager.onActivityResult(requestCode, resultCode, data);
    }

    private void getEventDetail() {

        showLoadingDialog();


        RestfulAPI restfulAPI = new RestfulAPI();

        Retrofit retrofit = restfulAPI.getRestClient();

        EventEndpointInterface apiService =
                retrofit.create(EventEndpointInterface.class);
        Call<Event> call = apiService.getEvent(DataStorage.id);

        call.enqueue(new Callback<Event>() {
            @Override
            public void onResponse(Call<Event> call, Response<Event> response) {
                hideLoadingDialog();

                int statusCode = response.code();

                if (statusCode == HttpURLConnection.HTTP_OK) {
                    Event event = response.body();
                    DataStorage.currentEvent = event;
                    setTitle(event.getTitle());
                    setupViewPager(viewPager);
                    tabLayout.setupWithViewPager(viewPager);
                    tabLayout.setVisibility(View.VISIBLE);
                    if (topicId != -1) {
                        viewPager.setCurrentItem(2);
                    }
                } else {
                    Toast.makeText(EventDetailActivity.this, getString(R.string.err_unknown), Toast.LENGTH_SHORT).show();
                    finish();
                }
            }

            @Override
            public void onFailure(Call<Event> call, Throwable t) {
                hideLoadingDialog();
                Toast.makeText(EventDetailActivity.this, getString(R.string.err_network_connection), Toast.LENGTH_SHORT).show();
                finish();
            }
        });


    }

    private void setupViewPager(ViewPager viewPager) {
        ViewPagerAdapter adapter = new ViewPagerAdapter(getSupportFragmentManager());
        adapter.addFragment(DescriptionFragment.newInstance(), "INTRODUCTION");
        adapter.addFragment(GMapFragment.newInstance(), "MAP");

        TimeLineFragment fragment = TimeLineFragment.newInstance();
        Bundle args = fragment.getArguments();
        args.putInt("topicId", topicId);

        adapter.addFragment(fragment, "AGENDA");
        adapter.addFragment(SpeakerFragment.newInstance(), "SPEAKER");
        adapter.addFragment(SponsorFragment.newInstance(), "SPONSOR");
        viewPager.setAdapter(adapter);
    }

    class ViewPagerAdapter extends FragmentPagerAdapter {
        private final List<Fragment> mFragmentList = new ArrayList<>();
        private final List<String> mFragmentTitleList = new ArrayList<>();

        ViewPagerAdapter(FragmentManager manager) {
            super(manager);
        }

        @Override
        public Fragment getItem(int position) {
            return mFragmentList.get(position);
        }

        @Override
        public int getCount() {
            return mFragmentList.size();
        }

        void addFragment(Fragment fragment, String title) {
            mFragmentList.add(fragment);
            mFragmentTitleList.add(title);
        }

        @Override
        public CharSequence getPageTitle(int position) {
            return mFragmentTitleList.get(position);
        }
    }

}
package vn.axonactive.aevent.activity;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.SharedPreferences;
import android.content.res.Configuration;
import android.os.Bundle;
import android.support.design.widget.NavigationView;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentTransaction;
import android.support.v4.view.GravityCompat;
import android.support.v4.view.MenuItemCompat;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.ActionBarDrawerToggle;
import android.support.v7.widget.Toolbar;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;

import com.facebook.login.LoginManager;
import com.getkeepsafe.taptargetview.TapTarget;
import com.getkeepsafe.taptargetview.TapTargetSequence;
import com.google.android.gms.auth.api.Auth;
import com.google.android.gms.auth.api.signin.GoogleSignInOptions;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.common.api.ResultCallback;
import com.google.android.gms.common.api.Status;
import com.google.firebase.iid.FirebaseInstanceId;
import com.google.firebase.messaging.FirebaseMessaging;
import com.squareup.picasso.Picasso;

import net.hockeyapp.android.UpdateManager;

import java.util.List;

import jonathanfinerty.once.Once;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;
import retrofit2.Retrofit;
import vn.axonactive.aevent.BuildConfig;
import vn.axonactive.aevent.R;
import vn.axonactive.aevent.api.EventEndpointInterface;
import vn.axonactive.aevent.api.RestfulAPI;
import vn.axonactive.aevent.common.AccountType;
import vn.axonactive.aevent.fragment.AboutUsFragment;
import vn.axonactive.aevent.fragment.FeedbackFragment;
import vn.axonactive.aevent.fragment.ListEventFragment;
import vn.axonactive.aevent.fragment.MyTicketFragment;
import vn.axonactive.aevent.fragment.NotificationCenterFragment;
import vn.axonactive.aevent.fragment.ProfileFragment;
import vn.axonactive.aevent.model.NotificationModel;
import vn.axonactive.aevent.service.FireBaseIDTask;
import vn.axonactive.aevent.service.MyFirebaseMessagingService;
import vn.axonactive.aevent.sqlite.EnrollmentDataSource;
import vn.axonactive.aevent.sqlite.NotificationDataSource;
import vn.axonactive.aevent.util.BadgeUtil;
import vn.axonactive.aevent.util.DataStorage;

public class MainActivity extends BaseActivity
        implements NavigationView.OnNavigationItemSelectedListener {


    private TextView mTextViewFullname;
    private TextView mTextViewAccountCode;
    private ImageView mImageViewAvatar;

    private TextView mTxtNotiCount;

    private NavigationView mNavigationView;

    private boolean notificationSelected = false;
    private OnPostDataRealTimeListener listener;

    private GoogleApiClient mGoogleApiClient;
    Toolbar toolbar;

    private String showAppTour = "showAppTour";

    private BroadcastReceiver br = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            if (mTxtNotiCount != null) {
                Bundle bundle = intent.getExtras();
                int count = bundle.getInt("num", 0);
                NotificationModel notification = bundle.getParcelable("data");

                if (getSupportFragmentManager().findFragmentById(R.id.content_main).getClass().getName().equals(NotificationCenterFragment.class.getName())) {
                    listener.onDataChange(notification);
                }
                setBadgeNotification(count);
            }
        }
    };

    public void addOnPostDataRealTimeListener(OnPostDataRealTimeListener listener) {
        this.listener = listener;
    }

    public interface OnPostDataRealTimeListener {
        void onDataChange(NotificationModel notification);
    }

    @Override
    protected void onResume() {
        super.onResume();
        registerReceiver(br, new IntentFilter("vn.axonactive.aevent.broadcast.notification"));
    }

    @Override
    protected void onPause() {
        super.onPause();
        unregisterReceiver(br);
        unregisterManagers();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        unregisterManagers();
    }

    private void checkForUpdates() {
        // Remove this for store builds!
        UpdateManager.register(this);
    }

    private void unregisterManagers() {
        UpdateManager.unregister();
    }

    private void initFirebase() {
        String token = FirebaseInstanceId.getInstance().getToken();
        new FireBaseIDTask().execute(token);
    }


    private void initOnBoardingTutorial(Menu menu) {

        TapTargetSequence sequence = new TapTargetSequence(this)
                .targets(
                        TapTarget.forView(menu.findItem(R.id.action_noti).getActionView(), "Tap here to view notifications"))
                .listener(new TapTargetSequence.Listener() {
                    // This listener will tell us when interesting(tm) events happen in regards
                    // to the sequence
                    @Override
                    public void onSequenceFinish() {
                        // Yay
                    }

                    @Override
                    public void onSequenceStep(TapTarget lastTarget) {
      
                    }

                    @Override
                    public void onSequenceCanceled(TapTarget lastTarget) {
                        // Boo
                    }
                });

        sequence.start();
    }


    @Override
    protected void onCreate(Bundle savedInstanceState) {

        Once.initialise(this);                                                                      //leave him here

        super.onCreate(savedInstanceState);

        checkForUpdates();

        notificationSelected = getIntent().getBooleanExtra("notificationSelected", false);

        initFirebase();

        setContentView(R.layout.activity_main);

        toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        DrawerLayout drawer = (DrawerLayout) findViewById(R.id.drawer_layout);
        ActionBarDrawerToggle toggle = new ActionBarDrawerToggle(
                this, drawer, toolbar, R.string.navigation_drawer_open, R.string.navigation_drawer_close);
        drawer.addDrawerListener(toggle);
        toggle.syncState();


        mNavigationView = (NavigationView) findViewById(R.id.nav_view);
        mNavigationView.setNavigationItemSelectedListener(this);

        getSupportFragmentManager().addOnBackStackChangedListener(new FragmentManager.OnBackStackChangedListener() {

            @Override
            public void onBackStackChanged() {
                Fragment fragment = getSupportFragmentManager().findFragmentById(R.id.content_main);
                if (fragment != null) {
                    updateTitleAndDrawer(fragment);
                }

            }
        });

        if (notificationSelected) {
            replaceFragment(NotificationCenterFragment.newInstance());
        } else {
            replaceFragment(ListEventFragment.newInstance());
        }

        View headerView = mNavigationView.getHeaderView(0);

        mTextViewFullname = (TextView) headerView.findViewById(R.id.text_view_full_name);
        mTextViewAccountCode = (TextView) headerView.findViewById(R.id.text_view_account_code);
        mImageViewAvatar = (ImageView) headerView.findViewById(R.id.image_view_avatar);

        SharedPreferences prefs = this.getSharedPreferences(DataStorage.APP_PREFS, Context.MODE_PRIVATE);

        String fullName = prefs.getString(DataStorage.FULL_NAME, null);
        String urlAvatar = prefs.getString(DataStorage.URL_AVATAR, null);
        String accountCode = prefs.getString(DataStorage.ACCOUNT_CODE, null);
        String accountType = prefs.getString(DataStorage.ACCOUNT_TYPE, null);
        DataStorage.email = prefs.getString(DataStorage.EMAIL, null);

        if (fullName != null) {
            mTextViewFullname.setText(fullName);
        }

        if (accountCode != null) {
            mTextViewAccountCode.setText(accountCode);

            if (urlAvatar != null) {

                if ("system".equals(accountType)) {
                    urlAvatar = BuildConfig.API_URL + urlAvatar;
                }

                if (!"".equals(urlAvatar)) {

                    Picasso.with(this)
                            .load(urlAvatar)
                            .placeholder(R.mipmap.avt_small_default)
                            .into(mImageViewAvatar);
                }
            }
        }


    }

    public void setBadgeNotification(int count) {

        if (mTxtNotiCount != null) {
            if (count > 0) {
                mTxtNotiCount.setText(MyFirebaseMessagingService.getNumberOfNotification() + "");
                mTxtNotiCount.setVisibility(View.VISIBLE);
            } else {
                mTxtNotiCount.setVisibility(View.GONE);
            }
        }

    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {

        getMenuInflater().inflate(R.menu.menu_tool_bar, menu);
        MenuItem item = menu.findItem(R.id.action_noti);
        MenuItemCompat.setActionView(item, R.layout.feed_update_count);
        View view = MenuItemCompat.getActionView(item);

        mTxtNotiCount = (TextView) view.findViewById(R.id.txt_noti_count);

        int count = MyFirebaseMessagingService.getNumberOfNotification();

        setBadgeNotification(count);

        boolean beenDone = Once.beenDone(Once.THIS_APP_INSTALL, showAppTour);
        if (!beenDone) {
            initOnBoardingTutorial(menu);
            Once.markDone(showAppTour);
        }

        view.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Fragment fragment = NotificationCenterFragment.newInstance();
                replaceFragment(fragment);
            }
        });

        return true;

    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {

        int itemId = item.getItemId();

        if (itemId == R.id.action_noti) {
            Fragment fragment = NotificationCenterFragment.newInstance();
            replaceFragment(fragment);
        }
        return true;
    }

    public void setUserCorner(String email, String fullName, String urlAvatar, String accountCode, String accountType) {

        SharedPreferences.Editor editor = getSharedPreferences(DataStorage.APP_PREFS, MODE_PRIVATE).edit();

        editor.putString(DataStorage.FULL_NAME, fullName);
        editor.putString(DataStorage.ACCOUNT_CODE, accountCode);
        editor.putString(DataStorage.URL_AVATAR, urlAvatar);
        editor.putString(DataStorage.ACCOUNT_TYPE, accountType);
        editor.putString(DataStorage.EMAIL, email);

        editor.apply();

        mTextViewFullname.setText(fullName);

        mTextViewAccountCode.setText(accountCode);


        if (AccountType.SYSTEM_ACCOUNT.equals(accountType)) {
            urlAvatar = BuildConfig.API_URL + urlAvatar;
        }

        if (!"".equals(urlAvatar)) {
            Picasso.with(this)
                    .load(urlAvatar)
                    .placeholder(R.mipmap.avt_small_default)
                    .into(mImageViewAvatar);
        }

    }

    @Override
    public void onConfigurationChanged(Configuration newConfig) {
        super.onConfigurationChanged(newConfig);
    }

    @Override
    public void onBackPressed() {
        DrawerLayout drawer = (DrawerLayout) findViewById(R.id.drawer_layout);
        if (drawer.isDrawerOpen(GravityCompat.START)) {
            drawer.closeDrawer(GravityCompat.START);
        } else {
            if (getSupportFragmentManager().getBackStackEntryCount() == 1) {
                finish();
            } else {
                super.onBackPressed();
            }
        }
    }

    @SuppressWarnings("StatementWithEmptyBody")
    @Override
    public boolean onNavigationItemSelected(MenuItem item) {

        int id = item.getItemId();

        Fragment fragment = null;

        if (id == R.id.nav_home) {
            fragment = ListEventFragment.newInstance();
        } else if (id == R.id.nav_profile) {
            fragment = ProfileFragment.newInstance();
        } else if (id == R.id.nav_my_ticket) {
            fragment = MyTicketFragment.newInstance();
        } else if (id == R.id.nav_about) {
            fragment = AboutUsFragment.newInstance();
        } else if (id == R.id.nav_noti) {
            MyFirebaseMessagingService.clearNumberOfNotification();
            BadgeUtil.setBadge(MainActivity.this, 0);
            setBadgeNotification(0);
            fragment = NotificationCenterFragment.newInstance();
        } else if (id == R.id.nav_feed_back) {
            fragment = FeedbackFragment.newInstance();
        } else if (id == R.id.nav_logout) {
            logout();
        }

        replaceFragment(fragment);

        DrawerLayout drawer = (DrawerLayout) findViewById(R.id.drawer_layout);
        drawer.closeDrawer(GravityCompat.START);
        return true;
    }

    @Override
    protected void onStart() {
        super.onStart();

        SharedPreferences prefs = this.getSharedPreferences(DataStorage.APP_PREFS, Context.MODE_PRIVATE);
        String accountType = prefs.getString(DataStorage.ACCOUNT_TYPE, null);
        if (AccountType.GOOGLE_ACCOUNT.equals(accountType)) {
            GoogleSignInOptions gso = new GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
                    .requestEmail()
                    .build();
            mGoogleApiClient = new GoogleApiClient.Builder(this)
                    .addApi(Auth.GOOGLE_SIGN_IN_API, gso)
                    .build();
            mGoogleApiClient.connect();
        }
    }

    private void logout() {

        // clear all data in sqlite
        NotificationDataSource dataSource = new NotificationDataSource(this);
        dataSource.open();
        dataSource.clearNotification();
        dataSource.close();

        EnrollmentDataSource enrollmentDataSource = new EnrollmentDataSource(this);
        enrollmentDataSource.open();
        enrollmentDataSource.clearEnrollment();
        enrollmentDataSource.close();

        SharedPreferences prefs = this.getSharedPreferences(DataStorage.APP_PREFS, Context.MODE_PRIVATE);
        String token = prefs.getString(DataStorage.TOKEN, null);

        SharedPreferences.Editor editor = getSharedPreferences(DataStorage.APP_PREFS, MODE_PRIVATE).edit();
        editor.putString(DataStorage.TOKEN, null);

        editor.apply();

        String accountType = prefs.getString(DataStorage.ACCOUNT_TYPE, null);

        if (AccountType.FACEBOOK_ACCOUNT.equals(accountType)) {
            LoginManager.getInstance().logOut();
        } else if (AccountType.GOOGLE_ACCOUNT.equals(accountType)) {

            Auth.GoogleSignInApi.signOut(mGoogleApiClient).setResultCallback(
                    new ResultCallback<Status>() {
                        @Override
                        public void onResult(Status status) {
                            //logout
                        }
                    });

        }

        RestfulAPI restfulAPI = new RestfulAPI();

        Retrofit retrofit = restfulAPI.getRestClient();

        EventEndpointInterface apiService = retrofit.create(EventEndpointInterface.class);

        Call<List<Long>> call = apiService.getListOfEventId(token);

        call.enqueue(new Callback<List<Long>>() {
            @Override
            public void onResponse(Call<List<Long>> call, final Response<List<Long>> response) {
                if (response != null) {
                    if (response.code() == 200) {

                        List<Long> ids = response.body();

                        for (Long id : ids) {
                            FirebaseMessaging.getInstance().unsubscribeFromTopic("e" + id);
                        }
                    }

                    Intent intent = new Intent(MainActivity.this, SplashActivity.class);
                    intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_NEW_TASK);
                    startActivity(intent);
                    finish();
                }
            }

            @Override
            public void onFailure(Call<List<Long>> call, Throwable t) {
                Intent intent = new Intent(MainActivity.this, SplashActivity.class);
                intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_NEW_TASK);
                startActivity(intent);
                finish();
            }
        });
    }

    private void replaceFragment(Fragment fragment) {

        if (fragment != null) {

            String backStateName = fragment.getClass().getName();
            String fragmentTag = backStateName;

            FragmentManager manager = getSupportFragmentManager();
            boolean fragmentPopped = manager.popBackStackImmediate(backStateName, 0);

            if (!fragmentPopped && manager.findFragmentByTag(fragmentTag) == null) { //fragment not in back stack, create it.
                FragmentTransaction ft = manager.beginTransaction();
                ft.replace(R.id.content_main, fragment, fragmentTag);
                ft.setTransition(FragmentTransaction.TRANSIT_FRAGMENT_FADE);
                ft.addToBackStack(backStateName);
                ft.commit();
                invalidateOptionsMenu();
            }
        }
    }

    private void updateTitleAndDrawer(Fragment fragment) {
        String fragClassName = fragment.getClass().getName();

        if (fragClassName.equals(ListEventFragment.class.getName())) {
            setTitle("Home");
            mNavigationView.setCheckedItem(R.id.nav_home);
        } else if (fragClassName.equals(ProfileFragment.class.getName())) {
            setTitle("Profile");
            mNavigationView.setCheckedItem(R.id.nav_profile);
        } else if (fragClassName.equals(AboutUsFragment.class.getName())) {
            setTitle("About Us");
            mNavigationView.setCheckedItem(R.id.nav_about);
        } else if (fragClassName.equals(NotificationCenterFragment.class.getName())) {
            setTitle("Notifications");
            mNavigationView.setCheckedItem(R.id.nav_noti);
        } else if (fragClassName.equals(MyTicketFragment.class.getName())) {
            setTitle("My Tickets");
            mNavigationView.setCheckedItem(R.id.nav_my_ticket);
        } else if (fragClassName.equals(FeedbackFragment.class.getName())) {
            setTitle("Feedback");
            mNavigationView.setCheckedItem(R.id.nav_feed_back);
        }
    }

}

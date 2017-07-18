package vn.axonactive.aevent_organizer.activity;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.design.widget.NavigationView;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentTransaction;
import android.support.v4.view.GravityCompat;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.ActionBarDrawerToggle;
import android.support.v7.widget.Toolbar;
import android.view.MenuItem;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;

import com.facebook.login.LoginManager;
import com.google.android.gms.auth.api.Auth;
import com.google.android.gms.auth.api.signin.GoogleSignInOptions;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.common.api.ResultCallback;
import com.google.android.gms.common.api.Status;
import com.squareup.picasso.Picasso;

import net.hockeyapp.android.UpdateManager;

import vn.axonactive.aevent_organizer.BuildConfig;
import vn.axonactive.aevent_organizer.R;
import vn.axonactive.aevent_organizer.common.AccountType;
import vn.axonactive.aevent_organizer.fragment.AboutUsFragment;
import vn.axonactive.aevent_organizer.fragment.FeedbackFragment;
import vn.axonactive.aevent_organizer.fragment.MyEventFragment;
import vn.axonactive.aevent_organizer.fragment.ProfileFragment;
import vn.axonactive.aevent_organizer.util.DataStorage;

public class MainActivity extends BaseActivity implements NavigationView.OnNavigationItemSelectedListener {

    private TextView mTextViewFullname;
    private TextView mTextViewAccountCode;
    private ImageView mImageViewAvatar;

    private Toolbar mToolbar;

    private DrawerLayout mDrawerLayout;

    private NavigationView mNavigationView;

    private GoogleApiClient mGoogleApiClient;

    private void initView() {

        setSupportActionBar(mToolbar);

        ActionBarDrawerToggle mToggle = new ActionBarDrawerToggle(
                this, mDrawerLayout, mToolbar, R.string.navigation_drawer_open, R.string.navigation_drawer_close);
        mDrawerLayout.addDrawerListener(mToggle);
        mToggle.syncState();

        mNavigationView.setNavigationItemSelectedListener(this);

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

        replaceFragment(MyEventFragment.newInstance());

        View headerView = mNavigationView.getHeaderView(0);

        mTextViewFullname = (TextView) headerView.findViewById(R.id.text_view_full_name);
        mTextViewAccountCode = (TextView) headerView.findViewById(R.id.text_view_account_code);
        mImageViewAvatar = (ImageView) headerView.findViewById(R.id.image_view_avatar);


        SharedPreferences prefs = this.getSharedPreferences(DataStorage.APP_PREFS, Context.MODE_PRIVATE);
        String fullName = prefs.getString(DataStorage.FULL_NAME, null);
        String urlAvatar = prefs.getString(DataStorage.URL_AVATAR, null);
        String accountCode = prefs.getString(DataStorage.ACCOUNT_CODE, null);
        String accountType = prefs.getString(DataStorage.ACCOUNT_TYPE, null);

        if (fullName != null) {
            mTextViewFullname.setText(fullName);
        }

        if (accountCode != null) {
            mTextViewAccountCode.setText(accountCode);

            if (urlAvatar != null) {

                if (AccountType.SYSTEM_ACCOUNT.equals(accountType)) {
                    urlAvatar = BuildConfig.API_URL + urlAvatar;
                }

                if (!"".equals(urlAvatar)) {
                    Picasso.with(MainActivity.this).load(urlAvatar).into(mImageViewAvatar, new com.squareup.picasso.Callback() {
                        @Override
                        public void onSuccess() {

                        }

                        @Override
                        public void onError() {
                            mImageViewAvatar.setImageResource(R.mipmap.avt_small_default);
                        }
                    });
                }
            }
        }


    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {

        super.onCreate(savedInstanceState);

        checkForUpdates();


        setContentView(R.layout.activity_main);

        mToolbar = (Toolbar) this.findViewById(R.id.toolbar);
        mDrawerLayout = (DrawerLayout) this.findViewById(R.id.drawer_layout);
        mNavigationView = (NavigationView) this.findViewById(R.id.nav_view);

        initView();

    }

    @Override
    protected void onPause() {
        super.onPause();
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

    public void setUserCorner(String fullName, String urlAvatar, String accountCode, String accountType) {

        if (fullName != null) {
            mTextViewFullname.setText(fullName);
        }

        if (accountCode != null) {
            mTextViewAccountCode.setText(accountCode);

            if (urlAvatar != null) {

                if (AccountType.SYSTEM_ACCOUNT.equals(accountType)) {
                    urlAvatar = BuildConfig.API_URL + urlAvatar;
                }

                if (!"".equals(urlAvatar)) {
                    Picasso.with(MainActivity.this).load(urlAvatar).into(mImageViewAvatar);
                }
            }
        }

        SharedPreferences.Editor editor = getSharedPreferences(DataStorage.APP_PREFS, MODE_PRIVATE).edit();

        editor.putString(DataStorage.FULL_NAME, fullName);
        editor.putString(DataStorage.ACCOUNT_CODE, accountCode);
        editor.putString(DataStorage.URL_AVATAR, urlAvatar);

        editor.apply();

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
    public boolean onNavigationItemSelected(@NonNull MenuItem item) {

        int id = item.getItemId();

        replaceFragment(id);

        mDrawerLayout.closeDrawer(GravityCompat.START);
        return true;
    }

    private void replaceFragment(int id) {

        Fragment fragment = null;

        if (id == R.id.nav_home) {
            fragment = MyEventFragment.newInstance();
        } else if (id == R.id.nav_profile) {
            fragment = ProfileFragment.newInstance();
        } else if (id == R.id.nav_about) {
            fragment = AboutUsFragment.newInstance();
        } else if (id == R.id.nav_feed_back) {
            fragment = FeedbackFragment.newInstance();
        } else if (id == R.id.nav_logout) {
            logout();
        }

        replaceFragment(fragment);

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

        SharedPreferences prefs = this.getSharedPreferences(DataStorage.APP_PREFS, Context.MODE_PRIVATE);
        String accountType = prefs.getString(DataStorage.ACCOUNT_TYPE, null);

        SharedPreferences.Editor editor = getSharedPreferences(DataStorage.APP_PREFS, MODE_PRIVATE).edit();
        editor.putString(DataStorage.TOKEN, null);
        editor.apply();

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
        Intent intent = new Intent(MainActivity.this, SplashActivity.class);
        intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_NEW_TASK);
        startActivity(intent);
        finish();
    }

    private void replaceFragment(Fragment fragment) {

        if (fragment != null) {

            String backStateName = fragment.getClass().getName();

            FragmentManager manager = getSupportFragmentManager();
            boolean fragmentPopped = manager.popBackStackImmediate(backStateName, 0);

            if (!fragmentPopped && manager.findFragmentByTag(backStateName) == null) { //fragment not in back stack, create it.
                FragmentTransaction ft = manager.beginTransaction();
                ft.replace(R.id.content_main, fragment, backStateName);
                ft.setTransition(FragmentTransaction.TRANSIT_FRAGMENT_FADE);
                ft.addToBackStack(backStateName);
                ft.commit();
            }
        }
    }

    private void updateTitleAndDrawer(Fragment fragment) {
        String fragClassName = fragment.getClass().getName();

        if (fragClassName.equals(MyEventFragment.class.getName())) {
            setTitle("Home");
            mNavigationView.setCheckedItem(R.id.nav_home);
        } else if (fragClassName.equals(ProfileFragment.class.getName())) {
            setTitle("Profile");
            mNavigationView.setCheckedItem(R.id.nav_profile);
        } else if (fragClassName.equals(AboutUsFragment.class.getName())) {
            setTitle("About Us");
            mNavigationView.setCheckedItem(R.id.nav_about);
        } else if (fragClassName.equals(FeedbackFragment.class.getName())) {
            setTitle("Feedback");
            mNavigationView.setCheckedItem(R.id.nav_feed_back);
        }
    }

}

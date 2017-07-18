package vn.axonactive.aevent.activity;

import android.app.Fragment;
import android.app.FragmentTransaction;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.view.View;
import android.widget.Button;
import android.widget.Toast;

import com.facebook.CallbackManager;
import com.facebook.FacebookCallback;
import com.facebook.FacebookException;
import com.facebook.login.LoginResult;
import com.facebook.login.widget.LoginButton;
import com.google.android.gms.auth.api.Auth;
import com.google.android.gms.auth.api.signin.GoogleSignInAccount;
import com.google.android.gms.auth.api.signin.GoogleSignInOptions;
import com.google.android.gms.auth.api.signin.GoogleSignInResult;
import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.SignInButton;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.firebase.messaging.FirebaseMessaging;
import com.squareup.otto.Subscribe;

import java.net.HttpURLConnection;
import java.util.List;

import es.dmoral.toasty.Toasty;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;
import retrofit2.Retrofit;
import vn.axonactive.aevent.R;
import vn.axonactive.aevent.api.EventEndpointInterface;
import vn.axonactive.aevent.api.RestfulAPI;
import vn.axonactive.aevent.api.UserEndPointInterface;
import vn.axonactive.aevent.common.AccountType;
import vn.axonactive.aevent.common.Constant;
import vn.axonactive.aevent.fragment.LoginFragment;
import vn.axonactive.aevent.fragment.SignUpFragment;
import vn.axonactive.aevent.model.AccessToken;
import vn.axonactive.aevent.model.Token;
import vn.axonactive.aevent.model.User;
import vn.axonactive.aevent.util.DataStorage;
import vn.axonactive.aevent.util.GlobalBus;

public class BoardingActivity extends BaseActivity implements View.OnClickListener, GoogleApiClient.OnConnectionFailedListener {

    public static final int RC_SIGN_IN = 517;
    private LoginButton facebookLoginButton;
    private SignInButton googleLoginButton;
    private CallbackManager facebookCallbackManager;
    private GoogleApiClient mGoogleApiClient;

    private Button mFacebook;
    private Button mGoogle;
    private RestfulAPI restfulAPI;

    @Override
    protected void onDestroy() {
        super.onDestroy();
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        restfulAPI = new RestfulAPI();

        setContentView(R.layout.activity_onboarding);

        Fragment newFragment = LoginFragment.newInstance();
        FragmentTransaction transaction = getFragmentManager().beginTransaction();
        transaction.replace(R.id.on_boarding_fragment_container, newFragment);

        transaction.commit();

        mFacebook = (Button) this.findViewById(R.id.button_facebook);
        mGoogle = (Button) this.findViewById(R.id.button_google);

        mFacebook.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                facebookLoginButton.callOnClick();
            }
        });

        facebookCallbackManager = CallbackManager.Factory.create();
        facebookLoginButton = (LoginButton) findViewById(R.id.facebook_login_button);
        facebookLoginButton.setReadPermissions("email");

        facebookLoginButton.registerCallback(facebookCallbackManager, new FacebookCallback<LoginResult>() {
            @Override
            public void onSuccess(LoginResult loginResult) {
                if (com.facebook.AccessToken.getCurrentAccessToken() != null) {
                    showLoadingDialog();

                    Retrofit retrofit = restfulAPI.getRestClient();

                    UserEndPointInterface apiService =
                            retrofit.create(UserEndPointInterface.class);

                    Call<Token> call = apiService.loginFacebook(new AccessToken(com.facebook.AccessToken.getCurrentAccessToken().getToken()));
                    call.enqueue(new Callback<Token>() {
                        @Override
                        public void onResponse(Call<Token> call, Response<Token> response) {
                            hideLoadingDialog();
                            postOnBoarding(AccountType.FACEBOOK_ACCOUNT, response.body());
                        }

                        @Override
                        public void onFailure(Call<Token> call, Throwable t) {
                            hideLoadingDialog();
                            showToastInfo(getString(R.string.err_network_connection));
                        }
                    });
                }
            }

            @Override
            public void onCancel() {
            }

            @Override
            public void onError(FacebookException e) {
                Toast.makeText(BoardingActivity.this, getString(R.string.facebook_login_error), Toast.LENGTH_LONG).show();
            }
        });

        GoogleSignInOptions gso = new GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
                .requestIdToken(this.getResources().getString(R.string.server_client_id))
                .requestEmail().build();

        mGoogleApiClient = new GoogleApiClient.Builder(this)
                .enableAutoManage(this, this)
                .addApi(Auth.GOOGLE_SIGN_IN_API, gso)
                .build();

        googleLoginButton = (SignInButton) findViewById(R.id.google_sign_in_button);
        googleLoginButton.setOnClickListener(this);

        mGoogle.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                signInByGoogle();
            }
        });

    }

    @Override
    protected void onResume() {
        super.onResume();
    }

    private void signInByGoogle() {
        Intent signInIntent = Auth.GoogleSignInApi.getSignInIntent(mGoogleApiClient);
        startActivityForResult(signInIntent, BoardingActivity.RC_SIGN_IN);
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.google_sign_in_button:
                signInByGoogle();
                break;
        }
    }

    @Override
    public void onConnectionFailed(@NonNull ConnectionResult connectionResult) {
        Toast.makeText(this, getString(R.string.google_login_error), Toast.LENGTH_LONG).show();
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        facebookCallbackManager.onActivityResult(requestCode, resultCode, data);

        if (requestCode == RC_SIGN_IN) {
            GoogleSignInResult result = Auth.GoogleSignInApi.getSignInResultFromIntent(data);
            handleSignInByGoogleResult(result);
        }
    }

    private void handleSignInByGoogleResult(GoogleSignInResult result) {
        if (result.isSuccess()) {
            GoogleSignInAccount acct = result.getSignInAccount();
            String idToken = acct.getIdToken();

            if (idToken != null) {
                showLoadingDialog();
                Retrofit retrofit = restfulAPI.getRestClient();

                UserEndPointInterface apiService =
                        retrofit.create(UserEndPointInterface.class);

                Call<Token> call = apiService.loginGooglePlus(new AccessToken(idToken));
                call.enqueue(new Callback<Token>() {
                    @Override
                    public void onResponse(Call<Token> call, Response<Token> response) {
                        hideLoadingDialog();
                        postOnBoarding(AccountType.GOOGLE_ACCOUNT, response.body());
                    }

                    @Override
                    public void onFailure(Call<Token> call, Throwable t) {
                        hideLoadingDialog();
                        showToastInfo(getString(R.string.err_network_connection));
                    }
                });
            }

        }
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

    @Subscribe
    public void getTokenFromFragment(Token token) {
        postOnBoarding(AccountType.SYSTEM_ACCOUNT, token);
    }

    @Subscribe
    public void getActionFromFragment(String action) {

        switch (action) {
            case Constant.ACCOUNT_EXIST: {
                showToastError(getString(R.string.err_account_exist));
                break;
            }
            case Constant.ACCOUNT_NOT_EXIST: {
                showToastError(getString(R.string.err_account_not_correct));
                break;
            }

            case Constant.FAILURE_UNKNOWN: {
                showToastInfo(getString(R.string.err_unknown));
                break;
            }
            case Constant.FAILURE_NETWORK: {
                showToastInfo(getString(R.string.err_network_connection));
                break;
            }

            case Constant.LOGIN_CLICK: {
                Fragment newFragment = LoginFragment.newInstance();
                FragmentTransaction transaction = getFragmentManager().beginTransaction();
                transaction.replace(R.id.on_boarding_fragment_container, newFragment);
                transaction.addToBackStack(null);
                transaction.commit();
                break;
            }
            case Constant.SIGN_UP_CLICK: {
                Fragment newFragment = SignUpFragment.newInstance();
                FragmentTransaction transaction = getFragmentManager().beginTransaction();
                transaction.replace(R.id.on_boarding_fragment_container, newFragment);
                transaction.addToBackStack(null);
                transaction.commit();
                break;
            }
        }

    }

    private void showToastError(String msg) {
        Toasty.error(this, msg).show();
    }

    private void showToastInfo(String msg) {
        Toasty.normal(this, msg).show();
    }

    void postOnBoarding(final String accountType, Token token) {


        final SharedPreferences.Editor editor = getSharedPreferences(DataStorage.APP_PREFS, MODE_PRIVATE).edit();
        editor.putBoolean(DataStorage.FIRST_LAUNCH, false);
        editor.putString(DataStorage.TOKEN, token.getToken());

        RestfulAPI restfulAPI = new RestfulAPI();

        Retrofit retrofit = restfulAPI.getRestClient();

        UserEndPointInterface userApiService = retrofit.create(UserEndPointInterface.class);

        Call<User> call = userApiService.getUser(token.getToken());
        call.enqueue(new Callback<User>() {
            @Override
            public void onResponse(Call<User> call, final Response<User> response) {
                if (response != null) {

                    int statusCode = response.code();

                    if (statusCode == HttpURLConnection.HTTP_OK) {
                        User user = response.body();

                        editor.putString(DataStorage.FULL_NAME, user.getFullName());
                        editor.putString(DataStorage.ACCOUNT_CODE, user.getAccountCode());
                        editor.putString(DataStorage.URL_AVATAR, user.getAvatar());
                        editor.putString(DataStorage.ACCOUNT_TYPE, accountType);
                        editor.putString(DataStorage.EMAIL, user.getEmail());
                        DataStorage.email = user.getEmail();

                        editor.apply();

                        Intent intent = new Intent(BoardingActivity.this, MainActivity.class);
                        intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_NEW_TASK);
                        startActivity(intent);
                        finish();

                    } else {
                        Toast.makeText(BoardingActivity.this, getString(R.string.err_network_connection), Toast.LENGTH_SHORT).show();
                    }

                    hideLoadingDialog();
                }
            }

            @Override
            public void onFailure(Call<User> call, Throwable t) {
                Toast.makeText(BoardingActivity.this, getString(R.string.err_network_connection), Toast.LENGTH_SHORT).show();
                hideLoadingDialog();
            }
        });


        EventEndpointInterface apiService = retrofit.create(EventEndpointInterface.class);

        Call<List<Long>> callTopic = apiService.getListOfEventId(token.getToken());

        callTopic.enqueue(new Callback<List<Long>>() {
            @Override
            public void onResponse(Call<List<Long>> call, final Response<List<Long>> response) {
                if (response != null) {
                    if (response.code() == 200) {

                        List<Long> ids = response.body();

                        for (Long id : ids) {
                            FirebaseMessaging.getInstance().subscribeToTopic("e" + id);
                        }
                    }
                }
            }

            @Override
            public void onFailure(Call<List<Long>> call, Throwable t) {

            }
        });
    }
}

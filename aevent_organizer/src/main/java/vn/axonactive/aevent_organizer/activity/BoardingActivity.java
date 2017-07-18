package vn.axonactive.aevent_organizer.activity;

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

import java.net.HttpURLConnection;

import es.dmoral.toasty.Toasty;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;
import retrofit2.Retrofit;
import vn.axonactive.aevent_organizer.R;
import vn.axonactive.aevent_organizer.api.EventEndpointInterface;
import vn.axonactive.aevent_organizer.api.RestfulAPI;
import vn.axonactive.aevent_organizer.api.UserEndPointInterface;
import vn.axonactive.aevent_organizer.common.AccountType;
import vn.axonactive.aevent_organizer.fragment.LoginFragment;
import vn.axonactive.aevent_organizer.model.AccessToken;
import vn.axonactive.aevent_organizer.model.Token;
import vn.axonactive.aevent_organizer.model.User;
import vn.axonactive.aevent_organizer.util.DataStorage;


public class BoardingActivity extends BaseActivity implements View.OnClickListener, GoogleApiClient.OnConnectionFailedListener, LoginFragment.OnLoginClickListener {

    private static final int RC_SIGN_IN = 517;

    private LoginButton mLoginButtonFacebook;

    private SignInButton mSignInButtonGoogle;

    private Button mButtonFacebook;

    private Button mButtonGoogle;

    private CallbackManager mCallbackManager;
    private GoogleApiClient mGoogleApiClient;

    private RestfulAPI restfulAPI;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.activity_onboarding);

        mLoginButtonFacebook = (LoginButton) this.findViewById(R.id.login_button_facebook);
        mSignInButtonGoogle = (SignInButton) this.findViewById(R.id.sign_in_button_google);
        mButtonFacebook = (Button) this.findViewById(R.id.button_facebook);
        mButtonGoogle = (Button) this.findViewById(R.id.button_google);

        restfulAPI = new RestfulAPI();

        Fragment newFragment = LoginFragment.newInstance();
        FragmentTransaction transaction = getFragmentManager().beginTransaction();
        transaction.replace(R.id.on_boarding_fragment_container, newFragment);

        transaction.commit();

        mButtonFacebook.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                mLoginButtonFacebook.callOnClick();
            }
        });

        mCallbackManager = CallbackManager.Factory.create();

        mLoginButtonFacebook.registerCallback(mCallbackManager, new FacebookCallback<LoginResult>() {
            @Override
            public void onSuccess(LoginResult loginResult) {
                if (com.facebook.AccessToken.getCurrentAccessToken() != null) {
                    showLoadingDialog();

                    restfulAPI = new RestfulAPI();


                    Retrofit retrofit = restfulAPI.getRestClient();

                    EventEndpointInterface apiService =
                            retrofit.create(EventEndpointInterface.class);

                    Call<Token> call = apiService.loginFacebook(new AccessToken(com.facebook.AccessToken.getCurrentAccessToken().getToken()));
                    call.enqueue(new Callback<Token>() {
                        @Override
                        public void onResponse(Call<Token> call, Response<Token> response) {
                            hideLoadingDialog();

                            int statusCode = response.code();

                            Token token = response.body();

                            if (statusCode == HttpURLConnection.HTTP_OK) {
                                onLoginSuccess(AccountType.FACEBOOK_ACCOUNT, token);
                            } else if (statusCode == HttpURLConnection.HTTP_CLIENT_TIMEOUT) {
                                onLoginFailure();
                            } else {
                                onLoginFailureUnknown();
                            }
                        }

                        @Override
                        public void onFailure(Call<Token> call, Throwable t) {
                            hideLoadingDialog();
                            onLoginFailure();
                        }
                    });
                }

            }

            @Override
            public void onCancel() {
            }

            @Override
            public void onError(FacebookException e) {
                Toasty.info(BoardingActivity.this, getString(R.string.err_network_connection), Toast.LENGTH_LONG, true).show();
            }
        });

        GoogleSignInOptions gso = new GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
                .requestIdToken(this.getResources().getString(R.string.server_client_id))
                .requestEmail().build();


        mGoogleApiClient = new GoogleApiClient.Builder(this)
                .enableAutoManage(this, this)
                .addApi(Auth.GOOGLE_SIGN_IN_API, gso)
                .build();

        mSignInButtonGoogle.setOnClickListener(this);

        mButtonGoogle.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                signInByGoogle();
            }
        });

    }

    private void signInByGoogle() {
        Intent signInIntent = Auth.GoogleSignInApi.getSignInIntent(mGoogleApiClient);
        startActivityForResult(signInIntent, BoardingActivity.RC_SIGN_IN);
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.sign_in_button_google:
                signInByGoogle();
                break;
        }
    }

    @Override
    public void onConnectionFailed(@NonNull ConnectionResult connectionResult) {
        Toasty.info(this, getString(R.string.err_network_connection), Toast.LENGTH_SHORT, true).show();
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        mCallbackManager.onActivityResult(requestCode, resultCode, data);

        if (requestCode == RC_SIGN_IN) {
            GoogleSignInResult result = Auth.GoogleSignInApi.getSignInResultFromIntent(data);
            handleSignInByGoogleResult(result);
        }
    }

    private void handleSignInByGoogleResult(GoogleSignInResult result) {
        if (result.isSuccess()) {

            GoogleSignInAccount acct = result.getSignInAccount();

            String idToken = acct != null ? acct.getIdToken() : null;

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

                        int statusCode = response.code();

                        Token token = response.body();

                        if (statusCode == HttpURLConnection.HTTP_OK) {

                            if (token != null && token.getToken() != null) {
                                onLoginSuccess(AccountType.GOOGLE_ACCOUNT, token);
                            } else {
                                onLoginFailureAccountNotCorrect();
                            }

                        } else if (statusCode == HttpURLConnection.HTTP_CLIENT_TIMEOUT) {
                            onLoginFailure();
                        } else {
                            onLoginFailureUnknown();
                        }
                    }

                    @Override
                    public void onFailure(Call<Token> call, Throwable t) {
                        hideLoadingDialog();
                        onLoginFailure();
                    }
                });
            }

        }
    }

    @Override
    public void onLoginSuccess(String accountType, Token token) {
        postOnBoarding(accountType, token);
    }

    @Override
    public void onLoginFailureUnknown() {
        Toasty.info(this, getString(R.string.err_unknown), Toast.LENGTH_SHORT, true).show();
    }

    @Override
    public void onLoginFailureAccountNotCorrect() {
        Toasty.error(this, getString(R.string.err_account_not_correct), Toast.LENGTH_SHORT, true).show();
    }

    @Override
    public void onLoginFailure() {
        Toasty.info(this, getString(R.string.err_network_connection), Toast.LENGTH_SHORT, true).show();
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

                        editor.apply();

                        Intent intent = new Intent(BoardingActivity.this, MainActivity.class);
                        intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_NEW_TASK);
                        startActivity(intent);
                        finish();

                    } else {
                        Toasty.info(BoardingActivity.this, getString(R.string.err_network_connection), Toast.LENGTH_SHORT, true).show();
                    }

                    hideLoadingDialog();
                }
            }

            @Override
            public void onFailure(Call<User> call, Throwable t) {
                Toasty.info(BoardingActivity.this, getString(R.string.err_network_connection), Toast.LENGTH_SHORT, true).show();
                hideLoadingDialog();
            }
        });

    }
}

package vn.axonactive.aevent.fragment;


import android.app.Fragment;
import android.os.Build;
import android.os.Bundle;
import android.support.design.widget.TextInputLayout;
import android.text.Editable;
import android.text.Html;
import android.text.TextWatcher;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;

import java.net.HttpURLConnection;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;
import retrofit2.Retrofit;
import vn.axonactive.aevent.R;
import vn.axonactive.aevent.activity.BoardingActivity;
import vn.axonactive.aevent.api.RestfulAPI;
import vn.axonactive.aevent.api.UserEndPointInterface;
import vn.axonactive.aevent.common.Constant;
import vn.axonactive.aevent.model.Login;
import vn.axonactive.aevent.model.Token;
import vn.axonactive.aevent.util.GlobalBus;
import vn.axonactive.aevent.util.Validation;


/**
 * A simple {@link Fragment} subclass.
 */
public class LoginFragment extends Fragment {

    private EditText inputEmail;
    private EditText inputPassword;

    private TextInputLayout inputLayoutEmail, inputLayoutPassword;
    private BoardingActivity activity;

    public LoginFragment() {
        // Required empty public constructor
    }

    public static LoginFragment newInstance() {
        LoginFragment fragment = new LoginFragment();
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
    public View onCreateView(LayoutInflater inflater, final ViewGroup container,
                             Bundle savedInstanceState) {

        View view = inflater.inflate(R.layout.fragment_login, container, false);

        activity = (BoardingActivity) getActivity();

        TextView txtSignUp = (TextView) view.findViewById(R.id.txt_sign_up);
        txtSignUp.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                GlobalBus.getBus().post(Constant.SIGN_UP_CLICK);
            }
        });

        String text = "Need account? <font color=#ff0000>Tap here</font>";
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
            txtSignUp.setText(Html.fromHtml(text, Html.FROM_HTML_MODE_LEGACY));
        } else {
            txtSignUp.setText(Html.fromHtml(text));
        }


        inputEmail = (EditText) view.findViewById(R.id.input_email);
        inputPassword = (EditText) view.findViewById(R.id.input_password);

        inputLayoutEmail = (TextInputLayout) view.findViewById(R.id.input_layout_email);
        inputLayoutPassword = (TextInputLayout) view.findViewById(R.id.input_layout_password);

        inputEmail.addTextChangedListener(new LoginTextWatcher(inputEmail));
        inputPassword.addTextChangedListener(new LoginTextWatcher(inputPassword));

        Button btnLogin = (Button) view.findViewById(R.id.btn_login);
        btnLogin.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                submitForm();
            }
        });

        return view;
    }

    private class LoginTextWatcher implements TextWatcher {

        private View view;

        LoginTextWatcher(View view) {
            this.view = view;
        }

        @Override
        public void beforeTextChanged(CharSequence s, int start, int count, int after) {

        }

        @Override
        public void onTextChanged(CharSequence s, int start, int before, int count) {

        }

        @Override
        public void afterTextChanged(Editable s) {
            switch (view.getId()) {
                case R.id.input_email:
                    validateEmail();
                    break;
                case R.id.input_password:
                    validatePassword();
                    break;
            }
        }
    }

    /**
     * Validating form
     */
    private void submitForm() {

        if (!validateEmail()) {
            return;
        }

        if (!validatePassword()) {
            return;
        }

        activity.showLoadingDialog();

        RestfulAPI restfulAPI = new RestfulAPI();

        Retrofit retrofit = restfulAPI.getRestClient();

        UserEndPointInterface apiService =
                retrofit.create(UserEndPointInterface.class);

        Login login = new Login(inputEmail.getText().toString(), inputPassword.getText().toString());

        Call<Token> call = apiService.login(login);
        call.enqueue(new Callback<Token>() {
            @Override
            public void onResponse(Call<Token> call, Response<Token> response) {
                activity.hideLoadingDialog();

                int statusCode = response.code();

                Token token = response.body();

                if (statusCode == HttpURLConnection.HTTP_OK) {
                    GlobalBus.getBus().post(token);
                } else if (statusCode == HttpURLConnection.HTTP_FORBIDDEN) {
                    GlobalBus.getBus().post(Constant.ACCOUNT_NOT_EXIST);
                } else if (statusCode == HttpURLConnection.HTTP_CLIENT_TIMEOUT) {
                    GlobalBus.getBus().post(Constant.FAILURE_NETWORK);
                } else {
                    GlobalBus.getBus().post(Constant.FAILURE_UNKNOWN);
                }

            }

            @Override
            public void onFailure(Call<Token> call, Throwable t) {
                activity.hideLoadingDialog();
                GlobalBus.getBus().post(Constant.FAILURE_NETWORK);
            }
        });
    }

    private void requestFocus(View view) {
        if (view.requestFocus()) {
            activity.getWindow().setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_STATE_ALWAYS_VISIBLE);
        }
    }

    private boolean validateEmail() {
        String email = inputEmail.getText().toString().trim();

        if (email.isEmpty() || !Validation.isEmail(email)) {
            inputLayoutEmail.setError(getString(R.string.err_msg_email));
            requestFocus(inputEmail);
            return false;
        } else {
            inputLayoutEmail.setErrorEnabled(false);
        }

        return true;
    }

    private boolean validatePassword() {
        if (inputPassword.getText().toString().trim().isEmpty()) {
            inputLayoutPassword.setError(getString(R.string.err_msg_password_empty));
            requestFocus(inputPassword);
            return false;
        } else if (!Validation.isPassword(inputPassword.getText().toString().trim())) {
            inputLayoutPassword.setError(getString(R.string.err_msg_password_less_seven_letter));
            requestFocus(inputPassword);
            return false;
        } else {
            inputLayoutPassword.setErrorEnabled(false);
        }

        return true;
    }

}

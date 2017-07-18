package vn.axonactive.aevent.fragment;

import android.app.Fragment;
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
import vn.axonactive.aevent.model.Token;
import vn.axonactive.aevent.model.UserAccount;
import vn.axonactive.aevent.util.GlobalBus;
import vn.axonactive.aevent.util.Validation;

public class SignUpFragment extends Fragment {

    private EditText inputEmail;
    private EditText inputName;
    private EditText inputPassword;

    private TextInputLayout inputLayoutEmail, inputLayoutPassword, inputLayoutName;
    private BoardingActivity activity;

    public SignUpFragment() {
        // Required empty public constructor
    }

    public static SignUpFragment newInstance() {
        SignUpFragment fragment = new SignUpFragment();
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
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View view = inflater.inflate(R.layout.fragment_signup, container, false);

        activity = (BoardingActivity) getActivity();

        TextView txtLogin = (TextView) view.findViewById(R.id.txt_login);
        txtLogin.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                GlobalBus.getBus().post(Constant.LOGIN_CLICK);
            }
        });

        String text = "Already have account? <font color=#ff0000>Tap here to login</font>";
        txtLogin.setText(Html.fromHtml(text));

        inputEmail = (EditText) view.findViewById(R.id.input_email);
        inputPassword = (EditText) view.findViewById(R.id.input_password);
        inputName = (EditText) view.findViewById(R.id.input_name);

        inputLayoutEmail = (TextInputLayout) view.findViewById(R.id.input_layout_email);
        inputLayoutPassword = (TextInputLayout) view.findViewById(R.id.input_layout_password);
        inputLayoutName = (TextInputLayout) view.findViewById(R.id.input_layout_name);

        inputEmail.addTextChangedListener(new SignUpTextWatcher(inputEmail));
        inputPassword.addTextChangedListener(new SignUpTextWatcher(inputPassword));
        inputName.addTextChangedListener(new SignUpTextWatcher(inputName));

        Button btnSignUp = (Button) view.findViewById(R.id.btn_sign_up);
        btnSignUp.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                submitForm();
            }
        });

        return view;
    }

    private class SignUpTextWatcher implements TextWatcher {

        private View view;

        public SignUpTextWatcher(View view) {
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
                case R.id.input_name:
                    validateName();
            }
        }
    }

    /**
     * Validating form
     */
    private void submitForm() {

        if (!validateName()) {
            return;
        }

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
        UserAccount user = new UserAccount(inputName.getText().toString(), inputEmail.getText().toString(), inputPassword.getText().toString(), inputPassword.getText().toString());
        Call<Token> call = apiService.createUser(user);
        call.enqueue(new Callback<Token>() {
            @Override
            public void onResponse(Call<Token> call, Response<Token> response) {

                activity.hideLoadingDialog();

                int statusCode = response.code();
                Token token = response.body();

                if (statusCode == HttpURLConnection.HTTP_OK) {
                    if (token != null && token.getToken() != null) {
                        GlobalBus.getBus().post(token);
                    } else {
                        GlobalBus.getBus().post(Constant.ACCOUNT_EXIST);
                    }
                } else if (statusCode == HttpURLConnection.HTTP_BAD_REQUEST) {
                    GlobalBus.getBus().post(Constant.ACCOUNT_EXIST);
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

    private boolean validateName() {
        if (inputName.getText().toString().trim().isEmpty()) {
            inputLayoutName.setError(getString(R.string.err_msg_name));
            requestFocus(inputName);
            return false;
        } else {
            inputLayoutName.setErrorEnabled(false);
        }

        return true;
    }

}

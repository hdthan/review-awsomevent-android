package vn.axonactive.aevent_organizer.fragment;


import android.annotation.TargetApi;
import android.app.Activity;
import android.app.Fragment;
import android.content.Context;
import android.os.Build;
import android.os.Bundle;
import android.support.design.widget.TextInputLayout;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.EditText;

import java.net.HttpURLConnection;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;
import retrofit2.Retrofit;
import vn.axonactive.aevent_organizer.R;
import vn.axonactive.aevent_organizer.activity.BoardingActivity;
import vn.axonactive.aevent_organizer.api.EventEndpointInterface;
import vn.axonactive.aevent_organizer.api.RestfulAPI;
import vn.axonactive.aevent_organizer.common.AccountType;
import vn.axonactive.aevent_organizer.model.Login;
import vn.axonactive.aevent_organizer.model.Token;
import vn.axonactive.aevent_organizer.util.Validation;


/**
 * A simple {@link Fragment} subclass.
 */
public class LoginFragment extends Fragment {

    private OnLoginClickListener mListener;

    private EditText inputEmail;
    private EditText inputPassword;

    private TextInputLayout inputLayoutEmail, inputLayoutPassword;
    private BoardingActivity activity;

    public LoginFragment() {

    }

    public static LoginFragment newInstance() {
        LoginFragment fragment = new LoginFragment();
        Bundle args = new Bundle();
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public View onCreateView(LayoutInflater inflater, final ViewGroup container,
                             Bundle savedInstanceState) {

        View view = inflater.inflate(R.layout.fragment_login, container, false);

        activity = (BoardingActivity) getActivity();

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


    /*
     * Deprecated on API 23
     * Use onAttachToContext instead
      */
    @SuppressWarnings("deprecation")
    @Override
    public void onAttach(Activity activity) {
        super.onAttach(activity);
        if (Build.VERSION.SDK_INT < 23) {
            onAttachToContext(activity);
        }
    }

    protected void onAttachToContext(Context context) {
        if (context instanceof OnLoginClickListener) {
            mListener = (OnLoginClickListener) context;
        } else {
            throw new RuntimeException(context.toString()
                    + " must implement OnLoginClickListener");
        }
    }

    /*
* onAttach(Context) is not called on pre API 23 versions of Android and onAttach(Activity) is deprecated
* Use onAttachToContext instead
*/
    @TargetApi(23)
    @Override
    public final void onAttach(Context context) {
        super.onAttach(context);
        onAttachToContext(context);
    }


    @Override
    public void onDetach() {
        super.onDetach();
        mListener = null;
    }

    public interface OnLoginClickListener {
        void onLoginSuccess(String accountType, Token token);

        void onLoginFailureUnknown();

        void onLoginFailureAccountNotCorrect();

        void onLoginFailure();
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

        EventEndpointInterface apiService =
                retrofit.create(EventEndpointInterface.class);

        Login login = new Login(inputEmail.getText().toString(), inputPassword.getText().toString());

        Call<Token> call = apiService.login(login);
        call.enqueue(new Callback<Token>() {
            @Override
            public void onResponse(Call<Token> call, Response<Token> response) {
                activity.hideLoadingDialog();

                int statusCode = response.code();

                Token token = response.body();

                if (statusCode == HttpURLConnection.HTTP_OK) {
                    mListener.onLoginSuccess(AccountType.SYSTEM_ACCOUNT, token);
                } else if (statusCode == HttpURLConnection.HTTP_FORBIDDEN) {
                    mListener.onLoginFailureAccountNotCorrect();
                } else if (statusCode == HttpURLConnection.HTTP_CLIENT_TIMEOUT) {
                    mListener.onLoginFailure();
                } else {
                    mListener.onLoginFailureUnknown();
                }

            }

            @Override
            public void onFailure(Call<Token> call, Throwable t) {
                activity.hideLoadingDialog();
                mListener.onLoginFailure();
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

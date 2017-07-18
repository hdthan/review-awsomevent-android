package vn.axonactive.aevent.activity;

import android.content.Context;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.support.design.widget.TextInputLayout;
import android.support.v7.widget.Toolbar;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.WindowManager;
import android.widget.ArrayAdapter;
import android.widget.EditText;
import android.widget.RadioButton;
import android.widget.Spinner;
import android.widget.Toast;

import com.google.firebase.messaging.FirebaseMessaging;

import java.net.HttpURLConnection;

import es.dmoral.toasty.Toasty;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;
import retrofit2.Retrofit;
import vn.axonactive.aevent.R;
import vn.axonactive.aevent.api.EnrollmentEndpointInterface;
import vn.axonactive.aevent.api.RestfulAPI;
import vn.axonactive.aevent.api.UserEndPointInterface;
import vn.axonactive.aevent.model.Enrollment;
import vn.axonactive.aevent.model.EnrollmentPK;
import vn.axonactive.aevent.model.User;
import vn.axonactive.aevent.util.DataStorage;
import vn.axonactive.aevent.util.Validation;

public class JoinEventActivity extends BaseActivity {

    private EditText inputEmail;
    private EditText inputName;
    private EditText inputJob;
    private EditText inputPhone;
    private EditText inputCompany;

    private TextInputLayout inputLayoutEmail, inputLayoutPhone, inputLayoutName, inputLayoutJob, inputLayoutCompany;

    private RadioButton mRbMale, mRbFemale;

    private Spinner mAge;
    private Long userId;
    private EnrollmentEndpointInterface enrollmentApiService;
    private UserEndPointInterface userApiService;
    private Context context;
    private String token = null;
    private Retrofit retrofit;

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {

        MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.menu_join, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case R.id.action_join:
                submitForm();
                return true;
            default:
                return super.onOptionsItemSelected(item);
        }
    }


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_join_event);

        context = this;

        final Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);

        setTitle(DataStorage.currentEvent.getTitle());

        inputEmail = (EditText) this.findViewById(R.id.input_email);
        inputJob = (EditText) this.findViewById(R.id.input_job);
        inputPhone = (EditText) this.findViewById(R.id.input_phone);
        inputName = (EditText) this.findViewById(R.id.input_name);
        inputCompany = (EditText) this.findViewById(R.id.input_company);

        inputLayoutEmail = (TextInputLayout) this.findViewById(R.id.input_layout_email);
        inputLayoutPhone = (TextInputLayout) this.findViewById(R.id.input_layout_phone);
        inputLayoutJob = (TextInputLayout) this.findViewById(R.id.input_layout_job);
        inputLayoutName = (TextInputLayout) this.findViewById(R.id.input_layout_name);
        inputLayoutCompany = (TextInputLayout) this.findViewById(R.id.input_layout_company);

        inputEmail.addTextChangedListener(new JoinTextWatcher(inputEmail));
        inputJob.addTextChangedListener(new JoinTextWatcher(inputJob));
        inputPhone.addTextChangedListener(new JoinTextWatcher(inputPhone));
        inputName.addTextChangedListener(new JoinTextWatcher(inputName));
        inputCompany.addTextChangedListener(new JoinTextWatcher(inputCompany));

        mRbMale = (RadioButton) this.findViewById(R.id.radio_male);
        mRbFemale = (RadioButton) this.findViewById(R.id.radio_female);

        mAge = (Spinner) this.findViewById(R.id.age);

        final ArrayAdapter<CharSequence> adapter = ArrayAdapter.createFromResource(this,
                R.array.arrange_age_array, android.R.layout.simple_spinner_item);
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        mAge.setAdapter(adapter);

        RestfulAPI restfulAPI = new RestfulAPI();
        retrofit = restfulAPI.getRestClient();

        userApiService = retrofit.create(UserEndPointInterface.class);

        SharedPreferences prefs = getSharedPreferences(DataStorage.APP_PREFS, Context.MODE_PRIVATE);
        token = prefs.getString(DataStorage.TOKEN, null);

        if (token != null) {
            showLoadingDialog();
            Call<User> call = userApiService.getUser(token);
            call.enqueue(new Callback<User>() {
                @Override
                public void onResponse(Call<User> call, final Response<User> response) {
                    if (response != null) {
                        hideLoadingDialog();

                        User user = response.body();

                        String email = user.getEmail();

                        if (email != null && !"".equals(email)) {
                            inputEmail.setText(user.getEmail());
                            inputEmail.setFocusable(false);
                            inputEmail.setFocusableInTouchMode(false);
                            inputEmail.setClickable(false);
                        }

                        inputName.setText(user.getFullName());
                        inputJob.setText(user.getJob());
                        inputPhone.setText(user.getPhone());

                        userId = response.body().getId();

                        String age = response.body().getRangeAge();

                        if (age != null && !"".equals(age)) {
                            int pos = adapter.getPosition(age);
                            mAge.setSelection(pos);
                        }

                        int gender = user.getGender();
                        if (gender == 1) {
                            mRbMale.setChecked(true);
                        } else {
                            mRbFemale.setChecked(true);
                        }

                        String company = user.getCompany();

                        if (company != null && !"".equals(company)) {
                            inputCompany.setText(company);
                        }

                    }
                }

                @Override
                public void onFailure(Call<User> call, Throwable t) {
                    hideLoadingDialog();
                }
            });
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

        if (!validatePhone()) {
            return;
        }

        if (!validateJob()) {
            return;
        }

        if (!validateCompany()) {
            return;
        }

        showLoadingDialog();

        User user = new User();
        user.setId(userId);
        user.setEmail(inputEmail.getText().toString());
        user.setFullName(inputName.getText().toString());
        user.setRangeAge(String.valueOf(mAge.getSelectedItem()));
        user.setPhone(inputPhone.getText().toString());
        user.setJob(inputJob.getText().toString());

        if (mRbMale.isChecked()) {
            user.setGender(1);
        } else {
            user.setGender(0);
        }

        user.setCompany(inputCompany.getText().toString());

        Call<User> callUser = userApiService.updateUserBeforeJoin(token, user);

        callUser.enqueue(new Callback<User>() {
            @Override
            public void onResponse(Call<User> call, Response<User> response) {
                if (response.body() != null) {
                    joinEvent();
                } else {
                    hideLoadingDialog();
                    Toasty.error(context, "Something went wrong", Toast.LENGTH_LONG, true).show();
                }
            }

            @Override
            public void onFailure(Call<User> call, Throwable t) {
                hideLoadingDialog();
                Toasty.error(context, "Something went wrong", Toast.LENGTH_LONG, true).show();
            }
        });


    }

    private void joinEvent() {

        final Long eventId = (long) DataStorage.currentEvent.getId();

        final EnrollmentPK enrollmentPK = new EnrollmentPK(eventId, userId);

        enrollmentApiService = retrofit.create(EnrollmentEndpointInterface.class);

        Call<Enrollment> callEnroll = enrollmentApiService.becomeParticipant(token, enrollmentPK);

        callEnroll.enqueue(new Callback<Enrollment>() {
            @Override
            public void onResponse(Call<Enrollment> call, Response<Enrollment> response) {
                if (response != null) {
                    hideLoadingDialog();

                    if (response.code() == HttpURLConnection.HTTP_CREATED) {
                        Toasty.success(context, "Join successfully", Toast.LENGTH_LONG, true).show();
                        FirebaseMessaging.getInstance().subscribeToTopic("e" + eventId);
                        finish();
                    } else {
                        Toasty.error(context, "Something went wrong", Toast.LENGTH_LONG, true).show();
                    }
                }
            }

            @Override
            public void onFailure(Call<Enrollment> call, Throwable t) {
                hideLoadingDialog();
                Toasty.error(context, "Something went wrong", Toast.LENGTH_LONG, true).show();
            }
        });
    }

    private void requestFocus(View view) {
        if (view.requestFocus()) {
            getWindow().setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_STATE_ALWAYS_VISIBLE);
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

    private boolean validatePhone() {
        String phone = inputPhone.getText().toString().trim();

        if (phone.isEmpty() || !Validation.isPhoneNumber(phone)) {
            inputLayoutPhone.setError(getString(R.string.err_msg_phone));
            requestFocus(inputPhone);
            return false;
        } else {
            inputLayoutPhone.setErrorEnabled(false);
        }

        return true;
    }

    private boolean validateJob() {
        String job = inputJob.getText().toString().trim();

        if (job.isEmpty()) {
            inputLayoutJob.setError(getString(R.string.err_msg_job));
            requestFocus(inputJob);
            return false;
        } else {
            inputLayoutJob.setErrorEnabled(false);
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

    private boolean validateCompany() {
        if (inputCompany.getText().toString().trim().isEmpty()) {
            inputLayoutCompany.setError(getString(R.string.err_msg_com));
            requestFocus(inputCompany);
            return false;
        } else {
            inputLayoutCompany.setErrorEnabled(false);
        }

        return true;
    }

    private class JoinTextWatcher implements TextWatcher {

        private View view;

        JoinTextWatcher(View view) {
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
                case R.id.input_name:
                    validateName();
                    break;
                case R.id.input_phone:
                    validatePhone();
                    break;
                case R.id.input_job:
                    validateJob();
                    break;
                case R.id.input_company:
                    validateCompany();
                    break;
            }
        }

    }

}

package vn.axonactive.aevent_organizer.activity;

import android.app.Dialog;
import android.content.Context;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.support.design.widget.TabLayout;
import android.support.design.widget.TextInputLayout;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentPagerAdapter;
import android.support.v4.view.ViewPager;
import android.support.v7.app.AlertDialog;
import android.support.v7.widget.Toolbar;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.ProgressBar;
import android.widget.Toast;

import java.io.IOException;
import java.net.HttpURLConnection;
import java.util.ArrayList;
import java.util.List;

import es.dmoral.toasty.Toasty;
import okhttp3.MediaType;
import okhttp3.RequestBody;
import okhttp3.ResponseBody;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;
import retrofit2.Retrofit;
import retrofit2.converter.scalars.ScalarsConverterFactory;
import vn.axonactive.aevent_organizer.R;
import vn.axonactive.aevent_organizer.api.EventEndpointInterface;
import vn.axonactive.aevent_organizer.api.RestfulAPI;
import vn.axonactive.aevent_organizer.fragment.BroadcastFragment;
import vn.axonactive.aevent_organizer.fragment.CheckInFragment;
import vn.axonactive.aevent_organizer.fragment.DashBoardFragment;
import vn.axonactive.aevent_organizer.fragment.TimeLineFragment;
import vn.axonactive.aevent_organizer.model.User;
import vn.axonactive.aevent_organizer.util.DataStorage;
import vn.axonactive.aevent_organizer.util.Validation;

public class ManageParticipantActivity extends BaseActivity {

    private EditText inputEmail;
    private EditText inputName;
    private EditText inputPhone;
    private ProgressBar progressBar;

    private long id;
    private String title;
    private int type;

    private TextInputLayout inputLayoutEmail, inputLayoutName, inputLayoutPhone;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_manage_participant);

        Bundle bundle = getIntent().getExtras();

        if (bundle != null) {
            id = bundle.getLong("id");
            title = bundle.getString("title");
            type = bundle.getInt("type");
        }

        final Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);

        setTitle(title);

        ViewPager viewPager = (ViewPager) this.findViewById(R.id.viewpager);
        setupViewPager(viewPager);

        TabLayout tabLayout = (TabLayout) this.findViewById(R.id.tabs);
        tabLayout.setupWithViewPager(viewPager);

    }

    private void setupViewPager(ViewPager viewPager) {

        Bundle bundle = new Bundle();
        bundle.putLong("eventId", id);

        ViewPagerAdapter adapter = new ViewPagerAdapter(getSupportFragmentManager());

        CheckInFragment checkInFragment = CheckInFragment.newInstance();
        checkInFragment.setArguments(bundle);
        adapter.addFragment(checkInFragment, "CHECK IN");

        adapter.addFragment(DashBoardFragment.newInstance(), "STATISTIC");

        //organizer
        if (type == 1) {
            TimeLineFragment timeLineFragment = TimeLineFragment.newInstance();
            timeLineFragment.setArguments(bundle);
            adapter.addFragment(timeLineFragment, "AGENDA");

            BroadcastFragment broadcastFragment = BroadcastFragment.newInstance();
            broadcastFragment.setArguments(bundle);
            adapter.addFragment(broadcastFragment, "HISTORY");
        }
        viewPager.setAdapter(adapter);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {

        getMenuInflater().inflate(R.menu.menu_add_participant, menu);
        return true;

    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {

        int itemId = item.getItemId();

        switch (itemId) {
            case R.id.action_add: {

                final AlertDialog.Builder builder = new AlertDialog.Builder(this);

                LayoutInflater inflater = this.getLayoutInflater();

                View view = inflater.inflate(R.layout.dialog_add_participant, null);

                inputName = (EditText) view.findViewById(R.id.input_name);
                inputEmail = (EditText) view.findViewById(R.id.input_email);
                inputPhone = (EditText) view.findViewById(R.id.input_phone);

                inputName.addTextChangedListener(new AddParticipantTextWatcher(inputName));
                inputEmail.addTextChangedListener(new AddParticipantTextWatcher(inputEmail));
                inputPhone.addTextChangedListener(new AddParticipantTextWatcher(inputPhone));

                Button btnCancel = (Button) view.findViewById(R.id.button_cancel);
                Button btnSave = (Button) view.findViewById(R.id.button_save);
                ImageButton btnClose = (ImageButton) view.findViewById(R.id.btn_close);

                progressBar = (ProgressBar) view.findViewById(R.id.progressBar);

                inputLayoutEmail = (TextInputLayout) view.findViewById(R.id.input_layout_email);
                inputLayoutName = (TextInputLayout) view.findViewById(R.id.input_layout_name);
                inputLayoutPhone = (TextInputLayout) view.findViewById(R.id.input_layout_phone);

                builder.setView(view);

                final Dialog dialog = builder.create();

                dialog.show();

                btnCancel.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        dialog.dismiss();
                    }
                });

                btnSave.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        if (submitForm()) {
                            dialog.dismiss();
                        }
                    }
                });

                btnClose.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        dialog.dismiss();
                    }
                });

                return true;
            }
            default: {
                return super.onOptionsItemSelected(item);
            }
        }

    }

    private class AddParticipantTextWatcher implements TextWatcher {

        private View view;

        AddParticipantTextWatcher(View view) {
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
            }
        }
    }

    /**
     * Validating form
     */
    private boolean submitForm() {

        if (!validateName()) {
            return false;
        }

        if (!validateEmail()) {
            return false;
        }

        if (!validatePhone()) {
            return false;
        }

        progressBar.setVisibility(View.VISIBLE);
        progressBar.setIndeterminate(true);

        String email = inputEmail.getText().toString().trim();
        String name = inputName.getText().toString().trim();
        String phone = inputPhone.getText().toString().trim();

        User user = new User();
        user.setFullName(name);
        user.setEmail(email);
        user.setPhone(phone);

        SharedPreferences preferences = getSharedPreferences(DataStorage.APP_PREFS, Context.MODE_PRIVATE);
        String token = preferences.getString(DataStorage.TOKEN, null);

        RestfulAPI restfulAPI = new RestfulAPI(ScalarsConverterFactory.create());

        Retrofit retrofit = restfulAPI.getRestClient();

        EventEndpointInterface apiService = retrofit.create(EventEndpointInterface.class);

        String body = String.format("{\"email\": \"%s\", \"fullName\": \"%s\", \"phone\": \"%s\"}", email, name, phone);

        final RequestBody requestBody = RequestBody.create(MediaType.parse("application/json"), body);

        Call<ResponseBody> call = apiService.addParticipant(token, id, requestBody);

        call.enqueue(new Callback<ResponseBody>() {
            @Override
            public void onResponse(Call<ResponseBody> call, Response<ResponseBody> response) {
                if (response != null) {

                    int statusCode = response.code();

                    String message = "";

                    if (statusCode == HttpURLConnection.HTTP_OK) {
                        try {
                            message = response.body().string();
                        } catch (IOException e) {
                            e.printStackTrace();
                        }
                    } else if (statusCode == HttpURLConnection.HTTP_CONFLICT) {
                        try {
                            message = response.body().string();
                        } catch (IOException e) {
                            e.printStackTrace();
                        }
                    } else {
                        message = getString(R.string.err_unknown);
                    }

                    progressBar.setVisibility(View.GONE);

                    Toasty.success(ManageParticipantActivity.this, message, Toast.LENGTH_SHORT, true).show();

                }

            }

            @Override
            public void onFailure(Call<ResponseBody> call, Throwable t) {
                progressBar.setVisibility(View.GONE);
                Toasty.info(ManageParticipantActivity.this, getString(R.string.err_network_connection), Toast.LENGTH_SHORT, true).show();
            }
        });

        return true;

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

        if (phone.isEmpty()) {
            inputLayoutPhone.setError(getString(R.string.err_msg_phone_empty));
            requestFocus(inputLayoutPhone);
            return false;
        } else if (!Validation.isPhoneNumber(phone)) {
            inputLayoutPhone.setError(getString(R.string.err_msg_phone_invalid));
            requestFocus(inputLayoutPhone);
            return false;
        } else {
            inputLayoutPhone.setErrorEnabled(false);
        }

        return true;
    }

    private boolean validateName() {

        String name = inputName.getText().toString().trim();

        if (name.isEmpty()) {
            inputLayoutName.setError(getString(R.string.err_msg_name));
            requestFocus(inputName);
            return false;
        } else {
            inputLayoutName.setErrorEnabled(false);
        }

        return true;

    }

    private class ViewPagerAdapter extends FragmentPagerAdapter {
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

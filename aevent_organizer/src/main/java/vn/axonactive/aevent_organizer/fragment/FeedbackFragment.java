package vn.axonactive.aevent_organizer.fragment;

import android.app.ProgressDialog;
import android.content.SharedPreferences;
import android.graphics.Color;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.content.ContextCompat;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;

import java.net.HttpURLConnection;

import es.dmoral.toasty.Toasty;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;
import retrofit2.Retrofit;
import vn.axonactive.aevent_organizer.R;
import vn.axonactive.aevent_organizer.api.FeedbackEndpointInterface;
import vn.axonactive.aevent_organizer.api.RestfulAPI;
import vn.axonactive.aevent_organizer.model.Feedback;
import vn.axonactive.aevent_organizer.util.DataStorage;

import static android.content.Context.MODE_PRIVATE;

/**
 * Created by ltphuc on 3/16/2017.
 */

public class FeedbackFragment extends Fragment {

    private EditText mEdtFeedback;
    private TextView mTvCount;
    private Button mBtnReset;
    private Button mBtnSubmit;
    private ProgressDialog mProgressDialog;

    public FeedbackFragment() {

    }

    public static FeedbackFragment newInstance() {
        FeedbackFragment fragment = new FeedbackFragment();
        Bundle args = new Bundle();
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
        super.onCreateOptionsMenu(menu, inflater);
        menu.clear();
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        setHasOptionsMenu(true);

        mProgressDialog = new ProgressDialog(getContext());
        mProgressDialog.setIndeterminate(true);
        mProgressDialog.setMessage("Sending...");

        View view = inflater.inflate(R.layout.fragment_feedback, container, false);

        mEdtFeedback = (EditText) view.findViewById(R.id.edt_feed_back);
        mTvCount = (TextView) view.findViewById(R.id.txt_count);
        mBtnReset = (Button) view.findViewById(R.id.btn_reset);
        mBtnSubmit = (Button) view.findViewById(R.id.btn_submit);

        mEdtFeedback.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {

            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                String length = s.length() + "/250";
                mTvCount.setText(length);
                if (s.length() > 0) {
                    mBtnReset.setBackgroundResource(R.drawable.button_border_color);
                    mBtnReset.setTextColor(ContextCompat.getColor(getContext(), R.color.colorPrimary));
                    mBtnSubmit.setBackgroundResource(R.drawable.button_border_color);
                    mBtnSubmit.setTextColor(ContextCompat.getColor(getContext(), R.color.colorPrimary));
                    mBtnSubmit.setEnabled(true);
                } else {
                    mBtnReset.setBackgroundResource(R.drawable.button_border_transparent);
                    mBtnReset.setTextColor(Color.parseColor("#BDBDBD"));
                    mBtnSubmit.setBackgroundResource(R.drawable.button_border_transparent);
                    mBtnSubmit.setTextColor(Color.parseColor("#BDBDBD"));
                    mBtnSubmit.setEnabled(false);
                }
            }

            @Override
            public void afterTextChanged(Editable s) {

            }
        });

        mBtnReset.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                mEdtFeedback.setText("");
            }
        });

        mBtnSubmit.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                sendFeedback();
            }
        });

        return view;
    }

    private void sendFeedback() {
        //send data to server
        final String content = mEdtFeedback.getText().toString();

        if ("".equals(content)) {
            //error here
            return;
        }

        SharedPreferences prefs = getContext().getSharedPreferences(DataStorage.APP_PREFS, MODE_PRIVATE);
        String token = prefs.getString(DataStorage.TOKEN, null);

        if (token != null) {

            RestfulAPI restfulAPI = new RestfulAPI();

            Retrofit retrofit = restfulAPI.getRestClient();

            FeedbackEndpointInterface apiService = retrofit.create(FeedbackEndpointInterface.class);

            Feedback feedback = new Feedback(content);

            Call<Feedback> call = apiService.create(token, feedback);

            call.enqueue(new Callback<Feedback>() {
                @Override
                public void onResponse(Call<Feedback> call, Response<Feedback> response) {
                    if (response.body() != null) {
                        if (response.code() == HttpURLConnection.HTTP_OK) {

                            Toasty.success(getContext(), "Thanks for feedback").show();
                            mEdtFeedback.setText("");
                        } else {
                            Toasty.info(getContext(), "Something went wrong").show();
                        }
                    }
                    mProgressDialog.dismiss();
                }

                @Override
                public void onFailure(Call<Feedback> call, Throwable t) {
                    Toasty.info(getContext(), "Something went wrong").show();
                    mProgressDialog.dismiss();
                }
            });
        }
    }

}

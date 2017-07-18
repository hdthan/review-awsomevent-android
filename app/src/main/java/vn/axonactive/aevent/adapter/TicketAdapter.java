package vn.axonactive.aevent.adapter;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.TextView;

import com.squareup.picasso.Picasso;

import java.io.IOException;
import java.net.HttpURLConnection;
import java.text.SimpleDateFormat;
import java.util.List;

import es.dmoral.toasty.Toasty;
import okhttp3.ResponseBody;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;
import retrofit2.Retrofit;
import retrofit2.converter.scalars.ScalarsConverterFactory;
import vn.axonactive.aevent.BuildConfig;
import vn.axonactive.aevent.R;
import vn.axonactive.aevent.activity.EventDetailActivity;
import vn.axonactive.aevent.activity.MainActivity;
import vn.axonactive.aevent.activity.TicketActivity;
import vn.axonactive.aevent.api.EnrollmentEndpointInterface;
import vn.axonactive.aevent.api.RestfulAPI;
import vn.axonactive.aevent.model.Enrollment;
import vn.axonactive.aevent.model.Event;
import vn.axonactive.aevent.util.DataStorage;

/**
 * Created by Dell on 2/12/2017.
 */

public class TicketAdapter extends RecyclerView.Adapter<RecyclerView.ViewHolder> {

    private final int VIEW_ITEM = 1;
    private final int VIEW_PROGRESS_BAR = 0;

    private Context context;

    private List<Enrollment> mEnrollments;

    public TicketAdapter(List<Enrollment> mEnrollments) {
        this.mEnrollments = mEnrollments;
    }

    @Override
    public int getItemViewType(int position) {

        return mEnrollments.get(position) != null ? VIEW_ITEM : VIEW_PROGRESS_BAR;

    }

    @Override
    public RecyclerView.ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {


        context = parent.getContext();

        RecyclerView.ViewHolder viewHolder = null;

        context = parent.getContext();

        if (viewType == VIEW_ITEM) {
            View inflatedView = LayoutInflater.from(context)
                    .inflate(R.layout.ticket_row, parent, false);
            viewHolder = new TicketViewHolder(context, inflatedView);
        } else if (viewType == VIEW_PROGRESS_BAR) {
            View inflatedView = LayoutInflater.from(context)
                    .inflate(R.layout.progressbar_item, parent, false);
            viewHolder = new ProgressBarViewHolder(inflatedView);
        }


        return viewHolder;

    }

    @Override
    public void onBindViewHolder(RecyclerView.ViewHolder holder, int position) {

        if (holder instanceof TicketViewHolder) {
            Enrollment enrollment = mEnrollments.get(position);
            ((TicketViewHolder) holder).bindTicket(enrollment);
        } else {
            ((ProgressBarViewHolder) holder).setLoading(true);
        }

    }

    @Override
    public int getItemCount() {
        return mEnrollments.size();
    }

    public void showLoading() {
        mEnrollments.add(null);

        notifyItemInserted(mEnrollments.size() - 1);


    }

    public void hideLoading() {
        if (mEnrollments.size() > 1) {
            mEnrollments.remove(mEnrollments.size() - 1);
            notifyItemRemoved(mEnrollments.size());
        }
    }

    private class TicketViewHolder extends RecyclerView.ViewHolder {

        private Context context;

        private ImageView mImage;
        private TextView mTitle;
        private TextView mEnrollTime;
        private Button mTicket;
        private Button mDetail;
        private ProgressBar mProgressBar;

        private Enrollment mEnrollment;

        TicketViewHolder(Context context, View v) {
            super(v);

            this.context = context;

            mImage = (ImageView) v.findViewById(R.id.image);
            mTitle = (TextView) v.findViewById(R.id.title);
            mEnrollTime = (TextView) v.findViewById(R.id.enroll_time);
            mTicket = (Button) v.findViewById(R.id.ticket);
            mDetail = (Button) v.findViewById(R.id.detail);
            mProgressBar = (ProgressBar) v.findViewById(R.id.progressBar);

            v.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    navigateToTicket();
                }
            });

        }

        void bindTicket(Enrollment enrollment) {

            mEnrollment = enrollment;

            Event event = mEnrollment.getEvent();

            mTitle.setText(event.getTitle());

            SimpleDateFormat format = new SimpleDateFormat(context.getString(R.string.date_time_format));

            mEnrollTime.setText("Enroll Time: " + format.format(mEnrollment.getEnrollDate()));

            String urlImage = BuildConfig.API_URL + event.getImageCover() + "/cover";

            Picasso.with(context)
                    .load(urlImage)
                    .placeholder(R.drawable.image_cover_default)
                    .into(mImage, new com.squareup.picasso.Callback() {
                        @Override
                        public void onSuccess() {
                            mProgressBar.setVisibility(View.GONE);
                        }

                        @Override
                        public void onError() {
                            mProgressBar.setVisibility(View.GONE);
                        }
                    });

            mTicket.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    navigateToTicket();
                }
            });

            mDetail.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    navigateToDetail();
                }
            });

        }


        private void navigateToDetail() {
            Intent showEventDetailIntent = new Intent(context, EventDetailActivity.class);
            DataStorage.id = mEnrollment.getEvent().getId();
            context.startActivity(showEventDetailIntent);
        }

        private void navigateToTicket() {
            Intent intent = new Intent(context, TicketActivity.class);
            intent.putExtra("url", mEnrollment.getAuthorizationCode());
            intent.putExtra("title", mEnrollment.getEvent().getTitle());
            context.startActivity(intent);
        }

    }


    class ProgressBarViewHolder extends RecyclerView.ViewHolder {

        ProgressBar mProgressBar;

        ProgressBarViewHolder(View itemView) {
            super(itemView);

            mProgressBar = (ProgressBar) itemView.findViewById(R.id.progressBar);
            mProgressBar.setIndeterminate(true);

        }

        void setLoading(boolean loading) {
            mProgressBar.setIndeterminate(loading);
        }

    }

}

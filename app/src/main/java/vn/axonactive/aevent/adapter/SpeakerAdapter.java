package vn.axonactive.aevent.adapter;

import android.content.Context;
import android.content.Intent;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import com.squareup.picasso.Picasso;

import java.util.List;

import vn.axonactive.aevent.BuildConfig;
import vn.axonactive.aevent.R;
import vn.axonactive.aevent.activity.SpeakerDetailActivity;
import vn.axonactive.aevent.model.Speaker;

/**
 * Created by ltphuc on 1/17/2017.
 */

public class SpeakerAdapter extends RecyclerView.Adapter<SpeakerAdapter.SpeakerExpandHolder> {

    private Context context;

    private List<Speaker> mSpeakers;

    public SpeakerAdapter(List<Speaker> mSpeakers) {
        this.mSpeakers = mSpeakers;
    }

    @Override
    public SpeakerAdapter.SpeakerExpandHolder onCreateViewHolder(ViewGroup parent, int viewType) {

        context = parent.getContext();

        View inflatedView = LayoutInflater.from(context)
                .inflate(R.layout.speaker_expand_row, parent, false);

        return new SpeakerAdapter.SpeakerExpandHolder(context, inflatedView);
    }

    @Override
    public void onBindViewHolder(SpeakerExpandHolder holder, int position) {
        Speaker speaker = mSpeakers.get(position);
        holder.bindSpeaker(speaker);
    }

    @Override
    public int getItemCount() {

        if (mSpeakers != null) {
            return mSpeakers.size();
        }
        return 0;
    }

    class SpeakerExpandHolder extends RecyclerView.ViewHolder implements View.OnClickListener {

        private Context context;

        private ImageView mImage;
        private TextView mName;
        private TextView mMajor;
        private Speaker mSpeaker;

        SpeakerExpandHolder(Context context, View v) {
            super(v);

            this.context = context;

            mImage = (ImageView) v.findViewById(R.id.image);
            mName = (TextView) v.findViewById(R.id.name);
            mMajor = (TextView) v.findViewById(R.id.major);

            v.setOnClickListener(this);

        }

        void bindSpeaker(Speaker speaker) {

            mSpeaker = speaker;

            String urlImage = BuildConfig.API_URL + speaker.getAvatar();

            Picasso.with(context)
                    .load(urlImage)
                    .into(mImage);

            String prefix = "Mr. ";

            if (mSpeaker.getGender() == 0) {
                prefix = "Ms. ";
            }

            mName.setText(prefix + mSpeaker.getName());
            mMajor.setText(mSpeaker.getMajor());

        }

        @Override
        public void onClick(View v) {

            Intent intent = new Intent(context, SpeakerDetailActivity.class);
            intent.putExtra("speaker", mSpeaker);

            context.startActivity(intent);
        }
    }

}

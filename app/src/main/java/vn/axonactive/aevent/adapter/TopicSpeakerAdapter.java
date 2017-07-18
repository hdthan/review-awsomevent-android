package vn.axonactive.aevent.adapter;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.TextView;

import com.squareup.picasso.Picasso;

import java.util.List;

import vn.axonactive.aevent.BuildConfig;
import vn.axonactive.aevent.R;
import vn.axonactive.aevent.model.Speaker;
import vn.axonactive.aevent.model.TopicSpeaker;

/**
 * Created by Dell on 3/30/2017.
 */

public class TopicSpeakerAdapter extends BaseAdapter {

    private Context mContext;
    private List<TopicSpeaker> mSpeakers;

    public TopicSpeakerAdapter(Context mContext, List<TopicSpeaker> mSpeakers) {
        this.mContext = mContext;
        this.mSpeakers = mSpeakers;
    }

    @Override
    public int getCount() {
        return mSpeakers.size();
    }

    public Object getItem(int position) {
        return null;
    }

    public long getItemId(int position) {
        return 0;
    }

    @Override
    public View getView(int position, View convertView, ViewGroup viewGroup) {

        LayoutInflater inflater = (LayoutInflater) mContext.getSystemService(Context.LAYOUT_INFLATER_SERVICE);

        View gridView;

        if (convertView == null) {

            gridView = inflater.inflate(R.layout.topic_speaker_row, null);

        } else {
            gridView = convertView;
        }

        TextView tvSpeakerName = (TextView) gridView.findViewById(R.id.txt_speaker_name);
        ImageView ivAvatar = (ImageView) gridView.findViewById(R.id.iv_avatar);

        Speaker speaker = mSpeakers.get(position).getSpeaker();

        String prefix = "Mr. ";

        if (speaker.getGender() == 0) {
            prefix = "Ms. ";
        }

        tvSpeakerName.setText(prefix + speaker.getName());

        Picasso.with(mContext)
                .load(BuildConfig.API_URL + speaker.getAvatar())
                .into(ivAvatar);

        return gridView;
    }
}

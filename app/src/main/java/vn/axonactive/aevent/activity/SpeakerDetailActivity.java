package vn.axonactive.aevent.activity;

import android.content.Intent;
import android.content.res.Resources;
import android.net.Uri;
import android.os.Bundle;
import android.support.v7.widget.Toolbar;
import android.text.SpannableString;
import android.util.TypedValue;
import android.view.View;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.TextView;

import com.squareup.picasso.Picasso;

import vn.axonactive.aevent.BuildConfig;
import vn.axonactive.aevent.R;
import vn.axonactive.aevent.model.Speaker;

public class SpeakerDetailActivity extends BaseActivity {

    private Speaker mSpeaker;

    private ImageView mIvAvatar;
    private TextView mTvName;
    private TextView mTvMajor;
    private TextView mTvDes;
    private ImageButton mBtnLinkedIn;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        mSpeaker = getIntent().getParcelableExtra("speaker");

        setContentView(R.layout.activity_speaker_detail);

        mIvAvatar = (ImageView) this.findViewById(R.id.iv_avatar);
        mTvName = (TextView) this.findViewById(R.id.txt_speaker_name);
        mTvMajor = (TextView) this.findViewById(R.id.txt_major);
        mTvDes = (TextView) this.findViewById(R.id.txt_des);
        mBtnLinkedIn = (ImageButton) this.findViewById(R.id.btn_linkedIn);

        final Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        setTitle("Speaker Detail");

        if (mSpeaker != null) {

            String prefix = "Mr. ";

            if (mSpeaker.getGender() == 0) {
                prefix = "Ms. ";
            }

            mTvName.setText(prefix + mSpeaker.getName());

            mTvMajor.setText(mSpeaker.getMajor());

            String urlImage = BuildConfig.API_URL + mSpeaker.getAvatar();

            Picasso.with(this)
                    .load(urlImage)
                    .into(mIvAvatar);

            String description;

            String tmp = mSpeaker.getDescription();

            if (tmp == null || "".equals(tmp)) {
                description = "No description.";
            } else {
                description = tmp;
            }

            Resources r = getResources();
            int px = Math.round(TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, 30, r.getDisplayMetrics()));

            SpannableString spnDes = new SpannableString(description);
            if (spnDes.length() > 0) {
                spnDes.setSpan(new android.text.style.LeadingMarginSpan.Standard(px, 0), 0, 1, 0);
            }

            final String linkedIn = mSpeaker.getLinkedIn();

            if (linkedIn == null || "".equals(linkedIn)) {
                mBtnLinkedIn.setVisibility(View.GONE);
            } else {
                mBtnLinkedIn.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        Intent intent = new Intent(Intent.ACTION_VIEW,
                                Uri.parse(linkedIn));
                        startActivity(intent);
                    }
                });
            }

            mTvDes.setText(spnDes);

        }
        toolbar.setNavigationOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                finish();
            }
        });
    }
}

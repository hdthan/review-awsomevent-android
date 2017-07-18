package vn.axonactive.aevent.adapter;

import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.webkit.URLUtil;
import android.widget.ImageView;
import android.widget.TextView;

import com.squareup.picasso.Picasso;

import java.util.List;

import es.dmoral.toasty.Toasty;
import vn.axonactive.aevent.BuildConfig;
import vn.axonactive.aevent.R;
import vn.axonactive.aevent.model.Sponsor;

/**
 * Created by ltphuc on 1/17/2017.
 */

public class SponsorAdapter extends RecyclerView.Adapter<SponsorAdapter.SponsorHolder> {

    private Context context;

    private List<Sponsor> mSponsors;

    public SponsorAdapter(List<Sponsor> mSponsors) {
        this.mSponsors = mSponsors;
    }

    @Override
    public SponsorAdapter.SponsorHolder onCreateViewHolder(ViewGroup parent, int viewType) {

        context = parent.getContext();

        View inflatedView = LayoutInflater.from(context)
                .inflate(R.layout.sponsor_row, parent, false);

        return new SponsorAdapter.SponsorHolder(context, inflatedView);
    }

    @Override
    public void onBindViewHolder(SponsorHolder holder, int position) {
        Sponsor Sponsor = mSponsors.get(position);
        holder.bindSponsor(Sponsor);
    }

    @Override
    public int getItemCount() {
        return mSponsors.size();
    }

    class SponsorHolder extends RecyclerView.ViewHolder implements View.OnClickListener {

        private Context context;

        private ImageView mImage;
        private TextView mName;
        private TextView mLocation;
        private Sponsor mSponsor;

        SponsorHolder(Context context, View v) {
            super(v);

            this.context = context;

            mImage = (ImageView) v.findViewById(R.id.image);
            mName = (TextView) v.findViewById(R.id.name);
            mLocation = (TextView) v.findViewById(R.id.location);

            v.setOnClickListener(this);
        }

        void bindSponsor(Sponsor sponsor) {

            mSponsor = sponsor;

            String urlImage = BuildConfig.API_URL + sponsor.getImage();

            Picasso.with(context)
                    .load(urlImage)
                    .into(mImage);

            mName.setText(mSponsor.getSponsorName());
            mLocation.setText(mSponsor.getLocation());

        }

        @Override
        public void onClick(View v) {
            String website = mSponsor.getWebsite();

            if (website != null && !"".equals(website) && URLUtil.isValidUrl(website)) {

                Intent intent = new Intent(Intent.ACTION_VIEW,
                        Uri.parse(website));
                context.startActivity(intent);
            } else {
                Toasty.normal(context, "This company's website is unavailable").show();
            }
        }
    }

}

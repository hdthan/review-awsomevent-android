package vn.axonactive.aevent.adapter;

import android.content.Context;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ProgressBar;
import android.widget.TextView;

import java.text.SimpleDateFormat;
import java.util.List;

import vn.axonactive.aevent.R;
import vn.axonactive.aevent.model.Feedback;

/**
 * Created by ltphuc on 3/24/2017.
 */

public class FeedbackAdapter extends RecyclerView.Adapter<RecyclerView.ViewHolder> {

    private final int VIEW_ITEM = 1;
    private final int VIEW_PROGRESS_BAR = 0;

    private Context context;

    private List<Feedback> mFeedbacks;

    public FeedbackAdapter(List<Feedback> feedbacks) {
        this.mFeedbacks = feedbacks;
    }

    @Override
    public RecyclerView.ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {

        context = parent.getContext();

        RecyclerView.ViewHolder viewHolder = null;

        if (viewType == VIEW_ITEM) {
            View inflatedView = LayoutInflater.from(context)
                    .inflate(R.layout.feedback_row, parent, false);
            viewHolder = new FeedbackViewHolder(context, inflatedView);
        } else {
            View inflatedView = LayoutInflater.from(context)
                    .inflate(R.layout.progressbar_item, parent, false);
            viewHolder = new ProgressBarViewHolder(inflatedView);
        }

        return viewHolder;
    }

    @Override
    public void onBindViewHolder(RecyclerView.ViewHolder holder, int position) {

        if (holder instanceof FeedbackViewHolder) {
            Feedback feedback = mFeedbacks.get(position);
            ((FeedbackViewHolder) holder).bindFeedback(feedback);
        } else {
            ((ProgressBarViewHolder) holder).setLoading(true);
        }

    }

    @Override
    public int getItemViewType(int position) {
        return mFeedbacks.get(position) != null ? VIEW_ITEM : VIEW_PROGRESS_BAR;
    }

    @Override
    public int getItemCount() {
        return mFeedbacks.size();
    }

    public void showLoading() {
        mFeedbacks.add(null);

        notifyItemInserted(mFeedbacks.size() - 1);


    }

    public void hideLoading() {
        if (mFeedbacks.size() > 1) {
            mFeedbacks.remove(mFeedbacks.size() - 1);
            notifyItemRemoved(mFeedbacks.size());
        }
    }

    private class FeedbackViewHolder extends RecyclerView.ViewHolder {

        private TextView mTvSendDate;
        private TextView mTvContent;
        private Button mBtnSeeMore;

        FeedbackViewHolder(Context context, View v) {

            super(v);

            mTvSendDate = (TextView) v.findViewById(R.id.txt_send_date);
            mTvContent = (TextView) v.findViewById(R.id.txt_content);
            mBtnSeeMore = (Button) v.findViewById(R.id.btn_see_more);

        }

        void bindFeedback(Feedback feedback) {

            String content = feedback.getContent();

            mTvContent.setText(content);

            mTvContent.post(new Runnable() {
                @Override
                public void run() {
                    int lineCount = mTvContent.getLineCount();

                    if (lineCount > 3) {
                        mBtnSeeMore.setVisibility(View.VISIBLE);
                        mTvContent.setMaxLines(2);
                    } else {
                        mBtnSeeMore.setVisibility(View.GONE);
                    }

                }
            });

            SimpleDateFormat dateFormat = new SimpleDateFormat("MMM dd, yyyy");
            mTvSendDate.setText(dateFormat.format(feedback.getSendDate()));

            mBtnSeeMore.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {

                    if (mTvContent.getMaxLines() == 2) {
                        mTvContent.setMaxLines(10);
                        mBtnSeeMore.setText("SHOW LESS");
                    } else {
                        mTvContent.setMaxLines(2);
                        mBtnSeeMore.setText("SHOW MORE");
                    }

                }
            });

        }
    }

    private class ProgressBarViewHolder extends RecyclerView.ViewHolder {

        ProgressBar mProgressBar;

        ProgressBarViewHolder(View itemView) {
            super(itemView);

            mProgressBar = (ProgressBar) itemView.findViewById(R.id.progressBar);
            mProgressBar.setIndeterminate(true);

        }

        public void setLoading(boolean loading) {
            mProgressBar.setIndeterminate(loading);
        }

    }

}

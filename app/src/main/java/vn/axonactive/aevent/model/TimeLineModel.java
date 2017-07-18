package vn.axonactive.aevent.model;

import android.os.Parcel;
import android.os.Parcelable;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import vn.axonactive.aevent.util.OrderStatusUtil;

/**
 * Created by ltphuc on 3/10/2017.
 */

public class TimeLineModel implements Parcelable {

    private String mTitle;
    private String mLocation;
    private Date mStartTime;
    private Date mEndTime;
    private boolean mInvisibleDate;
    private boolean mInvisibleTime;
    private String mDescription;
    private List<TopicSpeaker> topicSpeakers;


    public TimeLineModel(String mTitle, String mLocation, Date mStartTime, Date mEndTime, String mDescription) {
        this.mTitle = mTitle;
        this.mLocation = mLocation;
        this.mStartTime = mStartTime;
        this.mEndTime = mEndTime;
        this.mDescription = mDescription;
    }

    public List<TopicSpeaker> getTopicSpeakers() {
        return topicSpeakers;
    }

    public void setTopicSpeakers(List<TopicSpeaker> topicSpeakers) {
        this.topicSpeakers = topicSpeakers;
    }

    public String getTitle() {
        return mTitle;
    }

    public String getLocation() {
        return mLocation;
    }

    public Date getStartTime() {
        return mStartTime;
    }

    public Date getEndTime() {
        return mEndTime;
    }

    public boolean isInvisibleDate() {
        return mInvisibleDate;
    }

    public boolean isInvisibleTime() {
        return mInvisibleTime;
    }

    public String getDescription() {
        return mDescription;
    }

    public OrderStatus getStatus() {
        return OrderStatusUtil.convertPeriodTimeToStatus(mStartTime, mEndTime);
    }

    public void setInvisibleDate(boolean mInvisibleDate) {
        this.mInvisibleDate = mInvisibleDate;
    }

    public void setInvisibleTime(boolean mInvisibleTime) {
        this.mInvisibleTime = mInvisibleTime;
    }

    protected TimeLineModel(Parcel in) {

        this.mTitle = in.readString();
        this.mLocation = in.readString();

        long tmpStartTime = in.readLong();
        mStartTime = tmpStartTime != -1 ? new Date(tmpStartTime) : null;

        long tmpEndTime = in.readLong();
        mEndTime = tmpEndTime != -1 ? new Date(tmpEndTime) : null;

        mInvisibleDate = in.readInt() == 1;
        mInvisibleTime = in.readInt() == 1;
        mDescription = in.readString();

        if (in.readByte() == 0x01) {
            topicSpeakers = new ArrayList<>();
            in.readList(topicSpeakers, TopicSpeaker.class.getClassLoader());
        } else {
            topicSpeakers = null;
        }
    }

    public static final Creator<TimeLineModel> CREATOR = new Creator<TimeLineModel>() {
        @Override
        public TimeLineModel createFromParcel(Parcel in) {
            return new TimeLineModel(in);
        }

        @Override
        public TimeLineModel[] newArray(int size) {
            return new TimeLineModel[size];
        }
    };

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {

        dest.writeString(mTitle);
        dest.writeString(mLocation);
        dest.writeLong(mStartTime != null ? mStartTime.getTime() : -1L);
        dest.writeLong(mEndTime != null ? mEndTime.getTime() : -1L);
        dest.writeInt(mInvisibleDate ? 1 : 0);
        dest.writeInt(mInvisibleTime ? 1 : 0);
        dest.writeString(mDescription);

        if (topicSpeakers == null) {
            dest.writeByte((byte) (0x00));
        } else {
            dest.writeByte((byte) (0x01));
            dest.writeList(topicSpeakers);
        }
    }
}

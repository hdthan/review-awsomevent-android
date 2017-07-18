package vn.axonactive.aevent_organizer.model;

import android.os.Parcel;
import android.os.Parcelable;

import java.util.Date;

import vn.axonactive.aevent_organizer.util.OrderStatusUtil;

/**
 * Created by ltphuc on 3/10/2017.
 */

public class TimeLineModel implements Parcelable {

    private long id;
    private String mTitle;
    private String mLocation;
    private Date mStartTime;
    private Date mEndTime;
    private boolean mInvisibleDate;
    private boolean mInvisibleTime;


    public TimeLineModel(long id, String mTitle, String mLocation, Date mStartTime, Date mEndTime) {
        this.id = id;
        this.mTitle = mTitle;
        this.mLocation = mLocation;
        this.mStartTime = mStartTime;
        this.mEndTime = mEndTime;
    }

    public long getId() {
        return id;
    }

    public void setId(long id) {
        this.id = id;
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

        this.id = in.readLong();
        this.mTitle = in.readString();
        this.mLocation = in.readString();

        long tmpStartTime = in.readLong();
        mStartTime = tmpStartTime != -1 ? new Date(tmpStartTime) : null;

        long tmpEndTime = in.readLong();
        mEndTime = tmpEndTime != -1 ? new Date(tmpEndTime) : null;

        mInvisibleDate = in.readInt() == 1;
        mInvisibleTime = in.readInt() == 1;
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

        dest.writeLong(id);
        dest.writeString(mTitle);
        dest.writeString(mLocation);
        dest.writeLong(mStartTime != null ? mStartTime.getTime() : -1L);
        dest.writeLong(mEndTime != null ? mEndTime.getTime() : -1L);
        dest.writeInt(mInvisibleDate ? 1 : 0);
        dest.writeInt(mInvisibleTime ? 1 : 0);
    }
}

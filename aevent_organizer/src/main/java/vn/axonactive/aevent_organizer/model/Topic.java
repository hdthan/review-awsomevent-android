package vn.axonactive.aevent_organizer.model;

import android.os.Parcel;
import android.os.Parcelable;

import com.google.gson.annotations.SerializedName;

import java.util.Date;

/**
 * Created by dangducnam on 1/10/17.
 */

public class Topic implements Parcelable {
    @SerializedName("endTime")
    Date endTime;

    @SerializedName("id")
    long id;

    @SerializedName("location")
    String location;

    @SerializedName("startTime")
    Date startTime;

    @SerializedName("title")
    String title;

    public Date getEndTime() {
        return endTime;
    }

    public void setEndTime(Date endTime) {
        this.endTime = endTime;
    }

    public long getId() {
        return id;
    }

    public void setId(long id) {
        this.id = id;
    }

    public String getLocation() {
        return location;
    }

    public void setLocation(String location) {
        this.location = location;
    }

    public Date getStartTime() {
        return startTime;
    }

    public void setStartTime(Date startTime) {
        this.startTime = startTime;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }


    public Topic(Date endTime, long id, String location, Date startTime, String title) {
        this.endTime = endTime;
        this.id = id;
        this.location = location;
        this.startTime = startTime;
        this.title = title;
    }

    protected Topic(Parcel in) {
        long tmpEndTime = in.readLong();
        endTime = tmpEndTime != -1 ? new Date(tmpEndTime) : null;
        id = in.readLong();
        location = in.readString();
        long tmpStartTime = in.readLong();
        startTime = tmpStartTime != -1 ? new Date(tmpStartTime) : null;
        title = in.readString();
    }

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeLong(endTime != null ? endTime.getTime() : -1L);
        dest.writeLong(id);
        dest.writeString(location);
        dest.writeLong(startTime != null ? startTime.getTime() : -1L);
        dest.writeString(title);
    }

    @SuppressWarnings("unused")
    public static final Creator<Topic> CREATOR = new Creator<Topic>() {
        @Override
        public Topic createFromParcel(Parcel in) {
            return new Topic(in);
        }

        @Override
        public Topic[] newArray(int size) {
            return new Topic[size];
        }
    };
}

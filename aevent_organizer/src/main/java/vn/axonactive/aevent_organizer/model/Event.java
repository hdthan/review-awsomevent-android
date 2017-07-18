package vn.axonactive.aevent_organizer.model;

import android.os.Parcel;
import android.os.Parcelable;

import com.google.gson.annotations.SerializedName;

import java.util.Date;

/**
 * Created by dangducnam on 1/10/17.
 */

public class Event implements Parcelable {

    @SerializedName("createDate")
    Date createDate;

    @SerializedName("id")
    long id;

    @SerializedName("imageCover")
    String imageCover;

    @SerializedName("location")
    String location;

    @SerializedName("startDate")
    Date startDate;

    @SerializedName("title")
    String title;

    public Date getCreateDate() {
        return createDate;
    }

    public void setCreateDate(Date createDate) {
        this.createDate = createDate;
    }

    public long getId() {
        return id;
    }

    public void setId(long id) {
        this.id = id;
    }

    public String getImageCover() {
        return imageCover;
    }

    public void setImageCover(String imageCover) {
        this.imageCover = imageCover;
    }

    public String getLocation() {
        return location;
    }

    public void setLocation(String location) {
        this.location = location;
    }

    public Date getStartDate() {
        return startDate;
    }

    public void setStartDate(Date startDate) {
        this.startDate = startDate;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public Event(long id, String imageCover, String location, Date startDate, String title) {
        this.id = id;
        this.imageCover = imageCover;
        this.location = location;
        this.startDate = startDate;
        this.title = title;
    }

    protected Event(Parcel in) {
        id = in.readLong();
        imageCover = in.readString();
        location = in.readString();
        long tmpStartDate = in.readLong();
        startDate = tmpStartDate != -1 ? new Date(tmpStartDate) : null;
        title = in.readString();
    }

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeLong(id);
        dest.writeString(imageCover);
        dest.writeString(location);
        dest.writeLong(startDate != null ? startDate.getTime() : -1L);
        dest.writeString(title);
    }

    @SuppressWarnings("unused")
    public static final Creator<Event> CREATOR = new Creator<Event>() {
        @Override
        public Event createFromParcel(Parcel in) {
            return new Event(in);
        }

        @Override
        public Event[] newArray(int size) {
            return new Event[size];
        }
    };
}

package vn.axonactive.aevent.model;

import android.os.Parcel;
import android.os.Parcelable;

import com.google.gson.annotations.SerializedName;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

/**
 * Created by dangducnam on 1/10/17.
 */

public class Event implements Parcelable {

    @SerializedName("address")
    String address;

    @SerializedName("categories")
    List<Category> categories;

    @SerializedName("createDate")
    Date createDate;

    @SerializedName("description")
    String description;

    @SerializedName("endDate")
    Date endDate;

    @SerializedName("id")
    int id;

    @SerializedName("imageCover")
    String imageCover;

    @SerializedName("latitude")
    float latitude;

    @SerializedName("longitude")
    float longitude;

    @SerializedName("location")
    String location;

    @SerializedName("startDate")
    Date startDate;

    @SerializedName("title")
    String title;

    @SerializedName("topics")
    List<Topic> topics;

    public String getAddress() {
        return address;
    }

    public void setAddress(String address) {
        this.address = address;
    }

    public List<Category> getCategories() {
        return categories;
    }

    public void setCategories(List<Category> categories) {
        this.categories = categories;
    }

    public Date getCreateDate() {
        return createDate;
    }

    public void setCreateDate(Date createDate) {
        this.createDate = createDate;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public Date getEndDate() {
        return endDate;
    }

    public void setEndDate(Date endDate) {
        this.endDate = endDate;
    }

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public String getImageCover() {
        return imageCover;
    }

    public void setImageCover(String imageCover) {
        this.imageCover = imageCover;
    }


    public float getLatitude() {
        return latitude;
    }

    public void setLatitude(float latitude) {
        this.latitude = latitude;
    }

    public float getLongitude() {
        return longitude;
    }

    public void setLongitude(float longitude) {
        this.longitude = longitude;
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

    public List<Topic> getTopics() {
        return topics;
    }

    public void setTopics(List<Topic> topics) {
        this.topics = topics;
    }

    public Event() {
    }

    protected Event(Parcel in) {
        address = in.readString();
        if (in.readByte() == 0x01) {
            categories = new ArrayList<Category>();
            in.readList(categories, Category.class.getClassLoader());
        } else {
            categories = null;
        }
        long tmpCreateDate = in.readLong();
        createDate = tmpCreateDate != -1 ? new Date(tmpCreateDate) : null;
        description = in.readString();
        long tmpEndDate = in.readLong();
        endDate = tmpEndDate != -1 ? new Date(tmpEndDate) : null;
        id = in.readInt();
        imageCover = in.readString();
        latitude = in.readFloat();
        longitude = in.readFloat();
        location = in.readString();
        long tmpStartDate = in.readLong();
        startDate = tmpStartDate != -1 ? new Date(tmpStartDate) : null;
        title = in.readString();
        if (in.readByte() == 0x01) {
            topics = new ArrayList<Topic>();
            in.readList(topics, Topic.class.getClassLoader());
        } else {
            topics = null;
        }
    }

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeString(address);
        if (categories == null) {
            dest.writeByte((byte) (0x00));
        } else {
            dest.writeByte((byte) (0x01));
            dest.writeList(categories);
        }
        dest.writeLong(createDate != null ? createDate.getTime() : -1L);
        dest.writeString(description);
        dest.writeLong(endDate != null ? endDate.getTime() : -1L);
        dest.writeInt(id);
        dest.writeString(imageCover);
        dest.writeFloat(latitude);
        dest.writeFloat(longitude);
        dest.writeString(location);
        dest.writeLong(startDate != null ? startDate.getTime() : -1L);
        dest.writeString(title);
        if (topics == null) {
            dest.writeByte((byte) (0x00));
        } else {
            dest.writeByte((byte) (0x01));
            dest.writeList(topics);
        }
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

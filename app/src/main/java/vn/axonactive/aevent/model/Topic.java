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

public class Topic implements Parcelable {
    @SerializedName("description")
    String description;

    @SerializedName("endTime")
    Date endTime;

    @SerializedName("id")
    int id;

    @SerializedName("location")
    String location;

    @SerializedName("startTime")
    Date startTime;

    @SerializedName("title")
    String title;

    @SerializedName("topicSpeakers")
    List<TopicSpeaker> topicSpeakers;

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public Date getEndTime() {
        return endTime;
    }

    public void setEndTime(Date endTime) {
        this.endTime = endTime;
    }

    public int getId() {
        return id;
    }

    public void setId(int id) {
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

    public List<TopicSpeaker> getTopicSpeakers() {
        return topicSpeakers;
    }

    public void setTopicSpeakers(List<TopicSpeaker> topicSpeakers) {
        this.topicSpeakers = topicSpeakers;
    }

    public Topic(String description, Date endTime, int id, String location, Date startTime, String title, List<TopicSpeaker> topicSpeakers) {
        this.description = description;
        this.endTime = endTime;
        this.id = id;
        this.location = location;
        this.startTime = startTime;
        this.title = title;
        this.topicSpeakers = topicSpeakers;
    }

    protected Topic(Parcel in) {
        description = in.readString();
        long tmpEndTime = in.readLong();
        endTime = tmpEndTime != -1 ? new Date(tmpEndTime) : null;
        id = in.readInt();
        location = in.readString();
        long tmpStartTime = in.readLong();
        startTime = tmpStartTime != -1 ? new Date(tmpStartTime) : null;
        title = in.readString();
        if (in.readByte() == 0x01) {
            topicSpeakers = new ArrayList<TopicSpeaker>();
            in.readList(topicSpeakers, TopicSpeaker.class.getClassLoader());
        } else {
            topicSpeakers = null;
        }
    }

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeString(description);
        dest.writeLong(endTime != null ? endTime.getTime() : -1L);
        dest.writeInt(id);
        dest.writeString(location);
        dest.writeLong(startTime != null ? startTime.getTime() : -1L);
        dest.writeString(title);
        if (topicSpeakers == null) {
            dest.writeByte((byte) (0x00));
        } else {
            dest.writeByte((byte) (0x01));
            dest.writeList(topicSpeakers);
        }
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

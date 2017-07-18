package vn.axonactive.aevent.model;

import android.os.Parcel;
import android.os.Parcelable;

/**
 * Created by ltphuc on 3/7/2017.
 */

public class NotificationModel implements Parcelable{

    private long id;
    private String title;
    private String content;
    private String subTitle;
    private long revTime;
    private int unread;
    private int topicId;
    private int eventId;

    public NotificationModel() {
    }

    public NotificationModel(String title, String content, String subTitle, long revTime, int unread, int topicId, int eventId) {
        this.title = title;
        this.content = content;
        this.subTitle = subTitle;
        this.revTime = revTime;
        this.unread = unread;
        this.topicId = topicId;
        this.eventId = eventId;
    }

    protected NotificationModel(Parcel in) {
        id = in.readLong();
        title = in.readString();
        content = in.readString();
        subTitle = in.readString();
        revTime = in.readLong();
        unread = in.readInt();
        topicId = in.readInt();
        eventId = in.readInt();
    }

    public static final Creator<NotificationModel> CREATOR = new Creator<NotificationModel>() {
        @Override
        public NotificationModel createFromParcel(Parcel in) {
            return new NotificationModel(in);
        }

        @Override
        public NotificationModel[] newArray(int size) {
            return new NotificationModel[size];
        }
    };

    public String getTitle() {
        return title;
    }

    public String getContent() {
        return content;
    }

    public String getSubTitle() {
        return subTitle;
    }

    public long getRevTime() {
        return revTime;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public void setContent(String content) {
        this.content = content;
    }

    public void setSubTitle(String subTitle) {
        this.subTitle = subTitle;
    }

    public void setRevTime(long revTime) {
        this.revTime = revTime;
    }

    public long getId() {
        return id;
    }

    public void setId(long id) {
        this.id = id;
    }

    public int getUnread() {
        return unread;
    }

    public void setUnread(int unread) {
        this.unread = unread;
    }

    public int getTopicId() {
        return topicId;
    }

    public void setTopicId(int topicId) {
        this.topicId = topicId;
    }

    public int getEventId() {
        return eventId;
    }

    public void setEventId(int eventId) {
        this.eventId = eventId;
    }

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeLong(id);
        dest.writeString(title);
        dest.writeString(content);
        dest.writeString(subTitle);
        dest.writeLong(revTime);
        dest.writeInt(unread);
        dest.writeInt(topicId);
        dest.writeInt(eventId);
    }
}

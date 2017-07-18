package vn.axonactive.aevent_organizer.model;

import android.os.Parcel;
import android.os.Parcelable;

import java.util.Date;

/**
 * Created by ltphuc on 3/14/2017.
 */

public class Notification implements Parcelable {

    private long id;
    private String title;
    private String content;
    private Date sendDate;

    public Notification(long id, String title, String content, Date sendDate) {
        this.id = id;
        this.title = title;
        this.content = content;
        this.sendDate = sendDate;
    }

    public Notification(String title, String content, Date sendDate) {
        this.title = title;
        this.content = content;
        this.sendDate = sendDate;
    }

    public Notification() {

    }

    protected Notification(Parcel in) {
        id = in.readLong();
        title = in.readString();
        content = in.readString();

        long temp = in.readLong();

        sendDate = new Date(temp);
    }

    public static final Creator<Notification> CREATOR = new Creator<Notification>() {
        @Override
        public Notification createFromParcel(Parcel in) {
            return new Notification(in);
        }

        @Override
        public Notification[] newArray(int size) {
            return new Notification[size];
        }
    };

    public long getId() {
        return id;
    }

    public String getTitle() {
        return title;
    }

    public String getContent() {
        return content;
    }

    public Date getSendDate() {
        return sendDate;
    }

    public void setSendDate(Date sendTime) {
        this.sendDate = sendTime;
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
        dest.writeLong(sendDate != null ? sendDate.getTime() : -1L);
    }
}

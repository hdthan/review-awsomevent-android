package vn.axonactive.aevent_organizer.model;

import android.os.Parcel;
import android.os.Parcelable;

import java.util.Date;

/**
 * Created by ltphuc on 3/20/2017.
 */

public class Feedback implements Parcelable {

    private long id;
    private String content;
    private Date sendDate;

    public String getContent() {
        return content;
    }

    public void setContent(String content) {
        this.content = content;
    }

    public Date getSendDate() {
        return sendDate;
    }

    public void setSendDate(Date sendDate) {
        this.sendDate = sendDate;
    }

    public Feedback(String content) {
        this.content = content;
    }

    public Feedback(String content, Date sendDate) {
        this.content = content;
        this.sendDate = sendDate;
    }

    public Feedback(long id, String content, Date sendDate) {
        this.id = id;
        this.content = content;
        this.sendDate = sendDate;
    }

    protected Feedback(Parcel in) {
        id = in.readLong();
        content = in.readString();
        long tmp = in.readLong();
        if (tmp != -1) {
            sendDate = new Date(tmp);
        } else {
            sendDate = null;
        }
    }

    public static final Creator<Feedback> CREATOR = new Creator<Feedback>() {
        @Override
        public Feedback createFromParcel(Parcel in) {
            return new Feedback(in);
        }

        @Override
        public Feedback[] newArray(int size) {
            return new Feedback[size];
        }
    };

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeLong(id);
        dest.writeString(content);
        dest.writeLong(sendDate.getTime());
    }
}

package vn.axonactive.aevent.model;

import android.os.Parcel;
import android.os.Parcelable;

import com.google.gson.annotations.SerializedName;

import java.util.Date;

/**
 * Created by dlong on 1/17/2017.
 */

public class Enrollment implements Parcelable {

    @SerializedName("event")
    private Event event;

    @SerializedName("enrollDate")
    private Date enrollDate;

    @SerializedName("authorizationCode")
    private String authorizationCode;


    public Enrollment(Event event, Date enrollDate, String authorizationCode) {
        this.event = event;
        this.enrollDate = enrollDate;
        this.authorizationCode = authorizationCode;
    }

    public Enrollment() {

    }


    protected Enrollment(Parcel in) {
        event = in.readParcelable(Event.class.getClassLoader());
        authorizationCode = in.readString();
    }

    public static final Creator<Enrollment> CREATOR = new Creator<Enrollment>() {
        @Override
        public Enrollment createFromParcel(Parcel in) {
            return new Enrollment(in);
        }

        @Override
        public Enrollment[] newArray(int size) {
            return new Enrollment[size];
        }
    };

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeParcelable(event, flags);
        dest.writeString(authorizationCode);
    }

    public Event getEvent() {
        return event;
    }

    public Date getEnrollDate() {
        return enrollDate;
    }

    public String getAuthorizationCode() {
        return authorizationCode;
    }

    public void setEvent(Event event) {
        this.event = event;
    }

    public void setEnrollDate(Date enrollDate) {
        this.enrollDate = enrollDate;
    }

    public void setAuthorizationCode(String authorizationCode) {
        this.authorizationCode = authorizationCode;
    }
}

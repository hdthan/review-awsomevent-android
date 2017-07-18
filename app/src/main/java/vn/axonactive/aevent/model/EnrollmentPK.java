package vn.axonactive.aevent.model;

import android.os.Parcel;
import android.os.Parcelable;

import com.google.gson.annotations.SerializedName;

/**
 * Created by dlong on 1/17/2017.
 */

public class EnrollmentPK implements Parcelable {
    @SerializedName("event")
    private Long event;
    @SerializedName("user")
    private Long user;

    public EnrollmentPK(Long event, Long user) {
        this.event = event;
        this.user = user;
    }

    protected EnrollmentPK(Parcel in) {
    }

    public static final Creator<EnrollmentPK> CREATOR = new Creator<EnrollmentPK>() {
        @Override
        public EnrollmentPK createFromParcel(Parcel in) {
            return new EnrollmentPK(in);
        }

        @Override
        public EnrollmentPK[] newArray(int size) {
            return new EnrollmentPK[size];
        }
    };

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
    }
}

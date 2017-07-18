package vn.axonactive.aevent_organizer.model;
import android.os.Parcel;
import android.os.Parcelable;
import com.google.gson.annotations.SerializedName;
/**
 * Created by dtnhat on 1/17/2017.
 */
public class Enrollment implements Parcelable{
    @SerializedName("authorizationCode")
    String authorizationCode;
    @SerializedName("checkIn")
    int checkIn;
    @SerializedName("confirm")
    int confirm;
    @SerializedName("user")
    User user;
    public Enrollment(){}
    public Enrollment(String authorizationCode, int checkIn, int confirm, User user) {
        this.authorizationCode = authorizationCode;
        this.checkIn = checkIn;
        this.confirm = confirm;
        this.user = user;
    }
    public String getAuthorizationCode() {
        return authorizationCode;
    }
    public void setAuthorizationCode(String authorizationCode) {
        this.authorizationCode = authorizationCode;
    }
    public int getCheckIn() {
        return checkIn;
    }
    public void setCheckIn(int checkIn) {
        this.checkIn = checkIn;
    }
    public int getConfirm() {
        return confirm;
    }
    public void setConfirm(int confirm) {
        this.confirm = confirm;
    }
    public User getUser() {
        return user;
    }
    public void setUser(User user) {
        this.user = user;
    }
    protected  Enrollment(Parcel in){
        authorizationCode = in.readString();
        checkIn = in.readInt();
        confirm = in.readInt();
        user = (User)in.readValue(User.class.getClassLoader());
    }
    @Override
    public int describeContents() {
        return 0;
    }
    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeString(authorizationCode);
        dest.writeInt(checkIn);
        dest.writeInt(confirm);
        dest.writeValue(user);
    }
    @SuppressWarnings("unused")
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
}
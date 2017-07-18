package vn.axonactive.aevent.model;

import android.os.Parcel;
import android.os.Parcelable;

/**
 * Created by ltphuc on 3/20/2017.
 */

public class UserAccount implements Parcelable {

    private String fullName;
    private String email;
    private String password;
    private String rePassword;

    public UserAccount() {

    }

    public UserAccount(String fullName, String email, String password, String rePassword) {
        this.fullName = fullName;
        this.email = email;
        this.password = password;
        this.rePassword = rePassword;
    }


    protected UserAccount(Parcel in) {
        fullName = in.readString();
        email = in.readString();
        password = in.readString();
        rePassword = in.readString();
    }

    public static final Creator<UserAccount> CREATOR = new Creator<UserAccount>() {
        @Override
        public UserAccount createFromParcel(Parcel in) {
            return new UserAccount(in);
        }

        @Override
        public UserAccount[] newArray(int size) {
            return new UserAccount[size];
        }
    };

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeString(fullName);
        dest.writeString(email);
        dest.writeString(password);
        dest.writeString(rePassword);
    }
}

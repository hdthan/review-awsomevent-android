package vn.axonactive.aevent_organizer.model;

import com.google.gson.annotations.SerializedName;

/**
 * Created by dangducnam on 1/6/17.
 */

public class Login {

    @SerializedName("email")
    String mEmail;

    @SerializedName("password")
    String mPassword;

    public Login(String email, String password) {
        this.mEmail = email;
        this.mPassword = password;
    }
}

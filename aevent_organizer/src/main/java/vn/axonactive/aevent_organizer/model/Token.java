package vn.axonactive.aevent_organizer.model;

import com.google.gson.annotations.SerializedName;

/**
 * Created by dangducnam on 1/6/17.
 */

public class Token {
    public String getToken() {
        return mToken;
    }

    @SerializedName("token")
    String mToken;

    public Token(String token) {
        this.mToken = token;
    }
}

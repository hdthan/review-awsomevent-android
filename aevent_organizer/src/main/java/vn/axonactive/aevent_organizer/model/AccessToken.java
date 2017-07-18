package vn.axonactive.aevent_organizer.model;

import com.google.gson.annotations.SerializedName;

/**
 * Created by dangducnam on 1/10/17.
 */

public class AccessToken {

    @SerializedName("accessToken")
    String mToken;

    public AccessToken(String token) {
        this.mToken = token;
    }
}

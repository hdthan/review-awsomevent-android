package vn.axonactive.aevent.service;

import com.google.firebase.iid.FirebaseInstanceId;
import com.google.firebase.iid.FirebaseInstanceIdService;

/**
 * Created by ltphuc on 24/02/2017.
 */

public class MyFirebaseInstanceIDService extends FirebaseInstanceIdService {

    @Override
    public void onTokenRefresh() {
        super.onTokenRefresh();
        String token = FirebaseInstanceId.getInstance().getToken();
        sendTokenToServer(token);
    }

    private void sendTokenToServer(String token) {
        new FireBaseIDTask().execute(token);
    }

}

package vn.axonactive.aevent_organizer.listener;

/**
 * Created by ltphuc on 2/10/2017.
 */

public interface OnReceivedDataListener {

    void onSuccess(Object data);
    void onFailure();
    void onStart();

}

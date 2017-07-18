package vn.axonactive.aevent_organizer.api;

import java.util.List;

import okhttp3.ResponseBody;
import retrofit2.Call;
import retrofit2.http.DELETE;
import retrofit2.http.GET;
import retrofit2.http.Header;
import retrofit2.http.Path;
import vn.axonactive.aevent_organizer.model.Notification;

/**
 * Created by ltphuc on 3/14/2017.
 */

public interface NotificationEndpointInterface {

    @GET("/api/notification/event/{id}")
    Call<List<Notification>> getNotificationsByEventId(@Header("Authorization") String token, @Path("id") long id);

    @DELETE("/api/notification/event/{id}")
    Call<ResponseBody> clearNotificationByEventId(@Header("Authorization") String token, @Path("id") long id);
    
    @DELETE("api/notification/{id}")
    Call<ResponseBody> deleteNotificationById(@Header("Authorization") String token, @Path("id") long id);

}

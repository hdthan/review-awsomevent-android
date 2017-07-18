package vn.axonactive.aevent_organizer.api;

import java.util.List;

import okhttp3.RequestBody;
import okhttp3.ResponseBody;
import retrofit2.Call;
import retrofit2.http.Body;
import retrofit2.http.GET;
import retrofit2.http.Header;
import retrofit2.http.POST;
import retrofit2.http.Path;
import vn.axonactive.aevent_organizer.model.AccessToken;
import vn.axonactive.aevent_organizer.model.Enrollment;
import vn.axonactive.aevent_organizer.model.Event;
import vn.axonactive.aevent_organizer.model.EventExpand;
import vn.axonactive.aevent_organizer.model.Login;
import vn.axonactive.aevent_organizer.model.Participant;
import vn.axonactive.aevent_organizer.model.Token;
import vn.axonactive.aevent_organizer.model.User;

/**
 * Created by dangducnam on 1/6/17.
 */

public interface EventEndpointInterface {

    @POST("/api/users")
    Call<Token> createUser(@Body User user);

    @POST("/auth/login")
    Call<Token> login(@Body Login login);

    @POST("/api/social/fb")
    Call<Token> loginFacebook(@Body AccessToken accessToken);

    @GET("/api/event/live/both/{num}")
    Call<List<EventExpand>> getMyLiveEvents(@Header("Authorization") String token, @Path("num") int num);

    @GET("/api/event/pass/both/{num}")
    Call<List<EventExpand>> getMyPassEvents(@Header("Authorization") String token, @Path("num") int num);

    @POST("/api/tickets/checkIn")
    Call<User> sendQRCodeToCheckIn(@Header("Authorization") String token, @Body RequestBody body);

    @POST("/api/event/quickForm/{eventId}")
    Call<ResponseBody> addParticipant(@Header("Authorization") String token, @Path("eventId") long eventId, @Body RequestBody user);

    @GET("/api/enrollments/{eventId}/list-participants")
    Call<List<Enrollment>> getListParticipant(@Path("eventId") long eventId, @Header("Authorization") String token);

}

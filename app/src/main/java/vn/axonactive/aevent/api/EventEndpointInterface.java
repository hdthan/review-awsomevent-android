package vn.axonactive.aevent.api;

import java.util.List;

import okhttp3.ResponseBody;
import retrofit2.Call;
import retrofit2.http.GET;
import retrofit2.http.Header;
import retrofit2.http.Path;
import vn.axonactive.aevent.model.Event;
import vn.axonactive.aevent.model.Speaker;
import vn.axonactive.aevent.model.Sponsor;
import vn.axonactive.aevent.model.Topic;

/**
 * Created by dangducnam on 1/6/17.
 */

public interface EventEndpointInterface {

    @GET("/api/event/upcoming/{num}")
    Call<List<Event>> getEvents(@Path("num") int num);

    @GET("/api/event/{id}/description")
    Call<Event> getEvent(@Path("id") int id);

    @GET("/api/event/{id}/topic")
    Call<List<Topic>> getTopic(@Path("id") int id);

    @GET("/api/event/{id}/speaker")
    Call<List<Speaker>> getSpeaker(@Path("id") int id);

    @GET("/api/event/{id}/sponsor")
    Call<List<Sponsor>> getSponsor(@Path("id") int id);

    @GET("/api/event/{id}/check-join")
    Call<ResponseBody> checkJoin(@Header("Authorization") String token, @Path("id") int id);

    @GET("/api/event/{lat}/{lng}/{num}")
    Call<List<Event>> getNearByEvent(@Path("lat") double lat, @Path("lng") double lng, @Path("num") int num);

    @GET("/api/event/id")
    Call<List<Long>> getListOfEventId(@Header("Authorization") String token);
}

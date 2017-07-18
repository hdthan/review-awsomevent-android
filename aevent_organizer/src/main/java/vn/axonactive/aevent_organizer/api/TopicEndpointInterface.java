package vn.axonactive.aevent_organizer.api;

import java.util.List;

import retrofit2.Call;
import retrofit2.http.GET;
import retrofit2.http.Header;
import retrofit2.http.Path;
import vn.axonactive.aevent_organizer.model.Topic;

/**
 * Created by ltphuc on 2/27/2017.
 */

public interface TopicEndpointInterface {

    @GET("/api/topics/events/order/{id}")
    Call<List<Topic>> getTopicsByEventId(@Header("Authorization") String token, @Path("id") long id);

}

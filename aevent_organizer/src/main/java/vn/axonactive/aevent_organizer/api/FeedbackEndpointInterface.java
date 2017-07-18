package vn.axonactive.aevent_organizer.api;

import java.util.List;

import retrofit2.Call;
import retrofit2.http.Body;
import retrofit2.http.GET;
import retrofit2.http.Header;
import retrofit2.http.POST;
import retrofit2.http.Path;
import vn.axonactive.aevent_organizer.model.Feedback;

/**
 * Created by ltphuc on 3/20/2017.
 */

public interface FeedbackEndpointInterface {

    @POST("/api/feedback")
    Call<Feedback> create(@Header("Authorization") String token, @Body Feedback feedback);

    @GET("/api/feedback/{num}")
    Call<List<Feedback>> getFeedback(@Header("Authorization") String token, @Path("num") int num);
}

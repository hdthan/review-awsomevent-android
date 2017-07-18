package vn.axonactive.aevent_organizer.api;

import okhttp3.RequestBody;
import okhttp3.ResponseBody;
import retrofit2.Call;
import retrofit2.http.Body;
import retrofit2.http.Header;
import retrofit2.http.POST;

/**
 * Created by Dell on 2/12/2017.
 */

public interface EnrollmentEndpointInterface {

    @POST("/api/enrollments/send-broadcast")
    Call<ResponseBody> sendReminder(@Header("Authorization") String token,@Body RequestBody body);

    @POST("/api/enrollments/send-message")
    Call<ResponseBody> sendMessage(@Header("Authorization") String token,@Body RequestBody body);

}

package vn.axonactive.aevent.api;

import java.util.List;
import java.util.Map;

import okhttp3.ResponseBody;
import retrofit2.Call;
import retrofit2.http.Body;
import retrofit2.http.FieldMap;
import retrofit2.http.FormUrlEncoded;
import retrofit2.http.GET;
import retrofit2.http.Header;
import retrofit2.http.POST;
import retrofit2.http.Path;
import vn.axonactive.aevent.model.Enrollment;
import vn.axonactive.aevent.model.EnrollmentPK;

/**
 * Created by Dell on 2/12/2017.
 */

public interface EnrollmentEndpointInterface {

    @GET("/api/enrollments/{id}/qrcode")
    Call<ResponseBody> getQRCode(@Header("Authorization") String token, @Path("id") int id);

    @POST("api/enrollments/join")
    Call<Enrollment> becomeParticipant(@Header("Authorization") String token, @Body EnrollmentPK enrollmentPK);

    @GET("api/enrollments/up-coming/{num}")
    Call<List<Enrollment>> getUpcomingTicket(@Header("Authorization") String token, @Path("num") int num);

    @GET("api/enrollments/passing/{num}")
    Call<List<Enrollment>> getPassingTicket(@Header("Authorization") String token, @Path("num") int num);

}

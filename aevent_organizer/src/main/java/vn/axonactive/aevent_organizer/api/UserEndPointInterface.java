package vn.axonactive.aevent_organizer.api;

import retrofit2.Call;
import retrofit2.http.Body;
import retrofit2.http.GET;
import retrofit2.http.Header;
import retrofit2.http.POST;
import retrofit2.http.PUT;
import vn.axonactive.aevent_organizer.model.AccessToken;
import vn.axonactive.aevent_organizer.model.Login;
import vn.axonactive.aevent_organizer.model.Token;
import vn.axonactive.aevent_organizer.model.User;

import static com.facebook.internal.CallbackManagerImpl.RequestCodeOffset.Login;

/**
 * Created by dlong on 1/18/2017.
 */

public interface UserEndPointInterface {

    @POST("/api/users")
    Call<Token> createUser(@Body User user);

    @POST("/auth/login")
    Call<Token> login(@Body Login login);

    @POST("/api/social/fb")
    Call<Token> loginFacebook(@Body AccessToken accessToken);

    @POST("/api/social/gg")
    Call<Token> loginGooglePlus(@Body AccessToken accessToken);

    @GET("api/users/info")
    Call<User> getUser(@Header("Authorization") String token);

    @PUT("/api/users/update/info")
    Call<User> updateUser(@Header("Authorization") String token, @Body User user);

    @PUT("api/users/update-join")
    Call<User> updateUserBeforeJoin(@Header("Authorization") String token, @Body User user);

}

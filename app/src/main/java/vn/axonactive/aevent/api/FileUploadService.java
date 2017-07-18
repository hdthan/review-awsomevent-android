package vn.axonactive.aevent.api;

import okhttp3.MultipartBody;
import okhttp3.RequestBody;
import okhttp3.ResponseBody;
import retrofit2.Call;
import retrofit2.http.Header;
import retrofit2.http.Multipart;
import retrofit2.http.PUT;
import retrofit2.http.Part;

/**
 * Created by ltphuc on 2/20/2017.
 */

public interface FileUploadService {

    @Multipart
    @PUT("/api/users/update/avatar")
    Call<ResponseBody> upload(@Header("Authorization") String token, @Part MultipartBody.Part file, @Part("name") RequestBody name);

}

package vn.axonactive.aevent_organizer.activity;

import android.Manifest;
import android.app.Activity;
import android.content.Context;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.media.AudioManager;
import android.media.ToneGenerator;
import android.os.Bundle;
import android.os.Handler;
import android.support.v4.app.ActivityCompat;
import android.util.DisplayMetrics;
import android.util.Log;
import android.util.SparseArray;
import android.view.SurfaceHolder;
import android.view.SurfaceView;
import android.widget.Toast;

import com.google.android.gms.vision.CameraSource;
import com.google.android.gms.vision.Detector;
import com.google.android.gms.vision.barcode.Barcode;
import com.google.android.gms.vision.barcode.BarcodeDetector;

import java.io.IOException;
import java.net.HttpURLConnection;

import es.dmoral.toasty.Toasty;
import okhttp3.MediaType;
import okhttp3.RequestBody;
import okhttp3.ResponseBody;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;
import retrofit2.Retrofit;
import vn.axonactive.aevent_organizer.R;
import vn.axonactive.aevent_organizer.api.EventEndpointInterface;
import vn.axonactive.aevent_organizer.api.RestfulAPI;
import vn.axonactive.aevent_organizer.model.User;
import vn.axonactive.aevent_organizer.util.DataStorage;

/**
 * Created by Dell on 4/4/2017.
 */

public class ScanQrCodeActivity extends Activity {

    private BarcodeDetector barcodeDetector;
    private CameraSource cameraSource;
    private SurfaceView cameraView;

    private String preQrCode = "";
    private long eventId;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        eventId = getIntent().getExtras().getLong("eventId");

        setContentView(R.layout.activity_scan_qr_code);

        cameraView = (SurfaceView) findViewById(R.id.camera_view);

        barcodeDetector =
                new BarcodeDetector.Builder(this)
                        .setBarcodeFormats(Barcode.QR_CODE)
                        .build();

        DisplayMetrics displayMetrics = new DisplayMetrics();
        getWindowManager().getDefaultDisplay().getMetrics(displayMetrics);
        int height = displayMetrics.heightPixels;
        int width = displayMetrics.widthPixels;

        cameraSource = new CameraSource
                .Builder(this, barcodeDetector)
                .setRequestedPreviewSize(width, height)
                .build();

        cameraView.getHolder().addCallback(new SurfaceHolder.Callback() {
            @Override
            public void surfaceCreated(SurfaceHolder holder) {
                try {
                    if (ActivityCompat.checkSelfPermission(ScanQrCodeActivity.this, Manifest.permission.CAMERA) != PackageManager.PERMISSION_GRANTED) {
                        Toasty.normal(ScanQrCodeActivity.this, "Camera permissions is not granted").show();
                        return;
                    }
                    cameraSource.start(cameraView.getHolder());
                } catch (IOException ie) {
                    Log.e("CAMERA SOURCE", ie.getMessage());
                }
            }

            @Override
            public void surfaceChanged(SurfaceHolder holder, int format, int width, int height) {
            }

            @Override
            public void surfaceDestroyed(SurfaceHolder holder) {
                cameraSource.stop();
            }
        });

        barcodeDetector.setProcessor(new Detector.Processor<Barcode>() {
            @Override
            public void release() {
            }

            @Override
            public void receiveDetections(Detector.Detections<Barcode> detections) {
                final SparseArray<Barcode> barcodes = detections.getDetectedItems();

                if (barcodes.size() != 0) {

                    cameraView.post(new Runnable() {
                        @Override
                        public void run() {

                            String qrCode = barcodes.valueAt(0).displayValue;

                            if (!qrCode.equals(preQrCode)) {
                                sendQrCode(qrCode);
                                preQrCode = qrCode;
                            }
                        }
                    });

                }
            }
        });

    }

    private void sendQrCode(String qrCode) {

        ToneGenerator toneGenerator = new ToneGenerator(AudioManager.STREAM_MUSIC, 100);
        toneGenerator.startTone(ToneGenerator.TONE_PROP_BEEP,100);

        RestfulAPI restfulAPI = new RestfulAPI();

        SharedPreferences preferences = getSharedPreferences(DataStorage.APP_PREFS, Context.MODE_PRIVATE);
        String token = preferences.getString(DataStorage.TOKEN, null);

        Retrofit retrofit = restfulAPI.getRestClient();
        final EventEndpointInterface apiService = retrofit.create(EventEndpointInterface.class);

        String body = String.format("{\"code\": \"%s\", \"eventId\": \"%s\"}", qrCode, String.valueOf(eventId));

        final RequestBody requestBody = RequestBody.create(MediaType.parse("application/json"), body);

        Call<User> call = apiService.sendQRCodeToCheckIn(token, requestBody);
        call.enqueue(new Callback<User>() {
            @Override
            public void onResponse(Call<User> call, Response<User> response) {
                int statusCode = response.code();

                User user = response.body();

                if (statusCode == HttpURLConnection.HTTP_OK) {
                    Toasty.success(ScanQrCodeActivity.this, "Successfully checked-in", Toast.LENGTH_SHORT, true).show();
                    Toasty.info(ScanQrCodeActivity.this, "Participant'name: " + user.getFullName(), Toast.LENGTH_SHORT, false).show();
                } else if (statusCode == HttpURLConnection.HTTP_NOT_FOUND) {
                    Toasty.error(ScanQrCodeActivity.this, "Ticket is not correct, try again", Toast.LENGTH_LONG, true).show();
                } else if (statusCode == HttpURLConnection.HTTP_ACCEPTED) {
                    Toasty.info(ScanQrCodeActivity.this, "User have already checked-in", Toast.LENGTH_SHORT, true).show();
                    Toasty.info(ScanQrCodeActivity.this, "Participant'name: " + user.getFullName(), Toast.LENGTH_SHORT, false).show();
                } else {
                    Toasty.info(ScanQrCodeActivity.this, "Something went wrong, try again", Toast.LENGTH_LONG, true).show();
                }
            }

            @Override
            public void onFailure(Call<User> call, Throwable t) {
                Toasty.info(ScanQrCodeActivity.this, getString(R.string.err_network_connection), Toast.LENGTH_LONG, true).show();
            }
        });

    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        cameraSource.release();
        barcodeDetector.release();
    }

}

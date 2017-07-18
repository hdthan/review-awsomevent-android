package vn.axonactive.aevent.activity;

import android.graphics.Bitmap;
import android.os.Bundle;
import android.support.v7.widget.Toolbar;
import android.widget.ImageView;
import android.widget.TextView;

import com.google.zxing.BarcodeFormat;
import com.google.zxing.WriterException;
import com.squareup.picasso.Picasso;

import vn.axonactive.aevent.BuildConfig;
import vn.axonactive.aevent.R;
import vn.axonactive.aevent.api.RestfulAPI;
import vn.axonactive.aevent.util.DataStorage;
import vn.axonactive.aevent.util.QrCodeUtil;

public class TicketActivity extends BaseActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.activity_ticket);

        final Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);

        ImageView ivQrCode = (ImageView) this.findViewById(R.id.qr_code);
        TextView tvEmail = (TextView) this.findViewById(R.id.email);
        TextView tvAuthCode = (TextView) this.findViewById(R.id.auth_code);

        Bundle bundle = this.getIntent().getExtras();

        String url = bundle.getString("url");
        setTitle(bundle.getString("title"));

        try {
            Bitmap bmQrCode = QrCodeUtil.encodeAsBitmap(url, BarcodeFormat.QR_CODE, 500, 500);
            ivQrCode.setImageBitmap(bmQrCode);
        } catch (WriterException e) {
            e.printStackTrace();
        }

        tvAuthCode.setText("Authentication Code: " + url);
        tvEmail.setText("Email: " + DataStorage.email);
    }

}

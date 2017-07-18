package vn.axonactive.aevent.activity;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.os.Handler;
import android.support.v7.app.AppCompatActivity;

import jonathanfinerty.once.Once;
import vn.axonactive.aevent.R;
import vn.axonactive.aevent.util.BadgeUtil;
import vn.axonactive.aevent.util.DataStorage;

public class SplashActivity extends AppCompatActivity {

    private static final int SPLASH_TIME_OUT = 2000;
    private boolean firstLaunch;
    private String token;

    @Override
    protected void onCreate(Bundle savedInstanceState) {

        super.onCreate(savedInstanceState);
        Once.initialise(this);

        setContentView(R.layout.activity_splash);

        SharedPreferences prefs = getSharedPreferences(DataStorage.APP_PREFS, MODE_PRIVATE);

        firstLaunch = prefs.getBoolean(DataStorage.FIRST_LAUNCH, true);
        token = prefs.getString(DataStorage.TOKEN, null);

        if (firstLaunch) {
            BadgeUtil.setBadge(this, 0);

            new Handler().postDelayed(new Runnable() {
                @Override
                public void run() {
                    Intent intent = new Intent(SplashActivity.this, OnBoardingTutorial.class);
                    intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_NEW_TASK);
                    startActivity(intent);
                    finish();
                }
            }, SPLASH_TIME_OUT);

        } else {
            if (token == null) {
                Intent intent = new Intent(SplashActivity.this, BoardingActivity.class);
                intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_NEW_TASK);
                startActivity(intent);
                finish();
            } else {
                Intent intent = new Intent(SplashActivity.this, MainActivity.class);
                intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_NEW_TASK);
                startActivity(intent);
                finish();
            }
        }
    }
}

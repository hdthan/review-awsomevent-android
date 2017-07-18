package vn.axonactive.aevent.activity;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.util.DisplayMetrics;

import com.codemybrainsout.onboarder.AhoyOnboarderActivity;
import com.codemybrainsout.onboarder.AhoyOnboarderCard;

import java.util.ArrayList;
import java.util.List;

import vn.axonactive.aevent.R;
import vn.axonactive.aevent.util.DataStorage;


public class OnBoardingTutorial extends AhoyOnboarderActivity {

    List<AhoyOnboarderCard> pages;

    @Override

    protected void onCreate(Bundle savedInstanceState) {

        super.onCreate(savedInstanceState);
        pages = new ArrayList<>();

        setColorBackground(R.color.colorPrimary);

        initOnBoardingCards();
        setOnboardPages(pages);
        setFinishButtonTitle("Get Started");
    }


    void initOnBoardingCards() {
        DisplayMetrics displayMetrics = new DisplayMetrics();
        getWindowManager().getDefaultDisplay().getMetrics(displayMetrics);
        int height = displayMetrics.heightPixels;

        AhoyOnboarderCard cardOne = new AhoyOnboarderCard("Keep track",
                "View topic schedules and      upcoming events",
                R.drawable.onboarding);

        cardOne.setBackgroundColor(R.color.white);
        cardOne.setTitleColor(R.color.black);
        cardOne.setDescriptionColor(R.color.black);
        cardOne.setTitleTextSize(dpToPixels(9, this));
        cardOne.setDescriptionTextSize(dpToPixels(6, this));
        cardOne.setIconLayoutParams(height / 4, height / 4, height / 16, 0, 0,height / 16);

        AhoyOnboarderCard cardTwo = new AhoyOnboarderCard("Check in",
                "Use the QR code on your ticket to check into the event",
                R.drawable.onboarding2);

        cardTwo.setBackgroundColor(R.color.white);
        cardTwo.setTitleColor(R.color.black);
        cardTwo.setDescriptionColor(R.color.black);
        cardTwo.setTitleTextSize(dpToPixels(9, this));
        cardTwo.setDescriptionTextSize(dpToPixels(6, this));
        cardTwo.setIconLayoutParams(height / 4, height / 4, height / 16, 0, 0, height / 16);

        AhoyOnboarderCard cardThree = new AhoyOnboarderCard("Communicate",
                "Receive push notifications from organizers",
                R.drawable.onboarding3);

        cardThree.setBackgroundColor(R.color.white);
        cardThree.setTitleColor(R.color.black);
        cardThree.setDescriptionColor(R.color.black);
        cardThree.setTitleTextSize(dpToPixels(9, this));
        cardThree.setDescriptionTextSize(dpToPixels(6, this));
        cardThree.setIconLayoutParams(height / 4, height / 4,height / 16, 0, 0, height / 16);

        pages.add(cardOne);
        pages.add(cardTwo);
        pages.add(cardThree);
    }

    @Override
    public void onFinishButtonPressed() {

        SharedPreferences.Editor editor = getSharedPreferences(DataStorage.APP_PREFS, MODE_PRIVATE).edit();
        editor.putBoolean(DataStorage.FIRST_LAUNCH, false);
        editor.apply();

        Intent onBoardingActivity = new Intent(this, BoardingActivity.class);
        startActivity(onBoardingActivity);
        finish();
    }
}


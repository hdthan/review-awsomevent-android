package vn.axonactive.aevent_organizer.activity;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.util.DisplayMetrics;

import com.codemybrainsout.onboarder.AhoyOnboarderActivity;
import com.codemybrainsout.onboarder.AhoyOnboarderCard;

import java.util.ArrayList;
import java.util.List;

import vn.axonactive.aevent_organizer.R;
import vn.axonactive.aevent_organizer.util.DataStorage;


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
                "Schedule topics and manage participants ",
                R.drawable.onboarding);
        cardOne.setBackgroundColor(R.color.white);
        cardOne.setTitleColor(R.color.black);
        cardOne.setDescriptionColor(R.color.black);
        cardOne.setTitleTextSize(dpToPixels(9, this));
        cardOne.setDescriptionTextSize(dpToPixels(6, this));
        cardOne.setIconLayoutParams(height / 4, height / 4, height / 8, 0, 0, 0);


        AhoyOnboarderCard cardTwo = new AhoyOnboarderCard("Check in",
                "Check in guests using the QR code on their ticket",
                R.drawable.onboarding2);
        cardTwo.setBackgroundColor(R.color.white);
        cardTwo.setTitleColor(R.color.black);
        cardTwo.setDescriptionColor(R.color.black);
        cardTwo.setTitleTextSize(dpToPixels(9, this));
        cardTwo.setDescriptionTextSize(dpToPixels(6, this));
        cardTwo.setIconLayoutParams(height / 4, height / 4, height / 8, 0, 0, 0);

        AhoyOnboarderCard cardThree = new AhoyOnboarderCard("Communicate",
                "Send push notifications for schedule changes and announcements",
                R.drawable.onboarding3);
        cardThree.setBackgroundColor(R.color.white);
        cardThree.setTitleColor(R.color.black);
        cardThree.setDescriptionColor(R.color.black);
        cardThree.setTitleTextSize(dpToPixels(9, this));
        cardThree.setDescriptionTextSize(dpToPixels(6, this));
        cardThree.setIconLayoutParams(height / 4, height / 4, height / 8, 0, 0, 0);

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

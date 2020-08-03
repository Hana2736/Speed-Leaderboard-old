package dev.stickbit.speed;

import android.content.Intent;
import android.os.Bundle;
import android.provider.Settings;
import android.view.View;
import android.widget.TextView;
import androidx.appcompat.app.AppCompatActivity;
import com.google.android.material.snackbar.Snackbar;

import java.util.Map;

public class HomePage extends AppCompatActivity {
    public static Map<String, Integer> records;
    public static Map<String, Integer> oldRecords;
    public static Map<String, String[]> tTrials;
    public static Map<String, Double> myTTrialTimes;
    public static String name;
    public static String league;
    int ready = 0;
    public static String showSaveMessage;


    public void ready() {
        ready++;
        if (ready == 3) {
            findViewById(R.id.driveButton).setEnabled(true);
            findViewById(R.id.statsButton).setEnabled(true);
            findViewById(R.id.tTrialsBtn).setEnabled(true);
            findViewById(R.id.createTTrialBtn).setEnabled(true);
            ready = 0;

            ((TextView) findViewById(R.id.wbMessage)).setText((getText(R.string.wb).toString().replace("%%", name).replace("$$", league)));

        }
    }

    @Override
    public void onBackPressed() {
        Intent intent = new Intent(Intent.ACTION_MAIN);
        intent.addCategory(Intent.CATEGORY_HOME);
        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_EXCLUDE_FROM_RECENTS);
        startActivity(intent);
        finish();
        System.exit(0);
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        if (getSupportActionBar() != null) {
            getSupportActionBar().hide();
        }
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_home_page);
        if (showSaveMessage != null) {
            Snackbar.make(findViewById(R.id.wbMessage).getRootView(), showSaveMessage, Snackbar.LENGTH_LONG).show();
            showSaveMessage = null;
        }
        records = null;
        oldRecords = null;
        tTrials = null;
        name = null;
        league = null;
        myTTrialTimes = null;
        DrivePage.lats = null;
        DrivePage.longs = null;
        DrivePage.creatRoute = false;
        DrivePage.doneWithIt = false;
        DrivePage.clearedDrive = false;
        DrivePage.tTrialToLoad = null;
        DrivePage.remainingMarkers = null;
        DrivePage.s = null;
        DrivePage.m = null;
        DrivePage.cachedTime = Double.MAX_VALUE;
        DrivePage.tim = null;
        DrivePage.time = 0;
        DrivePage.tName = null;
        DriveModeService.isReady = false;
        DriveModeService.location = null;
        DriveModeService.roadsDiscovered = 0;
        DriveModeService.speed = 0;
        DriveModeService.v = null;
        SeeTTrialLeaderboard.courseName = null;
        HandleRequest.requestGeneric(this, StarterPage.ipAddr + "GETNAME~" + StarterPage.token + "~", "getName", this);
        HandleRequest.requestGeneric(this, StarterPage.ipAddr + "GETRECORDS~" + StarterPage.token + "~", "getRecords", this);
        HandleRequest.requestGeneric(this, StarterPage.ipAddr + "GETLEAGUE~" + StarterPage.token + "~", "getLeague", this);
    }

    public void driveButton(View v) {
        StarterPage.changeActivities(this, DrivePage.class);
    }

    public void notSetBtn(View v) {
        Intent settingsIntent = new Intent(Settings.ACTION_APP_NOTIFICATION_SETTINGS)
                .addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
                .putExtra(Settings.EXTRA_APP_PACKAGE, getPackageName());
        startActivity(settingsIntent);
    }

    public void seeLeaderboard(View v) {
        StarterPage.changeActivities(this, SeeLeaderboard.class);
    }

    public void seeTTrials(View v) {
        StarterPage.changeActivities(this, TimeTrialList.class);
    }

    public void createTTrial(View v) {
        DrivePage.creatRoute = true;
        StarterPage.changeActivities(this, DrivePage.class);
    }

}

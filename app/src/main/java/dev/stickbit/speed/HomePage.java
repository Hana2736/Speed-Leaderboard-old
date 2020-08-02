package dev.stickbit.speed;

import android.content.Intent;
import android.content.res.Configuration;
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
    public static String name;
    public static String league;
    int ready = 0;
    public static boolean showSaveMessage = false;


    public void ready() {
        ready++;
        if (ready == 3) {
            findViewById(R.id.driveButton).setEnabled(true);
            findViewById(R.id.statsButton).setEnabled(true);
            ready = 0;

            ((TextView) findViewById(R.id.wbMessage)).setText((getText(R.string.wb).toString().replace("%%", name).replace("$$", league)));

        }
    }

    @Override
    public void onBackPressed() {
        Intent intent = new Intent(Intent.ACTION_MAIN);
        intent.addCategory(Intent.CATEGORY_HOME);
        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
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
        if (showSaveMessage) {
            showSaveMessage = false;
            Snackbar.make(findViewById(R.id.wbMessage).getRootView(), R.string.saved, Snackbar.LENGTH_SHORT).show();
        }
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
}

package dev.stickbit.speed;

import android.os.Bundle;
import androidx.appcompat.app.AppCompatActivity;

public class SeeTTrialLeaderboard extends AppCompatActivity {
    public static String courseName;

    @Override
    public void onBackPressed() {
        HomePage.showSaveMessage = null;
        StarterPage.changeActivities(this, TimeTrialList.class);
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        if (getSupportActionBar() != null) {
            getSupportActionBar().hide();
        }
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_see_t_trial_leaderboard);

        HandleRequest.requestGeneric(this, StarterPage.ipAddr + "GETLEADERSTRIAL~" + StarterPage.token + "~" + courseName + "~", "getTTrialSingle", findViewById(R.id.llayoutres));

    }
}
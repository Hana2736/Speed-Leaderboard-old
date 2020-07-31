package dev.stickbit.speed;

import android.os.Bundle;
import android.view.View;
import androidx.appcompat.app.AppCompatActivity;

public class SeeLeaderboard extends AppCompatActivity {

    @Override
    public void onBackPressed() {
        StarterPage.changeActivities(this, StarterPage.class);
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        if (getSupportActionBar() != null) {
            getSupportActionBar().hide();
        }
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_see_leaderboard);
    }

    public void button(View v) {
        HandleRequest.requestGeneric(this, StarterPage.ipAddr + "GETALLRESULTS~" + StarterPage.token + "~", "pullAll", null);
    }
}

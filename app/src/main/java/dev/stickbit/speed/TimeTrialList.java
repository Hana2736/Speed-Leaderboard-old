package dev.stickbit.speed;

import android.content.res.Resources;
import android.os.Bundle;
import androidx.appcompat.app.AppCompatActivity;

public class TimeTrialList extends AppCompatActivity {
    @Override
    public void onBackPressed() {
        HomePage.showSaveMessage = null;
        StarterPage.changeActivities(this, StarterPage.class);
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_time_trial_list);
        if (getSupportActionBar() != null) {
            getSupportActionBar().hide();
        }
        HandleRequest.requestGeneric(this, StarterPage.ipAddr + "GETTTRIALS~" + StarterPage.token + "~", "getTTrials", null);

    }

    public static int dpToPx(int dp) {
        return (int) (dp * Resources.getSystem().getDisplayMetrics().density);
    }
}

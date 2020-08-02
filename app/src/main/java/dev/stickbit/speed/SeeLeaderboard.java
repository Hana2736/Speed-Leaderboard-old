package dev.stickbit.speed;

import android.app.Activity;
import android.os.Bundle;
import android.view.View;
import android.widget.AdapterView;
import android.widget.Spinner;
import androidx.appcompat.app.AppCompatActivity;

public class SeeLeaderboard extends AppCompatActivity {

    @Override
    public void onBackPressed() {
        StarterPage.changeActivities(this, StarterPage.class);
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {

        Activity a = this;
        if (getSupportActionBar() != null) {
            getSupportActionBar().hide();
        }
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_see_leaderboard);
        ((Spinner) findViewById(R.id.sortMode)).setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                if (parent.getSelectedItem().equals("Sort by person")) {
                    doIt(1);
                } else if (parent.getSelectedItem().equals("Sort by road")) {
                    doIt(0);
                } else if (parent.getSelectedItem().equals("Sort by speed")) {
                    doIt(2);
                }
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {

            }

            void doIt(int mode) {
                HandleRequest.requestGeneric(a, StarterPage.ipAddr + "GETALLRESULTS~" + StarterPage.token + "~", "pullAll", mode);
            }
        });
        ///

    }


}

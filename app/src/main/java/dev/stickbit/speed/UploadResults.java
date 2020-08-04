package dev.stickbit.speed;

import android.app.Activity;
import android.os.Bundle;
import android.view.View;
import android.widget.ProgressBar;
import android.widget.TextView;
import androidx.appcompat.app.AppCompatActivity;

public class UploadResults extends AppCompatActivity {
    int progress = 0;
    boolean keepRun = true;
    boolean first = false;
    long time;
    long startTime;
    double timeForFirst;
    int goodCount = 0;

    @Override
    public void onBackPressed() {
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        DriveModeService.v = null;
        if (getSupportActionBar() != null) {
            getSupportActionBar().hide();
        }
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_upload_results);
        startTime = System.currentTimeMillis();
        for (String road : HomePage.records.keySet()) {
            try {
                if (!HomePage.records.get(road).equals(HomePage.oldRecords.get(road))) {
                    goodCount++;
                    HandleRequest.requestGeneric(this, StarterPage.ipAddr + "SETRECORD~" + StarterPage.token + "~" + road + "~" + HomePage.records.get(road) + "~", "setRecord", this);
                }
            } catch (Exception e) {
                //lol
            }

        }
        if (DrivePage.tTrialToLoad != null) {
            if (DrivePage.cachedTime > DrivePage.time && DrivePage.time != 0) {
                HandleRequest.requestGeneric(this, StarterPage.ipAddr + "SETBESTTIMETRIAL~" + StarterPage.token + "~" + DrivePage.tTrialToLoad + "~" + DrivePage.time + "~", "setRecord", this);
                goodCount++;
                HomePage.showSaveMessage = getString(R.string.savedTTrialRun);
            } else {
                HomePage.showSaveMessage = getString(R.string.failedTTrialRun);
            }
        }
        if (DrivePage.creatRoute) {
            if (DrivePage.lats.size() > 3) {
                HomePage.showSaveMessage = getString(R.string.savedTTrial);
                String coords = DrivePage.tName + "~";
                while (!DrivePage.lats.isEmpty()) {
                    coords += DrivePage.lats.remove() + "," + DrivePage.longs.remove() + "~";
                }
                coords = coords.substring(0, coords.length() - 1);
                goodCount++;
                HandleRequest.requestGeneric(this, StarterPage.ipAddr + "SAVETTRIAL~" + StarterPage.token + "~" + coords, "setRecord", this);
            } else {
                HomePage.showSaveMessage = getString(R.string.notLongTtrial);
            }
        }
        new asyncTrash(this).start();
    }

    private class asyncTrash extends Thread {
        Activity a;

        public asyncTrash(Activity a) {
            this.a = a;
        }

        @Override
        public void run() {

            while (keepRun) {
                try {
                    Thread.sleep(500);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        double prog = (double) progress / (goodCount == 0 ? 1D : (double) goodCount);
                        TextView t = findViewById(R.id.uploadETA);
                        if (first) {
                            t.setVisibility(View.VISIBLE);
                            t.setText(getText(R.string.uploadETA).toString().replace("<Calculating>", String.valueOf((int) (1d + (timeForFirst / 1000d) * ((goodCount + 1d) - progress)))));
                        }
                        ((ProgressBar) findViewById(R.id.progressBar)).setProgress((int) ((prog) * 100D), true);
                        if (progress == goodCount) {
                            t.setText(R.string.finishing);
                            keepRun = false;
                            finish();
                            StarterPage.changeActivities(a, StarterPage.class);
                            if (HomePage.showSaveMessage == null) {
                                HomePage.showSaveMessage = getString(R.string.saved);
                            }
                        }
                    }
                });

            }
        }
    }
}

package dev.stickbit.speed;

import android.app.Activity;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.content.Intent;
import android.os.Bundle;
import android.transition.Slide;
import android.view.Gravity;
import android.view.View;
import android.view.animation.DecelerateInterpolator;
import androidx.appcompat.app.AppCompatActivity;
import com.android.volley.toolbox.Volley;
import com.google.android.material.snackbar.Snackbar;

import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;

public class StarterPage extends AppCompatActivity {
    public static String token;
    public static String ipAddr = "https://mario.stickbit.dev:8448/~";

    public static void changeActivities(Activity co, Class cl) {
        Intent i = new Intent(co, cl);
        i.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_SINGLE_TOP | Intent.FLAG_ACTIVITY_NO_ANIMATION);
        co.finish();
        co.startActivity(i);
        co.overridePendingTransition(0, 0);
    }

    @Override
    public void onBackPressed() {

    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        HandleRequest.q = Volley.newRequestQueue(this);
        if (getSupportActionBar() != null) {
            getSupportActionBar().hide();
        }
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_starter_page);

        List<NotificationChannel> chan = new ArrayList<>();
        chan.add(new NotificationChannel("Background", getText(R.string.bgCat), NotificationManager.IMPORTANCE_LOW));
        chan.add(new NotificationChannel("pb", getText(R.string.recordCat), NotificationManager.IMPORTANCE_LOW));
        chan.add(new NotificationChannel("crash", getText(R.string.crashCat), NotificationManager.IMPORTANCE_HIGH));
        chan.add(new NotificationChannel("newRoad", getText(R.string.newRoadCat), NotificationManager.IMPORTANCE_LOW));
        chan.add(new NotificationChannel("defeat", getText(R.string.defeatCat), NotificationManager.IMPORTANCE_DEFAULT));
        NotificationManager notificationManager = getSystemService(NotificationManager.class);
        for (NotificationChannel c : chan) {
            notificationManager.createNotificationChannel(c);
        }

        try {
            if (Files.exists(Paths.get(getFilesDir() + "/token"))) {
                token = Files.readAllLines(Paths.get(getFilesDir() + "/token")).get(0);
                System.out.println(token);
                if (DriveModeService.v == null) {
                    changeActivities(this, HomePage.class);
                } else {
                    findViewById(R.id.textView).setVisibility(View.INVISIBLE);
                    Snackbar.make(findViewById(R.id.textView).getRootView(), R.string.useNotif, Snackbar.LENGTH_INDEFINITE).show();
                }

            } else {
                changeActivities(this, Register.class);
            }
        } catch (Exception e) {
            Snackbar.make(getWindow().getDecorView().getRootView(), getText(R.string.genericError).toString().replace("%%", e.toString()), Snackbar.LENGTH_INDEFINITE).show();
            e.printStackTrace();
        }
    }
}

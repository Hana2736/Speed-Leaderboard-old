package dev.stickbit.speed;

import android.app.Activity;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import androidx.appcompat.app.AppCompatActivity;
import com.android.volley.BuildConfig;
import com.android.volley.toolbox.Volley;
import com.google.android.material.snackbar.Snackbar;
import com.google.android.play.core.appupdate.AppUpdateInfo;
import com.google.android.play.core.appupdate.AppUpdateManager;
import com.google.android.play.core.appupdate.AppUpdateManagerFactory;
import com.google.android.play.core.appupdate.AppUpdateOptions;
import com.google.android.play.core.install.model.AppUpdateType;
import com.google.android.play.core.install.model.UpdateAvailability;
import com.google.android.play.core.tasks.Task;
import org.osmdroid.config.Configuration;

import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;

public class StarterPage extends AppCompatActivity {
    public static String token;
    public static String ipAddr = "https://speed.travisgenzer.com/~";
    public static List<String> tokens;
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
        Configuration.getInstance().setUserAgentValue(BuildConfig.APPLICATION_ID);
        AppUpdateManager appUpdateManager = AppUpdateManagerFactory.create(this);
        Task<AppUpdateInfo> appUpdateInfoTask = appUpdateManager.getAppUpdateInfo();
        appUpdateInfoTask.addOnSuccessListener(appUpdateInfo -> {
            if (appUpdateInfo.updateAvailability() == UpdateAvailability.UPDATE_AVAILABLE) {
                appUpdateManager.startUpdateFlow(appUpdateInfo, this, AppUpdateOptions.newBuilder(AppUpdateType.IMMEDIATE).build());
            }
        });


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
        NotificationManager notificationManager = getSystemService(NotificationManager.class);
        for (NotificationChannel c : chan) {
            notificationManager.createNotificationChannel(c);
        }

        try {
            if (Files.exists(Paths.get(getFilesDir() + "/tokens"))) {
                tokens = (List<String>) HomePage.fileToObj(getFilesDir() + "/tokens", this);
                token = tokens.get(Integer.parseInt((String) HomePage.fileToObj(getFilesDir() + "/mainToken",this)));
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

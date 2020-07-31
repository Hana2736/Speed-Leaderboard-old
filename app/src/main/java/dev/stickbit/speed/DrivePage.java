package dev.stickbit.speed;

import android.Manifest;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.view.View;
import android.widget.TextView;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.ContextCompat;
import com.google.android.material.snackbar.Snackbar;

public class DrivePage extends AppCompatActivity {
    public static DriveModeService s;
    boolean doneWithIt = false;

    @Override
    public void onBackPressed() {
        Snackbar.make(getWindow().getDecorView().getRootView(), R.string.stopDriveButtonWarn, Snackbar.LENGTH_SHORT).show();
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, String[] permissions, int[] grantResults) {
        StarterPage.changeActivities(this, DrivePage.class);
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        if (getSupportActionBar() != null) {
            getSupportActionBar().hide();
        }
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_drive_page);
        boolean ready = true;
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_DENIED) {
            Snackbar s = Snackbar.make(getWindow().getDecorView().getRootView(), R.string.locationPermissionRequired, Snackbar.LENGTH_INDEFINITE);
            s.setAction(R.string.grantButton, new askPermClickListen(0));
            s.show();
            ready = false;
        } else if (ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_BACKGROUND_LOCATION) == PackageManager.PERMISSION_DENIED) {
            Snackbar s = Snackbar.make(getWindow().getDecorView().getRootView(), R.string.bgLocationPermission, Snackbar.LENGTH_INDEFINITE);
            s.setAction(R.string.grantButton, new askPermClickListen(1));
            s.show();
            ready = false;
        }
        if (ready) {
            stopService(new Intent(this, DriveModeService.class));
            startService(new Intent(this, DriveModeService.class));
            DriveModeService.v = this;
            new AsyncTrash(findViewById(R.id.speedText), findViewById(R.id.locText), findViewById(R.id.newRoads)).start();
        }
    }

    public void stopButton(View w) {
        doneWithIt = true;
        s.kill();
        DriveModeService.v = null;
        w.setEnabled(false);
        DriveModeService.roadsDiscovered = 0;
        StarterPage.changeActivities(this, UploadResults.class);
    }

    private class AsyncTrash extends Thread {
        TextView t;
        TextView location;
        TextView newRoads;

        private AsyncTrash(TextView t, TextView location, TextView newRoads) {
            this.t = t;
            this.location = location;
            this.newRoads = newRoads;
        }

        @Override
        public void run() {
            try {
                Thread.sleep(5000);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
            runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    findViewById(R.id.stopDriving).setEnabled(true);
                }
            });
            while (true) {
                if (doneWithIt)
                    return;
                try {
                    Thread.sleep(1000);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
                if (DriveModeService.isReady) {
                    runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            t.setText(getText(R.string.speedMenu).toString().replace("$$", String.valueOf(DriveModeService.speed)));
                            location.setText(DriveModeService.location);
                            newRoads.setText(getText(R.string.roadsDiscovered).toString().replace("0", String.valueOf(DriveModeService.roadsDiscovered)));
                        }
                    });
                }
            }
        }
    }

    private class askPermClickListen implements View.OnClickListener {
        int perm;

        public askPermClickListen(int perm) {
            this.perm = perm;
        }

        @Override
        public void onClick(View view) {
            requestPermissions(new String[]{perm == 0 ? Manifest.permission.ACCESS_FINE_LOCATION : Manifest.permission.ACCESS_BACKGROUND_LOCATION}, 1000);
        }
    }
}
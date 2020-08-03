package dev.stickbit.speed;

import android.Manifest;
import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.content.res.Configuration;
import android.graphics.Color;
import android.graphics.ColorMatrix;
import android.graphics.ColorMatrixColorFilter;
import android.os.Bundle;
import android.text.InputType;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Switch;
import android.widget.TextView;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.ContextCompat;
import com.google.android.material.snackbar.Snackbar;
import org.osmdroid.tileprovider.tilesource.TileSourceFactory;
import org.osmdroid.util.GeoPoint;
import org.osmdroid.views.MapView;
import org.osmdroid.views.overlay.Marker;
import org.osmdroid.views.overlay.mylocation.MyLocationNewOverlay;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.LinkedList;
import java.util.Queue;

public class DrivePage extends AppCompatActivity {
    public static DriveModeService s;
    public static MapView m;
    public static String tTrialToLoad;
    public static double cachedTime = Double.MAX_VALUE;
    public static double time = 0;
    public static Queue<Object[]> remainingMarkers;
    public static boolean clearedDrive = false;
    public static boolean creatRoute = false;
    public static Queue<Double> lats;
    public static Queue<Double> longs;
    public static String tName;
    static boolean doneWithIt = false;
    static DrivePage.Timer tim;
    static int initPts;
    MyLocationNewOverlay mover;

    public static void removeMarker(Marker m, MapView ma) {
        ma.getOverlays().remove(m);
        ma.invalidate();
    }

    public static void startTimer() {
        tim = new DrivePage.Timer();
        tim.start();
    }

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
        super.onCreate(savedInstanceState);
        doneWithIt = false;
        setContentView(R.layout.activity_drive_page);
        m = findViewById(R.id.driveMap);
        mover = new MyLocationNewOverlay(m);
        m.getOverlays().add(mover);
        mover.enableMyLocation();
        mover.setEnableAutoStop(false);


        if (Files.exists(Paths.get(getFilesDir() + "/setFollowMap"))) {
            try {
                ((Switch) findViewById(R.id.followSwitch)).setChecked(Boolean.parseBoolean(Files.readAllLines(Paths.get(getFilesDir() + "/setFollowMap")).get(0)));
            } catch (IOException ignored) {
            }
        }

        ((Switch) findViewById(R.id.followSwitch)).setOnCheckedChangeListener((compoundButton, b) -> {
            if (b) {
                mover.enableFollowLocation();
            } else {
                mover.disableFollowLocation();
            }
            try {
                Files.write(Paths.get(getFilesDir() + "/setFollowMap"), String.valueOf(b).getBytes());
            } catch (IOException ignored) {
            }
        });


        if (getSupportActionBar() != null) {
            getSupportActionBar().hide();
        }


        boolean ready = true;
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_DENIED) {
            Snackbar s = Snackbar.make(getWindow().getDecorView().getRootView(), R.string.locationPermissionRequired, Snackbar.LENGTH_INDEFINITE);
            s.setAction(R.string.grantButton, new askPermClickListen(0));
            s.show();
            findViewById(R.id.driveMap).setVisibility(View.INVISIBLE);
            ready = false;
        }
        if (ready) {
            if (creatRoute) {
                AlertDialog.Builder builder = new AlertDialog.Builder(this);
                final String[] title = new String[1];
                builder.setTitle(R.string.nameTrial);
                final EditText input = new EditText(this);
                input.setInputType(InputType.TYPE_CLASS_TEXT);
                builder.setView(input);
                builder.setPositiveButton(R.string.OK, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        tName = input.getText().toString().replaceAll("~", "");
                    }
                });
                builder.setNegativeButton("", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        dialog.cancel();
                    }
                });
                builder.show();
            }


            stopService(new Intent(this, DriveModeService.class));
            startService(new Intent(this, DriveModeService.class));
            DriveModeService.v = this;
            new AsyncTrash(findViewById(R.id.speedText), findViewById(R.id.locText), findViewById(R.id.newRoads)).start();
            MapView map = findViewById(R.id.driveMap);
            map.setTileSource(TileSourceFactory.MAPNIK);
            map.getController().setZoom(17d);
            map.setTilesScaledToDpi(true);
            Switch s = findViewById(R.id.followSwitch);
            s.toggle();
            s.toggle();

            if (tTrialToLoad != null) {
                ((Button) findViewById(R.id.stopDriving)).setText(R.string.guBtn);
                remainingMarkers = new LinkedList<>();
                Switch sw = findViewById(R.id.followSwitch);
                if (sw.isChecked()) {
                    sw.toggle();
                }
                String[] pts = HomePage.tTrials.get(tTrialToLoad);
                ((TextView) findViewById(R.id.speedText)).setText(R.string.getToStart);
                ((TextView) findViewById(R.id.locText)).setText(R.string.timeElapsed);
                ((TextView) findViewById(R.id.newRoads)).setText(getText(R.string.pinInfo).toString().replace("$$", String.valueOf(pts.length)).replace("%%", String.valueOf(pts.length)));
                for (int i = 0, ptsLength = pts.length; i < ptsLength - 1; i++) {
                    String pt = pts[i];
                    String[] coords = pt.split(",");
                    double lat = Double.parseDouble(coords[0]);
                    double longi = Double.parseDouble(coords[1]);
                    int type = i == 0 ? 1 : i == ptsLength - 2 ? 3 : 0;
                    remainingMarkers.add(new Object[]{addMarker(lat, longi, map, type), type});
                }
                map.getController().setCenter(((Marker) remainingMarkers.peek()[0]).getPosition());
                initPts = remainingMarkers.size();

            }


            if ((getResources().getConfiguration().uiMode & Configuration.UI_MODE_NIGHT_MASK) == Configuration.UI_MODE_NIGHT_YES) {
                ColorMatrix inverseMatrix = new ColorMatrix(new float[]{
                        -1.0f, 0.0f, 0.0f, 0.0f, 255f,
                        0.0f, -1.0f, 0.0f, 0.0f, 255f,
                        0.0f, 0.0f, -1.0f, 0.0f, 255f,
                        0.0f, 0.0f, 0.0f, 1.0f, 0.0f
                });

                int destinationColor = Color.parseColor("#FF2A2A2A");
                float lr = (255.0f - Color.red(destinationColor)) / 255.0f;
                float lg = (255.0f - Color.green(destinationColor)) / 255.0f;
                float lb = (255.0f - Color.blue(destinationColor)) / 255.0f;
                ColorMatrix grayscaleMatrix = new ColorMatrix(new float[]{
                        lr, lg, lb, 0, 0, //
                        lr, lg, lb, 0, 0, //
                        lr, lg, lb, 0, 0, //
                        0, 0, 0, 0, 255, //
                });
                grayscaleMatrix.preConcat(inverseMatrix);
                int dr = Color.red(destinationColor);
                int dg = Color.green(destinationColor);
                int db = Color.blue(destinationColor);
                float drf = dr / 255f;
                float dgf = dg / 255f;
                float dbf = db / 255f;
                ColorMatrix tintMatrix = new ColorMatrix(new float[]{
                        drf, 0, 0, 0, 0, //
                        0, dgf, 0, 0, 0, //
                        0, 0, dbf, 0, 0, //
                        0, 0, 0, 1, 0, //
                });
                tintMatrix.preConcat(grayscaleMatrix);
                float lDestination = drf * lr + dgf * lg + dbf * lb;
                float scale = 1f - lDestination;
                float translate = 1 - scale * 0.5f;
                ColorMatrix scaleMatrix = new ColorMatrix(new float[]{
                        scale, 0, 0, 0, dr * translate, //
                        0, scale, 0, 0, dg * translate, //
                        0, 0, scale, 0, db * translate, //
                        0, 0, 0, 1, 0, //
                });
                scaleMatrix.preConcat(tintMatrix);
                ColorMatrixColorFilter filter = new ColorMatrixColorFilter(scaleMatrix);
                map.getOverlayManager().getTilesOverlay().setColorFilter(filter);
            }
        }
    }

    public Marker addMarker(double lat, double longi, MapView m, int icon) {
        GeoPoint startPoint = new GeoPoint(lat, longi);
        Marker startMarker = new Marker(m);
        startMarker.setPosition(startPoint);
        startMarker.setAnchor(Marker.ANCHOR_CENTER, Marker.ANCHOR_BOTTOM);
        String title = "error";
        String desc = "invalid marker";
        switch (icon) {
            case 0: {
                startMarker.setIcon(getDrawable(R.drawable.future_point));
                title = getString(R.string.fCPTitle);
                desc = getString(R.string.fCPDesc);
                break;
            }
            case 1: {
                startMarker.setIcon(getDrawable(R.drawable.start_marker));
                title = getString(R.string.startPointTitle);
                desc = getString(R.string.startPointDesc);
                break;
            }
            case 2: {
                startMarker.setIcon(getDrawable(R.drawable.next_point));
                title = getString(R.string.nCPTitle);
                desc = getString(R.string.nCPDesc);
                break;
            }
            case 3: {
                startMarker.setIcon(getDrawable(R.drawable.finish_marker));
                title = getString(R.string.finishPointTitle);
                desc = getString(R.string.finishPointDesc);
                break;
            }
        }
        startMarker.setTitle(title);
        startMarker.setSubDescription(desc);
        m.getOverlays().add(startMarker);
        return startMarker;
    }

    public void stopButton(View w) {

        if (clearedDrive)
            time = tim.time;
        clearedDrive = false;
        doneWithIt = true;
        s.kill();
        w.setEnabled(false);
        StarterPage.changeActivities(this, UploadResults.class);
    }

    private static class Timer extends Thread {
        public double time = 0;
        double initTime;

        public Timer() {
            initTime = System.currentTimeMillis();
        }

        @Override
        public void run() {
            try {
                while (true) {
                    time = System.currentTimeMillis() - initTime;
                    Thread.sleep(50);
                    if (doneWithIt)
                        return;
                }
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
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
                            if (tTrialToLoad == null) {
                                t.setText(getText(R.string.speedMenu).toString().replace("$$", String.valueOf(DriveModeService.speed)));
                                location.setText(DriveModeService.location);
                                newRoads.setText(getText(R.string.roadsDiscovered).toString().replace("0", String.valueOf(DriveModeService.roadsDiscovered)));
                            } else {
                                if (tim != null) {
                                    t.setText(getText(R.string.speedMenu).toString().replace("$$", String.valueOf(DriveModeService.speed)));
                                    int min = (int) (tim.time / 1000 / 60);
                                    int sec = (int) (tim.time / 1000 - (min * 60));
                                    System.out.println(min + " mins and secs: " + sec);
                                    String mString = String.valueOf(min);
                                    String sString = String.valueOf(sec).length() == 1 ? "0" + sec : String.valueOf(sec);
                                    ((TextView) findViewById(R.id.locText)).setText(getText((R.string.timeElapsed)).toString().replace("0:00", mString + ":" + sString));
                                    ((TextView) findViewById(R.id.newRoads)).setText(getText(R.string.markerRmn).toString().replace("%%", String.valueOf(remainingMarkers.size())).replace("$$", String.valueOf(initPts)));
                                }
                            }
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
            requestPermissions(new String[]{Manifest.permission.ACCESS_FINE_LOCATION}, (int) (Math.random() * 99999));
        }
    }

}
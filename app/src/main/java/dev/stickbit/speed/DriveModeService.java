package dev.stickbit.speed;

import android.app.Notification;
import android.app.PendingIntent;
import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.location.*;
import android.os.*;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.core.app.NotificationManagerCompat;
import com.google.android.material.snackbar.Snackbar;
import org.osmdroid.views.overlay.Marker;

import java.io.FileOutputStream;
import java.io.ObjectOutputStream;
import java.nio.file.Paths;
import java.util.LinkedList;

public class DriveModeService extends Service {
    public static int speed;
    public static boolean isReady = false;
    public static String location;
    public static DrivePage v = null;
    public static int roadsDiscovered = 0;
    public double lastLat;
    public double lastLongi;
    double speedCalc;
    LocationManager l;
    boolean kill;
    trash t;
    HandlerThread th;
    Location lastPnt;
    boolean hasPlacedBeginPoint;
    Marker lastmarker;
    private Looper serviceLooper;
    private ServiceHandler serviceHandler;
    private Location last;
    int count = 5;
    Address a;

    public void kill() {
        stopForeground(true);
        stopSelf();
        serviceHandler.removeCallbacksAndMessages(null);
        serviceLooper.quit();
        kill = true;
        l.removeUpdates(t);
        th.quit();
        th.getLooper().quit();
    }

    @Override
    public void onCreate() {
        t = new trash();
        // Start up the thread running the service. Note that we create a
        // separate thread because the service normally runs in the process's
        // main thread, which we don't want to block. We also make it
        // background priority so CPU-intensive work doesn't disrupt our UI.
        HandlerThread thread = new HandlerThread("ServiceStartArguments", Thread.MIN_PRIORITY);
        th = thread;
        thread.start();

        // Get the HandlerThread's Looper and use it for our Handler
        serviceLooper = thread.getLooper();
        serviceHandler = new ServiceHandler(serviceLooper);


        Intent notificationIntent = new Intent(v, DrivePage.class);
        PendingIntent pendingIntent = PendingIntent.getActivity(v, 0, notificationIntent, 0);

        Notification notification =
                new Notification.Builder(this, "Background")
                        .setContentTitle(getText(R.string.bgService))
                        .setSmallIcon(R.mipmap.ic_launcher_foreground)
                        .setContentIntent(pendingIntent)
                        .setStyle(new Notification.BigTextStyle().bigText(getText(R.string.bgServiceTxt)))
                        .build();
        startForeground(1, notification);
        l = ((LocationManager) getSystemService(Context.LOCATION_SERVICE));
        DrivePage.s = this;

    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        Message msg = serviceHandler.obtainMessage();
        msg.arg1 = startId;
        serviceHandler.sendMessage(msg);
        return START_NOT_STICKY;
    }


    @Override
    public void onDestroy() {

    }

    @Nullable
    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }

    private final class ServiceHandler extends Handler {
        public ServiceHandler(Looper looper) {
            super(looper);
        }

        @Override
        public void handleMessage(Message msg) {
            while (true) {
                if (kill)
                    return;
                try {
                    Thread.sleep(750);
                    count++;
                    l.requestLocationUpdates(LocationManager.GPS_PROVIDER, 0, 0, t);
                    if (last == null) {
                        last = l.getLastKnownLocation(LocationManager.GPS_PROVIDER);
                    }
                    if (!v.isTaskRoot() && !DrivePage.doneWithIt) {
                        Notification notification =
                                new Notification.Builder(v, "crash")
                                        .setContentTitle(getText(R.string.bgBannerCrash))
                                        .setSmallIcon(R.mipmap.ic_launcher_foreground)
                                        .setStyle(new Notification.BigTextStyle().bigText(getText(R.string.backgroundCrash)))
                                        .build();
                        NotificationManagerCompat.from(v).notify(25601440, notification);
                        v.finishAffinity();
                        System.exit(0);
                    }
                    Location newL = l.getLastKnownLocation(LocationManager.GPS_PROVIDER);
                    double dist = 0;
                    double timeDiff = 9999999999999d;
                    try {
                        dist = newL.distanceTo(last);
                        timeDiff = newL.getTime() - last.getTime();
                    } catch (Exception e) {
                        //lol
                    }

                    isReady = false;
                    //meter/sec
                    speedCalc = dist / (timeDiff / 1000d);
                    //meter/min
                    speedCalc *= 60d;
                    //meter/hour
                    speedCalc *= 60d;
                    //km/hr
                    speedCalc /= 1000d;
                    //mph
                    speedCalc *= 0.621371192d;


                    boolean accurate = last.getAccuracy() < 25 && newL.getAccuracy() < 25;
                    last = newL;
                    if (count >= 3) {
                        Geocoder c = new Geocoder(v);
                        a = c.getFromLocation(newL.getLatitude(), newL.getLongitude(), 1).get(0);
                        count = 0;
                    }

                    if (a != null) {
                        location = a.getThoroughfare() + ", " + a.getPostalCode() + ", " + a.getCountryName();
                        location = location.replaceAll("null", "N/A");
                    }
                    boolean toAdd = false;
                    boolean newR = false;
                    if (accurate) {
                        if (speedCalc < 300) {
                            speed = (int) speedCalc;

                        } else {
                            System.out.println("WE ARE GOING WAY TOO FAST");
                        }
                        if (DrivePage.creatRoute) {
                            if (DrivePage.longs == null) {
                                DrivePage.longs = new LinkedList<>();
                                DrivePage.lats = new LinkedList<>();
                            }
                            double lastPntDist;
                            if (lastPnt == null) {
                                lastPntDist = 99999999;
                            } else {
                                lastPntDist = Math.abs(newL.distanceTo(lastPnt));
                            }
                            if (lastPntDist > 155) {
                                lastPnt = new Location("");
                                lastPnt.setLatitude(newL.getLatitude());
                                lastPnt.setLongitude(newL.getLongitude());
                                DrivePage.lats.add(newL.getLatitude());
                                DrivePage.longs.add(newL.getLongitude());
                                if (lastmarker != null && DrivePage.lats.size() >= 3) {
                                    lastmarker.setIcon(v.getDrawable(R.drawable.future_point));
                                }
                                lastmarker = v.addMarker(newL.getLatitude(), newL.getLongitude(), DrivePage.m, hasPlacedBeginPoint ? 3 : 1);
                                if (!hasPlacedBeginPoint) {
                                    hasPlacedBeginPoint = true;
                                }

                            }

                        }
                        System.out.println("hello?");
                        lastLat = newL.getLatitude();
                        lastLongi = newL.getLongitude();
                        if (DrivePage.tTrialToLoad != null) {
                            Marker m = (Marker) DrivePage.remainingMarkers.peek()[0];
                            int type = (int) DrivePage.remainingMarkers.peek()[1];
                            Location loc = new Location("");
                            loc.setLatitude(m.getPosition().getLatitude());
                            loc.setLongitude(m.getPosition().getLongitude());
                            double distBtwn = Math.abs(loc.distanceTo(newL));
                            System.out.println("Distance is " + distBtwn);
                            if (distBtwn < 40) {
                                DrivePage.removeMarker((Marker) DrivePage.remainingMarkers.remove()[0], DrivePage.m);
                                if (type == 1) {
                                    DrivePage.startTimer();
                                }
                                if (type == 3) {
                                    DrivePage.clearedDrive = true;
                                    v.stopButton(v.findViewById(R.id.stopDriving));
                                }
                                Marker m2 = ((Marker) DrivePage.remainingMarkers.peek()[0]);
                                if ((int) DrivePage.remainingMarkers.peek()[1] == 0) {
                                    m2.setIcon(getDrawable(R.drawable.next_point));
                                    m2.setTitle(getString(R.string.nCPTitle));
                                    m2.setSubDescription(getString(R.string.nCPDesc));
                                }
                            }
                        }
                        if (HomePage.records.containsKey(location)) {
                            if (HomePage.records.get(location) < speed) {
                                toAdd = true;
                            }
                        } else {
                            toAdd = true;
                            newR = true;
                        }
                    }
                    if (toAdd) {
                        HomePage.records.put(location, speed);
                        roadsDiscovered++;
                        Notification notification =
                                new Notification.Builder(v, newR ? "newRoad" : "pb")
                                        .setContentTitle(newR ? getText(R.string.newRoadHeader) : getText(R.string.newRecord))
                                        .setSmallIcon(R.mipmap.ic_launcher_foreground)
                                        .setStyle(new Notification.BigTextStyle().bigText(newR ? getText(R.string.newRoadTxt).toString().replace("$$", location) : getText(R.string.newRecordText).toString().replace("%%", String.valueOf(speed)).replace("!!", location)))
                                        .build();
                        NotificationManagerCompat.from(v).notify(newR ? (int) (Math.random() * 100000) : location.hashCode(), notification);
                        try {
                            FileOutputStream outStream = new FileOutputStream(String.valueOf(Paths.get(getCacheDir() + "/cachedAttempt")));
                            ObjectOutputStream objectOutputStream = new ObjectOutputStream(outStream);
                            objectOutputStream.writeObject(HomePage.records);
                            outStream.close();
                            objectOutputStream.close();
                        } catch (Exception e) {
                            Snackbar.make(v.getWindow().getDecorView().getRootView(), R.string.cacheFail, Snackbar.LENGTH_INDEFINITE).show();
                        }
                    }


                    isReady = true;
                } catch (Exception e) {
                    e.printStackTrace();
                }

            }
        }
    }

    private class trash implements LocationListener {

        @Override
        public void onLocationChanged(@NonNull Location location) {
            //lol
        }

        @Override
        public void onStatusChanged(String provider, int status, Bundle extras) {
            //lol
        }

        @Override
        public void onProviderEnabled(String provider) {
            //lol
        }

        @Override
        public void onProviderDisabled(String provider) {
            //lol
        }
    }
}
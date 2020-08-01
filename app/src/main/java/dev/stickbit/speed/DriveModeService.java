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

public class DriveModeService extends Service {
    public static int speed;
    public static boolean isReady = false;
    public static String location;
    public static DrivePage v = null;
    public static int roadsDiscovered = 0;
    double speedCalc;
    LocationManager l;
    boolean kill;
    trash t;
    HandlerThread th;
    private Looper serviceLooper;
    private ServiceHandler serviceHandler;
    private Location last;

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
                        .setSmallIcon(R.drawable.ic_launcher_foreground)
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
                    Thread.sleep(2000);
                    l.requestLocationUpdates(LocationManager.GPS_PROVIDER, 0, 0, t);
                    if (last == null) {
                        last = l.getLastKnownLocation(LocationManager.GPS_PROVIDER);
                    }
                    if (!v.isTaskRoot() && !v.doneWithIt) {
                        Notification notification =
                                new Notification.Builder(v, "crash")
                                        .setContentTitle(getText(R.string.bgBannerCrash))
                                        .setSmallIcon(R.drawable.ic_launcher_foreground)
                                        .setStyle(new Notification.BigTextStyle().bigText(getText(R.string.backgroundCrash)))
                                        .build();
                        NotificationManagerCompat.from(v).notify(25601440, notification);
                        v.finishAffinity();
                        System.exit(0);
                    }
                    Location newL = l.getLastKnownLocation(LocationManager.GPS_PROVIDER);
                    double dist = 0;
                    double timeDiff = 250;
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
                    speedCalc *= 0.621371192;
                    speed = (int) speedCalc;
                    boolean accurate = last.getAccuracy() < 30 && newL.getAccuracy() < 30;
                    last = newL;
                    Geocoder c = new Geocoder(v);
                    Address a = c.getFromLocation(newL.getLatitude(), newL.getLongitude(), 1).get(0);
                    location = a.getThoroughfare() + ", " + a.getPostalCode() + ", " + a.getCountryName();
                    location = location.replaceAll("null", "N/A");
                    boolean toAdd = false;
                    boolean newR = false;
                    if (accurate) {
                        if (HomePage.records.containsKey(location)) {
                            if (HomePage.records.get(location) < speed) {
                                toAdd = true;
                            }
                        } else {
                            toAdd = true;
                            roadsDiscovered++;
                            newR = true;
                        }
                    }
                    if (toAdd) {
                        HomePage.records.put(location, speed);
                        Notification notification =
                                new Notification.Builder(v, newR ? "newRoad" : "pb")
                                        .setContentTitle(newR ? getText(R.string.newRoadHeader) : getText(R.string.newRecord))
                                        .setSmallIcon(R.drawable.ic_launcher_foreground)
                                        .setStyle(new Notification.BigTextStyle().bigText(newR ? getText(R.string.newRoadTxt).toString().replace("$$", location) : getText(R.string.newRecordText).toString().replace("%%", String.valueOf(speed)).replace("!!", location)))
                                        .build();
                        NotificationManagerCompat.from(v).notify(newR ? (int) (Math.random() * 100000) : location.hashCode(), notification);
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
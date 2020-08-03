package dev.stickbit.speed;

import android.app.Activity;
import android.content.res.Resources;
import android.view.Gravity;
import android.view.View;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.TextView;
import androidx.constraintlayout.widget.ConstraintLayout;
import androidx.constraintlayout.widget.ConstraintSet;
import com.android.volley.ClientError;
import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.toolbox.StringRequest;
import com.google.android.material.button.MaterialButton;
import com.google.android.material.snackbar.Snackbar;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.*;

public class HandleRequest {
    public static RequestQueue q = null;
    static Snackbar s = null;

    public static void requestGeneric(Activity a, String url, String mode, Object extra) {
        if (mode.equals("pullAll")) {
            LinearLayout l = a.findViewById(R.id.resultLayout);
            l.removeAllViews();
        }
        if (mode.equals("pullAll") || mode.equals("getTTrials") || mode.equals("getTTrialSingle")) {
            s = Snackbar.make(a.findViewById(mode.equals("pullAll") ? R.id.leaderboardHeader : mode.equals("getTTrials") ? R.id.mainCnstLay : R.id.llayoutres).getRootView(), R.string.loggingIn, Snackbar.LENGTH_INDEFINITE);
            s.show();
        }
        StringRequest stringRequest = new StringRequest(Request.Method.GET, url,
                response -> {
                    response = StrVerify.checkString(response, a, url, mode);
                    if (response == null) {
                        requestGeneric(a, url, mode, extra);
                        return;
                    }
                    try {
                        netHelper h = new netHelper();
                        switch (mode) {
                            case "getRecords": {
                                h.getRecords(a, response, (HomePage) extra);
                                break;
                            }
                            case "getLeague": {
                                h.getLeague(response, (HomePage) extra);
                                break;
                            }
                            case "getName": {
                                h.getName(response, (HomePage) extra);
                                break;
                            }
                            case "register": {
                                h.register(response, a);
                                break;
                            }
                            case "setRecord": {
                                h.setRecord((UploadResults) extra);
                                break;
                            }
                            case "pullAll": {
                                h.pullAllResults(a, response, (Integer) extra);
                                break;
                            }
                            case "getTTrials": {
                                h.getTTrials(response, a);
                                break;
                            }
                            case "getTTrialSingle": {
                                h.pullTTrialResults(response, a, (LinearLayout) extra);
                                break;
                            }
                        }
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                }, error -> {
            if (error instanceof ClientError) {
                Snackbar.make(a.getWindow().getDecorView().getRootView(), R.string.foroforerror, Snackbar.LENGTH_INDEFINITE).show();
            } else {
                Snackbar.make(a.getWindow().getDecorView().getRootView(), a.getText(R.string.networkError).toString().replace("%%", String.valueOf(error)), Snackbar.LENGTH_INDEFINITE).show();
            }
            error.printStackTrace();
        });
        q.add(stringRequest);
    }

    static void sortByStreet(Map<String, Map<String, Integer>> records, Activity s, LinearLayout l) {
        SortedSet<String> keys = new TreeSet<>(records.keySet());
        for (String street : keys) {
            for (String user : records.get(street).keySet()) {
                addTrash(s, user, records, l, street, records.get(street).get(user));
            }
        }
    }

    static void sortBySpeed(Map<String, Map<String, Integer>> records, Activity s, LinearLayout l) {
        List<Integer[]> speed = new ArrayList<>();
        List<String[]> res = new ArrayList<>();
        int i = 0;
        for (String street : records.keySet()) {
            for (String user : records.get(street).keySet()) {
                speed.add(new Integer[]{records.get(street).get(user), i++});
                res.add(new String[]{street, user});
            }
        }
        Collections.sort(speed, new Comparator<Integer[]>() {
            public int compare(Integer[] ints, Integer[] otherInts) {
                return otherInts[0].compareTo(ints[0]);
            }
        });
        for (Integer[] sp : speed) {
            addTrash(s, res.get(sp[1])[1], records, l, res.get(sp[1])[0], sp[0]);
        }
    }

    static void sortByName(Map<String, Map<String, Integer>> records, Activity s, LinearLayout l) {
        Map<String, Integer> counts = new HashMap<>();
        for (String street : records.keySet()) {
            for (String user : records.get(street).keySet()) {
                counts.put(user, ((counts.get(user) != null) ? counts.get(user) + 1 : 1));
            }
        }
        for (int i = 0; i < counts.keySet().size(); i++) {
            for (int j = 0; j < counts.get(counts.keySet().toArray()[i]); j++) {
                for (String street : records.keySet()) {
                    for (String user : records.get(street).keySet()) {
                        if (user.equals(counts.keySet().toArray()[i])) {
                            addTrash(s, user, records, l, street, records.get(street).get(user));
                            counts.put(user, counts.get(user) - 1);
                        }
                    }
                }
            }
        }
    }

    static void addTrash(Activity s, String name, Map<String, Map<String, Integer>> records, LinearLayout l, String street, int speed) {
        TextView t = new TextView(s);
        t.setText(name + ": " + speed + " on " + street.substring(0, street.lastIndexOf(",")));
        l.addView(t);
        t = new TextView(s);
        t.setText(" ");
        l.addView(t);
    }

    static void addTrash2(Activity s, String name, LinearLayout l, double time) {
        TextView t = new TextView(s);
        int min = (int) (time / 1000 / 60);
        int sec = (int) (time / 1000 - (min * 60));
        String mString = String.valueOf(min);
        String sString = String.valueOf(sec).length() == 1 ? "0" + sec : String.valueOf(sec);
        t.setText(name + ": " + mString + ":" + sString);
        l.addView(t);
        t = new TextView(s);
        t.setText(" ");
        l.addView(t);
    }

    private static class netHelper {
        public Snackbar sn = s;

        public void getRecords(Activity a, String res, HomePage h) {
            HomePage.records = new HashMap<>();
            String[] split = res.split("\n");
            for (String s : split) {
                if (s.contains("~")) {
                    try {
                        String[] splitRec = s.split("~");
                        System.out.println(Arrays.toString(splitRec));
                        HomePage.records.put(splitRec[0], Integer.parseInt(splitRec[1]));
                    } catch (Exception e) {
                        Snackbar.make(a.getWindow().getDecorView().getRootView(), a.getText(R.string.genericError).toString().replace("%%", e.toString()), Snackbar.LENGTH_LONG).show();
                    }
                }
            }
            HomePage.oldRecords = new HashMap<>();
            HomePage.oldRecords.putAll(HomePage.records);
            h.ready();
        }

        public void getLeague(String res, HomePage h) {
            HomePage.league = res;
            h.ready();
        }

        public void getName(String res, HomePage h) {
            HomePage.name = res;
            h.ready();

        }

        public void getTTrials(String res, Activity h) {
            HomePage.tTrials = new HashMap<>();
            HomePage.myTTrialTimes = new HashMap<>();
            String[] split = res.split("\n");
            for (String s : split) {
                if (s.contains("~")) {
                    try {
                        String[] splitPts = s.split("~");
                        String title = splitPts[0];
                        String[] arr = new String[splitPts.length - 1];
                        for (int i = 1; i < splitPts.length; i++) {
                            arr[i - 1] = splitPts[i];
                        }
                        HomePage.tTrials.put(title, arr);
                        try {
                            HomePage.myTTrialTimes.put(title, Double.valueOf(splitPts[splitPts.length - 1]));
                        } catch (Exception e) {
                            HomePage.myTTrialTimes.put(title, Double.MAX_VALUE);
                        }
                    } catch (Exception ignored) {
                    }
                }
            }
            ConstraintLayout mConstraintLayout = (ConstraintLayout) h.findViewById(R.id.cLao);
            ConstraintSet set = new ConstraintSet();

            View lastView = null;
            int width = Resources.getSystem().getDisplayMetrics().widthPixels;
            width = width / 2 - TimeTrialList.dpToPx(12);
            for (String name : HomePage.tTrials.keySet()) {
                TextView title = new TextView(h);
                title.setText(name);
                title.setTextAppearance(R.style.TextAppearance_AppCompat_Large);
                title.setGravity(Gravity.CENTER);
                title.setId(View.generateViewId());
                mConstraintLayout.addView(title, 0);
                MaterialButton goBtn = new MaterialButton(h);
                goBtn.setText(R.string.tTrialGo);
                goBtn.setId(View.generateViewId());
                goBtn.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View view) {
                        DrivePage.tTrialToLoad = name;
                        DrivePage.cachedTime = HomePage.myTTrialTimes.get(name);
                        StarterPage.changeActivities(h, DrivePage.class);
                    }
                });
                mConstraintLayout.addView(goBtn, 0);
                MaterialButton lbBtn = new MaterialButton(h);
                lbBtn.setText(R.string.tTrialLB);
                lbBtn.setId(View.generateViewId());
                mConstraintLayout.addView(lbBtn, 0);
                set.clone(mConstraintLayout);
                set.connect(title.getId(), ConstraintSet.TOP, lastView == null ? mConstraintLayout.getId() : lastView.getId(), lastView == null ? ConstraintSet.TOP : ConstraintSet.BOTTOM, 24);
                set.connect(title.getId(), ConstraintSet.START, mConstraintLayout.getId(), ConstraintSet.START, 0);
                set.connect(title.getId(), ConstraintSet.END, mConstraintLayout.getId(), ConstraintSet.END, 0);
                System.out.println("Width is " + width);
                goBtn.setWidth(width);
                goBtn.setMinWidth(width);
                goBtn.setMaxWidth(width);
                set.connect(goBtn.getId(), ConstraintSet.TOP, title.getId(), ConstraintSet.BOTTOM, 16);
                set.connect(goBtn.getId(), ConstraintSet.START, mConstraintLayout.getId(), ConstraintSet.START, 24);
                lbBtn.setWidth(width);
                lbBtn.setMinWidth(width);
                lbBtn.setMaxWidth(width);
                lbBtn.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View view) {
                        SeeTTrialLeaderboard.courseName = title.getText().toString();
                        StarterPage.changeActivities(h, SeeTTrialLeaderboard.class);
                    }
                });
                set.connect(lbBtn.getId(), ConstraintSet.TOP, title.getId(), ConstraintSet.BOTTOM, 16);
                set.connect(lbBtn.getId(), ConstraintSet.END, mConstraintLayout.getId(), ConstraintSet.END, 24);
                lastView = lbBtn;
                set.applyTo(mConstraintLayout);
            }
            sn.dismiss();
        }

        public void register(String res, Activity v) {
            Button b = v.findViewById(R.id.regButton);
            if (res.equals("BadName!")) {
                b.setEnabled(true);
                Snackbar.make(v.getWindow().getDecorView().getRootView(), R.string.registerError, Snackbar.LENGTH_INDEFINITE).show();
                return;
            }
            if (res.equals("NameInUse!")) {
                b.setEnabled(true);
                Snackbar.make(v.getWindow().getDecorView().getRootView(), R.string.nameInUse, Snackbar.LENGTH_INDEFINITE).show();
                return;
            }
            try {
                Files.write(Paths.get(v.getFilesDir() + "/token"), res.getBytes());
                Snackbar.make(v.getWindow().getDecorView().getRootView(), R.string.registered, Snackbar.LENGTH_SHORT).show();
                StarterPage.changeActivities(v, StarterPage.class);
            } catch (IOException e) {
                Snackbar.make(v.getWindow().getDecorView().getRootView(), v.getText(R.string.genericError).toString().replace("%%", e.toString()), Snackbar.LENGTH_INDEFINITE).show();
                e.printStackTrace();
            }
        }

        public void setRecord(UploadResults resP) {
            resP.progress++;
            resP.first = true;
            resP.time = System.currentTimeMillis();
            resP.timeForFirst = resP.time - resP.startTime;
            resP.startTime = System.currentTimeMillis();
        }

        public void pullAllResults(Activity s, String reply, int mode) {
            LinearLayout l = s.findViewById(R.id.resultLayout);
            l.removeAllViews();
            System.out.println("we got here");
            Map<String, Map<String, Integer>> records = new HashMap<>();

            String[] splitRoadBad = reply.split("STARTROAD:");
            String[] splitRoad = new String[splitRoadBad.length - 1];
            for (int i = 1; i < splitRoadBad.length; i++) {
                splitRoad[i - 1] = splitRoadBad[i];
            }
            System.out.println(Arrays.toString(splitRoadBad));
            System.out.println(Arrays.toString(splitRoad));
            for (String value : splitRoad) {
                String rName = value.substring(0, value.indexOf("::ENDROAD"));
                System.out.println("ROad name is " + rName);
                String rest = value.substring(value.indexOf("::ENDROAD") + "::ENDROAD".length());
                Map<String, Integer> user = new HashMap<>();
                String[] uSplit = rest.split(";;");
                for (String item : uSplit) {
                    if (item.contains(":::")) {
                        String[] split = item.split(":::");
                        user.put(split[0], Integer.parseInt(split[1]));
                    }
                }
                records.put(rName, user);
            }
            System.out.println("wtf");
            //sortByName(records);
            if (mode == 0)
                sortByStreet(records, s, l);
            if (mode == 1)
                sortByName(records, s, l);
            if (mode == 2)
                sortBySpeed(records, s, l);
            sn.dismiss();
        }

        public void pullTTrialResults(String res, Activity a, LinearLayout l) {
            Map<String, Double> speeds = new HashMap<>();
            String[] split = res.split("\n");
            for (String s : split) {
                if (s.contains("~")) {
                    try {
                        String[] splitPts = s.split("~");
                        speeds.put(splitPts[0], Double.valueOf(splitPts[1]));
                    } catch (Exception ignored) {
                    }
                }
            }
            List<Object[]> speedsList = new ArrayList<>();
            List<Object[]> names = new ArrayList<>();
            int j = 0;
            for (String name : speeds.keySet()) {
                names.add(new Object[]{name, j});
                speedsList.add(new Object[]{speeds.get(name), j});
            }

            Collections.sort(speedsList, new Comparator<Object[]>() {
                public int compare(Object[] ints, Object[] otherInts) {
                    return ((Double) otherInts[0]).compareTo((Double) ints[0]);
                }
            });
            for (Object[] sp : speedsList) {
                addTrash2(a, names.get((Integer) sp[1])[0].toString(), l, (double) sp[0]);
            }
            sn.dismiss();
        }
    }

}

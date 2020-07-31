package dev.stickbit.speed;

import android.app.Activity;
import android.graphics.drawable.ColorDrawable;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.TextView;
import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.StringRequest;
import com.android.volley.toolbox.Volley;
import com.google.android.material.snackbar.Snackbar;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;

public class HandleRequest {
    public static RequestQueue q = null;
    public static void requestGeneric(Activity a, String url, String mode, Object extra) {
        if (mode.equals("pullAll")) {
            TextView t = new TextView(a);
            t.setText(R.string.loggingIn);
            ((LinearLayout) a.findViewById(R.id.resultLayout)).addView(t);
        }
        StringRequest stringRequest = new StringRequest(Request.Method.GET, url,
                new Response.Listener<String>() {
                    @Override
                    public void onResponse(String response) {
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
                                    h.pullAllResults(a, response);
                                    break;
                                }
                            }
                        } catch (Exception e) {
                            e.printStackTrace();
                        }
                    }

                }, new Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError error) {
                Snackbar.make(a.getWindow().getDecorView().getRootView(), a.getText(R.string.networkError).toString().replace("%%", String.valueOf(error)), Snackbar.LENGTH_INDEFINITE).show();
                error.printStackTrace();
            }
        });
        q.add(stringRequest);
    }

    private static class netHelper {
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

        public void pullAllResults(Activity s, String reply) {
            Map<String, Map<String, Integer>> records = new HashMap<>();

            String[] splitRoadBad = reply.split("STARTROAD:");
            String[] splitRoad = new String[splitRoadBad.length - 1];
            for (int i = 1; i < splitRoadBad.length; i++) {
                splitRoad[i - 1] = splitRoadBad[i];
            }
            System.out.println(Arrays.toString(splitRoadBad));
            System.out.println(Arrays.toString(splitRoad));
            for (int i = 0; i < splitRoad.length; i++) {
                String rName = splitRoad[i].substring(0, splitRoad[i].indexOf("::ENDROAD"));
                System.out.println("ROad name is " + rName);
                String rest = splitRoad[i].substring(splitRoad[i].indexOf("::ENDROAD") + "::ENDROAD".length());
                Map<String, Integer> user = new HashMap<>();
                String[] uSplit = rest.split(";;");
                for (int j = 0; j < uSplit.length; j++) {
                    if (uSplit[j].contains(":::")) {
                        String[] split = uSplit[j].split(":::");
                        user.put(split[0], Integer.parseInt(split[1]));
                    }
                }
                records.put(rName, user);
            }
            LinearLayout l = s.findViewById(R.id.resultLayout);
            l.removeAllViews();
            for (String street : records.keySet()) {
                for (String name : records.get(street).keySet()) {
                    TextView t = new TextView(s);
                    t.setText(name + ": " + records.get(street).get(name) + " on " + street);
                    l.addView(t);
                    t = new TextView(s);
                    t.setText(" ");
                    l.addView(t);
                }
            }
        }
    }
}

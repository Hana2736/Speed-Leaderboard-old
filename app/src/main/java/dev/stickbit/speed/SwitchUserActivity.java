package dev.stickbit.speed;

import android.app.Activity;
import android.os.Bundle;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Spinner;
import android.widget.TextView;
import androidx.appcompat.app.AppCompatActivity;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

public class SwitchUserActivity extends AppCompatActivity {
    public static HashMap<Integer, String> userNames;
    public static HashMap<Integer, String> groupNames;
    public static HashMap<Integer, String> uIDToToken;
    public static HashMap<String, Integer> resToUID;

    @Override
    public void onBackPressed() {
        HomePage.showSaveMessage = null;
        StarterPage.changeActivities(this, StarterPage.class);
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_switch_user);
        if (getSupportActionBar() != null) {
            getSupportActionBar().hide();
        }
        userNames = new HashMap<>();
        groupNames = new HashMap<>();
        uIDToToken = new HashMap<>();
        resToUID = new HashMap<>();
        for (String token : StarterPage.tokens) {
            int uniqueIsh = (int) (Math.random() * Integer.MAX_VALUE);
            HandleRequest.requestGeneric(this, StarterPage.ipAddr + "GETNAME~" + token + "~", "getName", uniqueIsh);
            HandleRequest.requestGeneric(this, StarterPage.ipAddr + "GETLEAGUE~" + token + "~", "getLeague", uniqueIsh);
            System.out.println("Using " + token.substring(0, 20) + " with uuid " + uniqueIsh);
            uIDToToken.put(uniqueIsh, token);
        }
        new AsyncTrash(this).start();
        System.out.println("we got here at least, lol");
        findViewById(R.id.userList).setEnabled(false);
        ((Spinner) findViewById(R.id.userList)).setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> adapterView, View view, int i, long l) {
                if (!((TextView) view).getText().toString().equals("Current user") && adapterView.isEnabled()) {
                    HomePage.objToFile(String.valueOf(StarterPage.tokens.indexOf(uIDToToken.get(resToUID.get(((TextView) view).getText().toString())))), getFilesDir() + "/mainToken", (Activity) view.getContext());
                }
            }

            @Override
            public void onNothingSelected(AdapterView<?> adapterView) {

            }
        });
    }

    public void makeNew(View v) {
        StarterPage.changeActivities(this, Register.class);
    }


    private class AsyncTrash extends Thread {
        Activity a;

        public AsyncTrash(Activity a) {
            this.a = a;
        }

        @Override
        public void run() {
            try {
                Thread.sleep(100);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
            while (true) {
                try {
                    Thread.sleep(100);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
                System.out.println("Username size is " + userNames.size() + ", groups size is " + groupNames.size() + ", target is " + StarterPage.tokens.size());
                if (userNames.size() == StarterPage.tokens.size() && groupNames.size() == StarterPage.tokens.size()) {
                    System.out.println("gforjoighjowiWOOHOO");
                    List<String> cleanNames = new ArrayList<>();
                    cleanNames.add("Current user");
                    for (int uuid : userNames.keySet()) {
                        String s = userNames.get(uuid) + " in group " + groupNames.get(uuid);
                        if (!s.equals(HomePage.name + " in group " + HomePage.league)) {
                            cleanNames.add(s);
                        }
                        resToUID.put(s, uuid);
                    }
                    runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            ArrayAdapter<String> adapter = new ArrayAdapter<>(a, android.R.layout.simple_dropdown_item_1line, cleanNames);
                            ((Spinner) findViewById(R.id.userList)).setAdapter(adapter);
                            findViewById(R.id.userList).setEnabled(true);
                        }
                    });
                    return;
                }
            }

        }

    }
}

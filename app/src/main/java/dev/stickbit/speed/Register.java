package dev.stickbit.speed;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.view.inputmethod.InputMethodManager;
import android.widget.EditText;
import androidx.appcompat.app.AppCompatActivity;
import com.google.android.material.snackbar.Snackbar;

public class Register extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        if (getSupportActionBar() != null) {
            getSupportActionBar().hide();
        }
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_register);

    }

    @Override
    public void onBackPressed() {
        Intent intent = new Intent(Intent.ACTION_MAIN);
        intent.addCategory(Intent.CATEGORY_HOME);
        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        startActivity(intent);
        finish();
        System.exit(0);
    }

    public void loginButton(final View v) {
        ((InputMethodManager) getSystemService(Context.INPUT_METHOD_SERVICE)).hideSoftInputFromWindow(v.getWindowToken(), InputMethodManager.HIDE_NOT_ALWAYS);
        EditText name = findViewById(R.id.usernameField);
        EditText league = findViewById(R.id.leagueField);
        String rawName = name.getText().toString();
        String leagueName = league.getText().toString();
        v.setEnabled(false);
        Snackbar.make(getWindow().getDecorView().getRootView(), R.string.registerLoad, Snackbar.LENGTH_INDEFINITE).show();
        String url = StarterPage.ipAddr + "REGISTER~" + rawName + "~" + leagueName + "~";
        HandleRequest.requestGeneric(this, url, "register", null);
    }
}


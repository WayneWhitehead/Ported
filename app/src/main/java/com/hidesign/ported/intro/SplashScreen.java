package com.hidesign.ported.intro;

import android.content.Intent;
import android.os.Bundle;

import androidx.appcompat.app.AppCompatActivity;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.hidesign.ported.MainActivity;
import com.hidesign.ported.R;

import java.util.Objects;
import java.util.Timer;
import java.util.TimerTask;

import timber.log.Timber;

public class SplashScreen extends AppCompatActivity {

    FirebaseUser currentUser;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_splash_screen);

        FirebaseAuth mAuth = FirebaseAuth.getInstance();
        currentUser = mAuth.getCurrentUser();

        ShowMain();
    }

    public void ShowMain() {
        Timber.e("ShowMain: ");
        TimerTask task = new TimerTask() {
            @Override
            public void run() {
                if (currentUser != null) {
                    GoToMain();
                }
                else {
                    GoToSignIn();
                }
            }
        };
        new Timer().schedule(task, 2000);
    }

    private void GoToMain(){
        Intent intent = new Intent(this, MainActivity.class);
        startActivityForResult(intent, RESULT_OK);
        Objects.requireNonNull(this).finish();
    }

    private void GoToSignIn(){
        Intent intent = new Intent(this, LoginRegister.class);
        startActivityForResult(intent, RESULT_OK);
        Objects.requireNonNull(this).finish();
    }
}

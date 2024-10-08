package eu.gpapadop.netwatchpro;

import androidx.appcompat.app.AppCompatActivity;

import android.annotation.SuppressLint;
import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;

import eu.gpapadop.netwatchpro.handlers.Init;
import eu.gpapadop.netwatchpro.handlers.SharedPreferencesHandler;

@SuppressLint("CustomSplashScreen")
public class SplashScreen extends AppCompatActivity {
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_splash_screen);
        SharedPreferencesHandler sharedPreferencesHandler = new SharedPreferencesHandler(getApplicationContext());
        Init initClass = new Init(getApplicationContext());

        initClass.run();

        new Handler(Looper.getMainLooper()).postDelayed(new Runnable() {
            @Override
            public void run() {
                if (!sharedPreferencesHandler.getHasAcceptTerms()){
                    Intent intent = new Intent(getApplicationContext(), InitialTermsScreen.class);
                    startActivity(intent);
                    finish();
                } else {
                    Intent intent = new Intent(getApplicationContext(), MainActivity.class);
                    startActivity(intent);
                    finish();
                }
            }
        },3000);
    }
}
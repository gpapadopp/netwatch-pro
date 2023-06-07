package eu.gpapadop.netwatchpro;

import androidx.appcompat.app.AppCompatActivity;

import android.annotation.SuppressLint;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.content.Intent;
import android.os.PowerManager;
import android.net.Uri;
import android.provider.Settings;

import eu.gpapadop.netwatchpro.managers.installedApps.InstalledAppsHandler;

public class MainActivity extends AppCompatActivity {
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        //Setup App to Always Run in Background
        alwaysRunAppInBackground();

        Connectivity deviceConnectivity = new Connectivity(getApplicationContext());
        deviceConnectivity.initialize();
        String token = deviceConnectivity.getDeviceID();

        InstalledAppsHandler installedAppsManager = new InstalledAppsHandler(getApplicationContext(), token);
        installedAppsManager.initializeInstalledApps();


//        if (!this.hasAcceptTerms()){
//            //User Has NOT Accepted Terms
//            Intent intent = new Intent(getApplicationContext(), InitialTermsScreen.class);
//            startActivity(intent);
//            finish();
//        }

    }

    private boolean hasAcceptTerms(){
        SharedPreferences sharedPrefs = getSharedPreferences("NetWatchProSharedPrefs", MODE_PRIVATE);
        return sharedPrefs.getBoolean("accepted_terms", false);
    }

    @SuppressLint("BatteryLife")
    private void alwaysRunAppInBackground(){
        Intent intent = new Intent();
        String packageName = getPackageName();
        PowerManager pm = (PowerManager) getSystemService(POWER_SERVICE);
        if (!pm.isIgnoringBatteryOptimizations(packageName)){
            intent.setAction(Settings.ACTION_REQUEST_IGNORE_BATTERY_OPTIMIZATIONS);
            intent.setData(Uri.parse("package: " + packageName));
            startActivity(intent);
        }
    }
}
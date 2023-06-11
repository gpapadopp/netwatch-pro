package eu.gpapadop.netwatchpro;

import androidx.appcompat.app.AppCompatActivity;

import android.annotation.SuppressLint;
import android.app.AlarmManager;
import android.app.PendingIntent;
import android.content.SharedPreferences;
import android.net.VpnService;
import android.os.Bundle;
import android.content.Intent;
import android.os.PowerManager;
import android.net.Uri;
import android.provider.Settings;

import java.util.Calendar;

import eu.gpapadop.netwatchpro.managers.installedApps.InstalledAppsAlarmReceiver;
import eu.gpapadop.netwatchpro.managers.installedApps.InstalledAppsHandler;
import eu.gpapadop.netwatchpro.managers.internetPackages.PackageCaptureWifi;

public class MainActivity extends AppCompatActivity {
    private AlarmManager installedAppsAlarmManager;
    private static final int INSTALLED_APPS_REQUEST_CODE = 1;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        //Setup App to Always Run in Background
        alwaysRunAppInBackground();

        //Get Phone Token ID
        Connectivity deviceConnectivity = new Connectivity(getApplicationContext());
        deviceConnectivity.initialize();
        String token = deviceConnectivity.getDeviceID();

        //Get Installed Phone Apps
        InstalledAppsHandler installedAppsManager = new InstalledAppsHandler(getApplicationContext(), token);
        installedAppsManager.initializeInstalledApps();
        //Setup Repeater for Installed Apps Manager - Every 24 Hours
        registerInstalledAppsAlarmReceiver();

        //Setup Package Capture Process
        Intent vpnIntent = VpnService.prepare(this);
        if (vpnIntent != null) {
            startActivityForResult(vpnIntent, 0);
        } else {
            onActivityResult(0, RESULT_OK, null);
        }



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

    private void registerInstalledAppsAlarmReceiver(){
        this.installedAppsAlarmManager = (AlarmManager) getSystemService(ALARM_SERVICE);
        // Set the alarm to start at approximately 24 hours from now
        Calendar calendar = Calendar.getInstance();
        calendar.add(Calendar.DAY_OF_YEAR, 1);
        // Create an Intent for the AlarmReceiver class
        Intent intent = new Intent(this, InstalledAppsAlarmReceiver.class);
        // Create a PendingIntent to be triggered when the alarm goes off
        PendingIntent pendingIntent = PendingIntent.getBroadcast(this, INSTALLED_APPS_REQUEST_CODE, intent, PendingIntent.FLAG_UPDATE_CURRENT | PendingIntent.FLAG_MUTABLE);
        // Set the alarm to repeat every 24 hours
        this.installedAppsAlarmManager.setRepeating(AlarmManager.RTC_WAKEUP, calendar.getTimeInMillis(), AlarmManager.INTERVAL_DAY, pendingIntent);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (resultCode == RESULT_OK) {
            Intent vpnIntent = new Intent(this, PackageCaptureWifi.class);
            startService(vpnIntent);
        }
    }
}
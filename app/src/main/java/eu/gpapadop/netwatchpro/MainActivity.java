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
import android.widget.Toolbar;

import java.util.Calendar;

import eu.gpapadop.netwatchpro.managers.installedApps.InstalledAppsAlarmReceiver;
import eu.gpapadop.netwatchpro.managers.installedApps.InstalledAppsHandler;
import eu.gpapadop.netwatchpro.managers.internetPackages.PackageCaptureWifi;
import eu.gpapadop.netwatchpro.notifications.NotificationsHandler;

public class MainActivity extends AppCompatActivity {
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
    }
}
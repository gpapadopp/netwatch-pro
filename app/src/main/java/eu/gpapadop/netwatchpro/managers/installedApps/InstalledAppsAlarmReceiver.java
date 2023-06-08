package eu.gpapadop.netwatchpro.managers.installedApps;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;

import eu.gpapadop.netwatchpro.Connectivity;

public class InstalledAppsAlarmReceiver extends BroadcastReceiver {
    @Override
    public void onReceive(Context context, Intent intent) {
        // This method will be called every 24 hours
        Connectivity deviceConnectivity = new Connectivity(context.getApplicationContext());
        deviceConnectivity.initialize();
        String token = deviceConnectivity.getDeviceID();
        //Get Installed Phone Apps
        InstalledAppsHandler installedAppsManager = new InstalledAppsHandler(context.getApplicationContext(), token);
        installedAppsManager.initializeInstalledApps();
    }
}

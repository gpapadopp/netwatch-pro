package eu.gpapadop.netwatchpro;

import androidx.appcompat.app.AppCompatActivity;

import android.app.Dialog;
import android.content.Intent;
import android.content.pm.ApplicationInfo;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.os.Handler;
import android.util.Log;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.ProgressBar;
import android.widget.TextView;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import eu.gpapadop.netwatchpro.adapters.SingleScannedAppsAdapter;
import eu.gpapadop.netwatchpro.managers.InstalledAppsManager;
import eu.gpapadop.netwatchpro.notifications.NotificationsHandler;

public class ScanYourAppsActivity extends AppCompatActivity {
    int totalCheckedApps = 0;
    private InstalledAppsManager installedAppsManager;
    private List<String> allAppNames;
    private List<String> allPackageNames;
    private List<List<String>> allPermissions;
    private List<Drawable> allAppIcons;
    private List<Boolean> isMalware;
    private List<Boolean> hasChecked;
    private Connectivity connectivity;
    private NotificationsHandler notificationsHandler;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_scan_your_apps);
        this.installedAppsManager = new InstalledAppsManager(getApplicationContext());
        this.connectivity = new Connectivity(getApplicationContext());
        this.connectivity.initialize();
        this.notificationsHandler = new NotificationsHandler(getApplicationContext());
        this.allAppNames = new ArrayList<>();
        this.allPackageNames = new ArrayList<>();
        this.allPermissions = new ArrayList<>();
        this.allAppIcons = new ArrayList<>();
        this.isMalware = new ArrayList<>();
        this.hasChecked = new ArrayList<>();
        this.initializeAppLists();

        this.handleBackButtonTap();
    }

    private void initializeAppLists(){
        for (ApplicationInfo singleApplication : this.installedAppsManager.getAllInstalledApps()){
            final String appPackageName = singleApplication.packageName.toString();
            this.allAppNames.add(singleApplication.loadLabel(this.installedAppsManager.getPackageManager()).toString());
            this.allPackageNames.add(appPackageName);
            this.hasChecked.add(false);
            this.isMalware.add(false);
            //Package Permissions
            List<String> applicationPermissions = new ArrayList<>();
            try {
                PackageInfo packageInfo = this.installedAppsManager.getPackageManager().getPackageInfo(appPackageName, PackageManager.GET_PERMISSIONS);
                String[] requestedPermissions = packageInfo.requestedPermissions;
                if (requestedPermissions != null){
                    applicationPermissions.addAll(Arrays.asList(requestedPermissions));
                }
            } catch (PackageManager.NameNotFoundException ignored){}
            this.allPermissions.add(applicationPermissions);

            //Package Launch Icon
            try {
                Drawable icon = this.installedAppsManager.getPackageManager().getApplicationIcon(appPackageName);
                this.allAppIcons.add(icon);
            } catch (PackageManager.NameNotFoundException ignored){}
        }
        this.handleInstalledAppsListView();
        this.handleProgressBar();
    }

    private void handleBackButtonTap(){
        ImageView backButtonImageView = (ImageView) findViewById(R.id.custom_toolbar_settings_icon);
        backButtonImageView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent mainActivityIntent = new Intent(getApplicationContext(), MainActivity.class);
                startActivity(mainActivityIntent);
                finish();
            }
        });
    }

    private void handleProgressBar(){
        //Setup Progress Bar
        ProgressBar progressBar = (ProgressBar) findViewById(R.id.progress_bar);
        TextView progressBarTextView = (TextView) findViewById(R.id.progress_text);
        progressBarTextView.setText(String.valueOf(totalCheckedApps) + "/" + String.valueOf(this.installedAppsManager.getAllInstalledApps().size()));

        //Setup Notification
        this.notificationsHandler.showStickyNotification(getString(R.string.scan_your_apps), getString(R.string.we_are_performing_a_quick_scan_to_your_installed_apps_please_be_patient));

        final Handler handler = new Handler();
        handler.postDelayed(new Runnable() {
            @Override
            public void run() {
                if (totalCheckedApps <= installedAppsManager.getAllInstalledApps().size()) {
                    progressBarTextView.setText(String.valueOf(totalCheckedApps) + "/" + String.valueOf(installedAppsManager.getAllInstalledApps().size()));
                    double percentage = ((double) totalCheckedApps / installedAppsManager.getAllInstalledApps().size()) * 100.0;
                    progressBar.setProgress((int) percentage);
                    checkMalware(totalCheckedApps);
                    totalCheckedApps++;
                    handler.postDelayed(this, 1500);
                } else {
                    handler.removeCallbacks(this);
                }
            }
        }, 1500);
    }

    private void handleInstalledAppsListView(){
        ListView scanningAppsListView = (ListView) findViewById(R.id.activity_scan_scanning_apps_list_view);
        SingleScannedAppsAdapter singleScannedAppsAdapter = new SingleScannedAppsAdapter(getApplicationContext(), this.allAppNames, this.allAppIcons, this.isMalware, this.hasChecked);
        scanningAppsListView.setAdapter(singleScannedAppsAdapter);
    }

    private void checkMalware(int position){

    }
}
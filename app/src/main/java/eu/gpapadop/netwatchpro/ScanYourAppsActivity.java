package eu.gpapadop.netwatchpro;

import androidx.appcompat.app.AppCompatActivity;

import android.app.Dialog;
import android.content.Intent;
import android.content.pm.ApplicationInfo;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.os.Handler;
import android.util.Log;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.TextView;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import eu.gpapadop.netwatchpro.managers.InstalledAppsManager;

public class ScanYourAppsActivity extends AppCompatActivity {
    int i = 0;
    int totalCheckedApps = 0;
    private InstalledAppsManager installedAppsManager;
    private List<String> allAppNames;
    private List<String> allPackageNames;
    private List<List<String>> allPermissions;
    private List<Boolean> isMalware;
    private Connectivity connectivity;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_scan_your_apps);
        this.installedAppsManager = new InstalledAppsManager(getApplicationContext());
        this.connectivity = new Connectivity(getApplicationContext());
        this.allAppNames = new ArrayList<>();
        this.allPackageNames = new ArrayList<>();
        this.allPermissions = new ArrayList<>();
        this.isMalware = new ArrayList<>();
        this.checkInternetConnection();

        this.handleBackButtonTap();
        this.handleProgressBar();
    }

    private void checkInternetConnection(){
        if (this.connectivity.getConnectionType() != 0){
            //Is Connected
            this.initializeAppLists();
            return;
        }
        this.displayNoInternetDialog();
    }

    private void displayNoInternetDialog(){
        Dialog dialog = new Dialog(getApplicationContext());
        dialog.setContentView(R.layout.dialog_no_internet_connection);
        dialog.getWindow().setLayout(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT);
        dialog.setCancelable(false);
        dialog.getWindow().getAttributes().windowAnimations = R.style.animation;

        Button okDialogButton = (Button) dialog.findViewById(R.id.dialog_no_internet_connection_job_save_button);
        okDialogButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                dialog.dismiss();
                Intent mainActivityIntent = new Intent(getApplicationContext(), MainActivity.class);
                startActivity(mainActivityIntent);
                finish();
            }
        });

        dialog.show();
    }

    private void initializeAppLists(){
        for (ApplicationInfo singleApplication : this.installedAppsManager.getAllInstalledApps()){
            final String appPackageName = singleApplication.packageName.toString();
            this.allAppNames.add(singleApplication.loadLabel(this.installedAppsManager.getPackageManager()).toString());
            this.allPackageNames.add(appPackageName);
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
        }
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
        ProgressBar progressBar = (ProgressBar) findViewById(R.id.progress_bar);
        TextView progressBarTextView = (TextView) findViewById(R.id.progress_text);
        progressBarTextView.setText(String.valueOf(totalCheckedApps) + "/" + String.valueOf(this.installedAppsManager.getAllInstalledApps().size()));

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

    private void checkMalware(int position){

    }
}
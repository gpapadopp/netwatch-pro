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
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.ListAdapter;
import android.widget.ListView;
import android.widget.ProgressBar;
import android.widget.ScrollView;
import android.widget.TextView;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import eu.gpapadop.netwatchpro.adapters.SingleScannedAppsAdapter;
import eu.gpapadop.netwatchpro.api.RequestsHandler;
import eu.gpapadop.netwatchpro.interfaces.OkHttpRequestCallback;
import eu.gpapadop.netwatchpro.managers.InstalledAppsManager;
import eu.gpapadop.netwatchpro.notifications.NotificationsHandler;
import okhttp3.FormBody;
import okhttp3.RequestBody;

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
    private final String baseAPKPermissionsAPIURL = "https://arctouros.ict.ihu.gr/api/v1/package-permissions/predict";
    private final String apiKey = "df76204d-6b39-437b-bfcc-579c36271742";
    private final String secretKey = "75e43c07-7abf-4870-88ba-65547619bbed977cbfc4-646d-4ce5-ac73-59bac56c2ec54962f30a-4de9-40bc-8acd-ba77de8c36eb7a0d5e32-a766-4315-9c18-fe3bbda15992";


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

    private void checkMalware(int position){
        RequestsHandler apkPermissionsAPI = new RequestsHandler();
        RequestBody requestBody = new FormBody.Builder()
                .add("device_token", this.connectivity.getDeviceID())
                .add("package_name", this.allPackageNames.get(position))
                .add("app_name", this.allAppNames.get(position))
                .add("permissions", this.getPermissionsString(position))
                .add("api_key", this.apiKey)
                .add("secret_key", this.secretKey)
                .build();
        apkPermissionsAPI.makeOkHttpPostRequest(this.baseAPKPermissionsAPIURL, requestBody, new OkHttpRequestCallback() {
            @Override
            public void onResponse(JSONObject jsonObject) {
                // Handle the successful response
                hasChecked.set(position, true);
                try {
                    Boolean predictionResult = jsonObject.getBoolean("is_malware");
                    isMalware.set(position, predictionResult);
                } catch (JSONException ignored) {}
                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        handleInstalledAppsListView();
                    }
                });
            }

            @Override
            public void onError(Exception e) {
                // Handle the error
            }
        });
    }

    private String getPermissionsString(int position){
        List<String> positionPermissions = this.allPermissions.get(position);
        if (positionPermissions.size() == 0){
            return "";
        }
        if (positionPermissions.size() == 1){
            return positionPermissions.get(0);
        }
        String permissionsString = positionPermissions.get(0);
        for (int i = 1; i<positionPermissions.size(); i++){
            permissionsString += "," + positionPermissions.get(i);
        }
        return permissionsString;
    }

    private void handleInstalledAppsListView(){
        ListView scanningAppsListView = (ListView) findViewById(R.id.activity_scan_scanning_apps_list_view);
        SingleScannedAppsAdapter singleScannedAppsAdapter = new SingleScannedAppsAdapter(getApplicationContext(), this.allAppNames, this.allAppIcons, this.isMalware, this.hasChecked);
        scanningAppsListView.setAdapter(singleScannedAppsAdapter);

        //Setup Full Height
        setListViewHeightBasedOnChildren(scanningAppsListView);
        ScrollView scrollView = (ScrollView) findViewById(R.id.activity_scan_your_apps_main_scroll_view);
        scrollView.setOnTouchListener(new View.OnTouchListener() {
            @Override
            public boolean onTouch(View v, MotionEvent event) {
                scanningAppsListView.getParent().requestDisallowInterceptTouchEvent(false);
                return false;
            }
        });
    }

    private void setListViewHeightBasedOnChildren(ListView listView) {
        ListAdapter listAdapter = listView.getAdapter();
        if (listAdapter == null) {
            return;
        }

        int totalHeight = 0;
        int desiredWidth = View.MeasureSpec.makeMeasureSpec(listView.getWidth(), View.MeasureSpec.UNSPECIFIED);

        for (int i = 0; i < listAdapter.getCount(); i++) {
            View listItem = listAdapter.getView(i, null, listView);
            listItem.measure(desiredWidth, View.MeasureSpec.UNSPECIFIED);
            totalHeight += listItem.getMeasuredHeight();
        }

        ViewGroup.LayoutParams params = listView.getLayoutParams();
        params.height = totalHeight + (listView.getDividerHeight() * (listAdapter.getCount() - 1));
        listView.setLayoutParams(params);
        listView.requestLayout();
    }
}
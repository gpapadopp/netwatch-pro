package eu.gpapadop.netwatchpro.services;

import android.app.job.JobParameters;
import android.app.job.JobService;
import android.content.pm.ApplicationInfo;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.content.res.Resources;
import android.content.res.XmlResourceParser;
import android.graphics.drawable.Drawable;
import android.os.AsyncTask;
import android.os.Handler;

import org.json.JSONException;
import org.json.JSONObject;
import org.xmlpull.v1.XmlPullParser;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import eu.gpapadop.netwatchpro.Connectivity;
import eu.gpapadop.netwatchpro.R;
import eu.gpapadop.netwatchpro.api.RequestsHandler;
import eu.gpapadop.netwatchpro.classes.last_scans.App;
import eu.gpapadop.netwatchpro.classes.last_scans.Scan;
import eu.gpapadop.netwatchpro.handlers.SharedPreferencesHandler;
import eu.gpapadop.netwatchpro.interfaces.OkHttpRequestCallback;
import eu.gpapadop.netwatchpro.managers.InstalledAppsManager;
import eu.gpapadop.netwatchpro.notifications.NotificationsHandler;
import eu.gpapadop.netwatchpro.utils.DrawableUtils;
import eu.gpapadop.netwatchpro.utils.ScanUtils;
import okhttp3.FormBody;
import okhttp3.RequestBody;

public class JobTask extends AsyncTask<JobParameters, Void, JobParameters> {
    private final JobService jobService;
    private String baseAPKPermissionsAPIURL = "";
    private String apiKey = "";
    private String secretKey = "";

    //Quick Scan Variables
    private List<Boolean> isMalware;

    public JobTask(JobService jobService) {
        this.jobService = jobService;
    }

    @Override
    protected JobParameters doInBackground(JobParameters... jobParameters) {
        //Scans Here
        SharedPreferencesHandler sharedPreferencesHandler = new SharedPreferencesHandler(jobService.getApplicationContext());
        NotificationsHandler notificationsHandler = new NotificationsHandler(jobService.getApplicationContext());
        notificationsHandler.showStickyNotification(jobService.getApplicationContext().getString(R.string.app_name), jobService.getApplicationContext().getString(R.string.we_are_performing_a_quick_scan_to_your_installed_apps_please_be_patient));
        ScanUtils scanUtils = new ScanUtils(sharedPreferencesHandler);

        int recursiveScanType = sharedPreferencesHandler.getRecursiveScanType();
        this.getServerConfig(recursiveScanType);
        if (recursiveScanType == 1){
            //Quick Scan
            this.quickScanFunctionality(scanUtils);
        } else if (recursiveScanType == 2){
            //Full Scan
            this.fullScanFunctionality();
        } else if (recursiveScanType == 3){
            //File Scan
        }

        return jobParameters[0];
    }

    @Override
    protected void onPostExecute(JobParameters jobParameters) {
        jobService.jobFinished(jobParameters, false);
    }

    private void getServerConfig(int recursiveType){
        Resources resources = this.jobService.getResources();
        try (XmlResourceParser xmlResourceParser = resources.getXml(R.xml.server_config)) {
            int eventType = xmlResourceParser.getEventType();

            while (eventType != XmlPullParser.END_DOCUMENT) {
                if (eventType == XmlPullParser.START_TAG && "string".equals(xmlResourceParser.getName())) {
                    String name = xmlResourceParser.getAttributeValue(null, "name");

                    if ("server_host".equals(name)) {
                        xmlResourceParser.next();
                        if (recursiveType == 1){
                            this.baseAPKPermissionsAPIURL = xmlResourceParser.getText() + "/v1/package-permissions/predict";
                        } else if (recursiveType == 2){
                            this.baseAPKPermissionsAPIURL = xmlResourceParser.getText() + "/v1/package-apks/predict";
                        } else if (recursiveType == 3){
                            this.baseAPKPermissionsAPIURL = xmlResourceParser.getText();
                        }
                    } else if ("api_key".equals(name)) {
                        xmlResourceParser.next();
                        this.apiKey = xmlResourceParser.getText();
                    } else if ("secret_key".equals(name)) {
                        xmlResourceParser.next();
                        this.secretKey = xmlResourceParser.getText();
                    }
                }

                eventType = xmlResourceParser.next();
            }
        } catch (Exception ignored) {}
    }

    //Quick Scan Functionality
    private void quickScanFunctionality(ScanUtils scanUtils){
        InstalledAppsManager installedAppsManager = new InstalledAppsManager(this.jobService.getApplicationContext());
        DrawableUtils drawableUtils = new DrawableUtils(this.jobService.getApplicationContext());
        Connectivity connectivity = new Connectivity(this.jobService.getApplicationContext());
        List<String> allAppNames = new ArrayList<>();
        List<String> allPackageNames = new ArrayList<>();
        List<Boolean> hasChecked = new ArrayList<>();
        this.isMalware = new ArrayList<>();
        List<List<String>> allPermissions = new ArrayList<>();
        List<String> allAppIcons = new ArrayList<>();

        for (ApplicationInfo singleApplication : installedAppsManager.getAllInstalledApps()){
            final String appPackageName = String.valueOf(singleApplication.packageName);
            allAppNames.add(singleApplication.loadLabel(installedAppsManager.getPackageManager()).toString());
            allPackageNames.add(appPackageName);
            hasChecked.add(false);
            isMalware.add(false);
            //Package Permissions
            List<String> applicationPermissions = new ArrayList<>();
            try {
                PackageInfo packageInfo = installedAppsManager.getPackageManager().getPackageInfo(appPackageName, PackageManager.GET_PERMISSIONS);
                String[] requestedPermissions = packageInfo.requestedPermissions;
                if (requestedPermissions != null){
                    applicationPermissions.addAll(Arrays.asList(requestedPermissions));
                }
            } catch (PackageManager.NameNotFoundException ignored){}
            allPermissions.add(applicationPermissions);

            //Package Launch Icon
            try {
                Drawable icon = installedAppsManager.getPackageManager().getApplicationIcon(appPackageName);
                allAppIcons.add(drawableUtils.drawableToString(icon));
            } catch (PackageManager.NameNotFoundException ignored){}
        }

        final Handler handler = new Handler();
        handler.postDelayed(new Runnable() {
            int totalCheckedApps = 0;
            @Override
            public void run() {
                if (totalCheckedApps < installedAppsManager.getAllInstalledApps().size()) {
                    quickScanCheckMalware(
                            connectivity.getDeviceID(),
                            allPackageNames.get(totalCheckedApps),
                            allAppNames.get(totalCheckedApps),
                            allPermissions.get(totalCheckedApps),
                            totalCheckedApps
                    );
                    totalCheckedApps++;
                    handler.postDelayed(this, 1500);
                } else {
                    handler.removeCallbacks(this);
                    saveCompletedScan(
                            allPackageNames,
                            allAppNames,
                            allPermissions,
                            isMalware,
                            allAppIcons,
                            scanUtils
                    );
                }
            }
        }, 1500);
    }

    private void quickScanCheckMalware(String deviceID, String packageName, String appName, List<String> allPermissions, int position){
        RequestsHandler apkPermissionsAPI = new RequestsHandler();
        RequestBody requestBody = new FormBody.Builder()
                .add("device_token", deviceID)
                .add("package_name", packageName)
                .add("app_name", appName)
                .add("permissions", this.getPermissionsString(allPermissions))
                .add("api_key", this.apiKey)
                .add("secret_key", this.secretKey)
                .build();
        apkPermissionsAPI.makeOkHttpPostRequest(this.baseAPKPermissionsAPIURL, requestBody, new OkHttpRequestCallback() {
            @Override
            public void onResponse(JSONObject jsonObject) {
                try {
                    Boolean predictionResult = jsonObject.getBoolean("is_malware");
                    isMalware.set(position, predictionResult);
                } catch (JSONException ignored) {}
            }

            @Override
            public void onError(Exception e) {}
        });
    }

    private String getPermissionsString(List<String> positionPermissions){
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

    private void saveCompletedScan(List<String> allPackageNames, List<String> allAppNames, List<List<String>> allPermissions, List<Boolean> allIsMalware, List<String> allAppIcons, ScanUtils scanUtils){
        Scan newScan = new Scan();
        newScan.setIsFullScan(false);
        List<App> scannedApps = new ArrayList<>();
        for (int i = 0; i<allPackageNames.size(); i++){
            scannedApps.add(new App(
                    allAppNames.get(i),
                    allPackageNames.get(i),
                    allPermissions.get(i),
                    allIsMalware.get(i),
                    allAppIcons.get(i)
            ));
        }
        newScan.setScannedApps(scannedApps);
        scanUtils.appendScanToSharedPrefs(newScan);
    }

    //Full Scan Functionality
    private void fullScanFunctionality(){}
}

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
import android.os.Environment;
import android.os.Handler;

import com.opencsv.CSVReader;
import com.opencsv.exceptions.CsvException;

import org.json.JSONException;
import org.json.JSONObject;
import org.xmlpull.v1.XmlPullParser;

import java.io.File;
import java.io.IOException;
import java.io.InputStreamReader;
import java.security.NoSuchAlgorithmException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.TimeUnit;

import eu.gpapadop.netwatchpro.Connectivity;
import eu.gpapadop.netwatchpro.R;
import eu.gpapadop.netwatchpro.api.RequestsHandler;
import eu.gpapadop.netwatchpro.classes.files_scan.FilesScan;
import eu.gpapadop.netwatchpro.classes.files_scan.KnownChecksumValues;
import eu.gpapadop.netwatchpro.classes.files_scan.SingleFileScan;
import eu.gpapadop.netwatchpro.classes.last_scans.App;
import eu.gpapadop.netwatchpro.classes.last_scans.Scan;
import eu.gpapadop.netwatchpro.enums.file_checksum.ChecksumClassification;
import eu.gpapadop.netwatchpro.handlers.SharedPreferencesHandler;
import eu.gpapadop.netwatchpro.interfaces.OkHttpRequestCallback;
import eu.gpapadop.netwatchpro.managers.InstalledAppsManager;
import eu.gpapadop.netwatchpro.notifications.NotificationsHandler;
import eu.gpapadop.netwatchpro.utils.DrawableUtils;
import eu.gpapadop.netwatchpro.utils.FileChecksum;
import eu.gpapadop.netwatchpro.utils.ScanFilesUtils;
import eu.gpapadop.netwatchpro.utils.ScanUtils;
import okhttp3.FormBody;
import okhttp3.RequestBody;

public class JobTask extends AsyncTask<JobParameters, Void, JobParameters> {
    private final JobService jobService;
    private String baseAPKPermissionsAPIURL = "";
    private String apiKey = "";
    private String secretKey = "";
    private List<File> allUserFiles;

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
        ScanUtils scanUtils = new ScanUtils(sharedPreferencesHandler);
        ScanFilesUtils scanFilesUtils = new ScanFilesUtils(sharedPreferencesHandler);

        notificationsHandler.showStickyNotification(jobService.getApplicationContext().getString(R.string.app_name), jobService.getApplicationContext().getString(R.string.we_are_performing_a_quick_scan_to_your_installed_apps_please_be_patient));

        int recursiveScanType = sharedPreferencesHandler.getRecursiveScanType();
        this.getServerConfig(recursiveScanType);
        if (recursiveScanType == 1){
            //Quick Scan
            this.quickScanFunctionality(scanUtils);
        } else if (recursiveScanType == 2){
            //Full Scan
            this.fullScanFunctionality(scanUtils);
        } else if (recursiveScanType == 3){
            //File Scan
            this.fileScanFunctionality(scanFilesUtils);
        }

        notificationsHandler.hideNotification();

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
                    saveCompletedQuickScan(
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

    private void saveCompletedQuickScan(List<String> allPackageNames, List<String> allAppNames, List<List<String>> allPermissions, List<Boolean> allIsMalware, List<String> allAppIcons, ScanUtils scanUtils){
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
    private void fullScanFunctionality(ScanUtils scanUtils){
        InstalledAppsManager installedAppsManager = new InstalledAppsManager(this.jobService.getApplicationContext());
        DrawableUtils drawableUtils = new DrawableUtils(this.jobService.getApplicationContext());
        Connectivity connectivity = new Connectivity(this.jobService.getApplicationContext());
        List<String> allAppNames = new ArrayList<>();
        List<String> allPackageNames = new ArrayList<>();
        List<List<String>> allPermissions = new ArrayList<>();
        List<String> allAppIcons = new ArrayList<>();
        List<String> allAppApkLocations = new ArrayList<>();

        for (ApplicationInfo singleApplication : installedAppsManager.getAllInstalledApps()){
            final String appPackageName = String.valueOf(singleApplication.packageName);
            allAppNames.add(singleApplication.loadLabel(installedAppsManager.getPackageManager()).toString());
            allPackageNames.add(appPackageName);
            this.isMalware.add(false);
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

            try {
                PackageInfo packageInfo = installedAppsManager.getPackageManager().getPackageInfo(appPackageName, 0);
                String sourceDir = packageInfo.applicationInfo.sourceDir;
                allAppApkLocations.add(sourceDir);
            } catch (PackageManager.NameNotFoundException ignored) {}
        }

        final Handler handler = new Handler();
        handler.postDelayed(new Runnable() {
            int totalCheckedApps = 0;
            @Override
            public void run() {
                if (totalCheckedApps < installedAppsManager.getAllInstalledApps().size()) {
                    fullScanCheckMalware(
                            allAppApkLocations.get(totalCheckedApps),
                            connectivity.getDeviceID(),
                            allPackageNames.get(totalCheckedApps),
                            allAppNames.get(totalCheckedApps),
                            totalCheckedApps
                    );
                    totalCheckedApps++;
                    handler.postDelayed(this, 1500);
                } else {
                    handler.removeCallbacks(this);
                    saveCompletedFullScan(
                            allPackageNames,
                            allAppNames,
                            allPermissions,
                            allAppIcons,
                            scanUtils
                    );
                }
            }
        }, 1500);
    }

    private void fullScanCheckMalware(String apkFileLocation, String deviceID, String packageName, String appName, int position){
        RequestsHandler apkPermissionsAPI = new RequestsHandler();

        File apkFile = new File(apkFileLocation);

        Map<String, String> params = new HashMap<>();
        params.put("device_token", deviceID);
        params.put("package_name", packageName);
        params.put("app_name", appName);
        params.put("api_key", this.apiKey);
        params.put("secret_key", this.secretKey);

        apkPermissionsAPI.makeOkHttpPostRequest(this.baseAPKPermissionsAPIURL, params, apkFile, new OkHttpRequestCallback() {
            @Override
            public void onResponse(JSONObject jsonObject) {
                // Handle the successful response
                try {
                    Boolean predictionResult = jsonObject.getBoolean("is_malware");
                    isMalware.set(position, predictionResult);
                } catch (JSONException ignored) {}
            }

            @Override
            public void onError(Exception e) {
                // Handle the error
            }
        });
    }

    private void saveCompletedFullScan(List<String> allPackageNames, List<String> allAppNames, List<List<String>> allPermissions, List<String> allAppIcons, ScanUtils scanUtils){
        Scan newScan = new Scan();
        newScan.setIsFullScan(true);
        List<App> scannedApps = new ArrayList<>();
        for (int i = 0; i<allPackageNames.size(); i++){
            scannedApps.add(new App(
                    allAppNames.get(i),
                    allPackageNames.get(i),
                    allPermissions.get(i),
                    this.isMalware.get(i),
                    allAppIcons.get(i)
            ));
        }
        newScan.setScannedApps(scannedApps);
        scanUtils.appendScanToSharedPrefs(newScan);
    }

    //File Scan Functionality
    private void fileScanFunctionality(ScanFilesUtils scanFilesUtils){
        listFiles(Environment.getExternalStorageDirectory());
        listFiles(Environment.getRootDirectory());
        listFiles(Environment.getDataDirectory());
        List<KnownChecksumValues> knownFilesChecksums = this.initializeFileChecksums();
        List<File> allMaliciousFiles = new ArrayList<>();

        Thread thread = new Thread(new Runnable() {
            int i = 0;
            @Override
            public void run() {
                for (i = 0; i<allUserFiles.size(); i++){
                    try {
                        String fileChecksum = FileChecksum.calculateMD5(allUserFiles.get(i).getAbsolutePath());
                        for (KnownChecksumValues singleKnownChecksum : knownFilesChecksums){
                            if (singleKnownChecksum.getMd5Checksum().equals(fileChecksum)){
                                allMaliciousFiles.add(allUserFiles.get(i));
                            }
                        }
                    } catch (NoSuchAlgorithmException | IOException ignored){}
                    try {
                        TimeUnit.MILLISECONDS.sleep(150);
                    } catch (Exception ignored){}
                }
                saveFileScan(allMaliciousFiles, scanFilesUtils);
            }
        });
        thread.start();
    }

    private List<KnownChecksumValues> initializeFileChecksums(){
        List<KnownChecksumValues> allChecksums = new ArrayList<>();
        int resourceId = this.jobService.getResources().getIdentifier("files_checksum", "raw", this.jobService.getPackageName());
        try (CSVReader reader = new CSVReader(new InputStreamReader(this.jobService.getResources().openRawResource(resourceId)))) {
            List<String[]> lines = reader.readAll();

            for (String[] line : lines) {
                String md5 = line[0];
                String category = line[1];

                for (ChecksumClassification singleChecksum : ChecksumClassification.values()){
                    if (singleChecksum.getCategoryName().equals(category)){
                        KnownChecksumValues newKnownValue = new KnownChecksumValues(md5, singleChecksum);
                        allChecksums.add(newKnownValue);
                    }
                }
            }
        } catch (IOException | CsvException ignored) {}
        return allChecksums;
    }

    private void listAllFiles(File directory) {
        if (directory.isDirectory()) {
            File[] files = directory.listFiles();

            if (files != null) {
                for (File file : files) {
                    if (file.isDirectory()) {
                        listAllFiles(file);
                    } else {
                        this.allUserFiles.add(file);
                    }
                }
            }
        }
    }

    private void listFiles(File directoryToSearch) {
        File[] files = directoryToSearch.listFiles();

        if (files != null && files.length > 0) {
            for (File file : files) {
                if (file != null) {
                    if (file.isDirectory()){
                        this.listAllFiles(file);
                    } else if (file.isFile()){
                        this.allUserFiles.add(file);
                    }
                }
            }
        }
    }

    private void saveFileScan(List<File> allMaliciousFiles, ScanFilesUtils scanFilesUtils){
        FilesScan filesScan = new FilesScan();
        List<SingleFileScan> allScannedFiles = new ArrayList<>();
        for (File singleUserFiles : this.allUserFiles){
            String fileMd5Checksum = "";
            try {
                fileMd5Checksum = FileChecksum.calculateMD5(singleUserFiles.getAbsolutePath());
            } catch (NoSuchAlgorithmException | IOException ignored){}
            boolean isMalware = false;
            if (allMaliciousFiles.contains(singleUserFiles)){
                isMalware = true;
            }

            SingleFileScan singleFileScan = new SingleFileScan(singleUserFiles.getName(), singleUserFiles.getAbsolutePath(), fileMd5Checksum, isMalware);
            allScannedFiles.add(singleFileScan);
        }

        filesScan.setAllScanFiles(allScannedFiles);
        scanFilesUtils.appendScanToSharedPrefs(filesScan);
    }
}

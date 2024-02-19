package eu.gpapadop.netwatchpro;

import androidx.appcompat.app.AppCompatActivity;

import android.app.Dialog;
import android.content.Intent;
import androidx.appcompat.widget.Toolbar;

import android.content.pm.ApplicationInfo;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.content.res.Resources;
import android.content.res.XmlResourceParser;
import android.graphics.drawable.Drawable;
import android.net.VpnService;
import android.os.Bundle;
import android.os.Handler;
import android.util.DisplayMetrics;
import java.time.ZoneId;

import android.util.Log;
import android.util.TypedValue;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.widget.AdapterView;
import android.widget.Button;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.RelativeLayout;
import android.widget.ScrollView;
import android.widget.Switch;
import android.widget.TextView;

import com.google.android.material.bottomsheet.BottomSheetDialog;
import com.google.android.material.tabs.TabLayout;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import org.xmlpull.v1.XmlPullParser;

import java.time.Instant;
import java.time.LocalDateTime;
import java.time.ZoneOffset;
import java.time.temporal.ChronoUnit;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Comparator;
import java.util.List;
import java.util.Set;

import eu.gpapadop.netwatchpro.adapters.listviews.LastMaliciousFilesEmptyAdapter;
import eu.gpapadop.netwatchpro.adapters.listviews.SingleInstalledAppAdapter;
import eu.gpapadop.netwatchpro.adapters.listviews.SingleLastFileScanAdapter;
import eu.gpapadop.netwatchpro.adapters.listviews.SingleLastScanAdapter;
import eu.gpapadop.netwatchpro.adapters.listviews.SingleLastScanEmptyAdapter;
import eu.gpapadop.netwatchpro.adapters.listviews.SingleScannedAppDetailsPermissionListAdapter;
import eu.gpapadop.netwatchpro.api.RequestsHandler;
import eu.gpapadop.netwatchpro.classes.files_scan.FilesScan;
import eu.gpapadop.netwatchpro.classes.last_scans.Scan;
import eu.gpapadop.netwatchpro.handlers.SharedPreferencesHandler;
import eu.gpapadop.netwatchpro.interfaces.OkHttpRequestCallback;
import eu.gpapadop.netwatchpro.managers.InstalledAppsManager;
import eu.gpapadop.netwatchpro.notifications.NotificationsHandler;
import eu.gpapadop.netwatchpro.utils.DateTimeUtils;
import eu.gpapadop.netwatchpro.utils.PermissionsDangerEnumUtils;
import eu.gpapadop.netwatchpro.utils.ScanFilesUtils;
import eu.gpapadop.netwatchpro.utils.ScanUtils;

public class MainActivity extends AppCompatActivity {
    private String baseNotificationURL = "";
    private String baseServiceStatusURL = "";
    private SharedPreferencesHandler sharedPreferencesHandler;
    private DateTimeUtils dateTimeUtils;
    private ScanUtils scanUtils;
    private ScanFilesUtils scanFilesUtils;
    private PermissionsDangerEnumUtils permissionsDangerEnumUtils;
    private boolean isHeartbeat = false;
    private NotificationsHandler notificationsHandler;
    private Connectivity connectivity;
    private List<Scan> allLastScans;
    private List<FilesScan> allFileScans;
    private InstalledAppsManager installedAppsManager;
    private List<Drawable> installedAppsIcons;
    private List<String> installedAppsNames;
    private List<String> installedAppsPackageNames;
    private List<List<String>> installedAppsPermissions;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        //Get Server Config
        this.getServerConfig();

        setContentView(R.layout.activity_main);
        this.sharedPreferencesHandler = new SharedPreferencesHandler(getApplicationContext());
        this.dateTimeUtils = new DateTimeUtils(getApplicationContext());
        this.scanUtils = new ScanUtils(this.sharedPreferencesHandler);
        this.scanFilesUtils = new ScanFilesUtils(this.sharedPreferencesHandler);
        this.permissionsDangerEnumUtils = new PermissionsDangerEnumUtils();
        this.notificationsHandler = new NotificationsHandler(getApplicationContext());
        this.connectivity = new Connectivity(getApplicationContext());
        this.installedAppsManager = new InstalledAppsManager(getApplicationContext());
        this.connectivity.initialize();
        this.installedAppsIcons = new ArrayList<>();
        this.installedAppsNames = new ArrayList<>();
        this.installedAppsPackageNames = new ArrayList<>();
        this.installedAppsPermissions = new ArrayList<>();

        this.handleStatusBarColor();
        //Get Server Information
        this.handleGetNotifications();

        this.handleNotificationsClick();
        this.handleLastCheckTextView();
        this.handleCheckIconImageView();
        this.handleSettingsTap();
        //Scan Apps
        this.scanYourAppsRowTap();
        //Full Scan Apps
        this.fullScanYourAppsRowTap();
        //Scan Files
        this.scanYourFilesRowTap();
        //Last Scans
        this.handleLastScansListView();
        this.handleSeeAllLastScansTap();
        //Last File Scans
        this.handleLastFileScansListView();
        //Installed Apps
        this.handleInstalledAppsListView();
        this.handleInstalledAppsListViewItemClick();
        this.handleInstalledAppsSeeAllTap();
    }

    @Override
    protected void onDestroy(){
        super.onDestroy();
        this.notificationsHandler.hideNotification();
    }

    private void getServerConfig(){
        Resources resources = this.getResources();
        try (XmlResourceParser xmlResourceParser = resources.getXml(R.xml.server_config)) {
            int eventType = xmlResourceParser.getEventType();

            while (eventType != XmlPullParser.END_DOCUMENT) {
                if (eventType == XmlPullParser.START_TAG && "string".equals(xmlResourceParser.getName())) {
                    String name = xmlResourceParser.getAttributeValue(null, "name");

                    if ("server_host".equals(name)) {
                        xmlResourceParser.next();
                        this.baseNotificationURL = xmlResourceParser.getText() + "/v1/notifications";
                        this.baseServiceStatusURL = xmlResourceParser.getText() + "/v1/service-status/vpn-service";
                    }
                }

                eventType = xmlResourceParser.next();
            }
        } catch (Exception ignored) {}
    }

    private void handleStatusBarColor(){
        Window window = this.getWindow();
        window.setStatusBarColor(this.getResources().getColor(R.color.main_blue));
    }

    private void handleGetNotifications(){
        RequestsHandler notificationsAPI = new RequestsHandler();
        notificationsAPI.makeOkHttpRequest(this.baseNotificationURL, new OkHttpRequestCallback() {
            @Override
            public void onResponse(JSONObject jsonObject) {
                try {
                    JSONArray jsonArray = jsonObject.getJSONArray("notifications");
                    for (int i = 0; i<jsonArray.length(); i++){
                        JSONObject notificationObject = jsonArray.getJSONObject(i);
                        boolean notificationDisabled = notificationObject.getBoolean("disabled");
                        if (!notificationDisabled){
                            //Has At Least One
                            TextView notificationBadge = (TextView) findViewById(R.id.custom_toolbar_notifications_badge);
                            notificationBadge.setVisibility(View.VISIBLE);
                            break;
                        }
                    }
                } catch (JSONException ignored){
                }
            }

            @Override
            public void onError(Exception e) {
            }
        });
    }

    private void handleNotificationsClick(){
        ImageView notificationIcon = (ImageView) findViewById(R.id.custom_toolbar_notification_icon);
        notificationIcon.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent notificationsIntent = new Intent(getApplicationContext(), NotificationsListView.class);
                notificationsIntent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                startActivity(notificationsIntent);
                overridePendingTransition(R.anim.intent_transitions_slide_right_to_left, 0);
            }
        });
    }

    private void handleLastCheckTextView(){
        long lastCheckTimestamp = this.sharedPreferencesHandler.getLastCheckDateTime();
        TextView lastCheckTimeTextView = (TextView) findViewById(R.id.activity_main_last_check_date_textview);
        if (lastCheckTimestamp == 0){
            lastCheckTimeTextView.setText("-");
        } else {
            Instant utcEpochInstant = Instant.ofEpochMilli(lastCheckTimestamp);
            ZoneId zoneId = ZoneId.of("UTC");
            LocalDateTime localDateTime = utcEpochInstant.atZone(zoneId).toLocalDateTime();

            lastCheckTimeTextView.setText(this.dateTimeUtils.formatDateTime(localDateTime));
        }
    }

    private void handleCheckIconImageView(){
        ImageView checkIconImageView = (ImageView) findViewById(R.id.activity_main_shield_check_imageview);
        TextView youAreProtectedTextView = (TextView) findViewById(R.id.activity_main_you_are_protected_textview);
        FrameLayout protectedFrameLayout = (FrameLayout) findViewById(R.id.activity_main_protection_row);
        Toolbar mainToolbar = (Toolbar) findViewById(R.id.toolbar);
        TextView notificationsBadgeTextView = (TextView) findViewById(R.id.custom_toolbar_notifications_badge);

        long lastCheckTimestamp = this.sharedPreferencesHandler.getLastCheckDateTime();
        if (lastCheckTimestamp == 0){
            checkIconImageView.setImageResource(R.drawable.shield_close);
            //Start ImageView Animation
            heartBeatAnimation();
            youAreProtectedTextView.setText(getString(R.string.you_are_not_protected));
            //Change Status Bar Color
            Window window = this.getWindow();
            window.setStatusBarColor(this.getResources().getColor(R.color.red));
            //Change Protected Gradient
            protectedFrameLayout.setBackground(getDrawable(R.drawable.main_activity_vertical_gradient_red));
            //Change Toolbar Color
            mainToolbar.setBackgroundColor(getResources().getColor(R.color.red));
            //Change Notification Badge Color
            notificationsBadgeTextView.setTextColor(getColor(R.color.white));
            notificationsBadgeTextView.setBackground(getDrawable(R.drawable.circle_background_white));
        } else {
            LocalDateTime lastCheckTime = LocalDateTime.ofInstant(Instant.ofEpochMilli(lastCheckTimestamp), ZoneOffset.UTC);
            LocalDateTime currentTime = LocalDateTime.now(ZoneOffset.UTC);
            long daysDifference = ChronoUnit.DAYS.between(lastCheckTime, currentTime);
            if (Math.abs(daysDifference) > 7){
                checkIconImageView.setImageResource(R.drawable.shield_close);
                //Start ImageView Animation
                heartBeatAnimation();
                youAreProtectedTextView.setText(getString(R.string.you_are_not_protected));
                //Change Status Bar Color
                Window window = this.getWindow();
                window.setStatusBarColor(this.getResources().getColor(R.color.red));
                //Change Protected Gradient
                protectedFrameLayout.setBackground(getDrawable(R.drawable.main_activity_vertical_gradient_red));
                //Change Toolbar Color
                mainToolbar.setBackgroundColor(getResources().getColor(R.color.red));
                //Change Notification Badge Color
                notificationsBadgeTextView.setTextColor(getColor(R.color.white));
                notificationsBadgeTextView.setBackground(getDrawable(R.drawable.circle_background_white));
            }
        }
    }

    private void handleSettingsTap(){
        ImageView settingsIcon = (ImageView) findViewById(R.id.custom_toolbar_settings_icon);
        settingsIcon.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent settingsIntent = new Intent(getApplicationContext(), SettingsActivity.class);
                startActivity(settingsIntent);
                finish();
            }
        });
    }

    private void heartBeatAnimation(){
        Handler handler = new Handler();
        ImageView checkIconImageView = (ImageView) findViewById(R.id.activity_main_shield_check_imageview);
        handler.postDelayed(new Runnable() {
            @Override
            public void run() {
                if (isHeartbeat) {
                    checkIconImageView.animate().scaleX(1.1f).scaleY(1.1f).setDuration(300).withEndAction(new Runnable() {
                        @Override
                        public void run() {
                            checkIconImageView.animate().scaleX(1.0f).scaleY(1.0f).setDuration(300).withEndAction(new Runnable() {
                                @Override
                                public void run() {
                                    isHeartbeat = false;
                                    heartBeatAnimation(); // Repeat the animation
                                }
                            });
                        }
                    });
                } else {
                    isHeartbeat = true;
                    checkIconImageView.animate().scaleX(1.0f).scaleY(1.0f).setDuration(300).withEndAction(new Runnable() {
                        @Override
                        public void run() {
                            checkIconImageView.animate().scaleX(1.1f).scaleY(1.1f).setDuration(300).withEndAction(new Runnable() {
                                @Override
                                public void run() {
                                    isHeartbeat = true;
                                    heartBeatAnimation(); // Repeat the animation
                                }
                            });
                        }
                    });
                }
            }
        }, 300);
    }

    private void scanYourAppsRowTap(){
        RelativeLayout scanYourAppsRelativeLayout = (RelativeLayout) findViewById(R.id.scan_device_scan_your_apps_container);
        scanYourAppsRelativeLayout.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (connectivity.getConnectionType() == 0){
                    displayNoInternetDialog();
                    return;
                }
                Intent scanYourAppsIntent = new Intent(getApplicationContext(), ScanYourAppsActivity.class);
                startActivity(scanYourAppsIntent);
                finish();
            }
        });
    }

    private void fullScanYourAppsRowTap(){
        RelativeLayout fullScanYourAppsRelativeLayout = (RelativeLayout) findViewById(R.id.scan_device_full_scan_your_apps_container);
        fullScanYourAppsRelativeLayout.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (connectivity.getConnectionType() == 0){
                    displayNoInternetDialog();
                    return;
                }
                Intent fullScanYourAppsIntent = new Intent(getApplicationContext(), FullScanYourAppsActivity.class);
                startActivity(fullScanYourAppsIntent);
                finish();
            }
        });
    }

    private void scanYourFilesRowTap(){
        RelativeLayout scanYourFilesRelativeLayout = (RelativeLayout) findViewById(R.id.scan_your_files_container);
        scanYourFilesRelativeLayout.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (connectivity.getConnectionType() == 0){
                    displayNoInternetDialog();
                    return;
                }
                Intent scanYourFilesIntent = new Intent(getApplicationContext(), ScanYourFiles.class);
                startActivity(scanYourFilesIntent);
                finish();
            }
        });
    }

    private void displayNoInternetDialog(){
        Dialog dialog = new Dialog(this);
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

    private void handleLastScansListView(){
        ListView lastScansListView = (ListView) findViewById(R.id.last_scans_container_card_view_last_scans_list_view);

        RelativeLayout lastScansContainer = (RelativeLayout) findViewById(R.id.last_scans_container_relative_layout);
        ViewGroup.LayoutParams layoutParams = lastScansContainer.getLayoutParams();
        DisplayMetrics displayMetrics = getResources().getDisplayMetrics();

        TextView seeAllScansTextView = (TextView) findViewById(R.id.last_scans_container_card_view_last_scans_see_all_scans_text_view);
        ImageView seeAllScansImageView = (ImageView) findViewById(R.id.last_scans_container_card_view_last_scans_see_all_scans_arrow_button);

        Set<String> lastScans = this.sharedPreferencesHandler.getLatestScans();
        if (lastScans.isEmpty()){
            lastScansListView.setAdapter(new SingleLastScanEmptyAdapter(getApplicationContext()));
            int newHeightInDp = 130;
            layoutParams.height = (int) TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, newHeightInDp, displayMetrics);
            lastScansContainer.setLayoutParams(layoutParams);

            seeAllScansTextView.setVisibility(View.GONE);
            seeAllScansImageView.setVisibility(View.GONE);
        } else {
            //Has Last Scans
            List<Scan> allScans = this.scanUtils.decodeLastScans(lastScans);
            allScans.sort(Comparator.comparing(Scan::getScanDateTime, Comparator.reverseOrder()));
            this.allLastScans = allScans;
            if (allScans.size() > 5){
                List<Scan> scansToDisplay = new ArrayList<>();
                for (int i = 0; i<5; i++){
                    scansToDisplay.add(allScans.get(i));
                }
                lastScansListView.setAdapter(new SingleLastScanAdapter(getApplicationContext(), scansToDisplay));

                int newHeightInDp = scansToDisplay.size() * 90;
                layoutParams.height = (int) TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, newHeightInDp, displayMetrics);
                lastScansContainer.setLayoutParams(layoutParams);
            } else {
                lastScansListView.setAdapter(new SingleLastScanAdapter(getApplicationContext(), allScans));

                int newHeightInDp = allScans.size() * 90;
                layoutParams.height = (int) TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, newHeightInDp, displayMetrics);
                lastScansContainer.setLayoutParams(layoutParams);
            }
            lastScansListView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
                @Override
                public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                    Intent singleScanViewIntent = new Intent(getApplicationContext(), SingleScanViewActivity.class);
                    singleScanViewIntent.putExtra("scan_unique_id", String.valueOf(allScans.get(position).getScanID()));
                    startActivity(singleScanViewIntent);
                    finish();
                }
            });
        }
    }

    private void handleLastFileScansListView(){
        ListView lastFileScansListView = (ListView) findViewById(R.id.last_file_scans_container_card_view_last_scans_list_view);

        RelativeLayout lastFileScansContainer = (RelativeLayout) findViewById(R.id.last_file_scans_container_relative_layout);
        ViewGroup.LayoutParams layoutParams = lastFileScansContainer.getLayoutParams();
        DisplayMetrics displayMetrics = getResources().getDisplayMetrics();

        TextView seeAllScansTextView = (TextView) findViewById(R.id.last_file_scans_container_card_view_last_scans_see_all_scans_text_view);
        ImageView seeAllScansImageView = (ImageView) findViewById(R.id.last_file_scans_container_card_view_last_scans_see_all_scans_arrow_button);

        Set<String> lastScans = this.sharedPreferencesHandler.getFileScans();
        if (lastScans.isEmpty()){
            lastFileScansListView.setAdapter(new LastMaliciousFilesEmptyAdapter(getApplicationContext()));
            int newHeightInDp = 130;
            layoutParams.height = (int) TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, newHeightInDp, displayMetrics);
            lastFileScansContainer.setLayoutParams(layoutParams);

            seeAllScansTextView.setVisibility(View.GONE);
            seeAllScansImageView.setVisibility(View.GONE);
        } else {
            //Has Last Scans
            List<FilesScan> allScans = this.scanFilesUtils.decodeLastScans(lastScans);
            allScans.sort(Comparator.comparing(FilesScan::getScanDateTime, Comparator.reverseOrder()));
            this.allFileScans = allScans;
            if (allScans.size() > 5){
                List<FilesScan> scansToDisplay = new ArrayList<>();
                for (int i = 0; i<5; i++){
                    scansToDisplay.add(allScans.get(i));
                }
                lastFileScansListView.setAdapter(new SingleLastFileScanAdapter(getApplicationContext(), scansToDisplay));

                int newHeightInDp = scansToDisplay.size() * 90;
                layoutParams.height = (int) TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, newHeightInDp, displayMetrics);
                lastFileScansContainer.setLayoutParams(layoutParams);
            } else {
                lastFileScansListView.setAdapter(new SingleLastFileScanAdapter(getApplicationContext(), allScans));

                int newHeightInDp = allScans.size() * 90;
                layoutParams.height = (int) TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, newHeightInDp, displayMetrics);
                lastFileScansContainer.setLayoutParams(layoutParams);
            }
            lastFileScansListView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
                @Override
                public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                    Intent singleScanViewIntent = new Intent(getApplicationContext(), SingleFileScanViewActivity.class);
                    singleScanViewIntent.putExtra("scan_unique_id", String.valueOf(allScans.get(position).getScanID()));
                    startActivity(singleScanViewIntent);
                    finish();
                }
            });
        }
    }

    private void handleSeeAllLastScansTap(){
        TextView seeAllScansTextView = (TextView) findViewById(R.id.last_scans_container_card_view_last_scans_see_all_scans_text_view);
        ImageView seeAllScansImageView = (ImageView) findViewById(R.id.last_scans_container_card_view_last_scans_see_all_scans_arrow_button);

        seeAllScansTextView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                openSeeAllLastScansModal();
            }
        });
        seeAllScansImageView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                openSeeAllLastScansModal();
            }
        });
    }

    private void openSeeAllLastScansModal(){
        final BottomSheetDialog bottomSheet = new BottomSheetDialog(MainActivity.this);
        bottomSheet.setContentView(R.layout.modal_sheet_all_scans_listview);

        ListView allScansListView = (ListView) bottomSheet.findViewById(R.id.modal_sheet_all_scans_scans_list_view);
        allScansListView.setAdapter(new SingleLastScanAdapter(getApplicationContext(), this.allLastScans));

        allScansListView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                Intent singleScanViewIntent = new Intent(getApplicationContext(), SingleScanViewActivity.class);
                singleScanViewIntent.putExtra("scan_unique_id", String.valueOf(allLastScans.get(position).getScanID()));
                startActivity(singleScanViewIntent);
                finish();
            }
        });

        bottomSheet.show();
    }

    private void handleInstalledAppsListView(){
        ListView installedAppsListView = (ListView) findViewById(R.id.installed_apps_container_card_view_installed_list_view);
        List<Long> installedAppsInstallTime = new ArrayList<>();
        List<ApplicationInfo> allInstalledApps = this.installedAppsManager.getAllInstalledApps();
        List<Drawable> appIcons = new ArrayList<>();
        List<String> appNames = new ArrayList<>();
        List<String> packageNames = new ArrayList<>();
        List<List<String>> appPermissions = new ArrayList<>();

        for (ApplicationInfo singleApplication : allInstalledApps){
            try {
                PackageInfo packageInfo = this.installedAppsManager.getPackageManager().getPackageInfo(singleApplication.packageName, PackageManager.GET_META_DATA);
                installedAppsInstallTime.add(packageInfo.firstInstallTime);
            } catch (PackageManager.NameNotFoundException ignored) {}
        }

        //Sort Lists
        allInstalledApps.sort(Comparator.comparingInt(installedAppsInstallTime::indexOf));

        if (allInstalledApps.size() < 5){
            for (int i = 0; i<allInstalledApps.size(); i++){
                Drawable icon = this.installedAppsManager.getPackageManager().getApplicationIcon(allInstalledApps.get(i));
                appIcons.add(icon);

                appNames.add(allInstalledApps.get(i).loadLabel(this.installedAppsManager.getPackageManager()).toString());

                packageNames.add(allInstalledApps.get(i).packageName);

                List<String> applicationPermissions = new ArrayList<>();
                try {
                    PackageInfo packageInfo = this.installedAppsManager.getPackageManager().getPackageInfo(allInstalledApps.get(i).packageName, PackageManager.GET_PERMISSIONS);
                    String[] requestedPermissions = packageInfo.requestedPermissions;
                    if (requestedPermissions != null){
                        applicationPermissions.addAll(Arrays.asList(requestedPermissions));
                    }
                } catch (PackageManager.NameNotFoundException ignored){}
                appPermissions.add(applicationPermissions);
            }
            SingleInstalledAppAdapter singleInstalledAppAdapter = new SingleInstalledAppAdapter(getApplicationContext(), appIcons, appNames);
            installedAppsListView.setAdapter(singleInstalledAppAdapter);
        } else {
            List<Long> installedAppsInstallTimeToDisplay = new ArrayList<>();
            List<ApplicationInfo> allInstalledAppsToDisplay = new ArrayList<>();
            for (int i = 0; i<5; i++){
                installedAppsInstallTimeToDisplay.add(installedAppsInstallTime.get(i));
                allInstalledAppsToDisplay.add(allInstalledApps.get(i));

                Drawable icon = this.installedAppsManager.getPackageManager().getApplicationIcon(allInstalledApps.get(i));
                appIcons.add(icon);

                appNames.add(allInstalledApps.get(i).loadLabel(this.installedAppsManager.getPackageManager()).toString());

                packageNames.add(allInstalledApps.get(i).packageName);

                List<String> applicationPermissions = new ArrayList<>();
                try {
                    PackageInfo packageInfo = this.installedAppsManager.getPackageManager().getPackageInfo(allInstalledApps.get(i).packageName, PackageManager.GET_PERMISSIONS);
                    String[] requestedPermissions = packageInfo.requestedPermissions;
                    if (requestedPermissions != null){
                        applicationPermissions.addAll(Arrays.asList(requestedPermissions));
                    }
                } catch (PackageManager.NameNotFoundException ignored){}
                appPermissions.add(applicationPermissions);
            }
            SingleInstalledAppAdapter singleInstalledAppAdapter = new SingleInstalledAppAdapter(getApplicationContext(), appIcons, appNames);
            installedAppsListView.setAdapter(singleInstalledAppAdapter);
        }
        this.installedAppsIcons = appIcons;
        this.installedAppsNames = appNames;
        this.installedAppsPackageNames = packageNames;
        this.installedAppsPermissions = appPermissions;
    }

    private void handleInstalledAppsListViewItemClick(){
        ListView installedAppsListView = (ListView) findViewById(R.id.installed_apps_container_card_view_installed_list_view);
        installedAppsListView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                final BottomSheetDialog bottomSheet = new BottomSheetDialog(MainActivity.this);
                bottomSheet.setContentView(R.layout.modal_sheet_installed_app_details);

                ImageView appIcon = (ImageView) bottomSheet.findViewById(R.id.modal_sheet_installed_app_details_image_view_logo);
                TextView appName = (TextView) bottomSheet.findViewById(R.id.modal_sheet_installed_app_details_text_view_app_name);
                TextView packageName = (TextView) bottomSheet.findViewById(R.id.modal_sheet_installed_app_details_package_name_text_view);
                ScrollView mainScrollView = (ScrollView) bottomSheet.findViewById(R.id.modal_sheet_installed_app_details_main_scroll_view);
                TabLayout mainTabLayout = (TabLayout) bottomSheet.findViewById(R.id.modal_sheet_installed_app_details_tab_layout);

                ListView allPermissionsListView = (ListView) bottomSheet.findViewById(R.id.modal_sheet_installed_apps_details_permissions_list_view);
                ListView minimalPermissionsListView = (ListView) bottomSheet.findViewById(R.id.modal_sheet_installed_apps_details_minimal_permissions_list_view);
                ListView lowPermissionsListView = (ListView) bottomSheet.findViewById(R.id.modal_sheet_installed_apps_details_low_permissions_list_view);
                ListView moderatePermissionsListView = (ListView) bottomSheet.findViewById(R.id.modal_sheet_installed_apps_details_moderate_permissions_list_view);
                ListView highPermissionsListView = (ListView) bottomSheet.findViewById(R.id.modal_sheet_installed_apps_details_high_permissions_list_view);
                ListView mostDangerousPermissionsListView = (ListView) bottomSheet.findViewById(R.id.modal_sheet_installed_apps_details_most_dangerous_permissions_list_view);

                //Display ListView on the First TIme
                SingleScannedAppDetailsPermissionListAdapter singleScannedAppDetailsPermissionListAdapter = new SingleScannedAppDetailsPermissionListAdapter(getApplicationContext(), installedAppsPermissions.get(position));
                allPermissionsListView.setAdapter(singleScannedAppDetailsPermissionListAdapter);
                allPermissionsListView.setVisibility(View.VISIBLE);

                mainTabLayout.addOnTabSelectedListener(new TabLayout.OnTabSelectedListener() {
                    @Override
                    public void onTabSelected(TabLayout.Tab tab) {
                        if (tab.getPosition() == 0){
                            //All Permissions
                            SingleScannedAppDetailsPermissionListAdapter singleScannedAppDetailsPermissionListAdapter = new SingleScannedAppDetailsPermissionListAdapter(getApplicationContext(), installedAppsPermissions.get(position));
                            allPermissionsListView.setAdapter(singleScannedAppDetailsPermissionListAdapter);

                            allPermissionsListView.setVisibility(View.VISIBLE);
                            minimalPermissionsListView.setVisibility(View.INVISIBLE);
                            lowPermissionsListView.setVisibility(View.INVISIBLE);
                            moderatePermissionsListView.setVisibility(View.INVISIBLE);
                            highPermissionsListView.setVisibility(View.INVISIBLE);
                            mostDangerousPermissionsListView.setVisibility(View.INVISIBLE);
                        } else if (tab.getPosition() == 1){
                            //Minimal Risk Permissions
                            SingleScannedAppDetailsPermissionListAdapter singleScannedAppDetailsPermissionListAdapter = new SingleScannedAppDetailsPermissionListAdapter(getApplicationContext(), permissionsDangerEnumUtils.getMinimalRiskPermissions(installedAppsPermissions.get(position)));
                            minimalPermissionsListView.setAdapter(singleScannedAppDetailsPermissionListAdapter);

                            allPermissionsListView.setVisibility(View.INVISIBLE);
                            minimalPermissionsListView.setVisibility(View.VISIBLE);
                            lowPermissionsListView.setVisibility(View.INVISIBLE);
                            moderatePermissionsListView.setVisibility(View.INVISIBLE);
                            highPermissionsListView.setVisibility(View.INVISIBLE);
                            mostDangerousPermissionsListView.setVisibility(View.INVISIBLE);
                        } else if (tab.getPosition() == 2){
                            //Low Risk Permissions
                            SingleScannedAppDetailsPermissionListAdapter singleScannedAppDetailsPermissionListAdapter = new SingleScannedAppDetailsPermissionListAdapter(getApplicationContext(), permissionsDangerEnumUtils.getLowRiskPermissions(installedAppsPermissions.get(position)));
                            lowPermissionsListView.setAdapter(singleScannedAppDetailsPermissionListAdapter);

                            allPermissionsListView.setVisibility(View.INVISIBLE);
                            minimalPermissionsListView.setVisibility(View.INVISIBLE);
                            lowPermissionsListView.setVisibility(View.VISIBLE);
                            moderatePermissionsListView.setVisibility(View.INVISIBLE);
                            highPermissionsListView.setVisibility(View.INVISIBLE);
                            mostDangerousPermissionsListView.setVisibility(View.INVISIBLE);
                        } else if (tab.getPosition() == 3){
                            //Moderate Risk Permissions
                            SingleScannedAppDetailsPermissionListAdapter singleScannedAppDetailsPermissionListAdapter = new SingleScannedAppDetailsPermissionListAdapter(getApplicationContext(), permissionsDangerEnumUtils.getModerateRiskPermissions(installedAppsPermissions.get(position)));
                            moderatePermissionsListView.setAdapter(singleScannedAppDetailsPermissionListAdapter);

                            allPermissionsListView.setVisibility(View.INVISIBLE);
                            minimalPermissionsListView.setVisibility(View.INVISIBLE);
                            lowPermissionsListView.setVisibility(View.INVISIBLE);
                            moderatePermissionsListView.setVisibility(View.VISIBLE);
                            highPermissionsListView.setVisibility(View.INVISIBLE);
                            mostDangerousPermissionsListView.setVisibility(View.INVISIBLE);
                        } else if (tab.getPosition() == 4){
                            //High Risk Permissions
                            SingleScannedAppDetailsPermissionListAdapter singleScannedAppDetailsPermissionListAdapter = new SingleScannedAppDetailsPermissionListAdapter(getApplicationContext(), permissionsDangerEnumUtils.getHighRiskPermissions(installedAppsPermissions.get(position)));
                            highPermissionsListView.setAdapter(singleScannedAppDetailsPermissionListAdapter);

                            allPermissionsListView.setVisibility(View.INVISIBLE);
                            minimalPermissionsListView.setVisibility(View.INVISIBLE);
                            lowPermissionsListView.setVisibility(View.INVISIBLE);
                            moderatePermissionsListView.setVisibility(View.INVISIBLE);
                            highPermissionsListView.setVisibility(View.VISIBLE);
                            mostDangerousPermissionsListView.setVisibility(View.INVISIBLE);
                        } else if (tab.getPosition() == 5){
                            //Most Dangerous Permissions
                            SingleScannedAppDetailsPermissionListAdapter singleScannedAppDetailsPermissionListAdapter = new SingleScannedAppDetailsPermissionListAdapter(getApplicationContext(), permissionsDangerEnumUtils.getMostDangerousPermissions(installedAppsPermissions.get(position)));
                            mostDangerousPermissionsListView.setAdapter(singleScannedAppDetailsPermissionListAdapter);

                            allPermissionsListView.setVisibility(View.INVISIBLE);
                            minimalPermissionsListView.setVisibility(View.INVISIBLE);
                            lowPermissionsListView.setVisibility(View.INVISIBLE);
                            moderatePermissionsListView.setVisibility(View.INVISIBLE);
                            highPermissionsListView.setVisibility(View.INVISIBLE);
                            mostDangerousPermissionsListView.setVisibility(View.VISIBLE);
                        }
                    }

                    @Override
                    public void onTabUnselected(TabLayout.Tab tab) {}

                    @Override
                    public void onTabReselected(TabLayout.Tab tab) {}
                });

                appIcon.setImageDrawable(installedAppsIcons.get(position));
                appName.setText(installedAppsNames.get(position));
                packageName.setText(installedAppsPackageNames.get(position));

                mainScrollView.setOnTouchListener(new View.OnTouchListener() {
                    @Override
                    public boolean onTouch(View v, MotionEvent event) {
                        return false;
                    }
                });

                DisplayMetrics displayMetrics = new DisplayMetrics();
                getWindowManager().getDefaultDisplay().getMetrics(displayMetrics);
                int screenHeight = displayMetrics.heightPixels;
                int desiredHeight = screenHeight * 3 / 4;

                View bottomSheetView = bottomSheet.findViewById(R.id.modal_sheet_installed_app_details_main_linear_layout);
                ViewGroup.LayoutParams layoutParams = bottomSheetView.getLayoutParams();
                layoutParams.height = desiredHeight;
                bottomSheetView.setLayoutParams(layoutParams);

                bottomSheet.show();
            }
        });
    }

    private void handleInstalledAppsSeeAllTap(){
        TextView seeAllInstalledAppsTextView = (TextView) findViewById(R.id.installed_apps_container_card_view_last_scans_see_all_installed_apps_text_view);
        ImageView seeAllInstalledAppsImageView = (ImageView) findViewById(R.id.installed_apps_container_card_view_last_scans_see_all_installed_apps_arrow_button);

        seeAllInstalledAppsTextView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                displayAllInstalledAppsModalSheet();
            }
        });

        seeAllInstalledAppsImageView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                displayAllInstalledAppsModalSheet();
            }
        });
    }

    private void displayAllInstalledAppsModalSheet(){
        List<Drawable> allAppIcons = new ArrayList<>();
        List<String> allAppNames = new ArrayList<>();
        List<String> allPackageNames = new ArrayList<>();
        List<List<String>> allPermissions = new ArrayList<>();

        for (ApplicationInfo singleApplication : this.installedAppsManager.getAllInstalledApps()){
            Drawable icon = this.installedAppsManager.getPackageManager().getApplicationIcon(singleApplication);
            allAppIcons.add(icon);
            allAppNames.add(singleApplication.loadLabel(this.installedAppsManager.getPackageManager()).toString());
            allPackageNames.add(singleApplication.packageName);

            List<String> applicationPermissions = new ArrayList<>();
            try {
                PackageInfo packageInfo = this.installedAppsManager.getPackageManager().getPackageInfo(singleApplication.packageName, PackageManager.GET_PERMISSIONS);
                String[] requestedPermissions = packageInfo.requestedPermissions;
                if (requestedPermissions != null){
                    applicationPermissions.addAll(Arrays.asList(requestedPermissions));
                }
            } catch (PackageManager.NameNotFoundException ignored){}
            allPermissions.add(applicationPermissions);
        }

        final BottomSheetDialog bottomSheet = new BottomSheetDialog(MainActivity.this);
        bottomSheet.setContentView(R.layout.modal_sheet_all_installed_apps_listview);

        ListView allInstalledAppListView = (ListView) bottomSheet.findViewById(R.id.modal_sheet_all_installed_apps_scans_list_view);
        allInstalledAppListView.setAdapter(new SingleInstalledAppAdapter(getApplicationContext(), allAppIcons, allAppNames));

        allInstalledAppListView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                final BottomSheetDialog bottomSheet = new BottomSheetDialog(MainActivity.this);
                bottomSheet.setContentView(R.layout.modal_sheet_installed_app_details);

                ImageView appIcon = (ImageView) bottomSheet.findViewById(R.id.modal_sheet_installed_app_details_image_view_logo);
                TextView appName = (TextView) bottomSheet.findViewById(R.id.modal_sheet_installed_app_details_text_view_app_name);
                TextView packageName = (TextView) bottomSheet.findViewById(R.id.modal_sheet_installed_app_details_package_name_text_view);
                TabLayout mainTabLayout = (TabLayout) bottomSheet.findViewById(R.id.modal_sheet_installed_app_details_tab_layout);

                ListView allPermissionsListView = (ListView) bottomSheet.findViewById(R.id.modal_sheet_installed_apps_details_permissions_list_view);
                ListView minimalPermissionsListView = (ListView) bottomSheet.findViewById(R.id.modal_sheet_installed_apps_details_minimal_permissions_list_view);
                ListView lowPermissionsListView = (ListView) bottomSheet.findViewById(R.id.modal_sheet_installed_apps_details_low_permissions_list_view);
                ListView moderatePermissionsListView = (ListView) bottomSheet.findViewById(R.id.modal_sheet_installed_apps_details_moderate_permissions_list_view);
                ListView highPermissionsListView = (ListView) bottomSheet.findViewById(R.id.modal_sheet_installed_apps_details_high_permissions_list_view);
                ListView mostDangerousPermissionsListView = (ListView) bottomSheet.findViewById(R.id.modal_sheet_installed_apps_details_most_dangerous_permissions_list_view);

                //Display ListView on the First TIme
                SingleScannedAppDetailsPermissionListAdapter singleScannedAppDetailsPermissionListAdapter = new SingleScannedAppDetailsPermissionListAdapter(getApplicationContext(), allPermissions.get(position));
                allPermissionsListView.setAdapter(singleScannedAppDetailsPermissionListAdapter);
                allPermissionsListView.setVisibility(View.VISIBLE);

                mainTabLayout.addOnTabSelectedListener(new TabLayout.OnTabSelectedListener() {
                    @Override
                    public void onTabSelected(TabLayout.Tab tab) {
                        if (tab.getPosition() == 0){
                            //All Permissions
                            SingleScannedAppDetailsPermissionListAdapter singleScannedAppDetailsPermissionListAdapter = new SingleScannedAppDetailsPermissionListAdapter(getApplicationContext(), allPermissions.get(position));
                            allPermissionsListView.setAdapter(singleScannedAppDetailsPermissionListAdapter);

                            allPermissionsListView.setVisibility(View.VISIBLE);
                            minimalPermissionsListView.setVisibility(View.INVISIBLE);
                            lowPermissionsListView.setVisibility(View.INVISIBLE);
                            moderatePermissionsListView.setVisibility(View.INVISIBLE);
                            highPermissionsListView.setVisibility(View.INVISIBLE);
                            mostDangerousPermissionsListView.setVisibility(View.INVISIBLE);
                        } else if (tab.getPosition() == 1){
                            //Minimal Risk Permissions
                            SingleScannedAppDetailsPermissionListAdapter singleScannedAppDetailsPermissionListAdapter = new SingleScannedAppDetailsPermissionListAdapter(getApplicationContext(), permissionsDangerEnumUtils.getMinimalRiskPermissions(allPermissions.get(position)));
                            minimalPermissionsListView.setAdapter(singleScannedAppDetailsPermissionListAdapter);

                            allPermissionsListView.setVisibility(View.INVISIBLE);
                            minimalPermissionsListView.setVisibility(View.VISIBLE);
                            lowPermissionsListView.setVisibility(View.INVISIBLE);
                            moderatePermissionsListView.setVisibility(View.INVISIBLE);
                            highPermissionsListView.setVisibility(View.INVISIBLE);
                            mostDangerousPermissionsListView.setVisibility(View.INVISIBLE);
                        } else if (tab.getPosition() == 2){
                            //Low Risk Permissions
                            SingleScannedAppDetailsPermissionListAdapter singleScannedAppDetailsPermissionListAdapter = new SingleScannedAppDetailsPermissionListAdapter(getApplicationContext(), permissionsDangerEnumUtils.getLowRiskPermissions(allPermissions.get(position)));
                            lowPermissionsListView.setAdapter(singleScannedAppDetailsPermissionListAdapter);

                            allPermissionsListView.setVisibility(View.INVISIBLE);
                            minimalPermissionsListView.setVisibility(View.INVISIBLE);
                            lowPermissionsListView.setVisibility(View.VISIBLE);
                            moderatePermissionsListView.setVisibility(View.INVISIBLE);
                            highPermissionsListView.setVisibility(View.INVISIBLE);
                            mostDangerousPermissionsListView.setVisibility(View.INVISIBLE);
                        } else if (tab.getPosition() == 3){
                            //Moderate Risk Permissions
                            SingleScannedAppDetailsPermissionListAdapter singleScannedAppDetailsPermissionListAdapter = new SingleScannedAppDetailsPermissionListAdapter(getApplicationContext(), permissionsDangerEnumUtils.getModerateRiskPermissions(allPermissions.get(position)));
                            moderatePermissionsListView.setAdapter(singleScannedAppDetailsPermissionListAdapter);

                            allPermissionsListView.setVisibility(View.INVISIBLE);
                            minimalPermissionsListView.setVisibility(View.INVISIBLE);
                            lowPermissionsListView.setVisibility(View.INVISIBLE);
                            moderatePermissionsListView.setVisibility(View.VISIBLE);
                            highPermissionsListView.setVisibility(View.INVISIBLE);
                            mostDangerousPermissionsListView.setVisibility(View.INVISIBLE);
                        } else if (tab.getPosition() == 4){
                            //High Risk Permissions
                            SingleScannedAppDetailsPermissionListAdapter singleScannedAppDetailsPermissionListAdapter = new SingleScannedAppDetailsPermissionListAdapter(getApplicationContext(), permissionsDangerEnumUtils.getHighRiskPermissions(allPermissions.get(position)));
                            highPermissionsListView.setAdapter(singleScannedAppDetailsPermissionListAdapter);

                            allPermissionsListView.setVisibility(View.INVISIBLE);
                            minimalPermissionsListView.setVisibility(View.INVISIBLE);
                            lowPermissionsListView.setVisibility(View.INVISIBLE);
                            moderatePermissionsListView.setVisibility(View.INVISIBLE);
                            highPermissionsListView.setVisibility(View.VISIBLE);
                            mostDangerousPermissionsListView.setVisibility(View.INVISIBLE);
                        } else if (tab.getPosition() == 5){
                            //Most Dangerous Permissions
                            SingleScannedAppDetailsPermissionListAdapter singleScannedAppDetailsPermissionListAdapter = new SingleScannedAppDetailsPermissionListAdapter(getApplicationContext(), permissionsDangerEnumUtils.getMostDangerousPermissions(allPermissions.get(position)));
                            mostDangerousPermissionsListView.setAdapter(singleScannedAppDetailsPermissionListAdapter);

                            allPermissionsListView.setVisibility(View.INVISIBLE);
                            minimalPermissionsListView.setVisibility(View.INVISIBLE);
                            lowPermissionsListView.setVisibility(View.INVISIBLE);
                            moderatePermissionsListView.setVisibility(View.INVISIBLE);
                            highPermissionsListView.setVisibility(View.INVISIBLE);
                            mostDangerousPermissionsListView.setVisibility(View.VISIBLE);
                        }
                    }

                    @Override
                    public void onTabUnselected(TabLayout.Tab tab) {}

                    @Override
                    public void onTabReselected(TabLayout.Tab tab) {}
                });

                ScrollView mainScrollView = (ScrollView) bottomSheet.findViewById(R.id.modal_sheet_installed_app_details_main_scroll_view);

                appIcon.setImageDrawable(allAppIcons.get(position));
                appName.setText(allAppNames.get(position));
                packageName.setText(allPackageNames.get(position));

                mainScrollView.setOnTouchListener(new View.OnTouchListener() {
                    @Override
                    public boolean onTouch(View v, MotionEvent event) {
                        return false;
                    }
                });

                DisplayMetrics displayMetrics = new DisplayMetrics();
                getWindowManager().getDefaultDisplay().getMetrics(displayMetrics);
                int screenHeight = displayMetrics.heightPixels;
                int desiredHeight = screenHeight * 3 / 4;

                View bottomSheetView = bottomSheet.findViewById(R.id.modal_sheet_installed_app_details_main_linear_layout);
                ViewGroup.LayoutParams layoutParams = bottomSheetView.getLayoutParams();
                layoutParams.height = desiredHeight;
                bottomSheetView.setLayoutParams(layoutParams);

                bottomSheet.show();
            }
        });

        bottomSheet.show();
    }
}
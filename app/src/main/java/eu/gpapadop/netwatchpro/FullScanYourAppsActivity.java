package eu.gpapadop.netwatchpro;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.content.pm.ApplicationInfo;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.content.res.Resources;
import android.content.res.XmlResourceParser;
import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.os.Handler;
import android.util.DisplayMetrics;
import android.util.Log;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.widget.AdapterView;
import android.widget.ImageView;
import android.widget.ListAdapter;
import android.widget.ListView;
import android.widget.ProgressBar;
import android.widget.ScrollView;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.material.bottomsheet.BottomSheetDialog;
import com.google.android.material.tabs.TabLayout;

import org.json.JSONException;
import org.json.JSONObject;
import org.xmlpull.v1.XmlPullParser;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import eu.gpapadop.netwatchpro.adapters.listviews.SingleScannedAppDetailsPermissionListAdapter;
import eu.gpapadop.netwatchpro.adapters.listviews.SingleScannedAppsAdapter;
import eu.gpapadop.netwatchpro.api.RequestsHandler;
import eu.gpapadop.netwatchpro.classes.last_scans.App;
import eu.gpapadop.netwatchpro.classes.last_scans.Scan;
import eu.gpapadop.netwatchpro.handlers.SharedPreferencesHandler;
import eu.gpapadop.netwatchpro.interfaces.OkHttpRequestCallback;
import eu.gpapadop.netwatchpro.managers.InstalledAppsManager;
import eu.gpapadop.netwatchpro.notifications.NotificationsHandler;
import eu.gpapadop.netwatchpro.utils.DrawableUtils;
import eu.gpapadop.netwatchpro.utils.PermissionsDangerEnumUtils;
import eu.gpapadop.netwatchpro.utils.ScanUtils;
import okhttp3.FormBody;
import okhttp3.RequestBody;

public class FullScanYourAppsActivity extends AppCompatActivity {
    int totalCheckedApps = 0;
    private Connectivity connectivity;
    private InstalledAppsManager installedAppsManager;
    private SharedPreferencesHandler sharedPreferencesHandler;
    private DrawableUtils drawableUtils;
    private ScanUtils scanUtils;
    private NotificationsHandler notificationsHandler;
    private List<List<String>> allPermissions;
    private List<String> allAppIcons;
    private List<String> allAppNames;
    private List<String> allAppApkLocations;
    private List<String> allPackageNames;
    private List<Boolean> isMalware;
    private List<Boolean> hasChecked;
    private PermissionsDangerEnumUtils permissionsDangerEnumUtils;
    private String baseAPKPermissionsAPIURL = "";
    private String apiKey = "";
    private String secretKey = "";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_full_scan_your_apps);

        //Get Server Config
        this.getServerConfig();

        this.connectivity = new Connectivity(getApplicationContext());
        this.connectivity.initialize();
        this.sharedPreferencesHandler = new SharedPreferencesHandler(getApplicationContext());
        this.installedAppsManager = new InstalledAppsManager(getApplicationContext());
        this.drawableUtils = new DrawableUtils(getApplicationContext());
        this.scanUtils = new ScanUtils(this.sharedPreferencesHandler);
        this.permissionsDangerEnumUtils = new PermissionsDangerEnumUtils();
        this.notificationsHandler = new NotificationsHandler(getApplicationContext());
        this.allAppNames = new ArrayList<>();
        this.allAppApkLocations = new ArrayList<>();
        this.allPackageNames = new ArrayList<>();
        this.allPermissions = new ArrayList<>();
        this.allAppIcons = new ArrayList<>();
        this.isMalware = new ArrayList<>();
        this.hasChecked = new ArrayList<>();

        this.handleStatusBarColor();
        this.handleBackButtonTap();

        this.initializeAppLists();
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
                        this.baseAPKPermissionsAPIURL = xmlResourceParser.getText() + "/v1/package-permissions/predict";
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

    private void handleStatusBarColor(){
        Window window = this.getWindow();
        window.setStatusBarColor(this.getResources().getColor(R.color.main_blue));
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

    private void initializeAppLists(){
        for (ApplicationInfo singleApplication : this.installedAppsManager.getAllInstalledApps()){
            final String appPackageName = String.valueOf(singleApplication.packageName);
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
                this.allAppIcons.add(this.drawableUtils.drawableToString(icon));
            } catch (PackageManager.NameNotFoundException ignored){}

            try {
                PackageInfo packageInfo = this.installedAppsManager.getPackageManager().getPackageInfo(appPackageName, 0);
                String sourceDir = packageInfo.applicationInfo.sourceDir;
                this.allAppApkLocations.add(sourceDir);
            } catch (PackageManager.NameNotFoundException ignored) {}
        }
        this.handleInstalledAppsListView();
        this.handleProgressBar();
    }

    private void handleInstalledAppsListView(){
        ListView scanningAppsListView = (ListView) findViewById(R.id.activity_full_scan_scanning_apps_list_view);
        SingleScannedAppsAdapter singleScannedAppsAdapter = new SingleScannedAppsAdapter(getApplicationContext(), this.allAppNames, this.allAppIcons, this.isMalware, this.hasChecked);
        scanningAppsListView.setAdapter(singleScannedAppsAdapter);

        scanningAppsListView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                if (hasChecked.get(position)){
                    displayAppDetailsModalSheet(position);
                } else {
                    Toast.makeText(getApplicationContext(), getString(R.string.wait_for_the_scan_to_be_completed), Toast.LENGTH_LONG).show();
                }
            }
        });

        //Setup Full Height
        setListViewHeightBasedOnChildren(scanningAppsListView);
        ScrollView scrollView = (ScrollView) findViewById(R.id.activity_full_scan_your_apps_main_scroll_view);
        scrollView.setOnTouchListener(new View.OnTouchListener() {
            @Override
            public boolean onTouch(View v, MotionEvent event) {
                scanningAppsListView.getParent().requestDisallowInterceptTouchEvent(false);
                return false;
            }
        });
    }

    private void displayAppDetailsModalSheet(int position){
        final BottomSheetDialog bottomSheet = new BottomSheetDialog(FullScanYourAppsActivity.this);
        bottomSheet.setContentView(R.layout.modal_sheet_scanned_app_details);

        ImageView appIcon = (ImageView) bottomSheet.findViewById(R.id.modal_sheet_scanned_app_details_image_view_logo);
        TextView appName = (TextView) bottomSheet.findViewById(R.id.modal_sheet_scanned_app_details_text_view_app_name);
        TextView packageName = (TextView) bottomSheet.findViewById(R.id.modal_sheet_scanned_app_details_package_name_text_view);
        ScrollView mainScrollView = (ScrollView) bottomSheet.findViewById(R.id.modal_sheet_scanned_app_details_main_scroll_view);
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

        appIcon.setImageDrawable(this.drawableUtils.stringToDrawable(this.allAppIcons.get(position)));
        appName.setText(this.allAppNames.get(position));
        packageName.setText(this.allPackageNames.get(position));

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

        View bottomSheetView = bottomSheet.findViewById(R.id.modal_sheet_scanned_app_details_main_linear_layout);
        ViewGroup.LayoutParams layoutParams = bottomSheetView.getLayoutParams();
        layoutParams.height = desiredHeight;
        bottomSheetView.setLayoutParams(layoutParams);

        bottomSheet.show();
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

    private void handleProgressBar(){
        //Setup Progress Bar
        ProgressBar progressBar = (ProgressBar) findViewById(R.id.full_scan_your_apps_progress_bar);
        TextView progressBarTextView = (TextView) findViewById(R.id.full_scan_your_apps_progress_text);
        TextView scanStatusTextView = (TextView) findViewById(R.id.activity_full_scan_status_text_view);
        progressBarTextView.setText(String.valueOf(totalCheckedApps) + "/" + String.valueOf(this.installedAppsManager.getAllInstalledApps().size()));

        //Setup Notification
        this.notificationsHandler.showStickyNotification(getString(R.string.scan_your_apps), getString(R.string.we_are_performing_a_quick_scan_to_your_installed_apps_please_be_patient));

        final Handler handler = new Handler();
        handler.postDelayed(new Runnable() {
            @Override
            public void run() {
                if (totalCheckedApps < installedAppsManager.getAllInstalledApps().size()) {
                    progressBarTextView.setText(String.valueOf(totalCheckedApps) + "/" + String.valueOf(installedAppsManager.getAllInstalledApps().size()));
                    double percentage = ((double) totalCheckedApps / installedAppsManager.getAllInstalledApps().size()) * 100.0;
                    progressBar.setProgress((int) percentage);
                    checkMalware(totalCheckedApps);
                    totalCheckedApps++;
                    handler.postDelayed(this, 1500);
                } else {
                    progressBarTextView.setText(String.valueOf(totalCheckedApps) + "/" + String.valueOf(installedAppsManager.getAllInstalledApps().size()));
                    double percentage = ((double) totalCheckedApps / installedAppsManager.getAllInstalledApps().size()) * 100.0;
                    progressBar.setProgress((int) percentage);
                    scanStatusTextView.setText(getString(R.string.scan_is_complete_you_can_view_the_results_below));
                    handler.removeCallbacks(this);
                    saveCompletedScan();
                    notificationsHandler.hideNotification();
                }
            }
        }, 1500);
    }

    private void checkMalware(int position){
//        RequestsHandler apkPermissionsAPI = new RequestsHandler();
//        RequestBody requestBody = new FormBody.Builder()
//                .add("device_token", this.connectivity.getDeviceID())
//                .add("package_name", this.allPackageNames.get(position))
//                .add("app_name", this.allAppNames.get(position))
//                .add("permissions", this.getPermissionsString(position))
//                .add("api_key", this.apiKey)
//                .add("secret_key", this.secretKey)
//                .build();
//        apkPermissionsAPI.makeOkHttpPostRequest(this.baseAPKPermissionsAPIURL, requestBody, new OkHttpRequestCallback() {
//            @Override
//            public void onResponse(JSONObject jsonObject) {
//                // Handle the successful response
//                hasChecked.set(position, true);
//                try {
//                    Boolean predictionResult = jsonObject.getBoolean("is_malware");
//                    isMalware.set(position, predictionResult);
//                } catch (JSONException ignored) {}
//                runOnUiThread(new Runnable() {
//                    @Override
//                    public void run() {
//                        handleInstalledAppsListView();
//                    }
//                });
//            }
//
//            @Override
//            public void onError(Exception e) {
//                // Handle the error
//            }
//        });
    }

    private void saveCompletedScan(){
        Scan newScan = new Scan();
        List<App> scannedApps = new ArrayList<>();
        for (int i = 0; i<this.allPackageNames.size(); i++){
            scannedApps.add(new App(
                    this.allAppNames.get(i),
                    this.allPackageNames.get(i),
                    this.allPermissions.get(i),
                    this.isMalware.get(i),
                    this.allAppIcons.get(i)
            ));
        }
        newScan.setScannedApps(scannedApps);
        this.scanUtils.appendScanToSharedPrefs(newScan);
    }
}
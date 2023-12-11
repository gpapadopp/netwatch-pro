package eu.gpapadop.netwatchpro;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.content.pm.ApplicationInfo;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.os.Handler;
import android.util.Base64;
import android.util.DisplayMetrics;
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

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import eu.gpapadop.netwatchpro.adapters.listviews.SingleScannedAppDetailsPermissionListAdapter;
import eu.gpapadop.netwatchpro.adapters.listviews.SingleScannedAppsAdapter;
import eu.gpapadop.netwatchpro.api.RequestsHandler;
import eu.gpapadop.netwatchpro.classes.last_scans.App;
import eu.gpapadop.netwatchpro.classes.last_scans.Scan;
import eu.gpapadop.netwatchpro.enums.permissions_danger.HighRiskPermissions;
import eu.gpapadop.netwatchpro.enums.permissions_danger.LowRiskPermissions;
import eu.gpapadop.netwatchpro.enums.permissions_danger.MinimalRiskPermissions;
import eu.gpapadop.netwatchpro.enums.permissions_danger.ModerateRiskPermissions;
import eu.gpapadop.netwatchpro.enums.permissions_danger.MostDangerousPermissions;
import eu.gpapadop.netwatchpro.handlers.SharedPreferencesHandler;
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
    private List<String> allAppIcons;
    private List<Boolean> isMalware;
    private List<Boolean> hasChecked;
    private Connectivity connectivity;
    private NotificationsHandler notificationsHandler;
    private SharedPreferencesHandler sharedPreferencesHandler;
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
        this.sharedPreferencesHandler = new SharedPreferencesHandler(getApplicationContext());
        this.allAppNames = new ArrayList<>();
        this.allPackageNames = new ArrayList<>();
        this.allPermissions = new ArrayList<>();
        this.allAppIcons = new ArrayList<>();
        this.isMalware = new ArrayList<>();
        this.hasChecked = new ArrayList<>();
        this.initializeAppLists();
        this.handleStatusBarColor();

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
                this.allAppIcons.add(this.drawableToString(icon));
            } catch (PackageManager.NameNotFoundException ignored){}
        }
        this.handleInstalledAppsListView();
        this.handleProgressBar();
    }

    private Bitmap drawableToBitmap(Drawable drawable) {
        if (drawable instanceof BitmapDrawable) {
            return ((BitmapDrawable) drawable).getBitmap();
        }

        int width = drawable.getIntrinsicWidth();
        int height = drawable.getIntrinsicHeight();

        Bitmap bitmap = Bitmap.createBitmap(width, height, Bitmap.Config.ARGB_8888);
        Canvas canvas = new Canvas(bitmap);
        drawable.setBounds(0, 0, canvas.getWidth(), canvas.getHeight());
        drawable.draw(canvas);

        return bitmap;
    }

    private String drawableToString(Drawable drawable) {
        if (drawable == null) {
            return null;
        }

        // Convert the Drawable to a Bitmap
        Bitmap bitmap = drawableToBitmap(drawable);

        // Convert the Bitmap to a base64 string
        ByteArrayOutputStream byteArrayOutputStream = new ByteArrayOutputStream();
        bitmap.compress(Bitmap.CompressFormat.PNG, 100, byteArrayOutputStream);
        byte[] byteArray = byteArrayOutputStream.toByteArray();
        return Base64.encodeToString(byteArray, Base64.DEFAULT);
    }

    private Drawable stringToDrawable(String drawableString) {
        if (drawableString == null) {
            return null;
        }

        byte[] byteArray = Base64.decode(drawableString, Base64.DEFAULT);
        Bitmap bitmap = BitmapFactory.decodeByteArray(byteArray, 0, byteArray.length);
        return new BitmapDrawable(getResources(), bitmap);
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

    private void handleStatusBarColor(){
        Window window = this.getWindow();
        window.setStatusBarColor(this.getResources().getColor(R.color.main_blue));
    }

    private void handleProgressBar(){
        //Setup Progress Bar
        ProgressBar progressBar = (ProgressBar) findViewById(R.id.progress_bar);
        TextView progressBarTextView = (TextView) findViewById(R.id.progress_text);
        TextView scanStatusTextView = (TextView) findViewById(R.id.activity_scan_scan_status_text_view);
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
        this.appendScanToSharedPrefs(newScan);
    }

    private void appendScanToSharedPrefs(Scan newScan){
        Set<String> lastScans = this.sharedPreferencesHandler.getLatestScans();
        List<Scan> allScans = this.decodeLastScans(lastScans);

        allScans.add(newScan);

        Set<String> encodedScans = this.encodeLastScans(allScans);
        this.sharedPreferencesHandler.setLatestScans(encodedScans);

        LocalDateTime nowDate = LocalDateTime.now();
        ZoneId zoneId = ZoneId.of("UTC");
        ZonedDateTime utcDateTime = ZonedDateTime.of(nowDate, zoneId);
        this.sharedPreferencesHandler.saveLastCheckDateTime(utcDateTime.toInstant().toEpochMilli());
    }

    private List<Scan> decodeLastScans(Set<String> allLastScans){
        List<Scan> allDecodedLastScans = new ArrayList<>();
        for (String lastScan : allLastScans){
            if (lastScan != null){
                byte[] serializedBytes = Base64.decode(lastScan, Base64.DEFAULT);
                ByteArrayInputStream byteArrayInputStream = new ByteArrayInputStream(serializedBytes);
                try {
                    ObjectInputStream objectInputStream = new ObjectInputStream(byteArrayInputStream);
                    Scan singleJob = (Scan) objectInputStream.readObject();
                    allDecodedLastScans.add(singleJob);
                    objectInputStream.close();
                } catch (IOException | ClassNotFoundException ignored) {}
            }
        }
        return allDecodedLastScans;
    }

    private Set<String> encodeLastScans(List<Scan> allScans){
        Set<String> scansToSave = new HashSet<>();
        for (int i = 0; i<allScans.size(); i++){
            ByteArrayOutputStream byteArrayOutputStream = new ByteArrayOutputStream();
            try {
                ObjectOutputStream objectOutputStream = new ObjectOutputStream(byteArrayOutputStream);
                objectOutputStream.writeObject(allScans.get(i));
                objectOutputStream.close();
            } catch (IOException ignored) {}
            String serializedObject = Base64.encodeToString(byteArrayOutputStream.toByteArray(), Base64.DEFAULT);
            scansToSave.add(serializedObject);
        }
        return scansToSave;
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
        ScrollView scrollView = (ScrollView) findViewById(R.id.activity_scan_your_apps_main_scroll_view);
        scrollView.setOnTouchListener(new View.OnTouchListener() {
            @Override
            public boolean onTouch(View v, MotionEvent event) {
                scanningAppsListView.getParent().requestDisallowInterceptTouchEvent(false);
                return false;
            }
        });
    }

    private void displayAppDetailsModalSheet(int position){
        final BottomSheetDialog bottomSheet = new BottomSheetDialog(ScanYourAppsActivity.this);
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
                    SingleScannedAppDetailsPermissionListAdapter singleScannedAppDetailsPermissionListAdapter = new SingleScannedAppDetailsPermissionListAdapter(getApplicationContext(), getMinimalRiskPermissions(allPermissions.get(position)));
                    minimalPermissionsListView.setAdapter(singleScannedAppDetailsPermissionListAdapter);

                    allPermissionsListView.setVisibility(View.INVISIBLE);
                    minimalPermissionsListView.setVisibility(View.VISIBLE);
                    lowPermissionsListView.setVisibility(View.INVISIBLE);
                    moderatePermissionsListView.setVisibility(View.INVISIBLE);
                    highPermissionsListView.setVisibility(View.INVISIBLE);
                    mostDangerousPermissionsListView.setVisibility(View.INVISIBLE);
                } else if (tab.getPosition() == 2){
                    //Low Risk Permissions
                    SingleScannedAppDetailsPermissionListAdapter singleScannedAppDetailsPermissionListAdapter = new SingleScannedAppDetailsPermissionListAdapter(getApplicationContext(), getLowRiskPermissions(allPermissions.get(position)));
                    lowPermissionsListView.setAdapter(singleScannedAppDetailsPermissionListAdapter);

                    allPermissionsListView.setVisibility(View.INVISIBLE);
                    minimalPermissionsListView.setVisibility(View.INVISIBLE);
                    lowPermissionsListView.setVisibility(View.VISIBLE);
                    moderatePermissionsListView.setVisibility(View.INVISIBLE);
                    highPermissionsListView.setVisibility(View.INVISIBLE);
                    mostDangerousPermissionsListView.setVisibility(View.INVISIBLE);
                } else if (tab.getPosition() == 3){
                    //Moderate Risk Permissions
                    SingleScannedAppDetailsPermissionListAdapter singleScannedAppDetailsPermissionListAdapter = new SingleScannedAppDetailsPermissionListAdapter(getApplicationContext(), getModerateRiskPermissions(allPermissions.get(position)));
                    moderatePermissionsListView.setAdapter(singleScannedAppDetailsPermissionListAdapter);

                    allPermissionsListView.setVisibility(View.INVISIBLE);
                    minimalPermissionsListView.setVisibility(View.INVISIBLE);
                    lowPermissionsListView.setVisibility(View.INVISIBLE);
                    moderatePermissionsListView.setVisibility(View.VISIBLE);
                    highPermissionsListView.setVisibility(View.INVISIBLE);
                    mostDangerousPermissionsListView.setVisibility(View.INVISIBLE);
                } else if (tab.getPosition() == 4){
                    //High Risk Permissions
                    SingleScannedAppDetailsPermissionListAdapter singleScannedAppDetailsPermissionListAdapter = new SingleScannedAppDetailsPermissionListAdapter(getApplicationContext(), getHighRiskPermissions(allPermissions.get(position)));
                    highPermissionsListView.setAdapter(singleScannedAppDetailsPermissionListAdapter);

                    allPermissionsListView.setVisibility(View.INVISIBLE);
                    minimalPermissionsListView.setVisibility(View.INVISIBLE);
                    lowPermissionsListView.setVisibility(View.INVISIBLE);
                    moderatePermissionsListView.setVisibility(View.INVISIBLE);
                    highPermissionsListView.setVisibility(View.VISIBLE);
                    mostDangerousPermissionsListView.setVisibility(View.INVISIBLE);
                } else if (tab.getPosition() == 5){
                    //Most Dangerous Permissions
                    SingleScannedAppDetailsPermissionListAdapter singleScannedAppDetailsPermissionListAdapter = new SingleScannedAppDetailsPermissionListAdapter(getApplicationContext(), getMostDangerousPermissions(allPermissions.get(position)));
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

        appIcon.setImageDrawable(this.stringToDrawable(this.allAppIcons.get(position)));
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

    private List<String> getMinimalRiskPermissions(List<String> allPermissions){
        List<String> minimalPermissions = new ArrayList<>();
        for (MinimalRiskPermissions minimalRiskPermissions : MinimalRiskPermissions.values()){
            if (allPermissions.contains(minimalRiskPermissions.getPermissionName())){
                minimalPermissions.add(minimalRiskPermissions.getPermissionName());
            }
        }
        return minimalPermissions;
    }

    private List<String> getLowRiskPermissions(List<String> allPermissions){
        List<String> lowPermissions = new ArrayList<>();
        for (LowRiskPermissions lowRiskPermissions : LowRiskPermissions.values()){
            if (allPermissions.contains(lowRiskPermissions.getPermissionName())){
                lowPermissions.add(lowRiskPermissions.getPermissionName());
            }
        }
        return lowPermissions;
    }

    private List<String> getModerateRiskPermissions(List<String> allPermissions){
        List<String> moderatePermissions = new ArrayList<>();
        for (ModerateRiskPermissions moderateRiskPermissions : ModerateRiskPermissions.values()){
            if (allPermissions.contains(moderateRiskPermissions.getPermissionName())){
                moderatePermissions.add(moderateRiskPermissions.getPermissionName());
            }
        }
        return moderatePermissions;
    }

    private List<String> getHighRiskPermissions(List<String> allPermissions){
        List<String> highPermissions = new ArrayList<>();
        for (HighRiskPermissions highRiskPermissions : HighRiskPermissions.values()){
            if (allPermissions.contains(highRiskPermissions.getPermissionName())){
                highPermissions.add(highRiskPermissions.getPermissionName());
            }
        }
        return highPermissions;
    }

    private List<String> getMostDangerousPermissions(List<String> allPermissions){
        List<String> mostDangerousPermissions = new ArrayList<>();
        for (MostDangerousPermissions mostDangerousPermission : MostDangerousPermissions.values()){
            if (allPermissions.contains(mostDangerousPermission.getPermissionName())){
                mostDangerousPermissions.add(mostDangerousPermission.getPermissionName());
            }
        }
        return mostDangerousPermissions;
    }
}
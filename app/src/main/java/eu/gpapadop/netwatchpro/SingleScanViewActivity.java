package eu.gpapadop.netwatchpro;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.graphics.RenderEffect;
import android.graphics.Shader;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.util.DisplayMetrics;
import android.util.Log;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.AdapterView;
import android.widget.ImageView;
import android.widget.ListAdapter;
import android.widget.ListView;
import android.widget.ScrollView;
import android.widget.TextView;

import com.google.android.material.bottomsheet.BottomSheetDialog;
import com.google.android.material.tabs.TabLayout;

import java.util.List;
import java.util.Set;
import java.util.UUID;

import eu.gpapadop.netwatchpro.adapters.listviews.SingleScannedAppDetailsPermissionListAdapter;
import eu.gpapadop.netwatchpro.adapters.listviews.SingleScannedAppsAdapter;
import eu.gpapadop.netwatchpro.classes.last_scans.App;
import eu.gpapadop.netwatchpro.classes.last_scans.Scan;
import eu.gpapadop.netwatchpro.enums.OverallSituations;
import eu.gpapadop.netwatchpro.handlers.SharedPreferencesHandler;
import eu.gpapadop.netwatchpro.utils.DateTimeUtils;
import eu.gpapadop.netwatchpro.utils.DrawableUtils;
import eu.gpapadop.netwatchpro.utils.OverallSituationUtils;
import eu.gpapadop.netwatchpro.utils.PermissionsDangerEnumUtils;
import eu.gpapadop.netwatchpro.utils.ScanUtils;

public class SingleScanViewActivity extends AppCompatActivity {
    private String scanUUID;
    private SharedPreferencesHandler sharedPreferencesHandler;
    private ScanUtils scanUtils;
    private DrawableUtils drawableUtils;
    private DateTimeUtils dateTimeUtils;
    private PermissionsDangerEnumUtils permissionsDangerEnumUtils;
    private OverallSituationUtils overallSituationUtils;
    private OverallSituations overallSituationsToView;
    private Scan scanToView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_single_scan_view);

        this.sharedPreferencesHandler = new SharedPreferencesHandler(getApplicationContext());
        this.scanUtils = new ScanUtils(this.sharedPreferencesHandler);
        this.drawableUtils = new DrawableUtils(getApplicationContext());
        this.dateTimeUtils = new DateTimeUtils(getApplicationContext());
        this.permissionsDangerEnumUtils = new PermissionsDangerEnumUtils();
        this.overallSituationUtils = new OverallSituationUtils();

        Bundle extras = getIntent().getExtras();
        if (extras != null) {
            this.scanUUID = extras.getString("scan_unique_id");
        }

        this.handleBackButtonTap();
        this.getScanFromSharedPrefs();
    }

    private void handleBackButtonTap(){
        ImageView backButtonImageView = (ImageView) findViewById(R.id.single_scan_view_activity_toolbar_back_button);
        backButtonImageView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent mainActivityIntent = new Intent(getApplicationContext(), MainActivity.class);
                startActivity(mainActivityIntent);
                finish();
            }
        });
    }

    private void getScanFromSharedPrefs(){
        Set<String> lastScans = this.sharedPreferencesHandler.getLatestScans();
        List<Scan> allScans = this.scanUtils.decodeLastScans(lastScans);
        UUID parsedUUID = UUID.fromString(this.scanUUID);

        boolean found = false;
        for (Scan singleScan : allScans){
            if (singleScan.getScanID().equals(parsedUUID)){
                this.scanToView = singleScan;
                found = true;
                break;
            }
        }
        if (!found){
            Intent mainActivityIntent = new Intent(getApplicationContext(), MainActivity.class);
            startActivity(mainActivityIntent);
            finish();
            return;
        }
        this.handleScannedDateTextView();
        this.handleScannedAppsFullNumberTextView();
        this.handleIssuesFoundNumberTextView();
        this.handleOverallSituationTextView();
        this.handleBackgroundImageView();
        this.handleScannedAppsListView();
    }

    private void handleScannedDateTextView(){
        TextView scannedDateTextView = (TextView) findViewById(R.id.activity_single_scan_view_scanned_date_full_date_text_view);
        scannedDateTextView.setText(this.dateTimeUtils.formatDateTime(this.scanToView.getScanDateTime()));
    }

    private void handleScannedAppsFullNumberTextView(){
        TextView scannedAppsNumberTextView = (TextView) findViewById(R.id.activity_single_scan_view_scanned_apps_full_number_text_view);
        scannedAppsNumberTextView.setText(String.valueOf(this.scanToView.getScannedApps().size()));
    }

    private void handleIssuesFoundNumberTextView(){
        TextView issuesFoundNumberTextView = (TextView) findViewById(R.id.activity_single_scan_view_issues_found_full_number_text_view);
        int issuesCounter = 0;
        for (App singleScannedApp : this.scanToView.getScannedApps()){
            if (singleScannedApp.getIsMalware()){
                issuesCounter += 1;
            }
        }
        issuesFoundNumberTextView.setText(String.valueOf(issuesCounter));
    }

    private void handleOverallSituationTextView(){
        TextView overallSituationTextView = (TextView) findViewById(R.id.activity_single_scan_view_overall_situation_content_text_view);

        this.overallSituationsToView = this.overallSituationUtils.calculateOverallSituation(this.scanToView.getScannedApps());
        String overallSituationDisplayText = this.overallSituationsToView.getStringValue();
        String packageName = getPackageName();
        int resId = getResources().getIdentifier(overallSituationDisplayText, "string", packageName);

        overallSituationTextView.setText(getString(resId));
    }

    private void handleBackgroundImageView(){
        ImageView backgroundImageView = (ImageView) findViewById(R.id.single_scan_view_activity_background_image_view);

        if (this.overallSituationsToView == OverallSituations.Excellent){
            backgroundImageView.setImageDrawable(getDrawable(R.drawable.activity_single_scan_view_excellent_badge));
            backgroundImageView.setAlpha(0.2F);
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
                backgroundImageView.setRenderEffect(RenderEffect.createBlurEffect(30, 30, Shader.TileMode.CLAMP));
            }
        } else if (this.overallSituationsToView == OverallSituations.Good){
            backgroundImageView.setImageDrawable(getDrawable(R.drawable.activity_single_scan_view_good_badge));
            backgroundImageView.setAlpha(0.35F);
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
                backgroundImageView.setRenderEffect(RenderEffect.createBlurEffect(25, 25, Shader.TileMode.CLAMP));
            }
        } else if (this.overallSituationsToView == OverallSituations.Average){
            backgroundImageView.setImageDrawable(getDrawable(R.drawable.activity_single_scan_view_average_badge));
            backgroundImageView.setAlpha(0.35F);
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
                backgroundImageView.setRenderEffect(RenderEffect.createBlurEffect(25, 25, Shader.TileMode.CLAMP));
            }
        } else if (this.overallSituationsToView == OverallSituations.Warning){
            backgroundImageView.setImageDrawable(getDrawable(R.drawable.activity_single_scan_view_warning_badge));
            backgroundImageView.setAlpha(0.35F);
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
                backgroundImageView.setRenderEffect(RenderEffect.createBlurEffect(25, 25, Shader.TileMode.CLAMP));
            }
        } else if (this.overallSituationsToView == OverallSituations.Danger){
            backgroundImageView.setImageDrawable(getDrawable(R.drawable.activity_single_scan_view_danger_badge));
            backgroundImageView.setAlpha(0.35F);
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
                backgroundImageView.setRenderEffect(RenderEffect.createBlurEffect(25, 25, Shader.TileMode.CLAMP));
            }
        } else {
            backgroundImageView.setImageDrawable(getDrawable(R.drawable.activity_single_scan_view_excellent_badge));
            backgroundImageView.setAlpha(0.2F);
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
                backgroundImageView.setRenderEffect(RenderEffect.createBlurEffect(30, 30, Shader.TileMode.CLAMP));
            }
        }

        Animation animation = AnimationUtils.loadAnimation(this, R.anim.blink_anim);
        backgroundImageView.startAnimation(animation);

        new Handler(Looper.getMainLooper()).postDelayed(new Runnable() {
            @Override
            public void run() {
                backgroundImageView.clearAnimation();
            }
        }, 4000);
    }

    private void handleScannedAppsListView(){
        ListView scannedAppsListView = (ListView) findViewById(R.id.activity_single_scan_view_scanned_apps_list_view);
        SingleScannedAppsAdapter singleScannedAppsAdapter = new SingleScannedAppsAdapter(getApplicationContext(), this.scanToView.getAllAppNames(), this.scanToView.getAllAppIcons(), this.scanToView.getAllAppsIsMalware(), this.scanToView.getAllAppsHasChecked());
        scannedAppsListView.setAdapter(singleScannedAppsAdapter);

        scannedAppsListView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                displayAppDetailsModalSheet(position);
            }
        });

        setListViewHeightBasedOnChildren(scannedAppsListView);
        ScrollView scrollView = (ScrollView) findViewById(R.id.activity_single_scan_view_main_scroll_view);
        scrollView.setOnTouchListener(new View.OnTouchListener() {
            @Override
            public boolean onTouch(View v, MotionEvent event) {
                scannedAppsListView.getParent().requestDisallowInterceptTouchEvent(false);
                return false;
            }
        });
    }

    private void displayAppDetailsModalSheet(int position){
        final BottomSheetDialog bottomSheet = new BottomSheetDialog(SingleScanViewActivity.this);
        bottomSheet.setContentView(R.layout.modal_sheet_scanned_app_details);

        TextView safeTextView = (TextView) bottomSheet.findViewById(R.id.modal_sheet_scanned_app_is_save_text_view);
        TextView warningTextView = (TextView) bottomSheet.findViewById(R.id.modal_sheet_scanned_app_warning_text_view);
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
        SingleScannedAppDetailsPermissionListAdapter singleScannedAppDetailsPermissionListAdapter = new SingleScannedAppDetailsPermissionListAdapter(getApplicationContext(), scanToView.getScannedApps().get(position).getAllPermissions());
        allPermissionsListView.setAdapter(singleScannedAppDetailsPermissionListAdapter);
        allPermissionsListView.setVisibility(View.VISIBLE);

        mainTabLayout.addOnTabSelectedListener(new TabLayout.OnTabSelectedListener() {
            @Override
            public void onTabSelected(TabLayout.Tab tab) {
                if (tab.getPosition() == 0){
                    //All Permissions
                    SingleScannedAppDetailsPermissionListAdapter singleScannedAppDetailsPermissionListAdapter = new SingleScannedAppDetailsPermissionListAdapter(getApplicationContext(), scanToView.getScannedApps().get(position).getAllPermissions());
                    allPermissionsListView.setAdapter(singleScannedAppDetailsPermissionListAdapter);

                    allPermissionsListView.setVisibility(View.VISIBLE);
                    minimalPermissionsListView.setVisibility(View.INVISIBLE);
                    lowPermissionsListView.setVisibility(View.INVISIBLE);
                    moderatePermissionsListView.setVisibility(View.INVISIBLE);
                    highPermissionsListView.setVisibility(View.INVISIBLE);
                    mostDangerousPermissionsListView.setVisibility(View.INVISIBLE);
                } else if (tab.getPosition() == 1){
                    //Minimal Risk Permissions
                    SingleScannedAppDetailsPermissionListAdapter singleScannedAppDetailsPermissionListAdapter = new SingleScannedAppDetailsPermissionListAdapter(getApplicationContext(), permissionsDangerEnumUtils.getMinimalRiskPermissions(scanToView.getScannedApps().get(position).getAllPermissions()));
                    minimalPermissionsListView.setAdapter(singleScannedAppDetailsPermissionListAdapter);

                    allPermissionsListView.setVisibility(View.INVISIBLE);
                    minimalPermissionsListView.setVisibility(View.VISIBLE);
                    lowPermissionsListView.setVisibility(View.INVISIBLE);
                    moderatePermissionsListView.setVisibility(View.INVISIBLE);
                    highPermissionsListView.setVisibility(View.INVISIBLE);
                    mostDangerousPermissionsListView.setVisibility(View.INVISIBLE);
                } else if (tab.getPosition() == 2){
                    //Low Risk Permissions
                    SingleScannedAppDetailsPermissionListAdapter singleScannedAppDetailsPermissionListAdapter = new SingleScannedAppDetailsPermissionListAdapter(getApplicationContext(), permissionsDangerEnumUtils.getLowRiskPermissions(scanToView.getScannedApps().get(position).getAllPermissions()));
                    lowPermissionsListView.setAdapter(singleScannedAppDetailsPermissionListAdapter);

                    allPermissionsListView.setVisibility(View.INVISIBLE);
                    minimalPermissionsListView.setVisibility(View.INVISIBLE);
                    lowPermissionsListView.setVisibility(View.VISIBLE);
                    moderatePermissionsListView.setVisibility(View.INVISIBLE);
                    highPermissionsListView.setVisibility(View.INVISIBLE);
                    mostDangerousPermissionsListView.setVisibility(View.INVISIBLE);
                } else if (tab.getPosition() == 3){
                    //Moderate Risk Permissions
                    SingleScannedAppDetailsPermissionListAdapter singleScannedAppDetailsPermissionListAdapter = new SingleScannedAppDetailsPermissionListAdapter(getApplicationContext(), permissionsDangerEnumUtils.getModerateRiskPermissions(scanToView.getScannedApps().get(position).getAllPermissions()));
                    moderatePermissionsListView.setAdapter(singleScannedAppDetailsPermissionListAdapter);

                    allPermissionsListView.setVisibility(View.INVISIBLE);
                    minimalPermissionsListView.setVisibility(View.INVISIBLE);
                    lowPermissionsListView.setVisibility(View.INVISIBLE);
                    moderatePermissionsListView.setVisibility(View.VISIBLE);
                    highPermissionsListView.setVisibility(View.INVISIBLE);
                    mostDangerousPermissionsListView.setVisibility(View.INVISIBLE);
                } else if (tab.getPosition() == 4){
                    //High Risk Permissions
                    SingleScannedAppDetailsPermissionListAdapter singleScannedAppDetailsPermissionListAdapter = new SingleScannedAppDetailsPermissionListAdapter(getApplicationContext(), permissionsDangerEnumUtils.getHighRiskPermissions(scanToView.getScannedApps().get(position).getAllPermissions()));
                    highPermissionsListView.setAdapter(singleScannedAppDetailsPermissionListAdapter);

                    allPermissionsListView.setVisibility(View.INVISIBLE);
                    minimalPermissionsListView.setVisibility(View.INVISIBLE);
                    lowPermissionsListView.setVisibility(View.INVISIBLE);
                    moderatePermissionsListView.setVisibility(View.INVISIBLE);
                    highPermissionsListView.setVisibility(View.VISIBLE);
                    mostDangerousPermissionsListView.setVisibility(View.INVISIBLE);
                } else if (tab.getPosition() == 5){
                    //Most Dangerous Permissions
                    SingleScannedAppDetailsPermissionListAdapter singleScannedAppDetailsPermissionListAdapter = new SingleScannedAppDetailsPermissionListAdapter(getApplicationContext(), permissionsDangerEnumUtils.getMostDangerousPermissions(scanToView.getScannedApps().get(position).getAllPermissions()));
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

        appIcon.setImageDrawable(this.drawableUtils.stringToDrawable(this.scanToView.getAllAppIcons().get(position)));
        appName.setText(this.scanToView.getAllAppNames().get(position));
        packageName.setText(this.scanToView.getAllPackageNames().get(position));

        if (this.scanToView.getScannedApps().get(position).getIsMalware()){
            warningTextView.setVisibility(View.VISIBLE);
            safeTextView.setVisibility(View.GONE);
        } else {
            warningTextView.setVisibility(View.GONE);
            safeTextView.setVisibility(View.VISIBLE);
        }

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
}
package eu.gpapadop.netwatchpro;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.util.Base64;
import android.util.DisplayMetrics;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ImageView;
import android.widget.ListAdapter;
import android.widget.ListView;
import android.widget.ScrollView;
import android.widget.TextView;

import com.google.android.material.bottomsheet.BottomSheetDialog;
import com.google.android.material.tabs.TabLayout;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import java.util.UUID;

import eu.gpapadop.netwatchpro.adapters.listviews.SingleScannedAppDetailsPermissionListAdapter;
import eu.gpapadop.netwatchpro.adapters.listviews.SingleScannedAppsAdapter;
import eu.gpapadop.netwatchpro.classes.last_scans.Scan;
import eu.gpapadop.netwatchpro.enums.permissions_danger.HighRiskPermissions;
import eu.gpapadop.netwatchpro.enums.permissions_danger.LowRiskPermissions;
import eu.gpapadop.netwatchpro.enums.permissions_danger.MinimalRiskPermissions;
import eu.gpapadop.netwatchpro.enums.permissions_danger.ModerateRiskPermissions;
import eu.gpapadop.netwatchpro.enums.permissions_danger.MostDangerousPermissions;
import eu.gpapadop.netwatchpro.handlers.SharedPreferencesHandler;

public class SingleScanViewActivity extends AppCompatActivity {
    private String scanUUID;
    private SharedPreferencesHandler sharedPreferencesHandler;
    private Scan scanToView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_single_scan_view);

        this.sharedPreferencesHandler = new SharedPreferencesHandler(getApplicationContext());

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
        List<Scan> allScans = this.decodeLastScans(lastScans);
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
    }

    private List<Scan> decodeLastScans(Set<String> allLastScans) {
        List<Scan> allDecodedLastScans = new ArrayList<>();

        for (String lastScan : allLastScans) {
            if (lastScan != null) {
                try {
                    byte[] serializedBytes = Base64.decode(lastScan, Base64.DEFAULT);
                    ByteArrayInputStream byteArrayInputStream = new ByteArrayInputStream(serializedBytes);
                    ObjectInputStream objectInputStream = new ObjectInputStream(byteArrayInputStream);

                    Scan singleScan = (Scan) objectInputStream.readObject();
                    allDecodedLastScans.add(singleScan);

                    objectInputStream.close();
                    byteArrayInputStream.close();
                } catch (ClassNotFoundException ignored) {
                } catch (IOException ignored) {}
            }
        }

        return allDecodedLastScans;
    }

    private void handleScannedDateTextView(){
        TextView scannedDateTextView = (TextView) findViewById(R.id.activity_single_scan_view_scanned_date_full_date_text_view);
        scannedDateTextView.setText(this.formatDateTime());
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
                    SingleScannedAppDetailsPermissionListAdapter singleScannedAppDetailsPermissionListAdapter = new SingleScannedAppDetailsPermissionListAdapter(getApplicationContext(), getMinimalRiskPermissions(scanToView.getScannedApps().get(position).getAllPermissions()));
                    minimalPermissionsListView.setAdapter(singleScannedAppDetailsPermissionListAdapter);

                    allPermissionsListView.setVisibility(View.INVISIBLE);
                    minimalPermissionsListView.setVisibility(View.VISIBLE);
                    lowPermissionsListView.setVisibility(View.INVISIBLE);
                    moderatePermissionsListView.setVisibility(View.INVISIBLE);
                    highPermissionsListView.setVisibility(View.INVISIBLE);
                    mostDangerousPermissionsListView.setVisibility(View.INVISIBLE);
                } else if (tab.getPosition() == 2){
                    //Low Risk Permissions
                    SingleScannedAppDetailsPermissionListAdapter singleScannedAppDetailsPermissionListAdapter = new SingleScannedAppDetailsPermissionListAdapter(getApplicationContext(), getLowRiskPermissions(scanToView.getScannedApps().get(position).getAllPermissions()));
                    lowPermissionsListView.setAdapter(singleScannedAppDetailsPermissionListAdapter);

                    allPermissionsListView.setVisibility(View.INVISIBLE);
                    minimalPermissionsListView.setVisibility(View.INVISIBLE);
                    lowPermissionsListView.setVisibility(View.VISIBLE);
                    moderatePermissionsListView.setVisibility(View.INVISIBLE);
                    highPermissionsListView.setVisibility(View.INVISIBLE);
                    mostDangerousPermissionsListView.setVisibility(View.INVISIBLE);
                } else if (tab.getPosition() == 3){
                    //Moderate Risk Permissions
                    SingleScannedAppDetailsPermissionListAdapter singleScannedAppDetailsPermissionListAdapter = new SingleScannedAppDetailsPermissionListAdapter(getApplicationContext(), getModerateRiskPermissions(scanToView.getScannedApps().get(position).getAllPermissions()));
                    moderatePermissionsListView.setAdapter(singleScannedAppDetailsPermissionListAdapter);

                    allPermissionsListView.setVisibility(View.INVISIBLE);
                    minimalPermissionsListView.setVisibility(View.INVISIBLE);
                    lowPermissionsListView.setVisibility(View.INVISIBLE);
                    moderatePermissionsListView.setVisibility(View.VISIBLE);
                    highPermissionsListView.setVisibility(View.INVISIBLE);
                    mostDangerousPermissionsListView.setVisibility(View.INVISIBLE);
                } else if (tab.getPosition() == 4){
                    //High Risk Permissions
                    SingleScannedAppDetailsPermissionListAdapter singleScannedAppDetailsPermissionListAdapter = new SingleScannedAppDetailsPermissionListAdapter(getApplicationContext(), getHighRiskPermissions(scanToView.getScannedApps().get(position).getAllPermissions()));
                    highPermissionsListView.setAdapter(singleScannedAppDetailsPermissionListAdapter);

                    allPermissionsListView.setVisibility(View.INVISIBLE);
                    minimalPermissionsListView.setVisibility(View.INVISIBLE);
                    lowPermissionsListView.setVisibility(View.INVISIBLE);
                    moderatePermissionsListView.setVisibility(View.INVISIBLE);
                    highPermissionsListView.setVisibility(View.VISIBLE);
                    mostDangerousPermissionsListView.setVisibility(View.INVISIBLE);
                } else if (tab.getPosition() == 5){
                    //Most Dangerous Permissions
                    SingleScannedAppDetailsPermissionListAdapter singleScannedAppDetailsPermissionListAdapter = new SingleScannedAppDetailsPermissionListAdapter(getApplicationContext(), getMostDangerousPermissions(scanToView.getScannedApps().get(position).getAllPermissions()));
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

        appIcon.setImageDrawable(this.stringToDrawable(this.scanToView.getAllAppIcons().get(position)));
        appName.setText(this.scanToView.getAllAppNames().get(position));
        packageName.setText(this.scanToView.getAllPackageNames().get(position));

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

    private String formatDateTime(){
        String dateString = "";
        if (this.scanToView.getScanDateTime().getDayOfMonth() < 10){
            dateString += "0" + String.valueOf(this.scanToView.getScanDateTime().getDayOfMonth());
        } else {
            dateString += String.valueOf(this.scanToView.getScanDateTime().getDayOfMonth());
        }
        if (this.scanToView.getScanDateTime().getMonthValue() < 10){
            dateString += "/0" + String.valueOf(this.scanToView.getScanDateTime().getMonthValue());
        } else {
            dateString += "/" + String.valueOf(this.scanToView.getScanDateTime().getMonthValue());
        }
        dateString += "/" + String.valueOf(this.scanToView.getScanDateTime().getYear());

        if (this.scanToView.getScanDateTime().getHour() < 10){
            dateString += " 0" + String.valueOf(this.scanToView.getScanDateTime().getHour());
        } else {
            dateString += " " + String.valueOf(this.scanToView.getScanDateTime().getHour());
        }
        if (this.scanToView.getScanDateTime().getMinute() < 10){
            dateString += ":0" + String.valueOf(this.scanToView.getScanDateTime().getMinute());
        } else {
            dateString += ":" + String.valueOf(this.scanToView.getScanDateTime().getMinute());
        }
        if (this.scanToView.getScanDateTime().getSecond() < 10){
            dateString += ":0" + String.valueOf(this.scanToView.getScanDateTime().getSecond());
        } else {
            dateString += ":" + String.valueOf(this.scanToView.getScanDateTime().getSecond());
        }
        return dateString;
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
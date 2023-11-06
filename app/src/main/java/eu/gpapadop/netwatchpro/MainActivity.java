package eu.gpapadop.netwatchpro;

import androidx.appcompat.app.AppCompatActivity;

import android.app.Dialog;
import android.content.Intent;
import androidx.appcompat.widget.Toolbar;

import android.net.VpnService;
import android.os.Bundle;
import android.os.Handler;
import android.util.Base64;
import android.util.DisplayMetrics;
import android.util.Log;
import android.util.TypedValue;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.widget.Button;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.RelativeLayout;
import android.widget.Switch;
import android.widget.TextView;

import com.google.android.material.bottomsheet.BottomSheetDialog;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.time.Instant;
import java.time.LocalDateTime;
import java.time.ZoneOffset;
import java.time.temporal.ChronoUnit;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import eu.gpapadop.netwatchpro.adapters.SingleLastScanAdapter;
import eu.gpapadop.netwatchpro.adapters.SingleLastScanEmptyAdapter;
import eu.gpapadop.netwatchpro.api.RequestsHandler;
import eu.gpapadop.netwatchpro.classes.last_scans.Scan;
import eu.gpapadop.netwatchpro.handlers.SharedPreferencesHandler;
import eu.gpapadop.netwatchpro.interfaces.OkHttpRequestCallback;
import eu.gpapadop.netwatchpro.notifications.NotificationsHandler;
import eu.gpapadop.netwatchpro.services.ArctourosVpnService;

public class MainActivity extends AppCompatActivity {
    final String baseNotificationURL = "https://arctouros.ict.ihu.gr/api/v1/notifications/";
    final String baseServiceStatusURL = "https://arctouros.ict.ihu.gr/api/v1/service-status/vpn-service";
    private SharedPreferencesHandler sharedPreferencesHandler;
    private boolean isHeartbeat = false;
    private NotificationsHandler notificationsHandler;
    private boolean serresVpnRunning = false;
    private Connectivity connectivity;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        this.sharedPreferencesHandler = new SharedPreferencesHandler(getApplicationContext());
        this.notificationsHandler = new NotificationsHandler(getApplicationContext());
        this.connectivity = new Connectivity(getApplicationContext());
        this.connectivity.initialize();
        this.handleStatusBarColor();
        //Get Server Information
        this.handleGetNotifications();
        this.checkVpnServerRunning();

        this.handleNotificationsClick();
        this.handleLastCheckTextView();
        this.handleCheckIconImageView();
        this.handleSettingsTap();
        //Scan Apps
        this.scanYourAppsRowTap();
        //Full Scan Apps
        this.fullScanYourAppsRowTap();
        //VPN
        this.handleVpnSwitchTap();
        this.handleOurServerStatusRowClick();
        //Last Scans
        this.handleLastScansListView();
    }

    @Override
    protected void onDestroy(){
        super.onDestroy();
        this.notificationsHandler.hideNotification();
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

    private void checkVpnServerRunning(){
        RequestsHandler serviceStatusAPI = new RequestsHandler();
        serviceStatusAPI.makeOkHttpRequest(baseServiceStatusURL, new OkHttpRequestCallback() {
            @Override
            public void onResponse(JSONObject jsonObject) {
                try {
                    String vpnServerStatus = jsonObject.getString("status");
                    serresVpnRunning = vpnServerStatus.toLowerCase().contains("running");
                } catch (JSONException ignored){}
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
            LocalDateTime lastCheckTime = LocalDateTime.ofInstant(Instant.ofEpochSecond(lastCheckTimestamp), ZoneOffset.UTC);
            lastCheckTimeTextView.setText(this.formatDateTime(lastCheckTime));
        }
    }

    private void handleCheckIconImageView(){
        ImageView checkIconImageView = (ImageView) findViewById(R.id.activity_main_shield_check_imageview);
        TextView youAreProtectedTextView = (TextView) findViewById(R.id.activity_main_you_are_protected_textview);
        FrameLayout protectedFrameLayout = (FrameLayout) findViewById(R.id.activity_main_protection_row);
        Toolbar mainToolbar = (Toolbar) findViewById(R.id.toolbar);
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
        } else {
            LocalDateTime lastCheckTime = LocalDateTime.ofInstant(Instant.ofEpochSecond(lastCheckTimestamp), ZoneOffset.UTC);
            LocalDateTime currentTime = LocalDateTime.now(ZoneOffset.UTC);
            long daysDifference = ChronoUnit.DAYS.between(lastCheckTime, currentTime);
            if (daysDifference > 7){
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

    private String formatDateTime(LocalDateTime dateTimeToFormat){
        String returnString = "";
        if (dateTimeToFormat.getDayOfMonth() < 10){
            returnString += "0" + String.valueOf(dateTimeToFormat.getDayOfMonth());
        } else {
            returnString += String.valueOf(dateTimeToFormat.getDayOfMonth());
        }
        if (dateTimeToFormat.getMonthValue() < 10){
            returnString += "/0" + String.valueOf(dateTimeToFormat.getMonthValue());
        } else {
            returnString += "/" + String.valueOf(dateTimeToFormat.getMonthValue());
        }
        returnString += "/" + String.valueOf(dateTimeToFormat.getYear());
        if (dateTimeToFormat.getHour() < 10){
            returnString += " 0" + String.valueOf(dateTimeToFormat.getHour());
        } else {
            returnString += " " + String.valueOf(dateTimeToFormat.getHour());
        }
        if (dateTimeToFormat.getMinute() < 10){
            returnString += ":0" + String.valueOf(dateTimeToFormat.getMinute());
        } else {
            returnString += ":" + String.valueOf(dateTimeToFormat.getMinute());
        }
        if (dateTimeToFormat.getSecond() < 10){
            returnString += ":0" + String.valueOf(dateTimeToFormat.getSecond());
        } else {
            returnString += ":" + String.valueOf(dateTimeToFormat.getSecond());
        }
        return returnString;
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

    private void handleVpnSwitchTap(){
        Switch vpnToggleSwitch = (Switch) findViewById(R.id.vpn_container_card_view_vpn_switch);
        vpnToggleSwitch.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (connectivity.getConnectionType() == 0){
                    displayNoInternetDialog();
                    return;
                }
                if (vpnToggleSwitch.isChecked()){
                    //Connect to VPN
                    Intent intent = VpnService.prepare(getApplicationContext());
                    if (intent != null) {
                        startActivityForResult(intent, 2002);
                    } else {
                        startVpnService();
                    }
                } else {
                    //Disconnect to VPN
                    stopVpnService();
                }
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

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == 2002 && resultCode == RESULT_OK) {
            startVpnService();
        }
    }

    private void startVpnService() {
        Intent vpnIntent = new Intent(this, ArctourosVpnService.class);
        startService(vpnIntent);
        this.notificationsHandler.showStickyNotification(getString(R.string.netwatch_pro_vpn), getString(R.string.you_are_connected_to_netwatch_pro_vpn));
    }

    private void stopVpnService() {
        Intent vpnIntent = new Intent(this, ArctourosVpnService.class);
        stopService(vpnIntent);
        this.notificationsHandler.hideNotification();
    }

    private void handleOurServerStatusRowClick(){
        TextView ourServerStatusTextView = (TextView) findViewById(R.id.vpn_container_card_view_server_status_text_view);
        ImageView ourServerStatusArrow = (ImageView) findViewById(R.id.vpn_container_card_view_server_status_arrow_button);

        ourServerStatusTextView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                final BottomSheetDialog bottomSheet = new BottomSheetDialog(MainActivity.this);
                bottomSheet.setContentView(R.layout.modal_sheet_our_server_status);
                TextView serresServerStatusTextView = (TextView) bottomSheet.findViewById(R.id.modal_sheet_our_server_status_running_stop_text_view);

                if (serresVpnRunning){
                    serresServerStatusTextView.setText(getString(R.string.running));
                    serresServerStatusTextView.setTextColor(getColor(R.color.primary_green));
                } else {
                    serresServerStatusTextView.setText(getString(R.string.down));
                    serresServerStatusTextView.setTextColor(getColor(R.color.red));
                }

                bottomSheet.show();
            }
        });

        ourServerStatusArrow.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                final BottomSheetDialog bottomSheet = new BottomSheetDialog(MainActivity.this);
                bottomSheet.setContentView(R.layout.modal_sheet_our_server_status);
                TextView serresServerStatusTextView = (TextView) bottomSheet.findViewById(R.id.modal_sheet_our_server_status_running_stop_text_view);

                if (serresVpnRunning){
                    serresServerStatusTextView.setText(getString(R.string.running));
                    serresServerStatusTextView.setTextColor(getColor(R.color.primary_green));
                } else {
                    serresServerStatusTextView.setText(getString(R.string.down));
                    serresServerStatusTextView.setTextColor(getColor(R.color.red));
                }

                bottomSheet.show();
            }
        });
    }

    private void handleLastScansListView(){
        ListView lastScansListView = (ListView) findViewById(R.id.last_scans_container_card_view_last_scans_list_view);

        RelativeLayout lastScansContainer = (RelativeLayout) findViewById(R.id.last_scans_container_relative_layout);
        ViewGroup.LayoutParams layoutParams = lastScansContainer.getLayoutParams();
        DisplayMetrics displayMetrics = getResources().getDisplayMetrics();

        Set<String> lastScans = this.sharedPreferencesHandler.getLatestScans();
        if (lastScans.isEmpty()){
            lastScansListView.setAdapter(new SingleLastScanEmptyAdapter(getApplicationContext()));
            int newHeightInDp = 130;
            layoutParams.height = (int) TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, newHeightInDp, displayMetrics);
            lastScansContainer.setLayoutParams(layoutParams);
        } else {
            //Has Last Scans
            List<Scan> allScans = this.decodeLastScans(lastScans);
            if (allScans.size() > 5){
                List<Scan> scansToDisplay = new ArrayList<>();
                for (int i = 0; i<5; i++){
                    scansToDisplay.add(allScans.get(i));
                }
                lastScansListView.setAdapter(new SingleLastScanAdapter(getApplicationContext(), scansToDisplay));
            } else {
                lastScansListView.setAdapter(new SingleLastScanAdapter(getApplicationContext(), allScans));
            }
        }
    }

    private List<Scan> decodeLastScans(Set<String> allLastScans){
        List<Scan> allScans = new ArrayList<>();
        for (String scan : allLastScans){
            if (scan != null){
                byte[] serializedBytes = Base64.decode(scan, Base64.DEFAULT);
                ByteArrayInputStream byteArrayInputStream = new ByteArrayInputStream(serializedBytes);
                try {
                    ObjectInputStream objectInputStream = new ObjectInputStream(byteArrayInputStream);
                    Scan singleScan = (Scan) objectInputStream.readObject();
                    allScans.add(singleScan);
                    objectInputStream.close();
                } catch (IOException | ClassNotFoundException ignored){}
            }
        }
        return allScans;
    }
}
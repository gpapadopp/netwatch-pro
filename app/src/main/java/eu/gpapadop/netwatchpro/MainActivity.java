package eu.gpapadop.netwatchpro;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import androidx.appcompat.widget.Toolbar;
import android.os.Bundle;
import android.os.Handler;
import android.view.View;
import android.view.Window;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.TextView;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.time.Instant;
import java.time.LocalDateTime;
import java.time.ZoneOffset;
import java.time.temporal.ChronoUnit;

import eu.gpapadop.netwatchpro.api.RequestsHandler;
import eu.gpapadop.netwatchpro.handlers.SharedPreferencesHandler;
import eu.gpapadop.netwatchpro.interfaces.OkHttpRequestCallback;

public class MainActivity extends AppCompatActivity {
    final String baseNotificationURL = "https://arctouros.ict.ihu.gr/api/v1/notifications/";
    private SharedPreferencesHandler sharedPreferencesHandler;
    private boolean isHeartbeat = false;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        this.sharedPreferencesHandler = new SharedPreferencesHandler(getApplicationContext());
        this.handleStatusBarColor();
        this.handleGetNotifications();
        this.handleNotificationsClick();
        this.handleLastCheckTextView();
        this.handleCheckIconImageView();
        this.handleSettingsTap();
        //Scan Apps
        this.scanYourAppsRowTap();
        //Full Scan Apps
        this.fullScanYourAppsRowTap();
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
                Intent fullScanYourAppsIntent = new Intent(getApplicationContext(), FullScanYourAppsActivity.class);
                startActivity(fullScanYourAppsIntent);
                finish();
            }
        });
    }
}
package eu.gpapadop.netwatchpro;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.time.Instant;
import java.time.LocalDateTime;
import java.time.ZoneOffset;
import java.time.temporal.ChronoUnit;

import eu.gpapadop.netwatchpro.api.RequestsHandler;
import eu.gpapadop.netwatchpro.interfaces.OkHttpRequestCallback;

public class MainActivity extends AppCompatActivity {
    final String baseNotificationURL = "https://arctouros.ict.ihu.gr/api/v1/notifications/";
    private SharedPreferencesHandler sharedPreferencesHandler;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        this.sharedPreferencesHandler = new SharedPreferencesHandler(getApplicationContext());
        this.handleGetNotifications();
        this.handleNotificationsClick();
        this.handleLastCheckTextView();
        this.handleCheckIconImageView();
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
        long lastCheckTimestamp = this.sharedPreferencesHandler.getLastCheckDateTime();
        if (lastCheckTimestamp == 0){
            checkIconImageView.setImageResource(R.drawable.shield_close);
            youAreProtectedTextView.setText(getString(R.string.you_are_not_protected));
        } else {
            LocalDateTime lastCheckTime = LocalDateTime.ofInstant(Instant.ofEpochSecond(lastCheckTimestamp), ZoneOffset.UTC);
            LocalDateTime currentTime = LocalDateTime.now(ZoneOffset.UTC);
            long daysDifference = ChronoUnit.DAYS.between(lastCheckTime, currentTime);
            if (daysDifference > 7){
                checkIconImageView.setImageResource(R.drawable.shield_close);
                youAreProtectedTextView.setText(getString(R.string.you_are_not_protected));
            }
        }
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
}
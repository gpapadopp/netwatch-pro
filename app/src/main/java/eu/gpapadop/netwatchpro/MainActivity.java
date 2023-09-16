package eu.gpapadop.netwatchpro;

import androidx.appcompat.app.AppCompatActivity;

import android.app.ActivityOptions;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import eu.gpapadop.netwatchpro.api.RequestsHandler;
import eu.gpapadop.netwatchpro.interfaces.OkHttpRequestCallback;

public class MainActivity extends AppCompatActivity {
    final String baseNotificationURL = "https://arctouros.ict.ihu.gr/api/v1/notifications/";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        this.handleGetNotifications();
        this.handleNotificationsClick();
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
                startActivity(notificationsIntent);
            }
        });
    }
}
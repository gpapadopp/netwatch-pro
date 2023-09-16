package eu.gpapadop.netwatchpro;

import androidx.appcompat.app.AppCompatActivity;

import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.ListView;
import android.widget.ProgressBar;
import android.widget.TextView;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.List;

import eu.gpapadop.netwatchpro.adapters.SingleNotificationCustomAdapter;
import eu.gpapadop.netwatchpro.api.RequestsHandler;
import eu.gpapadop.netwatchpro.interfaces.OkHttpRequestCallback;

public class NotificationsListView extends AppCompatActivity {
    final String baseNotificationURL = "https://arctouros.ict.ihu.gr/api/v1/notifications/";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_notifications_list_view);
        this.handleGetNotifications();
    }

    private void handleGetNotifications(){
        RequestsHandler notificationsAPI = new RequestsHandler();
        notificationsAPI.makeOkHttpRequest(this.baseNotificationURL, new OkHttpRequestCallback() {
            @Override
            public void onResponse(JSONObject jsonObject) {
                try {
                    JSONArray jsonArray = jsonObject.getJSONArray("notifications");
                    List<String> allIDs = new ArrayList<>();
                    List<String> allTitles = new ArrayList<>();
                    List<String> allContexts = new ArrayList<>();
                    for (int i = 0; i<jsonArray.length(); i++){
                        JSONObject notificationObject = jsonArray.getJSONObject(i);
                        if (!notificationObject.getBoolean("disabled")) {
                            allIDs.add(notificationObject.getString("id"));
                            allTitles.add(notificationObject.getString("title"));
                            allContexts.add(notificationObject.getString("context"));
                        }
                    }
                    runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            ProgressBar notificationProgressBar = findViewById(R.id.notifications_activity_progress_bar);
                            ListView notificationListView = findViewById(R.id.notifications_activity_list_view);
                            SingleNotificationCustomAdapter singleNotificationCustomAdapter = new SingleNotificationCustomAdapter(getBaseContext(), allIDs, allTitles, allContexts);
                            notificationProgressBar.setVisibility(View.INVISIBLE);
                            notificationListView.setAdapter(singleNotificationCustomAdapter);
                        }
                    });
                } catch (JSONException ignored){
                }
            }

            @Override
            public void onError(Exception e) {
            }
        });
    }
}
package eu.gpapadop.netwatchpro;

import androidx.appcompat.app.AppCompatActivity;
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ImageButton;
import android.widget.ListView;
import android.widget.ProgressBar;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.List;

import eu.gpapadop.netwatchpro.adapters.listviews.SingleNotificationCustomAdapter;
import eu.gpapadop.netwatchpro.api.RequestsHandler;
import eu.gpapadop.netwatchpro.interfaces.OkHttpRequestCallback;

public class NotificationsListView extends AppCompatActivity {
    final String baseNotificationURL = "https://arctouros.ict.ihu.gr/api/v1/notifications/";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_notifications_list_view);
        this.handleGetNotifications();
        this.handleSwipeToRefreshLayout();
        this.handleToolbarBackButton();
    }

    @Override
    public void onBackPressed() {
        super.onBackPressed();
        overridePendingTransition(0, R.anim.intent_transitions_slide_left_to_right);
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
                    List<String> allCreatedDates = new ArrayList<>();
                    for (int i = 0; i<jsonArray.length(); i++){
                        JSONObject notificationObject = jsonArray.getJSONObject(i);
                        if (!notificationObject.getBoolean("disabled")) {
                            allIDs.add(notificationObject.getString("id"));
                            allTitles.add(notificationObject.getString("title"));
                            allContexts.add(notificationObject.getString("context"));
                            allCreatedDates.add(notificationObject.getString("created_at"));
                        }
                    }
                    runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            ProgressBar notificationProgressBar = findViewById(R.id.notifications_activity_progress_bar);
                            ListView notificationListView = findViewById(R.id.notifications_activity_list_view);
                            SingleNotificationCustomAdapter singleNotificationCustomAdapter = new SingleNotificationCustomAdapter(getBaseContext(), allIDs, allTitles, allContexts, allCreatedDates);
                            notificationProgressBar.setVisibility(View.INVISIBLE);
                            notificationListView.setAdapter(null);
                            notificationListView.setAdapter(singleNotificationCustomAdapter);
                            notificationListView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
                                @Override
                                public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                                    Intent singleNotificationIntent = new Intent(getApplicationContext(), SingleNotificationView.class);
                                    singleNotificationIntent.putExtra("id", allIDs.get(position));
                                    singleNotificationIntent.putExtra("title", allTitles.get(position));
                                    singleNotificationIntent.putExtra("context", allContexts.get(position));
                                    singleNotificationIntent.putExtra("created_date", allCreatedDates.get(position));
                                    startActivity(singleNotificationIntent);
                                }
                            });
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

    private void handleToolbarBackButton(){
        ImageButton toolbarBackButton = (ImageButton) findViewById(R.id.notifications_activity_toolbar_back_button);
        toolbarBackButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                onBackPressed();
            }
        });
    }

    private void handleSwipeToRefreshLayout(){
        SwipeRefreshLayout swipeRefreshLayout = (SwipeRefreshLayout) findViewById(R.id.notifications_activity_swipe_to_refresh_layout);
        swipeRefreshLayout.setOnRefreshListener(new SwipeRefreshLayout.OnRefreshListener() {
            @Override
            public void onRefresh() {
                handleGetNotifications();
                swipeRefreshLayout.setRefreshing(false);
            }
        });
    }
}
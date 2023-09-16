package eu.gpapadop.netwatchpro;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.TextView;

import com.squareup.picasso.Picasso;

import java.time.Duration;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

public class SingleNotificationView extends AppCompatActivity {
    private final String baseNotificationURL = "https://arctouros.ict.ihu.gr/api/v1/notifications/";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_single_notification_view);

        Intent intent = getIntent();
        String notificationID = intent.getStringExtra("id");
        String notificationTitle = intent.getStringExtra("title");
        String notificationContext = intent.getStringExtra("context");
        String notificationCreatedDate = intent.getStringExtra("created_date");

        this.handleToolBarBackButton();

        TextView notificationTitleTextView = (TextView) findViewById(R.id.single_notification_view_title_text_view);
        ImageView notificationImageView = (ImageView) findViewById(R.id.single_notification_view_banner_image_view);
        TextView notificationMainContext = (TextView) findViewById(R.id.single_notification_view_context_text_view);
        TextView notificationTimeAgo = (TextView) findViewById(R.id.single_notification_view_time_ago_text_view);

        notificationTitleTextView.setText(notificationTitle);
        Picasso.get().load(this.baseNotificationURL + "get-banner/" + notificationID).into(notificationImageView);
        notificationMainContext.setText(notificationContext);
        notificationTimeAgo.setText(this.calculateTimeAgo(notificationCreatedDate));
    }

    @Override
    public void onBackPressed() {
        super.onBackPressed();
    }

    private void handleToolBarBackButton(){
        ImageButton toolbarBackButton = (ImageButton) findViewById(R.id.single_notification_view_toolbar_back_button);
        toolbarBackButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                onBackPressed();
            }
        });
    }

    private String calculateTimeAgo(String dateTimeString){
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss.SSSSSS");
        LocalDateTime dateTime = LocalDateTime.parse(dateTimeString, formatter);

        LocalDateTime currentDateTime = LocalDateTime.now();
        Duration duration = Duration.between(dateTime, currentDateTime);

        long secondsAgo = duration.getSeconds();
        long minutesAgo = duration.toMinutes();
        long hoursAgo = duration.toHours();
        long daysAgo = duration.toDays();

        if (secondsAgo < 60) {
            return secondsAgo + " " + getString(R.string.seconds_ago);
        } else if (minutesAgo < 60) {
            return minutesAgo + " " + getString(R.string.minutes_ago);
        } else if (hoursAgo < 24) {
            return hoursAgo + " " + getString(R.string.hours_ago);
        } else {
            return daysAgo + " " + getString(R.string.days_ago);
        }
    }
}
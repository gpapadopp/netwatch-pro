package eu.gpapadop.netwatchpro;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.content.res.Resources;
import android.content.res.XmlResourceParser;
import android.os.Bundle;
import android.view.View;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.TextView;

import com.squareup.picasso.Picasso;

import org.xmlpull.v1.XmlPullParser;

import eu.gpapadop.netwatchpro.utils.DateTimeUtils;

public class SingleNotificationView extends AppCompatActivity {
    private String baseNotificationURL = "";
    private DateTimeUtils dateTimeUtils;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_single_notification_view);

        //Get Server Config
        this.getServerConfig();

        this.dateTimeUtils = new DateTimeUtils(getApplicationContext());

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
        notificationTimeAgo.setText(this.dateTimeUtils.calculateTimeAgo(notificationCreatedDate));
    }

    @Override
    public void onBackPressed() {
        super.onBackPressed();
    }

    private void getServerConfig(){
        Resources resources = this.getResources();
        try (XmlResourceParser xmlResourceParser = resources.getXml(R.xml.server_config)) {
            int eventType = xmlResourceParser.getEventType();

            while (eventType != XmlPullParser.END_DOCUMENT) {
                if (eventType == XmlPullParser.START_TAG && "string".equals(xmlResourceParser.getName())) {
                    String name = xmlResourceParser.getAttributeValue(null, "name");

                    if ("server_host".equals(name)) {
                        xmlResourceParser.next();
                        this.baseNotificationURL = xmlResourceParser.getText() + "/v1/notifications/";
                    }
                }

                eventType = xmlResourceParser.next();
            }
        } catch (Exception ignored) {}
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
}
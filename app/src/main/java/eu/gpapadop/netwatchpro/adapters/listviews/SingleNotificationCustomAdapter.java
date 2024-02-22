package eu.gpapadop.netwatchpro.adapters.listviews;

import android.content.Context;
import android.content.res.Resources;
import android.content.res.XmlResourceParser;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.TextView;

import com.squareup.picasso.Picasso;

import org.xmlpull.v1.XmlPullParser;

import java.time.Duration;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;

import eu.gpapadop.netwatchpro.R;

public class SingleNotificationCustomAdapter extends BaseAdapter {
    private String baseNotificationURL = "";
    private Context context;
    private List<String> ids;
    private List<String> titles;
    private List<String> contexts;
    private List<String> createdDates;

    public SingleNotificationCustomAdapter(Context newContext, List<String> newIDs, List<String> newTitles, List<String> newContexts, List<String> newCreatedDates){
        this.context = newContext;
        this.ids = newIDs;
        this.titles = newTitles;
        this.contexts = newContexts;
        this.createdDates = newCreatedDates;
        this.getServerConfig();
    }

    private void getServerConfig(){
        Resources resources = this.context.getResources();
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

    @Override
    public int getCount() {
        return this.ids.size();
    }

    @Override
    public Object getItem(int position) {
        return this;
    }

    @Override
    public long getItemId(int position) {
        String identifier = ids.get(position); // Replace with your actual data retrieval logic
        return identifier.hashCode();
    }

    public Context getContext(){
        return this.context;
    }

    public String getItemTitle(int position){
        return this.titles.get(position);
    }

    public String getItemContext(int position){
        return this.contexts.get(position);
    }

    public String getCreatedAt(int position){
        return this.createdDates.get(position);
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        if (convertView == null){
            LayoutInflater inflater = LayoutInflater.from(context);
            convertView = inflater.inflate(R.layout.custom_notification_list_item, parent, false);
        }
        TextView titleTextView = (TextView) convertView.findViewById(R.id.custom_notification_list_item_title_text_view);
        ImageView bannerImageView = (ImageView) convertView.findViewById(R.id.custom_notification_list_item_banner_image_view);
        TextView contextTextView = (TextView) convertView.findViewById(R.id.custom_notification_list_item_context_text_view);
        TextView timeAgoTextView = (TextView) convertView.findViewById(R.id.custom_notification_list_item_time_ago_text_view);

        titleTextView.setText(this.titles.get(position));
        contextTextView.setText(this.contexts.get(position));
        timeAgoTextView.setText(this.calculateTimeAgo(this.createdDates.get(position)));
        Picasso.get().load(this.baseNotificationURL + "get-banner/" + this.ids.get(position)).into(bannerImageView);

        return convertView;
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
            return secondsAgo + " " + getContext().getString(R.string.seconds_ago);
        } else if (minutesAgo < 60) {
            return minutesAgo + " " + getContext().getString(R.string.minutes_ago);
        } else if (hoursAgo < 24) {
            return hoursAgo + " " + getContext().getString(R.string.hours_ago);
        } else {
            return daysAgo + " " + getContext().getString(R.string.days_ago);
        }
    }
}

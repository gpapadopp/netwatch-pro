package eu.gpapadop.netwatchpro.adapters;

import android.content.Context;
import android.content.Intent;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.cardview.widget.CardView;

import com.squareup.picasso.Picasso;

import java.time.Duration;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;

import eu.gpapadop.netwatchpro.R;

public class SingleNotificationCustomAdapter extends BaseAdapter {
    private final String baseNotificationURL = "https://arctouros.ict.ihu.gr/api/v1/notifications/";
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

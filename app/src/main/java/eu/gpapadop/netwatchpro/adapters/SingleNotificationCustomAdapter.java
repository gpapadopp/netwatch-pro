package eu.gpapadop.netwatchpro.adapters;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.TextView;

import com.squareup.picasso.Picasso;

import java.util.List;

import eu.gpapadop.netwatchpro.R;

public class SingleNotificationCustomAdapter extends BaseAdapter {
    private final String baseNotificationURL = "https://arctouros.ict.ihu.gr/api/v1/notifications/";
    private Context context;
    private List<String> ids;
    private List<String> titles;
    private List<String> contexts;

    public SingleNotificationCustomAdapter(Context newContext, List<String> newIDs, List<String> newTitles, List<String> newContexts){
        this.context = newContext;
        this.ids = newIDs;
        this.titles = newTitles;
        this.contexts = newContexts;
    }

    @Override
    public int getCount() {
        return this.ids.size();
    }

    @Override
    public Object getItem(int position) {
        return this.ids.get(position);
    }

    @Override
    public long getItemId(int position) {
        return Long.parseLong(this.ids.get(position));
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

        titleTextView.setText(this.titles.get(position));
        contextTextView.setText(this.contexts.get(position));
        Picasso.get().load(this.baseNotificationURL + "get-banner/" + this.ids.get(position)).into(bannerImageView);

        return convertView;
    }
}

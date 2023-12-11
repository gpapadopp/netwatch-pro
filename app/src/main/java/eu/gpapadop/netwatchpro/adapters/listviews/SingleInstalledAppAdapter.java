package eu.gpapadop.netwatchpro.adapters.listviews;

import android.content.Context;
import android.graphics.drawable.Drawable;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.TextView;

import java.util.List;

import eu.gpapadop.netwatchpro.R;

public class SingleInstalledAppAdapter extends BaseAdapter {
    private Context context;
    private List<Drawable> allAppIcons;
    private List<String> allAppNames;

    public SingleInstalledAppAdapter(Context newContext, List<Drawable> newAllAppIcons, List<String> newAllAppNames){
        this.context = newContext;
        this.allAppIcons = newAllAppIcons;
        this.allAppNames = newAllAppNames;
    }

    @Override
    public int getCount() {
        return this.allAppNames.size();
    }

    @Override
    public Object getItem(int position) {
        return this;
    }

    @Override
    public long getItemId(int position) {
        return Long.parseLong(String.valueOf(position));
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        if (convertView == null) {
            LayoutInflater inflater = LayoutInflater.from(context);
            convertView = inflater.inflate(R.layout.installed_apps_single_listitem, parent, false);
        }

        ImageView appIconImageView = (ImageView) convertView.findViewById(R.id.installed_apps_single_list_item_app_icon);
        TextView appNameTextView = (TextView) convertView.findViewById(R.id.installed_apps_single_list_item_app_name_textview);

        appIconImageView.setImageDrawable(this.allAppIcons.get(position));
        appNameTextView.setText(this.allAppNames.get(position));

        return convertView;
    }
}

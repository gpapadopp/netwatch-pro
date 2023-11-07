package eu.gpapadop.netwatchpro.adapters;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import android.util.Base64;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.TextView;

import java.util.List;

import eu.gpapadop.netwatchpro.R;
import pl.droidsonroids.gif.GifImageView;

public class SingleScannedAppsAdapter extends BaseAdapter {
    private Context context;
    private List<String> appNames;
    private List<String> allIcons;
    private List<Boolean> allIsMalware;
    private List<Boolean> allIsChecked;

    public SingleScannedAppsAdapter(Context newContext, List<String> newAppNames, List<String> newAllIcons, List<Boolean> newAllIsMalware, List<Boolean> newAllIsChecked){
        this.context = newContext;
        this.appNames = newAppNames;
        this.allIcons = newAllIcons;
        this.allIsMalware = newAllIsMalware;
        this.allIsChecked = newAllIsChecked;
    }

    @Override
    public int getCount() {
        return this.appNames.size();
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
        if (convertView == null){
            LayoutInflater inflater = LayoutInflater.from(context);
            convertView = inflater.inflate(R.layout.scanned_apps_single_listitem, parent, false);
        }

        ImageView appIconImageView = (ImageView) convertView.findViewById(R.id.scanned_apps_single_list_item_app_image_view);
        appIconImageView.setImageDrawable(this.stringToDrawable(this.allIcons.get(position)));

        TextView appNameTextView = (TextView) convertView.findViewById(R.id.scanned_apps_single_list_item_application_name_text_view);
        appNameTextView.setText(this.appNames.get(position));

        GifImageView loadingGifImageView = (GifImageView) convertView.findViewById(R.id.scanned_apps_single_list_item_loading_animation);
        TextView appIsSafeTextView = (TextView) convertView.findViewById(R.id.scanned_apps_single_list_item_app_is_save_text_view);
        TextView appWarningTextView = (TextView) convertView.findViewById(R.id.scanned_apps_single_list_item_app_warning_text_view);

        if (this.allIsChecked.get(position)){
            try {
                boolean isMalwareItem = this.allIsMalware.get(position);
                if (isMalwareItem){
                    appWarningTextView.setVisibility(View.VISIBLE);
                    appIsSafeTextView.setVisibility(View.GONE);
                } else {
                    appIsSafeTextView.setVisibility(View.VISIBLE);
                    appWarningTextView.setVisibility(View.GONE);
                }
                loadingGifImageView.setVisibility(View.GONE);
            } catch (IndexOutOfBoundsException e){
                appWarningTextView.setVisibility(View.GONE);
                appIsSafeTextView.setVisibility(View.GONE);
                loadingGifImageView.setVisibility(View.VISIBLE);
            }
        }

        return convertView;
    }

    public Drawable stringToDrawable(String drawableString) {
        if (drawableString == null) {
            return null;
        }

        byte[] byteArray = Base64.decode(drawableString, Base64.DEFAULT);
        Bitmap bitmap = BitmapFactory.decodeByteArray(byteArray, 0, byteArray.length);
        return new BitmapDrawable(this.context.getResources(), bitmap);
    }
}

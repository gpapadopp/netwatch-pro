package eu.gpapadop.netwatchpro.adapters;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;

import java.util.List;

import eu.gpapadop.netwatchpro.R;

public class SingleScannedAppsAdapter extends BaseAdapter {
    private Context context;
    private List<String> appNames;

    public SingleScannedAppsAdapter(Context newContext, List<String> newAppNames){
        this.context = newContext;
        this.appNames = newAppNames;
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

        return convertView;
    }
}

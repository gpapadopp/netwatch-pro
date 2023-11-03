package eu.gpapadop.netwatchpro.adapters;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;

import eu.gpapadop.netwatchpro.R;

public class SingleLastScanEmptyAdapter extends BaseAdapter {
    private Context context;

    public SingleLastScanEmptyAdapter(Context newContext){
        this.context = newContext;
    }

    @Override
    public int getCount() {
        return 1;
    }

    @Override
    public Object getItem(int position) {
        return this;
    }

    @Override
    public long getItemId(int position) {
        return 1;
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        if (convertView == null){
            LayoutInflater inflater = LayoutInflater.from(context);
            convertView = inflater.inflate(R.layout.last_scans_single_listitem_empty, parent, false);
        }
        return convertView;
    }
}

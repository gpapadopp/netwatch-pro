package eu.gpapadop.netwatchpro.adapters.listviews;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.TextView;

import java.util.List;

import eu.gpapadop.netwatchpro.R;

public class SingleScannedAppDetailsPermissionListAdapter extends BaseAdapter {
    private Context context;
    private List<String> allPermissions;

    public SingleScannedAppDetailsPermissionListAdapter(Context newContext, List<String> newAllPermissions){
        this.context = newContext;
        this.allPermissions = newAllPermissions;
    }

    @Override
    public int getCount() {
        return this.allPermissions.size();
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
            convertView = inflater.inflate(R.layout.modal_sheet_scanned_app_details_permission_listview_item, parent, false);
        }

        TextView permissionNameTextView = (TextView) convertView.findViewById(R.id.modal_sheet_scanned_app_details_permission_list_view_permission_name);
        permissionNameTextView.setText(this.allPermissions.get(position));

        return convertView;
    }
}

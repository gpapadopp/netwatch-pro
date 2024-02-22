package eu.gpapadop.netwatchpro.adapters.listviews;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.TextView;

import java.util.List;

import eu.gpapadop.netwatchpro.R;

public class SingleFileScannedAppsAdapter extends BaseAdapter {
    private Context context;
    private List<String> fileNames;
    private List<String> filePaths;

    public SingleFileScannedAppsAdapter(Context newContext, List<String> newFileNames, List<String> newFilePaths){
        this.context = newContext;
        this.fileNames = newFileNames;
        this.filePaths = newFilePaths;
    }

    @Override
    public int getCount() {
        return this.fileNames.size();
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
            convertView = inflater.inflate(R.layout.malicious_files_single_listitem, parent, false);
        }

        TextView fileNameTextView = (TextView) convertView.findViewById(R.id.malicious_files_single_list_item_name_text_view);
        TextView fileFullPathTextView = (TextView) convertView.findViewById(R.id.malicious_files_single_list_item_full_path_text_view);

        fileNameTextView.setText(this.fileNames.get(position));
        fileFullPathTextView.setText(this.filePaths.get(position));

        return convertView;
    }
}

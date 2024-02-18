package eu.gpapadop.netwatchpro.adapters.listviews;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.TextView;

import java.io.File;
import java.util.List;

import eu.gpapadop.netwatchpro.R;

public class MaliciousFilesAdapter extends BaseAdapter {
    private Context context;
    private List<File> maliciousFiles;

    public MaliciousFilesAdapter(Context newContext, List<File> newMaliciousFiles){
        this.context = newContext;
        this.maliciousFiles = newMaliciousFiles;
    }

    @Override
    public int getCount() {
        return this.maliciousFiles.size();
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
        TextView filePathTextView = (TextView) convertView.findViewById(R.id.malicious_files_single_list_item_full_path_text_view);

        fileNameTextView.setText(this.maliciousFiles.get(position).getName());
        filePathTextView.setText(this.maliciousFiles.get(position).getAbsolutePath());

        return convertView;
    }
}

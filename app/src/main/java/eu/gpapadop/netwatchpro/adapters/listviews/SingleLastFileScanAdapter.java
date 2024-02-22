package eu.gpapadop.netwatchpro.adapters.listviews;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.TextView;

import java.time.LocalDateTime;
import java.util.List;

import eu.gpapadop.netwatchpro.R;
import eu.gpapadop.netwatchpro.classes.files_scan.FilesScan;

public class SingleLastFileScanAdapter extends BaseAdapter {
    private Context context;
    private List<FilesScan> allScans;

    public SingleLastFileScanAdapter(Context newContext, List<FilesScan> newAllScans){
        this.context = newContext;
        this.allScans = newAllScans;
    }

    @Override
    public int getCount() {
        return this.allScans.size();
    }

    @Override
    public Object getItem(int position) {
        return this;
    }

    @Override
    public long getItemId(int position) {
        return this.allScans.get(position).getLongScanID();
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        if (convertView == null){
            LayoutInflater inflater = LayoutInflater.from(context);
            convertView = inflater.inflate(R.layout.last_scans_single_listitem, parent, false);
        }
        TextView dateTextView = (TextView) convertView.findViewById(R.id.last_scans_single_list_item_name_text_view);
        dateTextView.setText(this.convertDateToString(this.allScans.get(position).getScanDateTime()));
        return convertView;
    }

    private String convertDateToString(LocalDateTime dateToConvert){
        String returnDate = "";
        if (dateToConvert.getDayOfMonth() < 10){
            returnDate += "0" + String.valueOf(dateToConvert.getDayOfMonth());
        } else {
            returnDate += String.valueOf(dateToConvert.getDayOfMonth());
        }
        if (dateToConvert.getMonthValue() < 10){
            returnDate += "/0" + String.valueOf(dateToConvert.getMonthValue());
        } else {
            returnDate += "/" + String.valueOf(dateToConvert.getMonthValue());
        }
        returnDate += "/" + String.valueOf(dateToConvert.getYear());
        if (dateToConvert.getHour() < 10){
            returnDate += " 0" + String.valueOf(dateToConvert.getHour());
        } else {
            returnDate += " " + String.valueOf(dateToConvert.getHour());
        }
        if (dateToConvert.getMinute() < 10){
            returnDate += ":0" + String.valueOf(dateToConvert.getMinute());
        } else {
            returnDate += ":" + String.valueOf(dateToConvert.getMinute());
        }
        return returnDate;
    }
}

package eu.gpapadop.netwatchpro.handlers;

import static android.content.Context.MODE_PRIVATE;

import android.content.Context;
import android.content.SharedPreferences;

import java.util.HashSet;
import java.util.Set;

public class SharedPreferencesHandler {
    private Context appContext;
    private SharedPreferences sharedPrefs;

    public SharedPreferencesHandler(Context newAppContext){
        this.appContext = newAppContext;
        this.sharedPrefs = newAppContext.getSharedPreferences("NetWatchProSharedPrefs", MODE_PRIVATE);
    }

    public String getDeviceID(){
        return this.sharedPrefs.getString("device_unique_token", "NONE");
    }

    public void saveDeviceID(String newToken){
        SharedPreferences.Editor sharedPrefsEditor = this.sharedPrefs.edit();
        sharedPrefsEditor.putString("device_unique_token", newToken);
        sharedPrefsEditor.apply();
    }

    public boolean getHasAcceptTerms(){
        return this.sharedPrefs.getBoolean("user_has_accept_terms", false);
    }

    public void saveHasAcceptTerms(){
        SharedPreferences.Editor sharedPrefsEditor = this.sharedPrefs.edit();
        sharedPrefsEditor.putBoolean("user_has_accept_terms", true);
        sharedPrefsEditor.apply();
    }

    public long getLastCheckDateTime(){
        return this.sharedPrefs.getLong("last_check_timestamp_unix", 0);
    }

    public void saveLastCheckDateTime(long newTimestamp){
        SharedPreferences.Editor sharedPrefsEditor = this.sharedPrefs.edit();
        sharedPrefsEditor.putLong("last_check_timestamp_unix", newTimestamp);
        sharedPrefsEditor.apply();
    }

    public boolean getRecursiveEnabled(){
        return this.sharedPrefs.getBoolean("recursive_scan_enabled", false);
    }

    public void setRecursiveEnabled(boolean newRecursiveEnabled){
        SharedPreferences.Editor sharedPrefsEditor = this.sharedPrefs.edit();
        sharedPrefsEditor.putBoolean("recursive_scan_enabled", newRecursiveEnabled);
        sharedPrefsEditor.apply();
    }

    public int getRecursiveFrequency(){
        return this.sharedPrefs.getInt("recursive_scan_frequency", 0);
    }

    public void setRecursiveFrequency(int newRecursiveScanFrequency){
        //1 = Every Week
        //2 = Every 2 Weeks
        //4 = Every 4 Weeks
        SharedPreferences.Editor sharedPrefsEditor = this.sharedPrefs.edit();
        sharedPrefsEditor.putInt("recursive_scan_frequency", newRecursiveScanFrequency);
        sharedPrefsEditor.apply();
    }

    public int getRecursiveScanType(){
        return this.sharedPrefs.getInt("recursive_scan_type", 0);
    }

    public void setRecursiveScanType(int newRecursiveScanType){
        //0 = Disabled
        //1 = Quick Scan
        //2 = Full Scan
        //3 = File Scan
        SharedPreferences.Editor sharedPrefsEditor = this.sharedPrefs.edit();
        sharedPrefsEditor.putInt("recursive_scan_type", newRecursiveScanType);
        sharedPrefsEditor.apply();
    }

    public Set<String> getLatestScans(){
        return this.sharedPrefs.getStringSet("latest_scans_set", new HashSet<>());
    }

    public void setLatestScans(Set<String> newLatestScans){
        SharedPreferences.Editor sharedPrefsEditor = this.sharedPrefs.edit();
        sharedPrefsEditor.putStringSet("latest_scans_set", newLatestScans);
        sharedPrefsEditor.apply();
    }

    public Set<String> getFileScans(){
        return this.sharedPrefs.getStringSet("latest_file_scans_set", new HashSet<>());
    }

    public void setFileScans(Set<String> newFileScans){
        SharedPreferences.Editor sharedPrefsEditor = this.sharedPrefs.edit();
        sharedPrefsEditor.putStringSet("latest_file_scans_set", newFileScans);
        sharedPrefsEditor.apply();
    }

    public String getExportPath(){
        return this.sharedPrefs.getString("export_data_folder_path", "//");
    }

    public void setExportPath(String newFolderPath){
        SharedPreferences.Editor sharedPrefsEditor = this.sharedPrefs.edit();
        sharedPrefsEditor.putString("export_data_folder_path", newFolderPath);
        sharedPrefsEditor.apply();
    }

    public boolean getHasInit(){
        return this.sharedPrefs.getBoolean("app_has_initialize", false);
    }

    public void setHasInit(boolean newHasInit){
        SharedPreferences.Editor sharedPrefsEditor = this.sharedPrefs.edit();
        sharedPrefsEditor.putBoolean("app_has_initialize", newHasInit);
        sharedPrefsEditor.apply();
    }
}

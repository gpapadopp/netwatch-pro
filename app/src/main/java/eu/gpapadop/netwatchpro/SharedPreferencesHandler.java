package eu.gpapadop.netwatchpro;

import static android.content.Context.MODE_PRIVATE;

import android.content.Context;
import android.content.SharedPreferences;

public class SharedPreferencesHandler {
    private Context appContext;

    public SharedPreferencesHandler(Context newAppContext){
        this.appContext = newAppContext;
    }

    public String getDeviceID(){
        SharedPreferences sharedPrefs = this.appContext.getSharedPreferences("NetWatchProSharedPrefs", MODE_PRIVATE);
        return sharedPrefs.getString("device_unique_token", "NONE");
    }

    public void saveDeviceID(String newToken){
        SharedPreferences sharedPrefs = this.appContext.getSharedPreferences("NetWatchProSharedPrefs", MODE_PRIVATE);
        SharedPreferences.Editor sharedPrefsEditor = sharedPrefs.edit();
        sharedPrefsEditor.putString("device_unique_token", newToken);
        sharedPrefsEditor.apply();
    }

    public boolean getHasAcceptTerms(){
        SharedPreferences sharedPrefs = this.appContext.getSharedPreferences("NetWatchProSharedPrefs", MODE_PRIVATE);
        return sharedPrefs.getBoolean("user_has_accept_terms", false);
    }

    public void saveHasAcceptTerms(){
        SharedPreferences sharedPrefs = this.appContext.getSharedPreferences("NetWatchProSharedPrefs", MODE_PRIVATE);
        SharedPreferences.Editor sharedPrefsEditor = sharedPrefs.edit();
        sharedPrefsEditor.putBoolean("user_has_accept_terms", true);
        sharedPrefsEditor.apply();
    }
}

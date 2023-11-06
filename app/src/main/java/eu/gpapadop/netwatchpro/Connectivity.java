package eu.gpapadop.netwatchpro;

import android.content.Context;
import android.net.ConnectivityManager;
import android.net.NetworkCapabilities;

import java.util.UUID;

import eu.gpapadop.netwatchpro.handlers.SharedPreferencesHandler;

public class Connectivity {
    private Context appContext;
    private int connectionType;
    //0 = None
    //1 = Wi-Fi
    //2 = Mobile Data
    //3 = Ethernet
    private String deviceID;
    private SharedPreferencesHandler sharedPreferencesHandler;

    public Connectivity(Context newAppContext){
        this.connectionType = 0;
        this.appContext = newAppContext;
        this.deviceID = "";
        this.sharedPreferencesHandler = new SharedPreferencesHandler(newAppContext);
    }

    public Connectivity(int newConnectionType, Context newAppContext){
        this.connectionType = newConnectionType;
        this.appContext = newAppContext;
        this.deviceID = "";
        this.sharedPreferencesHandler = new SharedPreferencesHandler(newAppContext);
    }

    public void initialize(){
        this.generateDeviceUniqueID();
        this.checkConnectionStatus();
    }

    private void checkConnectionStatus(){
        ConnectivityManager connMgr = (ConnectivityManager) this.appContext.getSystemService(Context.CONNECTIVITY_SERVICE);

        if (connMgr != null) {
            NetworkCapabilities capabilities = connMgr.getNetworkCapabilities(connMgr.getActiveNetwork());

            if (capabilities != null) {
                boolean isWifi = capabilities.hasTransport(NetworkCapabilities.TRANSPORT_WIFI);
                boolean isMobile = capabilities.hasTransport(NetworkCapabilities.TRANSPORT_CELLULAR);
                boolean isEthernet = capabilities.hasTransport(NetworkCapabilities.TRANSPORT_ETHERNET);

                if (isWifi) {
                    // Device is connected to a Wi-Fi network
                    this.connectionType = 1;
                } else if (isMobile) {
                    // Device is connected to a mobile network
                    this.connectionType = 2;
                } else if (isEthernet){
                    this.connectionType = 3;
                } else {
                    // Device is not connected to any network
                    this.connectionType = 0;
                }
            }
        }

    }

    public void setConnectionType(int newConnectionType){
        this.connectionType = newConnectionType;
    }

    public int getConnectionType(){
        return this.connectionType;
    }

    public void setAppContext(Context newAppContext){
        this.appContext = newAppContext;
    }

    public Context getAppContext(){
        return this.appContext;
    }

    public String getDeviceID(){
        return this.deviceID;
    }

    public void setDeviceID(String newMacAddress){
        this.deviceID = newMacAddress;
    }

    private void generateDeviceUniqueID(){
        String deviceToken = this.sharedPreferencesHandler.getDeviceID();
        if (!deviceToken.equals("NONE")){
            //Device Has Already Token
            this.deviceID = deviceToken;
            return;
        }
        //Save New Token
        this.deviceID = UUID.randomUUID().toString();
        this.sharedPreferencesHandler.saveDeviceID(this.deviceID);
    }
}

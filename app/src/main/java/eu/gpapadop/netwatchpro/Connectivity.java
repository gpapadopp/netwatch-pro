package eu.gpapadop.netwatchpro;

import android.content.Context;
import java.util.UUID;

import eu.gpapadop.netwatchpro.handlers.SharedPreferencesHandler;

public class Connectivity {
    private Context appContext;
    private boolean hasError;
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
        this.hasError = false;
        this.deviceID = "";
        this.sharedPreferencesHandler = new SharedPreferencesHandler(newAppContext);
    }

    public Connectivity(int newConnectionType, Context newAppContext){
        this.connectionType = newConnectionType;
        this.appContext = newAppContext;
        this.hasError = false;
        this.deviceID = "";
        this.sharedPreferencesHandler = new SharedPreferencesHandler(newAppContext);
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

    public void setHasError(boolean newHasError){
        this.hasError = newHasError;
    }

    public boolean getHasError(){
        return this.hasError;
    }

    public String getDeviceID(){
        return this.deviceID;
    }

    public void setDeviceID(String newMacAddress){
        this.deviceID = newMacAddress;
    }

    public void initialize(){
        this.generateDeviceUniqueID();
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

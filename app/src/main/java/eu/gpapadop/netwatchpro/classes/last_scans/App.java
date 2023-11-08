package eu.gpapadop.netwatchpro.classes.last_scans;

import android.graphics.Bitmap;
import android.graphics.drawable.Drawable;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

public class App implements Serializable {
    private String name;
    private String packageName;
    private List<String> allPermissions;
    private boolean isMalware;
    private String launchIcon;

    public App(){
        this.name = "";
        this.packageName = "";
        this.allPermissions = new ArrayList<>();
        this.isMalware = false;
    }

    public App(String newName, String newPackageName, List<String> newAllPermissions, boolean newIsMalware, String newLaunchIcon){
        this.name = newName;
        this.packageName = newPackageName;
        this.allPermissions = newAllPermissions;
        this.isMalware = newIsMalware;
        this.launchIcon = newLaunchIcon;
    }

    public void setName(String newName){
        this.name = newName;
    }

    public String getName(){
        return this.name;
    }

    public void setPackageName(String newPackageName){
        this.packageName = newPackageName;
    }

    public String getPackageName(){
        return this.packageName;
    }

    public void setAllPermissions(List<String> newAllPermissions){
        this.allPermissions = newAllPermissions;
    }

    public List<String> getAllPermissions(){
        return this.allPermissions;
    }

    public void setIsMalware(boolean newIsMalware){
        this.isMalware = newIsMalware;
    }

    public boolean getIsMalware(){
        return this.isMalware;
    }

    public void setLaunchIcon(String newLaunchIcon){
        this.launchIcon = newLaunchIcon;
    }

    public String getLaunchIcon(){
        return this.launchIcon;
    }
}

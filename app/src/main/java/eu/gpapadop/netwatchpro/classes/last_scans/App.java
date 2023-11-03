package eu.gpapadop.netwatchpro.classes.last_scans;

import java.util.ArrayList;
import java.util.List;

public class App {
    private String name;
    private String packageName;
    private List<String> allPermissions;

    public App(){
        this.name = "";
        this.packageName = "";
        this.allPermissions = new ArrayList<>();
    }

    public App(String newName, String newPackageName, List<String> newAllPermissions){
        this.name = newName;
        this.packageName = newPackageName;
        this.allPermissions = newAllPermissions;
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
}

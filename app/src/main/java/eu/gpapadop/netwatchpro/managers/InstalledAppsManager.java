package eu.gpapadop.netwatchpro.managers;

import android.content.Context;
import android.content.pm.ApplicationInfo;
import android.content.pm.PackageManager;

import java.util.List;

public class InstalledAppsManager {
    private Context context;
    private List<ApplicationInfo> allInstalledApps;
    private PackageManager packageManager;

    public InstalledAppsManager(Context newContext){
        this.context = newContext;
        this.packageManager = newContext.getPackageManager();
        this.allInstalledApps = newContext.getPackageManager().getInstalledApplications(PackageManager.GET_META_DATA);
    }

    public List<ApplicationInfo> getAllInstalledApps(){
        return this.allInstalledApps;
    }

    public PackageManager getPackageManager(){
        return this.packageManager;
    }
}

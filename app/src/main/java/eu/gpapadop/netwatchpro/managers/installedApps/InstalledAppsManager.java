package eu.gpapadop.netwatchpro.managers.installedApps;

import android.content.Context;
import android.content.pm.ApplicationInfo;
import android.content.pm.PackageManager;

import java.util.List;

public class InstalledAppsManager {
    private Context appContext;
    private List<ApplicationInfo> allInstalledApps;
    private PackageManager packageManager;

    public InstalledAppsManager(Context newAppContext){
        this.appContext = newAppContext;
        this.packageManager = newAppContext.getPackageManager();
    }

    public void initialize(){
        this.getInstalledApps();
    }

    private void getInstalledApps(){
        this.allInstalledApps = this.packageManager.getInstalledApplications(PackageManager.GET_META_DATA);
    }

    protected List<ApplicationInfo> getAllInstalledApps(){
        return this.allInstalledApps;
    }

    protected PackageManager getPackageManager(){
        return this.packageManager;
    }
}

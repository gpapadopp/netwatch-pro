package eu.gpapadop.netwatchpro.managers.installedApps;

import android.content.Context;
import android.content.pm.ApplicationInfo;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class InstalledAppsHandler extends InstalledAppsManager {
    private List<String> allPackageNames;
    private List<String> allRealNames;
    private List<List<String>> allPermissions;

    public InstalledAppsHandler(Context newAppContext){
        super(newAppContext);
        super.initialize();
        this.allPackageNames = new ArrayList<>();
        this.allRealNames = new ArrayList<>();
        this.allPermissions = new ArrayList<>(new ArrayList<>());
    }

    public void initializeInstalledApps(){
        this.listAllBasicInfo();
        this.listAllPermissions();
    }

    private void listAllBasicInfo(){
        List<ApplicationInfo> allAppsInfo = super.getAllInstalledApps();
        PackageManager packageManager = super.getPackageManager();
        for (ApplicationInfo applicationInfo : allAppsInfo){
            final String appPackageName = applicationInfo.packageName.toString();
            this.allPackageNames.add(appPackageName);
            this.allRealNames.add(applicationInfo.loadLabel(packageManager).toString());
            this.allPermissions.add(new ArrayList<>());
        }
    }

    private void listAllPermissions(){
        PackageManager packageManager = super.getPackageManager();
        List<PackageInfo> allPackageInfo = packageManager.getInstalledPackages(PackageManager.GET_PERMISSIONS|PackageManager.GET_RECEIVERS|PackageManager.GET_SERVICES|PackageManager.GET_PROVIDERS);
        for (PackageInfo packageInfo : allPackageInfo){
            String[] packagePermissions = packageInfo.requestedPermissions;
            if (packagePermissions != null) {
                String packageName = packageInfo.packageName;
                int packageIndex = this.allPackageNames.indexOf(packageName);
                this.allPermissions.set(packageIndex, Arrays.asList(packagePermissions));
            }
        }
    }
}

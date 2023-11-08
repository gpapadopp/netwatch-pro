package eu.gpapadop.netwatchpro.classes.last_scans;

import java.io.Serializable;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

public class Scan implements Serializable {
    private UUID scanID;
    private LocalDateTime scanDateTime;
    private List<App> scannedApps;

    public Scan(){
        this.scanID = UUID.randomUUID();
        this.scanDateTime = LocalDateTime.now();
        this.scannedApps = new ArrayList<>();
    }

    public Scan(UUID newScanID, LocalDateTime newScanDateTime, List<App> newScannedApps){
        this.scanID = newScanID;
        this.scanDateTime = newScanDateTime;
        this.scannedApps = newScannedApps;
    }

    public UUID getScanID(){
        return this.scanID;
    }

    public long getLongScanID(){
        long mostSigBits = this.scanID.getMostSignificantBits();
        long leastSigBits = this.scanID.getLeastSignificantBits();
        return (mostSigBits << 32) | (leastSigBits & 0xFFFFFFFFL);
    }

    public LocalDateTime getScanDateTime(){
        return this.scanDateTime;
    }

    public List<App> getScannedApps(){
        return this.scannedApps;
    }

    public void setScannedApps(List<App> newScannedApps){
        this.scannedApps = newScannedApps;
    }

    public List<String> getAllAppNames(){
        List<String> appNames = new ArrayList<>();
        for (App singleApp : this.scannedApps){
            appNames.add(singleApp.getName());
        }
        return appNames;
    }

    public List<String> getAllPackageNames(){
        List<String> packageNames = new ArrayList<>();
        for (App singleApp : this.scannedApps){
            packageNames.add(singleApp.getPackageName());
        }
        return packageNames;
    }

    public List<String> getAllAppIcons(){
        List<String> appIcons = new ArrayList<>();
        for (App singleApp : this.scannedApps){
            appIcons.add(singleApp.getLaunchIcon());
        }
        return appIcons;
    }

    public List<Boolean> getAllAppsIsMalware(){
        List<Boolean> appMalwares = new ArrayList<>();
        for (App singleApp : this.scannedApps){
            appMalwares.add(singleApp.getIsMalware());
        }
        return appMalwares;
    }

    public List<Boolean> getAllAppsHasChecked(){
        List<Boolean> appHasChecked = new ArrayList<>();
        for (App singleApp : this.scannedApps){
            appHasChecked.add(true);
        }
        return appHasChecked;
    }
}

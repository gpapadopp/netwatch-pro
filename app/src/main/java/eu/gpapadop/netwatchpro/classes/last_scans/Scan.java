package eu.gpapadop.netwatchpro.classes.last_scans;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

public class Scan {
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
}

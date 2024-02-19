package eu.gpapadop.netwatchpro.classes.files_scan;

import java.io.Serializable;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

public class FilesScan implements Serializable {
    private UUID scanID;
    private LocalDateTime scanDateTime;
    private List<SingleFileScan> allScanFiles;

    public FilesScan(){
        this.scanID = UUID.randomUUID();
        this.scanDateTime = LocalDateTime.now();
        this.allScanFiles = new ArrayList<>();
    }

    public FilesScan(UUID newScanID, LocalDateTime newScanDateTime, List<SingleFileScan> newAllScanFiles){
        this.scanID = newScanID;
        this.scanDateTime = newScanDateTime;
        this.allScanFiles = newAllScanFiles;
    }

    public void setScanID(UUID newScanID){
        this.scanID = newScanID;
    }

    public UUID getScanID(){
        return this.scanID;
    }

    public long getLongScanID(){
        long mostSigBits = this.scanID.getMostSignificantBits();
        long leastSigBits = this.scanID.getLeastSignificantBits();
        return (mostSigBits << 32) | (leastSigBits & 0xFFFFFFFFL);
    }

    public void setScanDateTime(LocalDateTime newScanDateTime){
        this.scanDateTime = newScanDateTime;
    }

    public LocalDateTime getScanDateTime(){
        return this.scanDateTime;
    }

    public void setAllScanFiles(List<SingleFileScan> newAllScanFiles){
        this.allScanFiles = newAllScanFiles;
    }

    public List<SingleFileScan> getAllScanFiles(){
        return this.allScanFiles;
    }
}

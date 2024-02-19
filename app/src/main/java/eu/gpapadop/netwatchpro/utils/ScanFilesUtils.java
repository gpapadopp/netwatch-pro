package eu.gpapadop.netwatchpro.utils;

import android.util.Base64;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import eu.gpapadop.netwatchpro.classes.files_scan.FilesScan;
import eu.gpapadop.netwatchpro.handlers.SharedPreferencesHandler;

public class ScanFilesUtils {
    private SharedPreferencesHandler sharedPreferencesHandler;
    public ScanFilesUtils(SharedPreferencesHandler newSharedPreferencesHandler){
        this.sharedPreferencesHandler = newSharedPreferencesHandler;
    }

    public List<FilesScan> decodeLastScans(Set<String> allLastScans) {
        List<FilesScan> allDecodedLastScans = new ArrayList<>();

        for (String lastScan : allLastScans) {
            if (lastScan != null) {
                try {
                    byte[] serializedBytes = Base64.decode(lastScan, Base64.DEFAULT);
                    ByteArrayInputStream byteArrayInputStream = new ByteArrayInputStream(serializedBytes);
                    ObjectInputStream objectInputStream = new ObjectInputStream(byteArrayInputStream);

                    FilesScan singleScan = (FilesScan) objectInputStream.readObject();
                    allDecodedLastScans.add(singleScan);

                    objectInputStream.close();
                    byteArrayInputStream.close();
                } catch (ClassNotFoundException | IOException ignored) {}
            }
        }

        return allDecodedLastScans;
    }

    public void appendScanToSharedPrefs(FilesScan newScan){
        Set<String> filesScan = this.sharedPreferencesHandler.getFileScans();
        List<FilesScan> allFileScans = this.decodeLastScans(filesScan);

        allFileScans.add(newScan);

        Set<String> encodedScans = this.encodeLastScans(allFileScans);
        this.sharedPreferencesHandler.setFileScans(encodedScans);

        LocalDateTime nowDate = LocalDateTime.now();
        ZoneId zoneId = ZoneId.of("UTC");
        ZonedDateTime utcDateTime = ZonedDateTime.of(nowDate, zoneId);
        this.sharedPreferencesHandler.saveLastCheckDateTime(utcDateTime.toInstant().toEpochMilli());
    }

    public Set<String> encodeLastScans(List<FilesScan> allFileScans){
        Set<String> scansToSave = new HashSet<>();
        for (int i = 0; i<allFileScans.size(); i++){
            ByteArrayOutputStream byteArrayOutputStream = new ByteArrayOutputStream();
            try {
                ObjectOutputStream objectOutputStream = new ObjectOutputStream(byteArrayOutputStream);
                objectOutputStream.writeObject(allFileScans.get(i));
                objectOutputStream.close();
            } catch (IOException ignored) {}
            String serializedObject = Base64.encodeToString(byteArrayOutputStream.toByteArray(), Base64.DEFAULT);
            scansToSave.add(serializedObject);
        }
        return scansToSave;
    }
}

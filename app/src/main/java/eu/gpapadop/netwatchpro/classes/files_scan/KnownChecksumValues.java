package eu.gpapadop.netwatchpro.classes.files_scan;

import eu.gpapadop.netwatchpro.enums.file_checksum.ChecksumClassification;

public class KnownChecksumValues {
    private String md5Checksum;
    private ChecksumClassification md5Classification;

    public KnownChecksumValues(){
        this.md5Checksum = "";
        this.md5Classification = null;
    }

    public KnownChecksumValues(String newMd5Checksum, ChecksumClassification newMd5Classification){
        this.md5Checksum = newMd5Checksum;
        this.md5Classification = newMd5Classification;
    }

    public void setMd5Checksum(String newMd5Checksum) {
        this.md5Checksum = newMd5Checksum;
    }

    public String getMd5Checksum(){
        return this.md5Checksum;
    }

    public void setMd5Classification(ChecksumClassification newMd5Classification) {
        this.md5Classification = newMd5Classification;
    }

    public ChecksumClassification getMd5Classification(){
        return this.md5Classification;
    }
}

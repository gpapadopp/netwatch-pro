package eu.gpapadop.netwatchpro.classes.files_scan;

public class SingleFileScan {
    private String name;
    private String absoluteFilePath;
    private String md5Checksum;
    private boolean isMalware;

    public SingleFileScan(){
        this.name = "";
        this.absoluteFilePath = "";
        this.md5Checksum = "";
        this.isMalware = false;
    }

    public SingleFileScan(String newName, String newAbsoluteFilepath, String newMd5Checksum, boolean newIsMalware){
        this.name = newName;
        this.absoluteFilePath = newAbsoluteFilepath;
        this.md5Checksum = newMd5Checksum;
        this.isMalware = newIsMalware;
    }

    public void setName(String newName){
        this.name = newName;
    }

    public String getName(){
        return this.name;
    }

    public void setAbsoluteFilePath(String newAbsoluteFilePath){
        this.absoluteFilePath = newAbsoluteFilePath;
    }

    public String getAbsoluteFilePath(){
        return this.absoluteFilePath;
    }

    public void setMd5Checksum(String newMd5Checksum){
        this.md5Checksum = newMd5Checksum;
    }

    public String getMd5Checksum(){
        return this.md5Checksum;
    }

    public void setIsMalware(boolean newIsMalware){
        this.isMalware = newIsMalware;
    }

    public boolean getIsMalware(){
        return this.isMalware;
    }
}

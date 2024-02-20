package eu.gpapadop.netwatchpro.handlers;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;

public class FileGenerate {
    private String absolutePath;
    private String fileName;
    private String fileContent;

    public FileGenerate(){
        this.absolutePath = "";
        this.fileName = "";
        this.fileContent = "";
    }

    public FileGenerate(String newAbsolutePath, String newFileName, String newFileContent){
        this.absolutePath = newAbsolutePath;
        this.fileName = newFileName;
        this.fileContent = newFileContent;
    }

    public void setAbsolutePath(String newAbsolutePath){
        this.absolutePath = newAbsolutePath;
    }

    public String getAbsolutePath(){
        return this.absolutePath;
    }

    public void setFileName(String newFileName){
        this.fileName = newFileName;
    }

    public String getFileName(){
        return this.fileName;
    }

    public boolean generateFile(){
        File file = new File(this.absolutePath, this.fileName);
        try {
            file.createNewFile();
            FileWriter fileWriter = new FileWriter(file);
            BufferedWriter bufferedWriter = new BufferedWriter(fileWriter);

            bufferedWriter.write(this.fileContent);
            bufferedWriter.close();
            return true;
        } catch (IOException e){
            return false;
        }
    }
}

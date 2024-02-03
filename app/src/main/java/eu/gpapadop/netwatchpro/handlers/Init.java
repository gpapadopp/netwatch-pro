package eu.gpapadop.netwatchpro.handlers;

import android.content.Context;
import android.os.Environment;

import java.io.File;

public class Init {
    private Context context;
    private SharedPreferencesHandler sharedPreferencesHandler;

    public Init(Context newApplicationContext){
        this.context = newApplicationContext;
        this.sharedPreferencesHandler = new SharedPreferencesHandler(newApplicationContext);
    }

    public void run(){
        if (!this.sharedPreferencesHandler.getHasInit()){
            this.exportPathSave();
            this.saveHasInit();
        }
    }

    private void exportPathSave(){
        File documentsDirectory = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOCUMENTS);
        this.sharedPreferencesHandler.setExportPath(documentsDirectory.getAbsolutePath());
    }

    private void saveHasInit(){
        this.sharedPreferencesHandler.setHasInit(true);
    }
}

package eu.gpapadop.netwatchpro;

import androidx.appcompat.app.AppCompatActivity;

import android.content.SharedPreferences;
import android.os.Bundle;

import eu.gpapadop.netwatchpro.managers.installedApps.InstalledAppsHandler;
import eu.gpapadop.netwatchpro.managers.installedApps.InstalledAppsManager;

public class MainActivity extends AppCompatActivity {
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        Connectivity deviceConnectivity = new Connectivity(getApplicationContext());
        deviceConnectivity.initialize();
        String token = deviceConnectivity.getDeviceID();

        InstalledAppsHandler installedAppsManager = new InstalledAppsHandler(getApplicationContext());
        installedAppsManager.initializeInstalledApps();


//        if (!this.hasAcceptTerms()){
//            //User Has NOT Accepted Terms
//            Intent intent = new Intent(getApplicationContext(), InitialTermsScreen.class);
//            startActivity(intent);
//            finish();
//        }

    }

    private boolean hasAcceptTerms(){
        SharedPreferences sharedPrefs = getSharedPreferences("NetWatchProSharedPrefs", MODE_PRIVATE);
        return sharedPrefs.getBoolean("accepted_terms", false);
    }
}
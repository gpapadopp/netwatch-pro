package eu.gpapadop.netwatchpro;

import androidx.appcompat.app.AppCompatActivity;

import android.content.SharedPreferences;
import android.os.Bundle;
import android.util.Log;

public class MainActivity extends AppCompatActivity {
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        Connectivity deviceConnectivity = new Connectivity(getApplicationContext());
        deviceConnectivity.initialize();
        String token = deviceConnectivity.getDeviceID();
        Log.d("george", token);


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
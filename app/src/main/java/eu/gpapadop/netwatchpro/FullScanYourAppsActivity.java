package eu.gpapadop.netwatchpro;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.view.Window;
import android.widget.ImageView;

import java.util.List;

import eu.gpapadop.netwatchpro.managers.InstalledAppsManager;

public class FullScanYourAppsActivity extends AppCompatActivity {
    int totalCheckedApps = 0;
    private InstalledAppsManager installedAppsManager;
    private List<String> allAppNames;
    private List<String> allPackageNames;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_full_scan_your_apps);

        this.handleStatusBarColor();
        this.handleBackButtonTap();
    }

    private void handleStatusBarColor(){
        Window window = this.getWindow();
        window.setStatusBarColor(this.getResources().getColor(R.color.main_blue));
    }

    private void handleBackButtonTap(){
        ImageView backButtonImageView = (ImageView) findViewById(R.id.custom_toolbar_settings_icon);
        backButtonImageView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent mainActivityIntent = new Intent(getApplicationContext(), MainActivity.class);
                startActivity(mainActivityIntent);
                finish();
            }
        });
    }
}
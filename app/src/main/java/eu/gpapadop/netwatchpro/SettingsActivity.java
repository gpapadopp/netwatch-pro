package eu.gpapadop.netwatchpro;

import androidx.appcompat.app.AppCompatActivity;

import android.app.AlertDialog;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.TextView;

import eu.gpapadop.netwatchpro.handlers.SharedPreferencesHandler;

public class SettingsActivity extends AppCompatActivity {
    private SharedPreferencesHandler sharedPreferencesHandler;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_settings);
        this.sharedPreferencesHandler = new SharedPreferencesHandler(getApplicationContext());
        this.handleBackIconTap();
        //Recursive Future Scan Section
        this.handleRecursiveFutureScanText();
        this.handleRecursiveFutureScanRowTap();
    }

    private void handleBackIconTap(){
        ImageView backIconTap = (ImageView) findViewById(R.id.custom_toolbar_settings_icon);
        backIconTap.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent mainActivityIntent = new Intent(getApplicationContext(), MainActivity.class);
                startActivity(mainActivityIntent);
                finish();
            }
        });
    }

    private void handleRecursiveFutureScanText(){
        TextView recursiveFutureScanSubtext = (TextView) findViewById(R.id.activity_settings_recursive_future_scans_subtitle_text_view);
        boolean recursiveEnabled = this.sharedPreferencesHandler.getRecursiveEnabled();
        if (!recursiveEnabled){
            recursiveFutureScanSubtext.setText(getString(R.string.inactive));
            return;
        }
        int recursiveFrequency = this.sharedPreferencesHandler.getRecursiveFrequency();
        if (recursiveFrequency == 1){
            recursiveFutureScanSubtext.setText(getString(R.string.every_week));
            return;
        }
        if (recursiveFrequency == 2){
            recursiveFutureScanSubtext.setText(getString(R.string.every_two_weeks));
            return;
        }
        if (recursiveFrequency == 4){
            recursiveFutureScanSubtext.setText(getString(R.string.every_four_weeks));
        }
    }

    private void handleRecursiveFutureScanRowTap(){
        FrameLayout recursiveFutureScanFrameLayout = (FrameLayout) findViewById(R.id.activity_settings_recursive_scans_container);
        int[] selectedFrequency = {this.sharedPreferencesHandler.getRecursiveFrequency()};
        recursiveFutureScanFrameLayout.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String[] recursiveChoices = {
                        getString(R.string.disable),
                        getString(R.string.every_week),
                        getString(R.string.every_two_weeks),
                        getString(R.string.every_four_weeks)
                };
                AlertDialog.Builder builder = new AlertDialog.Builder(SettingsActivity.this);
                builder.setTitle(getString(R.string.choose_recursive_future_scans_frequency))
                        .setPositiveButton(getString(R.string.save), (dialog, which) -> {
                            if (selectedFrequency[0] == 0){
                                sharedPreferencesHandler.setRecursiveEnabled(false);
                                sharedPreferencesHandler.setRecursiveFrequency(0);
                            } else {
                                sharedPreferencesHandler.setRecursiveEnabled(true);
                                sharedPreferencesHandler.setRecursiveFrequency(selectedFrequency[0]);
                            }
                            handleRecursiveFutureScanText();
                            dialog.dismiss();
                        })
                        .setNegativeButton(getString(R.string.cancel), (dialog, which) -> {
                            dialog.dismiss();
                        })
                        .setSingleChoiceItems(recursiveChoices, selectedFrequency[0], (dialog, which) -> {
                            selectedFrequency[0] = which;
                        });
                AlertDialog dialog = builder.create();
                dialog.show();
            }
        });
    }
}
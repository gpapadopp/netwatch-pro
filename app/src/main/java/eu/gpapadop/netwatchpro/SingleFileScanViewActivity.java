package eu.gpapadop.netwatchpro;

import android.content.Intent;
import android.graphics.RenderEffect;
import android.graphics.Shader;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.ImageView;
import android.widget.ListAdapter;
import android.widget.ListView;
import android.widget.ScrollView;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import java.util.UUID;

import eu.gpapadop.netwatchpro.adapters.listviews.SingleFileScannedAppsAdapter;
import eu.gpapadop.netwatchpro.classes.files_scan.FilesScan;
import eu.gpapadop.netwatchpro.classes.files_scan.SingleFileScan;
import eu.gpapadop.netwatchpro.handlers.SharedPreferencesHandler;
import eu.gpapadop.netwatchpro.utils.DateTimeUtils;
import eu.gpapadop.netwatchpro.utils.ScanFilesUtils;

public class SingleFileScanViewActivity extends AppCompatActivity {
    private String scanUUID;
    private SharedPreferencesHandler sharedPreferencesHandler;
    private ScanFilesUtils scanFilesUtils;
    private DateTimeUtils dateTimeUtils;
    private FilesScan scanToView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_single_file_scan_view);

        this.sharedPreferencesHandler = new SharedPreferencesHandler(getApplicationContext());
        this.scanFilesUtils = new ScanFilesUtils(this.sharedPreferencesHandler);
        this.dateTimeUtils = new DateTimeUtils(getApplicationContext());

        Bundle extras = getIntent().getExtras();
        if (extras != null) {
            this.scanUUID = extras.getString("scan_unique_id");
        }

        this.handleBackButtonTap();
        this.getScanFromSharedPrefs();
    }

    private void handleBackButtonTap(){
        ImageView backButtonImageView = (ImageView) findViewById(R.id.single_file_scan_view_activity_toolbar_back_button);
        backButtonImageView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent mainActivityIntent = new Intent(getApplicationContext(), MainActivity.class);
                startActivity(mainActivityIntent);
                finish();
            }
        });
    }

    private void getScanFromSharedPrefs(){
        Set<String> lastScans = this.sharedPreferencesHandler.getFileScans();
        List<FilesScan> allScans = this.scanFilesUtils.decodeLastScans(lastScans);
        UUID parsedUUID = UUID.fromString(this.scanUUID);

        boolean found = false;
        for (FilesScan singleScan : allScans){
            if (singleScan.getScanID().equals(parsedUUID)){
                this.scanToView = singleScan;
                found = true;
                break;
            }
        }
        if (!found){
            Intent mainActivityIntent = new Intent(getApplicationContext(), MainActivity.class);
            startActivity(mainActivityIntent);
            finish();
            return;
        }
        this.handleScannedDateTextView();
        this.handleScannedAppsFullNumberTextView();
        this.handleIssuesFoundNumberTextView();
        this.handleOverallSituationTextView();
        this.handleBackgroundImageView();
        this.handleScannedAppsListView();
    }

    private void handleScannedDateTextView(){
        TextView scannedDateTextView = (TextView) findViewById(R.id.activity_single_file_scan_view_scanned_date_full_date_text_view);
        scannedDateTextView.setText(this.dateTimeUtils.formatDateTime(this.scanToView.getScanDateTime()));
    }

    private void handleScannedAppsFullNumberTextView(){
        TextView scannedAppsNumberTextView = (TextView) findViewById(R.id.activity_single_file_scan_view_scanned_apps_full_number_text_view);
        scannedAppsNumberTextView.setText(String.valueOf(this.scanToView.getAllScanFiles().size()));
    }

    private void handleIssuesFoundNumberTextView(){
        TextView issuesFoundNumberTextView = (TextView) findViewById(R.id.activity_single_file_scan_view_issues_found_full_number_text_view);
        int issuesCounter = 0;
        for (SingleFileScan singleScannedApp : this.scanToView.getAllScanFiles()){
            if (singleScannedApp.getIsMalware()){
                issuesCounter += 1;
            }
        }
        issuesFoundNumberTextView.setText(String.valueOf(issuesCounter));
    }

    private void handleOverallSituationTextView(){
        TextView overallSituationTextView = (TextView) findViewById(R.id.activity_single_file_scan_view_overall_situation_content_text_view);
        int issuesCounter = 0;
        for (SingleFileScan singleScannedApp : this.scanToView.getAllScanFiles()){
            if (singleScannedApp.getIsMalware()){
                issuesCounter += 1;
            }
        }
        if (issuesCounter == 0){
            overallSituationTextView.setText(getString(R.string.your_device_is_safe));
        } else {
            overallSituationTextView.setText(getString(R.string.your_device_needs_attention));
        }
    }

    private void handleBackgroundImageView(){
        ImageView backgroundImageView = (ImageView) findViewById(R.id.single_file_scan_view_activity_background_image_view);

        int issuesCounter = 0;
        for (SingleFileScan singleScannedApp : this.scanToView.getAllScanFiles()){
            if (singleScannedApp.getIsMalware()){
                issuesCounter += 1;
            }
        }
        if (issuesCounter == 0){
            backgroundImageView.setImageDrawable(getDrawable(R.drawable.activity_single_scan_view_excellent_badge));
            backgroundImageView.setAlpha(0.2F);
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
                backgroundImageView.setRenderEffect(RenderEffect.createBlurEffect(30, 30, Shader.TileMode.CLAMP));
            }
        } else {
            backgroundImageView.setImageDrawable(getDrawable(R.drawable.activity_single_scan_view_danger_badge));
            backgroundImageView.setAlpha(0.35F);
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
                backgroundImageView.setRenderEffect(RenderEffect.createBlurEffect(25, 25, Shader.TileMode.CLAMP));
            }
        }

        Animation animation = AnimationUtils.loadAnimation(this, R.anim.blink_anim);
        backgroundImageView.startAnimation(animation);

        new Handler(Looper.getMainLooper()).postDelayed(new Runnable() {
            @Override
            public void run() {
                backgroundImageView.clearAnimation();
            }
        }, 4000);
    }

    private void handleScannedAppsListView(){
        ListView scannedAppsListView = (ListView) findViewById(R.id.activity_single_file_scan_view_scanned_apps_list_view);
        List<String> allFileNames = new ArrayList<>();
        List<String> allFilePaths = new ArrayList<>();
        for (SingleFileScan singleFileScan : this.scanToView.getAllScanFiles()){
            allFileNames.add(singleFileScan.getName());
            allFilePaths.add(singleFileScan.getAbsoluteFilePath());
        }
        SingleFileScannedAppsAdapter singleScannedAppsAdapter = new SingleFileScannedAppsAdapter(getApplicationContext(), allFileNames, allFilePaths);
        scannedAppsListView.setAdapter(singleScannedAppsAdapter);

        setListViewHeightBasedOnChildren(scannedAppsListView);
        ScrollView scrollView = (ScrollView) findViewById(R.id.activity_single_file_scan_view_main_scroll_view);
        scrollView.setOnTouchListener(new View.OnTouchListener() {
            @Override
            public boolean onTouch(View v, MotionEvent event) {
                scannedAppsListView.getParent().requestDisallowInterceptTouchEvent(false);
                return false;
            }
        });
    }

    private void setListViewHeightBasedOnChildren(ListView listView) {
        ListAdapter listAdapter = listView.getAdapter();
        if (listAdapter == null) {
            return;
        }

        int totalHeight = 0;
        int desiredWidth = View.MeasureSpec.makeMeasureSpec(listView.getWidth(), View.MeasureSpec.UNSPECIFIED);

        for (int i = 0; i < listAdapter.getCount(); i++) {
            View listItem = listAdapter.getView(i, null, listView);
            listItem.measure(desiredWidth, View.MeasureSpec.UNSPECIFIED);
            totalHeight += listItem.getMeasuredHeight();
        }

        ViewGroup.LayoutParams params = listView.getLayoutParams();
        params.height = totalHeight + (listView.getDividerHeight() * (listAdapter.getCount() - 1));
        listView.setLayoutParams(params);
        listView.requestLayout();
    }
}
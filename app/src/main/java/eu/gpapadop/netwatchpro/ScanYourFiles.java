package eu.gpapadop.netwatchpro;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import android.app.Dialog;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Color;
import android.graphics.ColorFilter;
import android.graphics.PorterDuff;
import android.graphics.PorterDuffColorFilter;
import android.os.Bundle;
import android.os.Environment;
import android.os.Handler;
import android.util.DisplayMetrics;
import android.util.TypedValue;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.ProgressBar;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;
import android.Manifest;

import com.airbnb.lottie.LottieAnimationView;
import com.airbnb.lottie.LottieProperty;
import com.airbnb.lottie.model.KeyPath;
import com.airbnb.lottie.value.LottieFrameInfo;
import com.airbnb.lottie.value.SimpleLottieValueCallback;
import com.opencsv.CSVReader;
import com.opencsv.exceptions.CsvException;

import java.io.File;
import java.io.IOException;
import java.io.InputStreamReader;
import java.security.NoSuchAlgorithmException;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.concurrent.TimeUnit;

import eu.gpapadop.netwatchpro.adapters.listviews.MaliciousFilesAdapter;
import eu.gpapadop.netwatchpro.adapters.listviews.MaliciousFilesEmptyAdapter;
import eu.gpapadop.netwatchpro.classes.files_scan.FilesScan;
import eu.gpapadop.netwatchpro.classes.files_scan.KnownChecksumValues;
import eu.gpapadop.netwatchpro.classes.files_scan.SingleFileScan;
import eu.gpapadop.netwatchpro.enums.file_checksum.ChecksumClassification;
import eu.gpapadop.netwatchpro.handlers.SharedPreferencesHandler;
import eu.gpapadop.netwatchpro.utils.FileChecksum;
import eu.gpapadop.netwatchpro.utils.ScanFilesUtils;

public class ScanYourFiles extends AppCompatActivity {
    private List<KnownChecksumValues> knownFilesChecksums;
    private List<File> allUserFiles;
    private static final int READ_EXTERNAL_STORAGE_PERMISSION_REQUEST_CODE = 123;
    private List<File> allMaliciousFiles;
    private Handler handler;
    private int timeElapsed = 0;
    private int filesCheckedCounter = 0;
    private SharedPreferencesHandler sharedPreferencesHandler;
    private ScanFilesUtils scanFilesUtils;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_scan_your_files);
        //Initialize Lists
        this.allUserFiles = new ArrayList<>();
        this.allMaliciousFiles = new ArrayList<>();
        this.sharedPreferencesHandler = new SharedPreferencesHandler(getApplicationContext());
        this.scanFilesUtils = new ScanFilesUtils(this.sharedPreferencesHandler);

        this.handleStatusBarColor();
        this.handleBackButtonTap();
        //Lottie Animation
        this.changeLottieColor();

        //Loading Progress Bar
        this.initializeProgressBarValue();

        //Current Scanning File
        this.initializeCurrentScanningFileTextView();

        //Scanned Files Counter
        this.initializeScannedFilesCounterTextView();

        //Total Scan Time
        this.initializeScanTimeTextView();

        //Malicious Files List View
        this.initializeMaliciousFilesListView();

        //Initialize Clock
        this.initializeTimeElapsedTimer();

        this.initializeFileChecksums();

        this.initializeGetAllUserFiles();
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);

        if (requestCode == READ_EXTERNAL_STORAGE_PERMISSION_REQUEST_CODE) {
            if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                this.getAllUserFiles();
            } else {
                Toast.makeText(this, "Permission denied. Cannot list files.", Toast.LENGTH_SHORT).show();
            }
        }
    }

    private void listFiles(File directoryToSearch) {
        File[] files = directoryToSearch.listFiles();

        if (files != null && files.length > 0) {
            for (File file : files) {
                if (file != null) {
                    if (file.isDirectory()){
                        listAllFiles(file);
                    } else if (file.isFile()){
                        this.allUserFiles.add(file);
                    }
                }
            }
        }
    }

    private void listAllFiles(File directory) {
        if (directory.isDirectory()) {
            File[] files = directory.listFiles();

            if (files != null) {
                for (File file : files) {
                    if (file.isDirectory()) {
                        listAllFiles(file);
                    } else {
                        this.allUserFiles.add(file);
                    }
                }
            }
        }
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

    private void changeLottieColor(){
        LottieAnimationView lottieAnimationView = (LottieAnimationView) findViewById(R.id.animationView);
        lottieAnimationView.addValueCallback(
                new KeyPath("**"),
                LottieProperty.COLOR_FILTER,
                new SimpleLottieValueCallback<ColorFilter>() {
                    @Override
                    public ColorFilter getValue(LottieFrameInfo<ColorFilter> frameInfo) {
                        return new PorterDuffColorFilter(Color.WHITE, PorterDuff.Mode.SRC_ATOP);
                    }
                }
        );
    }

    private void initializeFileChecksums(){
        List<KnownChecksumValues> allChecksums = new ArrayList<>();
        int resourceId = this.getResources().getIdentifier("files_checksum", "raw", this.getPackageName());
        try (CSVReader reader = new CSVReader(new InputStreamReader(this.getResources().openRawResource(resourceId)))) {
            List<String[]> lines = reader.readAll();

            for (String[] line : lines) {
                String md5 = line[0];
                String category = line[1];

                for (ChecksumClassification singleChecksum : ChecksumClassification.values()){
                    if (singleChecksum.getCategoryName().equals(category)){
                        KnownChecksumValues newKnownValue = new KnownChecksumValues(md5, singleChecksum);
                        allChecksums.add(newKnownValue);
                    }
                }
            }

            this.knownFilesChecksums = allChecksums;
        } catch (IOException | CsvException ignored) {}
    }

    private void initializeGetAllUserFiles(){
        if (checkSelfPermission(Manifest.permission.READ_EXTERNAL_STORAGE) == PackageManager.PERMISSION_GRANTED) {
            this.getAllUserFiles();
        } else {
            requestPermissions(
                    new String[]{Manifest.permission.READ_EXTERNAL_STORAGE},
                    READ_EXTERNAL_STORAGE_PERMISSION_REQUEST_CODE
            );
        }
    }

    private void getAllUserFiles(){
        listFiles(Environment.getExternalStorageDirectory());
        listFiles(Environment.getRootDirectory());
        listFiles(Environment.getDataDirectory());

        this.runMaliciousFilesCheck();
    }

    private void initializeProgressBarValue(){
        ProgressBar progressBar = (ProgressBar) findViewById(R.id.activity_scan_your_files_simple_progress_bar);
        progressBar.setProgress(0);

        TextView progressTextView = (TextView) findViewById(R.id.activity_scan_your_files_progress_bar_percent_text_view);
        progressTextView.setText("0%");
    }

    private void initializeCurrentScanningFileTextView(){
        TextView currentScanningFileTextView = (TextView) findViewById(R.id.activity_scan_your_files_current_scanning_file_text_view);
        currentScanningFileTextView.setText(getString(R.string.loading_files) + "...");
    }

    private void initializeScannedFilesCounterTextView(){
        TextView currentScannedFilesCounterTextView = (TextView) findViewById(R.id.activity_scan_your_files_scanned_files_counter_text_view);
        currentScannedFilesCounterTextView.setText("0");
    }

    private void initializeScanTimeTextView(){
        TextView scannedTimeTextView = (TextView) findViewById(R.id.activity_scan_your_files_time_elapsed_timer_text_view);
        scannedTimeTextView.setText("00:00:00");
    }

    private void initializeMaliciousFilesListView(){
        ListView maliciousFilesListView = (ListView) findViewById(R.id.scanned_files_container_card_view_last_scans_list_view);
        RelativeLayout scannedFilesContainer = (RelativeLayout) findViewById(R.id.scanned_files_container_card_view_relative_layout);
        ViewGroup.LayoutParams layoutParams = scannedFilesContainer.getLayoutParams();
        DisplayMetrics displayMetrics = getResources().getDisplayMetrics();

        if (this.allMaliciousFiles.isEmpty()){
            maliciousFilesListView.setAdapter(new MaliciousFilesEmptyAdapter(getApplicationContext()));
            int newHeightInDp = 130;
            layoutParams.height = (int) TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, newHeightInDp, displayMetrics);
            scannedFilesContainer.setLayoutParams(layoutParams);
        } else {
            maliciousFilesListView.setAdapter(new MaliciousFilesAdapter(getApplicationContext(), this.allMaliciousFiles));
            int newHeightInDp = this.allMaliciousFiles.size() * 100;
            layoutParams.height = (int) TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, newHeightInDp, displayMetrics);
            scannedFilesContainer.setLayoutParams(layoutParams);
        }
    }

    private void initializeTimeElapsedTimer(){
        TextView timeElapsedTextView = (TextView) findViewById(R.id.activity_scan_your_files_time_elapsed_timer_text_view);
        this.handler = new Handler();
        Runnable updateTimeRunnable = new Runnable() {
            @Override
            public void run() {
                timeElapsed++;
                String currentTime = String.format(Locale.getDefault(), "%02d:%02d:%02d", timeElapsed / 3600, (timeElapsed % 3600) / 60, timeElapsed % 60);
                timeElapsedTextView.setText(currentTime);
                initializeMaliciousFilesListView();
                handler.postDelayed(this, 1000);
            }
        };
        handler.post(updateTimeRunnable);
    }

    private void calculatePercentage(){
        TextView percentageTextView = (TextView) findViewById(R.id.activity_scan_your_files_progress_bar_percent_text_view);
        ProgressBar percentageProgressBar = (ProgressBar) findViewById(R.id.activity_scan_your_files_simple_progress_bar);
        float calculatedPercentage = (float) Math.round((this.filesCheckedCounter / (float) this.allUserFiles.size()) * 100);
        int calculatedPercentageInt = (int) Math.round((this.filesCheckedCounter / (float) this.allUserFiles.size()) * 100);

        percentageTextView.setText(String.valueOf(calculatedPercentage) + "%");
        percentageProgressBar.setProgress(calculatedPercentageInt);
    }

    private void increaseScannedFilesCounterTextView(){
        TextView currentScannedFilesCounterTextView = (TextView) findViewById(R.id.activity_scan_your_files_scanned_files_counter_text_view);
        currentScannedFilesCounterTextView.setText(String.valueOf(this.filesCheckedCounter));
    }

    private void viewCurrentTestFileTextView(int indexToView){
        TextView currentTestFileTextView = (TextView) findViewById(R.id.activity_scan_your_files_current_scanning_file_text_view);
        if (indexToView == (this.allUserFiles.size() - 1)){
            //Scan Completed
            currentTestFileTextView.setText(getString(R.string.scan_completed_successfully));
            handler.removeCallbacksAndMessages(null);
            this.saveScan();
            this.scanCompleteDialog();

        } else {
            currentTestFileTextView.setText(this.allUserFiles.get(indexToView).getAbsolutePath());
        }
    }

    private void scanCompleteDialog(){
        Dialog dialog = new Dialog(this);
        dialog.setContentView(R.layout.dialog_scan_completed_successfully);
        dialog.getWindow().setLayout(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT);
        dialog.setCancelable(false);
        dialog.getWindow().getAttributes().windowAnimations = R.style.animation;

        TextView titleTextView = (TextView) dialog.findViewById(R.id.dialog_scan_completed_successfully_title);
        titleTextView.setText(getString(R.string.scan_your_files));

        TextView mainTextView = (TextView) dialog.findViewById(R.id.dialog_scan_completed_successfully_job_hint_textview);
        mainTextView.setText(getString(R.string.scan_completed_successfully) + ".");

        Button okDialogButton = (Button) dialog.findViewById(R.id.dialog_scan_completed_successfully_job_save_button);
        okDialogButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                dialog.dismiss();
            }
        });

        dialog.show();
    }

    private void saveScan(){
        FilesScan filesScan = new FilesScan();
        List<SingleFileScan> allScannedFiles = new ArrayList<>();
        for (File singleUserFiles : this.allUserFiles){
            String fileMd5Checksum = "";
            try {
                fileMd5Checksum = FileChecksum.calculateMD5(singleUserFiles.getAbsolutePath());
            } catch (NoSuchAlgorithmException | IOException ignored){}
            boolean isMalware = false;
            if (this.allMaliciousFiles.contains(singleUserFiles)){
                isMalware = true;
            }

            SingleFileScan singleFileScan = new SingleFileScan(singleUserFiles.getName(), singleUserFiles.getAbsolutePath(), fileMd5Checksum, isMalware);
            allScannedFiles.add(singleFileScan);
        }

        filesScan.setAllScanFiles(allScannedFiles);
        this.scanFilesUtils.appendScanToSharedPrefs(filesScan);
    }

    private void runMaliciousFilesCheck(){
        Thread thread = new Thread(new Runnable() {
            int i = 0;
            @Override
            public void run() {
                for (i = 0; i<allUserFiles.size(); i++){
                    try {
                        String fileChecksum = FileChecksum.calculateMD5(allUserFiles.get(i).getAbsolutePath());
                        for (KnownChecksumValues singleKnownChecksum : knownFilesChecksums){
                            if (singleKnownChecksum.getMd5Checksum().equals(fileChecksum)){
                                allMaliciousFiles.add(allUserFiles.get(i));
                            }
                        }
                    } catch (NoSuchAlgorithmException | IOException ignored){}
                    runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            filesCheckedCounter++;
                            initializeMaliciousFilesListView();
                            calculatePercentage();
                            increaseScannedFilesCounterTextView();
                            viewCurrentTestFileTextView(i);
                        }
                    });
                    try {
                        TimeUnit.MILLISECONDS.sleep(150);
                    } catch (Exception ignored){}
                }
            }
        });
        thread.start();
    }
}
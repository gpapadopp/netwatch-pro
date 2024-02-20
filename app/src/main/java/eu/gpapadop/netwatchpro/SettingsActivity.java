package eu.gpapadop.netwatchpro;

import androidx.activity.result.ActivityResult;
import androidx.activity.result.ActivityResultCallback;
import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.appcompat.app.AppCompatActivity;
import androidx.documentfile.provider.DocumentFile;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.Dialog;
import android.content.Intent;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.widget.Button;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.TextView;

import com.google.android.material.bottomsheet.BottomSheetDialog;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.HashSet;
import java.util.List;
import java.util.Set;

import eu.gpapadop.netwatchpro.classes.files_scan.FilesScan;
import eu.gpapadop.netwatchpro.classes.last_scans.Scan;
import eu.gpapadop.netwatchpro.handlers.FileGenerate;
import eu.gpapadop.netwatchpro.handlers.SharedPreferencesHandler;
import eu.gpapadop.netwatchpro.utils.PathUtils;
import eu.gpapadop.netwatchpro.utils.ScanFilesUtils;
import eu.gpapadop.netwatchpro.utils.ScanUtils;

public class SettingsActivity extends AppCompatActivity {
    private SharedPreferencesHandler sharedPreferencesHandler;
    private TextView exportPathTextView;
    private ActivityResultLauncher<Intent> folderActivityResultLauncher;
    private ScanUtils scanUtils;
    private ScanFilesUtils scanFilesUtils;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_settings);

        this.sharedPreferencesHandler = new SharedPreferencesHandler(getApplicationContext());
        this.scanUtils = new ScanUtils(this.sharedPreferencesHandler);
        this.scanFilesUtils = new ScanFilesUtils(this.sharedPreferencesHandler);

        this.handleBackIconTap();
        this.handleStatusBarColor();
        //Recursive Future Scan Section
        this.handleRecursiveFutureScanText();
        this.handleRecursiveFutureScanRowTap();

        //Export Path Section
        this.handleExportPathTextview();
        this.initializeFolderActivityResult();
        this.handleExportPathRowClick();

        //Delete All Data Section
        this.handleDeleteAllDataRowClick();

        //Export Data Section
        this.exportAllDataRowClick();

        //Terms of Use Section
        this.handleTermsOfUseRowTap();
        //Privacy Policy Section
        this.handlePrivacyPolicyRowTap();
        //About Us Section
        this.handleAboutUsRowTap();
        //Version Number Section
        this.handleVersionNumberText();
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

    private void handleStatusBarColor(){
        Window window = this.getWindow();
        window.setStatusBarColor(this.getResources().getColor(R.color.super_light_gray));
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

    private void handleExportPathTextview(){
        String path = this.sharedPreferencesHandler.getExportPath();
        this.exportPathTextView = (TextView) findViewById(R.id.activity_settings_export_path_subtext);
        this.exportPathTextView.setText(path);
    }

    private void handleExportPathRowClick(){
        FrameLayout exportPathRowFrameLayout = (FrameLayout) findViewById(R.id.activity_settings_export_path_container);
        exportPathRowFrameLayout.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(Intent.ACTION_OPEN_DOCUMENT_TREE);
                intent.addCategory(Intent.CATEGORY_DEFAULT);
                intent.putExtra(Intent.EXTRA_TITLE, "Choose directory");
                folderActivityResultLauncher.launch(intent);
            }
        });
    }

    private void handleDeleteAllDataRowClick(){
        FrameLayout deleteAllDataFrameLayout = (FrameLayout) findViewById(R.id.activity_settings_delete_all_data_container);
        deleteAllDataFrameLayout.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                displayDeleteAllDataDialog();
            }
        });
    }

    private void displayDeleteAllDataDialog(){
        Dialog dialog = new Dialog(this);
        dialog.setContentView(R.layout.dialog_delete_all_data);
        dialog.getWindow().setLayout(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT);
        dialog.setCancelable(false);
        dialog.getWindow().getAttributes().windowAnimations = R.style.animation;

        Button cancelButton = (Button) dialog.findViewById(R.id.dialog_delete_all_data_job_cancel_button);
        Button proceedButton = (Button) dialog.findViewById(R.id.dialog_delete_all_data_job_save_button);

        cancelButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                dialog.dismiss();
            }
        });

        proceedButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                deleteAllApplicationDataFunctionality();
                dialog.dismiss();
            }
        });

        dialog.show();
    }

    private void deleteAllApplicationDataFunctionality(){
        this.sharedPreferencesHandler.setFileScans(new HashSet<>());
        this.sharedPreferencesHandler.setLatestScans(new HashSet<>());
    }

    private void exportAllDataRowClick(){
        FrameLayout exportAllDataRowFrameLayout = (FrameLayout) findViewById(R.id.activity_settings_export_data_container);
        exportAllDataRowFrameLayout.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                displayExportAllDataDialog();
            }
        });
    }

    private void displayExportAllDataDialog(){
        Dialog dialog = new Dialog(this);
        dialog.setContentView(R.layout.dialog_export_all_data);
        dialog.getWindow().setLayout(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT);
        dialog.setCancelable(false);
        dialog.getWindow().getAttributes().windowAnimations = R.style.animation;

        Button cancelButton = (Button) dialog.findViewById(R.id.dialog_export_all_data_job_cancel_button);
        Button proceedButton = (Button) dialog.findViewById(R.id.dialog_export_all_data_job_save_button);

        cancelButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                dialog.dismiss();
            }
        });

        proceedButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                exportAllDataFunctionality();
                dialog.dismiss();
            }
        });

        dialog.show();
    }

    private void exportAllDataFunctionality(){
        String exportFilePath = this.sharedPreferencesHandler.getExportPath();
        String appScansFilePath = "app_scans.json";
        String fileScansFilePath = "file_scans.json";

        String appScansJsonContent = this.getAppsJsonFileContents();
        String fileScansJsonContent = this.getFileJsonFileContents();

        FileGenerate appScansFileGenerate = new FileGenerate(exportFilePath, appScansFilePath, appScansJsonContent);
        appScansFileGenerate.generateFile();

        FileGenerate appFileScansFileGenerate = new FileGenerate(exportFilePath, fileScansFilePath, fileScansJsonContent);
        appFileScansFileGenerate.generateFile();

        this.exportAllDataSuccessDialog();
    }

    private String getAppsJsonFileContents(){
        Set<String> allAppScans = this.sharedPreferencesHandler.getLatestScans();
        List<Scan> scanList = this.scanUtils.decodeLastScans(allAppScans);

        JSONArray scanJson = new JSONArray();
        for (int i = 0; i<scanList.size(); i++){
            JSONObject scanObj = new JSONObject();
            try {
                scanObj.put("scan_uuid", scanList.get(i).getScanID());
                scanObj.put("scan_date_time", scanList.get(i).getScanDateTime());
                scanObj.put("is_full_scan", scanList.get(i).getIsFullScan());

                //Append Scan Items to Scan
                JSONArray scanItemsJson = new JSONArray();
                for (int j = 0; j<scanList.get(i).getScannedApps().size(); j++){
                    JSONObject scanItemObj = new JSONObject();

                    scanItemObj.put("name", scanList.get(i).getScannedApps().get(j).getName());
                    scanItemObj.put("package_name", scanList.get(i).getScannedApps().get(j).getPackageName());
                    scanItemObj.put("is_malware", scanList.get(i).getScannedApps().get(j).getIsMalware());
                    scanItemObj.put("launch_icon", scanList.get(i).getScannedApps().get(j).getLaunchIcon());

                    //Append Scan Item Permissions to Scan Item
                    JSONArray scanItemPermissions = new JSONArray();
                    for (int k = 0; k<scanList.get(i).getScannedApps().get(j).getAllPermissions().size(); k++){
                        JSONObject scanItemPermissionObj = new JSONObject();
                        scanItemPermissionObj.put("name", scanList.get(i).getScannedApps().get(j).getAllPermissions().get(k));

                        scanItemPermissions.put(k, scanItemPermissionObj);
                    }
                    scanItemObj.put("package_permissions", scanItemPermissions);
                    scanItemsJson.put(j, scanItemObj);
                }
                scanObj.put("scan_items", scanItemsJson);
                scanJson.put(i, scanObj);
            } catch (JSONException ignored){}
        }
        return scanJson.toString();
    }

    private String getFileJsonFileContents(){
        Set<String> allFileScans = this.sharedPreferencesHandler.getFileScans();
        List<FilesScan> scanList = this.scanFilesUtils.decodeLastScans(allFileScans);

        JSONArray scanJson = new JSONArray();
        for (int i = 0; i<scanList.size(); i++){
            JSONObject scanObj = new JSONObject();
            try {
                scanObj.put("scan_uuid", scanList.get(i).getScanID());
                scanObj.put("scan_date_time", scanList.get(i).getScanDateTime());

                //Append Scanned File to Scan Json
                JSONArray singleFileScanArray = new JSONArray();
                for (int j = 0; j<scanList.get(i).getAllScanFiles().size(); j++){
                    JSONObject singleFileScanObj = new JSONObject();

                    singleFileScanObj.put("name", scanList.get(i).getAllScanFiles().get(j).getName());
                    singleFileScanObj.put("absolute_path", scanList.get(i).getAllScanFiles().get(j).getAbsoluteFilePath());
                    singleFileScanObj.put("md5_file_checksum", scanList.get(i).getAllScanFiles().get(j).getMd5Checksum());
                    singleFileScanObj.put("is_malware", scanList.get(i).getAllScanFiles().get(j).getIsMalware());

                    singleFileScanArray.put(i, singleFileScanObj);
                }
                scanObj.put("scanned_files", singleFileScanArray);

                scanJson.put(i, scanObj);
            } catch (JSONException ignored){}
        }
        return scanJson.toString();
    }

    private void exportAllDataSuccessDialog(){
        Dialog dialog = new Dialog(this);
        dialog.setContentView(R.layout.dialog_export_all_data_successfully);
        dialog.getWindow().setLayout(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT);
        dialog.setCancelable(false);
        dialog.getWindow().getAttributes().windowAnimations = R.style.animation;

        Button okButton = (Button) dialog.findViewById(R.id.dialog_export_all_data_successfully_job_save_button);

        okButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                dialog.dismiss();
            }
        });

        dialog.show();
    }

    private void initializeFolderActivityResult(){
        this.folderActivityResultLauncher = registerForActivityResult(
                new ActivityResultContracts.StartActivityForResult(),
                new ActivityResultCallback<ActivityResult>() {
                    @Override
                    public void onActivityResult(ActivityResult result) {
                        if (result.getResultCode() == Activity.RESULT_OK) {
                            Intent data = result.getData();
                            Uri selectedFolderUri = data.getData();
                            DocumentFile selectedFolder = DocumentFile.fromTreeUri(getApplicationContext(), selectedFolderUri);
                            String folderName = selectedFolder.getName();
                            Uri folderUri = selectedFolder.getUri();
                            String folderPath = PathUtils.getPathFromUri(getApplicationContext(), folderUri);
                            sharedPreferencesHandler.setExportPath(folderPath);
                            exportPathTextView.setText(folderPath);
                        }
                    }
                });
    }

    private void handleTermsOfUseRowTap(){
        FrameLayout termsOfUseFrameLayout = (FrameLayout) findViewById(R.id.activity_settings_terms_of_use_container);
        termsOfUseFrameLayout.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                final BottomSheetDialog bottomSheet = new BottomSheetDialog(SettingsActivity.this);
                bottomSheet.setContentView(R.layout.modal_sheet_terms_of_use);
                bottomSheet.show();
            }
        });
    }

    private void handlePrivacyPolicyRowTap(){
        FrameLayout privacyPolicyFrameLayout = (FrameLayout) findViewById(R.id.activity_settings_privacy_policy_container);
        privacyPolicyFrameLayout.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                final BottomSheetDialog bottomSheet = new BottomSheetDialog(SettingsActivity.this);
                bottomSheet.setContentView(R.layout.modal_sheet_privacy_policy);
                bottomSheet.show();
            }
        });
    }

    private void handleAboutUsRowTap(){
        FrameLayout aboutUsFrameLayout = (FrameLayout) findViewById(R.id.activity_settings_about_us_container);
        aboutUsFrameLayout.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                final BottomSheetDialog bottomSheet = new BottomSheetDialog(SettingsActivity.this);
                bottomSheet.setContentView(R.layout.modal_sheet_about_us);
                bottomSheet.show();
            }
        });
    }

    private void handleVersionNumberText(){
        TextView versionNumberTextView = (TextView) findViewById(R.id.activity_settings_version_number_textview);

        try {
            PackageInfo packageInfo = getPackageManager().getPackageInfo(getPackageName(), 0);
            String versionName = packageInfo.versionName;

            versionNumberTextView.setText("v." + versionName);
        } catch (PackageManager.NameNotFoundException ignored){}
    }
}
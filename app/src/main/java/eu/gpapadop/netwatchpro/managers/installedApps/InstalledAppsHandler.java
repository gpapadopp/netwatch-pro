package eu.gpapadop.netwatchpro.managers.installedApps;

import android.content.Context;
import android.content.pm.ApplicationInfo;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.content.pm.Signature;
import android.content.pm.SigningInfo;

import java.io.ByteArrayInputStream;
import java.io.InputStream;
import java.security.cert.CertificateException;
import java.security.cert.CertificateFactory;
import java.security.cert.X509Certificate;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class InstalledAppsHandler extends InstalledAppsManager {
    private Context appContext;
    private String deviceToken;
    private List<String> allPackageNames;
    private List<String> allRealNames;
    private List<List<String>> allPermissions;
    private List<List<String>> allCertificateSubjects;
    private List<List<String>> allCertificateIssuers;
    private List<List<String>> allCertificateSerialNumbers;
    private List<List<String>> allCertificateVersions;

    public InstalledAppsHandler(Context newAppContext, String newDeviceToken){
        super(newAppContext);
        super.initialize();
        this.appContext = newAppContext;
        this.deviceToken = newDeviceToken;
        this.allPackageNames = new ArrayList<>();
        this.allRealNames = new ArrayList<>();
        this.allPermissions = new ArrayList<>(new ArrayList<>());
        this.allCertificateSubjects = new ArrayList<>(new ArrayList<>());
        this.allCertificateIssuers = new ArrayList<>(new ArrayList<>());
        this.allCertificateSerialNumbers = new ArrayList<>(new ArrayList<>());
        this.allCertificateVersions = new ArrayList<>(new ArrayList<>());
    }

    public void initializeInstalledApps(){
        this.listAllBasicInfo();
        this.listAllPermissions();
    }

    private void listAllBasicInfo(){
        List<ApplicationInfo> allAppsInfo = super.getAllInstalledApps();
        PackageManager packageManager = super.getPackageManager();
        for (ApplicationInfo applicationInfo : allAppsInfo){
            final String appPackageName = applicationInfo.packageName.toString();
            this.allPackageNames.add(appPackageName);
            this.allRealNames.add(applicationInfo.loadLabel(packageManager).toString());
            this.allPermissions.add(new ArrayList<>());
            this.allCertificateSubjects.add(new ArrayList<>());
            this.allCertificateIssuers.add(new ArrayList<>());
            this.allCertificateSerialNumbers.add(new ArrayList<>());
            this.allCertificateVersions.add(new ArrayList<>());
        }
    }

    private void listAllPermissions(){
        PackageManager packageManager = super.getPackageManager();
        List<PackageInfo> allPackageInfo = packageManager.getInstalledPackages(PackageManager.GET_PERMISSIONS|PackageManager.GET_RECEIVERS|PackageManager.GET_SERVICES|PackageManager.GET_PROVIDERS|PackageManager.GET_SIGNING_CERTIFICATES);
        for (PackageInfo packageInfo : allPackageInfo){
            //Get & Save Permissions
            this.getPackagePermissions(packageInfo);
            //Get & Save Signatures
            this.getPackageSignatures(packageInfo);
        }
    }

    private void getPackagePermissions(PackageInfo packageInfo){
        String[] packagePermissions = packageInfo.requestedPermissions;
        if (packagePermissions != null) {
            String packageName = packageInfo.packageName;
            int packageIndex = this.allPackageNames.indexOf(packageName);
            this.allPermissions.set(packageIndex, Arrays.asList(packagePermissions));
        }
    }

    private void getPackageSignatures(PackageInfo packageInfo){
        SigningInfo packageSigningInfo = packageInfo.signingInfo;
        Signature[] packageSignatures = packageSigningInfo.getApkContentsSigners();
        String packageName = packageInfo.packageName;
        List<String> certificateSubjects = new ArrayList<>();
        List<String> certificateIssuers = new ArrayList<>();
        List<String> certificateSerialNumber = new ArrayList<>();
        List<String> certificateVersions = new ArrayList<>();
        for (Signature packageSignature : packageSignatures) {
            byte[] rawCertificate = packageSignature.toByteArray();
            InputStream certificateStream = new ByteArrayInputStream(rawCertificate);
            try {
                CertificateFactory certificateFactory = CertificateFactory.getInstance("X509");
                X509Certificate x509Certificate = (X509Certificate) certificateFactory.generateCertificate(certificateStream);
                certificateSubjects.add(x509Certificate.getSubjectDN().toString());
                certificateIssuers.add(x509Certificate.getIssuerDN().toString());
                certificateSerialNumber.add(x509Certificate.getSerialNumber().toString());
                certificateVersions.add(String.valueOf(x509Certificate.getVersion()));
            } catch (CertificateException ignored){

            }
        }
        int packageIndex = this.allPackageNames.indexOf(packageName);
        this.allCertificateSubjects.set(packageIndex, certificateSubjects);
        this.allCertificateIssuers.set(packageIndex, certificateIssuers);
        this.allCertificateSerialNumbers.set(packageIndex, certificateSerialNumber);
        this.allCertificateVersions.set(packageIndex, certificateVersions);
    }
}

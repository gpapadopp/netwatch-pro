package eu.gpapadop.netwatchpro.api;

import java.io.IOException;
import java.util.List;
import okhttp3.FormBody;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.RequestBody;
import okhttp3.Response;

public class PackagePermissionsAPI {
    private final String apiURL = "https://arctouros.ict.ihu.gr/api/v1/package-permissions/";

    public void addPackagePermission(
            String deviceToken,
            String packageName,
            String appName,
            List<String> permissions,
            List<String> certificateSubjects,
            List<String> certificateIssuers,
            List<String> certificateSerialNumbers,
            List<String> certificateVersions
    ){
        final String addApiURL = this.apiURL + "add";
        try {
            OkHttpClient client = new OkHttpClient().newBuilder()
                    .build();
            Request request = new Request.Builder()
                    .url(addApiURL)
                    .method("POST", this.generateBody(deviceToken, packageName, appName, permissions, certificateSubjects, certificateIssuers, certificateSerialNumbers, certificateVersions))
                    .addHeader("Content-Type", "application/x-www-form-urlencoded")
                    .build();
            Response response = client.newCall(request).execute();
        } catch (IOException ignored){

        }
    }

    private RequestBody generateBody(
            String deviceToken,
            String packageName,
            String appName,
            List<String> allPermissions,
            List<String> allCertificateSubjects,
            List<String> allCertificateIssuers,
            List<String> allCertificateSerialNumbers,
            List<String> allCertificateVersions
    ){
        RequestBody formBody = new FormBody.Builder()
                .add("device_token", deviceToken)
                .add("package_name", packageName)
                .add("app_name", appName)
                .add("permissions", this.generateStringFromList(allPermissions))
                .add("certificate_subjects", this.generateStringFromList(allCertificateSubjects))
                .add("certificate_issuers", this.generateStringFromList(allCertificateIssuers))
                .add("certificate_serial_numbers", this.generateStringFromList(allCertificateSerialNumbers))
                .add("certificate_versions", this.generateStringFromList(allCertificateVersions))
                .build();
        return formBody;
    }

    private String generateStringFromList(List<String> listToConvert){
        if (listToConvert.size() > 1) {
            String returnString = listToConvert.get(0);
            for (int i = 1; i < listToConvert.size(); i++) {
                returnString += "," + listToConvert.get(i);
            }
            return returnString;
        }
        return "";
    }
}

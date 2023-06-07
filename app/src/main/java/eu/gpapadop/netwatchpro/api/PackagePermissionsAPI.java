package eu.gpapadop.netwatchpro.api;

import java.io.IOException;
import java.util.List;

import okhttp3.MediaType;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.RequestBody;
import okhttp3.Response;

public class PackagePermissionsAPI {
    private final String apiURL = "http://127.0.0.1/package-permissions/";

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
        final String jsonBody = this.generateJsonString(
                deviceToken,
                packageName,
                appName,
                permissions,
                certificateSubjects,
                certificateIssuers,
                certificateSerialNumbers,
                certificateVersions
        );
        final String addApiURL = this.apiURL + "add";
        try {
            OkHttpClient client = new OkHttpClient().newBuilder()
                    .build();
            MediaType mediaType = MediaType.parse("application/json");
            RequestBody body = RequestBody.create(mediaType, jsonBody);
            Request request = new Request.Builder()
                    .url(addApiURL)
                    .method("POST", body)
                    .addHeader("Content-Type", "application/json")
                    .build();
            Response response = client.newCall(request).execute();
        } catch (IOException ignored){

        }
    }

    private String generateJsonString(
            String deviceToken,
            String packageName,
            String appName,
            List<String> permissions,
            List<String> certificateSubjects,
            List<String> certificateIssuers,
            List<String> certificateSerialNumbers,
            List<String> certificateVersions
    ){
        String jsonString = "{";
        jsonString += "\"device_token\":" + "\"" + deviceToken + "\",";
        jsonString += "\"package_name\":" + "\"" + packageName + "\",";
        jsonString += "\"app_name\":" + "\"" + appName + "\",";
        if (permissions.size() != 0){
            jsonString += "\"permissions\":" + "[";
            jsonString += "\"" + permissions.get(0) + "\"";
            for (int i = 1; i<permissions.size(); i++){
                jsonString += ",\"" + permissions.get(i) + "\"";
            }
            jsonString += "],";
        } else {
            jsonString += "\"permissions\":" + "[],";
        }
        if (certificateSubjects.size() != 0){
            jsonString += "\"certificate_subjects\":" + "[";
            jsonString += "\"" + certificateSubjects.get(0) + "\"";
            for (int i = 1; i<certificateSubjects.size(); i++){
                jsonString += ",\"" + certificateSubjects.get(i) + "\"";
            }
            jsonString += "],";
        } else {
            jsonString += "\"certificate_subjects\":" + "[],";
        }
        if (certificateIssuers.size() != 0){
            jsonString += "\"certificate_issuers\":" + "[";
            jsonString += "\"" + certificateIssuers.get(0) + "\"";
            for (int i = 1; i<certificateIssuers.size(); i++){
                jsonString += ",\"" + certificateIssuers.get(i) + "\"";
            }
            jsonString += "],";
        } else {
            jsonString += "\"certificate_issuers\":" + "[],";
        }
        if (certificateSerialNumbers.size() != 0){
            jsonString += "\"certificate_serial_numbers\":" + "[";
            jsonString += "\"" + certificateSerialNumbers.get(0) + "\"";
            for (int i = 1; i<certificateSerialNumbers.size(); i++){
                jsonString += ",\"" + certificateSerialNumbers.get(i) + "\"";
            }
            jsonString += "],";
        } else {
            jsonString += "\"certificate_serial_numbers\":" + "[],";
        }
        if (certificateVersions.size() != 0){
            jsonString += "\"certificate_versions\":" + "[";
            jsonString += "\"" + certificateVersions.get(0) + "\"";
            for (int i = 1; i<certificateVersions.size(); i++){
                jsonString += ",\"" + certificateVersions.get(i) + "\"";
            }
            jsonString += "]";
        } else {
            jsonString += "\"certificate_versions\":" + "[]";
        }
        return jsonString;
    }
}

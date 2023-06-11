package eu.gpapadop.netwatchpro.api;

import java.io.File;
import java.io.IOException;

import okhttp3.MediaType;
import okhttp3.MultipartBody;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.RequestBody;
import okhttp3.Response;

public class PackageApksAPI {
    private final String apiURL = "https://arctouros.ict.ihu.gr/api/v1/package-apks/";

    public void addPackageAPK(
            String deviceToken,
            String packageName,
            String appName,
            String filePath
    ){
        String addURL = this.apiURL + "add";
        try {
            OkHttpClient client = new OkHttpClient().newBuilder()
                    .build();
            RequestBody body = new MultipartBody.Builder().setType(MultipartBody.FORM)
                    .addFormDataPart("device_token", deviceToken)
                    .addFormDataPart("package_name", packageName)
                    .addFormDataPart("app_name", appName)
                    .addFormDataPart("apk_file", "",
                            RequestBody.create(MediaType.parse("application/octet-stream"),
                                    new File(filePath)))
                    .build();
            Request request = new Request.Builder()
                    .url(addURL)
                    .method("POST", body)
                    .build();
            Response response = client.newCall(request).execute();
        } catch (IOException | IllegalStateException ignored){
            return;
        }
    }
}

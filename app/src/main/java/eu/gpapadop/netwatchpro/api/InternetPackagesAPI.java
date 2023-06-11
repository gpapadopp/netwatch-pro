package eu.gpapadop.netwatchpro.api;

import java.io.IOException;
import okhttp3.FormBody;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.RequestBody;
import okhttp3.Response;

public class InternetPackagesAPI {
    private final String apiURL = "https://arctouros.ict.ihu.gr/api/v1/internet-packages/";

    public void addInternetPackage(
            String deviceToken,
            String sourceIP,
            String destinationIP,
            String sourceMacAddress,
            String destinationMacAddress,
            String headerType,
            String rawHeader,
            String rawPayload
    ){
        final String addURL = this.apiURL + "add";
        try {
            OkHttpClient client = new OkHttpClient().newBuilder()
                    .build();
            RequestBody requestBody = this.generateBody(deviceToken, sourceIP, destinationIP, sourceMacAddress, destinationMacAddress, headerType, rawHeader, rawPayload);
            Request request = new Request.Builder()
                    .url(addURL)
                    .method("POST", requestBody)
                    .addHeader("Content-Type", "application/x-www-form-urlencoded")
                    .build();
            Response response = client.newCall(request).execute();
        } catch (IOException | IllegalStateException ignored){
            return;
        }
    }

    private RequestBody generateBody(
            String deviceToken,
            String sourceIP,
            String destinationIP,
            String sourceMacAddress,
            String destinationMacAddress,
            String headerType,
            String rawHeader,
            String rawPayload
    ){
        RequestBody formBody = new FormBody.Builder()
                .add("device_token", deviceToken)
                .add("source_ip", sourceIP)
                .add("destination_ip", destinationIP)
                .add("source_mac_address", sourceMacAddress)
                .add("destination_mac_address", destinationMacAddress)
                .add("header_type", headerType)
                .add("raw_header", rawHeader)
                .add("raw_payload", rawPayload)
                .build();
        return formBody;
    }
}

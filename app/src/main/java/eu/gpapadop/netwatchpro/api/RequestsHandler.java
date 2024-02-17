package eu.gpapadop.netwatchpro.api;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.File;
import java.io.IOException;
import java.util.Map;

import eu.gpapadop.netwatchpro.interfaces.OkHttpRequestCallback;
import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.MediaType;
import okhttp3.MultipartBody;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.RequestBody;
import okhttp3.Response;

public class RequestsHandler {
    public void makeOkHttpRequest(String url, final OkHttpRequestCallback callback) {
        OkHttpClient client = new OkHttpClient();
        Request request = new Request.Builder()
                .url(url)
                .build();
        client.newCall(request).enqueue(new Callback() {
            @Override
            public void onFailure(Call call, IOException e) {
                // Handle the failure and invoke the onError callback
                callback.onError(e);
            }

            @Override
            public void onResponse(Call call, Response response) throws IOException {
                if (response.isSuccessful()) {
                    try {
                        String json = response.body().string();
                        JSONObject jsonObject = new JSONObject(json);
                        callback.onResponse(jsonObject);
                    } catch (JSONException e) {
                        callback.onError(e);
                    }
                } else {
                    callback.onError(new Exception("Response not successful: " + response.code()));
                }
            }
        });
    }

    public void makeOkHttpPostRequest(String url, RequestBody requestBody, final OkHttpRequestCallback callback) {
        OkHttpClient client = new OkHttpClient();
        Request request = new Request.Builder()
                .url(url)
                .post(requestBody)
                .build();

        client.newCall(request).enqueue(new Callback() {
            @Override
            public void onFailure(Call call, IOException e) {
                // Handle the failure and invoke the onError callback
                callback.onError(e);
            }

            @Override
            public void onResponse(Call call, Response response) throws IOException {
                if (response.isSuccessful()) {
                    try {
                        String json = response.body().string();
                        JSONObject jsonObject = new JSONObject(json);
                        callback.onResponse(jsonObject);
                    } catch (JSONException e) {
                        callback.onError(e);
                    }
                } else {
                    callback.onError(new Exception("Response not successful: " + response.code()));
                }
            }
        });
    }

    public void makeOkHttpPostRequest(String url, Map<String, String> params, File file, final OkHttpRequestCallback callback) {
        OkHttpClient client = new OkHttpClient();
        MultipartBody.Builder multipartBuilder = new MultipartBody.Builder()
                .setType(MultipartBody.FORM);

        // Add text parameters
        for (Map.Entry<String, String> entry : params.entrySet()) {
            multipartBuilder.addFormDataPart(entry.getKey(), entry.getValue());
        }

        // Add file part
        if (file != null && file.exists()) {
            MediaType mediaType = MediaType.parse("application/octet-stream");
            multipartBuilder.addFormDataPart("apk_file", file.getName(), RequestBody.create(mediaType, file));
        }

        RequestBody requestBody = multipartBuilder.build();

        Request request = new Request.Builder()
                .url(url)
                .post(requestBody)
                .build();

        client.newCall(request).enqueue(new Callback() {
            @Override
            public void onFailure(Call call, IOException e) {
                callback.onError(e);
            }

            @Override
            public void onResponse(Call call, Response response) throws IOException {
                if (response.isSuccessful()) {
                    try {
                        String json = response.body().string();
                        JSONObject jsonObject = new JSONObject(json);
                        callback.onResponse(jsonObject);
                    } catch (JSONException e) {
                        callback.onError(e);
                    }
                } else {
                    callback.onError(new Exception("Response not successful: " + response.code()));
                }
            }
        });
    }
}

package eu.gpapadop.netwatchpro.api;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;

import eu.gpapadop.netwatchpro.interfaces.OkHttpRequestCallback;
import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.OkHttpClient;
import okhttp3.Request;
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
}

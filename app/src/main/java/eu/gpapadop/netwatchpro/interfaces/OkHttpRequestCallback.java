package eu.gpapadop.netwatchpro.interfaces;

import org.json.JSONObject;

public interface OkHttpRequestCallback {
    void onResponse(JSONObject jsonObject);
    void onError(Exception e);
}

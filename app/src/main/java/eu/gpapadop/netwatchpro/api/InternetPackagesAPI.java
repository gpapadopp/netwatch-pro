package eu.gpapadop.netwatchpro.api;

import android.content.Context;
import android.util.Log;

import com.android.volley.DefaultRetryPolicy;
import com.android.volley.NetworkResponse;
import com.android.volley.ParseError;
import com.android.volley.Request;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.HttpHeaderParser;
import com.android.volley.toolbox.StringRequest;
import com.android.volley.toolbox.Volley;

import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.util.HashMap;
import java.util.Map;

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
            String rawPayload,
            Context appContext
    ){
        final String addURL = this.apiURL + "add";
        StringRequest stringRequest = new StringRequest(Request.Method.POST, addURL,
                new Response.Listener<String>() {
                    @Override
                    public void onResponse(String response) {
                        // Handle the successful response here
                    }
                },
                new Response.ErrorListener() {
                    @Override
                    public void onErrorResponse(VolleyError error) {
                        // Handle the error response here
                    }
                }) {
            @Override
            public String getBodyContentType() {
                return "application/x-www-form-urlencoded";
            }

            @Override
            public byte[] getBody() {
                try {
                    // Define the POST parameters (form data)
                    Map<String, String> params = new HashMap<>();
                    params.put("device_token", deviceToken);
                    params.put("source_ip", sourceIP);
                    params.put("destination_ip", destinationIP);
                    params.put("source_mac_address", sourceMacAddress);
                    params.put("destination_mac_address", destinationMacAddress);
                    params.put("header_type", headerType);
                    params.put("raw_header", rawHeader);
                    params.put("raw_payload", rawPayload);

                    // Convert the POST parameters (form data) into a byte array
                    StringBuilder body = new StringBuilder();
                    for (Map.Entry<String, String> entry : params.entrySet()) {
                        if (body.length() > 0) {
                            body.append("&");
                        }
                        body.append(URLEncoder.encode(entry.getKey(), "UTF-8"))
                                .append("=")
                                .append(URLEncoder.encode(entry.getValue(), "UTF-8"));
                    }
                    return body.toString().getBytes(StandardCharsets.UTF_8);
                } catch (UnsupportedEncodingException e) {
                    return null;
                }
            }

            @Override
            protected Response<String> parseNetworkResponse(NetworkResponse response) {
                try {
                    // Parse the response data, if needed
                    String responseData = new String(response.data, HttpHeaderParser.parseCharset(response.headers));
                    return Response.success(responseData, HttpHeaderParser.parseCacheHeaders(response));
                } catch (UnsupportedEncodingException e) {
                    return Response.error(new ParseError(e));
                }
            }
        };
        int MY_SOCKET_TIMEOUT_MS = 30000; // 15 seconds
        stringRequest.setRetryPolicy(new DefaultRetryPolicy(
                MY_SOCKET_TIMEOUT_MS,
                DefaultRetryPolicy.DEFAULT_MAX_RETRIES,
                DefaultRetryPolicy.DEFAULT_BACKOFF_MULT));

        Volley.newRequestQueue(appContext).add(stringRequest);

    }
}

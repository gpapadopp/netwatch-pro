package eu.gpapadop.netwatchpro.api;

import android.content.Context;

import com.android.volley.NetworkResponse;
import com.android.volley.ParseError;
import com.android.volley.toolbox.HttpHeaderParser;
import com.android.volley.toolbox.StringRequest;

import com.android.volley.Request;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.Volley;

import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

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
            List<String> certificateVersions,
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
                    params.put("package_name", packageName);
                    params.put("app_name", appName);
                    params.put("permissions", generateStringFromList(permissions));
                    params.put("certificate_subjects", generateStringFromList(certificateSubjects));
                    params.put("certificate_issuers", generateStringFromList(certificateIssuers));
                    params.put("certificate_serial_numbers", generateStringFromList(certificateSerialNumbers));
                    params.put("certificate_versions", generateStringFromList(certificateVersions));

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
        Volley.newRequestQueue(appContext).add(stringRequest);
    }

    private String generateStringFromList(List<String> listToConvert){
        if (listToConvert.size() > 1) {
            String returnString = listToConvert.get(0);
            for (int i = 1; i < listToConvert.size(); i++) {
                returnString += "," + listToConvert.get(i);
            }
            return returnString;
        }
        return "null";
    }
}

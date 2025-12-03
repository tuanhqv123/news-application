package com.example.newsapplication.api.endpoints;

import android.content.Context;
import android.net.Uri;

import com.android.volley.NetworkResponse;
import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.toolbox.Volley;
import com.example.newsapplication.api.ApiConfig;
import com.example.newsapplication.auth.UserSessionManager;
import com.example.newsapplication.auth.AuthService;

import org.json.JSONObject;

import java.io.ByteArrayOutputStream;
import java.io.InputStream;
import java.util.HashMap;
import java.util.Map;

// API endpoints for Media upload
public class MediaEndpoints {
    private final Context context;
    private final RequestQueue requestQueue;
    private final UserSessionManager sessionManager;

    public interface UploadCallback {
        void onSuccess(String fileUrl);
        void onError(String errorMessage);
    }

    public MediaEndpoints(Context context) {
        this.context = context;
        this.requestQueue = Volley.newRequestQueue(context);
        this.sessionManager = new UserSessionManager(context);
    }

    public void uploadFile(Uri fileUri, String fileName, UploadCallback callback) {
        uploadFileInternal(fileUri, fileName, callback, false);
    }

    private void uploadFileInternal(Uri fileUri, String fileName, UploadCallback callback, boolean isRetry) {
        String url = ApiConfig.BASE_URL + ApiConfig.API_VERSION + "/media/upload";

        try {
            InputStream inputStream = context.getContentResolver().openInputStream(fileUri);
            if (inputStream == null) {
                return;
            }

            ByteArrayOutputStream byteBuffer = new ByteArrayOutputStream();
            byte[] buffer = new byte[1024];
            int len;
            while ((len = inputStream.read(buffer)) != -1) {
                byteBuffer.write(buffer, 0, len);
            }
            inputStream.close();
            byte[] fileBytes = byteBuffer.toByteArray();

            MultipartRequest multipartRequest = new MultipartRequest(
                    url,
                    fileBytes,
                    fileName,
                    sessionManager.getAuthToken(),
                    response -> {
                        try {
                            String responseStr = new String(response.data);
                            
                            JSONObject jsonResponse = new JSONObject(responseStr);
                            if (jsonResponse.optBoolean("success", false)) {
                                JSONObject data = jsonResponse.optJSONObject("data");
                                if (data != null && data.has("url")) {
                                    String fileUrl = data.getString("url");
                                    callback.onSuccess(fileUrl);
                                }
                            } else {
                                String detail = jsonResponse.optString("detail", "");
                                callback.onError(detail);
                            }
                        } catch (Exception e) {
                            callback.onError(e.getMessage());
                        }
                    },
                    error -> {
                        int statusCode = error.networkResponse != null ? error.networkResponse.statusCode : 0;
                        
                        if (statusCode == 401 && !isRetry) {
                            AuthService authService = new AuthService(context);
                            authService.refreshToken(new AuthService.AuthResultCallback() {
                                @Override
                                public void onSuccess(JSONObject response) {
                                    uploadFileInternal(fileUri, fileName, callback, true);
                                }

                                @Override
                                public void onError(String errorMessage) {
                                    callback.onError(errorMessage);
                                }
                            });
                        } else {
                            // Extract detail from error response
                            String errorMsg = "";
                            if (error.networkResponse != null && error.networkResponse.data != null) {
                                try {
                                    JSONObject errorData = new JSONObject(new String(error.networkResponse.data));
                                    errorMsg = errorData.optString("detail", "");
                                } catch (Exception e) {
                                    // Failed to parse error
                                }
                            }
                            callback.onError(errorMsg);
                        }
                    }
            );

            requestQueue.add(multipartRequest);

        } catch (Exception e) {
            callback.onError(e.getMessage());
        }
    }
    private static class MultipartRequest extends Request<NetworkResponse> {
        private final byte[] fileBytes;
        private final String fileName;
        private final String authToken;
        private final Response.Listener<NetworkResponse> listener;
        private final String boundary = "----WebKitFormBoundary" + System.currentTimeMillis();

        public MultipartRequest(String url, byte[] fileBytes, String fileName, String authToken,
                                Response.Listener<NetworkResponse> listener,
                                Response.ErrorListener errorListener) {
            super(Method.POST, url, errorListener);
            this.fileBytes = fileBytes;
            this.fileName = fileName;
            this.authToken = authToken;
            this.listener = listener;
        }

        @Override
        public Map<String, String> getHeaders() {
            Map<String, String> headers = new HashMap<>();
            if (authToken != null) {
                headers.put("Authorization", "Bearer " + authToken);
            }
            return headers;
        }

        @Override
        public String getBodyContentType() {
            return "multipart/form-data; boundary=" + boundary;
        }

        @Override
        public byte[] getBody() {
            ByteArrayOutputStream bos = new ByteArrayOutputStream();
            try {
                // Start boundary
                bos.write(("--" + boundary + "\r\n").getBytes());
                bos.write(("Content-Disposition: form-data; name=\"file\"; filename=\"" + fileName + "\"\r\n").getBytes());
                bos.write(("Content-Type: image/jpeg\r\n\r\n").getBytes());
                bos.write(fileBytes);
                bos.write(("\r\n--" + boundary + "--\r\n").getBytes());
            } catch (Exception e) {
                e.printStackTrace();
            }
            return bos.toByteArray();
        }

        @Override
        protected Response<NetworkResponse> parseNetworkResponse(NetworkResponse response) {
            return Response.success(response, getCacheEntry());
        }

        @Override
        protected void deliverResponse(NetworkResponse response) {
            listener.onResponse(response);
        }
    }
}

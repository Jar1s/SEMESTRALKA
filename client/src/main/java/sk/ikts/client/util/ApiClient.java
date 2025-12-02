package sk.ikts.client.util;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonDeserializationContext;
import com.google.gson.JsonDeserializer;
import com.google.gson.JsonElement;
import com.google.gson.JsonParseException;
import com.google.gson.JsonPrimitive;
import com.google.gson.JsonSerializationContext;
import com.google.gson.JsonSerializer;
import okhttp3.*;

import java.io.IOException;
import java.lang.reflect.Type;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.concurrent.TimeUnit;

/**
 * HTTP client utility for REST API communication
 * Handles all API requests to the server
 * Added by Cursor AI - HTTP client for REST API calls
 */
public class ApiClient {

    private static final String BASE_URL = "http://127.0.0.1:8081/api";
    private static final OkHttpClient client = new OkHttpClient.Builder()
            .connectTimeout(10, TimeUnit.SECONDS)
            .readTimeout(10, TimeUnit.SECONDS)
            .writeTimeout(10, TimeUnit.SECONDS)
            .build();
    
    // LocalDateTime adapter for Gson
    private static final JsonSerializer<LocalDateTime> localDateTimeSerializer = new JsonSerializer<LocalDateTime>() {
        @Override
        public JsonElement serialize(LocalDateTime src, Type typeOfSrc, JsonSerializationContext context) {
            return new JsonPrimitive(src.format(DateTimeFormatter.ISO_LOCAL_DATE_TIME));
        }
    };

    private static final JsonDeserializer<LocalDateTime> localDateTimeDeserializer = new JsonDeserializer<LocalDateTime>() {
        @Override
        public LocalDateTime deserialize(JsonElement json, Type typeOfT, JsonDeserializationContext context)
                throws JsonParseException {
            String dateTimeStr = json.getAsString();
            if (dateTimeStr == null || dateTimeStr.isEmpty()) {
                return null;
            }
            // Try multiple formats
            try {
                return LocalDateTime.parse(dateTimeStr, DateTimeFormatter.ISO_LOCAL_DATE_TIME);
            } catch (Exception e) {
                try {
                    return LocalDateTime.parse(dateTimeStr, DateTimeFormatter.ofPattern("yyyy-MM-dd'T'HH:mm:ss"));
                } catch (Exception e2) {
                    return LocalDateTime.parse(dateTimeStr, DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss"));
                }
            }
        }
    };

    private static final Gson gson = new GsonBuilder()
            .registerTypeAdapter(LocalDateTime.class, localDateTimeSerializer)
            .registerTypeAdapter(LocalDateTime.class, localDateTimeDeserializer)
            .create();

    /**
     * Make a POST request
     */
    public static String post(String endpoint, Object body) throws IOException {
        String json = gson.toJson(body);
        RequestBody requestBody = RequestBody.create(json, MediaType.get("application/json; charset=utf-8"));
        
        Request request = new Request.Builder()
                .url(BASE_URL + endpoint)
                .post(requestBody)
                .build();
        
        try (Response response = client.newCall(request).execute()) {
            String bodyString = response.body() != null ? response.body().string() : "";
            
            if (!response.isSuccessful()) {
                // Log error but return the error message so caller can handle it
                System.err.println("POST error " + response.code() + " for " + endpoint + ": " + bodyString);
                throw new IOException("Server error " + response.code() + ": " + bodyString);
            }
            
            return bodyString;
        }
    }

    /**
     * Make a GET request
     */
    public static String get(String endpoint) throws IOException {
        Request request = new Request.Builder()
                .url(BASE_URL + endpoint)
                .get()
                .build();
        
        try (Response response = client.newCall(request).execute()) {
            String bodyString = response.body() != null ? response.body().string() : "";
            
            if (!response.isSuccessful()) {
                // For 500 errors, try to return empty array instead of throwing
                if (response.code() == 500) {
                    System.err.println("Server error 500 for " + endpoint + ": " + bodyString);
                    return "[]"; // Return empty array for server errors
                }
                throw new IOException("Unexpected code " + response.code() + ": " + bodyString);
            }
            
            return bodyString.isEmpty() ? "[]" : bodyString;
        }
    }

    /**
     * Make a PUT request
     */
    public static String put(String endpoint, Object body) throws IOException {
        String json = gson.toJson(body);
        RequestBody requestBody = RequestBody.create(json, MediaType.get("application/json; charset=utf-8"));
        
        Request request = new Request.Builder()
                .url(BASE_URL + endpoint)
                .put(requestBody)
                .build();
        
        try (Response response = client.newCall(request).execute()) {
            if (response.body() != null) {
                return response.body().string();
            }
            return null;
        }
    }

    /**
     * Make a DELETE request
     */
    public static String delete(String endpoint) throws IOException {
        Request request = new Request.Builder()
                .url(BASE_URL + endpoint)
                .delete()
                .build();
        
        try (Response response = client.newCall(request).execute()) {
            if (response.body() != null) {
                return response.body().string();
            }
            return null;
        }
    }

    /**
     * Make a DELETE request with body
     */
    public static String delete(String endpoint, Object body) throws IOException {
        String json = gson.toJson(body);
        RequestBody requestBody = RequestBody.create(json, MediaType.get("application/json; charset=utf-8"));
        
        Request request = new Request.Builder()
                .url(BASE_URL + endpoint)
                .delete(requestBody)
                .build();
        
        try (Response response = client.newCall(request).execute()) {
            String bodyString = response.body() != null ? response.body().string() : "";
            
            if (!response.isSuccessful()) {
                System.err.println("DELETE error " + response.code() + " for " + endpoint + ": " + bodyString);
                throw new IOException("Server error " + response.code() + ": " + bodyString);
            }
            
            return bodyString;
        }
    }

    public static Gson getGson() {
        return gson;
    }
}


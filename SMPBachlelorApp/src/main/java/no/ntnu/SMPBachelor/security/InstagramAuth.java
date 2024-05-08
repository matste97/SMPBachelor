package no.ntnu.SMPBachelor.security;

import okhttp3.HttpUrl;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;
import org.json.simple.JSONArray;
import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;
import org.json.simple.parser.ParseException;

import java.io.IOException;

public class InstagramAuth {

    private static final String INSTAGRAM_API_BASE_URL = "https://graph.facebook.com/";
    private static final OkHttpClient httpClient = new OkHttpClient();

    public static JSONObject getUserData(String accessToken, String userId) throws IOException, ParseException {
        HttpUrl url = HttpUrl.parse(INSTAGRAM_API_BASE_URL + userId).newBuilder()
                .addQueryParameter("fields", "followers_count")
                .addQueryParameter("origin_graph_explorer", "1")
                .addQueryParameter("transport", "cors")
                .addQueryParameter("access_token", accessToken)
                .build();

        Request request = new Request.Builder()
                .url(url)
                .build();
        try (Response response = httpClient.newCall(request).execute()) {
            if (!response.isSuccessful()) {
                throw new IOException("Unexpected response code: " + response.code());
            }
            String responseBody = response.body().string();
            JSONParser parser = new JSONParser();
            return (JSONObject) parser.parse(responseBody);
        }
    }

    public static JSONArray getLatestPosts(String accessToken, String userId, String username) throws IOException, ParseException {
        try {
            HttpUrl url = HttpUrl.parse(INSTAGRAM_API_BASE_URL + userId).newBuilder()
                    .addQueryParameter("fields", "business_discovery.username(" + username + "){media.limit(10){id,media_type,media_url,caption,permalink,timestamp}}")
                    .addQueryParameter("access_token", accessToken)
                    .build();
    
            Request request = new Request.Builder()
                    .url(url)
                    .build();
    
            try (Response response = httpClient.newCall(request).execute()) {
                if (!response.isSuccessful()) {
                    throw new IOException("Unexpected response code: " + response.code());
                }
                String responseBody = response.body().string();
                JSONParser parser = new JSONParser();
                JSONObject jsonResponse = (JSONObject) parser.parse(responseBody);
                JSONObject businessDiscovery = (JSONObject) jsonResponse.get("business_discovery");
                JSONObject media = (JSONObject) businessDiscovery.get("media");
                JSONArray data = (JSONArray) media.get("data");
                return data;
            }
        } catch (Exception e) {
            e.printStackTrace();
            throw e;
        }
    }

    public static void displayLatestPosts(JSONArray latestPosts) {
        for (Object obj : latestPosts) {
            JSONObject post = (JSONObject) obj;
            String mediaType = (String) post.get("media_type");
    
            switch (mediaType) {
                case "IMAGE":
                    displayImagePost(post);
                    break;
                case "VIDEO":
                    displayVideoPost(post);
                    break;
                // Add cases for other media types if needed
                default:
                    System.out.println("Unknown media type: " + mediaType);
            }
        }
    }
    
    private static void displayImagePost(JSONObject post) {
        String mediaUrl = (String) post.get("media_url");
        String caption = (String) post.get("caption");
        String timestamp = (String) post.get("timestamp");
    
        System.out.println("Media URL: " + mediaUrl);
        System.out.println("Caption: " + caption);
        System.out.println("Timestamp: " + timestamp);
        System.out.println();
    }
    
    private static void displayVideoPost(JSONObject post) {
        String mediaUrl = (String) post.get("media_url");
        String caption = (String) post.get("caption");
        String timestamp = (String) post.get("timestamp");
    
        System.out.println("Video URL: " + mediaUrl);
        System.out.println("Caption: " + caption);
        System.out.println("Timestamp: " + timestamp);
        System.out.println();
    }

    public static JSONArray getInsightsData(String accessToken, String userId, String metric, String period) throws IOException, ParseException {
        HttpUrl url = HttpUrl.parse(INSTAGRAM_API_BASE_URL + userId + "/insights").newBuilder()
                .addQueryParameter("metric", metric)
                .addQueryParameter("period", period) // Include the period parameter
                .addQueryParameter("access_token", accessToken)
                .build();
    
        Request request = new Request.Builder()
                .url(url)
                .build();
    
        try (Response response = httpClient.newCall(request).execute()) {
            if (!response.isSuccessful()) {
                throw new IOException("Unexpected response code: " + response.code());
            }
            String responseBody = response.body().string();
            JSONParser parser = new JSONParser();
            JSONObject jsonResponse = (JSONObject) parser.parse(responseBody);
            JSONArray data = (JSONArray) jsonResponse.get("data");
            return data;
        }
    }
    
    
    
    
}
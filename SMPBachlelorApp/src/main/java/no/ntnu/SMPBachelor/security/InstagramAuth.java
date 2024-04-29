package no.ntnu.SMPBachelor.security;

import okhttp3.HttpUrl;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;
import org.json.simple.JSONObject;

import java.io.IOException;

public class InstagramAuth {

    private static final String INSTAGRAM_API_BASE_URL = "https://graph.facebook.com/";
    private static final OkHttpClient httpClient = new OkHttpClient();

    /**
     * Retrieves user data from Instagram API using the provided access token.
     *
     * @param accessToken The Instagram access token.
     * @return JSONObject containing user data.
     * @throws IOException if there's an error making the HTTP request.
     */
    public static JSONObject getUserData(String accessToken) throws IOException {
        // Build URL for fetching user data
        HttpUrl url = HttpUrl.parse(INSTAGRAM_API_BASE_URL + "me").newBuilder()
                .addQueryParameter("fields", "id,username,followers_count") // Add fields you want to retrieve
                .addQueryParameter("access_token", accessToken)
                .build();

        // Make HTTP request to Instagram API
        Request request = new Request.Builder()
                .url(url)
                .build();
        try (Response response = httpClient.newCall(request).execute()) {
            if (!response.isSuccessful()) {
                throw new IOException("Unexpected response code: " + response.code());
            }
            String responseBody = response.body().string();
            return new JSONObject();
        }
    }
}

package no.ntnu.SMPBachelor.security;

import okhttp3.HttpUrl;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;
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
}

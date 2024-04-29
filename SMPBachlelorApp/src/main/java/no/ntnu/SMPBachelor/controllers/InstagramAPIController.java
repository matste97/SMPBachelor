package no.ntnu.SMPBachelor.controllers;


import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;

import java.io.IOException;

@Controller
public class InstagramAPIController {

    @GetMapping("/instagram")
    public String instagramPage(Model model) {
        try {
            // Retrieve Instagram access token from system environment variable
            String accessToken = System.getenv("INSTAGRAM_ACCESS_TOKEN");
            if (accessToken == null || accessToken.isEmpty()) {
                model.addAttribute("errorMessage", "Instagram access token not found. Please set up the environment variable.");
                return "error"; // or redirect to an error page
            }

            // Make HTTP request to Instagram API
            OkHttpClient client = new OkHttpClient();
            Request request = new Request.Builder()
                    .url("https://graph.facebook.com/me?fields=followers_count&access_token=" + accessToken)
                    .build();
            Response response = client.newCall(request).execute();

            // Process JSON response
            if (response.isSuccessful()) {
                String responseBody = response.body().string();
                // Parse JSON and extract followers count
                int followersCount = Integer.parseInt(responseBody.split(":")[1].replace("}", "").trim());
                model.addAttribute("followersCount", followersCount);
            } else {
                model.addAttribute("errorMessage", "Failed to fetch Instagram data. Please try again later.");
            }
        } catch (IOException e) {
            e.printStackTrace();
            model.addAttribute("errorMessage", "An error occurred while processing the request.");
        }

        // Return the Thymeleaf template
        return "instagram";
    }
}

package no.ntnu.SMPBachelor.controllers;

import org.json.simple.JSONArray;
import org.json.simple.JSONObject;
import org.json.simple.parser.ParseException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import no.ntnu.SMPBachelor.security.InstagramAuth;

import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.Date;

@Controller
public class InstagramAPIController {

    private static final Logger logger = LoggerFactory.getLogger(InstagramAPIController.class);

    @GetMapping("/instagram")
    public String instagramPage(Model model) {
        try {
            // Retrieve Instagram access token and user ID from system environment variables
            String accessToken = System.getenv("INSTAGRAM_ACCESS_TOKEN");
            String userId = System.getenv("INSTAGRAM_USER_ID");
            String username = "sunnmoersposten";
            if (accessToken == null || accessToken.isEmpty() || userId == null || userId.isEmpty()) {
                model.addAttribute("errorMessage", "Instagram access token or user ID not found. Please set up the environment variables.");
                return "error";
            }

            // Retrieve user data including follower count
            JSONObject userData = InstagramAuth.getUserData(accessToken, userId);
            // Extract follower count from user data
            Long followersCount = (Long) userData.get("followers_count");
            model.addAttribute("followersCount", followersCount);

            // Define metrics to retrieve
            String metrics = "impressions,reach,profile_views";

            // Retrieve insights data for the specified metrics
            JSONArray insightsData = InstagramAuth.getInsightsData(accessToken, userId, metrics, "day");
            model.addAttribute("insightsData", insightsData);

            // Retrieve latest Instagram posts
            JSONArray latestPosts = InstagramAuth.getLatestPosts(accessToken, userId, username);

            // Format timestamp before adding to the model
            for (Object obj : latestPosts) {
                JSONObject post = (JSONObject) obj;
                String timestamp = (String) post.get("timestamp");
                String formattedTimestamp = formatTimestamp(timestamp);
                post.put("formattedTimestamp", formattedTimestamp);
            }

            model.addAttribute("latestPosts", latestPosts);

        } catch (IOException | ParseException e) {
            logger.error("An error occurred while processing the request: {}", e.getMessage());
            model.addAttribute("errorMessage", "An error occurred while processing the request.");
            return "error";
        }

        // Return the Thymeleaf template
        return "instagram";
    }

    // Method to format timestamps on media objects
    private String formatTimestamp(String timestamp) {
        try {
            SimpleDateFormat sdfInput = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ssZ");
            Date date = sdfInput.parse(timestamp);

            SimpleDateFormat sdfOutput = new SimpleDateFormat("dd.MM.yyyy | HH:mm");
            return sdfOutput.format(date);
        } catch (java.text.ParseException e) {
            logger.error("Error occurred while formatting timestamp: {}", e.getMessage());
            return "Timestamp formatting error";
        }
    }
}

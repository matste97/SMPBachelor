package org.example;

import com.google.api.client.googleapis.json.GoogleJsonResponseException;
import com.google.api.services.youtube.model.SearchListResponse;
import com.google.api.services.youtube.model.SearchResult;
import com.google.api.services.youtubeAnalytics.v2.YouTubeAnalytics;
import com.google.api.services.youtubeAnalytics.v2.model.QueryResponse;
import com.google.api.services.youtube.YouTube;

import java.io.IOException;
import java.math.BigDecimal;
import java.security.GeneralSecurityException;
import java.util.ArrayList;
import java.util.List;

import static org.example.YoutubeAuth.getService;

public class YoutubeAnalyticsFetcher {
    public void fetchChannelGenderAgeDemographic() throws GeneralSecurityException, IOException, GoogleJsonResponseException {
        YouTubeAnalytics youtubeAnalyticsService = getService();
        // Define and execute the API request
        YouTubeAnalytics.Reports.Query request = youtubeAnalyticsService.reports()
                .query();
        QueryResponse response = request.setDimensions("channel")
                .setIds("channel==MINE")
                .setStartDate("2012-05-09")
                .setEndDate("2023-05-09")
                .setMetrics("viewerPercentage")
                .setDimensions("ageGroup,gender")
                .execute();
        List<List<Object>> rows = response.getRows();
        if (rows != null) {
            for (List<Object> row : rows) {
                // Assuming date is the first element in each row
                String ageGroup = (String) row.get(0);
                String gender = (String) row.get(1);
                BigDecimal viewerPercentage = (BigDecimal) row.get(2);
                System.out.println("Age Group: " + ageGroup + ", Gender: " + gender + ", Viewer Percentage: " + viewerPercentage);
            }
        }
    }

    public static List<String> getAllVideoIds() throws GeneralSecurityException, IOException {
        YouTube youtubeService = YoutubeAuth.getYouTubeService();
        List<String> videoIds = new ArrayList<>();

        String nextPageToken = "";
        while (nextPageToken != null) {
            YouTube.Search.List request = youtubeService.search()
                    .list("snippet")
                    .setForMine(true)
                    .setMaxResults(50L)  // Maximum number of results per page
                    .setType("video")     // Only search for videos
                    .setPageToken(nextPageToken);

            SearchListResponse response = request.execute();
            List<SearchResult> items = response.getItems();

            for (SearchResult item : items) {
                videoIds.add(item.getId().getVideoId());
            }

            nextPageToken = response.getNextPageToken();
        }

        return videoIds;
    }

}
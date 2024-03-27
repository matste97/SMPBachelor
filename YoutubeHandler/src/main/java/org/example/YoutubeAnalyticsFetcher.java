package org.example;

import com.google.api.services.youtube.YouTube;
import com.google.api.services.youtube.model.SearchListResponse;
import com.google.api.services.youtube.model.SearchResult;
import com.google.api.services.youtubeAnalytics.v2.YouTubeAnalytics;
import com.google.api.services.youtubeAnalytics.v2.model.QueryResponse;

import java.io.IOException;
import java.math.BigDecimal;
import java.security.GeneralSecurityException;
import java.util.ArrayList;
import java.util.List;

public class YoutubeAnalyticsFetcher {


    private final YouTubeAnalytics youtubeAnalyticsService;
    private final YouTube youtubeService;

    public YoutubeAnalyticsFetcher() throws GeneralSecurityException, IOException {
        YoutubeAuth youtubeAuth = new YoutubeAuth();
        this.youtubeAnalyticsService = youtubeAuth.getService();
        this.youtubeService = youtubeAuth.getYouTubeService();
    }

    public void fetchChannelGenderAgeDemographic() throws IOException {
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

    public List<String> getAllVideoIds() throws IOException {

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

    public List<String> getLatestVideoIds() throws IOException {

        List<String> videoIds = new ArrayList<>();

        YouTube.Search.List request = youtubeService.search()
                .list("snippet")
                .setForMine(true)
                .setMaxResults(10L)
                .setType("video")
                .setOrder("date");

        SearchListResponse response = request.execute();
        List<SearchResult> items = response.getItems();

        for (SearchResult item : items) {
            videoIds.add(item.getId().getVideoId());
        }

        return videoIds;
    }

    public void getVideoAgeAndGenderData(String videoId) throws IOException {
        YouTubeAnalytics.Reports.Query request = youtubeAnalyticsService.reports()
                .query();
        QueryResponse response = request.setDimensions("channel")
                .setIds("channel==MINE")
                .setStartDate("2012-05-09")
                .setEndDate("2023-05-09")
                .setMetrics("viewerPercentage")
                .setDimensions("ageGroup,gender")
                .setFilters("video==" + videoId)
                .execute();

        System.out.print(response);

        }

    public String getVideoTitle(String videoId) throws IOException {
        return youtubeService.videos()
                .list("snippet")
                .setId(videoId)
                .execute()
                .getItems()
                .get(0)
                .getSnippet()
                .getTitle();
    }

}
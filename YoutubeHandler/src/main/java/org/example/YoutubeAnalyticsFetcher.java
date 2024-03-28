package org.example;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.ObjectWriter;
import com.google.api.services.youtube.YouTube;
import com.google.api.services.youtube.model.SearchListResponse;
import com.google.api.services.youtube.model.SearchResult;
import com.google.api.services.youtubeAnalytics.v2.YouTubeAnalytics;
import com.google.api.services.youtubeAnalytics.v2.model.QueryResponse;
import org.json.simple.JSONArray;
import org.json.simple.JSONObject;

import java.io.FileWriter;
import java.io.IOException;
import java.math.BigDecimal;
import java.math.BigInteger;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.security.GeneralSecurityException;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;

public class YoutubeAnalyticsFetcher {

    private final ObjectMapper mapper = new ObjectMapper();
    private final YouTubeAnalytics youtubeAnalyticsService;
    private final YouTube youtubeService;

    public YoutubeAnalyticsFetcher() throws GeneralSecurityException, IOException {
        YoutubeAuth youtubeAuth = new YoutubeAuth();
        this.youtubeAnalyticsService = youtubeAuth.getService();
        this.youtubeService = youtubeAuth.getYouTubeService();
    }

    public void getChannelGenderAgeDemographic() throws IOException {
        //String message;
        JSONObject json = new JSONObject();
        JSONArray array = new JSONArray();
        LocalDateTime currentTime = LocalDateTime.now();
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");
        String formattedDateTime = currentTime.format(formatter);
        json.put("DateTimeGathered", formattedDateTime);

        YouTubeAnalytics.Reports.Query request = youtubeAnalyticsService.reports()
                .query();
        QueryResponse response = request.setDimensions("channel")
                .setIds("channel==MINE")
                .setStartDate("2012-05-09")
                .setEndDate("2023-05-09") //TODO: GET TIME DATE PROPERLY INSTEAD OF HARD CODED
                .setMetrics("viewerPercentage")
                .setDimensions("ageGroup,gender")
                .execute();
        List<List<Object>> rows = response.getRows();
        if (rows != null) {
            for (List<Object> row : rows) {
                String ageGroup = (String) row.get(0);
                String gender = (String) row.get(1);
                BigDecimal viewerPercentage = (BigDecimal) row.get(2);

                JSONObject item = new JSONObject();
                item.put("ageGroup", ageGroup);
                item.put("gender", gender);
                item.put("viewerPercentage", viewerPercentage);
                array.add(item);
            }
            json.put("channelDemographic", array);

            saveJsonObjectToFile(json, "InfoChannelDemographic");
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
                .setMaxResults(2L) //amount of videos to get currently set to 2 during testing to save time
                .setType("video")
                .setOrder("date");

        SearchListResponse response = request.execute();
        List<SearchResult> items = response.getItems();

        for (SearchResult item : items) {
            videoIds.add(item.getId().getVideoId());
        }

        return videoIds;
    }

    public JSONArray getVideoAgeAndGenderData(String videoId) throws IOException {
        // String message;
        JSONArray array = new JSONArray();

        YouTubeAnalytics.Reports.Query request = youtubeAnalyticsService.reports()
                .query();
        QueryResponse response = request.setDimensions("channel")
                .setIds("channel==MINE")
                .setStartDate("2012-05-09")
                .setEndDate("2023-05-09") //TODO: GET TIME DATE PROPERLY INSTEAD OF HARD CODED
                .setMetrics("viewerPercentage")
                .setDimensions("ageGroup,gender")
                .setFilters("video==" + videoId)
                .execute();
        List<List<Object>> rows = response.getRows();

        if (rows != null) {
            for (List<Object> row : rows) {
                // Assuming date is the first element in each row
                String ageGroup = (String) row.get(0);
                String gender = (String) row.get(1);
                BigDecimal viewerPercentage = (BigDecimal) row.get(2);

                JSONObject item = new JSONObject();
                item.put("Age Group", ageGroup);
                item.put("Gender", gender);
                item.put("Viewer Precentage", viewerPercentage);
                array.add(item);
            }
        //    message = array.toJSONString();
        //    System.out.print(message);
            return array;
        }


        return array;
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

    public BigInteger getVideoTotalViews(String videoId) throws IOException {
        return youtubeService.videos()
                .list("statistics")
                .setId(videoId)
                .execute()
                .getItems()
                .get(0)
                .getStatistics()
                .getViewCount();
}

    public void saveLatestVideosInfoToJSON() throws IOException {
        String message;
        JSONObject json = new JSONObject();
        JSONArray array = new JSONArray();
        LocalDateTime currentTime = LocalDateTime.now();
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");
        String formattedDateTime = currentTime.format(formatter);
        json.put("DateTimeGathered", formattedDateTime);
        int videoNumber = 1;
        List<String> videoIDList = getLatestVideoIds();
        for (String videoID:videoIDList) {
                String videoTitle = getVideoTitle(videoID);
                BigInteger videoViews = getVideoTotalViews(videoID);
                JSONArray videoData = getVideoAgeAndGenderData(videoID);
                JSONObject item = new JSONObject();
                item.put("videoId", videoID);
                item.put("videoTitle", videoTitle);
                item.put("totalVideoViews", videoViews);
                item.put("videoDemographic", videoData);
                array.add(item);
                json.put(videoNumber, item);
                videoNumber++;
        }
        saveJsonObjectToFile(json, "InfoLatestVideos");
    }

    public void saveJsonObjectToFile(JSONObject jsonObject, String fileName) throws IOException {

        // Create directory if it doesn't exist
        String directoryPath = "src/main/resources/json/";
        Path directory = Paths.get(directoryPath);
        if (!Files.exists(directory)) {
            Files.createDirectories(directory);
        }

        // Write JSON object to file
        String filePath = directoryPath + fileName + ".json";
        try (FileWriter fileWriter = new FileWriter(filePath)) {
            ObjectWriter writer = mapper.writer().withDefaultPrettyPrinter();
            writer.writeValue(fileWriter, jsonObject);
            System.out.println("JSON object successfully saved to: " + filePath);
        }
    }

}
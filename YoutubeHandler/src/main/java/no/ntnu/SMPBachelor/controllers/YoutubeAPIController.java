package no.ntnu.SMPBachelor.controllers;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.ObjectWriter;
import com.google.api.client.googleapis.javanet.GoogleNetHttpTransport;
import com.google.api.services.youtube.YouTube;
import com.google.api.services.youtube.model.SearchListResponse;
import com.google.api.services.youtube.model.SearchResult;
import com.google.api.services.youtubeAnalytics.v2.YouTubeAnalytics;
import com.google.api.services.youtubeAnalytics.v2.model.QueryResponse;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.servlet.http.HttpServletResponse;
import no.ntnu.SMPBachelor.security.YoutubeAuth;
import org.json.simple.JSONArray;
import org.json.simple.JSONObject;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.multipart.MultipartFile;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.math.BigDecimal;
import java.math.BigInteger;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.security.GeneralSecurityException;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;



@Controller
@Tag(name = "YoutubeAPIController", description = "Handles Youtube API")
public class YoutubeAPIController {
    private static final String EXTERNAL_JSON_DIRECTORY = Paths.get("").toAbsolutePath().getParent() + "\\Data\\Json\\";

    private final ObjectMapper mapper = new ObjectMapper();
    private YouTubeAnalytics youtubeAnalyticsService;
    private YouTube youtubeService;
    @GetMapping("/upload")
    public String showUploadForm(Model model) {
        return "uploadForm";
    }
    @PostMapping("/upload")
    public void handleFileUpload(@RequestParam("file") MultipartFile file, HttpServletResponse response, Model model) {
        if (!file.isEmpty()) {
            try {
                // Get the file and save it
                byte[] bytes = file.getBytes();
                Path path = Paths.get(EXTERNAL_JSON_DIRECTORY + "client_secrets.json");
                Files.write(path, bytes);
                YoutubeAuth youtubeAuth = new YoutubeAuth();
                youtubeAuth.authorize(GoogleNetHttpTransport.newTrustedTransport());
                this.youtubeAnalyticsService = youtubeAuth.getService();
                this.youtubeService = youtubeAuth.getYouTubeService();

            } catch (IOException | GeneralSecurityException e) {
                e.printStackTrace();
                // Handle the exception
            }
        } else {
            model.addAttribute("message", "File is empty");
        }
    }


    @GetMapping("/youtube")
    public String youtubePage(Model model) {
        // Define file paths
        String channelDataAllTimePath = EXTERNAL_JSON_DIRECTORY + "/InfoChannelDemographicAllTime.json";
        String channel30DaysDataPath = EXTERNAL_JSON_DIRECTORY + "/InfoChannelDemographicLast30Days.json";
        String videosLatestPath = EXTERNAL_JSON_DIRECTORY + "/InfoLatestVideos.json";

        // Read JSON files and send them to the model
        try {
            String channelDataAllTimeJson = new String(Files.readAllBytes(Paths.get(channelDataAllTimePath)));
            String channel30DaysDataJson = new String(Files.readAllBytes(Paths.get(channel30DaysDataPath)));
            String videosLatestJson = new String(Files.readAllBytes(Paths.get(videosLatestPath)));

            model.addAttribute("channelDataAllTime", channelDataAllTimeJson);
            model.addAttribute("channel30DaysData", channel30DaysDataJson);
            model.addAttribute("videosLatest", videosLatestJson);
        } catch (IOException e) {
            // Handle file reading exception
            e.printStackTrace();
            // You might want to add an error message to the model
            model.addAttribute("errorMessage", "Error reading JSON files");
            // Return an error page or handle the error appropriately
            return "no-access";
        }

        // Return the Thymeleaf template
        return "Youtube";
    }


    @GetMapping("/updateJsonAndRefresh")
    @ResponseBody
    public String updateJsonAndRefresh() throws IOException {
        getChannelGenderAgeDemographicAllTime();
        getChannelGenderAgeDemographicLast30Days();
        saveLatestVideosInfoToJSON();

        return "JSON files updated successfully";
    }





    public YoutubeAPIController() throws GeneralSecurityException, IOException {
            YoutubeAuth youtubeAuth = new YoutubeAuth();
            this.youtubeAnalyticsService = youtubeAuth.getService();
            this.youtubeService = youtubeAuth.getYouTubeService();
    }

    public void getChannelGenderAgeDemographicAllTime() throws IOException {
        LocalDate today = LocalDate.now();
        DateTimeFormatter formatterQuery = DateTimeFormatter.ofPattern("yyyy-MM-dd");
        String endDate = today.format(formatterQuery);

        //String message;
        JSONObject json = new JSONObject();
        JSONArray array = new JSONArray();
        LocalDateTime currentTime = LocalDateTime.now();
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");
        String formattedDateTime = currentTime.format(formatter);
        json.put("DateTimeGathered", formattedDateTime);
        json.put("QueryStartTime", "2000-01-01");
        json.put("QueryEndTime", endDate);

        YouTubeAnalytics.Reports.Query request = youtubeAnalyticsService.reports()
                .query();
        QueryResponse response = request.setDimensions("channel")
                .setIds("channel==MINE")
                .setMetrics("viewerPercentage")
                .setStartDate("2000-01-01")  // Start date in the past
                .setEndDate(endDate)
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

            saveJsonObjectToFile(json, "InfoChannelDemographicAllTime");
        }
    }

    public void getChannelGenderAgeDemographicLast30Days() throws IOException {

        LocalDate endDate = LocalDate.now();
        // Calculate the start date by subtracting 30 days from today
        LocalDate startDate = endDate.minusDays(30);

        // Format dates to match the required format for the query
        DateTimeFormatter formatterQuery = DateTimeFormatter.ofPattern("yyyy-MM-dd");
        String formattedStartDate = startDate.format(formatterQuery);
        String formattedEndDate = endDate.format(formatterQuery);

        //String message;
        JSONObject json = new JSONObject();
        JSONArray array = new JSONArray();
        LocalDateTime currentTime = LocalDateTime.now();
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");
        String formattedDateTime = currentTime.format(formatter);
        json.put("DateTimeGathered", formattedDateTime);
        json.put("QueryStartTime", formattedStartDate);
        json.put("QueryEndTime", formattedEndDate);

        YouTubeAnalytics.Reports.Query request = youtubeAnalyticsService.reports()
                .query();
        QueryResponse response = request.setDimensions("channel")
                .setIds("channel==MINE")
                .setMetrics("viewerPercentage")
                .setStartDate(formattedStartDate)  // Start date in the past
                .setEndDate(formattedEndDate)
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

            saveJsonObjectToFile(json, "InfoChannelDemographicLast30Days");
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
                .setMaxResults(5L) //amount of videos to get currently set to 2 during testing to save time
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
        int videoNumber = 1; //initial id
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

        // Define the directory path where you want to save the JSON file

        File directory = new File(EXTERNAL_JSON_DIRECTORY);

        // Create the directory if it doesn't exist
        if (!directory.exists()) {
            if (!directory.mkdirs()) {
                throw new IOException("Failed to create directory: " + EXTERNAL_JSON_DIRECTORY);
            }
        }

        // Write JSON object to file
        String filePath = EXTERNAL_JSON_DIRECTORY + fileName + ".json";
        try (FileWriter fileWriter = new FileWriter(filePath)) {
            ObjectWriter writer = mapper.writer().withDefaultPrettyPrinter();
            writer.writeValue(fileWriter, jsonObject);
            System.out.println("JSON object successfully saved to: " + filePath);
        }
    }
}
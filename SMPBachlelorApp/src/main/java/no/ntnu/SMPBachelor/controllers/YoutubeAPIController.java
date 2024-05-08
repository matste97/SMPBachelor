package no.ntnu.SMPBachelor.controllers;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.ObjectWriter;
import com.google.api.services.youtube.YouTube;
import com.google.api.services.youtube.model.SearchListResponse;
import com.google.api.services.youtube.model.SearchResult;
import com.google.api.services.youtubeAnalytics.v2.YouTubeAnalytics;
import com.google.api.services.youtubeAnalytics.v2.model.QueryResponse;
import io.swagger.v3.oas.annotations.tags.Tag;
import no.ntnu.SMPBachelor.security.YoutubeAuth;
import org.json.simple.JSONArray;
import org.json.simple.JSONObject;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;
import org.springframework.web.servlet.view.RedirectView;

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
import java.util.List;


@Controller
@Tag(name = "YoutubeAPIController", description = "Handles Youtube API")
public class YoutubeAPIController {
    private static final String EXTERNAL_JSON_DIRECTORY = Paths.get("").toAbsolutePath().getParent() + File.separator + "Data" + File.separator + "Json";

    private final ObjectMapper mapper = new ObjectMapper();

    /**
     *  Get the client secret upload form
     */

    @GetMapping("/upload")
    public String showUploadForm() {
        return "uploadForm";
    }
    /**
     * Client secret upload form, let's the user upload their client secret then redirect to authorization afterwards
     */
    @PostMapping("/upload")
    public String handleFileUpload(@RequestParam("file") MultipartFile file, RedirectAttributes redirectAttributes) {
        if (!file.isEmpty()) {
            try {
                // Get the file and save it
                byte[] bytes = file.getBytes();
                Path path = Paths.get(EXTERNAL_JSON_DIRECTORY + File.separator +  "client_secrets.json");
                Files.write(path, bytes);

                // Redirect to the authorization endpoint after successful upload
                return "redirect:/authorize";
            } catch (IOException e) {
                e.printStackTrace();
                // Handle the exception
                redirectAttributes.addFlashAttribute("error", "An error occurred while processing the file upload.");
            }
        } else {
            redirectAttributes.addFlashAttribute("message", "File is empty");
        }
        // Redirect back to the upload page if there's an error or if the file is empty
        return "redirect:/upload";
    }
    /**
     *  Get the youtube demographic mainpage, displays the info about latest videos and channel traffic.
     */

    @GetMapping("/youtube")
    public String youtubePage(Model model) {

        String channelDataAllTimePath = EXTERNAL_JSON_DIRECTORY + File.separator + "InfoChannelDemographicAllTime.json";
        String channel30DaysDataPath = EXTERNAL_JSON_DIRECTORY + File.separator + "InfoChannelDemographicLast30Days.json";
        String videosLatestPath = EXTERNAL_JSON_DIRECTORY + File.separator + "InfoLatestVideos.json";

        // Read JSON files and send them to the model
        try {
            String channelDataAllTimeJson = new String(Files.readAllBytes(Paths.get(channelDataAllTimePath)));
            String channel30DaysDataJson = new String(Files.readAllBytes(Paths.get(channel30DaysDataPath)));
            String videosLatestJson = new String(Files.readAllBytes(Paths.get(videosLatestPath)));

            model.addAttribute("channelDataAllTime", channelDataAllTimeJson);
            model.addAttribute("channel30DaysData", channel30DaysDataJson);
            model.addAttribute("videosLatest", videosLatestJson);
        } catch (IOException e) {
            e.printStackTrace();
            model.addAttribute("errorMessage", "Error reading JSON files");
            return "Youtube";
        }

        // Return the Thymeleaf template
        return "Youtube";
    }

    /**
     *  Run queries and save data to json files
     */
    @GetMapping("/updateJsonAndRefresh")
    @ResponseBody
    public ResponseEntity<?> updateJsonAndRefresh(@RequestParam("startDate") String startDate,
                                                         @RequestParam("endDate") String endDate) throws GeneralSecurityException {
        try {
            if ((getChannelGenderAgeDemographicWithinTimePeriod(startDate, endDate) == null) ||
                    (getChannelGenderAgeDemographicAllTime() == null)) {
                return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                        .body("Feil med authentication, kontakt admin for autentisering.");
            }
            saveLatestVideosInfoToJSON();
            return ResponseEntity.ok("Data updated successfully");
        } catch (IOException e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(e.getMessage());
        }
    }

    /**
     *  Run query to get the Gender and Age Groups for the channel all time.
     */

    public String getChannelGenderAgeDemographicAllTime() throws IOException {
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
        YouTubeAnalytics youTubeAnalytics = YoutubeAuth.getAnalyticsService();
        if(youTubeAnalytics == null){
            return null;
        }
        YouTubeAnalytics.Reports.Query request = youTubeAnalytics.reports()
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
            YouTubeAnalytics.Reports.Query totalViewsQuery = youTubeAnalytics.reports()
                    .query();
            QueryResponse totalViewsResponse = totalViewsQuery.setMetrics("views")
                    .setIds("channel==MINE")
                    .setStartDate("2000-01-01")  // Start date in the past
                    .setEndDate(endDate)
                    .execute();
            List<List<Object>> totalViewsRows = totalViewsResponse.getRows();
            BigDecimal totalViews;
            if (totalViewsRows != null && totalViewsRows.size() > 0) {
                totalViews = (BigDecimal) totalViewsRows.get(0).get(0);
                json.put("totalViews", totalViews);
            }
            json.put("channelDemographic", array);

            saveJsonObjectToFile(json, "InfoChannelDemographicAllTime");
        }
        return "Finished saving";
    }

    /**
     *  Run query to get the Gender and Age Groups for the channel for a specific time period
     */

    public String getChannelGenderAgeDemographicWithinTimePeriod(String startDate, String endDate) throws IOException {

        //String message;
        JSONObject json = new JSONObject();
        JSONArray array = new JSONArray();
        LocalDateTime currentTime = LocalDateTime.now();
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");
        String formattedDateTime = currentTime.format(formatter);
        json.put("DateTimeGathered", formattedDateTime);
        json.put("QueryStartTime", startDate);
        json.put("QueryEndTime", endDate);
        // Assuming you have obtained the credential through the authorization process
        YouTubeAnalytics youTubeAnalyticsService = YoutubeAuth.getAnalyticsService();
        if(youTubeAnalyticsService == null){
            return null;
        }

        YouTubeAnalytics.Reports.Query request = youTubeAnalyticsService.reports().query();
        QueryResponse response = request.setDimensions("channel")
                .setIds("channel==MINE")
                .setMetrics("viewerPercentage")
                .setStartDate(startDate)  // Start date 30 days in the past
                .setEndDate(endDate) //end date today
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
        return "Finished saving.";
    }

    public List<String> getAllVideoIds() throws IOException{

        List<String> videoIds = new ArrayList<>();

        String nextPageToken = "";
        while (nextPageToken != null) {
            YouTube youTubeService = YoutubeAuth.getService();
            if (youTubeService == null){
                return null;
            }

            YouTube.Search.List request = youTubeService.search()
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

    public List<String> getLatestVideoIds() throws IOException{

        List<String> videoIds = new ArrayList<>();

        YouTube youTubeService = YoutubeAuth.getService();
        if (youTubeService == null){
            return null;
        }

        YouTube.Search.List request = youTubeService.search()
                .list("snippet")
                .setForMine(true)
                .setMaxResults(5L) //amount of videos to get currently set to 5 during testing to save time
                .setType("video")
                .setOrder("date");

        SearchListResponse response = request.execute();
        List<SearchResult> items = response.getItems();

        for (SearchResult item : items) {
            videoIds.add(item.getId().getVideoId());
        }

        return videoIds;
    }

    /**
     *  Run query to get the Gender and Age Groups for video
     */

    public JSONArray getVideoAgeAndGenderData(String videoId) throws IOException {
        LocalDate endDate = LocalDate.now();
        // Format dates to match the required format for the query
        DateTimeFormatter formatterQuery = DateTimeFormatter.ofPattern("yyyy-MM-dd");
        String formattedEndDate = endDate.format(formatterQuery);

        JSONArray array = new JSONArray();

        YouTubeAnalytics youTubeAnalyticsService = YoutubeAuth.getAnalyticsService();
        if(youTubeAnalyticsService == null){
            return null;
        }

        YouTubeAnalytics.Reports.Query request = youTubeAnalyticsService.reports().query();
        QueryResponse response = request.setDimensions("channel")
                .setIds("channel==MINE")
                .setStartDate("2000-01-01") //all time by setting date way in the past
                .setEndDate(formattedEndDate)
                .setMetrics("viewerPercentage")
                .setDimensions("ageGroup,gender")
                .setFilters("video==" + videoId)
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
            return array;
        }


        return array;
    }

    /**
     *  Run query to get video title
     */

    public String getVideoTitle(String videoId) throws IOException{
        YouTube youTubeService = YoutubeAuth.getService();
        if (youTubeService == null){
            return null;
        }
        return youTubeService.videos()
                .list("snippet")
                .setId(videoId)
                .execute()
                .getItems()
                .get(0)
                .getSnippet()
                .getTitle();
    }

    /**
     *  Run query to get a video's total views
     */

    public BigInteger getVideoTotalViews(String videoId) throws IOException{
        YouTube youTubeService = YoutubeAuth.getService();
        if (youTubeService == null){
            return null;
        }

        return youTubeService.videos()
                .list("statistics")
                .setId(videoId)
                .execute()
                .getItems()
                .get(0)
                .getStatistics()
                .getViewCount();
}

    /**
     *  Run query to get an url to the video's thumbnail
     */

    public String getThumbnailUrl(String videoId) throws IOException{
        YouTube youTubeService = YoutubeAuth.getService();
        if (youTubeService == null){
            return null;
        }

        return youTubeService.videos()
                .list("snippet")
                .setId(videoId)
                .execute()
                .getItems()
                .get(0)
                .getSnippet()
                .getThumbnails().getMedium().getUrl();
    }
    /**
     *  Run query to get a video's average view time in seconds and percentage
     */
    public JSONArray getAvgViewTime(String videoId) throws IOException{
        LocalDate endDate = LocalDate.now();
        // Format dates to match the required format for the query
        DateTimeFormatter formatterQuery = DateTimeFormatter.ofPattern("yyyy-MM-dd");
        String formattedEndDate = endDate.format(formatterQuery);

        JSONArray array = new JSONArray();

        YouTubeAnalytics youTubeAnalyticsService = YoutubeAuth.getAnalyticsService();
        if(youTubeAnalyticsService == null){
            return null;
        }
        YouTubeAnalytics.Reports.Query request = youTubeAnalyticsService.reports()
                .query();
        QueryResponse response = request.setDimensions("video")
                .setEndDate(formattedEndDate)
                .setFilters("video=="+videoId)
                .setIds("channel==MINE")
                .setMetrics("averageViewDuration,averageViewPercentage")
                .setStartDate("2001-01-01")
                .execute();
        List<List<Object>> rows = response.getRows();
        if (rows != null) {
            for (List<Object> row : rows) {
                BigDecimal avgViewDuration = (BigDecimal) row.get(1);
                BigDecimal averageViewPercentage = (BigDecimal) row.get(2);

                JSONObject item = new JSONObject();
                item.put("avgViewDurationinSec", avgViewDuration);
                item.put("averageViewPercentage", averageViewPercentage);
                array.add(item);
            }
        }
        return array;
    }

    /**
     *  Run queries to get information about the latest videos
     */

    public void saveLatestVideosInfoToJSON() throws IOException, GeneralSecurityException {
        JSONObject json = new JSONObject();
        JSONArray array = new JSONArray();
        LocalDateTime currentTime = LocalDateTime.now();
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");
        String formattedDateTime = currentTime.format(formatter);
        json.put("DateTimeGathered", formattedDateTime);
        int videoNumber = 1; //initial id
        List<String> videoIDList = getLatestVideoIds();//videos to get info for
        for (String videoID:videoIDList) {
                String videoTitle = getVideoTitle(videoID);
                BigInteger videoViews = getVideoTotalViews(videoID);
                JSONArray videoData = getVideoAgeAndGenderData(videoID);
                JSONArray averageViews = getAvgViewTime(videoID);
                JSONObject item = new JSONObject();
                item.put("videoId", videoID);
                item.put("videoTitle", videoTitle);
                item.put("totalVideoViews", videoViews);
                item.put("videoDemographic", videoData);
                item.put("avgView", averageViews);
                item.put("thumbnail", getThumbnailUrl(videoID));

                array.add(item);
                json.put(videoNumber, item);
                videoNumber++;
        }
        saveJsonObjectToFile(json, "InfoLatestVideos");
    }

    /**
     *  Save a json object to a file
     */

    public void saveJsonObjectToFile(JSONObject jsonObject, String fileName) throws IOException {

        // Define the directory path
        File directory = new File(EXTERNAL_JSON_DIRECTORY);

        // Create the directory if it doesn't exist
        if (!directory.exists()) {
            if (!directory.mkdirs()) {
                throw new IOException("Failed to create directory: " + EXTERNAL_JSON_DIRECTORY);
            }
        }
        // Write JSON object to file
        String filePath = EXTERNAL_JSON_DIRECTORY + File.separator + fileName + ".json";
        try (FileWriter fileWriter = new FileWriter(filePath)) {
            ObjectWriter writer = mapper.writer().withDefaultPrettyPrinter();
            writer.writeValue(fileWriter, jsonObject);
            System.out.println("JSON object successfully saved to: " + filePath);
        }
    }
    /**
     *  Get mapping for authorization, reads the authorization url for google from the client secrets file.
     *  Redirects to upload clients secrets if file does not exist
     */

    @GetMapping("/authorize")
    public RedirectView authorizeInBrowser() throws IOException {
        if (YoutubeAuth.secretExists()) {
            String authorizationUrl = YoutubeAuth.getAuthorizationUrl();
            return new RedirectView(authorizationUrl);
        } else {
            // Redirect to upload if token doesn't exist
            return new RedirectView("/upload");
        }
    }

    /**
     *  Get mapping for callback from authorization, authorizes youtube api calls
     */

    @GetMapping("/Callback")
    public String callback(@RequestParam("code") String code) throws IOException{
        YoutubeAuth youtubeAuth = new YoutubeAuth();
        youtubeAuth.authorize(code);
        return "redirect:/youtube";
    }
}
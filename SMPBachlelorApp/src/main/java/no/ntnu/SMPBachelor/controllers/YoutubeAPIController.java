package no.ntnu.SMPBachelor.controllers;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.ObjectWriter;
import com.google.api.client.auth.oauth2.Credential;
import com.google.api.services.youtube.YouTube;
import com.google.api.services.youtube.model.SearchListResponse;
import com.google.api.services.youtube.model.SearchResult;
import com.google.api.services.youtubeAnalytics.v2.YouTubeAnalytics;
import com.google.api.services.youtubeAnalytics.v2.model.QueryResponse;
import io.swagger.v3.oas.annotations.tags.Tag;
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

    @GetMapping("/upload")
    public String showUploadForm() {
        return "uploadForm";
    }
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


    @GetMapping("/updateJsonAndRefresh")
    @ResponseBody
    public String updateJsonAndRefresh(Model model) {
        try {
            getChannelGenderAgeDemographicLast30Days();
            saveLatestVideosInfoToJSON();
            getChannelGenderAgeDemographicAllTime();
            return "JSON files updated successfully";
        } catch (IOException | GeneralSecurityException e) {
            e.printStackTrace();
            model.addAttribute("errorMessage", "Error occurred: " + e.getMessage());
            throw new RuntimeException(e);
        }
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
        YouTubeAnalytics youTubeAnalytics = YoutubeAuth.getAnalyticsService();
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
        // Assuming you have obtained the credential through the authorization process
        YouTubeAnalytics youTubeAnalyticsService = YoutubeAuth.getAnalyticsService();

        YouTubeAnalytics.Reports.Query request = youTubeAnalyticsService.reports().query();
        QueryResponse response = request.setDimensions("channel")
                .setIds("channel==MINE")
                .setMetrics("viewerPercentage")
                .setStartDate(formattedStartDate)  // Start date 30 days in the past
                .setEndDate(formattedEndDate) //end date today
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

    public List<String> getAllVideoIds() throws IOException, GeneralSecurityException {

        List<String> videoIds = new ArrayList<>();

        String nextPageToken = "";
        while (nextPageToken != null) {
            YouTube youTubeService = YoutubeAuth.getService();
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

    public JSONArray getVideoAgeAndGenderData(String videoId) throws IOException {
        LocalDate endDate = LocalDate.now();
        // Format dates to match the required format for the query
        DateTimeFormatter formatterQuery = DateTimeFormatter.ofPattern("yyyy-MM-dd");
        String formattedEndDate = endDate.format(formatterQuery);

        JSONArray array = new JSONArray();

        YouTubeAnalytics youTubeAnalyticsService = YoutubeAuth.getAnalyticsService();

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
                item.put("Age Group", ageGroup);
                item.put("Gender", gender);
                item.put("Viewer Percentage", viewerPercentage);
                array.add(item);
            }
            return array;
        }


        return array;
    }

    public String getVideoTitle(String videoId) throws IOException{
        YouTube youTubeService = YoutubeAuth.getService();
        return youTubeService.videos()
                .list("snippet")
                .setId(videoId)
                .execute()
                .getItems()
                .get(0)
                .getSnippet()
                .getTitle();
    }

    public BigInteger getVideoTotalViews(String videoId) throws IOException{
        YouTube youTubeService = YoutubeAuth.getService();
        return youTubeService.videos()
                .list("statistics")
                .setId(videoId)
                .execute()
                .getItems()
                .get(0)
                .getStatistics()
                .getViewCount();
}

    public void saveLatestVideosInfoToJSON() throws IOException, GeneralSecurityException {
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
        String filePath = EXTERNAL_JSON_DIRECTORY + File.separator + fileName + ".json";
        try (FileWriter fileWriter = new FileWriter(filePath)) {
            ObjectWriter writer = mapper.writer().withDefaultPrettyPrinter();
            writer.writeValue(fileWriter, jsonObject);
            System.out.println("JSON object successfully saved to: " + filePath);
        }
    }


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

    @GetMapping("/Callback")
    public String callback(@RequestParam("code") String code) throws IOException{
        YoutubeAuth youtubeAuth = new YoutubeAuth();
        Credential credential = youtubeAuth.authorize(code);

        // Save obtained credentials for future use
        youtubeAuth.saveCredentials(credential);
        return "redirect:/";
    }
}
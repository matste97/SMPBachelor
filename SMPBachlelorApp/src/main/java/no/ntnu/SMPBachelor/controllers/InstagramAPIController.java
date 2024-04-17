package no.ntnu.SMPBachelor.controllers;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.client.RestTemplate;

import io.swagger.v3.oas.annotations.tags.Tag;

@Controller
@Tag(name = "InstagramAPIController", description = "Handles Instagram API")
public class InstagramAPIController {

    @GetMapping("/demographics")
    public String getViewerDemographics() {
        String accessToken = " ";
        String accountId = "sunnmoersposten";
        String url = "https://graph.facebook.com/v12.0/" + accountId + "/insights?metric=audience_gender_age&access_token=" + accessToken;

        RestTemplate restTemplate = new RestTemplate();
        String response = restTemplate.getForObject(url, String.class);

        return response;
    }
}

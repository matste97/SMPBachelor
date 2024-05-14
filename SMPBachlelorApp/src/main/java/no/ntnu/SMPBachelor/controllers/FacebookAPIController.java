package no.ntnu.SMPBachelor.controllers;

import io.swagger.v3.oas.annotations.Operation;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;

@Controller
public class FacebookAPIController {
    @GetMapping("facebook")
    @Operation(
            summary = "Show facebook page",
            description = "Displays the facebook page"
    )
    public String facebook(Model model) {

        String apiKey = System.getenv("Facebook_Access_Token");
        model.addAttribute("apiKey", apiKey);
        return "facebook";
    }
}

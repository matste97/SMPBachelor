package no.ntnu.SMPBachelor.controllers;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
/**

Dummy controller that just takes care of some endpoints until proper conctollers are implemented

 DELETE LATER

 **/
@Controller
@Tag(name = "DummyController", description = "just temporary class so direct to end points")
public class DummyController {

    @GetMapping("tiktok")
    @Operation(
            summary = "Show tiktok page",
            description = "Displays the tiktok page"
    )
    public String tiktok(Model model) {
        return "tiktok";
    }




    @GetMapping("facebook")
    @Operation(
            summary = "Show facebook page",
            description = "Displays the facebook page"
    )
    public String facebook(Model model) {
        return "facebook";
    }

    @GetMapping("snapchat")
    @Operation(
            summary = "Show snapchat page",
            description = "Displays the snapchat page"
    )
    public String snapchat(Model model) {
        return "snapchat";
    }


}

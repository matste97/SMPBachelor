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
        return "no-access";
    }



    @GetMapping("instagram")
    @Operation(
            summary = "Show insta page",
            description = "Displays the insta page"
    )
    public String insta(Model model) {
        return "no-access";
    }


}

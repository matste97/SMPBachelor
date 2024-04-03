package no.ntnu.SMPBachelor.controllers;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;

@Controller
@Tag(name = "AdminController", description = "Handles admin operations")
public class AdminController {

    @GetMapping("admin")
    @Operation(
            summary = "Show admin page",
            description = "Displays the admin page"
    )
    public String adminPage(Model model) {
        return "admin";
    }


}

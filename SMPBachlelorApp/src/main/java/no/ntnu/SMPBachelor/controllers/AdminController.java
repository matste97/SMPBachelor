package no.ntnu.SMPBachelor.controllers;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import no.ntnu.SMPBachelor.repositories.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;

@Controller
@Tag(name = "AdminController", description = "Handles admin operations")
public class AdminController {

    @Autowired
    private UserRepository userRepository; // Assuming you have UserRepository autowired

    @GetMapping("admin")
    @Operation(
            summary = "Show admin page",
            description = "Displays the admin page"
    )
    public String adminPage(Model model) {
        model.addAttribute("users",userRepository.findAllUsers());
        return "adminPage";
    }

    @PostMapping("/deleteUser")
    public String deleteUser(@RequestParam("username") String username) {
        userRepository.findByUsername(username).ifPresent(userRepository::delete);
        return "redirect:/admin";
    }




}

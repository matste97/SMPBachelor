package no.ntnu.SMPBachelor.controllers;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import no.ntnu.SMPBachelor.dto.SignupDto;
import no.ntnu.SMPBachelor.service.AccessUserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PostMapping;
@Controller
@Tag(name = "AuthenticationController", description = "Takes care of user registration and authentication")
public class AuthenticationController {

    @Autowired
    private AccessUserService userService;


    @GetMapping("/login")
    @Operation(
            summary = "Show login form",
            description = "Displays the login form"
    )
    public String loginForm() {
        return "login";
    }

    @PostMapping("/login")
    public String handleLogin() {

        return "redirect:/";
    }

    /**
     * Sign-up form
     *
     * @return NAme of Thymeleaf template to use
     */
    @GetMapping("/signup")
    @Operation(
            summary = "Show sign-up form",
            description = "Displays the sign-up form"
    )
    public String signupForm() {
        return "signUp";
    }

    /**
     * This method processes data received from the sign-up form (HTTP POST)
     *
     * @return NAme of the template for the result page
     */
    @PostMapping("/signup")
    @Operation(
            summary = "Process sign-up form",
            description = "Processes the data received from the sign-up form (HTTP POST)"
    )
    public String signupProcess(@ModelAttribute SignupDto signupData, Model model) {
        model.addAttribute("signupData", signupData);
        if (!signupData.getPassword().equals(signupData.getRepeat())) {
            model.addAttribute("errorMessage", "Passwords do not match");
            return "signUp";
        }
        String errorMessage = userService.tryCreateNewUser(signupData.getUsername(), signupData.getPassword());
        if (errorMessage == null) {
            model.addAttribute("username", signupData.getUsername());
            return "signUpSuccess";
        } else {
            model.addAttribute("errorMessage", errorMessage);
            return "signUp";
        }
    }
}

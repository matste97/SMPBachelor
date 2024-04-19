package no.ntnu.SMPBachelor.controllers;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import no.ntnu.SMPBachelor.repositories.UserRepository;
import no.ntnu.SMPBachelor.service.AccessUserService;
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
    private UserRepository userRepository;

    @Autowired
    private AccessUserService accessUserService;

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


    @PostMapping("/admin/change-user-password")
    public String changeUserPassword(@RequestParam("username") String username,
                                                @RequestParam("newPassword") String newPassword,Model model) {
        if (accessUserService.changeUserPassword(username, newPassword)) {
            model.addAttribute("username", username);
            return "passwordChangeSuccess";
        } else {
            model.addAttribute("errorMessage", "Password must be at least 6 characters");
            model.addAttribute("username", username);
            return "adminPasswordChange";
        }
    }

    @GetMapping("/admin/change-user-password")
    public String showChangePasswordForm(@RequestParam("username") String username, Model model) {
        model.addAttribute("username", username);
        return "adminPasswordChange";
    }
}

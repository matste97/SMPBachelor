package no.ntnu.SMPBachelor.controllers;

import io.swagger.v3.oas.annotations.tags.Tag;
import no.ntnu.SMPBachelor.service.AccessUserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
@Controller
@Tag(name = "UserController", description = "Handles user-related operations")
public class UserController {
    @Autowired
    private AccessUserService userService;

}

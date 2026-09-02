package com.music.app.controller;

import com.music.app.entity.User;
import com.music.app.enums.Role;
import com.music.app.exception.DataAccessException;
import com.music.app.forms.RegisterForm;
import com.music.app.service.interfaces.AuthService;
import com.music.app.service.interfaces.UserService;
import jakarta.validation.Valid;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.sql.SQLException;

@Controller
public class AuthController {

    private final UserService userService;
    private final PasswordEncoder passwordEncoder;
    private final AuthService authService;

    public AuthController(UserService userService,  PasswordEncoder passwordEncoder,  AuthService authService) {
        this.userService = userService;
        this.passwordEncoder = passwordEncoder;
        this.authService = authService;
    }

@GetMapping("/register")
    public String register(Model model, RegisterForm registerForm) {

        model.addAttribute("form", registerForm);

        return "register";
}

@PostMapping("/register")
    public String register(@Valid @ModelAttribute("form") RegisterForm registerForm, BindingResult bindingResult, Model model, RedirectAttributes redirectAttributes) {

        if (bindingResult.hasErrors()) {
            model.addAttribute("message", "Your form contains errors");
            return "register";
        }

    if (authService.usernameExists(registerForm.getUserName())) {
        model.addAttribute("message", "Username is already taken");
        return "register";
    }

    if (authService.emailExists(registerForm.getEmail())) {
        model.addAttribute("message", "Email is already taken");
        return "register";
    }

    if (authService.phoneExists(registerForm.getPhoneNumber())) {
        model.addAttribute("message", "Phone number is already taken");
        return "register";
    }

    if(!registerForm.getPassword().equals(registerForm.getConfirmPassword())) {
        model.addAttribute("passwordNotEqual",
                "Passwords do not match");

        model.addAttribute("registerForm", registerForm);

        return "register";
    }

        try {
            User user = new User();
            user.setUserName(registerForm.getUserName());
            user.setEmail(registerForm.getEmail());
            user.setPhoneNumber(registerForm.getPhoneNumber());
            user.setPassword(passwordEncoder.encode(registerForm.getPassword()));
            user.setRole(Role.CLIENT);

            System.out.println("Controller: about to call userService.addUser()");

            userService.addUser(user);

            redirectAttributes.addFlashAttribute("successMessage", "User has been successfully registered");
            return "redirect:/login";

        } catch (Exception e) {
            model.addAttribute("error", e.getMessage());
            return "register";
        }
}

@GetMapping("/login")
    public String login(Model model, RedirectAttributes redirectAttributes) {
        return "login";
}

}

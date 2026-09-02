package com.music.app.controller;

import com.music.app.entity.User;
import com.music.app.forms.EditProfileForm;
import com.music.app.forms.RegisterForm;
import com.music.app.service.interfaces.AuthService;
import com.music.app.service.interfaces.SongService;
import com.music.app.service.interfaces.UserService;
import jakarta.validation.Valid;
import org.springframework.security.core.Authentication;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.validation.ObjectError;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.sql.SQLException;
import java.util.stream.Collectors;

@Controller
public class ProfileController {

    private final UserService userService;
    private final SongService songService;
    private final AuthService authService;
    public ProfileController(UserService userService, SongService songService,  AuthService authService) {
        this.userService = userService;
        this.songService = songService;
        this.authService = authService;
    }

    @GetMapping ("/profile")
    public String profile(Authentication auth, Model model) throws SQLException {

    User user = userService.getUserByName(auth.getName());
    model.addAttribute("user", user);
    model.addAttribute("email", user.getEmail());
    model.addAttribute("phone", user.getPhoneNumber());

    return "profile";
    }

    @GetMapping("/edit-profile")
    public String editProfile(Authentication auth, Model model) throws SQLException {

        User user = userService.getUserByName(auth.getName());

        if (user == null) {
            return "redirect:/login";
        }

        EditProfileForm form = new EditProfileForm();
        form.setUserName(user.getUserName());
        form.setEmail(user.getEmail());
        form.setPhone(user.getPhoneNumber());

        model.addAttribute("form", form);

        return "edit-profile";
    }

    @PostMapping("/edit-profile")
    public String editProfile(
            Authentication auth,
            @Valid @ModelAttribute("form") EditProfileForm form,
            BindingResult bindingResult,
            Model model,
            RedirectAttributes redirectAttributes) throws SQLException {

        if (bindingResult.hasErrors()) {
            return "edit-profile";
        }

        User user = userService.getUserByName(auth.getName());

        if (user == null) {
            return "redirect:/login";
        }

        // Username validation
        if (!user.getUserName().equals(form.getUserName())
                && authService.usernameExists(form.getUserName())) {

            model.addAttribute("form", form);
            model.addAttribute(
                    "usernameExists",
                    "A user with username '" + form.getUserName() + "' already exists."
            );

            return "edit-profile";
        }

        // Phone validation
        if (!user.getPhoneNumber().equals(form.getPhone())
                && authService.phoneExists(form.getPhone())) {

            model.addAttribute("form", form);
            model.addAttribute(
                    "phoneExists",
                    "A user with phone number '" + form.getPhone() + "' already exists."
            );

            return "edit-profile";
        }

        // Email validation
        if (!user.getEmail().equals(form.getEmail())
                && authService.emailExists(form.getEmail())) {

            model.addAttribute("form", form);
            model.addAttribute(
                    "emailExists",
                    "A user with email '" + form.getEmail() + "' already exists."
            );

            return "edit-profile";
        }

        user.setUserName(form.getUserName());
        user.setEmail(form.getEmail());
        user.setPhoneNumber(form.getPhone());

        userService.editProfile(user);

        redirectAttributes.addFlashAttribute(
                "successMessage",
                "Profile updated successfully. Please log in again."
        );

        return "redirect:/login";
    }
}

package com.birds.bird_app.controller;

import com.birds.bird_app.model.UserEntity;
import com.birds.bird_app.model.UserSettings;
import com.birds.bird_app.service.UserSettingsService;
import com.birds.bird_app.service.UserService;
import jakarta.servlet.http.HttpSession;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

@Controller
@RequestMapping("/users")
public class UserSettingsController {

    @Autowired
    private UserSettingsService userSettingsService;

    @Autowired
    private UserService userService;

    @GetMapping("/settings")
    public String getSettingsPage(Model model, HttpSession session) {
        UserEntity user = (UserEntity) session.getAttribute("user");
        if (user == null) {
            return "redirect:/users/loginForm";
        }

        UserSettings settings = userSettingsService.getUserSettings(user.getId());
        model.addAttribute("user", user);
        model.addAttribute("settings", settings);
        return "users/settings";
    }

    @PostMapping("/update-settings")
    public String updateSettings(@RequestParam String theme,
                               @RequestParam(required = false) boolean emailNotifications,
                               @RequestParam(required = false) boolean isPublic,
                               HttpSession session,
                               RedirectAttributes redirectAttributes) {
        UserEntity user = (UserEntity) session.getAttribute("user");
        if (user == null) {
            return "redirect:/users/loginForm";
        }

        UserSettings settings = userSettingsService.getUserSettings(user.getId());
        settings.setTheme(theme);
        settings.setEmailNotifications(emailNotifications);
        settings.setIsPublic(isPublic);
        userSettingsService.updateUserSettings(settings);

        // Update session with new settings
        session.setAttribute("userSettings", settings);
        
        redirectAttributes.addFlashAttribute("successMessage", "Settings updated successfully!");
        return "redirect:/users/settings";
    }

    @PostMapping("/update-profile")
    public String updateProfile(@RequestParam String name,
                              @RequestParam String email,
                              @RequestParam(required = false) String currentPassword,
                              @RequestParam(required = false) String newPassword,
                              @RequestParam(required = false) MultipartFile profilePicture,
                              HttpSession session,
                              RedirectAttributes redirectAttributes) {
        UserEntity user = (UserEntity) session.getAttribute("user");
        if (user == null) {
            return "redirect:/users/loginForm";
        }

        try {
            userService.updateProfile(user.getId(), name, email, currentPassword, newPassword, profilePicture);
            // Update session with new user data
            session.setAttribute("user", userService.getUserById(user.getId()));
            redirectAttributes.addFlashAttribute("successMessage", "Profile updated successfully!");
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("errorMessage", e.getMessage());
        }

        return "redirect:/users/settings";
    }
} 
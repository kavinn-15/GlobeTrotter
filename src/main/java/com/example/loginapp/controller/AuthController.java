package com.example.loginapp.controller;

import com.example.loginapp.entity.User;
import com.example.loginapp.model.DestinationCard;
import com.example.loginapp.service.UserService;
import jakarta.servlet.http.HttpSession;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.util.StringUtils;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

/**
 * Spring MVC controller. The DispatcherServlet (itself a Servlet) routes
 * incoming HTTP requests to these @RequestMapping handler methods.
 */
@Controller
public class AuthController {

    private final UserService userService;

    private static final String UPLOAD_DIR = "uploads";

    @Autowired
    public AuthController(UserService userService) {
        this.userService = userService;
    }

    // ---------------------------------------------------------------
    // Screen 1: Login
    // ---------------------------------------------------------------

    @GetMapping({"/", "/login"})
    public String loginPage(Model model) {
        if (!model.containsAttribute("username")) {
            model.addAttribute("username", "");
        }
        return "login";
    }

    @PostMapping("/login")
    public String doLogin(@RequestParam String username,
                           @RequestParam String password,
                           HttpSession session,
                           Model model) {

        Optional<User> user = userService.authenticate(username, password);

        if (user.isPresent()) {
            session.setAttribute("loggedInUser", user.get());
            return "redirect:/home";
        }

        model.addAttribute("loginError", "Invalid username or password.");
        model.addAttribute("username", username);
        return "login";
    }

    @GetMapping("/logout")
    public String logout(HttpSession session) {
        session.invalidate();
        return "redirect:/login";
    }

    // ---------------------------------------------------------------
    // Screen 2: Registration
    // ---------------------------------------------------------------

    @GetMapping("/register")
    public String registerPage(Model model) {
        if (!model.containsAttribute("user")) {
            model.addAttribute("user", new User());
        }
        return "register";
    }

    @PostMapping("/register")
    public String doRegister(@Valid @ModelAttribute("user") User user,
                              BindingResult bindingResult,
                              @RequestParam(value = "photo", required = false) MultipartFile photo,
                              Model model,
                              RedirectAttributes redirectAttributes) {

        if (bindingResult.hasErrors()) {
            return "register";
        }

        if (userService.usernameExists(user.getUsername())) {
            model.addAttribute("registerError", "That username is already taken.");
            return "register";
        }

        if (userService.emailExists(user.getEmail())) {
            model.addAttribute("registerError", "That email is already registered.");
            return "register";
        }

        // Handle optional photo upload
        if (photo != null && !photo.isEmpty()) {
            try {
                String storedName = storePhoto(photo);
                user.setPhotoPath(storedName);
            } catch (IOException e) {
                model.addAttribute("registerError", "Could not save the uploaded photo. Please try again.");
                return "register";
            }
        }

        userService.register(user);

        redirectAttributes.addFlashAttribute("registerSuccess",
                "Registration successful! You can now log in as \"" + user.getUsername() + "\".");
        return "redirect:/login";
    }

    // ---------------------------------------------------------------
    // Landing page after a successful login
    // ---------------------------------------------------------------

    @GetMapping("/home")
    public String home(HttpSession session, Model model) {
        User loggedInUser = (User) session.getAttribute("loggedInUser");
        if (loggedInUser == null) {
            return "redirect:/login";
        }
        model.addAttribute("user", loggedInUser);
        model.addAttribute("regionalSelections", REGIONAL_SELECTIONS);
        model.addAttribute("previousTrips", PREVIOUS_TRIPS);
        return "home";
    }

    /**
     * Sample destination data used to render the image tiles on the
     * home screen ("Top Regional Selections" and "Previous Trips").
     * See {@link DestinationCard}.
     */
    private static final List<DestinationCard> REGIONAL_SELECTIONS = List.of(
            new DestinationCard("Paris", "France", "https://images.unsplash.com/photo-1502602898657-3e91760cbb34?w=300&h=300&fit=crop&q=80"),
            new DestinationCard("Santorini", "Greece", "https://images.unsplash.com/photo-1533105079780-92b9be482077?w=300&h=300&fit=crop&q=80"),
            new DestinationCard("Kyoto", "Japan", "https://images.unsplash.com/photo-1493976040374-85c8e12f0c0e?w=300&h=300&fit=crop&q=80"),
            new DestinationCard("Bali", "Indonesia", "https://images.unsplash.com/photo-1537996194471-e657df975ab4?w=300&h=300&fit=crop&q=80"),
            new DestinationCard("New York", "USA", "https://images.unsplash.com/photo-1496442226666-8d4d0e62e6e9?w=300&h=300&fit=crop&q=80")
    );

    private static final List<DestinationCard> PREVIOUS_TRIPS = List.of(
            new DestinationCard("Rome", "Sep 2025 · 6 days", "https://images.unsplash.com/photo-1552832230-c0197dd311b5?w=400&h=400&fit=crop&q=80"),
            new DestinationCard("Dubai", "Jan 2026 · 4 days", "https://images.unsplash.com/photo-1512453979798-5ea266f8880c?w=400&h=400&fit=crop&q=80"),
            new DestinationCard("London", "Mar 2026 · 5 days", "https://images.unsplash.com/photo-1513635269975-59663e0ac1ad?w=400&h=400&fit=crop&q=80")
    );

    // ---------------------------------------------------------------
    // Screen 7: User Profile Page
    // ---------------------------------------------------------------

    @GetMapping("/profile")
    public String profile(HttpSession session, Model model) {
        User loggedInUser = (User) session.getAttribute("loggedInUser");
        if (loggedInUser == null) {
            return "redirect:/login";
        }
        model.addAttribute("user", loggedInUser);
        return "profile";
    }

    // ---------------------------------------------------------------
    // Screen 7b: Edit Profile
    // ---------------------------------------------------------------

    @GetMapping("/profile/edit")
    public String editProfilePage(HttpSession session, Model model) {
        User loggedInUser = (User) session.getAttribute("loggedInUser");
        if (loggedInUser == null) {
            return "redirect:/login";
        }
        if (!model.containsAttribute("user")) {
            model.addAttribute("user", loggedInUser);
        }
        return "edit-profile";
    }

    @PostMapping("/profile/edit")
    public String doEditProfile(@RequestParam String firstName,
                                 @RequestParam String lastName,
                                 @RequestParam String email,
                                 @RequestParam(required = false) String phoneNumber,
                                 @RequestParam(required = false) String city,
                                 @RequestParam(required = false) String country,
                                 @RequestParam(required = false) String additionalInfo,
                                 @RequestParam(value = "photo", required = false) MultipartFile photo,
                                 HttpSession session,
                                 Model model,
                                 RedirectAttributes redirectAttributes) {

        User loggedInUser = (User) session.getAttribute("loggedInUser");
        if (loggedInUser == null) {
            return "redirect:/login";
        }

        if (!StringUtils.hasText(firstName) || !StringUtils.hasText(lastName) || !StringUtils.hasText(email)) {
            model.addAttribute("editError", "First name, last name and email are required.");
            model.addAttribute("user", loggedInUser);
            return "edit-profile";
        }

        if (!email.equals(loggedInUser.getEmail()) && userService.emailExists(email)) {
            model.addAttribute("editError", "That email is already registered to another account.");
            model.addAttribute("user", loggedInUser);
            return "edit-profile";
        }

        String photoPath = loggedInUser.getPhotoPath();
        if (photo != null && !photo.isEmpty()) {
            try {
                photoPath = storePhoto(photo);
            } catch (IOException e) {
                model.addAttribute("editError", "Could not save the uploaded photo. Please try again.");
                model.addAttribute("user", loggedInUser);
                return "edit-profile";
            }
        }

        User updated = userService.updateProfile(loggedInUser.getId(), firstName.trim(), lastName.trim(),
                email.trim(), phoneNumber, city, country, additionalInfo, photoPath);

        // Keep the session copy in sync so the rest of the app (topbar,
        // profile page, etc.) reflects the change immediately.
        session.setAttribute("loggedInUser", updated);

        redirectAttributes.addFlashAttribute("profileSuccess", "Profile updated successfully.");
        return "redirect:/profile";
    }

    // ---------------------------------------------------------------
    // Helpers
    // ---------------------------------------------------------------

    private String storePhoto(MultipartFile photo) throws IOException {
        Path uploadPath = Paths.get(UPLOAD_DIR);
        if (!Files.exists(uploadPath)) {
            Files.createDirectories(uploadPath);
        }

        String original = StringUtils.cleanPath(photo.getOriginalFilename() == null ? "photo" : photo.getOriginalFilename());
        String extension = "";
        int dot = original.lastIndexOf('.');
        if (dot >= 0) {
            extension = original.substring(dot);
        }
        String storedName = UUID.randomUUID() + extension;

        Path target = uploadPath.resolve(storedName);
        photo.transferTo(target);

        return storedName;
    }
}

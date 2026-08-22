package com.example.loginapp.controller;

import com.example.loginapp.entity.User;
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
        return "home";
    }

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

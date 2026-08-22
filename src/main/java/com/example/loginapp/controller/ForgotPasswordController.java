package com.example.loginapp.controller;

import com.example.loginapp.entity.User;
import com.example.loginapp.service.UserService;
import jakarta.servlet.http.HttpSession;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.util.Optional;

/**
 * Handles the "Forgot Password" flow:
 *   1) /forgot-password  — user enters their registered email
 *   2) /verify-otp        — user enters the 6-digit code emailed to them
 *   3) /reset-password    — user sets a new password
 *
 * The in-progress email address is kept in the HttpSession (never exposed
 * in a URL or hidden form field) so the flow can't be hijacked by simply
 * guessing/editing a parameter; the actual OTP correctness and expiry are
 * still re-checked against the database on every step in UserService.
 */
@Controller
public class ForgotPasswordController {

    private static final String SESSION_OTP_EMAIL = "otpEmail";

    private final UserService userService;

    @Value("${app.otp.expiry-minutes:10}")
    private int otpExpiryMinutes;

    @Autowired
    public ForgotPasswordController(UserService userService) {
        this.userService = userService;
    }

    // ---------------------------------------------------------------
    // Step 1: request an OTP
    // ---------------------------------------------------------------

    @GetMapping("/forgot-password")
    public String forgotPasswordPage() {
        return "forgot-password";
    }

    @PostMapping("/forgot-password")
    public String sendOtp(@RequestParam String email,
                           HttpSession session,
                           Model model,
                           RedirectAttributes redirectAttributes) {

        Optional<User> maybeUser = userService.findByEmail(email.trim());

        if (maybeUser.isEmpty()) {
            model.addAttribute("forgotError", "We couldn't find an account with that email address.");
            return "forgot-password";
        }

        User user = maybeUser.get();
        userService.generateAndSendOtp(user);

        session.setAttribute(SESSION_OTP_EMAIL, user.getEmail());

        redirectAttributes.addFlashAttribute("otpSentMessage",
                "We've emailed a 6-digit code to " + maskEmail(user.getEmail()) + ".");
        return "redirect:/verify-otp";
    }

    // ---------------------------------------------------------------
    // Step 2: verify the OTP
    // ---------------------------------------------------------------

    @GetMapping("/verify-otp")
    public String verifyOtpPage(HttpSession session, Model model) {
        String email = (String) session.getAttribute(SESSION_OTP_EMAIL);
        if (email == null) {
            return "redirect:/forgot-password";
        }
        model.addAttribute("maskedEmail", maskEmail(email));
        model.addAttribute("otpExpiryMinutes", otpExpiryMinutes);
        return "verify-otp";
    }

    @PostMapping("/verify-otp")
    public String verifyOtp(@RequestParam String otp,
                             HttpSession session,
                             Model model,
                             RedirectAttributes redirectAttributes) {

        String email = (String) session.getAttribute(SESSION_OTP_EMAIL);
        if (email == null) {
            return "redirect:/forgot-password";
        }

        UserService.OtpCheckResult result = userService.verifyOtp(email, otp.trim());

        switch (result) {
            case SUCCESS:
                redirectAttributes.addFlashAttribute("otpVerifiedMessage", "Code verified — choose your new password.");
                return "redirect:/reset-password";

            case EXPIRED:
                model.addAttribute("otpError", "That code has expired. Please request a new one.");
                model.addAttribute("otpExpired", true);
                break;

            case TOO_MANY_ATTEMPTS:
                session.removeAttribute(SESSION_OTP_EMAIL);
                model.addAttribute("otpError", "Too many incorrect attempts. Please start over.");
                return "redirect:/forgot-password";

            case NO_REQUEST:
                session.removeAttribute(SESSION_OTP_EMAIL);
                return "redirect:/forgot-password";

            case INCORRECT:
            default:
                model.addAttribute("otpError", "That code isn't right. Please try again.");
                break;
        }

        model.addAttribute("maskedEmail", maskEmail(email));
        model.addAttribute("otpExpiryMinutes", otpExpiryMinutes);
        return "verify-otp";
    }

    @PostMapping("/verify-otp/resend")
    public String resendOtp(HttpSession session, RedirectAttributes redirectAttributes) {
        String email = (String) session.getAttribute(SESSION_OTP_EMAIL);
        if (email == null) {
            return "redirect:/forgot-password";
        }

        userService.findByEmail(email).ifPresent(userService::generateAndSendOtp);

        redirectAttributes.addFlashAttribute("otpSentMessage", "We've sent a new code to " + maskEmail(email) + ".");
        return "redirect:/verify-otp";
    }

    // ---------------------------------------------------------------
    // Step 3: set a new password
    // ---------------------------------------------------------------

    @GetMapping("/reset-password")
    public String resetPasswordPage(HttpSession session, Model model) {
        String email = (String) session.getAttribute(SESSION_OTP_EMAIL);
        if (email == null) {
            return "redirect:/forgot-password";
        }

        Optional<User> maybeUser = userService.findByEmail(email);
        if (maybeUser.isEmpty() || !maybeUser.get().isResetVerified()) {
            // They haven't completed OTP verification yet — send them back.
            return "redirect:/verify-otp";
        }

        return "reset-password";
    }

    @PostMapping("/reset-password")
    public String resetPassword(@RequestParam String password,
                                 @RequestParam String confirmPassword,
                                 HttpSession session,
                                 Model model,
                                 RedirectAttributes redirectAttributes) {

        String email = (String) session.getAttribute(SESSION_OTP_EMAIL);
        if (email == null) {
            return "redirect:/forgot-password";
        }

        if (password == null || password.length() < 4) {
            model.addAttribute("resetError", "Password must be at least 4 characters.");
            return "reset-password";
        }

        if (!password.equals(confirmPassword)) {
            model.addAttribute("resetError", "Passwords do not match.");
            return "reset-password";
        }

        boolean success = userService.resetPassword(email, password);
        session.removeAttribute(SESSION_OTP_EMAIL);

        if (!success) {
            redirectAttributes.addFlashAttribute("forgotError", "Your session expired. Please start over.");
            return "redirect:/forgot-password";
        }

        redirectAttributes.addFlashAttribute("registerSuccess", "Your password has been reset. Please log in.");
        return "redirect:/login";
    }

    // ---------------------------------------------------------------
    // Helpers
    // ---------------------------------------------------------------

    /** Turns "jane.doe@example.com" into "ja***@example.com" for display. */
    private String maskEmail(String email) {
        int at = email.indexOf('@');
        if (at <= 1) {
            return email;
        }
        String namePart = email.substring(0, at);
        String domainPart = email.substring(at);
        String visible = namePart.substring(0, Math.min(2, namePart.length()));
        return visible + "***" + domainPart;
    }
}

package com.example.loginapp.service;

import com.example.loginapp.entity.User;
import com.example.loginapp.repository.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.security.SecureRandom;
import java.time.LocalDateTime;
import java.util.Optional;

/**
 * Plain Spring-managed service bean (Spring Core / IoC container).
 * Contains the business logic for registering and authenticating users.
 *
 * NOTE: for clarity in this demo, passwords are stored as plain text.
 * In a real application, hash passwords (e.g. with BCryptPasswordEncoder)
 * before persisting them.
 */
@Service
public class UserService {

    private static final SecureRandom RANDOM = new SecureRandom();
    private static final int MAX_OTP_ATTEMPTS = 5;

    private final UserRepository userRepository;
    private final EmailService emailService;

    @Value("${app.otp.expiry-minutes:10}")
    private int otpExpiryMinutes;

    @Autowired
    public UserService(UserRepository userRepository, EmailService emailService) {
        this.userRepository = userRepository;
        this.emailService = emailService;
    }

    public boolean usernameExists(String username) {
        return userRepository.existsByUsername(username);
    }

    public boolean emailExists(String email) {
        return userRepository.existsByEmail(email);
    }

    public User register(User user) {
        return userRepository.save(user);
    }

    /**
     * Returns the user if the username/password combination is valid.
     */
    public Optional<User> authenticate(String username, String rawPassword) {
        return userRepository.findByUsername(username)
                .filter(u -> u.getPassword().equals(rawPassword));
    }

    // ---------------------------------------------------------------
    // Forgot Password / OTP flow
    // ---------------------------------------------------------------

    public Optional<User> findByEmail(String email) {
        return userRepository.findByEmail(email);
    }

    /**
     * Generates a fresh 6-digit OTP for the given user, stores it (with
     * expiry) on the user row, and emails it to their registered address.
     */
    public void generateAndSendOtp(User user) {
        String otp = String.format("%06d", RANDOM.nextInt(1_000_000));

        user.setResetOtp(otp);
        user.setResetOtpExpiry(LocalDateTime.now().plusMinutes(otpExpiryMinutes));
        user.setResetOtpAttempts(0);
        user.setResetVerified(false);
        userRepository.save(user);

        emailService.sendOtpEmail(user.getEmail(), user.getFirstName(), otp);
    }

    /**
     * Checks the submitted OTP against the one on file for this email.
     * Returns a result describing what happened so the controller can show
     * the right message (and re-send is only ever triggered explicitly by
     * the user, never automatically).
     */
    public OtpCheckResult verifyOtp(String email, String submittedOtp) {
        Optional<User> maybeUser = userRepository.findByEmail(email);
        if (maybeUser.isEmpty()) {
            return OtpCheckResult.NO_REQUEST;
        }

        User user = maybeUser.get();

        if (user.getResetOtp() == null || user.getResetOtpExpiry() == null) {
            return OtpCheckResult.NO_REQUEST;
        }

        if (LocalDateTime.now().isAfter(user.getResetOtpExpiry())) {
            clearOtp(user);
            return OtpCheckResult.EXPIRED;
        }

        if (user.getResetOtpAttempts() >= MAX_OTP_ATTEMPTS) {
            clearOtp(user);
            return OtpCheckResult.TOO_MANY_ATTEMPTS;
        }

        if (!user.getResetOtp().equals(submittedOtp)) {
            user.setResetOtpAttempts(user.getResetOtpAttempts() + 1);
            userRepository.save(user);
            return OtpCheckResult.INCORRECT;
        }

        // Correct: mark verified, clear the code itself (single use), but
        // keep resetVerified=true so the Reset Password step can proceed.
        user.setResetOtp(null);
        user.setResetVerified(true);
        userRepository.save(user);
        return OtpCheckResult.SUCCESS;
    }

    /**
     * Sets a new password for the given email, but only if that user has
     * a currently-verified OTP on file (i.e. they just completed the
     * Verify OTP step). Clears the verification flag afterwards either way.
     */
    public boolean resetPassword(String email, String newPassword) {
        Optional<User> maybeUser = userRepository.findByEmail(email);
        if (maybeUser.isEmpty()) {
            return false;
        }

        User user = maybeUser.get();
        if (!user.isResetVerified()) {
            return false;
        }

        user.setPassword(newPassword);
        clearOtp(user);
        userRepository.save(user);
        return true;
    }

    private void clearOtp(User user) {
        user.setResetOtp(null);
        user.setResetOtpExpiry(null);
        user.setResetOtpAttempts(0);
        user.setResetVerified(false);
        userRepository.save(user);
    }

    public enum OtpCheckResult {
        SUCCESS, INCORRECT, EXPIRED, TOO_MANY_ATTEMPTS, NO_REQUEST
    }
}

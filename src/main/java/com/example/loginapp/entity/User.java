package com.example.loginapp.entity;

import jakarta.persistence.*;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

import java.time.LocalDateTime;

/**
 * JPA entity mapped by Hibernate to the USERS table.
 * Backs both the Login screen (username / password) and the
 * Registration screen (personal details).
 */
@Entity
@Table(name = "users")
public class User {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @NotBlank(message = "Username is required")
    @Column(nullable = false, unique = true, length = 50)
    private String username;

    @NotBlank(message = "Password is required")
    @Size(min = 4, message = "Password must be at least 4 characters")
    @Column(nullable = false)
    private String password;

    @NotBlank(message = "First name is required")
    private String firstName;

    @NotBlank(message = "Last name is required")
    private String lastName;

    @NotBlank(message = "Email is required")
    @Email(message = "Email must be valid")
    @Column(unique = true)
    private String email;

    private String phoneNumber;

    private String city;

    private String country;

    @Column(length = 2000)
    private String additionalInfo;

    /** relative path (under /uploads) to the uploaded profile photo, may be null */
    private String photoPath;

    /**
     * Grants access to the Admin Panel (Screen 12). Defaults to false for
     * every new registration; promote a user by setting this column to true
     * directly in the database (e.g. {@code UPDATE users SET admin = true
     * WHERE username = '...'; }).
     */
    @Column(nullable = false)
    private boolean admin = false;

    /** Set automatically the first time this row is persisted. */
    private LocalDateTime createdAt;

    // ---------------------------------------------------------------
    // Forgot Password / OTP verification (Screens: Forgot Password,
    // Verify OTP, Reset Password). Not shown anywhere in the UI other
    // than driving that flow.
    // ---------------------------------------------------------------

    /** The most recently generated one-time code, cleared once used. */
    private String resetOtp;

    /** Moment after which {@link #resetOtp} is no longer valid. */
    private LocalDateTime resetOtpExpiry;

    /** Number of consecutive failed OTP attempts for the current code. */
    private int resetOtpAttempts = 0;

    /** True once the OTP has been correctly verified; required before
     *  a new password may be set, then cleared immediately after. */
    private boolean resetVerified = false;

    public User() {
    }

    @PrePersist
    protected void onCreate() {
        this.createdAt = LocalDateTime.now();
    }

    // ---------- Getters & Setters ----------

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getUsername() {
        return username;
    }

    public void setUsername(String username) {
        this.username = username;
    }

    public String getPassword() {
        return password;
    }

    public void setPassword(String password) {
        this.password = password;
    }

    public String getFirstName() {
        return firstName;
    }

    public void setFirstName(String firstName) {
        this.firstName = firstName;
    }

    public String getLastName() {
        return lastName;
    }

    public void setLastName(String lastName) {
        this.lastName = lastName;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public String getPhoneNumber() {
        return phoneNumber;
    }

    public void setPhoneNumber(String phoneNumber) {
        this.phoneNumber = phoneNumber;
    }

    public String getCity() {
        return city;
    }

    public void setCity(String city) {
        this.city = city;
    }

    public String getCountry() {
        return country;
    }

    public void setCountry(String country) {
        this.country = country;
    }

    public String getAdditionalInfo() {
        return additionalInfo;
    }

    public void setAdditionalInfo(String additionalInfo) {
        this.additionalInfo = additionalInfo;
    }

    public String getPhotoPath() {
        return photoPath;
    }

    public void setPhotoPath(String photoPath) {
        this.photoPath = photoPath;
    }

    public boolean isAdmin() {
        return admin;
    }

    public void setAdmin(boolean admin) {
        this.admin = admin;
    }

    public LocalDateTime getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(LocalDateTime createdAt) {
        this.createdAt = createdAt;
    }

    public String getResetOtp() {
        return resetOtp;
    }

    public void setResetOtp(String resetOtp) {
        this.resetOtp = resetOtp;
    }

    public LocalDateTime getResetOtpExpiry() {
        return resetOtpExpiry;
    }

    public void setResetOtpExpiry(LocalDateTime resetOtpExpiry) {
        this.resetOtpExpiry = resetOtpExpiry;
    }

    public int getResetOtpAttempts() {
        return resetOtpAttempts;
    }

    public void setResetOtpAttempts(int resetOtpAttempts) {
        this.resetOtpAttempts = resetOtpAttempts;
    }

    public boolean isResetVerified() {
        return resetVerified;
    }

    public void setResetVerified(boolean resetVerified) {
        this.resetVerified = resetVerified;
    }
}

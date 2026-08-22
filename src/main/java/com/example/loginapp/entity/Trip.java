package com.example.loginapp.entity;

import jakarta.persistence.*;
import jakarta.validation.constraints.NotBlank;

import java.time.LocalDate;
import java.time.LocalDateTime;

/**
 * JPA entity mapped by Hibernate to the TRIPS table. Created on Screen 4
 * (Create a new Trip), then read back by Screen 5 (Build Itinerary),
 * Screen 6 (User Trip Listing), Screen 9 (Itinerary View) and Screen 11
 * (Calendar View).
 */
@Entity
@Table(name = "trips")
public class Trip {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "owner_id", nullable = false)
    private User owner;

    @NotBlank(message = "Place is required")
    @Column(nullable = false)
    private String place;

    private LocalDate startDate;

    private LocalDate endDate;

    private LocalDateTime createdAt;

    public Trip() {
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

    public User getOwner() {
        return owner;
    }

    public void setOwner(User owner) {
        this.owner = owner;
    }

    public String getPlace() {
        return place;
    }

    public void setPlace(String place) {
        this.place = place;
    }

    public LocalDate getStartDate() {
        return startDate;
    }

    public void setStartDate(LocalDate startDate) {
        this.startDate = startDate;
    }

    public LocalDate getEndDate() {
        return endDate;
    }

    public void setEndDate(LocalDate endDate) {
        this.endDate = endDate;
    }

    public LocalDateTime getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(LocalDateTime createdAt) {
        this.createdAt = createdAt;
    }

    /**
     * Derived bucket used by Screen 6 (Ongoing / Up-coming / Completed) and
     * Screen 11's calendar highlighting. Not a persisted column.
     */
    @Transient
    public String getStatus() {
        if (startDate == null || endDate == null) {
            return "upcoming";
        }
        LocalDate today = LocalDate.now();
        if (today.isBefore(startDate)) {
            return "upcoming";
        }
        if (today.isAfter(endDate)) {
            return "completed";
        }
        return "ongoing";
    }
}

package com.example.loginapp.model;

/**
 * Simple, read-only view model for a single "thing to do" shown on Screen 8
 * (Activity Search). Not a JPA entity — just the sample display data plus
 * the fields the screen filters by (type, cost, duration).
 */
public class ActivityCard {

    private final String name;
    private final String city;
    private final String type;
    private final String cost;
    private final String duration;
    private final String description;
    private final String imageUrl;

    public ActivityCard(String name, String city, String type, String cost, String duration,
                         String description, String imageUrl) {
        this.name = name;
        this.city = city;
        this.type = type;
        this.cost = cost;
        this.duration = duration;
        this.description = description;
        this.imageUrl = imageUrl;
    }

    public String getName() {
        return name;
    }

    public String getCity() {
        return city;
    }

    public String getType() {
        return type;
    }

    public String getCost() {
        return cost;
    }

    public String getDuration() {
        return duration;
    }

    public String getDescription() {
        return description;
    }

    public String getImageUrl() {
        return imageUrl;
    }
}

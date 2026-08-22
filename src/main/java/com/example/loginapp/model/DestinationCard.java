package com.example.loginapp.model;

/**
 * Simple, read-only view model used to render the photo tiles that show
 * up in a few places across the app (home screen's "Top Regional
 * Selections" / "Previous Trips", and the "Suggestions for Places to
 * Visit" grid on the Create a Trip screen). It is intentionally not a
 * JPA entity — it's just a small bundle of display data.
 */
public class DestinationCard {

    private final String name;
    private final String subtitle;
    private final String imageUrl;

    public DestinationCard(String name, String subtitle, String imageUrl) {
        this.name = name;
        this.subtitle = subtitle;
        this.imageUrl = imageUrl;
    }

    public String getName() {
        return name;
    }

    public String getSubtitle() {
        return subtitle;
    }

    public String getImageUrl() {
        return imageUrl;
    }
}

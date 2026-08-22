package com.example.loginapp.controller;

import com.example.loginapp.entity.CommunityPost;
import com.example.loginapp.entity.Trip;
import com.example.loginapp.entity.User;
import com.example.loginapp.repository.CommunityPostRepository;
import com.example.loginapp.repository.TripRepository;
import com.example.loginapp.repository.UserRepository;
import jakarta.servlet.http.HttpSession;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;

import java.time.YearMonth;
import java.time.format.TextStyle;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.stream.Collectors;
import java.util.stream.Stream;

/**
 * Screen 12: Admin Panel. Only reachable by a {@link User} whose
 * {@code admin} flag is {@code true}. Provides the four sections shown in
 * the wireframe: Manage Users, Popular Cities, Popular Activities and
 * User Trends &amp; Analytics.
 */
@Controller
public class AdminController {

    private static final String LOGGED_IN_USER = "loggedInUser";

    private final UserRepository userRepository;
    private final TripRepository tripRepository;
    private final CommunityPostRepository communityPostRepository;

    @Autowired
    public AdminController(UserRepository userRepository,
                            TripRepository tripRepository,
                            CommunityPostRepository communityPostRepository) {
        this.userRepository = userRepository;
        this.tripRepository = tripRepository;
        this.communityPostRepository = communityPostRepository;
    }

    @GetMapping("/admin")
    public String adminPanel(@RequestParam(required = false, defaultValue = "manage-users") String section,
                              HttpSession session, Model model) {

        User loggedInUser = (User) session.getAttribute(LOGGED_IN_USER);
        if (loggedInUser == null) {
            return "redirect:/login";
        }
        if (!loggedInUser.isAdmin()) {
            return "redirect:/home";
        }

        List<User> allUsers = userRepository.findAll();
        List<Trip> allTrips = tripRepository.findAll();
        List<CommunityPost> allPosts = communityPostRepository.findAll();

        model.addAttribute("section", section);
        model.addAttribute("totalUsers", allUsers.size());
        model.addAttribute("totalTrips", allTrips.size());
        model.addAttribute("totalPosts", allPosts.size());
        model.addAttribute("users", allUsers);

        model.addAttribute("popularCities", topCounts(
                allTrips.stream()
                        .map(Trip::getPlace)
                        .filter(place -> place != null && !place.isBlank())));

        model.addAttribute("popularActivities", topCounts(
                allPosts.stream()
                        .map(CommunityPost::getActivityType)
                        .filter(activity -> activity != null && !activity.isBlank())));

        model.addAttribute("registrationTrend", registrationsByMonth(allUsers));

        return "admin";
    }

    // ---------------------------------------------------------------
    // Helpers
    // ---------------------------------------------------------------

    /**
     * Groups the given values by their text, keeps the top 5 by frequency,
     * and works out a 0-100 bar-width percentage relative to the most
     * frequent value so the template can render a simple CSS bar chart.
     */
    private List<CountItem> topCounts(Stream<String> values) {
        Map<String, Long> counted = values.collect(Collectors.groupingBy(v -> v, Collectors.counting()));

        long max = counted.values().stream().mapToLong(Long::longValue).max().orElse(1L);

        return counted.entrySet().stream()
                .sorted(Map.Entry.<String, Long>comparingByValue().reversed())
                .limit(5)
                .map(entry -> new CountItem(entry.getKey(), entry.getValue(),
                        (int) Math.round(entry.getValue() * 100.0 / max)))
                .collect(Collectors.toList());
    }

    /** Registrations for each of the last 6 months, oldest first. */
    private List<CountItem> registrationsByMonth(List<User> allUsers) {
        Map<YearMonth, Long> counted = new LinkedHashMap<>();
        YearMonth current = YearMonth.now();
        for (int i = 5; i >= 0; i--) {
            counted.put(current.minusMonths(i), 0L);
        }

        for (User user : allUsers) {
            if (user.getCreatedAt() == null) {
                continue;
            }
            YearMonth registeredIn = YearMonth.from(user.getCreatedAt());
            if (counted.containsKey(registeredIn)) {
                counted.put(registeredIn, counted.get(registeredIn) + 1);
            }
        }

        long max = counted.values().stream().mapToLong(Long::longValue).max().orElse(1L);
        if (max == 0L) {
            max = 1L;
        }

        List<CountItem> trend = new ArrayList<>();
        for (Map.Entry<YearMonth, Long> entry : counted.entrySet()) {
            String label = entry.getKey().getMonth().getDisplayName(TextStyle.SHORT, Locale.ENGLISH);
            trend.add(new CountItem(label, entry.getValue(), (int) Math.round(entry.getValue() * 100.0 / max)));
        }
        return trend;
    }

    /** View-model row for a simple CSS bar chart, used by the Thymeleaf template. */
    public static class CountItem {
        private final String label;
        private final long count;
        private final int percent;

        public CountItem(String label, long count, int percent) {
            this.label = label;
            this.count = count;
            this.percent = percent;
        }

        public String getLabel() {
            return label;
        }

        public long getCount() {
            return count;
        }

        public int getPercent() {
            return percent;
        }
    }
}

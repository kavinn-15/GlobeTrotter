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
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.time.YearMonth;
import java.time.format.TextStyle;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;
import java.util.stream.Stream;

/**
 * Screen 12: Admin / Analytics Dashboard. Only reachable by a {@link User}
 * whose {@code admin} flag is {@code true}. Gives the admin:
 * <ul>
 *     <li>Manage Users &ndash; a table of every account plus promote/demote
 *         and delete actions</li>
 *     <li>Popular Cities / Popular Activities &ndash; bar charts built from
 *         trip and community-post data</li>
 *     <li>Trips &ndash; a table of every trip created on the platform plus a
 *         status breakdown</li>
 *     <li>User Trends &amp; Analytics &ndash; registration and trip-creation
 *         trend charts, and engagement stats</li>
 * </ul>
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

        User loggedInUser = requireAdmin(session);
        if (loggedInUser == null) {
            return session.getAttribute(LOGGED_IN_USER) == null ? "redirect:/login" : "redirect:/home";
        }

        List<User> allUsers = userRepository.findAll();
        List<Trip> allTrips = tripRepository.findAllByOrderByCreatedAtDesc();
        List<CommunityPost> allPosts = communityPostRepository.findAll();

        Set<Long> ownersWithTrips = allTrips.stream()
                .map(trip -> trip.getOwner() == null ? null : trip.getOwner().getId())
                .filter(id -> id != null)
                .collect(Collectors.toSet());

        long ongoing = allTrips.stream().filter(t -> "ongoing".equals(t.getStatus())).count();
        long upcoming = allTrips.stream().filter(t -> "upcoming".equals(t.getStatus())).count();
        long completed = allTrips.stream().filter(t -> "completed".equals(t.getStatus())).count();
        double avgTripsPerUser = allUsers.isEmpty() ? 0.0 : (double) allTrips.size() / allUsers.size();

        model.addAttribute("section", section);
        model.addAttribute("currentAdminId", loggedInUser.getId());

        // Top-line stats
        model.addAttribute("totalUsers", allUsers.size());
        model.addAttribute("totalTrips", allTrips.size());
        model.addAttribute("totalPosts", allPosts.size());
        model.addAttribute("activeUsers", ownersWithTrips.size());
        model.addAttribute("avgTripsPerUser", String.format(Locale.ENGLISH, "%.1f", avgTripsPerUser));

        // Manage Users
        model.addAttribute("users", allUsers);

        // Popular cities / activities
        model.addAttribute("popularCities", topCounts(
                allTrips.stream()
                        .map(Trip::getPlace)
                        .filter(place -> place != null && !place.isBlank())));

        model.addAttribute("popularActivities", topCounts(
                allPosts.stream()
                        .map(CommunityPost::getActivityType)
                        .filter(activity -> activity != null && !activity.isBlank())));

        // Trips table + status breakdown
        model.addAttribute("allTrips", allTrips);
        model.addAttribute("ongoingCount", ongoing);
        model.addAttribute("upcomingCount", upcoming);
        model.addAttribute("completedCount", completed);

        // Trends & analytics
        model.addAttribute("registrationTrend", countsByMonth(
                allUsers.stream().map(User::getCreatedAt).filter(d -> d != null).map(YearMonth::from)));
        model.addAttribute("tripsTrend", countsByMonth(
                allTrips.stream().map(Trip::getCreatedAt).filter(d -> d != null).map(YearMonth::from)));

        return "admin";
    }

    // ---------------------------------------------------------------
    // User management actions
    // ---------------------------------------------------------------

    @PostMapping("/admin/users/{id}/toggle-admin")
    public String toggleAdmin(@PathVariable Long id, HttpSession session, RedirectAttributes redirectAttributes) {
        User loggedInUser = requireAdmin(session);
        if (loggedInUser == null) {
            return session.getAttribute(LOGGED_IN_USER) == null ? "redirect:/login" : "redirect:/home";
        }

        if (id.equals(loggedInUser.getId())) {
            redirectAttributes.addFlashAttribute("adminError", "You can't change your own admin status.");
            return "redirect:/admin?section=manage-users";
        }

        User target = userRepository.findById(id).orElse(null);
        if (target == null) {
            redirectAttributes.addFlashAttribute("adminError", "That user no longer exists.");
            return "redirect:/admin?section=manage-users";
        }

        target.setAdmin(!target.isAdmin());
        userRepository.save(target);
        redirectAttributes.addFlashAttribute("adminSuccess",
                "\"" + target.getUsername() + "\" is " + (target.isAdmin() ? "now an admin." : "no longer an admin."));
        return "redirect:/admin?section=manage-users";
    }

    @PostMapping("/admin/users/{id}/delete")
    public String deleteUser(@PathVariable Long id, HttpSession session, RedirectAttributes redirectAttributes) {
        User loggedInUser = requireAdmin(session);
        if (loggedInUser == null) {
            return session.getAttribute(LOGGED_IN_USER) == null ? "redirect:/login" : "redirect:/home";
        }

        if (id.equals(loggedInUser.getId())) {
            redirectAttributes.addFlashAttribute("adminError", "You can't delete your own account.");
            return "redirect:/admin?section=manage-users";
        }

        User target = userRepository.findById(id).orElse(null);
        if (target == null) {
            redirectAttributes.addFlashAttribute("adminError", "That user no longer exists.");
            return "redirect:/admin?section=manage-users";
        }

        // Clear out everything the user owns first so the delete doesn't
        // trip a foreign-key constraint on trips / community posts.
        tripRepository.deleteAll(tripRepository.findByOwnerOrderByStartDateDesc(target));
        communityPostRepository.deleteAll(
                communityPostRepository.findAll().stream()
                        .filter(post -> post.getAuthor() != null && id.equals(post.getAuthor().getId()))
                        .collect(Collectors.toList()));
        userRepository.delete(target);

        redirectAttributes.addFlashAttribute("adminSuccess", "\"" + target.getUsername() + "\" has been deleted.");
        return "redirect:/admin?section=manage-users";
    }

    // ---------------------------------------------------------------
    // Helpers
    // ---------------------------------------------------------------

    /** Returns the logged-in admin, or {@code null} if not logged in / not an admin. */
    private User requireAdmin(HttpSession session) {
        User loggedInUser = (User) session.getAttribute(LOGGED_IN_USER);
        if (loggedInUser == null || !loggedInUser.isAdmin()) {
            return null;
        }
        return loggedInUser;
    }

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

    /** Buckets the given year-months into counts for each of the last 6 months, oldest first. */
    private List<CountItem> countsByMonth(Stream<YearMonth> yearMonths) {
        Map<YearMonth, Long> counted = new LinkedHashMap<>();
        YearMonth current = YearMonth.now();
        for (int i = 5; i >= 0; i--) {
            counted.put(current.minusMonths(i), 0L);
        }

        yearMonths.forEach(ym -> {
            if (counted.containsKey(ym)) {
                counted.put(ym, counted.get(ym) + 1);
            }
        });

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

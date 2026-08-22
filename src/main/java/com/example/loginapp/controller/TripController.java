package com.example.loginapp.controller;

import com.example.loginapp.entity.Trip;
import com.example.loginapp.entity.User;
import com.example.loginapp.model.ActivityCard;
import com.example.loginapp.model.DestinationCard;
import com.example.loginapp.repository.TripRepository;
import com.example.loginapp.repository.UserRepository;
import jakarta.servlet.http.HttpSession;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.util.StringUtils;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;

import java.time.LocalDate;
import java.time.YearMonth;
import java.time.format.DateTimeFormatter;
import java.time.format.TextStyle;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Locale;
import java.util.function.Function;
import java.util.stream.Collectors;

/**
 * Handles the "Plan a trip" flow that starts on the Main Landing Page:
 * <p>
 * Screen 3 (Main Landing) --"+ Plan a trip"--&gt; Screen 4 (Create a new Trip)
 * --"Build Itinerary"--&gt; Screen 5 (Build Itinerary), plus the screens that
 * read the resulting trips back: Screen 6 (Trip Listing), Screen 9
 * (Itinerary View) and Screen 11 (Calendar View).
 */
@Controller
public class TripController {

    private static final String LOGGED_IN_USER = "loggedInUser";
    private static final DateTimeFormatter ISO_DATE = DateTimeFormatter.ISO_LOCAL_DATE;

    private final TripRepository tripRepository;
    private final UserRepository userRepository;

    @Autowired
    public TripController(TripRepository tripRepository, UserRepository userRepository) {
        this.tripRepository = tripRepository;
        this.userRepository = userRepository;
    }

    // ---------------------------------------------------------------
    // Screen 6: User Trip Listing
    // ---------------------------------------------------------------

    @GetMapping("/trips")
    public String tripListingPage(HttpSession session, Model model) {
        User loggedInUser = (User) session.getAttribute(LOGGED_IN_USER);
        if (loggedInUser == null) {
            return "redirect:/login";
        }

        List<Trip> trips = tripRepository.findByOwnerOrderByStartDateDesc(loggedInUser);

        List<Trip> ongoing = new ArrayList<>();
        List<Trip> upcoming = new ArrayList<>();
        List<Trip> completed = new ArrayList<>();

        for (Trip trip : trips) {
            switch (trip.getStatus()) {
                case "ongoing":
                    ongoing.add(trip);
                    break;
                case "completed":
                    completed.add(trip);
                    break;
                default:
                    upcoming.add(trip);
            }
        }

        model.addAttribute("ongoingTrips", ongoing);
        model.addAttribute("upcomingTrips", upcoming);
        model.addAttribute("completedTrips", completed);
        return "trip-listing";
    }

    // ---------------------------------------------------------------
    // Screen 4: Create a new Trip
    // ---------------------------------------------------------------

    @GetMapping("/trips/new")
    public String newTripPage(HttpSession session, Model model) {
        if (session.getAttribute(LOGGED_IN_USER) == null) {
            return "redirect:/login";
        }
        model.addAttribute("suggestedPlaces", SUGGESTED_PLACES);
        return "create-trip";
    }

    /**
     * Sample "places to visit / activities to perform" suggestions shown
     * on the Create a Trip screen. See {@link DestinationCard}.
     */
    private static final List<DestinationCard> SUGGESTED_PLACES = List.of(
            new DestinationCard("Machu Picchu", "Peru", "https://images.unsplash.com/photo-1526392060635-9d6019884377?w=300&h=300&fit=crop&q=80"),
            new DestinationCard("Great Barrier Reef", "Australia", "https://images.unsplash.com/photo-1544551763-46a013bb70d5?w=300&h=300&fit=crop&q=80"),
            new DestinationCard("Swiss Alps", "Switzerland", "https://images.unsplash.com/photo-1531366936337-7c912a4589a7?w=300&h=300&fit=crop&q=80"),
            new DestinationCard("Marrakech", "Morocco", "https://images.unsplash.com/photo-1489749798305-4fea3ae63d43?w=300&h=300&fit=crop&q=80"),
            new DestinationCard("Iceland Ring Road", "Iceland", "https://images.unsplash.com/photo-1504829857797-ddff29c27927?w=300&h=300&fit=crop&q=80"),
            new DestinationCard("Kyoto Temples", "Japan", "https://images.unsplash.com/photo-1493976040374-85c8e12f0c0e?w=300&h=300&fit=crop&q=80")
    );

    @PostMapping("/trips/new")
    public String createTrip(@RequestParam(required = false) String place,
                              @RequestParam(required = false) String startDate,
                              @RequestParam(required = false) String endDate,
                              HttpSession session) {

        User loggedInUser = (User) session.getAttribute(LOGGED_IN_USER);
        if (loggedInUser == null) {
            return "redirect:/login";
        }

        Trip trip = new Trip();
        trip.setOwner(userRepository.getReferenceById(loggedInUser.getId()));
        trip.setPlace(StringUtils.hasText(place) ? place.trim() : "Untitled trip");
        trip.setStartDate(parseDate(startDate));
        trip.setEndDate(parseDate(endDate));

        trip = tripRepository.save(trip);

        // Stash the trip on the session so Screen 5 (and Screen 9 as a
        // fallback) can read the basics back without a round trip to the DB.
        session.setAttribute("activeTripId", trip.getId());
        session.setAttribute("tripPlace", trip.getPlace());
        session.setAttribute("tripStartDate", startDate);
        session.setAttribute("tripEndDate", endDate);

        return "redirect:/trips/itinerary";
    }

    // ---------------------------------------------------------------
    // Screen 5: Build Itinerary
    // ---------------------------------------------------------------

    @GetMapping("/trips/itinerary")
    public String buildItineraryPage(HttpSession session, Model model) {
        if (session.getAttribute(LOGGED_IN_USER) == null) {
            return "redirect:/login";
        }

        model.addAttribute("tripPlace", session.getAttribute("tripPlace"));
        model.addAttribute("tripStartDate", session.getAttribute("tripStartDate"));
        model.addAttribute("tripEndDate", session.getAttribute("tripEndDate"));

        return "build-itinerary";
    }

    // ---------------------------------------------------------------
    // Screen 8: Activity Search / City Search
    // ---------------------------------------------------------------

    @GetMapping("/activities")
    public String activitySearchPage(@RequestParam(required = false) String query,
                                      @RequestParam(required = false) String type,
                                      @RequestParam(required = false) String cost,
                                      @RequestParam(required = false) String duration,
                                      HttpSession session, Model model) {
        if (session.getAttribute(LOGGED_IN_USER) == null) {
            return "redirect:/login";
        }

        String q = query == null ? "" : query.trim().toLowerCase(Locale.ENGLISH);

        List<ActivityCard> results = ACTIVITIES.stream()
                .filter(a -> q.isEmpty()
                        || a.getCity().toLowerCase(Locale.ENGLISH).contains(q)
                        || a.getName().toLowerCase(Locale.ENGLISH).contains(q)
                        || a.getType().toLowerCase(Locale.ENGLISH).contains(q))
                .filter(a -> !StringUtils.hasText(type) || a.getType().equals(type))
                .filter(a -> !StringUtils.hasText(cost) || a.getCost().equals(cost))
                .filter(a -> !StringUtils.hasText(duration) || a.getDuration().equals(duration))
                .sorted(Comparator.comparing(ActivityCard::getCity).thenComparing(ActivityCard::getName))
                .collect(Collectors.toList());

        model.addAttribute("query", query);
        model.addAttribute("selectedType", type);
        model.addAttribute("selectedCost", cost);
        model.addAttribute("selectedDuration", duration);
        model.addAttribute("results", results);

        model.addAttribute("activityTypes", distinct(ActivityCard::getType));
        model.addAttribute("activityCosts", distinct(ActivityCard::getCost));
        model.addAttribute("activityDurations", distinct(ActivityCard::getDuration));

        return "activity-search";
    }

    /**
     * Sample "things to do" shown on the Activity Search screen, browsable
     * by city, filterable by type / cost / duration. Currently seeded for
     * the first three Top Regional Selections on the home screen (Paris,
     * Santorini, Kyoto) — other cities searched here simply return no
     * results, same as an empty search.
     */
    private static final List<ActivityCard> ACTIVITIES = List.of(
            new ActivityCard("Eiffel Tower Summit Tour", "Paris", "Sightseeing", "$$", "2 hours",
                    "Skip the line and ride to the top for panoramic views over the city.",
                    "https://images.unsplash.com/photo-1543349689-9a4d426bee8e?w=400&h=300&fit=crop&q=80"),
            new ActivityCard("Louvre Museum Guided Walk", "Paris", "Culture", "$$", "3 hours",
                    "A guided highlights tour past the Mona Lisa, the Venus de Milo and more.",
                    "https://images.unsplash.com/photo-1499856871958-5b9627545d1a?w=400&h=300&fit=crop&q=80"),
            new ActivityCard("Seine River Dinner Cruise", "Paris", "Food Tour", "$$$", "2.5 hours",
                    "A relaxed evening cruise past the city's landmarks with a multi-course dinner.",
                    "https://images.unsplash.com/photo-1520939817895-152ee6f9a7b8?w=400&h=300&fit=crop&q=80"),
            new ActivityCard("Montmartre Food & Wine Walk", "Paris", "Food Tour", "$$", "3 hours",
                    "Wander the cobbled streets of Montmartre tasting cheese, pastries and wine.",
                    "https://images.unsplash.com/photo-1550340499-a6c60fc8287c?w=400&h=300&fit=crop&q=80"),

            new ActivityCard("Oia Sunset Catamaran Cruise", "Santorini", "Adventure", "$$$", "Half day",
                    "Sail the caldera, stop for a swim, and watch the famous sunset from the water.",
                    "https://images.unsplash.com/photo-1613395877344-13d4a8e0d49e?w=400&h=300&fit=crop&q=80"),
            new ActivityCard("Fira to Oia Caldera Hike", "Santorini", "Sightseeing", "$", "3 hours",
                    "A scenic clifftop walk between the island's two most iconic villages.",
                    "https://images.unsplash.com/photo-1570077188670-e3a8d69ac5ff?w=400&h=300&fit=crop&q=80"),
            new ActivityCard("Santorini Wine Tasting Tour", "Santorini", "Food Tour", "$$", "2 hours",
                    "Sample volcanic-soil wines at a family-run vineyard overlooking the sea.",
                    "https://images.unsplash.com/photo-1506377247377-2a5b3b417ebb?w=400&h=300&fit=crop&q=80"),
            new ActivityCard("Akrotiri Archaeological Site", "Santorini", "Culture", "$", "1.5 hours",
                    "Explore the remarkably preserved Bronze Age ruins buried by a volcanic eruption.",
                    "https://images.unsplash.com/photo-1601581987809-a874a81309e9?w=400&h=300&fit=crop&q=80"),

            new ActivityCard("Fushimi Inari Shrine Walk", "Kyoto", "Sightseeing", "Free", "2 hours",
                    "Walk beneath thousands of vermillion torii gates winding up the mountainside.",
                    "https://images.unsplash.com/photo-1478436127897-769e1b3f0f36?w=400&h=300&fit=crop&q=80"),
            new ActivityCard("Traditional Tea Ceremony", "Kyoto", "Culture", "$$", "1 hour",
                    "Learn the etiquette of matcha preparation from a tea master in a quiet tearoom.",
                    "https://images.unsplash.com/photo-1545048702-79362596cdc9?w=400&h=300&fit=crop&q=80"),
            new ActivityCard("Arashiyama Bamboo Grove & Kimono Walk", "Kyoto", "Sightseeing", "$", "2 hours",
                    "Stroll the towering bamboo grove, optionally dressed in a rented kimono.",
                    "https://images.unsplash.com/photo-1490806843957-31f4c9a91c65?w=400&h=300&fit=crop&q=80"),
            new ActivityCard("Nishiki Market Food Tour", "Kyoto", "Food Tour", "$$", "2.5 hours",
                    "Graze your way through 'Kyoto's Kitchen' with a guide who knows every stall.",
                    "https://images.unsplash.com/photo-1554797589-7241bb691973?w=400&h=300&fit=crop&q=80")
    );

    private List<String> distinct(Function<ActivityCard, String> extractor) {
        return ACTIVITIES.stream()
                .map(extractor)
                .collect(Collectors.toCollection(LinkedHashSet::new))
                .stream()
                .sorted()
                .collect(Collectors.toList());
    }

    // ---------------------------------------------------------------
    // Screen 9: Itinerary View with budget section
    // ---------------------------------------------------------------

    @GetMapping("/trips/view")
    public String itineraryViewPage(@RequestParam(required = false) String option,
                                     @RequestParam(required = false) Long tripId,
                                     HttpSession session, Model model) {
        if (session.getAttribute(LOGGED_IN_USER) == null) {
            return "redirect:/login";
        }

        String place = (String) session.getAttribute("tripPlace");
        if (tripId != null) {
            Trip trip = tripRepository.findById(tripId).orElse(null);
            if (trip != null) {
                place = trip.getPlace();
            }
        }

        model.addAttribute("selectedOption", option);
        model.addAttribute("tripPlace", place);
        return "itinerary-view";
    }

    // ---------------------------------------------------------------
    // Screen 11: Calendar View
    // ---------------------------------------------------------------

    @GetMapping("/calendar")
    public String calendarView(@RequestParam(required = false) Integer year,
                                @RequestParam(required = false) Integer month,
                                HttpSession session, Model model) {

        User loggedInUser = (User) session.getAttribute(LOGGED_IN_USER);
        if (loggedInUser == null) {
            return "redirect:/login";
        }

        YearMonth current = (year != null && month != null)
                ? YearMonth.of(year, month)
                : YearMonth.now();

        List<Trip> trips = tripRepository.findByOwnerOrderByStartDateDesc(loggedInUser);

        LocalDate firstOfMonth = current.atDay(1);
        // DayOfWeek.getValue() is Mon=1..Sun=7; rotate so the week starts on Sunday.
        int leadingBlanks = firstOfMonth.getDayOfWeek().getValue() % 7;
        int daysInMonth = current.lengthOfMonth();

        List<CalendarDay> days = new ArrayList<>();
        for (int i = 0; i < leadingBlanks; i++) {
            days.add(new CalendarDay(0, null));
        }
        for (int day = 1; day <= daysInMonth; day++) {
            LocalDate date = current.atDay(day);
            List<String> tripsOnDay = new ArrayList<>();
            for (Trip trip : trips) {
                if (trip.getStartDate() != null && trip.getEndDate() != null
                        && !date.isBefore(trip.getStartDate()) && !date.isAfter(trip.getEndDate())) {
                    tripsOnDay.add(trip.getPlace());
                }
            }
            days.add(new CalendarDay(day, tripsOnDay));
        }

        String monthLabel = current.getMonth().getDisplayName(TextStyle.FULL, Locale.ENGLISH) + " " + current.getYear();

        YearMonth previous = current.minusMonths(1);
        YearMonth next = current.plusMonths(1);

        model.addAttribute("monthLabel", monthLabel);
        model.addAttribute("days", days);
        model.addAttribute("prevYear", previous.getYear());
        model.addAttribute("prevMonth", previous.getMonthValue());
        model.addAttribute("nextYear", next.getYear());
        model.addAttribute("nextMonth", next.getMonthValue());
        return "calendar";
    }

    // ---------------------------------------------------------------
    // Helpers
    // ---------------------------------------------------------------

    private LocalDate parseDate(String raw) {
        if (!StringUtils.hasText(raw)) {
            return null;
        }
        try {
            return LocalDate.parse(raw, ISO_DATE);
        } catch (Exception e) {
            return null;
        }
    }

    /** View-model for a single calendar grid cell, used by the Thymeleaf template. */
    public static class CalendarDay {
        private final int dayOfMonth;
        private final List<String> trips;

        public CalendarDay(int dayOfMonth, List<String> trips) {
            this.dayOfMonth = dayOfMonth;
            this.trips = trips;
        }

        public int getDayOfMonth() {
            return dayOfMonth;
        }

        public List<String> getTrips() {
            return trips;
        }

        public boolean isBlank() {
            return dayOfMonth == 0;
        }

        public boolean isHasTrips() {
            return trips != null && !trips.isEmpty();
        }
    }
}

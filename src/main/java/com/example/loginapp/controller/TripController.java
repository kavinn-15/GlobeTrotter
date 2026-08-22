package com.example.loginapp.controller;

import com.example.loginapp.entity.Trip;
import com.example.loginapp.entity.User;
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
import java.util.List;
import java.util.Locale;

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
    public String newTripPage(HttpSession session) {
        if (session.getAttribute(LOGGED_IN_USER) == null) {
            return "redirect:/login";
        }
        return "create-trip";
    }

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
                                      HttpSession session, Model model) {
        if (session.getAttribute(LOGGED_IN_USER) == null) {
            return "redirect:/login";
        }

        model.addAttribute("query", query);
        return "activity-search";
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

package com.example.loginapp.controller;

import jakarta.servlet.http.HttpSession;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;

/**
 * Handles the "Plan a trip" flow that starts on the Main Landing Page:
 * <p>
 * Screen 3 (Main Landing) --"+ Plan a trip"--&gt; Screen 4 (Create a new Trip)
 * --"Build Itinerary"--&gt; Screen 5 (Build Itinerary)
 */
@Controller
public class TripController {

    private static final String LOGGED_IN_USER = "loggedInUser";

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
    public String createTrip(@RequestParam(required = false) String searchStartDate,
                              @RequestParam(required = false) String place,
                              @RequestParam(required = false) String startDate,
                              @RequestParam(required = false) String endDate,
                              HttpSession session) {

        if (session.getAttribute(LOGGED_IN_USER) == null) {
            return "redirect:/login";
        }

        // Stash the basic trip details on the session so a later step
        // (or Screen 5 itself, once it needs them) can read them back.
        session.setAttribute("tripPlace", place);
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
}

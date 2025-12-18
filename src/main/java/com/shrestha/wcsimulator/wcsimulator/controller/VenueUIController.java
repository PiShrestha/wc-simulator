package com.shrestha.wcsimulator.wcsimulator.controller;

import com.shrestha.wcsimulator.wcsimulator.service.VenueQueryService;
import com.shrestha.wcsimulator.wcsimulator.service.VenueTeamsService;
import com.shrestha.wcsimulator.wcsimulator.service.VenuePairingsService;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@Controller
public class VenueUIController {

    private final VenueQueryService venueQueryService;
    private final VenueTeamsService venueTeamsService;
    private final VenuePairingsService venuePairingsService;

    public VenueUIController(VenueQueryService venueQueryService, VenueTeamsService venueTeamsService, VenuePairingsService venuePairingsService) {
        this.venueQueryService = venueQueryService;
        this.venueTeamsService = venueTeamsService;
        this.venuePairingsService = venuePairingsService;
    }

    @GetMapping("/")
    public String home(Model model) {
        model.addAttribute("venues", List.of(
            "Philadelphia",
            "Los Angeles",
            "Dallas",
            "Boston",
            "Houston",
            "Miami",
            "New Jersey",
            "Seattle",
            "Mexico City",
            "Atlanta",
            "Vancouver",
            "Kansas City",
            "Monterrey",
            "Toronto",
            "San Francisco"
        ));
        return "index";
    }

    @GetMapping("/venue")
    public String venueMatches(@RequestParam String city, Model model) {
        model.addAttribute("city", city);
        model.addAttribute("matches",
                venueQueryService.getMatchesForVenue(city));
        // Add possible teams for the venue (MVP path-resolution)
        model.addAttribute("teams", venueTeamsService.teamsForVenue(city));
        // Add possible pairings for each match hosted at the venue
        model.addAttribute("pairings", venuePairingsService.pairingsForVenue(city));
        return "venue";
    }
}


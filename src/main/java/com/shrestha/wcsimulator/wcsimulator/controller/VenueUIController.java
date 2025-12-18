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
    public String venueMatches(@RequestParam String city,
                               @RequestParam(required = false) String stage,
                               Model model) {
        var allMatches = venueQueryService.getMatchesForVenue(city);
        var allPairings = venuePairingsService.pairingsForVenue(city);

        List<com.shrestha.wcsimulator.wcsimulator.dto.MatchView> matches = allMatches;
        List<com.shrestha.wcsimulator.wcsimulator.dto.PairingView> pairings = allPairings;

        if (stage != null && !stage.isBlank()) {
            matches = allMatches.stream()
                    .filter(m -> stage.equalsIgnoreCase(m.getStage()))
                    .collect(java.util.stream.Collectors.toList());
            pairings = allPairings.stream()
                    .filter(p -> stage.equalsIgnoreCase(p.getStage()))
                    .collect(java.util.stream.Collectors.toList());
        }

        model.addAttribute("city", city);
        model.addAttribute("stageFilter", stage == null ? "" : stage);
        model.addAttribute("matches", matches);
        model.addAttribute("teams", venueTeamsService.teamsForVenue(city));
        model.addAttribute("pairings", pairings);
        return "venue";
    }
}


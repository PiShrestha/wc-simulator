package com.shrestha.wcsimulator.wcsimulator.controller;

import com.shrestha.wcsimulator.wcsimulator.service.TicketService;
import com.shrestha.wcsimulator.wcsimulator.service.TicketService.TicketMatch;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;

import java.util.List;

@Controller
public class TicketController {

    private final TicketService ticketService;

    public TicketController(TicketService ticketService) {
        this.ticketService = ticketService;
    }

    @GetMapping("/tickets")
    public String tickets(@RequestParam(name = "venue", required = false) String venue, Model model) {
        List<String> venues = ticketService.venues();
        String selectedVenue = (venue == null || venue.isBlank()) && !venues.isEmpty() ? venues.get(0) : venue;
        List<TicketMatch> matches = ticketService.allMatches();

        model.addAttribute("venues", venues);
        model.addAttribute("selectedVenue", selectedVenue);
        model.addAttribute("stageOrder", ticketService.stageOrder());
        model.addAttribute("matches", matches);
        return "tickets";
    }
}

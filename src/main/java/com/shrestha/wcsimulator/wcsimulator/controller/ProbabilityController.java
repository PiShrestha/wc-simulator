package com.shrestha.wcsimulator.wcsimulator.controller;

import com.shrestha.wcsimulator.wcsimulator.service.ProbabilityService;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/probability")
public class ProbabilityController {

    private final ProbabilityService service;

    public ProbabilityController(ProbabilityService service) {
        this.service = service;
    }

    @GetMapping
    public double probability(
            @RequestParam String team,
            @RequestParam String city) {
        return service.probabilityTeamPlaysInCity(team, city);
    }
}

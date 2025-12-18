package com.shrestha.wcsimulator.wcsimulator.controller;

import com.shrestha.wcsimulator.wcsimulator.dto.PairingView;
import com.shrestha.wcsimulator.wcsimulator.provider.KnockoutStageProvider;
import com.shrestha.wcsimulator.wcsimulator.service.ProjectionService;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;

import java.util.*;
import java.util.stream.Collectors;

@Controller
public class SimulatorController {

    private final ProjectionService projectionService;
    private final KnockoutStageProvider knockoutProvider;

    public SimulatorController(ProjectionService projectionService, KnockoutStageProvider knockoutProvider) {
        this.projectionService = projectionService;
        this.knockoutProvider = knockoutProvider;
    }

    @GetMapping("/simulator")
    public String simulator(Model model) {
        Map<String, List<String>> ordered = projectionService.defaultOrderedGroups();
        model.addAttribute("groups", ordered);
        model.addAttribute("cities", cities());
        model.addAttribute("selectedCity", "Philadelphia");
        model.addAttribute("projected", List.of());
        return "simulator";
    }

    @PostMapping("/simulator")
    public String runSimulator(@RequestParam Map<String, String> params, Model model) {
        String city = params.getOrDefault("city", "New Jersey");
        Map<String, List<String>> ordered = projectionService.defaultOrderedGroups();
        // Parse group orders from form fields like order_A = "Team1,Team2,Team3,Team4"
        for (String g : ordered.keySet()) {
            String key = "order_" + g;
            if (params.containsKey(key)) {
                String csv = params.get(key);
                List<String> list = Arrays.stream(csv.split(","))
                        .map(String::trim)
                        .filter(s -> !s.isEmpty())
                        .collect(Collectors.toList());
                if (!list.isEmpty()) ordered.put(g, list);
            }
        }

        List<PairingView> projected;
        if ("All Cities".equalsIgnoreCase(city)) {
            // Get all matches across all cities
            projected = knockoutProvider.matches().stream()
                    .map(m -> projectionService.toPairingView(m, ordered))
                    .filter(Objects::nonNull)
                    .sorted((p1, p2) -> {
                        int so1 = stageOrder(p1.getStage());
                        int so2 = stageOrder(p2.getStage());
                        int cmp = Integer.compare(so1, so2);
                        if (cmp != 0) return cmp;
                        return Integer.compare(p1.getMatchId(), p2.getMatchId());
                    })
                    .collect(Collectors.toList());
        } else {
            projected = projectionService.projectedPairingsForVenue(city, ordered);
        }
        model.addAttribute("groups", ordered);
        model.addAttribute("cities", cities());
        model.addAttribute("selectedCity", city);
        model.addAttribute("projected", projected);
        return "simulator";
    }

    private List<String> cities() {
        List<String> allCities = knockoutProvider.matches().stream()
                .map(m -> m.getCity())
                .distinct()
                .sorted()
                .collect(Collectors.toList());
        // Add "All Cities" at the beginning
        List<String> result = new ArrayList<>(allCities);
        result.add(0, "All Cities");
        return result;
    }

    private int stageOrder(String stage) {
        if (stage == null) return 99;
        switch (stage) {
            case "Round of 32": return 1;
            case "Round of 16": return 2;
            case "Quarterfinal": return 3;
            case "Semifinal": return 4;
            case "Third Place": return 5;
            case "Final": return 6;
            default: return 98;
        }
    }
}

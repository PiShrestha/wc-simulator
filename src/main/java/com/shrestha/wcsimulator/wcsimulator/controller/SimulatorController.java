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
        String city = params.getOrDefault("city", "Philadelphia");
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

        List<PairingView> projected = projectionService.projectedPairingsForVenue(city, ordered);
        model.addAttribute("groups", ordered);
        model.addAttribute("cities", cities());
        model.addAttribute("selectedCity", city);
        model.addAttribute("projected", projected);
        return "simulator";
    }

    private List<String> cities() {
        return knockoutProvider.matches().stream()
                .map(m -> m.getCity())
                .distinct()
                .sorted()
                .collect(Collectors.toList());
    }
}

package com.shrestha.wcsimulator.wcsimulator.service;

import com.shrestha.wcsimulator.wcsimulator.domain.*;
import com.shrestha.wcsimulator.wcsimulator.service.ProbabilityService;
import org.springframework.stereotype.Service;

import java.util.*;

@Service
public class ProbabilityServiceImpl implements ProbabilityService {

    @Override
    public double probabilityTeamPlaysInCity(String teamName, String city) {

        // 1. Determine current group winners
        Map<String, Team> groupWinners = calculateGroupWinners();

        // 2. Map group winners to match slots
        Map<String, MatchSlot> bracketMapping = mapToBracket(groupWinners);

        // 3. Find which slot is hosted in the city
        Optional<MatchSlot> citySlot = findSlotByCity(city);

        // 4. Probability calculation
        return citySlot
                .filter(slot -> bracketMapping.get(teamName).equals(slot))
                .map(slot -> 1.0)
                .orElse(0.0);
    }

    private Map<String, Team> calculateGroupWinners() {
        // Stubbed for MVP
        return new HashMap<>();
    }

    private Map<String, MatchSlot> mapToBracket(Map<String, Team> winners) {
        return new HashMap<>();
    }

    private Optional<MatchSlot> findSlotByCity(String city) {
        return Optional.empty();
    }
}


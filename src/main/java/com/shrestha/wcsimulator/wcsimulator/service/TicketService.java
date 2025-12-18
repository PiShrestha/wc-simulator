package com.shrestha.wcsimulator.wcsimulator.service;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.core.io.ClassPathResource;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.io.InputStream;
import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

@Service
public class TicketService {

    public record TicketMatch(
            int matchId,
            String date,
            String time,
            String stadium,
            List<String> teams,
            Map<String, BigDecimal> tickets,
            String stage
    ) {
    }

    private final List<TicketMatch> matches;

    public TicketService() throws IOException {
        this.matches = loadMatches();
    }

    public List<TicketMatch> allMatches() {
        return matches;
    }

    public List<String> venues() {
        return matches.stream()
                .map(TicketMatch::stadium)
                .filter(s -> s != null && !s.isBlank())
                .distinct()
                .sorted()
                .toList();
    }

    public List<TicketMatch> byVenue(String venue) {
        if (venue == null || venue.isBlank()) return matches;
        return matches.stream()
                .filter(m -> venue.equalsIgnoreCase(m.stadium()))
                .toList();
    }

    private List<TicketMatch> loadMatches() throws IOException {
        ClassPathResource resource = new ClassPathResource("static/tickets.json");
        try (InputStream is = resource.getInputStream()) {
            ObjectMapper mapper = new ObjectMapper();
            List<Map<String, Object>> raw = mapper.readValue(is, new TypeReference<>() {});
            List<TicketMatch> parsed = new ArrayList<>();
            for (Map<String, Object> node : raw) {
                int matchId = ((Number) node.getOrDefault("match", 0)).intValue();
                String date = (String) node.getOrDefault("date", "");
                String time = (String) node.getOrDefault("time", "");
                String stadium = (String) node.getOrDefault("stadium", "");
                @SuppressWarnings("unchecked")
                List<String> teams = (List<String>) node.getOrDefault("teams", List.of());
                @SuppressWarnings("unchecked")
                Map<String, Object> ticketsNode = (Map<String, Object>) node.getOrDefault("tickets", Map.of());
                Map<String, BigDecimal> tickets = ticketsNode.entrySet().stream()
                        .collect(Collectors.toMap(Map.Entry::getKey, e -> toBigDecimal(e.getValue())));
                parsed.add(new TicketMatch(matchId, date, time, stadium, teams, tickets, stageFor(matchId)));
            }
            parsed.sort(Comparator.comparingInt(TicketMatch::matchId));
            return parsed;
        }
    }

    private BigDecimal toBigDecimal(Object value) {
        if (value == null) return null;
        if (value instanceof BigDecimal bd) return bd;
        if (value instanceof Number num) return BigDecimal.valueOf(num.doubleValue());
        try {
            return new BigDecimal(value.toString());
        } catch (NumberFormatException ex) {
            return null;
        }
    }

    private String stageFor(int matchId) {
        if (matchId >= 73 && matchId <= 88) return "Round of 32";
        if (matchId >= 89 && matchId <= 96) return "Round of 16";
        if (matchId >= 97 && matchId <= 100) return "Quarterfinal";
        if (matchId == 101 || matchId == 102) return "Semifinal";
        if (matchId == 103) return "Third Place";
        if (matchId == 104) return "Final";
        return "Match";
    }

    public Map<String, List<TicketMatch>> byVenueGroupedByStage(String venue) {
        List<TicketMatch> scoped = byVenue(venue);
        Map<String, List<TicketMatch>> grouped = new LinkedHashMap<>();
        List<String> stageOrder = stageOrder();
        Map<String, List<TicketMatch>> map = scoped.stream()
                .collect(Collectors.groupingBy(TicketMatch::stage));
        for (String stage : stageOrder) {
            List<TicketMatch> list = map.get(stage);
            if (list != null && !list.isEmpty()) {
                list = list.stream().sorted(Comparator.comparingInt(TicketMatch::matchId)).toList();
                grouped.put(stage, list);
            }
        }
        // Append any unexpected stages
        Set<String> seen = new LinkedHashSet<>(grouped.keySet());
        map.forEach((k, v) -> {
            if (!seen.contains(k)) grouped.put(k, v);
        });
        return grouped;
    }

    public List<String> stageOrder() {
        return List.of("Final", "Semifinal", "Quarterfinal", "Round of 16", "Round of 32", "Third Place", "Match");
    }
}

package com.shrestha.wcsimulator.wcsimulator.service;

import com.shrestha.wcsimulator.wcsimulator.domain.KnockoutMatch;
import com.shrestha.wcsimulator.wcsimulator.domain.MatchSlot;
import com.shrestha.wcsimulator.wcsimulator.domain.Team;
import com.shrestha.wcsimulator.wcsimulator.dto.PairingView;
import com.shrestha.wcsimulator.wcsimulator.provider.FifaRankProvider;
import com.shrestha.wcsimulator.wcsimulator.provider.GroupStageProvider;
import com.shrestha.wcsimulator.wcsimulator.provider.KnockoutStageProvider;
import org.springframework.stereotype.Service;

import java.util.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

@Service
public class ProjectionService {
    private final GroupStageProvider groupProvider;
    private final KnockoutStageProvider knockoutProvider;
    private final FifaRankProvider rankProvider;

    private static final Pattern GROUP_SLOT = Pattern.compile("([123])([A-Z])");
    private static final Pattern THIRD_AGG = Pattern.compile("3([A-Z]+)");

    public ProjectionService(GroupStageProvider groupProvider,
                             KnockoutStageProvider knockoutProvider,
                             FifaRankProvider rankProvider) {
        this.groupProvider = groupProvider;
        this.knockoutProvider = knockoutProvider;
        this.rankProvider = rankProvider;
    }

        public List<PairingView> projectedPairingsForVenue(String city, Map<String, List<String>> orderedGroups) {
        return knockoutProvider.matches().stream()
            .filter(m -> m.getCity().equalsIgnoreCase(city))
            .map(m -> {
                Team ht = projectedTeamForSlot(m.getHomeSlot(), orderedGroups);
                Team at = projectedTeamForSlot(m.getAwaySlot(), orderedGroups);
                String h = labelWithRank(nameOf(ht));
                String a = labelWithRank(nameOf(at));
                int hr = rankProvider.rankOf(ht == null ? null : ht.getName());
                int ar = rankProvider.rankOf(at == null ? null : at.getName());
                double avg = (hr + ar) / 2.0;
                return new PairingView(
                    h,
                    a,
                    m.getHomeSlot().getCode(),
                    m.getAwaySlot().getCode(),
                    m.getStage(),
                    m.getDate(),
                    m.getCity(),
                    avg,
                    m.getMatchId()
                );
            })
            .collect(Collectors.toList());
        }

    private String nameOf(Team t) { return t == null ? "TBD" : t.getName(); }
    private String labelWithRank(String name) {
        int r = rankProvider.rankOf(name);
        String rs = r >= 1 && r < 999 ? String.valueOf(r) : "?";
        return name + " (" + rs + ")";
    }

    public Team projectedTeamForSlot(MatchSlot slot, Map<String, List<String>> orderedGroups) {
        String code = slot.getCode();

        Matcher m = GROUP_SLOT.matcher(code);
        if (m.matches()) {
            int pos = Integer.parseInt(m.group(1));
            String group = m.group(2);
            List<String> ranking = orderedGroups.getOrDefault(group, List.of());
            if (ranking.size() >= pos) return team(ranking.get(pos - 1), group);
            return team("TBD", group);
        }

        Matcher agg = THIRD_AGG.matcher(code);
        if (agg.matches()) {
            String letters = agg.group(1);
            String best = null;
            int bestRank = Integer.MAX_VALUE;
            for (char ch : letters.toCharArray()) {
                String g = String.valueOf(ch);
                List<String> ranking = orderedGroups.getOrDefault(g, List.of());
                if (ranking.size() >= 3) {
                    String candidate = ranking.get(2);
                    int r = rankProvider.rankOf(candidate);
                    if (r < bestRank) { bestRank = r; best = candidate; }
                }
            }
            return team(best == null ? "TBD" : best, "");
        }

        if (code.startsWith("W") || code.startsWith("L")) {
            int id = Integer.parseInt(code.substring(1));
            KnockoutMatch match = findMatch(id);
            Team h = projectedTeamForSlot(match.getHomeSlot(), orderedGroups);
            Team a = projectedTeamForSlot(match.getAwaySlot(), orderedGroups);
            // Decide winner/loser by better rank (lower is better)
            int rh = rankProvider.rankOf(h.getName());
            int ra = rankProvider.rankOf(a.getName());
            boolean homeWins = rh <= ra;
            if (code.startsWith("W")) {
                return homeWins ? h : a;
            } else {
                return homeWins ? a : h;
            }
        }

        return team("TBD", "");
    }

    private KnockoutMatch findMatch(int id) {
        return knockoutProvider.matches().stream()
                .filter(m -> m.getMatchId() == id)
                .findFirst().orElseThrow();
    }

    public Map<String, List<String>> defaultOrderedGroups() {
        // Already keyed by single-letter group ids (A-L)
        Map<String, List<String>> ordered = new LinkedHashMap<>();
        groupProvider.groupStrings().forEach((k, v) -> ordered.put(k, new ArrayList<>(v)));
        return ordered;
    }

    private Team team(String name, String group) {
        Team t = new Team();
        t.setName(name);
        t.setGroupId(group);
        t.setFifaRank(rankProvider.rankOf(name));
        t.setPoints(0);
        return t;
    }
}

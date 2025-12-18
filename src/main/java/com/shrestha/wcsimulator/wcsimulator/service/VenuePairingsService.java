package com.shrestha.wcsimulator.wcsimulator.service;

import com.shrestha.wcsimulator.wcsimulator.domain.Team;
import com.shrestha.wcsimulator.wcsimulator.dto.PairingView;
import com.shrestha.wcsimulator.wcsimulator.provider.KnockoutStageProvider;
import com.shrestha.wcsimulator.wcsimulator.provider.GroupStageProvider;
import com.shrestha.wcsimulator.wcsimulator.provider.FifaRankProvider;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

@Service
public class VenuePairingsService {

    private final KnockoutStageProvider provider;
    private final SlotResolverService resolver;
    private final FifaRankProvider rankProvider;
    private final GroupStageProvider groupProvider;

    public VenuePairingsService(KnockoutStageProvider provider, SlotResolverService resolver, FifaRankProvider rankProvider, GroupStageProvider groupProvider) {
        this.provider = provider;
        this.resolver = resolver;
        this.rankProvider = rankProvider;
        this.groupProvider = groupProvider;
    }

    public List<PairingView> pairingsForVenue(String city) {
        List<PairingView> out = new ArrayList<>();

        provider.matches().stream()
                .filter(m -> m.getCity().equalsIgnoreCase(city))
                .forEach(m -> {
                    // Generate broad candidate sets: anyone from the relevant groups can finish required positions
                    Set<Team> home = resolver.resolveSlot(m.getHomeSlot());
                    Set<Team> away = resolver.resolveSlot(m.getAwaySlot());

                    // Cross-product pairings
                    for (Team h : home) {
                        for (Team a : away) {
                            // Skip self-versus-self matchups
                            if (equalsIgnoreCase(h.getName(), a.getName())) {
                                continue;
                            }
                            int hr = rankProvider.rankOf(h.getName());
                            int ar = rankProvider.rankOf(a.getName());
                            String hLabel = labelWithRank(h.getName(), hr);
                            String aLabel = labelWithRank(a.getName(), ar);
                            double avg = (hr + ar) / 2.0;
                            String hOrigin = originForTeam(m.getHomeSlot().getCode(), h);
                            String aOrigin = originForTeam(m.getAwaySlot().getCode(), a);
                            out.add(new PairingView(
                                    hLabel,
                                    aLabel,
                                    hOrigin,
                                    aOrigin,
                                    m.getStage(),
                                    m.getDate(),
                                    m.getCity(),
                                    avg,
                                    m.getMatchId()
                            ));
                        }
                    }
                });

        // Sort by average rank ascending, then by matchId, then names
        return out.stream()
                .sorted((p1, p2) -> {
                    int cmp = Double.compare(p1.getAvgRank(), p2.getAvgRank());
                    if (cmp != 0) return cmp;
                    // Prefer higher group finishes (1 over 2 over 3) when ranks tie
                    int w1 = finishWeight(p1.getHomeSlot()) + finishWeight(p1.getAwaySlot());
                    int w2 = finishWeight(p2.getHomeSlot()) + finishWeight(p2.getAwaySlot());
                    cmp = Integer.compare(w1, w2);
                    if (cmp != 0) return cmp;
                    cmp = Integer.compare(p1.getMatchId(), p2.getMatchId());
                    if (cmp != 0) return cmp;
                    cmp = p1.getHome().compareTo(p2.getHome());
                    if (cmp != 0) return cmp;
                    return p1.getAway().compareTo(p2.getAway());
                })
                .collect(Collectors.toList());
    }

    private String labelWithRank(String name, int rank) {
        String r = rank >= 1 && rank < 999 ? String.valueOf(rank) : "?";
        return name + " (" + r + ")";
    }

    // Best-effort: parse the trailing "(rank)" back to integers for sorting,
    // but also consult provider in case of unknown labels.
    private int[] extractRanks(PairingView p) {
        int hr = parseRankFromLabel(p.getHome());
        int ar = parseRankFromLabel(p.getAway());
        if (hr == 999) hr = rankProvider.rankOf(stripLabel(p.getHome()));
        if (ar == 999) ar = rankProvider.rankOf(stripLabel(p.getAway()));
        return new int[]{hr, ar};
    }

    private int parseRankFromLabel(String label) {
        int open = label.lastIndexOf('(');
        int close = label.lastIndexOf(')');
        if (open > -1 && close > open) {
            try {
                return Integer.parseInt(label.substring(open + 1, close).trim());
            } catch (NumberFormatException ignored) { }
        }
        return 999;
    }

    private String stripLabel(String label) {
        int open = label.lastIndexOf('(');
        if (open > 0) return label.substring(0, open).trim();
        return label;
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

    private String originForTeam(String slotCode, Team team) {
        // Group-based slot like 1A, 2B, 3C -> the required finish for the team
        if (slotCode.matches("[123][A-Z]")) return slotCode;

        // Aggregated third pool like 3ABCDF -> show 3{team.groupId} when applicable
        if (slotCode.matches("3[A-Z]{2,}")) {
            String g = team.getGroupId();
            if (g != null && g.length() == 1 && slotCode.contains(g)) {
                return "3" + g;
            }
            return "?";
        }

        // Winner/Loser of previous match: determine which branch contains the team via resolver
        if (slotCode.startsWith("W") || slotCode.startsWith("L")) {
            int id = Integer.parseInt(slotCode.substring(1));
            var child = provider.matches().stream()
                    .filter(mm -> mm.getMatchId() == id)
                    .findFirst().orElse(null);
            if (child == null) return "?";
            Set<Team> left = resolver.resolveSlot(child.getHomeSlot());
            Set<Team> right = resolver.resolveSlot(child.getAwaySlot());
            if (containsTeam(left, team)) return originForTeam(child.getHomeSlot().getCode(), team);
            if (containsTeam(right, team)) return originForTeam(child.getAwaySlot().getCode(), team);
            return "?";
        }

        return "?";
    }

    // Not needed for broad permutations anymore

    private String teamFinishCode(Team team, java.util.Map<String, java.util.List<String>> orderedGroups) {
        String g = team.getGroupId();
        if (g == null || g.length() != 1) return "?";
        var ranking = orderedGroups.getOrDefault(g, java.util.List.of());
        for (int i = 0; i < ranking.size(); i++) {
            if (equalsIgnoreCase(ranking.get(i), team.getName())) {
                return (i + 1) + g;
            }
        }
        return "?";
    }

    private boolean isGroupFinish(String origin) {
        return origin != null && origin.matches("[123][A-Z]");
    }

    private int finishWeight(String origin) {
        if (origin == null) return 9;
        if (origin.matches("[123][A-Z]")) {
            return Character.digit(origin.charAt(0), 10);
        }
        if (origin.matches("3[A-Z]")) return 3;
        return 9;
    }

    private Set<Team> candidatesForSlot(com.shrestha.wcsimulator.wcsimulator.domain.MatchSlot slot,
                                        java.util.Map<String, java.util.List<String>> orderedGroups) {
        String code = slot.getCode();
        java.util.Set<Team> set = new java.util.LinkedHashSet<>();
        if (code.matches("[123][A-Z]")) {
            int pos = Character.digit(code.charAt(0), 10);
            String g = String.valueOf(code.charAt(1));
            var ranking = orderedGroups.getOrDefault(g, java.util.List.of());
            if (ranking.size() >= pos) {
                set.add(buildTeam(ranking.get(pos - 1), g));
            }
            return set;
        }
        if (code.matches("3[A-Z]{2,}")) {
            String letters = code.substring(1);
            for (char ch : letters.toCharArray()) {
                String g = String.valueOf(ch);
                var ranking = orderedGroups.getOrDefault(g, java.util.List.of());
                if (ranking.size() >= 3) {
                    set.add(buildTeam(ranking.get(2), g));
                }
            }
            return set;
        }
        if (code.startsWith("W") || code.startsWith("L")) {
            int id = Integer.parseInt(code.substring(1));
            var child = provider.matches().stream()
                    .filter(mm -> mm.getMatchId() == id)
                    .findFirst().orElse(null);
            if (child == null) return set;
            java.util.Set<Team> left = candidatesForSlot(child.getHomeSlot(), orderedGroups);
            java.util.Set<Team> right = candidatesForSlot(child.getAwaySlot(), orderedGroups);
            if (left.isEmpty() || right.isEmpty()) return set;
            // Determine winner/loser by FIFA rank (lower is better)
            Team lh = left.iterator().next();
            Team rh = right.iterator().next();
            int rL = rankProvider.rankOf(lh.getName());
            int rR = rankProvider.rankOf(rh.getName());
            boolean homeWins = rL <= rR;
            Team winner = homeWins ? lh : rh;
            Team loser = homeWins ? rh : lh;
            set.add(code.startsWith("W") ? winner : loser);
            return set;
        }
        return set;
    }

    private Team buildTeam(String name, String group) {
        Team t = new Team();
        t.setName(name);
        t.setGroupId(group);
        t.setFifaRank(rankProvider.rankOf(name));
        t.setPoints(0);
        return t;
    }

    private java.util.Map<String, java.util.List<String>> defaultOrderedGroups() {
        java.util.Map<String, java.util.List<String>> ordered = new java.util.LinkedHashMap<>();
        groupProvider.groupStrings().forEach((k, v) -> ordered.put(k, new java.util.ArrayList<>(v)));
        return ordered;
    }

    private boolean containsTeam(Set<Team> set, Team t) {
        if (set == null || t == null) return false;
        for (Team x : set) {
            if (equalsIgnoreCase(x.getName(), t.getName())) return true;
        }
        return false;
    }

    private boolean equalsIgnoreCase(String a, String b) {
        if (a == null || b == null) return false;
        return a.equalsIgnoreCase(b);
    }
}

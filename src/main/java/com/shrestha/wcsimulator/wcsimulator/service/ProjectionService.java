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
                        if (ht != null && at != null && equalsIgnoreCase(ht.getName(), at.getName())) {
                            // Skip unrealistic self-versus-self projections
                            return null;
                        }
                String h = labelWithRank(nameOf(ht));
                String a = labelWithRank(nameOf(at));
                int hr = rankProvider.rankOf(ht == null ? null : ht.getName());
                int ar = rankProvider.rankOf(at == null ? null : at.getName());
                double avg = (hr + ar) / 2.0;
                        String hOrigin = projectedOriginSlot(m.getHomeSlot(), orderedGroups, ht);
                        String aOrigin = projectedOriginSlot(m.getAwaySlot(), orderedGroups, at);
                return new PairingView(
                    h,
                    a,
                                hOrigin,
                                aOrigin,
                    m.getStage(),
                    m.getDate(),
                    m.getCity(),
                    avg,
                    m.getMatchId()
                );
                    })
                    .filter(Objects::nonNull)
                    .filter(p -> qualifies(p, "qualifying"))
                    .sorted((p1, p2) -> {
                        int so1 = stageOrder(p1.getStage());
                        int so2 = stageOrder(p2.getStage());
                        int cmp = Integer.compare(so1, so2);
                        if (cmp != 0) return cmp;
                        cmp = Double.compare(p1.getAvgRank(), p2.getAvgRank());
                        if (cmp != 0) return cmp;
                        return Integer.compare(p1.getMatchId(), p2.getMatchId());
                    })
                    .collect(Collectors.toList());
        }

    public PairingView toPairingView(KnockoutMatch m, Map<String, List<String>> orderedGroups) {
        Team ht = projectedTeamForSlot(m.getHomeSlot(), orderedGroups);
        Team at = projectedTeamForSlot(m.getAwaySlot(), orderedGroups);
        if (ht != null && at != null && equalsIgnoreCase(ht.getName(), at.getName())) {
            return null;
        }
        String h = labelWithRank(nameOf(ht));
        String a = labelWithRank(nameOf(at));
        int hr = rankProvider.rankOf(ht == null ? null : ht.getName());
        int ar = rankProvider.rankOf(at == null ? null : at.getName());
        double avg = (hr + ar) / 2.0;
        String hOrigin = projectedOriginSlot(m.getHomeSlot(), orderedGroups, ht);
        String aOrigin = projectedOriginSlot(m.getAwaySlot(), orderedGroups, at);
        return new PairingView(h, a, hOrigin, aOrigin, m.getStage(), m.getDate(), m.getCity(), avg, m.getMatchId());
    }

    private String nameOf(Team t) { return t == null ? "TBD" : t.getName(); }
    private String labelWithRank(String name) {
        int r = rankProvider.rankOf(name);
        String rs = r >= 1 && r < 999 ? String.valueOf(r) : "?";
        return name + " (" + rs + ")";
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

    private boolean equalsIgnoreCase(String a, String b) {
        if (a == null || b == null) return false;
        return a.equalsIgnoreCase(b);
    }

    // Compute the team-aware origin slot at the group stage for a given knockout slot.
    // Examples: 1E -> 1E; 3ABCDF -> 3X for the target's group X; W74 -> origin of the winner/loser that matches target.
    private String projectedOriginSlot(MatchSlot slot, Map<String, List<String>> orderedGroups, Team targetTeam) {
        String code = slot.getCode();

        Matcher m = GROUP_SLOT.matcher(code);
        if (m.matches()) {
            // Already a group slot like 1A, 2B, 3C
            return code;
        }

        Matcher agg = THIRD_AGG.matcher(code);
        if (agg.matches()) {
            String letters = agg.group(1);
            // If we know the target team and its group is among the pooled letters, use it
            if (targetTeam != null && targetTeam.getGroupId() != null && targetTeam.getGroupId().length() == 1) {
                String g = targetTeam.getGroupId();
                if (letters.contains(g)) {
                    return "3" + g;
                }
            }
            // Fallback: choose the best-ranked 3rd among the pooled groups
            String bestGroup = null;
            int bestRank = Integer.MAX_VALUE;
            for (char ch : letters.toCharArray()) {
                String g = String.valueOf(ch);
                List<String> ranking = orderedGroups.getOrDefault(g, List.of());
                if (ranking.size() >= 3) {
                    String candidate = ranking.get(2);
                    int r = rankProvider.rankOf(candidate);
                    if (r < bestRank) { bestRank = r; bestGroup = g; }
                }
            }
            return bestGroup == null ? "3?" : ("3" + bestGroup);
        }

        if (code.startsWith("W") || code.startsWith("L")) {
            int id = Integer.parseInt(code.substring(1));
            KnockoutMatch match = findMatch(id);
            Team h = projectedTeamForSlot(match.getHomeSlot(), orderedGroups);
            Team a = projectedTeamForSlot(match.getAwaySlot(), orderedGroups);
            // If targetTeam is known, trace the branch that yields that team
            if (targetTeam != null) {
                String tn = targetTeam.getName();
                if (equalsIgnoreCase(tn, h == null ? null : h.getName())) {
                    return projectedOriginSlot(match.getHomeSlot(), orderedGroups, targetTeam);
                }
                if (equalsIgnoreCase(tn, a == null ? null : a.getName())) {
                    return projectedOriginSlot(match.getAwaySlot(), orderedGroups, targetTeam);
                }
            }
            // Fallback: decide winner/loser by better rank and return that origin
            int rh = rankProvider.rankOf(h == null ? null : h.getName());
            int ra = rankProvider.rankOf(a == null ? null : a.getName());
            boolean homeWins = rh <= ra;
            String hOrigin = projectedOriginSlot(match.getHomeSlot(), orderedGroups, h);
            String aOrigin = projectedOriginSlot(match.getAwaySlot(), orderedGroups, a);
            if (code.startsWith("W")) {
                return homeWins ? hOrigin : aOrigin;
            } else {
                return homeWins ? aOrigin : hOrigin;
            }
        }

        return "?";
    }

    public Team projectedTeamForSlot(MatchSlot slot, Map<String, List<String>> orderedGroups) {
        String code = slot.getCode();

        Matcher m = GROUP_SLOT.matcher(code);
        if (m.matches()) {
            int pos = Integer.parseInt(m.group(1));
            String group = m.group(2);
            List<String> ranking = orderedGroups.getOrDefault(group, List.of());
            if (ranking.size() >= pos) {
                String candidate = ranking.get(pos - 1);
                if (pos >= 3 && rankProvider.isTop12(candidate)) {
                    // Skip Top-12 teams that did not win or finish runner-up
                    String fallback = firstNonTop12From(ranking, pos - 1);
                    return team(fallback == null ? "TBD" : fallback, group);
                }
                return team(candidate, group);
            }
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
                    if (!rankProvider.isTop12(candidate) && r < bestRank) { bestRank = r; best = candidate; }
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
        FifaRankProvider provider = new FifaRankProvider();
        groupProvider.groupStrings().forEach((k, v) -> ordered.put(k, new ArrayList<>(v)));
        ordered.forEach((k, v) -> v.sort(Comparator.comparingInt(provider::rankOf)));
        // Add rank labels to each team name (Does not work yet, needs to modify logic in the html)
//        ordered.forEach((k, v) -> {
//            for (int i = 0; i < v.size(); i++) {
//                String team = v.get(i);
//                v.set(i, team + " (" + provider.rankOf(team) + ")");
//            }
//        });
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

    private String firstNonTop12From(List<String> ranking, int startIdx) {
        for (int i = startIdx; i < ranking.size(); i++) {
            String candidate = ranking.get(i);
            if (!rankProvider.isTop12(candidate)) {
                return candidate;
            }
        }
        return null;
    }

    private boolean qualifies(PairingView pairing, String mode) {
        String home = stripRankLabel(pairing.getHome());
        String away = stripRankLabel(pairing.getAway());

        boolean homeTop12 = rankProvider.isTop12(home);
        boolean awayTop12 = rankProvider.isTop12(away);
        boolean homeSA = rankProvider.isSouthAmerican(home);
        boolean awaySA = rankProvider.isSouthAmerican(away);

        boolean hasQualified = homeTop12 || awayTop12 || homeSA || awaySA;

        switch (mode) {
            case "top12":
                return homeTop12 || awayTop12;
            case "south_american":
                return homeSA || awaySA;
            case "all":
                return true;
            case "qualifying":
            default:
                return hasQualified;
        }
    }

    private String stripRankLabel(String label) {
        if (label == null) return null;
        int open = label.lastIndexOf('(');
        if (open > 0) return label.substring(0, open).trim();
        return label;
    }
}

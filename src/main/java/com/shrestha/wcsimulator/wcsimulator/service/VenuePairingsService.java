package com.shrestha.wcsimulator.wcsimulator.service;

import com.shrestha.wcsimulator.wcsimulator.domain.Team;
import com.shrestha.wcsimulator.wcsimulator.dto.PairingView;
import com.shrestha.wcsimulator.wcsimulator.provider.KnockoutStageProvider;
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

    public VenuePairingsService(KnockoutStageProvider provider, SlotResolverService resolver, FifaRankProvider rankProvider) {
        this.provider = provider;
        this.resolver = resolver;
        this.rankProvider = rankProvider;
    }

    public List<PairingView> pairingsForVenue(String city) {
        List<PairingView> out = new ArrayList<>();

        provider.matches().stream()
                .filter(m -> m.getCity().equalsIgnoreCase(city))
                .forEach(m -> {
                    Set<Team> home = resolver.resolveSlot(m.getHomeSlot());
                    Set<Team> away = resolver.resolveSlot(m.getAwaySlot());

                    // Cross-product pairings
                    for (Team h : home) {
                        for (Team a : away) {
                            int hr = rankProvider.rankOf(h.getName());
                            int ar = rankProvider.rankOf(a.getName());
                            String hLabel = labelWithRank(h.getName(), hr);
                            String aLabel = labelWithRank(a.getName(), ar);
                            double avg = (hr + ar) / 2.0;
                            out.add(new PairingView(
                                    hLabel,
                                    aLabel,
                                    m.getHomeSlot().getCode(),
                                    m.getAwaySlot().getCode(),
                                    m.getStage(),
                                    m.getDate(),
                                    m.getCity(),
                                    avg,
                                    m.getMatchId()
                            ));
                        }
                    }
                });

        // Sort by stage order, then average rank ascending, then matchId, then names
        return out.stream()
                .sorted((p1, p2) -> {
                    int so1 = stageOrder(p1.getStage());
                    int so2 = stageOrder(p2.getStage());
                    int cmp = Integer.compare(so1, so2);
                    if (cmp != 0) return cmp;
                    int[] r1 = extractRanks(p1);
                    int[] r2 = extractRanks(p2);
                    double a1 = (r1[0] + r1[1]) / 2.0;
                    double a2 = (r2[0] + r2[1]) / 2.0;
                    cmp = Double.compare(a1, a2);
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
}

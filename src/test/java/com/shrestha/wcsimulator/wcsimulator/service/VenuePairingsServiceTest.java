package com.shrestha.wcsimulator.wcsimulator.service;

import com.shrestha.wcsimulator.wcsimulator.dto.PairingView;
import com.shrestha.wcsimulator.wcsimulator.provider.FifaRankProvider;
import com.shrestha.wcsimulator.wcsimulator.provider.GroupStageProvider;
import com.shrestha.wcsimulator.wcsimulator.provider.KnockoutStageProvider;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

class VenuePairingsServiceTest {

    private final GroupStageProvider groupProvider = new GroupStageProvider();
    private final KnockoutStageProvider knockoutProvider = new KnockoutStageProvider();
    private final FifaRankProvider rankProvider = new FifaRankProvider();
    private final SlotResolverService resolver = new SlotResolverService(groupProvider, knockoutProvider, rankProvider);
    private final VenuePairingsService service = new VenuePairingsService(knockoutProvider, resolver, rankProvider, groupProvider);

    @Test
    void qualifyingFiltersRemoveNonHeadlineMatches() {
        List<PairingView> sample = List.of(
                new PairingView("Canada (27)", "Qatar (51)", "2A", "2B", "Round of 32", "TBD", "Test City", 39, 1),
                new PairingView("Spain (1)", "Canada (27)", "1H", "2A", "Round of 16", "TBD", "Test City", 14, 2),
                new PairingView("Brazil (5)", "Curacao (82)", "1C", "3E", "Round of 32", "TBD", "Test City", 43.5, 3),
                new PairingView("Paraguay (39)", "Peru (60)", "2D", "2E", "Round of 32", "TBD", "Test City", 49.5, 4)
        );

        var qualifying = service.filterPairingsByTeamType(sample, "qualifying");
        assertEquals(3, qualifying.size(), "Qualifying filter should keep only pairings with Top-12 or South American teams");
        assertTrue(qualifying.stream().noneMatch(p -> strip("Canada (27)").equals(strip(p.getHome())) && strip("Qatar (51)").equals(strip(p.getAway()))));

        var top12Only = service.filterPairingsByTeamType(sample, "top12");
        assertEquals(2, top12Only.size(), "Top-12 filter should keep pairings with at least one Top-12 team");
        assertTrue(top12Only.stream().allMatch(p -> rankProvider.isTop12(strip(p.getHome())) || rankProvider.isTop12(strip(p.getAway()))));

        var southAmericanOnly = service.filterPairingsByTeamType(sample, "south_american");
        assertEquals(2, southAmericanOnly.size(), "South American filter should keep pairings with South American representation");
        assertTrue(southAmericanOnly.stream().allMatch(p -> rankProvider.isSouthAmerican(strip(p.getHome())) || rankProvider.isSouthAmerican(strip(p.getAway()))));

        var all = service.filterPairingsByTeamType(sample, "all");
        assertEquals(sample.size(), all.size(), "All filter should preserve every pairing");
    }

    @Test
    void top12TeamsDoNotEnterThroughThirdPlacePaths() {
        var pairings = service.pairingsForVenue("Boston"); // includes match 74 with third-place aggregate slot
        assertFalse(pairings.isEmpty(), "Expected Boston pairings to be generated");

        boolean hasInvalidTop12Third = pairings.stream().anyMatch(p ->
                (p.getHomeSlot().startsWith("3") && rankProvider.isTop12(strip(p.getHome()))) ||
                        (p.getAwaySlot().startsWith("3") && rankProvider.isTop12(strip(p.getAway()))));

        assertFalse(hasInvalidTop12Third, "Top-12 teams should not appear from third-place slots");
    }

    private String strip(String label) {
        if (label == null) return null;
        int open = label.lastIndexOf('(');
        if (open > 0) return label.substring(0, open).trim();
        return label.trim();
    }
}

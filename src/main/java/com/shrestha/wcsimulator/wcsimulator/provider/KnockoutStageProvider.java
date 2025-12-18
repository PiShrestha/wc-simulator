package com.shrestha.wcsimulator.wcsimulator.provider;

import com.shrestha.wcsimulator.wcsimulator.domain.*;
import org.springframework.stereotype.Component;

import java.util.*;

@Component
public class KnockoutStageProvider {

    public List<KnockoutMatch> getKnockoutMatches() {
        List<KnockoutMatch> matches = new ArrayList<>();

        // ---------- Round of 32 (Matches 73–88) ----------
        matches.add(km(73, "2A", "2B", "Los Angeles", "Round of 32", "TBD"));
        matches.add(km(74, "1E", "3ABCDF", "Boston", "Round of 32", "TBD"));
        matches.add(km(75, "1F", "2C", "Monterrey", "Round of 32", "TBD"));
        matches.add(km(76, "1C", "2F", "Houston", "Round of 32", "TBD"));
        matches.add(km(77, "1I", "3CDFGH", "New Jersey", "Round of 32", "TBD"));
        matches.add(km(78, "2E", "2I", "Dallas", "Round of 32", "TBD"));
        matches.add(km(79, "1A", "3CEFHI", "Mexico City", "Round of 32", "TBD"));
        matches.add(km(80, "1L", "3EHIJK", "Atlanta", "Round of 32", "TBD"));
        matches.add(km(81, "1D", "3BEFIJ", "San Francisco", "Round of 32", "TBD"));
        matches.add(km(82, "1G", "3AEHIJ", "Seattle", "Round of 32", "TBD"));
        matches.add(km(83, "2K", "2L", "Toronto", "Round of 32", "TBD"));
        matches.add(km(84, "1H", "2J", "Los Angeles", "Round of 32", "TBD"));
        matches.add(km(85, "1B", "3EFGIJ", "Vancouver", "Round of 32", "TBD"));
        matches.add(km(86, "1J", "2H", "Miami", "Round of 32", "TBD"));
        matches.add(km(87, "1K", "3DEIJL", "Kansas City", "Round of 32", "TBD"));
        matches.add(km(88, "2D", "2G", "Dallas", "Round of 32", "TBD"));

        // ---------- Round of 16 (Matches 89–96) ----------
        matches.add(km(89, "W74", "W77", "Philadelphia", "Round of 16", "TBD"));
        matches.add(km(90, "W73", "W75", "Houston", "Round of 16", "TBD"));
        matches.add(km(91, "W76", "W78", "New Jersey", "Round of 16", "TBD"));
        matches.add(km(92, "W79", "W80", "Mexico City", "Round of 16", "TBD"));
        matches.add(km(93, "W83", "W84", "Dallas", "Round of 16", "TBD"));
        matches.add(km(94, "W81", "W82", "Seattle", "Round of 16", "TBD"));
        matches.add(km(95, "W86", "W88", "Atlanta", "Round of 16", "TBD"));
        matches.add(km(96, "W85", "W87", "Vancouver", "Round of 16", "TBD"));

        // ---------- Quarterfinals (Matches 97–100) ----------
        matches.add(km(97, "W89", "W90", "Boston", "Quarterfinal", "TBD"));
        matches.add(km(98, "W93", "W94", "Los Angeles", "Quarterfinal", "TBD"));
        matches.add(km(99, "W91", "W92", "Miami", "Quarterfinal", "TBD"));
        matches.add(km(100, "W95", "W96", "Kansas City", "Quarterfinal", "TBD"));

        // ---------- Semifinals (Matches 101–102) ----------
        matches.add(km(101, "W97", "W98", "Dallas", "Semifinal", "TBD"));
        matches.add(km(102, "W99", "W100", "Atlanta", "Semifinal", "TBD"));

        // ---------- Third Place (Match 103) ----------
        matches.add(km(103, "L101", "L102", "Miami", "Third Place", "TBD"));

        // ---------- Final (Match 104) ----------
        matches.add(km(104, "W101", "W102", "New Jersey", "Final", "TBD"));

        return matches;
    }

    /**
     * Minimal MVP knockout chain used by SlotResolverService and VenueTeamsService.
     * Provides a small subset to demonstrate recursive slot resolution (e.g., W74 -> 74).
     */
    public List<KnockoutMatch> matches() {
        // For consistency across services, reuse the same schedule
        return getKnockoutMatches();
    }
    
    // Helper to construct MatchSlot without relying on Lombok all-args constructor
    private static MatchSlot slot(String code) {
        MatchSlot s = new MatchSlot();
        s.setCode(code);
        return s;
    }

    private static KnockoutMatch km(int id, String homeCode, String awayCode, String city, String stage, String date) {
        KnockoutMatch m = new KnockoutMatch();
        m.setMatchId(id);
        m.setHomeSlot(slot(homeCode));
        m.setAwaySlot(slot(awayCode));
        m.setCity(city);
        m.setStage(stage);
        m.setDate(date);
        return m;
    }
}


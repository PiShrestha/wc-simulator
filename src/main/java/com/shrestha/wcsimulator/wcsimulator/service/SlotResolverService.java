package com.shrestha.wcsimulator.wcsimulator.service;

import com.shrestha.wcsimulator.wcsimulator.domain.KnockoutMatch;
import com.shrestha.wcsimulator.wcsimulator.domain.MatchSlot;
import com.shrestha.wcsimulator.wcsimulator.domain.Team;
import com.shrestha.wcsimulator.wcsimulator.provider.FifaRankProvider;
import com.shrestha.wcsimulator.wcsimulator.provider.GroupStageProvider;
import com.shrestha.wcsimulator.wcsimulator.provider.KnockoutStageProvider;
import org.springframework.stereotype.Service;

import java.util.HashSet;
import java.util.List;
import java.util.Set;

@Service
public class SlotResolverService {

    private final GroupStageProvider groupProvider;
    private final KnockoutStageProvider knockoutProvider;
    private final FifaRankProvider rankProvider;

    public SlotResolverService(
            GroupStageProvider groupProvider,
            KnockoutStageProvider knockoutProvider,
            FifaRankProvider rankProvider
    ) {
        this.groupProvider = groupProvider;
        this.knockoutProvider = knockoutProvider;
        this.rankProvider = rankProvider;
    }

    /**
     * Resolve a slot to the set of teams that could fulfill it.
     * Supports group positions like "1A", "2B" and winners like "W74".
     */
    public Set<Team> resolveSlot(MatchSlot slot) {

        String code = slot.getCode();

        // Group-based slot (e.g., 1A, 2B, 3C)
        if (code.matches("[123][A-Z]")) {
            String group = code.substring(1);
            int pos = Integer.parseInt(code.substring(0, 1));
            // Filter out Top-12 teams when the slot represents 3rd-place eligibility
            return groupProvider.groups().getOrDefault(group, List.of()).stream()
                    .filter(t -> allowForPosition(pos, t))
                    .collect(java.util.stream.Collectors.toCollection(HashSet::new));
        }

        // Aggregated third-place pool (e.g., 3ABCDF): include all teams from listed groups
        if (code.matches("3[A-Z]{2,}")) {
            String groups = code.substring(1);
            Set<Team> teams = new HashSet<>();
            for (char ch : groups.toCharArray()) {
                String g = String.valueOf(ch);
                groupProvider.groups().getOrDefault(g, List.of()).stream()
                        .filter(t -> allowForPosition(3, t))
                        .forEach(teams::add);
            }
            return teams;
        }

        // Winner of previous match (e.g., W74)
        if (code.startsWith("W")) {
            int matchId = Integer.parseInt(code.substring(1));
            KnockoutMatch match = findMatch(matchId);

            Set<Team> teams = new HashSet<>();
            teams.addAll(resolveSlot(match.getHomeSlot()));
            teams.addAll(resolveSlot(match.getAwaySlot()));
            return teams;
        }

        // Loser of previous match (e.g., L101) — same candidate set as participants
        if (code.startsWith("L")) {
            int matchId = Integer.parseInt(code.substring(1));
            KnockoutMatch match = findMatch(matchId);

            Set<Team> teams = new HashSet<>();
            teams.addAll(resolveSlot(match.getHomeSlot()));
            teams.addAll(resolveSlot(match.getAwaySlot()));
            return teams;
        }

        return Set.of();
    }

    private KnockoutMatch findMatch(int id) {
        return knockoutProvider.matches()
                .stream()
                .filter(m -> m.getMatchId() == id)
                .findFirst()
                .orElseThrow(() -> new IllegalArgumentException("Unknown knockout match id: " + id));
    }

    private boolean allowForPosition(int position, Team team) {
        if (team == null) return false;
        // Top-12 teams cannot qualify through 3rd/4th-place slots
        boolean isTop12 = rankProvider.isTop12(team.getName());
        if (position >= 3 && isTop12) return false;
        return true;
    }
}

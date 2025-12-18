package com.shrestha.wcsimulator.wcsimulator.service;

import com.shrestha.wcsimulator.wcsimulator.domain.KnockoutMatch;
import com.shrestha.wcsimulator.wcsimulator.domain.MatchSlot;
import com.shrestha.wcsimulator.wcsimulator.domain.Team;
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

    public SlotResolverService(
            GroupStageProvider groupProvider,
            KnockoutStageProvider knockoutProvider
    ) {
        this.groupProvider = groupProvider;
        this.knockoutProvider = knockoutProvider;
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
            return new HashSet<>(groupProvider.groups().getOrDefault(group, List.of()));
        }

        // Aggregated third-place pool (e.g., 3ABCDF): include all teams from listed groups
        if (code.matches("3[A-Z]{2,}")) {
            String groups = code.substring(1);
            Set<Team> teams = new HashSet<>();
            for (char ch : groups.toCharArray()) {
                String g = String.valueOf(ch);
                teams.addAll(groupProvider.groups().getOrDefault(g, List.of()));
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
                .orElseThrow();
    }
}

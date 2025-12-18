package com.shrestha.wcsimulator.wcsimulator.service;

import com.shrestha.wcsimulator.wcsimulator.domain.Team;
import com.shrestha.wcsimulator.wcsimulator.provider.KnockoutStageProvider;
import org.springframework.stereotype.Service;

import java.util.HashSet;
import java.util.Set;

@Service
public class VenueTeamsService {

    private final KnockoutStageProvider provider;
    private final SlotResolverService resolver;

    public VenueTeamsService(
            KnockoutStageProvider provider,
            SlotResolverService resolver
    ) {
        this.provider = provider;
        this.resolver = resolver;
    }

    /**
     * Compute all teams that could appear in any match hosted at the given city.
     */
    public Set<Team> teamsForVenue(String city) {

        Set<Team> teams = new HashSet<>();

        provider.matches().stream()
                .filter(m -> m.getCity().equalsIgnoreCase(city))
                .forEach(m -> {
                    teams.addAll(resolver.resolveSlot(m.getHomeSlot()));
                    teams.addAll(resolver.resolveSlot(m.getAwaySlot()));
                });

        return teams;
    }
}

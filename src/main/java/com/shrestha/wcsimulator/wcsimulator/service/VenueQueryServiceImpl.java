package com.shrestha.wcsimulator.wcsimulator.service;


import com.shrestha.wcsimulator.wcsimulator.domain.KnockoutMatch;
import com.shrestha.wcsimulator.wcsimulator.dto.MatchView;
import com.shrestha.wcsimulator.wcsimulator.provider.KnockoutStageProvider;
import com.shrestha.wcsimulator.wcsimulator.service.VenueQueryService;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.stream.Collectors;

@Service
public class VenueQueryServiceImpl implements VenueQueryService {

    private final KnockoutStageProvider knockoutStageProvider;

    public VenueQueryServiceImpl(KnockoutStageProvider knockoutStageProvider) {
        this.knockoutStageProvider = knockoutStageProvider;
    }

    @Override
    public List<MatchView> getMatchesForVenue(String city) {
        return knockoutStageProvider.getKnockoutMatches()
                .stream()
                .filter(m -> m.getCity().equalsIgnoreCase(city))
                .map(this::toView)
                .collect(Collectors.toList());
    }

    private MatchView toView(KnockoutMatch match) {
        return new MatchView(
                match.getMatchId(),
                match.getStage(),
                match.getDate(),
                match.getHomeSlot().getCode(),
                match.getAwaySlot().getCode(),
                match.getCity()
        );
    }

}

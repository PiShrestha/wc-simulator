package com.shrestha.wcsimulator.wcsimulator.service;

import com.shrestha.wcsimulator.wcsimulator.dto.MatchView;

import java.util.List;

public interface VenueQueryService {
    List<MatchView> getMatchesForVenue(String city);
}

package com.shrestha.wcsimulator.wcsimulator.service;

import com.shrestha.wcsimulator.wcsimulator.domain.Team;

public interface ProbabilityService {

    double probabilityTeamPlaysInCity(String teamName, String city);
}

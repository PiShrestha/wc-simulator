package com.shrestha.wcsimulator.wcsimulator.domain;

import lombok.*;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class Team {
    private String name;
    private int fifaRank;
    private int points;
    private String groupId;
}

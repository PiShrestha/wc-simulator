package com.shrestha.wcsimulator.wcsimulator.domain;

import lombok.*;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class Game {
    private MatchSlot slot;
    private Team home;
    private Team away;
    private Integer homeGoals;
    private Integer awayGoals;
}

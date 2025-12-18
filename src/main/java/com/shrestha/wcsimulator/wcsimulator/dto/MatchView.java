package com.shrestha.wcsimulator.wcsimulator.dto;

import lombok.*;

@Data
@AllArgsConstructor
public class MatchView {
    private int matchId;
    private String stage;
    private String date;
    private String homeSlot;
    private String awaySlot;
    private String city;
}


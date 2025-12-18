package com.shrestha.wcsimulator.wcsimulator.domain;

import lombok.*;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class KnockoutMatch {
    private int matchId;
    private MatchSlot homeSlot;
    private MatchSlot awaySlot;
    private String city;
    private String stage;
    private String date; // ISO string like 2026-06-XX or "TBD"
}
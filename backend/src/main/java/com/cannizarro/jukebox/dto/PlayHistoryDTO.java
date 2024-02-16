package com.cannizarro.jukebox.dto;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.Setter;

import java.time.Instant;

@Getter
@Setter
@AllArgsConstructor
public class PlayHistoryDTO {
    private TrackDTO track;
    private Instant instant;
}

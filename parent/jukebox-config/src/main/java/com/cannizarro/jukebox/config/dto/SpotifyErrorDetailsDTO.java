package com.cannizarro.jukebox.config.dto;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class SpotifyErrorDetailsDTO {
    private int status;
    private String message;
}

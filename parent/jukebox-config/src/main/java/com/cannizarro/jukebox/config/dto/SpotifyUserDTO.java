package com.cannizarro.jukebox.config.dto;

import com.cannizarro.jukebox.config.constants.Constants;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Getter;

@Getter
public class SpotifyUserDTO {
    private String country;
    @JsonProperty("display_name")
    private String displayName;
    private String id;
    public String getUniqueIdentifier(){
        return String.format(Constants.SPOTIFY_ID_PREFIX, id);
    }
}

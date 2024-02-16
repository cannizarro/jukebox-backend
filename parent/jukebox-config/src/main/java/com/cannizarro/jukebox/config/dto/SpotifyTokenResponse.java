package com.cannizarro.jukebox.config.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Getter;

import static java.lang.String.format;

@Getter
public class SpotifyTokenResponse {

    @JsonProperty("access_token")
    private String accessToken;

    @JsonProperty("token_type")
    private String tokenType;

    private String scope;

    @JsonProperty("expires_in")
    private int expiresIn;

    @JsonProperty("refresh_token")
    private String refreshToken;

    public String getBearerToken(){
        return format("Bearer " + accessToken);
    }
}

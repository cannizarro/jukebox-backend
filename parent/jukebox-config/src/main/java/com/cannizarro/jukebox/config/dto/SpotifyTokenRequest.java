package com.cannizarro.jukebox.config.dto;

import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.Builder;
import lombok.Getter;
import lombok.Setter;


@Builder
@Getter
@Setter
@JsonInclude(JsonInclude.Include.NON_EMPTY)
public class SpotifyTokenRequest {

    private String grant_type;

    private String code;

    private String redirect_uri;

    private String refresh_token;

    private String client_id;
}

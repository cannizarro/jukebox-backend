package com.cannizarro.jukebox.dto;

import lombok.Builder;
import lombok.Getter;
import lombok.Setter;

import java.util.List;

@Getter
@Setter
@Builder
public class TrackDTO {
    private String name;
    private String album;
    private List<String> artists;
    private String image;
    private String id;
    private Integer length;
    private String uri;
}

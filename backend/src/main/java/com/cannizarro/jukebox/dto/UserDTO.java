package com.cannizarro.jukebox.dto;

import lombok.Getter;
import lombok.Setter;

import java.time.Instant;
import java.util.Set;

@Getter
@Setter
public class UserDTO {
    private String username;
    private String displayName;
    private Instant createTimeStamp;
    private Instant updateTimeStamp;
    private Set<String> roles;
    private String restaurantName;
    private Boolean online;
    private Float price;
}

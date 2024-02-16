package com.cannizarro.jukebox.dto;

import com.cannizarro.jukebox.jackson.StateDeserializer;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.Instant;
import java.util.List;

@Getter
@Setter
@Builder
@JsonDeserialize(using = StateDeserializer.class)
@JsonInclude(JsonInclude.Include.NON_NULL)
@NoArgsConstructor
@AllArgsConstructor
public class StateDTO {

    public StateDTO(String error){
        this.error = error;
        timestamp = Instant.now().toString();
    }

    public StateDTO(String error, String restaurantName){
        this.error = error;
        this.restaurantName = restaurantName;
        timestamp = Instant.now().toString();
    }

    private TrackDTO track;
    private DeviceDTO device;
    private boolean playing;
    private String timestamp;
    private List<TrackDTO> queue;
    private String error;

    //Customer specific attributes
    private String restaurantName;
    private Long secondsQueued;
}

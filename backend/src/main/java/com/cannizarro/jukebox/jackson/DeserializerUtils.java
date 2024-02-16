package com.cannizarro.jukebox.jackson;

import com.cannizarro.jukebox.dto.DeviceDTO;
import com.cannizarro.jukebox.dto.TrackDTO;
import com.fasterxml.jackson.databind.JsonNode;
import lombok.experimental.UtilityClass;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

@UtilityClass
public class DeserializerUtils {

    public static TrackDTO getTrack(JsonNode track){
        Iterator<JsonNode> iterator = track.get("artists").iterator();
        List<String> artists = new ArrayList<>();
        while (iterator.hasNext()) {
            artists.add(iterator.next().get("name").asText());
        }
        iterator = track.get("album").get("images").iterator();
        String albumName = track.get("album").get("album_type").asText().equals("single") ? "Single" : track.get("album").get("name").asText();
        return TrackDTO.builder()
                .id(track.get("id").asText())
                .album(albumName)
                .artists(artists)
                .name(track.get("name").asText())
                .length((track.get("duration_ms").asInt())/1000)
                .uri(track.get("uri").asText())
                .image(iterator.hasNext() ? iterator.next().get("url").asText() : null)
                .build();
    }

    public static DeviceDTO getDevice(JsonNode device){
        return DeviceDTO.builder()
                .id(device.get("id").asText())
                .name(device.get("name").asText())
                .build();
    }
}

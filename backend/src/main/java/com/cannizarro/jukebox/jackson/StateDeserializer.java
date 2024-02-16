package com.cannizarro.jukebox.jackson;

import com.cannizarro.jukebox.config.exception.JukeboxException;
import com.cannizarro.jukebox.constants.ErrorMessages;
import com.cannizarro.jukebox.dto.StateDTO;
import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.databind.DeserializationContext;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.deser.std.StdDeserializer;

import java.io.IOException;
import java.time.Instant;

public class StateDeserializer extends StdDeserializer<StateDTO> {
    public StateDeserializer(Class<?> vc) {
        super(vc);
    }

    public StateDeserializer(){
        this(null);
    }

    @Override
    public StateDTO deserialize(JsonParser jsonParser, DeserializationContext deserializationContext) throws IOException {
        JsonNode root = jsonParser.getCodec().readTree(jsonParser);

        if(!root.get("currently_playing_type").asText().equals("track"))
            throw new JukeboxException(ErrorMessages.ONLY_TRACK_TYPE_EXCEPTION);

        if(root.get("device").get("is_restricted").asBoolean())
            throw new JukeboxException(ErrorMessages.DEVICE_RESTRICTED_EXCEPTION);

        return StateDTO.builder()
                .track(DeserializerUtils.getTrack(root.get("item")))
                .device(DeserializerUtils.getDevice(root.get("device")))
                .timestamp(Instant.now().toString())
                .playing(root.get("is_playing").asBoolean())
                .build();
    }
}

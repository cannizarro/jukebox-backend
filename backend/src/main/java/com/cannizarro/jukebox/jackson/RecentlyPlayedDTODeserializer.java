package com.cannizarro.jukebox.jackson;

import com.cannizarro.jukebox.dto.PlayHistoryDTO;
import com.cannizarro.jukebox.dto.RecentlyPlayedDTO;
import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.databind.DeserializationContext;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.deser.std.StdDeserializer;

import java.io.IOException;
import java.time.Instant;
import java.time.ZoneOffset;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

public class RecentlyPlayedDTODeserializer extends StdDeserializer<RecentlyPlayedDTO> {
    private static final DateTimeFormatter fmt = DateTimeFormatter.ISO_INSTANT.withZone(ZoneOffset.UTC);
    public RecentlyPlayedDTODeserializer(Class<?> vc) {
        super(vc);
    }
    public RecentlyPlayedDTODeserializer(){
        this(null);
    }


    @Override
    public RecentlyPlayedDTO deserialize(JsonParser jsonParser, DeserializationContext deserializationContext) throws IOException {
        JsonNode root = jsonParser.getCodec().readTree(jsonParser);
        Iterator<JsonNode> iterator = root.get("items").iterator();
        List<PlayHistoryDTO> playHistoryDTOS = new ArrayList<>();
        while (iterator.hasNext()){
            JsonNode item = iterator.next();
            PlayHistoryDTO playHistoryDTO = new PlayHistoryDTO(
                    DeserializerUtils.getTrack(item.get("track")),
                    Instant.from(fmt.parse(item.get("played_at").asText()))
            );
            playHistoryDTOS.add(playHistoryDTO);
        }
        return new RecentlyPlayedDTO(playHistoryDTOS);
    }
}

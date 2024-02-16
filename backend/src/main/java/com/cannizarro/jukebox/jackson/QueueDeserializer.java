package com.cannizarro.jukebox.jackson;

import com.cannizarro.jukebox.dto.QueueDTO;
import com.cannizarro.jukebox.dto.TrackDTO;
import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.databind.DeserializationContext;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.deser.std.StdDeserializer;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

public class QueueDeserializer extends StdDeserializer<QueueDTO> {

    public QueueDeserializer(Class<?> vc) {
        super(vc);
    }
    public QueueDeserializer(){
        this(null);
    }
    @Override
    public QueueDTO deserialize(JsonParser jsonParser, DeserializationContext deserializationContext) throws IOException {
        JsonNode root = jsonParser.getCodec().readTree(jsonParser);
        JsonNode node;
        if(root.get("queue") == null){
            if(root.get("tracks").isArray())
                node = root.get("tracks");
            else
                node = root.get("tracks").get("items");
        }else{
            node = root.get("queue");
        }

        Iterator<JsonNode> iterator = node.iterator();
        List<TrackDTO> tracks = new ArrayList<>();
        while (iterator.hasNext()){
            tracks.add(DeserializerUtils.getTrack(iterator.next()));
        }
        return new QueueDTO(tracks);
    }
}

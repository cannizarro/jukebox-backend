package com.cannizarro.jukebox.dto;

import com.cannizarro.jukebox.jackson.QueueDeserializer;
import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.util.List;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@JsonDeserialize(using = QueueDeserializer.class)
public class QueueDTO {
     private List<TrackDTO> queue;
}

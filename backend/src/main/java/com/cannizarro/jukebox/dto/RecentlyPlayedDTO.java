package com.cannizarro.jukebox.dto;

import com.cannizarro.jukebox.jackson.RecentlyPlayedDTODeserializer;
import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.util.List;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@JsonDeserialize(using = RecentlyPlayedDTODeserializer.class)
public class RecentlyPlayedDTO {
   private List<PlayHistoryDTO> items;
}

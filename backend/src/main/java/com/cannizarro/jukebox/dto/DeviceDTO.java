package com.cannizarro.jukebox.dto;

import lombok.Builder;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@Builder
public class DeviceDTO {
    private String id;
    private String name;
}

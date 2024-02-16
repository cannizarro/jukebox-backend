package com.cannizarro.jukebox.dto;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class TransactionPageRequestDTO {
    private String startKey;
    private Boolean fulfilled;
    private Boolean ascending;
}

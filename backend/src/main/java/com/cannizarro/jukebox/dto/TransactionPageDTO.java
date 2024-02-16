package com.cannizarro.jukebox.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.util.List;

@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class TransactionPageDTO {
    private List<TransactionDTO> transactions;
    private String nextKey;
    private String currentKey;
}

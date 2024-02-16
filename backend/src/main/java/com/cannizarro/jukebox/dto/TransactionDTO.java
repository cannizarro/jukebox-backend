package com.cannizarro.jukebox.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Builder
@NoArgsConstructor
@AllArgsConstructor
@Getter
@Setter
public class TransactionDTO {
    private String transactionId;
    private Float price;
    private Boolean fulfilled;
    private String customerName;
    private String trackUrl;
    private String createTimestamp;
}

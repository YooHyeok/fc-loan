package com.fc.loan.dto;

import lombok.*;

import java.io.Serializable;
import java.math.BigDecimal;
import java.time.LocalDateTime;

public class BalanceDTO implements Serializable {

    @NoArgsConstructor
    @AllArgsConstructor
    @Getter
    @Builder
    public static class Request {
        private Long applicationId; // 어떤 신청정보의 상환인지
        private BigDecimal entryAmount; // 집행금액
    }

    @NoArgsConstructor
    @AllArgsConstructor
    @Getter
    @Builder
    public static class UpdateRequest {
        private Long applicationId; // 어떤 신청정보의 상환인지
        private BigDecimal beforeEntryAmount; // 집행금액
        private BigDecimal afterEntryAmount; // 집행금액
    }

    @NoArgsConstructor
    @AllArgsConstructor
    @Builder
    @Getter
    @Setter
    public static class Response {

        private Long balanceId;
        private Long applicationId;
        private BigDecimal balance;
        private LocalDateTime createdAt;
        private LocalDateTime updatedAt;

    }
}

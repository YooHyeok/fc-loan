package com.fc.loan.dto;

import lombok.*;

import java.io.Serializable;
import java.math.BigDecimal;
import java.time.LocalDateTime;

public class RepaymentDTO implements Serializable {
    @NoArgsConstructor
    @AllArgsConstructor
    @Builder
    @Getter
    @Setter
    public static class Request {
        private BigDecimal repaymentAmount; // 상환할 금액
    }
    @NoArgsConstructor
    @AllArgsConstructor
    @Builder
    @Getter
    @Setter
    public static class Response {
        private Long applicationId;
        private BigDecimal repaymentAmount; // 상환된 금액
        private BigDecimal balance; //상환 후 잔고
        private LocalDateTime createdAt;
        private LocalDateTime updatedAt;

    }
    @NoArgsConstructor
    @AllArgsConstructor
    @Builder
    @Getter
    @Setter
    public static class UpdateResponse {
        private Long applicationId;
        private BigDecimal beforeRepaymentAmount; // As-Is: 변경 전
        private BigDecimal afterRepaymentAmount; // To-Be: 변경 후
        private BigDecimal balance; //상환 후 잔고
        private LocalDateTime createdAt;
        private LocalDateTime updatedAt;

    }
    @NoArgsConstructor
    @AllArgsConstructor
    @Builder
    @Getter
    @Setter
    public static class ListResponse {
        private Long repaymentId;
        private BigDecimal repaymentAmount; // 상환된 금액
        private LocalDateTime createdAt;
        private LocalDateTime updatedAt;

    }
}

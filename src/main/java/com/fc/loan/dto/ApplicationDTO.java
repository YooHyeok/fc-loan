package com.fc.loan.dto;

import lombok.*;

import java.io.Serializable;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

public class ApplicationDTO implements Serializable {

    @NoArgsConstructor
    @AllArgsConstructor
    @Getter
    @Builder
    public static class Request {
        private String name;
        private String cellPhone;
        private String email;
        private BigDecimal hopeAmount;
    }

    @NoArgsConstructor
    @AllArgsConstructor
    @Builder
    @Getter
    @Setter
    public static class Response {
        private Long applicationId;
        private String name;
        private String cellPhone;
        private String email;
        private BigDecimal hopeAmount;
        private LocalDateTime appliedAt;
        private LocalDateTime createdAt;
        private LocalDateTime updatedAt;

    }

    /**
     * Ch.03-09 대출 신청 별 동의한 약관 정보 관리를 위한 동의약관 DTO(Request)
     * 이중 관계로 인한 중간 테이블 구성으로 DTO를 구성해야 한다.
     * 실제 약관 동의시 동의한 약관들을 List로 받는다.
     * 실제 신청정보에 반영되므로 ApplicationDTO에 생성한다.
     */
    @NoArgsConstructor
    @AllArgsConstructor
    @Builder
    @Getter
    @Setter
    public static class AcceptTerms {
        List<Long> acceptTermsIds;

    }

    /**
     * Ch.04-07 대출 심사 금액 부여 기능을 위한 금액부여 DTO(Response)
     * 어떠한 금액이 어떤 신청정보에 최종적으로 부여됐다 라는 의미
     * 실제 신청정보에 반영되므로 ApplicationDTO에 생성한다.
     */
    @NoArgsConstructor
    @AllArgsConstructor
    @Builder
    @Getter
    @Setter
    public static class GrantAmount {
        private Long applicationId; // 어떤 신청 정보에 대해 반영 되는가.
        private BigDecimal approvalAmount; // 얼마가 승인이 되는가
        private LocalDateTime createdAt; // 신청정보 생성 일자.
        private LocalDateTime updatedAt; // 신청정보 수정 일자.

    }

}

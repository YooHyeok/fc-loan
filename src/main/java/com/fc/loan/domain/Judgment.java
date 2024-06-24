package com.fc.loan.domain;

import lombok.*;
import org.hibernate.annotations.DynamicInsert;
import org.hibernate.annotations.DynamicUpdate;
import org.hibernate.annotations.Where;

import javax.persistence.*;
import java.math.BigDecimal;
@ToString
@Entity
@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
@DynamicInsert
@DynamicUpdate // 변경감지를 통해 변경된 컬럼만 Update되도록 설정 (SQL문에 출력됨)
@Where(clause = "is_deleted=false") // SELECT 조회시 is_deleted가 false인 경우만 조회되도록 WHERE절 설정
public class Judgment extends BaseEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(nullable = false, updatable = false)
    private Long judgmentId;

    @Column(columnDefinition = "BIGINT NOT NULL COMMENT '대출 신청 ID'")
    private Long applicationId;

    @Column(columnDefinition = "VARCHAR(12) DEFAULT NULL COMMENT '심사자'")
    private String name;

    @Column(columnDefinition = "BIGINT NOT NULL COMMENT '승인 금액'")
    private BigDecimal approvalAmount;
}
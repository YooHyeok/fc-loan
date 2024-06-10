package com.fc.loan.domain;

import lombok.*;
import org.hibernate.annotations.DynamicInsert;
import org.hibernate.annotations.DynamicUpdate;
import org.hibernate.annotations.Where;

import javax.persistence.*;
import java.math.BigDecimal;
import java.time.LocalDateTime;

@ToString
@Entity
@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
@DynamicInsert
@DynamicUpdate // 변경감지를 통해 변경된 컬럼만 Update되도록 설정 (SQL문에 출력됨)
//@SQLDelete(sql = "UPDATE application SET updated_at = NOW(), is_deleted = true WHERE appication_id = ?")
@Where(clause = "is_deleted=false") // SELECT 조회시 is_deleted가 false인 경우만 조회되도록 WHERE절 설정
public class Application extends BaseEntity {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(nullable = false, updatable = false)
    private Long applicationId;
    @Column(columnDefinition = "DATETIME DEFAULT NULL COMMENT '신청일자'")
    private LocalDateTime appliedAt;
    @Column(columnDefinition = "VARCHAR(12) DEFAULT NULL COMMENT '신청자'")
    private String name;
    @Column(columnDefinition = "VARCHAR(23) DEFAULT NULL COMMENT '전화번호'")
    private String cellPhone;
    @Column(columnDefinition = "VARCHAR(50) DEFAULT NULL COMMENT '신청자 이메일'")
    private String email;
    @Column(columnDefinition = "DECIMAL(5,4) DEFAULT NULL COMMENT '취급수수료'")
    private BigDecimal fee;
    @Column(columnDefinition = "DECIMAL(15,2) DEFAULT NULL COMMENT '대출 신청 금액'")
    private BigDecimal hopeAmount;
    @Column(columnDefinition = "DECIMAL(5,4) DEFAULT NULL COMMENT '금리'")
    private BigDecimal interestedRate;
    @Column(columnDefinition = "DATETIME DEFAULT NULL COMMENT '만기'")
    private LocalDateTime maturity;

}

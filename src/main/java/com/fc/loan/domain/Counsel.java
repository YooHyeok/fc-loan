package com.fc.loan.domain;

import lombok.*;
import org.hibernate.annotations.*;

import javax.persistence.*;
import javax.persistence.Entity;
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
@SQLDelete(sql = "UPDATE counsel SET deleted_at = NOW() where counsel_id = ?")
@Where(clause = "is_deleted=false") // SELECT 조회시 is_deleted가 false인 경우만 조회되도록 WHERE절 설정
public class Counsel extends BaseEntity {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(nullable = false, updatable = false)
    private Long counselId;
    @Column(nullable = false, columnDefinition = "DATETIME COMMENT '신청일자'")
    private LocalDateTime appliedAt;
    @Column(nullable = false, columnDefinition = "VARCHAR(12) COMMENT '상담 요청자'")
    private String name;
    @Column(nullable = false, columnDefinition = "VARCHAR(23) COMMENT '전화번호'")
    private String cellPhone;
    @Column(columnDefinition = "VARCHAR(50) DEFAULT NULL COMMENT '상담 요청자 이메일'")
    private String email;
    @Column(columnDefinition = "TEXT DEFAULT NULL COMMENT '상담 메모'")
    private String memo;
    @Column(columnDefinition = "VARCHAR(50) DEFAULT NULL COMMENT '주소'")
    private String address;
    @Column(columnDefinition = "VARCHAR(50) DEFAULT NULL COMMENT '상세주소'")
    private String addressDetail;
    @Column(columnDefinition = "VARCHAR(5) DEFAULT NULL COMMENT '우편번호'")
    private String zipCode;
}

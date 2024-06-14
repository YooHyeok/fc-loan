package com.fc.loan.domain;

import lombok.*;
import org.hibernate.annotations.DynamicInsert;
import org.hibernate.annotations.DynamicUpdate;
import org.hibernate.annotations.Where;

import javax.persistence.*;

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
public class Terms extends BaseEntity {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(nullable = false, updatable = false)
    private Long termsId;
    @Column(columnDefinition = "VARCHAR(255) DEFAULT NULL COMMENT '약관'")
    private String name;
    @Column(columnDefinition = "VARCHAR(255) DEFAULT NULL COMMENT '약관상세 URL'")
    private String termsDetailUrl;
}
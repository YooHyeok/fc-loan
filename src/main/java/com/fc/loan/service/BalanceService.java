package com.fc.loan.service;

import java.math.BigDecimal;

import static com.fc.loan.dto.BalanceDTO.*;

public interface BalanceService {
    Response create(Long applicationId, Request request);

    /* 대출 `집행` 등록/삭제: 수정(추가/삭제)용 */
    Response update(Long applicationId, UpdateRequest request);

    /* 대출 `상환` 등록/삭제: 수정(추가/삭제)용 */
    Response repaymentUpdate(Long applicationId, RepaymentRequest request);
}
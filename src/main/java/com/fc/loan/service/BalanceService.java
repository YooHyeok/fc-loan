package com.fc.loan.service;

import java.math.BigDecimal;

import static com.fc.loan.dto.BalanceDTO.*;

public interface BalanceService {
    Response create(Long applicationId, Request request);

    Response update(Long applicationId, UpdateRequest request);
}
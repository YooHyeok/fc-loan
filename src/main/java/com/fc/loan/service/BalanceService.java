package com.fc.loan.service;

import static com.fc.loan.dto.BalanceDTO.*;

public interface BalanceService {
    Response create(Long applicationId, Request request);
}
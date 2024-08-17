package com.fc.loan.service;

import com.fc.loan.dto.RepaymentDTO;

import static com.fc.loan.dto.RepaymentDTO.*;

public interface RepaymentService {
    Response create(Long applicationId, Request request);

}

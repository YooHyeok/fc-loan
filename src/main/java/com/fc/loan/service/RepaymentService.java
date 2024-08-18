package com.fc.loan.service;

import com.fc.loan.dto.RepaymentDTO;

import java.util.List;

import static com.fc.loan.dto.RepaymentDTO.*;

public interface RepaymentService {
    Response create(Long applicationId, Request request);
    List<ListResponse> get(Long applicationId);

}

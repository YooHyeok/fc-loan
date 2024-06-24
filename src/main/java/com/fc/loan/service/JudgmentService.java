package com.fc.loan.service;

import java.util.List;

import static com.fc.loan.dto.JudgmentDTO.Request;
import static com.fc.loan.dto.JudgmentDTO.Response;

public interface JudgmentService {
    Response create(Request request);
    Response get(Long judgmentId);

}

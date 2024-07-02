package com.fc.loan.service;

import com.fc.loan.dto.ApplicationDTO;

import static com.fc.loan.dto.JudgmentDTO.Request;
import static com.fc.loan.dto.JudgmentDTO.Response;

public interface JudgmentService {
    Response create(Request request);
    Response get(Long judgmentId);
    Response getJudgmentOfApplication(Long applicationId);
    Response update(Long judgmentId, Request request);
    void delete(Long judgmentId);

    ApplicationDTO.GrantAmount grant(Long judgmentId); // GrantAmount 어떠한 금액이 어떤 신청정보에 최종적으로 부여됐다 라는 의미


}

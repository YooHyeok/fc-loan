package com.fc.loan.service;

import com.fc.loan.dto.CounselDTO;

import static com.fc.loan.dto.CounselDTO.*;

public interface CounselService {
    Response create(Request request);
    Response get(Long counselId);
    Response update(Long counselId, Request request);
}

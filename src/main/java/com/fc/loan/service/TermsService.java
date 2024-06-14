package com.fc.loan.service;

import static com.fc.loan.dto.TermsDTO.Request;
import static com.fc.loan.dto.TermsDTO.Response;

public interface TermsService {
    Response create(Request request);

}

package com.fc.loan.service;

import java.util.List;

import static com.fc.loan.dto.TermsDTO.Request;
import static com.fc.loan.dto.TermsDTO.Response;

public interface TermsService {
    Response create(Request request);
    List<Response> getAll();

}

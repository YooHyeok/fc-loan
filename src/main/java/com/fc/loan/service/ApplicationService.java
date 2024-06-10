package com.fc.loan.service;

import static com.fc.loan.dto.ApplicationDTO.Request;
import static com.fc.loan.dto.ApplicationDTO.Response;

public interface ApplicationService {
    Response create(Request request);

}

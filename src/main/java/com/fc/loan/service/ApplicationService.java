package com.fc.loan.service;




import static com.fc.loan.dto.ApplicationDTO.*;

public interface ApplicationService {
    /* --- 대출 신청 --- */
    Response create(Request request);
    Response get(Long applicationId);
    Response update(Long applicationId, Request request);

    void delete(Long applicationId);

    /* --- 대출 신청 이용 약관 --- */
    Boolean acceptTerms(Long applicationId, AcceptTerms request); // 약관 동의
}

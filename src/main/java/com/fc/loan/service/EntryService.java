package com.fc.loan.service;

import com.fc.loan.dto.EntryDTO;

import static com.fc.loan.dto.EntryDTO.*;

public interface EntryService {

    /* 대출 집행 등록 기능 */
    Response create(Long applicationId, Request request);

    /* 대출 집행 조회 기능 */
    Response get(Long applicationId);

    /* 대출 집행 수정 기능 */
    UpdateResponse update(Long entryId, Request request);

    /* 대출 집행 삭제 기능 */
    void delete(Long entryId);
}

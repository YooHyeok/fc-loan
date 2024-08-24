package com.fc.loan.controller;

import com.fc.loan.dto.RepaymentDTO;
import com.fc.loan.dto.ResponseDTO;
import com.fc.loan.service.EntryService;
import com.fc.loan.service.RepaymentService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.List;

import static com.fc.loan.dto.EntryDTO.*;

/**
 * <pre>
 * Internal - 내부용
 * 계약 채결을 내부에서 한다고 가정한다.
 * </pre>
 */
@RequiredArgsConstructor
@RestController
@RequestMapping("/internal/applications")
public class InternalController extends AbstractController{

    private final EntryService entryService;
    private final RepaymentService repaymentService;

    @PostMapping("/{applicationId}/entries")
    public ResponseDTO<Response> create(@PathVariable Long applicationId, @RequestBody Request request) {
        return ok(entryService.create(applicationId, request));
    }

    @GetMapping("/{applicationId}/entries")
    public ResponseDTO<Response> get(@PathVariable Long applicationId) {
        return ok(entryService.get(applicationId));
    }

    @PutMapping("/entries/{entryId}")
    public ResponseDTO<UpdateResponse> update(@PathVariable Long entryId, @RequestBody Request request) {
        return ok(entryService.update(entryId, request));
    }
    @DeleteMapping("/entries/{entryId}")
    public ResponseDTO<Void> delete(@PathVariable Long entryId) {
        entryService.delete(entryId);
        return ok();
    }

    @PostMapping("/{applicationId}/repayments")
    public ResponseDTO<RepaymentDTO.Response> createRepayments(@PathVariable Long applicationId, @RequestBody RepaymentDTO.Request request) {
        return ok(repaymentService.create(applicationId, request));
    }
    @GetMapping("/{applicationId}/repayments")
    public ResponseDTO<List<RepaymentDTO.ListResponse>> getRepayments(@PathVariable Long applicationId) {
        return ok(repaymentService.get(applicationId));
    }
    @PutMapping("/{applicationId}/repayments")
    public ResponseDTO<RepaymentDTO.UpdateResponse> updateRepayments(@PathVariable Long applicationId, @RequestBody RepaymentDTO.Request request) {
        return ok(repaymentService.update(applicationId, request));
    }

    @PutMapping("/{applicationId}/repayments")
    public ResponseDTO<Void> deleteRepayments(@PathVariable Long applicationId) {
        repaymentService.delete(applicationId);
        return ok();
    }
}

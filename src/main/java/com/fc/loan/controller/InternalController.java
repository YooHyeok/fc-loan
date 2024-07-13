package com.fc.loan.controller;

import com.fc.loan.dto.ResponseDTO;
import com.fc.loan.service.EntryService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

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

    @PostMapping("/{applicationId}/entries")
    public ResponseDTO<Response> create(@PathVariable Long applicationId, @RequestBody Request request) {
        return ok(entryService.create(applicationId, request));
    }

}

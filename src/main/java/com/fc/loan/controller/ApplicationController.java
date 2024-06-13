package com.fc.loan.controller;

import com.fc.loan.dto.ApplicationDTO;
import com.fc.loan.dto.ResponseDTO;
import com.fc.loan.service.ApplicationService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import static com.fc.loan.dto.ApplicationDTO.*;


@RequiredArgsConstructor
@RestController
@RequestMapping("/applications")
public class ApplicationController extends AbstractController{

    private final ApplicationService counselService;

    @PostMapping
    public ResponseDTO<Response> create(@RequestBody Request request) {
        return ok(counselService.create(request)); // AbstractController에 정의된 ok 메소드 호출
    }

    @GetMapping("/{applicationId}")
    public ResponseDTO<Response> create(@PathVariable Long applicationId) {
        return ok(counselService.get(applicationId)); // AbstractController에 정의된 ok 메소드 호출
    }

    @PutMapping("/{applicationId}")
    public ResponseDTO<Response> update(@PathVariable Long applicationId, @RequestBody Request request) {
        return ok(counselService.update(applicationId, request)); // AbstractController에 정의된 ok 메소드 호출
    }

}

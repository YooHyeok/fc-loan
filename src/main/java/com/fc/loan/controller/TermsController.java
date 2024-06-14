package com.fc.loan.controller;

import com.fc.loan.dto.ResponseDTO;
import com.fc.loan.service.TermsService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import static com.fc.loan.dto.TermsDTO.*;


@RequiredArgsConstructor
@RequestMapping("/terms")
@RestController
public class TermsController extends AbstractController{
    private final TermsService termsService;

    @PostMapping
    public ResponseDTO<Response> create(@RequestBody Request request) {
        return ok(termsService.create(request));
    }
}

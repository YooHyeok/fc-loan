package com.fc.loan.controller;

import com.fc.loan.dto.JudgmentDTO;
import com.fc.loan.dto.ResponseDTO;
import com.fc.loan.service.JudgmentService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import static com.fc.loan.dto.JudgmentDTO.*;

@RequiredArgsConstructor
@RestController
@RequestMapping("/judgments")
public class JudgmentController extends AbstractController{

    private final JudgmentService judgmentService;

    @PostMapping
    public ResponseDTO<Response> create (@RequestBody Request request) {
        return ok(judgmentService.create(request));
    }

}

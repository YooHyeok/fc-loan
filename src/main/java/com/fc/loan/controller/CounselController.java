package com.fc.loan.controller;

import com.fc.loan.dto.CounselDTO;
import com.fc.loan.dto.ResponseDTO;
import com.fc.loan.service.CounselService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import static com.fc.loan.dto.CounselDTO.*;

@RequiredArgsConstructor
@RestController
@RequestMapping("/counsels")
public class CounselController extends AbstractController{

    private final CounselService counselService;

    @PostMapping
    public ResponseDTO<Response> create(@RequestBody Request request) {
        return ok(counselService.create(request)); // AbstractController에 정의된 ok 메소드 호출
    }
}

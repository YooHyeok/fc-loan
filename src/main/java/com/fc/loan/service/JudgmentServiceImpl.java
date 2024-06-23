package com.fc.loan.service;


import com.fc.loan.domain.Judgment;
import com.fc.loan.domain.JudgmentRepository;
import com.fc.loan.exception.BaseException;
import com.fc.loan.exception.ResultType;
import com.fc.loan.repository.ApplicationRepository;
import lombok.RequiredArgsConstructor;
import org.modelmapper.ModelMapper;
import org.springframework.stereotype.Service;

import static com.fc.loan.dto.JudgmentDTO.*;

@Service
@RequiredArgsConstructor
public class JudgmentServiceImpl implements JudgmentService {

    private final ModelMapper modelMapper; // ModelMapperConfig로 등록한 ModelMapper Bean 생성자 의존성 주입
    private final JudgmentRepository judgmentRepository;
    private final ApplicationRepository applicationRepository;


    @Override
    public Response create(Request request) {
        /**
         * 신청 정보 검증 (Validate)
         * request DTO -> Entity mapping -> Save
         * Save -> return ResponseDTO
         */


        if (!isPresentApplication(request)) throw new BaseException(ResultType.SYSTEM_ERROR);
        Judgment entity = modelMapper.map(request, Judgment.class);
        return modelMapper.map(judgmentRepository.save(entity), Response.class);
    }

    private boolean isPresentApplication(Request request) {
        return applicationRepository.existsById(request.getApplicationId());
    }
}

package com.fc.loan.service;


import com.fc.loan.domain.Judgment;
import com.fc.loan.domain.JudgmentRepository;
import com.fc.loan.exception.BaseException;
import com.fc.loan.exception.ResultType;
import com.fc.loan.repository.ApplicationRepository;
import lombok.RequiredArgsConstructor;
import org.modelmapper.ModelMapper;
import org.springframework.stereotype.Service;

import static com.fc.loan.dto.JudgmentDTO.Request;
import static com.fc.loan.dto.JudgmentDTO.Response;

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


        if (!isPresentApplication(request.getApplicationId())) throw new BaseException(ResultType.SYSTEM_ERROR);
        Judgment entity = modelMapper.map(request, Judgment.class);
        return modelMapper.map(judgmentRepository.save(entity), Response.class);
    }

    @Override
    public Response get(Long judgmentId) {
        Judgment judgment = judgmentRepository.findById(judgmentId).orElseThrow(() -> {
                    throw new BaseException(ResultType.SYSTEM_ERROR);
                });
        return modelMapper.map(judgment, Response.class);
    }

    @Override
    public Response getJudgmentOfApplication(Long applicationId) {

        if (!isPresentApplication(applicationId)) throw new BaseException(ResultType.SYSTEM_ERROR);

        Judgment judgment = judgmentRepository.findByApplicationId(applicationId).orElseThrow(() -> {
            throw new BaseException(ResultType.SYSTEM_ERROR);
        });

        return modelMapper.map(judgment, Response.class);
    }

    @Override
    public Response update(Long judgmentId, Request request) {
        Judgment judgment = judgmentRepository.findById(judgmentId).orElseThrow(() -> {
            throw new BaseException(ResultType.SYSTEM_ERROR);
        });
        judgment.setName(request.getName());
        judgment.setApprovalAmount(request.getApprovalAmount());
        judgmentRepository.save(judgment);
        return modelMapper.map(judgment, Response.class);
    }

    @Override
    public void delete(Long judgmentId) {
        Judgment judgment = judgmentRepository.findById(judgmentId).orElseThrow(() -> {
            throw new BaseException(ResultType.SYSTEM_ERROR);
        });
        judgment.setIsDeleted(true);
        judgmentRepository.save(judgment);
    }

    private boolean isPresentApplication(Long applicationId) {
        return applicationRepository.existsById(applicationId);
    }
}

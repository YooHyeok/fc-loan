package com.fc.loan.service;

import com.fc.loan.domain.Application;
import com.fc.loan.exception.BaseException;
import com.fc.loan.exception.ResultType;
import com.fc.loan.repository.ApplicationRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.modelmapper.ModelMapper;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;

import static com.fc.loan.dto.ApplicationDTO.*;


@Slf4j
@Service
@RequiredArgsConstructor
public class ApplicationServiceImpl implements ApplicationService {

    private final ModelMapper modelMapper; // ModelMapperConfig로 등록한 ModelMapper Bean 생성자 의존성 주입
    private final ApplicationRepository applicationRepository;

    @Override
    public Response create(Request request) {
        Application application = modelMapper.map(request, Application.class); // 요청 객체를 Counsel Entity클래스로 매핑하여 반환
        application.setAppliedAt(LocalDateTime.now());
        log.info("mapped Counsel Data : {}", application);
        return modelMapper.map(applicationRepository.save(application), Response.class);
    }

    @Override
    public Response get(Long applicationId) {
        Application application = applicationRepository.findById(applicationId).orElseThrow(() -> {
            throw new BaseException(ResultType.SYSTEM_ERROR);
        });
        return modelMapper.map(application, Response.class);
    }

}

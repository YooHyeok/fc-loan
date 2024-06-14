package com.fc.loan.service;

import com.fc.loan.domain.Application;
import com.fc.loan.domain.Terms;
import com.fc.loan.exception.BaseException;
import com.fc.loan.exception.ResultType;
import com.fc.loan.repository.ApplicationRepository;
import com.fc.loan.repository.TermsRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.modelmapper.ModelMapper;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;

import static com.fc.loan.dto.TermsDTO.Request;
import static com.fc.loan.dto.TermsDTO.Response;


@Slf4j
@Service
@RequiredArgsConstructor
public class TermsServiceImpl implements TermsService {

    private final ModelMapper modelMapper; // ModelMapperConfig로 등록한 ModelMapper Bean 생성자 의존성 주입
    private final TermsRepository termsRepository;

    @Override
    public Response create(Request request) {
        Terms terms = modelMapper.map(request, Terms.class); // 요청 객체를 Counsel Entity클래스로 매핑하여 반환
        log.info("mapped Terms Data : {}", terms);
        return modelMapper.map(termsRepository.save(terms), Response.class);
    }

}

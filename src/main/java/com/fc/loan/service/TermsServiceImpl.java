package com.fc.loan.service;

import com.fc.loan.domain.Application;
import com.fc.loan.domain.Terms;
import com.fc.loan.dto.TermsDTO;
import com.fc.loan.exception.BaseException;
import com.fc.loan.exception.ResultType;
import com.fc.loan.repository.ApplicationRepository;
import com.fc.loan.repository.TermsRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.modelmapper.ModelMapper;
import org.modelmapper.TypeToken;
import org.springframework.stereotype.Service;

import java.util.List;

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

    @Override
    public List<Response> getAll() {
//        return termsRepository.findAll().stream().map(terms -> modelMapper.map(terms, Response.class)).collect(Collectors.toList()); // 강의에서 사용한 코드 stream은 비효율적일 수 도 있음
//        return modelMapper.map(termsRepository.findAll(), List.class); // 제네릭 타입 정보 손실 및 타입 안정성 문제 발생 가능성 높음
        return modelMapper.map(termsRepository.findAll(), new TypeToken<List<Response>>() {}.getType()); // TypeToken의 제네릭 타입 정보 전달 (TypeToken의 Type인 List<Response> 전달 - reflection)

    }

}

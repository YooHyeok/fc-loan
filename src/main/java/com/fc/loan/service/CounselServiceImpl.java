package com.fc.loan.service;

import com.fc.loan.domain.Counsel;
import com.fc.loan.dto.CounselDTO;
import com.fc.loan.exception.BaseException;
import com.fc.loan.exception.ResultType;
import com.fc.loan.repository.CounselRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.modelmapper.ModelMapper;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;

import static com.fc.loan.dto.CounselDTO.*;

@Slf4j
@Service
@RequiredArgsConstructor
public class CounselServiceImpl implements CounselService {

    private final ModelMapper modelMapper; // ModelMapperConfig로 등록한 ModelMapper Bean 생성자 의존성 주입
    private final CounselRepository counselRepository;

    @Override
    public Response create(Request request) {
        Counsel counsel = modelMapper.map(request, Counsel.class); // 요청 객체를 Counsel Entity클래스로 매핑하여 반환
        counsel.setAppliedAt(LocalDateTime.now());
        log.info("mapped Counsel Data : {}", counsel);
        return modelMapper.map(counselRepository.save(counsel), Response.class);
    }

    @Override
    public Response get(Long counselId) {
        Counsel counsel = counselRepository.findById(counselId).orElseThrow(() -> {
            throw new BaseException(ResultType.SYSTEM_ERROR);
        });
        return modelMapper.map(counsel, Response.class);
    }

    @Override
    public Response update(Long counselId, Request request) {
        Counsel counsel = counselRepository.findById(counselId).orElseThrow(() -> {
            throw new BaseException(ResultType.SYSTEM_ERROR);
        });
        /**
         * 만약 공백으로 넘겨받은 데이터는
         * Validation 처리를 해야하지만 (Error처리등, 업데이트 조건 정의)
         * 최대한 심플하게 그대로 공백으로 update 한다.
         */
        counsel.setName(request.getName());
        counsel.setCellPhone(request.getCellPhone());
        counsel.setEmail(request.getEmail());
        counsel.setMemo(request.getMemo());
        counsel.setAddress(request.getAddress());
        counsel.setAddressDetail(request.getAddressDetail());
        counsel.setZipCode(request.getZipCode());
        return modelMapper.map(counselRepository.save(counsel), Response.class);
    }
}

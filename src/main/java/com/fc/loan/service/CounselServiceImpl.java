package com.fc.loan.service;

import com.fc.loan.domain.Counsel;
import com.fc.loan.dto.CounselDTO;
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
}

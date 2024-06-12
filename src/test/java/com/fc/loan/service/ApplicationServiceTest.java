package com.fc.loan.service;

import com.fc.loan.domain.Application;
import com.fc.loan.domain.Counsel;
import com.fc.loan.exception.BaseException;
import com.fc.loan.exception.ResultType;
import com.fc.loan.repository.ApplicationRepository;
import com.fc.loan.repository.CounselRepository;
import org.assertj.core.api.Assertions;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.*;
import org.mockito.junit.jupiter.MockitoExtension;
import org.modelmapper.ModelMapper;

import java.math.BigDecimal;
import java.util.Optional;

import static com.fc.loan.dto.ApplicationDTO.*;

@ExtendWith(MockitoExtension.class)
class ApplicationServiceTest {

    @InjectMocks
    ApplicationServiceImpl applicationService;

    @Mock
    private ApplicationRepository applicationRepository;

    @Spy // ModelMapper는 서로 다른 오브젝트를 매핑해주는 유틸성 객체이므로 따로 모킹처리 없이 역할 자체를 순수하게 하기 위해서 @Spy로 지정한다
    private ModelMapper modelMapper;

    @Test
    @DisplayName("상담 요청이 왔을 때 새로운 상담 엔티티에 대한 응답을 반환받아야 한다.")
    void Should_ReturnResponseOfNewCounselEntity_When_RequestCounsel() throws Exception {
        //given
        Application entity = Application.builder()
                .name("Member Yoo")
                .cellPhone("010-1111-2222")
                .email("abc@def.g")
                .hopeAmount(BigDecimal.valueOf(5000000))
                .build();
        Request request = Request.builder()
                .name("Member Yoo")
                .cellPhone("010-1111-2222")
                .email("abc@def.g")
                .hopeAmount(BigDecimal.valueOf(5000000))// 5천만원 대출 요청 객체
                .build();

        //mocking - 특정 값이 들어왔을 때 Counsel을 반환하도록 모킹
        Mockito.when(applicationRepository.save(ArgumentMatchers.any(Application.class))).thenReturn(entity);

        //when - 요청객체를 전달하며 create메소드를 호출하여 내부 save메소드에 대한 mocking when이 적용되고 기대값으로 Response객체를 반환받게 된다.
        Response actual = applicationService.create(request);

        //then
        Assertions.assertThat(actual.getHopeAmount()).isEqualTo(request.getHopeAmount()); // 객체가 같은 값을 지니고 있는지 비교
        Assertions.assertThat(actual.getName()).isSameAs(entity.getName()); // 메모리상 같은 객체를 가리키는지 주소 비교
    }

    @Test
    @DisplayName("존재하는 상담 신청 ID로 요청이 왔을 때 존재하는 상담신청 엔티티를 응답객체로 반환한다.")
    void Should_ReturnResponseOfExistApplicationEntity_When_RequestExistApplicationId() throws Exception {
        //given
        Long findId = 1L;

        Application entity = Application.builder()
                .applicationId(1L)
                .build();
        //mocking - 특정 값이 들어왔을 때 Counsel을 반환하도록 모킹
        Mockito.when(applicationRepository.findById(findId)).thenReturn(Optional.ofNullable(entity));

        //when
        Response actual = applicationService.get(findId);

        //then
        Assertions.assertThat(actual.getApplicationId()).isSameAs(entity.getApplicationId());
    }
}
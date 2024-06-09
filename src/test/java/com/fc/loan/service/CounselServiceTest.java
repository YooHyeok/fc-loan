package com.fc.loan.service;

import com.fc.loan.domain.Counsel;
import com.fc.loan.exception.BaseException;
import com.fc.loan.exception.ResultType;
import com.fc.loan.repository.CounselRepository;
import org.assertj.core.api.Assertions;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.*;
import org.mockito.junit.jupiter.MockitoExtension;
import org.modelmapper.ModelMapper;

import java.util.Optional;

import static com.fc.loan.dto.CounselDTO.*;

/**
 * Mockito를 활용하여 CounselService 레이어에 대한 비즈니스 로직을 테스트한다 <br/>
 * 이외의 다른 레이어 호출하는 부분에 대해서 혹은 다른 의존성이 걸려있는 영역에 대해서는 검증할 필요가 없다 <br/>
 * 따라서 CounselService 외에 호출되는 부분에 대해서는 이미 성공을 했다는 가정하에 <br/>
 * 외부 호출로 부터 반환되는 기대 결과가 들어온다고 가정을 하고,
 * CounselService에 구현되어 있는 순수 비즈니스 로직만 검증하는 형식으로 진행할 예정이다 <br/>
 * 따라서 해당 부분을 모킹 처리한다
 */
@ExtendWith(MockitoExtension.class)
class CounselServiceTest {

    @InjectMocks
    CounselServiceImpl counselService;

    @Mock
    private CounselRepository counselRepository;

    @Spy // ModelMapper는 서로 다른 오브젝트를 매핑해주는 유틸성 객체이므로 따로 모킹처리 없이 역할 자체를 순수하게 하기 위해서 @Spy로 지정한다
    private ModelMapper modelMapper;

    @Test
    @DisplayName("상담 요청이 왔을 때 새로운 상담 엔티티에 대한 응답을 반환받아야 한다.")
    void Should_ReturnResponseOfNewCounselEntity_When_RequestCounsel() throws Exception {
        //given
        Counsel entity = Counsel.builder()
                .name("Member Yoo")
                .cellPhone("010-1111-2222")
                .email("abc@def.g")
                .memo("저는 대출을 받고 싶어요. 연락을 주세요.")
                .address("12345")
                .addressDetail("서울특별시 어딘구 모른동")
                .zipCode("101동 101호")
                .build();
        Request request = Request.builder()
                .name("Member Yoo")
                .cellPhone("010-1111-2222")
                .email("abc@def.g")
                .memo("저는 대출을 받고 싶어요. 연락을 주세요.")
                .address("12345")
                .addressDetail("서울특별시 어딘구 모른동")
                .zipCode("101동 101호")
                .build();

        //mocking - 특정 값이 들어왔을 때 Counsel을 반환하도록 모킹
        Mockito.when(counselRepository.save(ArgumentMatchers.any(Counsel.class))).thenReturn(entity);

        //when - 요청객체를 전달하며 create메소드를 호출하여 내부 save메소드에 대한 mocking when이 적용되고 기대값으로 Response객체를 반환받게 된다.
        Response actual = counselService.create(request);

        //then
        Assertions.assertThat(actual.getName()).isEqualTo(request.getName()); // 객체가 같은 값을 지니고 있는지 비교
        Assertions.assertThat(actual.getName()).isSameAs(entity.getName()); // 메모리상 같은 객체를 가리키는지 주소 비교
    }

    @Test
    @DisplayName("대출 상담에 대한 아이디가 존재할 때 존재하는 상담 엔티티를 조회하여 반환받아야 한다.")
    void Should_ReturnResponseOfExistCounselEntity_When_RequestExistCounselId() throws Exception {
        //given
        Long findId = 1L;
        Counsel entity = Counsel.builder()
                .counselId(1L)
                .build();

        //mocking - 특정 값이 들어왔을 때 Null을 허용한 Optional Counsel을 반환하도록 모킹
        Mockito.when(counselRepository.findById(findId)).thenReturn(Optional.ofNullable(entity));

        //when
        Response actual = counselService.get(1L);

        //then
        Assertions.assertThat(actual.getCounselId()).isSameAs(findId);

    }

    @Test
    @DisplayName("존재하지 않는 상담 ID로 조회시 Exception Throw 해야 한다.")
    void Should_ThrowException_When_RequestNotExistCounselId() throws Exception {
        //given
        Long findId = 2L;

        //mocking - 특정 값이 들어왔을 때 Null을 허용한 Optional Counsel을 반환하도록 모킹
        Mockito.when(counselRepository.findById(findId)).thenThrow(new BaseException(ResultType.SYSTEM_ERROR));

        //when & then
        org.junit.jupiter.api.Assertions.assertThrows(BaseException.class, () -> counselService.get(findId));

    }

    @Test
    @DisplayName("정보가 존재하는 상담에 대한 수정 요청이 왔을 때 수정된 존재하는 상담 엔티티를 반환해야 한다.")
    void Should_ReturnUpdatedResponseOfExistCounselEntity_When_RequestUpdateExistCounselInfo() throws Exception {
        //given
        Long findId = 1L;

        /* 변경 완료 후 반환된 Entity */
        Counsel entity = Counsel.builder()
                .counselId(1L)
                .name("Member Kim")
                .build();

        /* 변경할 요청 객체 */
        Request request = Request.builder()
                .name("Member Kang")
                .build();
        //mocking - 특정 값이 들어왔을 때 Null을 허용한 Optional Counsel을 반환하도록 모킹
        Mockito.when(counselRepository.findById(findId)).thenReturn(Optional.ofNullable(entity));
        //mocking - 특정 값이 들어왔을 때 Counsel을 반환하도록 모킹
        Mockito.when(counselRepository.save(ArgumentMatchers.any(Counsel.class))).thenReturn(entity);

        //when
        Response actual = counselService.update(findId, request);

        //then
        Assertions.assertThat(actual.getCounselId()).isSameAs(findId);
        Assertions.assertThat(actual.getName()).isSameAs(request.getName());
    }
}
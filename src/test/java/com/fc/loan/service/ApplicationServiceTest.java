package com.fc.loan.service;

import com.fc.loan.domain.AcceptTerms;
import com.fc.loan.domain.Application;
import com.fc.loan.domain.Counsel;
import com.fc.loan.domain.Terms;
import com.fc.loan.dto.ApplicationDTO;
import com.fc.loan.exception.BaseException;
import com.fc.loan.exception.ResultType;
import com.fc.loan.repository.AcceptTermsRepository;
import com.fc.loan.repository.ApplicationRepository;
import com.fc.loan.repository.CounselRepository;
import com.fc.loan.repository.TermsRepository;
import org.assertj.core.api.Assertions;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.*;
import org.mockito.junit.jupiter.MockitoExtension;
import org.modelmapper.ModelMapper;
import org.springframework.data.domain.Sort;

import java.math.BigDecimal;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;

import static com.fc.loan.dto.ApplicationDTO.*;

@ExtendWith(MockitoExtension.class)
class ApplicationServiceTest {

    @InjectMocks
    ApplicationServiceImpl applicationService;

    @Mock
    private ApplicationRepository applicationRepository;
    @Mock
    private TermsRepository termsRepository;
    @Mock
    private AcceptTermsRepository acceptTermsRepository;

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

    @Test
    @DisplayName("존재하는 상담 신청 ID로 수정 요청이 왔을 때 존재하는 상담신청 수정된 엔티티를 응답객체로 반환한다.")
    void Should_ReturnUpdatedResponseOfExistApplicationEntity_When_RequestUpdateExistApplicationInfo() throws Exception {
        //given
        Long targetId = 1L;
        Application entity = Application.builder()
                .applicationId(targetId)
                .name("Member Yoo")
                .hopeAmount(BigDecimal.valueOf(5000000))
                .build();
        Request request = Request.builder()
                .name("Member U")
                .hopeAmount(BigDecimal.valueOf(4000000))// 4천만원 대출 요청 객체
                .build();

        //mocking - 특정 값이 들어왔을 때 Counsel을 반환하도록 모킹
        Mockito.when(applicationRepository.findById(targetId)).thenReturn(Optional.ofNullable(entity));
        Mockito.when(applicationRepository.save(ArgumentMatchers.any(Application.class))).thenReturn(entity);

        //when - 요청객체를 전달하며 create메소드를 호출하여 내부 save메소드에 대한 mocking when이 적용되고 기대값으로 Response객체를 반환받게 된다.
        Response actual = applicationService.update(targetId, request);

        //then
        Assertions.assertThat(actual.getApplicationId()).isSameAs(targetId);
        Assertions.assertThat(actual.getHopeAmount()).isSameAs(request.getHopeAmount());
        Assertions.assertThat(actual.getName()).isSameAs(request.getName()); // 메모리상 같은 객체를 가리키는지 주소 비교
        Assertions.assertThat(actual.getName()).isSameAs(entity.getName()); // Entity는 영속화되어서 수정후 값이 반영된다.

    }
    @Test
    @DisplayName("정보가 존재하는 신청에 대한 삭제 요청이 왔을 때 신청 엔티티를 삭제한다.")
    void Should_DeletedApplicationEntity_When_RequestDeleteExistApplicationInfo() throws Exception {
        //given
        Long targetId = 1L;

        Application entity = Application.builder()
                .applicationId(1L)
                .build();

        //Mocking
        Mockito.when(applicationRepository.findById(targetId)).thenReturn(Optional.ofNullable(entity));
        Mockito.when(applicationRepository.save(ArgumentMatchers.any(Application.class))).thenReturn(entity);

        //when
        applicationService.delete(targetId);

        //then
        Assertions.assertThat(entity.getIsDeleted()).isSameAs(true);

    }

    @Test
    @DisplayName("대출신청의 동의약관에 대한 요청이 왔을 때 동의 약관을 추가한다.")
    void Should_AddAcceptTerms_When_RequestAcceptTermsOfApplication() throws Exception {
        //given
        Terms entityA = Terms.builder()
                .termsId(1L)
                .name("약관 1")
                .termsDetailUrl("https://abcd.efg1")
                .build();

        Terms entityB = Terms.builder()
                .termsId(2L)
                .name("약관 2")
                .termsDetailUrl("https://abcd.efg2")
                .build();

        // applicationId
        Long applicationIdId = 1L;

        // 1번과 2번의 약관을 동의하여 RequestDTO인 AcceptTerms 정적 중첩 요청객체에 담아 전달
        ApplicationDTO.AcceptTerms request =
                ApplicationDTO.AcceptTerms.builder()
                .acceptTermsIds(Arrays.asList(1L, 2L)) // 약관 1, 2
                .build();

        // Mocking!
        Mockito.when(applicationRepository.findById(applicationIdId)).thenReturn(Optional.ofNullable(new Application()));
        Mockito.when(termsRepository.findAll(Sort.by(Sort.Direction.ASC, "termsId"))).thenReturn(List.of(entityA, entityB));
//        Mockito.when(acceptTermsRepository.save(ArgumentMatchers.any(AcceptTerms.class))).thenReturn(new AcceptTerms()); // 반복문으로 할경우 이렇게 테스트
        Mockito.when(acceptTermsRepository.saveAll(ArgumentMatchers.anyList())).thenReturn(List.of(new AcceptTerms(), new AcceptTerms())); // save All

        //when
        Boolean actual = applicationService.acceptTerms(applicationIdId, request);

        //then
        Assertions.assertThat(actual).isTrue();
    }

    @Test
    @DisplayName("대출신청의 동의약관에 대해 2개중 1개의 약관에 대한 요청이 왔을 때 예외를 던진다.")
    void Should_ThrowsException_When_RequestNotAcceptTermsOfApplication() throws Exception {
        //given
        Terms entityA = Terms.builder()
                .termsId(1L)
                .name("약관 1")
                .termsDetailUrl("https://abcd.efg1")
                .build();

        Terms entityB = Terms.builder()
                .termsId(2L)
                .name("약관 2")
                .termsDetailUrl("https://abcd.efg2")
                .build();

        // applicationId
        Long applicationIdId = 1L;

        // 1번과 2번의 약관을 동의하여 RequestDTO인 AcceptTerms 정적 중첩 요청객체에 담아 전달
        ApplicationDTO.AcceptTerms request =
                ApplicationDTO.AcceptTerms.builder()
                        .acceptTermsIds(Arrays.asList(1L)) // 약관 1개만 동의했을때 Exception이 발생할것이다.
                        .build();

        // Mocking! - 저장까지 가지 못하고 Exception을 Throw하므로 saveAll에 대한 Mocking을 해서는 안된다.
        Mockito.when(applicationRepository.findById(applicationIdId)).thenReturn(Optional.ofNullable(new Application()));
        Mockito.when(termsRepository.findAll(Sort.by(Sort.Direction.ASC, "termsId"))).thenReturn(List.of(entityA, entityB));

        // when & then - Throws (Exception 발생!)
        org.junit.jupiter.api.Assertions.assertThrows(BaseException.class, () -> applicationService.acceptTerms(applicationIdId, request));

    }

    @Test
    @DisplayName("대출신청의 동의약관에 대한 존재하지 않는 약관 요청이 왔을 때 예외를 던진다.")
    void Should_ThrowsException_When_RequestNotExistAcceptTermsOfApplication() throws Exception {
        //given
        Terms entityA = Terms.builder()
                .termsId(1L)
                .name("약관 1")
                .termsDetailUrl("https://abcd.efg1")
                .build();

        Terms entityB = Terms.builder()
                .termsId(2L)
                .name("약관 2")
                .termsDetailUrl("https://abcd.efg2")
                .build();

        // applicationId
        Long applicationIdId = 1L;

        // 1번과 2번의 약관을 동의하여 RequestDTO인 AcceptTerms 정적 중첩 요청객체에 담아 전달
        ApplicationDTO.AcceptTerms request =
                ApplicationDTO.AcceptTerms.builder()
                        .acceptTermsIds(Arrays.asList(1L, 3L)) // 약관 1개만 동의했을때 Exception이 발생할것이다.
                        .build();

        // Mocking! - 저장까지 가지 못하고 Exception을 Throw하므로 saveAll에 대한 Mocking을 해서는 안된다.
        Mockito.when(applicationRepository.findById(applicationIdId)).thenReturn(Optional.ofNullable(new Application()));
        Mockito.when(termsRepository.findAll(Sort.by(Sort.Direction.ASC, "termsId"))).thenReturn(List.of(entityA, entityB));

        // when & then - Throws (Exception 발생!)
        org.junit.jupiter.api.Assertions.assertThrows(BaseException.class, () -> applicationService.acceptTerms(applicationIdId, request));

    }
}
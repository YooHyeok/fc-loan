package com.fc.loan.service;

import com.fc.loan.domain.Application;
import com.fc.loan.domain.Judgment;
import com.fc.loan.repository.JudgmentRepository;
import com.fc.loan.repository.ApplicationRepository;
import org.assertj.core.api.Assertions;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.*;
import org.mockito.junit.jupiter.MockitoExtension;
import org.modelmapper.ModelMapper;

import java.math.BigDecimal;
import java.util.Optional;

import static com.fc.loan.dto.JudgmentDTO.*;

@ExtendWith(MockitoExtension.class)
class JudgmentServiceTest {
    @InjectMocks
    JudgmentServiceImpl judgmentService;

    @Mock
    private JudgmentRepository judgmentRepository;

    @Mock
    private ApplicationRepository applicationRepository;

    @Spy
    private ModelMapper modelMapper;

    @Test
    @DisplayName("등록 - 대출 심사 생성 요청이 왔을때 새 대출 심사 엔티티를 반환하여 응답한다.")
    void Should_ReturnResponseOfNewJudgmentEntity_When_RequestNewJudgment() throws Exception {
        //given
        Long findByApplicationId = 1L;
        Request request = Request.builder()
                .applicationId(findByApplicationId)
                .name("Member")
                .approvalAmount(BigDecimal.valueOf(5000000))
                .build();
        Judgment entity = Judgment.builder()
                .applicationId(findByApplicationId)
                .name("Member")
                .approvalAmount(BigDecimal.valueOf(5000000))
                .build();
        //Mocking
        Mockito.when(applicationRepository.existsById(findByApplicationId)).thenReturn(true);
        Mockito.when(judgmentRepository.save(ArgumentMatchers.any(Judgment.class))).thenReturn(entity);

        //when
        Response actual = judgmentService.create(request);

        //then
        Assertions.assertThat(actual.getApplicationId()).isSameAs(entity.getApplicationId());
        Assertions.assertThat(actual.getName()).isSameAs(entity.getName());
        Assertions.assertThat(actual.getApprovalAmount()).isSameAs(entity.getApprovalAmount());

    }

    @Test
    @DisplayName("조회 - 존재하는 대출 심사 ID 요청이 들어왔을 때 존재하는 대출 심사 엔티티를 응답으로 반환한다.")
    void Should_ReturnResponseOfExistJudgmentEntity_When_RequestExistJudgmentId() throws Exception {
        //given
        Long findByJudgementId = 1L;
        Judgment entity = Judgment.builder()
                .judgmentId(findByJudgementId)
                .build();
        //Mocing
        Mockito.when(judgmentRepository.findById(findByJudgementId)).thenReturn(Optional.ofNullable(entity));
        //when
        Response actual = judgmentService.get(findByJudgementId);
        //then
        Assertions.assertThat(actual.getJudgmentId()).isSameAs(findByJudgementId);

    }

    @Test
    @DisplayName("조회 - 존재하는 대출 신청 ID 요청이 들어왔을 때 존재하는 대출 심사 엔티티를 응답으로 반환한다. ")
    void Should_ReturnResponseOfExistJudgmentEntity_When_RequestExistApplicationId() throws Exception {
        //given
        Long findByApplicationId = 1L;
        Judgment entity = Judgment.builder()
                .judgmentId(1L)
                .applicationId(findByApplicationId)
                .build();

        //Mocking
        Mockito.when(applicationRepository.existsById(findByApplicationId)).thenReturn(true);
        Mockito.when(judgmentRepository.findByApplicationId(findByApplicationId)).thenReturn(Optional.ofNullable(entity));

        //when
        Response actual = judgmentService.getJudgmentOfApplication(findByApplicationId);

        //then
        Assertions.assertThat(actual.getJudgmentId()).isSameAs(1L);
        Assertions.assertThat(actual.getApplicationId()).isSameAs(entity.getApplicationId());
    }
    @Test
    @DisplayName("수정 - 존재하는 대출신청 정보에 대한 수정 요청이 왔을 때 존재하는 대출신청 엔티티를 수정한 뒤 응답 객체로 반환한다.")
    void Should_ReturnUpdatedResponseOfExistJudgementEntity_When_RequestUpdateExistJudgmentInfo() throws Exception {
        //given
        Long findByJudgmentId = 1L;

        Judgment entity = Judgment.builder()
                .judgmentId(findByJudgmentId)
                .name("Member U")
                .approvalAmount(BigDecimal.valueOf(5000000))
                .build();

        Request request = Request.builder()
                .name("Member Yoo")
                .approvalAmount(BigDecimal.valueOf(10000000))
                .build();


        Mockito.when(judgmentRepository.findById(findByJudgmentId)).thenReturn(Optional.ofNullable(entity));
        Mockito.when(judgmentRepository.save(ArgumentMatchers.any(Judgment.class))).thenReturn(entity); // return절의 entity는 service의 update() 메소드 내부적으로 값이 변경된다. (name/approvalAmount)

        //when
        Response actual = judgmentService.update(findByJudgmentId, request);
        //then
        Assertions.assertThat(actual.getJudgmentId()).isSameAs(findByJudgmentId);
        Assertions.assertThat(actual.getName()).isSameAs(request.getName());
        Assertions.assertThat(actual.getApprovalAmount()).isSameAs(request.getApprovalAmount());

    }
    @Test
    @DisplayName("삭제 - 존재하는 대출신청 정보에 대한 삭제 요청이 왔을 때 대출신청 엔티티를 삭제한다.")
    void Should_ReturnDeletedJudgementEntity_When_RequestDeleteExistJudgementId() throws Exception {
        //given
        Long findByJudgmentId = 1L;

        Judgment entity = Judgment.builder()
                .judgmentId(findByJudgmentId)
                .build();

        Mockito.when(judgmentRepository.findById(findByJudgmentId)).thenReturn(Optional.ofNullable(entity));
        Mockito.when(judgmentRepository.save(ArgumentMatchers.any(Judgment.class))).thenReturn(entity); // return절의 entity는 service의 update() 메소드 내부적으로 값이 변경된다. (name/approvalAmount)
        //when

        judgmentService.delete(findByJudgmentId);

        //then
        Assertions.assertThat(entity.getIsDeleted()).isSameAs(true);
    }

    @Test
    @DisplayName("대출심사 금액부여 - 대출심사 정보에 대한 대출 심사 금액 부여 요청이 왔을 때 대출 신청 엔티티를 수정 후 반환한다.")
    void Should_ReturnUpdateResponseOfApplicationEntity_When_RequestGrantApprovalAmountOfJudgmentInfo() throws Exception {
        //given
        Long findByApplication = 1L;
        Long findByJudgmentId = 1L;
        Application application = Application.builder()
                .applicationId(findByApplication)
                .build();
        Judgment judgment = Judgment.builder()
                .judgmentId(findByJudgmentId)
                .applicationId(application.getApplicationId())
                .approvalAmount(BigDecimal.valueOf(5000000))
                .build();
        //when
        Mockito.when(judgmentRepository.findById(findByJudgmentId)).thenReturn(Optional.ofNullable(judgment));
        Mockito.when(applicationRepository.findById(judgment.getApplicationId())).thenReturn(Optional.ofNullable(application));
        Mockito.when(applicationRepository.save(ArgumentMatchers.any(Application.class))).thenReturn(application);

        judgmentService.grant(findByJudgmentId);

        //then
        Assertions.assertThat(judgment.getApprovalAmount()).isSameAs(application.getApprovalAmount());

    }
}
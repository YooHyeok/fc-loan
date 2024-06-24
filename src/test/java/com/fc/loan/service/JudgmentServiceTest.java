package com.fc.loan.service;

import com.fc.loan.domain.Counsel;
import com.fc.loan.domain.Judgment;
import com.fc.loan.domain.JudgmentRepository;
import com.fc.loan.dto.JudgmentDTO;
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
}
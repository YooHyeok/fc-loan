package com.fc.loan.service;

import com.fc.loan.domain.Terms;
import com.fc.loan.dto.TermsDTO;
import com.fc.loan.repository.TermsRepository;
import org.assertj.core.api.Assertions;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.*;
import org.mockito.junit.jupiter.MockitoExtension;
import org.modelmapper.ModelMapper;

import java.util.Arrays;
import java.util.Collection;
import java.util.List;

import static com.fc.loan.dto.TermsDTO.*;

@ExtendWith(MockitoExtension.class)
public class TemrsServiceTest {

    @InjectMocks // @Mock, @Spy가 선언된 목 객체들을 해당 클래스에 주입시켜 준다.
    TermsServiceImpl termsService; // Mockito.when()을 활용하여 모킹한다.

    @Mock
    private TermsRepository termsRepository;

    @Spy
    private ModelMapper modelMapper;

    @Test
    @DisplayName("")
    void Should_ReturnResponseExistTerms_When_() throws Exception {
        //given
        Request request = Request.builder()
                .name("대출 이용 약관")
                .termsDetailUrl("https://abc-storage.acc/exampletest")
                .build();
        Terms entity = Terms.builder()
                .name("대출 이용 약관")
                .termsDetailUrl("https://abc-storage.acc/exampletest")
                .build();

        //mocking - any의 의미 Terms 클래스 타입의 어떠한 객체를 argument로 가정한다는 의미
        Mockito.when(termsRepository.save(ArgumentMatchers.any(Terms.class))).thenReturn(entity);
        //when
        Response actual = termsService.create(request);
        //then
        Assertions.assertThat(actual.getTermsDetailUrl()).isSameAs(request.getTermsDetailUrl());
        Assertions.assertThat(actual.getName()).isSameAs(request.getName());
        Assertions.assertThat(actual.getTermsId()).isSameAs(entity.getTermsId());
    }
    @Test
    @DisplayName("")
    void Should_ReturnAllResponseOfExistTermsEntities_When_RequestTermsList() throws Exception {
        //given
        Terms entityA = Terms.builder()
                .name("대출 이용 약관1")
                .termsDetailUrl("https://abc-storage.acc/exampletestA")
                .build();

        Terms entityB = Terms.builder()
                .name("대출 이용 약관2")
                .termsDetailUrl("https://abc-storage.acc/exampletestB")
                .build();

        List<Terms> entities = Arrays.asList(entityA, entityB);

        //mocking
        Mockito.when(termsRepository.findAll()).thenReturn(entities);

        //when
        List<Response> actuals = termsService.getAll();

        //then
        Assertions.assertThat(actuals.size()).isSameAs(entities.size());
    }
}

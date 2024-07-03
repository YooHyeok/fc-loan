package com.fc.loan.service;

import com.fc.loan.domain.AcceptTerms;
import com.fc.loan.domain.Application;
import com.fc.loan.domain.Terms;
import com.fc.loan.dto.ApplicationDTO;
import com.fc.loan.exception.BaseException;
import com.fc.loan.exception.ResultType;
import com.fc.loan.repository.AcceptTermsRepository;
import com.fc.loan.repository.ApplicationRepository;
import com.fc.loan.repository.JudgmentRepository;
import com.fc.loan.repository.TermsRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.modelmapper.ModelMapper;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;

import static com.fc.loan.dto.ApplicationDTO.Request;
import static com.fc.loan.dto.ApplicationDTO.Response;


@Slf4j
@Service
@RequiredArgsConstructor
public class ApplicationServiceImpl implements ApplicationService {
    private final JudgmentRepository judgmentRepository;
    private final AcceptTermsRepository acceptTermsRepository;

    private final ModelMapper modelMapper; // ModelMapperConfig로 등록한 ModelMapper Bean 생성자 의존성 주입
    private final ApplicationRepository applicationRepository;
    private final TermsRepository termsRepository;

    /* --- 대출 신청 --- */
    @Override
    public Response create(Request request) {
        Application application = modelMapper.map(request, Application.class); // 요청 객체를 Counsel Entity클래스로 매핑하여 반환
        application.setAppliedAt(LocalDateTime.now());
        log.info("mapped Counsel Data : {}", application);
        return modelMapper.map(applicationRepository.save(application), Response.class);
    }

    @Override
    public Response get(Long applicationId) {
        Application application = applicationRepository.findById(applicationId).orElseThrow(() -> {
            throw new BaseException(ResultType.SYSTEM_ERROR);
        });
        return modelMapper.map(application, Response.class);
    }

    @Override
    public Response update(Long applicationId, Request request) {
        Application application = applicationRepository.findById(applicationId).orElseThrow(() -> {
            throw new BaseException(ResultType.SYSTEM_ERROR);
        });

        application.setName(request.getName());
        application.setCellPhone(request.getCellPhone());
        application.setEmail(request.getEmail());
        application.setHopeAmount(request.getHopeAmount());

        return modelMapper.map(applicationRepository.save(application), Response.class);
    }

    @Override
    public void delete(Long applicationId) {
        Application application = applicationRepository.findById(applicationId).orElseThrow(() -> {
            throw new BaseException(ResultType.SYSTEM_ERROR);
        });
        application.setIsDeleted(true);
        applicationRepository.save(application);
//  TODO. JPA의 @SQLDelete에 의해 SoftDelete 처리된다. 추후 리팩토링 예정
//        applicationRepository.deleteById(application.getApplicationId());
    }


    /* --- 대출 신청 이용 약관 --- */

    /**
     * 약관 동의
     * @param applicationId
     * @param request
     * @return false - Exception발생시 Advice에 의해 에러코드만 반환
     */
    @Override
    public Boolean acceptTerms(Long applicationId, ApplicationDTO.AcceptTerms request) {
        /* 대출 신청 조회 validation */
        applicationRepository.findById(applicationId).orElseThrow(() -> {
            throw new BaseException(ResultType.SYSTEM_ERROR);
        });

        /* 약관 전체 조회 (contains 비교를 위한 정렬) */
        List<Terms> termsList = termsRepository.findAll(Sort.by(Sort.Direction.ASC, "termsId"));

        /* 약관 전체 조회 validation */
        if (termsList.isEmpty()) throw new BaseException(ResultType.SYSTEM_ERROR);

        /* 요청(동의한) 약관 개수, 전체 약관 개수 validation (약관은 무조건 모두 가져가야함) */
        List<Long> acceptTermsIds = request.getAcceptTermsIds();
        if (acceptTermsIds.size() != termsList.size()) throw new BaseException(ResultType.SYSTEM_ERROR);

        List<Long> termsIds = termsList.stream().map(Terms::getTermsId).collect(Collectors.toList());
        Collections.sort(acceptTermsIds); // contains 비교를 위한 정렬

        /* 전체 약관 ID에 동의한 ID가 모두 포함 validation */
        if (!termsIds.containsAll(acceptTermsIds)) throw new BaseException(ResultType.SYSTEM_ERROR);

        /*for (Long termsId : acceptTermsIds) {
            AcceptTerms accepted = AcceptTerms.builder()
                    .applicationId(applicationId)
                    .termsId(termsId)
                    .build();
            acceptTermsRepository.save(accepted);
        }*/

        /* saveAll이라는 메소드를 1회 호출함으로써 DB와 JPA와의 통신이 1회만 발생 */
        acceptTermsRepository.saveAll(
            acceptTermsIds.stream()
                    .map(termsId ->
                        AcceptTerms.builder()
                        .applicationId(applicationId)
                        .termsId(termsId)
                        .build()
                )
                .collect(Collectors.toList())
        );
        return true;
    }

    /**
     * Ch.05-04 대출 계약 기능
     * flow
     * <pre>
     * 1. 신청 정보 존재 여부
     * 2. 심사 정보 존재 여부
     * 3. 승인 금액 > 0
     *      계약을 한다는 조건 자체는 대출 심사를 통해 빌린 사람에게 대출 조건으로 얼마를 집행을 해줄지에 대해서 제시를 했을 것이며,
     *      승인을 했을 때 계약이 이루어 지는 것이기 때문에 신청 정보에서 승인된 금액이 0보다 커야 한다.
     * 4. 계약 체결
     * </pre>
     * @param applicationId
     * @return Response - null을 반환하므로 추후 void 고려
     */
    @Override
    public Response contract(Long applicationId) {
        // 신청 정보 존재 여부
        Application application = applicationRepository.findById(applicationId).orElseThrow(() -> {
            throw new BaseException(ResultType.SYSTEM_ERROR);
        });

        // 심사 정보 존재 여부
        judgmentRepository.findByApplicationId(application.getApplicationId()).orElseThrow(() -> {
            throw new BaseException(ResultType.SYSTEM_ERROR);
        });

        // 승인 금액 > 0 (compareTo - 값 비교: 반환값이 0 이면 두 값이 동일 | 양수이면 호출 객체가 더 큼/음수이면 호출 객체가 더 작음)
        if (application.getApprovalAmount() == null || application.getApprovalAmount().compareTo(BigDecimal.ZERO) == 0) {
            throw new BaseException(ResultType.SYSTEM_ERROR);
        }

        // 계약 체결 (계약 일자 현재 시점으로 반영)
        application.setContractedAt(LocalDateTime.now());
        applicationRepository.save(application);
        return null;
    }

}

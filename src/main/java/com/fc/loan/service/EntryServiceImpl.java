package com.fc.loan.service;

import com.fc.loan.domain.Application;
import com.fc.loan.domain.Entry;
import com.fc.loan.dto.BalanceDTO;
import com.fc.loan.dto.EntryDTO;
import com.fc.loan.exception.BaseException;
import com.fc.loan.exception.ResultType;
import com.fc.loan.repository.ApplicationRepository;
import com.fc.loan.repository.EntryRepository;
import lombok.RequiredArgsConstructor;
import org.modelmapper.ModelMapper;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Optional;

import static com.fc.loan.dto.EntryDTO.*;

@Service
@RequiredArgsConstructor
public class EntryServiceImpl implements EntryService {

    private final EntryRepository entryRepository;
    private final BalanceService balanceService;
    private final ApplicationRepository applicationRepository;
    private final ModelMapper modelMapper;

    @Override
    public Response create(Long applicationId, Request request) {
        //계약 체결 여부 검증
        if (!isContractedApplication(applicationId)) throw new BaseException(ResultType.SYSTEM_ERROR);

        // 대출 집행 등록
        Entry entry = modelMapper.map(request, Entry.class);
        entry.setApplicationId(applicationId);
        entryRepository.save(entry);

        // 대출 잔고 관리 - 잔고 저장
        balanceService.create(
                applicationId,
                /* 잔고: EntryDTO 요청으로 부터 집행금액(EntryAmount) 추출 */
                BalanceDTO.Request
                    .builder()
                    .entryAmount(request.getEntryAmount())
                    .build()
        );
        return modelMapper.map(entry, Response.class);
    }

    @Override
    public Response get(Long applicationId) {
        Optional<Entry> entry = entryRepository.findByApplicationId();
        /* 존재한다면 Response DTO로 변환후 반환 - 반대는 null*/
        if (entry.isPresent()) return modelMapper.map(entry, Response.class);
        return null;
    }

    @Override
    public UpdateResponse update(Long entryId, Request request) {
        // entry 존재 유무 파악
        Entry entry = entryRepository.findById(entryId).orElseThrow(() -> {
            throw new BaseException(ResultType.SYSTEM_ERROR);
        });

        BigDecimal beforeEntryAmount = entry.getEntryAmount();

        // before → after 변경
        entry.setEntryAmount(request.getEntryAmount());
        entryRepository.save(entry);

        // balance update
        BalanceDTO.Response update = balanceService.update(entry.getApplicationId(),
                BalanceDTO.UpdateRequest.builder()
                        .applicationId(entry.getApplicationId())
                        .beforeEntryAmount(beforeEntryAmount)
                        .afterEntryAmount(request.getEntryAmount())
                        .build());
        return UpdateResponse.builder()
                .applicationId(entry.getApplicationId())
                .entryId(entryId)
                .beforeEntryAmount(beforeEntryAmount)
                .afterEntryAmount(request.getEntryAmount())
                .build();

    }

    private boolean isContractedApplication(Long applicationId) {
        Optional<Application> existed = applicationRepository.findById(applicationId);
        if (existed.isEmpty()) return false; // 비어있다면 false 리턴
        return existed.get().getContractedAt() != null; // 비어있지 않다면 승인여부 null 체크
    }
}

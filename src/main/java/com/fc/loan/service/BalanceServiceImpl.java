package com.fc.loan.service;

import com.fc.loan.domain.Balance;
import com.fc.loan.dto.BalanceDTO;
import com.fc.loan.exception.BaseException;
import com.fc.loan.exception.ResultType;
import com.fc.loan.repository.BalanceRepository;
import lombok.RequiredArgsConstructor;
import org.modelmapper.ModelMapper;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;

import static com.fc.loan.dto.BalanceDTO.*;

@Service
@RequiredArgsConstructor
public class BalanceServiceImpl implements BalanceService {

    private final BalanceRepository balanceRepository;
    private final ModelMapper modelMapper;

    @Override
    public Response create(Long applicationId, Request request) {
        /* null 처리만 하고 반환하진 않는다. */
        if (balanceRepository.findByApplicationId(applicationId).isPresent()) {
            throw new BaseException(ResultType.SYSTEM_ERROR);
        }

        Balance balance = modelMapper.map(request, Balance.class);
        balance.setApplicationId(applicationId);
        balance.setBalance(request.getEntryAmount());

        Balance saved = balanceRepository.save(balance);
        return modelMapper.map(saved, Response.class);
    }

    @Override
    public Response update(Long applicationId, UpdateRequest request) {
        //balance - isPresent처리와 같으나 값을 반환까지 해준다.
        Balance balance = balanceRepository.findByApplicationId(applicationId).orElseThrow(() -> {
            throw new BaseException(ResultType.SYSTEM_ERROR);
        });

        BigDecimal beforeEntryAmount = request.getBeforeEntryAmount();
        BigDecimal afterEntryAmount = request.getAfterEntryAmount();

        //as-is -> to-be : 밸런스(잔고) 값 보정
        BigDecimal updatedBalance = balance.getBalance();
        updatedBalance = updatedBalance
                            .subtract(beforeEntryAmount) // 과거 집행되었던 금액 차감 (잔고를 0원으로 만든다)
                            .add(afterEntryAmount); // 현재 수정하고자 하는 금액 증감
        balance.setBalance(updatedBalance);

        return modelMapper.map(balanceRepository.save(balance), Response.class); // 수정-저장 후 DTO변환 및 반환
    }
}

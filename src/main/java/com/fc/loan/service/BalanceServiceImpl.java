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
        /*if (balanceRepository.findByApplicationId(applicationId).isPresent()) {
            throw new BaseException(ResultType.SYSTEM_ERROR);
        }*/

        Balance balance = modelMapper.map(request, Balance.class);
        balance.setApplicationId(applicationId);
        balance.setBalance(request.getEntryAmount());

        /**
         * 대출 잔고가 존재할 경우 기존 null처리 후 SYSTEM_ERROR을 출력했던 방식에서
         * 대출 집행 삭제 기능 추가 기능에 의해 기존 잔금을 덮어씌우는 방식으로 수정한다.
         */
        balanceRepository.findByApplicationId(applicationId).ifPresent(b -> {
            balance.setBalanceId(b.getBalanceId());
            balance.setIsDeleted(b.getIsDeleted());
            balance.setCreatedAt(b.getCreatedAt());
            balance.setUpdatedAt(b.getUpdatedAt());
        });

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

    @Override
    public Response repaymentUpdate(Long applicationId, RepaymentRequest request) {
        Balance balance = balanceRepository.findByApplicationId(applicationId).orElseThrow(() -> {
            throw new BaseException(ResultType.SYSTEM_ERROR);
        });

        BigDecimal updatedBalance = balance.getBalance(); // 잔여 대출 금액
        BigDecimal repaymentAmout = request.getRepaymentAmount(); // 상환 금액
        if (request.getType().equals(RepaymentRequest.RepaymentType.ADD)) {
            updatedBalance.add(repaymentAmout);
        } else {
            updatedBalance.subtract(repaymentAmout);
        }
        balance.setBalance(updatedBalance);

        return modelMapper.map(balanceRepository.save(balance), Response.class); // 수정-저장 후 DTO변환 및 반환
    }
}

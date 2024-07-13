package com.fc.loan.service;

import com.fc.loan.domain.Balance;
import com.fc.loan.dto.BalanceDTO;
import com.fc.loan.exception.BaseException;
import com.fc.loan.exception.ResultType;
import com.fc.loan.repository.BalanceRepository;
import lombok.RequiredArgsConstructor;
import org.modelmapper.ModelMapper;
import org.springframework.stereotype.Service;

import static com.fc.loan.dto.BalanceDTO.*;

@Service
@RequiredArgsConstructor
public class BalanceServiceImpl implements BalanceService {

    private final BalanceRepository balanceRepository;
    private final ModelMapper modelMapper;

    @Override
    public Response create(Long applicationId, Request request) {
        if (balanceRepository.findByApplicationId(applicationId).isPresent()) {
            throw new BaseException(ResultType.SYSTEM_ERROR);
        }

        Balance balance = modelMapper.map(request, Balance.class);
        balance.setApplicationId(applicationId);
        balance.setBalance(request.getEntryAmount());

        Balance saved = balanceRepository.save(balance);
        return modelMapper.map(saved, Response.class);
    }
}

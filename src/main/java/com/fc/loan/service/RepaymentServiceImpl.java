package com.fc.loan.service;

import com.fc.loan.domain.Application;
import com.fc.loan.domain.Entry;
import com.fc.loan.domain.Repayment;
import com.fc.loan.dto.BalanceDTO;
import com.fc.loan.dto.RepaymentDTO;
import com.fc.loan.exception.BaseException;
import com.fc.loan.exception.ResultType;
import com.fc.loan.repository.ApplicationRepository;
import com.fc.loan.repository.EntryRepository;
import com.fc.loan.repository.RepaymentRepository;
import lombok.RequiredArgsConstructor;
import org.modelmapper.ModelMapper;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

import static com.fc.loan.dto.RepaymentDTO.*;

@Service
@RequiredArgsConstructor
public class RepaymentServiceImpl implements RepaymentService{
    private final RepaymentRepository repaymentRepository;
    private final ApplicationRepository applicationRepository;
    private final EntryRepository entrynRepository;
    private final BalanceService balanceService;
    private final ModelMapper modelMapper;
    @Override
    public Response create(Long applicationId, Request request) {
        //TODO. [validation] 1.신청 정보 2.집행 여부
        if (!isRepayableApplication(applicationId)) {
            throw new BaseException(ResultType.SYSTEM_ERROR);
        }
        Repayment repayment = modelMapper.map(request, Repayment.class);
        repayment.setApplicationId(applicationId);

        /* 잔고 ex) balance 500 -> - 100 = 400 */
        BalanceDTO.Response updatedBalance = balanceService.repaymentUpdate(applicationId,
                BalanceDTO.RepaymentRequest.builder()
                        .repaymentAmount(request.getRepaymentAmount())
                        .type(BalanceDTO.RepaymentRequest.RepaymentType.REMOVE)
                        .build());

        Response response = modelMapper.map(repaymentRepository.save(repayment), Response.class);
        response.setBalance(updatedBalance.getBalance());

        return response;
    }

    @Override
    public List<ListResponse> get(Long applicationId) {
        return repaymentRepository.findAllByApplicationId(applicationId)
                .stream()
                .map(repayment-> modelMapper.map(repaymentRepository.save(repayment), ListResponse.class))
                .collect(Collectors.toList());
    }

    private boolean isRepayableApplication(Long applicationId) {
        Optional<Application> existedApplication = applicationRepository.findById(applicationId);
        /* 신청 정보가 없는 경우 */
        if (existedApplication.isEmpty()) {
            return false;
        }
        /* 신청 정보는 있으나 계약이력이 없는 경우. */
        if (existedApplication.get().getContractedAt() == null) {
            return false;
        }
        /* 대출 집행 여부 */
        Optional<Entry> existedEntry = entrynRepository.findByApplicationId(applicationId);
        return existedEntry.isPresent();
    }
}

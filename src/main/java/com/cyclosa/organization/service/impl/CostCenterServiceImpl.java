package com.cyclosa.organization.service.impl;

import com.cyclosa.common.exception.AppException;
import com.cyclosa.organization.dto.request.CreateCostCenterRequest;
import com.cyclosa.organization.dto.response.CostCenterResponse;
import com.cyclosa.organization.entity.CostCenter;
import com.cyclosa.organization.exception.OrganizationErrorCode;
import com.cyclosa.organization.mapper.CostCenterMapper;
import com.cyclosa.organization.repository.CostCenterRepository;
import com.cyclosa.organization.service.CostCenterService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.UUID;

@Slf4j
@Service
@RequiredArgsConstructor
public class CostCenterServiceImpl implements CostCenterService {

    private final CostCenterRepository costCenterRepository;
    private final CostCenterMapper costCenterMapper;

    @Override
    @Transactional
    public CostCenterResponse createCostCenter(UUID companyId, CreateCostCenterRequest request) {
        UUID effectiveCompanyId = request.getCompanyId() != null ? request.getCompanyId() : companyId;
        if (effectiveCompanyId == null) {
            throw AppException.badRequest("Yêu cầu cung cấp ID công ty");
        }

        if (costCenterRepository.existsByCodeAndCompanyId(request.getCode(), effectiveCompanyId)) {
            throw new AppException(OrganizationErrorCode.COST_CENTER_CODE_EXISTS);
        }

        CostCenter costCenter = costCenterMapper.toEntity(request);
        costCenter.setCompanyId(effectiveCompanyId);
        costCenter = costCenterRepository.save(costCenter);
        log.info("Created CostCenter id={}, code={}, companyId={}", costCenter.getId(), costCenter.getCode(), effectiveCompanyId);
        return costCenterMapper.toResponse(costCenter);
    }

    @Override
    @Transactional(readOnly = true)
    public List<CostCenterResponse> getCostCenters(UUID companyId) {
        if (companyId == null) {
            throw AppException.badRequest("Yêu cầu cung cấp ID công ty");
        }
        List<CostCenter> list = costCenterRepository.findByCompanyId(companyId);
        return costCenterMapper.toResponseList(list);
    }
}

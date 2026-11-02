package com.cyclosa.organization.service;

import com.cyclosa.common.exception.AppException;
import com.cyclosa.common.response.PageData;
import com.cyclosa.organization.dto.request.CreateCostCenterRequest;
import com.cyclosa.organization.dto.request.UpdateCostCenterRequest;
import com.cyclosa.organization.dto.response.CostCenterResponse;
import com.cyclosa.organization.entity.CostCenter;
import com.cyclosa.organization.exception.OrganizationErrorCode;
import com.cyclosa.organization.repository.CompanyRepository;
import com.cyclosa.organization.repository.CostCenterRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.UUID;

@Slf4j
@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class CostCenterService {

    private final CostCenterRepository costCenterRepository;
    private final CompanyRepository companyRepository;

    public PageData<CostCenterResponse> getCostCenters(UUID companyId, String keyword, Pageable pageable) {
        verifyCompanyExists(companyId);
        Page<CostCenter> page = costCenterRepository.search(companyId, keyword, pageable);
        return PageData.of(page, this::mapToResponse);
    }

    public List<CostCenterResponse> getAllCostCentersByCompany(UUID companyId) {
        verifyCompanyExists(companyId);
        return costCenterRepository.findAllByCompanyId(companyId).stream()
                .map(this::mapToResponse)
                .toList();
    }

    public CostCenterResponse getCostCenterById(UUID id) {
        CostCenter costCenter = findCostCenterOrThrow(id);
        return mapToResponse(costCenter);
    }

    @Transactional
    public CostCenterResponse createCostCenter(CreateCostCenterRequest request) {
        verifyCompanyExists(request.getCompanyId());

        if (costCenterRepository.existsByCompanyIdAndCode(request.getCompanyId(), request.getCode())) {
            throw new AppException(OrganizationErrorCode.COST_CENTER_CODE_EXISTS);
        }

        CostCenter costCenter = CostCenter.builder()
                .companyId(request.getCompanyId())
                .code(request.getCode().toUpperCase().trim())
                .name(request.getName().trim())
                .description(request.getDescription())
                .deleted(false)
                .build();

        costCenter = costCenterRepository.save(costCenter);
        log.info("Đã tạo mới trung tâm chi phí: id={}, code={}", costCenter.getId(), costCenter.getCode());
        return mapToResponse(costCenter);
    }

    @Transactional
    public CostCenterResponse updateCostCenter(UUID id, UpdateCostCenterRequest request) {
        CostCenter costCenter = findCostCenterOrThrow(id);
        costCenter.setName(request.getName().trim());
        costCenter.setDescription(request.getDescription());
        costCenter = costCenterRepository.save(costCenter);
        log.info("Đã cập nhật trung tâm chi phí: id={}", costCenter.getId());
        return mapToResponse(costCenter);
    }

    @Transactional
    public void deleteCostCenter(UUID id) {
        CostCenter costCenter = findCostCenterOrThrow(id);
        costCenterRepository.delete(costCenter);
        log.info("Đã xóa mềm trung tâm chi phí: id={}", id);
    }

    public CostCenter findCostCenterOrThrow(UUID id) {
        return costCenterRepository.findById(id)
                .orElseThrow(() -> new AppException(OrganizationErrorCode.COST_CENTER_NOT_FOUND));
    }

    private void verifyCompanyExists(UUID companyId) {
        if (!companyRepository.existsById(companyId)) {
            throw new AppException(OrganizationErrorCode.COMPANY_NOT_FOUND);
        }
    }

    private CostCenterResponse mapToResponse(CostCenter costCenter) {
        return CostCenterResponse.builder()
                .id(costCenter.getId())
                .companyId(costCenter.getCompanyId())
                .code(costCenter.getCode())
                .name(costCenter.getName())
                .description(costCenter.getDescription())
                .createdAt(costCenter.getCreatedAt())
                .updatedAt(costCenter.getUpdatedAt())
                .build();
    }
}


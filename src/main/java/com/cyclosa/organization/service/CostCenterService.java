package com.cyclosa.organization.service;

import com.cyclosa.common.exception.AppException;
import com.cyclosa.common.response.PageData;
import com.cyclosa.common.util.PageableUtils;
import com.cyclosa.organization.dto.request.CostCenterFilterRequest;
import com.cyclosa.organization.dto.request.CreateCostCenterRequest;
import com.cyclosa.organization.dto.request.UpdateCostCenterRequest;
import com.cyclosa.organization.dto.response.CostCenterResponse;
import com.cyclosa.organization.entity.CostCenter;
import com.cyclosa.organization.exception.OrganizationErrorCode;
import com.cyclosa.organization.mapper.CostCenterMapper;
import com.cyclosa.organization.repository.CompanyRepository;
import com.cyclosa.organization.repository.CostCenterRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Set;
import java.util.UUID;

@Slf4j
@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class CostCenterService {

    private static final Set<String> ALLOWED_SORT_FIELDS = Set.of("code", "name", "createdAt");

    private final CostCenterRepository costCenterRepository;
    private final CompanyRepository companyRepository;
    private final CostCenterMapper costCenterMapper;

    public PageData<CostCenterResponse> getCostCenters(CostCenterFilterRequest filter) {
        verifyCompanyExists(filter.getCompanyId());
        String kw = PageableUtils.normalizeKeyword(filter.getKeyword());
        Pageable pageable = filter.toPageable("createdAt", ALLOWED_SORT_FIELDS);
        Page<CostCenter> page = costCenterRepository.search(filter.getCompanyId(), kw, pageable);
        return PageData.of(page, costCenterMapper::toResponse);
    }

    public List<CostCenterResponse> getAllCostCentersByCompany(UUID companyId) {
        verifyCompanyExists(companyId);
        return costCenterRepository.findAllByCompanyId(companyId).stream()
                .map(costCenterMapper::toResponse)
                .toList();
    }

    public CostCenterResponse getCostCenterById(UUID id) {
        CostCenter costCenter = findCostCenterOrThrow(id);
        return costCenterMapper.toResponse(costCenter);
    }

    @Transactional
    public CostCenterResponse createCostCenter(CreateCostCenterRequest request) {
        verifyCompanyExists(request.getCompanyId());

        if (costCenterRepository.existsByCompanyIdAndCode(request.getCompanyId(), request.getCode())) {
            throw new AppException(OrganizationErrorCode.COST_CENTER_CODE_EXISTS);
        }

        CostCenter costCenter = costCenterMapper.toEntity(request);
        costCenter.setCode(request.getCode().toUpperCase().trim());
        costCenter.setName(request.getName().trim());

        costCenter = costCenterRepository.save(costCenter);
        log.info("Đã tạo mới trung tâm chi phí: id={}, code={}", costCenter.getId(), costCenter.getCode());
        return costCenterMapper.toResponse(costCenter);
    }

    @Transactional
    public CostCenterResponse updateCostCenter(UUID id, UpdateCostCenterRequest request) {
        CostCenter costCenter = findCostCenterOrThrow(id);
        costCenterMapper.updateEntityFromDto(request, costCenter);
        costCenter.setName(request.getName().trim());

        costCenter = costCenterRepository.save(costCenter);
        log.info("Đã cập nhật trung tâm chi phí: id={}", costCenter.getId());
        return costCenterMapper.toResponse(costCenter);
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
}

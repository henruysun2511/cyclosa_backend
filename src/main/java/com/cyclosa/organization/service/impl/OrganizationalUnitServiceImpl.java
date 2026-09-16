package com.cyclosa.organization.service.impl;

import com.cyclosa.common.exception.AppException;
import com.cyclosa.organization.dto.request.CreateOrgUnitRequest;
import com.cyclosa.organization.dto.request.MoveOrgUnitRequest;
import com.cyclosa.organization.dto.request.UpdateOrgUnitRequest;
import com.cyclosa.organization.dto.response.OrgUnitImpactPreviewResponse;
import com.cyclosa.organization.dto.response.OrgUnitResponse;
import com.cyclosa.organization.dto.response.OrgUnitTreeResponse;
import com.cyclosa.organization.entity.CostCenter;
import com.cyclosa.organization.entity.OrganizationalUnit;
import com.cyclosa.organization.exception.OrganizationErrorCode;
import com.cyclosa.organization.mapper.OrganizationalUnitMapper;
import com.cyclosa.organization.repository.CostCenterRepository;
import com.cyclosa.organization.repository.OrganizationalUnitRepository;
import com.cyclosa.organization.service.OrganizationalUnitService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.*;

@Slf4j
@Service
@RequiredArgsConstructor
public class OrganizationalUnitServiceImpl implements OrganizationalUnitService {

    private final OrganizationalUnitRepository orgUnitRepository;
    private final CostCenterRepository costCenterRepository;
    private final OrganizationalUnitMapper orgUnitMapper;

    @Override
    @Transactional
    public OrgUnitResponse createUnit(UUID companyId, CreateOrgUnitRequest request) {
        UUID effectiveCompanyId = request.getCompanyId() != null ? request.getCompanyId() : companyId;
        if (effectiveCompanyId == null) {
            throw AppException.badRequest("Yêu cầu cung cấp ID công ty");
        }

        if (orgUnitRepository.existsByCodeAndCompanyId(request.getCode(), effectiveCompanyId)) {
            throw new AppException(OrganizationErrorCode.ORG_UNIT_CODE_EXISTS);
        }

        OrganizationalUnit unit = orgUnitMapper.toEntity(request);
        unit.setCompanyId(effectiveCompanyId);

        if (request.getParentUnitId() != null) {
            OrganizationalUnit parent = orgUnitRepository.findByIdAndCompanyId(request.getParentUnitId(), effectiveCompanyId)
                    .orElseThrow(() -> new AppException(OrganizationErrorCode.ORG_UNIT_NOT_FOUND));
            unit.setParentUnit(parent);
        }

        if (request.getCostCenterId() != null) {
            CostCenter costCenter = costCenterRepository.findById(request.getCostCenterId())
                    .orElseThrow(() -> new AppException(OrganizationErrorCode.COST_CENTER_NOT_FOUND));
            unit.setCostCenter(costCenter);
        }

        unit = orgUnitRepository.save(unit);
        log.info("Created OrganizationalUnit id={}, code={}, companyId={}", unit.getId(), unit.getCode(), effectiveCompanyId);
        return orgUnitMapper.toResponse(unit);
    }

    @Override
    @Transactional
    public OrgUnitResponse updateUnit(UUID companyId, UUID id, UpdateOrgUnitRequest request) {
        OrganizationalUnit unit = orgUnitRepository.findById(id)
                .orElseThrow(() -> new AppException(OrganizationErrorCode.ORG_UNIT_NOT_FOUND));

        if (companyId != null && !unit.getCompanyId().equals(companyId)) {
            throw new AppException(OrganizationErrorCode.ORG_UNIT_NOT_FOUND);
        }

        unit.setName(request.getName());
        unit.setUnitType(request.getUnitType());
        unit.setManagerEmployeeId(request.getManagerEmployeeId());
        unit.setDescription(request.getDescription());
        unit.setStatus(request.getStatus());

        if (request.getCostCenterId() != null) {
            CostCenter costCenter = costCenterRepository.findById(request.getCostCenterId())
                    .orElseThrow(() -> new AppException(OrganizationErrorCode.COST_CENTER_NOT_FOUND));
            unit.setCostCenter(costCenter);
        } else {
            unit.setCostCenter(null);
        }

        unit = orgUnitRepository.save(unit);
        log.info("Updated OrganizationalUnit id={}", unit.getId());
        return orgUnitMapper.toResponse(unit);
    }

    @Override
    @Transactional
    public void deleteUnit(UUID companyId, UUID id) {
        OrganizationalUnit unit = orgUnitRepository.findById(id)
                .orElseThrow(() -> new AppException(OrganizationErrorCode.ORG_UNIT_NOT_FOUND));

        if (companyId != null && !unit.getCompanyId().equals(companyId)) {
            throw new AppException(OrganizationErrorCode.ORG_UNIT_NOT_FOUND);
        }

        if (orgUnitRepository.countByParentUnitId(id) > 0) {
            throw new AppException(OrganizationErrorCode.CANNOT_DELETE_UNIT_WITH_CHILDREN);
        }

        orgUnitRepository.delete(unit);
        log.info("Soft-deleted OrganizationalUnit id={}", id);
    }

    @Override
    @Transactional(readOnly = true)
    public OrgUnitResponse getUnitById(UUID companyId, UUID id) {
        OrganizationalUnit unit = orgUnitRepository.findById(id)
                .orElseThrow(() -> new AppException(OrganizationErrorCode.ORG_UNIT_NOT_FOUND));

        if (companyId != null && !unit.getCompanyId().equals(companyId)) {
            throw new AppException(OrganizationErrorCode.ORG_UNIT_NOT_FOUND);
        }
        return orgUnitMapper.toResponse(unit);
    }

    @Override
    @Transactional(readOnly = true)
    public List<OrgUnitTreeResponse> getUnitTree(UUID companyId) {
        if (companyId == null) {
            throw AppException.badRequest("Yêu cầu cung cấp ID công ty");
        }

        List<OrganizationalUnit> allUnits = orgUnitRepository.findAllByCompanyIdWithDetails(companyId);

        Map<UUID, OrgUnitTreeResponse> nodeMap = new LinkedHashMap<>();
        for (OrganizationalUnit unit : allUnits) {
            nodeMap.put(unit.getId(), orgUnitMapper.toTreeResponse(unit));
        }

        List<OrgUnitTreeResponse> rootNodes = new ArrayList<>();
        for (OrganizationalUnit unit : allUnits) {
            OrgUnitTreeResponse currentNode = nodeMap.get(unit.getId());
            if (unit.getParentUnit() != null && nodeMap.containsKey(unit.getParentUnit().getId())) {
                OrgUnitTreeResponse parentNode = nodeMap.get(unit.getParentUnit().getId());
                parentNode.getChildren().add(currentNode);
            } else {
                rootNodes.add(currentNode);
            }
        }

        return rootNodes;
    }

    @Override
    @Transactional(readOnly = true)
    public OrgUnitImpactPreviewResponse getImpactPreview(UUID companyId, UUID id, UUID targetParentId) {
        OrganizationalUnit unit = orgUnitRepository.findById(id)
                .orElseThrow(() -> new AppException(OrganizationErrorCode.ORG_UNIT_NOT_FOUND));

        UUID currentParentId = unit.getParentUnit() != null ? unit.getParentUnit().getId() : null;
        String currentParentName = unit.getParentUnit() != null ? unit.getParentUnit().getName() : "Gốc (Root)";

        String targetParentName = "Gốc (Root)";
        boolean isCircularLoop = false;

        if (targetParentId != null) {
            if (targetParentId.equals(id)) {
                isCircularLoop = true;
            } else {
                OrganizationalUnit targetParent = orgUnitRepository.findById(targetParentId)
                        .orElseThrow(() -> new AppException(OrganizationErrorCode.ORG_UNIT_NOT_FOUND));
                targetParentName = targetParent.getName();

                // Kiểm tra xem targetParent có phải là con/cháu của unit đang xét không
                OrganizationalUnit ancestor = targetParent.getParentUnit();
                while (ancestor != null) {
                    if (ancestor.getId().equals(id)) {
                        isCircularLoop = true;
                        break;
                    }
                    ancestor = ancestor.getParentUnit();
                }
            }
        }

        // Đếm tổng số đơn vị con cháu trực thuộc
        int affectedSubUnitsCount = countDescendants(id);

        String message = isCircularLoop
                ? "Cảnh báo: Phát hiện vòng lặp phân cấp! Không thể chuyển đơn vị vào cấp dưới của chính nó."
                : "Hợp lệ. Có thể tiến hành điều chuyển cơ cấu tổ chức.";

        return OrgUnitImpactPreviewResponse.builder()
                .unitId(unit.getId())
                .unitName(unit.getName())
                .currentParentId(currentParentId)
                .currentParentName(currentParentName)
                .targetParentId(targetParentId)
                .targetParentName(targetParentName)
                .affectedSubUnitsCount(affectedSubUnitsCount)
                .affectedEmployeesCount(0)
                .isCircularLoop(isCircularLoop)
                .message(message)
                .build();
    }

    @Override
    @Transactional
    public OrgUnitResponse moveUnit(UUID companyId, UUID id, MoveOrgUnitRequest request) {
        OrgUnitImpactPreviewResponse preview = getImpactPreview(companyId, id, request.getTargetParentId());

        if (preview.isCircularLoop()) {
            throw new AppException(OrganizationErrorCode.CIRCULAR_PARENT_DEPENDENCY);
        }

        OrganizationalUnit unit = orgUnitRepository.findById(id)
                .orElseThrow(() -> new AppException(OrganizationErrorCode.ORG_UNIT_NOT_FOUND));

        if (request.getTargetParentId() != null) {
            OrganizationalUnit newParent = orgUnitRepository.findById(request.getTargetParentId())
                    .orElseThrow(() -> new AppException(OrganizationErrorCode.ORG_UNIT_NOT_FOUND));
            unit.setParentUnit(newParent);
        } else {
            unit.setParentUnit(null);
        }

        unit = orgUnitRepository.save(unit);
        log.info("Moved OrganizationalUnit id={} to new targetParentId={}", id, request.getTargetParentId());
        return orgUnitMapper.toResponse(unit);
    }

    private int countDescendants(UUID parentId) {
        List<OrganizationalUnit> directChildren = orgUnitRepository.findByParentUnitId(parentId);
        int count = directChildren.size();
        for (OrganizationalUnit child : directChildren) {
            count += countDescendants(child.getId());
        }
        return count;
    }
}

package com.cyclosa.organization.service.impl;

import com.cyclosa.common.exception.AppException;
import com.cyclosa.organization.dto.request.CreateOrgUnitRequest;
import com.cyclosa.organization.dto.request.MoveOrgUnitRequest;
import com.cyclosa.organization.dto.request.UpdateOrgUnitRequest;
import com.cyclosa.organization.dto.response.OrgUnitDetailResponse;
import com.cyclosa.organization.dto.response.OrgUnitHistoryResponse;
import com.cyclosa.organization.dto.response.OrgUnitImpactPreviewResponse;
import com.cyclosa.organization.dto.response.OrgUnitResponse;
import com.cyclosa.organization.dto.response.OrgUnitTreeResponse;
import com.cyclosa.organization.entity.CostCenter;
import com.cyclosa.organization.entity.OrganizationalUnit;
import com.cyclosa.organization.entity.OrganizationalUnitHistory;
import com.cyclosa.organization.exception.OrganizationErrorCode;
import com.cyclosa.organization.mapper.OrganizationalUnitMapper;
import com.cyclosa.organization.repository.CostCenterRepository;
import com.cyclosa.organization.repository.OrganizationalUnitHistoryRepository;
import com.cyclosa.organization.repository.OrganizationalUnitRepository;
import com.cyclosa.organization.service.CompanyService;
import com.cyclosa.organization.service.OrganizationalUnitService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.*;

@Slf4j
@Service
@RequiredArgsConstructor
public class OrganizationalUnitServiceImpl implements OrganizationalUnitService {

    private final OrganizationalUnitRepository orgUnitRepository;
    private final OrganizationalUnitHistoryRepository historyRepository;
    private final CostCenterRepository costCenterRepository;
    private final OrganizationalUnitMapper orgUnitMapper;
    private final CompanyService companyService;

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

        // Lưu phiên bản lịch sử ban đầu (Effective Dating)
        OrganizationalUnitHistory initialHistory = OrganizationalUnitHistory.builder()
                .unitId(unit.getId())
                .companyId(effectiveCompanyId)
                .parentUnitId(unit.getParentUnit() != null ? unit.getParentUnit().getId() : null)
                .costCenterId(unit.getCostCenter() != null ? unit.getCostCenter().getId() : null)
                .managerEmployeeId(unit.getManagerEmployeeId())
                .code(unit.getCode())
                .name(unit.getName())
                .unitType(unit.getUnitType())
                .effectiveFrom(LocalDateTime.now())
                .effectiveTo(null)
                .changeReason("Khởi tạo đơn vị tổ chức mới")
                .status(unit.getStatus())
                .build();
        historyRepository.save(initialHistory);

        log.info("Created OrganizationalUnit id={}, code={}, companyId={}", unit.getId(), unit.getCode(), effectiveCompanyId);
        OrgUnitResponse response = orgUnitMapper.toResponse(unit);
        response.setCompany(companyService.getCompanySummary(effectiveCompanyId));
        return response;
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

        OrganizationalUnit savedUnit = orgUnitRepository.save(unit);

        // Cập nhật thông tin trên phiên bản lịch sử đang hiệu lực
        historyRepository.findCurrentActive(id).ifPresent(curr -> {
            curr.setName(savedUnit.getName());
            curr.setUnitType(savedUnit.getUnitType());
            curr.setManagerEmployeeId(savedUnit.getManagerEmployeeId());
            curr.setCostCenterId(savedUnit.getCostCenter() != null ? savedUnit.getCostCenter().getId() : null);
            curr.setStatus(savedUnit.getStatus());
            historyRepository.save(curr);
        });

        log.info("Updated OrganizationalUnit id={}", savedUnit.getId());
        OrgUnitResponse updateResponse = orgUnitMapper.toResponse(savedUnit);
        updateResponse.setCompany(companyService.getCompanySummary(savedUnit.getCompanyId()));
        return updateResponse;
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

        // Đóng phiên bản hiệu lực lịch sử
        historyRepository.findCurrentActive(id).ifPresent(curr -> {
            curr.setEffectiveTo(LocalDateTime.now());
            curr.setChangeReason("Xóa/giải thể đơn vị tổ chức");
            historyRepository.save(curr);
        });

        orgUnitRepository.delete(unit);
        log.info("Soft-deleted OrganizationalUnit id={}", id);
    }

    @Override
    @Transactional(readOnly = true)
    public OrgUnitDetailResponse getUnitById(UUID companyId, UUID id) {
        OrganizationalUnit unit = orgUnitRepository.findById(id)
                .orElseThrow(() -> new AppException(OrganizationErrorCode.ORG_UNIT_NOT_FOUND));

        if (companyId != null && !unit.getCompanyId().equals(companyId)) {
            throw new AppException(OrganizationErrorCode.ORG_UNIT_NOT_FOUND);
        }
        OrgUnitDetailResponse detailResponse = orgUnitMapper.toDetailResponse(unit);
        detailResponse.setCompany(companyService.getCompanySummary(unit.getCompanyId()));
        detailResponse.setChildUnitsCount(orgUnitRepository.countByParentUnitId(id));
        return detailResponse;
    }

    @Override
    @Transactional(readOnly = true)
    public List<OrgUnitTreeResponse> getUnitTree(UUID companyId, LocalDateTime atDate) {
        if (companyId == null) {
            throw AppException.badRequest("Yêu cầu cung cấp ID công ty");
        }

        // KỊCH BẢN 1: Nếu có tham số atDate -> Tái dựng cây tổ chức tại thời điểm quá khứ đó (Temporal Versioning)
        if (atDate != null) {
            List<OrganizationalUnitHistory> histories = historyRepository.findByCompanyIdAtTimestamp(companyId, atDate);
            Map<UUID, OrgUnitTreeResponse> historyNodeMap = new LinkedHashMap<>();
            for (OrganizationalUnitHistory h : histories) {
                historyNodeMap.put(h.getUnitId(), orgUnitMapper.historyToTreeResponse(h));
            }

            List<OrgUnitTreeResponse> rootNodes = new ArrayList<>();
            for (OrganizationalUnitHistory h : histories) {
                OrgUnitTreeResponse currentNode = historyNodeMap.get(h.getUnitId());
                if (h.getParentUnitId() != null && historyNodeMap.containsKey(h.getParentUnitId())) {
                    OrgUnitTreeResponse parentNode = historyNodeMap.get(h.getParentUnitId());
                    parentNode.getChildren().add(currentNode);
                } else {
                    rootNodes.add(currentNode);
                }
            }
            return rootNodes;
        }

        // KỊCH BẢN 2: Lấy cây cơ cấu hiện tại (Current State)
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

        // 1. Chốt phiên bản hiệu lực cũ
        LocalDateTime now = LocalDateTime.now();
        historyRepository.findCurrentActive(id).ifPresent(curr -> {
            curr.setEffectiveTo(now);
            historyRepository.save(curr);
        });

        // 2. Mở phiên bản hiệu lực mới với thông tin cha mới và lý do điều chuyển
        String reason = (request.getChangeReason() != null && !request.getChangeReason().isBlank())
                ? request.getChangeReason()
                : "Điều chuyển nhánh cây tổ chức";

        OrganizationalUnitHistory newHistory = OrganizationalUnitHistory.builder()
                .unitId(unit.getId())
                .companyId(unit.getCompanyId())
                .parentUnitId(unit.getParentUnit() != null ? unit.getParentUnit().getId() : null)
                .costCenterId(unit.getCostCenter() != null ? unit.getCostCenter().getId() : null)
                .managerEmployeeId(unit.getManagerEmployeeId())
                .code(unit.getCode())
                .name(unit.getName())
                .unitType(unit.getUnitType())
                .effectiveFrom(now)
                .effectiveTo(null)
                .changeReason(reason)
                .status(unit.getStatus())
                .build();
        historyRepository.save(newHistory);

        log.info("Moved OrganizationalUnit id={} to targetParentId={}, history version recorded", id, request.getTargetParentId());
        OrgUnitResponse moveResponse = orgUnitMapper.toResponse(unit);
        moveResponse.setCompany(companyService.getCompanySummary(unit.getCompanyId()));
        return moveResponse;
    }

    @Override
    @Transactional(readOnly = true)
    public List<OrgUnitHistoryResponse> getUnitHistory(UUID companyId, UUID id) {
        // Đảm bảo đơn vị tồn tại
        getUnitById(companyId, id);

        List<OrganizationalUnitHistory> histories = historyRepository.findByUnitIdOrderByEffectiveFromDesc(id);
        List<OrgUnitHistoryResponse> responses = orgUnitMapper.toHistoryResponseList(histories);

        // Bổ sung tên đơn vị cha nếu có
        for (OrgUnitHistoryResponse res : responses) {
            if (res.getParentUnitId() != null) {
                orgUnitRepository.findById(res.getParentUnitId())
                        .ifPresent(p -> res.setParentUnitName(p.getName()));
            } else {
                res.setParentUnitName("Gốc (Root)");
            }
        }
        return responses;
    }

    @Override
    @Transactional(readOnly = true)
    public Set<UUID> getSelfAndDescendantUnitIds(UUID companyId, UUID unitId) {
        // Đảm bảo đơn vị tồn tại
        getUnitById(companyId, unitId);

        Set<UUID> resultSet = new LinkedHashSet<>();
        collectDescendantsRecursively(unitId, resultSet);
        return resultSet;
    }

    private void collectDescendantsRecursively(UUID parentId, Set<UUID> collected) {
        collected.add(parentId);
        List<OrganizationalUnit> directChildren = orgUnitRepository.findByParentUnitId(parentId);
        for (OrganizationalUnit child : directChildren) {
            collectDescendantsRecursively(child.getId(), collected);
        }
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

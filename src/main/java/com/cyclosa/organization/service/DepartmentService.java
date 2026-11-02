package com.cyclosa.organization.service;

import com.cyclosa.common.exception.AppException;
import com.cyclosa.common.response.PageData;
import com.cyclosa.organization.dto.request.CreateDepartmentRequest;
import com.cyclosa.organization.dto.request.MoveDepartmentRequest;
import com.cyclosa.organization.dto.request.PermissionInheritanceRequest;
import com.cyclosa.organization.dto.request.UpdateDepartmentRequest;
import com.cyclosa.organization.dto.response.DepartmentImpactResponse;
import com.cyclosa.organization.dto.response.DepartmentNodeResponse;
import com.cyclosa.organization.dto.response.DepartmentResponse;
import com.cyclosa.organization.dto.response.DepartmentVersionResponse;
import com.cyclosa.organization.entity.CostCenter;
import com.cyclosa.organization.entity.Department;
import com.cyclosa.organization.entity.DepartmentVersion;
import com.cyclosa.organization.exception.OrganizationErrorCode;
import com.cyclosa.organization.repository.CompanyRepository;
import com.cyclosa.organization.repository.CostCenterRepository;
import com.cyclosa.organization.repository.DepartmentRepository;
import com.cyclosa.organization.repository.DepartmentVersionRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.poi.ss.usermodel.*;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.time.LocalDate;
import java.util.*;
import java.util.stream.Collectors;

@Slf4j
@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class DepartmentService {

    private final DepartmentRepository departmentRepository;
    private final DepartmentVersionRepository versionRepository;
    private final CompanyRepository companyRepository;
    private final CostCenterRepository costCenterRepository;

    public PageData<DepartmentResponse> getDepartments(UUID companyId, String keyword, Pageable pageable) {
        verifyCompanyExists(companyId);
        Page<Department> page = departmentRepository.search(companyId, keyword, pageable);
        LocalDate today = LocalDate.now();

        return PageData.of(page, d -> {
            DepartmentVersion version = versionRepository.findActiveVersionAt(d.getId(), today)
                    .or(() -> versionRepository.findLatestVersion(d.getId()))
                    .orElse(null);
            return mapToDepartmentResponse(d, version);
        });
    }

    public DepartmentResponse getDepartmentById(UUID id, LocalDate asOfDate) {
        Department department = findDepartmentOrThrow(id);
        LocalDate queryDate = asOfDate != null ? asOfDate : LocalDate.now();

        DepartmentVersion version = versionRepository.findActiveVersionAt(id, queryDate)
                .or(() -> versionRepository.findLatestVersion(id))
                .orElse(null);

        return mapToDepartmentResponse(department, version);
    }

    public List<DepartmentVersionResponse> getDepartmentHistory(UUID id) {
        findDepartmentOrThrow(id);
        List<DepartmentVersion> versions = versionRepository.findAllByDepartmentIdOrderByEffectiveFromDesc(id);

        Map<UUID, String> parentNameMap = resolveDepartmentNames(
                versions.stream().map(DepartmentVersion::getParentDepartmentId).filter(Objects::nonNull).collect(Collectors.toSet())
        );
        Map<UUID, String> costCenterNameMap = resolveCostCenterNames(
                versions.stream().map(DepartmentVersion::getCostCenterId).filter(Objects::nonNull).collect(Collectors.toSet())
        );

        return versions.stream().map(v -> DepartmentVersionResponse.builder()
                .id(v.getId())
                .departmentId(v.getDepartment().getId())
                .effectiveFrom(v.getEffectiveFrom())
                .name(v.getName())
                .parentDepartmentId(v.getParentDepartmentId())
                .parentDepartmentName(v.getParentDepartmentId() != null ? parentNameMap.get(v.getParentDepartmentId()) : null)
                .costCenterId(v.getCostCenterId())
                .costCenterName(v.getCostCenterId() != null ? costCenterNameMap.get(v.getCostCenterId()) : null)
                .managerEmployeeId(v.getManagerEmployeeId())
                .isPermissionInherited(v.getIsPermissionInherited())
                .createdAt(v.getCreatedAt())
                .build()).toList();
    }

    public List<DepartmentNodeResponse> getDepartmentTree(UUID companyId, LocalDate asOfDate) {
        verifyCompanyExists(companyId);
        LocalDate queryDate = asOfDate != null ? asOfDate : LocalDate.now();

        List<DepartmentVersion> activeVersions = versionRepository.findActiveVersionsForCompanyAt(companyId, queryDate);
        if (activeVersions.isEmpty()) {
            activeVersions = versionRepository.findLatestVersionsForCompany(companyId);
        }

        Map<UUID, String> costCenterNameMap = resolveCostCenterNames(
                activeVersions.stream().map(DepartmentVersion::getCostCenterId).filter(Objects::nonNull).collect(Collectors.toSet())
        );

        Map<UUID, DepartmentNodeResponse> nodeMap = new HashMap<>();
        List<DepartmentNodeResponse> rootNodes = new ArrayList<>();

        for (DepartmentVersion v : activeVersions) {
            Department dept = v.getDepartment();
            DepartmentNodeResponse node = DepartmentNodeResponse.builder()
                    .id(dept.getId())
                    .code(dept.getCode())
                    .name(v.getName())
                    .parentDepartmentId(v.getParentDepartmentId())
                    .costCenterId(v.getCostCenterId())
                    .costCenterName(v.getCostCenterId() != null ? costCenterNameMap.get(v.getCostCenterId()) : null)
                    .managerEmployeeId(v.getManagerEmployeeId())
                    .isPermissionInherited(v.getIsPermissionInherited())
                    .effectiveFrom(v.getEffectiveFrom())
                    .memberCount(0)
                    .children(new ArrayList<>())
                    .build();
            nodeMap.put(dept.getId(), node);
        }

        for (DepartmentNodeResponse node : nodeMap.values()) {
            if (node.getParentDepartmentId() != null && nodeMap.containsKey(node.getParentDepartmentId())) {
                nodeMap.get(node.getParentDepartmentId()).getChildren().add(node);
            } else {
                rootNodes.add(node);
            }
        }

        return rootNodes;
    }

    @Transactional
    public DepartmentResponse createDepartment(CreateDepartmentRequest request) {
        verifyCompanyExists(request.getCompanyId());

        if (departmentRepository.existsByCompanyIdAndCode(request.getCompanyId(), request.getCode())) {
            throw new AppException(OrganizationErrorCode.DEPARTMENT_CODE_EXISTS);
        }

        if (request.getParentDepartmentId() != null) {
            findDepartmentOrThrow(request.getParentDepartmentId());
        }

        if (request.getCostCenterId() != null) {
            verifyCostCenterExists(request.getCostCenterId());
        }

        Department department = Department.builder()
                .companyId(request.getCompanyId())
                .code(request.getCode().toUpperCase().trim())
                .dissolvedDate(null)
                .deleted(false)
                .build();

        department = departmentRepository.save(department);

        DepartmentVersion version = DepartmentVersion.builder()
                .department(department)
                .effectiveFrom(request.getEffectiveFrom())
                .name(request.getName().trim())
                .parentDepartmentId(request.getParentDepartmentId())
                .costCenterId(request.getCostCenterId())
                .managerEmployeeId(request.getManagerEmployeeId())
                .isPermissionInherited(request.getIsPermissionInherited() != null ? request.getIsPermissionInherited() : true)
                .deleted(false)
                .build();

        version = versionRepository.save(version);
        log.info("Đã tạo mới phòng ban: id={}, code={}, versionId={}", department.getId(), department.getCode(), version.getId());

        return mapToDepartmentResponse(department, version);
    }

    @Transactional
    public DepartmentResponse updateDepartment(UUID id, UpdateDepartmentRequest request) {
        Department department = findDepartmentOrThrow(id);

        if (request.getParentDepartmentId() != null) {
            validateNoCycle(id, request.getParentDepartmentId());
        }

        if (request.getCostCenterId() != null) {
            verifyCostCenterExists(request.getCostCenterId());
        }

        Optional<DepartmentVersion> latestOpt = versionRepository.findLatestVersion(id);
        DepartmentVersion targetVersion;

        if (latestOpt.isPresent()) {
            DepartmentVersion latest = latestOpt.get();
            if (request.getEffectiveFrom().isBefore(latest.getEffectiveFrom())) {
                throw new AppException(OrganizationErrorCode.EFFECTIVE_DATE_INVALID);
            }

            if (request.getEffectiveFrom().isEqual(latest.getEffectiveFrom())) {
                latest.setName(request.getName().trim());
                latest.setParentDepartmentId(request.getParentDepartmentId());
                latest.setCostCenterId(request.getCostCenterId());
                latest.setManagerEmployeeId(request.getManagerEmployeeId());
                if (request.getIsPermissionInherited() != null) {
                    latest.setIsPermissionInherited(request.getIsPermissionInherited());
                }
                targetVersion = versionRepository.save(latest);
            } else {
                targetVersion = DepartmentVersion.builder()
                        .department(department)
                        .effectiveFrom(request.getEffectiveFrom())
                        .name(request.getName().trim())
                        .parentDepartmentId(request.getParentDepartmentId())
                        .costCenterId(request.getCostCenterId())
                        .managerEmployeeId(request.getManagerEmployeeId())
                        .isPermissionInherited(request.getIsPermissionInherited() != null ? request.getIsPermissionInherited() : latest.getIsPermissionInherited())
                        .deleted(false)
                        .build();
                targetVersion = versionRepository.save(targetVersion);
            }
        } else {
            targetVersion = DepartmentVersion.builder()
                    .department(department)
                    .effectiveFrom(request.getEffectiveFrom())
                    .name(request.getName().trim())
                    .parentDepartmentId(request.getParentDepartmentId())
                    .costCenterId(request.getCostCenterId())
                    .managerEmployeeId(request.getManagerEmployeeId())
                    .isPermissionInherited(request.getIsPermissionInherited() != null ? request.getIsPermissionInherited() : true)
                    .deleted(false)
                    .build();
            targetVersion = versionRepository.save(targetVersion);
        }

        log.info("Đã cập nhật phòng ban: id={}, versionId={}", department.getId(), targetVersion.getId());
        return mapToDepartmentResponse(department, targetVersion);
    }

    @Transactional
    public DepartmentResponse moveDepartment(UUID id, MoveDepartmentRequest request) {
        Department department = findDepartmentOrThrow(id);

        if (request.getNewParentDepartmentId() != null) {
            validateNoCycle(id, request.getNewParentDepartmentId());
        }

        DepartmentVersion latest = versionRepository.findLatestVersion(id)
                .orElseThrow(() -> new AppException(OrganizationErrorCode.DEPARTMENT_NOT_FOUND));

        if (request.getEffectiveFrom().isBefore(latest.getEffectiveFrom())) {
            throw new AppException(OrganizationErrorCode.EFFECTIVE_DATE_INVALID);
        }

        DepartmentVersion newVersion;
        if (request.getEffectiveFrom().isEqual(latest.getEffectiveFrom())) {
            latest.setParentDepartmentId(request.getNewParentDepartmentId());
            newVersion = versionRepository.save(latest);
        } else {
            newVersion = DepartmentVersion.builder()
                    .department(department)
                    .effectiveFrom(request.getEffectiveFrom())
                    .name(latest.getName())
                    .parentDepartmentId(request.getNewParentDepartmentId())
                    .costCenterId(latest.getCostCenterId())
                    .managerEmployeeId(latest.getManagerEmployeeId())
                    .isPermissionInherited(latest.getIsPermissionInherited())
                    .deleted(false)
                    .build();
            newVersion = versionRepository.save(newVersion);
        }

        log.info("Đã di chuyển phòng ban id={} sang parentId={}", id, request.getNewParentDepartmentId());
        return mapToDepartmentResponse(department, newVersion);
    }

    public DepartmentImpactResponse calculateMoveImpact(UUID departmentId, UUID targetParentId) {
        Department department = findDepartmentOrThrow(departmentId);
        DepartmentVersion latest = versionRepository.findLatestVersion(departmentId).orElse(null);
        String deptName = latest != null ? latest.getName() : department.getCode();

        String targetParentName = null;
        if (targetParentId != null) {
            Department targetParent = findDepartmentOrThrow(targetParentId);
            DepartmentVersion targetParentVer = versionRepository.findLatestVersion(targetParentId).orElse(null);
            targetParentName = targetParentVer != null ? targetParentVer.getName() : targetParent.getCode();
        }

        boolean isValid = true;
        String invalidReason = null;

        if (targetParentId != null) {
            if (targetParentId.equals(departmentId)) {
                isValid = false;
                invalidReason = "Không thể chọn chính phòng ban này làm cấp cha.";
            } else {
                List<UUID> descendants = collectAllSubDepartmentIds(departmentId);
                if (descendants.contains(targetParentId)) {
                    isValid = false;
                    invalidReason = "Phát hiện vòng lặp phân cấp: Không thể chuyển vào phòng ban cấp con cháu trực thuộc.";
                }
            }
        }

        List<UUID> subIds = collectAllSubDepartmentIds(departmentId);

        return DepartmentImpactResponse.builder()
                .departmentId(departmentId)
                .departmentName(deptName)
                .targetParentDepartmentId(targetParentId)
                .targetParentDepartmentName(targetParentName)
                .isValid(isValid)
                .invalidReason(invalidReason)
                .affectedSubDepartmentCount(subIds.size())
                .affectedEmployeeCount(0) // Liên kết module Nhân viên ở giai đoạn sau
                .affectedDepartmentIds(subIds)
                .build();
    }

    @Transactional
    public void updatePermissionInheritance(UUID id, PermissionInheritanceRequest request) {
        Department department = findDepartmentOrThrow(id);
        DepartmentVersion latest = versionRepository.findLatestVersion(id)
                .orElseThrow(() -> new AppException(OrganizationErrorCode.DEPARTMENT_NOT_FOUND));

        latest.setIsPermissionInherited(request.getIsPermissionInherited());
        versionRepository.save(latest);
        log.info("Đã cập nhật cờ kế thừa quyền cho phòng ban id={}: {}", id, request.getIsPermissionInherited());
    }

    @Transactional
    public void dissolveDepartment(UUID id, LocalDate dissolvedDate) {
        Department department = findDepartmentOrThrow(id);

        List<UUID> subIds = collectAllSubDepartmentIds(id);
        if (!subIds.isEmpty()) {
            throw new AppException(OrganizationErrorCode.DEPARTMENT_HAS_CHILDREN);
        }

        department.setDissolvedDate(dissolvedDate != null ? dissolvedDate : LocalDate.now());
        departmentRepository.save(department);
        log.info("Đã giải thể phòng ban: id={}, dissolvedDate={}", id, department.getDissolvedDate());
    }

    public byte[] exportOrganizationTree(UUID companyId, LocalDate asOfDate) {
        List<DepartmentNodeResponse> tree = getDepartmentTree(companyId, asOfDate);

        try (Workbook workbook = new XSSFWorkbook(); ByteArrayOutputStream out = new ByteArrayOutputStream()) {
            Sheet sheet = workbook.createSheet("Sơ đồ tổ chức");

            CellStyle headerStyle = workbook.createCellStyle();
            Font headerFont = workbook.createFont();
            headerFont.setBold(true);
            headerFont.setColor(IndexedColors.WHITE.getIndex());
            headerStyle.setFont(headerFont);
            headerStyle.setFillForegroundColor(IndexedColors.DARK_BLUE.getIndex());
            headerStyle.setFillPattern(FillPatternType.SOLID_FOREGROUND);
            headerStyle.setAlignment(HorizontalAlignment.CENTER);

            Row headerRow = sheet.createRow(0);
            String[] columns = {"STT", "Cấp bậc", "Mã đơn vị", "Tên đơn vị / Phòng ban", "Trung tâm chi phí", "Ngày hiệu lực", "Kế thừa quyền"};
            for (int i = 0; i < columns.length; i++) {
                Cell cell = headerRow.createCell(i);
                cell.setCellValue(columns[i]);
                cell.setCellStyle(headerStyle);
            }

            int[] rowIdx = {1};
            int[] stt = {1};
            writeTreeToExcel(sheet, tree, 0, rowIdx, stt);

            for (int i = 0; i < columns.length; i++) {
                sheet.autoSizeColumn(i);
            }

            workbook.write(out);
            return out.toByteArray();
        } catch (IOException e) {
            log.error("Lỗi khi xuất sơ đồ tổ chức ra Excel", e);
            throw new RuntimeException("Không thể xuất file sơ đồ tổ chức", e);
        }
    }

    private void writeTreeToExcel(Sheet sheet, List<DepartmentNodeResponse> nodes, int level, int[] rowIdx, int[] stt) {
        for (DepartmentNodeResponse node : nodes) {
            Row row = sheet.createRow(rowIdx[0]++);
            row.createCell(0).setCellValue(stt[0]++);
            row.createCell(1).setCellValue("Cấp " + (level + 1));
            row.createCell(2).setCellValue(node.getCode());
            String prefix = "    ".repeat(level);
            row.createCell(3).setCellValue(prefix + (level > 0 ? "↳ " : "") + node.getName());
            row.createCell(4).setCellValue(node.getCostCenterName() != null ? node.getCostCenterName() : "");
            row.createCell(5).setCellValue(node.getEffectiveFrom() != null ? node.getEffectiveFrom().toString() : "");
            row.createCell(6).setCellValue(Boolean.TRUE.equals(node.getIsPermissionInherited()) ? "Có" : "Không");

            if (node.getChildren() != null && !node.getChildren().isEmpty()) {
                writeTreeToExcel(sheet, node.getChildren(), level + 1, rowIdx, stt);
            }
        }
    }

    private void validateNoCycle(UUID movingDeptId, UUID newParentId) {
        if (movingDeptId.equals(newParentId)) {
            throw new AppException(OrganizationErrorCode.ORG_CYCLE_DETECTED);
        }

        UUID current = newParentId;
        Set<UUID> visited = new HashSet<>();

        while (current != null) {
            if (current.equals(movingDeptId)) {
                throw new AppException(OrganizationErrorCode.ORG_CYCLE_DETECTED);
            }
            if (visited.contains(current)) {
                break;
            }
            visited.add(current);

            DepartmentVersion ver = versionRepository.findLatestVersion(current).orElse(null);
            current = ver != null ? ver.getParentDepartmentId() : null;
        }
    }

    private List<UUID> collectAllSubDepartmentIds(UUID parentId) {
        List<UUID> result = new ArrayList<>();
        List<DepartmentVersion> allLatest = versionRepository.findAll();
        Map<UUID, List<UUID>> parentToChildren = new HashMap<>();

        for (DepartmentVersion v : allLatest) {
            if (v.getParentDepartmentId() != null) {
                parentToChildren.computeIfAbsent(v.getParentDepartmentId(), k -> new ArrayList<>())
                        .add(v.getDepartment().getId());
            }
        }

        Queue<UUID> queue = new LinkedList<>();
        queue.add(parentId);

        while (!queue.isEmpty()) {
            UUID current = queue.poll();
            List<UUID> children = parentToChildren.getOrDefault(current, Collections.emptyList());
            for (UUID child : children) {
                if (!result.contains(child)) {
                    result.add(child);
                    queue.add(child);
                }
            }
        }

        return result;
    }

    public Department findDepartmentOrThrow(UUID id) {
        return departmentRepository.findById(id)
                .orElseThrow(() -> new AppException(OrganizationErrorCode.DEPARTMENT_NOT_FOUND));
    }

    private void verifyCompanyExists(UUID companyId) {
        if (!companyRepository.existsById(companyId)) {
            throw new AppException(OrganizationErrorCode.COMPANY_NOT_FOUND);
        }
    }

    private void verifyCostCenterExists(UUID costCenterId) {
        if (!costCenterRepository.existsById(costCenterId)) {
            throw new AppException(OrganizationErrorCode.COST_CENTER_NOT_FOUND);
        }
    }

    private Map<UUID, String> resolveDepartmentNames(Set<UUID> deptIds) {
        if (deptIds.isEmpty()) return Collections.emptyMap();
        Map<UUID, String> map = new HashMap<>();
        for (UUID id : deptIds) {
            versionRepository.findLatestVersion(id).ifPresent(v -> map.put(id, v.getName()));
        }
        return map;
    }

    private Map<UUID, String> resolveCostCenterNames(Set<UUID> costCenterIds) {
        if (costCenterIds.isEmpty()) return Collections.emptyMap();
        return costCenterRepository.findAllById(costCenterIds).stream()
                .collect(Collectors.toMap(CostCenter::getId, CostCenter::getName));
    }

    private DepartmentResponse mapToDepartmentResponse(Department dept, DepartmentVersion version) {
        String parentName = null;
        String costCenterName = null;

        if (version != null) {
            if (version.getParentDepartmentId() != null) {
                parentName = versionRepository.findLatestVersion(version.getParentDepartmentId())
                        .map(DepartmentVersion::getName).orElse(null);
            }
            if (version.getCostCenterId() != null) {
                costCenterName = costCenterRepository.findById(version.getCostCenterId())
                        .map(CostCenter::getName).orElse(null);
            }
        }

        boolean isActive = dept.getDissolvedDate() == null || dept.getDissolvedDate().isAfter(LocalDate.now());

        return DepartmentResponse.builder()
                .id(dept.getId())
                .companyId(dept.getCompanyId())
                .code(dept.getCode())
                .name(version != null ? version.getName() : dept.getCode())
                .parentDepartmentId(version != null ? version.getParentDepartmentId() : null)
                .parentDepartmentName(parentName)
                .costCenterId(version != null ? version.getCostCenterId() : null)
                .costCenterName(costCenterName)
                .managerEmployeeId(version != null ? version.getManagerEmployeeId() : null)
                .isPermissionInherited(version != null ? version.getIsPermissionInherited() : true)
                .effectiveFrom(version != null ? version.getEffectiveFrom() : null)
                .dissolvedDate(dept.getDissolvedDate())
                .isActive(isActive)
                .createdAt(dept.getCreatedAt())
                .updatedAt(dept.getUpdatedAt())
                .build();
    }
}


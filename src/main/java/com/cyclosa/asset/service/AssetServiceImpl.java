package com.cyclosa.asset.service;

import com.cyclosa.asset.dto.request.*;
import com.cyclosa.asset.dto.response.AssetAllocationResponse;
import com.cyclosa.asset.dto.response.AssetDetailResponse;
import com.cyclosa.asset.dto.response.AssetResponse;
import com.cyclosa.asset.entity.Asset;
import com.cyclosa.asset.entity.AssetAllocation;
import com.cyclosa.asset.enums.AssetCondition;
import com.cyclosa.asset.enums.AssetStatus;
import com.cyclosa.asset.exception.AssetErrorCode;
import com.cyclosa.asset.mapper.AssetMapper;
import com.cyclosa.asset.repository.AssetAllocationRepository;
import com.cyclosa.asset.repository.AssetRepository;
import com.cyclosa.common.exception.AppException;
import com.cyclosa.common.response.PageData;
import com.cyclosa.employee.entity.Employee;
import com.cyclosa.employee.enums.EmploymentStatus;
import com.cyclosa.employee.repository.EmployeeRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.*;
import java.util.stream.Collectors;

@Slf4j
@Service
@RequiredArgsConstructor
public class AssetServiceImpl implements AssetService {

    private final AssetRepository assetRepository;
    private final AssetAllocationRepository assetAllocationRepository;
    private final EmployeeRepository employeeRepository;
    private final AssetMapper assetMapper;

    @Override
    @Transactional
    public AssetResponse createAsset(CreateAssetRequest request) {
        if (assetRepository.existsByAssetCodeAndCompanyId(request.getAssetCode(), request.getCompanyId())) {
            throw new AppException(AssetErrorCode.ASSET_CODE_ALREADY_EXISTS);
        }

        Asset asset = assetMapper.toEntity(request);
        asset.setStatus(AssetStatus.IN_STOCK);
        Asset savedAsset = assetRepository.save(asset);

        log.info("Đã tạo mới tài sản: code={}, id={}, companyId={}", savedAsset.getAssetCode(), savedAsset.getId(), savedAsset.getCompanyId());
        return assetMapper.toResponse(savedAsset);
    }

    @Override
    @Transactional
    public AssetResponse updateAsset(UUID id, UpdateAssetRequest request) {
        Asset asset = assetRepository.findById(id)
                .orElseThrow(() -> new AppException(AssetErrorCode.ASSET_NOT_FOUND));

        asset.setName(request.getName());
        asset.setCategory(request.getCategory());
        asset.setPurchaseDate(request.getPurchaseDate());
        asset.setPurchaseCost(request.getPurchaseCost());
        asset.setSerialNumber(request.getSerialNumber());
        asset.setSpecifications(request.getSpecifications());
        asset.setWarrantyExpiryDate(request.getWarrantyExpiryDate());
        asset.setLocation(request.getLocation());
        asset.setNote(request.getNote());

        Asset updated = assetRepository.save(asset);
        AssetResponse response = assetMapper.toResponse(updated);
        enrichCurrentEmployee(response, updated.getId());
        return response;
    }

    @Override
    @Transactional(readOnly = true)
    public AssetDetailResponse getAssetById(UUID id) {
        Asset asset = assetRepository.findById(id)
                .orElseThrow(() -> new AppException(AssetErrorCode.ASSET_NOT_FOUND));

        AssetDetailResponse response = assetMapper.toDetailResponse(asset);

        // Lấy lịch sử cấp phát
        List<AssetAllocation> allocations = assetAllocationRepository.findByAssetIdOrderByAllocatedDateDesc(id);
        if (!allocations.isEmpty()) {
            Set<UUID> employeeIds = allocations.stream().map(AssetAllocation::getEmployeeId).collect(Collectors.toSet());
            Map<UUID, Employee> employeeMap = employeeRepository.findAllById(employeeIds).stream()
                    .collect(Collectors.toMap(Employee::getId, e -> e));

            List<AssetAllocationResponse> allocationResponses = allocations.stream().map(alloc -> {
                AssetAllocationResponse r = assetMapper.toAllocationResponse(alloc);
                Employee emp = employeeMap.get(alloc.getEmployeeId());
                if (emp != null) {
                    r.setEmployeeName(emp.getFullName());
                    r.setEmployeeCode(emp.getEmployeeCode());
                }
                return r;
            }).toList();

            response.setAllocations(allocationResponses);

            // Gán thông tin nhân viên hiện tại nếu đang cấp phát
            allocations.stream()
                    .filter(a -> a.getReturnedDate() == null)
                    .findFirst()
                    .ifPresent(openAlloc -> {
                        response.setCurrentEmployeeId(openAlloc.getEmployeeId());
                        response.setCurrentAllocatedDate(openAlloc.getAllocatedDate());
                        Employee emp = employeeMap.get(openAlloc.getEmployeeId());
                        if (emp != null) {
                            response.setCurrentEmployeeName(emp.getFullName());
                            response.setCurrentEmployeeCode(emp.getEmployeeCode());
                        }
                    });
        }

        return response;
    }

    @Override
    @Transactional(readOnly = true)
    public PageData<AssetResponse> getAssets(AssetFilter filter, Pageable pageable) {
        Page<Asset> page = assetRepository.searchAssets(
                filter.getCompanyId(),
                filter.getCategory(),
                filter.getStatus(),
                filter.getKeyword(),
                pageable
        );

        List<Asset> assets = page.getContent();
        if (assets.isEmpty()) {
            return PageData.from(page.map(assetMapper::toResponse));
        }

        // Tìm các asset đang ALLOCATED để enrich thông tin nhân viên đang giữ
        List<UUID> allocatedAssetIds = assets.stream()
                .filter(a -> a.getStatus() == AssetStatus.ALLOCATED)
                .map(Asset::getId)
                .toList();

        Map<UUID, AssetAllocation> openAllocMap = new HashMap<>();
        Map<UUID, Employee> employeeMap = new HashMap<>();

        if (!allocatedAssetIds.isEmpty()) {
            for (UUID assetId : allocatedAssetIds) {
                assetAllocationRepository.findByAssetIdAndReturnedDateIsNull(assetId)
                        .ifPresent(alloc -> openAllocMap.put(assetId, alloc));
            }

            Set<UUID> empIds = openAllocMap.values().stream()
                    .map(AssetAllocation::getEmployeeId)
                    .collect(Collectors.toSet());

            if (!empIds.isEmpty()) {
                employeeRepository.findAllById(empIds)
                        .forEach(e -> employeeMap.put(e.getId(), e));
            }
        }

        Page<AssetResponse> responsePage = page.map(asset -> {
            AssetResponse res = assetMapper.toResponse(asset);
            AssetAllocation openAlloc = openAllocMap.get(asset.getId());
            if (openAlloc != null) {
                res.setCurrentEmployeeId(openAlloc.getEmployeeId());
                res.setCurrentAllocatedDate(openAlloc.getAllocatedDate());
                Employee emp = employeeMap.get(openAlloc.getEmployeeId());
                if (emp != null) {
                    res.setCurrentEmployeeName(emp.getFullName());
                    res.setCurrentEmployeeCode(emp.getEmployeeCode());
                }
            }
            return res;
        });

        return PageData.from(responsePage);
    }

    @Override
    @Transactional
    public void deleteAsset(UUID id) {
        Asset asset = assetRepository.findById(id)
                .orElseThrow(() -> new AppException(AssetErrorCode.ASSET_NOT_FOUND));

        if (asset.getStatus() == AssetStatus.ALLOCATED) {
            throw new AppException(AssetErrorCode.CANNOT_DISPOSE_ALLOCATED_ASSET, "Không thể xóa tài sản đang được cấp phát");
        }

        assetRepository.delete(asset);
        log.info("Đã xóa mềm tài sản: id={}, code={}", id, asset.getAssetCode());
    }

    @Override
    @Transactional
    public AssetAllocationResponse allocateAsset(UUID id, AllocateAssetRequest request) {
        Asset asset = assetRepository.findById(id)
                .orElseThrow(() -> new AppException(AssetErrorCode.ASSET_NOT_FOUND));

        if (asset.getStatus() != AssetStatus.IN_STOCK) {
            throw new AppException(AssetErrorCode.ASSET_NOT_AVAILABLE);
        }

        if (assetAllocationRepository.existsByAssetIdAndReturnedDateIsNull(id)) {
            throw new AppException(AssetErrorCode.ASSET_ALREADY_ALLOCATED);
        }

        Employee employee = employeeRepository.findById(request.getEmployeeId())
                .orElseThrow(() -> new AppException(AssetErrorCode.EMPLOYEE_NOT_FOUND));

        if (employee.getEmploymentStatus() == EmploymentStatus.RESIGNED
                || employee.getEmploymentStatus() == EmploymentStatus.TERMINATED) {
            throw new AppException(AssetErrorCode.EMPLOYEE_NOT_ACTIVE);
        }

        AssetAllocation allocation = AssetAllocation.builder()
                .asset(asset)
                .employeeId(employee.getId())
                .onboardingProcessId(request.getOnboardingProcessId())
                .allocatedDate(request.getAllocatedDate())
                .conditionOnAllocation(request.getConditionOnAllocation())
                .note(request.getNote())
                .build();

        asset.setStatus(AssetStatus.ALLOCATED);
        assetRepository.save(asset);
        AssetAllocation savedAlloc = assetAllocationRepository.save(allocation);

        log.info("Cấp phát tài sản: assetCode={}, employeeCode={}, allocatedDate={}",
                asset.getAssetCode(), employee.getEmployeeCode(), request.getAllocatedDate());

        AssetAllocationResponse response = assetMapper.toAllocationResponse(savedAlloc);
        response.setEmployeeName(employee.getFullName());
        response.setEmployeeCode(employee.getEmployeeCode());
        return response;
    }

    @Override
    @Transactional
    public AssetAllocationResponse returnAsset(UUID id, ReturnAssetRequest request) {
        Asset asset = assetRepository.findById(id)
                .orElseThrow(() -> new AppException(AssetErrorCode.ASSET_NOT_FOUND));

        AssetAllocation allocation = assetAllocationRepository.findByAssetIdAndReturnedDateIsNull(id)
                .orElseThrow(() -> new AppException(AssetErrorCode.NO_OPEN_ALLOCATION_FOUND));

        if (request.getConditionOnReturn() == null) {
            throw new AppException(AssetErrorCode.CONDITION_ON_RETURN_REQUIRED);
        }

        if (request.getReturnedDate().isBefore(allocation.getAllocatedDate())) {
            throw new AppException(AssetErrorCode.RETURN_DATE_BEFORE_ALLOCATED_DATE);
        }

        allocation.setReturnedDate(request.getReturnedDate());
        allocation.setConditionOnReturn(request.getConditionOnReturn());
        if (request.getNote() != null && !request.getNote().isBlank()) {
            String note = allocation.getNote() != null ? allocation.getNote() + " | Trả: " + request.getNote() : request.getNote();
            allocation.setNote(note);
        }

        // Nếu hỏng -> chuyển UNDER_REPAIR, ngược lại -> IN_STOCK
        if (request.getConditionOnReturn() == AssetCondition.DAMAGED) {
            asset.setStatus(AssetStatus.UNDER_REPAIR);
        } else {
            asset.setStatus(AssetStatus.IN_STOCK);
        }

        assetRepository.save(asset);
        AssetAllocation savedAlloc = assetAllocationRepository.save(allocation);

        log.info("Thu hồi tài sản: assetCode={}, condition={}, returnedDate={}",
                asset.getAssetCode(), request.getConditionOnReturn(), request.getReturnedDate());

        AssetAllocationResponse response = assetMapper.toAllocationResponse(savedAlloc);
        employeeRepository.findById(allocation.getEmployeeId()).ifPresent(emp -> {
            response.setEmployeeName(emp.getFullName());
            response.setEmployeeCode(emp.getEmployeeCode());
        });

        return response;
    }

    @Override
    @Transactional
    public AssetResponse changeStatus(UUID id, ChangeAssetStatusRequest request) {
        Asset asset = assetRepository.findById(id)
                .orElseThrow(() -> new AppException(AssetErrorCode.ASSET_NOT_FOUND));

        if (asset.getStatus() == AssetStatus.ALLOCATED && request.getStatus() != AssetStatus.ALLOCATED) {
            throw new AppException(AssetErrorCode.INVALID_ASSET_STATUS_TRANSITION,
                    "Tài sản đang cấp phát cho nhân viên. Hãy thực hiện thu hồi tài sản trước khi chuyển trạng thái.");
        }

        asset.setStatus(request.getStatus());
        if (request.getNote() != null && !request.getNote().isBlank()) {
            String note = asset.getNote() != null ? asset.getNote() + " | " + request.getNote() : request.getNote();
            asset.setNote(note);
        }

        Asset updated = assetRepository.save(asset);
        log.info("Đổi trạng thái tài sản: assetCode={}, newStatus={}", asset.getAssetCode(), request.getStatus());
        return assetMapper.toResponse(updated);
    }

    @Override
    @Transactional(readOnly = true)
    public PageData<AssetAllocationResponse> getEmployeeAssets(UUID employeeId, Pageable pageable) {
        Page<AssetAllocation> page = assetAllocationRepository.findByEmployeeId(employeeId, pageable);
        List<AssetAllocation> content = page.getContent();

        if (content.isEmpty()) {
            return PageData.from(page.map(assetMapper::toAllocationResponse));
        }

        Employee employee = employeeRepository.findById(employeeId).orElse(null);
        String empName = employee != null ? employee.getFullName() : null;
        String empCode = employee != null ? employee.getEmployeeCode() : null;

        Page<AssetAllocationResponse> responsePage = page.map(alloc -> {
            AssetAllocationResponse r = assetMapper.toAllocationResponse(alloc);
            r.setEmployeeName(empName);
            r.setEmployeeCode(empCode);
            return r;
        });

        return PageData.from(responsePage);
    }

    @Override
    @Transactional(readOnly = true)
    public long countUnreturnedAssets(UUID employeeId) {
        return assetAllocationRepository.countUnreturnedAssetsByEmployeeId(employeeId);
    }

    private void enrichCurrentEmployee(AssetResponse response, UUID assetId) {
        if (response.getStatus() == AssetStatus.ALLOCATED) {
            assetAllocationRepository.findByAssetIdAndReturnedDateIsNull(assetId).ifPresent(alloc -> {
                response.setCurrentEmployeeId(alloc.getEmployeeId());
                response.setCurrentAllocatedDate(alloc.getAllocatedDate());
                employeeRepository.findById(alloc.getEmployeeId()).ifPresent(emp -> {
                    response.setCurrentEmployeeName(emp.getFullName());
                    response.setCurrentEmployeeCode(emp.getEmployeeCode());
                });
            });
        }
    }
}

package com.cyclosa.asset.service;

import com.cyclosa.asset.dto.request.CreateInventoryCheckRequest;
import com.cyclosa.asset.dto.request.InventoryCheckItemRequest;
import com.cyclosa.asset.dto.response.InventoryCheckItemResponse;
import com.cyclosa.asset.dto.response.InventoryCheckReportResponse;
import com.cyclosa.asset.dto.response.InventoryCheckResponse;
import com.cyclosa.asset.entity.Asset;
import com.cyclosa.asset.entity.AssetInventoryCheck;
import com.cyclosa.asset.entity.AssetInventoryCheckItem;
import com.cyclosa.asset.enums.InventoryResult;
import com.cyclosa.asset.exception.AssetErrorCode;
import com.cyclosa.asset.mapper.AssetMapper;
import com.cyclosa.asset.repository.AssetInventoryCheckItemRepository;
import com.cyclosa.asset.repository.AssetInventoryCheckRepository;
import com.cyclosa.asset.repository.AssetRepository;
import com.cyclosa.common.exception.AppException;
import com.cyclosa.common.response.PageData;
import com.cyclosa.employee.entity.Employee;
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
public class AssetInventoryServiceImpl implements AssetInventoryService {

    private final AssetInventoryCheckRepository inventoryCheckRepository;
    private final AssetInventoryCheckItemRepository inventoryCheckItemRepository;
    private final AssetRepository assetRepository;
    private final EmployeeRepository employeeRepository;
    private final AssetMapper assetMapper;

    @Override
    @Transactional
    public InventoryCheckResponse createInventoryCheck(CreateInventoryCheckRequest request) {
        Employee employee = employeeRepository.findById(request.getPerformedByEmployeeId())
                .orElseThrow(() -> new AppException(AssetErrorCode.EMPLOYEE_NOT_FOUND));

        AssetInventoryCheck check = AssetInventoryCheck.builder()
                .companyId(request.getCompanyId() != null ? request.getCompanyId() : employee.getCompanyId())
                .title(request.getTitle())
                .checkDate(request.getCheckDate())
                .performedByEmployeeId(employee.getId())
                .note(request.getNote())
                .items(new ArrayList<>())
                .build();

        Set<UUID> assetIds = request.getItems().stream()
                .map(InventoryCheckItemRequest::getAssetId)
                .collect(Collectors.toSet());

        Map<UUID, Asset> assetMap = assetRepository.findAllById(assetIds).stream()
                .collect(Collectors.toMap(Asset::getId, a -> a));

        int matched = 0;
        int missing = 0;
        int damaged = 0;

        for (InventoryCheckItemRequest itemReq : request.getItems()) {
            Asset asset = assetMap.get(itemReq.getAssetId());
            if (asset == null) {
                throw new AppException(AssetErrorCode.ASSET_NOT_FOUND, "Không tìm thấy tài sản ID: " + itemReq.getAssetId());
            }

            AssetInventoryCheckItem item = AssetInventoryCheckItem.builder()
                    .inventoryCheck(check)
                    .asset(asset)
                    .actualResult(itemReq.getActualResult())
                    .note(itemReq.getNote())
                    .build();

            check.getItems().add(item);

            if (itemReq.getActualResult() == InventoryResult.MATCHED) {
                matched++;
            } else if (itemReq.getActualResult() == InventoryResult.MISSING) {
                missing++;
            } else if (itemReq.getActualResult() == InventoryResult.DAMAGED) {
                damaged++;
            }
        }

        AssetInventoryCheck savedCheck = inventoryCheckRepository.save(check);
        log.info("Tạo đợt kiểm kê tài sản: id={}, totalItems={}, checkDate={}",
                savedCheck.getId(), savedCheck.getItems().size(), savedCheck.getCheckDate());

        InventoryCheckResponse response = assetMapper.toInventoryCheckResponse(savedCheck);
        response.setPerformedByEmployeeName(employee.getFullName());
        response.setPerformedByEmployeeCode(employee.getEmployeeCode());
        response.setTotalItems(savedCheck.getItems().size());
        response.setMatchedCount(matched);
        response.setMissingCount(missing);
        response.setDamagedCount(damaged);

        return response;
    }

    @Override
    @Transactional(readOnly = true)
    public PageData<InventoryCheckResponse> getInventoryChecks(UUID companyId, Pageable pageable) {
        Page<AssetInventoryCheck> page = companyId != null
                ? inventoryCheckRepository.findByCompanyId(companyId, pageable)
                : inventoryCheckRepository.findAll(pageable);

        List<AssetInventoryCheck> content = page.getContent();
        if (content.isEmpty()) {
            return PageData.from(page.map(assetMapper::toInventoryCheckResponse));
        }

        Set<UUID> employeeIds = content.stream()
                .map(AssetInventoryCheck::getPerformedByEmployeeId)
                .collect(Collectors.toSet());

        Map<UUID, Employee> employeeMap = employeeRepository.findAllById(employeeIds).stream()
                .collect(Collectors.toMap(Employee::getId, e -> e));

        Page<InventoryCheckResponse> responsePage = page.map(check -> {
            InventoryCheckResponse r = assetMapper.toInventoryCheckResponse(check);
            Employee emp = employeeMap.get(check.getPerformedByEmployeeId());
            if (emp != null) {
                r.setPerformedByEmployeeName(emp.getFullName());
                r.setPerformedByEmployeeCode(emp.getEmployeeCode());
            }

            List<AssetInventoryCheckItem> items = check.getItems();
            r.setTotalItems(items.size());
            r.setMatchedCount((int) items.stream().filter(i -> i.getActualResult() == InventoryResult.MATCHED).count());
            r.setMissingCount((int) items.stream().filter(i -> i.getActualResult() == InventoryResult.MISSING).count());
            r.setDamagedCount((int) items.stream().filter(i -> i.getActualResult() == InventoryResult.DAMAGED).count());

            return r;
        });

        return PageData.from(responsePage);
    }

    @Override
    @Transactional(readOnly = true)
    public InventoryCheckReportResponse getInventoryCheckReport(UUID id) {
        AssetInventoryCheck check = inventoryCheckRepository.findById(id)
                .orElseThrow(() -> new AppException(AssetErrorCode.INVENTORY_CHECK_NOT_FOUND));

        List<AssetInventoryCheckItem> items = check.getItems();
        int total = items.size();
        int matched = (int) items.stream().filter(i -> i.getActualResult() == InventoryResult.MATCHED).count();
        int missing = (int) items.stream().filter(i -> i.getActualResult() == InventoryResult.MISSING).count();
        int damaged = (int) items.stream().filter(i -> i.getActualResult() == InventoryResult.DAMAGED).count();
        double matchPercentage = total > 0 ? Math.round(((double) matched / total * 100.0) * 100.0) / 100.0 : 0.0;

        Employee employee = employeeRepository.findById(check.getPerformedByEmployeeId()).orElse(null);

        List<InventoryCheckItemResponse> itemResponses = items.stream().map(item -> {
            InventoryCheckItemResponse ir = assetMapper.toInventoryCheckItemResponse(item);
            if (item.getAsset() != null) {
                ir.setAssetId(item.getAsset().getId());
                ir.setAssetCode(item.getAsset().getAssetCode());
                ir.setAssetName(item.getAsset().getName());
                ir.setAssetCategory(item.getAsset().getCategory());
                ir.setSerialNumber(item.getAsset().getSerialNumber());
            }
            return ir;
        }).toList();

        return InventoryCheckReportResponse.builder()
                .id(check.getId())
                .companyId(check.getCompanyId())
                .title(check.getTitle())
                .checkDate(check.getCheckDate())
                .performedByEmployeeId(check.getPerformedByEmployeeId())
                .performedByEmployeeName(employee != null ? employee.getFullName() : null)
                .performedByEmployeeCode(employee != null ? employee.getEmployeeCode() : null)
                .totalItems(total)
                .matchedCount(matched)
                .missingCount(missing)
                .damagedCount(damaged)
                .matchPercentage(matchPercentage)
                .note(check.getNote())
                .createdAt(check.getCreatedAt())
                .items(itemResponses)
                .build();
    }
}

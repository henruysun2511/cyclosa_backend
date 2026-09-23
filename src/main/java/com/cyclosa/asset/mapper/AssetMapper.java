package com.cyclosa.asset.mapper;

import com.cyclosa.asset.dto.request.CreateAssetRequest;
import com.cyclosa.asset.dto.response.*;
import com.cyclosa.asset.entity.Asset;
import com.cyclosa.asset.entity.AssetAllocation;
import com.cyclosa.asset.entity.AssetInventoryCheck;
import com.cyclosa.asset.entity.AssetInventoryCheckItem;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.MappingConstants;

import java.util.List;

@Mapper(componentModel = MappingConstants.ComponentModel.SPRING)
public interface AssetMapper {

    @Mapping(target = "status", ignore = true)
    @Mapping(target = "allocations", ignore = true)
    Asset toEntity(CreateAssetRequest request);

    @Mapping(target = "currentEmployeeId", ignore = true)
    @Mapping(target = "currentEmployeeName", ignore = true)
    @Mapping(target = "currentEmployeeCode", ignore = true)
    @Mapping(target = "currentAllocatedDate", ignore = true)
    AssetResponse toResponse(Asset asset);

    List<AssetResponse> toResponseList(List<Asset> assets);

    @Mapping(target = "currentEmployeeId", ignore = true)
    @Mapping(target = "currentEmployeeName", ignore = true)
    @Mapping(target = "currentEmployeeCode", ignore = true)
    @Mapping(target = "currentAllocatedDate", ignore = true)
    @Mapping(target = "allocations", ignore = true)
    AssetDetailResponse toDetailResponse(Asset asset);

    @Mapping(target = "assetId", source = "asset.id")
    @Mapping(target = "assetCode", source = "asset.assetCode")
    @Mapping(target = "assetName", source = "asset.name")
    @Mapping(target = "assetCategory", source = "asset.category")
    @Mapping(target = "serialNumber", source = "asset.serialNumber")
    @Mapping(target = "employeeName", ignore = true)
    @Mapping(target = "employeeCode", ignore = true)
    AssetAllocationResponse toAllocationResponse(AssetAllocation allocation);

    List<AssetAllocationResponse> toAllocationResponseList(List<AssetAllocation> allocations);

    @Mapping(target = "performedByEmployeeName", ignore = true)
    @Mapping(target = "performedByEmployeeCode", ignore = true)
    @Mapping(target = "totalItems", ignore = true)
    @Mapping(target = "matchedCount", ignore = true)
    @Mapping(target = "missingCount", ignore = true)
    @Mapping(target = "damagedCount", ignore = true)
    InventoryCheckResponse toInventoryCheckResponse(AssetInventoryCheck check);

    List<InventoryCheckResponse> toInventoryCheckResponseList(List<AssetInventoryCheck> checks);

    @Mapping(target = "assetId", source = "asset.id")
    @Mapping(target = "assetCode", source = "asset.assetCode")
    @Mapping(target = "assetName", source = "asset.name")
    @Mapping(target = "assetCategory", source = "asset.category")
    @Mapping(target = "serialNumber", source = "asset.serialNumber")
    InventoryCheckItemResponse toInventoryCheckItemResponse(AssetInventoryCheckItem item);

    List<InventoryCheckItemResponse> toInventoryCheckItemResponseList(List<AssetInventoryCheckItem> items);
}

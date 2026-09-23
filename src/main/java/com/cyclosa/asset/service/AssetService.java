package com.cyclosa.asset.service;

import com.cyclosa.asset.dto.request.*;
import com.cyclosa.asset.dto.response.AssetAllocationResponse;
import com.cyclosa.asset.dto.response.AssetDetailResponse;
import com.cyclosa.asset.dto.response.AssetResponse;
import com.cyclosa.common.response.PageData;
import org.springframework.data.domain.Pageable;

import java.util.UUID;

public interface AssetService {

    AssetResponse createAsset(CreateAssetRequest request);

    AssetResponse updateAsset(UUID id, UpdateAssetRequest request);

    AssetDetailResponse getAssetById(UUID id);

    PageData<AssetResponse> getAssets(AssetFilter filter, Pageable pageable);

    void deleteAsset(UUID id);

    AssetAllocationResponse allocateAsset(UUID id, AllocateAssetRequest request);

    AssetAllocationResponse returnAsset(UUID id, ReturnAssetRequest request);

    AssetResponse changeStatus(UUID id, ChangeAssetStatusRequest request);

    PageData<AssetAllocationResponse> getEmployeeAssets(UUID employeeId, Pageable pageable);

    long countUnreturnedAssets(UUID employeeId);
}

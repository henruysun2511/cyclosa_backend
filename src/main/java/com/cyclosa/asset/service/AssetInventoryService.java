package com.cyclosa.asset.service;

import com.cyclosa.asset.dto.request.CreateInventoryCheckRequest;
import com.cyclosa.asset.dto.response.InventoryCheckReportResponse;
import com.cyclosa.asset.dto.response.InventoryCheckResponse;
import com.cyclosa.common.response.PageData;
import org.springframework.data.domain.Pageable;

import java.util.UUID;

public interface AssetInventoryService {

    InventoryCheckResponse createInventoryCheck(CreateInventoryCheckRequest request);

    PageData<InventoryCheckResponse> getInventoryChecks(UUID companyId, Pageable pageable);

    InventoryCheckReportResponse getInventoryCheckReport(UUID id);
}

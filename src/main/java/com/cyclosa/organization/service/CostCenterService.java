package com.cyclosa.organization.service;

import com.cyclosa.organization.dto.request.CreateCostCenterRequest;
import com.cyclosa.organization.dto.response.CostCenterResponse;

import java.util.List;
import java.util.UUID;

public interface CostCenterService {

    CostCenterResponse createCostCenter(UUID companyId, CreateCostCenterRequest request);

    List<CostCenterResponse> getCostCenters(UUID companyId);
}

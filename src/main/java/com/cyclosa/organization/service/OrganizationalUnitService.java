package com.cyclosa.organization.service;

import com.cyclosa.organization.dto.request.CreateOrgUnitRequest;
import com.cyclosa.organization.dto.request.MoveOrgUnitRequest;
import com.cyclosa.organization.dto.request.UpdateOrgUnitRequest;
import com.cyclosa.organization.dto.response.OrgUnitImpactPreviewResponse;
import com.cyclosa.organization.dto.response.OrgUnitResponse;
import com.cyclosa.organization.dto.response.OrgUnitTreeResponse;

import java.util.List;
import java.util.UUID;

public interface OrganizationalUnitService {

    OrgUnitResponse createUnit(UUID companyId, CreateOrgUnitRequest request);

    OrgUnitResponse updateUnit(UUID companyId, UUID id, UpdateOrgUnitRequest request);

    void deleteUnit(UUID companyId, UUID id);

    OrgUnitResponse getUnitById(UUID companyId, UUID id);

    List<OrgUnitTreeResponse> getUnitTree(UUID companyId);

    OrgUnitImpactPreviewResponse getImpactPreview(UUID companyId, UUID id, UUID targetParentId);

    OrgUnitResponse moveUnit(UUID companyId, UUID id, MoveOrgUnitRequest request);
}

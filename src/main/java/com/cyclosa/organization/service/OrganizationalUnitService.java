package com.cyclosa.organization.service;

import com.cyclosa.organization.dto.request.CreateOrgUnitRequest;
import com.cyclosa.organization.dto.request.MoveOrgUnitRequest;
import com.cyclosa.organization.dto.request.UpdateOrgUnitRequest;
import com.cyclosa.organization.dto.response.OrgUnitDetailResponse;
import com.cyclosa.organization.dto.response.OrgUnitHistoryResponse;
import com.cyclosa.organization.dto.response.OrgUnitImpactPreviewResponse;
import com.cyclosa.organization.dto.response.OrgUnitResponse;
import com.cyclosa.organization.dto.response.OrgUnitTreeResponse;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Set;
import java.util.UUID;

public interface OrganizationalUnitService {

    OrgUnitResponse createUnit(UUID companyId, CreateOrgUnitRequest request);

    OrgUnitResponse updateUnit(UUID companyId, UUID id, UpdateOrgUnitRequest request);

    void deleteUnit(UUID companyId, UUID id);

    OrgUnitDetailResponse getUnitById(UUID companyId, UUID id);

    /**
     * Lấy cây sơ đồ tổ chức. Nếu atDate != null, tái dựng cây theo cơ cấu lịch sử tại thời điểm đó.
     */
    List<OrgUnitTreeResponse> getUnitTree(UUID companyId, LocalDateTime atDate);

    default List<OrgUnitTreeResponse> getUnitTree(UUID companyId) {
        return getUnitTree(companyId, null);
    }

    OrgUnitImpactPreviewResponse getImpactPreview(UUID companyId, UUID id, UUID targetParentId);

    OrgUnitResponse moveUnit(UUID companyId, UUID id, MoveOrgUnitRequest request);

    /**
     * Xem toàn bộ lịch sử phiên bản điều chuyển, đổi tên theo thời gian của một đơn vị.
     */
    List<OrgUnitHistoryResponse> getUnitHistory(UUID companyId, UUID id);

    /**
     * Lấy tập hợp gồm ID của chính đơn vị và toàn bộ các đơn vị con, cháu... trực thuộc (Kế thừa quyền)
     */
    Set<UUID> getSelfAndDescendantUnitIds(UUID companyId, UUID unitId);
}

package com.cyclosa.workplace.service;

import com.cyclosa.common.dto.summary.BranchSummary;
import com.cyclosa.common.dto.summary.OrgUnitSummary;
import com.cyclosa.common.exception.AppException;
import com.cyclosa.common.response.PageData;
import com.cyclosa.organization.service.GeographyService;
import com.cyclosa.organization.service.OrganizationalUnitService;
import com.cyclosa.workplace.dto.request.*;
import com.cyclosa.workplace.dto.response.*;
import com.cyclosa.workplace.entity.*;
import com.cyclosa.workplace.exception.WorkplaceErrorCode;
import com.cyclosa.workplace.mapper.WorkplaceMapper;
import com.cyclosa.workplace.repository.*;
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
public class WorkplaceHierarchyService {

    private final WorkplaceSiteRepository siteRepository;
    private final WorkplaceFloorRepository floorRepository;
    private final WorkplaceZoneRepository zoneRepository;
    private final WorkplaceRowRepository rowRepository;
    private final WorkplaceMapper workplaceMapper;
    private final GeographyService geographyService;
    private final OrganizationalUnitService organizationalUnitService;

    // ==========================================
    // 1. SITES (TÒA NHÀ / CƠ SỞ)
    // ==========================================

    @Transactional
    public WorkplaceSiteResponse createSite(UUID companyId, CreateSiteRequest request) {
        if (siteRepository.existsByCompanyIdAndCode(companyId, request.getCode().trim().toUpperCase())) {
            throw new AppException(WorkplaceErrorCode.SITE_CODE_ALREADY_EXISTS);
        }

        BranchSummary branchSummary = geographyService.getBranchSummary(request.getBranchId());
        if (branchSummary == null) {
            throw new AppException(WorkplaceErrorCode.BRANCH_NOT_FOUND);
        }

        WorkplaceSite site = WorkplaceSite.builder()
                .companyId(companyId)
                .branchId(request.getBranchId())
                .name(request.getName().trim())
                .code(request.getCode().trim().toUpperCase())
                .address(request.getAddress())
                .description(request.getDescription())
                .build();

        site = siteRepository.save(site);
        log.info("Created workplace site id={}, code={}, companyId={}", site.getId(), site.getCode(), companyId);

        WorkplaceSiteResponse response = workplaceMapper.toSiteResponse(site);
        response.setBranch(branchSummary);
        return response;
    }

    @Transactional
    public WorkplaceSiteResponse updateSite(UUID companyId, UUID siteId, UpdateSiteRequest request) {
        WorkplaceSite site = siteRepository.findByIdAndCompanyId(siteId, companyId)
                .orElseThrow(() -> new AppException(WorkplaceErrorCode.SITE_NOT_FOUND));

        String newCode = request.getCode().trim().toUpperCase();
        if (siteRepository.existsByCompanyIdAndCodeAndIdNot(companyId, newCode, siteId)) {
            throw new AppException(WorkplaceErrorCode.SITE_CODE_ALREADY_EXISTS);
        }

        BranchSummary branchSummary = geographyService.getBranchSummary(request.getBranchId());
        if (branchSummary == null) {
            throw new AppException(WorkplaceErrorCode.BRANCH_NOT_FOUND);
        }

        site.setBranchId(request.getBranchId());
        site.setName(request.getName().trim());
        site.setCode(newCode);
        site.setAddress(request.getAddress());
        site.setDescription(request.getDescription());

        site = siteRepository.save(site);
        log.info("Updated workplace site id={}, code={}", site.getId(), site.getCode());

        WorkplaceSiteResponse response = workplaceMapper.toSiteResponse(site);
        response.setBranch(branchSummary);
        return response;
    }

    @Transactional(readOnly = true)
    public WorkplaceSiteResponse getSiteById(UUID companyId, UUID siteId) {
        WorkplaceSite site = siteRepository.findByIdAndCompanyId(siteId, companyId)
                .orElseThrow(() -> new AppException(WorkplaceErrorCode.SITE_NOT_FOUND));

        WorkplaceSiteResponse response = workplaceMapper.toSiteResponse(site);
        response.setBranch(geographyService.getBranchSummary(site.getBranchId()));
        return response;
    }

    @Transactional(readOnly = true)
    public PageData<WorkplaceSiteResponse> getSites(UUID companyId, String search, Pageable pageable) {
        Page<WorkplaceSite> page = siteRepository.searchSites(companyId, search, pageable);
        if (page.isEmpty()) {
            return PageData.empty(pageable);
        }

        Set<UUID> branchIds = page.getContent().stream()
                .map(WorkplaceSite::getBranchId)
                .filter(Objects::nonNull)
                .collect(Collectors.toSet());
        Map<UUID, BranchSummary> branchMap = geographyService.getBranchSummaries(branchIds);

        List<WorkplaceSiteResponse> list = page.getContent().stream().map(site -> {
            WorkplaceSiteResponse res = workplaceMapper.toSiteResponse(site);
            res.setBranch(branchMap.get(site.getBranchId()));
            return res;
        }).toList();

        return PageData.of(page, list);
    }

    @Transactional
    public void deleteSite(UUID companyId, UUID siteId) {
        WorkplaceSite site = siteRepository.findByIdAndCompanyId(siteId, companyId)
                .orElseThrow(() -> new AppException(WorkplaceErrorCode.SITE_NOT_FOUND));
        siteRepository.delete(site);
        log.info("Deleted workplace site id={}", siteId);
    }

    // ==========================================
    // 2. FLOORS (TẦNG)
    // ==========================================

    @Transactional
    public WorkplaceFloorResponse createFloor(UUID companyId, CreateFloorRequest request) {
        WorkplaceSite site = siteRepository.findByIdAndCompanyId(request.getSiteId(), companyId)
                .orElseThrow(() -> new AppException(WorkplaceErrorCode.SITE_NOT_FOUND));

        String floorNumber = request.getFloorNumber().trim();
        if (floorRepository.existsByCompanyIdAndSiteIdAndFloorNumber(companyId, site.getId(), floorNumber)) {
            throw new AppException(WorkplaceErrorCode.FLOOR_ALREADY_EXISTS);
        }

        WorkplaceFloor floor = WorkplaceFloor.builder()
                .companyId(companyId)
                .site(site)
                .floorNumber(floorNumber)
                .name(request.getName() != null ? request.getName().trim() : null)
                .description(request.getDescription())
                .build();

        floor = floorRepository.save(floor);
        log.info("Created floor id={}, floorNumber={} in site id={}", floor.getId(), floor.getFloorNumber(), site.getId());
        return workplaceMapper.toFloorResponse(floor);
    }

    @Transactional
    public WorkplaceFloorResponse updateFloor(UUID companyId, UUID floorId, UpdateFloorRequest request) {
        WorkplaceFloor floor = floorRepository.findByIdAndCompanyIdWithSite(floorId, companyId)
                .orElseThrow(() -> new AppException(WorkplaceErrorCode.FLOOR_NOT_FOUND));

        String floorNumber = request.getFloorNumber().trim();
        if (floorRepository.existsByCompanyIdAndSiteIdAndFloorNumberAndIdNot(companyId, floor.getSite().getId(), floorNumber, floorId)) {
            throw new AppException(WorkplaceErrorCode.FLOOR_ALREADY_EXISTS);
        }

        floor.setFloorNumber(floorNumber);
        floor.setName(request.getName() != null ? request.getName().trim() : null);
        floor.setDescription(request.getDescription());

        floor = floorRepository.save(floor);
        log.info("Updated floor id={}", floor.getId());
        return workplaceMapper.toFloorResponse(floor);
    }

    @Transactional(readOnly = true)
    public WorkplaceFloorResponse getFloorById(UUID companyId, UUID floorId) {
        WorkplaceFloor floor = floorRepository.findByIdAndCompanyIdWithSite(floorId, companyId)
                .orElseThrow(() -> new AppException(WorkplaceErrorCode.FLOOR_NOT_FOUND));
        return workplaceMapper.toFloorResponse(floor);
    }

    @Transactional(readOnly = true)
    public List<WorkplaceFloorResponse> getFloorsBySite(UUID companyId, UUID siteId) {
        return workplaceMapper.toFloorResponseList(floorRepository.findBySiteIdAndCompanyId(siteId, companyId));
    }

    @Transactional
    public void deleteFloor(UUID companyId, UUID floorId) {
        WorkplaceFloor floor = floorRepository.findByIdAndCompanyId(floorId, companyId)
                .orElseThrow(() -> new AppException(WorkplaceErrorCode.FLOOR_NOT_FOUND));
        floorRepository.delete(floor);
        log.info("Deleted floor id={}", floorId);
    }

    // ==========================================
    // 3. ZONES (KHU VỰC)
    // ==========================================

    @Transactional
    public WorkplaceZoneResponse createZone(UUID companyId, CreateZoneRequest request) {
        WorkplaceFloor floor = floorRepository.findByIdAndCompanyIdWithSite(request.getFloorId(), companyId)
                .orElseThrow(() -> new AppException(WorkplaceErrorCode.FLOOR_NOT_FOUND));

        WorkplaceZone zone = WorkplaceZone.builder()
                .companyId(companyId)
                .floor(floor)
                .name(request.getName().trim())
                .organizationalUnitId(request.getOrganizationalUnitId())
                .shapeType(request.getShapeType())
                .boundaryCoordinates(request.getBoundaryCoordinates())
                .colorCode(request.getColorCode())
                .description(request.getDescription())
                .build();

        zone = zoneRepository.save(zone);
        log.info("Created zone id={}, name={} in floor id={}", zone.getId(), zone.getName(), floor.getId());

        WorkplaceZoneResponse response = workplaceMapper.toZoneResponse(zone);
        enrichZoneOrgUnits(List.of(response));
        return response;
    }

    @Transactional
    public WorkplaceZoneResponse updateZone(UUID companyId, UUID zoneId, UpdateZoneRequest request) {
        WorkplaceZone zone = zoneRepository.findByIdAndCompanyIdWithHierarchy(zoneId, companyId)
                .orElseThrow(() -> new AppException(WorkplaceErrorCode.ZONE_NOT_FOUND));

        zone.setName(request.getName().trim());
        zone.setOrganizationalUnitId(request.getOrganizationalUnitId());
        zone.setShapeType(request.getShapeType());
        zone.setBoundaryCoordinates(request.getBoundaryCoordinates());
        zone.setColorCode(request.getColorCode());
        zone.setDescription(request.getDescription());

        zone = zoneRepository.save(zone);
        log.info("Updated zone id={}", zone.getId());

        WorkplaceZoneResponse response = workplaceMapper.toZoneResponse(zone);
        enrichZoneOrgUnits(List.of(response));
        return response;
    }

    @Transactional(readOnly = true)
    public WorkplaceZoneResponse getZoneById(UUID companyId, UUID zoneId) {
        WorkplaceZone zone = zoneRepository.findByIdAndCompanyIdWithHierarchy(zoneId, companyId)
                .orElseThrow(() -> new AppException(WorkplaceErrorCode.ZONE_NOT_FOUND));

        WorkplaceZoneResponse response = workplaceMapper.toZoneResponse(zone);
        enrichZoneOrgUnits(List.of(response));
        return response;
    }

    @Transactional(readOnly = true)
    public List<WorkplaceZoneResponse> getZonesByFloor(UUID companyId, UUID floorId) {
        List<WorkplaceZone> zones = zoneRepository.findByFloorIdAndCompanyId(floorId, companyId);
        List<WorkplaceZoneResponse> responses = workplaceMapper.toZoneResponseList(zones);
        enrichZoneOrgUnits(responses);
        return responses;
    }

    @Transactional
    public void deleteZone(UUID companyId, UUID zoneId) {
        WorkplaceZone zone = zoneRepository.findByIdAndCompanyId(zoneId, companyId)
                .orElseThrow(() -> new AppException(WorkplaceErrorCode.ZONE_NOT_FOUND));
        zoneRepository.delete(zone);
        log.info("Deleted zone id={}", zoneId);
    }

    // ==========================================
    // 4. ROWS (DÃY BÀN / CỤM)
    // ==========================================

    @Transactional
    public WorkplaceRowResponse createRow(UUID companyId, CreateRowRequest request) {
        WorkplaceZone zone = zoneRepository.findByIdAndCompanyId(request.getZoneId(), companyId)
                .orElseThrow(() -> new AppException(WorkplaceErrorCode.ZONE_NOT_FOUND));

        WorkplaceRow row = WorkplaceRow.builder()
                .companyId(companyId)
                .zone(zone)
                .name(request.getName().trim())
                .clusterType(request.getClusterType())
                .positionX(request.getPositionX())
                .positionY(request.getPositionY())
                .rotation(request.getRotation())
                .build();

        row = rowRepository.save(row);
        log.info("Created row id={}, name={} in zone id={}", row.getId(), row.getName(), zone.getId());
        return workplaceMapper.toRowResponse(row);
    }

    @Transactional
    public WorkplaceRowResponse updateRow(UUID companyId, UUID rowId, UpdateRowRequest request) {
        WorkplaceRow row = rowRepository.findByIdAndCompanyIdWithHierarchy(rowId, companyId)
                .orElseThrow(() -> new AppException(WorkplaceErrorCode.ROW_NOT_FOUND));

        row.setName(request.getName().trim());
        row.setClusterType(request.getClusterType());
        row.setPositionX(request.getPositionX());
        row.setPositionY(request.getPositionY());
        row.setRotation(request.getRotation());

        row = rowRepository.save(row);
        log.info("Updated row id={}", row.getId());
        return workplaceMapper.toRowResponse(row);
    }

    @Transactional(readOnly = true)
    public WorkplaceRowResponse getRowById(UUID companyId, UUID rowId) {
        WorkplaceRow row = rowRepository.findByIdAndCompanyIdWithHierarchy(rowId, companyId)
                .orElseThrow(() -> new AppException(WorkplaceErrorCode.ROW_NOT_FOUND));
        return workplaceMapper.toRowResponse(row);
    }

    @Transactional(readOnly = true)
    public List<WorkplaceRowResponse> getRowsByZone(UUID companyId, UUID zoneId) {
        return workplaceMapper.toRowResponseList(rowRepository.findByZoneIdAndCompanyId(zoneId, companyId));
    }

    @Transactional
    public void deleteRow(UUID companyId, UUID rowId) {
        WorkplaceRow row = rowRepository.findByIdAndCompanyId(rowId, companyId)
                .orElseThrow(() -> new AppException(WorkplaceErrorCode.ROW_NOT_FOUND));
        rowRepository.delete(row);
        log.info("Deleted row id={}", rowId);
    }

    // --- Helper Methods ---
    private void enrichZoneOrgUnits(List<WorkplaceZoneResponse> responses) {
        if (responses == null || responses.isEmpty()) return;
        Set<UUID> orgUnitIds = responses.stream()
                .map(WorkplaceZoneResponse::getOrganizationalUnitId)
                .filter(Objects::nonNull)
                .collect(Collectors.toSet());
        if (orgUnitIds.isEmpty()) return;

        Map<UUID, OrgUnitSummary> unitMap = organizationalUnitService.getUnitSummaries(orgUnitIds);
        responses.forEach(r -> {
            if (r.getOrganizationalUnitId() != null) {
                r.setOrganizationalUnit(unitMap.get(r.getOrganizationalUnitId()));
            }
        });
    }
}

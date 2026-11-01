package com.cyclosa.authorization.service;

import com.cyclosa.common.exception.AppException;
import com.cyclosa.authorization.dto.response.PermissionResponse;
import com.cyclosa.authorization.entity.Permission;
import com.cyclosa.authorization.exception.RoleErrorCode;
import com.cyclosa.authorization.mapper.PermissionMapper;
import com.cyclosa.authorization.repository.PermissionRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.stream.Collectors;

@Slf4j
@Service
@RequiredArgsConstructor
public class PermissionService {

    private final PermissionRepository permissionRepository;
    private final PermissionMapper permissionMapper;

    /**
     * Lấy toàn bộ danh mục quyền chức năng hệ thống, sắp xếp theo Resource và Code
     */
    @Transactional(readOnly = true)
    public List<PermissionResponse> getAllPermissions() {
        return permissionMapper.toResponseList(permissionRepository.findAllByOrderByResourceAscCodeAsc());
    }

    /**
     * Lấy danh mục quyền nhóm theo từng phân hệ tài nguyên (Resource)
     */
    @Transactional(readOnly = true)
    public Map<String, List<PermissionResponse>> getPermissionsGroupedByResource() {
        List<PermissionResponse> list = getAllPermissions();
        return list.stream().collect(Collectors.groupingBy(PermissionResponse::getResource));
    }

    /**
     * Lấy chi tiết quyền chức năng theo ID
     */
    @Transactional(readOnly = true)
    public Permission getEntityById(UUID id) {
        return permissionRepository.findById(id)
                .orElseThrow(() -> new AppException(RoleErrorCode.PERMISSION_NOT_FOUND));
    }
}


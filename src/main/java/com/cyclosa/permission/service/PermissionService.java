package com.cyclosa.permission.service;

import com.cyclosa.common.exception.AppException;
import com.cyclosa.permission.dto.response.PermissionResponse;
import com.cyclosa.permission.entity.Permission;
import com.cyclosa.permission.mapper.PermissionMapper;
import com.cyclosa.permission.repository.PermissionRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class PermissionService {

    private final PermissionRepository permissionRepository;
    private final PermissionMapper permissionMapper;

    @Transactional(readOnly = true)
    public List<PermissionResponse> getAllPermissions() {
        List<Permission> list = permissionRepository.findAllByOrderByModuleAscCodeAsc();
        return permissionMapper.toResponseList(list);
    }

    @Transactional(readOnly = true)
    public Map<String, List<PermissionResponse>> getPermissionsGroupedByModule() {
        List<Permission> list = permissionRepository.findAllByOrderByModuleAscCodeAsc();
        return list.stream()
                .map(permissionMapper::toResponse)
                .collect(Collectors.groupingBy(PermissionResponse::getModule));
    }

    @Transactional(readOnly = true)
    public Permission findById(UUID id) {
        return permissionRepository.findById(id)
                .orElseThrow(() -> AppException.notFound("Permission not found with id: " + id));
    }

    @Transactional(readOnly = true)
    public List<Permission> findAllById(Iterable<UUID> ids) {
        return permissionRepository.findAllById(ids);
    }

    @Transactional(readOnly = true)
    public Optional<Permission> findByCode(String code) {
        return permissionRepository.findByCode(code);
    }

    @Transactional(readOnly = true)
    public boolean existsByCode(String code) {
        return permissionRepository.existsByCode(code);
    }

    @Transactional
    public List<Permission> saveAll(Iterable<Permission> permissions) {
        return permissionRepository.saveAll(permissions);
    }
}

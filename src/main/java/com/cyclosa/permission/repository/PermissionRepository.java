package com.cyclosa.permission.repository;

import com.cyclosa.permission.entity.Permission;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
public interface PermissionRepository extends JpaRepository<Permission, UUID> {

    Optional<Permission> findByCode(String code);

    List<Permission> findByModuleOrderByCodeAsc(String module);

    List<Permission> findAllByOrderByModuleAscCodeAsc();

    boolean existsByCode(String code);
}

package com.cyclosa.authorization.repository;

import com.cyclosa.authorization.entity.Permission;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.UUID;

@Repository
public interface PermissionRepository extends JpaRepository<Permission, UUID> {

    List<Permission> findAllByOrderByResourceAscCodeAsc();

    Optional<Permission> findByCode(String code);

    boolean existsByCode(String code);

    List<Permission> findByResource(String resource);

    List<Permission> findByIdIn(Set<UUID> ids);
}


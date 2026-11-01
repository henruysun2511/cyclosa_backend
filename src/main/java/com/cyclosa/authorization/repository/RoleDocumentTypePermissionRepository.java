package com.cyclosa.authorization.repository;

import com.cyclosa.authorization.entity.RoleDocumentTypePermission;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.UUID;

@Repository
public interface RoleDocumentTypePermissionRepository extends JpaRepository<RoleDocumentTypePermission, UUID> {

    List<RoleDocumentTypePermission> findAllByRoleId(UUID roleId);

    @Modifying
    @Query("DELETE FROM RoleDocumentTypePermission rdtp WHERE rdtp.role.id = :roleId")
    void deleteByRoleId(@Param("roleId") UUID roleId);
}


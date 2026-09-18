package com.cyclosa.role.repository;

import com.cyclosa.role.entity.UserRoleMapping;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.UUID;

@Repository
public interface UserRoleMappingRepository extends JpaRepository<UserRoleMapping, UUID> {

    @Query("SELECT urm FROM UserRoleMapping urm JOIN FETCH urm.role WHERE urm.user.id = :userId")
    List<UserRoleMapping> findByUserIdWithRole(@Param("userId") UUID userId);

    void deleteByUserId(UUID userId);

    long countByRoleId(UUID roleId);

    @Query("SELECT urm.user.id FROM UserRoleMapping urm WHERE urm.role.id = :roleId AND (urm.companyId IS NULL OR urm.companyId = :companyId)")
    List<UUID> findUserIdsByRoleIdAndCompanyId(@Param("roleId") UUID roleId, @Param("companyId") UUID companyId);
}

package com.cyclosa.authorization.repository;

import com.cyclosa.authorization.entity.Role;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
public interface RoleRepository extends JpaRepository<Role, UUID>, JpaSpecificationExecutor<Role> {

    Optional<Role> findByIdAndDeletedFalse(UUID id);

    boolean existsByCompanyIdAndCodeAndDeletedFalse(UUID companyId, String code);

    boolean existsByCodeAndCompanyIdIsNullAndDeletedFalse(String code);

    Optional<Role> findByCodeAndCompanyIdIsNullAndDeletedFalse(String code);

    Optional<Role> findByCompanyIdAndCodeAndDeletedFalse(UUID companyId, String code);

    List<Role> findByCompanyIdOrCompanyIdIsNullAndDeletedFalse(UUID companyId);

    @Query("SELECT r FROM Role r WHERE r.deleted = false AND (r.companyId = :companyId OR r.companyId IS NULL)")
    List<Role> findAvailableRolesForCompany(@Param("companyId") UUID companyId);
}


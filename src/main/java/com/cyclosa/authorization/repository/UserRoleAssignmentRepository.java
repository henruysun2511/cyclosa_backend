package com.cyclosa.authorization.repository;

import com.cyclosa.authorization.entity.UserRoleAssignment;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
public interface UserRoleAssignmentRepository extends JpaRepository<UserRoleAssignment, UUID> {

    @Query("SELECT ura FROM UserRoleAssignment ura JOIN FETCH ura.role WHERE ura.userId = :userId")
    List<UserRoleAssignment> findAllWithRoleByUserId(@Param("userId") UUID userId);

    @Query("SELECT ura FROM UserRoleAssignment ura JOIN FETCH ura.role WHERE ura.userId = :userId AND ura.companyId = :companyId")
    List<UserRoleAssignment> findAllWithRoleByUserIdAndCompanyId(@Param("userId") UUID userId, @Param("companyId") UUID companyId);

    boolean existsByUserIdAndRoleIdAndCompanyId(UUID userId, UUID roleId, UUID companyId);

    long countByRoleId(UUID roleId);

    Page<UserRoleAssignment> findByRoleId(UUID roleId, Pageable pageable);

    @Query("SELECT DISTINCT ura.userId FROM UserRoleAssignment ura WHERE ura.role.id = :roleId")
    List<UUID> findUserIdsByRoleId(@Param("roleId") UUID roleId);

    Optional<UserRoleAssignment> findById(UUID id);
}


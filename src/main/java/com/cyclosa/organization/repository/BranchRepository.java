package com.cyclosa.organization.repository;

import com.cyclosa.organization.entity.Branch;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
public interface BranchRepository extends JpaRepository<Branch, UUID> {

    boolean existsByCodeAndCompanyId(String code, UUID companyId);

    boolean existsByCodeAndCompanyIdAndIdNot(String code, UUID companyId, UUID id);

    Optional<Branch> findByIdAndCompanyId(UUID id, UUID companyId);

    @Query("SELECT b FROM Branch b " +
           "LEFT JOIN FETCH b.region " +
           "WHERE b.companyId = :companyId " +
           "ORDER BY b.name ASC")
    List<Branch> findByCompanyIdWithRegion(@Param("companyId") UUID companyId);
}

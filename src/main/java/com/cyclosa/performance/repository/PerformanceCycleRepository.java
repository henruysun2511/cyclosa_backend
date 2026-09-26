package com.cyclosa.performance.repository;

import com.cyclosa.performance.entity.PerformanceCycle;
import com.cyclosa.performance.enums.PerformanceCycleStatus;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;
import java.util.Optional;
import java.util.UUID;

@Repository
public interface PerformanceCycleRepository extends JpaRepository<PerformanceCycle, UUID> {

    @Query("SELECT c FROM PerformanceCycle c WHERE " +
           "(:companyId IS NULL OR c.companyId = :companyId) AND " +
           "(:status IS NULL OR c.status = :status) AND " +
           "(:search IS NULL OR LOWER(c.name) LIKE LOWER(CONCAT('%', :search, '%')))")
    Page<PerformanceCycle> searchCycles(
            @Param("companyId") UUID companyId,
            @Param("status") PerformanceCycleStatus status,
            @Param("search") String search,
            Pageable pageable
    );

    boolean existsByNameAndCompanyId(String name, UUID companyId);

    Optional<PerformanceCycle> findFirstByStatus(PerformanceCycleStatus status);
}

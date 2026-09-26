package com.cyclosa.discipline.repository;

import com.cyclosa.discipline.entity.Reward;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.UUID;

@Repository
public interface RewardRepository extends JpaRepository<Reward, UUID> {

    @Query("SELECT r FROM Reward r WHERE " +
           "(:companyId IS NULL OR r.companyId = :companyId) AND " +
           "(:employeeId IS NULL OR r.employeeId = :employeeId) AND " +
           "(:search IS NULL OR LOWER(r.title) LIKE LOWER(CONCAT('%', :search, '%')))")
    Page<Reward> searchRewards(
            @Param("companyId") UUID companyId,
            @Param("employeeId") UUID employeeId,
            @Param("search") String search,
            Pageable pageable
    );

    List<Reward> findByCompanyIdAndPushedToPayrollFalse(UUID companyId);
}

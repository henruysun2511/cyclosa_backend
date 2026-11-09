package com.cyclosa.contract.repository;

import com.cyclosa.contract.entity.Contract;
import com.cyclosa.contract.entity.enums.ContractStatus;
import com.cyclosa.contract.entity.enums.ContractType;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
public interface ContractRepository extends JpaRepository<Contract, UUID> {

    Optional<Contract> findByContractNo(String contractNo);

    boolean existsByContractNo(String contractNo);

    List<Contract> findByEmployeeIdOrderByStartDateDesc(UUID employeeId);

    @Query("SELECT c FROM Contract c WHERE (:companyId IS NULL OR c.employerCompanyId = :companyId) " +
           "AND (:employeeId IS NULL OR c.employeeId = :employeeId) " +
           "AND (:contractType IS NULL OR c.contractType = :contractType) " +
           "AND (:status IS NULL OR c.status = :status OR (:status = com.cyclosa.contract.entity.enums.ContractStatus.IN_SIGNING AND c.status = com.cyclosa.contract.entity.enums.ContractStatus.PENDING_SIGNATURE) OR (:status = com.cyclosa.contract.entity.enums.ContractStatus.PENDING_SIGNATURE AND c.status = com.cyclosa.contract.entity.enums.ContractStatus.IN_SIGNING)) " +
           "AND (:expiringBefore IS NULL OR (c.endDate IS NOT NULL AND c.endDate <= :expiringBefore AND c.status = com.cyclosa.contract.entity.enums.ContractStatus.ACTIVE)) " +
           "AND (:keyword IS NULL OR :keyword = '' " +
           "     OR LOWER(c.contractNo) LIKE LOWER(CONCAT('%', :keyword, '%')) " +
           "     OR EXISTS (" +
           "         SELECT 1 FROM com.cyclosa.employee.entity.Employee e WHERE e.id = c.employeeId " +
           "         AND (LOWER(e.employeeCode) LIKE LOWER(CONCAT('%', :keyword, '%')) " +
           "              OR LOWER(CONCAT(e.lastName, ' ', e.firstName)) LIKE LOWER(CONCAT('%', :keyword, '%')))" +
           "     )) " +
           "ORDER BY c.createdAt DESC")
    Page<Contract> findByCompanyAndFilter(
            @Param("companyId") UUID companyId,
            @Param("employeeId") UUID employeeId,
            @Param("contractType") ContractType contractType,
            @Param("status") ContractStatus status,
            @Param("keyword") String keyword,
            @Param("expiringBefore") LocalDate expiringBefore,
            Pageable pageable
    );

    @Query("SELECT c FROM Contract c WHERE c.employerCompanyId = :companyId " +
           "AND c.status = 'ACTIVE' " +
           "AND c.endDate IS NOT NULL " +
           "AND c.endDate BETWEEN :fromDate AND :toDate " +
           "ORDER BY c.endDate ASC")
    List<Contract> findExpiringContracts(
            @Param("companyId") UUID companyId,
            @Param("fromDate") LocalDate fromDate,
            @Param("toDate") LocalDate toDate
    );

    @Query("SELECT COUNT(c) FROM Contract c WHERE c.employeeId = :employeeId " +
           "AND c.contractType = 'FIXED_TERM' AND c.status IN ('ACTIVE', 'EXPIRED')")
    long countFixedTermContractsByEmployee(@Param("employeeId") UUID employeeId);
}


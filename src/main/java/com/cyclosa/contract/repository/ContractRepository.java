package com.cyclosa.contract.repository;

import com.cyclosa.contract.entity.Contract;
import com.cyclosa.contract.enums.ContractStatus;
import com.cyclosa.contract.enums.ContractType;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
public interface ContractRepository extends JpaRepository<Contract, UUID>, JpaSpecificationExecutor<Contract> {

    boolean existsByContractNumberAndCompanyId(String contractNumber, UUID companyId);

    Optional<Contract> findByContractNumberAndCompanyId(String contractNumber, UUID companyId);

    /**
     * Tìm hợp đồng đang ACTIVE của một nhân viên.
     */
    Optional<Contract> findByEmployeeIdAndContractStatus(UUID employeeId, ContractStatus status);

    /**
     * Kiểm tra nhân viên đã có hợp đồng ACTIVE hay chưa.
     */
    boolean existsByEmployeeIdAndContractStatus(UUID employeeId, ContractStatus status);

    /**
     * Đếm số lượng hợp đồng xác định thời hạn đã ký của nhân viên với công ty (Điều 20.2 BLLĐ 2019).
     */
    @Query("SELECT COUNT(c) FROM Contract c WHERE c.employeeId = :employeeId AND c.companyId = :companyId AND c.contractType = :contractType AND c.contractStatus != 'DRAFT'")
    long countDefiniteContracts(@Param("employeeId") UUID employeeId, @Param("companyId") UUID companyId, @Param("contractType") ContractType contractType);

    /**
     * Kiểm tra nhân viên đã từng ký HĐ thử việc hoặc có thời gian thử việc chưa (Điều 24.2).
     */
    boolean existsByEmployeeIdAndContractType(UUID employeeId, ContractType contractType);

    /**
     * Tìm các hợp đồng sắp hết hạn trong khoảng ngày [today, targetDate].
     */
    @Query("SELECT c FROM Contract c WHERE c.contractStatus = 'ACTIVE' AND c.endDate IS NOT NULL AND c.endDate BETWEEN :startDate AND :endDate")
    List<Contract> findExpiringContracts(@Param("startDate") LocalDate startDate, @Param("endDate") LocalDate endDate);

    /**
     * Tìm các hợp đồng đã quá hạn 30 ngày nhưng chưa chuyển trạng thái (Điều 20.2.b).
     */
    @Query("SELECT c FROM Contract c WHERE c.contractStatus = 'ACTIVE' AND c.contractType = 'DEFINITE_TERM' AND c.endDate IS NOT NULL AND c.endDate <= :thresholdDate")
    List<Contract> findContractsPendingIndefiniteConversion(@Param("thresholdDate") LocalDate thresholdDate);

    Page<Contract> findByCompanyId(UUID companyId, Pageable pageable);

    List<Contract> findByEmployeeIdOrderByStartDateDesc(UUID employeeId);

    @Query("""
        SELECT c FROM Contract c
        WHERE (:companyId IS NULL OR c.companyId = :companyId)
          AND (:employeeId IS NULL OR c.employeeId = :employeeId)
          AND (:status IS NULL OR c.contractStatus = :status)
          AND (:type IS NULL OR c.contractType = :type)
          AND (:keyword IS NULL OR LOWER(c.contractNumber) LIKE LOWER(CONCAT('%', :keyword, '%')))
    """)
    Page<Contract> search(
            @Param("keyword") String keyword,
            @Param("companyId") UUID companyId,
            @Param("employeeId") UUID employeeId,
            @Param("status") ContractStatus status,
            @Param("type") ContractType type,
            Pageable pageable
    );
}

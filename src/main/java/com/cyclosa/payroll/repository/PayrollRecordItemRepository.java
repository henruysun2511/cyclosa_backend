package com.cyclosa.payroll.repository;

import com.cyclosa.payroll.entity.PayrollRecordItem;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.UUID;

@Repository
public interface PayrollRecordItemRepository extends JpaRepository<PayrollRecordItem, UUID> {

    List<PayrollRecordItem> findAllByPayrollRecordId(UUID payrollRecordId);

    void deleteAllByPayrollRecordId(UUID payrollRecordId);
}

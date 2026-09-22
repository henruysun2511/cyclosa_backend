package com.cyclosa.leave.repository;

import com.cyclosa.leave.entity.SickLeaveEntitlementTier;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.math.BigDecimal;
import java.util.List;

@Repository
public interface SickLeaveEntitlementTierRepository extends JpaRepository<SickLeaveEntitlementTier, java.util.UUID> {

    List<SickLeaveEntitlementTier> findAllByOrderByMinBhxhYearsAsc();
}

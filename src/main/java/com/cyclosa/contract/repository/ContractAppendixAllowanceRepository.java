package com.cyclosa.contract.repository;

import com.cyclosa.contract.entity.ContractAppendixAllowance;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.UUID;

@Repository
public interface ContractAppendixAllowanceRepository extends JpaRepository<ContractAppendixAllowance, UUID> {
    List<ContractAppendixAllowance> findByAppendixId(UUID appendixId);
    void deleteByAppendixId(UUID appendixId);
}


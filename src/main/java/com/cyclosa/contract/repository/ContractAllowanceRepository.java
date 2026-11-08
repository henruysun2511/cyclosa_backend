package com.cyclosa.contract.repository;

import com.cyclosa.contract.entity.ContractAllowance;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.UUID;

@Repository
public interface ContractAllowanceRepository extends JpaRepository<ContractAllowance, UUID> {
    List<ContractAllowance> findByContractId(UUID contractId);
    void deleteByContractId(UUID contractId);
}


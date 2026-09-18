package com.cyclosa.contract.repository;

import com.cyclosa.contract.entity.ContractTermination;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;
import java.util.UUID;

@Repository
public interface ContractTerminationRepository extends JpaRepository<ContractTermination, UUID> {

    Optional<ContractTermination> findByContractId(UUID contractId);

    boolean existsByContractId(UUID contractId);
}

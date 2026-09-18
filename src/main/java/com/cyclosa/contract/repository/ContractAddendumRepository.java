package com.cyclosa.contract.repository;

import com.cyclosa.contract.entity.ContractAddendum;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
public interface ContractAddendumRepository extends JpaRepository<ContractAddendum, UUID> {

    boolean existsByContractIdAndAddendumNumber(UUID contractId, String addendumNumber);

    List<ContractAddendum> findByContractIdOrderByEffectiveDateAsc(UUID contractId);

    Optional<ContractAddendum> findFirstByContractIdOrderByEffectiveDateDesc(UUID contractId);
}

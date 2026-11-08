package com.cyclosa.contract.repository;

import com.cyclosa.contract.entity.ContractSigner;
import com.cyclosa.contract.entity.enums.SignerStatus;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
public interface ContractSignerRepository extends JpaRepository<ContractSigner, UUID> {

    List<ContractSigner> findByContractVersionIdOrderBySigningOrderAsc(UUID contractVersionId);

    Optional<ContractSigner> findByContractVersionIdAndSignerEmployeeId(UUID contractVersionId, UUID signerEmployeeId);

    Optional<ContractSigner> findByTransactionId(String transactionId);

    List<ContractSigner> findBySignerEmployeeIdAndStatus(UUID signerEmployeeId, SignerStatus status);
}


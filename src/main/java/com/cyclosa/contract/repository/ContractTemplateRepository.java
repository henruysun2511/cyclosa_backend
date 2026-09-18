package com.cyclosa.contract.repository;

import com.cyclosa.contract.entity.ContractTemplate;
import com.cyclosa.contract.enums.ContractType;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
public interface ContractTemplateRepository extends JpaRepository<ContractTemplate, UUID> {

    Optional<ContractTemplate> findByTemplateCodeAndCompanyId(String templateCode, UUID companyId);

    boolean existsByTemplateCodeAndCompanyId(String templateCode, UUID companyId);

    List<ContractTemplate> findByCompanyIdAndActiveTrue(UUID companyId);

    Optional<ContractTemplate> findFirstByCompanyIdAndContractTypeAndActiveTrueOrderByVersionDesc(UUID companyId, ContractType contractType);
}

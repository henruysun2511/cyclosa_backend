package com.cyclosa.contract.repository;

import com.cyclosa.contract.entity.ContractTemplate;
import com.cyclosa.contract.entity.enums.TemplateKind;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
public interface ContractTemplateRepository extends JpaRepository<ContractTemplate, UUID> {

    @Query("SELECT t FROM ContractTemplate t WHERE (:companyId IS NULL OR t.companyId = :companyId) " +
           "AND (:templateKind IS NULL OR t.templateKind = :templateKind) " +
           "AND (:active IS NULL OR t.active = :active) " +
           "ORDER BY t.createdAt DESC")
    Page<ContractTemplate> findByCompanyAndFilter(
            @Param("companyId") UUID companyId,
            @Param("templateKind") TemplateKind templateKind,
            @Param("active") Boolean active,
            Pageable pageable
    );

    List<ContractTemplate> findByCompanyIdAndActiveTrue(UUID companyId);

    Optional<ContractTemplate> findByIdAndCompanyId(UUID id, UUID companyId);
}


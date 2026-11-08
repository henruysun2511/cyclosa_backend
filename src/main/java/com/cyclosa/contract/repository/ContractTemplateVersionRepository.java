package com.cyclosa.contract.repository;

import com.cyclosa.contract.entity.ContractTemplateVersion;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
public interface ContractTemplateVersionRepository extends JpaRepository<ContractTemplateVersion, UUID> {

    List<ContractTemplateVersion> findByTemplateIdOrderByVersionNoDesc(UUID templateId);

    @Query("SELECT MAX(v.versionNo) FROM ContractTemplateVersion v WHERE v.template.id = :templateId")
    Optional<Integer> findMaxVersionNoByTemplateId(@Param("templateId") UUID templateId);

    Optional<ContractTemplateVersion> findByTemplateIdAndVersionNo(UUID templateId, int versionNo);
}


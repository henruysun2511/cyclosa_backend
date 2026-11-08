package com.cyclosa.contract.repository;

import com.cyclosa.contract.entity.ContractVersion;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
public interface ContractVersionRepository extends JpaRepository<ContractVersion, UUID> {

    List<ContractVersion> findByContractIdOrderByVersionNoDesc(UUID contractId);

    List<ContractVersion> findByContractIdAndAppendixIdIsNullOrderByVersionNoDesc(UUID contractId);

    List<ContractVersion> findByAppendixIdOrderByVersionNoDesc(UUID appendixId);

    @Query("SELECT MAX(v.versionNo) FROM ContractVersion v WHERE v.contract.id = :contractId AND v.appendixId IS NULL")
    Optional<Integer> findMaxVersionNoByContractId(@Param("contractId") UUID contractId);

    @Query("SELECT MAX(v.versionNo) FROM ContractVersion v WHERE v.appendixId = :appendixId")
    Optional<Integer> findMaxVersionNoByAppendixId(@Param("appendixId") UUID appendixId);
}


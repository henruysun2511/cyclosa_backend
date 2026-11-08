package com.cyclosa.contract.repository;

import com.cyclosa.contract.entity.ContractAppendix;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
public interface ContractAppendixRepository extends JpaRepository<ContractAppendix, UUID> {

    List<ContractAppendix> findByContractIdOrderByAppendixNoAsc(UUID contractId);

    @Query("SELECT MAX(a.appendixNo) FROM ContractAppendix a WHERE a.contract.id = :contractId")
    Optional<Integer> findMaxAppendixNoByContractId(@Param("contractId") UUID contractId);
}


package com.cyclosa.recruitment.repository;

import com.cyclosa.recruitment.entity.Offer;
import com.cyclosa.recruitment.enums.OfferStatus;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
public interface OfferRepository extends JpaRepository<Offer, UUID>, JpaSpecificationExecutor<Offer> {
    Optional<Offer> findByIdAndCompanyId(UUID id, UUID companyId);
    Page<Offer> findByCompanyId(UUID companyId, Pageable pageable);
    List<Offer> findByApplicationIdOrderByCreatedAtDesc(UUID applicationId);
    Optional<Offer> findFirstByApplicationIdAndStatus(UUID applicationId, OfferStatus status);
}

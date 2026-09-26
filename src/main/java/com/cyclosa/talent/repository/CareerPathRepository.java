package com.cyclosa.talent.repository;

import com.cyclosa.talent.entity.CareerPath;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
public interface CareerPathRepository extends JpaRepository<CareerPath, UUID>, JpaSpecificationExecutor<CareerPath> {

    boolean existsByCompanyIdAndFromPositionIdAndToPositionId(UUID companyId, UUID fromPositionId, UUID toPositionId);

    List<CareerPath> findByCompanyIdAndFromPositionId(UUID companyId, UUID fromPositionId);

    Optional<CareerPath> findByIdAndCompanyId(UUID id, UUID companyId);
}

package com.cyclosa.talent.repository;

import com.cyclosa.talent.entity.CareerSimulationSavedPath;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
public interface CareerSimulationSavedPathRepository extends JpaRepository<CareerSimulationSavedPath, UUID>, JpaSpecificationExecutor<CareerSimulationSavedPath> {

    List<CareerSimulationSavedPath> findByCompanyIdAndEmployeeIdOrderBySavedAtDesc(UUID companyId, UUID employeeId);

    Optional<CareerSimulationSavedPath> findByIdAndCompanyId(UUID id, UUID companyId);
}

package com.cyclosa.discipline.repository;

import com.cyclosa.discipline.entity.Discipline;
import com.cyclosa.discipline.enums.DisciplineStatus;
import com.cyclosa.discipline.enums.DisciplineType;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;
import java.util.List;
import java.util.UUID;

@Repository
public interface DisciplineRepository extends JpaRepository<Discipline, UUID> {

    @Query("SELECT d FROM Discipline d WHERE " +
           "(:companyId IS NULL OR d.companyId = :companyId) AND " +
           "(:employeeId IS NULL OR d.employeeId = :employeeId) AND " +
           "(:status IS NULL OR d.status = :status) AND " +
           "(:disciplineType IS NULL OR d.disciplineType = :disciplineType)")
    Page<Discipline> searchDisciplines(
            @Param("companyId") UUID companyId,
            @Param("employeeId") UUID employeeId,
            @Param("status") DisciplineStatus status,
            @Param("disciplineType") DisciplineType disciplineType,
            Pageable pageable
    );

    List<Discipline> findByStatusAndExpiryDateLessThanEqual(DisciplineStatus status, LocalDate date);

    List<Discipline> findByEmployeeIdAndStatus(UUID employeeId, DisciplineStatus status);
}

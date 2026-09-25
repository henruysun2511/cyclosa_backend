package com.cyclosa.organization.repository;

import com.cyclosa.organization.entity.Position;
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
public interface PositionRepository extends JpaRepository<Position, UUID> {

    boolean existsByCodeAndCompanyId(String code, UUID companyId);

    boolean existsByCodeAndCompanyIdAndIdNot(String code, UUID companyId, UUID id);

    Optional<Position> findByIdAndCompanyId(UUID id, UUID companyId);

    List<Position> findByCompanyId(UUID companyId);

    @Query("SELECT p FROM Position p " +
           "LEFT JOIN FETCH p.jobLevel " +
           "WHERE p.companyId = :companyId " +
           "AND (:keyword IS NULL OR LOWER(p.name) LIKE LOWER(CONCAT('%', :keyword, '%')) " +
           "OR LOWER(p.code) LIKE LOWER(CONCAT('%', :keyword, '%'))) " +
           "AND (:jobLevelId IS NULL OR p.jobLevel.id = :jobLevelId)")
    Page<Position> search(@Param("keyword") String keyword,
                          @Param("companyId") UUID companyId,
                          @Param("jobLevelId") UUID jobLevelId,
                          Pageable pageable);
}

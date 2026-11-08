package com.cyclosa.organization.repository;

import com.cyclosa.organization.entity.Company;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.Optional;
import java.util.UUID;

@Repository
public interface CompanyRepository extends JpaRepository<Company, UUID> {

    Optional<Company> findByCode(String code);

    boolean existsByCode(String code);

    boolean existsByCodeAndIdNot(String code, UUID id);

    @Query("SELECT c FROM Company c WHERE " +
           "(cast(:keyword as string) IS NULL OR LOWER(c.name) LIKE LOWER(CONCAT('%', cast(:keyword as string), '%')) " +
           "OR LOWER(c.code) LIKE LOWER(CONCAT('%', cast(:keyword as string), '%'))) " +
           "AND (cast(:status as string) IS NULL OR c.status = cast(:status as string))")
    Page<Company> search(@Param("keyword") String keyword, @Param("status") String status, Pageable pageable);
}


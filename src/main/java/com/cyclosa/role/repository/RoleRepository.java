package com.cyclosa.role.repository;

import com.cyclosa.role.entity.Role;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
public interface RoleRepository extends JpaRepository<Role, UUID> {

    Optional<Role> findByCode(String code);

    Optional<Role> findByCodeAndCompanyId(String code, UUID companyId);

    Optional<Role> findByCodeAndCompanyIdIsNull(String code);

    List<Role> findByCompanyIdIsNull();

    List<Role> findByCompanyId(UUID companyId);

    List<Role> findByCompanyIdOrCompanyIdIsNull(UUID companyId);

    boolean existsByCodeAndCompanyId(String code, UUID companyId);

    boolean existsByCodeAndCompanyIdIsNull(String code);
}

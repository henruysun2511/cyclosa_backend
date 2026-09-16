package com.cyclosa.role.repository;

import com.cyclosa.role.entity.Role;
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
public interface RoleRepository extends JpaRepository<Role, UUID> {

    Optional<Role> findByCode(String code);

    Optional<Role> findByCodeAndCompanyId(String code, UUID companyId);

    Optional<Role> findByCodeAndCompanyIdIsNull(String code);

    List<Role> findByCompanyIdIsNull();

    List<Role> findByCompanyId(UUID companyId);

    List<Role> findByCompanyIdOrCompanyIdIsNull(UUID companyId);

    boolean existsByCodeAndCompanyId(String code, UUID companyId);

    boolean existsByCodeAndCompanyIdIsNull(String code);

    /**
     * Tìm kiếm vai trò có phân trang.
     * - keyword: khớp với name hoặc code (case-insensitive), null = bỏ qua
     * - companyId: null = chỉ lấy system roles; non-null = lấy cả system + company roles
     * - isSystemRole: null = bỏ qua filter
     */
    @Query("""
            SELECT r FROM Role r
            WHERE (:keyword IS NULL
                   OR LOWER(r.name) LIKE LOWER(CONCAT('%', :keyword, '%'))
                   OR LOWER(r.code) LIKE LOWER(CONCAT('%', :keyword, '%')))
              AND (r.companyId IS NULL OR r.companyId = :companyId)
              AND (:isSystemRole IS NULL OR r.isSystemRole = :isSystemRole)
            """)
    Page<Role> search(@Param("keyword") String keyword,
                      @Param("companyId") UUID companyId,
                      @Param("isSystemRole") Boolean isSystemRole,
                      Pageable pageable);
}

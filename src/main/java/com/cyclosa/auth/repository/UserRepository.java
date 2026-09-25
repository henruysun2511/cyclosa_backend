package com.cyclosa.auth.repository;

import com.cyclosa.auth.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.Optional;
import java.util.UUID;

@Repository
public interface UserRepository extends JpaRepository<User, UUID>,
                                        JpaSpecificationExecutor<User> {

    Optional<User> findByEmail(String email);

    Optional<User> findByUsername(String username);

    boolean existsByEmail(String email);

    boolean existsByEmailAndIdNot(String email, UUID excludeId);

    Optional<User> findByEmployeeId(UUID employeeId);

    @Query("SELECT u FROM User u " +
           "LEFT JOIN FETCH u.userRoles ur " +
           "LEFT JOIN FETCH ur.role r " +
           "WHERE LOWER(u.email) = LOWER(:email)")
    Optional<User> findByEmailWithRoles(@Param("email") String email);

    @Query("SELECT u FROM User u " +
           "LEFT JOIN FETCH u.userRoles ur " +
           "LEFT JOIN FETCH ur.role r " +
           "WHERE u.id = :id")
    Optional<User> findByIdWithRoles(@Param("id") UUID id);

    @Query("SELECT u FROM User u " +
           "WHERE (:keyword IS NULL " +
           "       OR LOWER(u.email) LIKE LOWER(CONCAT('%', :keyword, '%')) " +
           "       OR LOWER(u.fullName) LIKE LOWER(CONCAT('%', :keyword, '%')) " +
           "       OR LOWER(u.username) LIKE LOWER(CONCAT('%', :keyword, '%'))) " +
           "  AND (:status IS NULL OR u.status = :status)")
    org.springframework.data.domain.Page<User> searchUsers(
            @Param("keyword") String keyword,
            @Param("status") com.cyclosa.common.enums.UserStatus status,
            org.springframework.data.domain.Pageable pageable);
}

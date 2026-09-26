package com.cyclosa.workplace.repository;

import com.cyclosa.workplace.entity.WorkplaceSeat;
import com.cyclosa.workplace.enums.SeatStatus;
import com.cyclosa.workplace.enums.SeatType;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.Collection;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface WorkplaceSeatRepository extends JpaRepository<WorkplaceSeat, UUID> {

    @Query("SELECT s FROM WorkplaceSeat s " +
           "JOIN FETCH s.row r " +
           "JOIN FETCH r.zone z " +
           "JOIN FETCH z.floor f " +
           "JOIN FETCH f.site " +
           "WHERE s.id = :id AND s.companyId = :companyId")
    Optional<WorkplaceSeat> findByIdAndCompanyIdWithHierarchy(@Param("id") UUID id, @Param("companyId") UUID companyId);

    default Optional<WorkplaceSeat> findByIdWithHierarchy(UUID id, UUID companyId) {
        return findByIdAndCompanyIdWithHierarchy(id, companyId);
    }

    Optional<WorkplaceSeat> findByIdAndCompanyId(UUID id, UUID companyId);

    Optional<WorkplaceSeat> findByCompanyIdAndSeatCode(UUID companyId, String seatCode);

    boolean existsByCompanyIdAndSeatCode(UUID companyId, String seatCode);

    boolean existsByCompanyIdAndSeatCodeAndIdNot(UUID companyId, String seatCode, UUID id);

    Optional<WorkplaceSeat> findByCompanyIdAndAssignedEmployeeId(UUID companyId, UUID employeeId);

    @Query("SELECT s FROM WorkplaceSeat s " +
           "JOIN FETCH s.row r " +
           "JOIN FETCH r.zone z " +
           "JOIN FETCH z.floor f " +
           "JOIN FETCH f.site " +
           "WHERE s.companyId = :companyId AND s.assignedEmployeeId IN :employeeIds")
    List<WorkplaceSeat> findByCompanyIdAndAssignedEmployeeIdInWithHierarchy(@Param("companyId") UUID companyId, @Param("employeeIds") Collection<UUID> employeeIds);

    @Query("SELECT s FROM WorkplaceSeat s " +
           "JOIN FETCH s.row r " +
           "JOIN FETCH r.zone z " +
           "JOIN FETCH z.floor f " +
           "JOIN FETCH f.site " +
           "WHERE s.companyId = :companyId AND s.assignedEmployeeId IS NOT NULL")
    List<WorkplaceSeat> findAllAssignedSeatsWithHierarchy(@Param("companyId") UUID companyId);

    @Query("SELECT s FROM WorkplaceSeat s " +
           "JOIN FETCH s.row r " +
           "JOIN FETCH r.zone z " +
           "JOIN FETCH z.floor f " +
           "WHERE f.id = :floorId AND s.companyId = :companyId")
    List<WorkplaceSeat> findByFloorIdAndCompanyId(@Param("floorId") UUID floorId, @Param("companyId") UUID companyId);

    @Query("SELECT s FROM WorkplaceSeat s " +
           "JOIN FETCH s.row r " +
           "JOIN FETCH r.zone z " +
           "JOIN FETCH z.floor f " +
           "JOIN FETCH f.site " +
           "WHERE f.id = :floorId")
    List<WorkplaceSeat> findByFloorIdWithHierarchy(@Param("floorId") UUID floorId);

    @Query("SELECT s FROM WorkplaceSeat s " +
           "JOIN FETCH s.row r " +
           "JOIN FETCH r.zone z " +
           "JOIN FETCH z.floor f " +
           "JOIN FETCH f.site site " +
           "WHERE s.companyId = :companyId " +
           "AND (:floorId IS NULL OR f.id = :floorId) " +
           "AND (:zoneId IS NULL OR z.id = :zoneId) " +
           "AND (:rowId IS NULL OR r.id = :rowId) " +
           "AND (:seatType IS NULL OR s.seatType = :seatType) " +
           "AND (:status IS NULL OR s.status = :status) " +
           "AND (:search IS NULL OR LOWER(s.seatCode) LIKE LOWER(CONCAT('%', :search, '%')))")
    Page<WorkplaceSeat> filterSeats(@Param("companyId") UUID companyId,
                                    @Param("floorId") UUID floorId,
                                    @Param("zoneId") UUID zoneId,
                                    @Param("rowId") UUID rowId,
                                    @Param("seatType") SeatType seatType,
                                    @Param("status") SeatStatus status,
                                    @Param("search") String search,
                                    Pageable pageable);

    @Query("SELECT s FROM WorkplaceSeat s " +
           "JOIN FETCH s.row r " +
           "JOIN FETCH r.zone z " +
           "JOIN FETCH z.floor f " +
           "JOIN FETCH f.site " +
           "WHERE s.companyId = :companyId " +
           "AND s.status = 'AVAILABLE' " +
           "AND (:departmentId IS NULL OR z.organizationalUnitId = :departmentId) " +
           "AND (:floorId IS NULL OR f.id = :floorId)")
    List<WorkplaceSeat> findRecommendedAvailableSeats(@Param("companyId") UUID companyId,
                                                     @Param("departmentId") UUID departmentId,
                                                     @Param("floorId") UUID floorId);

    long countByCompanyId(UUID companyId);

    long countByCompanyIdAndStatus(UUID companyId, SeatStatus status);

    long countByCompanyIdAndRowZoneFloorId(UUID companyId, UUID floorId);

    long countByCompanyIdAndRowZoneFloorIdAndStatus(UUID companyId, UUID floorId, SeatStatus status);
}

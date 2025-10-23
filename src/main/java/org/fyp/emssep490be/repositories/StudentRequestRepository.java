package org.fyp.emssep490be.repositories;

import jakarta.persistence.LockModeType;
import org.fyp.emssep490be.entities.StudentRequest;
import org.fyp.emssep490be.entities.enums.RequestStatus;
import org.fyp.emssep490be.entities.enums.StudentRequestType;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

/**
 * Repository for StudentRequest entity
 * Handles database operations for student requests (absence, makeup, transfer)
 */
@Repository
public interface StudentRequestRepository extends JpaRepository<StudentRequest, Long>, JpaSpecificationExecutor<StudentRequest> {

    /**
     * Find requests by student ID and status
     */
    List<StudentRequest> findByStudentIdAndStatus(Long studentId, RequestStatus status);

    /**
     * Find requests by student ID and request type
     */
    List<StudentRequest> findByStudentIdAndRequestType(Long studentId, StudentRequestType requestType);

    /**
     * Find requests by student ID with optional filters
     */
    @Query("""
        SELECT sr FROM StudentRequest sr
        WHERE sr.student.id = :studentId
          AND (:requestType IS NULL OR sr.requestType = :requestType)
          AND (:status IS NULL OR sr.status = :status)
        ORDER BY sr.submittedAt DESC
    """)
    List<StudentRequest> findByStudentIdWithFilters(
            @Param("studentId") Long studentId,
            @Param("requestType") StudentRequestType requestType,
            @Param("status") RequestStatus status
    );

    /**
     * Check if a pending absence request already exists for a specific session
     * Used to prevent duplicate absence requests
     */
    @Query("""
        SELECT COUNT(sr) > 0
        FROM StudentRequest sr
        WHERE sr.student.id = :studentId
          AND sr.targetSession.id = :sessionId
          AND sr.requestType = 'ABSENCE'
          AND sr.status = 'PENDING'
    """)
    boolean existsPendingAbsenceRequestForSession(
            @Param("studentId") Long studentId,
            @Param("sessionId") Long sessionId
    );

    /**
     * Count approved absence requests for a student in a specific class
     * Used to check absence quota
     */
    @Query("""
        SELECT COUNT(sr)
        FROM StudentRequest sr
        WHERE sr.student.id = :studentId
          AND sr.targetSession.clazz.id = :classId
          AND sr.requestType = 'ABSENCE'
          AND sr.status = 'APPROVED'
    """)
    int countApprovedAbsenceRequestsInClass(
            @Param("studentId") Long studentId,
            @Param("classId") Long classId
    );

    /**
     * Find all requests with filters for Academic Staff dashboard
     * Supports filtering by status, type, branch, and student
     */
    @Query("""
        SELECT sr FROM StudentRequest sr
        LEFT JOIN FETCH sr.student s
        LEFT JOIN FETCH sr.targetSession ts
        LEFT JOIN FETCH ts.clazz tc
        LEFT JOIN FETCH tc.branch tb
        WHERE (:status IS NULL OR sr.status = :status)
          AND (:type IS NULL OR sr.requestType = :type)
          AND (:branchId IS NULL OR tc.branch.id = :branchId)
          AND (:studentId IS NULL OR sr.student.id = :studentId)
        ORDER BY sr.submittedAt DESC
    """)
    Page<StudentRequest> findAllWithFilters(
            @Param("status") RequestStatus status,
            @Param("type") StudentRequestType type,
            @Param("branchId") Long branchId,
            @Param("studentId") Long studentId,
            Pageable pageable
    );

    /**
     * Find pending requests for a specific branch
     * Used by Academic Staff to see requests needing their attention
     */
    @Query("""
        SELECT sr FROM StudentRequest sr
        WHERE sr.status = 'PENDING'
          AND sr.targetSession.clazz.branch.id = :branchId
        ORDER BY sr.submittedAt ASC
    """)
    List<StudentRequest> findPendingRequestsByBranch(@Param("branchId") Long branchId);

    /**
     * Count pending requests by type for a branch
     * Used for dashboard statistics
     */
    @Query("""
        SELECT sr.requestType, COUNT(sr)
        FROM StudentRequest sr
        WHERE sr.status = 'PENDING'
          AND sr.targetSession.clazz.branch.id = :branchId
        GROUP BY sr.requestType
    """)
    List<Object[]> countPendingRequestsByTypeAndBranch(@Param("branchId") Long branchId);

    /**
     * Check if there's already a pending makeup request for the same target and makeup sessions
     * Prevents duplicate requests
     */
    @Query("""
        SELECT CASE WHEN COUNT(sr) > 0 THEN true ELSE false END
        FROM StudentRequest sr
        WHERE sr.student.id = :studentId
          AND sr.targetSession.id = :targetSessionId
          AND sr.makeupSession.id = :makeupSessionId
          AND sr.requestType = 'MAKEUP'
          AND sr.status = 'PENDING'
    """)
    boolean existsPendingMakeupRequest(
            @Param("studentId") Long studentId,
            @Param("targetSessionId") Long targetSessionId,
            @Param("makeupSessionId") Long makeupSessionId
    );

    /**
     * Find student request by ID with pessimistic write lock.
     * This prevents concurrent modifications to the same request during critical operations
     * such as approval, rejection, or cancellation.
     * 
     * USE CASES:
     * - Request approval: Lock request to prevent concurrent approvals by multiple staff
     * - Request rejection: Lock to prevent concurrent modifications
     * - Request cancellation: Lock to prevent concurrent operations
     * 
     * RACE CONDITION PREVENTION:
     * Scenario: Staff A and Staff B both click "Approve" at the same time
     * - Staff A acquires lock → processes approval → releases lock
     * - Staff B waits for lock → sees status = APPROVED → throws error
     * 
     * @param id the request ID
     * @return optional request with exclusive lock
     */
    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @Query("SELECT sr FROM StudentRequest sr WHERE sr.id = :id")
    Optional<StudentRequest> findByIdWithLock(@Param("id") Long id);
}

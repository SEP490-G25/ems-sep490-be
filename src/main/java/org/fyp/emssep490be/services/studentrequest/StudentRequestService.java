package org.fyp.emssep490be.services.studentrequest;

import org.fyp.emssep490be.dtos.studentrequest.*;
import org.fyp.emssep490be.entities.enums.Modality;
import org.fyp.emssep490be.entities.enums.RequestStatus;
import org.fyp.emssep490be.entities.enums.StudentRequestType;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.time.LocalDate;
import java.util.List;

/**
 * Service interface for Student Request operations
 * Handles absence, makeup, and transfer requests
 */
public interface StudentRequestService {

    // ==================== ABSENCE REQUEST OPERATIONS ====================

    /**
     * Create an absence request for a student
     * Student submits this when they know they will be absent for a session
     *
     * @param studentId The ID of the student making the request
     * @param request The absence request details
     * @return The created StudentRequestDTO
     * @throws org.fyp.emssep490be.exceptions.CustomException if validation fails
     */
    StudentRequestDTO createAbsenceRequest(Long studentId, CreateAbsenceRequestDTO request);

    /**
     * Approve an absence request
     * Academic Staff approves the request and updates student_session to 'excused'
     *
     * @param requestId The ID of the request to approve
     * @param staffId The ID of the staff member approving the request
     * @param dto The approval details (optional notes)
     * @return The updated StudentRequestDTO
     * @throws org.fyp.emssep490be.exceptions.CustomException if request not found or not pending
     */
    StudentRequestDTO approveAbsenceRequest(Long requestId, Long staffId, ApproveRequestDTO dto);

    /**
     * Reject an absence request
     * Academic Staff rejects the request with a reason
     *
     * @param requestId The ID of the request to reject
     * @param staffId The ID of the staff member rejecting the request
     * @param dto The rejection details (required reason)
     * @return The updated StudentRequestDTO
     * @throws org.fyp.emssep490be.exceptions.CustomException if request not found or not pending
     */
    StudentRequestDTO rejectAbsenceRequest(Long requestId, Long staffId, RejectRequestDTO dto);

    // ==================== QUERY OPERATIONS ====================

    /**
     * Get a student request by ID
     *
     * @param requestId The ID of the request
     * @return The StudentRequestDTO
     * @throws org.fyp.emssep490be.exceptions.CustomException if request not found
     */
    StudentRequestDTO getRequestById(Long requestId);

    /**
     * Get all requests for a specific student with optional filters
     *
     * @param studentId The ID of the student
     * @param type Optional filter by request type (ABSENCE, MAKEUP, TRANSFER)
     * @param status Optional filter by status (PENDING, APPROVED, REJECTED)
     * @return List of StudentRequestDTO
     */
    List<StudentRequestDTO> getStudentRequests(Long studentId, StudentRequestType type, RequestStatus status);

    /**
     * Get all requests with filters (for Academic Staff)
     * Supports pagination and filtering by status, type, branch, and student
     *
     * @param status Optional filter by status
     * @param type Optional filter by request type
     * @param branchId Optional filter by branch
     * @param studentId Optional filter by student
     * @param pageable Pagination parameters
     * @return Page of StudentRequestDTO
     */
    Page<StudentRequestDTO> getAllRequests(
            RequestStatus status,
            StudentRequestType type,
            Long branchId,
            Long studentId,
            Pageable pageable
    );

    /**
     * Cancel a pending request
     * Student can cancel their own pending request
     *
     * @param requestId The ID of the request to cancel
     * @param studentId The ID of the student (for validation)
     * @return The updated StudentRequestDTO
     * @throws org.fyp.emssep490be.exceptions.CustomException if request not found, not owned by student, or not pending
     */
    StudentRequestDTO cancelRequest(Long requestId, Long studentId);

    // ==================== MAKEUP REQUEST OPERATIONS ====================

    /**
     * Find available makeup sessions for a missed session
     * Returns sessions with the same course content that have available capacity
     *
     * @param studentId The ID of the student
     * @param missedSessionId The ID of the missed session
     * @param dateFrom Optional filter for earliest date
     * @param dateTo Optional filter for latest date
     * @param branchId Optional filter for specific branch
     * @param modality Optional filter for modality (OFFLINE, ONLINE, HYBRID)
     * @return MakeupSessionSearchResultDTO containing available sessions
     * @throws org.fyp.emssep490be.exceptions.CustomException if student or session not found
     */
    MakeupSessionSearchResultDTO findAvailableMakeupSessions(
            Long studentId,
            Long missedSessionId,
            LocalDate dateFrom,
            LocalDate dateTo,
            Long branchId,
            Modality modality
    );

    /**
     * Create a makeup request
     * Student requests to attend a different session to make up for a missed one
     *
     * @param studentId The ID of the student
     * @param request The makeup request details
     * @return The created StudentRequestDTO
     * @throws org.fyp.emssep490be.exceptions.CustomException if validation fails
     */
    StudentRequestDTO createMakeupRequest(Long studentId, CreateMakeupRequestDTO request);

    /**
     * Approve a makeup request
     * Marks original session as excused and creates new student_session for makeup with is_makeup=true
     *
     * @param requestId The ID of the request to approve
     * @param staffId The ID of the staff member approving the request
     * @param dto The approval details (optional notes)
     * @return The updated StudentRequestDTO
     * @throws org.fyp.emssep490be.exceptions.CustomException if validation fails or capacity full
     */
    StudentRequestDTO approveMakeupRequest(Long requestId, Long staffId, ApproveRequestDTO dto);

    // ==================== TRANSFER REQUEST OPERATIONS (Future) ====================
    // TODO: Implement in Phase 4
    // TransferValidationResultDTO validateTransfer(Long studentId, Long currentClassId, Long targetClassId, LocalDate effectiveDate);
    // StudentRequestDTO createTransferRequest(Long studentId, CreateTransferRequestDTO request);
    // StudentRequestDTO approveTransferRequest(Long requestId, Long staffId, ApproveRequestDTO dto);
}

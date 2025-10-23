package org.fyp.emssep490be.services.studentrequest;

import org.fyp.emssep490be.dtos.studentrequest.ApproveRequestDTO;
import org.fyp.emssep490be.dtos.studentrequest.CreateAbsenceRequestDTO;
import org.fyp.emssep490be.dtos.studentrequest.RejectRequestDTO;
import org.fyp.emssep490be.dtos.studentrequest.StudentRequestDTO;
import org.fyp.emssep490be.entities.enums.RequestStatus;
import org.fyp.emssep490be.entities.enums.StudentRequestType;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

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

    // ==================== MAKEUP REQUEST OPERATIONS (Future) ====================
    // TODO: Implement in Phase 3
    // StudentRequestDTO createMakeupRequest(Long studentId, CreateMakeupRequestDTO request);
    // List<AvailableMakeupSessionDTO> findAvailableMakeupSessions(Long studentId, Long sessionId);
    // StudentRequestDTO approveMakeupRequest(Long requestId, Long staffId, ApproveRequestDTO dto);

    // ==================== TRANSFER REQUEST OPERATIONS (Future) ====================
    // TODO: Implement in Phase 4
    // TransferValidationResultDTO validateTransfer(Long studentId, Long currentClassId, Long targetClassId, LocalDate effectiveDate);
    // StudentRequestDTO createTransferRequest(Long studentId, CreateTransferRequestDTO request);
    // StudentRequestDTO approveTransferRequest(Long requestId, Long staffId, ApproveRequestDTO dto);
}

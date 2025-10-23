package org.fyp.emssep490be.controllers.studentrequest;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.fyp.emssep490be.configs.CustomUserDetails;
import org.fyp.emssep490be.dtos.ResponseObject;
import org.fyp.emssep490be.dtos.studentrequest.*;
import org.fyp.emssep490be.entities.enums.Modality;
import org.fyp.emssep490be.entities.enums.RequestStatus;
import org.fyp.emssep490be.entities.enums.StudentRequestType;

import java.time.LocalDate;

import org.fyp.emssep490be.exceptions.CustomException;
import org.fyp.emssep490be.exceptions.ErrorCode;
import org.fyp.emssep490be.services.studentrequest.StudentRequestService;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * Controller for Student Request operations
 * Handles absence, makeup, and transfer requests
 * Base path: /api/v1
 */
@RestController
@RequestMapping("/api/v1")
@RequiredArgsConstructor
@Slf4j
public class StudentRequestController {

    private final StudentRequestService studentRequestService;

    /**
     * Extract authenticated user ID from SecurityContext
     * 
     * @return User ID from JWT token
     * @throws CustomException if authentication is missing or invalid
     */
    private Long getAuthenticatedUserId() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        
        if (authentication == null || !authentication.isAuthenticated()) {
            log.error("No authentication found in SecurityContext");
            throw new CustomException(ErrorCode.UNAUTHORIZED);
        }
        
        Object principal = authentication.getPrincipal();
        if (!(principal instanceof CustomUserDetails)) {
            log.error("Invalid principal type: {}", principal.getClass().getName());
            throw new CustomException(ErrorCode.UNAUTHORIZED);
        }
        
        CustomUserDetails userDetails = (CustomUserDetails) principal;
        Long userId = userDetails.getUserId();
        
        log.debug("Extracted user ID from SecurityContext: {}", userId);
        return userId;
    }

    // ==================== STUDENT OPERATIONS ====================

    /**
     * Create absence request
     * POST /api/v1/students/{studentId}/requests/absence
     *
     * @param studentId The ID of the student making the request
     * @param request The absence request details
     * @return Created StudentRequestDTO
     */
    @PostMapping("/students/{studentId}/requests/absence")
    @PreAuthorize("hasRole('STUDENT')")
    public ResponseEntity<ResponseObject<StudentRequestDTO>> createAbsenceRequest(
            @PathVariable Long studentId,
            @Valid @RequestBody CreateAbsenceRequestDTO request) {

        log.info("Received absence request from student {}", studentId);
        StudentRequestDTO result = studentRequestService.createAbsenceRequest(studentId, request);

        return ResponseEntity.status(HttpStatus.CREATED).body(
                new ResponseObject<>(
                        HttpStatus.CREATED.value(),
                        "Absence request created successfully",
                        result
                )
        );
    }

    /**
     * Get student's requests with optional filters
     * GET /api/v1/students/{studentId}/requests
     *
     * @param studentId The ID of the student
     * @param type Optional filter by request type (ABSENCE, MAKEUP, TRANSFER)
     * @param status Optional filter by status (PENDING, APPROVED, REJECTED, CANCELLED)
     * @return List of StudentRequestDTO
     */
    @GetMapping("/students/{studentId}/requests")
    @PreAuthorize("hasAnyRole('STUDENT', 'ACADEMIC_STAFF', 'CENTER_HEAD', 'MANAGER')")
    public ResponseEntity<ResponseObject<List<StudentRequestDTO>>> getStudentRequests(
            @PathVariable Long studentId,
            @RequestParam(required = false) StudentRequestType type,
            @RequestParam(required = false) RequestStatus status) {

        log.info("Getting requests for student {} (type={}, status={})", studentId, type, status);
        List<StudentRequestDTO> result = studentRequestService.getStudentRequests(studentId, type, status);

        return ResponseEntity.ok(
                new ResponseObject<>(
                        HttpStatus.OK.value(),
                        "Requests retrieved successfully",
                        result
                )
        );
    }

    /**
     * Get request detail
     * GET /api/v1/students/{studentId}/requests/{requestId}
     *
     * @param studentId The ID of the student
     * @param requestId The ID of the request
     * @return StudentRequestDTO
     */
    @GetMapping("/students/{studentId}/requests/{requestId}")
    @PreAuthorize("hasAnyRole('STUDENT', 'ACADEMIC_STAFF', 'CENTER_HEAD', 'MANAGER')")
    public ResponseEntity<ResponseObject<StudentRequestDTO>> getRequestById(
            @PathVariable Long studentId,
            @PathVariable Long requestId) {

        log.info("Getting request {} for student {}", requestId, studentId);
        StudentRequestDTO result = studentRequestService.getRequestById(requestId);

        return ResponseEntity.ok(
                new ResponseObject<>(
                        HttpStatus.OK.value(),
                        "Request retrieved successfully",
                        result
                )
        );
    }

    /**
     * Cancel a pending request
     * POST /api/v1/students/{studentId}/requests/{requestId}/cancel
     *
     * @param studentId The ID of the student
     * @param requestId The ID of the request to cancel
     * @return Updated StudentRequestDTO
     */
    @PostMapping("/students/{studentId}/requests/{requestId}/cancel")
    @PreAuthorize("hasRole('STUDENT')")
    public ResponseEntity<ResponseObject<StudentRequestDTO>> cancelRequest(
            @PathVariable Long studentId,
            @PathVariable Long requestId) {

        log.info("Student {} cancelling request {}", studentId, requestId);
        StudentRequestDTO result = studentRequestService.cancelRequest(requestId, studentId);

        return ResponseEntity.ok(
                new ResponseObject<>(
                        HttpStatus.OK.value(),
                        "Request cancelled successfully",
                        result
                )
        );
    }

    // ==================== ACADEMIC STAFF OPERATIONS ====================

    /**
     * Get all requests with filters (Academic Staff dashboard)
     * GET /api/v1/student-requests
     *
     * @param status Optional filter by status (default: PENDING)
     * @param type Optional filter by request type
     * @param branchId Optional filter by branch
     * @param studentId Optional filter by student
     * @param page Page number (default: 0)
     * @param size Items per page (default: 20)
     * @param sort Sort field (default: submittedAt)
     * @return Page of StudentRequestDTO
     */
    @GetMapping("/student-requests")
    @PreAuthorize("hasAnyRole('ACADEMIC_STAFF', 'CENTER_HEAD', 'MANAGER')")
    public ResponseEntity<ResponseObject<Page<StudentRequestDTO>>> getAllRequests(
            @RequestParam(required = false, defaultValue = "PENDING") RequestStatus status,
            @RequestParam(required = false) StudentRequestType type,
            @RequestParam(required = false) Long branchId,
            @RequestParam(required = false) Long studentId,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size,
            @RequestParam(defaultValue = "submittedAt") String sort) {

        log.info("Getting all requests with filters (status={}, type={}, branchId={}, studentId={})",
                status, type, branchId, studentId);

        Pageable pageable = PageRequest.of(page, size, Sort.by(Sort.Direction.DESC, sort));
        Page<StudentRequestDTO> result = studentRequestService.getAllRequests(status, type, branchId, studentId, pageable);

        return ResponseEntity.ok(
                new ResponseObject<>(
                        HttpStatus.OK.value(),
                        "Requests retrieved successfully",
                        result
                )
        );
    }

    /**
     * Get request detail for review (Academic Staff)
     * GET /api/v1/student-requests/{requestId}
     *
     * @param requestId The ID of the request
     * @return StudentRequestDTO
     */
    @GetMapping("/student-requests/{requestId}")
    @PreAuthorize("hasAnyRole('ACADEMIC_STAFF', 'CENTER_HEAD', 'MANAGER')")
    public ResponseEntity<ResponseObject<StudentRequestDTO>> getRequestByIdForStaff(
            @PathVariable Long requestId) {

        log.info("Academic staff getting request {}", requestId);
        StudentRequestDTO result = studentRequestService.getRequestById(requestId);

        return ResponseEntity.ok(
                new ResponseObject<>(
                        HttpStatus.OK.value(),
                        "Request retrieved successfully",
                        result
                )
        );
    }

    /**
     * Approve a student request (handles ABSENCE and MAKEUP types)
     * POST /api/v1/student-requests/{requestId}/approve
     *
     * @param requestId The ID of the request to approve
     * @param dto Approval details (optional notes)
     * @return Updated StudentRequestDTO
     */
    @PostMapping("/student-requests/{requestId}/approve")
    @PreAuthorize("hasAnyRole('ACADEMIC_STAFF', 'CENTER_HEAD', 'MANAGER')")
    public ResponseEntity<ResponseObject<StudentRequestDTO>> approveRequest(
            @PathVariable Long requestId,
            @Valid @RequestBody(required = false) ApproveRequestDTO dto) {

        log.info("Approving request {}", requestId);

        // Extract authenticated staff ID from SecurityContext
        Long staffId = getAuthenticatedUserId();

        ApproveRequestDTO approveDto = dto != null ? dto : new ApproveRequestDTO();

        // Get request to determine type
        StudentRequestDTO request = studentRequestService.getRequestById(requestId);
        StudentRequestDTO result;

        // Route to appropriate service method based on request type
        switch (request.getRequestType()) {
            case ABSENCE:
                result = studentRequestService.approveAbsenceRequest(requestId, staffId, approveDto);
                break;
            case MAKEUP:
                result = studentRequestService.approveMakeupRequest(requestId, staffId, approveDto);
                break;
            default:
                throw new IllegalStateException("Unsupported request type: " + request.getRequestType());
        }

        return ResponseEntity.ok(
                new ResponseObject<>(
                        HttpStatus.OK.value(),
                        "Request approved successfully",
                        result
                )
        );
    }

    /**
     * Reject an absence request
     * POST /api/v1/student-requests/{requestId}/reject
     *
     * @param requestId The ID of the request to reject
     * @param dto Rejection details (required reason)
     * @return Updated StudentRequestDTO
     */
    @PostMapping("/student-requests/{requestId}/reject")
    @PreAuthorize("hasAnyRole('ACADEMIC_STAFF', 'CENTER_HEAD', 'MANAGER')")
    public ResponseEntity<ResponseObject<StudentRequestDTO>> rejectRequest(
            @PathVariable Long requestId,
            @Valid @RequestBody RejectRequestDTO dto) {

        log.info("Rejecting request {}", requestId);

        // Extract authenticated staff ID from SecurityContext
        Long staffId = getAuthenticatedUserId();

        StudentRequestDTO result = studentRequestService.rejectAbsenceRequest(requestId, staffId, dto);

        return ResponseEntity.ok(
                new ResponseObject<>(
                        HttpStatus.OK.value(),
                        "Request rejected",
                        result
                )
        );
    }

    // ==================== MAKEUP REQUEST ENDPOINTS ====================

    /**
     * Find available makeup sessions for a missed session
     * GET /api/v1/students/{studentId}/sessions/{sessionId}/available-makeups
     *
     * @param studentId The ID of the student
     * @param sessionId The ID of the missed session
     * @param dateFrom Optional filter for earliest date
     * @param dateTo Optional filter for latest date
     * @param branchId Optional filter for specific branch
     * @param modality Optional filter for modality (OFFLINE, ONLINE, HYBRID)
     * @return MakeupSessionSearchResultDTO with available sessions
     */
    @GetMapping("/students/{studentId}/sessions/{sessionId}/available-makeups")
    @PreAuthorize("hasAnyRole('STUDENT', 'ACADEMIC_STAFF', 'CENTER_HEAD', 'MANAGER')")
    public ResponseEntity<ResponseObject<MakeupSessionSearchResultDTO>> findAvailableMakeupSessions(
            @PathVariable Long studentId,
            @PathVariable Long sessionId,
            @RequestParam(required = false) LocalDate dateFrom,
            @RequestParam(required = false) LocalDate dateTo,
            @RequestParam(required = false) Long branchId,
            @RequestParam(required = false) Modality modality) {

        log.info("Finding available makeup sessions for student {} and session {} (filters: dateFrom={}, dateTo={}, branchId={}, modality={})",
                studentId, sessionId, dateFrom, dateTo, branchId, modality);

        MakeupSessionSearchResultDTO result = studentRequestService.findAvailableMakeupSessions(
                studentId, sessionId, dateFrom, dateTo, branchId, modality
        );

        return ResponseEntity.ok(
                new ResponseObject<>(
                        HttpStatus.OK.value(),
                        "Available makeup sessions retrieved successfully",
                        result
                )
        );
    }

    /**
     * Create makeup request
     * POST /api/v1/students/{studentId}/requests/makeup
     *
     * @param studentId The ID of the student making the request
     * @param request The makeup request details (targetSessionId, makeupSessionId, reason)
     * @return Created StudentRequestDTO
     */
    @PostMapping("/students/{studentId}/requests/makeup")
    @PreAuthorize("hasRole('STUDENT')")
    public ResponseEntity<ResponseObject<StudentRequestDTO>> createMakeupRequest(
            @PathVariable Long studentId,
            @Valid @RequestBody CreateMakeupRequestDTO request) {

        log.info("Received makeup request from student {} - target: {}, makeup: {}",
                studentId, request.getTargetSessionId(), request.getMakeupSessionId());

        StudentRequestDTO result = studentRequestService.createMakeupRequest(studentId, request);

        return ResponseEntity.status(HttpStatus.CREATED).body(
                new ResponseObject<>(
                        HttpStatus.CREATED.value(),
                        "Makeup request created successfully",
                        result
                )
        );
    }

    // ==================== FUTURE ENDPOINTS ====================

    // TODO: Phase 4 - Transfer Request
    // POST /api/v1/students/{studentId}/requests/transfer
    // POST /api/v1/students/{studentId}/transfer-validation
}

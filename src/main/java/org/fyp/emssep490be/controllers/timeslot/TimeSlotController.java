package org.fyp.emssep490be.controllers.timeslot;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.fyp.emssep490be.dtos.ResponseObject;
import org.fyp.emssep490be.dtos.timeslot.CreateTimeSlotRequestDTO;
import org.fyp.emssep490be.dtos.timeslot.TimeSlotDTO;
import org.fyp.emssep490be.dtos.timeslot.UpdateTimeSlotRequestDTO;
import org.fyp.emssep490be.services.timeslot.TimeSlotService;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * Controller for Time Slot Template management operations
 * Base path: /api/v1/branches/{branchId}/time-slots
 */
@RestController
@RequestMapping("/api/v1/branches/{branchId}/time-slots")
@RequiredArgsConstructor
public class TimeSlotController {

    private final TimeSlotService timeSlotService;

    /**
     * Get Time Slots by Branch
     * GET /branches/{branchId}/time-slots
     *
     * @param branchId Branch ID
     * @return List of time slots for the branch
     */
    @GetMapping
    public ResponseEntity<ResponseObject<List<TimeSlotDTO>>> getTimeSlotsByBranch(
            @PathVariable Long branchId) {
        List<TimeSlotDTO> result = timeSlotService.getTimeSlotsByBranch(branchId);
        return ResponseEntity.ok(
                new ResponseObject<>(HttpStatus.OK.value(), "Time slots retrieved successfully", result)
        );
    }

    /**
     * Get Time Slot by ID
     * GET /branches/{branchId}/time-slots/{id}
     *
     * @param branchId Branch ID
     * @param id       Time slot ID
     * @return Time slot information
     */
    @GetMapping("/{id}")
    public ResponseEntity<ResponseObject<TimeSlotDTO>> getTimeSlotById(
            @PathVariable Long branchId,
            @PathVariable Long id) {
        TimeSlotDTO result = timeSlotService.getTimeSlotById(branchId, id);
        return ResponseEntity.ok(
                new ResponseObject<>(HttpStatus.OK.value(), "Time slot retrieved successfully", result)
        );
    }

    /**
     * Create Time Slot
     * POST /branches/{branchId}/time-slots
     * Roles: MANAGER, CENTER_HEAD
     *
     * @param branchId Branch ID
     * @param request  Time slot creation data
     * @return Created time slot information
     */
    @PostMapping
    public ResponseEntity<ResponseObject<TimeSlotDTO>> createTimeSlot(
            @PathVariable Long branchId,
            @Valid @RequestBody CreateTimeSlotRequestDTO request) {
        TimeSlotDTO result = timeSlotService.createTimeSlot(branchId, request);
        return ResponseEntity.status(HttpStatus.CREATED).body(
                new ResponseObject<>(HttpStatus.CREATED.value(), "Time slot created successfully", result)
        );
    }

    /**
     * Update Time Slot
     * PUT /branches/{branchId}/time-slots/{id}
     * Roles: MANAGER, CENTER_HEAD
     *
     * @param branchId Branch ID
     * @param id       Time slot ID
     * @param request  Time slot update data
     * @return Updated time slot information
     */
    @PutMapping("/{id}")
    public ResponseEntity<ResponseObject<TimeSlotDTO>> updateTimeSlot(
            @PathVariable Long branchId,
            @PathVariable Long id,
            @Valid @RequestBody UpdateTimeSlotRequestDTO request) {
        TimeSlotDTO result = timeSlotService.updateTimeSlot(branchId, id, request);
        return ResponseEntity.ok(
                new ResponseObject<>(HttpStatus.OK.value(), "Time slot updated successfully", result)
        );
    }

    /**
     * Delete Time Slot
     * DELETE /branches/{branchId}/time-slots/{id}
     * Roles: MANAGER, CENTER_HEAD
     *
     * @param branchId Branch ID
     * @param id       Time slot ID
     * @return No content
     */
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteTimeSlot(
            @PathVariable Long branchId,
            @PathVariable Long id) {
        timeSlotService.deleteTimeSlot(branchId, id);
        return ResponseEntity.noContent().build();
    }
}

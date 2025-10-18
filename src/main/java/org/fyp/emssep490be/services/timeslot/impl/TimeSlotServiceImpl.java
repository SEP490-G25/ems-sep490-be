package org.fyp.emssep490be.services.timeslot.impl;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.fyp.emssep490be.dtos.timeslot.CreateTimeSlotRequestDTO;
import org.fyp.emssep490be.dtos.timeslot.TimeSlotDTO;
import org.fyp.emssep490be.dtos.timeslot.UpdateTimeSlotRequestDTO;
import org.fyp.emssep490be.entities.Branch;
import org.fyp.emssep490be.entities.TimeSlotTemplate;
import org.fyp.emssep490be.exceptions.CustomException;
import org.fyp.emssep490be.exceptions.ErrorCode;
import org.fyp.emssep490be.repositories.BranchRepository;
import org.fyp.emssep490be.repositories.TimeSlotTemplateRepository;
import org.fyp.emssep490be.services.timeslot.TimeSlotService;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Duration;
import java.time.OffsetDateTime;
import java.time.ZoneOffset;
import java.util.List;
import java.util.stream.Collectors;

/**
 * Implementation of TimeSlotService for Time Slot Template management operations
 */
@Service
@RequiredArgsConstructor
@Slf4j
@Transactional
public class TimeSlotServiceImpl implements TimeSlotService {

    private final TimeSlotTemplateRepository timeSlotTemplateRepository;
    private final BranchRepository branchRepository;

    /**
     * Get all time slots for a branch
     *
     * @param branchId Branch ID
     * @return List of time slots
     */
    @Override
    @Transactional(readOnly = true)
    public List<TimeSlotDTO> getTimeSlotsByBranch(Long branchId) {
        log.info("Getting time slots for branch ID: {}", branchId);

        List<TimeSlotTemplate> timeSlots = timeSlotTemplateRepository.findByBranchIdOrderByStartTimeAsc(branchId);

        return timeSlots.stream()
                .map(this::convertToDTO)
                .collect(Collectors.toList());
    }

    /**
     * Get time slot by ID
     *
     * @param branchId Branch ID
     * @param id       Time slot ID
     * @return Time slot information
     */
    @Override
    @Transactional(readOnly = true)
    public TimeSlotDTO getTimeSlotById(Long branchId, Long id) {
        log.info("Getting time slot ID: {} for branch ID: {}", id, branchId);

        TimeSlotTemplate timeSlot = timeSlotTemplateRepository.findByIdAndBranchId(id, branchId)
                .orElseThrow(() -> new CustomException(ErrorCode.TIMESLOT_NOT_FOUND));

        return convertToDTO(timeSlot);
    }

    /**
     * Create a new time slot for a branch
     *
     * @param branchId Branch ID
     * @param request  Time slot creation data
     * @return Created time slot information
     */
    @Override
    public TimeSlotDTO createTimeSlot(Long branchId, CreateTimeSlotRequestDTO request) {
        log.info("Creating time slot for branch ID: {}", branchId);

        // Validate branch exists
        Branch branch = branchRepository.findById(branchId)
                .orElseThrow(() -> new CustomException(ErrorCode.BRANCH_NOT_FOUND));

        // Validate time range (start < end)
        if (!request.getStartTime().isBefore(request.getEndTime())) {
            throw new CustomException(ErrorCode.TIMESLOT_INVALID_TIME_RANGE);
        }

        // Calculate duration and validate if provided
        long calculatedMinutes = Duration.between(request.getStartTime(), request.getEndTime()).toMinutes();
        if (request.getDurationMin() != null && request.getDurationMin() != calculatedMinutes) {
            throw new CustomException(ErrorCode.TIMESLOT_DURATION_MISMATCH);
        }

        // Check for overlapping time slots
        List<TimeSlotTemplate> overlappingSlots = timeSlotTemplateRepository.findOverlappingTimeSlots(
                branchId,
                request.getStartTime(),
                request.getEndTime()
        );

        if (!overlappingSlots.isEmpty()) {
            throw new CustomException(ErrorCode.TIMESLOT_OVERLAP);
        }

        // Create time slot entity
        TimeSlotTemplate timeSlot = new TimeSlotTemplate();
        timeSlot.setBranch(branch);
        timeSlot.setName(request.getName());
        timeSlot.setStartTime(request.getStartTime());
        timeSlot.setEndTime(request.getEndTime());
        timeSlot.setDurationMinutes((int) calculatedMinutes);
        timeSlot.setCreatedAt(OffsetDateTime.now(ZoneOffset.UTC));
        timeSlot.setUpdatedAt(OffsetDateTime.now(ZoneOffset.UTC));

        TimeSlotTemplate savedTimeSlot = timeSlotTemplateRepository.save(timeSlot);
        log.info("Time slot created successfully with ID: {}", savedTimeSlot.getId());

        return convertToDTO(savedTimeSlot);
    }

    /**
     * Update an existing time slot
     *
     * @param branchId Branch ID
     * @param id       Time slot ID
     * @param request  Time slot update data
     * @return Updated time slot information
     */
    @Override
    public TimeSlotDTO updateTimeSlot(Long branchId, Long id, UpdateTimeSlotRequestDTO request) {
        log.info("Updating time slot ID: {} for branch ID: {}", id, branchId);

        TimeSlotTemplate timeSlot = timeSlotTemplateRepository.findByIdAndBranchId(id, branchId)
                .orElseThrow(() -> new CustomException(ErrorCode.TIMESLOT_NOT_FOUND));

        // Update name if provided
        if (request.getName() != null && !request.getName().isBlank()) {
            timeSlot.setName(request.getName());
        }

        // Update times if both provided
        if (request.getStartTime() != null && request.getEndTime() != null) {
            // Validate time range
            if (!request.getStartTime().isBefore(request.getEndTime())) {
                throw new CustomException(ErrorCode.TIMESLOT_INVALID_TIME_RANGE);
            }

            // Calculate duration and validate if provided
            long calculatedMinutes = Duration.between(request.getStartTime(), request.getEndTime()).toMinutes();
            if (request.getDurationMin() != null && request.getDurationMin() != calculatedMinutes) {
                throw new CustomException(ErrorCode.TIMESLOT_DURATION_MISMATCH);
            }

            // Check for overlapping time slots (exclude current one)
            List<TimeSlotTemplate> overlappingSlots = timeSlotTemplateRepository.findOverlappingTimeSlots(
                    branchId,
                    request.getStartTime(),
                    request.getEndTime()
            );
            overlappingSlots = overlappingSlots.stream()
                    .filter(slot -> !slot.getId().equals(id))
                    .collect(Collectors.toList());

            if (!overlappingSlots.isEmpty()) {
                throw new CustomException(ErrorCode.TIMESLOT_OVERLAP);
            }

            timeSlot.setStartTime(request.getStartTime());
            timeSlot.setEndTime(request.getEndTime());
            timeSlot.setDurationMinutes((int) calculatedMinutes);
        }

        timeSlot.setUpdatedAt(OffsetDateTime.now(ZoneOffset.UTC));

        TimeSlotTemplate updatedTimeSlot = timeSlotTemplateRepository.save(timeSlot);
        log.info("Time slot updated successfully: {}", updatedTimeSlot.getId());

        return convertToDTO(updatedTimeSlot);
    }

    /**
     * Delete a time slot
     *
     * @param branchId Branch ID
     * @param id       Time slot ID
     */
    @Override
    public void deleteTimeSlot(Long branchId, Long id) {
        log.info("Deleting time slot ID: {} for branch ID: {}", id, branchId);

        TimeSlotTemplate timeSlot = timeSlotTemplateRepository.findByIdAndBranchId(id, branchId)
                .orElseThrow(() -> new CustomException(ErrorCode.TIMESLOT_NOT_FOUND));

        // Note: In production, you should check if this time slot is used by any classes
        // For now, we'll allow deletion

        timeSlotTemplateRepository.delete(timeSlot);
        log.info("Time slot deleted successfully: {}", id);
    }

    // ============ Private Helper Methods ============

    /**
     * Convert TimeSlotTemplate entity to TimeSlotDTO
     */
    private TimeSlotDTO convertToDTO(TimeSlotTemplate timeSlot) {
        TimeSlotDTO dto = new TimeSlotDTO();
        dto.setId(timeSlot.getId());
        dto.setBranchId(timeSlot.getBranch().getId());
        dto.setName(timeSlot.getName());
        dto.setStartTime(timeSlot.getStartTime());
        dto.setEndTime(timeSlot.getEndTime());
        dto.setDurationMinutes(timeSlot.getDurationMinutes());
        dto.setCreatedAt(timeSlot.getCreatedAt() != null ? timeSlot.getCreatedAt().toLocalDateTime() : null);
        dto.setUpdatedAt(timeSlot.getUpdatedAt() != null ? timeSlot.getUpdatedAt().toLocalDateTime() : null);
        return dto;
    }
}

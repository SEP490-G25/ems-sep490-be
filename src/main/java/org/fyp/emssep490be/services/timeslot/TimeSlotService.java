package org.fyp.emssep490be.services.timeslot;

import org.fyp.emssep490be.dtos.timeslot.CreateTimeSlotRequestDTO;
import org.fyp.emssep490be.dtos.timeslot.TimeSlotDTO;
import org.fyp.emssep490be.dtos.timeslot.UpdateTimeSlotRequestDTO;

import java.util.List;

/**
 * Service interface for Time Slot Template management operations
 */
public interface TimeSlotService {

    /**
     * Get all time slots for a branch
     *
     * @param branchId Branch ID
     * @return List of time slots
     */
    List<TimeSlotDTO> getTimeSlotsByBranch(Long branchId);

    /**
     * Get time slot by ID
     *
     * @param branchId Branch ID
     * @param id       Time slot ID
     * @return Time slot information
     */
    TimeSlotDTO getTimeSlotById(Long branchId, Long id);

    /**
     * Create a new time slot for a branch
     *
     * @param branchId Branch ID
     * @param request  Time slot creation data
     * @return Created time slot information
     */
    TimeSlotDTO createTimeSlot(Long branchId, CreateTimeSlotRequestDTO request);

    /**
     * Update an existing time slot
     *
     * @param branchId Branch ID
     * @param id       Time slot ID
     * @param request  Time slot update data
     * @return Updated time slot information
     */
    TimeSlotDTO updateTimeSlot(Long branchId, Long id, UpdateTimeSlotRequestDTO request);

    /**
     * Delete a time slot
     *
     * @param branchId Branch ID
     * @param id       Time slot ID
     */
    void deleteTimeSlot(Long branchId, Long id);
}

package org.fyp.emssep490be.services.timeslot.impl;

import org.fyp.emssep490be.dtos.timeslot.CreateTimeSlotRequestDTO;
import org.fyp.emssep490be.dtos.timeslot.TimeSlotDTO;
import org.fyp.emssep490be.dtos.timeslot.UpdateTimeSlotRequestDTO;
import org.fyp.emssep490be.entities.Branch;
import org.fyp.emssep490be.entities.Center;
import org.fyp.emssep490be.entities.TimeSlotTemplate;
import org.fyp.emssep490be.entities.enums.BranchStatus;
import org.fyp.emssep490be.exceptions.CustomException;
import org.fyp.emssep490be.exceptions.ErrorCode;
import org.fyp.emssep490be.repositories.BranchRepository;
import org.fyp.emssep490be.repositories.TimeSlotTemplateRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalTime;
import java.time.OffsetDateTime;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

/**
 * Unit tests for TimeSlotServiceImpl
 */
@ExtendWith(MockitoExtension.class)
class TimeSlotServiceImplTest {

    @Mock
    private TimeSlotTemplateRepository timeSlotTemplateRepository;

    @Mock
    private BranchRepository branchRepository;

    @InjectMocks
    private TimeSlotServiceImpl timeSlotService;

    private Branch testBranch;
    private TimeSlotTemplate testTimeSlot;

    @BeforeEach
    void setUp() {
        // Setup test center
        Center testCenter = new Center();
        testCenter.setId(1L);
        testCenter.setCode("CTR001");

        // Setup test branch
        testBranch = new Branch();
        testBranch.setId(1L);
        testBranch.setCenter(testCenter);
        testBranch.setCode("BR001");
        testBranch.setStatus(BranchStatus.ACTIVE);

        // Setup test time slot
        testTimeSlot = new TimeSlotTemplate();
        testTimeSlot.setId(1L);
        testTimeSlot.setBranch(testBranch);
        testTimeSlot.setName("Morning Slot");
        testTimeSlot.setStartTime(LocalTime.of(9, 0));
        testTimeSlot.setEndTime(LocalTime.of(11, 0));
        testTimeSlot.setDurationMinutes(120);
        testTimeSlot.setCreatedAt(OffsetDateTime.now());
        testTimeSlot.setUpdatedAt(OffsetDateTime.now());
    }

    @Test
    void getTimeSlotsByBranch_WithValidBranchId_ShouldReturnTimeSlots() {
        // Arrange
        when(timeSlotTemplateRepository.findByBranchIdOrderByStartTimeAsc(1L))
                .thenReturn(Arrays.asList(testTimeSlot));

        // Act
        List<TimeSlotDTO> result = timeSlotService.getTimeSlotsByBranch(1L);

        // Assert
        assertThat(result).isNotNull();
        assertThat(result).hasSize(1);
        assertThat(result.get(0).getName()).isEqualTo("Morning Slot");
        assertThat(result.get(0).getStartTime()).isEqualTo(LocalTime.of(9, 0));
        assertThat(result.get(0).getEndTime()).isEqualTo(LocalTime.of(11, 0));
        assertThat(result.get(0).getDurationMinutes()).isEqualTo(120);
        verify(timeSlotTemplateRepository).findByBranchIdOrderByStartTimeAsc(1L);
    }

    @Test
    void getTimeSlotsByBranch_WithNoTimeSlots_ShouldReturnEmptyList() {
        // Arrange
        when(timeSlotTemplateRepository.findByBranchIdOrderByStartTimeAsc(1L))
                .thenReturn(Collections.emptyList());

        // Act
        List<TimeSlotDTO> result = timeSlotService.getTimeSlotsByBranch(1L);

        // Assert
        assertThat(result).isEmpty();
        verify(timeSlotTemplateRepository).findByBranchIdOrderByStartTimeAsc(1L);
    }

    @Test
    void getTimeSlotById_WithValidId_ShouldReturnTimeSlot() {
        // Arrange
        when(timeSlotTemplateRepository.findByIdAndBranchId(1L, 1L))
                .thenReturn(Optional.of(testTimeSlot));

        // Act
        TimeSlotDTO result = timeSlotService.getTimeSlotById(1L, 1L);

        // Assert
        assertThat(result).isNotNull();
        assertThat(result.getName()).isEqualTo("Morning Slot");
        verify(timeSlotTemplateRepository).findByIdAndBranchId(1L, 1L);
    }

    @Test
    void getTimeSlotById_WithInvalidId_ShouldThrowException() {
        // Arrange
        when(timeSlotTemplateRepository.findByIdAndBranchId(999L, 1L))
                .thenReturn(Optional.empty());

        // Act & Assert
        assertThatThrownBy(() -> timeSlotService.getTimeSlotById(1L, 999L))
                .isInstanceOf(CustomException.class)
                .hasFieldOrPropertyWithValue("errorCode", ErrorCode.TIMESLOT_NOT_FOUND);
    }

    @Test
    void createTimeSlot_WithValidData_ShouldCreateTimeSlot() {
        // Arrange
        CreateTimeSlotRequestDTO request = new CreateTimeSlotRequestDTO();
        request.setName("Afternoon Slot");
        request.setStartTime(LocalTime.of(14, 0));
        request.setEndTime(LocalTime.of(16, 0));
        request.setDurationMin(120);

        when(branchRepository.findById(1L)).thenReturn(Optional.of(testBranch));
        when(timeSlotTemplateRepository.findOverlappingTimeSlots(eq(1L), any(), any()))
                .thenReturn(Collections.emptyList());
        when(timeSlotTemplateRepository.save(any(TimeSlotTemplate.class)))
                .thenReturn(testTimeSlot);

        // Act
        TimeSlotDTO result = timeSlotService.createTimeSlot(1L, request);

        // Assert
        assertThat(result).isNotNull();
        verify(branchRepository).findById(1L);
        verify(timeSlotTemplateRepository).findOverlappingTimeSlots(eq(1L), any(), any());
        verify(timeSlotTemplateRepository).save(any(TimeSlotTemplate.class));
    }

    @Test
    void createTimeSlot_WithInvalidBranchId_ShouldThrowException() {
        // Arrange
        CreateTimeSlotRequestDTO request = new CreateTimeSlotRequestDTO();
        request.setName("Test Slot");
        request.setStartTime(LocalTime.of(9, 0));
        request.setEndTime(LocalTime.of(11, 0));

        when(branchRepository.findById(999L)).thenReturn(Optional.empty());

        // Act & Assert
        assertThatThrownBy(() -> timeSlotService.createTimeSlot(999L, request))
                .isInstanceOf(CustomException.class)
                .hasFieldOrPropertyWithValue("errorCode", ErrorCode.BRANCH_NOT_FOUND);
    }

    @Test
    void createTimeSlot_WithInvalidTimeRange_ShouldThrowException() {
        // Arrange
        CreateTimeSlotRequestDTO request = new CreateTimeSlotRequestDTO();
        request.setName("Invalid Slot");
        request.setStartTime(LocalTime.of(11, 0));
        request.setEndTime(LocalTime.of(9, 0)); // End before start

        when(branchRepository.findById(1L)).thenReturn(Optional.of(testBranch));

        // Act & Assert
        assertThatThrownBy(() -> timeSlotService.createTimeSlot(1L, request))
                .isInstanceOf(CustomException.class)
                .hasFieldOrPropertyWithValue("errorCode", ErrorCode.TIMESLOT_INVALID_TIME_RANGE);
    }

    @Test
    void createTimeSlot_WithDurationMismatch_ShouldThrowException() {
        // Arrange
        CreateTimeSlotRequestDTO request = new CreateTimeSlotRequestDTO();
        request.setName("Test Slot");
        request.setStartTime(LocalTime.of(9, 0));
        request.setEndTime(LocalTime.of(11, 0)); // 120 minutes
        request.setDurationMin(60); // Mismatch

        when(branchRepository.findById(1L)).thenReturn(Optional.of(testBranch));

        // Act & Assert
        assertThatThrownBy(() -> timeSlotService.createTimeSlot(1L, request))
                .isInstanceOf(CustomException.class)
                .hasFieldOrPropertyWithValue("errorCode", ErrorCode.TIMESLOT_DURATION_MISMATCH);
    }

    @Test
    void createTimeSlot_WithOverlap_ShouldThrowException() {
        // Arrange
        CreateTimeSlotRequestDTO request = new CreateTimeSlotRequestDTO();
        request.setName("Overlapping Slot");
        request.setStartTime(LocalTime.of(10, 0));
        request.setEndTime(LocalTime.of(12, 0));

        TimeSlotTemplate overlappingSlot = new TimeSlotTemplate();
        overlappingSlot.setId(2L);
        overlappingSlot.setStartTime(LocalTime.of(9, 0));
        overlappingSlot.setEndTime(LocalTime.of(11, 0));

        when(branchRepository.findById(1L)).thenReturn(Optional.of(testBranch));
        when(timeSlotTemplateRepository.findOverlappingTimeSlots(eq(1L), any(), any()))
                .thenReturn(Arrays.asList(overlappingSlot));

        // Act & Assert
        assertThatThrownBy(() -> timeSlotService.createTimeSlot(1L, request))
                .isInstanceOf(CustomException.class)
                .hasFieldOrPropertyWithValue("errorCode", ErrorCode.TIMESLOT_OVERLAP);
    }

    @Test
    void updateTimeSlot_WithValidData_ShouldUpdateTimeSlot() {
        // Arrange
        UpdateTimeSlotRequestDTO request = new UpdateTimeSlotRequestDTO();
        request.setName("Updated Slot");
        request.setStartTime(LocalTime.of(9, 0));
        request.setEndTime(LocalTime.of(10, 30));

        when(timeSlotTemplateRepository.findByIdAndBranchId(1L, 1L))
                .thenReturn(Optional.of(testTimeSlot));
        when(timeSlotTemplateRepository.findOverlappingTimeSlots(eq(1L), any(), any()))
                .thenReturn(Collections.emptyList());
        when(timeSlotTemplateRepository.save(any(TimeSlotTemplate.class)))
                .thenReturn(testTimeSlot);

        // Act
        TimeSlotDTO result = timeSlotService.updateTimeSlot(1L, 1L, request);

        // Assert
        assertThat(result).isNotNull();
        verify(timeSlotTemplateRepository).findByIdAndBranchId(1L, 1L);
        verify(timeSlotTemplateRepository).save(any(TimeSlotTemplate.class));
    }

    @Test
    void updateTimeSlot_WithInvalidId_ShouldThrowException() {
        // Arrange
        UpdateTimeSlotRequestDTO request = new UpdateTimeSlotRequestDTO();
        when(timeSlotTemplateRepository.findByIdAndBranchId(999L, 1L))
                .thenReturn(Optional.empty());

        // Act & Assert
        assertThatThrownBy(() -> timeSlotService.updateTimeSlot(1L, 999L, request))
                .isInstanceOf(CustomException.class)
                .hasFieldOrPropertyWithValue("errorCode", ErrorCode.TIMESLOT_NOT_FOUND);
    }

    @Test
    void updateTimeSlot_WithInvalidTimeRange_ShouldThrowException() {
        // Arrange
        UpdateTimeSlotRequestDTO request = new UpdateTimeSlotRequestDTO();
        request.setStartTime(LocalTime.of(11, 0));
        request.setEndTime(LocalTime.of(9, 0));

        when(timeSlotTemplateRepository.findByIdAndBranchId(1L, 1L))
                .thenReturn(Optional.of(testTimeSlot));

        // Act & Assert
        assertThatThrownBy(() -> timeSlotService.updateTimeSlot(1L, 1L, request))
                .isInstanceOf(CustomException.class)
                .hasFieldOrPropertyWithValue("errorCode", ErrorCode.TIMESLOT_INVALID_TIME_RANGE);
    }

    @Test
    void deleteTimeSlot_WithValidId_ShouldDeleteTimeSlot() {
        // Arrange
        when(timeSlotTemplateRepository.findByIdAndBranchId(1L, 1L))
                .thenReturn(Optional.of(testTimeSlot));
        doNothing().when(timeSlotTemplateRepository).delete(any(TimeSlotTemplate.class));

        // Act
        timeSlotService.deleteTimeSlot(1L, 1L);

        // Assert
        verify(timeSlotTemplateRepository).findByIdAndBranchId(1L, 1L);
        verify(timeSlotTemplateRepository).delete(testTimeSlot);
    }

    @Test
    void deleteTimeSlot_WithInvalidId_ShouldThrowException() {
        // Arrange
        when(timeSlotTemplateRepository.findByIdAndBranchId(999L, 1L))
                .thenReturn(Optional.empty());

        // Act & Assert
        assertThatThrownBy(() -> timeSlotService.deleteTimeSlot(1L, 999L))
                .isInstanceOf(CustomException.class)
                .hasFieldOrPropertyWithValue("errorCode", ErrorCode.TIMESLOT_NOT_FOUND);
    }
}

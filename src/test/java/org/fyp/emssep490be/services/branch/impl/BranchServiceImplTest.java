package org.fyp.emssep490be.services.branch.impl;

import org.fyp.emssep490be.dtos.branch.BranchDTO;
import org.fyp.emssep490be.dtos.branch.BranchDetailDTO;
import org.fyp.emssep490be.dtos.branch.CreateBranchRequestDTO;
import org.fyp.emssep490be.dtos.branch.UpdateBranchRequestDTO;
import org.fyp.emssep490be.dtos.common.PagedResponseDTO;
import org.fyp.emssep490be.entities.Branch;
import org.fyp.emssep490be.entities.Center;
import org.fyp.emssep490be.entities.Resource;
import org.fyp.emssep490be.entities.TimeSlotTemplate;
import org.fyp.emssep490be.entities.enums.BranchStatus;
import org.fyp.emssep490be.entities.enums.ResourceType;
import org.fyp.emssep490be.exceptions.CustomException;
import org.fyp.emssep490be.exceptions.ErrorCode;
import org.fyp.emssep490be.repositories.BranchRepository;
import org.fyp.emssep490be.repositories.CenterRepository;
import org.fyp.emssep490be.repositories.ResourceRepository;
import org.fyp.emssep490be.repositories.TimeSlotTemplateRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;

import java.time.LocalDate;
import java.time.LocalTime;
import java.time.OffsetDateTime;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

/**
 * Unit tests for BranchServiceImpl
 */
@ExtendWith(MockitoExtension.class)
class BranchServiceImplTest {

    @Mock
    private BranchRepository branchRepository;

    @Mock
    private CenterRepository centerRepository;

    @Mock
    private TimeSlotTemplateRepository timeSlotTemplateRepository;

    @Mock
    private ResourceRepository resourceRepository;

    @InjectMocks
    private BranchServiceImpl branchService;

    private Center testCenter;
    private Branch testBranch;
    private TimeSlotTemplate testTimeSlot;
    private Resource testResource;

    @BeforeEach
    void setUp() {
        // Setup test center
        testCenter = new Center();
        testCenter.setId(1L);
        testCenter.setCode("CTR001");
        testCenter.setName("Test Center");

        // Setup test branch
        testBranch = new Branch();
        testBranch.setId(1L);
        testBranch.setCenter(testCenter);
        testBranch.setCode("BR001");
        testBranch.setName("Test Branch");
        testBranch.setAddress("123 Test St");
        testBranch.setLocation("Test Location");
        testBranch.setPhone("0123456789");
        testBranch.setCapacity(100);
        testBranch.setStatus(BranchStatus.ACTIVE);
        testBranch.setOpeningDate(LocalDate.now());
        testBranch.setCreatedAt(OffsetDateTime.now());
        testBranch.setUpdatedAt(OffsetDateTime.now());

        // Setup test time slot
        testTimeSlot = new TimeSlotTemplate();
        testTimeSlot.setId(1L);
        testTimeSlot.setBranch(testBranch);
        testTimeSlot.setName("Morning Slot");
        testTimeSlot.setStartTime(LocalTime.of(9, 0));
        testTimeSlot.setEndTime(LocalTime.of(11, 0));
        testTimeSlot.setDurationMinutes(120);

        // Setup test resource
        testResource = new Resource();
        testResource.setId(1L);
        testResource.setBranch(testBranch);
        testResource.setResourceType(ResourceType.ROOM);
        testResource.setName("Room 101");
        testResource.setLocation("Building A");
        testResource.setCapacity(30);
    }

    @Test
    void getAllBranches_WithNoPagination_ShouldReturnAllBranches() {
        // Arrange
        Page<Branch> branchPage = new PageImpl<>(Arrays.asList(testBranch));
        when(branchRepository.findAll(any(Pageable.class))).thenReturn(branchPage);

        // Act
        PagedResponseDTO<BranchDTO> result = branchService.getAllBranches(null, null, null, null);

        // Assert
        assertThat(result).isNotNull();
        assertThat(result.getData()).hasSize(1);
        assertThat(result.getData().get(0).getCode()).isEqualTo("BR001");
        assertThat(result.getPagination().getTotalItems()).isEqualTo(1);
        verify(branchRepository).findAll(any(Pageable.class));
    }

    @Test
    void getAllBranches_WithCenterIdFilter_ShouldReturnFilteredBranches() {
        // Arrange
        Page<Branch> branchPage = new PageImpl<>(Arrays.asList(testBranch));
        when(branchRepository.findByCenterId(eq(1L), any(Pageable.class))).thenReturn(branchPage);

        // Act
        PagedResponseDTO<BranchDTO> result = branchService.getAllBranches(1L, null, 1, 10);

        // Assert
        assertThat(result).isNotNull();
        assertThat(result.getData()).hasSize(1);
        verify(branchRepository).findByCenterId(eq(1L), any(Pageable.class));
    }

    @Test
    void getAllBranches_WithStatusFilter_ShouldReturnFilteredBranches() {
        // Arrange
        Page<Branch> branchPage = new PageImpl<>(Arrays.asList(testBranch));
        when(branchRepository.findByStatus(eq("ACTIVE"), any(Pageable.class))).thenReturn(branchPage);

        // Act
        PagedResponseDTO<BranchDTO> result = branchService.getAllBranches(null, "ACTIVE", 1, 10);

        // Assert
        assertThat(result).isNotNull();
        assertThat(result.getData()).hasSize(1);
        verify(branchRepository).findByStatus(eq("ACTIVE"), any(Pageable.class));
    }

    @Test
    void getAllBranches_WithBothFilters_ShouldReturnFilteredBranches() {
        // Arrange
        Page<Branch> branchPage = new PageImpl<>(Arrays.asList(testBranch));
        when(branchRepository.findByCenterIdAndStatus(eq(1L), eq("ACTIVE"), any(Pageable.class)))
                .thenReturn(branchPage);

        // Act
        PagedResponseDTO<BranchDTO> result = branchService.getAllBranches(1L, "ACTIVE", 1, 10);

        // Assert
        assertThat(result).isNotNull();
        assertThat(result.getData()).hasSize(1);
        verify(branchRepository).findByCenterIdAndStatus(eq(1L), eq("ACTIVE"), any(Pageable.class));
    }

    @Test
    void getBranchById_WithValidId_ShouldReturnBranchDetail() {
        // Arrange
        when(branchRepository.findById(1L)).thenReturn(Optional.of(testBranch));
        when(timeSlotTemplateRepository.findByBranchIdOrderByStartTimeAsc(1L))
                .thenReturn(Arrays.asList(testTimeSlot));
        when(resourceRepository.findByBranchId(1L))
                .thenReturn(Arrays.asList(testResource));

        // Act
        BranchDetailDTO result = branchService.getBranchById(1L);

        // Assert
        assertThat(result).isNotNull();
        assertThat(result.getCode()).isEqualTo("BR001");
        assertThat(result.getTimeSlots()).hasSize(1);
        assertThat(result.getResources()).hasSize(1);
        verify(branchRepository).findById(1L);
        verify(timeSlotTemplateRepository).findByBranchIdOrderByStartTimeAsc(1L);
        verify(resourceRepository).findByBranchId(1L);
    }

    @Test
    void getBranchById_WithInvalidId_ShouldThrowException() {
        // Arrange
        when(branchRepository.findById(999L)).thenReturn(Optional.empty());

        // Act & Assert
        assertThatThrownBy(() -> branchService.getBranchById(999L))
                .isInstanceOf(CustomException.class)
                .hasFieldOrPropertyWithValue("errorCode", ErrorCode.BRANCH_NOT_FOUND);
        verify(branchRepository).findById(999L);
    }

    @Test
    void createBranch_WithValidData_ShouldCreateBranch() {
        // Arrange
        CreateBranchRequestDTO request = new CreateBranchRequestDTO();
        request.setCenterId(1L);
        request.setCode("BR002");
        request.setName("New Branch");
        request.setAddress("456 New St");
        request.setStatus("ACTIVE");

        when(centerRepository.findById(1L)).thenReturn(Optional.of(testCenter));
        when(branchRepository.existsByCodeAndCenterId("BR002", 1L)).thenReturn(false);
        when(branchRepository.save(any(Branch.class))).thenReturn(testBranch);

        // Act
        BranchDTO result = branchService.createBranch(request);

        // Assert
        assertThat(result).isNotNull();
        verify(centerRepository).findById(1L);
        verify(branchRepository).existsByCodeAndCenterId("BR002", 1L);
        verify(branchRepository).save(any(Branch.class));
    }

    @Test
    void createBranch_WithNonExistentCenter_ShouldThrowException() {
        // Arrange
        CreateBranchRequestDTO request = new CreateBranchRequestDTO();
        request.setCenterId(999L);
        request.setCode("BR002");

        when(centerRepository.findById(999L)).thenReturn(Optional.empty());

        // Act & Assert
        assertThatThrownBy(() -> branchService.createBranch(request))
                .isInstanceOf(CustomException.class)
                .hasFieldOrPropertyWithValue("errorCode", ErrorCode.CENTER_NOT_FOUND);
        verify(centerRepository).findById(999L);
    }

    @Test
    void createBranch_WithDuplicateCode_ShouldThrowException() {
        // Arrange
        CreateBranchRequestDTO request = new CreateBranchRequestDTO();
        request.setCenterId(1L);
        request.setCode("BR001");

        when(centerRepository.findById(1L)).thenReturn(Optional.of(testCenter));
        when(branchRepository.existsByCodeAndCenterId("BR001", 1L)).thenReturn(true);

        // Act & Assert
        assertThatThrownBy(() -> branchService.createBranch(request))
                .isInstanceOf(CustomException.class)
                .hasFieldOrPropertyWithValue("errorCode", ErrorCode.BRANCH_CODE_ALREADY_EXISTS);
        verify(branchRepository).existsByCodeAndCenterId("BR001", 1L);
    }

    @Test
    void createBranch_WithInvalidStatus_ShouldThrowException() {
        // Arrange
        CreateBranchRequestDTO request = new CreateBranchRequestDTO();
        request.setCenterId(1L);
        request.setCode("BR002");
        request.setStatus("INVALID_STATUS");

        when(centerRepository.findById(1L)).thenReturn(Optional.of(testCenter));
        when(branchRepository.existsByCodeAndCenterId("BR002", 1L)).thenReturn(false);

        // Act & Assert
        assertThatThrownBy(() -> branchService.createBranch(request))
                .isInstanceOf(CustomException.class)
                .hasFieldOrPropertyWithValue("errorCode", ErrorCode.INVALID_STATUS);
    }

    @Test
    void updateBranch_WithValidData_ShouldUpdateBranch() {
        // Arrange
        UpdateBranchRequestDTO request = new UpdateBranchRequestDTO();
        request.setName("Updated Branch");
        request.setAddress("Updated Address");

        when(branchRepository.findById(1L)).thenReturn(Optional.of(testBranch));
        when(branchRepository.save(any(Branch.class))).thenReturn(testBranch);

        // Act
        BranchDTO result = branchService.updateBranch(1L, request);

        // Assert
        assertThat(result).isNotNull();
        verify(branchRepository).findById(1L);
        verify(branchRepository).save(any(Branch.class));
    }

    @Test
    void updateBranch_WithInvalidId_ShouldThrowException() {
        // Arrange
        UpdateBranchRequestDTO request = new UpdateBranchRequestDTO();
        when(branchRepository.findById(999L)).thenReturn(Optional.empty());

        // Act & Assert
        assertThatThrownBy(() -> branchService.updateBranch(999L, request))
                .isInstanceOf(CustomException.class)
                .hasFieldOrPropertyWithValue("errorCode", ErrorCode.BRANCH_NOT_FOUND);
    }

    @Test
    void updateBranch_WithInvalidStatus_ShouldThrowException() {
        // Arrange
        UpdateBranchRequestDTO request = new UpdateBranchRequestDTO();
        request.setStatus("INVALID_STATUS");

        when(branchRepository.findById(1L)).thenReturn(Optional.of(testBranch));

        // Act & Assert
        assertThatThrownBy(() -> branchService.updateBranch(1L, request))
                .isInstanceOf(CustomException.class)
                .hasFieldOrPropertyWithValue("errorCode", ErrorCode.INVALID_STATUS);
    }

    @Test
    void deleteBranch_WithValidId_ShouldSoftDelete() {
        // Arrange
        when(branchRepository.findById(1L)).thenReturn(Optional.of(testBranch));
        when(branchRepository.save(any(Branch.class))).thenReturn(testBranch);

        // Act
        branchService.deleteBranch(1L);

        // Assert
        verify(branchRepository).findById(1L);
        verify(branchRepository).save(argThat(branch -> branch.getStatus() == BranchStatus.CLOSED));
    }

    @Test
    void deleteBranch_WithInvalidId_ShouldThrowException() {
        // Arrange
        when(branchRepository.findById(999L)).thenReturn(Optional.empty());

        // Act & Assert
        assertThatThrownBy(() -> branchService.deleteBranch(999L))
                .isInstanceOf(CustomException.class)
                .hasFieldOrPropertyWithValue("errorCode", ErrorCode.BRANCH_NOT_FOUND);
    }
}

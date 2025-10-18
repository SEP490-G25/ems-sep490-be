package org.fyp.emssep490be.services.resource.impl;

import org.fyp.emssep490be.dtos.resource.CreateResourceRequestDTO;
import org.fyp.emssep490be.dtos.resource.ResourceDTO;
import org.fyp.emssep490be.dtos.resource.UpdateResourceRequestDTO;
import org.fyp.emssep490be.entities.Branch;
import org.fyp.emssep490be.entities.Center;
import org.fyp.emssep490be.entities.Resource;
import org.fyp.emssep490be.entities.enums.BranchStatus;
import org.fyp.emssep490be.entities.enums.ResourceType;
import org.fyp.emssep490be.exceptions.CustomException;
import org.fyp.emssep490be.exceptions.ErrorCode;
import org.fyp.emssep490be.repositories.BranchRepository;
import org.fyp.emssep490be.repositories.ResourceRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDate;
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
 * Unit tests for ResourceServiceImpl
 * Includes comprehensive tests for the conflict detection algorithm
 */
@ExtendWith(MockitoExtension.class)
class ResourceServiceImplTest {

    @Mock
    private ResourceRepository resourceRepository;

    @Mock
    private BranchRepository branchRepository;

    @InjectMocks
    private ResourceServiceImpl resourceService;

    private Branch testBranch;
    private Resource testRoomResource;
    private Resource testVirtualResource;

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

        // Setup test ROOM resource
        testRoomResource = new Resource();
        testRoomResource.setId(1L);
        testRoomResource.setBranch(testBranch);
        testRoomResource.setResourceType(ResourceType.ROOM);
        testRoomResource.setName("Room 101");
        testRoomResource.setLocation("Building A");
        testRoomResource.setCapacity(30);
        testRoomResource.setEquipment("Projector, Whiteboard");
        testRoomResource.setDescription("Standard classroom");
        testRoomResource.setCreatedAt(OffsetDateTime.now());
        testRoomResource.setUpdatedAt(OffsetDateTime.now());

        // Setup test VIRTUAL resource
        testVirtualResource = new Resource();
        testVirtualResource.setId(2L);
        testVirtualResource.setBranch(testBranch);
        testVirtualResource.setResourceType(ResourceType.VIRTUAL);
        testVirtualResource.setName("Zoom Room 1");
        testVirtualResource.setMeetingUrl("https://zoom.us/j/123456789");
        testVirtualResource.setMeetingId("123456789");
        testVirtualResource.setAccountEmail("zoom@example.com");
        testVirtualResource.setLicenseType("Business");
        testVirtualResource.setExpiryDate(LocalDate.now().plusYears(1));
        testVirtualResource.setCreatedAt(OffsetDateTime.now());
        testVirtualResource.setUpdatedAt(OffsetDateTime.now());
    }

    // ========== GET RESOURCES TESTS ==========

    @Test
    void getResourcesByBranch_WithNoFilters_ShouldReturnAllResources() {
        // Arrange
        when(resourceRepository.findByBranchId(1L))
                .thenReturn(Arrays.asList(testRoomResource, testVirtualResource));

        // Act
        List<ResourceDTO> result = resourceService.getResourcesByBranch(1L, null, null, null, null);

        // Assert
        assertThat(result).hasSize(2);
        assertThat(result.get(0).getResourceType()).isEqualTo("ROOM");
        assertThat(result.get(1).getResourceType()).isEqualTo("VIRTUAL");
        verify(resourceRepository).findByBranchId(1L);
    }

    @Test
    void getResourcesByBranch_WithTypeFilter_ShouldReturnFilteredResources() {
        // Arrange
        when(resourceRepository.findByBranchIdAndResourceType(1L, "ROOM"))
                .thenReturn(Arrays.asList(testRoomResource));

        // Act
        List<ResourceDTO> result = resourceService.getResourcesByBranch(1L, "ROOM", null, null, null);

        // Assert
        assertThat(result).hasSize(1);
        assertThat(result.get(0).getResourceType()).isEqualTo("ROOM");
        verify(resourceRepository).findByBranchIdAndResourceType(1L, "ROOM");
    }

    @Test
    void getResourcesByBranch_WithAvailabilityFilter_ShouldUseConflictDetection() {
        // Arrange - CORE ALGORITHM TEST
        LocalDate testDate = LocalDate.now();
        LocalTime startTime = LocalTime.of(9, 0);
        LocalTime endTime = LocalTime.of(11, 0);

        when(resourceRepository.findAvailableResources(1L, null, testDate, startTime, endTime))
                .thenReturn(Arrays.asList(testRoomResource));

        // Act
        List<ResourceDTO> result = resourceService.getResourcesByBranch(
                1L, null, testDate, startTime, endTime);

        // Assert
        assertThat(result).hasSize(1);
        assertThat(result.get(0).getName()).isEqualTo("Room 101");
        verify(resourceRepository).findAvailableResources(1L, null, testDate, startTime, endTime);
    }

    @Test
    void getResourcesByBranch_WithTypeAndAvailabilityFilter_ShouldApplyBothFilters() {
        // Arrange - CORE ALGORITHM TEST with type filter
        LocalDate testDate = LocalDate.now();
        LocalTime startTime = LocalTime.of(14, 0);
        LocalTime endTime = LocalTime.of(16, 0);

        when(resourceRepository.findAvailableResources(1L, "ROOM", testDate, startTime, endTime))
                .thenReturn(Arrays.asList(testRoomResource));

        // Act
        List<ResourceDTO> result = resourceService.getResourcesByBranch(
                1L, "ROOM", testDate, startTime, endTime);

        // Assert
        assertThat(result).hasSize(1);
        assertThat(result.get(0).getResourceType()).isEqualTo("ROOM");
        verify(resourceRepository).findAvailableResources(1L, "ROOM", testDate, startTime, endTime);
    }

    @Test
    void getResourcesByBranch_WithAvailabilityFilter_NoConflicts_ShouldReturnAllAvailable() {
        // Arrange - Test conflict detection returns multiple resources
        LocalDate testDate = LocalDate.now();
        LocalTime startTime = LocalTime.of(8, 0);
        LocalTime endTime = LocalTime.of(9, 0);

        when(resourceRepository.findAvailableResources(1L, null, testDate, startTime, endTime))
                .thenReturn(Arrays.asList(testRoomResource, testVirtualResource));

        // Act
        List<ResourceDTO> result = resourceService.getResourcesByBranch(
                1L, null, testDate, startTime, endTime);

        // Assert
        assertThat(result).hasSize(2);
        verify(resourceRepository).findAvailableResources(1L, null, testDate, startTime, endTime);
    }

    @Test
    void getResourcesByBranch_WithAvailabilityFilter_AllConflicts_ShouldReturnEmpty() {
        // Arrange - Test when all resources are in conflict
        LocalDate testDate = LocalDate.now();
        LocalTime startTime = LocalTime.of(10, 0);
        LocalTime endTime = LocalTime.of(12, 0);

        when(resourceRepository.findAvailableResources(1L, null, testDate, startTime, endTime))
                .thenReturn(Collections.emptyList());

        // Act
        List<ResourceDTO> result = resourceService.getResourcesByBranch(
                1L, null, testDate, startTime, endTime);

        // Assert
        assertThat(result).isEmpty();
        verify(resourceRepository).findAvailableResources(1L, null, testDate, startTime, endTime);
    }

    // ========== GET RESOURCE BY ID TESTS ==========

    @Test
    void getResourceById_WithValidId_ShouldReturnResource() {
        // Arrange
        when(resourceRepository.findByIdAndBranchId(1L, 1L))
                .thenReturn(Optional.of(testRoomResource));

        // Act
        ResourceDTO result = resourceService.getResourceById(1L, 1L);

        // Assert
        assertThat(result).isNotNull();
        assertThat(result.getName()).isEqualTo("Room 101");
        assertThat(result.getResourceType()).isEqualTo("ROOM");
        verify(resourceRepository).findByIdAndBranchId(1L, 1L);
    }

    @Test
    void getResourceById_WithInvalidId_ShouldThrowException() {
        // Arrange
        when(resourceRepository.findByIdAndBranchId(999L, 1L))
                .thenReturn(Optional.empty());

        // Act & Assert
        assertThatThrownBy(() -> resourceService.getResourceById(1L, 999L))
                .isInstanceOf(CustomException.class)
                .hasFieldOrPropertyWithValue("errorCode", ErrorCode.RESOURCE_NOT_FOUND);
    }

    // ========== CREATE RESOURCE TESTS ==========

    @Test
    void createResource_WithValidRoomData_ShouldCreateRoomResource() {
        // Arrange
        CreateResourceRequestDTO request = new CreateResourceRequestDTO();
        request.setResourceType("ROOM");
        request.setName("Room 102");
        request.setLocation("Building B");
        request.setCapacity(40);
        request.setEquipment("Projector");
        request.setDescription("Large classroom");

        when(branchRepository.findById(1L)).thenReturn(Optional.of(testBranch));
        when(resourceRepository.existsByBranchIdAndName(1L, "Room 102")).thenReturn(false);
        when(resourceRepository.save(any(Resource.class))).thenReturn(testRoomResource);

        // Act
        ResourceDTO result = resourceService.createResource(1L, request);

        // Assert
        assertThat(result).isNotNull();
        verify(branchRepository).findById(1L);
        verify(resourceRepository).existsByBranchIdAndName(1L, "Room 102");
        verify(resourceRepository).save(argThat(resource ->
                resource.getResourceType() == ResourceType.ROOM &&
                resource.getLocation() != null &&
                resource.getCapacity() != null
        ));
    }

    @Test
    void createResource_WithValidVirtualData_ShouldCreateVirtualResource() {
        // Arrange
        CreateResourceRequestDTO request = new CreateResourceRequestDTO();
        request.setResourceType("VIRTUAL");
        request.setName("Zoom Room 2");
        request.setMeetingUrl("https://zoom.us/j/987654321");
        request.setMeetingId("987654321");
        request.setAccountEmail("zoom2@example.com");
        request.setLicenseType("Enterprise");
        request.setExpiryDate(LocalDate.now().plusYears(2));

        when(branchRepository.findById(1L)).thenReturn(Optional.of(testBranch));
        when(resourceRepository.existsByBranchIdAndName(1L, "Zoom Room 2")).thenReturn(false);
        when(resourceRepository.save(any(Resource.class))).thenReturn(testVirtualResource);

        // Act
        ResourceDTO result = resourceService.createResource(1L, request);

        // Assert
        assertThat(result).isNotNull();
        verify(resourceRepository).save(argThat(resource ->
                resource.getResourceType() == ResourceType.VIRTUAL &&
                resource.getMeetingUrl() != null &&
                resource.getAccountEmail() != null
        ));
    }

    @Test
    void createResource_WithInvalidBranchId_ShouldThrowException() {
        // Arrange
        CreateResourceRequestDTO request = new CreateResourceRequestDTO();
        request.setResourceType("ROOM");
        request.setName("Room 103");

        when(branchRepository.findById(999L)).thenReturn(Optional.empty());

        // Act & Assert
        assertThatThrownBy(() -> resourceService.createResource(999L, request))
                .isInstanceOf(CustomException.class)
                .hasFieldOrPropertyWithValue("errorCode", ErrorCode.BRANCH_NOT_FOUND);
    }

    @Test
    void createResource_WithInvalidResourceType_ShouldThrowException() {
        // Arrange
        CreateResourceRequestDTO request = new CreateResourceRequestDTO();
        request.setResourceType("INVALID_TYPE");
        request.setName("Test Resource");

        when(branchRepository.findById(1L)).thenReturn(Optional.of(testBranch));

        // Act & Assert
        assertThatThrownBy(() -> resourceService.createResource(1L, request))
                .isInstanceOf(CustomException.class)
                .hasFieldOrPropertyWithValue("errorCode", ErrorCode.RESOURCE_INVALID_TYPE);
    }

    @Test
    void createResource_WithDuplicateName_ShouldThrowException() {
        // Arrange
        CreateResourceRequestDTO request = new CreateResourceRequestDTO();
        request.setResourceType("ROOM");
        request.setName("Room 101");

        when(branchRepository.findById(1L)).thenReturn(Optional.of(testBranch));
        when(resourceRepository.existsByBranchIdAndName(1L, "Room 101")).thenReturn(true);

        // Act & Assert
        assertThatThrownBy(() -> resourceService.createResource(1L, request))
                .isInstanceOf(CustomException.class)
                .hasFieldOrPropertyWithValue("errorCode", ErrorCode.RESOURCE_NAME_ALREADY_EXISTS);
    }

    // ========== UPDATE RESOURCE TESTS ==========

    @Test
    void updateResource_WithValidRoomData_ShouldUpdateResource() {
        // Arrange
        UpdateResourceRequestDTO request = new UpdateResourceRequestDTO();
        request.setName("Updated Room");
        request.setLocation("Updated Location");
        request.setCapacity(50);

        when(resourceRepository.findByIdAndBranchId(1L, 1L))
                .thenReturn(Optional.of(testRoomResource));
        when(resourceRepository.save(any(Resource.class)))
                .thenReturn(testRoomResource);

        // Act
        ResourceDTO result = resourceService.updateResource(1L, 1L, request);

        // Assert
        assertThat(result).isNotNull();
        verify(resourceRepository).findByIdAndBranchId(1L, 1L);
        verify(resourceRepository).save(any(Resource.class));
    }

    @Test
    void updateResource_WithValidVirtualData_ShouldUpdateResource() {
        // Arrange
        UpdateResourceRequestDTO request = new UpdateResourceRequestDTO();
        request.setName("Updated Zoom Room");
        request.setMeetingUrl("https://zoom.us/j/updated");
        request.setAccountEmail("updated@example.com");

        when(resourceRepository.findByIdAndBranchId(2L, 1L))
                .thenReturn(Optional.of(testVirtualResource));
        when(resourceRepository.save(any(Resource.class)))
                .thenReturn(testVirtualResource);

        // Act
        ResourceDTO result = resourceService.updateResource(1L, 2L, request);

        // Assert
        assertThat(result).isNotNull();
        verify(resourceRepository).save(any(Resource.class));
    }

    @Test
    void updateResource_WithInvalidId_ShouldThrowException() {
        // Arrange
        UpdateResourceRequestDTO request = new UpdateResourceRequestDTO();
        when(resourceRepository.findByIdAndBranchId(999L, 1L))
                .thenReturn(Optional.empty());

        // Act & Assert
        assertThatThrownBy(() -> resourceService.updateResource(1L, 999L, request))
                .isInstanceOf(CustomException.class)
                .hasFieldOrPropertyWithValue("errorCode", ErrorCode.RESOURCE_NOT_FOUND);
    }

    // ========== DELETE RESOURCE TESTS ==========

    @Test
    void deleteResource_WithValidId_ShouldDeleteResource() {
        // Arrange
        when(resourceRepository.findByIdAndBranchId(1L, 1L))
                .thenReturn(Optional.of(testRoomResource));
        doNothing().when(resourceRepository).delete(any(Resource.class));

        // Act
        resourceService.deleteResource(1L, 1L);

        // Assert
        verify(resourceRepository).findByIdAndBranchId(1L, 1L);
        verify(resourceRepository).delete(testRoomResource);
    }

    @Test
    void deleteResource_WithInvalidId_ShouldThrowException() {
        // Arrange
        when(resourceRepository.findByIdAndBranchId(999L, 1L))
                .thenReturn(Optional.empty());

        // Act & Assert
        assertThatThrownBy(() -> resourceService.deleteResource(1L, 999L))
                .isInstanceOf(CustomException.class)
                .hasFieldOrPropertyWithValue("errorCode", ErrorCode.RESOURCE_NOT_FOUND);
    }
}

package org.fyp.emssep490be.services.branch;

import org.fyp.emssep490be.entities.Branch;
import org.fyp.emssep490be.entities.Center;
import org.fyp.emssep490be.entities.enums.BranchStatus;
import org.fyp.emssep490be.repositories.BranchRepository;
import org.fyp.emssep490be.repositories.CenterRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.Mockito.*;

/**
 * Unit test for BranchService using Mockito.
 *
 * This test demonstrates:
 * - Mocking repository dependencies
 * - Testing service layer business logic in isolation
 * - Verifying method calls and interactions
 * - Testing exception scenarios
 *
 * Note: Tests are currently DISABLED because BranchService implementation doesn't exist yet.
 * Remove @Disabled annotation when BranchServiceImpl is created and uncomment the test code.
 */
@Disabled("BranchService implementation not yet available - enable when service is implemented")
@ExtendWith(MockitoExtension.class)
class BranchServiceTest {

    @Mock
    private BranchRepository branchRepository;

    @Mock
    private CenterRepository centerRepository;

    // TODO: Replace with actual service implementation when available
    // @InjectMocks
    // private BranchServiceImpl branchService;

    private Center testCenter;
    private Branch testBranch;

    @BeforeEach
    void setUp() {
        // Setup test data
        testCenter = new Center();
        testCenter.setId(1L);
        testCenter.setName("Test Language Center");
        testCenter.setCode("TLC001");
        testCenter.setCreatedAt(java.time.OffsetDateTime.now());

        testBranch = new Branch();
        testBranch.setId(1L);
        testBranch.setCenter(testCenter);
        testBranch.setCode("BR001");
        testBranch.setName("Main Branch");
        testBranch.setAddress("123 Test Street");
        testBranch.setPhone("0123456789");
        testBranch.setStatus(BranchStatus.ACTIVE);
        testBranch.setCreatedAt(java.time.OffsetDateTime.now());
    }

    @Test
    void testGetBranchById_WhenBranchExists_ShouldReturnBranch() {
        // Given
        when(branchRepository.findById(1L)).thenReturn(Optional.of(testBranch));

        // When
        // TODO: Uncomment when service implementation is available
        // BranchDTO result = branchService.getBranchById(1L);

        // Then
        // TODO: Add assertions when service implementation is available
        // assertThat(result).isNotNull();
        // assertThat(result.getBranchCode()).isEqualTo("BR001");
        // assertThat(result.getBranchName()).isEqualTo("Main Branch");

        verify(branchRepository, times(1)).findById(1L);
    }

    @Test
    void testGetBranchById_WhenBranchNotFound_ShouldThrowException() {
        // Given
        when(branchRepository.findById(anyLong())).thenReturn(Optional.empty());

        // When & Then
        // TODO: Uncomment when service implementation is available
        // assertThatThrownBy(() -> branchService.getBranchById(999L))
        //         .isInstanceOf(CustomException.class)
        //         .hasMessageContaining("Branch not found");

        verify(branchRepository, times(1)).findById(999L);
    }

    @Test
    void testGetBranchesByCenter_ShouldReturnListOfBranches() {
        // Given
        Branch branch2 = new Branch();
        branch2.setId(2L);
        branch2.setCenter(testCenter);
        branch2.setCode("BR002");
        branch2.setName("Secondary Branch");
        branch2.setStatus(BranchStatus.ACTIVE);

        when(centerRepository.findById(1L)).thenReturn(Optional.of(testCenter));
        when(branchRepository.findByCenter(testCenter))
                .thenReturn(Arrays.asList(testBranch, branch2));

        // When
        // TODO: Uncomment when service implementation is available
        // List<BranchDTO> results = branchService.getBranchesByCenter(1L);

        // Then
        // TODO: Add assertions when service implementation is available
        // assertThat(results).hasSize(2);
        // assertThat(results).extracting(BranchDTO::getBranchCode)
        //         .containsExactlyInAnyOrder("BR001", "BR002");

        verify(centerRepository, times(1)).findById(1L);
        verify(branchRepository, times(1)).findByCenter(testCenter);
    }

    @Test
    void testCreateBranch_WithValidData_ShouldReturnCreatedBranch() {
        // Given
        when(centerRepository.findById(1L)).thenReturn(Optional.of(testCenter));
        when(branchRepository.save(any(Branch.class))).thenReturn(testBranch);

        // When
        // TODO: Uncomment when service implementation is available
        // CreateBranchRequestDTO request = new CreateBranchRequestDTO();
        // request.setCenterId(1L);
        // request.setBranchCode("BR001");
        // request.setBranchName("Main Branch");
        // request.setAddress("123 Test Street");
        // request.setPhone("0123456789");
        // request.setEmail("branch@test.com");
        //
        // BranchDTO result = branchService.createBranch(request);

        // Then
        // TODO: Add assertions when service implementation is available
        // assertThat(result).isNotNull();
        // assertThat(result.getBranchCode()).isEqualTo("BR001");

        verify(centerRepository, times(1)).findById(1L);
        verify(branchRepository, times(1)).save(any(Branch.class));
    }

    @Test
    void testUpdateBranchStatus_ShouldUpdateAndReturnBranch() {
        // Given
        when(branchRepository.findById(1L)).thenReturn(Optional.of(testBranch));
        when(branchRepository.save(any(Branch.class))).thenAnswer(invocation -> invocation.getArgument(0));

        // When
        // TODO: Uncomment when service implementation is available
        // BranchDTO result = branchService.updateBranchStatus(1L, BranchStatus.INACTIVE);

        // Then
        // TODO: Add assertions when service implementation is available
        // assertThat(result).isNotNull();
        // assertThat(result.getStatus()).isEqualTo(BranchStatus.INACTIVE);

        verify(branchRepository, times(1)).findById(1L);
        verify(branchRepository, times(1)).save(any(Branch.class));
    }
}

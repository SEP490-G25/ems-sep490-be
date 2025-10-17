package org.fyp.emssep490be.controllers.branch;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.fyp.emssep490be.services.branch.BranchService;
import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;

import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

/**
 * Controller test for BranchController using MockMvc.
 *
 * This test demonstrates:
 * - Testing REST endpoints with @WebMvcTest
 * - Mocking service layer dependencies
 * - Testing request/response validation
 * - Testing security and authentication
 * - Testing exception handling via GlobalExceptionHandler
 *
 * Note: Tests are currently DISABLED because BranchController endpoints are not fully implemented.
 * Remove @Disabled annotation when controller is complete and uncomment the test code.
 */
@Disabled("BranchController endpoints not yet fully implemented - enable when controller is ready")
@WebMvcTest(BranchController.class)
@ActiveProfiles("test")
class BranchControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockBean
    private BranchService branchService;

    @Test
    @WithMockUser(roles = "ADMIN")
    void testGetBranchById_WhenBranchExists_ShouldReturnOk() throws Exception {
        // Given
        Long branchId = 1L;
        // TODO: Mock service response when DTOs are available
        // BranchDetailDTO mockResponse = new BranchDetailDTO();
        // mockResponse.setBranchId(branchId);
        // mockResponse.setBranchCode("BR001");
        // mockResponse.setBranchName("Main Branch");
        //
        // when(branchService.getBranchById(branchId)).thenReturn(mockResponse);

        // When & Then
        // TODO: Uncomment when controller is implemented
        // mockMvc.perform(get("/api/branches/{id}", branchId)
        //                 .contentType(MediaType.APPLICATION_JSON))
        //         .andExpect(status().isOk())
        //         .andExpect(jsonPath("$.status").value(200))
        //         .andExpect(jsonPath("$.data.branchCode").value("BR001"))
        //         .andExpect(jsonPath("$.data.branchName").value("Main Branch"));
    }

    @Test
    @WithMockUser(roles = "ADMIN")
    void testGetBranchById_WhenBranchNotFound_ShouldReturnNotFound() throws Exception {
        // Given
        Long branchId = 999L;
        // TODO: Mock service to throw exception
        // when(branchService.getBranchById(branchId))
        //         .thenThrow(new CustomException(ErrorCode.BRANCH_NOT_FOUND));

        // When & Then
        // TODO: Uncomment when controller and exception handling is implemented
        // mockMvc.perform(get("/api/branches/{id}", branchId)
        //                 .contentType(MediaType.APPLICATION_JSON))
        //         .andExpect(status().isNotFound())
        //         .andExpect(jsonPath("$.status").value(404))
        //         .andExpect(jsonPath("$.message").exists());
    }

    @Test
    @WithMockUser(roles = "MANAGER")
    void testCreateBranch_WithValidData_ShouldReturnCreated() throws Exception {
        // Given
        // TODO: Create request DTO when available
        // CreateBranchRequestDTO request = new CreateBranchRequestDTO();
        // request.setCenterId(1L);
        // request.setBranchCode("BR002");
        // request.setBranchName("New Branch");
        // request.setAddress("456 New Street");
        // request.setPhone("0987654321");
        // request.setEmail("new@branch.com");
        //
        // BranchDTO mockResponse = new BranchDTO();
        // mockResponse.setBranchId(2L);
        // mockResponse.setBranchCode("BR002");
        //
        // when(branchService.createBranch(any(CreateBranchRequestDTO.class)))
        //         .thenReturn(mockResponse);

        // When & Then
        // TODO: Uncomment when controller is implemented
        // mockMvc.perform(post("/api/branches")
        //                 .with(csrf())
        //                 .contentType(MediaType.APPLICATION_JSON)
        //                 .content(objectMapper.writeValueAsString(request)))
        //         .andExpect(status().isCreated())
        //         .andExpect(jsonPath("$.status").value(201))
        //         .andExpect(jsonPath("$.data.branchCode").value("BR002"));
    }

    @Test
    @WithMockUser(roles = "MANAGER")
    void testCreateBranch_WithInvalidData_ShouldReturnBadRequest() throws Exception {
        // Given
        // TODO: Create invalid request (e.g., missing required fields)
        // CreateBranchRequestDTO invalidRequest = new CreateBranchRequestDTO();
        // // Missing required fields

        // When & Then
        // TODO: Uncomment when validation is implemented
        // mockMvc.perform(post("/api/branches")
        //                 .with(csrf())
        //                 .contentType(MediaType.APPLICATION_JSON)
        //                 .content(objectMapper.writeValueAsString(invalidRequest)))
        //         .andExpect(status().isBadRequest())
        //         .andExpect(jsonPath("$.status").value(400));
    }

    @Test
    @WithMockUser(roles = "ADMIN")
    void testUpdateBranchStatus_ShouldReturnOk() throws Exception {
        // Given
        Long branchId = 1L;
        String newStatus = "INACTIVE";
        // TODO: Mock service response
        // BranchDTO mockResponse = new BranchDTO();
        // mockResponse.setBranchId(branchId);
        // mockResponse.setStatus(BranchStatus.INACTIVE);
        //
        // when(branchService.updateBranchStatus(branchId, BranchStatus.INACTIVE))
        //         .thenReturn(mockResponse);

        // When & Then
        // TODO: Uncomment when controller is implemented
        // mockMvc.perform(patch("/api/branches/{id}/status", branchId)
        //                 .with(csrf())
        //                 .contentType(MediaType.APPLICATION_JSON)
        //                 .param("status", newStatus))
        //         .andExpect(status().isOk())
        //         .andExpect(jsonPath("$.status").value(200))
        //         .andExpect(jsonPath("$.data.status").value("INACTIVE"));
    }

    @Test
    void testUnauthorizedAccess_ShouldReturnUnauthorized() throws Exception {
        // When & Then - Access without authentication
        // TODO: Uncomment when security is fully configured
        // mockMvc.perform(get("/api/branches/1")
        //                 .contentType(MediaType.APPLICATION_JSON))
        //         .andExpect(status().isUnauthorized());
    }
}

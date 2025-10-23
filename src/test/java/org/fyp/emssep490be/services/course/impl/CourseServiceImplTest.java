package org.fyp.emssep490be.services.course.impl;

import org.fyp.emssep490be.configs.CustomUserDetails;
import org.fyp.emssep490be.dtos.common.PagedResponseDTO;
import org.fyp.emssep490be.dtos.course.*;
import org.fyp.emssep490be.entities.*;
import org.fyp.emssep490be.exceptions.CustomException;
import org.fyp.emssep490be.exceptions.ErrorCode;
import org.fyp.emssep490be.repositories.*;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.mockito.junit.jupiter.MockitoSettings;
import org.mockito.quality.Strictness;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContext;
import org.springframework.security.core.context.SecurityContextHolder;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.OffsetDateTime;
import java.time.ZoneOffset;
import java.util.Arrays;
import java.util.Collections;
import java.util.Optional;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

/**
 * Unit tests for CourseServiceImpl
 * Tests all CRUD operations, approval workflow, and business logic validation
 */
@ExtendWith(MockitoExtension.class)
@MockitoSettings(strictness = Strictness.LENIENT)
class CourseServiceImplTest {

    @Mock
    private CourseRepository courseRepository;

    @Mock
    private SubjectRepository subjectRepository;

    @Mock
    private LevelRepository levelRepository;

    @Mock
    private CoursePhaseRepository coursePhaseRepository;

    @Mock
    private ClassRepository classRepository;

    @Mock
    private UserAccountRepository userAccountRepository;

    @Mock
    private SecurityContext securityContext;

    @Mock
    private Authentication authentication;

    @InjectMocks
    private CourseServiceImpl courseService;

    private Course testCourse;
    private Subject testSubject;
    private Level testLevel;
    private UserAccount testUser;
    private CoursePhase testPhase;

    @BeforeEach
    void setUp() {
        // Setup test user
        testUser = new UserAccount();
        testUser.setId(1L);
        testUser.setEmail("subject.leader@ems.com");

        // Setup test subject
        testSubject = new Subject();
        testSubject.setId(1L);
        testSubject.setCode("ENG-GEN");
        testSubject.setName("English General");

        // Setup test level
        testLevel = new Level();
        testLevel.setId(1L);
        testLevel.setSubject(testSubject);
        testLevel.setCode("A1");
        testLevel.setName("Beginner A1");

        // Setup test course
        testCourse = new Course();
        testCourse.setId(1L);
        testCourse.setSubject(testSubject);
        testCourse.setLevel(testLevel);
        testCourse.setCode("ENG-A1-V1");
        testCourse.setName("English A1 Course");
        testCourse.setVersion(1);
        testCourse.setDescription("Basic English course for beginners");
        testCourse.setTotalHours(120);
        testCourse.setDurationWeeks(12);
        testCourse.setSessionPerWeek(3);
        testCourse.setHoursPerSession(BigDecimal.valueOf(3.33));
        testCourse.setStatus("draft");
        testCourse.setCreatedBy(testUser);
        testCourse.setCreatedAt(OffsetDateTime.now(ZoneOffset.UTC));
        testCourse.setUpdatedAt(OffsetDateTime.now(ZoneOffset.UTC));

        // Setup test phase
        testPhase = new CoursePhase();
        testPhase.setId(1L);
        testPhase.setCourse(testCourse);
        testPhase.setPhaseNumber(1);
        testPhase.setName("Phase 1");
        testPhase.setDurationWeeks(4);
        testPhase.setSortOrder(1);

        // Mock SecurityContext
        CustomUserDetails userDetails = mock(CustomUserDetails.class);
        when(userDetails.getUserId()).thenReturn(1L);
        when(authentication.getPrincipal()).thenReturn(userDetails);
        when(securityContext.getAuthentication()).thenReturn(authentication);
        SecurityContextHolder.setContext(securityContext);
        when(userAccountRepository.findById(1L)).thenReturn(Optional.of(testUser));
    }

    // ==================== getAllCourses Tests ====================

    @Test
    @DisplayName("Should return all courses without filters")
    void testGetAllCourses_NoFilters_Success() {
        // Arrange
        Page<Course> coursePage = new PageImpl<>(Arrays.asList(testCourse));
        when(courseRepository.findByFilters(isNull(), isNull(), isNull(), isNull(), any(Pageable.class)))
                .thenReturn(coursePage);
        when(coursePhaseRepository.countByCourseId(1L)).thenReturn(1L);

        // Act
        PagedResponseDTO<CourseDTO> result = courseService.getAllCourses(null, null, null, null, 1, 10);

        // Assert
        assertThat(result).isNotNull();
        assertThat(result.getData()).hasSize(1);
        assertThat(result.getData().get(0).getCode()).isEqualTo("ENG-A1-V1");
        assertThat(result.getData().get(0).getPhasesCount()).isEqualTo(1);
        verify(courseRepository).findByFilters(isNull(), isNull(), isNull(), isNull(), any(Pageable.class));
    }

    @Test
    @DisplayName("Should return filtered courses by subject and level")
    void testGetAllCourses_WithFilters_Success() {
        // Arrange
        Page<Course> coursePage = new PageImpl<>(Arrays.asList(testCourse));
        when(courseRepository.findByFilters(eq(1L), eq(1L), any(), any(), any(Pageable.class)))
                .thenReturn(coursePage);
        when(coursePhaseRepository.countByCourseId(1L)).thenReturn(1L);

        // Act
        PagedResponseDTO<CourseDTO> result = courseService.getAllCourses(1L, 1L, "draft", null, 1, 10);

        // Assert
        assertThat(result).isNotNull();
        assertThat(result.getData()).hasSize(1);
        verify(courseRepository).findByFilters(eq(1L), eq(1L), any(), any(), any(Pageable.class));
    }

    // ==================== getCourseById Tests ====================

    @Test
    @DisplayName("Should return course detail when course exists")
    void testGetCourseById_Success() {
        // Arrange
        when(courseRepository.findById(1L)).thenReturn(Optional.of(testCourse));
        when(coursePhaseRepository.findByCourseIdOrderBySortOrderAsc(1L))
                .thenReturn(Arrays.asList(testPhase));

        // Act
        CourseDetailDTO result = courseService.getCourseById(1L);

        // Assert
        assertThat(result).isNotNull();
        assertThat(result.getId()).isEqualTo(1L);
        assertThat(result.getCode()).isEqualTo("ENG-A1-V1");
        assertThat(result.getPhases()).hasSize(1);
        verify(courseRepository).findById(1L);
    }

    @Test
    @DisplayName("Should throw exception when course not found")
    void testGetCourseById_NotFound() {
        // Arrange
        when(courseRepository.findById(999L)).thenReturn(Optional.empty());

        // Act & Assert
        assertThatThrownBy(() -> courseService.getCourseById(999L))
                .isInstanceOf(CustomException.class)
                .hasFieldOrPropertyWithValue("errorCode", ErrorCode.COURSE_NOT_FOUND);
        verify(courseRepository).findById(999L);
    }

    // ==================== createCourse Tests ====================

    @Test
    @DisplayName("Should create course successfully")
    void testCreateCourse_Success() {
        // Arrange
        CreateCourseRequestDTO request = new CreateCourseRequestDTO();
        request.setSubjectId(1L);
        request.setLevelId(1L);
        request.setCode("ENG-A2-V1");
        request.setName("English A2 Course");
        request.setVersion(1);
        request.setTotalHours(120.0);
        request.setDurationWeeks(12);
        request.setSessionPerWeek(3);
        request.setHoursPerSession(3.33);

        when(subjectRepository.findById(1L)).thenReturn(Optional.of(testSubject));
        when(levelRepository.findById(1L)).thenReturn(Optional.of(testLevel));
        when(courseRepository.existsByCode("ENG-A2-V1")).thenReturn(false);
        when(courseRepository.save(any(Course.class))).thenReturn(testCourse);
        when(coursePhaseRepository.countByCourseId(any())).thenReturn(0L);

        // Act
        CourseDTO result = courseService.createCourse(request);

        // Assert
        assertThat(result).isNotNull();
        assertThat(result.getStatus()).isEqualTo("draft");
        verify(subjectRepository).findById(1L);
        verify(levelRepository).findById(1L);
        verify(courseRepository).existsByCode("ENG-A2-V1");
        verify(courseRepository).save(any(Course.class));
    }

    @Test
    @DisplayName("Should throw exception when subject not found")
    void testCreateCourse_SubjectNotFound() {
        // Arrange
        CreateCourseRequestDTO request = new CreateCourseRequestDTO();
        request.setSubjectId(999L);
        request.setLevelId(1L);
        request.setCode("TEST-001");

        when(subjectRepository.findById(999L)).thenReturn(Optional.empty());

        // Act & Assert
        assertThatThrownBy(() -> courseService.createCourse(request))
                .isInstanceOf(CustomException.class)
                .hasFieldOrPropertyWithValue("errorCode", ErrorCode.SUBJECT_NOT_FOUND);
        verify(subjectRepository).findById(999L);
        verify(courseRepository, never()).save(any());
    }

    @Test
    @DisplayName("Should throw exception when level not found")
    void testCreateCourse_LevelNotFound() {
        // Arrange
        CreateCourseRequestDTO request = new CreateCourseRequestDTO();
        request.setSubjectId(1L);
        request.setLevelId(999L);
        request.setCode("TEST-001");

        when(subjectRepository.findById(1L)).thenReturn(Optional.of(testSubject));
        when(levelRepository.findById(999L)).thenReturn(Optional.empty());

        // Act & Assert
        assertThatThrownBy(() -> courseService.createCourse(request))
                .isInstanceOf(CustomException.class)
                .hasFieldOrPropertyWithValue("errorCode", ErrorCode.LEVEL_NOT_FOUND);
        verify(levelRepository).findById(999L);
        verify(courseRepository, never()).save(any());
    }

    @Test
    @DisplayName("Should throw exception when course code already exists")
    void testCreateCourse_DuplicateCode() {
        // Arrange
        CreateCourseRequestDTO request = new CreateCourseRequestDTO();
        request.setSubjectId(1L);
        request.setLevelId(1L);
        request.setCode("ENG-A1-V1");

        when(subjectRepository.findById(1L)).thenReturn(Optional.of(testSubject));
        when(levelRepository.findById(1L)).thenReturn(Optional.of(testLevel));
        when(courseRepository.existsByCode("ENG-A1-V1")).thenReturn(true);

        // Act & Assert
        assertThatThrownBy(() -> courseService.createCourse(request))
                .isInstanceOf(CustomException.class)
                .hasFieldOrPropertyWithValue("errorCode", ErrorCode.COURSE_CODE_DUPLICATE);
        verify(courseRepository).existsByCode("ENG-A1-V1");
        verify(courseRepository, never()).save(any());
    }

    @Test
    @DisplayName("Should throw exception when total hours is inconsistent")
    void testCreateCourse_InvalidTotalHours() {
        // Arrange
        CreateCourseRequestDTO request = new CreateCourseRequestDTO();
        request.setSubjectId(1L);
        request.setLevelId(1L);
        request.setCode("TEST-001");
        request.setTotalHours(200.0); // Inconsistent
        request.setDurationWeeks(12);
        request.setSessionPerWeek(3);
        request.setHoursPerSession(3.0); // 12*3*3 = 108, not 200

        when(subjectRepository.findById(1L)).thenReturn(Optional.of(testSubject));
        when(levelRepository.findById(1L)).thenReturn(Optional.of(testLevel));
        when(courseRepository.existsByCode("TEST-001")).thenReturn(false);

        // Act & Assert
        assertThatThrownBy(() -> courseService.createCourse(request))
                .isInstanceOf(CustomException.class)
                .hasFieldOrPropertyWithValue("errorCode", ErrorCode.INVALID_TOTAL_HOURS);
        verify(courseRepository, never()).save(any());
    }

    // ==================== updateCourse Tests ====================

    @Test
    @DisplayName("Should update course successfully when status is draft")
    void testUpdateCourse_Success() {
        // Arrange
        UpdateCourseRequestDTO request = new UpdateCourseRequestDTO();
        request.setName("Updated Course Name");
        request.setDescription("Updated description");

        when(courseRepository.findById(1L)).thenReturn(Optional.of(testCourse));
        when(courseRepository.save(any(Course.class))).thenReturn(testCourse);
        when(coursePhaseRepository.countByCourseId(1L)).thenReturn(1L);

        // Act
        CourseDTO result = courseService.updateCourse(1L, request);

        // Assert
        assertThat(result).isNotNull();
        verify(courseRepository).findById(1L);
        verify(courseRepository).save(any(Course.class));
    }

    @Test
    @DisplayName("Should throw exception when updating non-existent course")
    void testUpdateCourse_NotFound() {
        // Arrange
        UpdateCourseRequestDTO request = new UpdateCourseRequestDTO();
        request.setName("Updated Name");

        when(courseRepository.findById(999L)).thenReturn(Optional.empty());

        // Act & Assert
        assertThatThrownBy(() -> courseService.updateCourse(999L, request))
                .isInstanceOf(CustomException.class)
                .hasFieldOrPropertyWithValue("errorCode", ErrorCode.COURSE_NOT_FOUND);
        verify(courseRepository, never()).save(any());
    }

    @Test
    @DisplayName("Should throw exception when updating approved course")
    void testUpdateCourse_CannotUpdateApproved() {
        // Arrange
        testCourse.setStatus("active");
        testCourse.setApprovedByManager(testUser);
        testCourse.setApprovedAt(OffsetDateTime.now(ZoneOffset.UTC));

        UpdateCourseRequestDTO request = new UpdateCourseRequestDTO();
        request.setName("Updated Name");

        when(courseRepository.findById(1L)).thenReturn(Optional.of(testCourse));

        // Act & Assert
        assertThatThrownBy(() -> courseService.updateCourse(1L, request))
                .isInstanceOf(CustomException.class)
                .hasFieldOrPropertyWithValue("errorCode", ErrorCode.COURSE_CANNOT_BE_UPDATED);
        verify(courseRepository, never()).save(any());
    }

    // ==================== submitCourseForApproval Tests ====================

    @Test
    @DisplayName("Should submit course for approval successfully")
    void testSubmitCourse_Success() {
        // Arrange
        when(courseRepository.findById(1L)).thenReturn(Optional.of(testCourse));
        when(coursePhaseRepository.countByCourseId(1L)).thenReturn(2L);
        when(courseRepository.save(any(Course.class))).thenReturn(testCourse);

        // Act
        CourseDTO result = courseService.submitCourseForApproval(1L);

        // Assert
        assertThat(result).isNotNull();
        verify(courseRepository).findById(1L);
        verify(coursePhaseRepository, atLeastOnce()).countByCourseId(1L); // Called in submit + convertToDTO
        verify(courseRepository).save(any(Course.class));
    }

    @Test
    @DisplayName("Should throw exception when submitting course without phases")
    void testSubmitCourse_NoPhases() {
        // Arrange
        when(courseRepository.findById(1L)).thenReturn(Optional.of(testCourse));
        when(coursePhaseRepository.countByCourseId(1L)).thenReturn(0L);

        // Act & Assert
        assertThatThrownBy(() -> courseService.submitCourseForApproval(1L))
                .isInstanceOf(CustomException.class)
                .hasFieldOrPropertyWithValue("errorCode", ErrorCode.COURSE_NO_PHASES);
        verify(courseRepository, never()).save(any());
    }

    @Test
    @DisplayName("Should throw exception when submitting already approved course")
    void testSubmitCourse_AlreadySubmitted() {
        // Arrange
        testCourse.setApprovedByManager(testUser);
        testCourse.setApprovedAt(OffsetDateTime.now(ZoneOffset.UTC));

        when(courseRepository.findById(1L)).thenReturn(Optional.of(testCourse));

        // Act & Assert
        assertThatThrownBy(() -> courseService.submitCourseForApproval(1L))
                .isInstanceOf(CustomException.class)
                .hasFieldOrPropertyWithValue("errorCode", ErrorCode.COURSE_ALREADY_SUBMITTED);
        verify(courseRepository, never()).save(any());
    }

    // ==================== approveCourse Tests ====================

    @Test
    @DisplayName("Should approve course successfully")
    void testApproveCourse_Approve_Success() {
        // Arrange
        ApprovalRequestDTO request = new ApprovalRequestDTO();
        request.setAction("approve");

        when(courseRepository.findById(1L)).thenReturn(Optional.of(testCourse));
        when(courseRepository.save(any(Course.class))).thenReturn(testCourse);
        when(coursePhaseRepository.countByCourseId(1L)).thenReturn(1L);

        // Act
        CourseDTO result = courseService.approveCourse(1L, request);

        // Assert
        assertThat(result).isNotNull();
        verify(courseRepository).findById(1L);
        verify(courseRepository).save(argThat(course ->
            "active".equals(course.getStatus()) &&
            course.getApprovedByManager() != null &&
            course.getApprovedAt() != null
        ));
    }

    @Test
    @DisplayName("Should reject course with reason successfully")
    void testApproveCourse_Reject_Success() {
        // Arrange
        ApprovalRequestDTO request = new ApprovalRequestDTO();
        request.setAction("reject");
        request.setRejectionReason("Content needs improvement");

        when(courseRepository.findById(1L)).thenReturn(Optional.of(testCourse));
        when(courseRepository.save(any(Course.class))).thenReturn(testCourse);
        when(coursePhaseRepository.countByCourseId(1L)).thenReturn(1L);

        // Act
        CourseDTO result = courseService.approveCourse(1L, request);

        // Assert
        assertThat(result).isNotNull();
        verify(courseRepository).save(argThat(course ->
            "draft".equals(course.getStatus()) &&
            course.getApprovedByManager() == null &&
            "Content needs improvement".equals(course.getRejectionReason())
        ));
    }

    @Test
    @DisplayName("Should throw exception when action is invalid")
    void testApproveCourse_InvalidAction() {
        // Arrange
        ApprovalRequestDTO request = new ApprovalRequestDTO();
        request.setAction("invalid_action");

        when(courseRepository.findById(1L)).thenReturn(Optional.of(testCourse));

        // Act & Assert
        assertThatThrownBy(() -> courseService.approveCourse(1L, request))
                .isInstanceOf(CustomException.class)
                .hasFieldOrPropertyWithValue("errorCode", ErrorCode.INVALID_ACTION);
        verify(courseRepository, never()).save(any());
    }

    @Test
    @DisplayName("Should throw exception when rejecting without reason")
    void testApproveCourse_RejectionReasonRequired() {
        // Arrange
        ApprovalRequestDTO request = new ApprovalRequestDTO();
        request.setAction("reject");
        request.setRejectionReason(null); // No reason provided

        when(courseRepository.findById(1L)).thenReturn(Optional.of(testCourse));

        // Act & Assert
        assertThatThrownBy(() -> courseService.approveCourse(1L, request))
                .isInstanceOf(CustomException.class)
                .hasFieldOrPropertyWithValue("errorCode", ErrorCode.REJECTION_REASON_REQUIRED);
        verify(courseRepository, never()).save(any());
    }

    // ==================== deleteCourse Tests ====================

    @Test
    @DisplayName("Should delete course successfully when not in use")
    void testDeleteCourse_Success() {
        // Arrange
        when(courseRepository.findById(1L)).thenReturn(Optional.of(testCourse));
        when(classRepository.countByCourseId(1L)).thenReturn(0L);
        when(courseRepository.save(any(Course.class))).thenReturn(testCourse);

        // Act
        courseService.deleteCourse(1L);

        // Assert
        verify(courseRepository).findById(1L);
        verify(classRepository).countByCourseId(1L);
        verify(courseRepository).save(argThat(course ->
            "inactive".equals(course.getStatus())
        ));
    }

    @Test
    @DisplayName("Should throw exception when deleting course in use")
    void testDeleteCourse_InUse() {
        // Arrange
        when(courseRepository.findById(1L)).thenReturn(Optional.of(testCourse));
        when(classRepository.countByCourseId(1L)).thenReturn(3L); // 3 classes using this course

        // Act & Assert
        assertThatThrownBy(() -> courseService.deleteCourse(1L))
                .isInstanceOf(CustomException.class)
                .hasFieldOrPropertyWithValue("errorCode", ErrorCode.COURSE_IN_USE);
        verify(courseRepository, never()).save(any());
    }

    @Test
    @DisplayName("Should throw exception when deleting non-existent course")
    void testDeleteCourse_NotFound() {
        // Arrange
        when(courseRepository.findById(999L)).thenReturn(Optional.empty());

        // Act & Assert
        assertThatThrownBy(() -> courseService.deleteCourse(999L))
                .isInstanceOf(CustomException.class)
                .hasFieldOrPropertyWithValue("errorCode", ErrorCode.COURSE_NOT_FOUND);
        verify(courseRepository, never()).save(any());
    }
}

package org.fyp.emssep490be.services.coursephase.impl;

import org.fyp.emssep490be.dtos.coursephase.CoursePhaseDTO;
import org.fyp.emssep490be.dtos.coursephase.CreateCoursePhaseRequestDTO;
import org.fyp.emssep490be.dtos.coursephase.UpdateCoursePhaseRequestDTO;
import org.fyp.emssep490be.entities.Course;
import org.fyp.emssep490be.entities.CoursePhase;
import org.fyp.emssep490be.entities.Level;
import org.fyp.emssep490be.entities.Subject;
import org.fyp.emssep490be.exceptions.CustomException;
import org.fyp.emssep490be.exceptions.ErrorCode;
import org.fyp.emssep490be.repositories.CoursePhaseRepository;
import org.fyp.emssep490be.repositories.CourseRepository;
import org.fyp.emssep490be.repositories.CourseSessionRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.mockito.junit.jupiter.MockitoSettings;
import org.mockito.quality.Strictness;

import java.time.OffsetDateTime;
import java.time.ZoneOffset;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

/**
 * Unit tests for CoursePhaseServiceImpl
 * Tests all CRUD operations and business logic validation for course phases
 */
@ExtendWith(MockitoExtension.class)
@MockitoSettings(strictness = Strictness.LENIENT)
class CoursePhaseServiceImplTest {

    @Mock
    private CoursePhaseRepository coursePhaseRepository;

    @Mock
    private CourseRepository courseRepository;

    @Mock
    private CourseSessionRepository courseSessionRepository;

    @InjectMocks
    private CoursePhaseServiceImpl coursePhaseService;

    private Course testCourse;
    private CoursePhase testPhase1;
    private CoursePhase testPhase2;
    private Subject testSubject;
    private Level testLevel;

    @BeforeEach
    void setUp() {
        // Setup test subject
        testSubject = new Subject();
        testSubject.setId(1L);
        testSubject.setName("English");
        testSubject.setCode("ENG");

        // Setup test level
        testLevel = new Level();
        testLevel.setId(1L);
        testLevel.setName("A1");
        testLevel.setCode("A1");
        testLevel.setSubject(testSubject);

        // Setup test course
        testCourse = new Course();
        testCourse.setId(1L);
        testCourse.setSubject(testSubject);
        testCourse.setLevel(testLevel);
        testCourse.setCode("ENG-A1-V1");
        testCourse.setName("English A1 Course");
        testCourse.setVersion(1);
        testCourse.setStatus("draft");
        testCourse.setCreatedAt(OffsetDateTime.now(ZoneOffset.UTC));
        testCourse.setUpdatedAt(OffsetDateTime.now(ZoneOffset.UTC));

        // Setup test phases
        testPhase1 = new CoursePhase();
        testPhase1.setId(1L);
        testPhase1.setCourse(testCourse);
        testPhase1.setPhaseNumber(1);
        testPhase1.setName("Phase 1 - Foundation");
        testPhase1.setDurationWeeks(4);
        testPhase1.setLearningFocus("Basic grammar and vocabulary");
        testPhase1.setSortOrder(1);
        testPhase1.setCreatedAt(OffsetDateTime.now(ZoneOffset.UTC));
        testPhase1.setUpdatedAt(OffsetDateTime.now(ZoneOffset.UTC));

        testPhase2 = new CoursePhase();
        testPhase2.setId(2L);
        testPhase2.setCourse(testCourse);
        testPhase2.setPhaseNumber(2);
        testPhase2.setName("Phase 2 - Intermediate");
        testPhase2.setDurationWeeks(4);
        testPhase2.setLearningFocus("Speaking and listening skills");
        testPhase2.setSortOrder(2);
        testPhase2.setCreatedAt(OffsetDateTime.now(ZoneOffset.UTC));
        testPhase2.setUpdatedAt(OffsetDateTime.now(ZoneOffset.UTC));
    }

    // ==================== getPhasesByCourse Tests ====================

    @Test
    @DisplayName("Should return all phases for a course")
    void testGetPhasesByCourse_Success() {
        // Arrange
        when(courseRepository.findById(1L)).thenReturn(Optional.of(testCourse));
        when(coursePhaseRepository.findByCourseIdOrderBySortOrderAsc(1L))
                .thenReturn(Arrays.asList(testPhase1, testPhase2));
        when(courseSessionRepository.countByPhaseId(1L)).thenReturn(3L);
        when(courseSessionRepository.countByPhaseId(2L)).thenReturn(5L);

        // Act
        List<CoursePhaseDTO> result = coursePhaseService.getPhasesByCourse(1L);

        // Assert
        assertThat(result).isNotNull();
        assertThat(result).hasSize(2);
        assertThat(result.get(0).getPhaseNumber()).isEqualTo(1);
        assertThat(result.get(0).getName()).isEqualTo("Phase 1 - Foundation");
        assertThat(result.get(0).getSessionsCount()).isEqualTo(3);
        assertThat(result.get(1).getPhaseNumber()).isEqualTo(2);
        assertThat(result.get(1).getSessionsCount()).isEqualTo(5);

        verify(courseRepository).findById(1L);
        verify(coursePhaseRepository).findByCourseIdOrderBySortOrderAsc(1L);
        verify(courseSessionRepository, times(2)).countByPhaseId(anyLong());
    }

    @Test
    @DisplayName("Should throw exception when course not found")
    void testGetPhasesByCourse_CourseNotFound() {
        // Arrange
        when(courseRepository.findById(999L)).thenReturn(Optional.empty());

        // Act & Assert
        assertThatThrownBy(() -> coursePhaseService.getPhasesByCourse(999L))
                .isInstanceOf(CustomException.class)
                .hasFieldOrPropertyWithValue("errorCode", ErrorCode.COURSE_NOT_FOUND);

        verify(courseRepository).findById(999L);
        verify(coursePhaseRepository, never()).findByCourseIdOrderBySortOrderAsc(anyLong());
    }

    // ==================== createPhase Tests ====================

    @Test
    @DisplayName("Should create phase successfully")
    void testCreatePhase_Success() {
        // Arrange
        CreateCoursePhaseRequestDTO request = new CreateCoursePhaseRequestDTO();
        request.setPhaseNumber(1);
        request.setName("Phase 1 - Foundation");
        request.setDurationWeeks(4);
        request.setLearningFocus("Basic grammar");
        request.setSortOrder(1);

        when(courseRepository.findById(1L)).thenReturn(Optional.of(testCourse));
        when(coursePhaseRepository.existsByPhaseNumberAndCourseId(1, 1L)).thenReturn(false);
        when(coursePhaseRepository.save(any(CoursePhase.class))).thenReturn(testPhase1);
        when(courseSessionRepository.countByPhaseId(1L)).thenReturn(0L);

        // Act
        CoursePhaseDTO result = coursePhaseService.createPhase(1L, request);

        // Assert
        assertThat(result).isNotNull();
        assertThat(result.getPhaseNumber()).isEqualTo(1);
        assertThat(result.getName()).isEqualTo("Phase 1 - Foundation");
        assertThat(result.getDurationWeeks()).isEqualTo(4);

        verify(courseRepository).findById(1L);
        verify(coursePhaseRepository).existsByPhaseNumberAndCourseId(1, 1L);
        verify(coursePhaseRepository).save(any(CoursePhase.class));
    }

    @Test
    @DisplayName("Should throw exception when creating phase for non-existent course")
    void testCreatePhase_CourseNotFound() {
        // Arrange
        CreateCoursePhaseRequestDTO request = new CreateCoursePhaseRequestDTO();
        request.setPhaseNumber(1);
        request.setName("Phase 1");

        when(courseRepository.findById(999L)).thenReturn(Optional.empty());

        // Act & Assert
        assertThatThrownBy(() -> coursePhaseService.createPhase(999L, request))
                .isInstanceOf(CustomException.class)
                .hasFieldOrPropertyWithValue("errorCode", ErrorCode.COURSE_NOT_FOUND);

        verify(courseRepository).findById(999L);
        verify(coursePhaseRepository, never()).save(any());
    }

    @Test
    @DisplayName("Should throw exception when creating phase for non-draft course")
    void testCreatePhase_CourseNotDraft() {
        // Arrange
        testCourse.setStatus("active");
        CreateCoursePhaseRequestDTO request = new CreateCoursePhaseRequestDTO();
        request.setPhaseNumber(1);
        request.setName("Phase 1");

        when(courseRepository.findById(1L)).thenReturn(Optional.of(testCourse));

        // Act & Assert
        assertThatThrownBy(() -> coursePhaseService.createPhase(1L, request))
                .isInstanceOf(CustomException.class)
                .hasFieldOrPropertyWithValue("errorCode", ErrorCode.COURSE_CANNOT_BE_MODIFIED);

        verify(courseRepository).findById(1L);
        verify(coursePhaseRepository, never()).save(any());
    }

    @Test
    @DisplayName("Should throw exception when phase number already exists")
    void testCreatePhase_DuplicatePhaseNumber() {
        // Arrange
        CreateCoursePhaseRequestDTO request = new CreateCoursePhaseRequestDTO();
        request.setPhaseNumber(1);
        request.setName("Phase 1");

        when(courseRepository.findById(1L)).thenReturn(Optional.of(testCourse));
        when(coursePhaseRepository.existsByPhaseNumberAndCourseId(1, 1L)).thenReturn(true);

        // Act & Assert
        assertThatThrownBy(() -> coursePhaseService.createPhase(1L, request))
                .isInstanceOf(CustomException.class)
                .hasFieldOrPropertyWithValue("errorCode", ErrorCode.PHASE_NUMBER_DUPLICATE);

        verify(coursePhaseRepository).existsByPhaseNumberAndCourseId(1, 1L);
        verify(coursePhaseRepository, never()).save(any());
    }

    // ==================== updatePhase Tests ====================

    @Test
    @DisplayName("Should update phase successfully")
    void testUpdatePhase_Success() {
        // Arrange
        UpdateCoursePhaseRequestDTO request = new UpdateCoursePhaseRequestDTO();
        request.setName("Updated Phase Name");
        request.setDurationWeeks(6);
        request.setLearningFocus("Updated focus");
        request.setSortOrder(1);

        when(coursePhaseRepository.findById(1L)).thenReturn(Optional.of(testPhase1));
        when(coursePhaseRepository.save(any(CoursePhase.class))).thenReturn(testPhase1);
        when(courseSessionRepository.countByPhaseId(1L)).thenReturn(0L);

        // Act
        CoursePhaseDTO result = coursePhaseService.updatePhase(1L, request);

        // Assert
        assertThat(result).isNotNull();
        verify(coursePhaseRepository).findById(1L);
        verify(coursePhaseRepository).save(any(CoursePhase.class));
    }

    @Test
    @DisplayName("Should throw exception when updating non-existent phase")
    void testUpdatePhase_NotFound() {
        // Arrange
        UpdateCoursePhaseRequestDTO request = new UpdateCoursePhaseRequestDTO();
        request.setName("Updated Name");

        when(coursePhaseRepository.findById(999L)).thenReturn(Optional.empty());

        // Act & Assert
        assertThatThrownBy(() -> coursePhaseService.updatePhase(999L, request))
                .isInstanceOf(CustomException.class)
                .hasFieldOrPropertyWithValue("errorCode", ErrorCode.PHASE_NOT_FOUND);

        verify(coursePhaseRepository).findById(999L);
        verify(coursePhaseRepository, never()).save(any());
    }

    @Test
    @DisplayName("Should throw exception when updating phase of non-draft course")
    void testUpdatePhase_CourseNotDraft() {
        // Arrange
        testCourse.setStatus("active");
        UpdateCoursePhaseRequestDTO request = new UpdateCoursePhaseRequestDTO();
        request.setName("Updated Name");

        when(coursePhaseRepository.findById(1L)).thenReturn(Optional.of(testPhase1));

        // Act & Assert
        assertThatThrownBy(() -> coursePhaseService.updatePhase(1L, request))
                .isInstanceOf(CustomException.class)
                .hasFieldOrPropertyWithValue("errorCode", ErrorCode.COURSE_CANNOT_BE_MODIFIED);

        verify(coursePhaseRepository).findById(1L);
        verify(coursePhaseRepository, never()).save(any());
    }

    // ==================== deletePhase Tests ====================

    @Test
    @DisplayName("Should delete phase successfully")
    void testDeletePhase_Success() {
        // Arrange
        when(coursePhaseRepository.findById(1L)).thenReturn(Optional.of(testPhase1));
        when(courseSessionRepository.countByPhaseId(1L)).thenReturn(0L);

        // Act
        coursePhaseService.deletePhase(1L);

        // Assert
        verify(coursePhaseRepository).findById(1L);
        verify(courseSessionRepository).countByPhaseId(1L);
        verify(coursePhaseRepository).delete(testPhase1);
    }

    @Test
    @DisplayName("Should throw exception when deleting non-existent phase")
    void testDeletePhase_NotFound() {
        // Arrange
        when(coursePhaseRepository.findById(999L)).thenReturn(Optional.empty());

        // Act & Assert
        assertThatThrownBy(() -> coursePhaseService.deletePhase(999L))
                .isInstanceOf(CustomException.class)
                .hasFieldOrPropertyWithValue("errorCode", ErrorCode.PHASE_NOT_FOUND);

        verify(coursePhaseRepository).findById(999L);
        verify(coursePhaseRepository, never()).delete(any());
    }

    @Test
    @DisplayName("Should throw exception when deleting phase with sessions")
    void testDeletePhase_HasSessions() {
        // Arrange
        when(coursePhaseRepository.findById(1L)).thenReturn(Optional.of(testPhase1));
        when(courseSessionRepository.countByPhaseId(1L)).thenReturn(5L);

        // Act & Assert
        assertThatThrownBy(() -> coursePhaseService.deletePhase(1L))
                .isInstanceOf(CustomException.class)
                .hasFieldOrPropertyWithValue("errorCode", ErrorCode.PHASE_HAS_SESSIONS);

        verify(coursePhaseRepository).findById(1L);
        verify(courseSessionRepository).countByPhaseId(1L);
        verify(coursePhaseRepository, never()).delete(any());
    }

    @Test
    @DisplayName("Should throw exception when deleting phase of non-draft course")
    void testDeletePhase_CourseNotDraft() {
        // Arrange
        testCourse.setStatus("active");
        when(coursePhaseRepository.findById(1L)).thenReturn(Optional.of(testPhase1));

        // Act & Assert
        assertThatThrownBy(() -> coursePhaseService.deletePhase(1L))
                .isInstanceOf(CustomException.class)
                .hasFieldOrPropertyWithValue("errorCode", ErrorCode.COURSE_CANNOT_BE_MODIFIED);

        verify(coursePhaseRepository).findById(1L);
        verify(coursePhaseRepository, never()).delete(any());
    }
}

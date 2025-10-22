package org.fyp.emssep490be.services.coursesession.impl;

import org.fyp.emssep490be.dtos.coursesession.CourseSessionDTO;
import org.fyp.emssep490be.dtos.coursesession.CreateCourseSessionRequestDTO;
import org.fyp.emssep490be.dtos.coursesession.UpdateCourseSessionRequestDTO;
import org.fyp.emssep490be.entities.*;
import org.fyp.emssep490be.entities.enums.Skill;
import org.fyp.emssep490be.exceptions.CustomException;
import org.fyp.emssep490be.exceptions.ErrorCode;
import org.fyp.emssep490be.repositories.CoursePhaseRepository;
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
 * Unit tests for CourseSessionServiceImpl
 * Tests all CRUD operations and business logic validation for course sessions
 */
@ExtendWith(MockitoExtension.class)
@MockitoSettings(strictness = Strictness.LENIENT)
class CourseSessionServiceImplTest {

    @Mock
    private CourseSessionRepository courseSessionRepository;

    @Mock
    private CoursePhaseRepository coursePhaseRepository;

    @InjectMocks
    private CourseSessionServiceImpl courseSessionService;

    private Course testCourse;
    private CoursePhase testPhase;
    private CourseSession testSession1;
    private CourseSession testSession2;
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

        // Setup test phase
        testPhase = new CoursePhase();
        testPhase.setId(1L);
        testPhase.setCourse(testCourse);
        testPhase.setPhaseNumber(1);
        testPhase.setName("Phase 1 - Foundation");
        testPhase.setDurationWeeks(4);
        testPhase.setSortOrder(1);
        testPhase.setCreatedAt(OffsetDateTime.now(ZoneOffset.UTC));
        testPhase.setUpdatedAt(OffsetDateTime.now(ZoneOffset.UTC));

        // Setup test sessions
        testSession1 = new CourseSession();
        testSession1.setId(1L);
        testSession1.setPhase(testPhase);
        testSession1.setSequenceNumber(1);
        testSession1.setTopic("Introduction to English");
        testSession1.setStudentTask("Complete homework 1");
        testSession1.setSkillSet(Arrays.asList(Skill.GENERAL, Skill.READING));
        testSession1.setCreatedAt(OffsetDateTime.now(ZoneOffset.UTC));
        testSession1.setUpdatedAt(OffsetDateTime.now(ZoneOffset.UTC));

        testSession2 = new CourseSession();
        testSession2.setId(2L);
        testSession2.setPhase(testPhase);
        testSession2.setSequenceNumber(2);
        testSession2.setTopic("Basic Grammar");
        testSession2.setStudentTask("Practice exercises");
        testSession2.setSkillSet(Arrays.asList(Skill.WRITING, Skill.SPEAKING));
        testSession2.setCreatedAt(OffsetDateTime.now(ZoneOffset.UTC));
        testSession2.setUpdatedAt(OffsetDateTime.now(ZoneOffset.UTC));
    }

    // ==================== getSessionsByPhase Tests ====================

    @Test
    @DisplayName("Should return all sessions for a phase")
    void testGetSessionsByPhase_Success() {
        // Arrange
        when(coursePhaseRepository.findById(1L)).thenReturn(Optional.of(testPhase));
        when(courseSessionRepository.findByPhaseIdOrderBySequenceNumberAsc(1L))
                .thenReturn(Arrays.asList(testSession1, testSession2));

        // Act
        List<CourseSessionDTO> result = courseSessionService.getSessionsByPhase(1L);

        // Assert
        assertThat(result).isNotNull();
        assertThat(result).hasSize(2);
        assertThat(result.get(0).getSequenceNo()).isEqualTo(1);
        assertThat(result.get(0).getTopic()).isEqualTo("Introduction to English");
        assertThat(result.get(0).getSkillSet()).containsExactly("GENERAL", "READING");
        assertThat(result.get(1).getSequenceNo()).isEqualTo(2);
        assertThat(result.get(1).getSkillSet()).containsExactly("WRITING", "SPEAKING");

        verify(coursePhaseRepository).findById(1L);
        verify(courseSessionRepository).findByPhaseIdOrderBySequenceNumberAsc(1L);
    }

    @Test
    @DisplayName("Should throw exception when phase not found")
    void testGetSessionsByPhase_PhaseNotFound() {
        // Arrange
        when(coursePhaseRepository.findById(999L)).thenReturn(Optional.empty());

        // Act & Assert
        assertThatThrownBy(() -> courseSessionService.getSessionsByPhase(999L))
                .isInstanceOf(CustomException.class)
                .hasFieldOrPropertyWithValue("errorCode", ErrorCode.PHASE_NOT_FOUND);

        verify(coursePhaseRepository).findById(999L);
        verify(courseSessionRepository, never()).findByPhaseIdOrderBySequenceNumberAsc(anyLong());
    }

    // ==================== createSession Tests ====================

    @Test
    @DisplayName("Should create session successfully")
    void testCreateSession_Success() {
        // Arrange
        CreateCourseSessionRequestDTO request = new CreateCourseSessionRequestDTO();
        request.setSequenceNo(1);
        request.setTopic("Introduction to English");
        request.setStudentTask("Complete homework 1");
        request.setSkillSet(Arrays.asList("GENERAL", "READING"));

        when(coursePhaseRepository.findById(1L)).thenReturn(Optional.of(testPhase));
        when(courseSessionRepository.existsBySequenceNumberAndPhaseId(1, 1L)).thenReturn(false);
        when(courseSessionRepository.save(any(CourseSession.class))).thenReturn(testSession1);

        // Act
        CourseSessionDTO result = courseSessionService.createSession(1L, request);

        // Assert
        assertThat(result).isNotNull();
        assertThat(result.getSequenceNo()).isEqualTo(1);
        assertThat(result.getTopic()).isEqualTo("Introduction to English");
        assertThat(result.getSkillSet()).containsExactly("GENERAL", "READING");

        verify(coursePhaseRepository).findById(1L);
        verify(courseSessionRepository).existsBySequenceNumberAndPhaseId(1, 1L);
        verify(courseSessionRepository).save(any(CourseSession.class));
    }

    @Test
    @DisplayName("Should throw exception when creating session for non-existent phase")
    void testCreateSession_PhaseNotFound() {
        // Arrange
        CreateCourseSessionRequestDTO request = new CreateCourseSessionRequestDTO();
        request.setSequenceNo(1);
        request.setTopic("Test");

        when(coursePhaseRepository.findById(999L)).thenReturn(Optional.empty());

        // Act & Assert
        assertThatThrownBy(() -> courseSessionService.createSession(999L, request))
                .isInstanceOf(CustomException.class)
                .hasFieldOrPropertyWithValue("errorCode", ErrorCode.PHASE_NOT_FOUND);

        verify(coursePhaseRepository).findById(999L);
        verify(courseSessionRepository, never()).save(any());
    }

    @Test
    @DisplayName("Should throw exception when creating session for non-draft course")
    void testCreateSession_CourseNotDraft() {
        // Arrange
        testCourse.setStatus("active");
        CreateCourseSessionRequestDTO request = new CreateCourseSessionRequestDTO();
        request.setSequenceNo(1);
        request.setTopic("Test");

        when(coursePhaseRepository.findById(1L)).thenReturn(Optional.of(testPhase));

        // Act & Assert
        assertThatThrownBy(() -> courseSessionService.createSession(1L, request))
                .isInstanceOf(CustomException.class)
                .hasFieldOrPropertyWithValue("errorCode", ErrorCode.COURSE_CANNOT_BE_MODIFIED);

        verify(coursePhaseRepository).findById(1L);
        verify(courseSessionRepository, never()).save(any());
    }

    @Test
    @DisplayName("Should throw exception when sequence number already exists")
    void testCreateSession_DuplicateSequenceNumber() {
        // Arrange
        CreateCourseSessionRequestDTO request = new CreateCourseSessionRequestDTO();
        request.setSequenceNo(1);
        request.setTopic("Test");

        when(coursePhaseRepository.findById(1L)).thenReturn(Optional.of(testPhase));
        when(courseSessionRepository.existsBySequenceNumberAndPhaseId(1, 1L)).thenReturn(true);

        // Act & Assert
        assertThatThrownBy(() -> courseSessionService.createSession(1L, request))
                .isInstanceOf(CustomException.class)
                .hasFieldOrPropertyWithValue("errorCode", ErrorCode.SESSION_SEQUENCE_DUPLICATE);

        verify(courseSessionRepository).existsBySequenceNumberAndPhaseId(1, 1L);
        verify(courseSessionRepository, never()).save(any());
    }

    @Test
    @DisplayName("Should throw exception when invalid skill set provided")
    void testCreateSession_InvalidSkillSet() {
        // Arrange
        CreateCourseSessionRequestDTO request = new CreateCourseSessionRequestDTO();
        request.setSequenceNo(1);
        request.setTopic("Test");
        request.setSkillSet(Arrays.asList("INVALID_SKILL"));

        when(coursePhaseRepository.findById(1L)).thenReturn(Optional.of(testPhase));
        when(courseSessionRepository.existsBySequenceNumberAndPhaseId(1, 1L)).thenReturn(false);

        // Act & Assert
        assertThatThrownBy(() -> courseSessionService.createSession(1L, request))
                .isInstanceOf(CustomException.class)
                .hasFieldOrPropertyWithValue("errorCode", ErrorCode.INVALID_SKILL_SET);

        verify(coursePhaseRepository).findById(1L);
        verify(courseSessionRepository, never()).save(any());
    }

    // ==================== updateSession Tests ====================

    @Test
    @DisplayName("Should update session successfully")
    void testUpdateSession_Success() {
        // Arrange
        UpdateCourseSessionRequestDTO request = new UpdateCourseSessionRequestDTO();
        request.setTopic("Updated Topic");
        request.setStudentTask("Updated Task");
        request.setSkillSet(Arrays.asList("LISTENING", "SPEAKING"));

        when(courseSessionRepository.findById(1L)).thenReturn(Optional.of(testSession1));
        when(courseSessionRepository.save(any(CourseSession.class))).thenReturn(testSession1);

        // Act
        CourseSessionDTO result = courseSessionService.updateSession(1L, request);

        // Assert
        assertThat(result).isNotNull();
        verify(courseSessionRepository).findById(1L);
        verify(courseSessionRepository).save(any(CourseSession.class));
    }

    @Test
    @DisplayName("Should throw exception when updating non-existent session")
    void testUpdateSession_NotFound() {
        // Arrange
        UpdateCourseSessionRequestDTO request = new UpdateCourseSessionRequestDTO();
        request.setTopic("Updated Topic");

        when(courseSessionRepository.findById(999L)).thenReturn(Optional.empty());

        // Act & Assert
        assertThatThrownBy(() -> courseSessionService.updateSession(999L, request))
                .isInstanceOf(CustomException.class)
                .hasFieldOrPropertyWithValue("errorCode", ErrorCode.SESSION_NOT_FOUND);

        verify(courseSessionRepository).findById(999L);
        verify(courseSessionRepository, never()).save(any());
    }

    @Test
    @DisplayName("Should throw exception when updating session of non-draft course")
    void testUpdateSession_CourseNotDraft() {
        // Arrange
        testCourse.setStatus("active");
        UpdateCourseSessionRequestDTO request = new UpdateCourseSessionRequestDTO();
        request.setTopic("Updated Topic");

        when(courseSessionRepository.findById(1L)).thenReturn(Optional.of(testSession1));

        // Act & Assert
        assertThatThrownBy(() -> courseSessionService.updateSession(1L, request))
                .isInstanceOf(CustomException.class)
                .hasFieldOrPropertyWithValue("errorCode", ErrorCode.COURSE_CANNOT_BE_MODIFIED);

        verify(courseSessionRepository).findById(1L);
        verify(courseSessionRepository, never()).save(any());
    }

    @Test
    @DisplayName("Should throw exception when updating with invalid skill set")
    void testUpdateSession_InvalidSkillSet() {
        // Arrange
        UpdateCourseSessionRequestDTO request = new UpdateCourseSessionRequestDTO();
        request.setSkillSet(Arrays.asList("INVALID_SKILL"));

        when(courseSessionRepository.findById(1L)).thenReturn(Optional.of(testSession1));

        // Act & Assert
        assertThatThrownBy(() -> courseSessionService.updateSession(1L, request))
                .isInstanceOf(CustomException.class)
                .hasFieldOrPropertyWithValue("errorCode", ErrorCode.INVALID_SKILL_SET);

        verify(courseSessionRepository).findById(1L);
        verify(courseSessionRepository, never()).save(any());
    }

    // ==================== deleteSession Tests ====================

    @Test
    @DisplayName("Should delete session successfully")
    void testDeleteSession_Success() {
        // Arrange
        when(courseSessionRepository.findById(1L)).thenReturn(Optional.of(testSession1));
        when(courseSessionRepository.countSessionUsages(1L)).thenReturn(0L);

        // Act
        courseSessionService.deleteSession(1L);

        // Assert
        verify(courseSessionRepository).findById(1L);
        verify(courseSessionRepository).countSessionUsages(1L);
        verify(courseSessionRepository).delete(testSession1);
    }

    @Test
    @DisplayName("Should throw exception when deleting non-existent session")
    void testDeleteSession_NotFound() {
        // Arrange
        when(courseSessionRepository.findById(999L)).thenReturn(Optional.empty());

        // Act & Assert
        assertThatThrownBy(() -> courseSessionService.deleteSession(999L))
                .isInstanceOf(CustomException.class)
                .hasFieldOrPropertyWithValue("errorCode", ErrorCode.SESSION_NOT_FOUND);

        verify(courseSessionRepository).findById(999L);
        verify(courseSessionRepository, never()).delete(any());
    }

    @Test
    @DisplayName("Should throw exception when deleting session in use")
    void testDeleteSession_SessionInUse() {
        // Arrange
        when(courseSessionRepository.findById(1L)).thenReturn(Optional.of(testSession1));
        when(courseSessionRepository.countSessionUsages(1L)).thenReturn(5L);

        // Act & Assert
        assertThatThrownBy(() -> courseSessionService.deleteSession(1L))
                .isInstanceOf(CustomException.class)
                .hasFieldOrPropertyWithValue("errorCode", ErrorCode.SESSION_IN_USE);

        verify(courseSessionRepository).findById(1L);
        verify(courseSessionRepository).countSessionUsages(1L);
        verify(courseSessionRepository, never()).delete(any());
    }

    @Test
    @DisplayName("Should throw exception when deleting session of non-draft course")
    void testDeleteSession_CourseNotDraft() {
        // Arrange
        testCourse.setStatus("active");
        when(courseSessionRepository.findById(1L)).thenReturn(Optional.of(testSession1));

        // Act & Assert
        assertThatThrownBy(() -> courseSessionService.deleteSession(1L))
                .isInstanceOf(CustomException.class)
                .hasFieldOrPropertyWithValue("errorCode", ErrorCode.COURSE_CANNOT_BE_MODIFIED);

        verify(courseSessionRepository).findById(1L);
        verify(courseSessionRepository, never()).delete(any());
    }
}

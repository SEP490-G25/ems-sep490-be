package org.fyp.emssep490be.services.coursematerial.impl;

import org.fyp.emssep490be.dtos.coursematerial.CourseMaterialDTO;
import org.fyp.emssep490be.dtos.coursematerial.UploadMaterialRequestDTO;
import org.fyp.emssep490be.entities.Course;
import org.fyp.emssep490be.entities.CourseMaterial;
import org.fyp.emssep490be.entities.CoursePhase;
import org.fyp.emssep490be.entities.CourseSession;
import org.fyp.emssep490be.exceptions.CustomException;
import org.fyp.emssep490be.exceptions.ErrorCode;
import org.fyp.emssep490be.repositories.CourseMaterialRepository;
import org.fyp.emssep490be.repositories.CoursePhaseRepository;
import org.fyp.emssep490be.repositories.CourseRepository;
import org.fyp.emssep490be.repositories.CourseSessionRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.mockito.junit.jupiter.MockitoSettings;
import org.mockito.quality.Strictness;
import org.springframework.web.multipart.MultipartFile;

import java.time.OffsetDateTime;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

/**
 * Unit tests for CourseMaterialServiceImpl
 * Tests file upload, validations, and multi-level context handling
 */
@ExtendWith(MockitoExtension.class)
@MockitoSettings(strictness = Strictness.LENIENT)
@DisplayName("CourseMaterialServiceImpl Tests")
class CourseMaterialServiceImplTest {

    @Mock
    private CourseMaterialRepository courseMaterialRepository;

    @Mock
    private CourseRepository courseRepository;

    @Mock
    private CoursePhaseRepository coursePhaseRepository;

    @Mock
    private CourseSessionRepository courseSessionRepository;

    @Mock
    private MultipartFile mockFile;

    @InjectMocks
    private CourseMaterialServiceImpl courseMaterialService;

    private Course testCourse;
    private CoursePhase testPhase;
    private CourseSession testSession;
    private CourseMaterial testMaterial;

    @BeforeEach
    void setUp() {
        // Setup test course
        testCourse = new Course();
        testCourse.setId(1L);
        testCourse.setCode("MATH-101");

        // Setup test phase
        testPhase = new CoursePhase();
        testPhase.setId(1L);
        testPhase.setCourse(testCourse);
        testPhase.setPhaseNumber(1);

        // Setup test session
        testSession = new CourseSession();
        testSession.setId(1L);
        testSession.setPhase(testPhase);
        testSession.setSequenceNumber(1);

        // Setup test material
        testMaterial = new CourseMaterial();
        testMaterial.setId(1L);
        testMaterial.setCourse(testCourse);
        testMaterial.setTitle("Test Material");
        testMaterial.setUrl("/uploads/test.pdf");
        testMaterial.setCreatedAt(OffsetDateTime.now());
        testMaterial.setUpdatedAt(OffsetDateTime.now());
    }

    @Nested
    @DisplayName("uploadMaterial Tests")
    class UploadMaterialTests {

        private UploadMaterialRequestDTO validRequest;

        @BeforeEach
        void setUp() {
            validRequest = new UploadMaterialRequestDTO();
            validRequest.setTitle("Test Material");
            validRequest.setFile(mockFile);

            when(mockFile.isEmpty()).thenReturn(false);
            when(mockFile.getOriginalFilename()).thenReturn("test-file.pdf");
        }

        @Test
        @DisplayName("Should upload material at course level successfully")
        void shouldUploadMaterialAtCourseLevelSuccessfully() {
            // Given
            Long courseId = 1L;
            validRequest.setPhaseId(null);
            validRequest.setCourseSessionId(null);

            when(courseRepository.findById(courseId)).thenReturn(Optional.of(testCourse));
            when(courseMaterialRepository.save(any(CourseMaterial.class))).thenAnswer(invocation -> {
                CourseMaterial savedMaterial = invocation.getArgument(0);
                savedMaterial.setId(10L);
                return savedMaterial;
            });

            // When
            CourseMaterialDTO result = courseMaterialService.uploadMaterial(courseId, validRequest);

            // Then
            assertThat(result).isNotNull();
            assertThat(result.getId()).isEqualTo(10L);
            assertThat(result.getTitle()).isEqualTo("Test Material");
            assertThat(result.getCourseId()).isEqualTo(courseId);
            assertThat(result.getPhaseId()).isNull();
            assertThat(result.getCourseSessionId()).isNull();
            assertThat(result.getUrl()).contains("test-file.pdf");

            verify(courseRepository).findById(courseId);
            verify(coursePhaseRepository, never()).findById(any());
            verify(courseSessionRepository, never()).findById(any());
            verify(courseMaterialRepository).save(any(CourseMaterial.class));
        }

        @Test
        @DisplayName("Should upload material at phase level successfully")
        void shouldUploadMaterialAtPhaseLevelSuccessfully() {
            // Given
            Long courseId = 1L;
            Long phaseId = 1L;
            validRequest.setPhaseId(phaseId);
            validRequest.setCourseSessionId(null);

            when(courseRepository.findById(courseId)).thenReturn(Optional.of(testCourse));
            when(coursePhaseRepository.findById(phaseId)).thenReturn(Optional.of(testPhase));
            when(courseMaterialRepository.save(any(CourseMaterial.class))).thenAnswer(invocation -> {
                CourseMaterial savedMaterial = invocation.getArgument(0);
                savedMaterial.setId(10L);
                return savedMaterial;
            });

            // When
            CourseMaterialDTO result = courseMaterialService.uploadMaterial(courseId, validRequest);

            // Then
            assertThat(result).isNotNull();
            assertThat(result.getPhaseId()).isEqualTo(phaseId);
            assertThat(result.getCourseSessionId()).isNull();

            verify(courseRepository).findById(courseId);
            verify(coursePhaseRepository).findById(phaseId);
            verify(courseMaterialRepository).save(any(CourseMaterial.class));
        }

        @Test
        @DisplayName("Should upload material at session level successfully")
        void shouldUploadMaterialAtSessionLevelSuccessfully() {
            // Given
            Long courseId = 1L;
            Long sessionId = 1L;
            validRequest.setPhaseId(null);
            validRequest.setCourseSessionId(sessionId);

            when(courseRepository.findById(courseId)).thenReturn(Optional.of(testCourse));
            when(courseSessionRepository.findById(sessionId)).thenReturn(Optional.of(testSession));
            when(courseMaterialRepository.save(any(CourseMaterial.class))).thenAnswer(invocation -> {
                CourseMaterial savedMaterial = invocation.getArgument(0);
                savedMaterial.setId(10L);
                return savedMaterial;
            });

            // When
            CourseMaterialDTO result = courseMaterialService.uploadMaterial(courseId, validRequest);

            // Then
            assertThat(result).isNotNull();
            assertThat(result.getPhaseId()).isNull();
            assertThat(result.getCourseSessionId()).isEqualTo(sessionId);

            verify(courseRepository).findById(courseId);
            verify(courseSessionRepository).findById(sessionId);
            verify(courseMaterialRepository).save(any(CourseMaterial.class));
        }

        @Test
        @DisplayName("Should upload material with both phase and session successfully")
        void shouldUploadMaterialWithPhaseAndSessionSuccessfully() {
            // Given
            Long courseId = 1L;
            Long phaseId = 1L;
            Long sessionId = 1L;
            validRequest.setPhaseId(phaseId);
            validRequest.setCourseSessionId(sessionId);

            when(courseRepository.findById(courseId)).thenReturn(Optional.of(testCourse));
            when(coursePhaseRepository.findById(phaseId)).thenReturn(Optional.of(testPhase));
            when(courseSessionRepository.findById(sessionId)).thenReturn(Optional.of(testSession));
            when(courseMaterialRepository.save(any(CourseMaterial.class))).thenAnswer(invocation -> {
                CourseMaterial savedMaterial = invocation.getArgument(0);
                savedMaterial.setId(10L);
                return savedMaterial;
            });

            // When
            CourseMaterialDTO result = courseMaterialService.uploadMaterial(courseId, validRequest);

            // Then
            assertThat(result).isNotNull();
            assertThat(result.getPhaseId()).isEqualTo(phaseId);
            assertThat(result.getCourseSessionId()).isEqualTo(sessionId);

            verify(courseRepository).findById(courseId);
            verify(coursePhaseRepository).findById(phaseId);
            verify(courseSessionRepository).findById(sessionId);
            verify(courseMaterialRepository).save(any(CourseMaterial.class));
        }

        @Test
        @DisplayName("Should throw CustomException when course not found")
        void shouldThrowExceptionWhenCourseNotFound() {
            // Given
            Long courseId = 999L;
            when(courseRepository.findById(courseId)).thenReturn(Optional.empty());

            // When & Then
            assertThatThrownBy(() -> courseMaterialService.uploadMaterial(courseId, validRequest))
                    .isInstanceOf(CustomException.class)
                    .hasFieldOrPropertyWithValue("errorCode", ErrorCode.COURSE_NOT_FOUND);

            verify(courseRepository).findById(courseId);
            verify(courseMaterialRepository, never()).save(any());
        }

        @Test
        @DisplayName("Should throw CustomException when phase not found")
        void shouldThrowExceptionWhenPhaseNotFound() {
            // Given
            Long courseId = 1L;
            Long phaseId = 999L;
            validRequest.setPhaseId(phaseId);

            when(courseRepository.findById(courseId)).thenReturn(Optional.of(testCourse));
            when(coursePhaseRepository.findById(phaseId)).thenReturn(Optional.empty());

            // When & Then
            assertThatThrownBy(() -> courseMaterialService.uploadMaterial(courseId, validRequest))
                    .isInstanceOf(CustomException.class)
                    .hasFieldOrPropertyWithValue("errorCode", ErrorCode.PHASE_NOT_FOUND);

            verify(coursePhaseRepository).findById(phaseId);
            verify(courseMaterialRepository, never()).save(any());
        }

        @Test
        @DisplayName("Should throw CustomException when phase belongs to different course")
        void shouldThrowExceptionWhenPhaseBelongsToDifferentCourse() {
            // Given
            Long courseId = 1L;
            Long phaseId = 1L;
            validRequest.setPhaseId(phaseId);

            Course differentCourse = new Course();
            differentCourse.setId(2L);

            CoursePhase phaseWithDifferentCourse = new CoursePhase();
            phaseWithDifferentCourse.setId(1L);
            phaseWithDifferentCourse.setCourse(differentCourse);

            when(courseRepository.findById(courseId)).thenReturn(Optional.of(testCourse));
            when(coursePhaseRepository.findById(phaseId)).thenReturn(Optional.of(phaseWithDifferentCourse));

            // When & Then
            assertThatThrownBy(() -> courseMaterialService.uploadMaterial(courseId, validRequest))
                    .isInstanceOf(CustomException.class)
                    .hasFieldOrPropertyWithValue("errorCode", ErrorCode.INVALID_INPUT);

            verify(courseMaterialRepository, never()).save(any());
        }

        @Test
        @DisplayName("Should throw CustomException when session not found")
        void shouldThrowExceptionWhenSessionNotFound() {
            // Given
            Long courseId = 1L;
            Long sessionId = 999L;
            validRequest.setCourseSessionId(sessionId);

            when(courseRepository.findById(courseId)).thenReturn(Optional.of(testCourse));
            when(courseSessionRepository.findById(sessionId)).thenReturn(Optional.empty());

            // When & Then
            assertThatThrownBy(() -> courseMaterialService.uploadMaterial(courseId, validRequest))
                    .isInstanceOf(CustomException.class)
                    .hasFieldOrPropertyWithValue("errorCode", ErrorCode.COURSE_SESSION_NOT_FOUND);

            verify(courseSessionRepository).findById(sessionId);
            verify(courseMaterialRepository, never()).save(any());
        }

        @Test
        @DisplayName("Should throw CustomException when session belongs to different course")
        void shouldThrowExceptionWhenSessionBelongsToDifferentCourse() {
            // Given
            Long courseId = 1L;
            Long sessionId = 1L;
            validRequest.setCourseSessionId(sessionId);

            Course differentCourse = new Course();
            differentCourse.setId(2L);

            CoursePhase phaseWithDifferentCourse = new CoursePhase();
            phaseWithDifferentCourse.setCourse(differentCourse);

            CourseSession sessionWithDifferentCourse = new CourseSession();
            sessionWithDifferentCourse.setId(1L);
            sessionWithDifferentCourse.setPhase(phaseWithDifferentCourse);

            when(courseRepository.findById(courseId)).thenReturn(Optional.of(testCourse));
            when(courseSessionRepository.findById(sessionId)).thenReturn(Optional.of(sessionWithDifferentCourse));

            // When & Then
            assertThatThrownBy(() -> courseMaterialService.uploadMaterial(courseId, validRequest))
                    .isInstanceOf(CustomException.class)
                    .hasFieldOrPropertyWithValue("errorCode", ErrorCode.INVALID_INPUT);

            verify(courseMaterialRepository, never()).save(any());
        }

        @Test
        @DisplayName("Should throw CustomException when title is null")
        void shouldThrowExceptionWhenTitleNull() {
            // Given
            Long courseId = 1L;
            validRequest.setTitle(null);

            when(courseRepository.findById(courseId)).thenReturn(Optional.of(testCourse));

            // When & Then
            assertThatThrownBy(() -> courseMaterialService.uploadMaterial(courseId, validRequest))
                    .isInstanceOf(CustomException.class)
                    .hasFieldOrPropertyWithValue("errorCode", ErrorCode.INVALID_INPUT);

            verify(courseMaterialRepository, never()).save(any());
        }

        @Test
        @DisplayName("Should throw CustomException when title is empty or whitespace")
        void shouldThrowExceptionWhenTitleEmpty() {
            // Given
            Long courseId = 1L;
            when(courseRepository.findById(courseId)).thenReturn(Optional.of(testCourse));

            // Test empty string
            validRequest.setTitle("");
            assertThatThrownBy(() -> courseMaterialService.uploadMaterial(courseId, validRequest))
                    .isInstanceOf(CustomException.class)
                    .hasFieldOrPropertyWithValue("errorCode", ErrorCode.INVALID_INPUT);

            // Test whitespace only
            validRequest.setTitle("   ");
            assertThatThrownBy(() -> courseMaterialService.uploadMaterial(courseId, validRequest))
                    .isInstanceOf(CustomException.class)
                    .hasFieldOrPropertyWithValue("errorCode", ErrorCode.INVALID_INPUT);

            verify(courseMaterialRepository, never()).save(any());
        }

        @Test
        @DisplayName("Should throw CustomException when file is null")
        void shouldThrowExceptionWhenFileNull() {
            // Given
            Long courseId = 1L;
            validRequest.setFile(null);

            when(courseRepository.findById(courseId)).thenReturn(Optional.of(testCourse));

            // When & Then
            assertThatThrownBy(() -> courseMaterialService.uploadMaterial(courseId, validRequest))
                    .isInstanceOf(CustomException.class)
                    .hasFieldOrPropertyWithValue("errorCode", ErrorCode.INVALID_INPUT);

            verify(courseMaterialRepository, never()).save(any());
        }

        @Test
        @DisplayName("Should throw CustomException when file is empty")
        void shouldThrowExceptionWhenFileEmpty() {
            // Given
            Long courseId = 1L;
            when(mockFile.isEmpty()).thenReturn(true);
            when(courseRepository.findById(courseId)).thenReturn(Optional.of(testCourse));

            // When & Then
            assertThatThrownBy(() -> courseMaterialService.uploadMaterial(courseId, validRequest))
                    .isInstanceOf(CustomException.class)
                    .hasFieldOrPropertyWithValue("errorCode", ErrorCode.INVALID_INPUT);

            verify(courseMaterialRepository, never()).save(any());
        }
    }

    @Nested
    @DisplayName("deleteMaterial Tests")
    class DeleteMaterialTests {

        @Test
        @DisplayName("Should delete material successfully")
        void shouldDeleteMaterialSuccessfully() {
            // Given
            Long courseId = 1L;
            Long materialId = 1L;
            when(courseMaterialRepository.findByIdAndCourseId(materialId, courseId))
                    .thenReturn(Optional.of(testMaterial));

            // When
            courseMaterialService.deleteMaterial(courseId, materialId);

            // Then
            verify(courseMaterialRepository).findByIdAndCourseId(materialId, courseId);
            verify(courseMaterialRepository).delete(testMaterial);
        }

        @Test
        @DisplayName("Should throw CustomException when material not found")
        void shouldThrowExceptionWhenMaterialNotFound() {
            // Given
            Long courseId = 1L;
            Long materialId = 999L;
            when(courseMaterialRepository.findByIdAndCourseId(materialId, courseId))
                    .thenReturn(Optional.empty());

            // When & Then
            assertThatThrownBy(() -> courseMaterialService.deleteMaterial(courseId, materialId))
                    .isInstanceOf(CustomException.class)
                    .hasFieldOrPropertyWithValue("errorCode", ErrorCode.COURSE_MATERIAL_NOT_FOUND);

            verify(courseMaterialRepository).findByIdAndCourseId(materialId, courseId);
            verify(courseMaterialRepository, never()).delete(any());
        }

        @Test
        @DisplayName("Should throw CustomException when material belongs to different course")
        void shouldThrowExceptionWhenMaterialBelongsToDifferentCourse() {
            // Given
            Long courseId = 1L;
            Long wrongCourseId = 2L;
            Long materialId = 1L;
            when(courseMaterialRepository.findByIdAndCourseId(materialId, wrongCourseId))
                    .thenReturn(Optional.empty());

            // When & Then
            assertThatThrownBy(() -> courseMaterialService.deleteMaterial(wrongCourseId, materialId))
                    .isInstanceOf(CustomException.class)
                    .hasFieldOrPropertyWithValue("errorCode", ErrorCode.COURSE_MATERIAL_NOT_FOUND);

            verify(courseMaterialRepository).findByIdAndCourseId(materialId, wrongCourseId);
            verify(courseMaterialRepository, never()).delete(any());
        }
    }
}

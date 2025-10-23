package org.fyp.emssep490be.services.clo.impl;

import org.fyp.emssep490be.dtos.clo.CloDTO;
import org.fyp.emssep490be.dtos.clo.CreateCloRequestDTO;
import org.fyp.emssep490be.dtos.clo.MappingRequestDTO;
import org.fyp.emssep490be.dtos.plo.PloDTO;
import org.fyp.emssep490be.entities.*;
import org.fyp.emssep490be.entities.ids.CourseSessionCloMappingId;
import org.fyp.emssep490be.entities.ids.PloCloMappingId;
import org.fyp.emssep490be.exceptions.CustomException;
import org.fyp.emssep490be.exceptions.ErrorCode;
import org.fyp.emssep490be.repositories.*;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.OffsetDateTime;
import java.util.*;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

/**
 * Unit tests for CloServiceImpl
 * Tests all 5 methods including CRITICAL cross-entity validations
 */
@ExtendWith(MockitoExtension.class)
@DisplayName("CloServiceImpl Tests")
class CloServiceImplTest {

    @Mock
    private CloRepository cloRepository;

    @Mock
    private CourseRepository courseRepository;

    @Mock
    private PloRepository ploRepository;

    @Mock
    private CourseSessionRepository courseSessionRepository;

    @Mock
    private PloCloMappingRepository ploCloMappingRepository;

    @Mock
    private CourseSessionCloMappingRepository courseSessionCloMappingRepository;

    @InjectMocks
    private CloServiceImpl cloService;

    private Subject testSubject;
    private Course testCourse;
    private Clo testClo;
    private Plo testPlo;
    private CoursePhase testPhase;
    private CourseSession testSession;

    @BeforeEach
    void setUp() {
        // Setup test subject
        testSubject = new Subject();
        testSubject.setId(1L);
        testSubject.setCode("MATH");

        // Setup test course
        testCourse = new Course();
        testCourse.setId(1L);
        testCourse.setSubject(testSubject);
        testCourse.setCode("MATH-101");

        // Setup test CLO
        testClo = new Clo();
        testClo.setId(1L);
        testClo.setCourse(testCourse);
        testClo.setCode("CLO-1");
        testClo.setDescription("Understand basic concepts");

        // Setup test PLO
        testPlo = new Plo();
        testPlo.setId(1L);
        testPlo.setSubject(testSubject);
        testPlo.setCode("PLO-1");
        testPlo.setDescription("Apply mathematical concepts");

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
    }

    @Nested
    @DisplayName("getClosByCourse Tests")
    class GetClosByCourseTests {

        @Test
        @DisplayName("Should return list of CLOs with mapped PLOs and sessions count")
        void shouldReturnClosWithMappedPlosAndSessionsCount() {
            // Given
            Long courseId = 1L;
            Clo clo2 = new Clo();
            clo2.setId(2L);
            clo2.setCourse(testCourse);
            clo2.setCode("CLO-2");
            clo2.setDescription("Analyze problems");

            List<Clo> clos = Arrays.asList(testClo, clo2);

            PloCloMapping mapping1 = new PloCloMapping();
            mapping1.setPlo(testPlo);
            mapping1.setClo(testClo);
            mapping1.setStatus("active");

            when(courseRepository.existsById(courseId)).thenReturn(true);
            when(cloRepository.findByCourseId(courseId)).thenReturn(clos);
            when(ploCloMappingRepository.findByCloId(1L)).thenReturn(Collections.singletonList(mapping1));
            when(ploCloMappingRepository.findByCloId(2L)).thenReturn(Collections.emptyList());
            when(courseSessionCloMappingRepository.countByCloId(1L)).thenReturn(3L);
            when(courseSessionCloMappingRepository.countByCloId(2L)).thenReturn(5L);

            // When
            List<CloDTO> result = cloService.getClosByCourse(courseId);

            // Then
            assertThat(result).hasSize(2);
            assertThat(result.get(0).getId()).isEqualTo(1L);
            assertThat(result.get(0).getCode()).isEqualTo("CLO-1");
            assertThat(result.get(0).getMappedPlos()).hasSize(1);
            assertThat(result.get(0).getMappedPlos().get(0).getCode()).isEqualTo("PLO-1");
            assertThat(result.get(0).getMappedSessionsCount()).isEqualTo(3);
            assertThat(result.get(1).getMappedPlos()).isEmpty();
            assertThat(result.get(1).getMappedSessionsCount()).isEqualTo(5);

            verify(courseRepository).existsById(courseId);
            verify(cloRepository).findByCourseId(courseId);
            verify(ploCloMappingRepository, times(2)).findByCloId(anyLong());
            verify(courseSessionCloMappingRepository, times(2)).countByCloId(anyLong());
        }

        @Test
        @DisplayName("Should return empty list when course has no CLOs")
        void shouldReturnEmptyListWhenCourseHasNoClos() {
            // Given
            Long courseId = 1L;
            when(courseRepository.existsById(courseId)).thenReturn(true);
            when(cloRepository.findByCourseId(courseId)).thenReturn(Collections.emptyList());

            // When
            List<CloDTO> result = cloService.getClosByCourse(courseId);

            // Then
            assertThat(result).isEmpty();
            verify(courseRepository).existsById(courseId);
            verify(cloRepository).findByCourseId(courseId);
            verify(ploCloMappingRepository, never()).findByCloId(anyLong());
        }

        @Test
        @DisplayName("Should throw CustomException when course not found")
        void shouldThrowExceptionWhenCourseNotFound() {
            // Given
            Long courseId = 999L;
            when(courseRepository.existsById(courseId)).thenReturn(false);

            // When & Then
            assertThatThrownBy(() -> cloService.getClosByCourse(courseId))
                    .isInstanceOf(CustomException.class)
                    .hasFieldOrPropertyWithValue("errorCode", ErrorCode.COURSE_NOT_FOUND);

            verify(courseRepository).existsById(courseId);
            verify(cloRepository, never()).findByCourseId(anyLong());
        }
    }

    @Nested
    @DisplayName("createClo Tests")
    class CreateCloTests {

        private CreateCloRequestDTO validRequest;

        @BeforeEach
        void setUp() {
            validRequest = new CreateCloRequestDTO();
            validRequest.setCode("CLO-NEW");
            validRequest.setDescription("New CLO description");
        }

        @Test
        @DisplayName("Should create CLO successfully with valid data")
        void shouldCreateCloSuccessfully() {
            // Given
            Long courseId = 1L;
            when(courseRepository.findById(courseId)).thenReturn(Optional.of(testCourse));
            when(cloRepository.existsByCodeAndCourseId(validRequest.getCode(), courseId)).thenReturn(false);
            when(cloRepository.save(any(Clo.class))).thenAnswer(invocation -> {
                Clo savedClo = invocation.getArgument(0);
                savedClo.setId(10L);
                return savedClo;
            });
            when(ploCloMappingRepository.findByCloId(10L)).thenReturn(Collections.emptyList());
            when(courseSessionCloMappingRepository.countByCloId(10L)).thenReturn(0L);

            // When
            CloDTO result = cloService.createClo(courseId, validRequest);

            // Then
            assertThat(result).isNotNull();
            assertThat(result.getId()).isEqualTo(10L);
            assertThat(result.getCode()).isEqualTo("CLO-NEW");
            assertThat(result.getDescription()).isEqualTo("New CLO description");
            assertThat(result.getCourseId()).isEqualTo(courseId);
            assertThat(result.getMappedPlos()).isEmpty();
            assertThat(result.getMappedSessionsCount()).isZero();

            verify(courseRepository).findById(courseId);
            verify(cloRepository).existsByCodeAndCourseId(validRequest.getCode(), courseId);
            verify(cloRepository).save(any(Clo.class));
        }

        @Test
        @DisplayName("Should throw CustomException when course not found")
        void shouldThrowExceptionWhenCourseNotFound() {
            // Given
            Long courseId = 999L;
            when(courseRepository.findById(courseId)).thenReturn(Optional.empty());

            // When & Then
            assertThatThrownBy(() -> cloService.createClo(courseId, validRequest))
                    .isInstanceOf(CustomException.class)
                    .hasFieldOrPropertyWithValue("errorCode", ErrorCode.COURSE_NOT_FOUND);

            verify(courseRepository).findById(courseId);
            verify(cloRepository, never()).save(any());
        }

        @Test
        @DisplayName("Should throw CustomException when CLO code already exists for course")
        void shouldThrowExceptionWhenCodeDuplicate() {
            // Given
            Long courseId = 1L;
            when(courseRepository.findById(courseId)).thenReturn(Optional.of(testCourse));
            when(cloRepository.existsByCodeAndCourseId(validRequest.getCode(), courseId)).thenReturn(true);

            // When & Then
            assertThatThrownBy(() -> cloService.createClo(courseId, validRequest))
                    .isInstanceOf(CustomException.class)
                    .hasFieldOrPropertyWithValue("errorCode", ErrorCode.CLO_CODE_DUPLICATE);

            verify(cloRepository, never()).save(any());
        }

        @Test
        @DisplayName("Should throw CustomException when code format is invalid")
        void shouldThrowExceptionWhenCodeFormatInvalid() {
            // Given
            Long courseId = 1L;
            validRequest.setCode("clo-lowercase");
            when(courseRepository.findById(courseId)).thenReturn(Optional.of(testCourse));
            when(cloRepository.existsByCodeAndCourseId(validRequest.getCode(), courseId)).thenReturn(false);

            // When & Then
            assertThatThrownBy(() -> cloService.createClo(courseId, validRequest))
                    .isInstanceOf(CustomException.class)
                    .hasFieldOrPropertyWithValue("errorCode", ErrorCode.INVALID_INPUT);

            verify(cloRepository, never()).save(any());
        }

        @Test
        @DisplayName("Should throw CustomException when description is empty")
        void shouldThrowExceptionWhenDescriptionEmpty() {
            // Given
            Long courseId = 1L;
            validRequest.setDescription("   ");
            when(courseRepository.findById(courseId)).thenReturn(Optional.of(testCourse));
            when(cloRepository.existsByCodeAndCourseId(validRequest.getCode(), courseId)).thenReturn(false);

            // When & Then
            assertThatThrownBy(() -> cloService.createClo(courseId, validRequest))
                    .isInstanceOf(CustomException.class)
                    .hasFieldOrPropertyWithValue("errorCode", ErrorCode.INVALID_INPUT);

            verify(cloRepository, never()).save(any());
        }
    }

    @Nested
    @DisplayName("mapPloToClo Tests - CRITICAL Validations")
    class MapPloToCloTests {

        private MappingRequestDTO validRequest;

        @BeforeEach
        void setUp() {
            validRequest = new MappingRequestDTO();
            validRequest.setStatus("active");
        }

        @Test
        @DisplayName("Should create PLO-CLO mapping successfully when same subject")
        void shouldCreateMappingSuccessfullyWhenSameSubject() {
            // Given
            Long ploId = 1L;
            Long cloId = 1L;
            when(ploRepository.findById(ploId)).thenReturn(Optional.of(testPlo));
            when(cloRepository.findById(cloId)).thenReturn(Optional.of(testClo));
            when(ploCloMappingRepository.existsByPloIdAndCloId(ploId, cloId)).thenReturn(false);
            when(ploCloMappingRepository.save(any(PloCloMapping.class))).thenAnswer(invocation -> invocation.getArgument(0));

            // When
            Map<String, Object> result = cloService.mapPloToClo(ploId, cloId, validRequest);

            // Then
            assertThat(result).isNotNull();
            assertThat(result.get("ploId")).isEqualTo(ploId);
            assertThat(result.get("ploCode")).isEqualTo("PLO-1");
            assertThat(result.get("cloId")).isEqualTo(cloId);
            assertThat(result.get("cloCode")).isEqualTo("CLO-1");
            assertThat(result.get("status")).isEqualTo("active");

            verify(ploRepository).findById(ploId);
            verify(cloRepository).findById(cloId);
            verify(ploCloMappingRepository).existsByPloIdAndCloId(ploId, cloId);
            verify(ploCloMappingRepository).save(any(PloCloMapping.class));
        }

        @Test
        @DisplayName("CRITICAL: Should throw CustomException when PLO and CLO belong to different subjects")
        void shouldThrowExceptionWhenDifferentSubjects() {
            // Given
            Long ploId = 1L;
            Long cloId = 1L;

            // Create a different subject for CLO
            Subject differentSubject = new Subject();
            differentSubject.setId(2L);
            differentSubject.setCode("PHYSICS");

            Course courseWithDifferentSubject = new Course();
            courseWithDifferentSubject.setId(2L);
            courseWithDifferentSubject.setSubject(differentSubject);

            Clo cloWithDifferentSubject = new Clo();
            cloWithDifferentSubject.setId(1L);
            cloWithDifferentSubject.setCourse(courseWithDifferentSubject);
            cloWithDifferentSubject.setCode("CLO-1");

            when(ploRepository.findById(ploId)).thenReturn(Optional.of(testPlo));
            when(cloRepository.findById(cloId)).thenReturn(Optional.of(cloWithDifferentSubject));

            // When & Then
            assertThatThrownBy(() -> cloService.mapPloToClo(ploId, cloId, validRequest))
                    .isInstanceOf(CustomException.class)
                    .hasFieldOrPropertyWithValue("errorCode", ErrorCode.PLO_CLO_SUBJECT_MISMATCH);

            verify(ploRepository).findById(ploId);
            verify(cloRepository).findById(cloId);
            verify(ploCloMappingRepository, never()).save(any());
        }

        @Test
        @DisplayName("Should throw CustomException when PLO not found")
        void shouldThrowExceptionWhenPloNotFound() {
            // Given
            Long ploId = 999L;
            Long cloId = 1L;
            when(ploRepository.findById(ploId)).thenReturn(Optional.empty());

            // When & Then
            assertThatThrownBy(() -> cloService.mapPloToClo(ploId, cloId, validRequest))
                    .isInstanceOf(CustomException.class)
                    .hasFieldOrPropertyWithValue("errorCode", ErrorCode.PLO_NOT_FOUND);

            verify(ploRepository).findById(ploId);
            verify(cloRepository, never()).findById(anyLong());
            verify(ploCloMappingRepository, never()).save(any());
        }

        @Test
        @DisplayName("Should throw CustomException when CLO not found")
        void shouldThrowExceptionWhenCloNotFound() {
            // Given
            Long ploId = 1L;
            Long cloId = 999L;
            when(ploRepository.findById(ploId)).thenReturn(Optional.of(testPlo));
            when(cloRepository.findById(cloId)).thenReturn(Optional.empty());

            // When & Then
            assertThatThrownBy(() -> cloService.mapPloToClo(ploId, cloId, validRequest))
                    .isInstanceOf(CustomException.class)
                    .hasFieldOrPropertyWithValue("errorCode", ErrorCode.CLO_NOT_FOUND);

            verify(ploRepository).findById(ploId);
            verify(cloRepository).findById(cloId);
            verify(ploCloMappingRepository, never()).save(any());
        }

        @Test
        @DisplayName("Should throw CustomException when mapping already exists")
        void shouldThrowExceptionWhenMappingAlreadyExists() {
            // Given
            Long ploId = 1L;
            Long cloId = 1L;
            when(ploRepository.findById(ploId)).thenReturn(Optional.of(testPlo));
            when(cloRepository.findById(cloId)).thenReturn(Optional.of(testClo));
            when(ploCloMappingRepository.existsByPloIdAndCloId(ploId, cloId)).thenReturn(true);

            // When & Then
            assertThatThrownBy(() -> cloService.mapPloToClo(ploId, cloId, validRequest))
                    .isInstanceOf(CustomException.class)
                    .hasFieldOrPropertyWithValue("errorCode", ErrorCode.PLO_CLO_MAPPING_ALREADY_EXISTS);

            verify(ploCloMappingRepository, never()).save(any());
        }

        @Test
        @DisplayName("Should use default status 'active' when status not provided")
        void shouldUseDefaultStatusWhenNotProvided() {
            // Given
            Long ploId = 1L;
            Long cloId = 1L;
            MappingRequestDTO requestWithoutStatus = new MappingRequestDTO();
            requestWithoutStatus.setStatus(null);

            when(ploRepository.findById(ploId)).thenReturn(Optional.of(testPlo));
            when(cloRepository.findById(cloId)).thenReturn(Optional.of(testClo));
            when(ploCloMappingRepository.existsByPloIdAndCloId(ploId, cloId)).thenReturn(false);
            when(ploCloMappingRepository.save(any(PloCloMapping.class))).thenAnswer(invocation -> invocation.getArgument(0));

            // When
            Map<String, Object> result = cloService.mapPloToClo(ploId, cloId, requestWithoutStatus);

            // Then
            assertThat(result.get("status")).isEqualTo("active");
            verify(ploCloMappingRepository).save(any(PloCloMapping.class));
        }
    }

    @Nested
    @DisplayName("mapCloToSession Tests - CRITICAL Validations")
    class MapCloToSessionTests {

        private MappingRequestDTO validRequest;

        @BeforeEach
        void setUp() {
            validRequest = new MappingRequestDTO();
            validRequest.setStatus("active");
        }

        @Test
        @DisplayName("Should create CLO-Session mapping successfully when same course")
        void shouldCreateMappingSuccessfullyWhenSameCourse() {
            // Given
            Long sessionId = 1L;
            Long cloId = 1L;
            when(courseSessionRepository.findById(sessionId)).thenReturn(Optional.of(testSession));
            when(cloRepository.findById(cloId)).thenReturn(Optional.of(testClo));
            when(courseSessionCloMappingRepository.existsByCourseSessionIdAndCloId(sessionId, cloId)).thenReturn(false);
            when(courseSessionCloMappingRepository.save(any(CourseSessionCloMapping.class))).thenAnswer(invocation -> invocation.getArgument(0));

            // When
            Map<String, Object> result = cloService.mapCloToSession(sessionId, cloId, validRequest);

            // Then
            assertThat(result).isNotNull();
            assertThat(result.get("courseSessionId")).isEqualTo(sessionId);
            assertThat(result.get("sessionSequence")).isEqualTo(1);
            assertThat(result.get("cloId")).isEqualTo(cloId);
            assertThat(result.get("cloCode")).isEqualTo("CLO-1");
            assertThat(result.get("status")).isEqualTo("active");

            verify(courseSessionRepository).findById(sessionId);
            verify(cloRepository).findById(cloId);
            verify(courseSessionCloMappingRepository).save(any(CourseSessionCloMapping.class));
        }

        @Test
        @DisplayName("CRITICAL: Should throw CustomException when CLO and Session belong to different courses")
        void shouldThrowExceptionWhenDifferentCourses() {
            // Given
            Long sessionId = 1L;
            Long cloId = 1L;

            // Create a different course for CLO
            Course differentCourse = new Course();
            differentCourse.setId(2L);
            differentCourse.setSubject(testSubject);

            Clo cloWithDifferentCourse = new Clo();
            cloWithDifferentCourse.setId(1L);
            cloWithDifferentCourse.setCourse(differentCourse);
            cloWithDifferentCourse.setCode("CLO-1");

            when(courseSessionRepository.findById(sessionId)).thenReturn(Optional.of(testSession));
            when(cloRepository.findById(cloId)).thenReturn(Optional.of(cloWithDifferentCourse));

            // When & Then
            assertThatThrownBy(() -> cloService.mapCloToSession(sessionId, cloId, validRequest))
                    .isInstanceOf(CustomException.class)
                    .hasFieldOrPropertyWithValue("errorCode", ErrorCode.CLO_SESSION_COURSE_MISMATCH);

            verify(courseSessionRepository).findById(sessionId);
            verify(cloRepository).findById(cloId);
            verify(courseSessionCloMappingRepository, never()).save(any());
        }

        @Test
        @DisplayName("Should throw CustomException when CourseSession not found")
        void shouldThrowExceptionWhenSessionNotFound() {
            // Given
            Long sessionId = 999L;
            Long cloId = 1L;
            when(courseSessionRepository.findById(sessionId)).thenReturn(Optional.empty());

            // When & Then
            assertThatThrownBy(() -> cloService.mapCloToSession(sessionId, cloId, validRequest))
                    .isInstanceOf(CustomException.class)
                    .hasFieldOrPropertyWithValue("errorCode", ErrorCode.COURSE_SESSION_NOT_FOUND);

            verify(courseSessionRepository).findById(sessionId);
            verify(cloRepository, never()).findById(anyLong());
            verify(courseSessionCloMappingRepository, never()).save(any());
        }

        @Test
        @DisplayName("Should throw CustomException when CLO not found")
        void shouldThrowExceptionWhenCloNotFound() {
            // Given
            Long sessionId = 1L;
            Long cloId = 999L;
            when(courseSessionRepository.findById(sessionId)).thenReturn(Optional.of(testSession));
            when(cloRepository.findById(cloId)).thenReturn(Optional.empty());

            // When & Then
            assertThatThrownBy(() -> cloService.mapCloToSession(sessionId, cloId, validRequest))
                    .isInstanceOf(CustomException.class)
                    .hasFieldOrPropertyWithValue("errorCode", ErrorCode.CLO_NOT_FOUND);

            verify(courseSessionRepository).findById(sessionId);
            verify(cloRepository).findById(cloId);
            verify(courseSessionCloMappingRepository, never()).save(any());
        }

        @Test
        @DisplayName("Should throw CustomException when mapping already exists")
        void shouldThrowExceptionWhenMappingAlreadyExists() {
            // Given
            Long sessionId = 1L;
            Long cloId = 1L;
            when(courseSessionRepository.findById(sessionId)).thenReturn(Optional.of(testSession));
            when(cloRepository.findById(cloId)).thenReturn(Optional.of(testClo));
            when(courseSessionCloMappingRepository.existsByCourseSessionIdAndCloId(sessionId, cloId)).thenReturn(true);

            // When & Then
            assertThatThrownBy(() -> cloService.mapCloToSession(sessionId, cloId, validRequest))
                    .isInstanceOf(CustomException.class)
                    .hasFieldOrPropertyWithValue("errorCode", ErrorCode.CLO_SESSION_MAPPING_ALREADY_EXISTS);

            verify(courseSessionCloMappingRepository, never()).save(any());
        }
    }

    @Nested
    @DisplayName("deleteClo Tests")
    class DeleteCloTests {

        @Test
        @DisplayName("Should delete CLO successfully when no mappings exist")
        void shouldDeleteCloSuccessfully() {
            // Given
            Long courseId = 1L;
            Long cloId = 1L;
            when(cloRepository.findByIdAndCourseId(cloId, courseId)).thenReturn(Optional.of(testClo));
            when(ploCloMappingRepository.existsByCloId(cloId)).thenReturn(false);
            when(courseSessionCloMappingRepository.existsByCloId(cloId)).thenReturn(false);

            // When
            cloService.deleteClo(courseId, cloId);

            // Then
            verify(cloRepository).findByIdAndCourseId(cloId, courseId);
            verify(ploCloMappingRepository).existsByCloId(cloId);
            verify(courseSessionCloMappingRepository).existsByCloId(cloId);
            verify(cloRepository).delete(testClo);
        }

        @Test
        @DisplayName("Should throw CustomException when CLO not found")
        void shouldThrowExceptionWhenCloNotFound() {
            // Given
            Long courseId = 1L;
            Long cloId = 999L;
            when(cloRepository.findByIdAndCourseId(cloId, courseId)).thenReturn(Optional.empty());

            // When & Then
            assertThatThrownBy(() -> cloService.deleteClo(courseId, cloId))
                    .isInstanceOf(CustomException.class)
                    .hasFieldOrPropertyWithValue("errorCode", ErrorCode.CLO_NOT_FOUND);

            verify(cloRepository).findByIdAndCourseId(cloId, courseId);
            verify(cloRepository, never()).delete(any());
        }

        @Test
        @DisplayName("Should throw CustomException when CLO has existing PLO mappings")
        void shouldThrowExceptionWhenCloHasPloMappings() {
            // Given
            Long courseId = 1L;
            Long cloId = 1L;
            when(cloRepository.findByIdAndCourseId(cloId, courseId)).thenReturn(Optional.of(testClo));
            when(ploCloMappingRepository.existsByCloId(cloId)).thenReturn(true);

            // When & Then
            assertThatThrownBy(() -> cloService.deleteClo(courseId, cloId))
                    .isInstanceOf(CustomException.class)
                    .hasFieldOrPropertyWithValue("errorCode", ErrorCode.CLO_HAS_MAPPINGS);

            verify(cloRepository).findByIdAndCourseId(cloId, courseId);
            verify(ploCloMappingRepository).existsByCloId(cloId);
            verify(cloRepository, never()).delete(any());
        }

        @Test
        @DisplayName("Should throw CustomException when CLO has existing session mappings")
        void shouldThrowExceptionWhenCloHasSessionMappings() {
            // Given
            Long courseId = 1L;
            Long cloId = 1L;
            when(cloRepository.findByIdAndCourseId(cloId, courseId)).thenReturn(Optional.of(testClo));
            when(ploCloMappingRepository.existsByCloId(cloId)).thenReturn(false);
            when(courseSessionCloMappingRepository.existsByCloId(cloId)).thenReturn(true);

            // When & Then
            assertThatThrownBy(() -> cloService.deleteClo(courseId, cloId))
                    .isInstanceOf(CustomException.class)
                    .hasFieldOrPropertyWithValue("errorCode", ErrorCode.CLO_HAS_MAPPINGS);

            verify(cloRepository).findByIdAndCourseId(cloId, courseId);
            verify(ploCloMappingRepository).existsByCloId(cloId);
            verify(courseSessionCloMappingRepository).existsByCloId(cloId);
            verify(cloRepository, never()).delete(any());
        }

        @Test
        @DisplayName("Should throw CustomException when CLO belongs to different course")
        void shouldThrowExceptionWhenCloBelongsToDifferentCourse() {
            // Given
            Long courseId = 1L;
            Long wrongCourseId = 2L;
            Long cloId = 1L;
            when(cloRepository.findByIdAndCourseId(cloId, wrongCourseId)).thenReturn(Optional.empty());

            // When & Then
            assertThatThrownBy(() -> cloService.deleteClo(wrongCourseId, cloId))
                    .isInstanceOf(CustomException.class)
                    .hasFieldOrPropertyWithValue("errorCode", ErrorCode.CLO_NOT_FOUND);

            verify(cloRepository).findByIdAndCourseId(cloId, wrongCourseId);
            verify(cloRepository, never()).delete(any());
        }
    }
}

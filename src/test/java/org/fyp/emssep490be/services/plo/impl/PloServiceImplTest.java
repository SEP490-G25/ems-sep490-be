package org.fyp.emssep490be.services.plo.impl;

import org.fyp.emssep490be.dtos.plo.CreatePloRequestDTO;
import org.fyp.emssep490be.dtos.plo.PloDTO;
import org.fyp.emssep490be.entities.Plo;
import org.fyp.emssep490be.entities.Subject;
import org.fyp.emssep490be.exceptions.CustomException;
import org.fyp.emssep490be.exceptions.ErrorCode;
import org.fyp.emssep490be.repositories.PloCloMappingRepository;
import org.fyp.emssep490be.repositories.PloRepository;
import org.fyp.emssep490be.repositories.SubjectRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.OffsetDateTime;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

/**
 * Unit tests for PloServiceImpl
 * Tests all business logic, validations, and error handling
 */
@ExtendWith(MockitoExtension.class)
@DisplayName("PloServiceImpl Tests")
class PloServiceImplTest {

    @Mock
    private PloRepository ploRepository;

    @Mock
    private SubjectRepository subjectRepository;

    @Mock
    private PloCloMappingRepository ploCloMappingRepository;

    @InjectMocks
    private PloServiceImpl ploService;

    private Subject testSubject;
    private Plo testPlo;

    @BeforeEach
    void setUp() {
        // Setup test subject
        testSubject = new Subject();
        testSubject.setId(1L);
        testSubject.setCode("MATH");
        testSubject.setName("Mathematics");

        // Setup test PLO
        testPlo = new Plo();
        testPlo.setId(1L);
        testPlo.setSubject(testSubject);
        testPlo.setCode("PLO-1");
        testPlo.setDescription("Apply mathematical concepts");
        testPlo.setCreatedAt(OffsetDateTime.now());
        testPlo.setUpdatedAt(OffsetDateTime.now());
    }

    @Nested
    @DisplayName("getPlosBySubject Tests")
    class GetPlosBySubjectTests {

        @Test
        @DisplayName("Should return list of PLOs when subject exists and has PLOs")
        void shouldReturnPlosWhenSubjectExistsAndHasPlos() {
            // Given
            Long subjectId = 1L;
            Plo plo2 = new Plo();
            plo2.setId(2L);
            plo2.setSubject(testSubject);
            plo2.setCode("PLO-2");
            plo2.setDescription("Analyze mathematical problems");

            List<Plo> plos = Arrays.asList(testPlo, plo2);

            when(subjectRepository.existsById(subjectId)).thenReturn(true);
            when(ploRepository.findBySubjectId(subjectId)).thenReturn(plos);
            when(ploCloMappingRepository.countByPloId(1L)).thenReturn(3L);
            when(ploCloMappingRepository.countByPloId(2L)).thenReturn(5L);

            // When
            List<PloDTO> result = ploService.getPlosBySubject(subjectId);

            // Then
            assertThat(result).hasSize(2);
            assertThat(result.get(0).getId()).isEqualTo(1L);
            assertThat(result.get(0).getCode()).isEqualTo("PLO-1");
            assertThat(result.get(0).getMappedClosCount()).isEqualTo(3);
            assertThat(result.get(1).getId()).isEqualTo(2L);
            assertThat(result.get(1).getMappedClosCount()).isEqualTo(5);

            verify(subjectRepository).existsById(subjectId);
            verify(ploRepository).findBySubjectId(subjectId);
            verify(ploCloMappingRepository, times(2)).countByPloId(anyLong());
        }

        @Test
        @DisplayName("Should return empty list when subject has no PLOs")
        void shouldReturnEmptyListWhenSubjectHasNoPlos() {
            // Given
            Long subjectId = 1L;
            when(subjectRepository.existsById(subjectId)).thenReturn(true);
            when(ploRepository.findBySubjectId(subjectId)).thenReturn(Collections.emptyList());

            // When
            List<PloDTO> result = ploService.getPlosBySubject(subjectId);

            // Then
            assertThat(result).isEmpty();
            verify(subjectRepository).existsById(subjectId);
            verify(ploRepository).findBySubjectId(subjectId);
            verify(ploCloMappingRepository, never()).countByPloId(anyLong());
        }

        @Test
        @DisplayName("Should throw CustomException when subject not found")
        void shouldThrowExceptionWhenSubjectNotFound() {
            // Given
            Long subjectId = 999L;
            when(subjectRepository.existsById(subjectId)).thenReturn(false);

            // When & Then
            assertThatThrownBy(() -> ploService.getPlosBySubject(subjectId))
                    .isInstanceOf(CustomException.class)
                    .hasFieldOrPropertyWithValue("errorCode", ErrorCode.SUBJECT_NOT_FOUND);

            verify(subjectRepository).existsById(subjectId);
            verify(ploRepository, never()).findBySubjectId(anyLong());
        }

        @Test
        @DisplayName("Should return PLO with zero mapped CLOs count")
        void shouldReturnPloWithZeroMappedClosCount() {
            // Given
            Long subjectId = 1L;
            when(subjectRepository.existsById(subjectId)).thenReturn(true);
            when(ploRepository.findBySubjectId(subjectId)).thenReturn(Collections.singletonList(testPlo));
            when(ploCloMappingRepository.countByPloId(1L)).thenReturn(0L);

            // When
            List<PloDTO> result = ploService.getPlosBySubject(subjectId);

            // Then
            assertThat(result).hasSize(1);
            assertThat(result.get(0).getMappedClosCount()).isZero();
        }
    }

    @Nested
    @DisplayName("createPlo Tests")
    class CreatePloTests {

        private CreatePloRequestDTO validRequest;

        @BeforeEach
        void setUp() {
            validRequest = new CreatePloRequestDTO();
            validRequest.setCode("PLO-NEW");
            validRequest.setDescription("New PLO description");
        }

        @Test
        @DisplayName("Should create PLO successfully with valid data")
        void shouldCreatePloSuccessfully() {
            // Given
            Long subjectId = 1L;
            when(subjectRepository.findById(subjectId)).thenReturn(Optional.of(testSubject));
            when(ploRepository.existsByCodeAndSubjectId(validRequest.getCode(), subjectId)).thenReturn(false);
            when(ploRepository.save(any(Plo.class))).thenAnswer(invocation -> {
                Plo savedPlo = invocation.getArgument(0);
                savedPlo.setId(10L);
                return savedPlo;
            });
            when(ploCloMappingRepository.countByPloId(10L)).thenReturn(0L);

            // When
            PloDTO result = ploService.createPlo(subjectId, validRequest);

            // Then
            assertThat(result).isNotNull();
            assertThat(result.getId()).isEqualTo(10L);
            assertThat(result.getCode()).isEqualTo("PLO-NEW");
            assertThat(result.getDescription()).isEqualTo("New PLO description");
            assertThat(result.getSubjectId()).isEqualTo(subjectId);
            assertThat(result.getMappedClosCount()).isZero();

            verify(subjectRepository).findById(subjectId);
            verify(ploRepository).existsByCodeAndSubjectId(validRequest.getCode(), subjectId);
            verify(ploRepository).save(any(Plo.class));
        }

        @Test
        @DisplayName("Should throw CustomException when subject not found")
        void shouldThrowExceptionWhenSubjectNotFoundOnCreate() {
            // Given
            Long subjectId = 999L;
            when(subjectRepository.findById(subjectId)).thenReturn(Optional.empty());

            // When & Then
            assertThatThrownBy(() -> ploService.createPlo(subjectId, validRequest))
                    .isInstanceOf(CustomException.class)
                    .hasFieldOrPropertyWithValue("errorCode", ErrorCode.SUBJECT_NOT_FOUND);

            verify(subjectRepository).findById(subjectId);
            verify(ploRepository, never()).save(any());
        }

        @Test
        @DisplayName("Should throw CustomException when PLO code already exists for subject")
        void shouldThrowExceptionWhenCodeDuplicate() {
            // Given
            Long subjectId = 1L;
            when(subjectRepository.findById(subjectId)).thenReturn(Optional.of(testSubject));
            when(ploRepository.existsByCodeAndSubjectId(validRequest.getCode(), subjectId)).thenReturn(true);

            // When & Then
            assertThatThrownBy(() -> ploService.createPlo(subjectId, validRequest))
                    .isInstanceOf(CustomException.class)
                    .hasFieldOrPropertyWithValue("errorCode", ErrorCode.PLO_CODE_DUPLICATE);

            verify(subjectRepository).findById(subjectId);
            verify(ploRepository).existsByCodeAndSubjectId(validRequest.getCode(), subjectId);
            verify(ploRepository, never()).save(any());
        }

        @Test
        @DisplayName("Should throw CustomException when code format is invalid - lowercase")
        void shouldThrowExceptionWhenCodeFormatInvalidLowercase() {
            // Given
            Long subjectId = 1L;
            validRequest.setCode("plo-lowercase");
            when(subjectRepository.findById(subjectId)).thenReturn(Optional.of(testSubject));
            when(ploRepository.existsByCodeAndSubjectId(validRequest.getCode(), subjectId)).thenReturn(false);

            // When & Then
            assertThatThrownBy(() -> ploService.createPlo(subjectId, validRequest))
                    .isInstanceOf(CustomException.class)
                    .hasFieldOrPropertyWithValue("errorCode", ErrorCode.INVALID_INPUT);

            verify(ploRepository, never()).save(any());
        }

        @Test
        @DisplayName("Should throw CustomException when code format has special characters")
        void shouldThrowExceptionWhenCodeFormatHasSpecialChars() {
            // Given
            Long subjectId = 1L;
            validRequest.setCode("PLO@INVALID!");
            when(subjectRepository.findById(subjectId)).thenReturn(Optional.of(testSubject));
            when(ploRepository.existsByCodeAndSubjectId(validRequest.getCode(), subjectId)).thenReturn(false);

            // When & Then
            assertThatThrownBy(() -> ploService.createPlo(subjectId, validRequest))
                    .isInstanceOf(CustomException.class)
                    .hasFieldOrPropertyWithValue("errorCode", ErrorCode.INVALID_INPUT);

            verify(ploRepository, never()).save(any());
        }

        @Test
        @DisplayName("Should accept valid code formats")
        void shouldAcceptValidCodeFormats() {
            // Given
            Long subjectId = 1L;
            String[] validCodes = {"PLO-1", "PLO1", "PLO-A-B-1", "OUTCOME123"};

            when(subjectRepository.findById(subjectId)).thenReturn(Optional.of(testSubject));
            when(ploRepository.existsByCodeAndSubjectId(anyString(), eq(subjectId))).thenReturn(false);
            when(ploRepository.save(any(Plo.class))).thenAnswer(invocation -> {
                Plo savedPlo = invocation.getArgument(0);
                savedPlo.setId(10L);
                return savedPlo;
            });
            when(ploCloMappingRepository.countByPloId(anyLong())).thenReturn(0L);

            // When & Then
            for (String code : validCodes) {
                validRequest.setCode(code);
                PloDTO result = ploService.createPlo(subjectId, validRequest);
                assertThat(result.getCode()).isEqualTo(code);
            }

            verify(ploRepository, times(validCodes.length)).save(any(Plo.class));
        }

        @Test
        @DisplayName("Should throw CustomException when description is null")
        void shouldThrowExceptionWhenDescriptionNull() {
            // Given
            Long subjectId = 1L;
            validRequest.setDescription(null);
            when(subjectRepository.findById(subjectId)).thenReturn(Optional.of(testSubject));
            when(ploRepository.existsByCodeAndSubjectId(validRequest.getCode(), subjectId)).thenReturn(false);

            // When & Then
            assertThatThrownBy(() -> ploService.createPlo(subjectId, validRequest))
                    .isInstanceOf(CustomException.class)
                    .hasFieldOrPropertyWithValue("errorCode", ErrorCode.INVALID_INPUT);

            verify(ploRepository, never()).save(any());
        }

        @Test
        @DisplayName("Should throw CustomException when description is empty or whitespace")
        void shouldThrowExceptionWhenDescriptionEmpty() {
            // Given
            Long subjectId = 1L;
            when(subjectRepository.findById(subjectId)).thenReturn(Optional.of(testSubject));
            when(ploRepository.existsByCodeAndSubjectId(validRequest.getCode(), subjectId)).thenReturn(false);

            // Test empty string
            validRequest.setDescription("");
            assertThatThrownBy(() -> ploService.createPlo(subjectId, validRequest))
                    .isInstanceOf(CustomException.class)
                    .hasFieldOrPropertyWithValue("errorCode", ErrorCode.INVALID_INPUT);

            // Test whitespace only
            validRequest.setDescription("   ");
            assertThatThrownBy(() -> ploService.createPlo(subjectId, validRequest))
                    .isInstanceOf(CustomException.class)
                    .hasFieldOrPropertyWithValue("errorCode", ErrorCode.INVALID_INPUT);

            verify(ploRepository, never()).save(any());
        }
    }

    @Nested
    @DisplayName("deletePlo Tests")
    class DeletePloTests {

        @Test
        @DisplayName("Should delete PLO successfully when no CLO mappings exist")
        void shouldDeletePloSuccessfully() {
            // Given
            Long subjectId = 1L;
            Long ploId = 1L;
            when(ploRepository.findByIdAndSubjectId(ploId, subjectId)).thenReturn(Optional.of(testPlo));
            when(ploCloMappingRepository.existsByPloId(ploId)).thenReturn(false);

            // When
            ploService.deletePlo(subjectId, ploId);

            // Then
            verify(ploRepository).findByIdAndSubjectId(ploId, subjectId);
            verify(ploCloMappingRepository).existsByPloId(ploId);
            verify(ploRepository).delete(testPlo);
        }

        @Test
        @DisplayName("Should throw CustomException when PLO not found")
        void shouldThrowExceptionWhenPloNotFound() {
            // Given
            Long subjectId = 1L;
            Long ploId = 999L;
            when(ploRepository.findByIdAndSubjectId(ploId, subjectId)).thenReturn(Optional.empty());

            // When & Then
            assertThatThrownBy(() -> ploService.deletePlo(subjectId, ploId))
                    .isInstanceOf(CustomException.class)
                    .hasFieldOrPropertyWithValue("errorCode", ErrorCode.PLO_NOT_FOUND);

            verify(ploRepository).findByIdAndSubjectId(ploId, subjectId);
            verify(ploRepository, never()).delete(any());
        }

        @Test
        @DisplayName("Should throw CustomException when PLO belongs to different subject")
        void shouldThrowExceptionWhenPloBelongsToDifferentSubject() {
            // Given
            Long subjectId = 1L;
            Long wrongSubjectId = 2L;
            Long ploId = 1L;
            when(ploRepository.findByIdAndSubjectId(ploId, wrongSubjectId)).thenReturn(Optional.empty());

            // When & Then
            assertThatThrownBy(() -> ploService.deletePlo(wrongSubjectId, ploId))
                    .isInstanceOf(CustomException.class)
                    .hasFieldOrPropertyWithValue("errorCode", ErrorCode.PLO_NOT_FOUND);

            verify(ploRepository).findByIdAndSubjectId(ploId, wrongSubjectId);
            verify(ploRepository, never()).delete(any());
        }

        @Test
        @DisplayName("Should throw CustomException when PLO has existing CLO mappings")
        void shouldThrowExceptionWhenPloHasMappings() {
            // Given
            Long subjectId = 1L;
            Long ploId = 1L;
            when(ploRepository.findByIdAndSubjectId(ploId, subjectId)).thenReturn(Optional.of(testPlo));
            when(ploCloMappingRepository.existsByPloId(ploId)).thenReturn(true);

            // When & Then
            assertThatThrownBy(() -> ploService.deletePlo(subjectId, ploId))
                    .isInstanceOf(CustomException.class)
                    .hasFieldOrPropertyWithValue("errorCode", ErrorCode.PLO_HAS_MAPPINGS);

            verify(ploRepository).findByIdAndSubjectId(ploId, subjectId);
            verify(ploCloMappingRepository).existsByPloId(ploId);
            verify(ploRepository, never()).delete(any());
        }
    }
}

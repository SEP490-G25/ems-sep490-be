package org.fyp.emssep490be.services.level.impl;

import org.fyp.emssep490be.dtos.level.CreateLevelRequestDTO;
import org.fyp.emssep490be.dtos.level.LevelDTO;
import org.fyp.emssep490be.dtos.level.UpdateLevelRequestDTO;
import org.fyp.emssep490be.entities.Level;
import org.fyp.emssep490be.entities.Subject;
import org.fyp.emssep490be.entities.enums.SubjectStatus;
import org.fyp.emssep490be.exceptions.CustomException;
import org.fyp.emssep490be.exceptions.ErrorCode;
import org.fyp.emssep490be.repositories.CourseRepository;
import org.fyp.emssep490be.repositories.LevelRepository;
import org.fyp.emssep490be.repositories.SubjectRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.OffsetDateTime;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

/**
 * Unit tests for LevelServiceImpl
 * Tests all CRUD operations for nested Level resource under Subject
 */
@ExtendWith(MockitoExtension.class)
class LevelServiceImplTest {

    @Mock
    private LevelRepository levelRepository;

    @Mock
    private SubjectRepository subjectRepository;

    @Mock
    private CourseRepository courseRepository;

    @InjectMocks
    private LevelServiceImpl levelService;

    private Subject testSubject;
    private Level testLevel;
    private Level testLevel2;

    @BeforeEach
    void setUp() {
        // Setup test subject
        testSubject = new Subject();
        testSubject.setId(1L);
        testSubject.setCode("ENG-101");
        testSubject.setName("English Language");
        testSubject.setStatus(SubjectStatus.ACTIVE);
        testSubject.setCreatedAt(OffsetDateTime.now());

        // Setup test level 1
        testLevel = new Level();
        testLevel.setId(1L);
        testLevel.setSubject(testSubject);
        testLevel.setCode("A1");
        testLevel.setName("Beginner A1");
        testLevel.setStandardType("CEFR");
        testLevel.setExpectedDurationHours(60);
        testLevel.setSortOrder(1);
        testLevel.setDescription("Elementary level");
        testLevel.setCreatedAt(OffsetDateTime.now());
        testLevel.setUpdatedAt(OffsetDateTime.now());

        // Setup test level 2
        testLevel2 = new Level();
        testLevel2.setId(2L);
        testLevel2.setSubject(testSubject);
        testLevel2.setCode("A2");
        testLevel2.setName("Elementary A2");
        testLevel2.setStandardType("CEFR");
        testLevel2.setExpectedDurationHours(80);
        testLevel2.setSortOrder(2);
        testLevel2.setCreatedAt(OffsetDateTime.now());
    }

    @Test
    void getLevelsBySubject_WithValidSubjectId_ShouldReturnLevelsOrderedBySortOrder() {
        // Arrange
        when(subjectRepository.existsById(1L)).thenReturn(true);
        when(levelRepository.findBySubjectIdOrderBySortOrderAsc(1L))
                .thenReturn(Arrays.asList(testLevel, testLevel2));

        // Act
        List<LevelDTO> result = levelService.getLevelsBySubject(1L);

        // Assert
        assertThat(result).isNotNull();
        assertThat(result).hasSize(2);
        assertThat(result.get(0).getCode()).isEqualTo("A1");
        assertThat(result.get(0).getSortOrder()).isEqualTo(1);
        assertThat(result.get(1).getCode()).isEqualTo("A2");
        assertThat(result.get(1).getSortOrder()).isEqualTo(2);
        verify(subjectRepository).existsById(1L);
        verify(levelRepository).findBySubjectIdOrderBySortOrderAsc(1L);
    }

    @Test
    void getLevelsBySubject_WithInvalidSubjectId_ShouldThrowException() {
        // Arrange
        when(subjectRepository.existsById(999L)).thenReturn(false);

        // Act & Assert
        assertThatThrownBy(() -> levelService.getLevelsBySubject(999L))
                .isInstanceOf(CustomException.class)
                .hasFieldOrPropertyWithValue("errorCode", ErrorCode.LEVEL_INVALID_SUBJECT);
        verify(subjectRepository).existsById(999L);
        verify(levelRepository, never()).findBySubjectIdOrderBySortOrderAsc(anyLong());
    }

    @Test
    void getLevelById_WithValidIds_ShouldReturnLevel() {
        // Arrange
        when(levelRepository.findByIdAndSubjectId(1L, 1L))
                .thenReturn(Optional.of(testLevel));

        // Act
        LevelDTO result = levelService.getLevelById(1L, 1L);

        // Assert
        assertThat(result).isNotNull();
        assertThat(result.getId()).isEqualTo(1L);
        assertThat(result.getSubjectId()).isEqualTo(1L);
        assertThat(result.getCode()).isEqualTo("A1");
        assertThat(result.getName()).isEqualTo("Beginner A1");
        verify(levelRepository).findByIdAndSubjectId(1L, 1L);
    }

    @Test
    void getLevelById_WithLevelNotBelongingToSubject_ShouldThrowException() {
        // Arrange
        when(levelRepository.findByIdAndSubjectId(1L, 999L))
                .thenReturn(Optional.empty());

        // Act & Assert
        assertThatThrownBy(() -> levelService.getLevelById(999L, 1L))
                .isInstanceOf(CustomException.class)
                .hasFieldOrPropertyWithValue("errorCode", ErrorCode.LEVEL_NOT_FOUND);
        verify(levelRepository).findByIdAndSubjectId(1L, 999L);
    }

    @Test
    void createLevel_WithValidData_ShouldCreateLevel() {
        // Arrange
        CreateLevelRequestDTO request = CreateLevelRequestDTO.builder()
                .code("B1")
                .name("Intermediate B1")
                .standardType("CEFR")
                .expectedDurationHours(100)
                .sortOrder(3)
                .description("Intermediate level")
                .build();

        when(subjectRepository.findById(1L)).thenReturn(Optional.of(testSubject));
        when(levelRepository.existsByCodeAndSubjectId("B1", 1L)).thenReturn(false);
        when(levelRepository.existsBySortOrderAndSubjectId(3, 1L)).thenReturn(false);
        when(levelRepository.save(any(Level.class))).thenReturn(testLevel);

        // Act
        LevelDTO result = levelService.createLevel(1L, request);

        // Assert
        assertThat(result).isNotNull();
        verify(subjectRepository).findById(1L);
        verify(levelRepository).existsByCodeAndSubjectId("B1", 1L);
        verify(levelRepository).existsBySortOrderAndSubjectId(3, 1L);
        verify(levelRepository).save(any(Level.class));
    }

    @Test
    void createLevel_WithInvalidSubjectId_ShouldThrowException() {
        // Arrange
        CreateLevelRequestDTO request = CreateLevelRequestDTO.builder()
                .code("B1")
                .name("Intermediate B1")
                .sortOrder(3)
                .build();

        when(subjectRepository.findById(999L)).thenReturn(Optional.empty());

        // Act & Assert
        assertThatThrownBy(() -> levelService.createLevel(999L, request))
                .isInstanceOf(CustomException.class)
                .hasFieldOrPropertyWithValue("errorCode", ErrorCode.LEVEL_INVALID_SUBJECT);
        verify(subjectRepository).findById(999L);
        verify(levelRepository, never()).save(any(Level.class));
    }

    @Test
    void createLevel_WithDuplicateCode_ShouldThrowException() {
        // Arrange
        CreateLevelRequestDTO request = CreateLevelRequestDTO.builder()
                .code("A1")
                .name("Beginner A1")
                .sortOrder(3)
                .build();

        when(subjectRepository.findById(1L)).thenReturn(Optional.of(testSubject));
        when(levelRepository.existsByCodeAndSubjectId("A1", 1L)).thenReturn(true);

        // Act & Assert
        assertThatThrownBy(() -> levelService.createLevel(1L, request))
                .isInstanceOf(CustomException.class)
                .hasFieldOrPropertyWithValue("errorCode", ErrorCode.LEVEL_CODE_DUPLICATE);
        verify(levelRepository).existsByCodeAndSubjectId("A1", 1L);
        verify(levelRepository, never()).save(any(Level.class));
    }

    @Test
    void createLevel_WithDuplicateSortOrder_ShouldThrowException() {
        // Arrange
        CreateLevelRequestDTO request = CreateLevelRequestDTO.builder()
                .code("B1")
                .name("Intermediate B1")
                .sortOrder(1)
                .build();

        when(subjectRepository.findById(1L)).thenReturn(Optional.of(testSubject));
        when(levelRepository.existsByCodeAndSubjectId("B1", 1L)).thenReturn(false);
        when(levelRepository.existsBySortOrderAndSubjectId(1, 1L)).thenReturn(true);

        // Act & Assert
        assertThatThrownBy(() -> levelService.createLevel(1L, request))
                .isInstanceOf(CustomException.class)
                .hasFieldOrPropertyWithValue("errorCode", ErrorCode.LEVEL_SORT_ORDER_DUPLICATE);
        verify(levelRepository).existsBySortOrderAndSubjectId(1, 1L);
        verify(levelRepository, never()).save(any(Level.class));
    }

    @Test
    void updateLevel_WithValidData_ShouldUpdateLevel() {
        // Arrange
        UpdateLevelRequestDTO request = UpdateLevelRequestDTO.builder()
                .name("Updated Beginner A1")
                .description("Updated description")
                .expectedDurationHours(70)
                .build();

        when(levelRepository.findByIdAndSubjectId(1L, 1L))
                .thenReturn(Optional.of(testLevel));
        when(levelRepository.save(any(Level.class))).thenReturn(testLevel);

        // Act
        LevelDTO result = levelService.updateLevel(1L, 1L, request);

        // Assert
        assertThat(result).isNotNull();
        verify(levelRepository).findByIdAndSubjectId(1L, 1L);
        verify(levelRepository).save(any(Level.class));
    }

    @Test
    void updateLevel_WithSortOrderChange_ShouldValidateUniqueness() {
        // Arrange
        UpdateLevelRequestDTO request = UpdateLevelRequestDTO.builder()
                .sortOrder(5)
                .build();

        when(levelRepository.findByIdAndSubjectId(1L, 1L))
                .thenReturn(Optional.of(testLevel));
        when(levelRepository.existsBySortOrderAndSubjectId(5, 1L)).thenReturn(false);
        when(levelRepository.save(any(Level.class))).thenReturn(testLevel);

        // Act
        LevelDTO result = levelService.updateLevel(1L, 1L, request);

        // Assert
        assertThat(result).isNotNull();
        verify(levelRepository).findByIdAndSubjectId(1L, 1L);
        verify(levelRepository).existsBySortOrderAndSubjectId(5, 1L);
        verify(levelRepository).save(any(Level.class));
    }

    @Test
    void updateLevel_WithDuplicateSortOrder_ShouldThrowException() {
        // Arrange
        UpdateLevelRequestDTO request = UpdateLevelRequestDTO.builder()
                .sortOrder(2)
                .build();

        when(levelRepository.findByIdAndSubjectId(1L, 1L))
                .thenReturn(Optional.of(testLevel));
        when(levelRepository.existsBySortOrderAndSubjectId(2, 1L)).thenReturn(true);

        // Act & Assert
        assertThatThrownBy(() -> levelService.updateLevel(1L, 1L, request))
                .isInstanceOf(CustomException.class)
                .hasFieldOrPropertyWithValue("errorCode", ErrorCode.LEVEL_SORT_ORDER_DUPLICATE);
        verify(levelRepository).existsBySortOrderAndSubjectId(2, 1L);
        verify(levelRepository, never()).save(any(Level.class));
    }

    @Test
    void deleteLevel_WithValidId_ShouldHardDelete() {
        // Arrange
        when(levelRepository.findByIdAndSubjectId(1L, 1L))
                .thenReturn(Optional.of(testLevel));
        when(courseRepository.countByLevelId(1L)).thenReturn(0L);

        // Act
        levelService.deleteLevel(1L, 1L);

        // Assert
        verify(levelRepository).findByIdAndSubjectId(1L, 1L);
        verify(courseRepository).countByLevelId(1L);
        verify(levelRepository).delete(testLevel);
    }

    @Test
    void deleteLevel_WithExistingCourses_ShouldThrowException() {
        // Arrange
        when(levelRepository.findByIdAndSubjectId(1L, 1L))
                .thenReturn(Optional.of(testLevel));
        when(courseRepository.countByLevelId(1L)).thenReturn(1L);

        // Act & Assert
        assertThatThrownBy(() -> levelService.deleteLevel(1L, 1L))
                .isInstanceOf(CustomException.class)
                .hasFieldOrPropertyWithValue("errorCode", ErrorCode.LEVEL_HAS_COURSES);
        verify(levelRepository).findByIdAndSubjectId(1L, 1L);
        verify(courseRepository).countByLevelId(1L);
        verify(levelRepository, never()).delete(any(Level.class));
    }

    @Test
    void deleteLevel_WithInvalidId_ShouldThrowException() {
        // Arrange
        when(levelRepository.findByIdAndSubjectId(999L, 1L))
                .thenReturn(Optional.empty());

        // Act & Assert
        assertThatThrownBy(() -> levelService.deleteLevel(1L, 999L))
                .isInstanceOf(CustomException.class)
                .hasFieldOrPropertyWithValue("errorCode", ErrorCode.LEVEL_NOT_FOUND);
        verify(levelRepository).findByIdAndSubjectId(999L, 1L);
        verify(levelRepository, never()).delete(any(Level.class));
    }
}

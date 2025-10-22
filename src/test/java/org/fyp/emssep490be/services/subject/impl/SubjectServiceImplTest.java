package org.fyp.emssep490be.services.subject.impl;

import org.fyp.emssep490be.dtos.common.PagedResponseDTO;
import org.fyp.emssep490be.dtos.subject.CreateSubjectRequestDTO;
import org.fyp.emssep490be.dtos.subject.SubjectDTO;
import org.fyp.emssep490be.dtos.subject.SubjectDetailDTO;
import org.fyp.emssep490be.dtos.subject.UpdateSubjectRequestDTO;
import org.fyp.emssep490be.entities.Level;
import org.fyp.emssep490be.entities.Subject;
import org.fyp.emssep490be.entities.UserAccount;
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
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;

import java.time.OffsetDateTime;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

/**
 * Unit tests for SubjectServiceImpl
 * Tests all CRUD operations and business logic validation
 */
@ExtendWith(MockitoExtension.class)
class SubjectServiceImplTest {

    @Mock
    private SubjectRepository subjectRepository;

    @Mock
    private LevelRepository levelRepository;

    @Mock
    private CourseRepository courseRepository;

    @InjectMocks
    private SubjectServiceImpl subjectService;

    private Subject testSubject;
    private UserAccount testUser;
    private Level testLevel;

    @BeforeEach
    void setUp() {
        // Setup test user
        testUser = new UserAccount();
        testUser.setId(1L);
        testUser.setEmail("test@ems.com");

        // Setup test subject
        testSubject = new Subject();
        testSubject.setId(1L);
        testSubject.setCode("ENG-101");
        testSubject.setName("English Language");
        testSubject.setDescription("Basic English course");
        testSubject.setStatus(SubjectStatus.ACTIVE);
        testSubject.setCreatedBy(testUser);
        testSubject.setCreatedAt(OffsetDateTime.now());
        testSubject.setUpdatedAt(OffsetDateTime.now());

        // Setup test level
        testLevel = new Level();
        testLevel.setId(1L);
        testLevel.setSubject(testSubject);
        testLevel.setCode("A1");
        testLevel.setName("Beginner A1");
        testLevel.setSortOrder(1);
        testLevel.setCreatedAt(OffsetDateTime.now());
    }

    @Test
    void getAllSubjects_WithNoPagination_ShouldReturnAllSubjects() {
        // Arrange
        Page<Subject> subjectPage = new PageImpl<>(Arrays.asList(testSubject));
        when(subjectRepository.findAll(any(Pageable.class))).thenReturn(subjectPage);
        when(levelRepository.countBySubjectId(1L)).thenReturn(1L);
        when(courseRepository.countBySubjectId(1L)).thenReturn(0L);

        // Act
        PagedResponseDTO<SubjectDTO> result = subjectService.getAllSubjects(null, null, null);

        // Assert
        assertThat(result).isNotNull();
        assertThat(result.getData()).hasSize(1);
        assertThat(result.getData().get(0).getCode()).isEqualTo("ENG-101");
        assertThat(result.getData().get(0).getLevelsCount()).isEqualTo(1);
        assertThat(result.getData().get(0).getCoursesCount()).isEqualTo(0);
        assertThat(result.getPagination().getTotalItems()).isEqualTo(1);
        verify(subjectRepository).findAll(any(Pageable.class));
        verify(levelRepository).countBySubjectId(1L);
        verify(courseRepository).countBySubjectId(1L);
    }

    @Test
    void getAllSubjects_WithStatusFilter_ShouldReturnFilteredSubjects() {
        // Arrange
        Page<Subject> subjectPage = new PageImpl<>(Arrays.asList(testSubject));
        when(subjectRepository.findByStatus(eq(SubjectStatus.ACTIVE), any(Pageable.class)))
                .thenReturn(subjectPage);
        when(levelRepository.countBySubjectId(1L)).thenReturn(1L);
        when(courseRepository.countBySubjectId(1L)).thenReturn(0L);

        // Act
        PagedResponseDTO<SubjectDTO> result = subjectService.getAllSubjects("ACTIVE", 1, 10);

        // Assert
        assertThat(result).isNotNull();
        assertThat(result.getData()).hasSize(1);
        assertThat(result.getData().get(0).getStatus()).isEqualTo("ACTIVE");
        verify(subjectRepository).findByStatus(eq(SubjectStatus.ACTIVE), any(Pageable.class));
    }

    @Test
    void getAllSubjects_WithInvalidStatus_ShouldThrowException() {
        // Act & Assert
        assertThatThrownBy(() -> subjectService.getAllSubjects("INVALID_STATUS", 1, 10))
                .isInstanceOf(CustomException.class)
                .hasFieldOrPropertyWithValue("errorCode", ErrorCode.INVALID_STATUS);
        verify(subjectRepository, never()).findAll(any(Pageable.class));
    }

    @Test
    void getSubjectById_WithValidId_ShouldReturnSubjectDetail() {
        // Arrange
        when(subjectRepository.findById(1L)).thenReturn(Optional.of(testSubject));
        when(levelRepository.findBySubjectIdOrderBySortOrderAsc(1L))
                .thenReturn(Arrays.asList(testLevel));
        when(courseRepository.countBySubjectId(1L)).thenReturn(0L);

        // Act
        SubjectDetailDTO result = subjectService.getSubjectById(1L);

        // Assert
        assertThat(result).isNotNull();
        assertThat(result.getId()).isEqualTo(1L);
        assertThat(result.getCode()).isEqualTo("ENG-101");
        assertThat(result.getName()).isEqualTo("English Language");
        assertThat(result.getLevels()).hasSize(1);
        assertThat(result.getLevels().get(0).getCode()).isEqualTo("A1");
        assertThat(result.getLevelsCount()).isEqualTo(1);
        verify(subjectRepository).findById(1L);
        verify(levelRepository).findBySubjectIdOrderBySortOrderAsc(1L);
        verify(courseRepository).countBySubjectId(1L);
    }

    @Test
    void getSubjectById_WithInvalidId_ShouldThrowException() {
        // Arrange
        when(subjectRepository.findById(999L)).thenReturn(Optional.empty());

        // Act & Assert
        assertThatThrownBy(() -> subjectService.getSubjectById(999L))
                .isInstanceOf(CustomException.class)
                .hasFieldOrPropertyWithValue("errorCode", ErrorCode.SUBJECT_NOT_FOUND);
        verify(subjectRepository).findById(999L);
    }

    @Test
    void createSubject_WithValidData_ShouldCreateSubject() {
        // Arrange
        CreateSubjectRequestDTO request = CreateSubjectRequestDTO.builder()
                .code("MATH-101")
                .name("Mathematics")
                .description("Basic Mathematics")
                .status("ACTIVE")
                .build();

        when(subjectRepository.existsByCode("MATH-101")).thenReturn(false);
        when(subjectRepository.save(any(Subject.class))).thenReturn(testSubject);
        when(levelRepository.countBySubjectId(anyLong())).thenReturn(0L);
        when(courseRepository.countBySubjectId(anyLong())).thenReturn(0L);

        // Act
        SubjectDTO result = subjectService.createSubject(request);

        // Assert
        assertThat(result).isNotNull();
        verify(subjectRepository).existsByCode("MATH-101");
        verify(subjectRepository).save(any(Subject.class));
    }

    @Test
    void createSubject_WithDuplicateCode_ShouldThrowException() {
        // Arrange
        CreateSubjectRequestDTO request = CreateSubjectRequestDTO.builder()
                .code("ENG-101")
                .name("English Language")
                .status("ACTIVE")
                .build();

        when(subjectRepository.existsByCode("ENG-101")).thenReturn(true);

        // Act & Assert
        assertThatThrownBy(() -> subjectService.createSubject(request))
                .isInstanceOf(CustomException.class)
                .hasFieldOrPropertyWithValue("errorCode", ErrorCode.SUBJECT_CODE_DUPLICATE);
        verify(subjectRepository).existsByCode("ENG-101");
        verify(subjectRepository, never()).save(any(Subject.class));
    }

    @Test
    void createSubject_WithInvalidStatus_ShouldThrowException() {
        // Arrange
        CreateSubjectRequestDTO request = CreateSubjectRequestDTO.builder()
                .code("MATH-101")
                .name("Mathematics")
                .status("INVALID_STATUS")
                .build();

        when(subjectRepository.existsByCode("MATH-101")).thenReturn(false);

        // Act & Assert
        assertThatThrownBy(() -> subjectService.createSubject(request))
                .isInstanceOf(CustomException.class)
                .hasFieldOrPropertyWithValue("errorCode", ErrorCode.INVALID_STATUS);
        verify(subjectRepository, never()).save(any(Subject.class));
    }

    @Test
    void updateSubject_WithValidData_ShouldUpdateSubject() {
        // Arrange
        UpdateSubjectRequestDTO request = UpdateSubjectRequestDTO.builder()
                .name("Updated English")
                .description("Updated description")
                .status("INACTIVE")
                .build();

        when(subjectRepository.findById(1L)).thenReturn(Optional.of(testSubject));
        when(subjectRepository.save(any(Subject.class))).thenReturn(testSubject);
        when(levelRepository.countBySubjectId(1L)).thenReturn(1L);
        when(courseRepository.countBySubjectId(1L)).thenReturn(0L);

        // Act
        SubjectDTO result = subjectService.updateSubject(1L, request);

        // Assert
        assertThat(result).isNotNull();
        verify(subjectRepository).findById(1L);
        verify(subjectRepository).save(any(Subject.class));
    }

    @Test
    void updateSubject_WithInvalidId_ShouldThrowException() {
        // Arrange
        UpdateSubjectRequestDTO request = UpdateSubjectRequestDTO.builder()
                .name("Updated Subject")
                .build();

        when(subjectRepository.findById(999L)).thenReturn(Optional.empty());

        // Act & Assert
        assertThatThrownBy(() -> subjectService.updateSubject(999L, request))
                .isInstanceOf(CustomException.class)
                .hasFieldOrPropertyWithValue("errorCode", ErrorCode.SUBJECT_NOT_FOUND);
        verify(subjectRepository).findById(999L);
    }

    @Test
    void deleteSubject_WithValidId_ShouldSoftDelete() {
        // Arrange
        when(subjectRepository.findById(1L)).thenReturn(Optional.of(testSubject));
        when(levelRepository.countBySubjectId(1L)).thenReturn(0L);
        when(courseRepository.countBySubjectId(1L)).thenReturn(0L);
        when(subjectRepository.save(any(Subject.class))).thenReturn(testSubject);

        // Act
        subjectService.deleteSubject(1L);

        // Assert
        verify(subjectRepository).findById(1L);
        verify(levelRepository).countBySubjectId(1L);
        verify(courseRepository).countBySubjectId(1L);
        verify(subjectRepository).save(argThat(subject ->
                subject.getStatus() == SubjectStatus.INACTIVE));
    }

    @Test
    void deleteSubject_WithExistingLevels_ShouldThrowException() {
        // Arrange
        when(subjectRepository.findById(1L)).thenReturn(Optional.of(testSubject));
        when(levelRepository.countBySubjectId(1L)).thenReturn(1L);

        // Act & Assert
        assertThatThrownBy(() -> subjectService.deleteSubject(1L))
                .isInstanceOf(CustomException.class)
                .hasFieldOrPropertyWithValue("errorCode", ErrorCode.SUBJECT_HAS_LEVELS);
        verify(subjectRepository).findById(1L);
        verify(levelRepository).countBySubjectId(1L);
        verify(subjectRepository, never()).save(any(Subject.class));
    }

    @Test
    void deleteSubject_WithExistingCourses_ShouldThrowException() {
        // Arrange
        when(subjectRepository.findById(1L)).thenReturn(Optional.of(testSubject));
        when(levelRepository.countBySubjectId(1L)).thenReturn(0L);
        when(courseRepository.countBySubjectId(1L)).thenReturn(1L);

        // Act & Assert
        assertThatThrownBy(() -> subjectService.deleteSubject(1L))
                .isInstanceOf(CustomException.class)
                .hasFieldOrPropertyWithValue("errorCode", ErrorCode.SUBJECT_HAS_COURSES);
        verify(subjectRepository).findById(1L);
        verify(levelRepository).countBySubjectId(1L);
        verify(courseRepository).countBySubjectId(1L);
        verify(subjectRepository, never()).save(any(Subject.class));
    }
}

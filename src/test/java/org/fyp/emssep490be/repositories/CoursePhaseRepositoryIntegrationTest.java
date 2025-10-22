package org.fyp.emssep490be.repositories;

import org.fyp.emssep490be.entities.*;
import org.fyp.emssep490be.entities.enums.SubjectStatus;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.boot.test.autoconfigure.orm.jpa.TestEntityManager;
import org.springframework.test.context.ActiveProfiles;

import java.math.BigDecimal;
import java.time.OffsetDateTime;
import java.time.ZoneOffset;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * Integration tests for CoursePhaseRepository
 * Tests repository layer with real H2 database
 * Pattern: @DataJpaTest with TestEntityManager
 */
@DataJpaTest
@ActiveProfiles("test")
@DisplayName("CoursePhase Repository Integration Tests")
class CoursePhaseRepositoryIntegrationTest {

    @Autowired
    private CoursePhaseRepository coursePhaseRepository;

    @Autowired
    private CourseRepository courseRepository;

    @Autowired
    private SubjectRepository subjectRepository;

    @Autowired
    private LevelRepository levelRepository;

    @Autowired
    private TestEntityManager entityManager;

    private Subject testSubject;
    private Level testLevel;
    private Course testCourse1;
    private Course testCourse2;
    private CoursePhase testPhase1;
    private CoursePhase testPhase2;
    private CoursePhase testPhase3;

    @BeforeEach
    void setUp() {
        // Clean database in correct order (respect foreign keys)
        coursePhaseRepository.deleteAll();
        courseRepository.deleteAll();
        levelRepository.deleteAll();
        subjectRepository.deleteAll();
        entityManager.flush();
        entityManager.clear();

        // Create test subject
        testSubject = new Subject();
        testSubject.setCode("ENG");
        testSubject.setName("English");
        testSubject.setDescription("English language courses");
        testSubject.setStatus(SubjectStatus.ACTIVE);
        testSubject.setCreatedAt(OffsetDateTime.now(ZoneOffset.UTC));
        testSubject = subjectRepository.save(testSubject);

        // Create test level
        testLevel = new Level();
        testLevel.setSubject(testSubject);
        testLevel.setCode("A1");
        testLevel.setName("Beginner A1");
        testLevel.setSortOrder(1);
        testLevel.setCreatedAt(OffsetDateTime.now(ZoneOffset.UTC));
        testLevel.setUpdatedAt(OffsetDateTime.now(ZoneOffset.UTC));
        testLevel = levelRepository.save(testLevel);

        // Create test courses
        testCourse1 = new Course();
        testCourse1.setSubject(testSubject);
        testCourse1.setLevel(testLevel);
        testCourse1.setCode("ENG-A1-V1");
        testCourse1.setName("English A1 Version 1");
        testCourse1.setDescription("Beginner English course");
        testCourse1.setVersion(1);
        testCourse1.setTotalHours(60);
        testCourse1.setDurationWeeks(10);
        testCourse1.setSessionPerWeek(3);
        testCourse1.setHoursPerSession(BigDecimal.valueOf(2.0));
        testCourse1.setStatus("draft");
        testCourse1.setHashChecksum("abc123");
        testCourse1.setCreatedAt(OffsetDateTime.now(ZoneOffset.UTC));
        testCourse1 = courseRepository.save(testCourse1);

        testCourse2 = new Course();
        testCourse2.setSubject(testSubject);
        testCourse2.setLevel(testLevel);
        testCourse2.setCode("ENG-A1-V2");
        testCourse2.setName("English A1 Version 2");
        testCourse2.setVersion(2);
        testCourse2.setTotalHours(80);
        testCourse2.setDurationWeeks(12);
        testCourse2.setSessionPerWeek(3);
        testCourse2.setHoursPerSession(BigDecimal.valueOf(2.5));
        testCourse2.setStatus("active");
        testCourse2.setHashChecksum("xyz789");
        testCourse2.setCreatedAt(OffsetDateTime.now(ZoneOffset.UTC));
        testCourse2 = courseRepository.save(testCourse2);

        entityManager.flush();
        entityManager.clear();

        // Create test phases
        testPhase1 = new CoursePhase();
        testPhase1.setCourse(testCourse1);
        testPhase1.setPhaseNumber(1);
        testPhase1.setName("Foundation Phase");
        testPhase1.setDurationWeeks(4);
        testPhase1.setLearningFocus("Basic grammar and vocabulary");
        testPhase1.setSortOrder(1);
        testPhase1.setCreatedAt(OffsetDateTime.now(ZoneOffset.UTC));
        testPhase1.setUpdatedAt(OffsetDateTime.now(ZoneOffset.UTC));

        testPhase2 = new CoursePhase();
        testPhase2.setCourse(testCourse1);
        testPhase2.setPhaseNumber(2);
        testPhase2.setName("Intermediate Phase");
        testPhase2.setDurationWeeks(3);
        testPhase2.setLearningFocus("Conversation practice");
        testPhase2.setSortOrder(2);
        testPhase2.setCreatedAt(OffsetDateTime.now(ZoneOffset.UTC));
        testPhase2.setUpdatedAt(OffsetDateTime.now(ZoneOffset.UTC));

        testPhase3 = new CoursePhase();
        testPhase3.setCourse(testCourse1);
        testPhase3.setPhaseNumber(3);
        testPhase3.setName("Advanced Phase");
        testPhase3.setDurationWeeks(3);
        testPhase3.setLearningFocus("Reading and writing");
        testPhase3.setSortOrder(3);
        testPhase3.setCreatedAt(OffsetDateTime.now(ZoneOffset.UTC));
        testPhase3.setUpdatedAt(OffsetDateTime.now(ZoneOffset.UTC));
    }

    @Test
    @DisplayName("Should save and retrieve course phase successfully")
    void testSaveAndRetrievePhase() {
        // When
        CoursePhase saved = coursePhaseRepository.save(testPhase1);
        entityManager.flush();
        entityManager.clear();

        // Then
        assertThat(saved.getId()).isNotNull();
        assertThat(saved.getPhaseNumber()).isEqualTo(1);
        assertThat(saved.getName()).isEqualTo("Foundation Phase");
        assertThat(saved.getDurationWeeks()).isEqualTo(4);

        // Verify retrieval
        Optional<CoursePhase> retrieved = coursePhaseRepository.findById(saved.getId());
        assertThat(retrieved).isPresent();
        assertThat(retrieved.get().getName()).isEqualTo("Foundation Phase");
        assertThat(retrieved.get().getCourse().getId()).isEqualTo(testCourse1.getId());
    }

    @Test
    @DisplayName("Should find phases by course ID ordered by sort order")
    void testFindByCourseIdOrderBySortOrderAsc() {
        // Given - Save phases in random order
        coursePhaseRepository.save(testPhase2); // sort_order 2
        coursePhaseRepository.save(testPhase3); // sort_order 3
        coursePhaseRepository.save(testPhase1); // sort_order 1
        entityManager.flush();
        entityManager.clear();

        // When
        List<CoursePhase> phases = coursePhaseRepository.findByCourseIdOrderBySortOrderAsc(testCourse1.getId());

        // Then - Should be ordered by sortOrder (1, 2, 3)
        assertThat(phases).hasSize(3);
        assertThat(phases.get(0).getSortOrder()).isEqualTo(1);
        assertThat(phases.get(0).getName()).isEqualTo("Foundation Phase");
        assertThat(phases.get(1).getSortOrder()).isEqualTo(2);
        assertThat(phases.get(1).getName()).isEqualTo("Intermediate Phase");
        assertThat(phases.get(2).getSortOrder()).isEqualTo(3);
        assertThat(phases.get(2).getName()).isEqualTo("Advanced Phase");
    }

    @Test
    @DisplayName("Should return empty list for course with no phases")
    void testFindByCourseIdWithNoPhases() {
        // Given - testCourse2 has no phases

        // When
        List<CoursePhase> phases = coursePhaseRepository.findByCourseIdOrderBySortOrderAsc(testCourse2.getId());

        // Then
        assertThat(phases).isEmpty();
    }

    @Test
    @DisplayName("Should find phase by ID and course ID")
    void testFindByIdAndCourseId() {
        // Given
        CoursePhase saved = coursePhaseRepository.save(testPhase1);
        entityManager.flush();
        entityManager.clear();

        // When - Find with correct course ID
        Optional<CoursePhase> found = coursePhaseRepository.findByIdAndCourseId(
                saved.getId(), testCourse1.getId()
        );

        // Then
        assertThat(found).isPresent();
        assertThat(found.get().getId()).isEqualTo(saved.getId());
        assertThat(found.get().getName()).isEqualTo("Foundation Phase");

        // When - Find with wrong course ID
        Optional<CoursePhase> notFound = coursePhaseRepository.findByIdAndCourseId(
                saved.getId(), testCourse2.getId()
        );

        // Then
        assertThat(notFound).isEmpty();
    }

    @Test
    @DisplayName("Should check if phase number exists for course")
    void testExistsByPhaseNumberAndCourseId() {
        // Given
        coursePhaseRepository.save(testPhase1); // phase_number = 1
        entityManager.flush();

        // When & Then - Existing phase number
        assertThat(coursePhaseRepository.existsByPhaseNumberAndCourseId(1, testCourse1.getId())).isTrue();

        // When & Then - Non-existing phase number
        assertThat(coursePhaseRepository.existsByPhaseNumberAndCourseId(99, testCourse1.getId())).isFalse();

        // When & Then - Phase number for different course
        assertThat(coursePhaseRepository.existsByPhaseNumberAndCourseId(1, testCourse2.getId())).isFalse();
    }

    @Test
    @DisplayName("Should enforce unique phase number per course")
    void testUniquePhaseNumberConstraint() {
        // Given - Save first phase with phase_number = 1
        coursePhaseRepository.save(testPhase1);
        entityManager.flush();

        // When - Try to save another phase with same phase_number for same course
        CoursePhase duplicate = new CoursePhase();
        duplicate.setCourse(testCourse1);
        duplicate.setPhaseNumber(1); // Duplicate phase number
        duplicate.setName("Duplicate Phase");
        duplicate.setDurationWeeks(2);
        duplicate.setSortOrder(4);
        duplicate.setCreatedAt(OffsetDateTime.now(ZoneOffset.UTC));
        duplicate.setUpdatedAt(OffsetDateTime.now(ZoneOffset.UTC));

        // Then - Should throw exception
        try {
            coursePhaseRepository.save(duplicate);
            entityManager.flush();
            assertThat(true).as("Should have thrown constraint violation exception").isFalse();
        } catch (Exception e) {
            assertThat(e).isNotNull();
        }
    }

    @Test
    @DisplayName("Should allow same phase number for different courses")
    void testSamePhaseNumberForDifferentCourses() {
        // Given - Save phase with phase_number = 1 for course1
        coursePhaseRepository.save(testPhase1);
        entityManager.flush();

        // When - Save phase with same phase_number for course2
        CoursePhase phaseForCourse2 = new CoursePhase();
        phaseForCourse2.setCourse(testCourse2);
        phaseForCourse2.setPhaseNumber(1); // Same phase number, different course
        phaseForCourse2.setName("Phase 1 for Course 2");
        phaseForCourse2.setDurationWeeks(5);
        phaseForCourse2.setSortOrder(1);
        phaseForCourse2.setCreatedAt(OffsetDateTime.now(ZoneOffset.UTC));
        phaseForCourse2.setUpdatedAt(OffsetDateTime.now(ZoneOffset.UTC));

        // Then - Should save successfully
        CoursePhase saved = coursePhaseRepository.save(phaseForCourse2);
        entityManager.flush();

        assertThat(saved.getId()).isNotNull();
        assertThat(saved.getPhaseNumber()).isEqualTo(1);
        assertThat(saved.getCourse().getId()).isEqualTo(testCourse2.getId());
    }

    @Test
    @DisplayName("Should count phases by course ID")
    void testCountByCourseId() {
        // Given - Save 3 phases for course1
        coursePhaseRepository.save(testPhase1);
        coursePhaseRepository.save(testPhase2);
        coursePhaseRepository.save(testPhase3);
        entityManager.flush();

        // When
        long countCourse1 = coursePhaseRepository.countByCourseId(testCourse1.getId());
        long countCourse2 = coursePhaseRepository.countByCourseId(testCourse2.getId());

        // Then
        assertThat(countCourse1).isEqualTo(3);
        assertThat(countCourse2).isEqualTo(0);
    }

    @Test
    @DisplayName("Should update phase successfully")
    void testUpdatePhase() {
        // Given - Save phase
        CoursePhase saved = coursePhaseRepository.save(testPhase1);
        entityManager.flush();
        entityManager.clear();

        // When - Update phase
        CoursePhase toUpdate = coursePhaseRepository.findById(saved.getId()).orElseThrow();
        toUpdate.setName("Updated Foundation Phase");
        toUpdate.setDurationWeeks(5);
        toUpdate.setLearningFocus("Updated learning focus");

        CoursePhase updated = coursePhaseRepository.save(toUpdate);
        entityManager.flush();
        entityManager.clear();

        // Then - Verify updates
        CoursePhase retrieved = coursePhaseRepository.findById(updated.getId()).orElseThrow();
        assertThat(retrieved.getName()).isEqualTo("Updated Foundation Phase");
        assertThat(retrieved.getDurationWeeks()).isEqualTo(5);
        assertThat(retrieved.getLearningFocus()).isEqualTo("Updated learning focus");
        assertThat(retrieved.getPhaseNumber()).isEqualTo(1); // Unchanged
    }

    @Test
    @DisplayName("Should delete phase without sessions")
    void testDeletePhaseWithoutSessions() {
        // Given
        CoursePhase saved = coursePhaseRepository.save(testPhase1);
        Long phaseId = saved.getId();
        entityManager.flush();
        entityManager.clear();

        // When
        coursePhaseRepository.deleteById(phaseId);
        entityManager.flush();

        // Then
        Optional<CoursePhase> deleted = coursePhaseRepository.findById(phaseId);
        assertThat(deleted).isEmpty();
    }

    @Test
    @DisplayName("Should retrieve phase with course relationship")
    void testPhaseWithCourseRelationship() {
        // Given
        CoursePhase saved = coursePhaseRepository.save(testPhase1);
        entityManager.flush();
        entityManager.clear();

        // When
        CoursePhase retrieved = coursePhaseRepository.findById(saved.getId()).orElseThrow();

        // Then - Verify course relationship
        assertThat(retrieved.getCourse()).isNotNull();
        assertThat(retrieved.getCourse().getId()).isEqualTo(testCourse1.getId());
        assertThat(retrieved.getCourse().getCode()).isEqualTo("ENG-A1-V1");
    }

    @Test
    @DisplayName("Should handle sort order correctly")
    void testSortOrderHandling() {
        // Given - Create phases with custom sort orders
        testPhase1.setSortOrder(10);
        testPhase2.setSortOrder(5);
        testPhase3.setSortOrder(15);

        coursePhaseRepository.save(testPhase1);
        coursePhaseRepository.save(testPhase2);
        coursePhaseRepository.save(testPhase3);
        entityManager.flush();
        entityManager.clear();

        // When - Retrieve ordered by sort_order
        List<CoursePhase> phases = coursePhaseRepository.findByCourseIdOrderBySortOrderAsc(testCourse1.getId());

        // Then - Should be ordered: 5, 10, 15
        assertThat(phases).hasSize(3);
        assertThat(phases.get(0).getSortOrder()).isEqualTo(5);
        assertThat(phases.get(0).getName()).isEqualTo("Intermediate Phase");
        assertThat(phases.get(1).getSortOrder()).isEqualTo(10);
        assertThat(phases.get(1).getName()).isEqualTo("Foundation Phase");
        assertThat(phases.get(2).getSortOrder()).isEqualTo(15);
        assertThat(phases.get(2).getName()).isEqualTo("Advanced Phase");
    }

    @Test
    @DisplayName("Should persist all phase fields correctly")
    void testPersistAllFields() {
        // Given - Phase with all fields set
        testPhase1.setLearningFocus("Complete learning focus description");

        // When
        CoursePhase saved = coursePhaseRepository.save(testPhase1);
        entityManager.flush();
        entityManager.clear();

        // Then - Verify all fields persisted
        CoursePhase retrieved = coursePhaseRepository.findById(saved.getId()).orElseThrow();
        assertThat(retrieved.getPhaseNumber()).isEqualTo(1);
        assertThat(retrieved.getName()).isEqualTo("Foundation Phase");
        assertThat(retrieved.getDurationWeeks()).isEqualTo(4);
        assertThat(retrieved.getLearningFocus()).isEqualTo("Complete learning focus description");
        assertThat(retrieved.getSortOrder()).isEqualTo(1);
        assertThat(retrieved.getCreatedAt()).isNotNull();
        assertThat(retrieved.getUpdatedAt()).isNotNull();
        assertThat(retrieved.getCourse().getId()).isEqualTo(testCourse1.getId());
    }
}

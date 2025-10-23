package org.fyp.emssep490be.repositories;

import org.fyp.emssep490be.entities.*;
import org.fyp.emssep490be.entities.enums.SubjectStatus;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.boot.test.autoconfigure.orm.jpa.TestEntityManager;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.test.context.ActiveProfiles;

import java.math.BigDecimal;
import java.time.OffsetDateTime;
import java.time.ZoneOffset;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * Integration tests for CourseRepository
 * Tests repository layer with real H2 database
 * Pattern: @DataJpaTest with TestEntityManager
 */
@DataJpaTest
@ActiveProfiles("test")
@DisplayName("Course Repository Integration Tests")
class CourseRepositoryIntegrationTest {

    @Autowired
    private CourseRepository courseRepository;

    @Autowired
    private SubjectRepository subjectRepository;

    @Autowired
    private LevelRepository levelRepository;

    @Autowired
    private CoursePhaseRepository coursePhaseRepository;

    @Autowired
    private UserAccountRepository userAccountRepository;

    @Autowired
    private TestEntityManager entityManager;

    private Subject testSubject;
    private Level testLevel;
    private UserAccount testManager;
    private Course testCourse1;
    private Course testCourse2;

    @BeforeEach
    void setUp() {
        // Clean database in correct order (respect foreign keys)
        coursePhaseRepository.deleteAll();
        courseRepository.deleteAll();
        levelRepository.deleteAll();
        subjectRepository.deleteAll();
        userAccountRepository.deleteAll();
        entityManager.flush();
        entityManager.clear();

        // Create test manager (for approval testing)
        testManager = new UserAccount();
        testManager.setEmail("manager@test.com");
        testManager.setPasswordHash("hashed");
        testManager.setFullName("Test Manager");
        testManager.setCreatedAt(OffsetDateTime.now(ZoneOffset.UTC));
        testManager = userAccountRepository.save(testManager);

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

        entityManager.flush();
        entityManager.clear();

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

        testCourse2 = new Course();
        testCourse2.setSubject(testSubject);
        testCourse2.setLevel(testLevel);
        testCourse2.setCode("ENG-A1-V2");
        testCourse2.setName("English A1 Version 2");
        testCourse2.setDescription("Updated beginner course");
        testCourse2.setVersion(2);
        testCourse2.setTotalHours(80);
        testCourse2.setDurationWeeks(12);
        testCourse2.setSessionPerWeek(3);
        testCourse2.setHoursPerSession(BigDecimal.valueOf(2.5));
        testCourse2.setStatus("active");
        testCourse2.setHashChecksum("xyz789");
        testCourse2.setApprovedByManager(testManager); // Set approved by test manager
        testCourse2.setApprovedAt(OffsetDateTime.now(ZoneOffset.UTC));
        testCourse2.setCreatedAt(OffsetDateTime.now(ZoneOffset.UTC));
    }

    @Test
    @DisplayName("Should save and retrieve course successfully")
    void testSaveAndRetrieveCourse() {
        // When
        Course saved = courseRepository.save(testCourse1);
        entityManager.flush();
        entityManager.clear();

        // Then
        assertThat(saved.getId()).isNotNull();
        assertThat(saved.getCode()).isEqualTo("ENG-A1-V1");
        assertThat(saved.getName()).isEqualTo("English A1 Version 1");
        assertThat(saved.getStatus()).isEqualTo("draft");
        assertThat(saved.getVersion()).isEqualTo(1);

        // Verify retrieval
        Optional<Course> retrieved = courseRepository.findById(saved.getId());
        assertThat(retrieved).isPresent();
        assertThat(retrieved.get().getCode()).isEqualTo("ENG-A1-V1");
        assertThat(retrieved.get().getSubject().getId()).isEqualTo(testSubject.getId());
        assertThat(retrieved.get().getLevel().getId()).isEqualTo(testLevel.getId());
    }

    @Test
    @DisplayName("Should allow different versions for same subject and level")
    void testMultipleVersionsAllowed() {
        // Given - Save first course with version 1
        courseRepository.save(testCourse1);
        entityManager.flush();
        entityManager.clear();

        // When - Save course with same subject/level but different version
        Course version2 = new Course();
        version2.setSubject(testSubject);
        version2.setLevel(testLevel);
        version2.setCode("ENG-A1-V3"); // Different code
        version2.setName("English A1 Version 3");
        version2.setVersion(3); // Different version
        version2.setTotalHours(40);
        version2.setDurationWeeks(8);
        version2.setSessionPerWeek(2);
        version2.setHoursPerSession(BigDecimal.valueOf(2.5));
        version2.setStatus("draft");
        version2.setHashChecksum("different");
        version2.setCreatedAt(OffsetDateTime.now(ZoneOffset.UTC));

        // Then - Should save successfully (different versions allowed)
        Course saved = courseRepository.save(version2);
        entityManager.flush();

        assertThat(saved.getId()).isNotNull();
        assertThat(saved.getVersion()).isEqualTo(3);

        // Verify both courses exist
        long count = courseRepository.countBySubjectId(testSubject.getId());
        assertThat(count).isEqualTo(2);
    }

    @Test
    @DisplayName("Should enforce unique code constraint per subject")
    void testUniqueCodePerSubjectConstraint() {
        // Given - Save first course
        courseRepository.save(testCourse1);
        entityManager.flush();

        // When - Try to save course with duplicate code in same subject
        Course duplicate = new Course();
        duplicate.setSubject(testSubject);
        duplicate.setLevel(testLevel);
        duplicate.setCode("ENG-A1-V1"); // Same code
        duplicate.setName("Different Name");
        duplicate.setVersion(3); // Different version
        duplicate.setTotalHours(40);
        duplicate.setDurationWeeks(8);
        duplicate.setSessionPerWeek(2);
        duplicate.setHoursPerSession(BigDecimal.valueOf(2.5));
        duplicate.setStatus("draft");
        duplicate.setHashChecksum("different");
        duplicate.setCreatedAt(OffsetDateTime.now(ZoneOffset.UTC));

        // Then - Should throw exception
        try {
            courseRepository.save(duplicate);
            entityManager.flush();
            assertThat(true).as("Should have thrown constraint violation exception").isFalse();
        } catch (Exception e) {
            assertThat(e).isNotNull();
        }
    }

    @Test
    @DisplayName("Should find courses by subject")
    void testFindBySubjectId() {
        // Given - Save both courses (same subject)
        courseRepository.save(testCourse1);
        courseRepository.save(testCourse2);
        entityManager.flush();
        entityManager.clear();

        // When
        Pageable pageable = PageRequest.of(0, 10);
        Page<Course> courses = courseRepository.findByFilters(
                testSubject.getId(), null, null, null, pageable
        );

        // Then
        assertThat(courses.getContent()).hasSize(2);
        assertThat(courses.getContent())
                .extracting(Course::getCode)
                .containsExactlyInAnyOrder("ENG-A1-V1", "ENG-A1-V2");
    }

    @Test
    @DisplayName("Should find courses by level")
    void testFindByLevelId() {
        // Given
        courseRepository.save(testCourse1);
        courseRepository.save(testCourse2);
        entityManager.flush();
        entityManager.clear();

        // When
        Pageable pageable = PageRequest.of(0, 10);
        Page<Course> courses = courseRepository.findByFilters(
                null, testLevel.getId(), null, null, pageable
        );

        // Then
        assertThat(courses.getContent()).hasSize(2);
    }

    @Test
    @DisplayName("Should find courses by status")
    void testFindByStatus() {
        // Given
        courseRepository.save(testCourse1); // draft
        courseRepository.save(testCourse2); // active
        entityManager.flush();
        entityManager.clear();

        // When - Find draft courses
        Pageable pageable = PageRequest.of(0, 10);
        Page<Course> draftCourses = courseRepository.findByFilters(
                null, null, "draft", null, pageable
        );

        // Then
        assertThat(draftCourses.getContent()).hasSize(1);
        assertThat(draftCourses.getContent().get(0).getStatus()).isEqualTo("draft");
        assertThat(draftCourses.getContent().get(0).getCode()).isEqualTo("ENG-A1-V1");

        // When - Find active courses
        Page<Course> activeCourses = courseRepository.findByFilters(
                null, null, "active", null, pageable
        );

        // Then
        assertThat(activeCourses.getContent()).hasSize(1);
        assertThat(activeCourses.getContent().get(0).getStatus()).isEqualTo("active");
        assertThat(activeCourses.getContent().get(0).getCode()).isEqualTo("ENG-A1-V2");
    }

    @Test
    @DisplayName("Should find approved courses only")
    void testFindByApproved() {
        // Given
        courseRepository.save(testCourse1); // Not approved
        courseRepository.save(testCourse2); // Approved
        entityManager.flush();
        entityManager.clear();

        // When - Find approved courses
        Pageable pageable = PageRequest.of(0, 10);
        Page<Course> approvedCourses = courseRepository.findByFilters(
                null, null, null, true, pageable
        );

        // Then
        assertThat(approvedCourses.getContent()).hasSize(1);
        assertThat(approvedCourses.getContent().get(0).getApprovedByManager()).isNotNull();
        assertThat(approvedCourses.getContent().get(0).getCode()).isEqualTo("ENG-A1-V2");

        // When - Find non-approved courses
        Page<Course> nonApprovedCourses = courseRepository.findByFilters(
                null, null, null, false, pageable
        );

        // Then
        assertThat(nonApprovedCourses.getContent()).hasSize(1);
        assertThat(nonApprovedCourses.getContent().get(0).getApprovedByManager()).isNull();
        assertThat(nonApprovedCourses.getContent().get(0).getCode()).isEqualTo("ENG-A1-V1");
    }

    @Test
    @DisplayName("Should find courses with combined filters")
    void testFindWithCombinedFilters() {
        // Given
        courseRepository.save(testCourse1);
        courseRepository.save(testCourse2);
        entityManager.flush();
        entityManager.clear();

        // When - Find by subject + status + approved
        Pageable pageable = PageRequest.of(0, 10);
        Page<Course> filtered = courseRepository.findByFilters(
                testSubject.getId(), null, "active", true, pageable
        );

        // Then
        assertThat(filtered.getContent()).hasSize(1);
        assertThat(filtered.getContent().get(0).getCode()).isEqualTo("ENG-A1-V2");
        assertThat(filtered.getContent().get(0).getStatus()).isEqualTo("active");
        assertThat(filtered.getContent().get(0).getApprovedByManager()).isNotNull();
    }

    @Test
    @DisplayName("Should check if code exists")
    void testExistsByCode() {
        // Given
        courseRepository.save(testCourse1);
        entityManager.flush();

        // When & Then
        assertThat(courseRepository.existsByCode("ENG-A1-V1")).isTrue();
        assertThat(courseRepository.existsByCode("NON-EXISTENT")).isFalse();
    }

    @Test
    @DisplayName("Should count courses by subject")
    void testCountBySubjectId() {
        // Given
        courseRepository.save(testCourse1);
        courseRepository.save(testCourse2);
        entityManager.flush();

        // When & Then
        long count = courseRepository.countBySubjectId(testSubject.getId());
        assertThat(count).isEqualTo(2);
    }

    @Test
    @DisplayName("Should count courses by level")
    void testCountByLevelId() {
        // Given
        courseRepository.save(testCourse1);
        courseRepository.save(testCourse2);
        entityManager.flush();

        // When & Then
        long count = courseRepository.countByLevelId(testLevel.getId());
        assertThat(count).isEqualTo(2);
    }

    @Test
    @DisplayName("Should update course successfully")
    void testUpdateCourse() {
        // Given - Save course
        Course saved = courseRepository.save(testCourse1);
        entityManager.flush();
        entityManager.clear();

        // When - Update course
        Course toUpdate = courseRepository.findById(saved.getId()).orElseThrow();
        toUpdate.setName("Updated Name");
        toUpdate.setDescription("Updated Description");
        toUpdate.setStatus("submitted");
        toUpdate.setHashChecksum("updated-hash");

        Course updated = courseRepository.save(toUpdate);
        entityManager.flush();
        entityManager.clear();

        // Then - Verify updates
        Course retrieved = courseRepository.findById(updated.getId()).orElseThrow();
        assertThat(retrieved.getName()).isEqualTo("Updated Name");
        assertThat(retrieved.getDescription()).isEqualTo("Updated Description");
        assertThat(retrieved.getStatus()).isEqualTo("submitted");
        assertThat(retrieved.getHashChecksum()).isEqualTo("updated-hash");
        assertThat(retrieved.getCode()).isEqualTo("ENG-A1-V1"); // Code unchanged
    }

    @Test
    @DisplayName("Should delete course without phases")
    void testDeleteCourseWithoutPhases() {
        // Given
        Course saved = courseRepository.save(testCourse1);
        Long courseId = saved.getId();
        entityManager.flush();
        entityManager.clear();

        // When
        courseRepository.deleteById(courseId);
        entityManager.flush();

        // Then
        Optional<Course> deleted = courseRepository.findById(courseId);
        assertThat(deleted).isEmpty();
    }

    @Test
    @DisplayName("Should retrieve course with phases relationship")
    void testCourseWithPhases() {
        // Given - Save course
        Course saved = courseRepository.save(testCourse1);
        entityManager.flush();

        // Create phases for this course
        CoursePhase phase1 = new CoursePhase();
        phase1.setCourse(saved);
        phase1.setPhaseNumber(1);
        phase1.setName("Phase 1");
        phase1.setDurationWeeks(5);
        phase1.setSortOrder(1);
        phase1.setCreatedAt(OffsetDateTime.now(ZoneOffset.UTC));
        phase1.setUpdatedAt(OffsetDateTime.now(ZoneOffset.UTC));

        CoursePhase phase2 = new CoursePhase();
        phase2.setCourse(saved);
        phase2.setPhaseNumber(2);
        phase2.setName("Phase 2");
        phase2.setDurationWeeks(5);
        phase2.setSortOrder(2);
        phase2.setCreatedAt(OffsetDateTime.now(ZoneOffset.UTC));
        phase2.setUpdatedAt(OffsetDateTime.now(ZoneOffset.UTC));

        coursePhaseRepository.save(phase1);
        coursePhaseRepository.save(phase2);
        entityManager.flush();
        entityManager.clear();

        // When - Retrieve course and count phases
        Course retrieved = courseRepository.findById(saved.getId()).orElseThrow();
        Long phaseCount = coursePhaseRepository.countByCourseId(retrieved.getId());

        // Then
        assertThat(phaseCount).isEqualTo(2);
        assertThat(retrieved.getCode()).isEqualTo("ENG-A1-V1");
    }

    @Test
    @DisplayName("Should handle pagination correctly")
    void testPagination() {
        // Given - Save 5 courses
        for (int i = 1; i <= 5; i++) {
            Course course = new Course();
            course.setSubject(testSubject);
            course.setLevel(testLevel);
            course.setCode("COURSE-" + String.format("%03d", i));
            course.setName("Course " + i);
            course.setVersion(i);
            course.setTotalHours(60);
            course.setDurationWeeks(10);
            course.setSessionPerWeek(3);
            course.setHoursPerSession(BigDecimal.valueOf(2.0));
            course.setStatus("draft");
            course.setHashChecksum("hash-" + i);
            course.setCreatedAt(OffsetDateTime.now(ZoneOffset.UTC));
            courseRepository.save(course);
        }
        entityManager.flush();
        entityManager.clear();

        // When - Request first page with 2 items
        Pageable page1 = PageRequest.of(0, 2);
        Page<Course> firstPage = courseRepository.findByFilters(
                testSubject.getId(), null, null, null, page1
        );

        // Then - Verify first page
        assertThat(firstPage.getContent()).hasSize(2);
        assertThat(firstPage.getTotalElements()).isEqualTo(5);
        assertThat(firstPage.getTotalPages()).isEqualTo(3);
        assertThat(firstPage.isFirst()).isTrue();
        assertThat(firstPage.hasNext()).isTrue();

        // When - Request second page
        Pageable page2 = PageRequest.of(1, 2);
        Page<Course> secondPage = courseRepository.findByFilters(
                testSubject.getId(), null, null, null, page2
        );

        // Then - Verify second page
        assertThat(secondPage.getContent()).hasSize(2);
        assertThat(secondPage.isFirst()).isFalse();
        assertThat(secondPage.hasNext()).isTrue();

        // When - Request last page
        Pageable page3 = PageRequest.of(2, 2);
        Page<Course> lastPage = courseRepository.findByFilters(
                testSubject.getId(), null, null, null, page3
        );

        // Then - Verify last page
        assertThat(lastPage.getContent()).hasSize(1); // Only 1 item left
        assertThat(lastPage.isLast()).isTrue();
        assertThat(lastPage.hasNext()).isFalse();
    }

    @Test
    @DisplayName("Should handle empty result set")
    void testEmptyResultSet() {
        // Given - No courses saved

        // When
        Pageable pageable = PageRequest.of(0, 10);
        Page<Course> courses = courseRepository.findByFilters(
                testSubject.getId(), null, null, null, pageable
        );

        // Then
        assertThat(courses.getContent()).isEmpty();
        assertThat(courses.getTotalElements()).isEqualTo(0);
        assertThat(courses.getTotalPages()).isEqualTo(0);
    }
}

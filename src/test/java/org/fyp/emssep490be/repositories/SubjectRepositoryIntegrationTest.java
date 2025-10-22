package org.fyp.emssep490be.repositories;

import org.fyp.emssep490be.entities.Subject;
import org.fyp.emssep490be.entities.Level;
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

import java.time.OffsetDateTime;
import java.time.ZoneOffset;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * Integration tests for SubjectRepository
 * Tests repository layer with real H2 database
 */
@DataJpaTest
@ActiveProfiles("test")
@DisplayName("Subject Repository Integration Tests")
class SubjectRepositoryIntegrationTest {

    @Autowired
    private SubjectRepository subjectRepository;

    @Autowired
    private LevelRepository levelRepository;

    @Autowired
    private TestEntityManager entityManager;

    private Subject testSubject1;
    private Subject testSubject2;

    @BeforeEach
    void setUp() {
        // Clean database
        levelRepository.deleteAll();
        subjectRepository.deleteAll();
        entityManager.flush();
        entityManager.clear();

        // Create test subjects
        testSubject1 = new Subject();
        testSubject1.setCode("TEST-001");
        testSubject1.setName("Test Subject 1");
        testSubject1.setDescription("Test description 1");
        testSubject1.setStatus(SubjectStatus.ACTIVE);
        testSubject1.setCreatedAt(OffsetDateTime.now(ZoneOffset.UTC));

        testSubject2 = new Subject();
        testSubject2.setCode("TEST-002");
        testSubject2.setName("Test Subject 2");
        testSubject2.setDescription("Test description 2");
        testSubject2.setStatus(SubjectStatus.INACTIVE);
        testSubject2.setCreatedAt(OffsetDateTime.now(ZoneOffset.UTC));
    }

    @Test
    @DisplayName("Should save and retrieve subject successfully")
    void testSaveAndRetrieveSubject() {
        // When
        Subject saved = subjectRepository.save(testSubject1);
        entityManager.flush();
        entityManager.clear();

        // Then
        assertThat(saved.getId()).isNotNull();
        assertThat(saved.getCode()).isEqualTo("TEST-001");
        assertThat(saved.getName()).isEqualTo("Test Subject 1");
        assertThat(saved.getStatus()).isEqualTo(SubjectStatus.ACTIVE);

        // Verify retrieval
        Optional<Subject> retrieved = subjectRepository.findById(saved.getId());
        assertThat(retrieved).isPresent();
        assertThat(retrieved.get().getCode()).isEqualTo("TEST-001");
    }

    @Test
    @DisplayName("Should enforce unique code constraint")
    void testUniqueCodeConstraint() {
        // Given - Save first subject
        subjectRepository.save(testSubject1);
        entityManager.flush();

        // When - Try to save subject with duplicate code
        Subject duplicate = new Subject();
        duplicate.setCode("TEST-001"); // Same code
        duplicate.setName("Duplicate Subject");
        duplicate.setStatus(SubjectStatus.ACTIVE);
        duplicate.setCreatedAt(OffsetDateTime.now(ZoneOffset.UTC));

        // Then - Should throw exception
        try {
            subjectRepository.save(duplicate);
            entityManager.flush();
            // If no exception, fail the test
            assertThat(true).as("Should have thrown constraint violation exception").isFalse();
        } catch (Exception e) {
            // Expected - constraint violation
            assertThat(e).isNotNull();
        }
    }

    @Test
    @DisplayName("Should find subjects by status")
    void testFindByStatus() {
        // Given - Save both subjects
        subjectRepository.save(testSubject1); // ACTIVE
        subjectRepository.save(testSubject2); // INACTIVE
        entityManager.flush();
        entityManager.clear();

        // When - Find by ACTIVE status
        Pageable pageable = PageRequest.of(0, 10);
        Page<Subject> activeSubjects = subjectRepository.findByStatus(SubjectStatus.ACTIVE, pageable);

        // Then
        assertThat(activeSubjects.getContent()).hasSize(1);
        assertThat(activeSubjects.getContent().get(0).getCode()).isEqualTo("TEST-001");
        assertThat(activeSubjects.getContent().get(0).getStatus()).isEqualTo(SubjectStatus.ACTIVE);

        // When - Find by INACTIVE status
        Page<Subject> inactiveSubjects = subjectRepository.findByStatus(SubjectStatus.INACTIVE, pageable);

        // Then
        assertThat(inactiveSubjects.getContent()).hasSize(1);
        assertThat(inactiveSubjects.getContent().get(0).getCode()).isEqualTo("TEST-002");
    }

    @Test
    @DisplayName("Should check if code exists")
    void testExistsByCode() {
        // Given
        subjectRepository.save(testSubject1);
        entityManager.flush();

        // When & Then
        assertThat(subjectRepository.existsByCode("TEST-001")).isTrue();
        assertThat(subjectRepository.existsByCode("NON-EXISTENT")).isFalse();
    }

    @Test
    @DisplayName("Should update subject successfully")
    void testUpdateSubject() {
        // Given - Save subject
        Subject saved = subjectRepository.save(testSubject1);
        entityManager.flush();
        entityManager.clear();

        // When - Update subject
        Subject toUpdate = subjectRepository.findById(saved.getId()).orElseThrow();
        toUpdate.setName("Updated Name");
        toUpdate.setDescription("Updated Description");
        toUpdate.setStatus(SubjectStatus.INACTIVE);

        Subject updated = subjectRepository.save(toUpdate);
        entityManager.flush();
        entityManager.clear();

        // Then - Verify updates
        Subject retrieved = subjectRepository.findById(updated.getId()).orElseThrow();
        assertThat(retrieved.getName()).isEqualTo("Updated Name");
        assertThat(retrieved.getDescription()).isEqualTo("Updated Description");
        assertThat(retrieved.getStatus()).isEqualTo(SubjectStatus.INACTIVE);
        assertThat(retrieved.getCode()).isEqualTo("TEST-001"); // Code unchanged
    }

    @Test
    @DisplayName("Should delete subject without levels")
    void testDeleteSubjectWithoutLevels() {
        // Given
        Subject saved = subjectRepository.save(testSubject1);
        Long subjectId = saved.getId();
        entityManager.flush();
        entityManager.clear();

        // When
        subjectRepository.deleteById(subjectId);
        entityManager.flush();

        // Then
        Optional<Subject> deleted = subjectRepository.findById(subjectId);
        assertThat(deleted).isEmpty();
    }

    @Test
    @DisplayName("Should retrieve subject with levels relationship")
    void testSubjectWithLevels() {
        // Given - Save subject
        Subject saved = subjectRepository.save(testSubject1);
        entityManager.flush();

        // Create levels for this subject
        Level level1 = new Level();
        level1.setSubject(saved);
        level1.setCode("L1");
        level1.setName("Level 1");
        level1.setSortOrder(1);
        level1.setCreatedAt(OffsetDateTime.now(ZoneOffset.UTC));
        level1.setUpdatedAt(OffsetDateTime.now(ZoneOffset.UTC));

        Level level2 = new Level();
        level2.setSubject(saved);
        level2.setCode("L2");
        level2.setName("Level 2");
        level2.setSortOrder(2);
        level2.setCreatedAt(OffsetDateTime.now(ZoneOffset.UTC));
        level2.setUpdatedAt(OffsetDateTime.now(ZoneOffset.UTC));

        levelRepository.save(level1);
        levelRepository.save(level2);
        entityManager.flush();
        entityManager.clear();

        // When - Retrieve subject and count levels
        Subject retrieved = subjectRepository.findById(saved.getId()).orElseThrow();
        Long levelCount = levelRepository.countBySubjectId(retrieved.getId());

        // Then
        assertThat(levelCount).isEqualTo(2);
        assertThat(retrieved.getCode()).isEqualTo("TEST-001");
    }

    @Test
    @DisplayName("Should handle pagination correctly")
    void testPagination() {
        // Given - Save 5 subjects
        for (int i = 1; i <= 5; i++) {
            Subject subject = new Subject();
            subject.setCode("PAGE-" + String.format("%03d", i));
            subject.setName("Subject " + i);
            subject.setStatus(SubjectStatus.ACTIVE);
            subject.setCreatedAt(OffsetDateTime.now(ZoneOffset.UTC));
            subjectRepository.save(subject);
        }
        entityManager.flush();
        entityManager.clear();

        // When - Request first page with 2 items
        Pageable page1 = PageRequest.of(0, 2);
        Page<Subject> firstPage = subjectRepository.findByStatus(SubjectStatus.ACTIVE, page1);

        // Then - Verify first page
        assertThat(firstPage.getContent()).hasSize(2);
        assertThat(firstPage.getTotalElements()).isEqualTo(5);
        assertThat(firstPage.getTotalPages()).isEqualTo(3);
        assertThat(firstPage.isFirst()).isTrue();
        assertThat(firstPage.hasNext()).isTrue();

        // When - Request second page
        Pageable page2 = PageRequest.of(1, 2);
        Page<Subject> secondPage = subjectRepository.findByStatus(SubjectStatus.ACTIVE, page2);

        // Then - Verify second page
        assertThat(secondPage.getContent()).hasSize(2);
        assertThat(secondPage.isFirst()).isFalse();
        assertThat(secondPage.hasNext()).isTrue();

        // When - Request last page
        Pageable page3 = PageRequest.of(2, 2);
        Page<Subject> lastPage = subjectRepository.findByStatus(SubjectStatus.ACTIVE, page3);

        // Then - Verify last page
        assertThat(lastPage.getContent()).hasSize(1); // Only 1 item left
        assertThat(lastPage.isLast()).isTrue();
        assertThat(lastPage.hasNext()).isFalse();
    }
}

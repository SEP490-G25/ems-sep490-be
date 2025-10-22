package org.fyp.emssep490be.repositories;

import org.fyp.emssep490be.entities.Level;
import org.fyp.emssep490be.entities.Subject;
import org.fyp.emssep490be.entities.enums.SubjectStatus;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.boot.test.autoconfigure.orm.jpa.TestEntityManager;
import org.springframework.test.context.ActiveProfiles;

import java.time.OffsetDateTime;
import java.time.ZoneOffset;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * Integration tests for LevelRepository
 * Tests repository layer with real H2 database
 */
@DataJpaTest
@ActiveProfiles("test")
@DisplayName("Level Repository Integration Tests")
class LevelRepositoryIntegrationTest {

    @Autowired
    private LevelRepository levelRepository;

    @Autowired
    private SubjectRepository subjectRepository;

    @Autowired
    private TestEntityManager entityManager;

    private Subject testSubject1;
    private Subject testSubject2;
    private Level testLevel1;
    private Level testLevel2;

    @BeforeEach
    void setUp() {
        // Clean database
        levelRepository.deleteAll();
        subjectRepository.deleteAll();
        entityManager.flush();
        entityManager.clear();

        // Create test subjects
        testSubject1 = new Subject();
        testSubject1.setCode("SUBJ-001");
        testSubject1.setName("English");
        testSubject1.setStatus(SubjectStatus.ACTIVE);
        testSubject1.setCreatedAt(OffsetDateTime.now(ZoneOffset.UTC));
        testSubject1 = subjectRepository.save(testSubject1);

        testSubject2 = new Subject();
        testSubject2.setCode("SUBJ-002");
        testSubject2.setName("Math");
        testSubject2.setStatus(SubjectStatus.ACTIVE);
        testSubject2.setCreatedAt(OffsetDateTime.now(ZoneOffset.UTC));
        testSubject2 = subjectRepository.save(testSubject2);

        entityManager.flush();
        entityManager.clear();

        // Create test levels
        testLevel1 = new Level();
        testLevel1.setSubject(testSubject1);
        testLevel1.setCode("A1");
        testLevel1.setName("Beginner A1");
        testLevel1.setStandardType("CEFR");
        testLevel1.setExpectedDurationHours(60);
        testLevel1.setSortOrder(1);
        testLevel1.setDescription("Beginner level");
        testLevel1.setCreatedAt(OffsetDateTime.now(ZoneOffset.UTC));
        testLevel1.setUpdatedAt(OffsetDateTime.now(ZoneOffset.UTC));

        testLevel2 = new Level();
        testLevel2.setSubject(testSubject1);
        testLevel2.setCode("A2");
        testLevel2.setName("Elementary A2");
        testLevel2.setStandardType("CEFR");
        testLevel2.setExpectedDurationHours(80);
        testLevel2.setSortOrder(2);
        testLevel2.setDescription("Elementary level");
        testLevel2.setCreatedAt(OffsetDateTime.now(ZoneOffset.UTC));
        testLevel2.setUpdatedAt(OffsetDateTime.now(ZoneOffset.UTC));
    }

    @Test
    @DisplayName("Should save and retrieve level successfully")
    void testSaveAndRetrieveLevel() {
        // When
        Level saved = levelRepository.save(testLevel1);
        entityManager.flush();
        entityManager.clear();

        // Then
        assertThat(saved.getId()).isNotNull();
        assertThat(saved.getCode()).isEqualTo("A1");
        assertThat(saved.getName()).isEqualTo("Beginner A1");
        assertThat(saved.getSortOrder()).isEqualTo(1);
        assertThat(saved.getExpectedDurationHours()).isEqualTo(60);

        // Verify retrieval
        Optional<Level> retrieved = levelRepository.findById(saved.getId());
        assertThat(retrieved).isPresent();
        assertThat(retrieved.get().getCode()).isEqualTo("A1");
        assertThat(retrieved.get().getSubject().getId()).isEqualTo(testSubject1.getId());
    }

    @Test
    @DisplayName("Should enforce unique code per subject constraint")
    void testUniqueCodePerSubjectConstraint() {
        // Given - Save first level
        levelRepository.save(testLevel1);
        entityManager.flush();

        // When - Try to save level with duplicate code in same subject
        Level duplicate = new Level();
        duplicate.setSubject(testSubject1); // Same subject
        duplicate.setCode("A1"); // Same code
        duplicate.setName("Duplicate A1");
        duplicate.setSortOrder(10); // Different sort order
        duplicate.setCreatedAt(OffsetDateTime.now(ZoneOffset.UTC));
        duplicate.setUpdatedAt(OffsetDateTime.now(ZoneOffset.UTC));

        // Then - Should throw exception
        try {
            levelRepository.save(duplicate);
            entityManager.flush();
            assertThat(true).as("Should have thrown constraint violation exception").isFalse();
        } catch (Exception e) {
            assertThat(e).isNotNull();
        }
    }

    @Test
    @DisplayName("Should allow same code in different subjects")
    void testSameCodeInDifferentSubjects() {
        // Given - Save level A1 in subject 1
        levelRepository.save(testLevel1);
        entityManager.flush();

        // When - Save level A1 in subject 2
        Level levelInSubject2 = new Level();
        levelInSubject2.setSubject(testSubject2); // Different subject
        levelInSubject2.setCode("A1"); // Same code as testLevel1
        levelInSubject2.setName("Math A1");
        levelInSubject2.setSortOrder(1);
        levelInSubject2.setCreatedAt(OffsetDateTime.now(ZoneOffset.UTC));
        levelInSubject2.setUpdatedAt(OffsetDateTime.now(ZoneOffset.UTC));

        Level saved = levelRepository.save(levelInSubject2);
        entityManager.flush();

        // Then - Should succeed
        assertThat(saved.getId()).isNotNull();
        assertThat(saved.getCode()).isEqualTo("A1");
        assertThat(saved.getSubject().getId()).isEqualTo(testSubject2.getId());

        // Verify both levels exist
        assertThat(levelRepository.count()).isEqualTo(2);
    }

    @Test
    @DisplayName("Should allow multiple levels with different sortOrders")
    void testMultipleLevelsWithDifferentSortOrders() {
        // Given - Save first level with sortOrder 1
        levelRepository.save(testLevel1);  // sortOrder 1
        entityManager.flush();

        // When - Save level with different sortOrder in same subject
        Level level2 = new Level();
        level2.setSubject(testSubject1); // Same subject
        level2.setCode("B1"); // Different code
        level2.setName("Intermediate B1");
        level2.setSortOrder(2); // Different sortOrder
        level2.setCreatedAt(OffsetDateTime.now(ZoneOffset.UTC));
        level2.setUpdatedAt(OffsetDateTime.now(ZoneOffset.UTC));

        Level saved = levelRepository.save(level2);
        entityManager.flush();

        // Then - Should succeed
        assertThat(saved.getId()).isNotNull();
        assertThat(saved.getSortOrder()).isEqualTo(2);
        assertThat(levelRepository.count()).isEqualTo(2);
    }

    @Test
    @DisplayName("Should find levels by subject ordered by sortOrder")
    void testFindBySubjectIdOrderedBySortOrder() {
        // Given - Save 3 levels with different sortOrders
        Level level3 = new Level();
        level3.setSubject(testSubject1);
        level3.setCode("B1");
        level3.setName("Intermediate B1");
        level3.setSortOrder(3);
        level3.setCreatedAt(OffsetDateTime.now(ZoneOffset.UTC));
        level3.setUpdatedAt(OffsetDateTime.now(ZoneOffset.UTC));

        levelRepository.save(testLevel2); // sortOrder 2
        levelRepository.save(level3);     // sortOrder 3
        levelRepository.save(testLevel1); // sortOrder 1
        entityManager.flush();
        entityManager.clear();

        // When - Find levels by subject
        List<Level> levels = levelRepository.findBySubjectIdOrderBySortOrderAsc(testSubject1.getId());

        // Then - Should be ordered by sortOrder ascending
        assertThat(levels).hasSize(3);
        assertThat(levels.get(0).getSortOrder()).isEqualTo(1);
        assertThat(levels.get(0).getCode()).isEqualTo("A1");
        assertThat(levels.get(1).getSortOrder()).isEqualTo(2);
        assertThat(levels.get(1).getCode()).isEqualTo("A2");
        assertThat(levels.get(2).getSortOrder()).isEqualTo(3);
        assertThat(levels.get(2).getCode()).isEqualTo("B1");
    }

    @Test
    @DisplayName("Should check if code exists in subject")
    void testExistsByCodeAndSubjectId() {
        // Given
        levelRepository.save(testLevel1);
        entityManager.flush();

        // When & Then
        assertThat(levelRepository.existsByCodeAndSubjectId("A1", testSubject1.getId())).isTrue();
        assertThat(levelRepository.existsByCodeAndSubjectId("B1", testSubject1.getId())).isFalse();
        assertThat(levelRepository.existsByCodeAndSubjectId("A1", testSubject2.getId())).isFalse();
    }

    @Test
    @DisplayName("Should check if sortOrder exists in subject")
    void testExistsBySortOrderAndSubjectId() {
        // Given
        levelRepository.save(testLevel1); // sortOrder 1
        entityManager.flush();

        // When & Then
        assertThat(levelRepository.existsBySortOrderAndSubjectId(1, testSubject1.getId())).isTrue();
        assertThat(levelRepository.existsBySortOrderAndSubjectId(2, testSubject1.getId())).isFalse();
        assertThat(levelRepository.existsBySortOrderAndSubjectId(1, testSubject2.getId())).isFalse();
    }

    @Test
    @DisplayName("Should count levels by subject")
    void testCountBySubjectId() {
        // Given - Save 2 levels for subject 1, 1 level for subject 2
        levelRepository.save(testLevel1);
        levelRepository.save(testLevel2);

        Level levelForSubject2 = new Level();
        levelForSubject2.setSubject(testSubject2);
        levelForSubject2.setCode("L1");
        levelForSubject2.setName("Level 1");
        levelForSubject2.setSortOrder(1);
        levelForSubject2.setCreatedAt(OffsetDateTime.now(ZoneOffset.UTC));
        levelForSubject2.setUpdatedAt(OffsetDateTime.now(ZoneOffset.UTC));
        levelRepository.save(levelForSubject2);

        entityManager.flush();

        // When & Then
        assertThat(levelRepository.countBySubjectId(testSubject1.getId())).isEqualTo(2);
        assertThat(levelRepository.countBySubjectId(testSubject2.getId())).isEqualTo(1);
    }

    @Test
    @DisplayName("Should update level successfully")
    void testUpdateLevel() {
        // Given - Save level
        Level saved = levelRepository.save(testLevel1);
        entityManager.flush();
        entityManager.clear();

        // When - Update level
        Level toUpdate = levelRepository.findById(saved.getId()).orElseThrow();
        toUpdate.setName("Updated Name");
        toUpdate.setExpectedDurationHours(70);
        toUpdate.setSortOrder(5);

        Level updated = levelRepository.save(toUpdate);
        entityManager.flush();
        entityManager.clear();

        // Then - Verify updates
        Level retrieved = levelRepository.findById(updated.getId()).orElseThrow();
        assertThat(retrieved.getName()).isEqualTo("Updated Name");
        assertThat(retrieved.getExpectedDurationHours()).isEqualTo(70);
        assertThat(retrieved.getSortOrder()).isEqualTo(5);
        assertThat(retrieved.getCode()).isEqualTo("A1"); // Code unchanged
    }

    @Test
    @DisplayName("Should delete level successfully")
    void testDeleteLevel() {
        // Given
        Level saved = levelRepository.save(testLevel1);
        Long levelId = saved.getId();
        entityManager.flush();
        entityManager.clear();

        // When
        levelRepository.deleteById(levelId);
        entityManager.flush();

        // Then
        Optional<Level> deleted = levelRepository.findById(levelId);
        assertThat(deleted).isEmpty();
    }

    @Test
    @DisplayName("Should handle multiple levels with different attributes")
    void testMultipleLevelsWithDifferentAttributes() {
        // Given - Create levels with various attributes
        Level levelCEFR = new Level();
        levelCEFR.setSubject(testSubject1);
        levelCEFR.setCode("C1");
        levelCEFR.setName("Advanced C1");
        levelCEFR.setStandardType("CEFR");
        levelCEFR.setExpectedDurationHours(120);
        levelCEFR.setSortOrder(5);
        levelCEFR.setCreatedAt(OffsetDateTime.now(ZoneOffset.UTC));
        levelCEFR.setUpdatedAt(OffsetDateTime.now(ZoneOffset.UTC));

        Level levelJLPT = new Level();
        levelJLPT.setSubject(testSubject2);
        levelJLPT.setCode("N5");
        levelJLPT.setName("JLPT N5");
        levelJLPT.setStandardType("JLPT");
        levelJLPT.setExpectedDurationHours(60);
        levelJLPT.setSortOrder(1);
        levelJLPT.setDescription("Japanese beginner level");
        levelJLPT.setCreatedAt(OffsetDateTime.now(ZoneOffset.UTC));
        levelJLPT.setUpdatedAt(OffsetDateTime.now(ZoneOffset.UTC));

        Level levelWithoutOptionals = new Level();
        levelWithoutOptionals.setSubject(testSubject2);
        levelWithoutOptionals.setCode("N4");
        levelWithoutOptionals.setName("JLPT N4");
        levelWithoutOptionals.setSortOrder(2);
        levelWithoutOptionals.setCreatedAt(OffsetDateTime.now(ZoneOffset.UTC));
        levelWithoutOptionals.setUpdatedAt(OffsetDateTime.now(ZoneOffset.UTC));
        // No standardType, expectedDurationHours, description

        // When
        Level saved1 = levelRepository.save(levelCEFR);
        Level saved2 = levelRepository.save(levelJLPT);
        Level saved3 = levelRepository.save(levelWithoutOptionals);
        entityManager.flush();
        entityManager.clear();

        // Then
        assertThat(saved1.getStandardType()).isEqualTo("CEFR");
        assertThat(saved2.getStandardType()).isEqualTo("JLPT");
        assertThat(saved3.getStandardType()).isNull(); // Optional field

        assertThat(saved2.getDescription()).isEqualTo("Japanese beginner level");
        assertThat(saved3.getDescription()).isNull(); // Optional field

        // Verify retrieval
        List<Level> subject1Levels = levelRepository.findBySubjectIdOrderBySortOrderAsc(testSubject1.getId());
        List<Level> subject2Levels = levelRepository.findBySubjectIdOrderBySortOrderAsc(testSubject2.getId());

        assertThat(subject1Levels).hasSize(1);
        assertThat(subject2Levels).hasSize(2);
        assertThat(subject2Levels.get(0).getCode()).isEqualTo("N5"); // sortOrder 1
        assertThat(subject2Levels.get(1).getCode()).isEqualTo("N4"); // sortOrder 2
    }

    @Test
    @DisplayName("Should maintain subject relationship integrity")
    void testSubjectRelationshipIntegrity() {
        // Given - Save level
        Level saved = levelRepository.save(testLevel1);
        entityManager.flush();
        entityManager.clear();

        // When - Retrieve level and access subject
        Level retrieved = levelRepository.findById(saved.getId()).orElseThrow();
        Subject subject = retrieved.getSubject();

        // Then - Subject should be properly loaded
        assertThat(subject).isNotNull();
        assertThat(subject.getId()).isEqualTo(testSubject1.getId());
        assertThat(subject.getCode()).isEqualTo("SUBJ-001");
        assertThat(subject.getName()).isEqualTo("English");
    }
}

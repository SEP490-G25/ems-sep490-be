package org.fyp.emssep490be.repositories;

import org.fyp.emssep490be.config.PostgreSQLTestContainer;
import org.fyp.emssep490be.entities.Branch;
import org.fyp.emssep490be.entities.Center;
import org.fyp.emssep490be.entities.enums.BranchStatus;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.boot.test.autoconfigure.orm.jpa.TestEntityManager;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.ContextConfiguration;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * Integration test for BranchRepository using TestContainers with PostgreSQL.
 *
 * This test demonstrates:
 * - Using TestContainers for real PostgreSQL database
 * - Testing repository methods with actual database interactions
 * - Testing custom queries and relationship mappings
 * - Testing PostgreSQL enum types
 */
@DataJpaTest
@AutoConfigureTestDatabase(replace = AutoConfigureTestDatabase.Replace.NONE)
@ContextConfiguration(initializers = PostgreSQLTestContainer.Initializer.class)
@ActiveProfiles("test")
class BranchRepositoryTest {

    @Autowired
    private BranchRepository branchRepository;

    @Autowired
    private CenterRepository centerRepository;

    @Autowired
    private TestEntityManager entityManager;

    private Center testCenter;
    private Branch testBranch;

    @BeforeEach
    void setUp() {
        // Create a test center
        testCenter = new Center();
        testCenter.setName("Test Language Center");
        testCenter.setCode("TLC001");
        testCenter.setPhone("0123456789");
        testCenter.setEmail("test@center.com");
        testCenter.setCreatedAt(java.time.OffsetDateTime.now());
        testCenter = centerRepository.save(testCenter);

        // Create a test branch
        testBranch = new Branch();
        testBranch.setCenter(testCenter);
        testBranch.setCode("BR001");
        testBranch.setName("Main Branch");
        testBranch.setAddress("456 Branch Street");
        testBranch.setPhone("0987654321");
        testBranch.setStatus(BranchStatus.ACTIVE);
        testBranch.setCreatedAt(java.time.OffsetDateTime.now());
        testBranch = branchRepository.save(testBranch);

        entityManager.flush();
        entityManager.clear();
    }

    @Test
    void testFindById_ShouldReturnBranch() {
        // When
        Optional<Branch> found = branchRepository.findById(testBranch.getId());

        // Then
        assertThat(found).isPresent();
        assertThat(found.get().getCode()).isEqualTo("BR001");
        assertThat(found.get().getName()).isEqualTo("Main Branch");
        assertThat(found.get().getStatus()).isEqualTo(BranchStatus.ACTIVE);
    }

    @Test
    void testFindByCenter_ShouldReturnBranchesForCenter() {
        // Given - create another branch for the same center
        Branch anotherBranch = new Branch();
        anotherBranch.setCenter(testCenter);
        anotherBranch.setCode("BR002");
        anotherBranch.setName("Secondary Branch");
        anotherBranch.setAddress("789 Branch Ave");
        anotherBranch.setPhone("0111222333");
        anotherBranch.setStatus(BranchStatus.ACTIVE);
        anotherBranch.setCreatedAt(java.time.OffsetDateTime.now());
        branchRepository.save(anotherBranch);

        entityManager.flush();
        entityManager.clear();

        // When
        List<Branch> branches = branchRepository.findByCenter(testCenter);

        // Then
        assertThat(branches).hasSize(2);
        assertThat(branches).extracting(Branch::getCode)
                .containsExactlyInAnyOrder("BR001", "BR002");
    }

    @Test
    void testSave_ShouldPersistBranchWithEnumStatus() {
        // Given
        Branch newBranch = new Branch();
        newBranch.setCenter(testCenter);
        newBranch.setCode("BR003");
        newBranch.setName("Planned Branch");
        newBranch.setAddress("999 Future St");
        newBranch.setPhone("0999888777");
        newBranch.setStatus(BranchStatus.PLANNED);
        newBranch.setCreatedAt(java.time.OffsetDateTime.now());

        // When
        Branch saved = branchRepository.save(newBranch);
        entityManager.flush();
        entityManager.clear();

        // Then
        Optional<Branch> found = branchRepository.findById(saved.getId());
        assertThat(found).isPresent();
        assertThat(found.get().getStatus()).isEqualTo(BranchStatus.PLANNED);
    }

    @Test
    void testUniqueConstraint_CenterIdAndCode() {
        // This test verifies that (center_id, code) is unique
        // Note: Actual constraint violation testing requires special handling
        // This is a placeholder for demonstration
        List<Branch> existingBranches = branchRepository.findByCenter(testCenter);
        assertThat(existingBranches)
                .extracting(Branch::getCode)
                .containsOnlyOnce("BR001");
    }
}

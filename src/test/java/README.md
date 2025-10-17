# EMS-SEP490 Test Configuration Guide

## Overview

This guide explains how to write and run tests for the EMS (Education Management System) backend. The test infrastructure is fully configured and ready to use.

## Quick Start

### Run All Tests
```bash
./mvnw test
# or on Windows:
mvnw.cmd test
```

### Run Specific Test Class
```bash
./mvnw test -Dtest=BranchRepositoryTest
```

### Run Tests with Coverage Report
```bash
./mvnw clean test jacoco:report
```
Open the report: `target/site/jacoco/index.html`

---

## Test Types & When to Use Them

### 1. Repository Tests - For Database Operations

**Purpose:** Test database queries, entity mappings, and PostgreSQL-specific features.

**When to use:**
- Testing custom repository queries
- Validating entity relationships (ManyToOne, OneToMany, etc.)
- Testing PostgreSQL enums and arrays
- Verifying unique constraints

**Example:**
```java
@DataJpaTest
@AutoConfigureTestDatabase(replace = AutoConfigureTestDatabase.Replace.NONE)
@ContextConfiguration(initializers = PostgreSQLTestContainer.Initializer.class)
@ActiveProfiles("test")
class MyRepositoryTest {

    @Autowired
    private MyRepository repository;

    @Autowired
    private TestEntityManager entityManager;

    @BeforeEach
    void setUp() {
        // Create test data
        MyEntity entity = new MyEntity();
        entity.setName("Test");
        repository.save(entity);

        entityManager.flush();  // Persist to database
        entityManager.clear();  // Clear persistence context for clean state
    }

    @Test
    void testFindByName_ShouldReturnEntity() {
        // When
        Optional<MyEntity> found = repository.findByName("Test");

        // Then
        assertThat(found).isPresent();
        assertThat(found.get().getName()).isEqualTo("Test");
    }
}
```

**Key Points:**
- Uses **TestContainers** with real PostgreSQL 16 database
- Requires **Docker running** on your machine
- Use `@ContextConfiguration(initializers = PostgreSQLTestContainer.Initializer.class)`
- Always `flush()` then `clear()` entity manager after setup

---

### 2. Service Tests - For Business Logic

**Purpose:** Test service layer business logic in isolation using mocks.

**When to use:**
- Testing business rules and workflows
- Testing exception handling
- Verifying method interactions
- Testing calculations and validations

**Example:**
```java
@ExtendWith(MockitoExtension.class)
class MyServiceTest {

    @Mock
    private MyRepository repository;

    @InjectMocks
    private MyServiceImpl service;

    @Test
    void testGetById_WhenExists_ShouldReturnDTO() {
        // Given
        MyEntity entity = new MyEntity();
        entity.setId(1L);
        entity.setName("Test");
        when(repository.findById(1L)).thenReturn(Optional.of(entity));

        // When
        MyDTO result = service.getById(1L);

        // Then
        assertThat(result).isNotNull();
        assertThat(result.getName()).isEqualTo("Test");
        verify(repository, times(1)).findById(1L);
    }

    @Test
    void testGetById_WhenNotFound_ShouldThrowException() {
        // Given
        when(repository.findById(999L)).thenReturn(Optional.empty());

        // When & Then
        assertThatThrownBy(() -> service.getById(999L))
            .isInstanceOf(CustomException.class)
            .hasMessageContaining("not found");
    }
}
```

**Key Points:**
- Use `@Mock` for dependencies
- Use `@InjectMocks` for the service being tested
- Use `when(...).thenReturn(...)` for mocking
- Use `verify(...)` to check method calls
- No database required - runs fast

---

### 3. Controller Tests - For API Endpoints

**Purpose:** Test REST API endpoints, request/response handling, and security.

**When to use:**
- Testing HTTP endpoints
- Validating request/response formats
- Testing authentication/authorization
- Testing exception handling via GlobalExceptionHandler

**Example:**
```java
@WebMvcTest(MyController.class)
@ActiveProfiles("test")
class MyControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockBean
    private MyService service;

    @Test
    @WithMockUser(roles = "ADMIN")
    void testGetById_ShouldReturnOk() throws Exception {
        // Given
        MyDTO dto = new MyDTO();
        dto.setId(1L);
        dto.setName("Test");
        when(service.getById(1L)).thenReturn(dto);

        // When & Then
        mockMvc.perform(get("/api/my-resource/1")
                .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status").value(200))
                .andExpect(jsonPath("$.data.name").value("Test"));
    }

    @Test
    @WithMockUser(roles = "ADMIN")
    void testCreate_WithValidData_ShouldReturnCreated() throws Exception {
        // Given
        CreateRequest request = new CreateRequest();
        request.setName("New Item");

        MyDTO response = new MyDTO();
        response.setId(1L);
        when(service.create(any())).thenReturn(response);

        // When & Then
        mockMvc.perform(post("/api/my-resource")
                .with(csrf())
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.status").value(201));
    }
}
```

**Key Points:**
- Use `@WebMvcTest(MyController.class)` to test specific controller
- Use `@MockBean` to mock service dependencies
- Use `@WithMockUser` to bypass security for tests
- Use `csrf()` for POST/PUT/DELETE requests
- Use `jsonPath()` to validate response JSON

---

## Test Configuration Files

### 1. `application-test.yml`
Test-specific Spring Boot configuration using H2 in-memory database (fast for simple tests).

```yaml
spring:
  datasource:
    driver-class-name: org.h2.Driver
    url: jdbc:h2:mem:testdb;MODE=PostgreSQL
  jpa:
    hibernate:
      ddl-auto: create-drop  # Recreate schema for each test
```

### 2. `PostgreSQLTestContainer.java`
TestContainers configuration for tests requiring real PostgreSQL (enums, arrays, etc.).

**Usage:**
```java
@ContextConfiguration(initializers = PostgreSQLTestContainer.Initializer.class)
```

**Requirements:**
- Docker must be running
- Container image: `postgres:16-alpine`
- Container is reused across tests for performance

---

## Writing Good Tests

### Test Naming Convention

**Pattern:** `test{MethodName}_{Scenario}_{ExpectedResult}`

**Examples:**
- ✅ `testFindById_WhenBranchExists_ShouldReturnBranch`
- ✅ `testCreateBranch_WithInvalidData_ShouldThrowException`
- ❌ `test1` (too vague)
- ❌ `testBranch` (not descriptive)

### AAA Pattern (Arrange-Act-Assert)

Always structure tests with clear sections:

```java
@Test
void testExample() {
    // Given (Arrange) - Setup test data
    Branch branch = new Branch();
    branch.setName("Test Branch");
    when(repository.findById(1L)).thenReturn(Optional.of(branch));

    // When (Act) - Execute the method being tested
    BranchDTO result = service.getBranchById(1L);

    // Then (Assert) - Verify the results
    assertThat(result).isNotNull();
    assertThat(result.getName()).isEqualTo("Test Branch");
    verify(repository, times(1)).findById(1L);
}
```

### Use AssertJ for Fluent Assertions

```java
// ✅ GOOD - Fluent and readable
assertThat(branches)
    .hasSize(2)
    .extracting(Branch::getCode)
    .containsExactlyInAnyOrder("BR001", "BR002");

// ❌ AVOID - Less readable
assertEquals(2, branches.size());
assertTrue(branches.stream().anyMatch(b -> b.getCode().equals("BR001")));
```

---

## Critical: Entity Field Names

**⚠️ ALWAYS use the exact field names from entity classes!**

Lombok generates setters based on field names (e.g., `id` → `setId()`).

### Common Entity Field Examples

```java
// Center entity
Center center = new Center();
center.setId(1L);           // ✅ CORRECT
center.setCode("C001");     // ✅ CORRECT
center.setName("Center");   // ✅ CORRECT

// ❌ WRONG - These methods don't exist!
center.setCenterId(1L);
center.setCenterCode("C001");
center.setCenterName("Center");

// Branch entity
Branch branch = new Branch();
branch.setId(1L);           // ✅ CORRECT
branch.setCode("BR001");    // ✅ CORRECT
branch.setName("Branch");   // ✅ CORRECT

// ❌ WRONG - These methods don't exist!
branch.setBranchId(1L);
branch.setBranchCode("BR001");
branch.setEmail("test@test.com");  // Branch has NO email field!

// Date fields
entity.setCreatedAt(java.time.OffsetDateTime.now());  // ✅ CORRECT
entity.setCreatedAt(LocalDateTime.now());              // ❌ WRONG type
```

**Pro Tip:** Before writing tests, open the entity class and check the actual field names!

---

## What to Test

### Must Test ✅
- Complex business logic (session generation, conflict detection)
- Custom repository queries
- Exception scenarios and error handling
- Validation rules
- PostgreSQL-specific features (enums, arrays)
- Authentication/authorization logic
- Request/response transformations

### Can Skip ⏭️
- Simple getters/setters
- Auto-generated code (Lombok methods)
- Spring Framework internals
- Database connection (handled by Spring Boot)

---

## Code Coverage Goals

**Target Coverage:**
- Service Layer: **70%+**
- Repository Layer: **60%+**
- Controller Layer: **60%+**
- Overall Project: **50%+** (enforced by JaCoCo)

**View Coverage Report:**
```bash
./mvnw clean test jacoco:report
# Open: target/site/jacoco/index.html
```

**Current Status:**
- Coverage threshold temporarily set to 0% until services are implemented
- TODO: Increase to 50% in `pom.xml` line 164 when implementing services

---

## Common Issues & Solutions

### 1. TestContainers Not Starting
**Error:** `Could not start container`

**Cause:** Docker not running or insufficient permissions

**Solution:**
- Start Docker Desktop
- Ensure Docker is accessible from terminal: `docker ps`

---

### 2. Compilation Errors - Cannot Resolve Method
**Error:** `Cannot resolve method 'setBranchId'`, `Cannot resolve method 'setEmail' in 'Branch'`

**Cause:** Using incorrect field names that don't exist in entity classes

**Solution:**
1. Open the entity class (e.g., `Branch.java`, `Center.java`)
2. Check the actual field names
3. Use exact field names: `setId()` NOT `setBranchId()`

---

### 3. Lazy Loading Errors
**Error:** `LazyInitializationException`

**Cause:** Accessing lazy-loaded entities outside transaction

**Solution:**
```java
// In repository tests, use flush() and clear()
repository.save(entity);
entityManager.flush();
entityManager.clear();  // This triggers the error if lazy loading issues exist
```

---

### 4. Missing Repository Methods
**Error:** `Cannot resolve method 'findByCenter' in 'BranchRepository'`

**Cause:** Method not defined in repository interface

**Solution:**
Add the method to your repository:
```java
@Repository
public interface BranchRepository extends JpaRepository<Branch, Long> {
    List<Branch> findByCenter(Center center);  // Spring Data JPA auto-implements
}
```

---

### 5. Security Test Failures
**Error:** Tests fail with 401 Unauthorized

**Cause:** Security auto-configuration interfering with tests

**Solution:**
```java
@Test
@WithMockUser(roles = "ADMIN")  // Mock authenticated user
void testEndpoint() throws Exception {
    // Test code
}
```

---

## Test Examples

### Example 1: Repository Test (with PostgreSQL)

```java
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

    @BeforeEach
    void setUp() {
        testCenter = new Center();
        testCenter.setName("Test Center");
        testCenter.setCode("TC001");
        testCenter.setCreatedAt(OffsetDateTime.now());
        testCenter = centerRepository.save(testCenter);

        entityManager.flush();
        entityManager.clear();
    }

    @Test
    void testFindByCenter_ShouldReturnBranches() {
        // Given
        Branch branch1 = new Branch();
        branch1.setCenter(testCenter);
        branch1.setCode("BR001");
        branch1.setName("Branch 1");
        branch1.setStatus(BranchStatus.ACTIVE);
        branch1.setCreatedAt(OffsetDateTime.now());
        branchRepository.save(branch1);

        Branch branch2 = new Branch();
        branch2.setCenter(testCenter);
        branch2.setCode("BR002");
        branch2.setName("Branch 2");
        branch2.setStatus(BranchStatus.ACTIVE);
        branch2.setCreatedAt(OffsetDateTime.now());
        branchRepository.save(branch2);

        entityManager.flush();
        entityManager.clear();

        // When
        List<Branch> branches = branchRepository.findByCenter(testCenter);

        // Then
        assertThat(branches).hasSize(2);
        assertThat(branches).extracting(Branch::getCode)
            .containsExactlyInAnyOrder("BR001", "BR002");
    }
}
```

### Example 2: Service Test (with Mockito)

```java
@ExtendWith(MockitoExtension.class)
class BranchServiceTest {

    @Mock
    private BranchRepository branchRepository;

    @Mock
    private CenterRepository centerRepository;

    @InjectMocks
    private BranchServiceImpl branchService;

    @Test
    void testCreateBranch_WithValidData_ShouldReturnDTO() {
        // Given
        Center center = new Center();
        center.setId(1L);

        CreateBranchRequest request = new CreateBranchRequest();
        request.setCenterId(1L);
        request.setCode("BR001");
        request.setName("New Branch");

        Branch savedBranch = new Branch();
        savedBranch.setId(1L);
        savedBranch.setCode("BR001");
        savedBranch.setName("New Branch");

        when(centerRepository.findById(1L)).thenReturn(Optional.of(center));
        when(branchRepository.save(any(Branch.class))).thenReturn(savedBranch);

        // When
        BranchDTO result = branchService.createBranch(request);

        // Then
        assertThat(result).isNotNull();
        assertThat(result.getCode()).isEqualTo("BR001");
        verify(centerRepository, times(1)).findById(1L);
        verify(branchRepository, times(1)).save(any(Branch.class));
    }
}
```

---

## CI/CD Integration

Tests run automatically via GitHub Actions on every push and pull request.

**Pipeline:** `.github/workflows/ci.yml`

**Stages:**
1. ✅ Build project
2. ✅ Run all tests
3. ✅ Generate coverage report
4. ✅ Check coverage threshold
5. ✅ Build Docker image
6. ✅ Security dependency scan

**PR Comments:**
- Automatic coverage report posted to pull requests
- Shows coverage for changed files
- Highlights coverage drops

---

## Best Practices Summary

1. ✅ **Write tests alongside implementation** (not after!)
2. ✅ **Use descriptive test names** (`testX_WhenY_ShouldZ`)
3. ✅ **Follow AAA pattern** (Arrange-Act-Assert)
4. ✅ **Check entity field names** before writing tests
5. ✅ **Use AssertJ** for fluent assertions
6. ✅ **Mock external dependencies** in service tests
7. ✅ **Use real database** (TestContainers) for repository tests
8. ✅ **Clear entity manager** after setup for clean state
9. ✅ **Test edge cases** and exceptions
10. ✅ **Aim for 70%+ coverage** on service layer

---

## Resources

- [Spring Boot Testing Docs](https://docs.spring.io/spring-boot/docs/current/reference/html/features.html#features.testing)
- [TestContainers Docs](https://www.testcontainers.org/)
- [Mockito Docs](https://site.mockito.org/)
- [AssertJ Docs](https://assertj.github.io/doc/)
- [JaCoCo Coverage Docs](https://www.jacoco.org/jacoco/trunk/doc/)

---

## Getting Help

If you encounter issues:

1. Check the **Common Issues & Solutions** section above
2. Review example tests in `src/test/java/org/fyp/emssep490be/repositories/BranchRepositoryTest.java`
3. Ask the team in your dev channel
4. Check the actual entity class before writing tests!

**Happy Testing! 🧪**

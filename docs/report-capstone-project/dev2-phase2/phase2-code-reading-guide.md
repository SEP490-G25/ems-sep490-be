# 📖 CODE READING GUIDE - PHASE 2
## Course Management with Approval Workflow

**Purpose**: Guide for understanding the implementation of Course, CoursePhase, and CourseSession modules
**Developer**: DEV 2 - Academic Curriculum Lead
**Last Updated**: 2025-10-22

---

## 🎯 OVERVIEW

This guide helps you understand the implementation of **Phase 2: Course Management with Approval Workflow**. The system manages three hierarchical entities:

```
Course (curriculum template)
  └── CoursePhase (phases within course)
        └── CourseSession (individual session templates)
```

---

## 📂 FILE STRUCTURE

### Core Implementation Files

```
src/main/java/org/fyp/emssep490be/
├── services/
│   ├── course/
│   │   ├── CourseService.java (interface)
│   │   └── impl/
│   │       └── CourseServiceImpl.java ⭐ (480 lines, 7 methods)
│   ├── coursephase/
│   │   ├── CoursePhaseService.java (interface)
│   │   └── impl/
│   │       └── CoursePhaseServiceImpl.java ⭐ (215 lines, 4 methods)
│   └── coursesession/
│       ├── CourseSessionService.java (interface)
│       └── impl/
│           └── CourseSessionServiceImpl.java ⭐ (250 lines, 4 methods)
│
├── controllers/
│   ├── course/
│   │   └── CourseController.java ⭐ (208 lines, 7 endpoints)
│   ├── coursephase/
│   │   └── CoursePhaseController.java ⭐ (130 lines, 4 endpoints)
│   └── coursesession/
│       └── CourseSessionController.java ⭐ (130 lines, 4 endpoints)
│
├── dtos/
│   ├── course/
│   │   ├── CourseDTO.java
│   │   ├── CourseDetailDTO.java
│   │   ├── CreateCourseRequestDTO.java
│   │   ├── UpdateCourseRequestDTO.java
│   │   └── ApprovalRequestDTO.java
│   ├── coursephase/
│   │   ├── CoursePhaseDTO.java
│   │   ├── CreateCoursePhaseRequestDTO.java
│   │   └── UpdateCoursePhaseRequestDTO.java ⭐ (new)
│   └── coursesession/
│       ├── CourseSessionDTO.java
│       ├── CreateCourseSessionRequestDTO.java
│       └── UpdateCourseSessionRequestDTO.java ⭐ (new)
│
├── repositories/
│   ├── CourseRepository.java (enhanced)
│   ├── CoursePhaseRepository.java ⭐ (+1 method)
│   └── CourseSessionRepository.java ⭐ (+2 methods)
│
├── entities/
│   ├── Course.java
│   ├── CoursePhase.java
│   ├── CourseSession.java
│   └── enums/
│       └── Skill.java (GENERAL, READING, WRITING, SPEAKING, LISTENING)
│
└── exceptions/
    └── ErrorCode.java ⭐ (+19 error codes)
```

### Test Files

```
src/test/java/org/fyp/emssep490be/services/
├── course/impl/
│   └── CourseServiceImplTest.java ⭐ (22 tests)
├── coursephase/impl/
│   └── CoursePhaseServiceImplTest.java ⭐ (13 tests)
└── coursesession/impl/
    └── CourseSessionServiceImplTest.java ⭐ (15 tests)
```

---

## 🔍 RECOMMENDED READING ORDER

### Level 1: Understand the Domain Model (15 min)

**Start Here**:
1. `entities/Course.java` - Main course entity with approval fields
2. `entities/CoursePhase.java` - Phase entity with sort_order
3. `entities/CourseSession.java` - Session with skill_set array
4. `entities/enums/Skill.java` - Skill enum (5 values)

**Key Relationships**:
- Course → Many CoursePhases (1:N)
- CoursePhase → Many CourseSessions (1:N)
- Course has approval fields: approved_by_manager, approved_at, rejection_reason

### Level 2: Review DTOs and Requests (10 min)

**Read in order**:
1. `dtos/course/CourseDTO.java` - Basic course info
2. `dtos/course/CourseDetailDTO.java` - Detailed with phases
3. `dtos/course/CreateCourseRequestDTO.java` - Create request with validation
4. `dtos/course/UpdateCourseRequestDTO.java` - Update request
5. `dtos/course/ApprovalRequestDTO.java` - Approval/rejection request

**Similar pattern for Phase and Session DTOs**

### Level 3: Service Layer - Business Logic (45 min)

#### 3.1. CourseServiceImpl ⭐ (Most Complex)
**File**: `services/course/impl/CourseServiceImpl.java` (480 lines)

**Reading Order**:
1. **Constructor & Dependencies** (lines 1-50)
   - Injected repositories
   - @Service, @Transactional annotations

2. **CRUD Methods**:
   - `getAllCourses()` (lines 60-95) → Pagination + filtering
   - `getCourseById()` (lines 100-130) → Load with phases
   - `createCourse()` (lines 135-210) ⭐ **Most important**
     - Subject/Level validation
     - Unique code check
     - Total hours validation (10% tolerance)
     - MD5 hash calculation
   - `updateCourse()` (lines 215-270) → Status-based restrictions
   - `deleteCourse()` (lines 275-310) → Soft delete with usage check

3. **Approval Workflow Methods** ⭐:
   - `submitCourseForApproval()` (lines 315-355)
     - Check phase count > 0
     - Set submitted_at timestamp
   - `approveCourse()` (lines 360-425) **Critical**
     - Action validation (approve/reject)
     - Status transitions
     - Approver tracking

4. **Helper Methods**:
   - `convertToDTO()` (lines 430-455)
   - `convertToDetailDTO()` (lines 460-490)
   - `calculateHashChecksum()` (lines 495-510) - MD5 calculation
   - `getCurrentUser()` (lines 515-525) - SecurityContext

**Key Business Rules** ⚠️:
```java
// Total hours validation (10% tolerance)
double calculated = weeks * sessionsPerWeek * hoursPerSession;
double tolerance = 0.1;
if (Math.abs(totalHours - calculated) / calculated > tolerance) {
    throw new CustomException(ErrorCode.INVALID_TOTAL_HOURS);
}

// Only draft or rejected courses can be updated
if (!"draft".equals(status) && approvedByManager != null) {
    throw new CustomException(ErrorCode.COURSE_CANNOT_BE_UPDATED);
}

// Approval: set status = 'active'
course.setApprovedByManager(currentUser);
course.setApprovedAt(now);
course.setStatus("active");

// Rejection: revert to 'draft', clear approval
course.setRejectionReason(reason);
course.setStatus("draft");
course.setApprovedByManager(null);
course.setApprovedAt(null);
```

#### 3.2. CoursePhaseServiceImpl
**File**: `services/coursephase/impl/CoursePhaseServiceImpl.java` (215 lines)

**Structure**:
1. `getPhasesByCourse()` (lines 45-65) → List ordered by sort_order
2. `createPhase()` (lines 70-115) → Draft validation + unique constraint
3. `updatePhase()` (lines 120-155) → Status check
4. `deletePhase()` (lines 160-190) → Check sessions count
5. `convertToDTO()` (lines 195-215) → Include sessions count

**Key Validations**:
```java
// Only draft courses can be modified
if (!"draft".equals(course.getStatus())) {
    throw new CustomException(ErrorCode.COURSE_CANNOT_BE_MODIFIED);
}

// Unique constraint: (course_id, phase_number)
if (exists(phaseNumber, courseId)) {
    throw new CustomException(ErrorCode.PHASE_NUMBER_DUPLICATE);
}

// Cannot delete if has sessions
if (countSessions(phaseId) > 0) {
    throw new CustomException(ErrorCode.PHASE_HAS_SESSIONS);
}
```

#### 3.3. CourseSessionServiceImpl
**File**: `services/coursesession/impl/CourseSessionServiceImpl.java` (250 lines)

**Structure**:
1. `getSessionsByPhase()` (lines 45-65) → List ordered by sequence_no
2. `createSession()` (lines 70-115) ⭐ → Skill set validation
3. `updateSession()` (lines 120-160) → Selective field update
4. `deleteSession()` (lines 165-195) → Usage check in SessionEntity
5. `validateAndConvertSkillSet()` (lines 200-220) ⭐ **Important**
6. `convertToDTO()` (lines 225-250) → Skill enum to string

**Skill Set Validation** ⚠️:
```java
private List<Skill> validateAndConvertSkillSet(List<String> skillStrings) {
    List<Skill> skills = new ArrayList<>();
    for (String skillStr : skillStrings) {
        try {
            Skill skill = Skill.valueOf(skillStr.toUpperCase());
            skills.add(skill);
        } catch (IllegalArgumentException e) {
            throw new CustomException(ErrorCode.INVALID_SKILL_SET);
        }
    }
    return skills;
}

// Valid skills: GENERAL, READING, WRITING, SPEAKING, LISTENING
// DTO uses List<String>, Entity uses List<Skill>
```

### Level 4: Controller Layer - REST API (30 min)

#### 4.1. CourseController
**File**: `controllers/course/CourseController.java` (208 lines)

**Endpoints Overview**:
```java
@RestController
@RequestMapping("/api/v1/courses")
public class CourseController {

    // 1. GET /api/v1/courses - List with pagination
    @GetMapping
    @PreAuthorize("hasAnyRole('ADMIN', 'MANAGER', 'CENTER_HEAD', 'ACADEMIC_STAFF', 'SUBJECT_LEADER')")

    // 2. GET /api/v1/courses/{id} - Get detailed
    @GetMapping("/{id}")

    // 3. POST /api/v1/courses - Create
    @PostMapping
    @PreAuthorize("hasRole('SUBJECT_LEADER')")

    // 4. PUT /api/v1/courses/{id} - Update
    @PutMapping("/{id}")
    @PreAuthorize("hasRole('SUBJECT_LEADER')")

    // 5. POST /api/v1/courses/{id}/submit - Submit for approval
    @PostMapping("/{id}/submit")
    @PreAuthorize("hasRole('SUBJECT_LEADER')")

    // 6. POST /api/v1/courses/{id}/approve - Approve/Reject
    @PostMapping("/{id}/approve")
    @PreAuthorize("hasAnyRole('MANAGER', 'CENTER_HEAD')")

    // 7. DELETE /api/v1/courses/{id} - Delete
    @DeleteMapping("/{id}")
    @PreAuthorize("hasAnyRole('ADMIN', 'SUBJECT_LEADER')")
}
```

**Response Pattern**:
All endpoints return `ResponseObject<T>`:
```java
ResponseObject.<CourseDTO>builder()
    .status(HttpStatus.OK.value())
    .message("Course created successfully")
    .data(courseDTO)
    .build()
```

#### 4.2. CoursePhaseController & CourseSessionController
Similar structure with:
- RESTful endpoint design
- Swagger annotations (@Operation, @Tag)
- Role-based authorization
- ResponseObject wrapper

### Level 5: Repository Layer (10 min)

**Enhanced Repositories**:

1. **CourseRepository.java**:
```java
Page<Course> findByFilters(Long subjectId, Long levelId,
                           String status, Boolean approved,
                           Pageable pageable);
```

2. **CoursePhaseRepository.java** ⭐:
```java
long countByCourseId(Long courseId);  // NEW - Count phases
```

3. **CourseSessionRepository.java** ⭐:
```java
long countByPhaseId(Long phaseId);  // NEW - Count sessions

@Query("SELECT COUNT(s) FROM SessionEntity s WHERE s.courseSession.id = :id")
long countSessionUsages(@Param("id") Long id);  // NEW - Check usage
```

### Level 6: Error Handling (5 min)

**File**: `exceptions/ErrorCode.java`

**New Error Codes** (19 codes):
```java
// Course errors (1240-1251)
COURSE_NOT_FOUND(1240, "Course not found"),
COURSE_CODE_DUPLICATE(1242, "Course code already exists"),
COURSE_CANNOT_BE_MODIFIED(1244, "Course cannot be modified"),
COURSE_IN_USE(1245, "Cannot delete course in use"),
COURSE_NO_PHASES(1248, "Course must have at least one phase"),
INVALID_ACTION(1249, "Invalid approval action"),
REJECTION_REASON_REQUIRED(1250, "Rejection reason is required"),
INVALID_TOTAL_HOURS(1251, "Total hours calculation is inconsistent"),

// Phase errors (1270-1272)
PHASE_NOT_FOUND(1270, "Phase not found"),
PHASE_NUMBER_DUPLICATE(1271, "Phase number already exists"),
PHASE_HAS_SESSIONS(1272, "Cannot delete phase that has sessions"),

// Session errors (1290-1293)
SESSION_NOT_FOUND(1290, "Session not found"),
SESSION_SEQUENCE_DUPLICATE(1291, "Sequence number already exists"),
SESSION_IN_USE(1292, "Cannot delete session in use"),
INVALID_SKILL_SET(1293, "Invalid skill set value(s)"),
```

---

## 🧪 UNDERSTANDING TESTS

### Test Structure Pattern

All test files follow this structure:
```java
@ExtendWith(MockitoExtension.class)
@MockitoSettings(strictness = Strictness.LENIENT)  // Avoid stubbing errors
class ServiceImplTest {

    @Mock private Repository1 repo1;
    @Mock private Repository2 repo2;
    @InjectMocks private ServiceImpl service;

    @BeforeEach
    void setUp() {
        // Setup test data
    }

    @Test
    @DisplayName("Should do something successfully")
    void testMethod_Success() {
        // Arrange - Setup mocks
        // Act - Call service method
        // Assert - Verify results
    }
}
```

### Key Test Files

#### 1. CourseServiceImplTest (22 tests)
**File**: `test/.../CourseServiceImplTest.java`

**Test Groups**:
- **getAllCourses**: 2 tests (no filters, with filters)
- **getCourseById**: 2 tests (success, not found)
- **createCourse**: 5 tests ⭐ **Most comprehensive**
  - Success case
  - Subject not found
  - Level not found
  - Duplicate code
  - Invalid total hours
- **updateCourse**: 3 tests
- **submitCourse**: 3 tests (including phase validation)
- **approveCourse**: 4 tests ⭐ **Workflow validation**
- **deleteCourse**: 3 tests

**Example Test**:
```java
@Test
@DisplayName("Should create course successfully")
void testCreateCourse_Success() {
    // Arrange
    CreateCourseRequestDTO request = new CreateCourseRequestDTO();
    request.setSubjectId(1L);
    request.setLevelId(1L);
    request.setCode("ENG-A1-V1");
    // ... set other fields

    when(subjectRepository.findById(1L)).thenReturn(Optional.of(testSubject));
    when(levelRepository.findById(1L)).thenReturn(Optional.of(testLevel));
    when(courseRepository.existsByCode("ENG-A1-V1")).thenReturn(false);
    when(courseRepository.save(any(Course.class))).thenReturn(testCourse);

    // Act
    CourseDTO result = courseService.createCourse(request);

    // Assert
    assertThat(result).isNotNull();
    assertThat(result.getCode()).isEqualTo("ENG-A1-V1");
    verify(courseRepository).save(any(Course.class));
}
```

#### 2. CoursePhaseServiceImplTest (13 tests)
Covers all CRUD + validation scenarios

#### 3. CourseSessionServiceImplTest (15 tests)
Includes skill set validation tests

---

## 🔑 KEY CONCEPTS TO UNDERSTAND

### 1. Approval Workflow State Machine

```
┌──────────┐
│  DRAFT   │ ← Initial state
└────┬─────┘
     │ submitCourseForApproval()
     ▼
┌──────────┐
│SUBMITTED │ (submitted_at set)
└────┬─────┘
     │ approveCourse()
     ├─────────────┬─────────────┐
     ▼             ▼             ▼
┌─────────┐   ┌─────────┐   ┌──────────┐
│ ACTIVE  │   │  DRAFT  │   │ REJECTED │
│(approved)│   │(rejected)│   │  (with   │
└─────────┘   └─────────┘   │ reason)  │
                             └──────────┘
                             Can edit & resubmit
```

### 2. Hierarchical Validation

```
Course (draft check)
  ├── Can only modify if status = 'draft'
  ├── Phase count must be > 0 before submit
  └── Total hours must match calculation (±10%)

CoursePhase
  ├── Inherits course draft check
  ├── Unique (course_id, phase_number)
  └── Cannot delete if has sessions

CourseSession
  ├── Inherits course draft check via phase
  ├── Unique (phase_id, sequence_no)
  ├── Skill set must be valid enums
  └── Cannot delete if used in SessionEntity
```

### 3. MD5 Hash Checksum

**Purpose**: Version control and change detection
**Implementation**: `calculateHashChecksum()`
```java
private String calculateHashChecksum(Course course) {
    String content = course.getCode() + course.getName() +
                     course.getTotalHours() + course.getVersion();
    return DigestUtils.md5Hex(content);
}
```

### 4. Skill Set Conversion

**Challenge**: DTO uses `List<String>`, Entity uses `List<Skill>` enum

**Solution**:
- **DTO → Entity**: `validateAndConvertSkillSet()` with try-catch
- **Entity → DTO**: `skill.name()` to convert enum to string
- **Validation**: Throw `INVALID_SKILL_SET` for invalid values

### 5. Soft Delete Pattern

```java
public void deleteCourse(Long id) {
    Course course = findById(id);

    // Check usage
    long classCount = classRepository.countByCourseId(id);
    if (classCount > 0) {
        throw new CustomException(ErrorCode.COURSE_IN_USE);
    }

    // Soft delete: set status = 'inactive'
    course.setStatus("inactive");
    courseRepository.save(course);
}
```

---

## 🎯 IMPORTANT CODE LOCATIONS

### Critical Business Logic

1. **Course Creation Validation** ⚠️
   - File: `CourseServiceImpl.java`
   - Lines: 135-210
   - Contains: Total hours check, hash calculation

2. **Approval Workflow** ⚠️
   - File: `CourseServiceImpl.java`
   - Lines: 360-425
   - Contains: State transitions, approver tracking

3. **Skill Set Validation** ⚠️
   - File: `CourseSessionServiceImpl.java`
   - Lines: 200-220
   - Contains: String to Enum conversion with validation

4. **Usage Detection** ⚠️
   - CoursePhase: Check sessions count
   - CourseSession: Query SessionEntity usage
   - Course: Query ClassEntity usage

### Authorization Points

All write operations check:
- **SUBJECT_LEADER**: Can create, update, delete, submit
- **MANAGER/CENTER_HEAD**: Can approve, reject
- **Draft Status**: Required for all modifications

---

## 📊 DATA FLOW EXAMPLES

### Example 1: Create Course Flow

```
User (SUBJECT_LEADER)
  │
  ├─→ POST /api/v1/courses
  │   Body: CreateCourseRequestDTO
  │
  ▼
CourseController.createCourse()
  │
  ├─→ @Valid validation (DTO constraints)
  │
  ▼
CourseService.createCourse()
  │
  ├─→ Validate subject exists
  ├─→ Validate level exists
  ├─→ Check unique code
  ├─→ Validate total hours (±10%)
  ├─→ Calculate MD5 hash
  ├─→ Set status = 'draft'
  ├─→ Set createdBy = current user
  │
  ▼
CourseRepository.save()
  │
  ▼
Return CourseDTO
```

### Example 2: Approval Flow

```
Subject Leader:
  1. Create Course (status='draft')
  2. Add Phases
  3. Submit for Approval
     - Check: phases.count > 0
     - Set: submitted_at

Manager/Center Head:
  4. Review Course
  5. Approve OR Reject

     If APPROVE:
       - Set: approvedByManager
       - Set: approvedAt
       - Set: status='active'
       - Clear: rejectionReason

     If REJECT:
       - Set: rejectionReason
       - Set: status='draft'
       - Clear: approvedByManager, approvedAt
       - Subject Leader can edit and resubmit
```

---

## 🐛 COMMON ISSUES & SOLUTIONS

### Issue 1: COURSE_CANNOT_BE_MODIFIED
**Cause**: Trying to modify non-draft course
**Solution**: Only modify courses with status='draft' or rejected

### Issue 2: INVALID_TOTAL_HOURS
**Cause**: Total hours doesn't match calculation
**Formula**: `total_hours = weeks × sessions_per_week × hours_per_session`
**Tolerance**: ±10%

### Issue 3: INVALID_SKILL_SET
**Cause**: Invalid skill name in request
**Valid Values**: GENERAL, READING, WRITING, SPEAKING, LISTENING (case-insensitive)

### Issue 4: PHASE_HAS_SESSIONS
**Cause**: Trying to delete phase with sessions
**Solution**: Delete sessions first, then delete phase

### Issue 5: SESSION_IN_USE
**Cause**: CourseSession is referenced by SessionEntity
**Solution**: Cannot delete - session is in use by actual class sessions

---

## 📚 ADDITIONAL RESOURCES

### Related Documentation
- `docs/dev2-phase2-plan.md` - Implementation plan
- `docs/dev2-phase2-progress.md` - Progress tracking
- `docs/openapi/openapi-academic.yaml` - API specification
- `docs/api-design.md` - Overall API design

### Database Schema
- Tables: `course`, `course_phase`, `course_session`
- Key constraints: Unique codes, phase numbers, sequence numbers
- Soft delete: Status field instead of actual deletion

### Testing Resources
- Run all tests: `./mvnw test`
- Run specific: `./mvnw test -Dtest=CourseServiceImplTest`
- Coverage report: `target/site/jacoco/index.html`

---

## 🎓 LEARNING PATH

**For New Developers**:
1. Start with entities → Understand data model
2. Read DTOs → Understand API contracts
3. Study CourseService → Learn business logic
4. Review tests → Understand expected behavior
5. Try CourseController → See it all together

**For Code Review**:
1. Check validation logic
2. Verify error handling
3. Review test coverage
4. Validate security annotations
5. Check transaction boundaries

**For Extension**:
1. Understand current patterns
2. Follow same structure for new features
3. Add corresponding tests
4. Update documentation
5. Consider backward compatibility

---

**Document Prepared By**: DEV 2 - Academic Curriculum Lead
**Last Updated**: 2025-10-22
**Version**: 1.0

# 📋 KẾ HOẠCH THỰC HIỆN - DEV 2 PHASE 2: COURSE MANAGEMENT VỚI APPROVAL WORKFLOW

**Phiên bản**: 1.0
**Ngày tạo**: 2025-10-22
**Timeline**: Week 2-4 (3 tuần)
**Dev**: DEV 2 - Academic Curriculum Lead

---

## 📊 TỔNG QUAN

### Mục tiêu Phase 2
Implement đầy đủ Course Management system với đầy đủ CRUD operations, Approval Workflow, và Course Structure (Phase & Session Templates).

### Dependencies
- ✅ **Phase 1 COMPLETED**: Subject & Level management đã hoàn thành
- ✅ **Entities**: Course, CoursePhase, CourseSession đã có sẵn
- ✅ **Repositories**: CourseRepository, CoursePhaseRepository, CourseSessionRepository đã có
- ✅ **DTOs**: Tất cả DTOs cần thiết đã được define
- ⏳ **Service Implementations**: Cần implement từ stubs hiện tại

### Deliverables
- ✅ Course CRUD API hoàn chỉnh
- ✅ Course Approval Workflow (submit → approve/reject)
- ✅ CoursePhase CRUD API
- ✅ CourseSession CRUD API
- ✅ Validation & Business Rules
- ✅ Unit Tests (coverage > 80%)
- ✅ Integration Tests

---

## 🎯 PHÂN CHIA CÔNG VIỆC

### **Task 1: Course CRUD (Week 2 - Days 1-3)**

#### 1.1. Implement CourseService CRUD Methods

**File**: `src/main/java/org/fyp/emssep490be/services/course/impl/CourseServiceImpl.java`

**Methods cần implement**:

1. **`getAllCourses()`**
   - **Mục đích**: Lấy danh sách courses với pagination và filtering
   - **Input**:
     - `subjectId` (Long, optional)
     - `levelId` (Long, optional)
     - `status` (String, optional)
     - `approved` (Boolean, optional)
     - `page`, `limit` (Integer)
   - **Output**: `PagedResponseDTO<CourseDTO>`
   - **Logic**:
     - Build dynamic query dựa trên filters
     - Support pagination với Spring Data JPA `Pageable`
     - Map entities sang DTOs
   - **Validations**:
     - Page >= 0, limit > 0
     - Status phải match enum values nếu có

2. **`getCourseById()`**
   - **Mục đích**: Lấy chi tiết 1 course bao gồm phases, CLOs, materials
   - **Input**: `id` (Long)
   - **Output**: `CourseDetailDTO`
   - **Logic**:
     - Query course với JOIN FETCH để tránh N+1
     - Load relationships: phases, clos, materials
     - Map sang CourseDetailDTO với đầy đủ thông tin
   - **Validations**:
     - Course phải tồn tại → throw `ErrorCode.COURSE_NOT_FOUND`

3. **`createCourse()`**
   - **Mục đích**: Tạo course mới
   - **Input**: `CreateCourseRequestDTO`
   - **Output**: `CourseDTO`
   - **Logic**:
     - Validate subject_id và level_id tồn tại
     - Check unique constraint: `unique(subject_id, level_id, version)`
     - Calculate `hash_checksum` (MD5 hash của course content)
     - Set `status = 'draft'` (pending approval)
     - Set `created_by` = current user
     - Set `created_at` = now
     - Save course entity
   - **Validations**:
     - Subject phải tồn tại → `ErrorCode.SUBJECT_NOT_FOUND`
     - Level phải tồn tại → `ErrorCode.LEVEL_NOT_FOUND`
     - Unique constraint: `(subject_id, level_id, version)` → `ErrorCode.COURSE_ALREADY_EXISTS`
     - Required fields: code, name, total_hours, duration_weeks, session_per_week, hours_per_session
     - Business rule: `total_hours = duration_weeks * session_per_week * hours_per_session` (tolerance check)

4. **`updateCourse()`**
   - **Mục đích**: Cập nhật course (chỉ cho phép update khi status='draft' hoặc 'rejected')
   - **Input**: `id` (Long), `UpdateCourseRequestDTO`
   - **Output**: `CourseDTO`
   - **Logic**:
     - Check course tồn tại
     - **Business Rule**: Chỉ cho phép update khi `status = 'draft'` hoặc course bị rejected
     - Update các fields: name, description, prerequisites, target_audience, teaching_methods, status
     - Recalculate `hash_checksum` nếu content thay đổi
     - Set `updated_at` = now
     - Save entity
   - **Validations**:
     - Course phải tồn tại → `ErrorCode.COURSE_NOT_FOUND`
     - Status phải là 'draft' hoặc 'rejected' → `ErrorCode.COURSE_CANNOT_BE_UPDATED`
     - Nếu course đã approved, không cho update critical fields (total_hours, duration, etc.)

5. **`deleteCourse()`**
   - **Mục đích**: Soft delete course
   - **Input**: `id` (Long)
   - **Output**: void
   - **Logic**:
     - Check course tồn tại
     - **Business Rule**: Chỉ xóa được khi chưa có class nào sử dụng course này
     - Check `classRepository.existsByCourseId(id)`
     - Nếu có class → throw error
     - Nếu không → set `status = 'inactive'` (soft delete)
   - **Validations**:
     - Course phải tồn tại → `ErrorCode.COURSE_NOT_FOUND`
     - Course chưa được sử dụng → `ErrorCode.COURSE_IN_USE`

#### 1.2. Implement CourseController Endpoints

**File**: `src/main/java/org/fyp/emssep490be/controllers/course/CourseController.java`

**Endpoints cần implement**:

- `GET /api/courses` - getAllCourses
  - Auth: `@PreAuthorize("hasAnyRole('ADMIN', 'MANAGER', 'SUBJECT_LEADER', 'ACADEMIC_STAFF')")`
  - Params: `subjectId`, `levelId`, `status`, `approved`, `page`, `limit`

- `GET /api/courses/{id}` - getCourseById
  - Auth: `@PreAuthorize("hasAnyRole('ADMIN', 'MANAGER', 'SUBJECT_LEADER', 'ACADEMIC_STAFF')")`

- `POST /api/courses` - createCourse
  - Auth: `@PreAuthorize("hasRole('SUBJECT_LEADER')")`
  - Body: `@Valid CreateCourseRequestDTO`

- `PUT /api/courses/{id}` - updateCourse
  - Auth: `@PreAuthorize("hasRole('SUBJECT_LEADER')")`
  - Body: `@Valid UpdateCourseRequestDTO`

- `DELETE /api/courses/{id}` - deleteCourse
  - Auth: `@PreAuthorize("hasAnyRole('ADMIN', 'SUBJECT_LEADER')")`

**Response Format**: Tất cả đều return `ResponseObject<T>` wrapper

#### 1.3. Testing

**Unit Tests** (`CourseServiceImplTest.java`):
- ✅ testGetAllCourses_WithFilters()
- ✅ testGetAllCourses_Pagination()
- ✅ testGetCourseById_Success()
- ✅ testGetCourseById_NotFound()
- ✅ testCreateCourse_Success()
- ✅ testCreateCourse_DuplicateCode()
- ✅ testCreateCourse_SubjectNotFound()
- ✅ testCreateCourse_LevelNotFound()
- ✅ testCreateCourse_InvalidTotalHours()
- ✅ testUpdateCourse_Success()
- ✅ testUpdateCourse_NotFound()
- ✅ testUpdateCourse_StatusNotAllowed()
- ✅ testDeleteCourse_Success()
- ✅ testDeleteCourse_InUse()

**Coverage Target**: > 80%

---

### **Task 2: Approval Workflow (Week 2 - Days 4-5)**

#### 2.1. Implement Workflow Methods

**File**: `CourseServiceImpl.java`

1. **`submitCourseForApproval()`**
   - **Mục đích**: Subject Leader submit course để Manager approve
   - **Input**: `id` (Long)
   - **Output**: `CourseDTO`
   - **Logic**:
     - Check course tồn tại và status = 'draft'
     - **Business Rule**: Course phải có ít nhất 1 CoursePhase trước khi submit
     - Check `coursePhaseRepository.countByCourseId(id) > 0`
     - Update `submitted_at` = now
     - Keep status = 'draft' (waiting for approval)
     - Save entity
   - **Validations**:
     - Course phải tồn tại → `ErrorCode.COURSE_NOT_FOUND`
     - Status phải là 'draft' → `ErrorCode.COURSE_ALREADY_SUBMITTED`
     - Course phải có ít nhất 1 phase → `ErrorCode.COURSE_NO_PHASES`

2. **`approveCourse()`**
   - **Mục đích**: Manager approve hoặc reject course
   - **Input**: `id` (Long), `ApprovalRequestDTO`
   - **Output**: `CourseDTO`
   - **Logic**:
     - Check course tồn tại và đã submitted
     - **ApprovalRequestDTO** có 2 fields:
       - `action`: "approve" hoặc "reject"
       - `rejectionReason`: String (required nếu action = "reject")
     - **Nếu approve**:
       - Set `approved_by_manager` = current user ID
       - Set `approved_at` = now
       - Set `status` = 'active'
       - Clear `rejection_reason`
     - **Nếu reject**:
       - Set `rejection_reason` = request.rejectionReason
       - Set `status` = 'draft' (cho phép edit lại)
       - Clear `approved_by_manager` và `approved_at`
     - Save entity
   - **Validations**:
     - Course phải tồn tại → `ErrorCode.COURSE_NOT_FOUND`
     - Course đã được submit (`submitted_at` != null) → `ErrorCode.COURSE_NOT_SUBMITTED`
     - Action phải là "approve" hoặc "reject" → `ErrorCode.INVALID_ACTION`
     - Nếu reject thì rejectionReason không được rỗng → `ErrorCode.REJECTION_REASON_REQUIRED`
     - Người approve phải có role MANAGER hoặc CENTER_HEAD

#### 2.2. Implement Controller Endpoints

**File**: `CourseController.java`

- `POST /api/courses/{id}/submit` - submitCourseForApproval
  - Auth: `@PreAuthorize("hasRole('SUBJECT_LEADER')")`

- `POST /api/courses/{id}/approve` - approveCourse
  - Auth: `@PreAuthorize("hasAnyRole('MANAGER', 'CENTER_HEAD')")`
  - Body: `@Valid ApprovalRequestDTO`

#### 2.3. Testing

**Unit Tests**:
- ✅ testSubmitCourse_Success()
- ✅ testSubmitCourse_AlreadySubmitted()
- ✅ testSubmitCourse_NoPhases()
- ✅ testApproveCourse_Approve_Success()
- ✅ testApproveCourse_Reject_Success()
- ✅ testApproveCourse_NotSubmitted()
- ✅ testApproveCourse_InvalidAction()
- ✅ testApproveCourse_RejectionReasonRequired()

---

### **Task 3: Course Structure - CoursePhase (Week 3 - Days 1-2)**

#### 3.1. Implement CoursePhaseService

**File**: `src/main/java/org/fyp/emssep490be/services/coursephase/impl/CoursePhaseServiceImpl.java`

**Methods cần implement**:

1. **`getPhasesByCourse()`**
   - **Input**: `courseId` (Long)
   - **Output**: `List<CoursePhaseDTO>`
   - **Logic**:
     - Query tất cả phases của 1 course
     - Order by `sort_order` ASC
     - Map sang DTOs
   - **Validations**:
     - Course phải tồn tại → `ErrorCode.COURSE_NOT_FOUND`

2. **`createPhase()`**
   - **Input**: `courseId` (Long), `CreatePhaseRequestDTO`
   - **Output**: `CoursePhaseDTO`
   - **Logic**:
     - Check course tồn tại và status = 'draft'
     - Check unique: `(course_id, phase_number)`
     - Create CoursePhase entity
     - Set `created_at`, `updated_at` = now
     - Save entity
   - **Validations**:
     - Course phải tồn tại → `ErrorCode.COURSE_NOT_FOUND`
     - Course status = 'draft' → `ErrorCode.COURSE_CANNOT_BE_MODIFIED`
     - Unique constraint: `(course_id, phase_number)` → `ErrorCode.PHASE_NUMBER_DUPLICATE`
     - Required fields: phase_number, name, duration_weeks, sort_order

3. **`updatePhase()`**
   - **Input**: `phaseId` (Long), `UpdatePhaseRequestDTO`
   - **Output**: `CoursePhaseDTO`
   - **Logic**:
     - Check phase tồn tại
     - Check course.status = 'draft'
     - Update fields: name, duration_weeks, learning_focus
     - Set `updated_at` = now
   - **Validations**:
     - Phase phải tồn tại → `ErrorCode.PHASE_NOT_FOUND`
     - Course status = 'draft' → `ErrorCode.COURSE_CANNOT_BE_MODIFIED`

4. **`deletePhase()`**
   - **Input**: `phaseId` (Long)
   - **Output**: void
   - **Logic**:
     - Check phase tồn tại
     - Check course.status = 'draft'
     - **Business Rule**: Kiểm tra phase có CourseSession chưa
     - Nếu có session → throw error
     - Delete phase
   - **Validations**:
     - Phase phải tồn tại → `ErrorCode.PHASE_NOT_FOUND`
     - Phase chưa có sessions → `ErrorCode.PHASE_HAS_SESSIONS`

#### 3.2. Implement CoursePhaseController

**File**: `src/main/java/org/fyp/emssep490be/controllers/coursephase/CoursePhaseController.java`

- `GET /api/courses/{courseId}/phases` - getPhasesByCourse
- `POST /api/courses/{courseId}/phases` - createPhase
- `PUT /api/phases/{id}` - updatePhase
- `DELETE /api/phases/{id}` - deletePhase

**Auth**: `@PreAuthorize("hasAnyRole('SUBJECT_LEADER', 'ADMIN')")`

#### 3.3. Testing

**Unit Tests** (`CoursePhaseServiceImplTest.java`):
- ✅ testGetPhasesByCourse_Success()
- ✅ testGetPhasesByCourse_CourseNotFound()
- ✅ testCreatePhase_Success()
- ✅ testCreatePhase_DuplicatePhaseNumber()
- ✅ testCreatePhase_CourseNotDraft()
- ✅ testUpdatePhase_Success()
- ✅ testDeletePhase_Success()
- ✅ testDeletePhase_HasSessions()

---

### **Task 4: Course Structure - CourseSession (Week 3 - Days 3-5)**

#### 4.1. Implement CourseSessionService

**File**: `src/main/java/org/fyp/emssep490be/services/coursesession/impl/CourseSessionServiceImpl.java`

**Methods cần implement**:

1. **`getSessionsByPhase()`**
   - **Input**: `phaseId` (Long)
   - **Output**: `List<CourseSessionDTO>`
   - **Logic**:
     - Query tất cả sessions của 1 phase
     - Order by `sequence_no` ASC
     - Load CLO mappings nếu có
     - Map sang DTOs
   - **Validations**:
     - Phase phải tồn tại → `ErrorCode.PHASE_NOT_FOUND`

2. **`createCourseSession()`**
   - **Input**: `phaseId` (Long), `CreateCourseSessionRequestDTO`
   - **Output**: `CourseSessionDTO`
   - **Logic**:
     - Check phase tồn tại
     - Check course.status = 'draft'
     - Check unique: `(phase_id, sequence_no)`
     - Validate `skill_set` array (phải là valid Skill enum values)
     - Create CourseSession entity
     - Set `created_at`, `updated_at` = now
     - Save entity
   - **Validations**:
     - Phase phải tồn tại → `ErrorCode.PHASE_NOT_FOUND`
     - Course status = 'draft' → `ErrorCode.COURSE_CANNOT_BE_MODIFIED`
     - Unique constraint: `(phase_id, sequence_no)` → `ErrorCode.SESSION_SEQUENCE_DUPLICATE`
     - Required fields: sequence_no, topic
     - Skill_set array: mỗi skill phải valid (GENERAL, READING, WRITING, SPEAKING, LISTENING)

3. **`updateCourseSession()`**
   - **Input**: `sessionId` (Long), `UpdateCourseSessionRequestDTO`
   - **Output**: `CourseSessionDTO`
   - **Logic**:
     - Check session tồn tại
     - Check course.status = 'draft'
     - Update fields: topic, student_task, skill_set
     - Validate skill_set nếu có thay đổi
     - Set `updated_at` = now
   - **Validations**:
     - Session phải tồn tại → `ErrorCode.SESSION_NOT_FOUND`
     - Course status = 'draft' → `ErrorCode.COURSE_CANNOT_BE_MODIFIED`
     - Skill_set array valid

4. **`deleteCourseSession()`**
   - **Input**: `sessionId` (Long)
   - **Output**: void
   - **Logic**:
     - Check session tồn tại
     - Check course.status = 'draft'
     - **Business Rule**: Kiểm tra session đã được sử dụng trong SessionEntity chưa
     - Delete session (cascade xóa CLO mappings và materials)
   - **Validations**:
     - Session phải tồn tại → `ErrorCode.SESSION_NOT_FOUND`
     - Course status = 'draft' → `ErrorCode.COURSE_CANNOT_BE_MODIFIED`
     - Session chưa được dùng trong classes → `ErrorCode.SESSION_IN_USE`

#### 4.2. Implement CourseSessionController

**File**: `src/main/java/org/fyp/emssep490be/controllers/coursesession/CourseSessionController.java`

- `GET /api/phases/{phaseId}/sessions` - getSessionsByPhase
- `POST /api/phases/{phaseId}/sessions` - createCourseSession
- `PUT /api/sessions/{id}` - updateCourseSession
- `DELETE /api/sessions/{id}` - deleteCourseSession

**Auth**: `@PreAuthorize("hasAnyRole('SUBJECT_LEADER', 'ADMIN')")`

#### 4.3. Testing

**Unit Tests** (`CourseSessionServiceImplTest.java`):
- ✅ testGetSessionsByPhase_Success()
- ✅ testGetSessionsByPhase_PhaseNotFound()
- ✅ testCreateSession_Success()
- ✅ testCreateSession_DuplicateSequence()
- ✅ testCreateSession_InvalidSkillSet()
- ✅ testCreateSession_CourseNotDraft()
- ✅ testUpdateSession_Success()
- ✅ testUpdateSession_InvalidSkillSet()
- ✅ testDeleteSession_Success()
- ✅ testDeleteSession_InUse()

---

## 🔧 IMPLEMENTATION DETAILS

### Hash Checksum Calculation

**Purpose**: Detect course content changes for versioning

**Algorithm**:
```
Combine fields:
  - code
  - name
  - total_hours
  - duration_weeks
  - session_per_week
  - hours_per_session
  - prerequisites
  - target_audience
  - teaching_methods

Generate MD5 hash from combined string
Store in `hash_checksum` field
```

**Use case**: Khi update course, nếu hash thay đổi → create new version

### Status Workflow

**Course Status Flow**:
```
draft → (submit) → pending → (approve) → active
                         ↓
                    (reject) → draft
```

**States**:
- `draft`: Đang soạn thảo, cho phép edit
- `pending`: Đã submit, đợi approve (hiện tại status vẫn là draft nhưng có `submitted_at`)
- `active`: Đã approved, có thể dùng để tạo class
- `inactive`: Đã xóa (soft delete)

### Validation Rules Summary

| Rule | Description | Error Code |
|------|-------------|------------|
| Course code unique | Code phải unique toàn hệ thống | `COURSE_CODE_DUPLICATE` |
| Course version unique | `(subject_id, level_id, version)` unique | `COURSE_ALREADY_EXISTS` |
| Total hours consistency | `total_hours ≈ duration_weeks * session_per_week * hours_per_session` | `INVALID_TOTAL_HOURS` |
| Phase number unique | `(course_id, phase_number)` unique | `PHASE_NUMBER_DUPLICATE` |
| Session sequence unique | `(phase_id, sequence_no)` unique | `SESSION_SEQUENCE_DUPLICATE` |
| Skill set valid | Mỗi skill trong array phải valid enum | `INVALID_SKILL_SET` |
| Edit only draft | Chỉ edit được khi status = 'draft' | `COURSE_CANNOT_BE_MODIFIED` |
| Delete check usage | Không xóa được nếu đã có class sử dụng | `COURSE_IN_USE` |
| Submit requires phases | Course phải có ít nhất 1 phase trước khi submit | `COURSE_NO_PHASES` |
| Rejection requires reason | Reject course phải có rejection_reason | `REJECTION_REASON_REQUIRED` |

---

## 📝 ERROR CODES CẦN BỔ SUNG

**File**: `src/main/java/org/fyp/emssep490be/exceptions/ErrorCode.java`

Thêm các error codes sau:

```
COURSE_NOT_FOUND
COURSE_ALREADY_EXISTS
COURSE_CODE_DUPLICATE
COURSE_CANNOT_BE_UPDATED
COURSE_CANNOT_BE_MODIFIED
COURSE_IN_USE
COURSE_ALREADY_SUBMITTED
COURSE_NOT_SUBMITTED
COURSE_NO_PHASES
INVALID_ACTION
REJECTION_REASON_REQUIRED
INVALID_TOTAL_HOURS

PHASE_NOT_FOUND
PHASE_NUMBER_DUPLICATE
PHASE_HAS_SESSIONS

SESSION_NOT_FOUND
SESSION_SEQUENCE_DUPLICATE
SESSION_IN_USE
INVALID_SKILL_SET
```

---

## 🧪 TESTING STRATEGY

### Unit Tests Structure

**Pattern**: Mock dependencies, test business logic

```
@ExtendWith(MockitoExtension.class)
class CourseServiceImplTest {
    @Mock
    private CourseRepository courseRepository;

    @Mock
    private SubjectRepository subjectRepository;

    @Mock
    private LevelRepository levelRepository;

    @InjectMocks
    private CourseServiceImpl courseService;

    @Test
    void testCreateCourse_Success() {
        // GIVEN: mock data
        // WHEN: call service method
        // THEN: verify results & interactions
    }
}
```

### Integration Tests Structure

**Pattern**: Real database với TestContainers

```
@SpringBootTest
@ContextConfiguration(initializers = PostgreSQLTestContainer.Initializer.class)
@Transactional
class CourseServiceIntegrationTest {
    @Autowired
    private CourseService courseService;

    @Autowired
    private CourseRepository courseRepository;

    @Test
    void testCourseApprovalWorkflow() {
        // Create course → Submit → Approve → Verify
    }
}
```

### Test Coverage Requirements

- **Unit Tests**: > 80% line coverage
- **Integration Tests**: Cover critical workflows
  - Course creation → submission → approval flow
  - Course with phases and sessions
  - Update restrictions based on status
  - Delete restrictions based on usage

### Key Test Scenarios

1. **CRUD Operations**:
   - Happy path for all operations
   - Not found scenarios
   - Duplicate constraint violations
   - Invalid input validations

2. **Approval Workflow**:
   - Submit course (with/without phases)
   - Approve course
   - Reject course (with/without reason)
   - Re-submit after rejection

3. **Status-based Restrictions**:
   - Cannot edit approved course
   - Cannot delete course in use
   - Cannot submit without phases

4. **Cascade Operations**:
   - Delete course → check phases
   - Delete phase → check sessions
   - Update course → recalculate hash

---

## 📅 TIMELINE CHI TIẾT

### **Week 2: Core CRUD & Approval**

**Day 1-3**: Course CRUD
- Day 1: Implement `getAllCourses()`, `getCourseById()`
- Day 2: Implement `createCourse()`, `updateCourse()`
- Day 3: Implement `deleteCourse()`, Controller endpoints, Unit tests

**Day 4-5**: Approval Workflow
- Day 4: Implement `submitCourseForApproval()`, `approveCourse()`
- Day 5: Controller endpoints, Unit tests, Integration tests

### **Week 3: Course Structure**

**Day 1-2**: CoursePhase
- Day 1: Implement CoursePhaseService (all methods)
- Day 2: Controller endpoints, Unit tests

**Day 3-5**: CourseSession
- Day 3: Implement CourseSessionService (CRUD methods)
- Day 4: Validation logic, skill_set handling
- Day 5: Controller endpoints, Unit tests, Integration tests

### **Week 4**: Buffer & Polish

**Day 1-2**: Integration Testing
- Test complete workflows
- Fix bugs discovered during testing

**Day 3**: Code Review & Refactoring
- Review code quality
- Refactor duplicated code
- Ensure DRY principle

**Day 4**: Documentation
- Update API documentation
- Document business rules
- Add code comments

**Day 5**: Final Testing & Handoff
- Run all tests
- Manual testing via Swagger UI
- Prepare handoff to DEV 4 (Class Management)

---

## ✅ DEFINITION OF DONE

### Per Method
- [ ] Implementation hoàn thành (không return null)
- [ ] Logging đầy đủ (info + error)
- [ ] Exception handling đúng với ErrorCode
- [ ] Validations đầy đủ
- [ ] Unit tests viết xong (> 80% coverage)

### Per Task
- [ ] All methods implemented
- [ ] Controller endpoints working
- [ ] Unit tests pass
- [ ] Integration tests pass (nếu có)
- [ ] Manual testing via Swagger UI success

### Per Phase (Phase 2 Complete)
- [ ] Course CRUD API hoàn chỉnh
- [ ] Approval workflow working
- [ ] CoursePhase CRUD working
- [ ] CourseSession CRUD working
- [ ] All tests passing (> 80% coverage)
- [ ] Code review approved
- [ ] Documentation updated

---

## 🚀 READY TO START

### Prerequisites Checklist
- [x] Phase 1 completed (Subject & Level)
- [x] Entities defined (Course, CoursePhase, CourseSession)
- [x] Repositories created
- [x] DTOs ready
- [x] Service interfaces defined
- [x] Plan reviewed and understood

### Next Steps
1. ✅ Đọc kỹ plan này
2. ✅ Check lại Phase 1 code để hiểu pattern
3. ✅ Tạo branch: `feature/dev2-course-management`
4. ✅ Bắt đầu Task 1: Course CRUD
5. ✅ Commit thường xuyên với clear messages
6. ✅ Test từng method ngay sau khi implement
7. ✅ Code review với team sau mỗi task

---

## 📚 REFERENCE FILES

**Code Reference**:
- Subject/Level implementations: `SubjectServiceImpl.java`, `LevelServiceImpl.java`
- Pattern examples: `BranchServiceImpl.java`, `ResourceServiceImpl.java`
- Test examples: `SubjectServiceImplTest.java`, `LevelServiceImplTest.java`

**Documentation**:
- API Spec: `docs/openapi/openapi-academic.yaml`
- Instructions: `docs/instruction.md`
- Work Division: `docs/work-division-plan.md`
- Project Context: `CLAUDE.md`

**Entity Models**:
- `entities/Course.java`
- `entities/CoursePhase.java`
- `entities/CourseSession.java`

---

**Version**: 1.0
**Status**: Ready for Implementation
**Owner**: DEV 2 - Academic Curriculum Lead
**Timeline**: Week 2-4 (3 weeks)
**Dependencies**: Phase 1 (Subject & Level) ✅ COMPLETED

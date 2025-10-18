# 📊 KẾ HOẠCH CHIA CÔNG VIỆC CHO 4 DEVELOPERS - EMS Backend

**Phiên bản**: 1.0
**Ngày tạo**: 2025-10-17
**Mục đích**: Phân chia công việc implement backend EMS cho 4 developers, đảm bảo song song tối đa, tránh blocking

---

## 🏗️ 1. TỔNG QUAN KIẾN TRÚC HỆ THỐNG

### 1.1. Bounded Contexts - 8 Module Chính

| Module | Entities | Services | Độ phức tạp | Dependencies |
|--------|----------|----------|-------------|--------------|
| **1. Auth & User** | UserAccount, UserRole, Role, UserBranch | AuthService | ⭐⭐ | None (Foundation) |
| **2. Org & Infrastructure** | Center, Branch, TimeSlotTemplate, Resource | BranchService, TimeSlotService, ResourceService | ⭐⭐ | Module 1 |
| **3. Academic Curriculum** | Subject, Level, Course, CoursePhase, CourseSession, Plo, Clo, PloCloMapping, CourseSessionCloMapping, CourseMaterial | SubjectService, LevelService, CourseService, CoursePhaseService, CourseSessionService, PloService, CloService, CourseMaterialService | ⭐⭐⭐⭐ | Module 1 |
| **4. Teacher Management** | Teacher, TeacherSkill, TeacherAvailability, TeacherAvailabilityOverride | TeacherService | ⭐⭐⭐ | Module 1, 2 |
| **5. Class & Session** | ClassEntity, SessionEntity, SessionResource, TeachingSlot | ClassManagementService, SessionService | ⭐⭐⭐⭐⭐ | Module 2, 3, 4 |
| **6. Enrollment & Student** | Student, Enrollment, StudentSession | StudentService | ⭐⭐⭐⭐ | Module 1, 2, 5 |
| **7. Attendance & Assessment** | StudentSession (attendance), Assessment, Score, StudentFeedback, QaReport | AttendanceService, AssessmentService | ⭐⭐⭐ | Module 6 |
| **8. Request & Workflow** | StudentRequest, TeacherRequest | StudentRequestService, TeacherRequestService | ⭐⭐⭐⭐⭐ | Module 5, 6, 7 |

### 1.2. Dependency Graph

```
┌─────────────────────────────────────────────────────┐
│                     FOUNDATION                      │
│  Module 1: Auth & User      Module 2: Org & Infra  │
│  (UserAccount, Role)         (Branch, TimeSlot)     │
└─────────────────────────────────────────────────────┘
                          ↓
┌─────────────────────────────────────────────────────┐
│                   ACADEMIC LAYER                    │
│  Module 3: Curriculum       Module 4: Teacher Mgmt │
│  (Subject → Course)         (Teacher, Skills)       │
└─────────────────────────────────────────────────────┘
                          ↓
┌─────────────────────────────────────────────────────┐
│                   OPERATIONAL CORE                  │
│  Module 5: Class & Session  Module 6: Enrollment   │
│  (Phụ thuộc: 2,3,4)         (Phụ thuộc: 5)         │
└─────────────────────────────────────────────────────┘
                          ↓
┌─────────────────────────────────────────────────────┐
│                  EXECUTION LAYER                    │
│  Module 7: Attendance       Module 8: Request Flow │
│  (Phụ thuộc: 6)             (Phụ thuộc: 5,6,7)     │
└─────────────────────────────────────────────────────┘
```

### 1.3. Tình trạng hiện tại

✅ **ĐÃ CÓ**:
- 35 entities hoàn chỉnh với relationships
- 22 repositories (Spring Data JPA)
- 18 service interfaces với TODO comments
- 18 service implementations (chỉ có log, chưa có logic)
- 18 controllers (stub)
- DTOs cho auth, branch, và một số domain
- GlobalExceptionHandler với ResponseObject pattern
- PostgreSQL schema với 16 enum types

❌ **CHƯA CÓ**:
- Business logic trong services
- Complex algorithms (session generation, conflict detection, request processing)
- Service implementations thực sự
- Integration tests
- Request/Workflow module (chưa có service)

---

## 👥 2. PHÂN CÔNG CÔNG VIỆC CHO 4 DEVELOPERS

### 🔑 Chiến lược phân chia:
1. ✅ **Độc lập tối đa** - Mỗi dev có module riêng không đợi nhau
2. ✅ **Ưu tiên Foundation trước** - Xây nền móng cho các module phức tạp
3. ✅ **Test độc lập** - Mỗi module có thể viết unit test và integration test riêng
4. ✅ **Mock dependencies** - Dev có thể mock các service phụ thuộc để test

---

## 📦 DEV 1: FOUNDATION & INFRASTRUCTURE LEAD

### Trách nhiệm chính
**Owner**: Module 1 (Auth) + Module 2 (Org & Infrastructure)

### Chi tiết công việc

#### Phase 1: Module 1 - Authentication & Authorization (Week 1-2)

**Tasks**:
1. **JWT Authentication Implementation**
   - [x] Configure Spring Security với JWT
   - [x] Implement JwtTokenProvider (generate, validate, parse)
   - [x] Implement login logic:
     - Validate email/phone + password
     - Query UserAccount với roles và branches
     - Generate access_token + refresh_token
     - Return LoginResponseDTO
   - [x] Implement refresh token logic:
     - Validate refresh_token
     - Generate new access_token
     - Return RefreshTokenResponseDTO
   - [x] Implement logout với token blacklist (Redis hoặc in-memory cache)

2. **RBAC Implementation**
   - [x] Create SecurityConfig với role-based access
   - [x] Implement @PreAuthorize annotations cho controllers
   - [x] Multi-branch access control (check user_branches)

3. **Testing**
   - [x] Unit tests cho AuthService
   - [x] Integration tests cho /auth endpoints
   - [x] Test cases: successful login, invalid credentials, expired token, etc.

**Deliverables**:
- ✅ Working login/logout API
- ✅ JWT token generation và validation
- ✅ Role-based access control
- ✅ Test coverage > 80%

#### Phase 2: Module 2 - Organization & Infrastructure (Week 2-4)

**Tasks**:
1. **Branch Management**
   - [x] Implement BranchService:
     - `getAllBranches()` với pagination, filter (centerId, status)
     - `getBranchById()` với TimeSlots và Resources
     - `createBranch()` với validation unique(center_id, code)
     - `updateBranch()`, `deleteBranch()` (soft delete)
   - [x] Implement BranchController endpoints

2. **TimeSlot Management**
   - [x] Implement TimeSlotService:
     - `getTimeSlotsByBranch()`
     - `createTimeSlot()` với validation (start < end, duration match)
     - `updateTimeSlot()`, `deleteTimeSlot()`
   - [x] Implement TimeSlotController

3. **Resource Management (Room + Zoom)**
   - [x] Implement ResourceService:
     - `getResourcesByBranch()` với filter (type, available_date/time)
     - `createResource()` (ROOM hoặc VIRTUAL)
     - `checkResourceConflict()` - **CORE ALGORITHM**:
       - Query session_resource + session
       - Check date overlap + time overlap
       - Return available resources
     - `updateResource()`, `deleteResource()`
   - [x] Implement ResourceController

4. **Testing**
   - [x] Unit tests với mock repositories
   - [ ] Integration tests với H2 database
   - [x] Test conflict detection algorithm

**Deliverables**:
- ✅ Branch management API hoàn chỉnh
- ✅ TimeSlot API
- ✅ Resource API với conflict detection
- ✅ Pagination và filtering
- ✅ Test coverage > 80% (pending verification)

**Dependencies**: Module 1 (chỉ cần auth, không block)

**Estimated Time**: 3-4 weeks

**Status**: ✅ **COMPLETED** (2025-10-18)

---

## 📚 DEV 2: ACADEMIC CURRICULUM LEAD

### Trách nhiệm chính
**Owner**: Module 3 (Academic Curriculum)

### Chi tiết công việc

#### Phase 1: Subject & Level (Week 1-2)

**Tasks**:
1. **Subject Management**
   - [ ] Implement SubjectService:
     - `getAllSubjects()` với pagination, filter (status)
     - `getSubjectById()`
     - `createSubject()` với validation unique(code)
     - `updateSubject()`, `deleteSubject()`
   - [ ] Implement SubjectController

2. **Level Management**
   - [ ] Implement LevelService:
     - `getLevelsBySubject()`
     - `createLevel()` với validation unique(subject_id, code)
     - `updateLevel()`, `deleteLevel()`
   - [ ] Implement LevelController

#### Phase 2: Course Management với Approval Workflow (Week 2-4)

**Tasks**:
1. **Course CRUD**
   - [ ] Implement CourseService:
     - `getAllCourses()` với filter (subject, level, approved, status)
     - `getCourseById()` với phases, CLOs, materials
     - `createCourse()` với validation:
       - Unique(subject_id, level_id, version)
       - Calculate hash_checksum
       - Set status='pending'
     - `updateCourse()`, `deleteCourse()`

2. **Approval Workflow**
   - [ ] `approveCourse(courseId, managerId)`:
     - Update approved_by_manager, approved_at
     - Status = 'active'
   - [ ] `rejectCourse(courseId, managerId, reason)`:
     - Update rejection_reason
     - Status = 'draft'

3. **Course Structure (Phase & Session Template)**
   - [ ] Implement CoursePhaseService:
     - `getPhasesByCourse()`
     - `createPhase()` với validation unique(course_id, phase_number)
     - `updatePhase()`, `deletePhase()`

   - [ ] Implement CourseSessionService:
     - `getSessionsByPhase()`
     - `createCourseSession()` với validation:
       - Unique(phase_id, sequence_no)
       - Validate skill_set array
     - `updateCourseSession()`, `deleteCourseSession()`

#### Phase 3: Learning Outcomes & Materials (Week 4-5)

**Tasks**:
1. **PLO & CLO Management**
   - [ ] Implement PloService:
     - `getPlosBySubject()`
     - `createPlo()` với validation unique(subject_id, code)
     - `mapPloToClo(ploId, cloId)` - validate cùng subject

   - [ ] Implement CloService:
     - `getClosByCourse()`
     - `createClo()` với validation unique(course_id, code)
     - `mapCloToCourseSession(cloId, sessionId)`

2. **Course Material Management**
   - [ ] Implement CourseMaterialService:
     - `getMaterialsByCourse()`
     - `uploadMaterial()` - S3 integration (hoặc local storage tạm)
     - Validate ít nhất 1 trong {course_id, phase_id, session_id}
     - `deleteMaterial()`

3. **Testing**
   - [ ] Test cascade relationships (Course → Phase → Session)
   - [ ] Test approval workflow
   - [ ] Test PLO-CLO-Session mapping

**Deliverables**:
- ✅ Complete curriculum structure API (Subject → Level → Course → Phase → Session)
- ✅ Approval workflow (pending → approved/rejected)
- ✅ PLO/CLO mapping với validation
- ✅ Course material management
- ✅ Test coverage > 80%

**Dependencies**: Module 1 (auth only) - Hoàn toàn độc lập

**Estimated Time**: 4-5 weeks

---

## 👨‍🏫 DEV 3: TEACHER & STUDENT MANAGEMENT LEAD

### Trách nhiệm chính
**Owner**: Module 4 (Teacher) + Module 6 (Student - CRUD only, không bao gồm Enrollment)

### Chi tiết công việc

#### Phase 1: Teacher Management (Week 1-3)

**Tasks**:
1. **Teacher Profile**
   - [ ] Implement TeacherService:
     - `getAllTeachers()` với pagination, filter (branch, skill)
     - `getTeacherById()`
     - `createTeacher()` với validation:
       - Link với UserAccount (1-1)
       - Unique employee_code
     - `updateTeacher()`, `deleteTeacher()`

2. **Teacher Skills**
   - [ ] `getTeacherSkills(teacherId)`
   - [ ] `addTeacherSkill(teacherId, skill, level)` - PK composite
   - [ ] `updateTeacherSkill()`, `removeTeacherSkill()`

3. **Teacher Availability**
   - [ ] `getTeacherAvailability(teacherId)`
   - [ ] `setWeeklyAvailability(teacherId, dayOfWeek, startTime, endTime)`
   - [ ] `setAvailabilityOverride(teacherId, date, startTime, endTime, isAvailable, reason)`
   - [ ] `getAvailabilityByDate(teacherId, date)` - check override trước, sau đó weekly

4. **Find Available Teachers - CORE ALGORITHM**
   - [ ] `findAvailableTeachers(branchId, date, startTime, endTime, skillSet)`:
     ```sql
     1. Filter teachers có skill khớp với skillSet
     2. Check availability:
        - Ưu tiên override (nếu có)
        - Fallback weekly availability
     3. Check không trùng lịch:
        - JOIN teaching_slot + session
        - WHERE date = ? AND time overlap
     4. Return list available teachers
     ```

5. **Testing**
   - [ ] Test availability calculation logic
   - [ ] Test conflict detection
   - [ ] Test find available teachers algorithm

**Deliverables**:
- ✅ Teacher management API
- ✅ Skills & Availability API
- ✅ Find available teachers query
- ✅ Test coverage > 80%

#### Phase 2: Student Management (Week 3-4)

**Tasks**:
1. **Student CRUD**
   - [ ] Implement StudentService:
     - `getAllStudents()` với pagination, filter (branch)
     - `getStudentById()`
     - `createStudent()` với logic:
       - Check UserAccount tồn tại (email/phone)
       - Nếu chưa có user: tạo UserAccount → tạo Student
       - Nếu có user: chỉ tạo Student (link 1-1)
       - Unique student_code
     - `updateStudent()`, `deleteStudent()`

2. **Bulk Import (Optional - nếu còn thời gian)**
   - [ ] `importStudentsFromCSV(file)`:
     - Parse CSV
     - Validate data
     - Batch insert với transaction

3. **Testing**
   - [ ] Test user-student linking
   - [ ] Test unique constraints

**Deliverables**:
- ✅ Student management API
- ✅ Student-User account linking
- ✅ Test coverage > 80%

**Dependencies**:
- Module 1 (UserAccount)
- Module 2 (TimeSlot - có thể mock tạm)

**Estimated Time**: 3-4 weeks

---

## 🎓 DEV 4: OPERATIONS & CORE LOGIC LEAD

### Trách nhiệm chính
**Owner**: Module 5 (Class & Session) + Module 6 (Enrollment) + Module 7 (Attendance & Assessment)

⚠️ **Module phức tạp nhất** - Chứa core business logic

### Chi tiết công việc

#### Phase 1: Class Management (Week 1-3)

**Tasks**:
1. **Class CRUD**
   - [ ] Implement ClassManagementService:
     - `getAllClasses()` với filter (branch, course, status, modality)
     - `getClassById()` với sessions, enrollments
     - `createClass()` với validation:
       - Course phải approved
       - Unique(branch_id, code)
       - Validate schedule_days (array 0-6)
       - Status = 'draft'

2. **Auto-Generate Sessions - CORE ALGORITHM** ⭐
   - [ ] `generateSessionsForClass(classId)`:
     ```java
     /**
      * Logic:
      * 1. Load Course → CoursePhase → CourseSession (ordered by phase, sequence)
      * 2. Foreach course_session:
      *    - Calculate global_sequence (ROW_NUMBER across all sessions)
      *    - Calculate week_index = floor((global_seq - 1) / schedule_days.length)
      *    - Calculate day_index = (global_seq - 1) % schedule_days.length
      *    - Get schedule_day = schedule_days[day_index]
      *    - Calculate session_date = start_date + offset_to_first(schedule_day) + week_index * 7
      *    - Get time from TimeSlotTemplate (match schedule_day)
      *    - Insert SessionEntity(course_session_id, date, start_time, end_time, type=CLASS, status=planned)
      * 3. Validate: tất cả sessions nằm trong schedule_days
      */
     ```
   - [ ] Test với nhiều scenarios:
     - 2 sessions/week, 3 sessions/week
     - Schedule_days = [2,4] (Mon, Wed)
     - Duration 12 weeks

3. **Resource & Teacher Assignment**
   - [ ] `assignResourceToSession(sessionId, resourceId, capacityOverride)`:
     - Check conflict: query session_resource + session (same resource, date/time overlap)
     - Insert session_resource

   - [ ] `assignTeacherToSession(sessionId, teacherId, skill, role)`:
     - Check teacher availability (gọi TeacherService.checkAvailability)
     - Check conflict: query teaching_slot + session (same teacher, date/time overlap)
     - Insert teaching_slot

4. **Class Approval Workflow**
   - [ ] `submitClassForApproval(classId)` - set submitted_at
   - [ ] `approveClass(classId, approverId)` - set approved_by, approved_at, status=scheduled
   - [ ] `rejectClass(classId, reason)` - set rejection_reason, status=draft

5. **Testing**
   - [ ] **CRITICAL**: Test session generation algorithm với nhiều edge cases
   - [ ] Test conflict detection (resource, teacher)

**Deliverables**:
- ✅ Class management API
- ✅ Session auto-generation (CORE)
- ✅ Resource/Teacher assignment với conflict check
- ✅ Approval workflow
- ✅ Test coverage > 85%

**Dependencies**:
- Module 2 (Branch, TimeSlot, Resource)
- Module 3 (Course template) - **CẦN interface từ DEV 2 (1-2 ngày đầu)**
- Module 4 (Teacher) - **CẦN interface từ DEV 3**

**Strategy**: Mock CourseService và TeacherService tuần đầu, integrate sau

---

#### Phase 2: Enrollment & Student Session (Week 3-5)

**Tasks**:
1. **Enrollment Logic**
   - [ ] Implement StudentService (phần enrollment):
     - `enrollStudent(studentId, classId)`:
       ```java
       /**
        * 1. Validate:
        *    - Class status in (scheduled, ongoing)
        *    - Capacity check: enrolled < max_capacity (hoặc override)
        *    - Student không trùng lịch (check student_session overlap)
        * 2. Insert enrollment(class_id, student_id, status=enrolled, enrolled_at=now)
        * 3. Call generateStudentSessions(enrollmentId)
        */
       ```

2. **Auto-Generate Student Sessions - CORE ALGORITHM** ⭐
   - [ ] `generateStudentSessions(enrollmentId)`:
     ```java
     /**
      * 1. Load enrollment → class → sessions (status=planned)
      * 2. Foreach session:
      *    - Insert student_session(student_id, session_id,
      *             attendance_status=planned, is_makeup=false)
      * 3. Return count inserted
      */
     ```

3. **Late Enrollment**
   - [ ] `enrollStudentLate(studentId, classId, joinDate)`:
     - Chỉ tạo student_session cho sessions có date >= joinDate
     - Set join_session_id trong enrollment

4. **Schedule Synchronization - CORE LOGIC**
   - [ ] `syncStudentSessionsOnClassChange(classId)`:
     - Trigger khi class reschedule/cancel session
     - Update student_session cho tất cả enrolled students

   - [ ] `handleSessionCancellation(sessionId)`:
     - Update student_session.attendance_status = 'excused'

5. **Testing**
   - [ ] Test enrollment validation (capacity, conflict)
   - [ ] Test student_session generation
   - [ ] Test late enrollment
   - [ ] Test sync logic

**Deliverables**:
- ✅ Enrollment API
- ✅ StudentSession auto-generation
- ✅ Late enrollment logic
- ✅ Schedule synchronization
- ✅ Test coverage > 85%

**Dependencies**: Module 5 (Class, Session) - sequential, không thể song song

---

#### Phase 3: Attendance & Assessment (Week 5-7)

**Tasks**:
1. **Attendance Recording**
   - [ ] Implement AttendanceService:
     - `getAttendanceBySession(sessionId)` - query student_session
     - `recordAttendance(sessionId, studentId, status, note)`:
       - Validate session.date <= today
       - Update student_session(attendance_status, recorded_at, note)
     - `bulkRecordAttendance(sessionId, List<AttendanceRecord>)`

2. **Session Report**
   - [ ] `submitSessionReport(sessionId, teacherNote)`:
     - Update session.teacher_note
     - Update session.status = 'done'

   - [ ] `lockAttendance(sessionId)` - prevent changes after T hours

3. **Assessment Management**
   - [ ] Implement AssessmentService:
     - `createAssessment(classId, name, kind, maxScore, weight, sessionId)`
     - Validate total weight <= 100%
     - `updateAssessment()`, `deleteAssessment()`

4. **Score Entry**
   - [ ] `enterScore(assessmentId, studentId, score, feedback, gradedBy)`:
     - Validate score <= max_score
     - Insert score (unique: assessment_id, student_id)

   - [ ] `importScoresFromCSV(assessmentId, file)` - bulk import

5. **Testing**
   - [ ] Test attendance validation
   - [ ] Test lock mechanism
   - [ ] Test assessment weight validation

**Deliverables**:
- ✅ Attendance recording API
- ✅ Session report API
- ✅ Assessment & Score management
- ✅ Test coverage > 80%

**Dependencies**: Module 6 (StudentSession) - sequential

**Estimated Time**: 6-7 weeks total

---

## 🚀 3. IMPLEMENTATION ROADMAP

### Sprint 1-2 (Week 1-4): Foundation Phase

| Dev | Module | Progress | Status |
|-----|--------|----------|--------|
| DEV 1 | Module 1 (Auth) + Module 2 (Org) | ████████████████ | Ready to start |
| DEV 2 | Module 3 (Curriculum - Part 1) | ████████████████ | Ready to start |
| DEV 3 | Module 4 (Teacher) | ████████████████ | Ready to start |
| DEV 4 | Module 5 (Class - Part 1) | ████████████████ | Wait for Course interface (Day 2-3) |

**Milestone 1**: Auth working, Basic CRUD done, Teacher availability working

---

### Sprint 3-4 (Week 5-8): Core Operations

| Dev | Module | Progress | Status |
|-----|--------|----------|--------|
| DEV 1 | Module 2 enhancements + Support | ████████████████ | Conflict detection refinement |
| DEV 2 | Module 3 (Curriculum - Part 2) | ████████████████ | PLO/CLO, Materials |
| DEV 3 | Module 6 (Student CRUD) | ████████████████ | Student management |
| DEV 4 | Module 5 (Complete) + Module 6 (Enrollment) | ████████████████ | Session gen + Enrollment |

**Milestone 2**: Session auto-generation working, Enrollment với StudentSession sync

---

### Sprint 5-6 (Week 9-12): Execution Layer

| Dev | Module | Progress | Status |
|-----|--------|----------|--------|
| DEV 1 | Integration testing + Refactor | ████████████████ | Support all modules |
| DEV 2 | Module 7 (Assessment) | ████████████████ | Assessment & Score |
| DEV 3 | Module 7 (Attendance) | ████████████████ | Attendance recording |
| DEV 4 | Module 8 (Request - Basic) | ████████████████ | Absence, Leave request |

**Milestone 3**: MVP Complete - Attendance, Assessment, Basic request working

---

### Sprint 7+ (Week 13-16): Advanced Features

| All Devs | Module | Progress | Status |
|----------|--------|----------|--------|
| Team effort | Module 8 (Advanced Request) | ████████████████ | Makeup, Transfer, Reschedule |
| Team effort | Module 5.6 (Reschedule Class) | ████████████████ | Complex reschedule logic |
| Team effort | Reporting (Optional) | ▓▓▓▓▓▓▓▓ | Dashboard APIs |

**Milestone 4**: Full system integration, Advanced workflows

---

## 📋 4. DELIVERABLES CHECKLIST

### DEV 1 Deliverables
- [ ] JWT authentication (login, refresh, logout)
- [ ] RBAC implementation với @PreAuthorize
- [ ] Branch CRUD với pagination
- [ ] TimeSlot CRUD
- [ ] Resource CRUD với conflict detection
- [ ] Unit tests + Integration tests (coverage > 80%)

### DEV 2 Deliverables
- [ ] Subject/Level CRUD
- [ ] Course CRUD với approval workflow
- [ ] CoursePhase/CourseSession CRUD
- [ ] PLO/CLO CRUD và mapping
- [ ] CourseMaterial management
- [ ] Unit tests + Integration tests (coverage > 80%)

### DEV 3 Deliverables
- [ ] Teacher CRUD với skills
- [ ] Availability/Override management
- [ ] Find available teachers algorithm
- [ ] Student CRUD với user linking
- [ ] Unit tests + Integration tests (coverage > 80%)

### DEV 4 Deliverables
- [ ] Class CRUD
- [ ] **Session auto-generation algorithm** (CORE)
- [ ] Resource/Teacher assignment với conflict check
- [ ] Enrollment với StudentSession auto-generation
- [ ] Schedule synchronization
- [ ] Attendance recording
- [ ] Assessment & Score management
- [ ] Unit tests + Integration tests (coverage > 85%)

---

## ⚠️ 5. CRITICAL SUCCESS FACTORS

### 5.1. API Contract First Approach

**RULE**: Mỗi dev phải commit **Service Interface + DTOs** trước khi implement

```java
// Example: DEV 2 commit CourseService interface
public interface CourseService {
    CourseDetailDTO getCourseById(Long id);
    CourseDTO createCourse(CreateCourseRequestDTO request);
    // ... other methods
}

// DEV 4 có thể mock ngay:
@MockBean
private CourseService courseService;
```

**Git Strategy**:
```
1. DEV 2 tạo branch: feature/curriculum-interfaces
2. Commit: CourseService.java + CourseDTO.java
3. Merge vào main sau 1-2 ngày
4. DEV 4 pull main → mock CourseService để tiếp tục
```

---

### 5.2. Mock Strategy for Dependencies

**DEV 4 Example** (phụ thuộc nhiều nhất):

```java
@SpringBootTest
public class ClassManagementServiceTest {

    @MockBean
    private CourseService courseService;  // DEV 2's module

    @MockBean
    private TeacherService teacherService;  // DEV 3's module

    @MockBean
    private ResourceService resourceService;  // DEV 1's module

    @Autowired
    private ClassManagementService classService;

    @Test
    void testCreateClass() {
        // Mock course data
        when(courseService.getCourseById(1L))
            .thenReturn(CourseDetailDTO.builder()
                .id(1L)
                .sessionPerWeek(3)
                .durationWeeks(12)
                .build());

        // Test class creation logic
        ClassDTO result = classService.createClass(request);

        assertNotNull(result);
        assertEquals("CLASS-001", result.getCode());
    }
}
```

---

### 5.3. Database Migration Strategy

**OWNER: DEV 1** (chịu trách nhiệm schema)

- ✅ `schema.sql` đã có 16 enum types → **KHÔNG SỬA**
- ✅ Hibernate `ddl-auto: update` → mỗi dev tạo entities, Hibernate tự động tạo tables
- ✅ Nếu cần thêm enum value → dùng `ALTER TYPE ... ADD VALUE` (PostgreSQL)

**Lưu ý**: PostgreSQL không cho phép sửa/xóa enum value dễ dàng → cẩn thận khi thiết kế

---

### 5.4. Git Workflow

```
main (protected branch)
│
├── feature/dev1-auth-org          (DEV 1)
├── feature/dev2-curriculum        (DEV 2)
├── feature/dev3-teacher-student   (DEV 3)
└── feature/dev4-class-ops         (DEV 4)
```

**Merge Strategy**:
1. Mỗi dev tạo feature branch từ main
2. Commit thường xuyên vào feature branch
3. Tạo Pull Request khi hoàn thành module
4. Code review chéo: DEV 1 review DEV 2, DEV 2 review DEV 3, ...
5. Merge vào main sau khi pass review + tests

**Daily Sync**:
- Morning standup: báo cáo tiến độ, blockers
- Afternoon: pull main để lấy interfaces mới từ các dev khác

---

### 5.5. Testing Strategy

| Test Type | Owner | When | Coverage Target |
|-----------|-------|------|-----------------|
| **Unit Test** | Mỗi dev | Sau mỗi method implement | > 80% |
| **Integration Test** | Mỗi dev | Sau hoàn thành module | > 70% |
| **E2E Test** | DEV 4 (lead) | Sprint 6 | Critical flows |
| **Performance Test** | DEV 1 | Sprint 7 | Response time < 500ms |

**Test Data Strategy**:
- Dùng `@DataJpaTest` với H2 in-memory cho repository tests
- Dùng `@SpringBootTest` với test containers (PostgreSQL) cho integration tests
- Tạo test fixtures trong `src/test/resources/data.sql`

---

## 🔧 6. REFACTORING OPPORTUNITIES

### 6.1. Tách Shared Services

**Problem**: Module 5 (Class) có logic dùng chung với nhiều module khác

**Solution**: Tạo các utility services

#### 6.1.1. ConflictDetectionService (Owner: DEV 1)

```java
@Service
public class ConflictDetectionService {

    /**
     * Check if resource is available at given date/time
     */
    public boolean isResourceAvailable(Long resourceId, LocalDate date,
                                       LocalTime startTime, LocalTime endTime) {
        // Query session_resource + session
        // Return true if no overlap
    }

    /**
     * Check if teacher is available at given date/time
     */
    public boolean isTeacherAvailable(Long teacherId, LocalDate date,
                                      LocalTime startTime, LocalTime endTime) {
        // Check availability + teaching_slot conflicts
    }

    /**
     * Find available resources for given criteria
     */
    public List<Resource> findAvailableResources(Long branchId, LocalDate date,
                                                  LocalTime startTime, LocalTime endTime);
}
```

**Sử dụng bởi**:
- DEV 1: ResourceService (check resource conflict)
- DEV 3: TeacherService (check teacher conflict)
- DEV 4: ClassManagementService (assign resource/teacher)

---

#### 6.1.2. SessionGenerationService (Owner: DEV 4)

```java
@Service
public class SessionGenerationService {

    /**
     * Generate sessions for a class based on course template
     * Input: Class (start_date, schedule_days), Course structure
     * Output: List<SessionEntity>
     */
    public List<SessionEntity> generateSessions(ClassEntity classEntity,
                                                 CourseDetailDTO course,
                                                 List<TimeSlotTemplate> timeSlots) {
        // Complex algorithm here
        // Return list of planned sessions
    }

    /**
     * Calculate session date based on sequence
     */
    private LocalDate calculateSessionDate(LocalDate startDate,
                                           List<Integer> scheduleDays,
                                           int globalSequence);
}
```

**Tách riêng vì**:
- Logic phức tạp (100+ lines)
- Dễ test độc lập
- Có thể reuse cho reschedule logic

---

#### 6.1.3. ApprovalWorkflowService (Owner: DEV 2)

```java
@Service
public class ApprovalWorkflowService {

    /**
     * Generic approval logic
     */
    public <T> T approve(T entity, Long approverId,
                         BiConsumer<T, Long> approveAction) {
        approveAction.accept(entity, approverId);
        return entity;
    }

    /**
     * Generic rejection logic
     */
    public <T> T reject(T entity, String reason,
                        BiConsumer<T, String> rejectAction) {
        rejectAction.accept(entity, reason);
        return entity;
    }
}
```

**Dùng chung cho**:
- Course approval (DEV 2)
- Class approval (DEV 4)
- Request approval (DEV 4 - Sprint 6)

---

### 6.2. DTO Mapping Strategy

**Problem**: Conversion Entity ↔ DTO lặp đi lặp lại

**Solution**: Dùng MapStruct hoặc tạo Mapper utilities

#### Option 1: MapStruct (Recommended)

```xml
<!-- pom.xml -->
<dependency>
    <groupId>org.mapstruct</groupId>
    <artifactId>mapstruct</artifactId>
    <version>1.5.5.Final</version>
</dependency>
```

```java
@Mapper(componentModel = "spring")
public interface CourseMapper {
    CourseDTO toDTO(Course course);
    Course toEntity(CreateCourseRequestDTO dto);

    @Mapping(target = "phases", source = "phases")
    @Mapping(target = "clos", source = "clos")
    CourseDetailDTO toDetailDTO(Course course);
}
```

#### Option 2: Manual Mapper (nếu không dùng MapStruct)

```java
@Component
public class CourseMapper {
    public CourseDTO toDTO(Course course) {
        return CourseDTO.builder()
            .id(course.getId())
            .code(course.getCode())
            // ... mapping logic
            .build();
    }
}
```

**Owner**: Mỗi dev tạo mapper cho entities của mình

---

## 📊 7. METRICS & TRACKING

### 7.1. Daily Progress Tracking

Mỗi dev cập nhật Google Sheet hàng ngày:

| Date | Dev | Module | Tasks Completed | Blockers | ETA |
|------|-----|--------|-----------------|----------|-----|
| 2025-10-17 | DEV 1 | Auth | JWT config | None | On track |
| 2025-10-17 | DEV 2 | Subject | Subject CRUD | None | On track |
| 2025-10-17 | DEV 3 | Teacher | Teacher CRUD | Waiting TimeSlot interface | 1 day delay |
| 2025-10-17 | DEV 4 | Class | Class entity setup | Waiting Course interface | 2 days delay |

---

### 7.2. Code Quality Metrics

| Metric | Target | Tool |
|--------|--------|------|
| **Code Coverage** | > 80% | JaCoCo |
| **Code Smells** | 0 Major | SonarQube |
| **Duplicated Code** | < 3% | SonarQube |
| **Cyclomatic Complexity** | < 10 per method | SonarQube |
| **API Response Time** | < 200ms (CRUD), < 500ms (complex) | JMeter |

**SonarQube Setup** (Owner: DEV 1):
```bash
# Run SonarQube in Docker
docker run -d --name sonarqube -p 9000:9000 sonarqube:latest

# Maven plugin
mvn sonar:sonar -Dsonar.host.url=http://localhost:9000
```

---

### 7.3. Success Criteria

**Sprint 1-2 Success**:
- [ ] All devs have working CRUD endpoints for their core entities
- [ ] Auth system working (login, token validation)
- [ ] Zero blockers due to missing interfaces
- [ ] Test coverage > 70%

**Sprint 3-4 Success**:
- [ ] Session auto-generation working correctly
- [ ] Enrollment creates StudentSession successfully
- [ ] Conflict detection prevents double-booking
- [ ] Test coverage > 80%

**Sprint 5-6 Success**:
- [ ] Attendance recording works
- [ ] Assessment & Score management complete
- [ ] Basic request workflow (absence, leave) working
- [ ] All integration tests passing

**Final Success** (Week 12):
- [ ] All MVP features implemented
- [ ] E2E test suite passing
- [ ] API documentation complete (Swagger)
- [ ] Performance benchmarks met
- [ ] Ready for UAT

---

## 🚦 8. RISK MANAGEMENT

### 8.1. Identified Risks & Mitigation

| Risk | Probability | Impact | Mitigation |
|------|------------|--------|------------|
| **DEV 4 blocked by missing Course interface** | High | High | DEV 2 commit interface trong 2 ngày đầu; DEV 4 mock tạm |
| **Session generation algorithm complex** | Medium | High | DEV 4 làm POC trước, review với team early |
| **Conflict detection performance issues** | Medium | Medium | Index database columns (date, start_time, end_time); Optimize queries |
| **Enum type cannot be modified** | Low | High | Careful design upfront; Document workaround (add new enum type if needed) |
| **Test data setup phức tạp** | Medium | Medium | Tạo shared fixtures; Dùng test data builder pattern |

---

### 8.2. Blocker Resolution Protocol

**Nếu bị block**:
1. ⏱️ **Trong 1 giờ**: Tự debug, search docs
2. 🤝 **Sau 1 giờ**: Hỏi teammate (Slack/Teams)
3. 📞 **Sau 2 giờ**: Daily sync meeting (15 phút)
4. 🚨 **Sau 4 giờ**: Escalate to Tech Lead, re-assign task

**Channels**:
- Slack: #ems-backend-dev (general)
- Slack: #ems-blockers (urgent issues)
- Daily standup: 9:00 AM (15 phút)

---

## 🎯 9. DEFINITION OF DONE

### 9.1. Per Task DoD

Một task được coi là DONE khi:
- [ ] Code implementation hoàn thành
- [ ] Unit tests viết xong (coverage > 80% cho code mới)
- [ ] Integration tests pass (nếu có)
- [ ] Code review passed (ít nhất 1 approver)
- [ ] No SonarQube critical issues
- [ ] API documentation updated (Swagger annotations)
- [ ] Merged vào main branch

---

### 9.2. Per Module DoD

Một module được coi là DONE khi:
- [ ] Tất cả CRUD endpoints working
- [ ] Business logic implemented correctly
- [ ] Validation rules enforced
- [ ] Error handling với GlobalExceptionHandler
- [ ] Test coverage > 80%
- [ ] Postman collection updated
- [ ] README documentation updated

---

### 9.3. Sprint DoD

Một sprint được coi là DONE khi:
- [ ] All planned stories completed
- [ ] All tests passing (unit + integration)
- [ ] No critical bugs in backlog
- [ ] Demo ready for stakeholders
- [ ] Sprint retrospective completed
- [ ] Next sprint planned

---

## 📚 10. REFERENCES & RESOURCES

### 10.1. Technical Documentation

- [API Design Specification](./api-design.md) - Complete API reference
- [Feature List](./feature-list.md) - Business requirements
- [Database Schema](../src/main/resources/schema.sql) - PostgreSQL enums
- [Project Setup](../README.md) - Environment setup
- [CLAUDE.md](../CLAUDE.md) - Development guidelines

### 10.2. External Resources

- [Spring Boot 3.5 Docs](https://docs.spring.io/spring-boot/docs/3.5.x/reference/html/)
- [Spring Data JPA](https://docs.spring.io/spring-data/jpa/docs/current/reference/html/)
- [Spring Security with JWT](https://www.baeldung.com/spring-security-oauth-jwt)
- [PostgreSQL Array Types](https://www.postgresql.org/docs/16/arrays.html)
- [MapStruct Documentation](https://mapstruct.org/documentation/stable/reference/html/)

### 10.3. Tools & Setup

```bash
# Required tools
- Java 21
- Maven 3.8+
- PostgreSQL 16
- Docker & Docker Compose
- Postman (API testing)
- IntelliJ IDEA / VS Code

# Optional tools
- SonarQube (code quality)
- JaCoCo (coverage)
- Swagger UI (API docs)
```

---

## 📝 11. APPENDIX

### A. Quick Start Guide for Each Dev

#### DEV 1 Quick Start
```bash
# 1. Clone repo
git clone <repo-url>
cd ems-sep490-be

# 2. Create feature branch
git checkout -b feature/dev1-auth-org

# 3. Start PostgreSQL
docker-compose up -d postgres

# 4. Run application
./mvnw spring-boot:run

# 5. Implement AuthService
# File: src/main/java/org/fyp/emssep490be/services/auth/impl/AuthServiceImpl.java

# 6. Write tests
# File: src/test/java/org/fyp/emssep490be/services/auth/AuthServiceTest.java

# 7. Commit & push
git add .
git commit -m "feat: Implement JWT authentication"
git push origin feature/dev1-auth-org
```

#### DEV 2 Quick Start
```bash
# 1. Create feature branch
git checkout -b feature/dev2-curriculum

# 2. Commit interfaces FIRST
# Files:
# - CourseService.java
# - CourseDTO.java
# - CreateCourseRequestDTO.java

git add services/course/CourseService.java dtos/course/
git commit -m "feat: Add Course service interface and DTOs"
git push  # Let DEV 4 use this immediately

# 3. Implement logic
# File: services/course/impl/CourseServiceImpl.java
```

#### DEV 3 Quick Start
```bash
# 1. Create feature branch
git checkout -b feature/dev3-teacher-student

# 2. Check if TimeSlotService interface exists
# If not, create mock:
@MockBean
private TimeSlotService timeSlotService;

# 3. Implement TeacherService
# Focus on find available teachers algorithm
```

#### DEV 4 Quick Start
```bash
# 1. Create feature branch
git checkout -b feature/dev4-class-ops

# 2. Wait for Day 2-3: Pull main to get Course interface
git pull origin main

# 3. Create mocks for dependencies
@MockBean
private CourseService courseService;
@MockBean
private TeacherService teacherService;
@MockBean
private ResourceService resourceService;

# 4. Implement session generation algorithm FIRST (most complex)
# File: services/classmanagement/SessionGenerationService.java
# Write comprehensive tests for this!

# 5. Then implement ClassManagementService
```

---

### B. Session Generation Algorithm - Detailed Pseudocode

```python
def generate_sessions(class_entity, course, time_slots):
    """
    Input:
    - class_entity: {id, start_date, schedule_days=[2,4,6], ...}
    - course: {id, session_per_week=3, duration_weeks=12, phases=[...]}
    - time_slots: [{id, branch_id, start_time, end_time, ...}]

    Output:
    - List of SessionEntity
    """

    sessions = []

    # Step 1: Flatten all course_sessions across all phases (ordered)
    course_sessions = []
    for phase in course.phases (ordered by phase_number):
        for cs in phase.course_sessions (ordered by sequence_no):
            course_sessions.append(cs)

    # Step 2: For each course_session, calculate date and time
    for idx, course_session in enumerate(course_sessions):
        global_sequence = idx + 1  # 1-based

        # Calculate which week this session belongs to
        week_index = floor((global_sequence - 1) / len(schedule_days))

        # Calculate which day of week
        day_index = (global_sequence - 1) % len(schedule_days)
        schedule_day = schedule_days[day_index]  # e.g., 2 (Monday)

        # Calculate session date
        # Find offset from start_date to first occurrence of schedule_day
        start_dow = start_date.day_of_week  # e.g., 3 (Tuesday)
        if schedule_day >= start_dow:
            offset_days = schedule_day - start_dow
        else:
            offset_days = 7 - start_dow + schedule_day

        first_session_date = start_date + offset_days
        session_date = first_session_date + (week_index * 7)

        # Get time from time_slot_template (match schedule_day or default)
        time_slot = find_time_slot_for_day(time_slots, schedule_day)

        # Create session entity
        session = SessionEntity(
            class_id = class_entity.id,
            course_session_id = course_session.id,
            date = session_date,
            start_time = time_slot.start_time,
            end_time = time_slot.end_time,
            type = 'CLASS',
            status = 'planned'
        )
        sessions.append(session)

    return sessions

def find_time_slot_for_day(time_slots, day_of_week):
    # Logic: tìm time_slot phù hợp với ngày trong tuần
    # Hoặc dùng default nếu không có rule riêng
    for slot in time_slots:
        if slot.applicable_days == null or day_of_week in slot.applicable_days:
            return slot
    return time_slots[0]  # fallback
```

**Test Cases**:
1. ✅ Class 2 sessions/week (Mon, Wed), 4 weeks → 8 sessions
2. ✅ Class 3 sessions/week (Mon, Wed, Fri), 12 weeks → 36 sessions
3. ✅ Edge case: start_date = Friday, schedule_days = [1,3,5] → first session on next Monday
4. ✅ Time slot mapping: different time for different days

---

### C. Conflict Detection Algorithm - Detailed Pseudocode

```sql
-- Resource Conflict Check
-- Input: resource_id, date, start_time, end_time
-- Output: true if conflict exists, false if available

SELECT COUNT(*) > 0 as has_conflict
FROM session_resource sr
JOIN session s ON s.id = sr.session_id
WHERE sr.resource_id = :resource_id
  AND s.date = :date
  AND s.status != 'cancelled'
  AND (
    -- Check time overlap
    (s.start_time < :end_time AND s.end_time > :start_time)
  );

-- Teacher Conflict Check
-- Input: teacher_id, date, start_time, end_time
-- Output: true if conflict exists, false if available

-- Step 1: Check teaching_slot conflicts
SELECT COUNT(*) > 0 as has_conflict
FROM teaching_slot ts
JOIN session s ON s.id = ts.session_id
WHERE ts.teacher_id = :teacher_id
  AND s.date = :date
  AND s.status != 'cancelled'
  AND (s.start_time < :end_time AND s.end_time > :start_time);

-- Step 2: Check availability
-- a) Check override first
SELECT is_available
FROM teacher_availability_override
WHERE teacher_id = :teacher_id
  AND date = :date
  AND start_time <= :start_time
  AND end_time >= :end_time;

-- b) If no override, check weekly availability
SELECT COUNT(*) > 0 as is_available
FROM teacher_availability
WHERE teacher_id = :teacher_id
  AND day_of_week = EXTRACT(DOW FROM :date)
  AND start_time <= :start_time
  AND end_time >= :end_time;
```

---

### D. Enrollment & StudentSession Sync - Detailed Pseudocode

```java
public void enrollStudent(Long studentId, Long classId) {
    // 1. Validate
    ClassEntity classEntity = classRepository.findById(classId)
        .orElseThrow(() -> new NotFoundException("Class not found"));

    if (!classEntity.getStatus().equals("scheduled") &&
        !classEntity.getStatus().equals("ongoing")) {
        throw new IllegalStateException("Class not available for enrollment");
    }

    long enrolledCount = enrollmentRepository.countByClassIdAndStatus(classId, "enrolled");
    if (enrolledCount >= classEntity.getMaxCapacity()) {
        // Check if override allowed
        if (!systemSettings.isCapacityOverrideAllowed()) {
            throw new CapacityExceededException("Class is full");
        }
    }

    // Check student schedule conflict
    List<StudentSession> existingSessions = studentSessionRepository
        .findByStudentIdAndDateRange(studentId,
            classEntity.getStartDate(),
            classEntity.getPlannedEndDate());

    List<SessionEntity> classSessions = sessionRepository
        .findByClassIdAndStatus(classId, "planned");

    boolean hasConflict = checkScheduleConflict(existingSessions, classSessions);
    if (hasConflict) {
        throw new ScheduleConflictException("Student has schedule conflict");
    }

    // 2. Create enrollment
    Enrollment enrollment = Enrollment.builder()
        .classId(classId)
        .studentId(studentId)
        .status(EnrollmentStatus.ENROLLED)
        .enrolledAt(LocalDateTime.now())
        .createdBy(getCurrentUserId())
        .build();
    enrollmentRepository.save(enrollment);

    // 3. Generate student sessions
    generateStudentSessions(enrollment.getId());
}

private void generateStudentSessions(Long enrollmentId) {
    Enrollment enrollment = enrollmentRepository.findById(enrollmentId).orElseThrow();

    List<SessionEntity> sessions = sessionRepository
        .findByClassIdAndStatus(enrollment.getClassId(), "planned");

    List<StudentSession> studentSessions = sessions.stream()
        .map(session -> StudentSession.builder()
            .studentId(enrollment.getStudentId())
            .sessionId(session.getId())
            .attendanceStatus(AttendanceStatus.PLANNED)
            .isMakeup(false)
            .build())
        .collect(Collectors.toList());

    studentSessionRepository.saveAll(studentSessions);

    log.info("Generated {} student sessions for enrollment {}",
        studentSessions.size(), enrollmentId);
}

// Late enrollment
public void enrollStudentLate(Long studentId, Long classId, LocalDate joinDate) {
    // Similar logic but filter sessions
    List<SessionEntity> futureSessions = sessionRepository
        .findByClassIdAndStatusAndDateGreaterThanEqual(classId, "planned", joinDate);

    // Generate student_session only for future sessions
    // Set join_session_id in enrollment
}

// Sync when class schedule changes
@Transactional
public void syncStudentSessionsOnReschedule(Long sessionId, LocalDate newDate,
                                             LocalTime newStartTime, LocalTime newEndTime) {
    // This is called when a session is rescheduled
    // StudentSession records point to sessionId, so they auto-update
    // But if session is CANCELLED:

    SessionEntity session = sessionRepository.findById(sessionId).orElseThrow();
    if (session.getStatus().equals("cancelled")) {
        // Mark all student_sessions as excused
        studentSessionRepository.updateAttendanceStatusBySessionId(
            sessionId, AttendanceStatus.EXCUSED);
    }
}
```

---

**END OF DOCUMENT**

---

**Next Steps**:
1. ✅ Team review this plan
2. ✅ Each dev confirms understanding of their module
3. ✅ Setup development environment (Docker, PostgreSQL)
4. ✅ Create feature branches
5. ✅ Start Sprint 1!

**Questions? Contact**:
- Tech Lead: [Name]
- Scrum Master: [Name]
- Slack: #ems-backend-dev

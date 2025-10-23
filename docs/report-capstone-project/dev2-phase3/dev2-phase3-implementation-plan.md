# 📋 KẾ HOẠCH IMPLEMENTATION - DEV 2 PHASE 3

**Module**: Academic Curriculum - Learning Outcomes & Materials
**Phase**: 3/3 (Week 4-5)
**Developer**: DEV 2 - Academic Curriculum Lead
**Ngày tạo**: 2025-10-23
**Phụ thuộc**: Phase 1-2 đã hoàn thành (Subject, Level, Course, CoursePhase, CourseSession)

---

## 📊 TỔNG QUAN

### Mục tiêu Phase 3
Implement hệ thống Learning Outcomes (PLO/CLO) và Course Materials để hoàn thiện module Academic Curriculum, bao gồm:
1. **PLO Management** - Program Learning Outcomes cho Subject
2. **CLO Management** - Course Learning Outcomes cho Course
3. **PLO-CLO Mapping** - Liên kết giữa PLO và CLO (many-to-many)
4. **CLO-Session Mapping** - Liên kết CLO với CourseSession
5. **Course Material Management** - Upload và quản lý tài liệu học tập

### Tình trạng hiện tại
✅ **Đã có sẵn**:
- Entities: `Plo`, `Clo`, `PloCloMapping`, `CourseSessionCloMapping`, `CourseMaterial`
- Repositories: `PloRepository`, `CloRepository`, `CourseMaterialRepository`
- Service interfaces: `PloService`, `CloService`, `CourseMaterialService`
- DTOs: `PloDTO`, `CloDTO`, `CreatePloRequestDTO`, `CreateCloRequestDTO`, `MappingRequestDTO`, `CourseMaterialDTO`, `UploadMaterialRequestDTO`
- Controllers: `PloController`, `CloController`, `CourseMaterialController` (stub)

❌ **Cần implement**:
- Service implementations (hiện tại chỉ có stub trả về `null`)
- Repository cho mapping tables: `PloCloMappingRepository`, `CourseSessionCloMappingRepository`
- Business logic cho CRUD và mapping operations
- Validation logic phức tạp (cross-entity validation)
- Controller implementations
- Unit tests và Integration tests

---

## 🏗️ KIẾN TRÚC & DEPENDENCIES

### Entity Relationships

```
Subject (1) ────→ (*) Plo
                       ↓
                  PloCloMapping (many-to-many)
                       ↓
Course (1) ─────→ (*) Clo
                       ↓
                  CourseSessionCloMapping (many-to-many)
                       ↓
CoursePhase (1) ─→ (*) CourseSession

Course (1) ─────→ (*) CourseMaterial
CoursePhase (1) ─→ (*) CourseMaterial (optional)
CourseSession (1) ─→ (*) CourseMaterial (optional)
```

### Key Constraints & Business Rules

1. **PLO (Program Learning Outcomes)**
   - Thuộc về một Subject
   - Unique constraint: `(subject_id, code)`
   - Một PLO có thể map với nhiều CLO

2. **CLO (Course Learning Outcomes)**
   - Thuộc về một Course
   - Unique constraint: `(course_id, code)`
   - Một CLO có thể map với nhiều PLO và nhiều CourseSession

3. **PLO-CLO Mapping**
   - Composite primary key: `(plo_id, clo_id)`
   - **Critical validation**: PLO và CLO phải cùng Subject (CLO.course.subject = PLO.subject)
   - Có status field (optional - có thể dùng để track mapping state)

4. **CLO-Session Mapping**
   - Composite primary key: `(course_session_id, clo_id)`
   - **Critical validation**: CourseSession và CLO phải cùng Course
   - Có status field (optional)

5. **CourseMaterial**
   - Thuộc về một Course (bắt buộc)
   - Có thể thuộc về CoursePhase hoặc CourseSession (optional)
   - **Business rule**: Phải có ít nhất một trong 3: course_id, phase_id, course_session_id
   - URL storage: Có thể local path hoặc S3/cloud storage URL

---

## 📦 TASK BREAKDOWN

### Task 1: Tạo Missing Repositories (30 phút)

**File cần tạo**:
1. `PloCloMappingRepository.java`
2. `CourseSessionCloMappingRepository.java`

**Requirements**:
- Interface extends `JpaRepository`
- Custom query methods để:
  - Find mappings by PLO ID
  - Find mappings by CLO ID
  - Find mappings by CourseSession ID
  - Check existence của mapping
  - Delete mappings

**Tham khảo pattern**: Các repository existing trong `src/main/java/org/fyp/emssep490be/repositories/`

---

### Task 2: Implement PloService (2-3 giờ)

**File**: `src/main/java/org/fyp/emssep490be/services/plo/impl/PloServiceImpl.java`

#### 2.1. Method: `getPlosBySubject(Long subjectId)`

**Logic flow**:
1. Validate `subjectId` tồn tại (throw `SUBJECT_NOT_FOUND` nếu không)
2. Query `PloRepository.findBySubjectId(subjectId)`
3. Convert list of `Plo` entities sang `PloDTO`
4. Tính `mappedClosCount` cho mỗi PLO:
   - Query `PloCloMappingRepository.countByPloId(ploId)`
   - Hoặc dùng `@Query` join để fetch cùng lúc (optimize N+1 query)
5. Return list of `PloDTO`

**Validations**:
- Subject phải tồn tại
- Log warning nếu không có PLO nào

**Test cases**:
- Subject có PLO → return list đầy đủ
- Subject không có PLO → return empty list
- Subject không tồn tại → throw exception
- PLO có mappings → `mappedClosCount` > 0
- PLO không có mappings → `mappedClosCount` = 0

---

#### 2.2. Method: `createPlo(Long subjectId, CreatePloRequestDTO request)`

**Logic flow**:
1. Validate `subjectId` tồn tại
2. Validate `code` unique cho subject:
   - Check `PloRepository.existsByCodeAndSubjectId(code, subjectId)`
   - Throw `PLO_CODE_DUPLICATE` nếu đã tồn tại
3. Validate request data:
   - `code` không blank, pattern `^[A-Z0-9-]+$` (uppercase, numbers, dashes only)
   - `description` không blank
4. Load `Subject` entity
5. Create `Plo` entity:
   - Set subject, code, description
   - Set `createdAt` = `updatedAt` = current timestamp (UTC)
6. Save entity
7. Convert to `PloDTO` với `mappedClosCount = 0`
8. Return DTO

**Validations**:
- Subject tồn tại
- Code unique per subject
- Code format valid (uppercase, no special chars except dash)
- Description not empty

**Test cases**:
- Create PLO thành công
- Duplicate code trong cùng subject → exception
- Duplicate code khác subject → OK
- Subject không tồn tại → exception
- Invalid code format → validation error

---

#### 2.3. Method: `deletePlo(Long subjectId, Long id)`

**Logic flow**:
1. Validate PLO tồn tại với `subjectId`:
   - `PloRepository.findByIdAndSubjectId(id, subjectId)`
   - Throw `PLO_NOT_FOUND` nếu không tồn tại
2. **Critical check**: Kiểm tra PLO có mappings không:
   - Query `PloCloMappingRepository.existsByPloId(id)`
   - Nếu có mappings → throw `PLO_HAS_MAPPINGS` (không cho phép xóa)
3. Delete PLO entity
4. Log success

**Validations**:
- PLO tồn tại và thuộc subject đúng
- PLO không có mappings với CLO

**Test cases**:
- Delete PLO không có mapping → success
- Delete PLO có mapping → exception
- Delete PLO không tồn tại → exception
- Delete PLO của subject khác → exception

---

### Task 3: Implement CloService (4-5 giờ)

**File**: `src/main/java/org/fyp/emssep490be/services/clo/impl/CloServiceImpl.java`

**Dependencies cần inject**:
- `CloRepository`
- `PloRepository`
- `PloCloMappingRepository`
- `CourseSessionCloMappingRepository`
- `CourseRepository`
- `CourseSessionRepository`

---

#### 3.1. Method: `getClosByCourse(Long courseId)`

**Logic flow**:
1. Validate course tồn tại
2. Query `CloRepository.findByCourseId(courseId)`
3. Convert sang `CloDTO` với:
   - `mappedPlos`: Load từ `PloCloMappingRepository` join với `Plo`
   - `mappedSessionsCount`: Count từ `CourseSessionCloMappingRepository`
4. Return list of `CloDTO`

**Optimization**:
- Sử dụng `@Query` với JOIN FETCH để tránh N+1 query problem
- Có thể dùng `@EntityGraph` trong repository

**Test cases**:
- Course có CLO với mappings → return đầy đủ data
- Course có CLO không có mappings → empty mappedPlos, count = 0
- Course không có CLO → empty list
- Course không tồn tại → exception

---

#### 3.2. Method: `createClo(Long courseId, CreateCloRequestDTO request)`

**Logic flow**:
1. Validate course tồn tại
2. Validate `code` unique:
   - `CloRepository.existsByCodeAndCourseId(code, courseId)`
   - Throw `CLO_CODE_DUPLICATE` nếu duplicate
3. Validate format:
   - Code pattern `^[A-Z0-9-]+$`
   - Description not empty
4. Load Course entity
5. Create CLO entity với timestamps
6. Save và return DTO

**Test cases**:
- Tương tự PLO creation
- Validate course approved? (optional - business rule decision)

---

#### 3.3. Method: `mapPloToClo(Long ploId, Long cloId, MappingRequestDTO request)`

**Logic flow - CRITICAL**:
1. Load PLO entity:
   - `PloRepository.findById(ploId)` → throw `PLO_NOT_FOUND`
2. Load CLO entity:
   - `CloRepository.findById(cloId)` → throw `CLO_NOT_FOUND`
3. **Validate cùng Subject** (quan trọng nhất):
   ```java
   Long ploSubjectId = plo.getSubject().getId();
   Long cloSubjectId = clo.getCourse().getSubject().getId();

   if (!ploSubjectId.equals(cloSubjectId)) {
       throw new CustomException(ErrorCode.PLO_CLO_SUBJECT_MISMATCH);
   }
   ```
4. Check mapping đã tồn tại:
   - `PloCloMappingRepository.existsByPloIdAndCloId(ploId, cloId)`
   - Nếu đã tồn tại → throw `PLO_CLO_MAPPING_ALREADY_EXISTS`
5. Create `PloCloMapping`:
   - Create composite key: `new PloCloMappingId(ploId, cloId)`
   - Set plo, clo entities
   - Set status từ request (hoặc default "active")
6. Save mapping
7. Return response map:
   ```java
   Map.of(
       "ploId", ploId,
       "cloId", cloId,
       "status", mapping.getStatus(),
       "message", "PLO-CLO mapping created successfully"
   )
   ```

**Test cases**:
- Map PLO-CLO cùng subject → success
- Map PLO-CLO khác subject → exception
- Map duplicate → exception
- PLO không tồn tại → exception
- CLO không tồn tại → exception
- Verify mapping xuất hiện trong `getClosByCourse`

---

#### 3.4. Method: `mapCloToSession(Long sessionId, Long cloId, MappingRequestDTO request)`

**Logic flow - CRITICAL**:
1. Load CourseSession:
   - `CourseSessionRepository.findById(sessionId)` → throw `COURSE_SESSION_NOT_FOUND`
2. Load CLO:
   - `CloRepository.findById(cloId)` → throw `CLO_NOT_FOUND`
3. **Validate cùng Course**:
   ```java
   Long sessionCourseId = courseSession.getPhase().getCourse().getId();
   Long cloCourseId = clo.getCourse().getId();

   if (!sessionCourseId.equals(cloCourseId)) {
       throw new CustomException(ErrorCode.CLO_SESSION_COURSE_MISMATCH);
   }
   ```
4. Check duplicate mapping
5. Create `CourseSessionCloMapping`:
   - Composite key: `new CourseSessionCloMappingId(sessionId, cloId)`
   - Set entities và status
6. Save và return response

**Test cases**:
- Map CLO-Session cùng course → success
- Map CLO-Session khác course → exception
- Duplicate mapping → exception
- Entities không tồn tại → exception

---

#### 3.5. Method: `deleteClo(Long courseId, Long id)`

**Logic flow**:
1. Validate CLO tồn tại với courseId
2. **Check dependencies**:
   - Count PLO mappings: `PloCloMappingRepository.countByCloId(id)`
   - Count Session mappings: `CourseSessionCloMappingRepository.countByCloId(id)`
   - Nếu > 0 → throw `CLO_HAS_MAPPINGS`
3. Delete CLO
4. Log success

**Alternative approach** (nếu cần soft delete):
- Thay vì delete, có thể thêm `status` field vào CLO entity
- Set status = "INACTIVE" thay vì delete

**Test cases**:
- Delete CLO không mapping → success
- Delete CLO có PLO mapping → exception
- Delete CLO có Session mapping → exception
- Delete CLO có cả 2 → exception

---

### Task 4: Implement CourseMaterialService (3-4 giờ)

**File**: `src/main/java/org/fyp/emssep490be/services/coursematerial/impl/CourseMaterialServiceImpl.java`

**Dependencies cần inject**:
- `CourseMaterialRepository`
- `CourseRepository`
- `CoursePhaseRepository`
- `CourseSessionRepository`
- `UserAccountRepository` (hoặc lấy từ SecurityContext)

---

#### 4.1. Method: `uploadMaterial(Long courseId, UploadMaterialRequestDTO request)`

**Logic flow**:
1. Validate course tồn tại
2. Validate request:
   - `title` not blank
   - `url` not blank (hoặc file upload - xem implementation detail)
   - **Business rule**: Phải có ít nhất 1 trong 3:
     ```java
     if (courseId == null && request.getPhaseId() == null
         && request.getCourseSessionId() == null) {
         throw new CustomException(ErrorCode.MATERIAL_MUST_HAVE_CONTEXT);
     }
     ```
3. Validate optional references:
   - Nếu có `phaseId`: Validate phase thuộc course
   - Nếu có `courseSessionId`: Validate session thuộc course (qua phase)
4. **File handling** (2 options):

   **Option A - Local Storage (MVP)**:
   - Lưu file vào `upload/materials/{courseId}/` directory
   - Generate unique filename: `{timestamp}_{originalFilename}`
   - Set url = relative path

   **Option B - S3/Cloud Storage (Production)**:
   - Upload file lên S3 bucket
   - Set url = S3 object URL
   - Implement retry logic
5. Get current user từ SecurityContext:
   ```java
   Authentication auth = SecurityContextHolder.getContext().getAuthentication();
   String username = auth.getName();
   UserAccount user = userAccountRepository.findByEmail(username)
       .orElseThrow(() -> new CustomException(ErrorCode.USER_NOT_FOUND));
   ```
6. Create `CourseMaterial` entity:
   - Set course, phase (optional), courseSession (optional)
   - Set title, url
   - Set uploadedBy = current user
   - Set timestamps
7. Save và return DTO

**File upload considerations**:
- Max file size validation (e.g., 50MB)
- File type validation (PDF, DOCX, PPTX, images, videos)
- Virus scan (optional for MVP)
- Storage quota per course (optional)

**Test cases**:
- Upload material for course only → success
- Upload material for course + phase → success
- Upload material for course + session → success
- Upload without course → exception
- Upload với phase không thuộc course → exception
- Upload với session không thuộc course → exception
- Invalid file type → exception
- File too large → exception

---

#### 4.2. Method: `deleteMaterial(Long courseId, Long id)`

**Logic flow**:
1. Validate material tồn tại với courseId:
   - `CourseMaterialRepository.findByIdAndCourseId(id, courseId)`
2. **File cleanup**:
   - Option A (Local): Delete file từ storage directory
   - Option B (S3): Delete object từ S3 bucket
   - Handle errors gracefully (log warning nếu file không tồn tại)
3. Delete database record
4. Log success

**Test cases**:
- Delete material → file và DB record đều bị xóa
- Delete material không tồn tại → exception
- Delete material của course khác → exception
- File không tồn tại trên disk → warning log, DB record vẫn xóa

---

### Task 5: Implement Controllers (2-3 giờ)

#### 5.1. PloController

**File**: `src/main/java/org/fyp/emssep490be/controllers/plo/PloController.java`

**Endpoints cần implement**:
1. `GET /subjects/{subject_id}/plos` - Get PLOs by subject
2. `POST /subjects/{subject_id}/plos` - Create PLO
3. `DELETE /subjects/{subject_id}/plos/{id}` - Delete PLO

**Pattern**:
- Follow existing controller pattern từ `SubjectController`, `CourseController`
- Use `@PreAuthorize("hasAnyRole('ADMIN', 'SUBJECT_LEADER')")`
- Return `ResponseObject<T>` format
- Use `@Valid` cho request validation

**Example skeleton**:
```java
@RestController
@RequestMapping("/api/subjects/{subjectId}/plos")
@RequiredArgsConstructor
@Validated
public class PloController {
    private final PloService ploService;

    @GetMapping
    @PreAuthorize("hasAnyRole('ADMIN', 'SUBJECT_LEADER', 'ACADEMIC_STAFF')")
    public ResponseEntity<ResponseObject<List<PloDTO>>> getPlosBySubject(
            @PathVariable Long subjectId) {
        // Implementation
    }

    @PostMapping
    @PreAuthorize("hasAnyRole('ADMIN', 'SUBJECT_LEADER')")
    public ResponseEntity<ResponseObject<PloDTO>> createPlo(
            @PathVariable Long subjectId,
            @Valid @RequestBody CreatePloRequestDTO request) {
        // Implementation
    }

    @DeleteMapping("/{id}")
    @PreAuthorize("hasAnyRole('ADMIN', 'SUBJECT_LEADER')")
    public ResponseEntity<ResponseObject<Void>> deletePlo(
            @PathVariable Long subjectId,
            @PathVariable Long id) {
        // Implementation
    }
}
```

---

#### 5.2. CloController

**File**: `src/main/java/org/fyp/emssep490be/controllers/clo/CloController.java`

**Endpoints**:
1. `GET /courses/{course_id}/clos` - Get CLOs by course
2. `POST /courses/{course_id}/clos` - Create CLO
3. `POST /plos/{plo_id}/map-clo/{clo_id}` - Map PLO to CLO
4. `POST /sessions/{session_id}/map-clo/{clo_id}` - Map CLO to Session
5. `DELETE /courses/{course_id}/clos/{id}` - Delete CLO

**Special notes**:
- Mapping endpoints có thể đặt ở đâu? → Đề xuất nested route hoặc dedicated mapping controller
- Consider RESTful design cho mapping operations

---

#### 5.3. CourseMaterialController

**File**: `src/main/java/org/fyp/emssep490be/controllers/coursematerial/CourseMaterialController.java`

**Endpoints**:
1. `POST /courses/{course_id}/materials` - Upload material
2. `DELETE /courses/{course_id}/materials/{id}` - Delete material

**Special handling**:
- `@RequestBody` với `MultipartFile` cho file upload
- Content-Type: `multipart/form-data`
- Max request size configuration trong `application.yml`

**Example**:
```java
@PostMapping(consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
public ResponseEntity<ResponseObject<CourseMaterialDTO>> uploadMaterial(
        @PathVariable Long courseId,
        @RequestParam("file") MultipartFile file,
        @RequestParam("title") String title,
        @RequestParam(required = false) Long phaseId,
        @RequestParam(required = false) Long courseSessionId) {
    // Build DTO from params
    // Call service
}
```

---

### Task 6: Add Error Codes (30 phút)

**File**: `src/main/java/org/fyp/emssep490be/exceptions/ErrorCode.java`

**Error codes cần thêm**:
```java
// PLO errors
PLO_NOT_FOUND(404, "PLO not found"),
PLO_CODE_DUPLICATE(400, "PLO code already exists for this subject"),
PLO_HAS_MAPPINGS(400, "Cannot delete PLO with existing CLO mappings"),

// CLO errors
CLO_NOT_FOUND(404, "CLO not found"),
CLO_CODE_DUPLICATE(400, "CLO code already exists for this course"),
CLO_HAS_MAPPINGS(400, "Cannot delete CLO with existing mappings"),

// Mapping errors
PLO_CLO_SUBJECT_MISMATCH(400, "PLO and CLO must belong to the same subject"),
PLO_CLO_MAPPING_ALREADY_EXISTS(400, "This PLO-CLO mapping already exists"),
CLO_SESSION_COURSE_MISMATCH(400, "CLO and CourseSession must belong to the same course"),
CLO_SESSION_MAPPING_ALREADY_EXISTS(400, "This CLO-Session mapping already exists"),

// Material errors
COURSE_MATERIAL_NOT_FOUND(404, "Course material not found"),
MATERIAL_MUST_HAVE_CONTEXT(400, "Material must be associated with course, phase, or session"),
INVALID_FILE_TYPE(400, "File type not allowed"),
FILE_TOO_LARGE(400, "File size exceeds maximum limit"),
FILE_UPLOAD_FAILED(500, "Failed to upload file"),

// Other
COURSE_SESSION_NOT_FOUND(404, "Course session not found"),
```

---

### Task 7: Write Unit Tests (4-6 giờ)

#### 7.1. PloServiceImplTest

**File**: `src/test/java/org/fyp/emssep490be/services/plo/impl/PloServiceImplTest.java`

**Test structure** (15-18 tests):
```
@ExtendWith(MockitoExtension.class)
class PloServiceImplTest {
    @Mock private PloRepository ploRepository;
    @Mock private SubjectRepository subjectRepository;
    @Mock private PloCloMappingRepository ploCloMappingRepository;
    @InjectMocks private PloServiceImpl ploService;

    // Setup test data

    // getPlosBySubject tests (4 tests)
    - testGetPlosBySubject_Success_WithMappings
    - testGetPlosBySubject_Success_NoMappings
    - testGetPlosBySubject_Success_EmptyList
    - testGetPlosBySubject_SubjectNotFound

    // createPlo tests (6 tests)
    - testCreatePlo_Success
    - testCreatePlo_SubjectNotFound
    - testCreatePlo_DuplicateCode_SameSubject
    - testCreatePlo_DuplicateCode_DifferentSubject_Success
    - testCreatePlo_InvalidCodeFormat
    - testCreatePlo_EmptyDescription

    // deletePlo tests (5 tests)
    - testDeletePlo_Success
    - testDeletePlo_NotFound
    - testDeletePlo_WrongSubject
    - testDeletePlo_HasMappings
    - testDeletePlo_VerifyRepositoryCalls
}
```

**Test pattern** (tham khảo từ Phase 1-2):
- Use Mockito `when().thenReturn()` cho mocking
- Use `verify()` để verify repository calls
- Use `assertThrows()` cho exception cases
- Use `assertEquals()`, `assertNotNull()` cho assertions

---

#### 7.2. CloServiceImplTest

**File**: `src/test/java/org/fyp/emssep490be/services/clo/impl/CloServiceImplTest.java`

**Test coverage** (20-25 tests):
```
// getClosByCourse tests (4 tests)
- testGetClosByCourse_Success_WithMappings
- testGetClosByCourse_Success_NoMappings
- testGetClosByCourse_EmptyList
- testGetClosByCourse_CourseNotFound

// createClo tests (5 tests)
- testCreateClo_Success
- testCreateClo_CourseNotFound
- testCreateClo_DuplicateCode
- testCreateClo_InvalidFormat
- testCreateClo_EmptyDescription

// mapPloToClo tests (7 tests)
- testMapPloToClo_Success
- testMapPloToClo_PloNotFound
- testMapPloToClo_CloNotFound
- testMapPloToClo_DifferentSubjects_Exception
- testMapPloToClo_SameSubject_Success
- testMapPloToClo_MappingAlreadyExists
- testMapPloToClo_VerifyCompositeKey

// mapCloToSession tests (6 tests)
- testMapCloToSession_Success
- testMapCloToSession_SessionNotFound
- testMapCloToSession_CloNotFound
- testMapCloToSession_DifferentCourses_Exception
- testMapCloToSession_SameCourse_Success
- testMapCloToSession_MappingAlreadyExists

// deleteClo tests (5 tests)
- testDeleteClo_Success
- testDeleteClo_NotFound
- testDeleteClo_HasPloMappings
- testDeleteClo_HasSessionMappings
- testDeleteClo_HasBothMappings
```

**Critical tests**:
- Subject/Course mismatch validation là quan trọng nhất
- Composite key creation
- Mapping existence checks

---

#### 7.3. CourseMaterialServiceImplTest

**File**: `src/test/java/org/fyp/emssep490be/services/coursematerial/impl/CourseMaterialServiceImplTest.java`

**Test coverage** (12-15 tests):
```
// uploadMaterial tests (8 tests)
- testUploadMaterial_CourseOnly_Success
- testUploadMaterial_CourseAndPhase_Success
- testUploadMaterial_CourseAndSession_Success
- testUploadMaterial_AllThree_Success
- testUploadMaterial_NoContext_Exception
- testUploadMaterial_PhaseNotInCourse_Exception
- testUploadMaterial_SessionNotInCourse_Exception
- testUploadMaterial_InvalidFileType_Exception

// deleteMaterial tests (4 tests)
- testDeleteMaterial_Success
- testDeleteMaterial_NotFound
- testDeleteMaterial_WrongCourse
- testDeleteMaterial_FileCleanup_Success
```

**Mock considerations**:
- Mock file operations (nếu có file handling)
- Mock SecurityContext để get current user
- Verify file cleanup calls

---

### Task 8: Integration Tests (3-4 giờ - Optional cho Phase 3)

**Quyết định**: Integration tests có thể defer sang sau MVP vì:
- Phase 3 phức tạp với nhiều cross-entity validations
- Unit tests đã cover được business logic
- Integration tests sẽ cần setup nhiều test data (Subject, Course, Phase, Session)

**Nếu implement**:
- Sử dụng `@SpringBootTest` với `PostgreSQLTestContainer`
- Test end-to-end workflows:
  - Create Subject → Create PLO → Create Course → Create CLO → Map PLO-CLO
  - Create Course → Create Phase → Create Session → Create CLO → Map CLO-Session
  - Upload material → Verify file exists → Delete material → Verify cleanup

---

## ⚠️ CRITICAL VALIDATIONS

### 1. PLO-CLO Mapping Subject Validation
```
PLO.subject.id == CLO.course.subject.id
```
**Tại sao quan trọng**:
- Business rule: CLO kế thừa PLO của Subject
- Nếu map sai → learning outcomes không align với program outcomes

**Implementation strategy**:
- Load full entity graph: `PLO → Subject` và `CLO → Course → Subject`
- So sánh subject ID
- Throw clear exception với message giải thích

---

### 2. CLO-Session Mapping Course Validation
```
CourseSession.phase.course.id == CLO.course.id
```
**Tại sao quan trọng**:
- Một session chỉ được map với CLO của course đó
- Prevent data integrity issues

**Implementation strategy**:
- Load entity graph: `CourseSession → CoursePhase → Course`
- Compare course ID
- Clear exception handling

---

### 3. CourseMaterial Context Validation
```
At least one of: course_id, phase_id, course_session_id must exist
AND if phase_id: phase.course.id == course_id
AND if course_session_id: session.phase.course.id == course_id
```

**Implementation strategy**:
- Multi-level validation
- Cascading checks

---

## 🧪 TESTING STRATEGY

### Test Coverage Goals
- **Unit Tests**: > 90% (vì logic phức tạp)
- **Integration Tests**: > 70% (optional cho Phase 3)

### Test Priorities
1. **P0 (Must have)**:
   - All service method happy paths
   - All critical validations (subject/course mismatch)
   - All exception cases
   - Repository interactions

2. **P1 (Should have)**:
   - Edge cases (empty lists, null values)
   - Composite key operations
   - Mapping count calculations

3. **P2 (Nice to have)**:
   - Integration tests
   - Performance tests
   - Concurrent operation tests

---

## 📝 IMPLEMENTATION CHECKLIST

### Pre-implementation
- [ ] Review Phase 1-2 code patterns
- [ ] Review OpenAPI spec (`openapi-academic.yaml`)
- [ ] Study entity relationships
- [ ] Understand composite key pattern

### Development Phase
- [ ] **Task 1**: Create `PloCloMappingRepository` (30m)
- [ ] **Task 1**: Create `CourseSessionCloMappingRepository` (30m)
- [ ] **Task 2**: Implement `PloServiceImpl` (2-3h)
  - [ ] `getPlosBySubject`
  - [ ] `createPlo`
  - [ ] `deletePlo`
- [ ] **Task 3**: Implement `CloServiceImpl` (4-5h)
  - [ ] `getClosByCourse`
  - [ ] `createClo`
  - [ ] `mapPloToClo` ⚠️ Critical
  - [ ] `mapCloToSession` ⚠️ Critical
  - [ ] `deleteClo`
- [ ] **Task 4**: Implement `CourseMaterialServiceImpl` (3-4h)
  - [ ] `uploadMaterial`
  - [ ] `deleteMaterial`
  - [ ] File handling logic
- [ ] **Task 5**: Implement Controllers (2-3h)
  - [ ] `PloController`
  - [ ] `CloController`
  - [ ] `CourseMaterialController`
- [ ] **Task 6**: Add ErrorCodes (30m)
- [ ] **Task 7**: Write Unit Tests (4-6h)
  - [ ] `PloServiceImplTest` (15-18 tests)
  - [ ] `CloServiceImplTest` (20-25 tests)
  - [ ] `CourseMaterialServiceImplTest` (12-15 tests)
- [ ] **Task 8**: Integration Tests (3-4h - Optional)

### Testing Phase
- [ ] Run all unit tests: `./mvnw test`
- [ ] Verify test coverage: `./mvnw test jacoco:report`
- [ ] Check coverage report: `target/site/jacoco/index.html`
- [ ] Target: > 90% coverage for new code

### Manual Testing
- [ ] Test via Swagger UI: `http://localhost:8080/swagger-ui/index.html`
- [ ] Test PLO CRUD operations
- [ ] Test CLO CRUD operations
- [ ] Test PLO-CLO mapping (same subject ✓, different subject ✗)
- [ ] Test CLO-Session mapping (same course ✓, different course ✗)
- [ ] Test material upload (with file)
- [ ] Test material delete (verify file cleanup)
- [ ] Test validation error messages

### Documentation
- [ ] Update `docs/work-division-plan.md` - mark Phase 3 as completed
- [ ] Create implementation notes (this document)
- [ ] Document API examples in Postman/curl
- [ ] Update README if needed

---

## 🚀 ESTIMATED TIME

| Task | Estimated Time | Priority |
|------|----------------|----------|
| Mapping Repositories | 1 hour | P0 |
| PloService Implementation | 2-3 hours | P0 |
| CloService Implementation | 4-5 hours | P0 |
| CourseMaterialService Implementation | 3-4 hours | P0 |
| Controllers Implementation | 2-3 hours | P0 |
| Error Codes | 30 minutes | P0 |
| Unit Tests | 4-6 hours | P0 |
| Integration Tests | 3-4 hours | P1 (Optional) |
| Manual Testing | 2 hours | P0 |
| Documentation | 1 hour | P1 |
| **TOTAL** | **22-29 hours** | |

**Realistic estimate**: 3-4 working days (với 8h/day)

---

## 🎯 DELIVERABLES

### Code Deliverables
1. ✅ 2 Repository interfaces (PloCloMapping, CourseSessionCloMapping)
2. ✅ 3 Service implementations (Plo, Clo, CourseMaterial)
3. ✅ 3 Controller implementations
4. ✅ 15+ ErrorCode entries
5. ✅ 50+ unit tests với > 90% coverage

### Documentation Deliverables
1. ✅ Implementation plan (this document)
2. ✅ API testing examples
3. ✅ Known issues/limitations list
4. ✅ Next phase recommendations

---

## 🔍 KNOWN CHALLENGES & SOLUTIONS

### Challenge 1: N+1 Query Problem
**Problem**: `getClosByCourse` cần load mapped PLOs → có thể gây N+1 queries

**Solution**:
- Option A: Sử dụng `@Query` với JOIN FETCH trong repository
- Option B: Sử dụng `@EntityGraph` annotation
- Option C: Load mappings riêng và group by CLO ID (manual join trong service)

**Recommended**: Option A (JOIN FETCH query)

---

### Challenge 2: File Upload Implementation
**Problem**: MVP nên dùng local storage hay S3?

**Solution**:
- **Phase 3 MVP**: Local storage (simple, fast to implement)
- **Production**: S3 integration (abstraction layer ready)
- Design interface: `FileStorageService` với 2 implementations:
  - `LocalFileStorageServiceImpl`
  - `S3FileStorageServiceImpl`

**Implementation**:
- Create `@Service` interface
- Use `@Profile` để switch implementation
- Config in `application.yml`

---

### Challenge 3: Composite Key Testing
**Problem**: Test composite keys phức tạp hơn simple ID

**Solution**:
- Create test helper methods để create composite keys
- Test equals/hashCode của ID classes
- Verify Hibernate persistence với composite keys

---

### Challenge 4: Cross-entity Validation
**Problem**: Validate PLO-CLO cùng subject cần load nhiều entities

**Solution**:
- Sử dụng JOIN queries thay vì lazy loading
- Cache entity relationships trong memory
- Consider DTO projection để giảm data loading

---

## 📋 ACCEPTANCE CRITERIA

### Functional Requirements
- [ ] CRUD operations cho PLO hoạt động đúng
- [ ] CRUD operations cho CLO hoạt động đúng
- [ ] PLO-CLO mapping chỉ cho phép cùng subject
- [ ] CLO-Session mapping chỉ cho phép cùng course
- [ ] Material upload lưu file thành công
- [ ] Material delete xóa cả file và DB record
- [ ] Không thể xóa PLO/CLO có mappings
- [ ] Validation errors trả về message rõ ràng

### Non-functional Requirements
- [ ] Test coverage > 90%
- [ ] API response time < 500ms (CRUD operations)
- [ ] API response time < 1000ms (complex queries với mappings)
- [ ] Không có N+1 query issues
- [ ] Code pass SonarQube quality gate
- [ ] No critical/blocker issues

---

## 🔗 REFERENCES

### Documentation
- Work Division Plan: `docs/work-division-plan.md` (lines 248-297)
- Instruction Guide: `docs/instruction.md`
- OpenAPI Spec: `docs/openapi/openapi-academic.yaml` (lines 487-653)
- CLAUDE.md: Project architecture

### Code References
- Entity examples: `src/main/java/org/fyp/emssep490be/entities/Plo.java`
- Service pattern: `src/main/java/org/fyp/emssep490be/services/subject/impl/SubjectServiceImpl.java`
- Test pattern: `src/test/java/org/fyp/emssep490be/services/subject/impl/SubjectServiceImplTest.java`
- Controller pattern: `src/main/java/org/fyp/emssep490be/controllers/subject/SubjectController.java`

---

## ✅ DEFINITION OF DONE

### Per Service Method
- [x] Implementation không return null
- [x] Business logic correct
- [x] Validation đầy đủ
- [x] Exception handling đúng pattern
- [x] Logging (info + error)
- [x] Unit tests với > 90% coverage
- [x] Test pass locally

### Per Module (Phase 3)
- [x] All service methods implemented
- [x] All controller endpoints working
- [x] All unit tests pass (50+ tests)
- [x] Integration tests pass (nếu có)
- [x] Manual testing success via Swagger UI
- [x] Code review approved
- [x] No SonarQube critical issues
- [x] Documentation updated

---

## 🎉 NEXT STEPS (After Phase 3)

### Immediate Next
1. Merge Phase 3 code vào main branch
2. Update work-division-plan.md status
3. Demo PLO/CLO/Material features cho team
4. Gather feedback

### Future Enhancements (Post-MVP)
1. **S3 Integration** cho file storage
2. **Batch operations** cho mapping (map multiple CLOs to one session)
3. **Versioning** cho CourseMaterial
4. **Analytics** cho learning outcomes alignment
5. **Export** PLO-CLO mapping reports
6. **Material preview** trong UI
7. **Search & filter** cho materials

### Coordination với Other Devs
- **DEV 4** cần PLO/CLO data để hiểu learning outcomes khi generate sessions
- **DEV 3** có thể cần material URLs để show trong teacher/student interface
- **Integration** với Session generation algorithm (DEV 4) - CLO mappings drive session planning

---

**Document Version**: 1.0
**Created**: 2025-10-23
**Author**: Claude Code (Assistant for DEV 2)
**Status**: Ready for Implementation
**Estimated Completion**: 3-4 working days
# Hướng Dẫn Sử Dụng Claude Code Hiệu Quả

> **Mục đích:** Giúp team EMS-SEP490 làm việc hiệu quả với Claude Code khi phát triển hệ thống.

## 📋 Mục Lục
1. [Quy Trình Làm Việc Chuẩn](#quy-trình-làm-việc-chuẩn)
2. [Mẫu Prompt Theo Module](#mẫu-prompt-theo-module)
3. [Best Practices](#best-practices)
4. [Các Tình Huống Thường Gặp](#các-tình-huống-thường-gặp)

---

## 🔄 Quy Trình Làm Việc Chuẩn

### Bước 1: Chuẩn Bị Context (Indexing & Analysis)

**Mục đích:** Giúp Claude hiểu rõ codebase và yêu cầu trước khi implement.

#### Template Prompt Chuẩn Bị:

```
Hãy phân tích và chuẩn bị cho việc implement [TÊN MODULE/FEATURE].

Các bước cần thực hiện:
1. Đọc và phân tích:
   - docs/feature-list.md (phần [SỐ MODULE]: [TÊN MODULE])
   - docs/api-design.md (phần [SỐ SECTION]: [TÊN SECTION])
   - CLAUDE.md (tất cả best practices liên quan)

2. Kiểm tra entities hiện có:
   - Liệt kê tất cả entities liên quan đến module này
   - Kiểm tra relationships, constraints, enums
   - Xác định entities nào đã implement, entities nào còn thiếu

3. Kiểm tra repositories:
   - Liệt kê repositories đã có
   - Xác định methods cần thêm (nếu có)

4. Kiểm tra DTOs:
   - Liệt kê DTOs hiện có
   - Xác định DTOs cần tạo mới

5. Phân tích business logic:
   - Liệt kê các business rules chính
   - Xác định validation rules
   - Xác định các luồng xử lý phức tạp (nếu có)

6. Tổng hợp:
   - Liệt kê files cần tạo mới
   - Liệt kê files cần chỉnh sửa
   - Đề xuất thứ tự implement

Sau khi phân tích xong, hãy cho tôi một bản tóm tắt về:
- Những gì đã có sẵn
- Những gì cần implement
- Những rủi ro/vấn đề tiềm ẩn cần lưu ý
```

### Bước 2: Review & Xác Nhận Plan

Sau khi Claude đưa ra phân tích và plan:
- ✅ Review kỹ các điểm Claude đề xuất
- ✅ Xác nhận/điều chỉnh scope nếu cần
- ✅ Hỏi thêm nếu có điểm chưa rõ

### Bước 3: Yêu Cầu Implementation

```
OK, hãy bắt đầu implement theo plan đã đề xuất.

Lưu ý:
- Tuân thủ tất cả best practices trong CLAUDE.md
- Apply đầy đủ validation và error handling
- Thêm logging ở các điểm quan trọng
- Viết code clean, tránh duplication
- Test từng phần nhỏ trước khi chuyển sang phần tiếp theo
```

---

## 📦 Mẫu Prompt Theo Module

### Module 1: Authentication & Authorization

#### Phase 1.1: Analysis & Preparation

```
Hãy phân tích và chuẩn bị cho việc implement Phase 1: Module 1 - Authentication & Authorization.

Các bước cần thực hiện:
1. Đọc docs/feature-list.md phần "1. User & Role Management"
2. Đọc docs/api-design.md phần "I. AUTHENTICATION & AUTHORIZATION"
3. Kiểm tra entities hiện có:
   - UserAccount, Role, UserRole, UserBranch
   - Student, Teacher
   - Các enums liên quan
4. Kiểm tra repositories đã có cho các entities này
5. Kiểm tra DTOs trong package dtos/auth/
6. Phân tích business rules:
   - JWT token generation/validation
   - Multi-role support (một user nhiều roles)
   - Multi-branch access control
   - Password hashing
   - Refresh token mechanism
7. Tổng hợp những gì cần implement:
   - AuthService methods
   - AuthController endpoints
   - Security configuration
   - JWT utilities
   - DTOs còn thiếu

Sau khi phân tích xong, đưa ra plan chi tiết với thứ tự implement hợp lý.
```

#### Phase 1.2: Implementation

```
OK, bắt đầu implement Authentication & Authorization theo plan.

Yêu cầu:
- Implement theo đúng pattern trong CLAUDE.md (Service Implementation Pattern)
- Sử dụng @RequiredArgsConstructor, @Slf4j, @Transactional
- Return ResponseObject<T> từ tất cả endpoints
- Xử lý đầy đủ edge cases (user not found, wrong password, expired token, etc.)
- Log tất cả authentication attempts (success/failed)
- Viết code tuân thủ "Implementation Plan: Core Principles" trong CLAUDE.md
```

---

### Module 2: Branch Management

```
Hãy phân tích và chuẩn bị cho việc implement Module 2 - Branch Management.

1. Đọc docs/feature-list.md phần "2. Organization & Infrastructure" (2.1)
2. Đọc docs/api-design.md phần "II. ORGANIZATION & RESOURCES" (2.1)
3. Kiểm tra:
   - Entity: Center, Branch (relationships, constraints)
   - Repository: CenterRepository, BranchRepository
   - DTOs: dtos/branch/
   - Service: BranchService interface
4. Phân tích business rules:
   - Unique constraint: (center_id, branch_code)
   - Branch status lifecycle
   - Multi-tenant access control
5. Plan implementation:
   - Service methods cần implement
   - Controller endpoints
   - DTOs còn thiếu
   - Validation rules

Sau khi phân tích, đề xuất plan implement chi tiết.
```

---

### Module 3: Subject, Level, Course Management

```
Hãy phân tích và chuẩn bị implement Module 3 - Academic Curriculum Management (Subject, Level, Course).

1. Đọc docs/feature-list.md phần "3. Academic Curriculum Management" (3.1-3.2)
2. Đọc docs/api-design.md phần tương ứng
3. Kiểm tra entities:
   - Subject, Level, Course
   - Các enum: subject_status_enum
   - Relationships và constraints
4. Phân tích business rules quan trọng:
   - Course approval workflow (approved_by_manager)
   - Version management (hash_checksum)
   - Prerequisites validation
   - Unique constraints
5. Plan implementation:
   - SubjectService, LevelService, CourseService
   - Controllers tương ứng
   - DTOs (create, update, detail, list)
   - Validation logic

Đề xuất plan với thứ tự hợp lý (Subject → Level → Course).
```

---

### Module 4: Teacher Management

```
Hãy phân tích và chuẩn bị implement Module 4 - Teacher Management.

1. Đọc docs/feature-list.md phần "4. Teacher Management"
2. Đọc docs/api-design.md phần tương ứng
3. Kiểm tra entities:
   - Teacher (1-1 với UserAccount)
   - TeacherSkill (composite key)
   - TeacherAvailability, TeacherAvailabilityOverride
   - Enums: skill_enum (general/reading/writing/speaking/listening)
4. Phân tích business rules:
   - Composite key pattern cho TeacherSkill
   - Availability vs Override logic
   - OT registration workflow
5. Plan implementation:
   - TeacherService methods
   - TeacherController endpoints
   - DTOs cho teacher profile, skills, availability
   - Query methods để tìm available teachers

Đề xuất plan implement chi tiết.
```

---

### Module 5: Class & Session Management ⚠️ (PHỨC TẠP)

```
Hãy phân tích kỹ và chuẩn bị cho Module 5 - Class & Session Management.

ĐÂY LÀ MODULE PHỨC TẠP NHẤT - cần đọc kỹ "Session-First Design Pattern" trong CLAUDE.md.

1. Đọc docs/feature-list.md phần "5. Class & Session Management"
2. Đọc CLAUDE.md phần "Session-First Design Pattern"
3. Kiểm tra entities:
   - ClassEntity, SessionEntity
   - CoursePhase, CourseSession (templates)
   - SessionResource, TeachingSlot
   - Enums: class_status_enum, session_status_enum, session_type_enum
4. Phân tích logic phức tạp:
   - AUTO-GENERATE SESSIONS từ course template:
     * Logic tính toán ngày session từ start_date + schedule_days[]
     * Mapping time_slot_template
     * Clone từ CourseSession template
   - Resource conflict detection
   - Teacher conflict detection
   - Class approval workflow
5. Plan implementation theo thứ tự:
   - Step 1: Basic class CRUD (không có auto-generate)
   - Step 2: Session generation algorithm
   - Step 3: Resource assignment + conflict check
   - Step 4: Teacher assignment + conflict check
   - Step 5: Approval workflow
   - Step 6: Reschedule logic

Đề xuất plan chi tiết với từng step nhỏ, vì module này rất phức tạp.
```

---

### Module 6: Enrollment & Student Management

```
Hãy phân tích và chuẩn bị implement Module 6 - Enrollment & Student Management.

Module này phụ thuộc vào Module 5 (Session đã được tạo).

1. Đọc docs/feature-list.md phần "6. Enrollment & Student Management"
2. Đọc CLAUDE.md phần "Session-First Design Pattern" (enrollment flow)
3. Kiểm tra entities:
   - Student, Enrollment
   - StudentSession (composite key)
   - Enums: enrollment_status_enum
4. Phân tích logic quan trọng:
   - AUTO-GENERATE StudentSession khi enroll:
     * Clone tất cả session của class
     * Set attendance_status='planned'
   - SYNCHRONIZATION: khi class thay đổi lịch, StudentSession phải sync
   - Late enrollment: chỉ tạo StudentSession cho session tương lai
   - Capacity check và conflict detection
5. Plan implementation:
   - Step 1: Student account creation
   - Step 2: Enrollment process + validation
   - Step 3: Auto-generate StudentSession
   - Step 4: Late enrollment logic
   - Step 5: Schedule synchronization trigger

Đề xuất plan implement chi tiết.
```

---

### Module 7: Attendance & Reporting

```
Hãy phân tích và chuẩn bị implement Module 7 - Attendance & Session Reporting.

1. Đọc docs/feature-list.md phần "7. Attendance & Session Reporting"
2. Kiểm tra entities:
   - StudentSession (trường attendance_status, recorded_at)
   - SessionEntity (trường teacher_note, status)
   - Assessment, Score
3. Phân tích business rules:
   - Attendance recording workflow
   - Attendance lock after T hours
   - Session report requirements
   - Score entry validation
4. Plan implementation:
   - AttendanceService methods
   - SessionReportService methods
   - AssessmentService methods
   - DTOs cho attendance list, session report, score entry

Đề xuất plan implement.
```

---

### Module 8: Request & Approval Flows ⚠️ (RẤT PHỨC TẠP)

```
Hãy phân tích CỰC KỲ KỸ Module 8 - Request & Approval Flows.

ĐÂY LÀ MODULE PHỨC TẠP NHẤT VỀ BUSINESS LOGIC.

Phase 8.1: Student Requests
1. Đọc docs/feature-list.md phần "8.1 Student Requests" (tất cả sub-sections)
2. Kiểm tra entities:
   - StudentRequest
   - Enum: student_request_type_enum, request_status_enum
3. Phân tích TỪNG LOẠI REQUEST:

   8.1.1 Absence Request (đơn giản nhất):
   - Input/Output
   - Validation rules
   - Approval flow

   8.1.2 Make-up Request (phức tạp):
   - Step-by-step workflow (5 steps trong docs)
   - Query tìm makeup sessions khả dụng
   - Validation cùng course_session_id
   - Create StudentSession với is_makeup=true

   8.1.3 Transfer Request (RẤT PHỨC TẠP):
   - Mapping session giữa 2 classes theo course_session_id
   - Cutoff logic (left_session_id, join_session_id)
   - Transaction phức tạp (update enrollment A, create enrollment B, sync StudentSession)
   - Edge cases

   8.1.4 Reschedule Request:
   - Workflow

4. Plan implementation:
   Phase 1: Absence request (đơn giản, để làm quen)
   Phase 2: Make-up request
   Phase 3: Transfer request (phức tạp nhất)
   Phase 4: Reschedule request

Phase 8.2: Teacher Requests
1. Đọc docs/feature-list.md phần "8.2 Teacher Requests"
2. Phân tích TỪNG LOẠI REQUEST:

   8.2.1 Leave Request với Substitution:
   - Query find_available_substitute_teachers (phức tạp)
   - Approval transaction: approve leave + create OT request + update TeachingSlot
   - Fallback options (reschedule/cancel)

   8.2.2 OT Request:
   - Auto-create khi assign substitute

   8.2.3 Reschedule Request:
   - Transaction: create new session + migrate all links + cancel old

   8.2.4 Cancellation Request:
   - Cascade updates

   8.2.5 Swap Request:
   - Swap TeachingSlot

3. Plan implementation theo thứ tự:
   Phase 1: Leave request (không có substitution)
   Phase 2: Substitution logic
   Phase 3: OT auto-generation
   Phase 4: Reschedule
   Phase 5: Cancel & Swap

ĐỀ XUẤT PLAN IMPLEMENTATION CỰC KỲ CHI TIẾT, TỪNG STEP NHỎ.
```

---

## 🎯 Best Practices

### 1. Luôn Bắt Đầu Với Analysis

**❌ KHÔNG NÊN:**
```
Hãy implement Authentication cho tôi.
```

**✅ NÊN:**
```
Hãy phân tích và chuẩn bị cho việc implement Phase 1: Module 1 - Authentication & Authorization.

1. Đọc docs/feature-list.md phần 1
2. Đọc docs/api-design.md phần I
3. Kiểm tra entities, repositories, DTOs đã có
4. Phân tích business rules
5. Đề xuất plan implementation

[Chi tiết như templates ở trên]
```

### 2. Kiểm Tra Entities & Relationships Trước

```
Trước khi implement [MODULE], hãy:
1. Liệt kê tất cả entities liên quan
2. Vẽ sơ đồ relationships (1-1, 1-N, N-N)
3. Liệt kê các constraints (unique, foreign key, check)
4. Xác định composite keys (nếu có)
5. Liệt kê các enums được sử dụng
```

### 3. Chia Module Phức Tạp Thành Phases

**Ví dụ Module 5 (Class & Session):**
```
Implement Module 5 theo phases:

Phase 5.1: Basic class CRUD
- Chỉ tạo class entity
- Không tạo sessions
- CRUD đơn giản

Phase 5.2: Session generation
- Implement algorithm tạo sessions từ template
- Test kỹ logic tính ngày

Phase 5.3: Resource assignment
- Implement conflict detection
- Test edge cases

[Và tiếp tục...]
```

### 4. Luôn Đề Cập Best Practices

```
Khi implement, hãy tuân thủ:
- "Implementation Plan: Core Principles" trong CLAUDE.md
- "Service Implementation Pattern" trong CLAUDE.md
- Sử dụng @RequiredArgsConstructor, @Slf4j, @Transactional
- Return ResponseObject<T>
- Logging đầy đủ
- Validation rules
- Error handling với GlobalExceptionHandler
```

### 5. Test Từng Phần Nhỏ

```
Sau khi implement [FEATURE], hãy:
1. Gợi ý test cases quan trọng
2. Gợi ý edge cases cần test
3. Gợi ý integration test scenarios
```

---

## 🔧 Các Tình Huống Thường Gặp

### Tình Huống 1: Cần Hiểu Một Luồng Nghiệp Vụ Phức Tạp

```
Hãy giải thích chi tiết luồng nghiệp vụ [TÊN LUỒNG] trong docs/feature-list.md.

Yêu cầu:
1. Vẽ sequence diagram (dạng text/mermaid)
2. Liệt kê các actors tham gia
3. Liệt kê từng step với input/output
4. Liệt kê các validation rules
5. Liệt kê các edge cases
6. Liệt kê entities bị ảnh hưởng (CRUD operations)
```

### Tình Huống 2: Debug Một Logic Phức Tạp

```
Module [TÊN MODULE] đang bị lỗi [MÔ TẢ LỖI].

Hãy:
1. Phân tích code hiện tại trong [FILE_PATH]
2. Xác định nguyên nhân lỗi
3. Đề xuất fix
4. Đề xuất test cases để tránh regression
```

### Tình Huống 3: Review Code Trước Khi Commit

```
Hãy review code của [MODULE] mà vừa implement.

Kiểm tra:
1. Tuân thủ best practices trong CLAUDE.md?
2. Có code duplication không?
3. Validation đầy đủ chưa?
4. Error handling đúng pattern chưa?
5. Logging đầy đủ chưa?
6. Có race condition tiềm ẩn không?
7. Có performance issue không?
8. Code clean & maintainable chưa?
```

### Tình Huống 4: Tìm Hiểu API Design

```
Hãy cho tôi biết chi tiết API design cho [MODULE] trong docs/api-design.md.

Bao gồm:
1. Tất cả endpoints (method, path, auth)
2. Request/Response format chi tiết
3. Query parameters
4. Error responses
5. Pagination (nếu có)
```

### Tình Huống 5: Cần Thêm Feature Mới Không Có Trong Docs

```
Tôi cần thêm feature [MÔ TẢ FEATURE] cho module [TÊN MODULE].

Hãy:
1. Phân tích xem feature này có conflict với design hiện tại không
2. Đề xuất cách implement hợp lý nhất (entities, services, APIs)
3. Đề xuất các thay đổi cần thiết (nếu có)
4. Tuân thủ patterns hiện có trong CLAUDE.md
```

---

## 📝 Checklist Trước Khi Bắt Đầu Module Mới

- [ ] Đã đọc phần tương ứng trong `docs/feature-list.md`
- [ ] Đã đọc phần tương ứng trong `docs/api-design.md`
- [ ] Đã kiểm tra entities liên quan đã được tạo chưa
- [ ] Đã kiểm tra repositories đã có những gì
- [ ] Đã hiểu rõ business rules và validation
- [ ] Đã xác định dependencies với modules khác
- [ ] Đã có plan implementation chi tiết
- [ ] Đã clear về best practices cần tuân thủ

---

## ⚠️ Lưu Ý Quan Trọng

### 1. Session-First Design Pattern (Module 5, 6, 8)
- **QUAN TRỌNG NHẤT:** Session là source of truth
- Mọi thay đổi lịch học phải qua Session
- StudentSession tự động sync khi Session thay đổi
- Đọc kỹ section này trong CLAUDE.md trước khi làm Module 5, 6, 8

### 2. Request Flows (Module 8)
- Module phức tạp nhất về business logic
- Nhiều luồng xử lý có transaction phức tạp
- Phải hiểu rõ từng loại request trước khi implement
- Implement từng loại một, test kỹ trước khi làm tiếp

### 3. Enum Management
- PostgreSQL enums đã được define trong `schema.sql`
- KHÔNG được modify enum sau khi đã tạo
- Nếu cần thêm value, phải dùng `ALTER TYPE ... ADD VALUE`

### 4. Composite Keys
- Nhiều entities dùng composite keys (StudentSession, TeacherSkill, etc.)
- Phải tạo ID class trong `entities/ids/`
- Implement `equals()` và `hashCode()`

---

## 📚 Tài Liệu Tham Khảo

- `CLAUDE.md`: Best practices, patterns, architecture
- `docs/feature-list.md`: Chi tiết requirements từng module
- `docs/api-design.md`: Spec API đầy đủ
- `docs/business-context.md`: Context nghiệp vụ tổng quan
- `README.md`: Setup instructions (tiếng Việt)

---

## 🤝 Khi Gặp Vấn Đề

Nếu Claude không hiểu rõ yêu cầu:
1. ✅ Cung cấp thêm context (file paths, requirements cụ thể)
2. ✅ Break down thành câu hỏi/yêu cầu nhỏ hơn
3. ✅ Tham chiếu cụ thể section trong docs
4. ✅ Đưa ví dụ về expected behavior

**Ví dụ tốt:**
```
Trong docs/feature-list.md phần 8.1.3 "Transfer Request",
có đề cập "map theo course_session_id".

Hãy giải thích chi tiết logic này hoạt động như thế nào,
kèm ví dụ cụ thể với 2 classes có course_id giống nhau.
```

---

**Version:** 1.0
**Last Updated:** 2025-10-18
**Maintainer:** EMS-SEP490 Team

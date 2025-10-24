# EMS MAIN FLOWS - CÁC LUỒNG NGHIỆP VỤ CHÍNH CỦA HỆ THỐNG

## Tổng quan
Tài liệu này mô tả toàn bộ các luồng nghiệp vụ chính (main flows) của hệ thống EMS (Education Management System). Mỗi flow mô tả chi tiết các bước tương tác giữa người dùng (actors) và hệ thống, cùng với cách hệ thống join dữ liệu từ các bảng database.

**Mục đích:**
- Giúp developer hiểu rõ nghiệp vụ trước khi implement
- Làm tài liệu tham khảo cho BA, QA, và Product Manager
- Đảm bảo tất cả stakeholders có cùng understanding về system behavior

**Ngôn ngữ:** Hoàn toàn bằng Tiếng Việt

---

## Cấu trúc Tài liệu

Tài liệu được chia thành 5 file theo vai trò người dùng:

### 1. [Teacher Flows](./mainflows/teacher-flows.md) - Luồng Nghiệp Vụ Giáo Viên
**Actors:** Teacher  
**Scope:** Giảng dạy, điểm danh, báo cáo, nhập điểm, quản lý lịch rảnh

**Các Flow Chính:**
- FLOW 1: Xem Lịch Dạy (Teacher Timetable View)
- FLOW 2: Điểm Danh Học Viên (Teacher Attendance Recording)
- FLOW 3: Báo Cáo Buổi Học (Teacher Session Report)
- FLOW 4: Nhập Điểm Đánh Giá (Teacher Score Entry)
- FLOW 5: Đăng Ký Lịch Rảnh & OT (Teacher Availability & OT Registration)
- FLOW 6: Gửi Yêu Cầu Nghỉ Học (Teacher Leave Request)
- FLOW 7: Xem Yêu Cầu OT Được Phân Công (Teacher OT Assignment)
- FLOW 8: Xem Feedback Từ Học Viên (Teacher View Student Feedback)
- FLOW 9: Xem Lịch Sử Thay Đổi Lịch Dạy (Teacher View Schedule Changes)

**Bảng Database Chính:**
- `teaching_slot` → `session` → `class` → `course`
- `student_session` → `student` → `user_account`
- `teacher_availability`, `teacher_availability_override`
- `teacher_request`
- `score` → `assessment`

---

### 2. [Student Flows](./mainflows/student-flows.md) - Luồng Nghiệp Vụ Học Viên
**Actors:** Student  
**Scope:** Xem lịch học, gửi request (nghỉ/học bù/chuyển lớp), xem điểm, đánh giá

**Các Flow Chính:**
- FLOW 1: Xem Lịch Học Cá Nhân (Student Personal Schedule View)
- FLOW 2: Gửi Yêu Cầu Báo Nghỉ (Student Absence Request)
- FLOW 3: Gửi Yêu Cầu Học Bù (Student Make-up Request) ⭐ COMPLEX
- FLOW 4: Gửi Yêu Cầu Chuyển Lớp (Student Transfer Request) ⭐ MOST COMPLEX
- FLOW 5: Xem Điểm Số & Kết Quả Học Tập (Student View Scores)
- FLOW 6: Đánh Giá Buổi Học (Student Feedback Submission)
- FLOW 7: Xem Lịch Sử Điểm Danh (Student View Attendance History)
- FLOW 8: Xem Tài Liệu Học Tập (Student View Course Materials)
- FLOW 9: Xem Thông Báo Thay Đổi Lịch (Student View Schedule Change Notifications)

**Bảng Database Chính:**
- `student_session` → `session` → `class` → `course`
- `student_request` (absence, makeup, transfer)
- `enrollment`
- `score` → `assessment`
- `student_feedback`
- `course_material`

---

### 3. [Academic Staff Flows](./mainflows/academic-flows.md) - Luồng Nghiệp Vụ Giáo Vụ
**Actors:** Academic Staff (Nhân viên Giáo vụ)  
**Scope:** Tạo lớp, phân công tài nguyên, ghi danh, xử lý requests

**Vai trò:** ⭐ **KEY OPERATIONAL ROLE** - Vận hành hàng ngày của hệ thống

**Các Flow Chính:**
- FLOW 1: Tạo Lớp Học Mới (Academic Staff Create Class) ⭐ CORE FLOW
- FLOW 2: Phân Công Giáo Viên Cho Buổi Học (Academic Staff Assign Teacher)
- FLOW 3: Phân Công Tài Nguyên (Phòng/Zoom) (Academic Staff Assign Resource)
- FLOW 4: Ghi Danh Học Viên Vào Lớp (Academic Staff Enroll Students)
- FLOW 5: Submit Lớp Để Duyệt (Academic Staff Submit Class for Approval)
- FLOW 6: Xử Lý Yêu Cầu Nghỉ Của Học Viên (Academic Staff Handle Student Absence Request)
- FLOW 7: Xử Lý Yêu Cầu Học Bù (Academic Staff Handle Student Makeup Request) ⭐ COMPLEX
- FLOW 8: Xử Lý Yêu Cầu Chuyển Lớp (Academic Staff Handle Student Transfer Request) ⭐ MOST COMPLEX
- FLOW 9: Xử Lý Yêu Cầu Nghỉ Của Giáo Viên (Academic Staff Handle Teacher Leave Request) ⭐ CRITICAL
- FLOW 10: Đổi Lịch Hàng Loạt Cho Lớp (Academic Staff Bulk Reschedule Class)

**Bảng Database Chính:**
- `class` → `course`
- `session` (auto-generated)
- `teaching_slot` ← `teacher`
- `session_resource` ← `resource`
- `enrollment` → `student_session`
- `student_request`, `teacher_request`

---

### 4. [Manager & Center Head Flows](./mainflows/manager-flows.md) - Luồng Nghiệp Vụ Quản Lý
**Actors:** Manager (Quản lý toàn hệ thống), Center Head (Quản lý chi nhánh)  
**Scope:** Duyệt course/class, dashboard, báo cáo, quản lý tổ chức

**Phân biệt vai trò:**
- **Manager**: Strategic oversight, duyệt course (curriculum), cross-branch coordination
- **Center Head**: Operational management, duyệt class, quản lý tài nguyên branch

**Các Flow Chính:**

#### Manager Flows:
- FLOW 1: Duyệt Course (Manager Approve Course) ⭐ STRATEGIC
- FLOW 2: Xem Executive Dashboard - System-Wide (Manager Dashboard)
- FLOW 3: Quản Lý Tổ Chức & Chi Nhánh (Manager Manage Organization)
- FLOW 4: Xem Báo Cáo Tổng Hợp (Manager View Reports)

#### Center Head Flows:
- FLOW 5: Duyệt Lớp (Center Head Approve Class) ⭐ OPERATIONAL
- FLOW 6: Xem Branch Dashboard (Center Head Dashboard)
- FLOW 7: Quản Lý Tài Nguyên (Center Head Manage Resources)
- FLOW 8: Approve Escalated Requests (Center Head Handle Complex Requests)

**Bảng Database Chính:**
- `course` (Manager approve)
- `class` (Center Head approve)
- `branch`, `center`, `resource`
- `time_slot_template`
- Dashboard queries: aggregate từ nhiều bảng

---

### 5. [System Flows](./mainflows/system-flows.md) - Luồng Tự Động Của Hệ Thống
**Actors:** System (Background workers, Scheduled jobs)  
**Scope:** Automation, data sync, validation, notification

**Các Flow Chính:**
- FLOW 1: Auto-Generate Sessions Từ Course Template ⭐ CORE AUTOMATION
- FLOW 2: Auto-Generate Student Schedule (student_session)
- FLOW 3: Auto-Sync Student Schedule Khi Session Thay Đổi
- FLOW 4: Conflict Detection (Teacher/Resource)
- FLOW 5: Attendance Lock (Tự Động Khóa Điểm Danh)
- FLOW 6: Auto-Notification System
- FLOW 7: Scheduled Reports (Báo Cáo Định Kỳ Tự Động)
- FLOW 8: Data Consistency Checker (Background Job)
- FLOW 9: Capacity Warning System
- FLOW 10: Automatic Class Status Transition

**Bảng Database Chính:**
- Tất cả bảng (tùy flow)
- `notification`
- `report_schedule`

---

## Sơ Đồ Tổng Quan Các Luồng Nghiệp Vụ

```
┌─────────────────────────────────────────────────────────────┐
│                    EMS MAIN FLOWS                            │
└─────────────────────────────────────────────────────────────┘
                              │
          ┌───────────────────┼───────────────────┐
          │                   │                   │
          ▼                   ▼                   ▼
┌──────────────────┐ ┌──────────────────┐ ┌──────────────────┐
│  CURRICULUM      │ │   OPERATIONS     │ │   ATTENDANCE     │
│  DESIGN          │ │   MANAGEMENT     │ │   & REPORTING    │
├──────────────────┤ ├──────────────────┤ ├──────────────────┤
│ Subject Leader   │ │ Academic Staff   │ │ Teacher          │
│ creates Course   │ │ creates Class    │ │ records          │
│ template         │ │ assigns Teacher/ │ │ attendance       │
│                  │ │ Resource         │ │                  │
│ Manager          │ │                  │ │ Academic Staff   │
│ approves Course  │ │ Center Head      │ │ processes        │
│                  │ │ approves Class   │ │ requests         │
└──────────────────┘ └──────────────────┘ └──────────────────┘
          │                   │                   │
          └───────────────────┼───────────────────┘
                              ▼
┌─────────────────────────────────────────────────────────────┐
│                    STUDENT JOURNEY                           │
├─────────────────────────────────────────────────────────────┤
│ 1. Academic Staff enrolls Student                            │
│ 2. System auto-generates student_session (personal schedule)│
│ 3. Student attends sessions                                  │
│ 4. Teacher marks attendance                                  │
│ 5. Student submits requests (absence/makeup/transfer)        │
│ 6. Academic Staff handles requests                           │
│ 7. Teacher enters scores                                     │
│ 8. Student views scores & provides feedback                  │
└─────────────────────────────────────────────────────────────┘
                              │
                              ▼
┌─────────────────────────────────────────────────────────────┐
│                  SYSTEM AUTOMATION                           │
├─────────────────────────────────────────────────────────────┤
│ • Auto-generate sessions from course template               │
│ • Auto-sync student_session when schedule changes           │
│ • Conflict detection (teacher/resource)                     │
│ • Attendance lock after T hours                             │
│ • Notification system (email/SMS/in-app)                    │
│ • Scheduled reports (daily/weekly/monthly)                  │
│ • Data consistency checker                                  │
└─────────────────────────────────────────────────────────────┘
```

---

## Quy Trình Nghiệp Vụ End-to-End

### Phase 1: Curriculum Design (Subject Leader → Manager)
1. Subject Leader tạo Subject, Level
2. Subject Leader tạo Course với Phases, Course Sessions, CLOs
3. Subject Leader submit Course để duyệt
4. **Manager review và approve Course** → Course sẵn sàng sử dụng

### Phase 2: Class Setup (Academic Staff → Center Head/Manager)
5. Academic Staff tạo Class mới (chọn Course, Branch, schedule)
6. **System tự động generate Sessions** từ Course template
7. Academic Staff phân công Teacher cho từng session
8. Academic Staff phân công Resource (phòng/Zoom) cho từng session
9. Academic Staff submit Class để duyệt
10. **Center Head/Manager approve Class** → Class sẵn sàng ghi danh

### Phase 3: Enrollment (Academic Staff → System)
11. Academic Staff ghi danh Students vào Class
12. **System tự động generate student_session** (lịch cá nhân)
13. Students nhận notification về lịch học

### Phase 4: Teaching & Attendance (Teacher → Students)
14. Teacher dạy session theo lịch
15. Teacher điểm danh học viên (cập nhật student_session.attendance_status)
16. Teacher báo cáo buổi học (cập nhật session.status = 'done')
17. **System lock attendance** sau T giờ

### Phase 5: Request Handling (Student/Teacher → Academic Staff)
18. Student/Teacher gửi request (absence/makeup/transfer/leave)
19. Academic Staff xử lý request:
    - Absence: Mark excused
    - Makeup: Tạo student_session mới cho buổi học bù
    - Transfer: Transaction phức tạp (update enrollment, sync student_session)
    - Leave: Tìm substitute hoặc reschedule session
20. **System gửi notification** cho các bên liên quan

### Phase 6: Assessment & Feedback (Teacher → Students)
21. Teacher tạo Assessment và nhập điểm
22. Students xem điểm và feedback từ Teacher
23. Students đánh giá buổi học (student_feedback)

### Phase 7: Reporting & Analytics (Manager/Center Head)
24. Manager/Center Head xem Dashboard
25. **System generate scheduled reports** (daily/weekly/monthly)
26. QA review feedback và tạo QA reports

---

## Các Bảng Database Quan Trọng Nhất

### Core Entities (Cốt lõi)
- **`course`**: Template khóa học (approved by Manager)
- **`class`**: Lớp học thực tế (approved by Center Head/Manager)
- **`session`**: Buổi học cụ thể (auto-generated từ course_session)
- **`student_session`**: Lịch học cá nhân của student (source of truth cho attendance)

### Assignments (Phân công)
- **`teaching_slot`**: Teacher được phân công dạy session nào
- **`session_resource`**: Session sử dụng resource (phòng/Zoom) nào
- **`enrollment`**: Student ghi danh vào class nào

### Requests (Yêu cầu thay đổi)
- **`student_request`**: Yêu cầu của student (absence/makeup/transfer)
- **`teacher_request`**: Yêu cầu của teacher (leave/ot/reschedule)

### Learning Outcomes & Assessment
- **`plo`**, **`clo`**: Learning outcomes
- **`assessment`**, **`score`**: Đánh giá và điểm số
- **`student_feedback`**: Phản hồi từ học viên

---

## Nguyên Tắc Thiết Kế Flow

### 1. Session-First Design Pattern
- Mọi hoạt động xoay quanh **`session`** (buổi học)
- `session` là đơn vị nhỏ nhất của lịch học
- `student_session` là attendance record của student cho session đó
- Khi session thay đổi → student_session tự động sync

### 2. Template-Instance Pattern
- **Template**: `course`, `course_session` (mẫu, tái sử dụng)
- **Instance**: `class`, `session` (thực tế, dựa trên template)
- Course approved một lần → dùng cho nhiều class
- System auto-generate instances từ templates

### 3. Request-Approval Workflow
- Mọi thay đổi quan trọng phải qua request/approval
- Request có 3 states: `pending` → `approved`/`rejected`
- Audit trail đầy đủ: submitted_by, decided_by, decided_at
- Không delete data, chỉ update status

### 4. No Deletion, Only Status Change
- Enrollment: `enrolled` → `transferred`/`dropped`/`completed`
- Session: `planned` → `done`/`cancelled`
- Student_session: không delete, chỉ change attendance_status
- Lý do: Preserve audit trail, enable historical reporting

### 5. Automation & Consistency
- System tự động sync dữ liệu (student_session khi session change)
- System kiểm tra conflict trước khi assign
- System lock data sau khi qua deadline (attendance lock)
- Background jobs đảm bảo data consistency

### 6. Multi-level Approval
- **Manager**: Approve course (strategic curriculum)
- **Center Head**: Approve class (operational, branch-level)
- **Academic Staff**: Handle day-to-day requests (absence, makeup, leave)

---

## Data Join Patterns Thường Gặp

### Pattern 1: Lấy Lịch Dạy Của Teacher
```
teacher → teaching_slot → session → class → course
```

### Pattern 2: Lấy Lịch Học Của Student
```
student → student_session → session → class → course → course_session
```

### Pattern 3: Lấy Danh Sách Điểm Danh
```
session → student_session → student → user_account
```

### Pattern 4: Tìm Teacher Khả Dụng
```
teacher → teacher_skill (match required_skills)
       → teacher_availability (check day_of_week)
       → teacher_availability_override (check specific date)
       → teaching_slot (check conflict)
```

### Pattern 5: Tìm Buổi Học Bù
```
session (target) → course_session_id
  → session (makeup candidates) WHERE same course_session_id
  → student_session (count enrolled) < class.max_capacity
```

### Pattern 6: Chuyển Lớp (Transfer)
```
enrollment (class A) → status = 'transferred'
enrollment (class B) → INSERT new
student_session (class A future) → attendance_status = 'excused'
student_session (class B future) → INSERT new (mapped by course_session_id)
```

---

## Metrics & KPIs Quan Trọng

### Enrollment Metrics
- **Fill Rate**: (enrolled count / max_capacity) × 100%
- **Enrollment Growth**: Month-over-month enrollment change
- **Drop Rate**: (dropped count / total enrolled) × 100%

### Attendance Metrics
- **Attendance Rate**: (present count / total sessions) × 100%
- **Absence Trend**: Tracking students with high absence count
- **Makeup Rate**: (makeup count / absence count) × 100%

### Teacher Metrics
- **Workload**: Total teaching hours per teacher
- **Leave Frequency**: Number of leave requests per month
- **OT Hours**: Overtime hours per teacher (for payroll)
- **Feedback Rating**: Average student feedback rating (1-5 stars)

### Operational Metrics
- **Resource Utilization**: (used hours / available hours) × 100%
- **Conflict Count**: Number of schedule conflicts detected
- **Request SLA**: Average time from submitted to decided
- **Class Completion Rate**: (completed classes / total classes) × 100%

### Quality Metrics
- **CLO Coverage**: % CLOs được map vào sessions
- **Assessment Completion**: % students có điểm đầy đủ
- **QA Score**: Average QA report score
- **Student Satisfaction**: Average student feedback rating

---

## Lưu Ý Quan Trọng Cho Developer

### 1. Luôn Dùng TRANSACTION Cho Flow Phức Tạp
- Enrollment (tạo enrollment + sinh student_session)
- Transfer (update enrollment A, create enrollment B, sync student_session)
- Reschedule (create new session, transfer links, cancel old)
- Leave handling (approve request + create OT request + update teaching_slot)

### 2. Validation Layers
- **Client-side**: Basic format validation
- **API-side**: Business logic validation (capacity, conflict, permissions)
- **Database-side**: Constraints (unique, foreign key, check)

### 3. Notification Best Practices
- Sử dụng queue (RabbitMQ/Kafka) để không block main flow
- Có retry mechanism cho failed notifications
- Log mọi notification đã gửi (audit trail)

### 4. Performance Considerations
- Index các cột thường được query (date, class_id, student_id, teacher_id)
- Sử dụng pagination cho list views
- Cache dashboard queries (Redis)
- Background job cho heavy reports

### 5. Security & Authorization
- Row-level security: User chỉ xem được data của branch mình
- Role-based access control (RBAC)
- Audit log mọi thao tác quan trọng (created_by, updated_by)

---

## Tài Liệu Liên Quan

- [Business Context](../business-context.md) - Ngữ cảnh nghiệp vụ tổng quan
- [Feature List](../feature-list.md) - Danh sách tính năng chi tiết
- [API Design](../api-design.md) - Thiết kế API endpoints
- [Database Schema](../schema.sql) - Cấu trúc database

---

## Lịch Sử Cập Nhật

| Ngày | Phiên bản | Nội dung |
|------|-----------|----------|
| 2025-10-24 | 1.0 | Phiên bản đầu tiên - Phân tích và mô tả toàn bộ main flows |

---

**Ghi chú:**  
Tài liệu này được viết hoàn toàn bằng Tiếng Việt để đảm bảo team Việt Nam hiểu rõ nghiệp vụ. Mỗi flow đều mô tả chi tiết cách join dữ liệu và các bước tương tác, giúp developer implement chính xác logic nghiệp vụ.

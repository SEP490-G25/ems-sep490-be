# MANAGER & CENTER HEAD MAIN FLOWS - CÁC LUỒNG NGHIỆP VỤ QUẢN LÝ

## Tổng quan
File này mô tả các luồng nghiệp vụ chính của Manager (quản lý toàn hệ thống/nhiều chi nhánh) và Center Head (quản lý một chi nhánh cụ thể).

**Phân biệt vai trò:**
- **Manager**: Quản lý chiến lược, oversight toàn hệ thống hoặc nhiều chi nhánh, duyệt course (curriculum), dashboard tổng hợp
- **Center Head**: Quản lý vận hành trực tiếp MỘT chi nhánh, duyệt class, quản lý tài nguyên branch, dashboard chi nhánh

---

## MANAGER FLOWS

### FLOW 1: Duyệt Course (Manager Approve Course) ⭐ STRATEGIC

**Actors involved:** Subject Leader, Manager, System  
**Description:** Manager duyệt course template do Subject Leader tạo. Course chỉ được sử dụng sau khi Manager approve.

**Database Tables Involved:**
- `course` (cập nhật approved_by_manager, approved_at)
- `subject`, `level`, `course_phase`, `course_session`

**Flow Steps:**

1. **Subject Leader submit course để duyệt** (xem curriculum design flow)
   - Course status = 'pending_approval'

2. **Manager vào menu "Course cần duyệt"**
   - System query:
   ```
   SELECT 
     c.id,
     c.code,
     c.name,
     s.name AS subject_name,
     l.name AS level_name,
     c.total_hours,
     c.duration_weeks,
     c.session_per_week,
     c.created_by,
     c.submitted_at
   FROM course c
   JOIN subject s ON c.subject_id = s.id
   JOIN level l ON c.level_id = l.id
   WHERE c.status = 'pending_approval'
   ORDER BY c.submitted_at
   ```

3. **Manager click vào course để review chi tiết**
   - System load:
     - Thông tin course (description, prerequisites, teaching_methods)
     - Danh sách phases:
       ```
       SELECT id, phase_number, name, duration_weeks, learning_focus
       FROM course_phase
       WHERE course_id = :course_id
       ORDER BY phase_number
       ```
     - Danh sách course_sessions:
       ```
       SELECT 
         cs.id,
         cp.phase_number,
         cs.sequence_no,
         cs.topic,
         cs.student_task,
         cs.skill_set
       FROM course_session cs
       JOIN course_phase cp ON cs.phase_id = cp.id
       WHERE cp.course_id = :course_id
       ORDER BY cp.phase_number, cs.sequence_no
       ```
     - CLO mapping:
       ```
       SELECT 
         clo.code,
         clo.description,
         COUNT(cscm.course_session_id) AS session_count
       FROM clo
       LEFT JOIN course_session_clo_mapping cscm ON clo.id = cscm.clo_id
       WHERE clo.course_id = :course_id
       GROUP BY clo.id
       ```

4. **Manager review và validate**
   - Kiểm tra:
     - Course structure hoàn chỉnh (phases, sessions)
     - Số buổi = duration_weeks × session_per_week
     - Tất cả CLO đều được map vào sessions (không có CLO nào bị thiếu)
     - Syllabus hợp lý

5. **Manager quyết định**

### Option A: Approve
   ```
   UPDATE course
   SET 
     status = 'approved',
     approved_by_manager = :manager_user_id,
     approved_at = NOW()
   WHERE id = :course_id
   ```

### Option B: Reject
   ```
   UPDATE course
   SET 
     status = 'draft',
     rejection_reason = :reason
   WHERE id = :course_id
   ```

6. **System gửi notification tới Subject Leader**
   - "Course [Tên] đã được [approved/rejected]"

**Result:** 
- Course approved có thể được sử dụng để tạo lớp trên toàn hệ thống
- Đảm bảo chất lượng curriculum trước khi triển khai

---

### FLOW 2: Xem Executive Dashboard - System-Wide (Manager Dashboard)

**Actors involved:** Manager, System  
**Description:** Manager xem dashboard tổng hợp toàn hệ thống hoặc nhiều chi nhánh.

**Database Tables Involved:**
- Tất cả bảng chính (class, enrollment, session, student_session, teacher, v.v.)

**Flow Steps:**

1. **Manager login và vào Dashboard**

2. **System load metrics tổng hợp**

#### 2A. Overview Metrics (Tổng quan toàn hệ thống)
   ```
   -- Tổng số chi nhánh
   SELECT COUNT(*) AS total_branches FROM branch WHERE status = 'active';
   
   -- Tổng số lớp đang chạy
   SELECT COUNT(*) AS ongoing_classes 
   FROM class 
   WHERE status IN ('scheduled', 'ongoing');
   
   -- Tổng số học viên
   SELECT COUNT(*) AS total_students 
   FROM student 
   WHERE status = 'active';
   
   -- Tổng số giáo viên
   SELECT COUNT(*) AS total_teachers 
   FROM teacher 
   WHERE status = 'active';
   ```

#### 2B. Enrollment Metrics (Cross-branch comparison)
   ```
   SELECT 
     b.name AS branch_name,
     COUNT(DISTINCT c.id) AS class_count,
     COUNT(DISTINCT e.student_id) AS student_count,
     AVG(
       (SELECT COUNT(*) FROM enrollment e2 
        WHERE e2.class_id = c.id AND e2.status = 'enrolled')::float / 
       NULLIF(c.max_capacity, 0) * 100
     ) AS avg_fill_rate
   FROM branch b
   LEFT JOIN class c ON c.branch_id = b.id
   LEFT JOIN enrollment e ON (e.class_id = c.id AND e.status = 'enrolled')
   WHERE b.status = 'active'
   GROUP BY b.id
   ORDER BY student_count DESC
   ```

#### 2C. Attendance Rate (System-wide)
   ```
   SELECT 
     AVG(
       CASE 
         WHEN ss.attendance_status IN ('present', 'late', 'remote') THEN 1.0
         ELSE 0.0
       END
     ) * 100 AS system_attendance_rate
   FROM student_session ss
   JOIN session s ON ss.session_id = s.id
   WHERE s.status = 'done'
     AND s.date >= CURRENT_DATE - INTERVAL '30 days'
   ```

#### 2D. Teacher Workload (Distribution across system)
   ```
   SELECT 
     t.id,
     u.full_name,
     b.name AS branch_name,
     COUNT(DISTINCT ts.session_id) AS session_count,
     SUM(
       EXTRACT(EPOCH FROM (s.end_time - s.start_time)) / 3600
     ) AS total_hours
   FROM teacher t
   JOIN user_account u ON t.user_account_id = u.id
   LEFT JOIN user_branches ub ON u.id = ub.user_account_id
   LEFT JOIN branch b ON ub.branch_id = b.id
   LEFT JOIN teaching_slot ts ON t.id = ts.teacher_id
   LEFT JOIN session s ON (ts.session_id = s.id AND s.status IN ('planned', 'done'))
   WHERE s.date >= CURRENT_DATE - INTERVAL '30 days'
   GROUP BY t.id, u.id, b.id
   ORDER BY total_hours DESC
   LIMIT 20
   ```

#### 2E. Alerts & Critical Issues (System-wide)
   ```
   -- Classes với fill rate thấp
   SELECT c.id, c.name, b.name AS branch_name,
     (SELECT COUNT(*) FROM enrollment WHERE class_id = c.id)::float / c.max_capacity * 100 AS fill_rate
   FROM class c
   JOIN branch b ON c.branch_id = b.id
   WHERE c.status = 'scheduled'
   HAVING fill_rate < 50
   ORDER BY fill_rate;
   
   -- Teachers vượt ngưỡng nghỉ
   SELECT t.id, u.full_name,
     COUNT(*) AS leave_count
   FROM teacher_request tr
   JOIN teacher t ON tr.teacher_id = t.id
   JOIN user_account u ON t.user_account_id = u.id
   WHERE tr.request_type = 'leave'
     AND tr.submitted_at >= CURRENT_DATE - INTERVAL '30 days'
   GROUP BY t.id, u.id
   HAVING COUNT(*) > 3
   ORDER BY leave_count DESC;
   
   -- Students vượt ngưỡng vắng
   SELECT s.id, u.full_name, c.name AS class_name,
     COUNT(*) AS absence_count
   FROM student_session ss
   JOIN student s ON ss.student_id = s.id
   JOIN user_account u ON s.user_account_id = u.id
   JOIN session sess ON ss.session_id = sess.id
   JOIN class c ON sess.class_id = c.id
   WHERE ss.attendance_status = 'absent'
     AND sess.date >= CURRENT_DATE - INTERVAL '30 days'
   GROUP BY s.id, u.id, c.id
   HAVING COUNT(*) >= 4
   ORDER BY absence_count DESC;
   ```

3. **Manager có thể drill-down**
   - Click vào branch cụ thể → xem chi tiết branch
   - Click vào alert → xem chi tiết vấn đề

**Result:** 
- Manager có cái nhìn tổng quan toàn hệ thống
- Phát hiện sớm vấn đề để can thiệp

---

### FLOW 3: Quản Lý Tổ Chức & Chi Nhánh (Manager Manage Organization)

**Actors involved:** Manager, System  
**Description:** Manager tạo/quản lý center, branch, time_slot_template.

**Database Tables Involved:**
- `center`, `branch`, `time_slot_template`

**Flow Steps:**

1. **Manager vào "Quản lý tổ chức"**

#### 3A. Tạo Branch Mới
   ```
   INSERT INTO branch (
     center_id,
     code,
     name,
     address,
     capacity,
     opening_date,
     status
   ) VALUES (
     :center_id,
     :code,
     :name,
     :address,
     :capacity,
     :opening_date,
     'active'
   )
   ```

#### 3B. Tạo Time Slot Template Cho Branch
   ```
   INSERT INTO time_slot_template (
     branch_id,
     name,
     start_time,
     end_time,
     duration_min
   ) VALUES (
     :branch_id,
     'Morning 1',
     '08:00',
     '10:30',
     150
   )
   ```

#### 3C. Phân Quyền User Vào Branch
   ```
   INSERT INTO user_branches (user_account_id, branch_id)
   VALUES (:user_id, :branch_id)
   ```

**Result:** 
- Cấu trúc tổ chức được setup
- Users được phân quyền theo branch

---

### FLOW 4: Xem Báo Cáo Tổng Hợp (Manager View Reports)

**Actors involved:** Manager, System  
**Description:** Manager xem các báo cáo định kỳ (tuần/tháng/quý).

**Database Tables Involved:**
- Nhiều bảng, tùy loại báo cáo

**Flow Steps:**

1. **Manager chọn loại báo cáo**
   - Enrollment Report
   - Attendance Report
   - Teacher Workload Report
   - Financial Report (nếu có module payment)
   - QA Report

2. **Manager chọn filter**
   - Time range (tuần này, tháng này, tháng trước, custom)
   - Branch (tất cả, hoặc chọn specific)
   - Course/Level (tất cả, hoặc specific)

3. **System generate báo cáo**
   - Thực hiện các query phức tạp (tùy loại báo cáo)
   - Aggregate dữ liệu
   - Generate charts (nếu có)

4. **Manager xem và export**
   - Xem trên màn hình
   - Export CSV/Excel/PDF

**Result:** 
- Manager có dữ liệu để ra quyết định chiến lược

---

## CENTER HEAD FLOWS

### FLOW 5: Duyệt Lớp (Center Head Approve Class) ⭐ OPERATIONAL

**Actors involved:** Academic Staff, Center Head, System  
**Description:** Center Head duyệt lớp do Academic Staff tạo (thuộc branch của họ).

**Database Tables Involved:**
- `class` (cập nhật approved_by, approved_at)

**Flow Steps:**

1. **Academic Staff submit class** (status='pending_approval')

2. **Center Head vào "Lớp cần duyệt"**
   - System query:
   ```
   SELECT 
     c.id,
     c.code,
     c.name,
     co.name AS course_name,
     c.start_date,
     c.max_capacity,
     c.submitted_at,
     ua.full_name AS created_by_name
   FROM class c
   JOIN course co ON c.course_id = co.id
   JOIN user_account ua ON c.created_by = ua.id
   WHERE c.branch_id IN (
     SELECT branch_id 
     FROM user_branches 
     WHERE user_account_id = :center_head_user_id
   )
   AND c.status = 'pending_approval'
   ORDER BY c.submitted_at
   ```

3. **Center Head click vào class để review**
   - System load:
     - Thông tin class
     - Danh sách sessions:
       ```
       SELECT s.id, s.date, s.start_time, s.end_time, cs.topic
       FROM session s
       LEFT JOIN course_session cs ON s.course_session_id = cs.id
       WHERE s.class_id = :class_id
       ORDER BY s.date
       LIMIT 10
       ```
     - Teacher assignments:
       ```
       SELECT DISTINCT
         u.full_name AS teacher_name,
         COUNT(ts.session_id) AS session_count
       FROM teaching_slot ts
       JOIN teacher t ON ts.teacher_id = t.id
       JOIN user_account u ON t.user_account_id = u.id
       JOIN session s ON ts.session_id = s.id
       WHERE s.class_id = :class_id
       GROUP BY t.id, u.id
       ```
     - Resource assignments:
       ```
       SELECT DISTINCT r.name, r.resource_type, COUNT(sr.session_id)
       FROM session_resource sr
       JOIN resource r ON sr.resource_id = r.id
       JOIN session s ON sr.session_id = s.id
       WHERE s.class_id = :class_id
       GROUP BY r.id
       ```

4. **Center Head validate**
   - Kiểm tra:
     - Tất cả sessions có teacher
     - Tất cả sessions có resource
     - Không có conflict
     - Lịch học hợp lý

5. **Center Head quyết định**

### Option A: Approve
   ```
   UPDATE class
   SET 
     status = 'scheduled',
     approved_by = :center_head_user_id,
     approved_at = NOW()
   WHERE id = :class_id
   ```

### Option B: Reject
   ```
   UPDATE class
   SET 
     status = 'draft',
     rejection_reason = :reason
   WHERE id = :class_id
   ```

6. **System gửi notification tới Academic Staff**
   - "Lớp [Tên] đã được [approved/rejected]"

**Result:** 
- Class approved → có thể ghi danh học viên
- Đảm bảo chất lượng lớp trước khi chạy

---

### FLOW 6: Xem Branch Dashboard (Center Head Dashboard)

**Actors involved:** Center Head, System  
**Description:** Center Head xem dashboard của chi nhánh mình quản lý.

**Database Tables Involved:**
- Các bảng liên quan đến branch

**Flow Steps:**

1. **Center Head login và vào Dashboard**

2. **System load metrics của branch**

#### 2A. Branch Overview
   ```
   -- Tổng số lớp đang chạy tại branch
   SELECT COUNT(*) 
   FROM class 
   WHERE branch_id = :branch_id 
     AND status IN ('scheduled', 'ongoing');
   
   -- Tổng số học viên tại branch
   SELECT COUNT(DISTINCT e.student_id)
   FROM enrollment e
   JOIN class c ON e.class_id = c.id
   WHERE c.branch_id = :branch_id
     AND e.status = 'enrolled';
   
   -- Fill rate trung bình
   SELECT AVG(
     (SELECT COUNT(*) FROM enrollment e 
      WHERE e.class_id = c.id AND e.status = 'enrolled')::float / 
     NULLIF(c.max_capacity, 0) * 100
   ) AS avg_fill_rate
   FROM class c
   WHERE c.branch_id = :branch_id
     AND c.status IN ('scheduled', 'ongoing');
   ```

#### 2B. Today's Operations (Vận hành hôm nay)
   ```
   -- Lớp cần điểm danh hôm nay
   SELECT 
     c.name AS class_name,
     s.start_time,
     s.end_time,
     u.full_name AS teacher_name,
     CASE 
       WHEN s.status = 'done' THEN 'Đã báo cáo'
       ELSE 'Chưa báo cáo'
     END AS status
   FROM session s
   JOIN class c ON s.class_id = c.id
   LEFT JOIN teaching_slot ts ON s.id = ts.session_id
   LEFT JOIN teacher t ON ts.teacher_id = t.id
   LEFT JOIN user_account u ON t.user_account_id = u.id
   WHERE c.branch_id = :branch_id
     AND s.date = CURRENT_DATE
   ORDER BY s.start_time;
   
   -- Conflicts cần giải quyết
   -- (resource conflicts, teacher conflicts)
   ```

#### 2C. Attendance Rate (Branch-specific)
   ```
   SELECT 
     c.name AS class_name,
     AVG(
       CASE 
         WHEN ss.attendance_status IN ('present', 'late', 'remote') THEN 1.0
         ELSE 0.0
       END
     ) * 100 AS attendance_rate
   FROM student_session ss
   JOIN session s ON ss.session_id = s.id
   JOIN class c ON s.class_id = c.id
   WHERE c.branch_id = :branch_id
     AND s.status = 'done'
     AND s.date >= CURRENT_DATE - INTERVAL '30 days'
   GROUP BY c.id
   ORDER BY attendance_rate ASC
   ```

#### 2D. Pending Requests (Yêu cầu chờ xử lý)
   ```
   -- Student requests
   SELECT COUNT(*) 
   FROM student_request sr
   JOIN student s ON sr.student_id = s.id
   WHERE s.branch_id = :branch_id
     AND sr.status = 'pending';
   
   -- Teacher requests
   SELECT COUNT(*)
   FROM teacher_request tr
   JOIN teacher t ON tr.teacher_id = t.id
   JOIN user_branches ub ON t.user_account_id = ub.user_account_id
   WHERE ub.branch_id = :branch_id
     AND tr.status = 'pending';
   ```

3. **Center Head có thể drill-down**
   - Click vào lớp cụ thể → xem chi tiết
   - Click vào alert → xử lý ngay

**Result:** 
- Center Head nắm rõ vận hành hàng ngày của branch
- Xử lý kịp thời các vấn đề phát sinh

---

### FLOW 7: Quản Lý Tài Nguyên (Center Head Manage Resources)

**Actors involved:** Center Head, System  
**Description:** Center Head quản lý phòng học và Zoom accounts của branch.

**Database Tables Involved:**
- `resource`

**Flow Steps:**

1. **Center Head vào "Quản lý tài nguyên"**

#### 7A. Tạo Resource Mới (Phòng Học)
   ```
   INSERT INTO resource (
     branch_id,
     name,
     resource_type,
     capacity,
     location,
     equipment,
     status
   ) VALUES (
     :branch_id,
     'Room 101',
     'ROOM',
     25,
     'Tầng 1',
     'Projector, AC, Whiteboard',
     'active'
   )
   ```

#### 7B. Tạo Resource Mới (Zoom Account)
   ```
   INSERT INTO resource (
     branch_id,
     name,
     resource_type,
     meeting_url,
     meeting_id,
     account_email,
     license_type,
     expiry_date,
     status
   ) VALUES (
     :branch_id,
     'Zoom Account 1',
     'VIRTUAL',
     'https://zoom.us/j/123456789',
     '123456789',
     'zoom1@center.edu.vn',
     'Pro',
     '2025-12-31',
     'active'
   )
   ```

#### 7C. Xem Utilization (Mức độ sử dụng)
   ```
   SELECT 
     r.name,
     r.resource_type,
     COUNT(sr.session_id) AS session_count,
     SUM(
       EXTRACT(EPOCH FROM (s.end_time - s.start_time)) / 3600
     ) AS total_hours_used
   FROM resource r
   LEFT JOIN session_resource sr ON r.id = sr.resource_id
   LEFT JOIN session s ON (sr.session_id = s.id AND s.status IN ('planned', 'done'))
   WHERE r.branch_id = :branch_id
     AND s.date >= CURRENT_DATE - INTERVAL '30 days'
   GROUP BY r.id
   ORDER BY total_hours_used DESC
   ```

#### 7D. Deactivate Resource (Tạm ngưng sử dụng)
   ```
   UPDATE resource
   SET status = 'inactive', note = 'AC bị hỏng, cần sửa chữa'
   WHERE id = :resource_id
   ```

**Result:** 
- Resources được quản lý chặt chẽ
- Biết mức độ sử dụng để tối ưu

---

### FLOW 8: Approve Escalated Requests (Center Head Handle Complex Requests)

**Actors involved:** Center Head, System  
**Description:** Center Head duyệt các request phức tạp mà Academic Staff escalate lên.

**Database Tables Involved:**
- `student_request`, `teacher_request`

**Flow Steps:**

1. **Academic Staff gặp case phức tạp** (vd: transfer có gap, capacity vượt quá nhiều)
   - Escalate lên Center Head

2. **Center Head vào "Yêu cầu cần duyệt"**
   - System query các request được escalate

3. **Center Head review và quyết định**
   - Có thể override policy (với lý do)
   - Approve với điều kiện đặc biệt

**Result:** 
- Các case đặc biệt được xử lý linh hoạt
- Giữ được balance giữa policy và thực tế

---

## Tóm Tắt Phân Biệt Manager vs Center Head

| Tiêu Chí | Manager | Center Head |
|----------|---------|-------------|
| **Phạm vi** | Toàn hệ thống / Nhiều chi nhánh | MỘT chi nhánh cụ thể |
| **Focus** | Strategic oversight, policy, cross-branch coordination | Operational execution, daily branch management |
| **Approve Course** | ✅ Yes (strategic curriculum decision) | ❌ No |
| **Approve Class** | ✅ Yes (cross-branch classes) | ✅ Yes (classes in their branch) |
| **Dashboard** | System-wide, cross-branch comparison | Branch-specific, daily operations |
| **Manage Resources** | Define policies, allocate budget | Direct management (rooms, Zoom) |
| **Handle Requests** | Escalated complex cases | Day-to-day requests in branch |
| **Reports** | Executive reports, strategic KPIs | Operational reports, branch KPIs |

---

## Tóm Tắt Flow Chính

| Flow | Actor | Mô Tả | Bảng Chính |
|------|-------|-------|-----------|
| 1. Duyệt Course | Manager | Approve course template | course |
| 2. Executive Dashboard | Manager | Xem metrics toàn hệ thống | All tables |
| 3. Quản Lý Tổ Chức | Manager | Tạo branch, time_slot_template | branch, time_slot_template |
| 4. Xem Báo Cáo | Manager | Reports tổng hợp | Multiple tables |
| 5. Duyệt Class | Center Head | Approve class của branch | class |
| 6. Branch Dashboard | Center Head | Metrics của branch | Branch-specific data |
| 7. Quản Lý Tài Nguyên | Center Head | Tạo/quản lý phòng/Zoom | resource |
| 8. Handle Escalated Requests | Center Head | Duyệt request phức tạp | student_request, teacher_request |

---

**Lưu Ý Quan Trọng:**
- Manager và Center Head đều có quyền cao, nhưng phạm vi khác nhau
- Manager focus vào strategic decisions và cross-branch coordination
- Center Head focus vào operational excellence của branch cụ thể
- Cả hai đều cần dashboard real-time để monitoring và decision-making
- Approval workflow đảm bảo quality gate trước khi deploy

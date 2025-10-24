# ACADEMIC STAFF MAIN FLOWS - CÁC LUỒNG NGHIỆP VỤ CHÍNH CỦA GIÁO VỤ

## Tổng quan
File này mô tả các luồng nghiệp vụ chính mà Nhân viên Giáo vụ (Academic Staff) thực hiện trong hệ thống EMS. Academic Staff là vai trò vận hành cốt lõi, chịu trách nhiệm tạo lớp, phân công tài nguyên, ghi danh học viên, và xử lý các request thay đổi lịch.

---

## FLOW 1: Tạo Lớp Học Mới (Academic Staff Create Class) ⭐ CORE FLOW

**Actors involved:** Academic Staff, System  
**Description:** Giáo vụ tạo lớp học mới dựa trên course template đã được Manager phê duyệt.

**Database Tables Involved:**
- `class` (tạo record mới)
- `course` (chọn course template)
- `branch` (chọn chi nhánh)
- `time_slot_template` (chọn khung giờ học)

**Flow Steps:**

1. **Academic Staff vào menu "Tạo lớp mới"**

2. **Academic Staff điền thông tin lớp**
   - **Chọn Branch**: Chi nhánh lớp học sẽ thuộc về
   - **Chọn Course**: Danh sách course đã được approved_by_manager
     ```
     SELECT id, code, name, total_hours, duration_weeks, session_per_week
     FROM course
     WHERE status = 'approved'
     ORDER BY created_at DESC
     ```
   
   - **Điền thông tin cơ bản**:
     - Code: Mã lớp (unique trong branch, vd: "ENG-A1-M01")
     - Name: Tên lớp (vd: "English A1 - Morning Class 01")
     - Modality: OFFLINE / ONLINE / HYBRID
     - Max capacity: Số học viên tối đa (vd: 25)
   
   - **Chọn lịch học**:
     - Start date: Ngày bắt đầu học (vd: 2025-03-01)
     - Schedule days: Các ngày trong tuần sẽ học (vd: [1, 3, 5] = Thứ 2, 4, 6)
   
   - **Map schedule days với time slot**:
     - Với mỗi schedule day, chọn time_slot_template
     - Ví dụ:
       - Thứ 2: "Morning 1 (08:00-10:30)"
       - Thứ 4: "Morning 1 (08:00-10:30)"
       - Thứ 6: "Morning 1 (08:00-10:30)"

3. **Academic Staff click "Tạo lớp"**
   - System thực hiện INSERT:
   ```
   INSERT INTO class (
     branch_id, 
     course_id, 
     code, 
     name, 
     modality, 
     start_date, 
     schedule_days, 
     max_capacity,
     status,
     created_by
   ) VALUES (
     :branch_id,
     :course_id,
     :code,
     :name,
     :modality,
     :start_date,
     :schedule_days,
     :max_capacity,
     'draft',
     :academic_staff_user_id
   )
   RETURNING id
   ```

4. **System validation**
   - Kiểm tra unique constraint: (branch_id, code)
   - Kiểm tra course đã được approved
   - Kiểm tra schedule_days hợp lệ (0-6)
   - Kiểm tra mỗi schedule_day có time_slot_template

5. **System tự động sinh sessions** (xem FLOW riêng: System Auto-Generate Sessions)
   - Đọc tất cả course_session từ course
   - Tính toán ngày giờ dựa trên start_date, schedule_days, time_slot_template
   - Insert vào bảng `session` với status='planned'

**Result:** 
- Class mới được tạo với status='draft'
- Tất cả sessions được sinh tự động
- Sẵn sàng để phân công teacher và resource

---

## FLOW 2: Phân Công Giáo Viên Cho Buổi Học (Academic Staff Assign Teacher)

**Actors involved:** Academic Staff, System  
**Description:** Sau khi tạo lớp và sinh sessions, Giáo vụ phân công giáo viên cho từng buổi học.

**Database Tables Involved:**
- `teaching_slot` (tạo assignment)
- `session` → `course_session` (để biết skill cần thiết)
- `teacher` → `teacher_skill` (tìm teacher phù hợp)
- `teacher_availability` + `teacher_availability_override` (kiểm tra rảnh)

**Flow Steps:**

1. **Academic Staff vào chi tiết lớp**
   - Click "Phân công giáo viên"

2. **System hiển thị danh sách sessions chưa có giáo viên**
   - Query:
   ```
   SELECT 
     s.id,
     s.date,
     s.start_time,
     s.end_time,
     cs.topic,
     cs.skill_set
   FROM session s
   LEFT JOIN course_session cs ON s.course_session_id = cs.id
   WHERE s.class_id = :class_id
     AND s.status = 'planned'
     AND NOT EXISTS (
       SELECT 1 FROM teaching_slot ts 
       WHERE ts.session_id = s.id
     )
   ORDER BY s.date
   ```

3. **Academic Staff chọn một hoặc nhiều sessions**
   - Có thể chọn hàng loạt (vd: tất cả sessions của tháng 3)

4. **System gợi ý giáo viên phù hợp**
   - Gọi function: `find_available_teachers(session_ids, required_skills)`
   - Logic:
   ```
   SELECT DISTINCT
     t.id AS teacher_id,
     u.full_name,
     ts.skill,
     ts.level,
     -- Kiểm tra availability
     CASE 
       WHEN tao.is_available IS NOT NULL THEN tao.is_available
       WHEN ta.id IS NOT NULL THEN TRUE
       ELSE FALSE
     END AS is_available,
     -- Kiểm tra conflict
     (
       SELECT COUNT(*) 
       FROM teaching_slot tslot
       JOIN session s2 ON tslot.session_id = s2.id
       WHERE tslot.teacher_id = t.id
         AND s2.date = :session_date
         AND (
           (s2.start_time, s2.end_time) OVERLAPS (:start_time, :end_time)
         )
     ) AS conflict_count
   FROM teacher t
   JOIN user_account u ON t.user_account_id = u.id
   JOIN teacher_skill ts ON t.id = ts.teacher_id
   LEFT JOIN teacher_availability ta ON (
     ta.teacher_id = t.id 
     AND ta.day_of_week = EXTRACT(DOW FROM :session_date)
     AND (ta.start_time, ta.end_time) OVERLAPS (:start_time, :end_time)
   )
   LEFT JOIN teacher_availability_override tao ON (
     tao.teacher_id = t.id
     AND tao.date = :session_date
     AND (tao.start_time, tao.end_time) OVERLAPS (:start_time, :end_time)
   )
   WHERE ts.skill = ANY(:required_skills)
   HAVING (
     CASE 
       WHEN tao.is_available IS NOT NULL THEN tao.is_available
       WHEN ta.id IS NOT NULL THEN TRUE
       ELSE FALSE
     END
   ) = TRUE
   AND conflict_count = 0
   ORDER BY ts.level DESC, u.full_name
   ```

5. **System hiển thị danh sách giáo viên phù hợp**
   - Mỗi giáo viên hiển thị:
     - Tên
     - Kỹ năng (skill + level)
     - Trạng thái rảnh (available/busy)
     - Số lớp đang dạy (workload)
     - Badge "OT" nếu đã đăng ký OT

6. **Academic Staff chọn giáo viên và click "Phân công"**
   - System thực hiện INSERT:
   ```
   INSERT INTO teaching_slot (
     session_id,
     teacher_id,
     skill,
     role
   ) VALUES (
     :session_id,
     :teacher_id,
     :skill,
     'primary'
   )
   ```

7. **System validation**
   - Kiểm tra lại conflict (double-check)
   - Kiểm tra teacher skill match với session requirement

8. **System gửi notification tới Teacher**
   - "Bạn được phân công dạy lớp [Tên] từ ngày [...]"

**Result:** 
- Teaching_slot được tạo
- Teacher thấy session trong lịch dạy của mình

---

## FLOW 3: Phân Công Tài Nguyên (Phòng/Zoom) (Academic Staff Assign Resource)

**Actors involved:** Academic Staff, System  
**Description:** Phân công phòng học (offline) hoặc Zoom (online) cho các buổi học.

**Database Tables Involved:**
- `session_resource` (tạo assignment)
- `resource` (danh sách phòng/Zoom của branch)
- `session` (để biết ngày giờ và modality)

**Flow Steps:**

1. **Academic Staff vào chi tiết lớp**
   - Click "Phân công phòng/Zoom"

2. **System hiển thị danh sách sessions chưa có resource**
   - Query tương tự như phân công teacher

3. **Academic Staff chọn sessions**

4. **System gợi ý resource khả dụng**
   - Gọi function: `find_available_resources(branch_id, session_date, start_time, end_time, modality)`
   - Logic:
   ```
   SELECT 
     r.id,
     r.name,
     r.resource_type,
     r.capacity,
     -- Kiểm tra conflict
     (
       SELECT COUNT(*) 
       FROM session_resource sr
       JOIN session s ON sr.session_id = s.id
       WHERE sr.resource_id = r.id
         AND s.date = :session_date
         AND (s.start_time, s.end_time) OVERLAPS (:start_time, :end_time)
         AND s.status != 'cancelled'
     ) AS conflict_count
   FROM resource r
   WHERE r.branch_id = :branch_id
     AND r.status = 'active'
     AND (
       (:modality = 'OFFLINE' AND r.resource_type = 'ROOM')
       OR (:modality = 'ONLINE' AND r.resource_type = 'VIRTUAL')
       OR (:modality = 'HYBRID' AND r.resource_type IN ('ROOM', 'VIRTUAL'))
     )
   HAVING conflict_count = 0
   ORDER BY r.capacity DESC
   ```

5. **System hiển thị danh sách resource**
   - Mỗi resource hiển thị:
     - Tên (vd: "Room 101" hoặc "Zoom Account 1")
     - Loại (ROOM / VIRTUAL)
     - Sức chứa
     - Trạng thái (available / busy)

6. **Academic Staff chọn resource và click "Phân công"**
   - System INSERT:
   ```
   INSERT INTO session_resource (
     session_id,
     resource_type,
     resource_id,
     capacity_override
   ) VALUES (
     :session_id,
     :resource_type,
     :resource_id,
     NULL  -- hoặc override nếu cần
   )
   ```

7. **System validation**
   - Kiểm tra conflict
   - Nếu HYBRID: cần cả ROOM và VIRTUAL

**Result:** 
- Session có resource
- Student và Teacher biết địa điểm/link Zoom

---

## FLOW 4: Ghi Danh Học Viên Vào Lớp (Academic Staff Enroll Students)

**Actors involved:** Academic Staff, System  
**Description:** Sau khi lớp được approve, Giáo vụ ghi danh học viên.

**Database Tables Involved:**
- `enrollment` (tạo record ghi danh)
- `student_session` (tự động sinh lịch cá nhân cho student)
- `student` (danh sách học viên)
- `class` (kiểm tra capacity)

**Flow Steps:**

1. **Academic Staff vào chi tiết lớp**
   - Click "Ghi danh học viên"

2. **System kiểm tra class status**
   - Chỉ cho phép ghi danh nếu class.status = 'scheduled' hoặc 'ongoing'

3. **Academic Staff có 2 options:**

### Option A: Ghi Danh Học Viên Hiện Có

4a. **System hiển thị danh sách học viên khả dụng**
   - Query:
   ```
   SELECT 
     s.id,
     s.student_code,
     u.full_name,
     u.email,
     u.phone
   FROM student s
   JOIN user_account u ON s.user_account_id = u.id
   WHERE s.branch_id = :branch_id
     AND NOT EXISTS (
       SELECT 1 FROM enrollment e 
       WHERE e.student_id = s.id 
         AND e.class_id = :class_id
     )
   ORDER BY s.student_code
   ```

5a. **Academic Staff chọn học viên (multi-select)**

6a. **Academic Staff click "Ghi danh"**

### Option B: Import CSV Học Viên Mới

4b. **Academic Staff upload file CSV**
   - Format: student_code, full_name, email, phone, ...

5b. **System parse và validate CSV**
   - Kiểm tra format
   - Kiểm tra email/phone đã tồn tại chưa

6b. **System hiển thị preview**
   - Danh sách học viên sẽ được tạo/import

7b. **Academic Staff confirm**

### Xử Lý Chung Cho Cả 2 Options:

7. **System kiểm tra capacity**
   - Query:
   ```
   SELECT COUNT(*) AS enrolled_count
   FROM enrollment
   WHERE class_id = :class_id
     AND status = 'enrolled'
   ```
   - Nếu enrolled_count + new_count > max_capacity:
     - Cảnh báo "Vượt sức chứa"
     - Academic Staff có thể override (với lý do)

8. **System thực hiện enrollment trong TRANSACTION**
   ```
   BEGIN;
   
   -- 1. Tạo student mới nếu chưa có (từ CSV)
   INSERT INTO user_account (email, phone, full_name, ...)
   VALUES (...) 
   ON CONFLICT (email) DO NOTHING
   RETURNING id;
   
   INSERT INTO student (user_account_id, student_code, branch_id, ...)
   VALUES (...)
   RETURNING id;
   
   -- 2. Tạo enrollment
   INSERT INTO enrollment (
     student_id,
     class_id,
     status,
     enrolled_at,
     created_by
   ) VALUES (
     :student_id,
     :class_id,
     'enrolled',
     NOW(),
     :academic_staff_user_id
   );
   
   -- 3. Tự động sinh student_session (lịch cá nhân)
   INSERT INTO student_session (student_id, session_id, is_makeup, attendance_status)
   SELECT 
     :student_id,
     s.id,
     FALSE,
     'planned'
   FROM session s
   WHERE s.class_id = :class_id
     AND s.status = 'planned'
     AND s.date >= CURRENT_DATE;
   
   COMMIT;
   ```

9. **System validation**
   - Kiểm tra schedule conflict: Student không bị trùng lịch với lớp khác
   - Nếu có conflict, cảnh báo và cho phép override

10. **System gửi notification tới Student**
    - "Bạn đã được ghi danh vào lớp [Tên]. Xem lịch học tại..."

**Result:** 
- Student được enroll
- Student có lịch học cá nhân (student_session)
- Student xuất hiện trong danh sách điểm danh của Teacher

---

## FLOW 5: Submit Lớp Để Duyệt (Academic Staff Submit Class for Approval)

**Actors involved:** Academic Staff, Center Head/Manager, System  
**Description:** Sau khi setup xong lớp (có teacher, resource), Giáo vụ submit để Center Head hoặc Manager duyệt.

**Database Tables Involved:**
- `class` (cập nhật status và submitted_at)

**Flow Steps:**

1. **Academic Staff kiểm tra lớp đã đủ thông tin chưa**
   - Tất cả sessions đã có teacher
   - Tất cả sessions đã có resource
   - Lịch học không có conflict

2. **Academic Staff click "Submit để duyệt"**

3. **System validation**
   - Kiểm tra:
     - Mọi session có ít nhất 1 teaching_slot
     - Mọi session có ít nhất 1 session_resource
     - Không có conflict về teacher/resource

4. **System cập nhật class**
   ```
   UPDATE class
   SET 
     status = 'pending_approval',
     submitted_at = NOW()
   WHERE id = :class_id
   ```

5. **System gửi notification tới Center Head (nếu class thuộc branch của họ) hoặc Manager**
   - "Lớp [Tên] đã sẵn sàng để duyệt"

6. **Center Head/Manager review và approve** (xem manager-flows.md)
   - Approve:
     ```
     UPDATE class
     SET 
       status = 'scheduled',
       approved_by = :approver_user_id,
       approved_at = NOW()
     WHERE id = :class_id
     ```
   - Reject:
     ```
     UPDATE class
     SET 
       status = 'draft',
       rejection_reason = :reason
     WHERE id = :class_id
     ```

7. **System gửi notification tới Academic Staff**
   - "Lớp [Tên] đã được duyệt. Có thể bắt đầu ghi danh."

**Result:** 
- Class status = 'scheduled'
- Sẵn sàng để ghi danh học viên

---

## FLOW 6: Xử Lý Yêu Cầu Nghỉ Của Học Viên (Academic Staff Handle Student Absence Request)

**Actors involved:** Academic Staff, System  
**Description:** Giáo vụ duyệt yêu cầu báo nghỉ của học viên.

**Database Tables Involved:**
- `student_request` (request cần xử lý)
- `student_session` (cập nhật attendance_status)

**Flow Steps:**

1. **Academic Staff vào menu "Yêu cầu học viên"**
   - Filter: request_type='absence', status='pending'

2. **System load danh sách request**
   ```
   SELECT 
     sr.id,
     s.student_code,
     u.full_name,
     sess.date,
     c.name AS class_name,
     sr.reason,
     sr.submitted_at
   FROM student_request sr
   JOIN student s ON sr.student_id = s.id
   JOIN user_account u ON s.user_account_id = u.id
   JOIN session sess ON sr.target_session_id = sess.id
   JOIN class c ON sess.class_id = c.id
   WHERE sr.request_type = 'absence'
     AND sr.status = 'pending'
     AND c.branch_id IN (SELECT branch_id FROM user_branches WHERE user_account_id = :staff_user_id)
   ORDER BY sr.submitted_at
   ```

3. **Academic Staff review từng request**
   - Đọc lý do
   - Kiểm tra lịch sử nghỉ của học viên:
     ```
     SELECT COUNT(*) AS absence_count
     FROM student_session ss
     JOIN session s ON ss.session_id = s.id
     WHERE ss.student_id = :student_id
       AND s.class_id = :class_id
       AND ss.attendance_status IN ('absent', 'excused')
     ```

4. **Academic Staff quyết định**

### Option A: Approve
   ```
   BEGIN;
   
   UPDATE student_request
   SET 
     status = 'approved',
     decided_by = :staff_user_id,
     decided_at = NOW()
   WHERE id = :request_id;
   
   UPDATE student_session
   SET attendance_status = 'excused'
   WHERE student_id = :student_id
     AND session_id = :target_session_id;
   
   COMMIT;
   ```

### Option B: Reject
   ```
   UPDATE student_request
   SET 
     status = 'rejected',
     decided_by = :staff_user_id,
     decided_at = NOW(),
     rejection_reason = 'Vượt quá số buổi nghỉ cho phép'
   WHERE id = :request_id;
   ```

5. **System gửi notification tới Student**
   - "Yêu cầu báo nghỉ đã được [approved/rejected]"

**Result:** 
- Request được xử lý
- Student_session cập nhật status

---

## FLOW 7: Xử Lý Yêu Cầu Học Bù (Academic Staff Handle Student Makeup Request) ⭐ COMPLEX

**Actors involved:** Academic Staff, System  
**Description:** Giáo vụ duyệt yêu cầu học bù, tạo student_session mới cho buổi học bù.

**Database Tables Involved:**
- `student_request` (request với makeup_session_id)
- `student_session` (tạo record mới cho makeup session)
- `session` (kiểm tra capacity)

**Flow Steps:**

1. **Academic Staff vào "Yêu cầu học viên"**
   - Filter: request_type='makeup', status='pending'

2. **System load danh sách request**
   - Tương tự absence request, nhưng có thêm makeup_session_id

3. **Academic Staff review request**
   - Xem:
     - Buổi gốc bị nghỉ (target_session_id)
     - Buổi học bù (makeup_session_id)
     - Kiểm tra cùng course_session_id không

4. **Academic Staff kiểm tra capacity của buổi học bù**
   ```
   SELECT 
     c.max_capacity,
     COUNT(ss.id) AS enrolled_count
   FROM class c
   LEFT JOIN student_session ss ON (
     ss.session_id = :makeup_session_id
     AND ss.attendance_status IN ('planned', 'present', 'late')
   )
   WHERE c.id = (SELECT class_id FROM session WHERE id = :makeup_session_id)
   GROUP BY c.id
   ```

5. **Nếu đủ chỗ, Academic Staff approve**
   ```
   BEGIN;
   
   UPDATE student_request
   SET status = 'approved', decided_by = :staff_user_id, decided_at = NOW()
   WHERE id = :request_id;
   
   -- Đánh dấu buổi gốc là excused (nếu chưa)
   UPDATE student_session
   SET attendance_status = 'excused'
   WHERE student_id = :student_id AND session_id = :target_session_id;
   
   -- Tạo student_session cho buổi học bù
   INSERT INTO student_session (
     student_id,
     session_id,
     is_makeup,
     attendance_status
   ) VALUES (
     :student_id,
     :makeup_session_id,
     TRUE,
     'planned'
   );
   
   COMMIT;
   ```

6. **System gửi notification**
   - Tới Student: "Yêu cầu học bù đã được duyệt"
   - Tới Teacher của buổi học bù: "Học viên [Tên] sẽ tham gia học bù"

**Result:** 
- Student có thêm buổi học bù trong lịch
- Teacher thấy student trong danh sách điểm danh

---

## FLOW 8: Xử Lý Yêu Cầu Chuyển Lớp (Academic Staff Handle Student Transfer Request) ⭐ MOST COMPLEX

**Actors involved:** Academic Staff, System  
**Description:** Giáo vụ duyệt yêu cầu chuyển lớp, thực hiện transaction phức tạp.

**Database Tables Involved:**
- `student_request` (current_class_id, target_class_id, effective_date)
- `enrollment` (cập nhật class A, tạo mới class B)
- `student_session` (excused buổi tương lai class A, tạo mới cho class B)

**Flow Steps:**

1. **Academic Staff vào "Yêu cầu học viên"**
   - Filter: request_type='transfer', status='pending'

2. **Academic Staff review request**
   - Kiểm tra:
     - Class A và class B cùng course_id
     - Class B còn chỗ
     - Effective_date hợp lý

3. **System phát hiện content gap** (nếu có)
   - So sánh course_session_id còn lại của class A vs class B

4. **Academic Staff quyết định approve**
   ```
   BEGIN;
   
   -- 1. Cập nhật request
   UPDATE student_request
   SET status = 'approved', decided_by = :staff_user_id, decided_at = NOW()
   WHERE id = :request_id;
   
   -- 2. Xác định cutoff sessions
   WITH cutoff AS (
     SELECT 
       (SELECT id FROM session 
        WHERE class_id = :current_class_id AND date < :effective_date 
        ORDER BY date DESC LIMIT 1) AS left_session_id,
       (SELECT id FROM session 
        WHERE class_id = :target_class_id AND date >= :effective_date 
        ORDER BY date ASC LIMIT 1) AS join_session_id
   )
   
   -- 3. Cập nhật enrollment class A
   UPDATE enrollment
   SET 
     status = 'transferred',
     left_at = NOW(),
     left_session_id = (SELECT left_session_id FROM cutoff)
   WHERE student_id = :student_id AND class_id = :current_class_id;
   
   -- 4. Tạo enrollment class B
   INSERT INTO enrollment (student_id, class_id, status, enrolled_at, join_session_id)
   VALUES (
     :student_id, 
     :target_class_id, 
     'enrolled', 
     NOW(),
     (SELECT join_session_id FROM cutoff)
   );
   
   -- 5. Excused buổi tương lai class A
   UPDATE student_session
   SET 
     attendance_status = 'excused',
     note = 'Chuyển sang lớp mới'
   WHERE student_id = :student_id
     AND session_id IN (
       SELECT id FROM session 
       WHERE class_id = :current_class_id AND date >= :effective_date
     );
   
   -- 6. Tạo student_session cho class B
   INSERT INTO student_session (student_id, session_id, is_makeup, attendance_status)
   SELECT 
     :student_id,
     s.id,
     FALSE,
     'planned'
   FROM session s
   WHERE s.class_id = :target_class_id
     AND s.date >= :effective_date
     AND s.status = 'planned';
   
   COMMIT;
   ```

5. **System gửi notification**
   - Tới Student
   - Tới Teacher cả 2 lớp

**Result:** 
- Student chuyển lớp thành công
- Lịch sử được bảo toàn

---

## FLOW 9: Xử Lý Yêu Cầu Nghỉ Của Giáo Viên (Academic Staff Handle Teacher Leave Request) ⭐ CRITICAL

**Actors involved:** Academic Staff, System  
**Description:** Giáo vụ phải tìm giải pháp khi giáo viên xin nghỉ (tìm substitute, reschedule, hoặc cancel).

**Database Tables Involved:**
- `teacher_request` (request leave)
- `teaching_slot` (cập nhật teacher_id nếu substitute)
- `session` (reschedule hoặc cancel)
- `student_session` (cập nhật nếu cancel)

**Flow Steps:**

1. **Academic Staff vào "Yêu cầu giáo viên"**
   - Filter: request_type='leave', status='pending'

2. **Academic Staff review request**
   - Xem session nào bị ảnh hưởng
   - Xem lý do nghỉ

3. **Academic Staff phải tìm giải pháp** (request chỉ được approve khi có giải pháp)

### Option A: Tìm Giáo Viên Thay Thế

4a. **System gợi ý substitute teachers**
   - Gọi function: `find_available_substitute_teachers(session_id)`
   - Logic: Tương tự như phân công teacher, nhưng ưu tiên teacher đã đăng ký OT

5a. **Academic Staff chọn substitute và approve**
   ```
   BEGIN;
   
   -- 1. Approve leave request
   UPDATE teacher_request
   SET 
     status = 'approved',
     decided_by = :staff_user_id,
     decided_at = NOW(),
     resolution = 'Teacher X sẽ dạy thay'
   WHERE id = :request_id;
   
   -- 2. Tạo OT request cho substitute (để tính lương)
   INSERT INTO teacher_request (
     teacher_id,
     session_id,
     request_type,
     status,
     decided_by,
     decided_at
   ) VALUES (
     :substitute_teacher_id,
     :session_id,
     'ot',
     'approved',
     :staff_user_id,
     NOW()
   );
   
   -- 3. Cập nhật teaching_slot
   UPDATE teaching_slot
   SET teacher_id = :substitute_teacher_id
   WHERE session_id = :session_id 
     AND teacher_id = :original_teacher_id;
   
   COMMIT;
   ```

6a. **System gửi notification**
   - Tới Teacher gốc: "Yêu cầu nghỉ đã được chấp nhận"
   - Tới Teacher thay: "Bạn được phân công thay thế"
   - Tới Students: "Giáo viên thay đổi cho buổi học [...]"

### Option B: Đổi Lịch Buổi Học (Reschedule)

4b. **Academic Staff chọn "Reschedule Session"**

5b. **System tìm slot mới khả dụng**
   - Input: new_date, new_slot_id
   - Kiểm tra:
     - Teacher gốc rảnh
     - Resource rảnh
     - Students không conflict (optional)

6b. **Academic Staff confirm reschedule**
   ```
   BEGIN;
   
   -- 1. Tạo session mới
   INSERT INTO session (class_id, course_session_id, date, start_time, end_time, type, status)
   SELECT 
     class_id,
     course_session_id,
     :new_date,
     :new_start_time,
     :new_end_time,
     type,
     'planned'
   FROM session
   WHERE id = :old_session_id
   RETURNING id INTO :new_session_id;
   
   -- 2. Copy teaching_slot
   INSERT INTO teaching_slot (session_id, teacher_id, skill, role)
   SELECT :new_session_id, teacher_id, skill, role
   FROM teaching_slot
   WHERE session_id = :old_session_id;
   
   -- 3. Copy session_resource
   INSERT INTO session_resource (session_id, resource_type, resource_id, capacity_override)
   SELECT :new_session_id, resource_type, resource_id, capacity_override
   FROM session_resource
   WHERE session_id = :old_session_id;
   
   -- 4. Transfer student_session
   UPDATE student_session
   SET session_id = :new_session_id
   WHERE session_id = :old_session_id;
   
   -- 5. Cancel old session
   UPDATE session
   SET status = 'cancelled', teacher_note = 'Rescheduled to ' || :new_date
   WHERE id = :old_session_id;
   
   -- 6. Approve leave request
   UPDATE teacher_request
   SET status = 'approved', decided_by = :staff_user_id, decided_at = NOW(),
       resolution = 'Session rescheduled'
   WHERE id = :request_id;
   
   COMMIT;
   ```

7b. **System gửi notification tới tất cả students**
   - "Buổi học ngày [...] đã được dời sang ngày [...]"

### Option C: Hủy Buổi Học (Last Resort)

4c. **Academic Staff chọn "Cancel Session"**
   ```
   BEGIN;
   
   -- 1. Cancel session
   UPDATE session
   SET status = 'cancelled', teacher_note = 'Cancelled due to teacher unavailability'
   WHERE id = :session_id;
   
   -- 2. Mark all students excused
   UPDATE student_session
   SET attendance_status = 'excused'
   WHERE session_id = :session_id;
   
   -- 3. Approve leave request
   UPDATE teacher_request
   SET status = 'approved', decided_by = :staff_user_id, decided_at = NOW(),
       resolution = 'Session cancelled'
   WHERE id = :request_id;
   
   COMMIT;
   ```

5c. **System gửi notification**
   - "Buổi học ngày [...] đã bị hủy. Bạn được đánh dấu nghỉ có phép."

**Result:** 
- Teacher request được xử lý
- Session vẫn diễn ra (substitute/reschedule) hoặc bị hủy

---

## FLOW 10: Đổi Lịch Hàng Loạt Cho Lớp (Academic Staff Bulk Reschedule Class)

**Actors involved:** Academic Staff, Center Head (approval), System  
**Description:** Thay đổi khung giờ học cho toàn bộ lớp (vd: từ sáng sang chiều).

**Database Tables Involved:**
- `session` (update hàng loạt start_time, end_time)
- `time_slot_template` (chọn slot mới)

**Flow Steps:**

1. **Academic Staff vào chi tiết lớp**
   - Click "Đổi lịch học"

2. **Academic Staff điền thông tin**
   - Effective_date: Ngày bắt đầu áp dụng
   - Target_dow: Ngày trong tuần cần đổi (NULL = tất cả)
   - New_slot_id: Khung giờ mới (từ time_slot_template)

3. **System preview thay đổi**
   - Hiển thị danh sách sessions sẽ bị ảnh hưởng
   - Kiểm tra conflict với teacher/resource

4. **Academic Staff submit yêu cầu**

5. **Center Head approve** (vì ảnh hưởng lớn)

6. **System thực hiện bulk update**
   ```
   UPDATE session
   SET 
     start_time = :new_start_time,
     end_time = :new_end_time,
     teacher_note = 'Rescheduled to ' || :new_slot_name || ' effective ' || :effective_date
   WHERE class_id = :class_id
     AND status = 'planned'
     AND date >= :effective_date
     AND (
       :target_dow IS NULL 
       OR EXTRACT(DOW FROM date) = :target_dow
     )
   ```

7. **System gửi notification hàng loạt**
   - Tới tất cả students của lớp
   - Tới teacher

**Result:** 
- Toàn bộ lịch học của lớp được cập nhật
- Students và teachers nhận notification

---

## Tóm Tắt Các Flow Chính Của Academic Staff

| Flow | Mô Tả | Bảng Chính Liên Quan |
|------|-------|---------------------|
| 1. Tạo Lớp | Tạo class mới, hệ thống auto-generate sessions | class → session |
| 2. Phân Công Teacher | Assign teacher cho sessions | teaching_slot ← teacher_skill, teacher_availability |
| 3. Phân Công Resource | Assign phòng/Zoom cho sessions | session_resource ← resource |
| 4. Ghi Danh | Enroll students, auto-generate student_session | enrollment → student_session |
| 5. Submit Lớp Để Duyệt | Submit class cho Center Head/Manager approve | class.status → 'pending_approval' |
| 6. Duyệt Absence Request | Approve request nghỉ của student | student_request → student_session (excused) |
| 7. Duyệt Makeup Request | Approve request học bù, tạo student_session mới | student_request → student_session (insert) |
| 8. Duyệt Transfer Request | Approve chuyển lớp, transaction phức tạp | enrollment, student_session (bulk update) |
| 9. Duyệt Leave Request | Xử lý nghỉ của teacher: substitute/reschedule/cancel | teacher_request → teaching_slot / session |
| 10. Bulk Reschedule | Đổi lịch hàng loạt cho lớp | session (bulk update) |

---

**Lưu Ý Quan Trọng:**
- Academic Staff là vai trò vận hành cốt lõi, handle nhiều flow phức tạp
- Nhiều flow yêu cầu TRANSACTION để đảm bảo data consistency
- Academic Staff phải kiểm tra conflict (teacher/resource/student) trước khi approve
- System tự động gửi notification cho các bên liên quan sau mỗi thay đổi
- Audit trail quan trọng: created_by, decided_by, decided_at

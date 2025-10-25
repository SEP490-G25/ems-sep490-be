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

6. **System tự động clone assessments từ course**
   - System thực hiện clone trong TRANSACTION:
   ```
   BEGIN;

   -- Clone tất cả course_assessment từ course template
   INSERT INTO assessment (
     class_id,
     course_assessment_id,
     name,
     kind,
     max_score,
     weight,
     description,
     created_by
   )
   SELECT
     :class_id,
     ca.id,  -- Link về course_assessment gốc
     ca.name,
     ca.kind,
     ca.max_score,
     ca.weight,
     ca.description,
     :academic_staff_user_id
   FROM course_assessment ca
   WHERE ca.course_id = :course_id
   ORDER BY ca.created_at;

   -- Clone assessment-CLO mappings
   -- (Mapping giữa assessment và CLO để track learning outcomes)
   INSERT INTO assessment_clo_mapping (assessment_id, clo_id)
   SELECT
     a.id,  -- assessment mới vừa tạo
     cacm.clo_id
   FROM assessment a
   JOIN course_assessment ca ON a.course_assessment_id = ca.id
   JOIN course_assessment_clo_mapping cacm ON cacm.course_assessment_id = ca.id
   WHERE a.class_id = :class_id;

   COMMIT;
   ```

7. **System validation**
   - Tổng weight của assessments = 100%
   - Mỗi assessment có ít nhất 1 CLO mapping (để track learning outcomes)

**Result:**
- Class mới được tạo với status='draft'
- Tất cả sessions được sinh tự động
- Tất cả assessments được clone từ course template
- Assessment-CLO mappings được clone để track learning outcomes
- Sẵn sàng để phân công teacher và resource

**Lợi ích của việc clone assessments:**
- **Consistency:** Tất cả classes của cùng course có cùng cấu trúc đánh giá
- **Easy management:** Teacher không cần tạo assessments từ đầu
- **Traceability:** Có thể track về course_assessment gốc qua `assessment.course_assessment_id`
- **Flexibility:** Academic Staff vẫn có thể edit/add/remove assessments nếu cần

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

## FLOW 2A: Quản Lý Lịch Dạy Cố Định Của Giáo Viên (Manage Teacher Availability)

**Actors involved:** Academic Staff, System
**Description:** Giáo vụ define và chỉnh sửa lịch dạy cố định (teacher_availability) cho giáo viên. Teacher KHÔNG có quyền tự chỉnh sửa, chỉ xem được.

**Database Tables Involved:**
- `teacher_availability` (lịch dạy cố định hàng tuần)
- `time_slot_template` (khung giờ)
- `teacher` → `user_account`
- `teaching_slot` → `session` (để check xem teacher còn đang dạy lớp nào không)

**Flow Steps:**

### Scenario A: Thêm/Chỉnh Sửa Teacher Availability

1. **Academic Staff vào Sidebar → "Teacher List"**

2. **System load danh sách teachers:**
   ```
   SELECT
     t.id AS teacher_id,
     u.full_name,
     u.email,
     -- Count số lớp đang dạy
     COUNT(DISTINCT CASE
       WHEN s.status IN ('planned', 'ongoing') AND s.date >= CURRENT_DATE
       THEN c.id
     END) AS active_classes_count
   FROM teacher t
   JOIN user_account u ON t.user_account_id = u.id
   LEFT JOIN teaching_slot ts ON ts.teacher_id = t.id
   LEFT JOIN session s ON ts.session_id = s.id
   LEFT JOIN class c ON s.class_id = c.id
   GROUP BY t.id, u.id
   ORDER BY u.full_name
   ```

3. **Academic Staff chọn teacher → Click "Manage Availability"**

4. **System hiển thị teacher detail với current availability:**
   ```
   SELECT
     ta.id,
     ta.day_of_week,
     ta.time_slot_template_id,
     ta.note,
     ta.effective_date,
     tst.name AS time_slot_name,
     tst.start_time,
     tst.end_time,
     b.name AS branch_name
   FROM teacher_availability ta
   JOIN time_slot_template tst ON ta.time_slot_template_id = tst.id
   JOIN branch b ON tst.branch_id = b.id
   WHERE ta.teacher_id = :teacher_id
     AND (ta.effective_date IS NULL OR ta.effective_date <= CURRENT_DATE)
   ORDER BY ta.day_of_week, tst.start_time
   ```

5. **System hiển thị calendar grid (7 ngày trong tuần):**
   - Mỗi ngày hiển thị các time slots mà teacher available
   - Có button "Add", "Edit", "Delete" cho từng slot

6. **Academic Staff có thể:**

   **6a. Thêm availability mới:**
   - Click "Add" → Chọn day_of_week, time_slot_template, branch
   - Optional: Chọn effective_date (ngày bắt đầu áp dụng lịch mới)
   - System INSERT:
   ```
   INSERT INTO teacher_availability (
     teacher_id,
     day_of_week,
     time_slot_template_id,
     note,
     effective_date
   ) VALUES (
     :teacher_id,
     :day_of_week,
     :time_slot_template_id,
     :note,
     :effective_date
   )
   ```

   **6b. Chỉnh sửa availability:**
   - Click "Edit" → Modify time_slot hoặc effective_date
   - System UPDATE:
   ```
   UPDATE teacher_availability
   SET
     time_slot_template_id = :new_time_slot_id,
     note = :note,
     effective_date = :effective_date
   WHERE id = :availability_id
   ```

   **6c. Xóa availability:**
   - Click "Delete"
   - System validation:
     ```
     -- Check xem teacher còn lớp nào sử dụng availability này không
     SELECT COUNT(*) AS conflict_count
     FROM teaching_slot ts
     JOIN session s ON ts.session_id = s.id
     WHERE ts.teacher_id = :teacher_id
       AND EXTRACT(DOW FROM s.date) = :day_of_week
       AND (s.start_time, s.end_time) OVERLAPS (:slot_start, :slot_end)
       AND s.status IN ('planned', 'ongoing')
       AND s.date >= CURRENT_DATE
     ```
   - Nếu conflict_count > 0: Cảnh báo "Teacher đang có X lớp trong khung giờ này. Không thể xóa."
   - Nếu conflict_count = 0: DELETE availability

7. **Trường hợp teacher đang dạy lớp (active_classes_count > 0):**
   - System cảnh báo: "⚠️ Teacher đang dạy X lớp. Chỉ có thể thêm availability mới với effective_date trong tương lai."
   - Academic Staff có thể:
     - **Option A:** Chọn effective_date = ngày sau khi các lớp hiện tại kết thúc
     - **Option B:** Chỉ add thêm slots, không xóa slots hiện tại

8. **System gửi notification tới Teacher:**
   - "Lịch dạy cố định của bạn đã được cập nhật bởi Giáo vụ [Name]. Vui lòng kiểm tra."

### Scenario B: Teacher Mới Vào Trung Tâm (Onboarding)

1. **Academic Staff tạo teacher account → Vào "Manage Availability"**

2. **System hiển thị empty calendar:**
   - "Chưa có lịch dạy cố định. Vui lòng thêm availability cho teacher này."

3. **Academic Staff define lịch dạy ban đầu:**
   - Ví dụ: Teacher có thể dạy các buổi sáng Thứ 2, 4, 6
   - Add từng availability slot

4. **System lưu và activate ngay:**
   - effective_date = CURRENT_DATE (hoặc ngày teacher bắt đầu làm việc)

**Result:**
- Teacher availability được define/update bởi Academic Staff
- Teacher chỉ có quyền XEM (read-only)
- Hệ thống sử dụng availability để:
  - Gợi ý teacher khi assign lớp mới
  - Check conflict khi phân công
  - Tìm replacement teacher phù hợp
- Có effective_date để quản lý thay đổi lịch theo thời gian

**Lưu ý quan trọng:**
- Teacher_availability là **lịch dạy cố định**, không phải lịch rảnh
- Chỉ được sửa khi teacher không còn lớp đang dạy trong khung giờ đó
- effective_date cho phép schedule lịch mới từ trước (vd: lịch mới áp dụng từ tháng sau)

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

## FLOW 8: Tạo Yêu Cầu Chuyển Lớp Cho Student (Academic Staff Create Transfer Request) ⭐ MOST COMPLEX

**Actors involved:** Academic Staff, Student, System
**Description:** **Academic Staff tạo** yêu cầu chuyển lớp cho student, sau đó gửi cho student xác nhận. Student confirm → System thực hiện transaction phức tạp.

**Database Tables Involved:**
- `student_request` (submitted_by = academic_staff_id, current_class_id, target_class_id, effective_date)
- `enrollment` (cập nhật class A, tạo mới class B)
- `student_session` (excused buổi tương lai class A, tạo mới cho class B)

**Flow Steps:**

### Phase 1: Academic Staff Tạo Transfer Request

1. **Academic Staff vào "Student Management" → Chọn student → Click "Chuyển lớp"**

2. **Academic Staff điền form:**
   - **Chọn current_class_id** (lớp hiện tại của student)
   - **System hiển thị danh sách lớp khả dụng:**
   ```
   SELECT
     c.id AS class_id,
     c.name,
     c.modality,
     b.name AS branch_name,
     c.max_capacity,
     COUNT(e.id) AS enrolled_count
   FROM class c
   JOIN branch b ON c.branch_id = b.id
   LEFT JOIN enrollment e ON (e.class_id = c.id AND e.status = 'enrolled')
   WHERE c.course_id = (
     SELECT course_id
     FROM class
     WHERE id = :current_class_id
   )
   AND c.id != :current_class_id
   AND c.status IN ('scheduled', 'ongoing')
   GROUP BY c.id, b.id
   HAVING COUNT(e.id) < c.max_capacity
   ORDER BY c.start_date
   ```
   - Chọn **target_class_id**
   - Chọn **effective_date** (ngày bắt đầu học lớp mới)
   - Nhập **lý do:** "Student yêu cầu đổi ca học", "Chuyển online"

3. **System kiểm tra content gap** (có buổi nào bị thiếu không)
   - Query các buổi còn lại của class A:
   ```
   SELECT DISTINCT course_session_id
   FROM session
   WHERE class_id = :current_class_id
     AND date >= :effective_date
     AND status = 'planned'
   ```
   - Query các buổi tương lai của class B:
   ```
   SELECT DISTINCT course_session_id
   FROM session
   WHERE class_id = :target_class_id
     AND date >= :effective_date
     AND status = 'planned'
   ```
   - So sánh: nếu class A có course_session_id mà class B không có → GAP

4. **System cảnh báo nếu có gap:**
   - "⚠️ Lưu ý: Lớp mới đã học qua Buổi 15 và 17. Student cần tự học nội dung này."
   - Academic Staff xác nhận "Đã thông báo student"

5. **Academic Staff click "Tạo yêu cầu chuyển lớp"**
   - System INSERT:
   ```
   INSERT INTO student_request
   (student_id, current_class_id, target_class_id, effective_date, request_type, reason, status, submitted_at, submitted_by)
   VALUES
   (:student_id, :current_class_id, :target_class_id, :effective_date, 'transfer', :reason, 'pending', NOW(), :academic_staff_id)
   ```

6. **System gửi notification tới Student:**
   - Email/In-app: "Giáo vụ đã tạo yêu cầu chuyển bạn từ lớp [A] sang lớp [B] từ ngày [Date]. Vui lòng xác nhận."

### Phase 2: Student Confirm (Xem student-flows.md FLOW 4)

7. **Student vào "Requests" → Xem request chuyển lớp**
   - Hiển thị thông tin: lớp cũ, lớp mới, effective_date, lý do, warning (nếu có gap)

8. **Student click "Chấp nhận" hoặc "Từ chối"**

### Phase 3: System Thực Hiện Transfer (Khi Student Chấp Nhận)

9. **Nếu Student chấp nhận, system thực hiện transaction phức tạp:**
   ```
   BEGIN;

   -- 1. Cập nhật request
   UPDATE student_request
   SET status = 'approved', decided_by = :student_user_id, decided_at = NOW()
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
   INSERT INTO enrollment (student_id, class_id, status, enrolled_at, join_session_id, created_by)
   VALUES (
     :student_id,
     :target_class_id,
     'enrolled',
     NOW(),
     (SELECT join_session_id FROM cutoff),
     :academic_staff_id
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

10. **System gửi notifications:**
    - Tới Academic Staff: "Student [Name] đã chấp nhận yêu cầu chuyển lớp."
    - Tới Student: "Bạn đã chuyển sang lớp [B]. Lịch học mới đã được cập nhật."
    - Tới Teacher lớp B: "Học viên [Name] sẽ tham gia lớp từ [Date]."

**Result:**
- **THAY ĐỔI:** Academic Staff tạo request → Student confirm (thay vì student tự tạo)
- `student_request.submitted_by = academic_staff_id` (để phân biệt)
- Student chuyển lớp thành công sau khi confirm
- Lịch sử được bảo toàn (audit trail đầy đủ)
- Lịch học tự động được cập nhật

---

## FLOW 9: Xử Lý Yêu Cầu Dạy Thay Của Giáo Viên (Academic Staff Handle Teacher Swap Request) ⭐ CRITICAL

**Actors involved:** Academic Staff, Replacement Teacher, System
**Description:** Giáo vụ xử lý yêu cầu dạy thay của teacher (type='swap'). Chọn replacement teacher → Gửi confirm → Replacement teacher xác nhận.

**Database Tables Involved:**
- `teacher_request` (request_type='swap', replacement_teacher_id, status)
- `teaching_slot` (update status, insert new)
- `teacher_skill`, `teacher_availability` (để tìm replacement teacher)

**Flow Steps:**

1. **Academic Staff vào "Teacher Requests"**
   - Filter: request_type='swap', status='pending'

2. **System load danh sách teacher swap requests:**
   ```
   SELECT
     tr.id AS request_id,
     tr.teacher_id,
     t.full_name AS teacher_name,
     tr.session_id,
     s.date,
     s.start_time,
     s.end_time,
     c.name AS class_name,
     tr.reason,
     tr.submitted_at,
     tr.replacement_teacher_id  -- Có thể NULL hoặc teacher gợi ý
   FROM teacher_request tr
   JOIN teacher t ON tr.teacher_id = t.id
   JOIN user_account ua ON t.user_account_id = ua.id
   JOIN session s ON tr.session_id = s.id
   JOIN class c ON s.class_id = c.id
   WHERE tr.request_type = 'swap'
     AND tr.status = 'pending'
   ORDER BY tr.submitted_at
   ```

3. **Academic Staff chọn request → Click "Handle"**

4. **System hiển thị request detail và list ra replacement teachers phù hợp:**
   - **Sort theo ưu tiên:**
     - Skill match
     - Level phù hợp
     - Availability (teacher_availability)
     - No conflict
   - Query (giống như trong teacher-flows.md FLOW 6, bước 5b):
   ```
   SELECT
     t.id AS teacher_id,
     u.full_name,
     ts.skill,
     ts.level,
     -- Check availability
     EXISTS (
       SELECT 1
       FROM teacher_availability ta
       WHERE ta.teacher_id = t.id
         AND ta.day_of_week = EXTRACT(DOW FROM :session_date)
         AND ta.time_slot_template_id IN (
           SELECT tst.id
           FROM time_slot_template tst
           WHERE (tst.start_time, tst.end_time) OVERLAPS (:session_start_time, :session_end_time)
         )
     ) AS is_available,
     -- Check conflict
     (
       SELECT COUNT(*)
       FROM teaching_slot tslot
       JOIN session s2 ON tslot.session_id = s2.id
       WHERE tslot.teacher_id = t.id
         AND s2.date = :session_date
         AND (s2.start_time, s2.end_time) OVERLAPS (:session_start_time, :session_end_time)
         AND s2.status IN ('planned', 'ongoing')
     ) AS conflict_count
   FROM teacher t
   JOIN user_account u ON t.user_account_id = u.id
   JOIN teacher_skill ts ON t.id = ts.teacher_id
   WHERE ts.skill = ANY(:required_skills)
     AND t.id != :original_teacher_id
   HAVING is_available = TRUE AND conflict_count = 0
   ORDER BY ts.level DESC, u.full_name
   ```

5. **Academic Staff chọn replacement teacher → Click "Send Confirmation"**
   - System UPDATE:
   ```
   UPDATE teacher_request
   SET
     status = 'waiting_confirm',
     replacement_teacher_id = :replacement_teacher_id,
     decided_by = :academic_staff_id,
     decided_at = NOW()
   WHERE id = :request_id
   ```

6. **System gửi notification tới Replacement Teacher:**
   - "Bạn được mời dạy thay buổi [Session] vào [Ngày]. Vui lòng xác nhận."

7. **Replacement Teacher confirm (xem teacher-flows.md FLOW 6, bước 7-8):**
   - Replacement teacher vào "Requests" → Click "Chấp nhận" hoặc "Từ chối"

8. **Nếu Replacement Teacher chấp nhận:**
   ```
   BEGIN;

   -- 1. Update teacher_request status
   UPDATE teacher_request
   SET status = 'confirmed'
   WHERE id = :request_id;

   -- 2. Update teaching_slot của teacher cũ (mark as on_leave)
   UPDATE teaching_slot
   SET status = 'on_leave'
   WHERE session_id = :session_id
     AND teacher_id = :original_teacher_id;

   -- 3. Insert teaching_slot mới cho replacement teacher
   INSERT INTO teaching_slot (session_id, teacher_id, skill, role, status)
   VALUES (
     :session_id,
     :replacement_teacher_id,
     :skill,
     'primary',
     'substituted'
   );

   COMMIT;
   ```

9. **System gửi notifications:**
   - Tới Teacher gốc: "Yêu cầu dạy thay đã được xác nhận. [Replacement Name] sẽ dạy thay."
   - Tới Students: "Thông báo: Giáo viên [Replacement Name] sẽ dạy buổi học ngày [Date]."

**Trường hợp Replacement Teacher từ chối:**
   - System UPDATE:
   ```
   UPDATE teacher_request
   SET
     status = 'pending',
     replacement_teacher_id = NULL,
     decided_by = NULL,
     decided_at = NULL
   WHERE id = :request_id
   ```
   - Notification tới Academic Staff: "Teacher [Name] đã từ chối. Vui lòng chọn teacher khác."
   - Academic Staff quay lại bước 4

**Result:**
- Teacher_request.status = 'confirmed'
- Teaching_slot cũ: status = 'on_leave'
- Teaching_slot mới: status = 'substituted', teacher_id = replacement_teacher_id
- Audit trail đầy đủ

**Lưu ý:**
- Chi tiết flow từ phía teacher xem **teacher-flows.md FLOW 6**
- Flow này là phần xử lý từ phía Academic Staff

**Result:**
- Teacher swap request được approve
- Replacement teacher được assign vào session
- Hệ thống tạo OT request tự động cho replacement teacher

---

## FLOW 9A: Xử Lý Đơn Đổi Lịch Của Giáo Viên (Academic Staff Handle Teacher Reschedule Request)

**Actors involved:** Academic Staff, System
**Description:** Xử lý đơn xin đổi lịch của giáo viên (teacher_request.type = 'reschedule'), hệ thống tự động tìm slot mới phù hợp và tạo makeup session.

**Database Tables Involved:**
- `teacher_request` (type='reschedule', status='pending' → 'approved')
- `session` (session gốc + tạo makeup session mới)
- `teaching_slot` (clone sang makeup session)
- `session_resource` (clone sang makeup session)
- `student_session` (tạo mới cho makeup session)
- `time_slot_template` (để tìm available slots)
- `resource` (để tìm available resources)

**Flow Steps:**

1. **Academic Staff vào "Pending Requests" → Filter "Teacher Reschedule"**
   - Xem danh sách teacher_request có type='reschedule', status='pending'

2. **Academic Staff click vào request để xem chi tiết**
   ```sql
   SELECT
     tr.id AS request_id,
     tr.teacher_id,
     tr.session_id AS original_session_id,
     tr.reason,
     tr.submitted_at,
     t.teacher_code,
     ua.full_name AS teacher_name,
     s.date AS original_date,
     s.start_time,
     s.end_time,
     c.class_code,
     c.class_name
   FROM teacher_request tr
   JOIN teacher t ON tr.teacher_id = t.id
   JOIN user_account ua ON t.user_account_id = ua.id
   JOIN session s ON tr.session_id = s.id
   JOIN class c ON s.class_id = c.id
   WHERE tr.id = :request_id
   ```

3. **Academic Staff click "Auto-Find Available Slots"**

4. **System tìm available time slots và resources**

   **Step 4.1: Query danh sách time slots khả dụng (trong 2 tuần tới)**
   ```sql
   SELECT
     tst.id AS time_slot_id,
     tst.day_of_week,
     tst.start_time,
     tst.end_time,
     tst.slot_name
   FROM time_slot_template tst
   WHERE tst.branch_id = :branch_id
     AND tst.is_active = TRUE
   ORDER BY tst.day_of_week, tst.start_time
   ```

   **Step 4.2: Lọc ra các slots mà teacher rảnh**
   - Generate danh sách ngày trong 2 tuần tới matching day_of_week
   - Với mỗi date + time_slot, check:
   ```sql
   -- Kiểm tra teacher có available không
   SELECT COUNT(*)
   FROM teaching_slot ts
   JOIN session s ON ts.session_id = s.id
   WHERE ts.teacher_id = :teacher_id
     AND s.date = :candidate_date
     AND s.status IN ('planned', 'ongoing')
     -- Check overlap time
     AND (
       (s.start_time, s.end_time) OVERLAPS (:slot_start, :slot_end)
     )
   -- Nếu COUNT = 0 → Teacher rảnh
   ```

   **Step 4.3: Với mỗi available slot, tìm available resources**
   ```sql
   SELECT
     r.id AS resource_id,
     r.resource_code,
     r.resource_name,
     r.resource_type,
     r.capacity
   FROM resource r
   WHERE r.branch_id = :branch_id
     AND r.resource_type IN ('ROOM', 'VIRTUAL')  -- Match với class modality
     AND r.capacity >= :class_enrolled_count
     AND NOT EXISTS (
       -- Check resource không bị conflict
       SELECT 1
       FROM session_resource sr
       JOIN session s ON sr.session_id = s.id
       WHERE sr.resource_id = r.id
         AND s.date = :candidate_date
         AND s.status IN ('planned', 'ongoing')
         AND (s.start_time, s.end_time) OVERLAPS (:slot_start, :slot_end)
     )
   ORDER BY r.capacity ASC
   LIMIT 5  -- Top 5 resources phù hợp nhất
   ```

5. **System hiển thị danh sách suggestions**
   - Format: "Thứ 3, 15/05/2025, 14:00-16:00, Phòng A301 (Capacity: 30)"
   - Sorted by date ASC (gần nhất trước)

6. **Academic Staff chọn slot + resource phù hợp**
   - Select: candidate_date, time_slot_id, resource_id

7. **Academic Staff click "Approve & Reschedule"**

8. **System tạo makeup session và sync students**
   ```sql
   BEGIN;

   -- 1. Cancel session gốc
   UPDATE session
   SET status = 'cancelled',
       teacher_note = 'Rescheduled by teacher request #' || :request_id
   WHERE id = :original_session_id;

   -- 2. Mark original student_session as excused
   UPDATE student_session
   SET attendance_status = 'excused',
       note = 'Original session cancelled, makeup session created'
   WHERE session_id = :original_session_id;

   -- 3. Tạo makeup session mới
   INSERT INTO session (
     class_id,
     course_session_id,
     date,
     start_time,
     end_time,
     type,
     status,
     teacher_note
   )
   SELECT
     s.class_id,
     s.course_session_id,
     :new_date,  -- Ngày mới
     tst.start_time,
     tst.end_time,
     'MAKEUP',  -- session_type_enum
     'planned',
     'Makeup session for original session #' || :original_session_id
   FROM session s
   JOIN time_slot_template tst ON tst.id = :new_time_slot_id
   WHERE s.id = :original_session_id
   RETURNING id INTO :makeup_session_id;

   -- 4. Clone teaching_slot (giữ nguyên teacher)
   INSERT INTO teaching_slot (session_id, teacher_id, skill, role, status)
   SELECT
     :makeup_session_id,
     teacher_id,
     skill,
     role,
     'scheduled'  -- teaching_slot_status_enum
   FROM teaching_slot
   WHERE session_id = :original_session_id;

   -- 5. Assign resource mới
   INSERT INTO session_resource (session_id, resource_type, resource_id)
   VALUES (
     :makeup_session_id,
     (SELECT resource_type FROM resource WHERE id = :new_resource_id),
     :new_resource_id
   );

   -- 6. Tạo student_session cho tất cả students đã enroll
   INSERT INTO student_session (
     student_id,
     session_id,
     attendance_status,
     is_makeup,
     note
   )
   SELECT
     ss.student_id,
     :makeup_session_id,
     'planned',
     TRUE,  -- Đánh dấu là makeup
     'Makeup for cancelled session #' || :original_session_id
   FROM student_session ss
   WHERE ss.session_id = :original_session_id;

   -- 7. Cập nhật teacher_request
   UPDATE teacher_request
   SET status = 'approved',
       decided_by = :academic_staff_user_id,
       decided_at = NOW(),
       new_date = :new_date,
       new_time_slot_id = :new_time_slot_id,
       new_resource_id = :new_resource_id,
       resolution = 'Makeup session created: #' || :makeup_session_id
   WHERE id = :request_id;

   COMMIT;
   ```

9. **System gửi notification**
   - **Tới teacher:** "Đơn đổi lịch của bạn đã được duyệt. Buổi học bù: [date] [time] tại [resource]"
   - **Tới all students:** "Buổi học ngày [original_date] đã bị hủy. Buổi học bù: [new_date] [time] tại [resource]"

**Lưu ý:**
- Chi tiết flow từ phía teacher xem **teacher-flows.md FLOW 7**
- Flow này là phần xử lý từ phía Academic Staff với auto-suggestion logic
- System suggest top 5 available slots trong 2 tuần tới
- Makeup session có type='MAKEUP' và is_makeup=TRUE trong student_session

**Result:**
- Teacher reschedule request được approve
- Session gốc bị cancel, tất cả students được mark excused
- Makeup session được tạo với slot/resource mới
- Tất cả students enrolled vào class đều được auto-add vào makeup session

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

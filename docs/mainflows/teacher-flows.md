# TEACHER MAIN FLOWS - CÁC LUỒNG NGHIỆP VỤ CHÍNH CỦA GIÁO VIÊN

## Tổng quan
File này mô tả các luồng nghiệp vụ chính mà Giáo viên (Teacher) thực hiện trong hệ thống EMS, bao gồm cách hệ thống join dữ liệu và các bước tương tác.

---

## FLOW 1: Xem Lịch Dạy (Teacher Timetable View)

**Actors involved:** Teacher, System  
**Description:** Giáo viên đăng nhập và xem lịch giảng dạy của mình (danh sách các buổi học được phân công).

**Database Tables Involved:**
- `user_account` → `teacher` → `teaching_slot` → `session` → `class` → `course` → `time_slot_template`
- `session_resource` (để xem phòng/Zoom)

**Flow Steps:**

1. **Teacher đăng nhập hệ thống**
   - Input: email/password
   - System: Xác thực → lấy `user_account.id`
   - System: JOIN `teacher` table → lấy `teacher_id`

2. **Teacher click "Lịch dạy của tôi"**
   - System thực hiện query:
   ```
   SELECT 
     session.id,
     session.date,
     session.start_time,
     session.end_time,
     class.name AS class_name,
     course.name AS course_name,
     teaching_slot.skill
   FROM teaching_slot
   JOIN session ON teaching_slot.session_id = session.id
   JOIN class ON session.class_id = class.id
   JOIN course ON class.course_id = course.id
   WHERE teaching_slot.teacher_id = :teacher_id
     AND session.status IN ('planned', 'ongoing')
     AND session.date >= CURRENT_DATE
   ORDER BY session.date, session.start_time
   ```

3. **System hiển thị danh sách buổi học**
   - Mỗi buổi học hiển thị:
     - Ngày giờ (date + start_time + end_time)
     - Tên lớp (class.name)
     - Tên khóa học (course.name)
     - Kỹ năng dạy (teaching_slot.skill)
     - Địa điểm: JOIN `session_resource` để lấy phòng/Zoom

4. **Teacher click vào một buổi học để xem chi tiết**
   - System load thêm thông tin:
     - Nội dung buổi học: JOIN `course_session` → lấy topic, student_task
     - Tài liệu: JOIN `course_material` WHERE course_session_id
     - Danh sách học viên: sẽ load ở flow tiếp theo

**Result:** Teacher thấy rõ lịch dạy, có thể chuẩn bị bài giảng trước.

---

## FLOW 2: Điểm Danh Học Viên (Teacher Attendance Recording)

**Actors involved:** Teacher, System
**Description:** Giáo viên mở danh sách học viên của một buổi học và cập nhật trạng thái điểm danh cho từng học viên. Teacher có 2 cách để vào màn hình điểm danh.

**Database Tables Involved:**
- `session` → `student_session` → `student` → `user_account`
- `enrollment` (để verify học viên có đăng ký lớp không)
- `teaching_slot` (để lấy danh sách các lớp mà teacher dạy, bao gồm cả lớp dạy thay)
- `class` (thông tin lớp học)

**Flow Steps:**

### Cách 1: Từ Sidebar "Take Attendance"

1. **Teacher vào sidebar → Click "Take Attendance"**

2. **System load danh sách các lớp mà teacher dạy**
   - System thực hiện query:
   ```
   SELECT DISTINCT
     c.id AS class_id,
     c.code AS class_code,
     c.name AS class_name,
     co.name AS course_name,
     b.name AS branch_name,
     -- Check xem hôm nay có session nào không
     EXISTS (
       SELECT 1
       FROM session s
       JOIN teaching_slot ts ON ts.session_id = s.id
       WHERE s.class_id = c.id
         AND ts.teacher_id = :teacher_id
         AND s.date = CURRENT_DATE
         AND s.status IN ('planned', 'ongoing')
     ) AS has_session_today
   FROM teaching_slot ts
   JOIN session s ON ts.session_id = s.id
   JOIN class c ON s.class_id = c.id
   JOIN course co ON c.course_id = co.id
   JOIN branch b ON c.branch_id = b.id
   WHERE ts.teacher_id = :teacher_id
     AND (
       ts.status = 'scheduled'
       OR (ts.status = 'substituted' AND s.date >= CURRENT_DATE)
     )
     AND c.status IN ('scheduled', 'ongoing')
   ORDER BY has_session_today DESC, c.name
   ```

   **Lưu ý:** Query này list ra:
   - Các lớp mà teacher dạy cố định (teaching_slot.status = 'scheduled')
   - Các lớp mà teacher dạy thay (teaching_slot.status = 'substituted' và buổi học chưa diễn ra)

3. **Teacher chọn lớp → System hiển thị danh sách sessions của lớp đó**
   - Filter: chỉ hiển thị session của ngày hôm nay (hoặc tuần này)

4. **Teacher chọn session cần điểm danh** → **Nhảy tới màn hình điểm danh (bước 6)**

### Cách 2: Từ My Schedule (Timetable)

5. **Teacher vào My Schedule → Chọn session trong timetable**
   - System hiển thị Session Detail popup, bao gồm:
     - Nội dung buổi học (course_session.topic, student_task)
     - Tài liệu (course_material)
     - **Thông tin lớp** (class.code, class.name) → **Có button/link "Đi tới điểm danh"**

5a. **Teacher click vào mã lớp hoặc button "Đi tới điểm danh"** → **Nhảy tới màn hình điểm danh**

### Màn Hình Điểm Danh (Chung cho cả 2 cách)

6. **System load danh sách học viên của session**
   - System thực hiện query:
   ```
   SELECT
     student_session.id AS student_session_id,
     student.id AS student_id,
     student.student_code,
     user_account.full_name,
     student_session.attendance_status,
     student_session.is_makeup,
     student_session.homework_status,
     student_session.note
   FROM student_session
   JOIN student ON student_session.student_id = student.id
   JOIN user_account ON student.user_account_id = user_account.id
   WHERE student_session.session_id = :session_id
   ORDER BY student.student_code
   ```

7. **System hiển thị danh sách học viên với các cột:**
   - **Mã học viên** (student_code)
   - **Họ tên** (full_name)
   - **Trạng thái** (attendance_status):
     - Badge màu xanh: "Học bù" nếu `is_makeup = TRUE`
     - Badge màu vàng: "Nghỉ có phép" nếu `attendance_status = 'excused'`
   - **Điểm danh** (dropdown):
     - `present`: Có mặt
     - `absent`: Vắng mặt
     - `late`: Đi muộn
     - `excused`: Nghỉ có phép (read-only, đã được duyệt trước)
     - `remote`: Học từ xa (cho hybrid)
   - **Hoàn thành bài tập** (checkbox/dropdown):
     - `completed`: Đã làm bài tập
     - `incomplete`: Chưa làm bài tập
     - `no_homework`: Không có bài tập
   - **Ghi chú** (textarea): Teacher có thể ghi note riêng cho từng học viên

8. **Teacher cập nhật điểm danh và bài tập**
   - Tick có mặt/vắng mặt/đi muộn cho từng học viên
   - Tick hoàn thành bài tập (nếu có bài tập)
   - Ghi note (optional)

9. **Teacher click "Lưu điểm danh"**
   - System thực hiện UPDATE hàng loạt:
   ```
   UPDATE student_session
   SET
     attendance_status = :new_status,
     homework_status = :homework_status,
     note = :note,
     recorded_at = NOW()
   WHERE id = :student_session_id
   ```
   - System ghi nhận timestamp (recorded_at)

10. **System validation**
    - Không cho phép điểm danh trước ngày session.date
    - Sau khi lock (T giờ sau buổi học), cần approval từ Admin/Manager để sửa

**Result:**
- Trạng thái điểm danh được lưu vào `student_session.attendance_status`
- Trạng thái bài tập được lưu vào `student_session.homework_status`
- Dữ liệu sẵn sàng cho báo cáo
- Teacher có thể tiếp tục vào màn hình báo cáo buổi học (FLOW 3) ngay sau khi điểm danh

---

## FLOW 3: Báo Cáo Buổi Học (Teacher Session Report)

**Actors involved:** Teacher, System
**Description:** Sau khi điểm danh, Teacher báo cáo nội dung đã dạy và ghi chú về buổi học. Màn hình báo cáo nằm CÙNG với màn hình điểm danh.

**Database Tables Involved:**
- `session` → `course_session`
- `student_session` (để tính sĩ số)

**Flow Steps:**

1. **Teacher ở màn hình điểm danh (FLOW 2)**
   - Sau khi điểm danh xong, Teacher scroll xuống phần "Báo cáo buổi học"
   - Hoặc click tab "Báo cáo" trong cùng màn hình

2. **System hiển thị form báo cáo (ngay trong màn hình điểm danh)**
   - Tự động điền:
     - Nội dung kế hoạch: `course_session.topic` (chủ đề dự kiến)
     - Nhiệm vụ học viên: `course_session.student_task`
     - **Sĩ số:** System tự động tính từ attendance:
       ```
       SELECT
         COUNT(*) FILTER (WHERE attendance_status IN ('present', 'late', 'remote')) AS present_count,
         COUNT(*) FILTER (WHERE attendance_status = 'absent') AS absent_count,
         COUNT(*) AS total_students
       FROM student_session
       WHERE session_id = :session_id
       ```
   - Teacher điền:
     - **Nội dung thực tế đã dạy** (textarea): có thể khác kế hoạch
     - **Ghi chú về buổi học** (textarea): học viên phản ứng thế nào, có vấn đề gì, tiến độ như thế nào

3. **Teacher click "Lưu và Hoàn thành buổi học"**
   - System thực hiện UPDATE:
   ```
   UPDATE session
   SET
     status = 'done',
     teacher_note = :note,
     updated_at = NOW()
   WHERE id = :session_id
   ```

4. **System validation**
   - **Điều kiện để mark session.status = 'done':**
     - Buổi học đã kết thúc (session.date + session.end_time <= NOW())
     - Đã điểm danh đủ (tất cả student_session.attendance_status != 'planned')
   - Không cho phép submit report trước ngày session.date
   - Nếu đã submit, vẫn có thể edit trong vòng T giờ (trước khi lock)

5. **System notification**
   - Hiển thị success message: "Báo cáo buổi học đã được lưu. Buổi học đã hoàn thành."

**Result:**
- Buổi học được đánh dấu `session.status = 'done'`
- Ghi lại nội dung thực tế trong `session.teacher_note`
- Dữ liệu phục vụ QA tracking và báo cáo tiến độ
- Teacher hoàn tất tất cả công việc trong 1 màn hình duy nhất (điểm danh + báo cáo)

---

## FLOW 4: Nhập Điểm Đánh Giá (Teacher Score Entry)

**Actors involved:** Teacher, System
**Description:** Teacher nhập điểm cho các bài kiểm tra (assessment) của học viên thông qua sidebar Assessments.

**Database Tables Involved:**
- `assessment` → `score` ← `student`
- `class` → `enrollment` (để lấy danh sách học viên của lớp)
- `teaching_slot` → `session` → `class` (để lấy danh sách lớp mà teacher dạy)

**Flow Steps:**

1. **Teacher vào Sidebar → Click "Assessments"**

2. **System load danh sách các lớp mà teacher dạy**
   - System thực hiện query:
   ```
   SELECT DISTINCT
     c.id AS class_id,
     c.code AS class_code,
     c.name AS class_name,
     co.name AS course_name,
     COUNT(DISTINCT a.id) AS assessment_count
   FROM teaching_slot ts
   JOIN session s ON ts.session_id = s.id
   JOIN class c ON s.class_id = c.id
   JOIN course co ON c.course_id = co.id
   LEFT JOIN assessment a ON a.class_id = c.id
   WHERE ts.teacher_id = :teacher_id
     AND c.status IN ('scheduled', 'ongoing', 'completed')
   GROUP BY c.id, co.id
   ORDER BY c.name
   ```

3. **Teacher chọn lớp**
   - Click vào lớp cần nhập điểm

4. **System load danh sách assessments của lớp đó**
   - System thực hiện query:
   ```
   SELECT
     a.id AS assessment_id,
     a.name AS assessment_name,
     a.kind,
     a.max_score,
     a.weight,
     -- Tính số học viên đã được chấm điểm
     COUNT(DISTINCT s.student_id) AS graded_count,
     -- Tổng số học viên
     (
       SELECT COUNT(*)
       FROM enrollment e
       WHERE e.class_id = :class_id
         AND e.status = 'enrolled'
     ) AS total_students
   FROM assessment a
   LEFT JOIN score s ON s.assessment_id = a.id
   WHERE a.class_id = :class_id
   GROUP BY a.id
   ORDER BY a.created_at
   ```

5. **Teacher chọn một assessment (vd: Quiz 1, Midterm, Final)**
   - Click vào assessment cần nhập điểm

6. **System load danh sách học viên**
   - System thực hiện query:
   ```
   SELECT
     student.id AS student_id,
     student.student_code,
     user_account.full_name,
     score.score AS current_score,
     score.feedback AS current_feedback,
     score.graded_at
   FROM enrollment
   JOIN student ON enrollment.student_id = student.id
   JOIN user_account ON student.user_account_id = user_account.id
   LEFT JOIN score ON (
     score.student_id = student.id
     AND score.assessment_id = :assessment_id
   )
   WHERE enrollment.class_id = :class_id
     AND enrollment.status = 'enrolled'
   ORDER BY student.student_code
   ```

7. **System hiển thị bảng nhập điểm**
   - Mỗi học viên có:
     - **Mã học viên** (student_code)
     - **Họ tên** (full_name)
     - **Điểm hiện tại** (nếu đã nhập): hiển thị score/max_score (vd: 85/100)
     - **Cột nhập điểm** (number input): nhập điểm mới
     - **Cột nhập feedback** (textarea): nhập nhận xét
     - **Ngày chấm** (graded_at): nếu đã chấm

8. **Teacher nhập điểm cho từng học viên**
   - Nhập điểm (0 - assessment.max_score)
   - Nhập feedback (optional): "Bài làm tốt, cần cải thiện phần grammar"

9. **Teacher click "Lưu điểm"**
   - System thực hiện UPSERT (INSERT or UPDATE):
   ```
   INSERT INTO score (assessment_id, student_id, score, feedback, graded_by, graded_at)
   VALUES (:assessment_id, :student_id, :score, :feedback, :teacher_id, NOW())
   ON CONFLICT (assessment_id, student_id) DO UPDATE
   SET
     score = EXCLUDED.score,
     feedback = EXCLUDED.feedback,
     graded_by = :teacher_id,
     graded_at = NOW()
   ```

10. **System validation**
    - Điểm không được vượt quá `assessment.max_score`
    - Điểm phải >= 0
    - Tổng weight của các assessment không quá 100%

**Alternative Flow: Import từ Excel**
- Teacher upload file CSV (student_code, score, feedback)
- System parse và validate
- System thực hiện bulk insert/update

**Result:**
- Điểm được lưu vào bảng `score`
- Học viên và Manager có thể xem báo cáo điểm
- Teacher có thể track tiến độ chấm điểm (graded_count/total_students)

---

## FLOW 5: Xem Lịch Dạy Cố Định (Teacher View Availability Schedule) - READ-ONLY

**Actors involved:** Teacher, System
**Description:** Giáo viên XEM lịch dạy cố định của mình (teacher_availability). Lịch này được Academic Staff quản lý và chỉnh sửa, teacher KHÔNG có quyền tự chỉnh sửa.

**Database Tables Involved:**
- `teacher_availability` (lịch dạy cố định hàng tuần, được Academic Staff quản lý)
- `time_slot_template` (khung giờ)

**Flow Steps:**

1. **Teacher vào sidebar → Click "My Availability"**

2. **System load lịch dạy cố định của teacher**
   - System thực hiện query:
   ```
   SELECT
     ta.teacher_id,
     ta.time_slot_template_id,
     ta.day_of_week,
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

3. **System hiển thị lịch dạy cố định dạng lịch tuần**
   - Hiển thị theo dạng calendar grid (7 ngày trong tuần)
   - Mỗi ô hiển thị:
     - Khung giờ (time_slot_template.name, start_time - end_time)
     - Branch (nếu có nhiều branch)
     - Note (nếu có)
     - Effective date (ngày bắt đầu áp dụng)

4. **Teacher chỉ có thể XEM, KHÔNG thể chỉnh sửa**
   - Không có button "Add", "Edit", "Delete"
   - Có note: "⚠️ Lịch dạy cố định được quản lý bởi Giáo vụ. Nếu cần thay đổi, vui lòng liên hệ Giáo vụ."

5. **Teacher có thể xem lịch sử thay đổi (optional)**
   - Click "Lịch sử thay đổi"
   - System hiển thị các phiên bản availability cũ (với effective_date khác nhau)

**Result:**
- Teacher biết được lịch dạy cố định của mình (các ngày và khung giờ được phép dạy)
- Teacher hiểu rằng mọi thay đổi phải qua Academic Staff
- System sử dụng teacher_availability để:
  - Gợi ý teacher khi phân công lớp mới
  - Kiểm tra conflict khi assign teacher
  - Tìm teacher thay thế phù hợp

**Lưu ý quan trọng:**
- Teacher availability bây giờ là **lịch dạy cố định** (fixed teaching schedule), không phải lịch rảnh
- Academic Staff define availability khi teacher bắt đầu làm việc hoặc khi cần thay đổi
- Không còn khái niệm "OT registration" nữa - tất cả đều nằm trong teacher_availability
- Teacher chỉ có thể GỬI REQUEST để xin thay đổi lịch (xem FLOW 6, FLOW 7)

---

## FLOW 6: Gửi Yêu Cầu Dạy Thay (Teacher Swap Request)

**Actors involved:** Teacher, Academic Staff, Replacement Teacher, System
**Description:** Giáo viên gửi yêu cầu tìm người dạy thay cho một buổi học. Academic Staff chọn teacher thay thế, gửi confirm, và replacement teacher xác nhận.

**Database Tables Involved:**
- `teacher_request` (request_type='swap', replacement_teacher_id, status)
- `session` → `teaching_slot`
- `teacher_skill`, `teacher_availability` (để tìm replacement teacher)

**Flow Steps:**

1. **Teacher vào Sidebar → "Requests" → Click "Gửi yêu cầu mới"**

2. **Teacher điền form yêu cầu**
   - **Chọn loại request:** "Dạy thay" (swap)
   - **Chọn ngày:** Ngày cần nghỉ
   - **System hiển thị danh sách sessions của teacher vào ngày đó:**
     ```
     SELECT
       s.id AS session_id,
       s.date,
       s.start_time,
       s.end_time,
       c.name AS class_name,
       cs.topic
     FROM teaching_slot ts
     JOIN session s ON ts.session_id = s.id
     JOIN class c ON s.class_id = c.id
     LEFT JOIN course_session cs ON s.course_session_id = cs.id
     WHERE ts.teacher_id = :teacher_id
       AND s.date = :selected_date
       AND s.status = 'planned'
     ORDER BY s.start_time
     ```
   - **Teacher chọn session (buổi học cần người dạy thay)**
   - **Nhập lý do:** "Có việc gia đình khẩn cấp"
   - **Optional: Chọn teacher muốn dạy thay** (nếu teacher biết ai có thể thay)

3. **Teacher click "Gửi yêu cầu"**
   - System thực hiện INSERT:
   ```
   INSERT INTO teacher_request
   (teacher_id, session_id, request_type, reason, status, submitted_at, replacement_teacher_id)
   VALUES (:teacher_id, :session_id, 'swap', :reason, 'pending', NOW(), :suggested_replacement_teacher_id)
   ```

4. **System gửi notification tới Academic Staff**
   - Email/SMS: "Giáo viên [Tên] yêu cầu tìm người dạy thay buổi [Session] vào [Ngày]"

5. **Academic Staff nhận request và xử lý** (chi tiết xem academic-flows.md FLOW 9)

   **5a. Academic Staff vào "Teacher Requests" → Detail của request**

   **5b. System list ra toàn bộ giáo viên phù hợp:**
   - **Sort theo ưu tiên:**
     - **Skill match:** Teacher có skill phù hợp với session.course_session.skill_set
     - **Level:** Teacher có level phù hợp để dạy
     - **Availability:** Teacher có lịch rảnh (teacher_availability match với session time)
     - **No conflict:** Teacher không có lớp trùng giờ

   Query:
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

   **5c. Academic Staff chọn replacement teacher → Click "Gửi xác nhận"**

   **5d. System UPDATE request:**
   ```
   UPDATE teacher_request
   SET
     status = 'waiting_confirm',
     replacement_teacher_id = :replacement_teacher_id,
     decided_by = :academic_staff_id,
     decided_at = NOW()
   WHERE id = :request_id
   ```

6. **System gửi notification tới Replacement Teacher**
   - "Bạn được mời dạy thay buổi [Session] vào [Ngày]. Vui lòng xác nhận."

7. **Replacement Teacher vào "Requests" → Xem request → Confirm**
   - Click "Chấp nhận" hoặc "Từ chối"

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
   - Academic Staff quay lại bước 5 để chọn teacher khác

**Trường hợp không tìm thấy teacher phù hợp:**
   - Academic Staff có thể:
     - Tạo thêm teacher_availability tạm thời cho ai đó
     - Reschedule session (xem FLOW 7)
     - Reject request của teacher gốc

**Result:**
- Teacher_request.status = 'confirmed'
- Teaching_slot cũ: status = 'on_leave'
- Teaching_slot mới: status = 'substituted', teacher_id = replacement_teacher_id
- Audit trail đầy đủ

---

## FLOW 7: Gửi Yêu Cầu Đổi Lịch (Teacher Reschedule Request)

**Actors involved:** Teacher, Academic Staff, System
**Description:** Giáo viên gửi yêu cầu đổi lịch một buổi học sang ngày/giờ khác. Hệ thống tìm time slot và resource khả dụng.

**Database Tables Involved:**
- `teacher_request` (request_type='reschedule', new_date, new_time_slot_id, new_resource_id)
- `session` → `class` → `enrollment` (để tính số lượng students)
- `resource` (để tìm room/zoom phù hợp)
- `time_slot_template` (để tìm khung giờ available)
- `student_session` (để update cho students)

**Flow Steps:**

1. **Teacher vào Sidebar → "Requests" → Click "Gửi yêu cầu mới"**

2. **Teacher điền form yêu cầu**
   - **Chọn loại request:** "Đổi buổi học" (reschedule)
   - **Chọn ngày:** Ngày cần đổi lịch
   - **System hiển thị danh sách sessions của teacher vào ngày đó (chưa diễn ra):**
     ```
     SELECT
       s.id AS session_id,
       s.date,
       s.start_time,
       s.end_time,
       c.name AS class_name,
       c.modality,
       cs.topic,
       COUNT(DISTINCT ss.student_id) AS student_count
     FROM teaching_slot ts
     JOIN session s ON ts.session_id = s.id
     JOIN class c ON s.class_id = c.id
     LEFT JOIN course_session cs ON s.course_session_id = cs.id
     LEFT JOIN student_session ss ON ss.session_id = s.id
     WHERE ts.teacher_id = :teacher_id
       AND s.date = :selected_date
       AND s.status = 'planned'
       AND s.date > CURRENT_DATE
     GROUP BY s.id, c.id, cs.id
     ORDER BY s.start_time
     ```
   - **Teacher chọn session cần đổi lịch**

3. **System check modality và student count:**
   ```
   SELECT
     c.modality,
     COUNT(DISTINCT ss.student_id) AS student_count
   FROM session s
   JOIN class c ON s.class_id = c.id
   LEFT JOIN student_session ss ON ss.session_id = s.id
   WHERE s.id = :session_id
   GROUP BY c.modality
   ```

4. **System tìm resources phù hợp (based on modality + capacity):**
   - **Nếu OFFLINE:** Tìm ROOM có capacity >= student_count
   - **Nếu ONLINE:** Tìm VIRTUAL (Zoom)
   - **Nếu HYBRID:** Cần cả ROOM và VIRTUAL

   Query:
   ```
   SELECT
     r.id AS resource_id,
     r.name,
     r.resource_type,
     r.capacity
   FROM resource r
   WHERE r.branch_id = :branch_id
     AND r.status = 'active'
     AND (
       (:modality = 'OFFLINE' AND r.resource_type = 'ROOM' AND r.capacity >= :student_count)
       OR (:modality = 'ONLINE' AND r.resource_type = 'VIRTUAL')
       OR (:modality = 'HYBRID' AND r.resource_type IN ('ROOM', 'VIRTUAL'))
     )
   ORDER BY r.capacity DESC
   ```

5. **Teacher chọn resource và ngày mới:**
   - Chọn resource (room hoặc zoom)
   - Chọn ngày mới (new_date)

6. **System tìm time slots available cho resource đó vào ngày mới:**
   ```
   SELECT
     tst.id AS time_slot_id,
     tst.name,
     tst.start_time,
     tst.end_time,
     -- Check xem time slot này có conflict với resource không
     NOT EXISTS (
       SELECT 1
       FROM session_resource sr
       JOIN session s2 ON sr.session_id = s2.id
       WHERE sr.resource_id = :selected_resource_id
         AND s2.date = :new_date
         AND s2.status IN ('planned', 'ongoing')
         AND (s2.start_time, s2.end_time) OVERLAPS (tst.start_time, tst.end_time)
     ) AS is_available
   FROM time_slot_template tst
   WHERE tst.branch_id = :branch_id
   HAVING is_available = TRUE
   ORDER BY tst.start_time
   ```

7. **Teacher chọn time slot → Nhập lý do → Click "Gửi yêu cầu"**
   - System INSERT:
   ```
   INSERT INTO teacher_request
   (teacher_id, session_id, request_type, reason, status, submitted_at,
    new_date, new_time_slot_id, new_resource_id)
   VALUES
   (:teacher_id, :session_id, 'reschedule', :reason, 'pending', NOW(),
    :new_date, :new_time_slot_id, :new_resource_id)
   ```

8. **System gửi notification tới Academic Staff**
   - "Giáo viên [Tên] yêu cầu đổi lịch buổi [Session] từ [Old Date] sang [New Date]"

9. **Academic Staff review và approve** (chi tiết xem academic-flows.md)

   **9a. Academic Staff kiểm tra:**
   - Resource available không
   - Teacher có conflict không (trong lịch dạy mới)
   - Xác nhận thay đổi hợp lý

   **9b. Academic Staff approve:**
   ```
   BEGIN;

   -- 1. Update teacher_request
   UPDATE teacher_request
   SET
     status = 'approved',
     decided_by = :academic_staff_id,
     decided_at = NOW()
   WHERE id = :request_id;

   -- 2. Cancel session cũ
   UPDATE session
   SET
     status = 'cancelled',
     teacher_note = 'Rescheduled to ' || :new_date || ' due to teacher request'
   WHERE id = :old_session_id;

   -- 3. Create session mới với type='makeup'
   INSERT INTO session (
     class_id, course_session_id, date, start_time, end_time, type, status
   )
   SELECT
     class_id,
     course_session_id,
     :new_date,
     tst.start_time,
     tst.end_time,
     'makeup',
     'planned'
   FROM session s
   JOIN time_slot_template tst ON tst.id = :new_time_slot_id
   WHERE s.id = :old_session_id
   RETURNING id INTO :new_session_id;

   -- 4. Copy teaching_slot sang session mới
   INSERT INTO teaching_slot (session_id, teacher_id, skill, role, status)
   SELECT :new_session_id, teacher_id, skill, role, 'scheduled'
   FROM teaching_slot
   WHERE session_id = :old_session_id;

   -- 5. Assign resource cho session mới
   INSERT INTO session_resource (session_id, resource_type, resource_id)
   VALUES (:new_session_id, :resource_type, :new_resource_id);

   -- 6. Update student_session (chuyển từ old sang new session)
   UPDATE student_session
   SET session_id = :new_session_id
   WHERE session_id = :old_session_id;

   COMMIT;
   ```

10. **System gửi notifications:**
    - Tới Teacher: "Yêu cầu đổi lịch đã được duyệt. Buổi học chuyển sang [New Date] [New Time]."
    - Tới Students: "Thông báo: Buổi học [Class] ngày [Old Date] đã được chuyển sang [New Date] [New Time] tại [Resource]."
    - Email hàng loạt tới tất cả students

**Trường hợp không tìm thấy resource phù hợp:**
   - System hiển thị message: "Không tìm thấy phòng/zoom phù hợp. Vui lòng liên hệ Giáo vụ để tạo thêm link Zoom."
   - Teacher có thể:
     - Chọn ngày khác
     - Gọi trực tiếp giáo vụ để xin tạo Zoom link mới

**Result:**
- Session cũ: status = 'cancelled'
- Session mới: type = 'makeup', với ngày giờ và resource mới
- Student_session được update tự động
- Emails gửi tới tất cả students
- Audit trail đầy đủ

---

## FLOW 8: Xem Feedback Từ Học Viên (Teacher View Student Feedback)

**Actors involved:** Teacher, System  
**Description:** Giáo viên xem đánh giá (rating, comment) từ học viên sau buổi học.

**Database Tables Involved:**
- `student_feedback` → `session` → `student`

**Flow Steps:**

1. **Teacher vào menu "Feedback từ học viên"**
   - System hiển thị danh sách feedback

2. **System load feedback**
   - System thực hiện query:
   ```
   SELECT 
     student_feedback.id,
     session.date,
     class.name AS class_name,
     student_feedback.rating,
     student_feedback.comment,
     student_feedback.submitted_at
   FROM student_feedback
   JOIN session ON student_feedback.session_id = session.id
   JOIN class ON session.class_id = class.id
   JOIN teaching_slot ON (
     teaching_slot.session_id = session.id 
     AND teaching_slot.teacher_id = :teacher_id
   )
   WHERE student_feedback.rating IS NOT NULL
   ORDER BY student_feedback.submitted_at DESC
   LIMIT 50
   ```

3. **System hiển thị feedback**
   - Mỗi feedback gồm:
     - Ngày buổi học (session.date)
     - Tên lớp (class.name)
     - Rating (1-5 sao)
     - Comment (nếu có)
   - Teacher có thể filter theo:
     - Lớp cụ thể
     - Rating (chỉ xem rating thấp để cải thiện)
     - Thời gian (tuần này, tháng này)

4. **Teacher xem tổng hợp**
   - Điểm rating trung bình của tất cả buổi học
   - Phân bố rating (bao nhiêu % học viên cho 5 sao, 4 sao, ...)

**Result:** 
- Teacher biết học viên đánh giá mình như thế nào
- Teacher có thể cải thiện phương pháp giảng dạy dựa trên feedback

---

## FLOW 9: Xem Phase Feedback Từ Học Viên (Teacher View Phase Feedback)

**Actors involved:** Teacher, System
**Description:** Teacher xem feedback của học viên cho từng phase (được hệ thống tự động tạo khi kết thúc phase). Feedback này giúp teacher cải thiện chất lượng giảng dạy.

**Database Tables Involved:**
- `student_feedback` → `course_phase` → `session` → `class`
- `student` → `user_account`

**Flow Steps:**

1. **Teacher vào Sidebar → "My Classes"**

2. **System load danh sách các lớp mà teacher dạy**
   - System thực hiện query:
   ```
   SELECT DISTINCT
     c.id AS class_id,
     c.code AS class_code,
     c.name AS class_name,
     co.name AS course_name,
     c.status
   FROM teaching_slot ts
   JOIN session s ON ts.session_id = s.id
   JOIN class c ON s.class_id = c.id
   JOIN course co ON c.course_id = co.id
   WHERE ts.teacher_id = :teacher_id
     AND c.status IN ('scheduled', 'ongoing', 'completed')
   GROUP BY c.id, co.id
   ORDER BY c.status, c.name
   ```

3. **Teacher chọn một lớp → Click "Phase Feedback"**

4. **System load danh sách các phases của course và feedback count**
   - System thực hiện query:
   ```
   SELECT
     cp.id AS phase_id,
     cp.phase_number,
     cp.name AS phase_name,
     cp.description,
     -- Tính số feedback đã nhận
     COUNT(DISTINCT sf.id) AS feedback_count,
     -- Tổng số students
     (
       SELECT COUNT(*)
       FROM enrollment e
       WHERE e.class_id = :class_id
         AND e.status IN ('enrolled', 'completed')
     ) AS total_students,
     -- Điểm rating trung bình của phase này
     AVG(sf.rating) AS avg_rating,
     -- Session cuối cùng của phase
     (
       SELECT MAX(s.date)
       FROM session s
       JOIN course_session cs ON s.course_session_id = cs.id
       WHERE cs.phase_id = cp.id
         AND s.class_id = :class_id
         AND s.status = 'done'
     ) AS last_session_date
   FROM course_phase cp
   LEFT JOIN student_feedback sf ON sf.phase_id = cp.id
   WHERE cp.course_id = (
     SELECT course_id FROM class WHERE id = :class_id
   )
   GROUP BY cp.id
   ORDER BY cp.phase_number
   ```

5. **System hiển thị danh sách phases**
   - Mỗi phase hiển thị:
     - **Phase number & name** (vd: "Phase 1: Foundation")
     - **Feedback progress:** "15/20 students đã feedback"
     - **Avg rating:** ⭐⭐⭐⭐☆ (4.2/5.0)
     - **Last session date:** Ngày buổi học cuối của phase

6. **Teacher chọn một phase → Xem chi tiết feedback**

7. **System load danh sách feedback của students cho phase đó**
   - System thực hiện query:
   ```
   SELECT
     sf.id AS feedback_id,
     s.student_code,
     u.full_name,
     sf.rating,
     sf.comment,
     sf.submitted_at,
     sf.is_feedback,
     sess.date AS session_date,
     cs.topic AS session_topic
   FROM student_feedback sf
   JOIN student s ON sf.student_id = s.id
   JOIN user_account u ON s.user_account_id = u.id
   JOIN session sess ON sf.session_id = sess.id
   LEFT JOIN course_session cs ON sess.course_session_id = cs.id
   WHERE sf.phase_id = :phase_id
     AND sess.class_id = :class_id
     AND sf.is_feedback = TRUE
   ORDER BY sf.submitted_at DESC
   ```

8. **System hiển thị feedback detail**
   - **Thống kê tổng quan:**
     - Tổng số feedback: 15/20
     - Điểm trung bình: 4.2/5.0
     - Phân bố rating:
       - 5 sao: 8 students (53%)
       - 4 sao: 5 students (33%)
       - 3 sao: 2 students (13%)
       - 2 sao: 0 students
       - 1 sao: 0 students

   - **Danh sách feedback từng student:**
     - **Mã học viên** (student_code)
     - **Họ tên** (full_name)
     - **Rating** (1-5 sao)
     - **Comment** (nếu có):
       - "Giáo viên dạy rất nhiệt tình và dễ hiểu"
       - "Cần thêm thời gian thực hành"
     - **Session date** (ngày feedback - là session cuối của phase)
     - **Ngày submit** (submitted_at)

9. **Teacher có thể filter/sort:**
   - Filter theo rating (chỉ xem rating thấp để cải thiện)
   - Sort theo ngày submit
   - Search theo tên học viên

10. **Teacher export feedback (optional):**
    - Click "Export to CSV"
    - System generate file CSV với tất cả feedback của phase

**Result:**
- Teacher biết học viên đánh giá phase như thế nào
- Teacher có insight để cải thiện phương pháp giảng dạy cho các phase tiếp theo
- Teacher track được tiến độ feedback (bao nhiêu students đã feedback)
- QA team có thể xem data này để đánh giá quality

**Lưu ý quan trọng:**
- Feedback được hệ thống **Tự ĐỘNG TẠO** khi kết thúc phase (xem system-flows.md FLOW 11)
- System detect session cuối của phase → auto-generate student_feedback records
- Student chỉ cần điền rating và comment vào records đã được tạo sẵn
- Teacher KHÔNG thể xem feedback của students chưa submit (is_feedback = FALSE)

---

## FLOW 10: Xem Lịch Sử Thay Đổi Lịch Dạy (Teacher View Schedule Changes)

**Actors involved:** Teacher, System  
**Description:** Giáo viên xem lịch sử các buổi học bị thay đổi (reschedule, cancel, substitute).

**Database Tables Involved:**
- `session` (có trường teacher_note ghi log thay đổi)
- `teacher_request` (lịch sử request leave/reschedule)

**Flow Steps:**

1. **Teacher vào menu "Lịch sử thay đổi"**

2. **System load lịch sử**
   - System thực hiện query:
   ```
   SELECT 
     session.id,
     session.date,
     session.start_time,
     session.end_time,
     session.status,
     class.name AS class_name,
     session.teacher_note
   FROM session
   JOIN class ON session.class_id = class.id
   JOIN teaching_slot ON teaching_slot.session_id = session.id
   WHERE teaching_slot.teacher_id = :teacher_id
     AND (
       session.status IN ('cancelled', 'rescheduled')
       OR session.teacher_note LIKE '%reschedule%'
       OR session.teacher_note LIKE '%substitute%'
     )
   ORDER BY session.date DESC
   LIMIT 50
   ```

3. **System hiển thị danh sách**
   - Mỗi thay đổi gồm:
     - Ngày giờ ban đầu
     - Loại thay đổi (cancel/reschedule/substitute)
     - Lý do (từ teacher_note)
     - Người phê duyệt (từ teacher_request.decided_by)

**Result:** 
- Teacher có cái nhìn rõ ràng về lịch dạy đã thay đổi
- Giúp Teacher theo dõi và xác nhận thay đổi

---

## Tóm Tắt Các Flow Chính Của Teacher

| Flow | Mô Tả | Bảng Chính Liên Quan |
|------|-------|---------------------|
| 1. Xem Lịch Dạy | Load danh sách session được phân công | teaching_slot → session → class → course |
| 2. Điểm Danh | Cập nhật attendance_status cho từng học viên | student_session → student → user_account |
| 3. Báo Cáo Buổi Học | Ghi nhận nội dung đã dạy, cập nhật session.status='done' | session → course_session |
| 4. Nhập Điểm | Nhập score cho assessment | score ← assessment, student |
| 5. Đăng Ký Lịch Rảnh/OT | Khai báo availability thường xuyên và OT | teacher_availability, teacher_availability_override |
| 6. Xin Nghỉ | Gửi teacher_request type='leave' | teacher_request → session |
| 7. Xem OT Được Phân Công | Xem teacher_request type='ot' (hệ thống tự tạo) | teacher_request |
| 8. Xem Feedback | Xem đánh giá từ học viên | student_feedback → session |
| 9. Xem Lịch Sử Thay Đổi | Xem session bị cancel/reschedule | session, teacher_request |

---

**Lưu Ý Quan Trọng:**
- Tất cả các flow đều phải thông qua authentication (Teacher phải login)
- Hệ thống luôn ghi audit trail (created_by, created_at, updated_at)
- Teacher chỉ được thao tác trên các session mà mình được phân công (teaching_slot.teacher_id = :teacher_id)
- Một số thao tác có thời gian lock (vd: điểm danh lock sau T giờ, điểm có thể lock sau deadline)

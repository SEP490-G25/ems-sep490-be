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
**Description:** Giáo viên mở danh sách học viên của một buổi học và cập nhật trạng thái điểm danh cho từng học viên.

**Database Tables Involved:**
- `session` → `student_session` → `student` → `user_account`
- `enrollment` (để verify học viên có đăng ký lớp không)

**Flow Steps:**

1. **Teacher chọn buổi học cần điểm danh**
   - Từ màn hình lịch dạy (Flow 1)
   - Click vào session cụ thể

2. **System load danh sách học viên**
   - System thực hiện query:
   ```
   SELECT 
     student_session.id AS student_session_id,
     student.student_code,
     user_account.full_name,
     student_session.attendance_status,
     student_session.is_makeup,
     student_session.note
   FROM student_session
   JOIN student ON student_session.student_id = student.id
   JOIN user_account ON student.user_account_id = user_account.id
   WHERE student_session.session_id = :session_id
     AND student_session.attendance_status IN ('planned', 'present', 'late', 'absent', 'excused', 'remote')
   ORDER BY student.student_code
   ```

3. **System hiển thị danh sách**
   - Mỗi học viên hiển thị:
     - Mã học viên (student_code)
     - Họ tên (full_name)
     - Trạng thái điểm danh hiện tại (attendance_status)
     - Có phải học bù không (is_makeup = TRUE → hiển thị badge "Học bù")

4. **Teacher cập nhật điểm danh cho từng học viên**
   - Teacher chọn trạng thái mới cho mỗi học viên:
     - `present`: Có mặt
     - `absent`: Vắng mặt
     - `late`: Đi muộn
     - `excused`: Nghỉ có phép (đã được duyệt)
     - `remote`: Học từ xa (cho trường hợp hybrid)
   - Teacher có thể ghi note riêng cho học viên (vd: "Không làm bài tập")

5. **Teacher click "Lưu điểm danh"**
   - System thực hiện UPDATE hàng loạt:
   ```
   UPDATE student_session
   SET 
     attendance_status = :new_status,
     note = :note,
     recorded_at = NOW()
   WHERE id = :student_session_id
   ```
   - System ghi nhận timestamp (recorded_at)

6. **System validation**
   - Không cho phép điểm danh trước ngày session.date
   - Sau khi lock (T giờ sau buổi học), cần approval từ Admin/Manager để sửa

**Result:** Trạng thái điểm danh được lưu vào `student_session.attendance_status`, dữ liệu sẵn sàng cho báo cáo.

---

## FLOW 3: Báo Cáo Buổi Học (Teacher Session Report)

**Actors involved:** Teacher, System  
**Description:** Sau khi dạy xong, Teacher báo cáo nội dung đã dạy, ghi chú về buổi học.

**Database Tables Involved:**
- `session` → `course_session`

**Flow Steps:**

1. **Teacher mở form báo cáo buổi học**
   - Từ màn hình chi tiết session
   - Click "Báo cáo buổi học"

2. **System hiển thị form**
   - Tự động điền:
     - Nội dung kế hoạch: `course_session.topic` (chủ đề dự kiến)
     - Nhiệm vụ học viên: `course_session.student_task`
   - Teacher điền:
     - Nội dung thực tế đã dạy (có thể khác kế hoạch)
     - Ghi chú về buổi học (học viên phản ứng thế nào, có vấn đề gì)
     - Sĩ số: System tự tính từ attendance (số present + late + remote)

3. **Teacher chọn trạng thái buổi học**
   - `done`: Hoàn thành bình thường
   - `cancelled`: Đã hủy (hiếm khi Teacher tự set, thường do Academic Staff)

4. **Teacher click "Submit Report"**
   - System thực hiện UPDATE:
   ```
   UPDATE session
   SET 
     status = 'done',
     teacher_note = :note,
     actual_content = :actual_content
   WHERE id = :session_id
   ```

5. **System validation**
   - Không cho phép submit report trước ngày session.date
   - Nếu đã submit, vẫn có thể edit trong vòng T giờ (trước khi lock)

**Result:** Buổi học được đánh dấu `done`, ghi lại nội dung thực tế, phục vụ QA tracking và báo cáo tiến độ.

---

## FLOW 4: Nhập Điểm Đánh Giá (Teacher Score Entry)

**Actors involved:** Teacher, System  
**Description:** Teacher nhập điểm cho các bài kiểm tra (assessment) của học viên.

**Database Tables Involved:**
- `assessment` → `score` ← `student`
- `class` → `enrollment` (để lấy danh sách học viên của lớp)

**Flow Steps:**

1. **Teacher chọn lớp và assessment cần nhập điểm**
   - Vào menu "Đánh giá"
   - Chọn lớp → Chọn assessment (vd: "Midterm Exam")

2. **System load danh sách học viên**
   - System thực hiện query:
   ```
   SELECT 
     student.id AS student_id,
     student.student_code,
     user_account.full_name,
     score.score AS current_score,
     score.feedback AS current_feedback
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

3. **System hiển thị bảng nhập điểm**
   - Mỗi học viên có:
     - Mã học viên (student_code)
     - Họ tên (full_name)
     - Điểm hiện tại (nếu đã nhập)
     - Ô input để nhập điểm mới
     - Ô textarea để nhập feedback

4. **Teacher nhập điểm cho từng học viên**
   - Nhập điểm (0 - assessment.max_score)
   - Nhập feedback (optional): "Bài làm tốt, cần cải thiện phần grammar"

5. **Teacher click "Lưu điểm"**
   - System thực hiện UPSERT (INSERT or UPDATE):
   ```
   INSERT INTO score (assessment_id, student_id, score, feedback, graded_by, graded_at)
   VALUES (:assessment_id, :student_id, :score, :feedback, :teacher_id, NOW())
   ON CONFLICT (assessment_id, student_id) DO UPDATE
   SET 
     score = EXCLUDED.score,
     feedback = EXCLUDED.feedback,
     graded_at = NOW()
   ```

6. **System validation**
   - Điểm không được vượt quá `assessment.max_score`
   - Tổng weight của các assessment không quá 100%

**Alternative Flow: Import từ Excel**
- Teacher upload file CSV (student_code, score, feedback)
- System parse và validate
- System thực hiện bulk insert

**Result:** Điểm được lưu vào bảng `score`, học viên và Manager có thể xem báo cáo điểm.

---

## FLOW 5: Đăng Ký Lịch Rảnh & OT (Teacher Availability & OT Registration)

**Actors involved:** Teacher, System  
**Description:** Giáo viên khai báo lịch rảnh thường xuyên và đăng ký OT (overtime) cho các buổi cụ thể.

**Database Tables Involved:**
- `teacher_availability` (lịch rảnh thường xuyên hàng tuần)
- `teacher_availability_override` (lịch rảnh/bận ngoại lệ cho ngày cụ thể)

**Flow Steps:**

### 5A. Khai Báo Lịch Rảnh Thường Xuyên

1. **Teacher vào menu "Lịch rảnh của tôi"**
   - Click "Thêm lịch rảnh thường xuyên"

2. **Teacher điền thông tin**
   - Chọn ngày trong tuần (day_of_week: 0=Chủ nhật, 1=Thứ 2, ..., 6=Thứ 7)
   - Chọn khung giờ (start_time, end_time)
   - Ghi chú (optional)

3. **Teacher click "Lưu"**
   - System thực hiện INSERT:
   ```
   INSERT INTO teacher_availability (teacher_id, day_of_week, start_time, end_time, note)
   VALUES (:teacher_id, :day_of_week, :start_time, :end_time, :note)
   ```

4. **System hiển thị danh sách lịch rảnh**
   - Teacher có thể xem, sửa, xóa các lịch rảnh đã khai báo

### 5B. Đăng Ký OT (Overtime) Cho Ngày Cụ Thể

1. **Teacher vào menu "Đăng ký OT"**
   - Click "Thêm lịch OT"

2. **Teacher điền thông tin**
   - Chọn ngày cụ thể (date)
   - Chọn khung giờ (start_time, end_time)
   - Lý do: "Sẵn sàng nhận thêm lớp" hoặc "Có thể thay thế GV khác"

3. **Teacher click "Đăng ký"**
   - System thực hiện INSERT:
   ```
   INSERT INTO teacher_availability_override 
   (teacher_id, date, start_time, end_time, is_available, reason)
   VALUES (:teacher_id, :date, :start_time, :end_time, TRUE, :reason)
   ```

4. **System ghi nhận**
   - `is_available = TRUE` → Teacher sẵn sàng nhận việc
   - Khi Academic Staff tìm giáo viên thay thế, hệ thống ưu tiên những người đã đăng ký OT

### 5C. Khai Báo Lịch Bận (Không Rảnh) Cho Ngày Cụ Thể

1. **Teacher khai báo không rảnh** (vd: đi du lịch, có việc riêng)
   - Chọn ngày (date)
   - Chọn khung giờ
   - `is_available = FALSE`

2. **System lưu**
   - Hệ thống sẽ không gợi ý Teacher này làm substitute trong khung giờ đó

**Result:** 
- Hệ thống có dữ liệu về khả năng sẵn sàng của Teacher
- Khi cần tìm giáo viên thay thế, hệ thống query `teacher_availability` và `teacher_availability_override` để gợi ý

---

## FLOW 6: Gửi Yêu Cầu Nghỉ Học (Teacher Leave Request)

**Actors involved:** Teacher, Academic Staff, System  
**Description:** Giáo viên gửi yêu cầu nghỉ dạy một buổi học. Academic Staff phải tìm giải pháp (substitute/reschedule/cancel).

**Database Tables Involved:**
- `teacher_request` → `session` → `teaching_slot`

**Flow Steps:**

1. **Teacher vào "Lịch dạy" và chọn buổi cần nghỉ**
   - Click "Xin nghỉ buổi này"

2. **Teacher điền form yêu cầu**
   - Chọn session_id (hệ thống tự điền)
   - Chọn loại request: `leave` (nghỉ)
   - Lý do: "Có việc gia đình khẩn cấp"
   - Ngày gửi yêu cầu (submitted_at)

3. **Teacher click "Gửi yêu cầu"**
   - System thực hiện INSERT:
   ```
   INSERT INTO teacher_request 
   (teacher_id, session_id, request_type, reason, status, submitted_at)
   VALUES (:teacher_id, :session_id, 'leave', :reason, 'pending', NOW())
   ```

4. **System gửi notification tới Academic Staff**
   - Email/SMS: "Giáo viên [Tên] xin nghỉ buổi [Session] vào [Ngày]"

5. **Academic Staff nhận request và tìm giải pháp** (xem academic-flows.md)
   - Option A: Tìm giáo viên thay thế
   - Option B: Dời lịch buổi học
   - Option C: Hủy buổi học (last resort)

6. **Academic Staff duyệt hoặc từ chối**
   - Duyệt: 
     ```
     UPDATE teacher_request
     SET 
       status = 'approved',
       decided_by = :academic_staff_id,
       decided_at = NOW(),
       resolution = 'Teacher X sẽ dạy thay'
     WHERE id = :request_id
     ```
   - Từ chối (nếu không hợp lý):
     ```
     UPDATE teacher_request
     SET 
       status = 'rejected',
       decided_by = :academic_staff_id,
       decided_at = NOW(),
       rejection_reason = 'Không có lý do chính đáng'
     WHERE id = :request_id
     ```

7. **System gửi notification tới Teacher**
   - "Yêu cầu nghỉ của bạn đã được duyệt. Teacher X sẽ dạy thay."

**Result:** 
- Request được ghi nhận trong hệ thống
- Không bị mất dữ liệu (audit trail)
- Teacher biết trạng thái request của mình

---

## FLOW 7: Xem Yêu Cầu OT Được Phân Công (Teacher OT Assignment)

**Actors involved:** Teacher, Academic Staff, System  
**Description:** Khi Academic Staff phân công Teacher dạy OT (thay thế hoặc thêm giờ), hệ thống tự động tạo teacher_request type='ot' để ghi nhận.

**Database Tables Involved:**
- `teacher_request` (type='ot')
- `teaching_slot` (ghi nhận Teacher mới được assign)

**Flow Steps:**

1. **Academic Staff cần tìm giáo viên thay thế** (vì giáo viên A xin nghỉ)
   - Hệ thống gợi ý Teacher B (đã đăng ký OT trong khung giờ đó)

2. **Academic Staff chọn Teacher B và assign**
   - System thực hiện:
   ```
   -- Step 1: Cập nhật teaching_slot (thay Teacher A bằng Teacher B)
   UPDATE teaching_slot
   SET teacher_id = :teacher_b_id
   WHERE session_id = :session_id AND teacher_id = :teacher_a_id;
   
   -- Step 2: Tự động tạo teacher_request cho Teacher B (ghi nhận OT)
   INSERT INTO teacher_request 
   (teacher_id, session_id, request_type, status, decided_by, decided_at, resolution)
   VALUES 
   (:teacher_b_id, :session_id, 'ot', 'approved', :academic_staff_id, NOW(), 
    'Assigned to substitute for Teacher A');
   ```

3. **System gửi notification tới Teacher B**
   - "Bạn được phân công dạy thay buổi [Session] vào [Ngày]. Đây là giờ OT."

4. **Teacher B vào "Yêu cầu của tôi"**
   - Xem danh sách teacher_request
   - Thấy request type='ot', status='approved'
   - Xác nhận số giờ OT đã được ghi nhận (để tính lương)

**Result:** 
- Giờ OT được ghi nhận chính xác trong hệ thống
- Phòng Kế toán có dữ liệu để tính lương OT cho Teacher

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

## FLOW 9: Xem Lịch Sử Thay Đổi Lịch Dạy (Teacher View Schedule Changes)

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

# STUDENT MAIN FLOWS - CÁC LUỒNG NGHIỆP VỤ CHÍNH CỦA HỌC VIÊN

## Tổng quan
File này mô tả các luồng nghiệp vụ chính mà Học viên (Student) thực hiện trong hệ thống EMS, bao gồm cách hệ thống join dữ liệu và các bước tương tác.

---

## FLOW 1: Xem Lịch Học Cá Nhân (Student Personal Schedule View)

**Actors involved:** Student, System  
**Description:** Học viên đăng nhập và xem lịch học cá nhân (danh sách các buổi học đã được ghi danh).

**Database Tables Involved:**
- `user_account` → `student` → `student_session` → `session` → `class` → `course`
- `session_resource` (để xem phòng/Zoom)
- `teaching_slot` → `teacher` (để xem giáo viên)

**Flow Steps:**

1. **Student đăng nhập hệ thống**
   - Input: email/password
   - System: Xác thực → lấy `user_account.id`
   - System: JOIN `student` table → lấy `student_id`

2. **Student click "Lịch học của tôi"**
   - System thực hiện query:
   ```
   SELECT 
     student_session.id AS student_session_id,
     session.id AS session_id,
     session.date,
     session.start_time,
     session.end_time,
     class.name AS class_name,
     course.name AS course_name,
     course_session.topic,
     student_session.attendance_status,
     student_session.is_makeup
   FROM student_session
   JOIN session ON student_session.session_id = session.id
   JOIN class ON session.class_id = class.id
   JOIN course ON class.course_id = course.id
   LEFT JOIN course_session ON session.course_session_id = course_session.id
   WHERE student_session.student_id = :student_id
     AND session.status IN ('planned', 'ongoing', 'done')
     AND session.date >= CURRENT_DATE
   ORDER BY session.date, session.start_time
   ```

3. **System hiển thị lịch học**
   - Dạng Calendar hoặc List view
   - Mỗi buổi học hiển thị:
     - Ngày giờ (date + start_time + end_time)
     - Tên lớp (class.name)
     - Chủ đề buổi học (course_session.topic)
     - Trạng thái điểm danh (attendance_status)
     - Badge "Học bù" nếu is_makeup = TRUE

4. **Student click vào một buổi học để xem chi tiết**
   - System load thêm:
     - Giáo viên: JOIN `teaching_slot` → `teacher` → `user_account.full_name`
     - Địa điểm: JOIN `session_resource` → `resource` (phòng/Zoom link)
     - Tài liệu: JOIN `course_material` WHERE course_session_id
     - Nhiệm vụ học viên: `course_session.student_task`

5. **Student có thể filter lịch**
   - Theo lớp (class_id)
   - Theo tuần/tháng
   - Chỉ xem buổi chưa điểm danh (attendance_status = 'planned')

**Result:** Student biết rõ lịch học, chuẩn bị bài trước, có link Zoom để tham gia.

---

## FLOW 2: Gửi Yêu Cầu Báo Nghỉ (Student Absence Request)

**Actors involved:** Student, Academic Staff, System  
**Description:** Học viên báo trước sẽ nghỉ một buổi học, Academic Staff duyệt để đánh dấu "excused" (nghỉ có phép).

**Database Tables Involved:**
- `student_request` → `session` → `student_session`

**Flow Steps:**

1. **Student vào "Lịch học" và chọn buổi sẽ nghỉ**
   - Click "Báo nghỉ buổi này"

2. **Student điền form yêu cầu**
   - Hệ thống tự điền:
     - target_session_id: buổi học sẽ nghỉ
   - Student điền:
     - Lý do: "Đi công tác", "Ốm", "Có việc gia đình"
     - Ngày gửi yêu cầu (submitted_at)

3. **Student click "Gửi yêu cầu"**
   - System thực hiện INSERT:
   ```
   INSERT INTO student_request 
   (student_id, target_session_id, request_type, reason, status, submitted_at)
   VALUES (:student_id, :target_session_id, 'absence', :reason, 'pending', NOW())
   ```

4. **System validation**
   - Không cho phép gửi request cho buổi đã qua (session.date < CURRENT_DATE)
   - Kiểm tra lead time: phải gửi trước X ngày (theo policy)

5. **System gửi notification tới Academic Staff**
   - Email/SMS: "Học viên [Tên] báo nghỉ buổi [Session] vào [Ngày]"

6. **Academic Staff duyệt request**
   - Duyệt:
     ```
     -- Cập nhật request
     UPDATE student_request
     SET 
       status = 'approved',
       decided_by = :academic_staff_id,
       decided_at = NOW()
     WHERE id = :request_id;
     
     -- Cập nhật student_session
     UPDATE student_session
     SET attendance_status = 'excused'
     WHERE student_id = :student_id 
       AND session_id = :target_session_id;
     ```
   - Từ chối (nếu không hợp lý hoặc vượt ngưỡng nghỉ):
     ```
     UPDATE student_request
     SET 
       status = 'rejected',
       decided_by = :academic_staff_id,
       decided_at = NOW(),
       rejection_reason = 'Vượt quá số buổi nghỉ cho phép'
     WHERE id = :request_id;
     ```

7. **System gửi notification tới Student**
   - "Yêu cầu báo nghỉ đã được chấp nhận. Bạn được đánh dấu nghỉ có phép."

**Result:** 
- Student_session.attendance_status = 'excused' (không bị tính vắng không phép)
- Request được lưu audit trail

---

## FLOW 3: Gửi Yêu Cầu Học Bù (Student Make-up Request) ⭐ COMPLEX

**Actors involved:** Student, Academic Staff, System  
**Description:** Học viên đã nghỉ một buổi và muốn học bù bằng cách tham gia buổi học cùng nội dung của lớp khác.

**Database Tables Involved:**
- `student_request` (target_session_id = buổi bị nghỉ, makeup_session_id = buổi học bù)
- `student_session` (tạo record mới cho buổi học bù)
- `session` → `course_session` (để tìm buổi cùng nội dung)

**Flow Steps:**

1. **Student vào "Lịch học" và chọn buổi đã nghỉ**
   - Chỉ hiển thị các buổi có attendance_status = 'absent' hoặc 'excused'
   - Click "Đăng ký học bù"

2. **System tìm các buổi học bù khả dụng**
   - System thực hiện query:
   ```
   SELECT 
     s2.id AS makeup_session_id,
     s2.date,
     s2.start_time,
     s2.end_time,
     c2.name AS class_name,
     b.name AS branch_name,
     COUNT(ss.id) AS enrolled_count,
     c2.max_capacity
   FROM session s2
   JOIN class c2 ON s2.class_id = c2.id
   JOIN branch b ON c2.branch_id = b.id
   LEFT JOIN student_session ss ON (
     ss.session_id = s2.id 
     AND ss.attendance_status IN ('planned', 'present', 'late')
   )
   WHERE s2.course_session_id = (
     SELECT course_session_id 
     FROM session 
     WHERE id = :target_session_id
   )
   AND s2.status = 'planned'
   AND s2.date >= CURRENT_DATE
   AND s2.id != :target_session_id
   GROUP BY s2.id, c2.id, b.id
   HAVING COUNT(ss.id) < c2.max_capacity
   ORDER BY (c2.max_capacity - COUNT(ss.id)) DESC, s2.date ASC
   LIMIT 10
   ```

3. **System hiển thị danh sách buổi học bù**
   - Mỗi buổi hiển thị:
     - Ngày giờ
     - Tên lớp
     - Chi nhánh
     - Số chỗ còn trống (max_capacity - enrolled_count)
   - Student chọn buổi phù hợp

4. **Student chọn buổi học bù và click "Gửi yêu cầu"**
   - System thực hiện INSERT:
   ```
   INSERT INTO student_request 
   (student_id, target_session_id, makeup_session_id, request_type, reason, status, submitted_at)
   VALUES 
   (:student_id, :target_session_id, :makeup_session_id, 'makeup', :reason, 'pending', NOW())
   ```

5. **System validation**
   - Kiểm tra makeup_session có cùng course_session_id với target_session
   - Kiểm tra capacity còn chỗ

6. **Academic Staff duyệt request**
   - Duyệt:
     ```
     -- Cập nhật request
     UPDATE student_request
     SET status = 'approved', decided_by = :staff_id, decided_at = NOW()
     WHERE id = :request_id;
     
     -- Cập nhật buổi gốc (nếu chưa excused)
     UPDATE student_session
     SET attendance_status = 'excused'
     WHERE student_id = :student_id AND session_id = :target_session_id;
     
     -- Tạo student_session cho buổi học bù
     INSERT INTO student_session 
     (student_id, session_id, is_makeup, attendance_status)
     VALUES 
     (:student_id, :makeup_session_id, TRUE, 'planned');
     ```

7. **System gửi notification**
   - Tới Student: "Yêu cầu học bù đã được duyệt. Bạn được tham gia lớp [Tên] vào [Ngày giờ]"
   - Tới Teacher của buổi học bù: "Học viên [Tên] sẽ tham gia học bù buổi này"

8. **Student tham gia buổi học bù**
   - Teacher thấy Student trong danh sách điểm danh (có badge "Học bù")
   - Teacher điểm danh bình thường

**Result:** 
- Student không bị thiếu nội dung học
- Buổi gốc được đánh dấu 'excused'
- Buổi học bù được ghi nhận với is_makeup=TRUE

---

## FLOW 4: Xác Nhận Yêu Cầu Chuyển Lớp (Student Confirm Transfer Request) ⭐ COMPLEX

**Actors involved:** Academic Staff, Student, System
**Description:** **Academic Staff tạo** yêu cầu chuyển lớp cho học viên, sau đó gửi cho student để xác nhận. Student chỉ cần confirm hoặc reject.

**Database Tables Involved:**
- `student_request` (submitted_by = academic_staff_id, current_class_id, target_class_id, effective_date)
- `enrollment` (cập nhật status class A = 'transferred', tạo mới cho class B)
- `student_session` (đánh dấu excused các buổi tương lai class A, tạo mới cho class B)

**Flow Steps:**

### Phase 1: Academic Staff Tạo Transfer Request

1. **Academic Staff vào "Student Management" → Chọn student → Click "Chuyển lớp"**

2. **Academic Staff điền form:**
   - Chọn current_class_id (lớp hiện tại của student)
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
   - Chọn target_class_id
   - Chọn effective_date (ngày bắt đầu học lớp mới)
   - Nhập lý do: "Student yêu cầu đổi ca học tối thành sáng"

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
   - "Lưu ý: Lớp mới đã học qua Buổi 15 và 17. Student cần tự học nội dung này."
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

### Phase 2: Student Xác Nhận (Confirm)

7. **Student vào "Requests" → Xem request chuyển lớp**

8. **System hiển thị thông tin request:**
   - Lớp hiện tại: [Class A Name]
   - Lớp mới: [Class B Name]
   - Ngày bắt đầu: [Effective Date]
   - Lý do: [Reason from Academic Staff]
   - **Warning (nếu có gap):** "Lớp mới đã học qua Buổi 15, 17. Bạn cần tự học nội dung này."

9. **Student click "Chấp nhận" hoặc "Từ chối"**

   **9a. Nếu Student chấp nhận:**
   ```
   BEGIN;

   -- 1. Cập nhật request
   UPDATE student_request
   SET status = 'approved', decided_by = :student_user_id, decided_at = NOW()
   WHERE id = :request_id;

   -- 2. Xác định mốc cutoff
   -- left_session_id: buổi cuối cùng ở class A (trước effective_date)
   -- join_session_id: buổi đầu tiên ở class B (từ effective_date trở đi)

   -- 3. Cập nhật enrollment class A
   UPDATE enrollment
   SET
     status = 'transferred',
     left_at = NOW(),
     left_session_id = (
       SELECT id FROM session
       WHERE class_id = :current_class_id
         AND date < :effective_date
       ORDER BY date DESC LIMIT 1
     )
   WHERE student_id = :student_id AND class_id = :current_class_id;

   -- 4. Tạo enrollment class B
   INSERT INTO enrollment
   (student_id, class_id, status, enrolled_at, join_session_id, created_by)
   VALUES (
     :student_id,
     :target_class_id,
     'enrolled',
     NOW(),
     (
       SELECT id FROM session
       WHERE class_id = :target_class_id
         AND date >= :effective_date
       ORDER BY date ASC LIMIT 1
     ),
     :academic_staff_id
   );

   -- 5. Đánh dấu excused các buổi tương lai của class A
   UPDATE student_session
   SET
     attendance_status = 'excused',
     note = 'Chuyển sang lớp ' || :target_class_name
   WHERE student_id = :student_id
     AND session_id IN (
       SELECT id FROM session
       WHERE class_id = :current_class_id
         AND date >= :effective_date
     );

   -- 6. Sinh student_session cho class B
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

   **9b. Nếu Student từ chối:**
   ```
   UPDATE student_request
   SET
     status = 'rejected',
     decided_by = :student_user_id,
     decided_at = NOW(),
     rejection_reason = 'Student không đồng ý chuyển lớp'
   WHERE id = :request_id
   ```

10. **System gửi notifications:**
    - Tới Academic Staff: "Student [Name] đã [chấp nhận/từ chối] yêu cầu chuyển lớp."
    - Nếu approved:
      - Tới Student: "Bạn đã chuyển sang lớp [B]. Lịch học mới đã được cập nhật."
      - Tới Teacher lớp B: "Học viên [Name] sẽ tham gia lớp từ [Date]."

**Result:**
- **BỎ HOÀN TOÀN:** Student tự tạo transfer request
- **THAY ĐỔI:** Academic Staff tạo request → Student confirm
- `student_request.submitted_by = academic_staff_id` (để phân biệt)
- Student chuyển sang lớp mới sau khi confirm
- Lịch sử lớp cũ được bảo toàn (audit trail)
- Lịch học tự động được cập nhật

---

## FLOW 5: Xem Điểm Số & Kết Quả Học Tập (Student View Scores)

**Actors involved:** Student, System  
**Description:** Học viên xem điểm các bài kiểm tra và feedback từ giáo viên.

**Database Tables Involved:**
- `score` → `assessment` → `class`
- `student` → `enrollment` (để lấy các lớp đang/đã học)

**Flow Steps:**

1. **Student vào menu "Điểm của tôi"**

2. **System load danh sách lớp**
   - System query:
   ```
   SELECT DISTINCT
     c.id AS class_id,
     c.name AS class_name,
     co.name AS course_name
   FROM enrollment e
   JOIN class c ON e.class_id = c.id
   JOIN course co ON c.course_id = co.id
   WHERE e.student_id = :student_id
     AND e.status IN ('enrolled', 'completed')
   ORDER BY e.enrolled_at DESC
   ```

3. **Student chọn lớp để xem điểm**
   - System load điểm:
   ```
   SELECT 
     a.name AS assessment_name,
     a.kind,
     a.max_score,
     a.weight,
     s.score,
     s.feedback,
     s.graded_at
   FROM assessment a
   LEFT JOIN score s ON (
     s.assessment_id = a.id 
     AND s.student_id = :student_id
   )
   WHERE a.class_id = :class_id
   ORDER BY a.created_at
   ```

4. **System hiển thị bảng điểm**
   - Mỗi assessment hiển thị:
     - Tên bài kiểm tra (assessment.name)
     - Loại (kind: quiz/midterm/final/assignment)
     - Điểm (score / max_score)
     - Trọng số (weight %)
     - Feedback từ Teacher
     - Ngày chấm (graded_at)

5. **System tính tổng điểm**
   - Tổng điểm = Σ(score / max_score * weight)
   - Hiển thị: "Tổng điểm: 85.5/100"

6. **Student xem chi tiết feedback**
   - Click vào từng assessment để đọc feedback chi tiết từ Teacher

**Result:** 
- Student biết được kết quả học tập
- Student biết điểm yếu cần cải thiện (từ feedback)

---

## FLOW 6: Đánh Giá Phase (Student Phase Feedback Submission)

**Actors involved:** Student, System
**Description:** Sau khi kết thúc phase, hệ thống **TỰ ĐỘNG TẠO** student_feedback records. Student chỉ cần điền rating và comment vào records đã được tạo sẵn.

**Database Tables Involved:**
- `student_feedback` (phase_id, session_id, student_id, is_feedback, rating, comment)
- `course_phase` → `course_session` → `session`
- `student_session` (để xác định student có tham gia phase không)

**Flow Steps:**

### Background: System Auto-Generate Feedback Records (Xem system-flows.md FLOW 11)

**Hệ thống tự động:**
- Detect session cuối cùng của mỗi phase (course_session có sequence_no cao nhất trong phase)
- Khi session này được mark status='done'
- Hệ thống tạo sẵn student_feedback cho TẤT CẢ students của lớp:
  ```
  INSERT INTO student_feedback (student_id, session_id, phase_id, is_feedback, rating, comment, created_at)
  SELECT
    e.student_id,
    :last_session_id_of_phase,
    :phase_id,
    FALSE,  -- Chưa feedback
    NULL,   -- Chưa có rating
    NULL,   -- Chưa có comment
    NOW()
  FROM enrollment e
  WHERE e.class_id = :class_id
    AND e.status IN ('enrolled', 'completed')
  ```

### Student Submit Feedback

1. **System gửi notification tới Student sau khi phase kết thúc:**
   - Email/In-app: "Đánh giá Phase [Phase Number] - [Phase Name] của lớp [Class Name]"

2. **Student click link trong notification hoặc vào sidebar "Feedback"**

3. **System load danh sách phases cần feedback:**
   ```
   SELECT
     sf.id AS feedback_id,
     cp.phase_number,
     cp.name AS phase_name,
     c.name AS class_name,
     s.date AS session_date,
     cs.topic,
     sf.is_feedback,
     sf.rating,
     sf.comment
   FROM student_feedback sf
   JOIN course_phase cp ON sf.phase_id = cp.id
   JOIN session s ON sf.session_id = s.id
   JOIN class c ON s.class_id = c.id
   LEFT JOIN course_session cs ON s.course_session_id = cs.id
   WHERE sf.student_id = :student_id
     AND sf.is_feedback = FALSE  -- Chưa feedback
     AND s.status = 'done'
   ORDER BY s.date DESC
   ```

4. **System hiển thị danh sách phases chưa feedback:**
   - Mỗi phase hiển thị:
     - **Badge màu đỏ:** "⚠️ Cần feedback" (nếu is_feedback = FALSE)
     - **Phase number & name:** "Phase 1: Foundation"
     - **Class name:** "English A1 - Morning Class 01"
     - **Session date:** Ngày buổi học cuối của phase
     - **Topic:** Chủ đề buổi học cuối

5. **Student click "Đánh giá phase này"**

6. **System hiển thị form đánh giá:**
   - **Phase info:**
     - Phase number & name
     - Class name
     - Session date (buổi học cuối của phase)
   - **Rating:** 1-5 sao (1=Rất kém, 5=Rất tốt)
   - **Comment (optional):**
     - "Giáo viên dạy rất nhiệt tình"
     - "Bài giảng dễ hiểu, cần thêm thời gian thực hành"
     - "Phase này khó, cần thêm bài tập"

7. **Student click "Gửi đánh giá"**
   - System UPDATE (không phải INSERT vì record đã tồn tại):
   ```
   UPDATE student_feedback
   SET
     rating = :rating,
     comment = :comment,
     is_feedback = TRUE,  -- Đánh dấu đã feedback
     submitted_at = NOW()
   WHERE id = :feedback_id
     AND student_id = :student_id
   ```

8. **System validation:**
   - Rating phải từ 1-5
   - is_feedback = FALSE → TRUE (chỉ cho phép feedback 1 lần)

9. **System gửi thank you message:**
   - "Cảm ơn bạn đã đánh giá Phase [Number]. Ý kiến của bạn giúp chúng tôi cải thiện chất lượng."

10. **Frontend check is_feedback flag:**
    - Nếu `is_feedback = FALSE`: Hiển thị badge "⚠️ Cần feedback" và bắt buộc student phải feedback
    - Nếu `is_feedback = TRUE`: Hiển thị "✅ Đã feedback" và không cho edit (hoặc cho edit trong X ngày)

**Result:**
- Feedback được lưu vào database với `is_feedback = TRUE`
- QA team và Manager có dữ liệu để đánh giá Teacher và Phase
- Teacher xem được feedback để cải thiện (xem teacher-flows.md FLOW 9)
- **Khác biệt chính:**
  - **Cũ:** System tạo feedback record khi student submit
  - **Mới:** System tạo sẵn feedback record → Student chỉ UPDATE rating/comment
  - **Lợi ích:** Tracking được student nào chưa feedback (is_feedback = FALSE)

**Lưu ý quan trọng:**
- Feedback theo **PHASE** (không phải theo session riêng lẻ)
- Mỗi student feedback 1 lần cho mỗi phase
- Feedback được tạo sẵn khi session cuối của phase kết thúc
- Frontend luôn check `is_feedback` flag để bắt buộc student feedback

---

## FLOW 7: Xem Lịch Sử Điểm Danh (Student View Attendance History)

**Actors involved:** Student, System  
**Description:** Học viên xem lịch sử điểm danh của mình (đã học bao nhiêu buổi, nghỉ bao nhiêu buổi).

**Database Tables Involved:**
- `student_session` → `session` → `class`

**Flow Steps:**

1. **Student vào menu "Lịch sử điểm danh"**

2. **System load lịch sử**
   - System query:
   ```
   SELECT 
     s.date,
     s.start_time,
     s.end_time,
     c.name AS class_name,
     cs.topic,
     ss.attendance_status,
     ss.is_makeup,
     ss.note
   FROM student_session ss
   JOIN session s ON ss.session_id = s.id
   JOIN class c ON s.class_id = c.id
   LEFT JOIN course_session cs ON s.course_session_id = cs.id
   WHERE ss.student_id = :student_id
     AND s.status IN ('done', 'cancelled')
   ORDER BY s.date DESC
   ```

3. **System hiển thị danh sách**
   - Mỗi buổi hiển thị:
     - Ngày giờ
     - Tên lớp
     - Chủ đề
     - Trạng thái điểm danh:
       - ✅ Có mặt (present)
       - ⏰ Đi muộn (late)
       - ❌ Vắng mặt (absent)
       - 📝 Nghỉ có phép (excused)
       - 🌐 Học từ xa (remote)
     - Badge "Học bù" nếu is_makeup=TRUE

4. **System tính thống kê**
   - Tổng số buổi: COUNT(*)
   - Số buổi có mặt: COUNT WHERE attendance_status IN ('present', 'late', 'remote')
   - Số buổi vắng: COUNT WHERE attendance_status = 'absent'
   - Tỷ lệ chuyên cần = (số buổi có mặt / tổng số buổi) × 100%
   - Hiển thị: "Tỷ lệ chuyên cần: 92% (23/25 buổi)"

5. **System cảnh báo nếu vượt ngưỡng**
   - Nếu số buổi vắng > ngưỡng cho phép:
     - "⚠️ Bạn đã vắng 5 buổi (ngưỡng: 4). Vui lòng chú ý chuyên cần."

**Result:** 
- Student tự theo dõi được tình hình học tập
- Student biết khi nào cần cải thiện chuyên cần

---

## FLOW 8: Xem Tài Liệu Học Tập (Student View Course Materials)

**Actors involved:** Student, System  
**Description:** Học viên xem/download tài liệu học tập (slides, bài tập) của các buổi học.

**Database Tables Involved:**
- `course_material` ← `course_session` ← `session` ← `student_session`
- `course_material` ← `course_phase` ← `course`
- `course_material` ← `course`

**Flow Steps:**

1. **Student vào "Tài liệu học tập"**

2. **System hiển thị các cách xem tài liệu**
   - Option A: Theo buổi học (session)
   - Option B: Theo lớp (class)
   - Option C: Theo khóa học (course)

3. **Option A: Xem tài liệu theo buổi học**
   - Student chọn buổi học từ lịch
   - System query:
   ```
   SELECT 
     cm.id,
     cm.title,
     cm.file_type,
     cm.file_url,
     cm.uploaded_at
   FROM course_material cm
   WHERE cm.course_session_id = (
     SELECT course_session_id 
     FROM session 
     WHERE id = :session_id
   )
   ORDER BY cm.uploaded_at
   ```

4. **Option B: Xem tài liệu theo lớp**
   - Student chọn lớp
   - System query tất cả tài liệu của các buổi trong lớp:
   ```
   SELECT DISTINCT
     cm.id,
     cm.title,
     cm.file_type,
     cm.file_url,
     cs.topic AS session_topic
   FROM course_material cm
   LEFT JOIN course_session cs ON cm.course_session_id = cs.id
   LEFT JOIN course_phase cp ON cm.phase_id = cp.id
   WHERE (
     cm.course_session_id IN (
       SELECT course_session_id FROM session WHERE class_id = :class_id
     )
     OR cm.phase_id IN (
       SELECT id FROM course_phase WHERE course_id = (
         SELECT course_id FROM class WHERE id = :class_id
       )
     )
     OR cm.course_id = (SELECT course_id FROM class WHERE id = :class_id)
   )
   ORDER BY cm.uploaded_at
   ```

5. **Student click download**
   - System kiểm tra quyền:
     - Student phải được enroll vào lớp của course đó
   - System generate presigned URL (nếu dùng S3)
   - Student download file

**Result:** 
- Student có thể tự học lại tài liệu
- Student chuẩn bị bài trước buổi học

---

## FLOW 9: Xem Thông Báo Thay Đổi Lịch (Student View Schedule Change Notifications)

**Actors involved:** Student, System  
**Description:** Khi lịch học bị thay đổi (reschedule, cancel, teacher change), Student nhận notification.

**Database Tables Involved:**
- `session` (có trường teacher_note ghi log thay đổi)
- `student_session` (link tới session)
- Notification system (có thể là bảng riêng hoặc email/SMS)

**Flow Steps:**

1. **Khi Academic Staff thay đổi lịch** (reschedule/cancel)
   - System tự động tạo notification cho tất cả enrolled students

2. **System gửi notification qua nhiều kênh**
   - Email: "Buổi học ngày 20/2 đã bị dời sang 22/2"
   - SMS (nếu cấp bách): "Lớp [Tên] hủy buổi học hôm nay"
   - In-app notification

3. **Student login và xem notification**
   - System query:
   ```
   SELECT 
     n.id,
     n.title,
     n.message,
     n.created_at,
     n.is_read
   FROM notification n
   WHERE n.user_id = :user_id
     AND n.created_at >= NOW() - INTERVAL '30 days'
   ORDER BY n.created_at DESC
   ```

4. **Student click vào notification để xem chi tiết**
   - Hiển thị:
     - Loại thay đổi (reschedule/cancel/teacher change)
     - Lịch cũ vs lịch mới
     - Lý do thay đổi
   - System đánh dấu is_read = TRUE

5. **Student xem lịch học đã được cập nhật tự động**
   - Vào "Lịch học" → thấy ngày giờ mới

**Result:** 
- Student không bị lỡ thông tin quan trọng
- Student biết lịch học mới và chuẩn bị kịp thời

---

## Tóm Tắt Các Flow Chính Của Student

| Flow | Mô Tả | Bảng Chính Liên Quan |
|------|-------|---------------------|
| 1. Xem Lịch Học | Load danh sách session từ student_session | student_session → session → class → course |
| 2. Báo Nghỉ | Gửi student_request type='absence' | student_request → student_session (update excused) |
| 3. Học Bù | Gửi student_request type='makeup', tạo student_session mới | student_request → student_session (insert new) |
| 4. Chuyển Lớp | Gửi student_request type='transfer', cập nhật enrollment | student_request → enrollment → student_session |
| 5. Xem Điểm | Xem score của các assessment | score → assessment → class |
| 6. Đánh Giá Buổi Học | Gửi feedback rating + comment | student_feedback → session |
| 7. Xem Lịch Sử Điểm Danh | Xem student_session.attendance_status | student_session → session |
| 8. Xem Tài Liệu | Download course_material | course_material ← course_session/phase/course |
| 9. Xem Thông Báo | Nhận notification về thay đổi lịch | notification, session.teacher_note |

---

**Lưu Ý Quan Trọng:**
- Tất cả flow đều yêu cầu authentication
- Student chỉ được xem/thao tác trên dữ liệu của chính mình
- Các request (absence/makeup/transfer) phải qua approval của Academic Staff
- Lead time policy được enforced (không gửi request quá gần ngày học)
- System tự động đồng bộ lịch học sau khi có thay đổi (reschedule/transfer)

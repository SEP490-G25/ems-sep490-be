# Luồng Học Viên Yêu Cầu Chuyển Lớp (Transfer Request)

Tài liệu này mô tả chi tiết luồng xử lý khi một học viên yêu cầu chuyển từ lớp này sang lớp khác. Các kịch bản bao gồm chuyển lớp trong cùng một khóa học (ví dụ: đổi lịch), hoặc chuyển sang một khóa học khác (ví dụ: lên cấp độ cao hơn).

Các truy vấn và kết quả mong đợi được cung cấp dựa trên `seed-data.sql` và ngày thực hiện test là `2025-10-31`.

---

## PHẦN 1: HỌC VIÊN TẠO YÊU CẦU

### Bước 1-10: Khởi tạo Request
1.  **Login & Navigate**: Học viên đăng nhập và vào mục "My Requests".
2.  **Tạo Request Mới**: Click button "Tạo Request Mới" và chọn loại "Transfer - Chuyển lớp".
3.  **Chọn Lớp Hiện Tại**: Hệ thống hiển thị danh sách các lớp học viên đang theo học. Học viên chọn lớp muốn chuyển đi.

**Context Test:**
*   **Học viên**: `Student ID = 7` (Bui Van Hieu).
*   **Lớp hiện tại**: Học viên chọn `Class ID = 2` (FOUND-F2-2025 - Lớp buổi tối Online).

### Bước 11-14: Tìm kiếm Lớp Học Mới
Hệ thống cung cấp các lựa chọn để tìm lớp học mới:
*   **Option 1: Giữ nguyên khóa học** (Chỉ đổi lịch/lớp).
*   **Option 2: Chuyển sang khóa học khác**.

#### Kịch bản 1: Tìm lớp học khác trong cùng khóa học
Đây là kịch bản phổ biến nhất. Hệ thống sẽ tìm tất cả các lớp khác thuộc cùng `course_id` và còn chỗ trống.

**Context Test:**
*   **Khóa học hiện tại**: `Course ID = 1` (IELTS Foundation 2025).
*   **Yêu cầu**: Tìm các lớp khác cũng thuộc `Course ID = 1`.

```sql
-- Tìm các lớp khác cùng khóa học
WITH current_class_info AS (
    SELECT course_id, id AS current_class_id, branch_id AS current_branch_id
    FROM "class"
    WHERE id = 2 -- Lớp hiện tại của Student 7 là Class 2
),
enrollment_counts AS (
    SELECT
        c.id AS class_id,
        COUNT(e.student_id) AS enrolled_count
    FROM "class" c
    LEFT JOIN enrollment e ON c.id = e.class_id AND e.status = 'enrolled'
    GROUP BY c.id
)
SELECT
    c.id AS class_id,
    c.code,
    c.name,
    c.modality,
    c.start_date,
    b.name AS branch_name,
    c.max_capacity - COALESCE(ec.enrolled_count, 0) AS available_slots,
    CASE WHEN b.id = cci.current_branch_id THEN true ELSE false END AS is_same_branch
FROM "class" c
JOIN branch b ON c.branch_id = b.id
CROSS JOIN current_class_info cci
LEFT JOIN enrollment_counts ec ON c.id = ec.class_id
WHERE c.course_id = cci.course_id
  AND c.id != cci.current_class_id
  AND c.status IN ('scheduled', 'ongoing')
  AND COALESCE(ec.enrolled_count, 0) < c.max_capacity
ORDER BY is_same_branch DESC, c.start_date ASC;
```

**Expected Output (khi chạy vào ngày 2025-10-31):**
*Ghi chú: `seed-data.sql` có 2 lớp khác cùng khóa học Foundation là `Class 1` và `Class 3`.*
```json
[
  {
    "class_id": 3,
    "code": "FOUND-F3-2025",
    "name": "Foundation F3 - Afternoon Hybrid",
    "modality": "hybrid",
    "start_date": "2025-11-06",
    "branch_name": "TMS Ha Noi Branch 01",
    "available_slots": 0,
    "is_same_branch": true
  },
  {
    "class_id": 1,
    "code": "FOUND-F1-2025",
    "name": "Foundation F1 - Morning Offline",
    "modality": "offline",
    "start_date": "2025-11-04",
    "branch_name": "TMS Ha Noi Branch 01",
    "available_slots": 15,
    "is_same_branch": true
  }
]
```

### Bước 15: Phân tích Chênh lệch Nội dung (Content Gap Detection)
Sau khi học viên chọn một lớp để chuyển đến, hệ thống sẽ tự động phân tích sự chênh lệch về nội dung đã học.

**Context Test:**
*   **Học viên**: Chọn chuyển từ `Class ID = 2` (lớp buổi tối) sang `Class ID = 1` (lớp buổi sáng).
*   **Phân tích**: So sánh các `course_session_id` đã hoàn thành (`status='done'`) của hai lớp.

```sql
-- Phân tích chênh lệch nội dung giữa Class 2 và Class 1
WITH sessions_info AS (
    SELECT
        s.class_id,
        s.course_session_id
    FROM session s
    WHERE s.class_id IN (2, 1) -- Current class 2, Target class 1
      AND s.status = 'done'
      AND s.date < '2025-10-31' -- Ngày thực hiện request
),
current_class_completed AS (
    SELECT course_session_id FROM sessions_info WHERE class_id = 2
),
target_class_completed AS (
    SELECT course_session_id FROM sessions_info WHERE class_id = 1
),
gap_sessions AS (
    -- Nội dung lớp Target ĐÃ HỌC mà lớp Current CHƯA HỌC
    SELECT course_session_id FROM target_class_completed
    EXCEPT
    SELECT course_session_id FROM current_class_completed
)
SELECT
    COUNT(gs.course_session_id) AS gap_count,
    ARRAY_AGG(gs.course_session_id) AS gap_session_ids,
    (SELECT ARRAY_AGG(topic ORDER BY id) FROM course_session WHERE id IN (SELECT course_session_id FROM gap_sessions)) AS gap_topics
FROM gap_sessions gs;
```

**Expected Output:**
*Ghi chú: `Class 1` (target) bắt đầu sau `Class 2` (current), nên tại ngày `2025-10-31`, `Class 1` chưa học buổi nào. Do đó, không có sự chênh lệch.*
```json
[
  {
    "gap_count": 0,
    "gap_session_ids": null,
    "gap_topics": null
  }
]
```

### Bước 16: Thực hiện Schedule Conflict Check (background)
Hệ thống kiểm tra xem lịch học của lớp mới có bị trùng với bất kỳ lớp học nào khác mà sinh viên đang tham gia hay không.

**Context Test:**
*   **Học viên**: `Student ID = 7`.
*   **Lớp hiện tại**: `Class ID = 2`.
*   **Lớp muốn chuyển đến**: `Class ID = 1`.

```sql
-- Kiểm tra xung đột lịch học cho Student 7 khi chuyển sang Class 1
WITH target_class_sessions AS (
    -- Lịch học của lớp mới (Target Class)
    SELECT s.date, tst.start_time, tst.end_time
    FROM session s
    JOIN time_slot_template tst ON s.time_slot_template_id = tst.id
    WHERE s.class_id = 1 -- Lớp mới
      AND s.status = 'planned' AND s.date >= CURRENT_DATE
),
student_other_sessions AS (
    -- Lịch học của các lớp khác mà sinh viên đang học
    SELECT s.date, tst.start_time, tst.end_time, c.code AS class_code
    FROM enrollment e
    JOIN session s ON e.class_id = s.class_id
    JOIN time_slot_template tst ON s.time_slot_template_id = tst.id
    JOIN class c ON e.class_id = c.id
    WHERE e.student_id = 7
      AND e.status = 'enrolled'
      AND e.class_id NOT IN (2, 1) -- Loại trừ lớp cũ và lớp mới
      AND s.status = 'planned' AND s.date >= CURRENT_DATE
)
SELECT
    tcs.date AS conflict_date,
    tcs.start_time AS target_start,
    tcs.end_time AS target_end,
    sos.class_code AS conflicting_class,
    sos.start_time AS conflicting_start,
    sos.end_time AS conflicting_end
FROM target_class_sessions tcs
JOIN student_other_sessions sos ON tcs.date = sos.date
WHERE (tcs.start_time, tcs.end_time) OVERLAPS (sos.start_time, sos.end_time);
```

**Expected Output:**
*Ghi chú: Vì Student 7 chỉ đang học 1 lớp (lớp sẽ chuyển đi), nên sẽ không có xung đột nào được tìm thấy.*
```json
[]
```

### Bước 17-24: Hoàn tất và Gửi Yêu Cầu
1.  **Hiển thị Kết quả**: Danh sách các lớp khả dụng được hiển thị kèm theo các cảnh báo về chênh lệch nội dung hoặc xung đột lịch.
2.  **Chọn Lớp và Điền Form**: Học viên chọn `Class ID = 1`, chọn ngày hiệu lực, điền lý do và gửi yêu cầu.

**Context Test (INSERT):**
*   **Học viên**: `student_id = 7` (`user_id = 106`).
*   **Từ Lớp**: `current_class_id = 2`.
*   **Sang Lớp**: `target_class_id = 1`.
*   **Ngày hiệu lực**: `2025-11-15`.

```sql
-- Insert transfer request cho Student 7
INSERT INTO student_request (
    student_id,
    current_class_id,
    target_class_id,
    request_type,
    status,
    effective_date,
    request_reason,
    submitted_at,
    submitted_by,
    created_at,
    updated_at
) VALUES (
    7,  -- Student ID
    2,  -- Từ Class 2
    1,  -- Sang Class 1
    'transfer',
    'pending',
    '2025-11-15', -- Ngày bắt đầu chuyển
    'My work schedule has changed, so I need to switch to the morning class.',
    '2025-10-31 14:00:00+07',
    106, -- User ID của Student 7
    '2025-10-31 14:00:00+07',
    '2025-10-31 14:00:00+07'
) RETURNING id;
```
**Expected Return**: ID của request mới (ví dụ: `13`).

---

## PHẦN 2: GIÁO VỤ XỬ LÝ YÊU CẦU

### Bước 28-33: Review Chi tiết Request
Giáo vụ (`user_id = 4`) vào trang quản lý, tìm và xem chi tiết yêu cầu vừa tạo.

```sql
-- Lấy thông tin chi tiết của transfer request
-- Giả sử request vừa tạo có ID = 13
SELECT
    sr.id AS request_id,
    sr.status,
    sr.effective_date,
    sr.request_reason,
    -- Student
    s.student_code,
    ua.full_name AS student_name,
    -- Current Class
    c_curr.code AS current_class_code,
    co_curr.name AS current_course_name,
    -- Target Class
    c_targ.code AS target_class_code,
    co_targ.name AS target_course_name,
    -- Attendance Stats in Current Class
    (SELECT ROUND(AVG(CASE WHEN ss.attendance_status = 'present' THEN 100 ELSE 0 END), 2)
     FROM student_session ss JOIN session s ON ss.session_id = s.id
     WHERE ss.student_id = sr.student_id AND s.class_id = sr.current_class_id) AS attendance_rate
FROM student_request sr
JOIN student s ON sr.student_id = s.id
JOIN user_account ua ON s.user_id = ua.id
JOIN class c_curr ON sr.current_class_id = c_curr.id
JOIN course co_curr ON c_curr.course_id = co_curr.id
JOIN class c_targ ON sr.target_class_id = c_targ.id
JOIN course co_targ ON c_targ.course_id = co_targ.id
WHERE sr.id = 13;
```

**Expected Output:**
```json
[
  {
    "request_id": 13,
    "status": "pending",
    "effective_date": "2025-11-15",
    "request_reason": "My work schedule has changed, so I need to switch to the morning class.",
    "student_code": "STD-0007",
    "student_name": "Bui Van Hieu",
    "current_class_code": "FOUND-F2-2025",
    "current_course_name": "IELTS Foundation 2025",
    "target_class_code": "FOUND-F1-2025",
    "target_course_name": "IELTS Foundation 2025",
    "attendance_rate": "85.71" -- Student 7 vắng 2/14 buổi đã diễn ra
  }
]
```

### Bước 34-40: Phê duyệt Yêu cầu
Giáo vụ xem xét thông tin và nhấn "Approve & Execute".

### Bước 41: Thực hiện Transaction Phê duyệt
Hệ thống thực hiện một loạt các thay đổi trong CSDL một cách an toàn.

**Context Test (Approve):**
*   **Request ID**: `13`.
*   **Học viên**: `student_id = 7`.
*   **Lớp cũ**: `class_id = 2`.
*   **Lớp mới**: `class_id = 1`.
*   **Ngày hiệu lực**: `2025-11-15`.
*   **Giáo vụ**: `user_id = 4`.

```sql
-- Transaction phê duyệt yêu cầu chuyển lớp
BEGIN;

-- Step 1: Cập nhật trạng thái request -> 'approved'
UPDATE student_request
SET
    status = 'approved',
    decided_by = 4, -- ID của giáo vụ
    decided_at = '2025-11-01 09:00:00+07'
WHERE id = 13 AND status = 'pending';

-- Step 2: Cập nhật enrollment cũ -> status = 'transferred'
UPDATE enrollment
SET
    status = 'transferred',
    left_at = '2025-11-14', -- Ngày trước ngày hiệu lực
    left_session_id = (SELECT MAX(id) FROM session WHERE class_id = 2 AND date < '2025-11-15')
WHERE student_id = 7 AND class_id = 2 AND status = 'enrolled';

-- Step 3: Hủy các buổi học tương lai ở lớp cũ
UPDATE student_session
SET attendance_status = 'excused',
    note = 'Transferred to class FOUND-F1-2025'
WHERE student_id = 7
  AND session_id IN (SELECT id FROM session WHERE class_id = 2 AND date >= '2025-11-15');

-- Step 4: Tạo enrollment mới cho lớp mới
INSERT INTO enrollment (class_id, student_id, status, enrolled_at, enrolled_by, join_session_id)
VALUES (
    1, -- Lớp mới
    7, -- Học viên
    'enrolled',
    '2025-11-01 09:00:00+07',
    4, -- Giáo vụ thực hiện
    (SELECT MIN(id) FROM session WHERE class_id = 1 AND date >= '2025-11-15')
);

-- Step 5: Thêm học viên vào các buổi học tương lai của lớp mới
INSERT INTO student_session (student_id, session_id, attendance_status)
SELECT
    7,          -- Học viên
    s.id,       -- ID của buổi học
    'planned'   -- Trạng thái
FROM session s
WHERE s.class_id = 1 AND s.date >= '2025-11-15'
ON CONFLICT (student_id, session_id) DO NOTHING;

COMMIT;
```

### Bước 42-47: Xác minh và Thông báo
Sau khi transaction thành công, hệ thống gửi email thông báo và cập nhật giao diện cho học viên.

```sql
-- Học viên kiểm tra lại danh sách lớp học của mình
SELECT
    e.class_id,
    c.code AS class_code,
    c.name AS class_name,
    e.status AS enrollment_status,
    e.enrolled_at,
    e.left_at
FROM enrollment e
JOIN "class" c ON e.class_id = c.id
WHERE e.student_id = 7
ORDER BY e.enrolled_at DESC;
```

**Expected Output:**
```json
[
  {
    "class_id": 1,
    "class_code": "FOUND-F1-2025",
    "class_name": "Foundation F1 - Morning Offline",
    "enrollment_status": "enrolled",
    "enrolled_at": "2025-11-01T09:00:00+07:00",
    "left_at": null
  },
  {
    "class_id": 2,
    "class_code": "FOUND-F2-2025",
    "class_name": "Foundation F2 - Evening Online",
    "enrollment_status": "transferred",
    "enrolled_at": "2025-09-25T10:00:00+07:00",
    "left_at": "2025-11-14T00:00:00+00:00"
  }
]
```

### Bước 48: Học viên kiểm tra "My Schedule"
Sau khi yêu cầu được duyệt, học viên kiểm tra lịch học của mình để xem các thay đổi. Lịch học sẽ hiển thị các buổi của lớp cũ bị hủy và các buổi của lớp mới được thêm vào.

**Context Test:**
*   **Học viên**: `Student ID = 7`.
*   **Ngày hiệu lực chuyển lớp**: `2025-11-15`.
*   **Query**: Lấy lịch học xung quanh ngày chuyển lớp.

```sql
-- Lấy lịch học của Student 7 xung quanh ngày chuyển lớp (ĐÃ SỬA LỖI ENUM)
SELECT
    s.date AS session_date,
    ss.attendance_status,
    c.code AS class_code,
    cs.topic AS session_topic,
    tst.start_time,
    tst.end_time,
    -- Cột `display_status` này chỉ dùng cho mục đích hiển thị trên UI
    CASE
        WHEN ss.attendance_status = 'excused' AND ss.note ILIKE '%Transferred%' THEN 'Transferred Out'
        WHEN c.id = 1 AND s.date >= '2025-11-15' THEN 'New Class'
        -- Chuyển đổi enum thành text để tránh lỗi kiểu dữ liệu
        ELSE ss.attendance_status::text
    END AS display_status
FROM student_session ss
JOIN session s ON ss.session_id = s.id
JOIN "class" c ON s.class_id = c.id
LEFT JOIN course_session cs ON s.course_session_id = cs.id
LEFT JOIN time_slot_template tst ON s.time_slot_template_id = tst.id
WHERE ss.student_id = 7
  AND s.date BETWEEN '2025-11-13' AND '2025-11-18'
ORDER BY s.date ASC, tst.start_time ASC;
```

**Expected Output:**
*Ghi chú: Lịch học cho thấy buổi cuối cùng ở lớp cũ (Class 2) và các buổi học đã được hủy, cùng với buổi học đầu tiên ở lớp mới (Class 1).*
```json
[
  {
    "session_date": "2025-11-13",
    "attendance_status": "planned",
    "class_code": "FOUND-F2-2025",
    "session_topic": "Vocabulary Building - Food and Shopping",
    "start_time": "18:00:00",
    "end_time": "21:30:00",
    "display_status": "planned"
  },
  {
    "session_date": "2025-11-15",
    "attendance_status": "excused",
    "class_code": "FOUND-F2-2025",
    "session_topic": "Reading Simple Texts - Personal Information",
    "start_time": "18:00:00",
    "end_time": "21:30:00",
    "display_status": "Transferred Out"
  },
  {
    "session_date": "2025-11-15",
    "attendance_status": "planned",
    "class_code": "FOUND-F1-2025",
    "session_topic": "Basic Listening Comprehension",
    "start_time": "08:00:00",
    "end_time": "11:30:00",
    "display_status": "New Class"
  },
  {
    "session_date": "2025-11-18",
    "attendance_status": "excused",
    "class_code": "FOUND-F2-2025",
    "session_topic": "Reading Simple Texts - Daily Activities",
    "start_time": "18:00:00",
    "end_time": "21:30:00",
    "display_status": "Transferred Out"
  },
  {
    "session_date": "2025-11-18",
    "attendance_status": "planned",
    "class_code": "FOUND-F1-2025",
    "session_topic": "Present Simple Tense - Affirmative",
    "start_time": "08:00:00",
    "end_time": "11:30:00",
    "display_status": "New Class"
  }
]
```

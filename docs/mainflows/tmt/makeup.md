# Luồng Học Viên Xin Học Bù (Makeup Request)

Tài liệu này mô tả chi tiết luồng xử lý khi một học viên tạo và gửi yêu cầu học bù cho một buổi đã nghỉ hoặc sẽ nghỉ trong tương lai. Các truy vấn SQL và kết quả mong đợi được cung cấp dựa trên `seed-data.sql` và ngày thực hiện test là `2025-10-31`.

---

## PHẦN 1: HỌC VIÊN TẠO YÊU CẦU HỌC BÙ

### Bước 1-8: Khởi tạo Request
1.  **Login**: Học viên đăng nhập vào hệ thống.
2.  **Navigate**: Học viên vào menu "My Requests".
3.  **Click "Tạo Request Mới"**: Mở modal form tạo request.
4.  **Chọn Loại Request**: Học viên chọn "Makeup" (Học bù).
5.  **Chọn Option**: Hệ thống hiển thị 2 lựa chọn:
    *   "Học bù cho buổi đã nghỉ".
    *   "Đăng ký học bù trước cho buổi tương lai".

---

### PHẦN 1A: CASE 1 - HỌC BÙ CHO BUỔI ĐÃ NGHỈ

#### Bước 9A: Chọn option "Học bù cho buổi đã nghỉ"
Học viên chọn radio button tương ứng.

#### Bước 10A: Hệ thống load danh sách các buổi đã nghỉ
System thực hiện query để lấy danh sách các buổi học viên đã vắng mặt và chưa có yêu cầu học bù nào được duyệt hoặc đang chờ xử lý.

**Context Test:**
*   **Ngày hiện tại**: `2025-10-31`
*   **Học viên**: `Student ID = 7` (Bui Van Hieu)
*   **Lớp**: `Class ID = 2` (FOUND-F2-2025 - Foundation F2 - Evening Online)
*   **Dữ liệu vắng**: Sinh viên 7 đã vắng 2 buổi: `session_id = 105` (ngày 2025-10-09) và `session_id = 110` (ngày 2025-10-21).

```sql
-- Lấy danh sách buổi đã nghỉ của sinh viên 7
SELECT
    ss.session_id,
    ss.attendance_status,
    s.date AS session_date,
    s.course_session_id, -- CRITICAL: để match với các buổi học bù
    c.code AS class_code,
    c.name AS class_name,
    cs.topic AS session_title,
    (CURRENT_DATE - s.date) AS days_ago
FROM student_session ss
JOIN session s ON ss.session_id = s.id
JOIN "class" c ON s.class_id = c.id
LEFT JOIN course_session cs ON s.course_session_id = cs.id
WHERE ss.student_id = 7  -- Student 7
    AND ss.attendance_status = 'absent'
    AND s.status = 'done'
    -- Policy: Chỉ cho phép học bù cho các buổi nghỉ trong vòng 1 năm (để test, thực tế có thể là 8 tuần)
    AND s.date >= (CURRENT_DATE - INTERVAL '1 year')
    -- Loại trừ các buổi đã có request học bù (pending hoặc approved)
    AND NOT EXISTS (
        SELECT 1 FROM student_request sr
        WHERE sr.student_id = ss.student_id
          AND sr.target_session_id = ss.session_id
          AND sr.request_type = 'makeup'
          AND sr.status IN ('pending', 'approved')
    )
ORDER BY s.date DESC;
```

**Expected Output (khi chạy vào ngày 2025-10-31):**
```json
[
  {
    "session_id": 110,
    "attendance_status": "absent",
    "session_date": "2025-10-21",
    "course_session_id": 10,
    "class_code": "FOUND-F2-2025",
    "class_name": "Foundation F2 - Evening Online",
    "session_title": "Prepositions of Time and Place",
    "days_ago": 10
  },
  {
    "session_id": 105,
    "attendance_status": "absent",
    "session_date": "2025-10-09",
    "course_session_id": 5,
    "class_code": "FOUND-F2-2025",
    "class_name": "Foundation F2 - Evening Online",
    "session_title": "Simple Questions and Answers - Wh-questions",
    "days_ago": 22
  }
]
```

#### Bước 11A-12A: Chọn buổi đã nghỉ
Học viên chọn một buổi từ danh sách, ví dụ: **Session 105** (ngày 2025-10-09).

#### Bước 13A: Hệ thống tìm các buổi học bù khả dụng
Dựa vào `course_session_id` của buổi đã nghỉ, hệ thống tìm các buổi học khác có cùng nội dung, còn chỗ trống và diễn ra trong tương lai.

**Context Test:**
*   **Buổi đã nghỉ**: `session_id = 105` (có `course_session_id = 5`).
*   **Lớp gốc**: `class_id = 2` (Online, Branch 1).
*   **Yêu cầu**: Tìm các buổi học khác cũng dạy `course_session_id = 5`.

```sql
-- Tìm các buổi học bù khả dụng cho session 105
WITH target_info AS (
    -- Lấy thông tin buổi học gốc bị vắng
    SELECT
        s.course_session_id,
        s.class_id,
        c.branch_id,
        c.modality,
        s.date AS target_date
    FROM session s
    JOIN "class" c ON s.class_id = c.id
    WHERE s.id = 105  -- Buổi học gốc student 7 đã vắng
)
SELECT
    s.id AS makeup_session_id,
    s.date AS makeup_date,
    c.id AS class_id,
    c.code AS class_code,
    c.name AS class_name,
    c.modality,
    b.name AS branch_name,
    -- Tính số chỗ trống
    c.max_capacity - COUNT(DISTINCT e.student_id) AS available_slots,
    -- Lấy danh sách giáo viên
    STRING_AGG(DISTINCT ua.full_name, ', ') AS teachers,
    -- Priority scoring để sắp xếp
    CASE
        WHEN b.id = ti.branch_id AND c.modality = ti.modality THEN 1 -- Cùng chi nhánh và hình thức
        WHEN b.id = ti.branch_id THEN 2                               -- Cùng chi nhánh
        WHEN c.modality = ti.modality THEN 3                           -- Cùng hình thức
        ELSE 4
    END AS priority_level
FROM session s
JOIN "class" c ON s.class_id = c.id
JOIN branch b ON c.branch_id = b.id
CROSS JOIN target_info ti
LEFT JOIN enrollment e ON c.id = e.class_id AND e.status = 'enrolled'
LEFT JOIN teaching_slot ts ON s.id = ts.session_id
LEFT JOIN teacher t ON ts.teacher_id = t.id
LEFT JOIN user_account ua ON t.user_account_id = ua.id
WHERE
    -- CRITICAL: Cùng nội dung học
    s.course_session_id = ti.course_session_id
    -- Không phải lớp gốc
    AND c.id != ti.class_id
    -- Buổi học chưa diễn ra
    AND s.status = 'planned'
    AND s.date >= CURRENT_DATE
GROUP BY s.id, c.id, b.id, ti.branch_id, ti.modality, ti.target_date
-- Lọc lớp còn chỗ trống
HAVING c.max_capacity > COUNT(DISTINCT e.student_id)
-- Sắp xếp theo độ ưu tiên
ORDER BY priority_level ASC, s.date ASC
LIMIT 20;
```

**Expected Output (khi chạy vào ngày 2025-10-31):**
*Ghi chú: `seed-data.sql` tạo sẵn 2 buổi học bù `250` và `251` cho `course_session_id = 5`. Ngày của các buổi này được tính dựa trên `CURRENT_DATE`.*
```json
[
  {
    "makeup_session_id": 250,
    "makeup_date": "2025-11-07",
    "class_id": 3,
    "class_code": "FOUND-F3-2025",
    "class_name": "Foundation F3 - Afternoon Hybrid",
    "modality": "hybrid",
    "branch_name": "TMS Ha Noi Branch 01",
    "available_slots": 0,
    "teachers": "John Smith, Anna Martinez",
    "priority_level": 2
  },
  {
    "makeup_session_id": 251,
    "makeup_date": "2025-11-10",
    "class_id": 1,
    "class_code": "FOUND-F1-2025",
    "class_name": "Foundation F1 - Morning Offline",
    "modality": "offline",
    "branch_name": "TMS Ha Noi Branch 01",
    "available_slots": 15,
    "teachers": "John Smith",
    "priority_level": 2
  }
]
```

---

### PHẦN 1B: CASE 2 - ĐĂNG KÝ HỌC BÙ TRƯỚC
*(Luồng này tương tự Absence Request, sau khi chọn buổi sẽ nghỉ, hệ thống sẽ thực hiện query tương tự Bước 13A để tìm buổi học bù phù hợp.)*

---

## PHẦN 2: SUBMIT YÊU CẦU

#### Bước 15-18: Chọn buổi học bù và điền form
Học viên chọn một buổi học bù từ danh sách (ví dụ: **Session 251**), điền lý do và nhấn "Submit".

#### Bước 19: Backend Validation
Hệ thống kiểm tra các điều kiện trước khi lưu.

```sql
-- Check 1: Duplicate request
SELECT COUNT(*) FROM student_request
WHERE student_id = 7 AND target_session_id = 105 AND request_type = 'makeup' AND status IN ('pending', 'approved');
-- Expected: 0

-- Check 2: Same content (course_session_id)
SELECT s1.course_session_id = s2.course_session_id AS is_same_content
FROM session s1, session s2
WHERE s1.id = 105 AND s2.id = 251;
-- Expected: true

-- Check 3: Capacity
SELECT c.max_capacity > COUNT(e.student_id) AS has_capacity
FROM session s
JOIN "class" c ON s.class_id = c.id
LEFT JOIN enrollment e ON c.id = e.class_id AND e.status = 'enrolled'
WHERE s.id = 251
GROUP BY c.id;
-- Expected: true
```

#### Bước 20: Insert `student_request` vào database
Hệ thống lưu yêu cầu mới vào CSDL.

**Context Test:**
*   **Học viên**: `student_id = 7` (có `user_id = 106`).
*   **Buổi nghỉ**: `target_session_id = 105` (thuộc `class_id = 2`).
*   **Buổi học bù**: `makeup_session_id = 251`.
*   **Ngày gửi**: `2025-10-31`.

```sql
-- Insert makeup request cho Student 7
INSERT INTO student_request (
    student_id,
    current_class_id,
    target_session_id,
    makeup_session_id,
    request_type,
    status,
    request_reason,
    note,
    submitted_at,
    submitted_by,
    created_at,
    updated_at
) VALUES (
    7,                       -- Student ID
    2,                       -- Class ID của buổi nghỉ
    105,                     -- Buổi đã nghỉ
    251,                     -- Buổi muốn học bù
    'makeup',
    'pending',
    'I was sick and could not attend the class. I would like to make up this session to catch up.',
    'Makeup for "Simple Questions and Answers" session',
    '2025-10-31 10:00:00+07', -- Giả định thời gian submit
    106,                     -- User ID của Student 7
    '2025-10-31 10:00:00+07',
    '2025-10-31 10:00:00+07'
) RETURNING id;
```
**Expected Return**: ID của request mới (ví dụ: `12`).

---

## PHẦN 3: GIÁO VỤ XỬ LÝ YÊU CẦU

#### Bước 24-25: Giáo vụ vào trang quản lý request
Giáo vụ (ví dụ: `user_id = 4`) vào trang "Request Management" và xem các yêu cầu đang chờ xử lý.

#### Bước 26: Hệ thống query danh sách pending makeup requests

```sql
-- Lấy danh sách makeup request đang chờ xử lý
SELECT
    sr.id AS request_id,
    sr.submitted_at,
    sr.request_reason,
    st.student_code,
    ua_student.full_name AS student_name,
    s_target.date AS target_date,
    c_target.code AS target_class_code,
    s_makeup.date AS makeup_date,
    c_makeup.code AS makeup_class_code,
    b_makeup.name AS makeup_branch_name
FROM student_request sr
JOIN student st ON sr.student_id = st.id
JOIN user_account ua_student ON st.user_id = ua_student.id
JOIN session s_target ON sr.target_session_id = s_target.id
JOIN "class" c_target ON s_target.class_id = c_target.id
JOIN branch b_target ON c_target.branch_id = b_target.id
JOIN session s_makeup ON sr.makeup_session_id = s_makeup.id
JOIN "class" c_makeup ON s_makeup.class_id = c_makeup.id
JOIN branch b_makeup ON c_makeup.branch_id = b_makeup.id
WHERE sr.request_type = 'makeup'
    AND sr.status = 'pending'
    -- Giáo vụ chỉ xem các request thuộc chi nhánh mình quản lý
    AND b_target.id IN (SELECT branch_id FROM user_branches WHERE user_id = 4)
ORDER BY sr.submitted_at ASC;
```

**Expected Output (sau khi insert request ở Bước 20):**
```json
[
  {
    "request_id": 12,
    "submitted_at": "2025-10-31T10:00:00+07:00",
    "request_reason": "I was sick and could not attend the class. I would like to make up this session to catch up.",
    "student_code": "STD-0007",
    "student_name": "Bui Van Hieu",
    "target_date": "2025-10-09",
    "target_class_code": "FOUND-F2-2025",
    "makeup_date": "2025-11-10",
    "makeup_class_code": "FOUND-F1-2025",
    "makeup_branch_name": "TMS Ha Noi Branch 01"
  }
]
```

#### Bước 28: Giáo vụ xem chi tiết request
Giáo vụ click vào request để xem thông tin chi tiết.

```sql
-- Lấy chi tiết của một makeup request
-- (Query này tương tự query trong file gốc nhưng WHERE theo ID cụ thể)
SELECT
    sr.id AS request_id,
    sr.status,
    sr.request_reason,
    st.student_code,
    ua_student.full_name AS student_name,
    -- Thông tin buổi nghỉ
    s_target.id AS target_session_id,
    s_target.date AS target_session_date,
    c_target.code AS target_class_code,
    cs_target.topic AS target_session_topic,
    -- Thông tin buổi học bù
    s_makeup.id AS makeup_session_id,
    s_makeup.date AS makeup_session_date,
    c_makeup.code AS makeup_class_code,
    c_makeup.modality AS makeup_modality,
    b_makeup.name AS makeup_branch_name,
    -- Thông tin giáo viên buổi bù
    (SELECT STRING_AGG(ua.full_name, ', ') FROM teaching_slot ts
     JOIN teacher t ON ts.teacher_id = t.id
     JOIN user_account ua ON t.user_account_id = ua.id
     WHERE ts.session_id = sr.makeup_session_id) AS makeup_teachers
FROM student_request sr
JOIN student st ON sr.student_id = st.id
JOIN user_account ua_student ON st.user_id = ua_student.id
JOIN session s_target ON sr.target_session_id = s_target.id
JOIN "class" c_target ON s_target.class_id = c_target.id
LEFT JOIN course_session cs_target ON s_target.course_session_id = cs_target.id
JOIN session s_makeup ON sr.makeup_session_id = s_makeup.id
JOIN "class" c_makeup ON s_makeup.class_id = c_makeup.id
JOIN branch b_makeup ON c_makeup.branch_id = b_makeup.id
WHERE sr.id = 12; -- ID của request vừa tạo
```

**Expected Output:**
```json
[
  {
    "request_id": 12,
    "status": "pending",
    "request_reason": "I was sick and could not attend the class. I would like to make up this session to catch up.",
    "student_code": "STD-0007",
    "student_name": "Bui Van Hieu",
    "target_session_id": 105,
    "target_session_date": "2025-10-09",
    "target_class_code": "FOUND-F2-2025",
    "target_session_topic": "Simple Questions and Answers - Wh-questions",
    "makeup_session_id": 251,
    "makeup_session_date": "2025-11-10",
    "makeup_class_code": "FOUND-F1-2025",
    "makeup_modality": "offline",
    "makeup_branch_name": "TMS Ha Noi Branch 01",
    "makeup_teachers": "John Smith"
  }
]
```

---

## PHẦN 4: GIÁO VỤ PHÊ DUYỆT YÊU CẦU

#### Bước 32-33: Giáo vụ nhấn "Approve" và xác nhận

#### Bước 34: Thực hiện transaction approve
Hệ thống thực hiện một transaction để đảm bảo tính toàn vẹn dữ liệu.

**Context Test:**
*   **Request ID**: `12` (từ bước 20).
*   **Giáo vụ**: `user_id = 4`.
*   **Học viên**: `student_id = 7`.
*   **Buổi nghỉ**: `session_id = 105`.
*   **Buổi bù**: `session_id = 251`.

```sql
-- Transaction phê duyệt yêu cầu học bù
BEGIN;

-- Step 1: Cập nhật trạng thái request -> 'approved'
UPDATE student_request
SET status = 'approved',
    decided_by = 4, -- ID của giáo vụ
    decided_at = '2025-10-31 11:00:00+07',
    updated_at = '2025-10-31 11:00:00+07'
WHERE id = 12 AND status = 'pending';

-- Step 2: Cập nhật buổi nghỉ -> 'excused'
-- Ghi chú: Dùng COALESCE để nối chuỗi an toàn nếu note đã có giá trị
UPDATE student_session
SET attendance_status = 'excused',
    note = COALESCE(note || E'
', '') || 'Excused - Approved for makeup session 251 on 2025-10-31',
    recorded_at = '2025-10-31 11:00:00+07'
WHERE student_id = 7 AND session_id = 105;

-- Step 3: Thêm học viên vào buổi học bù
-- Dùng ON CONFLICT để xử lý trường hợp record đã tồn tại (ví dụ: học viên đã được thêm vào buổi này trước đó)
INSERT INTO student_session (
    student_id,
    session_id,
    is_makeup,
    original_session_id,
    attendance_status,
    note
) VALUES (
    7,          -- Student ID
    251,        -- Makeup Session ID
    TRUE,
    105,        -- Original Session ID
    'planned',
    'Makeup for missed session 105'
)
ON CONFLICT (student_id, session_id) DO UPDATE
SET is_makeup = TRUE,
    original_session_id = 105,
    note = EXCLUDED.note,
    attendance_status = 'planned';

COMMIT;
```

#### Xác minh kết quả sau khi Approve
```sql
-- 1. Trạng thái request đã được duyệt
SELECT id, status, decided_by FROM student_request WHERE id = 12;
-- Expected: { "id": 12, "status": "approved", "decided_by": 4 }

-- 2. Buổi nghỉ đã được chuyển thành 'excused'
SELECT student_id, session_id, attendance_status, note FROM student_session WHERE student_id = 7 AND session_id = 105;
-- Expected: { "student_id": 7, "session_id": 105, "attendance_status": "excused", "note": "...Excused..." }

-- 3. Học viên đã được thêm vào buổi học bù
SELECT student_id, session_id, is_makeup, original_session_id, attendance_status FROM student_session WHERE student_id = 7 AND session_id = 251;
-- Expected: { "student_id": 7, "session_id": 251, "is_makeup": true, "original_session_id": 105, "attendance_status": "planned" }
```

---

## PHẦN 5: CÁC BƯỚC TIẾP THEO
*   **Gửi Email**: Hệ thống gửi email thông báo cho học viên và giáo viên của buổi học bù.
*   **Cập nhật Lịch học**: Lịch học của sinh viên được cập nhật với buổi học bù mới.
*   **Điểm danh**: Tại buổi học bù, giáo viên sẽ thấy sinh viên học bù trong danh sách điểm danh với một nhãn đặc biệt.
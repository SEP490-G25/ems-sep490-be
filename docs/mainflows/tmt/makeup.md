
STUDENT MAKEUP REQUEST
CÁC BƯỚC THỰC HIỆN (STEP-BY-STEP) - PHIÊN BẢN MỚI
PHẦN 1: HỌC VIÊN TẠO YÊU CẦU HỌC BÙ
Bước 1: Login hệ thống

Bước 2: Vào menu "My Requests" ở sidebar

Bước 3: Hệ thống load danh sách requests
Dựa dựa vào student_id

Bước 4: Hệ thống hiển thị trang My Requests

Bước 5: Click button "Tạo Request Mới"

Bước 6: Hệ thống hiển thị modal form tạo request

Bước 7: Học viên chọn loại request = "Makeup" (Học bù)

Bước 8: Hệ thống hiển thị 2 options cho Makeup Request
System hiển thị 2 radio button options:
Option 1: "Học bù cho buổi đã nghỉ"
Option 2: "Đăng ký học bù trước cho buổi tương lai"

PHẦN 1A: CASE 1 - HỌC BÙ CHO BUỔI ĐÃ NGHỈ
Bước 9A: Chọn option "Học bù cho buổi đã nghỉ"
Học viên chọn radio button "Học bù cho buổi đã nghỉ"

Bước 10A: Hệ thống load danh sách buổi đã nghỉ (trong X tuần gần nhất)
System thực hiện query:

-- =====================================================
-- QUERY: Lấy danh sách buổi đã nghỉ (Missed Sessions)
-- Use Case: Student 14 (S014 - Mac Thi Lan) checks missed sessions
-- =====================================================

SELECT
    ss.session_id,
    ss.attendance_status,
    s.date AS session_date,
    s.course_session_id, -- CRITICAL: để match makeup sessions
    c.code AS class_code,
    c.name AS class_name,
    cs.topic AS session_title, -- Nội dung buổi học
    cs.student_task,
    (CURRENT_DATE - s.date) AS days_ago,
    ROUND((CURRENT_DATE - s.date)::NUMERIC / 7, 1) AS weeks_ago
FROM student_session ss
JOIN session s ON ss.session_id = s.id
JOIN "class" c ON s.class_id = c.id
LEFT JOIN course_session cs ON s.course_session_id = cs.id
WHERE ss.student_id = 14  -- Student 14 (S014)
    AND ss.attendance_status IN ('absent', 'late')
    AND s.status = 'done'
    AND s.date >= (CURRENT_DATE - INTERVAL '4 weeks') -- Policy: 4 tuần
    AND NOT EXISTS (
        SELECT 1 FROM student_request sr
        WHERE sr.target_session_id = ss.session_id
            AND sr.request_type = 'makeup'
            AND sr.status IN ('pending', 'approved')
    )
ORDER BY s.date DESC;

-- Expected Output (nếu student 14 có absence ở session 53):
-- session_id: 53
-- attendance_status: 'absent'
-- session_date: CURRENT_DATE - 13 days
-- course_session_id: 14
-- class_code: 'B1-IELTS-001'
-- class_name: 'IELTS Foundation B1 - Afternoon'
-- session_title: 'Listening Section 1 - Forms & Details'
-- days_ago: 13
-- weeks_ago: 1.9


Bước 11A: Hiển thị danh sách buổi đã nghỉ

Bước 12A: Chọn buổi đã nghỉ
Học viên chọn một buổi học đã nghỉ từ dropdown

Bước 13A: Hệ thống tìm các buổi học bù khả dụng
System thực hiện query:

-- =====================================================
-- QUERY TÌM BUỔI HỌC BÙ KHẢ DỤNG (Makeup Session Search)
-- Use Case: Student 14 missed session 53 (Class 3, IELTS B1)
--           course_session_id = 14 (Listening Section 1)
-- Find available makeup sessions with same course_session_id
-- =====================================================

WITH target_info AS (
    -- Lấy thông tin buổi học bị vắng
    SELECT
        s.course_session_id,
        s.class_id,
        c.branch_id,
        c.modality,
        c.code AS target_class_code,
        c.name AS target_class_name,
        s.date AS target_date
    FROM session s
    JOIN "class" c ON s.class_id = c.id
    WHERE s.id = 53  -- Session 53: student 14 bị vắng (IELTS B1, course_session_id = 14)
)
SELECT
    s.id AS makeup_session_id,
    s.date AS makeup_date,
    -- Tính số ngày chênh lệch
    s.date - ti.target_date AS days_difference,
    c.code AS class_code,
    c.name AS class_name,
    c.modality,
    b.name AS branch_name,
    -- Tính số chỗ trống
    c.max_capacity - COALESCE(COUNT(DISTINCT e.student_id), 0) AS available_slots,
    c.max_capacity,
    COALESCE(COUNT(DISTINCT e.student_id), 0) AS enrolled_count,
    -- Lấy danh sách giáo viên (teacher -> user_account -> full_name)
    STRING_AGG(DISTINCT ua.full_name, ', ' ORDER BY ua.full_name) AS teachers,
    -- Flags quan trọng cho việc sắp xếp ưu tiên
    CASE WHEN b.id = ti.branch_id THEN true ELSE false END AS is_same_branch,
    CASE WHEN c.modality = ti.modality THEN true ELSE false END AS is_same_modality,
    -- Priority scoring
    CASE
        WHEN b.id = ti.branch_id AND c.modality = ti.modality THEN 1  -- Same branch + modality (best)
        WHEN b.id = ti.branch_id THEN 2                                -- Same branch only
        WHEN c.modality = ti.modality THEN 3                           -- Same modality only
        ELSE 4                                                          -- Different branch & modality
    END AS priority_level,
    -- Thêm thông tin session gốc
    ti.target_class_code,
    ti.target_class_name,
    ti.target_date,
    cs.topic AS session_topic,
    cs.student_task
FROM session s
JOIN "class" c ON s.class_id = c.id
JOIN branch b ON c.branch_id = b.id
CROSS JOIN target_info ti
JOIN course_session cs ON s.course_session_id = cs.id
LEFT JOIN enrollment e ON c.id = e.class_id AND e.status IN ('enrolled', 'waitlisted')
LEFT JOIN teaching_slot ts ON s.id = ts.session_id AND ts.role = 'primary'
LEFT JOIN teacher t ON ts.teacher_id = t.id
LEFT JOIN user_account ua ON t.user_account_id = ua.id
WHERE
    -- CRITICAL: Cùng nội dung học (course_session_id)
    s.course_session_id = ti.course_session_id
    -- Không phải buổi gốc bị vắng
    AND s.id != 53
    -- Không phải lớp gốc (cho phép học bù ở lớp khác)
    AND c.id != ti.class_id
    -- Chỉ lấy buổi đang planned (chưa diễn ra)
    AND s.status = 'planned'
    -- Chỉ lấy buổi trong tương lai
    AND s.date >= CURRENT_DATE
GROUP BY
    s.id,
    s.date,
    c.id,
    c.code,
    c.name,
    c.max_capacity,
    c.modality,
    b.id,
    b.name,
    ti.branch_id,
    ti.modality,
    ti.target_class_code,
    ti.target_class_name,
    ti.target_date,
    cs.topic,
    cs.student_task
-- Lọc chỉ lấy class còn chỗ trống
HAVING c.max_capacity > COALESCE(COUNT(DISTINCT e.student_id), 0)
-- Sắp xếp theo độ ưu tiên
ORDER BY
    priority_level ASC,      -- Ưu tiên branch + modality trước
    s.date ASC,              -- Sau đó theo ngày gần nhất
    available_slots DESC     -- Cuối cùng theo số chỗ trống nhiều nhất
LIMIT 20;

-- Expected Output:
-- makeup_session_id: 1000
-- makeup_date: CURRENT_DATE + 5 days
-- days_difference: 18 (5 - (-13))
-- class_code: 'B2-IELTS-001'
-- class_name: 'IELTS Intermediate B2 - Evening'
-- modality: 'hybrid'
-- branch_name: 'Main Campus'
-- available_slots: 20 (depends on class 4 capacity & enrollments)
-- is_same_branch: true
-- is_same_modality: false (target='offline', makeup='hybrid')
-- priority_level: 2 (same branch only)
-- session_topic: 'Listening Section 1 - Forms & Details'

Bước 14A: Hiển thị danh sách buổi học bù khả dụng

PHẦN 1B: CASE 2 - ĐĂNG KÝ HỌC BÙ TRƯỚC CHO BUỔI TƯƠNG LAI
Bước 9B: Chọn option "Đăng ký học bù trước cho buổi tương lai"

Bước 10B: Chọn ngày của buổi học sắp tới sẽ nghỉ
Học viên chọn ngày từ date picker (date >= TODAY)

Bước 11B: Hệ thống load danh sách lớp theo ngày
System query classes có session vào ngày đã chọn (giống absence flow)

Bước 12B: Học viên chọn lớp từ dropdown

Bước 13B: Hệ thống load sessions trong ngày đó
System query sessions của lớp trong ngày (giống absence flow)
Chỉ hiển thị sessions với status = 'planned'

Bước 14B: Chọn buổi học sẽ nghỉ
Học viên chọn buổi học sắp tới mà sẽ không thể tham gia
Lưu ý: Buổi này sẽ được đánh dấu 'excused' sau khi approve

Bước 15B: Hệ thống tìm các buổi học bù khả dụng (tương tự Bước 13A)
System thực hiện complex query tìm makeup sessions:
Cùng course_session_id
Trong tương lai (date >= CURRENT_DATE)
Có chỗ trống
Có thể online/offline/chi nhánh khác

Bước 16B: Hiển thị danh sách buổi học bù khả dụng (tương tự Bước 14A)
Hiển thị list với đầy đủ thông tin như Case 1

PHẦN 2: TIẾP TỤC CHUNG CHO CẢ 2 CASES
Bước 15 (chung): Chọn buổi học bù phù hợp
Học viên chọn một buổi học bù từ danh sách
Có thể filter theo:
Chi nhánh
Modality (Online/Offline/Hybrid)
Ngày giờ
Giáo viên

Bước 16 (chung): Điền form yêu cầu học bù
Lý do cần học bù (required, min 10 chars)
Ghi chú (optional)
Form hiển thị tóm tắt:
Buổi gốc: Date, Class, Content, Status (missed/will miss)
Buổi học bù: Date, Class, Branch, Modality, Teacher

Bước 17 (chung): Học viên nhấn nút "Submit Request"

Bước 18 (chung): Frontend validation
Hệ thống kiểm tra:
Loại request đã chọn
Target session và makeup session đều đã chọn
Lý do tối thiểu 10 ký tự
Check schedule conflict (nếu có lớp khác cùng giờ)
Warning nếu buổi học bù ở chi nhánh khác hoặc online

Bước 19 (chung): Backend validation
System kiểm tra:
Target session có tồn tại và hợp lệ không?
Makeup session có tồn tại và còn chỗ không?
Hai sessions có cùng course_session_id không? (cùng nội dung)
Đã có request duplicate chưa? (pending for same sessions)
Case 1: Target session đã diễn ra và status = 'absent'/'excused'?
Case 2: Target session chưa diễn ra và status = 'planned'?
Kiểm tra policy: buổi đã nghỉ không quá X tuần

-- =====================================================
-- VALIDATION QUERIES
-- =====================================================

-- Check 1: Kiểm tra duplicate request
SELECT COUNT(*) AS duplicate_count
FROM student_request
WHERE student_id = 14             -- Student 14
    AND target_session_id = 53    -- Session 53 (missed session)
    AND request_type = 'makeup'
    AND status IN ('pending', 'approved');
-- Expected: 0 (không có request trùng)

-- Check 2: Verify same content (cùng course_session_id)
SELECT
    s1.id AS target_session_id,
    s1.course_session_id AS target_course_session_id,
    s2.id AS makeup_session_id,
    s2.course_session_id AS makeup_course_session_id,
    s1.course_session_id = s2.course_session_id AS is_same_content,
    cs.topic AS session_topic
FROM session s1
CROSS JOIN session s2
LEFT JOIN course_session cs ON s1.course_session_id = cs.id
WHERE s1.id = 53         -- Target session
    AND s2.id = 1000;    -- Makeup session 1000
-- Expected: is_same_content = true (both have course_session_id = 14)

-- Check 3: Kiểm tra capacity còn chỗ trống
SELECT
    s.id AS session_id,
    c.id AS class_id,
    c.code AS class_code,
    c.max_capacity,
    COUNT(e.student_id) AS enrolled_count,
    c.max_capacity - COUNT(e.student_id) AS available_slots,
    c.max_capacity > COUNT(e.student_id) AS has_capacity
FROM session s
JOIN "class" c ON s.class_id = c.id
LEFT JOIN enrollment e ON c.id = e.class_id AND e.status IN ('enrolled', 'waitlisted')
WHERE s.id = 1000      -- Makeup session 1000
GROUP BY s.id, c.id, c.code, c.max_capacity;
-- Expected: has_capacity = true (còn chỗ trống)


Bước 20 (chung): Insert student_request vào database
System thực hiện INSERT student_request:

-- =====================================================
-- INSERT MAKEUP REQUEST
-- =====================================================

INSERT INTO student_request (
    student_id,
    current_class_id,        -- Class của target session
    target_session_id,       -- Buổi gốc (đã nghỉ hoặc sẽ nghỉ)
    makeup_session_id,       -- Buổi học bù đã chọn
    request_type,
    status,
    note,                    -- Lý do từ form
    submitted_at,
    submitted_by,            -- User ID của student
    created_at,
    updated_at
) VALUES (
    14,                      -- Student 14 (S014 - Mac Thi Lan)
    3,                       -- Class 3 (IELTS B1)
    53,                      -- Session 53 (missed session - course_session_id = 14)
    1000,                    -- Session 1000 (makeup session - course_session_id = 14, planned)
    'makeup',                -- Request type
    'pending',               -- Status
    'I was sick and could not attend the listening class. I would like to make up this session to catch up with the course content.', -- Reason
    CURRENT_TIMESTAMP,       -- Submitted at
    26,                      -- User ID of student 14 (user_account_id = 26)
    CURRENT_TIMESTAMP,
    CURRENT_TIMESTAMP
) RETURNING id;

-- Expected Output:
-- id: <new_request_id> (e.g., 4 or higher)


Bước 21 (chung): Hệ thống hiển thị thông báo thành công
Hiển thị success notification: "Yêu cầu học bù đã được gửi thành công"
Hiển thị tóm tắt:
Buổi gốc: [Date, Class]
Buổi học bù: [Date, Class, Branch, Modality]
Status: Pending
Đóng modal
Refresh danh sách requests trong tab "Requests tôi đã gửi"

Bước 22 (chung): Gửi email thông báo cho Academic Affairs (async)
System gửi email bất đồng bộ tới Academic Affairs:
"Học viên [Student Name] yêu cầu học bù"
"Buổi gốc: [Date, Class, Content]" (missed/will miss)
"Buổi học bù: [Date, Class, Branch, Modality]"
"Chỗ trống: X/Y"
Link đến request detail

Bước 23 (chung): Request xuất hiện trong danh sách với status "Pending"
Request hiển thị trong tab "Requests tôi đã gửi"
Badge: Pending (Chờ phê duyệt)
Icon: Makeup Request
Hiển thị: Target session + Makeup session info

PHẦN 3: GIÁO VỤ XỬ LÝ YÊU CẦU
Bước 24: Academic Affairs nhận email thông báo
Giáo vụ nhận email thông báo có yêu cầu học bù mới

Bước 25: Login hệ thống và vào menu "Pending Requests"
Giáo vụ đăng nhập và truy cập "Request Management"
Filter by type: "Makeup"

Bước 26: Hệ thống query danh sách pending makeup requests
System thực hiện query:

-- =====================================================
-- QUERY: List Pending Makeup Requests (for Academic Affairs)
-- Use Case: Academic Affairs 1 (user_id = 4) xem pending requests
-- =====================================================

SELECT
    sr.id AS request_id,
    sr.submitted_at,
    sr.note AS reason,
    -- Student info
    st.id AS student_id,
    st.student_code,
    ua_student.full_name AS student_name,
    ua_student.email AS student_email,
    ua_student.phone AS student_phone,
    -- Target session (buổi gốc - đã nghỉ)
    s_target.id AS target_session_id,
    s_target.date AS target_date,
    c_target.code AS target_class_code,
    c_target.name AS target_class_name,
    cs_target.topic AS target_session_topic,
    ss_target.attendance_status AS target_status,
    -- Makeup session (buổi học bù)
    s_makeup.id AS makeup_session_id,
    s_makeup.date AS makeup_date,
    c_makeup.code AS makeup_class_code,
    c_makeup.name AS makeup_class_name,
    cs_makeup.topic AS makeup_session_topic,
    b_makeup.name AS makeup_branch_name,
    c_makeup.modality AS makeup_modality,
    -- Validation flags
    s_target.course_session_id = s_makeup.course_session_id AS is_same_content,
    b_target.id = b_makeup.id AS is_same_branch,
    c_makeup.max_capacity - COUNT(DISTINCT e_makeup.student_id) AS available_slots,
    c_makeup.max_capacity AS makeup_class_capacity,
    -- Days difference
    s_makeup.date - s_target.date AS days_difference
FROM student_request sr
JOIN student st ON sr.student_id = st.id
JOIN user_account ua_student ON st.user_id = ua_student.id
JOIN session s_target ON sr.target_session_id = s_target.id
JOIN "class" c_target ON s_target.class_id = c_target.id
JOIN branch b_target ON c_target.branch_id = b_target.id
LEFT JOIN course_session cs_target ON s_target.course_session_id = cs_target.id
LEFT JOIN student_session ss_target ON sr.target_session_id = ss_target.session_id AND ss_target.student_id = st.id
JOIN session s_makeup ON sr.makeup_session_id = s_makeup.id
JOIN "class" c_makeup ON s_makeup.class_id = c_makeup.id
JOIN branch b_makeup ON c_makeup.branch_id = b_makeup.id
LEFT JOIN course_session cs_makeup ON s_makeup.course_session_id = cs_makeup.id
LEFT JOIN enrollment e_makeup ON c_makeup.id = e_makeup.class_id AND e_makeup.status IN ('enrolled', 'waitlisted')
WHERE sr.request_type = 'makeup'
    AND sr.status = 'pending'
    -- Academic Affairs chỉ xem requests thuộc branches mình quản lý
    AND (b_target.id IN (SELECT branch_id FROM user_branches WHERE user_id = 4)
         OR b_makeup.id IN (SELECT branch_id FROM user_branches WHERE user_id = 4))
GROUP BY
    sr.id, sr.submitted_at, sr.note,
    st.id, st.student_code, ua_student.full_name, ua_student.email, ua_student.phone,
    s_target.id, s_target.date, s_target.course_session_id,
    c_target.code, c_target.name, cs_target.topic, ss_target.attendance_status,
    s_makeup.id, s_makeup.date, s_makeup.course_session_id,
    c_makeup.code, c_makeup.name, c_makeup.modality, c_makeup.max_capacity,
    cs_makeup.topic, b_makeup.name,
    b_target.id, b_makeup.id
ORDER BY sr.submitted_at ASC;


Bước 27: Hệ thống hiển thị danh sách 
Branch_id -> select ra list các request

Bước 28: Click vào request để xem chi tiết
Giáo vụ click vào một row hoặc button "View Detail"

-- =====================================================
-- GET MAKEUP REQUEST DETAIL
-- Use Case: Academic Affairs xem chi tiết request_id = 4
-- =====================================================

SELECT
  sr.id AS request_id,
  sr.request_type,
  sr.status,
  sr.note AS reason,
  sr.submitted_at,
  sr.decided_at,
  -- Student info (detailed)
  st.id AS student_id,
  st.student_code,
  st.level AS education_level,
  ua_student.full_name AS student_name,
  ua_student.email AS student_email,
  ua_student.phone AS student_phone,
  ua_student.address AS student_address,
  -- Target Session info (buổi đã nghỉ)
  s_target.id AS target_session_id,
  s_target.date AS target_session_date,
  s_target.type AS target_session_type,
  s_target.status AS target_session_status,
  c_target.id AS target_class_id,
  c_target.code AS target_class_code,
  c_target.name AS target_class_name,
  cs_target.topic AS target_session_topic,
  cs_target.student_task AS target_student_task,
  tst_target.name AS target_time_slot_name,
  tst_target.start_time AS target_slot_start,
  tst_target.end_time AS target_slot_end,
  -- Makeup Session info (buổi học bù)
  s_makeup.id AS makeup_session_id,
  s_makeup.date AS makeup_session_date,
  s_makeup.type AS makeup_session_type,
  s_makeup.status AS makeup_session_status,
  c_makeup.id AS makeup_class_id,
  c_makeup.code AS makeup_class_code,
  c_makeup.name AS makeup_class_name,
  c_makeup.modality AS makeup_modality,
  c_makeup.max_capacity AS makeup_max_capacity,
  cs_makeup.topic AS makeup_session_topic,
  cs_makeup.student_task AS makeup_student_task,
  tst_makeup.name AS makeup_time_slot_name,
  tst_makeup.start_time AS makeup_slot_start,
  tst_makeup.end_time AS makeup_slot_end,
  -- Branch info
  b_target.name AS target_branch_name,
  b_makeup.name AS makeup_branch_name,
  -- Course info
  co.name AS course_name,
  co.code AS course_code,
  co.duration_weeks,
  co.session_per_week,
  -- Makeup teachers (aggregated as JSON)
  JSONB_AGG(
    JSONB_BUILD_OBJECT(
      'teacher_id', t_makeup.id,
      'name', ua_teacher_makeup.full_name,
      'email', ua_teacher_makeup.email,
      'role', ts_makeup.role,
      'skill', ts_makeup.skill
    ) ORDER BY ts_makeup.role
  ) FILTER (WHERE t_makeup.id IS NOT NULL) AS makeup_teachers,
  -- Absence statistics for this student in target class
  (
    SELECT COUNT(*)
    FROM student_session ss2
    JOIN session s2 ON ss2.session_id = s2.id
    WHERE ss2.student_id = st.id
      AND s2.class_id = c_target.id
      AND ss2.attendance_status IN ('absent', 'excused')
  ) AS total_absences,
  (
    SELECT COUNT(*)
    FROM student_session ss2
    JOIN session s2 ON ss2.session_id = s2.id
    WHERE ss2.student_id = st.id
      AND s2.class_id = c_target.id
  ) AS total_sessions,
  -- Absence percentage
  ROUND(
    (
      SELECT COUNT(*)
      FROM student_session ss2
      JOIN session s2 ON ss2.session_id = s2.id
      WHERE ss2.student_id = st.id
        AND s2.class_id = c_target.id
        AND ss2.attendance_status IN ('absent', 'excused')
    )::NUMERIC / NULLIF(
      (
        SELECT COUNT(*)
        FROM student_session ss2
        JOIN session s2 ON ss2.session_id = s2.id
        WHERE ss2.student_id = st.id
          AND s2.class_id = c_target.id
      ), 0
    ) * 100, 2
  ) AS absence_percentage,
  -- Days until makeup session
  (s_makeup.date - CURRENT_DATE) AS days_until_makeup_session,
  -- Enrollment info
  e.enrolled_at,
  e.status AS enrollment_status,
  -- Decider info (if decided)
  decider.full_name AS decided_by_name,
  decider.email AS decided_by_email
FROM student_request sr
JOIN student st ON sr.student_id = st.id
JOIN user_account ua_student ON st.user_id = ua_student.id
-- Target session
JOIN session s_target ON sr.target_session_id = s_target.id
JOIN "class" c_target ON s_target.class_id = c_target.id
JOIN branch b_target ON c_target.branch_id = b_target.id
LEFT JOIN course_session cs_target ON s_target.course_session_id = cs_target.id
LEFT JOIN time_slot_template tst_target ON s_target.time_slot_template_id = tst_target.id
-- Makeup session
JOIN session s_makeup ON sr.makeup_session_id = s_makeup.id
JOIN "class" c_makeup ON s_makeup.class_id = c_makeup.id
JOIN branch b_makeup ON c_makeup.branch_id = b_makeup.id
LEFT JOIN course_session cs_makeup ON s_makeup.course_session_id = cs_makeup.id
LEFT JOIN time_slot_template tst_makeup ON s_makeup.time_slot_template_id = tst_makeup.id
-- Course info (from target class)
JOIN course co ON c_target.course_id = co.id
-- Enrollment
LEFT JOIN enrollment e ON e.student_id = st.id AND e.class_id = c_target.id
-- Makeup session teachers
LEFT JOIN teaching_slot ts_makeup ON ts_makeup.session_id = s_makeup.id
LEFT JOIN teacher t_makeup ON ts_makeup.teacher_id = t_makeup.id
LEFT JOIN user_account ua_teacher_makeup ON t_makeup.user_account_id = ua_teacher_makeup.id
-- Decider
LEFT JOIN user_account decider ON sr.decided_by = decider.id
WHERE sr.id = 4  -- Request ID = 4
GROUP BY
  sr.id, sr.request_type, sr.status, sr.note, sr.submitted_at, sr.decided_at,
  st.id, st.student_code, st.level,
  ua_student.full_name, ua_student.email, ua_student.phone, ua_student.address,
  s_target.id, s_target.date, s_target.type, s_target.status,
  c_target.id, c_target.code, c_target.name,
  cs_target.topic, cs_target.student_task,
  tst_target.name, tst_target.start_time, tst_target.end_time,
  s_makeup.id, s_makeup.date, s_makeup.type, s_makeup.status,
  c_makeup.id, c_makeup.code, c_makeup.name, c_makeup.modality, c_makeup.max_capacity,
  cs_makeup.topic, cs_makeup.student_task,
  tst_makeup.name, tst_makeup.start_time, tst_makeup.end_time,
  b_target.name, b_makeup.name,
  co.name, co.code, co.duration_weeks, co.session_per_week,
  e.enrolled_at, e.status,
  decider.full_name, decider.email;


-- Expected output sẽ bao gồm đầy đủ thông tin của cả target session và makeup session

Bước 29: Hệ thống hiển thị chi tiết request

Bước 30: Review thông tin

Bước 31: Quyết định?
Giáo vụ đưa ra quyết định: Approve hoặc Reject
PHẦN 4A: TRƯỜNG HỢP APPROVE (PHÊ DUYỆT)

Bước 32:
Giáo vụ nhấn button "Approve"
(Optional) Có thể thêm approval note

Bước 33: Confirm approval
System hiển thị confirmation dialog:
"Bạn có chắc chắn muốn phê duyệt yêu cầu học bù này?"
Tóm tắt: Target session → Makeup session
Warning nếu makeup session ở chi nhánh khác hoặc modality khác
Giáo vụ confirm

Bước 34: Thực hiện transaction approve
System thực hiện BEGIN TRANSACTION:

-- =====================================================
-- APPROVE MAKEUP REQUEST TRANSACTION
-- Use Case: Academic Affairs 1 (user_id = 4) approve request_id = 4
-- =====================================================

BEGIN;

-- Step 1: Update request status to approved
UPDATE student_request
SET status = 'approved',
    decided_by = 4,           -- Academic Affairs 1 (user_id = 4)
    decided_at = CURRENT_TIMESTAMP,
    updated_at = CURRENT_TIMESTAMP
WHERE id = 5                  -- Request ID = 5
    AND status = 'pending';   -- Chỉ approve nếu đang pending

-- Step 2: Update target session attendance to 'excused'
UPDATE student_session
SET attendance_status = 'excused',
    note = COALESCE(note || E'\n', '') || 'Excused - Approved for makeup session 1000 on ' || CURRENT_DATE::TEXT,
    recorded_at = CURRENT_TIMESTAMP
WHERE student_id = 14         -- Student 14
    AND session_id = 53;      -- Target session 53

-- Step 3: Create makeup student_session record
INSERT INTO student_session (
    student_id,
    session_id,
    is_makeup,
    attendance_status,
    note
) VALUES (
    14,                       -- Student 14
    1000,                     -- Makeup session 1000 (Class 4, course_session_id = 14)
    TRUE,                     -- CRITICAL: is_makeup = true
    'planned',                -- Status
    'Makeup for missed session 53 (Listening Section 1 - Forms & Details)'
)
ON CONFLICT (student_id, session_id) DO UPDATE
SET is_makeup = TRUE,
    note = EXCLUDED.note,
    attendance_status = EXCLUDED.attendance_status;

COMMIT;

-- Verify results:
-- 1. student_request.status = 'approved'
-- 2. student_session(14, 53).attendance_status = 'excused'
-- 3. student_session(14, 1000).is_makeup = TRUE, attendance_status = 'planned'


Bước 35: Gửi email thông báo cho học viên (approved)

Bước 36: Gửi email thông báo cho giáo viên của buổi học bù

Bước 37: Giáo vụ xem thông báo xử lý thành công

Bước 38: Học viên nhận thông báo

Bước 39: Học viên kiểm tra lại trong "My Requests"
Dựa vào branch_id, và student_id -> select ra được các request

Bước 40: Hệ thống cập nhật lịch học

Bước 41: Học viên tham gia buổi học bù

Bước 42: Giáo viên điểm danh buổi học bù
Teacher mở màn hình điểm danh cho session
Hệ thống hiển thị danh sách students:
Student list bình thường (enrolled students)
Student học bù với badge đặc biệt:
Icon:
Label: "Makeup Student"
Tooltip: "Học bù cho buổi [Target Date]"
Teacher mark attendance cho tất cả students
Khi save: UPDATE student_session SET attendance_status = 'present'/'absent'/'late'

PHẦN 4B: TRƯỜNG HỢP REJECT (TỪ CHỐI)
Bước 32 (alternative): Click "Reject"
Giáo vụ nhấn button "Reject"

Bước 33 (alternative): Nhập lý do từ chối
System hiển thị dialog yêu cầu nhập lý do (required)
Giáo vụ nhập lý do, ví dụ:
"Buổi học bù đã hết chỗ"
"Nội dung không khớp với buổi gốc"
"Buổi đã nghỉ quá X tuần"
"Không phù hợp về lịch học"

Bước 34 (alternative): Confirm rejection
System hiển thị confirmation: "Bạn có chắc chắn muốn từ chối yêu cầu này?"
Giáo vụ confirm

Bước 35 (alternative): Thực hiện update reject
System thực hiện UPDATE student_request:
status = 'rejected'
rejection_reason = :reason
decided_by = :Affairs_id
decided_at = NOW()

Bước 36 (alternative): Gửi email thông báo từ chối

Bước 37 (alternative): Giáo vụ xem thông báo xử lý thành công
Hiển thị success notification: "Yêu cầu đã bị từ chối"
Email đã được gửi

Bước 38 (alternative): Học viên nhận thông báo
Học viên nhận email rejected
In-app notification

Bước 39 (alternative): Học viên kiểm tra lại trong "My Requests"
Request hiển thị với status: Rejected
Có thể xem chi tiết:
Rejected by
Rejected at
Rejection reason
Có button "Create New Request" để thử lại

Bước 40 (alternative): Lịch học không thay đổi
Target session giữ nguyên status hiện tại (absent/excused/planned)
Không có buổi học bù mới được thêm vào lịch

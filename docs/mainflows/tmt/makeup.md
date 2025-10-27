
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

SELECT 
    ss.session_id,
    ss.attendance_status,
    s.session_date,
    s.course_session_id, -- CRITICAL: để match makeup sessions
    c.class_name,
    cs.session_title, -- Nội dung buổi học
    EXTRACT(DAYS FROM (CURRENT_DATE - s.session_date)) / 7.0 AS weeks_ago
FROM student_session ss
JOIN session s ON ss.session_id = s.id
JOIN class c ON s.class_id = c.id
JOIN course_session cs ON s.course_session_id = cs.id
WHERE ss.student_id = :student_id
    AND ss.attendance_status IN ('absent', 'late')
    AND s.status = 'done'
    AND s.session_date >= (CURRENT_DATE - INTERVAL '4 weeks') -- Policy: 4 tuần
    AND NOT EXISTS ( 
        SELECT 1 FROM student_request sr
        WHERE sr.target_session_id = ss.session_id
            AND sr.status IN ('pending', 'approved')
    )
ORDER BY s.session_date DESC;


Bước 11A: Hiển thị danh sách buổi đã nghỉ

Bước 12A: Chọn buổi đã nghỉ
Học viên chọn một buổi học đã nghỉ từ dropdown

Bước 13A: Hệ thống tìm các buổi học bù khả dụng
System thực hiện query:
-- =====================================================
-- QUERY TÌM BUỔI HỌC BÙ KHẢ DỤNG (Makeup Session Search)
-- Use Case: Student missed session 58 (Class 3, IELTS B1)
-- Find available makeup sessions with same course_session_id
-- =====================================================


WITH target_info AS (
    -- Lấy thông tin buổi học bị vắng
    SELECT
        s.course_session_id,
        s.class_id,
        c.branch_id,
        c.modality,
        c.code as target_class_code,
        s.date as target_date
    FROM session s
    JOIN class c ON s.class_id = c.id
    WHERE s.id = 58  -- Session ID bị vắng (thay đổi theo từng trường hợp)
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
    c.max_capacity - COALESCE(COUNT(ss.student_id), 0) AS available_slots,
    c.max_capacity,
    COALESCE(COUNT(ss.student_id), 0) AS enrolled_count,
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
    ti.target_date,
    cs.topic AS session_topic
FROM session s
JOIN class c ON s.class_id = c.id
JOIN branch b ON c.branch_id = b.id
CROSS JOIN target_info ti
JOIN course_session cs ON s.course_session_id = cs.id
LEFT JOIN student_session ss ON s.id = ss.session_id
LEFT JOIN teaching_slot ts ON s.id = ts.session_id
LEFT JOIN teacher t ON ts.teacher_id = t.id
LEFT JOIN user_account ua ON t.user_account_id = ua.id  -- FIX: Join đúng qua user_account_id
WHERE
    -- CRITICAL: Cùng nội dung học (course_session_id)
    s.course_session_id = ti.course_session_id
    -- Không phải buổi gốc bị vắng
    AND s.id != 58
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
    ti.target_date,
    cs.topic
-- Lọc chỉ lấy class còn chỗ trống
HAVING COUNT(ss.student_id) < c.max_capacity
-- Sắp xếp theo độ ưu tiên
ORDER BY
    priority_level ASC,      -- Ưu tiên branch + modality trước
    s.date ASC,              -- Sau đó theo ngày gần nhất
    available_slots DESC;    -- Cuối cùng theo số chỗ trống nhiều nhất


OUTPUT
[
  {
    "makeup_session_id": 68,
    "makeup_date": "2025-10-27",
    "days_difference": 1,
    "class_code": "B2-IELTS-001",
    "class_name": "IELTS Intermediate B2 - Evening",
    "modality": "hybrid",
    "branch_name": "Main Campus",
    "available_slots": 20,
    "max_capacity": 20,
    "enrolled_count": 0,
    "teachers": "John Smith",
    "is_same_branch": true,
    "is_same_modality": false,
    "priority_level": 2,
    "target_class_code": "B1-IELTS-001",
    "target_date": "2025-10-26",
    "session_topic": "Listening Strategies - Prediction"
  }
]

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

Check duplicate
SELECT COUNT(*) FROM student_request
WHERE student_id = :student_id
    AND target_session_id = :target_id
    AND makeup_session_id = :makeup_id
    AND status IN ('pending', 'approved');
-- Expected: 0

Verify same content
SELECT 
    s1.course_session_id = s2.course_session_id AS is_same_content
FROM session s1, session s2
WHERE s1.id = :target_id AND s2.id = :makeup_id;
-- Expected: true

Check capacity
SELECT 
    c.max_capacity > COUNT(ss.student_id) AS has_capacity
FROM session s
JOIN class c ON s.class_id = c.id
LEFT JOIN student_session ss ON s.id = ss.session_id
WHERE s.id = :makeup_session_id
GROUP BY c.max_capacity;
-- Expected: true


Bước 20 (chung): Insert student_request vào database
System thực hiện INSERT student_request:
student_id (từ current user)
target_session_id (buổi gốc - đã nghỉ hoặc sẽ nghỉ)
makeup_session_id  (buổi học bù đã chọn)
request_type = 'makeup'
reason (từ form)
notes (từ form, optional)
status = 'pending' 
submitted_at = NOW()
submitted_by = student_id
Metadata: target_session_status (missed/future), selected_modality, selected_branch_id

INSERT INTO student_request (
    student_id,
    target_session_id,
    makeup_session_id,
    request_type,
    status,
    reason,
    notes,
    submitted_at,
    submitted_by
) VALUES (
    :student_id,
    :target_session_id,
    :makeup_session_id,
    'makeup'::student_request_type_enum,
    'pending'::request_status_enum,
    :reason,
    :notes,
    NOW(),
    :current_user_id
) RETURNING id;


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
SELECT 
    sr.id,
    st.student_name,
    -- Target session (buổi gốc)
    s_target.session_date AS target_date,
    c_target.class_name AS target_class,
    ss_target.attendance_status AS target_status,
    -- Makeup session (buổi học bù)
    s_makeup.session_date AS makeup_date,
    c_makeup.class_name AS makeup_class,
    b_makeup.branch_name AS makeup_branch,
    c_makeup.modality,
    -- Validation
    s_target.course_session_id = s_makeup.course_session_id AS is_same_content,
    b_target.id = b_makeup.id AS is_same_branch,
    c_makeup.max_capacity - COUNT(ss_makeup.student_id) AS available_slots
FROM student_request sr
JOIN student st ON sr.student_id = st.id
JOIN session s_target ON sr.target_session_id = s_target.id
JOIN class c_target ON s_target.class_id = c_target.id
JOIN branch b_target ON c_target.branch_id = b_target.id
JOIN session s_makeup ON sr.makeup_session_id = s_makeup.id
JOIN class c_makeup ON s_makeup.class_id = c_makeup.id
JOIN branch b_makeup ON c_makeup.branch_id = b_makeup.id
LEFT JOIN student_session ss_target ON sr.target_session_id = ss_target.session_id
LEFT JOIN student_session ss_makeup ON s_makeup.id = ss_makeup.session_id
WHERE sr.request_type = 'makeup'
    AND sr.status = 'pending'
    AND (b_target.id IN (SELECT branch_id FROM user_branch WHERE user_id = :Affairs_id)
         OR b_makeup.id IN (SELECT branch_id FROM user_branch WHERE user_id = :Affairs_id))
GROUP BY sr.id, st.student_name, s_target.session_date, c_target.class_name, 
         s_makeup.session_date, c_makeup.class_name, b_makeup.branch_name, 
         c_makeup.modality, c_makeup.max_capacity, b_target.id, b_makeup.id,
         s_target.course_session_id, s_makeup.course_session_id, ss_target.attendance_status
ORDER BY sr.submitted_at ASC;


Bước 27: Hệ thống hiển thị danh sách 
Branch_id -> select ra list các request

Bước 28: Click vào request để xem chi tiết
Giáo vụ click vào một row hoặc button "View Detail"

-- ========================================
-- GET REQUEST DETAIL
-- ========================================
select
  sr.id as request_id,
  sr.request_type,
  sr.status,
  sr.note as reason, 
  sr.submitted_at,
  sr.decided_at,
  -- Student info (detailed)
  st.id as student_id,
  st.student_code,
  ua_student.full_name as student_name,
  ua_student.email as student_email,
  ua_student.phone as student_phone,
  st.education_level,
  st.address as student_address,
  -- Session info (detailed)
  s.id as session_id,
  s.date as session_date, 
  s.type as session_type,
  s.status as session_status,
  -- Class info
  c.id as class_id,
  c.code as class_code,
  c.name as class_name,
  c.start_date as class_start_date,
  c.planned_end_date as class_end_date, 
  c.actual_end_date, 
  c.max_capacity,
  -- Branch info
  b.name as branch_name,
  b.phone as branch_phone,
  b.address as branch_address,
  -- Course info
  co.name as course_name,
  co.code as course_code,
  co.duration_weeks,
  co.session_per_week,
  -- Session template info
  cs.topic as session_title, 
  cs.student_task,
  cs.sequence_no,
  -- Time slot
  tst.name as time_slot_name,
  tst.start_time as slot_start,
  tst.end_time as slot_end,
  tst.duration_min,
  -- Room
  r.name as room_name,
  r.location as room_location,
  r.capacity as room_capacity,
  -- Teachers (aggregated as JSON)
  JSONB_AGG(
    distinct JSONB_BUILD_OBJECT(
      'teacher_id',
      t.id,
      'name',
      ua_teacher.full_name,
      'email',
      ua_teacher.email,
      'role',
      ts.role, 
      'skill',
      ts.skill,
      'status',
      ts.status
    )
  ) filter (
    where
      t.id is not null
  ) as teachers,
  -- Absence statistics for this student in this class
  (
    select
      COUNT(*)
    from
      student_session ss2
      join session s2 on ss2.session_id = s2.id
    where
      ss2.student_id = st.id
      and s2.class_id = c.id
      and ss2.attendance_status in ('absent', 'excused')
  ) as total_absences,
  (
    select
      COUNT(*)
    from
      student_session ss2
      join session s2 on ss2.session_id = s2.id
    where
      ss2.student_id = st.id
      and s2.class_id = c.id
  ) as total_sessions,
  -- Absence percentage
  ROUND(
    (
      select
        COUNT(*)
      from
        student_session ss2
        join session s2 on ss2.session_id = s2.id
      where
        ss2.student_id = st.id
        and s2.class_id = c.id
        and ss2.attendance_status in ('absent', 'excused')
    )::NUMERIC / NULLIF(
      (
        select
          COUNT(*)
        from
          student_session ss2
          join session s2 on ss2.session_id = s2.id
        where
          ss2.student_id = st.id
          and s2.class_id = c.id
      ),
      0
    ) * 100,
    2
  ) as absence_percentage,
  -- Lead time calculation
  (s.date - CURRENT_DATE) as days_until_session,
  -- Enrollment info
  e.enrolled_at, 
  e.status as enrollment_status,
  -- Decider info (if decided)
  decider.full_name as decided_by_name,
  decider.email as decided_by_email
from
  student_request sr
  join student st on sr.student_id = st.id
  join user_account ua_student on st.user_id = ua_student.id
  join session s on sr.target_session_id = s.id
  join "class" c on s.class_id = c.id 
  join branch b on c.branch_id = b.id
  join course co on c.course_id = co.id
  left join enrollment e on e.student_id = st.id
  and e.class_id = c.id
  left join course_session cs on s.course_session_id = cs.id
  left join time_slot_template tst on s.time_slot_template_id = tst.id
  left join session_resource sr_res on sr_res.session_id = s.id
  left join resource r on sr_res.resource_id = r.id
  left join teaching_slot ts on ts.session_id = s.id
  left join teacher t on ts.teacher_id = t.id
  left join user_account ua_teacher on t.user_account_id = ua_teacher.id
  left join user_account decider on sr.decided_by = decider.id
where
  sr.id = 1
group by
  sr.id,
  sr.request_type,
  sr.status,
  sr.note,
  sr.submitted_at,
  sr.decided_at,
  st.id,
  st.student_code,
  ua_student.full_name,
  ua_student.email,
  ua_student.phone,
  st.education_level,
  st.address,
  s.id,
  s.date,
  s.type,
  s.status,
  c.id,
  c.code,
  c.name,
  c.start_date,
  c.planned_end_date,
  c.actual_end_date,
  c.max_capacity,
  b.name,
  b.phone,
  b.address,
  co.name,
  co.code,
  co.duration_weeks,
  co.session_per_week,
  cs.topic,
  cs.student_task,
  cs.sequence_no,
  tst.name,
  tst.start_time,
  tst.end_time,
  tst.duration_min,
  r.name,
  r.location,
  r.capacity,
  e.enrolled_at,
  e.status,
  decider.full_name,
  decider.email;


OUTPUT:
[
  {
    "request_id": 1,
    "request_type": "absence",
    "status": "approved",
    "reason": "Family emergency",
    "submitted_at": "2025-10-12 03:39:48.986989+00",
    "decided_at": "2025-10-13 03:39:48.986989+00",
    "student_id": 14,
    "student_code": "S014",
    "student_name": "Mac Thi Lan",
    "student_email": "student014@gmail.com",
    "student_phone": "+84-913-444-444",
    "education_level": "Working Professional",
    "student_address": "Hanoi",
    "session_id": 53,
    "session_date": "2025-10-14",
    "session_type": "class",
    "session_status": "done",
    "class_id": 3,
    "class_code": "B1-IELTS-001",
    "class_name": "IELTS Foundation B1 - Afternoon",
    "class_start_date": "2025-09-07",
    "class_end_date": "2025-12-28",
    "actual_end_date": null,
    "max_capacity": 18,
    "branch_name": "Main Campus",
    "branch_phone": "+84-24-3123-4567",
    "branch_address": "123 Nguyen Trai Street, Thanh Xuan District",
    "course_name": "IELTS Foundation (B1)",
    "course_code": "ENG-B1-IELTS-V1",
    "duration_weeks": 16,
    "session_per_week": 3,
    "session_title": "Listening Section 1 - Forms & Details",
    "student_task": "Practice form completion, note-taking",
    "sequence_no": 2,
    "time_slot_name": "Afternoon Slot 1",
    "slot_start": "13:00:00",
    "slot_end": "14:30:00",
    "duration_min": 90,
    "room_name": "Room 201",
    "room_location": "Floor 2",
    "room_capacity": 15,
    "teachers": [
      {
        "name": "Emily Davis",
        "role": "primary",
        "email": "teacher.emily@elc-hanoi.edu.vn",
        "skill": "reading",
        "status": "completed",
        "teacher_id": 4
      }
    ],
    "total_absences": 1,
    "total_sessions": 7,
    "absence_percentage": "14.29",
    "days_until_session": -13,
    "enrolled_at": "2025-09-02 03:39:48.986989+00",
    "enrollment_status": "enrolled",
    "decided_by_name": "Pham Thi Academic",
    "decided_by_email": "academic1@elc-hanoi.edu.vn"
  }
]

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

BEGIN;

-- Step 1: Update request
UPDATE student_request
SET status = 'approved',
    decided_by = :Affairs_id,
    decided_at = NOW()
WHERE id = :request_id AND status = 'pending';

-- Step 2: Update target session to 'excused'
UPDATE student_session
SET attendance_status = 'excused',
    notes = 'Approved for makeup: ' || :makeup_session_id
WHERE student_id = :student_id 
    AND session_id = :target_session_id;

-- Step 3: Create makeup student_session
INSERT INTO student_session (
    student_id,
    session_id,
    attendance_status,
    is_makeup,             
    target_session_id,    -- Reference back
    notes
) VALUES (
    :student_id,
    :makeup_session_id,
    'planned',
    TRUE,                   -- CRITICAL!
    :target_session_id,
    'Makeup for session: ' || :target_session_id
);

COMMIT;


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

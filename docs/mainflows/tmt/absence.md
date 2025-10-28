STUDENT ABSENCE REQUEST

CÁC BƯỚC THỰC HIỆN (STEP-BY-STEP)
PHẦN 1: HỌC VIÊN GỬI YÊU CẦU

Bước 1: Login hệ thống
Học viên đăng nhập vào hệ thống EMS

Bước 2: Vào menu "My Requests" ở sidebar
Học viên click vào tab "My Requests" trên sidebar

Bước 3: Hệ thống load danh sách requests
System thực hiện query:
Query student_request WHERE student_id = :id
ORDER BY submitted_at DESC

SELECT 
    sr.id AS request_id,
    sr.request_type,
    sr.status,
    sr.note AS reason,
    sr.submitted_at,
    sr.decided_at,
    sr.decided_by,
    decider.full_name AS decided_by_name,
    -- Thông tin session (nếu có target_session_id)
    s.id AS session_id,
    s.date AS session_date,
    s.type AS session_type,
    s.status AS session_status,
    tst.name AS time_slot_name,
    tst.start_time,
    tst.end_time,
    -- Thông tin class
    c.id AS class_id,
    c.code AS class_code,
    c.name AS class_name,
    -- Thông tin student
    st.student_code,
    u.full_name AS student_name
FROM public.student_request sr
JOIN public.student st ON sr.student_id = st.id
JOIN public.user_account u ON st.user_id = u.id
LEFT JOIN public.session s ON sr.target_session_id = s.id
LEFT JOIN public.time_slot_template tst ON s.time_slot_template_id = tst.id
LEFT JOIN public.class c ON sr.current_class_id = c.id
LEFT JOIN public.user_account decider ON sr.decided_by = decider.id
WHERE sr.student_id = 14  -- Student ID có sẵn trong seed data
ORDER BY sr.submitted_at DESC;




Bước 4: Hệ thống hiển thị trang My Requests
Hiển thị danh sách các request đã gửi và nhận được
Hiển thị button "Tạo Request Mới" (+ Create Request)

Bước 5: Học viên click vào button "Tạo Request Mới"

Bước 6: Hệ thống hiển thị modal form tạo request
System hiển thị form với các trường:
Dropdown: Loại request (Absence/Makeup/Transfer/Reschedule)
Date picker: Chọn ngày
Dropdown: Chọn lớp (disabled, chờ chọn ngày trước)
Dropdown: Chọn session (disabled, chờ chọn lớp trước)
Textarea: Lý do (required)
Textarea: Ghi chú (optional)

Bước 7: Chọn loại request
Học viên chọn loại request = "Absence" (Xin nghỉ)

Bước 8: Chọn ngày
Học viên chọn ngày cần xin nghỉ từ date picker

Bước 9: Hệ thống load danh sách lớp theo ngày và student_id

Bước 10: Chọn lớp
Học viên chọn lớp từ dropdown

Bước 11: Hệ thống load danh sách session trong ngày đó của lớp đã chọn
SELECT DISTINCT
    c.id AS class_id,
    c.code AS class_code,
    c.name AS class_name,
    subj.name AS subject_name,
    subj.code AS subject_code,
    COUNT(s.id) AS session_count_on_date,
    -- Thêm thông tin sessions trong ngày đó
    STRING_AGG(
        tst.name || ' (' || tst.start_time::TEXT || '-' || tst.end_time::TEXT || ')',
        ', '
        ORDER BY tst.start_time
    ) AS sessions_detail
FROM public.enrollment e
JOIN public.class c ON e.class_id = c.id
JOIN public.session s ON s.class_id = c.id
JOIN public.course co ON c.course_id = co.id
JOIN public.subject subj ON co.subject_id = subj.id
LEFT JOIN public.time_slot_template tst ON s.time_slot_template_id = tst.id
WHERE e.student_id = 14  -- Student đã enroll trong class
    AND e.status = 'enrolled'
    AND s.date = CURRENT_DATE + INTERVAL '1 day'  -- Ngày đã chọn
    AND s.status = 'planned'  -- Chỉ lấy sessions chưa diễn ra
GROUP BY c.id, c.code, c.name, subj.name, subj.code
ORDER BY c.name;




Bước 12: Chọn session
Học viên chọn session cần xin nghỉ từ dropdown

Bước 13: Điền form
Học viên điền:
Lý do nghỉ (required)
Ghi chú (optional)

Bước 14: Click "Gửi yêu cầu"
Học viên nhấn nút "Submit Request"

Bước 15: Frontend validation
Hệ thống kiểm tra:
Loại request đã chọn
Ngày đã chọn >= TODAY
Lớp đã chọn
Session đã chọn
Lý do tối thiểu 10 ký tự
Cảnh báo lead time (nếu nghỉ gấp)

Bước 16: Backend validation
WITH validation_data AS (
    SELECT 
        s.id AS session_id,
        s.class_id,
        s.date AS session_date,
        s.status AS session_status,
        s.type AS session_type,
        c.code AS class_code,
        c.name AS class_name,
        e.id AS enrollment_id,
        e.status AS enrollment_status,
        tst.name AS time_slot_name,
        tst.start_time,
        tst.end_time
    FROM public.session s
    JOIN public.class c ON s.class_id = c.id
    LEFT JOIN public.enrollment e ON c.id = e.class_id AND e.student_id = 14
    LEFT JOIN public.time_slot_template tst ON s.time_slot_template_id = tst.id
    WHERE s.id = 59
),
enrollment_check AS (
    SELECT 
        vd.*,
        CASE 
            WHEN vd.enrollment_id IS NOT NULL AND vd.enrollment_status = 'enrolled' THEN TRUE
            ELSE FALSE
        END AS is_enrolled,
        CASE 
            WHEN vd.enrollment_id IS NULL THEN 'Student không enrolled trong class này'
            WHEN vd.enrollment_status != 'enrolled' THEN 'Enrollment status không hợp lệ: ' || vd.enrollment_status
            ELSE NULL
        END AS enrollment_error
    FROM validation_data vd
),
session_check AS (
    SELECT 
        ec.*,
        CASE 
            WHEN ec.session_id IS NULL THEN FALSE
            WHEN ec.session_status != 'planned' THEN FALSE
            WHEN ec.session_date < CURRENT_DATE THEN FALSE
            ELSE TRUE
        END AS is_session_valid,
        CASE 
            WHEN ec.session_id IS NULL THEN 'Session không tồn tại'
            WHEN ec.session_status != 'planned' THEN 'Session status không hợp lệ: ' || ec.session_status
            WHEN ec.session_date < CURRENT_DATE THEN 'Session đã qua (date: ' || ec.session_date::TEXT || ')'
            ELSE NULL
        END AS session_error
    FROM enrollment_check ec
),
duplicate_check AS (
    SELECT 
        sc.*,
        (
            SELECT COUNT(*) 
            FROM public.student_request sr
            WHERE sr.student_id = 14
                AND sr.target_session_id = 59
                AND sr.request_type = 'absence'
                AND sr.status IN ('pending', 'approved')
        ) AS duplicate_count,
        CASE 
            WHEN (
                SELECT COUNT(*) 
                FROM public.student_request sr
                WHERE sr.student_id = 14
                    AND sr.target_session_id = 59
                    AND sr.request_type = 'absence'
                    AND sr.status IN ('pending', 'approved')
            ) > 0 THEN 'Đã có absence request cho session này (duplicate)'
            ELSE NULL
        END AS duplicate_error
    FROM session_check sc
),
absence_stats AS (
    SELECT 
        dc.*,
        -- Tổng số buổi đã nghỉ (trong tất cả sessions đã done)
        (
            SELECT COUNT(*) FILTER (WHERE ss.attendance_status IN ('absent', 'excused'))
            FROM public.student_session ss
            JOIN public.session s ON ss.session_id = s.id
            WHERE ss.student_id = 14
                AND s.class_id = dc.class_id
                AND s.status = 'done'
        ) AS total_absences,
        -- Tổng số buổi đã hoàn thành
        (
            SELECT COUNT(*) 
            FROM public.student_session ss
            JOIN public.session s ON ss.session_id = s.id
            WHERE ss.student_id = 14
                AND s.class_id = dc.class_id
                AND s.status = 'done'
        ) AS completed_sessions,
        -- Tổng số buổi học của class (bao gồm planned và done)
        (
            SELECT COUNT(*) 
            FROM public.session s
            WHERE s.class_id = dc.class_id
                AND s.status IN ('planned', 'done')
        ) AS total_sessions
    FROM duplicate_check dc
),
absence_percentage_check AS (
    SELECT 
        ast.*,
        -- Absence percentage hiện tại (so với completed sessions)
        CASE 
            WHEN ast.completed_sessions > 0 
            THEN ROUND((ast.total_absences::NUMERIC / ast.completed_sessions) * 100, 2)
            ELSE 0
        END AS absence_percentage,
        -- Absence percentage sau khi approve request này
        CASE 
            WHEN ast.completed_sessions > 0 
            THEN ROUND(((ast.total_absences + 1)::NUMERIC / ast.completed_sessions) * 100, 2)
            ELSE 0
        END AS absence_percentage_after_request
    FROM absence_stats ast
)
-- Final output: Validation result (BỎ absence policy check)
SELECT 
    apc.session_id,
    apc.class_id,
    apc.class_code,
    apc.class_name,
    apc.session_date,
    apc.time_slot_name,
    apc.start_time,
    apc.end_time,
    apc.is_enrolled,
    apc.is_session_valid,
    apc.duplicate_count,
    -- Thống kê absences (chỉ để hiển thị, không block request)
    apc.total_absences,
    apc.completed_sessions,
    apc.total_sessions,
    apc.absence_percentage,
    apc.absence_percentage_after_request,
    -- Overall validation (CHỈ check: enrolled, session valid, no duplicate)
    CASE 
        WHEN apc.is_enrolled AND apc.is_session_valid AND apc.duplicate_count = 0
        THEN TRUE
        ELSE FALSE
    END AS is_valid,
    -- Error messages (BỎ absence policy error)
    ARRAY_REMOVE(ARRAY[
        apc.enrollment_error,
        apc.session_error,
        apc.duplicate_error
    ], NULL) AS validation_errors,
    -- Success/Error message
    CASE 
        WHEN apc.is_enrolled AND apc.is_session_valid AND apc.duplicate_count = 0
        THEN 'Validation passed. Student có thể tạo absence request cho session này.'
        ELSE 'Validation failed. Không thể tạo absence request.'
    END AS validation_message
FROM absence_percentage_check apc;


Bước 17: Insert student_request vào database
INSERT INTO public.student_request (
    student_id,
    current_class_id,
    target_session_id,
    request_type,
    note,
    status,
    submitted_at,
    submitted_by,
    created_at,
    updated_at
) VALUES (
    14,                                          -- student_id
    3,                                           -- current_class_id (class chứa session 59)
    59,                                          -- target_session_id
    'absence'::student_request_type_enum,       -- request_type
    'Có việc gia đình khẩn cấp. Em xin phép nghỉ buổi học này.',  -- note
    'pending'::request_status_enum,             -- status
    CURRENT_TIMESTAMP,                          -- submitted_at
    14,                                          -- submitted_by
    CURRENT_TIMESTAMP,                          -- created_at
    CURRENT_TIMESTAMP                           -- updated_at
)
RETURNING 
    id AS request_id,
    student_id,
    current_class_id,
    target_session_id,
    request_type,
    note,
    status,
    submitted_at,
    submitted_by;




Bước 18: Hiển thị success notification: "Yêu cầu đã được gửi thành công"
Đóng modal
Refresh danh sách requests trong tab "Requests tôi đã gửi"

Bước 19: Gửi email thông báo cho Academic Affairs (async)
System gửi email bất đồng bộ tới Academic Affairs thông báo:
"Học viên [Student Name] yêu cầu nghỉ buổi học"
"Lớp: [Class Name]"
"Ngày: [Session Date]"
"Giờ: [Time Slot]"
Link đến request detail

Bước 20: Request xuất hiện trong danh sách với status "Pending"
Request mới xuất hiện trong tab "Requests tôi đã gửi"
Hiển thị badge: Pending (Chờ phê duyệt)
Hiển thị thông tin: Date, Class, Session, Status, Submitted date

PHẦN 2: GIÁO VỤ XỬ LÝ YÊU CẦU

Bước 21: Academic Affairs nhận email thông báo
Giáo vụ nhận email thông báo có yêu cầu mới

Bước 22: Login hệ thống và vào menu request -> filter "Pending"

Bước 23: Hệ thống query danh sách pending requests
SELECT 
    sr.id AS request_id,
    sr.request_type,
    sr.status,
    sr.note,
    sr.submitted_at,
    -- Student info
    st.student_code,
    ua_student.full_name AS student_name,
    ua_student.email AS student_email,
    ua_student.phone AS student_phone,
    -- Session info
    s.id AS session_id,
    s.date AS session_date,
    s.type AS session_type,
    s.status AS session_status,
    tst.start_time,
    tst.end_time,
    tst.name AS time_slot_name,
    -- Class info
    c.id AS class_id,
    c.code AS class_code,
    c.name AS class_name,
    c.branch_id,
    b.name AS branch_name,
    -- Course info
    co.name AS course_name,
    cs.topic AS session_topic,  -- Sửa từ cs.title → cs.topic
    cs.sequence_no AS session_sequence,
    -- Room/Resource info
    STRING_AGG(DISTINCT r.name, ', ') AS room_names,
    -- Teacher info
    STRING_AGG(DISTINCT ua_teacher.full_name, ', ') AS teacher_names
FROM public.student_request sr
JOIN public.student st ON sr.student_id = st.id
JOIN public.user_account ua_student ON st.user_id = ua_student.id
JOIN public.session s ON sr.target_session_id = s.id
JOIN public.class c ON sr.current_class_id = c.id
JOIN public.branch b ON c.branch_id = b.id
JOIN public.course co ON c.course_id = co.id
LEFT JOIN public.course_session cs ON s.course_session_id = cs.id
LEFT JOIN public.time_slot_template tst ON s.time_slot_template_id = tst.id
LEFT JOIN public.session_resource sr_res ON sr_res.session_id = s.id
LEFT JOIN public.resource r ON sr_res.resource_id = r.id
LEFT JOIN public.teaching_slot ts ON ts.session_id = s.id
LEFT JOIN public.teacher t ON ts.teacher_id = t.id
LEFT JOIN public.user_account ua_teacher ON t.user_account_id = ua_teacher.id
WHERE sr.status = 'pending'
    AND sr.request_type = 'absence'
    AND c.branch_id IN (
        SELECT branch_id 
        FROM public.user_branches 
        WHERE user_id = 4  -- Academic Affairs 1
    )
GROUP BY 
    sr.id, sr.request_type, sr.status, sr.note, sr.submitted_at,
    st.student_code, ua_student.full_name, ua_student.email, ua_student.phone,
    s.id, s.date, s.type, s.status,
    c.id, c.code, c.name, c.branch_id, b.name,
    tst.start_time, tst.end_time, tst.name,
    co.name, cs.topic, cs.sequence_no
ORDER BY sr.submitted_at ASC;


Bước 24: Hệ thống hiển thị danh sách pending requests

Bước 25: Giáo vụ click vào một row hoặc button "View Detail"
SELECT 
    sr.id AS request_id,
    sr.request_type,
    sr.status,
    sr.note,
    sr.submitted_at,
    sr.decided_at,
    sr.decided_by,
    sr.effective_date,  -- Cho makeup request
    -- Student info
    st.id AS student_id,
    st.student_code,
    st.level AS education_level,
    ua_student.full_name AS student_name,
    ua_student.email AS student_email,
    ua_student.phone AS student_phone,
    ua_student.address AS student_address,
    ua_student.dob AS student_dob,
    ua_student.gender AS student_gender,
    -- Session info (NULL cho makeup request)
    s.id AS session_id,
    s.date AS session_date,
    s.type AS session_type,
    s.status AS session_status,
    -- Time slot info (NULL cho makeup request)
    tst.name AS time_slot_name,
    tst.start_time AS slot_start,
    tst.end_time AS slot_end,
    tst.duration_min,
    -- Class info
    c.id AS class_id,
    c.code AS class_code,
    c.name AS class_name,
    c.start_date AS class_start_date,
    c.planned_end_date AS class_planned_end_date,
    c.actual_end_date AS class_actual_end_date,
    c.max_capacity,
    c.modality,
    c.schedule_days,
    -- Branch info
    b.id AS branch_id,
    b.name AS branch_name,
    b.phone AS branch_phone,
    b.address AS branch_address,
    -- Course info
    co.name AS course_name,
    co.code AS course_code,
    co.duration_weeks,
    co.session_per_week,
    co.description AS course_description,
    -- Session template info (NULL cho makeup request)
    cs.topic AS session_topic,
    cs.sequence_no AS session_sequence,
    cs.student_task,
    -- Room/Resource info (NULL cho makeup request)
    STRING_AGG(DISTINCT r.name || ' (' || r.resource_type || ')', ', ') AS resources,
    -- Teachers (NULL cho makeup request nếu chưa assign session)
    JSONB_AGG(
        DISTINCT JSONB_BUILD_OBJECT(
            'teacher_id', t.id,
            'name', ua_teacher.full_name,
            'email', ua_teacher.email,
            'phone', ua_teacher.phone,
            'role', ts.role,
            'skill', ts.skill,
            'status', ts.status
        )
    ) FILTER (WHERE t.id IS NOT NULL) AS teachers,
    -- Absence statistics
    (
        SELECT COUNT(*) 
        FROM public.student_session ss2 
        JOIN public.session s2 ON ss2.session_id = s2.id
        WHERE ss2.student_id = st.id 
            AND s2.class_id = c.id
            AND ss2.attendance_status IN ('absent', 'excused')
            AND s2.status = 'done'
    ) AS total_absences,
    (
        SELECT COUNT(*) 
        FROM public.student_session ss2 
        JOIN public.session s2 ON ss2.session_id = s2.id
        WHERE ss2.student_id = st.id 
            AND s2.class_id = c.id
            AND s2.status = 'done'
    ) AS completed_sessions,
    (
        SELECT COUNT(*) 
        FROM public.session s2
        WHERE s2.class_id = c.id
            AND s2.status IN ('planned', 'done')
    ) AS total_class_sessions,
    -- Absence percentage
    CASE 
        WHEN (
            SELECT COUNT(*) 
            FROM public.student_session ss2 
            JOIN public.session s2 ON ss2.session_id = s2.id
            WHERE ss2.student_id = st.id 
                AND s2.class_id = c.id
                AND s2.status = 'done'
        ) > 0
        THEN ROUND(
            (
                SELECT COUNT(*) 
                FROM public.student_session ss2 
                JOIN public.session s2 ON ss2.session_id = s2.id
                WHERE ss2.student_id = st.id 
                    AND s2.class_id = c.id
                    AND ss2.attendance_status IN ('absent', 'excused')
                    AND s2.status = 'done'
            )::NUMERIC / (
                SELECT COUNT(*) 
                FROM public.student_session ss2 
                JOIN public.session s2 ON ss2.session_id = s2.id
                WHERE ss2.student_id = st.id 
                    AND s2.class_id = c.id
                    AND s2.status = 'done'
            ) * 100, 2
        )
        ELSE 0
    END AS absence_percentage,
    -- Lead time (NULL cho makeup request)
    (s.date - CURRENT_DATE) AS days_until_session,
    -- Enrollment info
    e.enrolled_at,
    e.status AS enrollment_status,
    -- Decider info
    decider.full_name AS decided_by_name,
    decider.email AS decided_by_email,
    -- Request type specific message
    CASE 
        WHEN sr.request_type = 'absence' AND s.id IS NOT NULL 
        THEN 'Học sinh xin nghỉ buổi học ngày ' || s.date::TEXT
        WHEN sr.request_type = 'makeup' 
        THEN 'Học sinh xin học bù (chưa chọn session cụ thể)'
        WHEN sr.request_type = 'transfer'
        THEN 'Học sinh xin chuyển lớp'
        WHEN sr.request_type = 'reschedule'
        THEN 'Học sinh xin dời lịch học'
        ELSE 'Request khác'
    END AS request_summary
FROM public.student_request sr
JOIN public.student st ON sr.student_id = st.id
JOIN public.user_account ua_student ON st.user_id = ua_student.id
LEFT JOIN public.session s ON sr.target_session_id = s.id  -- LEFT JOIN vì makeup có thể NULL
JOIN public.class c ON sr.current_class_id = c.id
JOIN public.branch b ON c.branch_id = b.id
JOIN public.course co ON c.course_id = co.id
LEFT JOIN public.enrollment e ON e.student_id = st.id AND e.class_id = c.id
LEFT JOIN public.course_session cs ON s.course_session_id = cs.id
LEFT JOIN public.time_slot_template tst ON s.time_slot_template_id = tst.id
LEFT JOIN public.session_resource sr_res ON sr_res.session_id = s.id
LEFT JOIN public.resource r ON sr_res.resource_id = r.id
LEFT JOIN public.teaching_slot ts ON ts.session_id = s.id
LEFT JOIN public.teacher t ON ts.teacher_id = t.id
LEFT JOIN public.user_account ua_teacher ON t.user_account_id = ua_teacher.id
LEFT JOIN public.user_account decider ON sr.decided_by = decider.id
WHERE sr.id = 4 
GROUP BY 
    sr.id, sr.request_type, sr.status, sr.note, sr.submitted_at, sr.decided_at, sr.decided_by, sr.effective_date,
    st.id, st.student_code, st.level,
    ua_student.full_name, ua_student.email, ua_student.phone, ua_student.address, ua_student.dob, ua_student.gender,
    s.id, s.date, s.type, s.status,
    c.id, c.code, c.name, c.start_date, c.planned_end_date, c.actual_end_date, c.max_capacity, c.modality, c.schedule_days,
    b.id, b.name, b.phone, b.address,
    co.name, co.code, co.duration_weeks, co.session_per_week, co.description,
    cs.topic, cs.sequence_no, cs.student_task,
    tst.name, tst.start_time, tst.end_time, tst.duration_min,
    e.enrolled_at, e.status,
    decider.full_name, decider.email;


OUTPUT từ query:

[
  {
    "request_id": 4,
    "request_type": "absence",
    "status": "pending",
    "note": "Có việc gia đình khẩn cấp. Em xin phép nghỉ buổi học này.",
    "submitted_at": "2025-10-27 16:44:01.620431+00",
    "decided_at": null,
    "decided_by": null,
    "effective_date": null,
    "student_id": 14,
    "student_code": "S014",
    "education_level": "Intermediate",
    "student_name": "Mac Thi Lan",
    "student_email": "student014@gmail.com",
    "student_phone": "+84-913-444-444",
    "student_address": "80 Tay Son, Dong Da, Hanoi",
    "student_dob": "2002-03-21",
    "student_gender": null,
    "session_id": 59,
    "session_date": "2025-10-28",
    "session_type": "class",
    "session_status": "planned",
    "time_slot_name": "Afternoon Slot 1",
    "slot_start": "13:00:00",
    "slot_end": "14:30:00",
    "duration_min": 90,
    "class_id": 3,
    "class_code": "B1-IELTS-001",
    "class_name": "IELTS Foundation B1 - Afternoon",
    "class_start_date": "2025-09-07",
    "class_planned_end_date": "2025-12-28",
    "class_actual_end_date": null,
    "max_capacity": 18,
    "modality": "offline",
    "schedule_days": [
      2,
      4,
      6
    ],
    "branch_id": 1,
    "branch_name": "Main Campus",
    "branch_phone": "+84-24-3123-4567",
    "branch_address": "123 Nguyen Trai Street, Thanh Xuan District",
    "course_name": "IELTS Foundation (B1)",
    "course_code": "ENG-B1-IELTS-V1",
    "duration_weeks": 16,
    "session_per_week": 3,
    "course_description": "IELTS preparation for intermediate learners targeting band 5.0-6.0",
    "session_topic": "Listening Strategies - Keywords",
    "session_sequence": 8,
    "student_task": "Identify keywords and paraphrasing",
    "resources": "Room 201 (room)",
    "teachers": [
      {
        "name": "Emily Davis",
        "role": "primary",
        "email": "teacher.emily@elc-hanoi.edu.vn",
        "phone": "+84-903-444-444",
        "skill": "reading",
        "status": "scheduled",
        "teacher_id": 4
      },
      {
        "name": "Le Thi Mai",
        "role": "assistant",
        "email": "teacher.mai@elc-hanoi.edu.vn",
        "phone": "+84-903-777-777",
        "skill": "general",
        "status": "scheduled",
        "teacher_id": 7
      }
    ],
    "total_absences": 1,
    "completed_sessions": 7,
    "total_class_sessions": 25,
    "absence_percentage": "14.29",
    "days_until_session": 1,
    "enrolled_at": "2025-09-02 16:04:33.027916+00",
    "enrollment_status": "enrolled",
    "decided_by_name": null,
    "decided_by_email": null,
    "request_summary": "Học sinh xin nghỉ buổi học ngày 2025-10-28"
  }
]

Bước 27: Review thông tin
Giáo vụ xem xét:
Lý do nghỉ có hợp lý không
Thống kê số buổi nghỉ (có vượt quá giới hạn không)
Lead time (thời gian báo trước)
Business rules khác

Bước 28: Quyết định?
Giáo vụ đưa ra quyết định: Approve hoặc Reject

PHẦN 3A: TRƯỜNG HỢP APPROVE (PHÊ DUYỆT)
Bước 29: Click "Approve"
Giáo vụ nhấn button "Approve"
(Optional) Có thể thêm approval note

Bước 30: Confirm approval
System hiển thị confirmation dialog: "Bạn có chắc chắn muốn phê duyệt yêu cầu này?"
Giáo vụ confirm

Bước 31: Thực hiện transaction approve
BEGIN;

-- 1. Update student_request status to approved
UPDATE public.student_request
SET 
    status = 'approved'::request_status_enum,
    decided_by = 4,  -- Academic Affairs user_id
    decided_at = CURRENT_TIMESTAMP,
    updated_at = CURRENT_TIMESTAMP
WHERE id = 4
    AND status = 'pending'
RETURNING id, student_id, target_session_id, status, decided_at;

-- 2. Update student_session attendance to excused
UPDATE public.student_session
SET 
    attendance_status = 'excused'::attendance_status_enum,
    note = COALESCE(note || E'\n', '') || 'Approved absence request #4 on ' || CURRENT_TIMESTAMP::DATE::TEXT
WHERE student_id = 14
    AND session_id = 59
    AND attendance_status = 'planned'
RETURNING student_id, session_id, attendance_status, note;

COMMIT;




Bước 32: Gửi email thông báo cho học viên (approved)
System gửi email tới Student:
"Yêu cầu xin nghỉ của bạn đã được phê duyệt"
"Lớp: [Class Name]"
"Ngày: [Session Date]"
"Giờ: [Time Slot]"
"Bạn được đánh dấu nghỉ có phép (Excused)"
"Approval Note: [...]" (nếu có)

Bước 33: Giáo vụ xem thông báo xử lý thành công
Hiển thị success notification: "Yêu cầu đã được phê duyệt"
Request biến mất khỏi danh sách "Pending"
(Optional) Chuyển sang "Processed Requests" với filter

Bước 34: Học viên nhận thông báo

Bước 35: Học viên kiểm tra lại trong "My Requests"
Học viên vào lại tab "My Requests"
Request hiển thị với status: Approved
Có thể xem chi tiết: Approved by, Approved at, Approval note

Bước 36: Lịch học cập nhật
Nếu học viên vào "My Schedule" / "Lịch Học Của Tôi"
Session đã được approve sẽ hiển thị: Excused (Nghỉ có phép)

PHẦN 3B: TRƯỜNG HỢP REJECT (TỪ CHỐI)
Bước 29 (alternative): Click "Reject"
Giáo vụ nhấn button "Reject"

Bước 30 (alternative): Nhập lý do từ chối
System hiển thị dialog yêu cầu nhập lý do từ chối (required)
Giáo vụ nhập lý do, ví dụ:
"Đã vượt quá số buổi nghỉ cho phép"
"Thời gian báo nghỉ quá gần giờ học"
"Lý do không hợp lý"

Bước 31 (alternative): Confirm rejection
System hiển thị confirmation: "Bạn có chắc chắn muốn từ chối yêu cầu này?"
Giáo vụ confirm

-- Update student_request status to rejected
UPDATE student_request
SET 
    status = 'rejected',
    rejection_reason = :rejection_reason,
    decided_by = :Affairs_user_id,
    decided_at = NOW()
WHERE id = :request_id
    AND status = 'pending'
RETURNING id, student_id, target_session_id, rejection_reason;


-- Note: student_session.attendance_status KHÔNG thay đổi (vẫn là 'planned')

Bước 32 (alternative): Thực hiện update reject
System thực hiện UPDATE student_request:
status = 'rejected'
rejection_reason = :reason
decided_by = :Affairs_id
decided_at = NOW()
(student_session.attendance_status KHÔNG thay đổi, vẫn là 'planned')

Bước 33 (alternative): Gửi email thông báo từ chối
System gửi email tới Student:
"Yêu cầu xin nghỉ của bạn đã bị từ chối"
"Lớp: [Class Name]"
"Ngày: [Session Date]"
"Giờ: [Time Slot]"
"Lý do từ chối: [rejection_reason]"
"Vui lòng liên hệ giáo vụ nếu có thắc mắc"

Bước 34 (alternative): Giáo vụ xem thông báo xử lý thành công
Hiển thị success notification: "Yêu cầu đã bị từ chối"
Request biến mất khỏi danh sách "Pending"

Bước 35 (alternative): Học viên nhận thông báo
Học viên nhận email thông báo rejected
In-app notification (nếu có)

Bước 36 (alternative): Học viên kiểm tra lại trong "My Requests"
Học viên vào lại tab "My Requests"
Request hiển thị với status: Rejected
Có thể xem chi tiết: Rejected by, Rejected at, Rejection reason

Bước 37 (alternative): Lịch học không thay đổi
Session vẫn giữ status 'planned'
Học viên vẫn phải đến lớp hoặc tạo request mới với lý do khác

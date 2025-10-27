STUDENT ABSENCE REQUEST
Đây là quy trình cho phép học viên quản lý và tạo yêu cầu xin nghỉ học từ trang "My Requests". Học viên chọn ngày, chọn lớp, sau đó chọn session trong ngày đó để gửi yêu cầu xin nghỉ. Giáo vụ (Academic Staff) sẽ xem xét và phê duyệt hoặc từ chối yêu cầu.

CÁC BƯỚC THỰC HIỆN (STEP-BY-STEP)
PHẦN 1: HỌC VIÊN GỬI YÊU CẦU

Bước 1: Login hệ thống
Học viên đăng nhập vào hệ thống EMS

Bước 2: Vào menu "My Requests" ở sidebar
Học viên click vào tab "My Requests" trên sidebar

Bước 3: Hệ thống load danh sách requests
System thực hiện query:
Query student_request WHERE student_id = :id
Hiển thị 2 tab: "Requests tôi đã gửi" và "Requests tôi nhận được"
ORDER BY submitted_at DESC

-- Load all requests của student (tab "Sent")
SELECT 
    sr.id,
    sr.request_type,
    sr.status,
    sr.reason,
    sr.notes,
    sr.submitted_at,
    sr.decided_at,
    sr.rejection_reason,
    s.session_date,
    s.start_time,
    s.end_time,
    c.name as class_name,
    c.class_code,
    decider.full_name as decided_by_name
FROM student_request sr
JOIN session s ON sr.target_session_id = s.id
JOIN class c ON s.class_id = c.id
LEFT JOIN user_account decider ON sr.decided_by = decider.id
WHERE sr.student_id = :student_id
ORDER BY sr.submitted_at DESC;

-- Load requests mà student nhận được (tab "Received")
-- (Nếu có flow học viên nhận request từ hệ thống - hiện tại chưa có trong flow này)


Bước 4: Hệ thống hiển thị trang My Requests
Hiển thị danh sách các request đã gửi và nhận được
Hiển thị button "Tạo Request Mới" (+ Create Request)

Bước 5: Click button "Tạo Request Mới"
Học viên click vào button "Tạo Request Mới"

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

Bước 9: Hệ thống load danh sách lớp theo ngày
System thực hiện query:
SELECT DISTINCT class FROM enrollment e
JOIN session s ON s.class_id = e.class_id
WHERE e.student_id = :id
AND s.session_date = :selected_date
AND s.status = 'planned'
Enable dropdown "Chọn lớp"

-- Query classes có session vào ngày đã chọn
SELECT DISTINCT
    c.id as class_id,
    c.class_code,
    c.name as class_name,
    subj.name as subject_name,
    COUNT(s.id) as session_count_on_date
FROM enrollment e
JOIN class c ON e.class_id = c.id
JOIN session s ON s.class_id = c.id
JOIN course co ON c.course_id = co.id
JOIN subject subj ON co.subject_id = subj.id
WHERE e.student_id = :student_id
    AND e.status = 'enrolled'
    AND s.session_date = :selected_date
    AND s.status = 'planned'
GROUP BY c.id, c.class_code, c.name, subj.name
ORDER BY c.name;


Bước 10: Chọn lớp
Học viên chọn lớp từ dropdown

Bước 11: Hệ thống load danh sách session trong ngày đó của lớp đã chọn
System thực hiện query:
SELECT session FROM student_session ss
JOIN session s ON ss.session_id = s.session_id
WHERE ss.student_id = :id
AND s.class_id = :selected_class_id
AND s.session_date = :selected_date
AND ss.attendance_status = 'planned'
Enable dropdown "Chọn session"
Hiển thị danh sách session với thông tin: Time slot, Room, Teacher

-- Query sessions của class trong ngày đã chọn
SELECT 
    s.id as session_id,
    s.session_date,
    s.start_time,
    s.end_time,
    s.session_type,
    ss.attendance_status,
    tst.name as time_slot_name,
    r.name as room_name,
    r.location as room_location,
    STRING_AGG(DISTINCT ua.full_name, ', ') as teacher_names,
    cs.title as session_title,
    cs.topics as session_topics
FROM student_session ss
JOIN session s ON ss.session_id = s.id
JOIN class c ON s.class_id = c.id
LEFT JOIN time_slot_template tst ON c.time_slot_template_id = tst.id
LEFT JOIN session_resource sr ON sr.session_id = s.id
LEFT JOIN resource r ON sr.resource_id = r.id
LEFT JOIN teaching_slot ts ON ts.session_id = s.id
LEFT JOIN teacher t ON ts.teacher_id = t.id
LEFT JOIN user_account ua ON t.user_account_id = ua.id
LEFT JOIN course_session cs ON s.course_session_id = cs.id
WHERE ss.student_id = :student_id
    AND c.id = :selected_class_id
    AND s.session_date = :selected_date
    AND ss.attendance_status = 'planned'
    AND s.status = 'planned'
GROUP BY s.id, s.session_date, s.start_time, s.end_time, s.session_type,
         ss.attendance_status, tst.name, r.name, r.location, cs.title, cs.topics
ORDER BY s.start_time;


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
System kiểm tra:
Student có enrolled trong class này không?
Session có tồn tại không?
Session date >= TODAY?
Có yêu cầu trùng lặp cho session này không? (status = pending)
Kiểm tra số buổi nghỉ tối đa cho class này

-- 1. Kiểm tra student có enrolled trong class này không
SELECT COUNT(*) as is_enrolled
FROM enrollment e
JOIN session s ON e.class_id = s.class_id
WHERE e.student_id = :student_id
    AND s.id = :target_session_id
    AND e.status = 'enrolled';

-- 2. Kiểm tra session có tồn tại và hợp lệ
SELECT 
    s.id,
    s.session_date,
    s.status,
    c.id as class_id,
    c.name as class_name
FROM session s
JOIN class c ON s.class_id = c.id
WHERE s.id = :target_session_id
    AND s.status = 'planned'
    AND s.session_date >= CURRENT_DATE;

-- 3. Kiểm tra duplicate request (đã có request pending cho session này chưa)
SELECT COUNT(*) as duplicate_count
FROM student_request
WHERE student_id = :student_id
    AND target_session_id = :target_session_id
    AND request_type = 'absence'
    AND status IN ('pending', 'waiting_confirm');

-- 4. Kiểm tra số buổi đã nghỉ (max absences check)
SELECT 
    COUNT(*) FILTER (WHERE ss.attendance_status IN ('absent', 'excused')) as total_absences,
    COUNT(*) as total_sessions,
    COUNT(*) FILTER (WHERE s.status = 'done') as completed_sessions,
    ROUND(
        COUNT(*) FILTER (WHERE ss.attendance_status IN ('absent', 'excused'))::NUMERIC / 
        NULLIF(COUNT(*) FILTER (WHERE s.status = 'done'), 0) * 100, 
        2
    ) as absence_percentage
FROM student_session ss
JOIN session s ON ss.session_id = s.id
WHERE ss.student_id = :student_id
    AND s.class_id = (
        SELECT class_id FROM session WHERE id = :target_session_id
    );

-- 5. Business rule: Check nếu vượt quá 20% (hoặc policy khác)
-- Giả sử policy: không được nghỉ quá 20% tổng số buổi
SELECT 
    CASE 
        WHEN absence_percentage >= 20.0 THEN FALSE
        ELSE TRUE
    END as can_request_absence
FROM (
    SELECT 
        ROUND(
            COUNT(*) FILTER (WHERE ss.attendance_status IN ('absent', 'excused'))::NUMERIC / 
            NULLIF(COUNT(*), 0) * 100, 
            2
        ) as absence_percentage
    FROM student_session ss
    JOIN session s ON ss.session_id = s.id
    WHERE ss.student_id = :student_id
        AND s.class_id = (SELECT class_id FROM session WHERE id = :target_session_id)
) sub;


Bước 17: Insert student_request vào database
System thực hiện INSERT student_request với các thông tin:
student_id (từ current user)
target_session_id (từ session đã chọn)
request_type = 'absence'
reason (từ form)
notes (từ form, optional)
status = 'pending' ⭐
submitted_at = NOW()
submitted_by = student_id

-- Insert absence request
INSERT INTO student_request (
    student_id,
    target_session_id,
    request_type,
    reason,
    notes,
    status,
    submitted_at,
    submitted_by
) VALUES (
    :student_id,
    :target_session_id,
    'absence',
    :reason,
    :notes,
    'pending',
    NOW(),
    :student_id
)
RETURNING id, submitted_at;


Bước 18: Hệ thống hiển thị thông báo thành công
Hiển thị success notification: ✅ "Yêu cầu đã được gửi thành công"
Đóng modal
Refresh danh sách requests trong tab "Requests tôi đã gửi"

Bước 19: Gửi email thông báo cho Academic Staff (async)
System gửi email bất đồng bộ tới Academic Staff thông báo:
"Học viên [Student Name] yêu cầu nghỉ buổi học"
"Lớp: [Class Name]"
"Ngày: [Session Date]"
"Giờ: [Time Slot]"
Link đến request detail

Bước 20: Request xuất hiện trong danh sách với status "Pending"
Request mới xuất hiện trong tab "Requests tôi đã gửi"
Hiển thị badge: ⏳ Pending (Chờ phê duyệt)
Hiển thị thông tin: Date, Class, Session, Status, Submitted date

PHẦN 2: GIÁO VỤ XỬ LÝ YÊU CẦU

Bước 21: Academic Staff nhận email thông báo
📧 Giáo vụ nhận email thông báo có yêu cầu mới

Bước 22: Login hệ thống và vào menu "Pending Requests"
Giáo vụ đăng nhập và truy cập phần "Request Management" hoặc "Pending Requests"

Bước 23: Hệ thống query danh sách pending requests
System thực hiện query:
SELECT * FROM student_request sr
JOIN student st ON sr.student_id = st.student_id
JOIN session s ON sr.target_session_id = s.session_id
JOIN class c ON s.class_id = c.class_id
WHERE sr.status = 'pending'
AND sr.request_type = 'absence'
AND c.branch_id IN (SELECT branch_id FROM user_branches WHERE user_id = :staff_id)
ORDER BY sr.submitted_at ASC

-- Load pending absence requests cho Academic Staff
SELECT 
    sr.id as request_id,
    sr.request_type,
    sr.status,
    sr.reason,
    sr.notes,
    sr.submitted_at,
    -- Student info
    st.student_code,
    ua_student.full_name as student_name,
    ua_student.email as student_email,
    ua_student.phone as student_phone,
    -- Session info
    s.id as session_id,
    s.session_date,
    s.start_time,
    s.end_time,
    s.session_type,
    -- Class info
    c.id as class_id,
    c.class_code,
    c.name as class_name,
    c.branch_id,
    b.name as branch_name,
    -- Teacher info
    STRING_AGG(DISTINCT ua_teacher.full_name, ', ') as teacher_names,
    -- Time slot info
    tst.name as time_slot_name,
    -- Room info
    r.name as room_name,
    -- Course info
    co.name as course_name,
    cs.title as session_title
FROM student_request sr
JOIN student st ON sr.student_id = st.id
JOIN user_account ua_student ON st.user_id = ua_student.id
JOIN session s ON sr.target_session_id = s.id
JOIN class c ON s.class_id = c.id
JOIN branch b ON c.branch_id = b.id
JOIN course co ON c.course_id = co.id
LEFT JOIN course_session cs ON s.course_session_id = cs.id
LEFT JOIN time_slot_template tst ON c.time_slot_template_id = tst.id
LEFT JOIN session_resource sr_res ON sr_res.session_id = s.id
LEFT JOIN resource r ON sr_res.resource_id = r.id
LEFT JOIN teaching_slot ts ON ts.session_id = s.id
LEFT JOIN teacher t ON ts.teacher_id = t.id
LEFT JOIN user_account ua_teacher ON t.user_account_id = ua_teacher.id
WHERE sr.status = 'pending'
    AND sr.request_type = 'absence'
    AND c.branch_id IN (
        -- Academic Staff chỉ thấy requests thuộc branches họ quản lý
        SELECT branch_id 
        FROM user_branches 
        WHERE user_id = :staff_user_id
    )
GROUP BY sr.id, sr.request_type, sr.status, sr.reason, sr.notes, sr.submitted_at,
         st.student_code, ua_student.full_name, ua_student.email, ua_student.phone,
         s.id, s.session_date, s.start_time, s.end_time, s.session_type,
         c.id, c.class_code, c.name, c.branch_id, b.name,
         tst.name, r.name, co.name, cs.title
ORDER BY sr.submitted_at ASC;


Bước 24: Hệ thống hiển thị danh sách pending requests
Hiển thị table với các cột:
Student Name
Class Name
Session Date & Time
Request Type
Reason (preview)
Submitted Date
Action buttons

Bước 25: Click vào request để xem chi tiết
Giáo vụ click vào một row hoặc button "View Detail"

Bước 26: Hệ thống hiển thị chi tiết request
System hiển thị modal/page với đầy đủ thông tin:
Student Information: Name, Student Code, Email, Phone
Session Information: Class Name, Session Date, Time Slot, Room, Teacher
Request Details: Request Type, Reason, Notes, Submitted Date
Absence Statistics: Số buổi đã nghỉ / Tổng số buổi của lớp (X/Y)
Action buttons: Approve, Reject

-- Get full detail of a specific request
SELECT 
    sr.id as request_id,
    sr.request_type,
    sr.status,
    sr.reason,
    sr.notes,
    sr.submitted_at,
    sr.decided_at,
    sr.rejection_reason,
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
    s.session_date,
    s.start_time,
    s.end_time,
    s.session_type,
    s.status as session_status,
    -- Class info
    c.id as class_id,
    c.class_code,
    c.name as class_name,
    c.start_date as class_start_date,
    c.end_date as class_end_date,
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
    cs.title as session_title,
    cs.topics as session_topics,
    cs.learning_objectives,
    cs.sequence_no,
    -- Time slot
    tst.name as time_slot_name,
    tst.start_time as slot_start,
    tst.end_time as slot_end,
    -- Room
    r.name as room_name,
    r.location as room_location,
    r.capacity as room_capacity,
    -- Teachers
    JSONB_AGG(
        DISTINCT JSONB_BUILD_OBJECT(
            'teacher_id', t.id,
            'name', ua_teacher.full_name,
            'email', ua_teacher.email,
            'role', ts.teaching_role,
            'skill', ts.skill
        )
    ) FILTER (WHERE t.id IS NOT NULL) as teachers,
    -- Absence statistics for this student in this class
    (
        SELECT COUNT(*) 
        FROM student_session ss2 
        JOIN session s2 ON ss2.session_id = s2.id
        WHERE ss2.student_id = st.id 
            AND s2.class_id = c.id
            AND ss2.attendance_status IN ('absent', 'excused')
    ) as total_absences,
    (
        SELECT COUNT(*) 
        FROM student_session ss2 
        JOIN session s2 ON ss2.session_id = s2.id
        WHERE ss2.student_id = st.id 
            AND s2.class_id = c.id
    ) as total_sessions,
    -- Lead time calculation
    (s.session_date - CURRENT_DATE) as days_until_session,
    -- Enrollment info
    e.enrollment_date,
    e.status as enrollment_status,
    -- Decider info (if decided)
    decider.full_name as decided_by_name
FROM student_request sr
JOIN student st ON sr.student_id = st.id
JOIN user_account ua_student ON st.user_id = ua_student.id
JOIN session s ON sr.target_session_id = s.id
JOIN class c ON s.class_id = c.id
JOIN branch b ON c.branch_id = b.id
JOIN course co ON c.course_id = co.id
LEFT JOIN enrollment e ON e.student_id = st.id AND e.class_id = c.id
LEFT JOIN course_session cs ON s.course_session_id = cs.id
LEFT JOIN time_slot_template tst ON c.time_slot_template_id = tst.id
LEFT JOIN session_resource sr_res ON sr_res.session_id = s.id
LEFT JOIN resource r ON sr_res.resource_id = r.id
LEFT JOIN teaching_slot ts ON ts.session_id = s.id
LEFT JOIN teacher t ON ts.teacher_id = t.id
LEFT JOIN user_account ua_teacher ON t.user_account_id = ua_teacher.id
LEFT JOIN user_account decider ON sr.decided_by = decider.id
WHERE sr.id = :request_id
GROUP BY sr.id, sr.request_type, sr.status, sr.reason, sr.notes, sr.submitted_at,
         sr.decided_at, sr.rejection_reason,
         st.id, st.student_code, ua_student.full_name, ua_student.email, 
         ua_student.phone, st.education_level, st.address,
         s.id, s.session_date, s.start_time, s.end_time, s.session_type, s.status,
         c.id, c.class_code, c.name, c.start_date, c.end_date, c.max_capacity,
         b.name, b.phone, b.address,
         co.name, co.code, co.duration_weeks, co.session_per_week,
         cs.title, cs.topics, cs.learning_objectives, cs.sequence_no,
         tst.name, tst.start_time, tst.end_time,
         r.name, r.location, r.capacity,
         e.enrollment_date, e.status,
         decider.full_name;


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
System thực hiện BEGIN TRANSACTION:
UPDATE student_request SET:
status = 'approved' ⭐
decided_by = :staff_id
decided_at = NOW()
approval_note = :note (nếu có)
UPDATE student_session SET:
attendance_status = 'excused' ⭐
WHERE student_id = :student_id AND session_id = :session_id
COMMIT

-- BEGIN TRANSACTION
BEGIN;

-- 1. Update student_request status to approved
UPDATE student_request
SET 
    status = 'approved',
    decided_by = :staff_user_id,
    decided_at = NOW(),
    approval_note = :approval_note  -- optional
WHERE id = :request_id
    AND status = 'pending'
RETURNING id, student_id, target_session_id;

-- 2. Update student_session attendance to excused
UPDATE student_session
SET 
    attendance_status = 'excused',
    notes = COALESCE(notes || E'\n', '') || 'Approved absence request ' || :request_id
WHERE student_id = (SELECT student_id FROM student_request WHERE id = :request_id)
    AND session_id = (SELECT target_session_id FROM student_request WHERE id = :request_id)
    AND attendance_status = 'planned'
RETURNING student_id, session_id, attendance_status;

-- COMMIT
COMMIT;

-- Nếu có lỗi, ROLLBACK sẽ tự động chạy


Bước 32: Gửi email thông báo cho học viên (approved)
📧 System gửi email tới Student:
"Yêu cầu xin nghỉ của bạn đã được phê duyệt"
"Lớp: [Class Name]"
"Ngày: [Session Date]"
"Giờ: [Time Slot]"
"Bạn được đánh dấu nghỉ có phép (Excused)"
"Approval Note: [...]" (nếu có)

Bước 33: Giáo vụ xem thông báo xử lý thành công
Hiển thị success notification: ✅ "Yêu cầu đã được phê duyệt"
Request biến mất khỏi danh sách "Pending"
(Optional) Chuyển sang "Processed Requests" với filter

Bước 34: Học viên nhận thông báo
📧 Học viên nhận email thông báo approved
🔔 In-app notification (nếu có)

Bước 35: Học viên kiểm tra lại trong "My Requests"
Học viên vào lại tab "My Requests"
Request hiển thị với status: ✅ Approved
Có thể xem chi tiết: Approved by, Approved at, Approval note

Bước 36: Lịch học cập nhật
Nếu học viên vào "My Schedule" / "Lịch Học Của Tôi"
Session đã được approve sẽ hiển thị: ✅ Excused (Nghỉ có phép)

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
    decided_by = :staff_user_id,
    decided_at = NOW()
WHERE id = :request_id
    AND status = 'pending'
RETURNING id, student_id, target_session_id, rejection_reason;


-- Note: student_session.attendance_status KHÔNG thay đổi (vẫn là 'planned')

Bước 32 (alternative): Thực hiện update reject
System thực hiện UPDATE student_request:
status = 'rejected' ⭐
rejection_reason = :reason
decided_by = :staff_id
decided_at = NOW()
(student_session.attendance_status KHÔNG thay đổi, vẫn là 'planned')

Bước 33 (alternative): Gửi email thông báo từ chối
📧 System gửi email tới Student:
"Yêu cầu xin nghỉ của bạn đã bị từ chối"
"Lớp: [Class Name]"
"Ngày: [Session Date]"
"Giờ: [Time Slot]"
"Lý do từ chối: [rejection_reason]"
"Vui lòng liên hệ giáo vụ nếu có thắc mắc"

Bước 34 (alternative): Giáo vụ xem thông báo xử lý thành công
Hiển thị success notification: ✅ "Yêu cầu đã bị từ chối"
Request biến mất khỏi danh sách "Pending"

Bước 35 (alternative): Học viên nhận thông báo
📧 Học viên nhận email thông báo rejected
🔔 In-app notification (nếu có)

Bước 36 (alternative): Học viên kiểm tra lại trong "My Requests"
Học viên vào lại tab "My Requests"
Request hiển thị với status: ❌ Rejected
Có thể xem chi tiết: Rejected by, Rejected at, Rejection reason

Bước 37 (alternative): Lịch học không thay đổi
Session vẫn giữ status 'planned'
Học viên vẫn phải đến lớp hoặc tạo request mới với lý do khác

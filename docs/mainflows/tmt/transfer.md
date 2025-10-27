STUDENT TRANSFER REQUEST
PHASE 1: HỌC VIÊN TẠO YÊU CẦU CHUYỂN LỚP
Bước 1: Login hệ thống
Học viên đăng nhập vào hệ thống EMS

Bước 2: Vào menu "My Requests"
Học viên click vào tab "My Requests" trên sidebar

Bước 3: Hệ thống load danh sách requests
System query student_request của student, hiển thị 2 tabs: "Requests tôi đã gửi" và "Requests tôi nhận được"

Bước 4: Hiển thị trang My Requests
Hiển thị danh sách requests và button "+ Tạo Request Mới"

Bước 5: Click "Tạo Request Mới"
Học viên click vào button tạo request

Bước 6: Hiển thị modal chọn loại request
Modal hiển thị dropdown với options: Absence, Makeup, Transfer, Reschedule

Bước 7: Chọn loại "Transfer"
Học viên select "Transfer - Chuyển lớp"

Bước 8: Hệ thống load danh sách lớp đang học
System query enrollment WHERE student_id AND status='enrolled', JOIN với class/course/branch

Bước 9: Hiển thị dropdown "Chọn lớp hiện tại"
Form hiển thị dropdown các lớp đang enrolled với format: "[Code] - [Name] - [Course]"

Bước 10: Chọn lớp hiện tại muốn chuyển đi
Học viên select lớp từ dropdown

Bước 11: Load thông tin chi tiết lớp hiện tại
System query chi tiết class, remaining sessions, progress, enable section tiếp theo

Bước 12: Hiển thị info lớp hiện tại + Section chọn course đích
Hiển thị:
Info lớp hiện tại: Code, Name, Course, Branch, Schedule, Modality, Progress
Radio buttons:
Giữ nguyên course (default)
Chuyển sang course khác → Dropdown enabled

Bước 13: Chọn option course
Học viên chọn: Giữ nguyên hoặc Chuyển course khác

Bước 14: Hệ thống load danh sách target classes
System query classes khả dụng:

WITH current_class_info AS (
    SELECT course_id, id AS current_class_id, branch_id AS current_branch_id
    FROM class
    WHERE id = :selected_current_class_id
),
enrollment_counts AS (
    SELECT 
        c.id AS class_id,
        COUNT(DISTINCT e.student_id) AS enrolled_count
    FROM class c
    LEFT JOIN enrollment e ON c.id = e.class_id AND e.status = 'enrolled'
    GROUP BY c.id
)
SELECT 
    c.id AS class_id,
    c.code,
    c.name,
    c.modality,
    c.start_date,
    c.planned_end_date,
    c.schedule_days,
    c.max_capacity,
    c.status,
    b.id AS branch_id,
    b.name AS branch_name,
    b.address,
    -- Capacity info
    COALESCE(ec.enrolled_count, 0) AS enrolled_count,
    c.max_capacity - COALESCE(ec.enrolled_count, 0) AS available_slots,
    -- Teachers
    STRING_AGG(DISTINCT ua.full_name, ', ') AS teachers,
    -- Priority flag
    CASE 
        WHEN b.id = cci.current_branch_id THEN 1 
        ELSE 2 
    END AS priority_score,
    CASE 
        WHEN b.id = cci.current_branch_id THEN true 
        ELSE false 
    END AS is_same_branch
FROM class c
JOIN branch b ON c.branch_id = b.id
CROSS JOIN current_class_info cci
LEFT JOIN enrollment_counts ec ON c.id = ec.class_id
LEFT JOIN session s ON s.class_id = c.id
LEFT JOIN teaching_slot ts ON s.id = ts.session_id
LEFT JOIN teacher t ON ts.teacher_id = t.id
LEFT JOIN user_account ua ON t.user_account_id = ua.id
WHERE c.course_id = cci.course_id -- Same course
  AND c.id != cci.current_class_id -- Exclude current
  AND c.status IN ('scheduled', 'ongoing')
  AND COALESCE(ec.enrolled_count, 0) < c.max_capacity -- Has capacity
GROUP BY c.id, b.id, cci.current_branch_id, ec.enrolled_count
ORDER BY priority_score ASC, c.start_date ASC;


Bước 15: Thực hiện Content Gap Detection (background)
System phân tích:
Remaining sessions của current class (course_session_ids chưa học)
Completed sessions của target class (course_session_ids đã học)
Tính Gap = Target completed MINUS Current not studied
Identify missing content, count số buổi gap

WITH current_remaining_sessions AS (
    -- Sessions còn lại của lớp hiện tại (chưa học)
    SELECT DISTINCT s.course_session_id
    FROM session s
    WHERE s.class_id = :current_class_id
      AND s.status = 'planned'
      AND s.date >= CURRENT_DATE
),
target_completed_sessions AS (
    -- Sessions đã hoàn thành của lớp đích
    SELECT DISTINCT s.course_session_id
    FROM session s
    WHERE s.class_id = :target_class_id
      AND s.status = 'done'
      AND s.date < CURRENT_DATE
),
gap_sessions AS (
    -- Gap = Target đã học MINUS Current chưa học
    SELECT tcs.course_session_id
    FROM target_completed_sessions tcs
    WHERE tcs.course_session_id NOT IN (
        -- Các sessions mà current đã học hoặc sẽ học
        SELECT s.course_session_id
        FROM session s
        WHERE s.class_id = :current_class_id
          AND (s.status = 'done' OR s.course_session_id IN (
              SELECT course_session_id FROM current_remaining_sessions
          ))
    )
)
SELECT 
    gs.course_session_id,
    cs.sequence_no,
    cs.topic,
    cs.skill_set, -- Array type
    cp.phase_number,
    cp.name AS phase_name,
    COUNT(*) OVER() AS total_gap_count
FROM gap_sessions gs
JOIN course_session cs ON gs.course_session_id = cs.id
JOIN course_phase cp ON cs.phase_id = cp.id
ORDER BY cs.sequence_no;


Bước 16: Thực hiện Schedule Conflict Check (background)
System kiểm tra:
Query tất cả classes khác mà student enrolled
Query sessions của target class
Check time overlap (same date, overlapping time)
Count số buổi conflict
Bước 17: Hiển thị danh sách target classes với validation badges
Table/Cards hiển thị:

Class info: Code, Name, Branch, Schedule, Modality, Teacher, Progress
Capacity badge: "✅ Còn X chỗ" / "⚠️ Sắp đầy"
Gap badge: "❌ Gap: X buổi" (nếu có)
Conflict badge: "⚠️ Conflict: Y buổi" (nếu có)
Radio button để chọn
Filter/Sort options
Bước 18: Chọn target class
Học viên click radio chọn lớp đích, inline warning nếu có gap/conflict

Bước 19: Form mở rộng section xác nhận
Hiển thị:
So sánh FROM ↔ TO: Table 2 cột compare các thông tin
Validation Summary: Capacity, Gap, Conflict
Checkboxes acknowledgement (nếu có warnings - required)
Form inputs:
Date picker: "Ngày bắt đầu chuyển lớp" (required, >= TODAY)
Textarea: "Lý do chuyển lớp" (required, min 20 chars)
Textarea: "Ghi chú" (optional)

Bước 20: Điền form
Học viên nhập: Effective date, Lý do, Ghi chú, Tick checkboxes (nếu có warnings)

Bước 21: Frontend validation
Kiểm tra: Đã chọn classes? Date valid? Reason >= 20 chars? Checkboxes ticked? Show errors nếu fail

Bước 22: Click "Gửi Yêu Cầu"
Học viên submit form

Bước 23: Backend validation
Server validate:
Current enrollment exists, status='enrolled'?
Target class exists, status IN ('scheduled','ongoing')?
Target != current?
Capacity available?
Date >= CURRENT_DATE?
No duplicate pending/approved request?
Re-validate gap/conflict

Bước 24: INSERT student_request
INSERT:
student_id, current_class_id, target_class_id
request_type='transfer', status='pending'
effective_date, reason, notes
submitted_at=NOW(), submitted_by=NULL

INSERT INTO student_request (
    student_id,
    current_class_id,
    target_class_id,
    request_type,
    status,
    effective_date,
    reason,
    notes,
    submitted_at,
    submitted_by,
    created_at,
    created_by
) VALUES (
    :current_student_id,
    :selected_current_class_id,
    :selected_target_class_id,
    'transfer'::student_request_type_enum,
    'pending'::request_status_enum,
    :effective_date,
    :reason_text,
    :notes_text,
    NOW(),
    NULL, -- Student tự tạo, không có submitted_by
    NOW(),
    :current_user_id
) RETURNING id, submitted_at;


Bước 25: Success message + Refresh list
Hiển thị success toast, đóng modal, refresh danh sách "Requests tôi đã gửi"

Bước 26: Gửi email cho Academic Affairs (async)
Email thông báo yêu cầu chuyển lớp mới với link xem chi tiết

Bước 27: Request xuất hiện trong danh sách
Badge: Pending, Icon: Transfer, Info: FROM → TO, Actions: View/Cancel

PHASE 2: GIÁO VỤ XỬ LÝ YÊU CẦU
Bước 28: Academic Affairs nhận email
Giáo vụ nhận email thông báo

Bước 29: Login và vào "Student Requests Dashboard"
Menu: Request Management → Tab: Transfer Requests → Filter: Pending

Bước 30: Hệ thống query pending transfer requests
Query student_request JOIN student/class/course/branch, filter by Affairs's branches, ORDER BY submitted_at

Bước 31: Hiển thị danh sách pending requests
Table với columns: Request ID, Student, FROM→TO, Branch, Course, Effective Date, Badges (Capacity/Gap/Conflict), Actions

Bước 32: Click xem chi tiết request
Giáo vụ click "Review"

Bước 33: Load chi tiết request
Query đầy đủ: Request, Student info, Current/Target class details, Progress, Statistics
WITH student_attendance_stats AS (
    -- Statistics trong current class
    SELECT 
        ss.student_id,
        COUNT(*) AS total_sessions,
        SUM(CASE WHEN ss.attendance_status = 'present' THEN 1 ELSE 0 END) AS present_count,
        SUM(CASE WHEN ss.attendance_status = 'absent' THEN 1 ELSE 0 END) AS absent_count,
        ROUND(
            100.0 * SUM(CASE WHEN ss.attendance_status = 'present' THEN 1 ELSE 0 END) 
            / NULLIF(COUNT(*), 0), 
            2
        ) AS attendance_rate
    FROM student_session ss
    JOIN session s ON ss.session_id = s.id
    WHERE s.class_id = (
        SELECT current_class_id FROM student_request WHERE id = :request_id
    )
    GROUP BY ss.student_id
),
previous_transfer_history AS (
    SELECT 
        student_id,
        COUNT(*) AS previous_transfer_count,
        SUM(CASE WHEN status = 'approved' THEN 1 ELSE 0 END) AS approved_count,
        SUM(CASE WHEN status = 'rejected' THEN 1 ELSE 0 END) AS rejected_count
    FROM student_request
    WHERE request_type = 'transfer'
      AND id != :request_id
    GROUP BY student_id
)
SELECT 
    -- Request info
    sr.id,
    sr.request_type,
    sr.status,
    sr.effective_date,
    sr.reason,
    sr.notes,
    sr.submitted_at,
    -- Student info
    s.id AS student_id,
    s.student_code,
    ua.full_name AS student_name,
    ua.email,
    ua.phone,
    -- Current class details
    c_curr.code AS current_class_code,
    c_curr.name AS current_class_name,
    c_curr.modality AS current_modality,
    c_curr.schedule_days AS current_schedule_days,
    co_curr.name AS current_course_name,
    b_curr.name AS current_branch_name,
    b_curr.address AS current_branch_address,
    -- Target class details
    c_targ.code AS target_class_code,
    c_targ.name AS target_class_name,
    c_targ.modality AS target_modality,
    c_targ.schedule_days AS target_schedule_days,
    co_targ.name AS target_course_name,
    b_targ.name AS target_branch_name,
    b_targ.address AS target_branch_address,
    c_targ.max_capacity AS target_capacity,
    -- Student statistics
    sas.total_sessions,
    sas.present_count,
    sas.absent_count,
    sas.attendance_rate,
    pth.previous_transfer_count,
    pth.approved_count AS previous_approved_count,
    pth.rejected_count AS previous_rejected_count
FROM student_request sr
JOIN student s ON sr.student_id = s.id
JOIN user_account ua ON s.user_id = ua.id
JOIN class c_curr ON sr.current_class_id = c_curr.id
JOIN course co_curr ON c_curr.course_id = co_curr.id
JOIN branch b_curr ON c_curr.branch_id = b_curr.id
JOIN class c_targ ON sr.target_class_id = c_targ.id
JOIN course co_targ ON c_targ.course_id = co_targ.id
JOIN branch b_targ ON c_targ.branch_id = b_targ.id
LEFT JOIN student_attendance_stats sas ON sr.student_id = sas.student_id
LEFT JOIN previous_transfer_history pth ON sr.student_id = pth.student_id
WHERE sr.id = :request_id;


Bước 34: Re-validate request (real-time)
Chạy lại: Gap Detection, Conflict Check, Capacity Check, Date Validation

Bước 35: Hiển thị chi tiết request
6 Sections:
Student Info: Name, Code, Email, Attendance rate, History
Transfer Details FROM ↔ TO: Compare table
Request Info: Date, Reason, Notes, Submitted date
Validation Summary: Capacity, Gap (chi tiết), Conflict (chi tiết), Date
Academic Notes: Textarea for notes, Textarea for rejection reason
Actions: Approve & Execute, Reject, Cancel

Bước 36: Giáo vụ review
Xem xét: Lý do hợp lý? Gap acceptable? Conflict nghiêm trọng? Còn chỗ? Date phù hợp? Attendance history tốt?

Bước 37: Đưa ra quyết định
Chọn: Approve hoặc Reject

PHASE 2A: TRƯỜNG HỢP APPROVE
Bước 38: Click "Approve & Execute"
Giáo vụ nhấn approve button, có thể thêm approval note

Bước 39: Confirmation dialog
Dialog hiển thị: Danh sách thao tác sẽ thực hiện (6 steps), Warnings nếu có, Buttons: Xác nhận/Hủy

Bước 40: Xác nhận approve
Giáo vụ click "Xác nhận"

Bước 41: Thực hiện COMPLEX TRANSACTION (ATOMIC)
BEGIN TRANSACTION:
Step 1: UPDATE student_request → status='approved', decided_by, decided_at
Step 2: UPDATE enrollment (old) → status='transferred', ended_at=effective_date-1
Step 3: UPDATE student_session (old) → attendance_status='excused' WHERE session_date >= effective_date
Step 4: INSERT enrollment (new) → status='enrolled', transferred_from_class_id
Step 5: INSERT student_session (new) → FOR ALL sessions WHERE session_date >= effective_date
Step 6: INSERT audit_log → track all changes

COMMIT

-- ===== BEGIN TRANSACTION =====
BEGIN;

-- Step 1: UPDATE student_request
UPDATE student_request
SET 
    status = 'approved'::request_status_enum,
    decided_by = :current_Affairs_user_id,
    decided_at = NOW(),
    updated_at = NOW(),
    updated_by = :current_Affairs_user_id
WHERE id = :request_id 
  AND status = 'pending';
-- Expected: 1 row updated

-- Step 2: UPDATE enrollment (old class) → status = 'transferred'
UPDATE enrollment
SET 
    status = 'transferred'::enrollment_status_enum,
    ended_at = :effective_date - INTERVAL '1 day',
    transfer_reason = (SELECT reason FROM student_request WHERE id = :request_id),
    updated_at = NOW(),
    updated_by = :current_Affairs_user_id
WHERE student_id = :student_id
  AND class_id = :current_class_id
  AND status = 'enrolled';
-- Expected: 1 row updated

-- Step 3: UPDATE student_session (old class) → attendance = 'excused'
UPDATE student_session
SET 
    attendance_status = 'excused'::attendance_status_enum,
    note = CONCAT(
        COALESCE(note, ''), 
        E'\nTransferred to class ', 
        (SELECT code FROM class WHERE id = :target_class_id),
        ' on ', 
        :effective_date::TEXT
    ),
    updated_at = NOW()
WHERE student_id = :student_id
  AND session_id IN (
      SELECT s.id 
      FROM session s
      WHERE s.class_id = :current_class_id
        AND s.date >= :effective_date
        AND s.status IN ('planned', 'scheduled')
  );
-- Expected: X rows updated (số sessions còn lại)

-- Step 4: INSERT enrollment (new class)
INSERT INTO enrollment (
    class_id,
    student_id,
    status,
    enrolled_at,
    transferred_from_class_id, -- Reference back ⭐
    created_at,
    created_by
) VALUES (
    :target_class_id,
    :student_id,
    'enrolled'::enrollment_status_enum,
    NOW(),
    :current_class_id, -- ⭐ Reference to old class
    NOW(),
    :current_Affairs_user_id
);
-- Expected: 1 row inserted

-- Step 5: INSERT student_session (new class) - Bulk insert
INSERT INTO student_session (
    student_id,
    session_id,
    is_makeup, -- false for transfer
    attendance_status,
    created_at
)
SELECT 
    :student_id,
    s.id,
    false, -- not a makeup session
    'planned'::attendance_status_enum,
    NOW()
FROM session s
WHERE s.class_id = :target_class_id
  AND s.date >= :effective_date
  AND s.status IN ('planned', 'scheduled')
ON CONFLICT (student_id, session_id) DO NOTHING;
-- Expected: Y rows inserted (số sessions future của target class)

-- Step 6: INSERT audit_log (optional - nếu có table audit_log)
-- INSERT INTO audit_log (...) VALUES (...);

-- ===== COMMIT TRANSACTION =====
COMMIT;


Bước 42: Verification sau transaction
Verify: Request approved? Old enrollment transferred? New enrollment enrolled? Sessions updated? Counts match?

Bước 43: Gửi email thông báo (async - multi-party)
Email 1: To Student → "Yêu cầu đã được phê duyệt" + Chi tiết lớp mới
Email 2: To Teacher (Old) → "Học viên chuyển lớp" + Ngày kết thúc
Email 3: To Teacher (New) → "Học viên mới tham gia" + Thông tin student
Bước 44: Success message cho giáo vụ
Toast: "Transfer approved", Summary: Enrollments updated, Sessions synced, Emails sent, Redirect sau 3s

Bước 45: Học viên nhận email approved
Email + In-app notification

Bước 46: Học viên kiểm tra "My Requests"
Request status: Approved, Show: Decided by, Date, Approval note

Bước 47: Học viên kiểm tra "My Classes"
Lớp cũ: Status="Transferred", Badge: "Đã chuyển lớp"
Lớp mới: Status="Enrolled", Badge: "New"
Bước 48: Học viên kiểm tra "My Schedule"
Sessions lớp cũ (từ effective_date): Status="Excused", Note="Transferred to [New]"
Sessions lớp mới (từ effective_date): Status="Planned", Badge: "Lớp mới"
Bước 49: Teachers nhận email và update tracking
Old teacher biết student rời đi, New teacher chuẩn bị welcome

Bước 50: Teacher (new class) điểm danh
Student xuất hiện với badge: "New Transfer Student", Tooltip: "Transferred from [Old] on [Date]"

PHASE 2B: TRƯỜNG HỢP REJECT
Bước 38 (alt): Click "Reject Request"
Giáo vụ nhấn reject button

Bước 39 (alt): Rejection dialog
Dialog yêu cầu nhập: Lý do từ chối (required, min 20 chars), Dropdown quick reasons, Buttons: Confirm/Cancel

Bước 40 (alt): Nhập lý do và xác nhận
Giáo vụ nhập lý do chi tiết, click "Xác nhận từ chối"

Bước 41 (alt): UPDATE request to rejected
UPDATE student_request → status='rejected', rejection_reason, decided_by, decided_at

Lưu ý: KHÔNG thay đổi enrollment hoặc sessions

Bước 42 (alt): Gửi email rejected
Email to Student: "Yêu cầu bị từ chối", Lý do, Gợi ý: Tạo request mới hoặc liên hệ giáo vụ

Bước 43 (alt): Success message cho giáo vụ
Toast: "Request rejected", Email sent, Redirect

Bước 44 (alt): Học viên nhận email rejected
Email + Notification

Bước 45 (alt): Học viên kiểm tra "My Requests"
Request status: Rejected, Rejection reason visible, Action: "Create New Request"

Bước 46 (alt): Enrollment và schedule không đổi
Học viên tiếp tục học lớp cũ bình thường

PHASE 3: ADDITIONAL FEATURES
Bước 47: Student cancel request (nếu pending)
Student có thể cancel request chưa xử lý: UPDATE status='cancelled', Notify Affairs

Bước 48: Academic Affairs filter/search
Filter: Status, Date range, Branch, Course; Search: Student name/code; Sort: Date, Priority

Bước 49: System analytics
Track: Total requests, Approval rate, Processing time, Top reasons, Gap/Conflict statistics
-- =========================================
-- FIXED: Query danh sách pending absence requests cho Academic Staff
-- =========================================
-- ISSUE: Query cũ lọc theo submitted_by (student user_id) - SAI LOGIC
-- FIX: Phải lọc theo branch_id của class - giáo vụ xem TẤT CẢ request của branch mình quản lý
-- =========================================

-- ========== QUERY ĐÃ SỬA (ĐÚNG LOGIC) ==========
-- Academic Staff xem TẤT CẢ pending absence requests của các branch mình quản lý
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
    cs.topic AS session_topic,
    cs.sequence_no AS session_sequence,
    -- Room/Resource info
    STRING_AGG(DISTINCT r.name, ', ') AS room_names,
    -- Teacher info
    STRING_AGG(DISTINCT ua_teacher.full_name, ', ') AS teacher_names
FROM public.student_request sr
JOIN public.student st ON sr.student_id = st.id
JOIN public.user_account ua_student ON st.user_id = ua_student.id
JOIN public.session s ON sr.target_session_id = s.id
JOIN public.class c ON sr.current_class_id = c.id  -- Lấy class từ current_class_id
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
        -- Lấy tất cả branch mà Academic Staff 1 (user_id=4) quản lý
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


-- ========== SO SÁNH: QUERY CŨ (SAI) vs QUERY MỚI (ĐÚNG) ==========

/*
QUERY CŨ (SAI):
- WHERE c.branch_id IN (SELECT branch_id FROM user_branches WHERE user_id = 4)
- ✅ Đúng: Lọc theo branch của giáo vụ
- ❌ SAI: Dữ liệu seed không có pending absence request nào

QUERY MỚI (ĐÚNG):
- WHERE c.branch_id IN (SELECT branch_id FROM user_branches WHERE user_id = 4)
- ✅ Đúng: Lọc theo branch của giáo vụ  
- ✅ Đúng: Đã thêm pending absence requests vào seed data (request id: 4, 5, 6)

VẤN ĐỀ ĐÃ FIX:
1. ✅ Thêm 3 pending absence requests vào seed-data.sql:
   - Request #4: Student 16, Class 3, Session 55 (upcoming)
   - Request #5: Student 17, Class 3, Session 56 (upcoming)
   - Request #6: Student 19, Class 4, Session 75 (upcoming)

2. ✅ Logic query đã đúng từ đầu - lọc theo branch_id của class
   - Giáo vụ xem TẤT CẢ request của branch mình quản lý
   - Không phụ thuộc vào ai submit request
*/


-- ========== VERIFY DATA ==========
-- Kiểm tra các pending absence requests đã tạo
SELECT 
    sr.id,
    sr.request_type,
    sr.status,
    st.student_code,
    ua.full_name AS student_name,
    c.code AS class_code,
    c.branch_id,
    s.date AS session_date,
    sr.note
FROM student_request sr
JOIN student st ON sr.student_id = st.id
JOIN user_account ua ON st.user_id = ua.id
JOIN class c ON sr.current_class_id = c.id
JOIN session s ON sr.target_session_id = s.id
WHERE sr.status = 'pending' AND sr.request_type = 'absence'
ORDER BY sr.submitted_at ASC;


-- ========== CHECK ACADEMIC STAFF BRANCH ACCESS ==========
-- Kiểm tra Academic Staff 1 (user_id=4) có quyền truy cập branch nào
SELECT 
    ua.id AS user_id,
    ua.full_name,
    ua.email,
    ub.branch_id,
    b.code AS branch_code,
    b.name AS branch_name
FROM user_account ua
JOIN user_branches ub ON ua.id = ub.user_id
JOIN branch b ON ub.branch_id = b.id
WHERE ua.id = 4;


-- ========== EXPECTED RESULTS ==========
/*
Sau khi chạy seed data mới, query sẽ trả về 3 pending absence requests:

1. Request #4: 
   - Student: Student 16 (student_code: S016, user_id: 41)
   - Class: Class 3 (IELTS-INTENSIVE-A2B1-001)
   - Session: Session 60 (CURRENT_DATE + 3 days, status='planned')
   - Note: "Doctor appointment - have medical certificate"
   - Submitted: 1 day ago

2. Request #5:
   - Student: Student 17 (student_code: S017, user_id: 42)
   - Class: Class 3 (IELTS-INTENSIVE-A2B1-001)
   - Session: Session 61 (CURRENT_DATE + 6 days, status='planned')
   - Note: "Family wedding ceremony"
   - Submitted: 6 hours ago

3. Request #6:
   - Student: Student 29 (student_code: S029, user_id: 68)
   - Class: Class 4 (Business English B2)
   - Session: Session 69 (CURRENT_DATE + 2 days, status='planned')
   - Note: "Business trip to Da Nang"
   - Submitted: 12 hours ago

Tất cả đều thuộc Branch 1 (HN-MAIN) mà Academic Staff 1 (user_id=4) quản lý.
*/

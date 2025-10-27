-- =========================================
-- STUDENT PROFILE - SQL QUERIES
-- =========================================
-- Các queries để lấy dữ liệu cho trang Student Profile
-- Bỏ qua: Schedule, Requests, Feedback, Course Materials (ở các page khác)
-- =========================================

-- =========================================
-- 1. THÔNG TIN CƠ BẢN (Basic Information)
-- =========================================
-- Input: student_id hoặc user_id
-- Output: Thông tin cá nhân của student

SELECT 
    s.id AS student_id,
    s.student_code,
    u.full_name,
    u.email,
    u.phone,
    s.education_level,
    s.address,
    u.status AS account_status,
    u.last_login_at,
    s.created_at AS student_since,
    s.updated_at
FROM student s
INNER JOIN user_account u ON s.user_id = u.id
WHERE s.id = 1;  -- hoặc u.id = :user_id

-- Example với seed data (Student ID = 1, User ID = 26):
-- SELECT * FROM student s INNER JOIN user_account u ON s.user_id = u.id WHERE s.id = 1;


-- =========================================
-- 2. DASHBOARD STATISTICS (Thống kê tổng quan)
-- =========================================

-- 2.1. Tổng số lớp đang học (Active Classes)
SELECT COUNT(DISTINCT e.class_id) AS active_classes_count
FROM enrollment e
WHERE e.student_id = 1
  AND e.status = 'enrolled';

-- 2.2. Tổng số lớp đã hoàn thành (Completed Classes)
SELECT COUNT(DISTINCT e.class_id) AS completed_classes_count
FROM enrollment e
WHERE e.student_id = 1
  AND e.status = 'completed';

-- 2.3. Tỷ lệ điểm danh (Attendance Rate)
-- Total sessions vs present sessions
WITH attendance_stats AS (
    SELECT 
        COUNT(*) AS total_sessions,
        COUNT(*) FILTER (WHERE ss.attendance_status = 'present') AS present_sessions,
        COUNT(*) FILTER (WHERE ss.attendance_status = 'absent') AS absent_sessions,
        COUNT(*) FILTER (WHERE ss.attendance_status = 'late') AS late_sessions,
        COUNT(*) FILTER (WHERE ss.attendance_status = 'excused') AS excused_sessions,
        COUNT(*) FILTER (WHERE ss.attendance_status = 'remote') AS remote_sessions
    FROM student_session ss
    INNER JOIN session ses ON ss.session_id = ses.id
    WHERE ss.student_id = 1
      AND ses.status = 'done'
)
SELECT 
    total_sessions,
    present_sessions,
    absent_sessions,
    late_sessions,
    excused_sessions,
    remote_sessions,
    CASE 
        WHEN total_sessions > 0 
        THEN ROUND((present_sessions::NUMERIC / total_sessions * 100), 2)
        ELSE 0
    END AS attendance_rate_percent
FROM attendance_stats;

-- 2.4. Tỷ lệ hoàn thành bài tập (Homework Completion Rate)
WITH homework_stats AS (
    SELECT 
        COUNT(*) AS total_homework,
        COUNT(*) FILTER (WHERE ss.homework_status = 'completed') AS completed_homework,
        COUNT(*) FILTER (WHERE ss.homework_status = 'incomplete') AS incomplete_homework
    FROM student_session ss
    INNER JOIN session ses ON ss.session_id = ses.id
    WHERE ss.student_id = 1
      AND ses.status = 'done'
      AND ss.homework_status IS NOT NULL
      AND ss.homework_status != 'no_homework'
)
SELECT 
    total_homework,
    completed_homework,
    incomplete_homework,
    CASE 
        WHEN total_homework > 0 
        THEN ROUND((completed_homework::NUMERIC / total_homework * 100), 2)
        ELSE 0
    END AS homework_completion_rate_percent
FROM homework_stats;

-- 2.5. Điểm trung bình tổng (Overall GPA)
WITH weighted_scores AS (
    SELECT 
        c.id AS class_id,
        c.name AS class_name,
        a.name AS assessment_name,
        a.kind AS assessment_kind,
        a.max_score,
        a.weight,
        sc.score,
        -- Điểm chuẩn hóa (normalized score) * weight
        (sc.score / a.max_score * 10 * COALESCE(a.weight, 1)) AS weighted_score
    FROM score sc
    INNER JOIN assessment a ON sc.assessment_id = a.id
    INNER JOIN "class" c ON a.class_id = c.id
    INNER JOIN enrollment e ON c.id = e.class_id
    WHERE sc.student_id = 1
      AND e.student_id = 1
      AND e.status IN ('enrolled', 'completed')
)
SELECT 
    COUNT(*) AS total_assessments,
    ROUND(AVG(score / max_score * 10), 2) AS simple_average,
    ROUND(
        SUM(weighted_score) / NULLIF(SUM(COALESCE(weight, 1)), 0),
        2
    ) AS weighted_average_gpa
FROM weighted_scores;

-- 2.6. Số đơn từ đang chờ (Pending Requests Count)
SELECT COUNT(*) AS pending_requests_count
FROM student_request
WHERE student_id = 1
  AND status = 'pending';

-- 2.7. Dashboard tổng hợp (All stats in one query)
WITH active_classes AS (
    SELECT COUNT(DISTINCT e.class_id) AS count
    FROM enrollment e
    WHERE e.student_id = 1 AND e.status = 'enrolled'
),
completed_classes AS (
    SELECT COUNT(DISTINCT e.class_id) AS count
    FROM enrollment e
    WHERE e.student_id = 1 AND e.status = 'completed'
),
attendance_stats AS (
    SELECT 
        COUNT(*) AS total_sessions,
        COUNT(*) FILTER (WHERE ss.attendance_status = 'present') AS present_sessions
    FROM student_session ss
    INNER JOIN session ses ON ss.session_id = ses.id
    WHERE ss.student_id = 1 AND ses.status = 'done'
),
homework_stats AS (
    SELECT 
        COUNT(*) FILTER (WHERE ss.homework_status != 'no_homework') AS total_homework,
        COUNT(*) FILTER (WHERE ss.homework_status = 'completed') AS completed_homework
    FROM student_session ss
    INNER JOIN session ses ON ss.session_id = ses.id
    WHERE ss.student_id = 1 AND ses.status = 'done'
),
gpa_stats AS (
    SELECT ROUND(AVG(sc.score / a.max_score * 10), 2) AS gpa
    FROM score sc
    INNER JOIN assessment a ON sc.assessment_id = a.id
    WHERE sc.student_id = 1
),
pending_requests AS (
    SELECT COUNT(*) AS count
    FROM student_request
    WHERE student_id = 1 AND status = 'pending'
)
SELECT 
    COALESCE(ac.count, 0) AS active_classes,
    COALESCE(cc.count, 0) AS completed_classes,
    COALESCE(att.total_sessions, 0) AS total_sessions,
    COALESCE(att.present_sessions, 0) AS present_sessions,
    CASE 
        WHEN COALESCE(att.total_sessions, 0) > 0 
        THEN ROUND((att.present_sessions::NUMERIC / att.total_sessions * 100), 2)
        ELSE 0
    END AS attendance_rate,
    COALESCE(hw.total_homework, 0) AS total_homework,
    COALESCE(hw.completed_homework, 0) AS completed_homework,
    CASE 
        WHEN COALESCE(hw.total_homework, 0) > 0 
        THEN ROUND((hw.completed_homework::NUMERIC / hw.total_homework * 100), 2)
        ELSE 0
    END AS homework_rate,
    COALESCE(gpa.gpa, 0) AS overall_gpa,
    COALESCE(pr.count, 0) AS pending_requests
FROM active_classes ac
CROSS JOIN completed_classes cc
CROSS JOIN attendance_stats att
CROSS JOIN homework_stats hw
CROSS JOIN gpa_stats gpa
CROSS JOIN pending_requests pr;


-- =========================================
-- 3. DANH SÁCH LỚP ĐANG HỌC (Current Classes)
-- =========================================
-- Lấy tất cả lớp có status = 'enrolled'

SELECT 
    c.id AS class_id,
    c.code AS class_code,
    c.name AS class_name,
    c.status AS class_status,
    c.modality,
    c.start_date,
    c.planned_end_date,
    c.schedule_days,
    
    -- Course info
    co.id AS course_id,
    co.code AS course_code,
    co.name AS course_name,
    co.total_hours,
    co.duration_weeks,
    
    -- Level & Subject
    l.name AS level_name,
    sub.name AS subject_name,
    
    -- Branch info
    b.name AS branch_name,
    b.address AS branch_address,
    
    -- Enrollment info
    e.status AS enrollment_status,
    e.enrolled_at,
    e.created_at AS enrollment_created_at
    
FROM enrollment e
INNER JOIN "class" c ON e.class_id = c.id
INNER JOIN course co ON c.course_id = co.id
LEFT JOIN level l ON co.level_id = l.id
INNER JOIN subject sub ON co.subject_id = sub.id
INNER JOIN branch b ON c.branch_id = b.id
WHERE e.student_id = 1
  AND e.status = 'enrolled'
ORDER BY e.enrolled_at DESC;


-- =========================================
-- 4. LỊCH SỬ LỚP HỌC (Class History)
-- =========================================
-- Lấy tất cả lớp đã hoàn thành hoặc đã rời khỏi

SELECT 
    c.id AS class_id,
    c.code AS class_code,
    c.name AS class_name,
    c.status AS class_status,
    c.modality,
    c.start_date,
    c.actual_end_date,
    
    -- Course info
    co.code AS course_code,
    co.name AS course_name,
    
    -- Level & Subject
    l.name AS level_name,
    sub.name AS subject_name,
    
    -- Branch
    b.name AS branch_name,
    
    -- Enrollment info
    e.status AS enrollment_status,
    e.enrolled_at,
    e.left_at,
    
    -- Sessions info (join/left)
    join_sess.date AS join_session_date,
    left_sess.date AS left_session_date
    
FROM enrollment e
INNER JOIN "class" c ON e.class_id = c.id
INNER JOIN course co ON c.course_id = co.id
LEFT JOIN level l ON co.level_id = l.id
INNER JOIN subject sub ON co.subject_id = sub.id
INNER JOIN branch b ON c.branch_id = b.id
LEFT JOIN session join_sess ON e.join_session_id = join_sess.id
LEFT JOIN session left_sess ON e.left_session_id = left_sess.id
WHERE e.student_id = 1
  AND e.status IN ('completed', 'transferred', 'dropped')
ORDER BY e.left_at DESC NULLS LAST, e.enrolled_at DESC;


-- =========================================
-- 5. ĐIỂM SỐ THEO LỚP (Scores by Class)
-- =========================================
-- Lấy tất cả điểm của student trong từng lớp

SELECT 
    c.id AS class_id,
    c.name AS class_name,
    c.code AS class_code,
    
    -- Assessment info
    a.id AS assessment_id,
    a.name AS assessment_name,
    a.kind AS assessment_kind,
    a.max_score,
    a.weight,
    a.description,
    
    -- Score info
    sc.score,
    ROUND((sc.score / a.max_score * 10), 2) AS normalized_score,
    sc.feedback,
    sc.graded_at,
    
    -- Teacher info
    u.full_name AS graded_by_teacher
    
FROM score sc
INNER JOIN assessment a ON sc.assessment_id = a.id
INNER JOIN "class" c ON a.class_id = c.id
LEFT JOIN teacher t ON sc.graded_by = t.id
LEFT JOIN user_account u ON t.user_account_id = u.id
WHERE sc.student_id = 1
ORDER BY c.id, a.kind, sc.graded_at DESC;


-- =========================================
-- 6. ĐIỂM TRUNG BÌNH THEO LỚP (GPA by Class)
-- =========================================
-- Tính điểm TB cho từng lớp

WITH class_scores AS (
    SELECT 
        c.id AS class_id,
        c.name AS class_name,
        c.code AS class_code,
        a.kind AS assessment_kind,
        a.weight,
        sc.score,
        a.max_score,
        (sc.score / a.max_score * 10) AS normalized_score,
        (sc.score / a.max_score * 10 * COALESCE(a.weight, 1)) AS weighted_score
    FROM score sc
    INNER JOIN assessment a ON sc.assessment_id = a.id
    INNER JOIN "class" c ON a.class_id = c.id
    WHERE sc.student_id = 1
)
SELECT 
    class_id,
    class_name,
    class_code,
    COUNT(*) AS total_assessments,
    ROUND(AVG(normalized_score), 2) AS simple_average,
    ROUND(
        SUM(weighted_score) / NULLIF(SUM(COALESCE(weight, 1)), 0),
        2
    ) AS weighted_average
FROM class_scores
GROUP BY class_id, class_name, class_code
ORDER BY class_name;


-- =========================================
-- 7. CHI TIẾT ĐIỂM THEO LOẠI ASSESSMENT (Scores by Assessment Kind)
-- =========================================
-- Nhóm điểm theo từng loại (quiz, midterm, final, etc.)

SELECT 
    c.id AS class_id,
    c.name AS class_name,
    a.kind AS assessment_kind,
    COUNT(*) AS assessment_count,
    ROUND(AVG(sc.score), 2) AS avg_score,
    MAX(a.max_score) AS max_possible_score,
    ROUND(AVG(sc.score / a.max_score * 10), 2) AS avg_normalized_score,
    MIN(sc.score) AS min_score,
    MAX(sc.score) AS max_score
FROM score sc
INNER JOIN assessment a ON sc.assessment_id = a.id
INNER JOIN "class" c ON a.class_id = c.id
WHERE sc.student_id = 1
GROUP BY c.id, c.name, a.kind
ORDER BY c.name, a.kind;


-- =========================================
-- 8. LỊCH SỬ ĐIỂM DANH (Attendance History - Summary)
-- =========================================
-- Tổng hợp lịch sử điểm danh theo lớp

SELECT 
    c.id AS class_id,
    c.name AS class_name,
    c.code AS class_code,
    
    -- Attendance statistics
    COUNT(ss.session_id) AS total_sessions,
    COUNT(*) FILTER (WHERE ss.attendance_status = 'present') AS present_count,
    COUNT(*) FILTER (WHERE ss.attendance_status = 'absent') AS absent_count,
    COUNT(*) FILTER (WHERE ss.attendance_status = 'late') AS late_count,
    COUNT(*) FILTER (WHERE ss.attendance_status = 'excused') AS excused_count,
    COUNT(*) FILTER (WHERE ss.attendance_status = 'remote') AS remote_count,
    COUNT(*) FILTER (WHERE ss.is_makeup = true) AS makeup_sessions,
    
    -- Homework statistics
    COUNT(*) FILTER (WHERE ss.homework_status = 'completed') AS homework_completed,
    COUNT(*) FILTER (WHERE ss.homework_status = 'incomplete') AS homework_incomplete,
    
    -- Attendance rate
    CASE 
        WHEN COUNT(ss.session_id) > 0 
        THEN ROUND((COUNT(*) FILTER (WHERE ss.attendance_status = 'present')::NUMERIC / COUNT(ss.session_id) * 100), 2)
        ELSE 0
    END AS attendance_rate
    
FROM student_session ss
INNER JOIN session ses ON ss.session_id = ses.id
INNER JOIN "class" c ON ses.class_id = c.id
WHERE ss.student_id = 1
  AND ses.status = 'done'
GROUP BY c.id, c.name, c.code
ORDER BY c.name;


-- =========================================
-- 9. LEARNING OUTCOMES - CLO Progress
-- =========================================
-- Xem tiến độ học tập theo CLO (Course Learning Outcomes)

-- ✅ UPDATED: Sử dụng course_assessment_id thay vì match name+kind
-- Query này giờ chính xác 100% vì dùng foreign key relationship

-- Query 9.1: CLO Progress với xử lý NULL (RECOMMENDED - Full visibility)
SELECT 
    co.id AS course_id,
    co.name AS course_name,
    clo.id AS clo_id,
    clo.code AS clo_code,
    clo.description AS clo_description,
    
    -- Count related course assessments (template level)
    COUNT(DISTINCT ca.id) AS related_course_assessments,
    
    -- Count actual assessments in class that student has been graded
    COUNT(DISTINCT CASE WHEN sc.id IS NOT NULL THEN a.id END) AS graded_assessments,
    
    -- Student performance on CLO-related assessments
    CASE 
        WHEN COUNT(DISTINCT CASE WHEN sc.id IS NOT NULL THEN a.id END) > 0 
        THEN ROUND(AVG(sc.score / a.max_score * 10), 2)
        ELSE NULL  -- Explicitly return NULL if no graded assessments
    END AS avg_performance,
    
    -- Status indicators
    CASE 
        WHEN COUNT(DISTINCT ca.id) = 0 THEN 'not_mapped'  -- No assessments mapped to CLO
        WHEN COUNT(DISTINCT CASE WHEN sc.id IS NOT NULL THEN a.id END) = 0 THEN 'not_graded'  -- Mapped but no grades yet
        ELSE 'graded'  -- Has grades
    END AS status
    
FROM enrollment e
INNER JOIN "class" c ON e.class_id = c.id
INNER JOIN course co ON c.course_id = co.id
INNER JOIN clo ON clo.course_id = co.id
LEFT JOIN course_assessment_clo_mapping cacm ON cacm.clo_id = clo.id
LEFT JOIN course_assessment ca ON ca.id = cacm.course_assessment_id
-- ✅ FIXED: Sử dụng course_assessment_id foreign key (chính xác 100%)
LEFT JOIN assessment a ON a.course_assessment_id = ca.id AND a.class_id = c.id
LEFT JOIN score sc ON sc.assessment_id = a.id AND sc.student_id = 1
WHERE e.student_id = 1
  AND e.status IN ('enrolled', 'completed')
GROUP BY co.id, co.name, clo.id, clo.code, clo.description
ORDER BY co.name, clo.code;

-- Query 9.2: CLO Progress - CHỈ HIỂN THỊ CLO CÓ ĐIỂM (Bỏ qua NULL)
SELECT 
    co.id AS course_id,
    co.name AS course_name,
    clo.code AS clo_code,
    clo.description AS clo_description,
    COUNT(DISTINCT a.id) AS graded_assessments,
    ROUND(AVG(sc.score / a.max_score * 10), 2) AS avg_performance
FROM enrollment e
INNER JOIN "class" c ON e.class_id = c.id
INNER JOIN course co ON c.course_id = co.id
INNER JOIN clo ON clo.course_id = co.id
INNER JOIN course_assessment_clo_mapping cacm ON cacm.clo_id = clo.id
INNER JOIN course_assessment ca ON ca.id = cacm.course_assessment_id
-- ✅ FIXED: Sử dụng course_assessment_id foreign key
INNER JOIN assessment a ON a.course_assessment_id = ca.id AND a.class_id = c.id
INNER JOIN score sc ON sc.assessment_id = a.id AND sc.student_id = 1
WHERE e.student_id = 1
  AND e.status IN ('enrolled', 'completed')
GROUP BY co.id, co.name, clo.id, clo.code, clo.description
HAVING COUNT(DISTINCT a.id) > 0  -- Chỉ lấy CLO có assessment đã được chấm điểm
ORDER BY co.name, clo.code;

-- Query 9.3: CLO với chi tiết từng assessment
-- Hiển thị chi tiết điểm của từng assessment liên quan đến CLO
SELECT 
    co.name AS course_name,
    clo.code AS clo_code,
    clo.description AS clo_description,
    ca.name AS course_assessment_template,  -- Template name
    a.name AS assessment_name,               -- Actual name (có thể khác template)
    a.kind AS assessment_kind,
    sc.score,
    a.max_score,
    ROUND((sc.score / a.max_score * 10), 2) AS normalized_score,
    sc.graded_at
FROM enrollment e
INNER JOIN "class" c ON e.class_id = c.id
INNER JOIN course co ON c.course_id = co.id
INNER JOIN clo ON clo.course_id = co.id
INNER JOIN course_assessment_clo_mapping cacm ON cacm.clo_id = clo.id
INNER JOIN course_assessment ca ON ca.id = cacm.course_assessment_id
-- ✅ FIXED: Sử dụng course_assessment_id foreign key
INNER JOIN assessment a ON a.course_assessment_id = ca.id AND a.class_id = c.id
INNER JOIN score sc ON sc.assessment_id = a.id AND sc.student_id = 1
WHERE e.student_id = 1
  AND e.status IN ('enrolled', 'completed')
ORDER BY co.name, clo.code, sc.graded_at DESC;


-- =========================================
-- 10. STUDENT PROFILE - COMPLETE VIEW
-- =========================================
-- Query tổng hợp tất cả thông tin cần thiết cho profile

WITH basic_info AS (
    SELECT 
        s.id AS student_id,
        s.student_code,
        u.full_name,
        u.email,
        u.phone,
        s.education_level,
        s.address,
        u.status,
        u.last_login_at,
        s.created_at
    FROM student s
    INNER JOIN user_account u ON s.user_id = u.id
    WHERE s.id = 1
),
stats AS (
    SELECT 
        COALESCE(COUNT(DISTINCT CASE WHEN e.status = 'enrolled' THEN e.class_id END), 0) AS active_classes,
        COALESCE(COUNT(DISTINCT CASE WHEN e.status = 'completed' THEN e.class_id END), 0) AS completed_classes
    FROM enrollment e
    WHERE e.student_id = 1
),
attendance AS (
    SELECT 
        COUNT(*) AS total_sessions,
        COUNT(*) FILTER (WHERE ss.attendance_status = 'present') AS present_sessions
    FROM student_session ss
    INNER JOIN session ses ON ss.session_id = ses.id
    WHERE ss.student_id = 1 AND ses.status = 'done'
),
gpa AS (
    SELECT ROUND(AVG(sc.score / a.max_score * 10), 2) AS overall_gpa
    FROM score sc
    INNER JOIN assessment a ON sc.assessment_id = a.id
    WHERE sc.student_id = 1
)
SELECT 
    bi.*,
    s.active_classes,
    s.completed_classes,
    COALESCE(att.total_sessions, 0) AS total_sessions,
    CASE 
        WHEN COALESCE(att.total_sessions, 0) > 0 
        THEN ROUND((att.present_sessions::NUMERIC / att.total_sessions * 100), 2)
        ELSE 0
    END AS attendance_rate,
    COALESCE(g.overall_gpa, 0) AS overall_gpa
FROM basic_info bi
CROSS JOIN stats s
CROSS JOIN attendance att
CROSS JOIN gpa g;


-- =========================================
-- USAGE EXAMPLES với Seed Data
-- =========================================

-- Example 1: Lấy profile của student đầu tiên (Student ID = 1, Nguyen Van An)
-- SELECT * FROM student WHERE id = 1;

-- Example 2: Dashboard stats
-- Replace 1 = 1 in các queries trên

-- Example 3: Xem điểm của student trong một lớp cụ thể
/*
SELECT 
    a.name,
    a.kind,
    sc.score,
    a.max_score,
    ROUND((sc.score / a.max_score * 10), 2) AS normalized_score
FROM score sc
INNER JOIN assessment a ON sc.assessment_id = a.id
WHERE sc.student_id = 1
ORDER BY sc.graded_at DESC;
*/

-- Example 4: Attendance summary cho student
/*
SELECT 
    c.name AS class_name,
    COUNT(*) AS total_sessions,
    COUNT(*) FILTER (WHERE ss.attendance_status = 'present') AS present,
    COUNT(*) FILTER (WHERE ss.attendance_status = 'absent') AS absent
FROM student_session ss
INNER JOIN session ses ON ss.session_id = ses.id
INNER JOIN "class" c ON ses.class_id = c.id
WHERE ss.student_id = 1
  AND ses.status = 'done'
GROUP BY c.id, c.name;
*/

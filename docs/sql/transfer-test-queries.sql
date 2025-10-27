-- =========================================
-- TRANSFER CLASS TEST QUERIES
-- =========================================
-- Test queries để verify các trường hợp chuyển lớp khác nhau
-- Date: October 27, 2025
-- =========================================

-- =========================================
-- TEST CASE 1: CHUYỂN CÙNG COURSE - TÌM LỚP OFFLINE
-- =========================================
-- Student đang học Class 3 (B1-IELTS-001 - Offline) muốn chuyển sang lớp OFFLINE khác cùng course
-- Expected: Trả về Class 17 (B1-IELTS-003 - Evening Offline, scheduled)

WITH current_class_info AS (
    SELECT course_id, id AS current_class_id, branch_id AS current_branch_id
    FROM class
    WHERE id = 3  -- Class 3: B1-IELTS-001 (IELTS Foundation B1, offline, ongoing)
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
WHERE c.course_id = cci.course_id -- Same course (course_id = 2)
  AND c.id != cci.current_class_id -- Exclude current (id != 3)
  AND c.status IN ('scheduled', 'ongoing')
  AND c.modality = 'offline' -- Filter: OFFLINE only
  AND COALESCE(ec.enrolled_count, 0) < c.max_capacity -- Has capacity
GROUP BY c.id, b.id, cci.current_branch_id, ec.enrolled_count
ORDER BY priority_score ASC, c.start_date ASC;

-- Expected Result:
-- class_id: 17 | code: B1-IELTS-003 | modality: offline | status: scheduled | available_slots: 18


-- =========================================
-- TEST CASE 2: CHUYỂN CÙNG COURSE - TÌM LỚP ONLINE
-- =========================================
-- Student đang học Class 3 (B1-IELTS-001 - Offline) muốn chuyển sang lớp ONLINE
-- Expected: Trả về Class 16 (B1-IELTS-002 - Morning Online, ongoing)

WITH current_class_info AS (
    SELECT course_id, id AS current_class_id, branch_id AS current_branch_id
    FROM class
    WHERE id = 3  -- Class 3: B1-IELTS-001 (offline)
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
WHERE c.course_id = cci.course_id -- Same course (course_id = 2)
  AND c.id != cci.current_class_id
  AND c.status IN ('scheduled', 'ongoing')
  AND c.modality = 'online' -- Filter: ONLINE only
  AND COALESCE(ec.enrolled_count, 0) < c.max_capacity
GROUP BY c.id, b.id, cci.current_branch_id, ec.enrolled_count
ORDER BY priority_score ASC, c.start_date ASC;

-- Expected Result:
-- class_id: 16 | code: B1-IELTS-002 | modality: online | status: ongoing | available_slots: 8


-- =========================================
-- TEST CASE 3: CHUYỂN CÙNG COURSE - TẤT CẢ LỚP (KHÔNG FILTER MODALITY)
-- =========================================
-- Student đang học Class 3 muốn xem tất cả các lớp khả dụng cùng course
-- Expected: Trả về Class 16 (online), 17 (offline), 18 (hybrid)

WITH current_class_info AS (
    SELECT course_id, id AS current_class_id, branch_id AS current_branch_id
    FROM class
    WHERE id = 3  -- Class 3: B1-IELTS-001
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
  AND c.id != cci.current_class_id
  AND c.status IN ('scheduled', 'ongoing')
  AND COALESCE(ec.enrolled_count, 0) < c.max_capacity
GROUP BY c.id, b.id, cci.current_branch_id, ec.enrolled_count
ORDER BY priority_score ASC, c.start_date ASC;

-- Expected Result: 3 classes
-- class_id: 16 (online, ongoing), 17 (offline, scheduled), 18 (hybrid, scheduled)


-- =========================================
-- TEST CASE 4: CHUYỂN KHÁC COURSE - TÌM LỚP ONLINE (PROGRESSION)
-- =========================================
-- Student đang học Class 3 (B1-IELTS) muốn chuyển sang B2-IELTS (course_id = 3) - lớp ONLINE
-- Expected: Trả về Class 19 (B2-IELTS-002 - Morning Online, ongoing)

WITH current_class_info AS (
    SELECT course_id, id AS current_class_id, branch_id AS current_branch_id
    FROM class
    WHERE id = 3  -- Class 3: B1-IELTS-001 (course_id = 2)
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
WHERE c.course_id = 3 -- Different course: B2-IELTS (course_id = 3)
  AND c.id != cci.current_class_id
  AND c.status IN ('scheduled', 'ongoing')
  AND c.modality = 'online' -- Filter: ONLINE only
  AND COALESCE(ec.enrolled_count, 0) < c.max_capacity
GROUP BY c.id, b.id, cci.current_branch_id, ec.enrolled_count
ORDER BY priority_score ASC, c.start_date ASC;

-- Expected Result:
-- class_id: 19 | code: B2-IELTS-002 | modality: online | status: ongoing | available_slots: 10


-- =========================================
-- TEST CASE 5: CHUYỂN KHÁC COURSE - TÌM LỚP OFFLINE
-- =========================================
-- Student đang học Class 3 (B1-IELTS) muốn chuyển sang B2-IELTS - lớp OFFLINE
-- Expected: Trả về Class 20 (B2-IELTS-003 - Afternoon Offline, scheduled)

WITH current_class_info AS (
    SELECT course_id, id AS current_class_id, branch_id AS current_branch_id
    FROM class
    WHERE id = 3
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
WHERE c.course_id = 3 -- Different course: B2-IELTS
  AND c.id != cci.current_class_id
  AND c.status IN ('scheduled', 'ongoing')
  AND c.modality = 'offline' -- Filter: OFFLINE only
  AND COALESCE(ec.enrolled_count, 0) < c.max_capacity
GROUP BY c.id, b.id, cci.current_branch_id, ec.enrolled_count
ORDER BY priority_score ASC, c.start_date ASC;

-- Expected Result:
-- class_id: 20 | code: B2-IELTS-003 | modality: offline | status: scheduled | available_slots: 18


-- =========================================
-- TEST CASE 6: CHUYỂN KHÁC COURSE - TẤT CẢ LỚP
-- =========================================
-- Student đang học Class 3 (B1-IELTS) muốn chuyển sang B2-IELTS - xem tất cả
-- Expected: Trả về Class 4, 19, 20

WITH current_class_info AS (
    SELECT course_id, id AS current_class_id, branch_id AS current_branch_id
    FROM class
    WHERE id = 3
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
WHERE c.course_id = 3 -- Different course: B2-IELTS
  AND c.id != cci.current_class_id
  AND c.status IN ('scheduled', 'ongoing')
  AND COALESCE(ec.enrolled_count, 0) < c.max_capacity
GROUP BY c.id, b.id, cci.current_branch_id, ec.enrolled_count
ORDER BY priority_score ASC, c.start_date ASC;

-- Expected Result: 3 classes
-- class_id: 4 (hybrid, ongoing), 19 (online, ongoing), 20 (offline, scheduled)


-- =========================================
-- TEST CASE 7: TỪNG STUDENT CỤ THỂ - CHUYỂN CÙNG COURSE
-- =========================================
-- Student 13 đang học Class 3 (B1-IELTS-001) muốn chuyển sang lớp online khác
-- Verify student_id = 13 đang enrolled trong class_id = 3

-- Check current enrollment
SELECT 
    e.id AS enrollment_id,
    e.class_id,
    c.code AS class_code,
    c.name AS class_name,
    c.course_id,
    e.student_id,
    s.student_code,
    ua.full_name AS student_name,
    e.status,
    c.modality
FROM enrollment e
JOIN class c ON e.class_id = c.id
JOIN student s ON e.student_id = s.id
JOIN user_account ua ON s.user_id = ua.id
WHERE e.student_id = 13 AND e.status = 'enrolled';

-- Find target classes for transfer (online only)
WITH current_class_info AS (
    SELECT c.course_id, c.id AS current_class_id, c.branch_id AS current_branch_id
    FROM enrollment e
    JOIN class c ON e.class_id = c.id
    WHERE e.student_id = 13 AND e.status = 'enrolled'
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
    COALESCE(ec.enrolled_count, 0) AS enrolled_count,
    c.max_capacity - COALESCE(ec.enrolled_count, 0) AS available_slots
FROM class c
JOIN branch b ON c.branch_id = b.id
CROSS JOIN current_class_info cci
LEFT JOIN enrollment_counts ec ON c.id = ec.class_id
WHERE c.course_id = cci.course_id
  AND c.id != cci.current_class_id
  AND c.status IN ('scheduled', 'ongoing')
  AND c.modality = 'online'
  AND COALESCE(ec.enrolled_count, 0) < c.max_capacity
ORDER BY c.start_date ASC;

-- Expected: Student 13 trong Class 3, có thể transfer sang Class 16 (online)


-- =========================================
-- TEST CASE 8: GENERAL ENGLISH A1 - ONLINE VS OFFLINE
-- =========================================
-- Student đang học Class 13 (A1-GEN-003 - Online) muốn chuyển sang offline

WITH current_class_info AS (
    SELECT course_id, id AS current_class_id, branch_id AS current_branch_id
    FROM class
    WHERE id = 13  -- Class 13: A1-GEN-003 (online, ongoing)
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
    COALESCE(ec.enrolled_count, 0) AS enrolled_count,
    c.max_capacity - COALESCE(ec.enrolled_count, 0) AS available_slots
FROM class c
CROSS JOIN current_class_info cci
LEFT JOIN enrollment_counts ec ON c.id = ec.class_id
WHERE c.course_id = cci.course_id -- Same course (course_id = 1)
  AND c.id != cci.current_class_id
  AND c.status IN ('scheduled', 'ongoing')
  AND c.modality = 'offline' -- Filter OFFLINE
  AND COALESCE(ec.enrolled_count, 0) < c.max_capacity
ORDER BY c.start_date ASC;

-- Expected Result:
-- class_id: 14 | code: A1-GEN-004 | modality: offline | status: scheduled


-- =========================================
-- SUMMARY TABLE - ALL CLASSES FOR REFERENCE
-- =========================================
SELECT 
    c.id,
    c.code,
    c.name,
    co.name AS course_name,
    c.modality,
    c.status,
    c.start_date,
    c.max_capacity,
    COALESCE(COUNT(DISTINCT e.student_id), 0) AS enrolled_count,
    c.max_capacity - COALESCE(COUNT(DISTINCT e.student_id), 0) AS available_slots
FROM class c
JOIN course co ON c.course_id = co.id
LEFT JOIN enrollment e ON c.id = e.class_id AND e.status = 'enrolled'
WHERE c.status IN ('scheduled', 'ongoing')
GROUP BY c.id, co.name
ORDER BY co.id, c.modality, c.start_date;

-- =========================================
-- NOTES
-- =========================================
-- Modality values: 'offline', 'online', 'hybrid'
-- Status for transfer: 'scheduled' or 'ongoing'
-- Available slots: max_capacity - enrolled_count > 0
-- 
-- Key Classes:
-- Course 1 (General English A1):
--   - Class 13: online, ongoing (15/20 students)
--   - Class 14: offline, scheduled (0/15 students)
--   - Class 15: online, scheduled (0/25 students)
--
-- Course 2 (IELTS Foundation B1):
--   - Class 3: offline, ongoing (16/18 students)
--   - Class 16: online, ongoing (12/20 students)
--   - Class 17: offline, scheduled (0/18 students)
--   - Class 18: hybrid, scheduled (0/16 students)
--
-- Course 3 (IELTS Intermediate B2):
--   - Class 4: hybrid, ongoing (14/20 students)
--   - Class 19: online, ongoing (10/20 students)
--   - Class 20: offline, scheduled (0/18 students)
--
-- Course 5 (Conversational A2):
--   - Class 5: offline, ongoing (8/10 students)
--   - Class 21: online, ongoing (8/15 students)
--   - Class 22: offline, scheduled (0/12 students)

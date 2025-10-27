-- =========================================
-- EMS-SEP490-BE: Session Queries
-- PostgreSQL 16
-- =========================================
--
-- PURPOSE:
-- This file contains comprehensive queries for session-related operations,
-- particularly for student and teacher views of session details.
--
-- BUSINESS CONTEXT:
-- Session is the SOURCE OF TRUTH for all schedule-related operations.
-- When a student views "My Schedule" and clicks on a session, they need to see:
-- - Session basic info (date, time, topic, status)
-- - Class & course context
-- - Teachers assigned
-- - Location (room or zoom)
-- - Their personal attendance & homework status
-- - Course materials
-- - Classmates list (with attendance if session is done)
-- - Available request options (absence, makeup, reschedule)
--
-- MAIN QUERIES:
-- 1. Student Session Detail View (comprehensive)
-- 2. Student Weekly Schedule
-- 3. Teacher Session Detail View
-- 4. Session Attendance Summary
-- 5. Session Materials
-- 6. Available Makeup Sessions
-- 7. Session Conflict Check
--
-- =========================================

-- =========================================
-- QUERY 1: STUDENT SESSION DETAIL VIEW
-- =========================================
-- USE CASE: Student clicks on a session in "My Schedule"
-- RETURNS: Complete session information including personal attendance,
--          teachers, location, materials, and classmates
-- EXAMPLE: Student ID 1 viewing Session ID 1

-- Step 1: Get Session Core Info with Class & Course Context
WITH session_info AS (
    SELECT
        s.id AS session_id,
        s.date AS session_date,
        s.type AS session_type,
        s.status AS session_status,
        s.teacher_note,
        
        -- Course Session Template Info
        cs.sequence_no,
        cs.topic,
        cs.student_task,
        cs.skill_set,
        
        -- Phase Info
        cp.phase_number,
        cp.name AS phase_name,
        cp.learning_focus,
        
        -- Course Info
        c.code AS course_code,
        c.name AS course_name,
        c.description AS course_description,
        
        -- Class Info
        cl.id AS class_id,
        cl.code AS class_code,
        cl.name AS class_name,
        cl.modality AS class_modality,
        cl.status AS class_status,
        
        -- Branch Info
        b.name AS branch_name,
        b.address AS branch_address,
        
        -- Time Slot Info
        tst.name AS time_slot_name,
        tst.start_time,
        tst.end_time,
        tst.duration_min
    FROM
        session s
        INNER JOIN "class" cl ON s.class_id = cl.id
        INNER JOIN branch b ON cl.branch_id = b.id
        INNER JOIN course c ON cl.course_id = c.id
        LEFT JOIN course_session cs ON s.course_session_id = cs.id
        LEFT JOIN course_phase cp ON cs.phase_id = cp.id
        LEFT JOIN time_slot_template tst ON s.time_slot_template_id = tst.id
    WHERE
        s.id = 1 -- Session ID from seed data
),

-- Step 2: Get Student's Personal Attendance & Homework Status
student_attendance AS (
    SELECT
        ss.session_id,
        ss.student_id,
        ss.is_makeup,
        ss.attendance_status,
        ss.homework_status,
        ss.note AS student_note,
        ss.recorded_at,
        
        -- Enrollment Info
        e.status AS enrollment_status,
        e.enrolled_at,
        
        -- Student Info
        st.student_code,
        ua.full_name AS student_name,
        ua.email AS student_email,
        ua.phone AS student_phone
    FROM
        student_session ss
        INNER JOIN student st ON ss.student_id = st.id
        INNER JOIN user_account ua ON st.user_id = ua.id
        LEFT JOIN enrollment e ON e.student_id = ss.student_id 
            AND e.class_id = (SELECT class_id FROM session WHERE id = 1)
    WHERE
        ss.session_id = 1
        AND ss.student_id = 1 -- Student ID from seed data
),

-- Step 3: Get Teachers Assigned to This Session
session_teachers AS (
    SELECT
        ts.session_id,
        t.id AS teacher_id,
        t.employee_code,
        ua.full_name AS teacher_name,
        ua.email AS teacher_email,
        ua.phone AS teacher_phone,
        ts.skill,
        ts.role AS teaching_role,
        ts.status AS teaching_slot_status
    FROM
        teaching_slot ts
        INNER JOIN teacher t ON ts.teacher_id = t.id
        INNER JOIN user_account ua ON t.user_account_id = ua.id
    WHERE
        ts.session_id = 1
    ORDER BY
        CASE ts.role
            WHEN 'primary' THEN 1
            WHEN 'assistant' THEN 2
            ELSE 3
        END
),

-- Step 4: Get Location (Room or Zoom)
session_location AS (
    SELECT
        sr.session_id,
        sr.resource_type,
        r.name AS resource_name,
        r.location,
        r.capacity,
        r.equipment,
        r.meeting_url,
        r.meeting_id,
        sr.capacity_override
    FROM
        session_resource sr
        INNER JOIN resource r ON sr.resource_id = r.id
    WHERE
        sr.session_id = 1
),

-- Step 5: Get Course Materials (Session, Phase, and Course level)
session_materials AS (
    SELECT
        cm.id AS material_id,
        cm.title,
        cm.url,
        cm.created_at,
        ua.full_name AS uploaded_by_name,
        CASE
            WHEN cm.course_session_id IS NOT NULL THEN 'Session'
            WHEN cm.phase_id IS NOT NULL THEN 'Phase'
            ELSE 'Course'
        END AS material_scope,
        CASE
            WHEN cm.course_session_id IS NOT NULL THEN 1
            WHEN cm.phase_id IS NOT NULL THEN 2
            ELSE 3
        END AS sort_priority
    FROM
        course_material cm
        LEFT JOIN user_account ua ON cm.uploaded_by = ua.id
    WHERE
        cm.course_id = (SELECT c.id FROM session_info si, course c WHERE si.course_code = c.code)
        AND (
            cm.course_session_id = (SELECT course_session_id FROM session WHERE id = 1)
            OR cm.phase_id = (SELECT cp.id FROM session s 
                              INNER JOIN course_session cs ON s.course_session_id = cs.id
                              INNER JOIN course_phase cp ON cs.phase_id = cp.id
                              WHERE s.id = 1)
            OR (cm.course_session_id IS NULL AND cm.phase_id IS NULL)
        )
    ORDER BY sort_priority, cm.created_at DESC
),

-- Step 6: Get Classmates (all students in this session)
classmates AS (
        SELECT
            ss.student_id,
            st.student_code,
            ua.full_name AS student_name,
            ss.attendance_status,
            ss.is_makeup,
            CASE
                WHEN ss.student_id = 1 THEN TRUE
                ELSE FALSE
            END AS is_current_user
        FROM
            student_session ss
            INNER JOIN student st ON ss.student_id = st.id
            INNER JOIN user_account ua ON st.user_id = ua.id
        WHERE
            ss.session_id = 1
        ORDER BY
            is_current_user DESC,
            ua.full_name
),

-- Step 7: Check Available Request Options
request_options AS (
    SELECT
        -- Can request absence? (session must be in future and student must be 'planned')
        CASE
            WHEN s.date > CURRENT_DATE 
                 AND ss.attendance_status = 'planned'
                 AND s.status = 'planned'
            THEN TRUE
            ELSE FALSE
        END AS can_request_absence,
        
        -- Can request makeup? (student must have 'absent' or 'excused' and no pending makeup request)
        CASE
            WHEN ss.attendance_status IN ('absent', 'excused')
                 AND NOT EXISTS (
                     SELECT 1 FROM student_request sr
                     WHERE sr.student_id = 1
                       AND sr.missed_session_id = 1
                       AND sr.request_type = 'makeup'
                       AND sr.status IN ('pending', 'waiting_confirm', 'approved')
                 )
            THEN TRUE
            ELSE FALSE
        END AS can_request_makeup,
        
        -- Can request reschedule? (session in future, student is enrolled)
        CASE
            WHEN s.date > CURRENT_DATE 
                 AND s.status = 'planned'
                 AND EXISTS (
                     SELECT 1 FROM enrollment e
                     WHERE e.student_id = 1
                       AND e.class_id = s.class_id
                       AND e.status = 'enrolled'
                 )
            THEN TRUE
            ELSE FALSE
        END AS can_request_reschedule,
        
        -- Has pending requests?
        EXISTS (
            SELECT 1 FROM student_request sr
            WHERE sr.student_id = 1
              AND (sr.missed_session_id = 1 OR sr.makeup_session_id = 1)
              AND sr.status IN ('pending', 'waiting_confirm')
        ) AS has_pending_request
    FROM
        session s
        LEFT JOIN student_session ss ON s.id = ss.session_id AND ss.student_id = 1
    WHERE
        s.id = 1
)

-- Final SELECT: Combine All Information
SELECT
    -- Session Info
    jsonb_build_object(
        'session_id', si.session_id,
        'date', si.session_date,
        'type', si.session_type,
        'status', si.session_status,
        'teacher_note', si.teacher_note,
        'time_slot', jsonb_build_object(
            'name', si.time_slot_name,
            'start_time', si.start_time,
            'end_time', si.end_time,
            'duration_min', si.duration_min
        ),
        'topic', si.topic,
        'student_task', si.student_task,
        'skill_set', si.skill_set,
        'sequence_no', si.sequence_no
    ) AS session_info,
    
    -- Class & Course Context
    jsonb_build_object(
        'class', jsonb_build_object(
            'id', si.class_id,
            'code', si.class_code,
            'name', si.class_name,
            'modality', si.class_modality,
            'status', si.class_status
        ),
        'course', jsonb_build_object(
            'code', si.course_code,
            'name', si.course_name,
            'description', si.course_description
        ),
        'phase', jsonb_build_object(
            'phase_number', si.phase_number,
            'name', si.phase_name,
            'learning_focus', si.learning_focus
        ),
        'branch', jsonb_build_object(
            'name', si.branch_name,
            'address', si.branch_address
        )
    ) AS class_course_info,
    
    -- Student's Attendance
    jsonb_build_object(
        'student_id', sa.student_id,
        'student_code', sa.student_code,
        'student_name', sa.student_name,
        'is_makeup', sa.is_makeup,
        'attendance_status', sa.attendance_status,
        'homework_status', sa.homework_status,
        'student_note', sa.student_note,
        'recorded_at', sa.recorded_at,
        'enrollment_status', sa.enrollment_status,
        'enrolled_at', sa.enrolled_at
    ) AS my_attendance,
    
    -- Teachers
    (
        SELECT jsonb_agg(
            jsonb_build_object(
                'teacher_id', st.teacher_id,
                'employee_code', st.employee_code,
                'name', st.teacher_name,
                'email', st.teacher_email,
                'phone', st.teacher_phone,
                'skill', st.skill,
                'role', st.teaching_role,
                'status', st.teaching_slot_status
            )
            ORDER BY CASE st.teaching_role WHEN 'primary' THEN 1 ELSE 2 END
        )
        FROM session_teachers st
    ) AS teachers,
    
    -- Location
    (
        SELECT jsonb_agg(
            jsonb_build_object(
                'resource_type', sl.resource_type,
                'name', sl.resource_name,
                'location', sl.location,
                'capacity', COALESCE(sl.capacity_override, sl.capacity),
                'equipment', sl.equipment,
                'meeting_url', sl.meeting_url,
                'meeting_id', sl.meeting_id
            )
        )
        FROM session_location sl
    ) AS location,
    
    -- Course Materials
    (
        SELECT jsonb_agg(
            jsonb_build_object(
                'material_id', sm.material_id,
                'title', sm.title,
                'url', sm.url,
                'scope', sm.material_scope,
                'uploaded_by', sm.uploaded_by_name,
                'created_at', sm.created_at
            )
            ORDER BY sm.sort_priority, sm.created_at DESC
        )
        FROM session_materials sm
    ) AS materials,
    
    -- Classmates
    (
        SELECT jsonb_agg(
            jsonb_build_object(
                'student_id', cm.student_id,
                'student_code', cm.student_code,
                'name', cm.student_name,
                'attendance_status', cm.attendance_status,
                'is_makeup', cm.is_makeup,
                'is_me', cm.is_current_user
            )
            ORDER BY cm.is_current_user DESC, cm.student_name
        )
        FROM classmates cm
    ) AS classmates,
    
    -- Request Options
    jsonb_build_object(
        'can_request_absence', ro.can_request_absence,
        'can_request_makeup', ro.can_request_makeup,
        'can_request_reschedule', ro.can_request_reschedule,
        'has_pending_request', ro.has_pending_request
    ) AS request_options
    
FROM
    session_info si
    CROSS JOIN student_attendance sa
    CROSS JOIN request_options ro;


-- =========================================
-- QUERY 2: STUDENT WEEKLY SCHEDULE
-- =========================================
-- USE CASE: Student views their schedule for a specific week
-- RETURNS: All sessions in the week with basic info and attendance status
-- EXAMPLE: Student ID 1, Week starting CURRENT_DATE

SELECT
    s.id AS session_id,
    s.date AS session_date,
    s.type AS session_type,
    s.status AS session_status,
    
    -- Time
    tst.name AS time_slot_name,
    tst.start_time,
    tst.end_time,
    
    -- Class & Course
    cl.code AS class_code,
    cl.name AS class_name,
    c.name AS course_name,
    
    -- Topic
    cs.topic,
    cs.sequence_no,
    
    -- Student's attendance
    ss.attendance_status,
    ss.homework_status,
    ss.is_makeup,
    
    -- Location
    (
        SELECT r.name
        FROM session_resource sr
        INNER JOIN resource r ON sr.resource_id = r.id
        WHERE sr.session_id = s.id
        LIMIT 1
    ) AS location_name,
    
    -- Primary teacher
    (
        SELECT ua.full_name
        FROM teaching_slot ts
        INNER JOIN teacher t ON ts.teacher_id = t.id
        INNER JOIN user_account ua ON t.user_account_id = ua.id
        WHERE ts.session_id = s.id AND ts.role = 'primary'
        LIMIT 1
    ) AS primary_teacher_name
    
FROM
    session s
    INNER JOIN student_session ss ON s.id = ss.session_id
    INNER JOIN "class" cl ON s.class_id = cl.id
    INNER JOIN course c ON cl.course_id = c.id
    LEFT JOIN course_session cs ON s.course_session_id = cs.id
    LEFT JOIN time_slot_template tst ON s.time_slot_template_id = tst.id
WHERE
    ss.student_id = 1 -- Student ID from seed data
    AND s.date >= CURRENT_DATE
    AND s.date < CURRENT_DATE + INTERVAL '7 days'
ORDER BY
    s.date,
    tst.start_time;


-- =========================================
-- QUERY 3: TEACHER SESSION DETAIL VIEW
-- =========================================
-- USE CASE: Teacher views a session they are assigned to
-- RETURNS: Session info with all students' attendance, homework status
-- EXAMPLE: Teacher ID 1, Session ID 1

WITH session_info AS (
    SELECT
        s.id AS session_id,
        s.date AS session_date,
        s.type AS session_type,
        s.status AS session_status,
        s.teacher_note,
        
        -- Course Session
        cs.sequence_no,
        cs.topic,
        cs.student_task,
        cs.skill_set,
        
        -- Class
        cl.id AS class_id,
        cl.code AS class_code,
        cl.name AS class_name,
        
        -- Course
        c.name AS course_name,
        
        -- Time
        tst.name AS time_slot_name,
        tst.start_time,
        tst.end_time
    FROM
        session s
        INNER JOIN "class" cl ON s.class_id = cl.id
        INNER JOIN course c ON cl.course_id = c.id
        LEFT JOIN course_session cs ON s.course_session_id = cs.id
        LEFT JOIN time_slot_template tst ON s.time_slot_template_id = tst.id
    WHERE
        s.id = 1 -- Session ID from seed data
),

-- Get all students in this session
students_in_session AS (
    SELECT
        ss.session_id,
        ss.student_id,
        st.student_code,
        ua.full_name AS student_name,
        ua.email AS student_email,
        ss.attendance_status,
        ss.homework_status,
        ss.is_makeup,
        ss.note AS student_note,
        ss.recorded_at
    FROM
        student_session ss
        INNER JOIN student st ON ss.student_id = st.id
        INNER JOIN user_account ua ON st.user_id = ua.id
    WHERE
        ss.session_id = 1
    ORDER BY
        ua.full_name
),

-- Get teaching role info
teacher_role AS (
    SELECT
        ts.skill,
        ts.role AS teaching_role,
        ts.status AS teaching_slot_status
    FROM
        teaching_slot ts
    WHERE
        ts.session_id = 1
        AND ts.teacher_id = 1 -- Teacher ID from seed data
),

-- Get location
location_info AS (
    SELECT
        sr.resource_type,
        r.name AS resource_name,
        r.location,
        r.meeting_url,
        r.meeting_id
    FROM
        session_resource sr
        INNER JOIN resource r ON sr.resource_id = r.id
    WHERE
        sr.session_id = 1
    LIMIT 1
)

-- Combine results
SELECT
    -- Session Info
    jsonb_build_object(
        'session_id', si.session_id,
        'date', si.session_date,
        'type', si.session_type,
        'status', si.session_status,
        'teacher_note', si.teacher_note,
        'time_slot', jsonb_build_object(
            'name', si.time_slot_name,
            'start_time', si.start_time,
            'end_time', si.end_time
        ),
        'topic', si.topic,
        'student_task', si.student_task,
        'skill_set', si.skill_set,
        'sequence_no', si.sequence_no,
        'class', jsonb_build_object(
            'id', si.class_id,
            'code', si.class_code,
            'name', si.class_name
        ),
        'course_name', si.course_name
    ) AS session_info,
    
    -- Teacher's role
    jsonb_build_object(
        'skill', tr.skill,
        'role', tr.teaching_role,
        'status', tr.teaching_slot_status
    ) AS my_teaching_role,
    
    -- Location
    jsonb_build_object(
        'resource_type', li.resource_type,
        'name', li.resource_name,
        'location', li.location,
        'meeting_url', li.meeting_url,
        'meeting_id', li.meeting_id
    ) AS location,
    
    -- Students with attendance
    (
        SELECT jsonb_agg(
            jsonb_build_object(
                'student_id', sis.student_id,
                'student_code', sis.student_code,
                'name', sis.student_name,
                'email', sis.student_email,
                'attendance_status', sis.attendance_status,
                'homework_status', sis.homework_status,
                'is_makeup', sis.is_makeup,
                'note', sis.student_note,
                'recorded_at', sis.recorded_at
            )
            ORDER BY sis.student_name
        )
        FROM students_in_session sis
    ) AS students,
    
    -- Attendance statistics
    jsonb_build_object(
        'total_students', (SELECT COUNT(*) FROM students_in_session),
        'present', (SELECT COUNT(*) FROM students_in_session WHERE attendance_status = 'present'),
        'absent', (SELECT COUNT(*) FROM students_in_session WHERE attendance_status = 'absent'),
        'late', (SELECT COUNT(*) FROM students_in_session WHERE attendance_status = 'late'),
        'excused', (SELECT COUNT(*) FROM students_in_session WHERE attendance_status = 'excused'),
        'remote', (SELECT COUNT(*) FROM students_in_session WHERE attendance_status = 'remote'),
        'planned', (SELECT COUNT(*) FROM students_in_session WHERE attendance_status = 'planned')
    ) AS attendance_summary
    
FROM
    session_info si
    CROSS JOIN teacher_role tr
    CROSS JOIN location_info li;


-- =========================================
-- QUERY 4: SESSION ATTENDANCE SUMMARY
-- =========================================
-- USE CASE: Get attendance statistics for a session (for academic staff/manager)
-- EXAMPLE: Session ID 1

SELECT
    s.id AS session_id,
    s.date AS session_date,
    s.status AS session_status,
    cl.code AS class_code,
    cl.name AS class_name,
    
    -- Attendance counts
    COUNT(*) AS total_enrolled,
    COUNT(*) FILTER (WHERE ss.attendance_status = 'present') AS count_present,
    COUNT(*) FILTER (WHERE ss.attendance_status = 'absent') AS count_absent,
    COUNT(*) FILTER (WHERE ss.attendance_status = 'late') AS count_late,
    COUNT(*) FILTER (WHERE ss.attendance_status = 'excused') AS count_excused,
    COUNT(*) FILTER (WHERE ss.attendance_status = 'remote') AS count_remote,
    COUNT(*) FILTER (WHERE ss.attendance_status = 'planned') AS count_planned,
    
    -- Percentages
    ROUND(100.0 * COUNT(*) FILTER (WHERE ss.attendance_status = 'present') / NULLIF(COUNT(*), 0), 2) AS percent_present,
    ROUND(100.0 * COUNT(*) FILTER (WHERE ss.attendance_status = 'absent') / NULLIF(COUNT(*), 0), 2) AS percent_absent,
    
    -- Homework stats
    COUNT(*) FILTER (WHERE ss.homework_status = 'completed') AS homework_completed,
    COUNT(*) FILTER (WHERE ss.homework_status = 'incomplete') AS homework_incomplete,
    
    -- Makeup students
    COUNT(*) FILTER (WHERE ss.is_makeup = TRUE) AS count_makeup_students
    
FROM
    session s
    INNER JOIN "class" cl ON s.class_id = cl.id
    LEFT JOIN student_session ss ON s.id = ss.session_id
WHERE
    s.id = 1 -- Session ID from seed data
GROUP BY
    s.id, s.date, s.status, cl.code, cl.name;


-- =========================================
-- QUERY 5: GET SESSION MATERIALS
-- =========================================
-- USE CASE: Get all materials for a session (session-level, phase-level, course-level)
-- EXAMPLE: Session ID 1

SELECT
    cm.id AS material_id,
    cm.title,
    cm.url,
    cm.created_at,
    ua.full_name AS uploaded_by,
    CASE
        WHEN cm.course_session_id IS NOT NULL THEN 'Session-specific'
        WHEN cm.phase_id IS NOT NULL THEN 'Phase-level'
        ELSE 'Course-level'
    END AS scope,
    cs.sequence_no AS session_sequence,
    cp.phase_number,
    c.name AS course_name
FROM
    session s
    INNER JOIN "class" cl ON s.class_id = cl.id
    INNER JOIN course c ON cl.course_id = c.id
    LEFT JOIN course_session cs ON s.course_session_id = cs.id
    LEFT JOIN course_phase cp ON cs.phase_id = cp.id
    LEFT JOIN course_material cm ON (
        cm.course_id = c.id
        AND (
            cm.course_session_id = cs.id
            OR cm.phase_id = cp.id
            OR (cm.course_session_id IS NULL AND cm.phase_id IS NULL)
        )
    )
    LEFT JOIN user_account ua ON cm.uploaded_by = ua.id
WHERE
    s.id = 1 -- Session ID from seed data
ORDER BY
    CASE
        WHEN cm.course_session_id IS NOT NULL THEN 1
        WHEN cm.phase_id IS NOT NULL THEN 2
        ELSE 3
    END,
    cm.created_at DESC;


-- =========================================
-- QUERY 6: FIND AVAILABLE MAKEUP SESSIONS
-- =========================================
-- USE CASE: Student missed a session and wants to find makeup sessions
-- RETURNS: Available sessions for makeup (same course_session_id, different class, in future)
-- EXAMPLE: Student ID 1 missed Session ID 5

WITH missed_session_info AS (
    SELECT
        s.course_session_id,
        s.date AS missed_date,
        cl.course_id,
        cl.branch_id
    FROM
        session s
        INNER JOIN "class" cl ON s.class_id = cl.id
    WHERE
        s.id = 5 -- Missed session ID from seed data
)

SELECT
    s.id AS makeup_session_id,
    s.date AS makeup_date,
    s.status AS session_status,
    
    -- Time
    tst.name AS time_slot_name,
    tst.start_time,
    tst.end_time,
    
    -- Class info
    cl.code AS class_code,
    cl.name AS class_name,
    cl.modality,
    
    -- Capacity check
    cl.max_capacity,
    COUNT(ss.student_id) AS current_enrolled,
    cl.max_capacity - COUNT(ss.student_id) AS available_slots,
    
    -- Teacher
    (
        SELECT ua.full_name
        FROM teaching_slot ts
        INNER JOIN teacher t ON ts.teacher_id = t.id
        INNER JOIN user_account ua ON t.user_account_id = ua.id
        WHERE ts.session_id = s.id AND ts.role = 'primary'
        LIMIT 1
    ) AS teacher_name,
    
    -- Location
    (
        SELECT r.name
        FROM session_resource sr
        INNER JOIN resource r ON sr.resource_id = r.id
        WHERE sr.session_id = s.id
        LIMIT 1
    ) AS location
    
FROM
    missed_session_info msi
    INNER JOIN session s ON s.course_session_id = msi.course_session_id
    INNER JOIN "class" cl ON s.class_id = cl.id
    LEFT JOIN student_session ss ON s.id = ss.session_id
    LEFT JOIN time_slot_template tst ON s.time_slot_template_id = tst.id
WHERE
    -- Same course session (topic)
    s.course_session_id = msi.course_session_id
    -- Different class (or same class, future occurrence)
    AND (cl.id != (SELECT class_id FROM session WHERE id = 5) OR s.date > msi.missed_date)
    -- Future session only
    AND s.date > CURRENT_DATE
    -- Status must be planned
    AND s.status = 'planned'
    -- Same branch
    AND cl.branch_id = msi.branch_id
    -- Student not already enrolled in this session
    AND NOT EXISTS (
        SELECT 1 FROM student_session ss2
        WHERE ss2.session_id = s.id
          AND ss2.student_id = 1 -- Student ID from seed data
    )
GROUP BY
    s.id, s.date, s.status, cl.id, cl.code, cl.name, cl.modality, 
    cl.max_capacity, tst.name, tst.start_time, tst.end_time
HAVING
    COUNT(ss.student_id) < cl.max_capacity  -- Has available slots
ORDER BY
    s.date, tst.start_time
LIMIT 20;


-- =========================================
-- QUERY 7: SESSION CONFLICT CHECK
-- =========================================
-- USE CASE: Check if a student/teacher has conflicting sessions at the same time
-- EXAMPLE: Check conflicts for date and time_slot_template_id

-- For Student Conflict Check (Student ID 1, checking a specific date and time slot)
SELECT
    s.id AS conflicting_session_id,
    s.date,
    cl.code AS class_code,
    cl.name AS class_name,
    c.name AS course_name,
    tst.name AS time_slot_name,
    tst.start_time,
    tst.end_time
FROM
    session s
    INNER JOIN student_session ss ON s.id = ss.session_id
    INNER JOIN "class" cl ON s.class_id = cl.id
    INNER JOIN course c ON cl.course_id = c.id
    INNER JOIN time_slot_template tst ON s.time_slot_template_id = tst.id
WHERE
    ss.student_id = 1 -- Student ID from seed data
    AND s.date = CURRENT_DATE + INTERVAL '7 days' -- Example: check 7 days from now
    AND s.time_slot_template_id = 1 -- Time slot ID from seed data
    AND s.status = 'planned';

-- For Teacher Conflict Check (Teacher ID 1, checking a specific date and time slot)
SELECT
    s.id AS conflicting_session_id,
    s.date,
    cl.code AS class_code,
    cl.name AS class_name,
    c.name AS course_name,
    tst.name AS time_slot_name,
    tst.start_time,
    tst.end_time,
    ts.role AS teaching_role
FROM
    session s
    INNER JOIN teaching_slot ts ON s.id = ts.session_id
    INNER JOIN "class" cl ON s.class_id = cl.id
    INNER JOIN course c ON cl.course_id = c.id
    INNER JOIN time_slot_template tst ON s.time_slot_template_id = tst.id
WHERE
    ts.teacher_id = 1 -- Teacher ID from seed data
    AND s.date = CURRENT_DATE + INTERVAL '7 days' -- Example: check 7 days from now
    AND s.time_slot_template_id = 1 -- Time slot ID from seed data
    AND s.status = 'planned'
    AND ts.status IN ('scheduled', 'substituted');


-- =========================================
-- QUERY 8: STUDENT'S UPCOMING SESSIONS
-- =========================================
-- USE CASE: Get student's next N upcoming sessions (for dashboard/homepage)
-- EXAMPLE: Student ID 1, next 5 sessions

SELECT
    s.id AS session_id,
    s.date AS session_date,
    s.type AS session_type,
    
    -- Time
    tst.name AS time_slot_name,
    tst.start_time,
    tst.end_time,
    
    -- Class & Course
    cl.code AS class_code,
    cl.name AS class_name,
    c.name AS course_name,
    
    -- Topic
    cs.topic,
    cs.sequence_no,
    
    -- Days until session
    s.date - CURRENT_DATE AS days_until,
    
    -- Location
    (
        SELECT jsonb_agg(
            jsonb_build_object(
                'type', sr.resource_type,
                'name', r.name,
                'location', r.location,
                'meeting_url', r.meeting_url
            )
        )
        FROM session_resource sr
        INNER JOIN resource r ON sr.resource_id = r.id
        WHERE sr.session_id = s.id
    ) AS locations,
    
    -- Teacher
    (
        SELECT ua.full_name
        FROM teaching_slot ts
        INNER JOIN teacher t ON ts.teacher_id = t.id
        INNER JOIN user_account ua ON t.user_account_id = ua.id
        WHERE ts.session_id = s.id AND ts.role = 'primary'
        LIMIT 1
    ) AS teacher_name
    
FROM
    session s
    INNER JOIN student_session ss ON s.id = ss.session_id
    INNER JOIN "class" cl ON s.class_id = cl.id
    INNER JOIN course c ON cl.course_id = c.id
    LEFT JOIN course_session cs ON s.course_session_id = cs.id
    LEFT JOIN time_slot_template tst ON s.time_slot_template_id = tst.id
WHERE
    ss.student_id = 1 -- Student ID from seed data
    AND s.date >= CURRENT_DATE
    AND s.status = 'planned'
ORDER BY
    s.date, tst.start_time
LIMIT 5;


-- =========================================
-- QUERY 9: CLASS SESSION CALENDAR VIEW
-- =========================================
-- USE CASE: View all sessions for a class in a month (for academic staff/manager)
-- EXAMPLE: Class ID 1, current month

SELECT
    s.id AS session_id,
    s.date AS session_date,
    EXTRACT(DOW FROM s.date) AS day_of_week, -- 0=Sunday, 1=Monday, etc.
    s.type AS session_type,
    s.status AS session_status,
    
    -- Time
    tst.name AS time_slot_name,
    tst.start_time,
    tst.end_time,
    
    -- Session details
    cs.sequence_no,
    cs.topic,
    cp.phase_number,
    cp.name AS phase_name,
    
    -- Teachers
    (
        SELECT jsonb_agg(
            jsonb_build_object(
                'teacher_name', ua.full_name,
                'role', ts.role,
                'status', ts.status
            )
        )
        FROM teaching_slot ts
        INNER JOIN teacher t ON ts.teacher_id = t.id
        INNER JOIN user_account ua ON t.user_account_id = ua.id
        WHERE ts.session_id = s.id
    ) AS teachers,
    
    -- Location
    (
        SELECT r.name
        FROM session_resource sr
        INNER JOIN resource r ON sr.resource_id = r.id
        WHERE sr.session_id = s.id
        LIMIT 1
    ) AS location_name,
    
    -- Attendance summary (if session is done)
    CASE WHEN s.status = 'done' THEN
        (
            SELECT jsonb_build_object(
                'total', COUNT(*),
                'present', COUNT(*) FILTER (WHERE ss.attendance_status = 'present'),
                'absent', COUNT(*) FILTER (WHERE ss.attendance_status = 'absent')
            )
            FROM student_session ss
            WHERE ss.session_id = s.id
        )
    ELSE NULL
    END AS attendance_summary
    
FROM
    session s
    LEFT JOIN course_session cs ON s.course_session_id = cs.id
    LEFT JOIN course_phase cp ON cs.phase_id = cp.id
    LEFT JOIN time_slot_template tst ON s.time_slot_template_id = tst.id
WHERE
    s.class_id = 1 -- Class ID from seed data
    AND s.date >= DATE_TRUNC('month', CURRENT_DATE)
    AND s.date < DATE_TRUNC('month', CURRENT_DATE) + INTERVAL '1 month'
ORDER BY
    s.date, tst.start_time;


-- =========================================
-- QUERY 10: TEACHER'S WEEKLY TEACHING SCHEDULE
-- =========================================
-- USE CASE: Teacher views their teaching schedule for a week
-- EXAMPLE: Teacher ID 1, current week

SELECT
    s.id AS session_id,
    s.date AS session_date,
    EXTRACT(DOW FROM s.date) AS day_of_week,
    
    -- Time
    tst.name AS time_slot_name,
    tst.start_time,
    tst.end_time,
    
    -- Class & Course
    cl.code AS class_code,
    cl.name AS class_name,
    c.name AS course_name,
    
    -- Session info
    cs.sequence_no,
    cs.topic,
    s.type AS session_type,
    s.status AS session_status,
    
    -- Teaching role
    ts.skill,
    ts.role AS teaching_role,
    ts.status AS teaching_slot_status,
    
    -- Location
    (
        SELECT jsonb_build_object(
            'type', sr.resource_type,
            'name', r.name,
            'location', r.location,
            'meeting_url', r.meeting_url,
            'meeting_id', r.meeting_id
        )
        FROM session_resource sr
        INNER JOIN resource r ON sr.resource_id = r.id
        WHERE sr.session_id = s.id
        LIMIT 1
    ) AS location,
    
    -- Student count
    (
        SELECT COUNT(*)
        FROM student_session ss
        WHERE ss.session_id = s.id
    ) AS student_count
    
FROM
    session s
    INNER JOIN teaching_slot ts ON s.id = ts.session_id
    INNER JOIN "class" cl ON s.class_id = cl.id
    INNER JOIN course c ON cl.course_id = c.id
    LEFT JOIN course_session cs ON s.course_session_id = cs.id
    LEFT JOIN time_slot_template tst ON s.time_slot_template_id = tst.id
WHERE
    ts.teacher_id = 1 -- Teacher ID from seed data
    AND s.date >= CURRENT_DATE
    AND s.date < CURRENT_DATE + INTERVAL '7 days'
    AND s.status != 'cancelled'
ORDER BY
    s.date, tst.start_time;


-- =========================================
-- EXAMPLE USAGE WITH SEED DATA
-- =========================================

-- All queries above now use concrete values from seed data:
-- - Student ID: 1
-- - Teacher ID: 1  
-- - Session IDs: 1, 5
-- - Class ID: 1
-- - Time Slot Template ID: 1
-- - Dates: CURRENT_DATE and relative intervals

-- You can run these queries directly against a database populated with seed-data.sql

-- =========================================
-- PERFORMANCE NOTES
-- =========================================
-- 1. All queries use appropriate indexes defined in database-schema.sql
-- 2. CTEs are used for readability; optimizer will inline when beneficial
-- 3. JSONB aggregation is used for nested objects (teachers, materials, etc.)
-- 4. LIMIT clauses prevent excessive data return
-- 5. For production, consider adding EXPLAIN ANALYZE to optimize query plans

-- =========================================
-- MAINTENANCE NOTES
-- =========================================
-- When adding new fields to entities:
-- 1. Update corresponding CTEs in queries
-- 2. Add to JSONB object builders
-- 3. Test with realistic data volumes
-- 4. Update API DTOs to match JSON structure
-- 5. Document any breaking changes

-- END OF FILE

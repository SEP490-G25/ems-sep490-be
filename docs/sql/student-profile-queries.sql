-- =========================================
-- STUDENT PROFILE - SQL QUERIES
-- =========================================
-- Các queries để lấy dữ liệu cho trang Student Profile
-- Bỏ qua: Schedule, Requests, Feedback, Course Materials (ở các page khác)
-- =========================================

-- =========================================
-- 1. THÔNG TIN CƠ BẢN (Basic Information + Skill Assessment)
-- =========================================
-- Input: student_id hoặc user_id
-- Output: Thông tin cá nhân + skill assessment history của student

-- 1.1. THÔNG TIN PROFILE ĐẦY ĐỦ
WITH student_basic AS (
    SELECT 
        s.id AS student_id,
        s.student_code,
        s.level AS current_level,
        u.id AS user_id,
        u.email,
        u.phone,
        u.facebook_url,
        u.full_name,
        u.gender,
        u.dob,
        u.address,
        u.status AS account_status,
        u.last_login_at,
        s.created_at AS student_since,
        s.updated_at AS last_updated,
        -- Calculate age
        DATE_PART('year', AGE(CURRENT_DATE, u.dob)) AS age
    FROM student s
    INNER JOIN user_account u ON s.user_id = u.id
    WHERE s.id = :student_id  -- Parameter binding
),
student_branches AS (
    SELECT 
        sb.student_id,
        json_agg(
            json_build_object(
                'branch_id', b.id,
                'branch_code', b.code,
                'branch_name', b.name,
                'center_name', c.name,
                'assigned_at', ub.assigned_at
            ) ORDER BY ub.assigned_at DESC
        ) AS branches
    FROM student_basic sb
    INNER JOIN user_branches ub ON ub.user_id = sb.user_id
    INNER JOIN branch b ON b.id = ub.branch_id
    INNER JOIN center c ON c.id = b.center_id
    GROUP BY sb.student_id
),
latest_skill_assessments AS (
    SELECT 
        rsa.student_id,
        json_agg(
            json_build_object(
                'skill', rsa.skill,
                'level_code', l.code,
                'level_name', l.name,
                'score', rsa.score,
                'assessment_date', rsa.assessment_date,
                'assessment_type', rsa.assessment_type,
                'assessed_by_name', assessor.full_name,
                'note', rsa.note
            ) ORDER BY rsa.skill, rsa.assessment_date DESC
        ) AS skill_assessments
    FROM replacement_skill_assessment rsa
    LEFT JOIN level l ON l.id = rsa.level_id
    LEFT JOIN user_account assessor ON assessor.id = rsa.assessed_by
    WHERE rsa.student_id = :student_id
      AND rsa.assessment_date = (
          -- Get most recent assessment for each skill
          SELECT MAX(rsa2.assessment_date)
          FROM replacement_skill_assessment rsa2
          WHERE rsa2.student_id = rsa.student_id
            AND rsa2.skill = rsa.skill
      )
    GROUP BY rsa.student_id
),
skill_summary AS (
    SELECT 
        rsa.student_id,
        json_object_agg(
            rsa.skill,
            json_build_object(
                'level_code', l.code,
                'score', rsa.score,
                'assessment_date', rsa.assessment_date
            )
        ) AS skill_levels
    FROM replacement_skill_assessment rsa
    LEFT JOIN level l ON l.id = rsa.level_id
    WHERE rsa.student_id = :student_id
      AND rsa.assessment_date = (
          SELECT MAX(rsa2.assessment_date)
          FROM replacement_skill_assessment rsa2
          WHERE rsa2.student_id = rsa.student_id
            AND rsa2.skill = rsa.skill
      )
    GROUP BY rsa.student_id
)
SELECT 
    sb.*,
    COALESCE(sbr.branches, '[]'::json) AS branches,
    COALESCE(lsa.skill_assessments, '[]'::json) AS skill_assessment_history,
    COALESCE(ss.skill_levels, '{}'::json) AS current_skill_levels
FROM student_basic sb
LEFT JOIN student_branches sbr ON sbr.student_id = sb.student_id
LEFT JOIN latest_skill_assessments lsa ON lsa.student_id = sb.student_id
LEFT JOIN skill_summary ss ON ss.student_id = sb.student_id;

-- Example usage:
-- WHERE s.id = 1;  -- Replace :student_id with actual value
-- WHERE u.id = 26; -- Or search by user_id

-- =========================================
-- 1.2. SKILL ASSESSMENT HISTORY (Chi tiết lịch sử đánh giá)
-- =========================================
-- Lấy tất cả lịch sử đánh giá của student (bao gồm cả đánh giá cũ)

SELECT 
    rsa.id AS assessment_id,
    rsa.student_id,
    s.student_code,
    rsa.skill,
    l.code AS level_code,
    l.name AS level_name,
    rsa.score,
    rsa.assessment_date,
    rsa.assessment_type,
    rsa.note,
    assessor.full_name AS assessed_by_name,
    assessor.email AS assessed_by_email,
    rsa.created_at,
    -- Đánh dấu assessment mới nhất cho mỗi skill
    CASE 
        WHEN rsa.assessment_date = (
            SELECT MAX(rsa2.assessment_date)
            FROM replacement_skill_assessment rsa2
            WHERE rsa2.student_id = rsa.student_id
              AND rsa2.skill = rsa.skill
        ) THEN TRUE
        ELSE FALSE
    END AS is_latest
FROM replacement_skill_assessment rsa
INNER JOIN student s ON s.id = rsa.student_id
LEFT JOIN level l ON l.id = rsa.level_id
LEFT JOIN user_account assessor ON assessor.id = rsa.assessed_by
WHERE rsa.student_id = :student_id
ORDER BY rsa.skill, rsa.assessment_date DESC;

-- =========================================
-- 1.3. SKILL COMPARISON (So sánh kỹ năng)
-- =========================================
-- So sánh điểm số các kỹ năng của student (radar chart data)

SELECT 
    rsa.skill,
    rsa.score,
    l.code AS level_code,
    rsa.assessment_date,
    rsa.assessment_type
FROM replacement_skill_assessment rsa
LEFT JOIN level l ON l.id = rsa.level_id
WHERE rsa.student_id = :student_id
  AND rsa.assessment_date = (
      SELECT MAX(rsa2.assessment_date)
      FROM replacement_skill_assessment rsa2
      WHERE rsa2.student_id = rsa.student_id
        AND rsa2.skill = rsa.skill
  )
ORDER BY 
    CASE rsa.skill
        WHEN 'reading' THEN 1
        WHEN 'writing' THEN 2
        WHEN 'speaking' THEN 3
        WHEN 'listening' THEN 4
        WHEN 'general' THEN 5
    END;


-- =========================================
-- 2. ENROLLMENT & CLASS PARTICIPATION (Thông tin lớp học)
-- =========================================
-- Input: student_id
-- Output: Danh sách các lớp student đã/đang tham gia với thông tin chi tiết

-- 2.1. DANH SÁCH LỚP HỌC ĐẦY ĐỦ
SELECT 
    e.id AS enrollment_id,
    e.class_id,
    c.code AS class_code,
    c.name AS class_name,
    c.status AS class_status,
    c.modality,
    e.status AS enrollment_status,
    
    -- Branch & Center info
    b.id AS branch_id,
    b.code AS branch_code,
    b.name AS branch_name,
    cen.name AS center_name,
    
    -- Course info
    co.id AS course_id,
    co.code AS course_code,
    co.name AS course_name,
    subj.name AS subject_name,
    lv.code AS level_code,
    lv.name AS level_name,
    
    -- Course structure
    co.total_hours,
    co.duration_weeks,
    co.session_per_week,
    co.hours_per_session,
    
    -- Class schedule
    c.start_date,
    c.planned_end_date,
    c.actual_end_date,
    c.schedule_days,  -- Array of days [2,4,6] = Mon, Wed, Fri
    
    -- Enrollment details
    e.enrolled_at,
    e.left_at,
    
    -- First & Last session info
    js.id AS join_session_id,
    js.date AS join_session_date,
    js_cs.sequence_no AS join_session_sequence,
    js_cs.topic AS join_session_topic,
    
    ls.id AS left_session_id,
    ls.date AS left_session_date,
    ls_cs.sequence_no AS left_session_sequence,
    ls_cs.topic AS left_session_topic,
    
    -- Calculate progress
    CASE 
        WHEN e.status = 'enrolled' THEN
            CASE 
                WHEN c.status = 'ongoing' THEN 'Đang học'
                WHEN c.status = 'scheduled' THEN 'Sắp học'
                ELSE c.status::TEXT
            END
        WHEN e.status = 'completed' THEN 'Đã hoàn thành'
        WHEN e.status = 'transferred' THEN 'Đã chuyển lớp'
        WHEN e.status = 'dropped' THEN 'Đã nghỉ'
        WHEN e.status = 'waitlisted' THEN 'Danh sách chờ'
    END AS status_display,
    
    -- Student's attendance in this class
    (
        SELECT COUNT(*)
        FROM student_session ss
        INNER JOIN session ses ON ses.id = ss.session_id
        WHERE ss.student_id = e.student_id
          AND ses.class_id = e.class_id
          AND ses.status = 'done'
    ) AS sessions_attended,
    
    (
        SELECT COUNT(*)
        FROM session ses
        WHERE ses.class_id = e.class_id
          AND ses.status = 'done'
          AND ses.date >= COALESCE(js.date, c.start_date)
          AND (e.left_at IS NULL OR ses.date <= COALESCE(ls.date, CURRENT_DATE))
    ) AS total_sessions_for_student,
    
    -- Attendance rate for this class
    (
        SELECT ROUND(
            COUNT(*) FILTER (WHERE ss.attendance_status = 'present')::NUMERIC / 
            NULLIF(COUNT(*), 0) * 100, 2
        )
        FROM student_session ss
        INNER JOIN session ses ON ses.id = ss.session_id
        WHERE ss.student_id = e.student_id
          AND ses.class_id = e.class_id
          AND ses.status = 'done'
    ) AS attendance_rate,
    
    -- Homework completion for this class
    (
        SELECT ROUND(
            COUNT(*) FILTER (WHERE ss.homework_status = 'completed')::NUMERIC / 
            NULLIF(COUNT(*) FILTER (WHERE ss.homework_status IS NOT NULL AND ss.homework_status != 'no_homework'), 0) * 100, 2
        )
        FROM student_session ss
        INNER JOIN session ses ON ses.id = ss.session_id
        WHERE ss.student_id = e.student_id
          AND ses.class_id = e.class_id
          AND ses.status = 'done'
    ) AS homework_completion_rate,
    
    -- Teachers of this class (JSON array)
    (
        SELECT json_agg(DISTINCT json_build_object(
            'teacher_id', t.id,
            'teacher_name', ua.full_name,
            'employee_code', t.employee_code,
            'primary_skill', ts_agg.skills
        ))
        FROM teaching_slot ts
        INNER JOIN session ses ON ses.id = ts.session_id
        INNER JOIN teacher t ON t.id = ts.teacher_id
        INNER JOIN user_account ua ON ua.id = t.user_account_id
        LEFT JOIN LATERAL (
            SELECT json_agg(DISTINCT ts2.skill) AS skills
            FROM teaching_slot ts2
            WHERE ts2.teacher_id = t.id
              AND ts2.session_id IN (
                  SELECT id FROM session WHERE class_id = e.class_id
              )
        ) ts_agg ON TRUE
        WHERE ses.class_id = e.class_id
    ) AS teachers,
    
    -- Created/updated tracking
    e.created_at AS enrollment_created_at,
    e.updated_at AS enrollment_updated_at

FROM enrollment e
INNER JOIN "class" c ON c.id = e.class_id
INNER JOIN branch b ON b.id = c.branch_id
INNER JOIN center cen ON cen.id = b.center_id
INNER JOIN course co ON co.id = c.course_id
INNER JOIN subject subj ON subj.id = co.subject_id
LEFT JOIN level lv ON lv.id = co.level_id
LEFT JOIN session js ON js.id = e.join_session_id
LEFT JOIN course_session js_cs ON js_cs.id = js.course_session_id
LEFT JOIN session ls ON ls.id = e.left_session_id
LEFT JOIN course_session ls_cs ON ls_cs.id = ls.course_session_id

WHERE e.student_id = :student_id

ORDER BY 
    CASE e.status
        WHEN 'enrolled' THEN 1
        WHEN 'waitlisted' THEN 2
        WHEN 'completed' THEN 3
        WHEN 'transferred' THEN 4
        WHEN 'dropped' THEN 5
    END,
    c.start_date DESC;


-- =========================================
-- 2.2. CHI TIẾT MỘT LỚP HỌC CỤ THỂ
-- =========================================
-- Input: student_id, class_id
-- Output: Thông tin chi tiết về enrollment và tiến độ học tập

WITH class_sessions AS (
    SELECT 
        ses.id,
        ses.date,
        ses.status,
        cs.sequence_no,
        cs.topic,
        cs.student_task,
        cs.skill_set,
        cp.phase_number,
        cp.name AS phase_name,
        -- Check if student has session record
        ss.attendance_status,
        ss.homework_status,
        ss.is_makeup,
        ss.note AS student_note,
        ss.recorded_at
    FROM session ses
    INNER JOIN course_session cs ON cs.id = ses.course_session_id
    INNER JOIN course_phase cp ON cp.id = cs.phase_id
    LEFT JOIN student_session ss ON ss.session_id = ses.id AND ss.student_id = :student_id
    WHERE ses.class_id = :class_id
),
phase_summary AS (
    SELECT 
        cp.id AS phase_id,
        cp.phase_number,
        cp.name AS phase_name,
        cp.duration_weeks,
        cp.learning_focus,
        COUNT(cs.id) AS total_sessions,
        MIN(ses.date) AS phase_start_date,
        MAX(ses.date) AS phase_end_date,
        COUNT(ss.session_id) FILTER (WHERE ss.attendance_status = 'present') AS attended_sessions
    FROM course_phase cp
    INNER JOIN course_session cs ON cs.phase_id = cp.id
    INNER JOIN session ses ON ses.course_session_id = cs.id
    LEFT JOIN student_session ss ON ss.session_id = ses.id AND ss.student_id = :student_id
    WHERE cp.course_id = (SELECT course_id FROM "class" WHERE id = :class_id)
      AND ses.class_id = :class_id
    GROUP BY cp.id, cp.phase_number, cp.name, cp.duration_weeks, cp.learning_focus
)
SELECT 
    e.id AS enrollment_id,
    e.class_id,
    c.code AS class_code,
    c.name AS class_name,
    e.status AS enrollment_status,
    e.enrolled_at,
    e.left_at,
    
    -- Enrollment period
    CASE 
        WHEN e.join_session_id IS NOT NULL THEN 'Late Enrollment'
        ELSE 'Regular Enrollment'
    END AS enrollment_type,
    
    -- Progress statistics
    (SELECT COUNT(*) FROM class_sessions) AS total_sessions,
    (SELECT COUNT(*) FROM class_sessions WHERE status = 'done') AS completed_sessions,
    (SELECT COUNT(*) FROM class_sessions WHERE attendance_status = 'present') AS attended_sessions,
    (SELECT COUNT(*) FROM class_sessions WHERE attendance_status = 'absent') AS absent_sessions,
    (SELECT COUNT(*) FROM class_sessions WHERE is_makeup = TRUE) AS makeup_sessions,
    
    -- Homework progress
    (SELECT COUNT(*) FROM class_sessions WHERE homework_status = 'completed') AS homework_completed,
    (SELECT COUNT(*) FROM class_sessions WHERE homework_status IS NOT NULL AND homework_status != 'no_homework') AS homework_assigned,
    
    -- Phase breakdown
    (
        SELECT json_agg(json_build_object(
            'phase_id', phase_id,
            'phase_number', phase_number,
            'phase_name', phase_name,
            'duration_weeks', duration_weeks,
            'learning_focus', learning_focus,
            'total_sessions', total_sessions,
            'attended_sessions', attended_sessions,
            'phase_start_date', phase_start_date,
            'phase_end_date', phase_end_date,
            'completion_rate', ROUND(attended_sessions::NUMERIC / NULLIF(total_sessions, 0) * 100, 2)
        ) ORDER BY phase_number)
        FROM phase_summary
    ) AS phases,
    
    -- All sessions with details
    (
        SELECT json_agg(json_build_object(
            'session_id', id,
            'date', date,
            'status', status,
            'sequence_no', sequence_no,
            'topic', topic,
            'student_task', student_task,
            'skill_set', skill_set,
            'phase_number', phase_number,
            'phase_name', phase_name,
            'attendance_status', attendance_status,
            'homework_status', homework_status,
            'is_makeup', is_makeup,
            'student_note', student_note,
            'recorded_at', recorded_at
        ) ORDER BY date, sequence_no)
        FROM class_sessions
    ) AS sessions

FROM enrollment e
INNER JOIN "class" c ON c.id = e.class_id
WHERE e.student_id = :student_id
  AND e.class_id = :class_id;


-- =========================================
-- 2.3. LỚP ĐANG HỌC (Active Classes Only)
-- =========================================
-- Simplified query for quick dashboard display

SELECT 
    c.id AS class_id,
    c.code AS class_code,
    c.name AS class_name,
    c.modality,
    co.name AS course_name,
    lv.code AS level_code,
    b.name AS branch_name,
    c.start_date,
    c.planned_end_date,
    c.schedule_days,
    
    -- Next session
    (
        SELECT json_build_object(
            'session_id', ses.id,
            'date', ses.date,
            'topic', cs.topic,
            'sequence_no', cs.sequence_no
        )
        FROM session ses
        LEFT JOIN course_session cs ON cs.id = ses.course_session_id
        WHERE ses.class_id = c.id
          AND ses.date >= CURRENT_DATE
          AND ses.status = 'planned'
        ORDER BY ses.date ASC
        LIMIT 1
    ) AS next_session,
    
    -- Progress percentage
    ROUND(
        (SELECT COUNT(*) FROM session WHERE class_id = c.id AND status = 'done')::NUMERIC /
        NULLIF((SELECT COUNT(*) FROM session WHERE class_id = c.id), 0) * 100, 2
    ) AS progress_percent

FROM enrollment e
INNER JOIN "class" c ON c.id = e.class_id
INNER JOIN course co ON co.id = c.course_id
INNER JOIN branch b ON b.id = c.branch_id
LEFT JOIN level lv ON lv.id = co.level_id

WHERE e.student_id = :student_id
  AND e.status = 'enrolled'
  AND c.status IN ('scheduled', 'ongoing')

ORDER BY c.start_date DESC;


-- =========================================
-- 3. ACADEMIC PERFORMANCE (Kết quả học tập)
-- =========================================
-- Input: student_id
-- Output: Danh sách classes → assessments → scores

-- 3.1. TỔNG QUAN ĐIỂM SỐ THEO LỚP (Classes with Assessments & Scores)
SELECT 
    -- Class info
    c.id AS class_id,
    c.code AS class_code,
    c.name AS class_name,
    c.status AS class_status,
    co.name AS course_name,
    lv.code AS level_code,
    b.name AS branch_name,
    
    -- Enrollment info
    e.status AS enrollment_status,
    e.enrolled_at,
    e.left_at,
    
    -- Assessments & Scores (JSON array)
    COALESCE(
        (
            SELECT json_agg(
                json_build_object(
                    'assessment_id', a.id,
                    'assessment_name', a.name,
                    'assessment_kind', a.kind,
                    'max_score', a.max_score,
                    'description', a.description,
                    'created_by_name', creator.full_name,
                    'created_at', a.created_at,
                    -- Course assessment reference (if any)
                    'course_assessment_id', a.course_assessment_id,
                    'course_assessment_name', ca.name,
                    'course_assessment_skills', ca.skills,
                    -- Student's score
                    'student_score', s.score,
                    'student_feedback', s.feedback,
                    'graded_by_name', grader.full_name,
                    'graded_at', s.graded_at,
                    -- Score percentage
                    'score_percentage', CASE 
                        WHEN s.score IS NOT NULL AND a.max_score > 0 
                        THEN ROUND((s.score / a.max_score * 100), 2)
                        ELSE NULL
                    END,
                    -- Status
                    'score_status', CASE 
                        WHEN s.score IS NULL THEN 'not_graded'
                        WHEN s.score >= (a.max_score * 0.8) THEN 'excellent'
                        WHEN s.score >= (a.max_score * 0.65) THEN 'good'
                        WHEN s.score >= (a.max_score * 0.5) THEN 'pass'
                        ELSE 'fail'
                    END
                ) ORDER BY 
                    CASE a.kind
                        WHEN 'quiz' THEN 1
                        WHEN 'assignment' THEN 2
                        WHEN 'practice' THEN 3
                        WHEN 'oral' THEN 4
                        WHEN 'midterm' THEN 5
                        WHEN 'project' THEN 6
                        WHEN 'final' THEN 7
                        ELSE 8
                    END,
                    a.created_at
            )
            FROM assessment a
            LEFT JOIN score s ON s.assessment_id = a.id AND s.student_id = e.student_id
            LEFT JOIN course_assessment ca ON ca.id = a.course_assessment_id
            LEFT JOIN user_account creator ON creator.id = a.created_by
            LEFT JOIN teacher grader_t ON grader_t.id = s.graded_by
            LEFT JOIN user_account grader ON grader.id = grader_t.user_account_id
            WHERE a.class_id = c.id
        ),
        '[]'::json
    ) AS assessments,
    
    -- Summary statistics for this class
    (
        SELECT json_build_object(
            'total_assessments', COUNT(a.id),
            'graded_assessments', COUNT(s.score),
            'pending_assessments', COUNT(a.id) - COUNT(s.score),
            'average_score', ROUND(AVG(s.score), 2),
            'average_percentage', ROUND(AVG(s.score / NULLIF(a.max_score, 0) * 100), 2),
            'highest_score', MAX(s.score),
            'lowest_score', MIN(s.score),
            'total_possible_score', SUM(a.max_score),
            'total_earned_score', SUM(s.score)
        )
        FROM assessment a
        LEFT JOIN score s ON s.assessment_id = a.id AND s.student_id = e.student_id
        WHERE a.class_id = c.id
    ) AS class_score_summary

FROM enrollment e
INNER JOIN "class" c ON c.id = e.class_id
INNER JOIN course co ON co.id = c.course_id
INNER JOIN branch b ON b.id = c.branch_id
LEFT JOIN level lv ON lv.id = co.level_id

WHERE e.student_id = :student_id

ORDER BY 
    CASE e.status
        WHEN 'enrolled' THEN 1
        WHEN 'completed' THEN 2
        WHEN 'transferred' THEN 3
        WHEN 'dropped' THEN 4
    END,
    c.start_date DESC;


-- =========================================
-- 3.2. CHI TIẾT ĐIỂM SỐ MỘT LỚP CỤ THỂ
-- =========================================
-- Input: student_id, class_id
-- Output: Detailed breakdown of assessments with CLO mappings

WITH assessment_details AS (
    SELECT 
        a.id AS assessment_id,
        a.name AS assessment_name,
        a.kind AS assessment_kind,
        a.max_score,
        a.description,
        
        -- Course assessment reference
        ca.id AS course_assessment_id,
        ca.name AS course_assessment_name,
        ca.skills AS course_assessment_skills,
        
        -- Student's score
        s.id AS score_id,
        s.score AS student_score,
        s.feedback AS teacher_feedback,
        s.graded_at,
        
        -- Grader info
        grader.full_name AS graded_by_name,
        
        -- Score metrics
        CASE 
            WHEN a.max_score > 0 
            THEN ROUND((s.score / a.max_score * 100), 2)
            ELSE NULL
        END AS score_percentage,
        
        CASE 
            WHEN s.score IS NULL THEN 'not_graded'
            WHEN s.score >= (a.max_score * 0.8) THEN 'excellent'
            WHEN s.score >= (a.max_score * 0.65) THEN 'good'
            WHEN s.score >= (a.max_score * 0.5) THEN 'pass'
            ELSE 'fail'
        END AS score_status,
        
        -- CLO mappings (if course_assessment exists)
        (
            SELECT json_agg(
                json_build_object(
                    'clo_id', clo.id,
                    'clo_code', clo.code,
                    'clo_description', clo.description,
                    -- PLO mappings for this CLO
                    'plos', (
                        SELECT json_agg(
                            json_build_object(
                                'plo_id', plo.id,
                                'plo_code', plo.code,
                                'plo_description', plo.description
                            )
                        )
                        FROM plo_clo_mapping pcm
                        INNER JOIN plo ON plo.id = pcm.plo_id
                        WHERE pcm.clo_id = clo.id
                    )
                )
            )
            FROM course_assessment_clo_mapping cacm
            INNER JOIN clo ON clo.id = cacm.clo_id
            WHERE cacm.course_assessment_id = ca.id
        ) AS clo_mappings,
        
        a.created_at,
        a.updated_at
        
    FROM assessment a
    LEFT JOIN course_assessment ca ON ca.id = a.course_assessment_id
    LEFT JOIN score s ON s.assessment_id = a.id AND s.student_id = :student_id
    LEFT JOIN teacher grader_t ON grader_t.id = s.graded_by
    LEFT JOIN user_account grader ON grader.id = grader_t.user_account_id
    WHERE a.class_id = :class_id
),
assessment_by_kind AS (
    SELECT 
        assessment_kind,
        json_agg(
            json_build_object(
                'assessment_id', assessment_id,
                'assessment_name', assessment_name,
                'max_score', max_score,
                'student_score', student_score,
                'score_percentage', score_percentage,
                'score_status', score_status,
                'teacher_feedback', teacher_feedback,
                'graded_by_name', graded_by_name,
                'graded_at', graded_at,
                'skills', course_assessment_skills,
                'clo_mappings', clo_mappings
            ) ORDER BY created_at
        ) AS assessments,
        COUNT(*) AS total_count,
        COUNT(student_score) AS graded_count,
        ROUND(AVG(student_score), 2) AS avg_score,
        ROUND(AVG(score_percentage), 2) AS avg_percentage
    FROM assessment_details
    GROUP BY assessment_kind
)
SELECT 
    -- Class basic info
    c.id AS class_id,
    c.code AS class_code,
    c.name AS class_name,
    co.name AS course_name,
    
    -- All assessments grouped by kind
    COALESCE(
        (
            SELECT json_object_agg(
                assessment_kind,
                json_build_object(
                    'assessments', assessments,
                    'total_count', total_count,
                    'graded_count', graded_count,
                    'avg_score', avg_score,
                    'avg_percentage', avg_percentage
                )
            )
            FROM assessment_by_kind
        ),
        '{}'::json
    ) AS assessments_by_kind,
    
    -- Overall summary
    (
        SELECT json_build_object(
            'total_assessments', COUNT(*),
            'graded_assessments', COUNT(student_score),
            'pending_assessments', COUNT(*) - COUNT(student_score),
            'overall_average', ROUND(AVG(student_score), 2),
            'overall_percentage', ROUND(AVG(score_percentage), 2),
            'highest_score_percentage', MAX(score_percentage),
            'lowest_score_percentage', MIN(score_percentage),
            'excellent_count', COUNT(*) FILTER (WHERE score_status = 'excellent'),
            'good_count', COUNT(*) FILTER (WHERE score_status = 'good'),
            'pass_count', COUNT(*) FILTER (WHERE score_status = 'pass'),
            'fail_count', COUNT(*) FILTER (WHERE score_status = 'fail'),
            'not_graded_count', COUNT(*) FILTER (WHERE score_status = 'not_graded')
        )
        FROM assessment_details
    ) AS overall_summary

FROM "class" c
INNER JOIN course co ON co.id = c.course_id
WHERE c.id = :class_id;


-- =========================================
-- 3.3. BẢNG ĐIỂM THEO LOẠI BÀI KIỂM TRA (Assessment Kind Breakdown)
-- =========================================
-- Input: student_id
-- Output: Scores grouped by assessment kind across all classes

SELECT 
    a.kind AS assessment_kind,
    COUNT(a.id) AS total_assessments,
    COUNT(s.score) AS graded_assessments,
    COUNT(a.id) - COUNT(s.score) AS pending_assessments,
    
    -- Score statistics
    ROUND(AVG(s.score), 2) AS average_score,
    ROUND(AVG(s.score / NULLIF(a.max_score, 0) * 100), 2) AS average_percentage,
    MAX(s.score / NULLIF(a.max_score, 0) * 100) AS highest_percentage,
    MIN(s.score / NULLIF(a.max_score, 0) * 100) AS lowest_percentage,
    
    -- Performance distribution
    COUNT(*) FILTER (WHERE s.score >= (a.max_score * 0.8)) AS excellent_count,
    COUNT(*) FILTER (WHERE s.score >= (a.max_score * 0.65) AND s.score < (a.max_score * 0.8)) AS good_count,
    COUNT(*) FILTER (WHERE s.score >= (a.max_score * 0.5) AND s.score < (a.max_score * 0.65)) AS pass_count,
    COUNT(*) FILTER (WHERE s.score < (a.max_score * 0.5)) AS fail_count,
    
    -- Sample assessments (latest 5)
    (
        SELECT json_agg(
            json_build_object(
                'class_name', c.name,
                'assessment_name', a2.name,
                'score', s2.score,
                'max_score', a2.max_score,
                'percentage', ROUND((s2.score / NULLIF(a2.max_score, 0) * 100), 2),
                'graded_at', s2.graded_at
            ) ORDER BY s2.graded_at DESC
        )
        FROM score s2
        INNER JOIN assessment a2 ON a2.id = s2.assessment_id
        INNER JOIN "class" c ON c.id = a2.class_id
        WHERE s2.student_id = e.student_id
          AND a2.kind = a.kind
          AND s2.score IS NOT NULL
        LIMIT 5
    ) AS recent_scores

FROM enrollment e
INNER JOIN "class" c ON c.id = e.class_id
INNER JOIN assessment a ON a.class_id = c.id
LEFT JOIN score s ON s.assessment_id = a.id AND s.student_id = e.student_id

WHERE e.student_id = :student_id

GROUP BY a.kind, e.student_id
ORDER BY 
    CASE a.kind
        WHEN 'quiz' THEN 1
        WHEN 'assignment' THEN 2
        WHEN 'practice' THEN 3
        WHEN 'oral' THEN 4
        WHEN 'midterm' THEN 5
        WHEN 'project' THEN 6
        WHEN 'final' THEN 7
        ELSE 8
    END;


-- =========================================
-- 3.4. CLO ACHIEVEMENT TRACKING (Theo dõi đạt chuẩn đầu ra)
-- =========================================
-- Input: student_id, class_id
-- Output: CLO achievement based on assessment scores

WITH class_clos AS (
    SELECT DISTINCT
        clo.id AS clo_id,
        clo.code AS clo_code,
        clo.description AS clo_description,
        co.id AS course_id,
        co.name AS course_name
    FROM "class" c
    INNER JOIN course co ON co.id = c.course_id
    INNER JOIN clo ON clo.course_id = co.id
    WHERE c.id = :class_id
),
clo_scores AS (
    SELECT 
        clo.clo_id,
        clo.clo_code,
        clo.clo_description,
        
        -- Assessments measuring this CLO
        json_agg(
            json_build_object(
                'assessment_name', a.name,
                'assessment_kind', a.kind,
                'score', s.score,
                'max_score', a.max_score,
                'percentage', ROUND((s.score / NULLIF(a.max_score, 0) * 100), 2)
            ) ORDER BY a.created_at
        ) AS assessments,
        
        -- Average achievement for this CLO
        ROUND(AVG(s.score / NULLIF(a.max_score, 0) * 100), 2) AS clo_achievement_percentage,
        
        -- Achievement status
        CASE 
            WHEN AVG(s.score / NULLIF(a.max_score, 0) * 100) >= 70 THEN 'achieved'
            WHEN AVG(s.score / NULLIF(a.max_score, 0) * 100) >= 50 THEN 'partial'
            ELSE 'not_achieved'
        END AS achievement_status,
        
        COUNT(a.id) AS total_assessments,
        COUNT(s.score) AS graded_assessments
        
    FROM class_clos clo
    INNER JOIN course_assessment_clo_mapping cacm ON cacm.clo_id = clo.clo_id
    INNER JOIN course_assessment ca ON ca.id = cacm.course_assessment_id
    INNER JOIN assessment a ON a.course_assessment_id = ca.id AND a.class_id = :class_id
    LEFT JOIN score s ON s.assessment_id = a.id AND s.student_id = :student_id
    GROUP BY clo.clo_id, clo.clo_code, clo.clo_description
)
SELECT 
    clo_id,
    clo_code,
    clo_description,
    assessments,
    clo_achievement_percentage,
    achievement_status,
    total_assessments,
    graded_assessments,
    
    -- Related PLOs
    (
        SELECT json_agg(
            json_build_object(
                'plo_id', plo.id,
                'plo_code', plo.code,
                'plo_description', plo.description
            )
        )
        FROM plo_clo_mapping pcm
        INNER JOIN plo ON plo.id = pcm.plo_id
        WHERE pcm.clo_id = cs.clo_id
    ) AS related_plos

FROM clo_scores cs
ORDER BY clo_code;


-- =========================================
-- 4. ATTENDANCE & SESSION TRACKING (Điểm danh & Theo dõi buổi học)
-- =========================================
-- Input: student_id
-- Output: Thông số và tình trạng điểm danh/bài tập của student

-- 4.1. TỔNG QUAN ATTENDANCE (Overall Attendance Summary)
WITH attendance_summary AS (
    SELECT 
        COUNT(*) AS total_sessions,
        COUNT(*) FILTER (WHERE ss.attendance_status = 'present') AS present_count,
        COUNT(*) FILTER (WHERE ss.attendance_status = 'absent') AS absent_count,
        COUNT(*) FILTER (WHERE ss.attendance_status = 'late') AS late_count,
        COUNT(*) FILTER (WHERE ss.attendance_status = 'excused') AS excused_count,
        COUNT(*) FILTER (WHERE ss.attendance_status = 'remote') AS remote_count,
        COUNT(*) FILTER (WHERE ss.attendance_status = 'planned') AS planned_count,
        COUNT(*) FILTER (WHERE ss.is_makeup = TRUE) AS makeup_count
    FROM student_session ss
    INNER JOIN session ses ON ses.id = ss.session_id
    WHERE ss.student_id = :student_id
      AND ses.status IN ('done', 'planned')
),
homework_summary AS (
    SELECT 
        COUNT(*) FILTER (WHERE ss.homework_status IS NOT NULL AND ss.homework_status != 'no_homework') AS total_homework,
        COUNT(*) FILTER (WHERE ss.homework_status = 'completed') AS completed_homework,
        COUNT(*) FILTER (WHERE ss.homework_status = 'incomplete') AS incomplete_homework,
        COUNT(*) FILTER (WHERE ss.homework_status = 'no_homework') AS no_homework_count
    FROM student_session ss
    INNER JOIN session ses ON ses.id = ss.session_id
    WHERE ss.student_id = :student_id
      AND ses.status = 'done'
),
recent_absences AS (
    SELECT 
        COUNT(*) AS consecutive_absences
    FROM (
        SELECT 
            ss.session_id,
            ses.date,
            ss.attendance_status,
            ROW_NUMBER() OVER (ORDER BY ses.date DESC) AS rn
        FROM student_session ss
        INNER JOIN session ses ON ses.id = ss.session_id
        WHERE ss.student_id = :student_id
          AND ses.status = 'done'
        ORDER BY ses.date DESC
        LIMIT 10
    ) recent
    WHERE attendance_status = 'absent'
      AND rn <= (
          SELECT MIN(rn) 
          FROM (
              SELECT ROW_NUMBER() OVER (ORDER BY ses.date DESC) AS rn
              FROM student_session ss2
              INNER JOIN session ses ON ses.id = ss2.session_id
              WHERE ss2.student_id = :student_id
                AND ses.status = 'done'
                AND ss2.attendance_status != 'absent'
              ORDER BY ses.date DESC
              LIMIT 1
          ) first_present
      )
)
SELECT 
    -- Attendance metrics
    a.total_sessions,
    a.present_count,
    a.absent_count,
    a.late_count,
    a.excused_count,
    a.remote_count,
    a.planned_count,
    a.makeup_count,
    
    -- Attendance rates
    ROUND((a.present_count::NUMERIC / NULLIF(a.total_sessions - a.planned_count, 0) * 100), 2) AS attendance_rate,
    ROUND((a.absent_count::NUMERIC / NULLIF(a.total_sessions - a.planned_count, 0) * 100), 2) AS absence_rate,
    ROUND((a.late_count::NUMERIC / NULLIF(a.total_sessions - a.planned_count, 0) * 100), 2) AS late_rate,
    
    -- Homework metrics
    h.total_homework,
    h.completed_homework,
    h.incomplete_homework,
    h.no_homework_count,
    ROUND((h.completed_homework::NUMERIC / NULLIF(h.total_homework, 0) * 100), 2) AS homework_completion_rate,
    
    -- Alert indicators
    CASE 
        WHEN ROUND((a.absent_count::NUMERIC / NULLIF(a.total_sessions - a.planned_count, 0) * 100), 2) > 20 THEN 'high_absence'
        WHEN ROUND((a.absent_count::NUMERIC / NULLIF(a.total_sessions - a.planned_count, 0) * 100), 2) > 10 THEN 'warning_absence'
        ELSE 'good'
    END AS attendance_status,
    
    CASE 
        WHEN ROUND((h.completed_homework::NUMERIC / NULLIF(h.total_homework, 0) * 100), 2) < 50 THEN 'poor_homework'
        WHEN ROUND((h.completed_homework::NUMERIC / NULLIF(h.total_homework, 0) * 100), 2) < 80 THEN 'needs_improvement'
        ELSE 'good'
    END AS homework_status,
    
    ra.consecutive_absences

FROM attendance_summary a
CROSS JOIN homework_summary h
CROSS JOIN recent_absences ra;


-- 4.2. ATTENDANCE THEO LỚP (Attendance by Class)
SELECT 
    c.id AS class_id,
    c.code AS class_code,
    c.name AS class_name,
    co.name AS course_name,
    
    -- Session counts
    COUNT(ses.id) AS total_sessions,
    COUNT(ses.id) FILTER (WHERE ses.status = 'done') AS completed_sessions,
    COUNT(ses.id) FILTER (WHERE ses.status = 'planned') AS upcoming_sessions,
    
    -- Attendance breakdown
    COUNT(ss.session_id) FILTER (WHERE ss.attendance_status = 'present') AS present_count,
    COUNT(ss.session_id) FILTER (WHERE ss.attendance_status = 'absent') AS absent_count,
    COUNT(ss.session_id) FILTER (WHERE ss.attendance_status = 'late') AS late_count,
    COUNT(ss.session_id) FILTER (WHERE ss.attendance_status = 'excused') AS excused_count,
    COUNT(ss.session_id) FILTER (WHERE ss.attendance_status = 'remote') AS remote_count,
    COUNT(ss.session_id) FILTER (WHERE ss.is_makeup = TRUE) AS makeup_sessions,
    
    -- Attendance rate
    ROUND(
        COUNT(ss.session_id) FILTER (WHERE ss.attendance_status = 'present')::NUMERIC /
        NULLIF(COUNT(ses.id) FILTER (WHERE ses.status = 'done'), 0) * 100, 
        2
    ) AS attendance_rate,
    
    -- Homework stats
    COUNT(ss.session_id) FILTER (WHERE ss.homework_status = 'completed') AS homework_completed,
    COUNT(ss.session_id) FILTER (WHERE ss.homework_status = 'incomplete') AS homework_incomplete,
    COUNT(ss.session_id) FILTER (WHERE ss.homework_status IS NOT NULL AND ss.homework_status != 'no_homework') AS homework_assigned,
    
    ROUND(
        COUNT(ss.session_id) FILTER (WHERE ss.homework_status = 'completed')::NUMERIC /
        NULLIF(COUNT(ss.session_id) FILTER (WHERE ss.homework_status IS NOT NULL AND ss.homework_status != 'no_homework'), 0) * 100,
        2
    ) AS homework_completion_rate,
    
    -- Status indicators
    CASE 
        WHEN COUNT(ses.id) FILTER (WHERE ses.status = 'done') = 0 THEN 'not_started'
        WHEN COUNT(ss.session_id) FILTER (WHERE ss.attendance_status = 'absent') > (COUNT(ses.id) FILTER (WHERE ses.status = 'done') * 0.2) THEN 'at_risk'
        WHEN COUNT(ss.session_id) FILTER (WHERE ss.attendance_status = 'absent') > (COUNT(ses.id) FILTER (WHERE ses.status = 'done') * 0.1) THEN 'needs_attention'
        ELSE 'good'
    END AS class_attendance_status

FROM enrollment e
INNER JOIN "class" c ON c.id = e.class_id
INNER JOIN course co ON co.id = c.course_id
LEFT JOIN session ses ON ses.class_id = c.id
LEFT JOIN student_session ss ON ss.session_id = ses.id AND ss.student_id = e.student_id

WHERE e.student_id = :student_id
  AND e.status IN ('enrolled', 'completed')

GROUP BY c.id, c.code, c.name, co.name
ORDER BY 
    CASE e.status 
        WHEN 'enrolled' THEN 1 
        ELSE 2 
    END,
    c.start_date DESC;


-- 4.3. LỊCH SỬ ĐIỂM DANH CHI TIẾT (Detailed Attendance History)
SELECT 
    ses.id AS session_id,
    ses.date AS session_date,
    ses.status AS session_status,
    
    -- Class info
    c.id AS class_id,
    c.code AS class_code,
    c.name AS class_name,
    
    -- Session details
    cs.sequence_no,
    cs.topic,
    cs.student_task,
    cp.phase_number,
    cp.name AS phase_name,
    
    -- Time slot
    tst.name AS time_slot_name,
    tst.start_time,
    tst.end_time,
    
    -- Student attendance
    ss.attendance_status,
    ss.homework_status,
    ss.is_makeup,
    ss.note AS student_note,
    ss.recorded_at,
    
    -- Makeup session reference (if this is a makeup)
    CASE 
        WHEN ss.is_makeup = TRUE THEN (
            SELECT json_build_object(
                'original_session_id', orig_ses.id,
                'original_date', orig_ses.date,
                'original_topic', orig_cs.topic
            )
            FROM student_request sr
            INNER JOIN session orig_ses ON orig_ses.id = sr.target_session_id
            LEFT JOIN course_session orig_cs ON orig_cs.id = orig_ses.course_session_id
            WHERE sr.student_id = ss.student_id
              AND sr.makeup_session_id = ses.id
              AND sr.request_type = 'makeup'
              AND sr.status = 'approved'
            LIMIT 1
        )
        ELSE NULL
    END AS makeup_info,
    
    -- Teacher info
    (
        SELECT json_agg(
            json_build_object(
                'teacher_name', ua.full_name,
                'skill', ts.skill,
                'role', ts.role
            )
        )
        FROM teaching_slot ts
        INNER JOIN teacher t ON t.id = ts.teacher_id
        INNER JOIN user_account ua ON ua.id = t.user_account_id
        WHERE ts.session_id = ses.id
    ) AS teachers

FROM student_session ss
INNER JOIN session ses ON ses.id = ss.session_id
INNER JOIN "class" c ON c.id = ses.class_id
LEFT JOIN course_session cs ON cs.id = ses.course_session_id
LEFT JOIN course_phase cp ON cp.id = cs.phase_id
LEFT JOIN time_slot_template tst ON tst.id = ses.time_slot_template_id

WHERE ss.student_id = :student_id

ORDER BY ses.date DESC, ses.id DESC
LIMIT 50;  -- Lấy 50 sessions gần nhất


-- 4.4. UPCOMING SESSIONS (Các buổi học sắp tới)
SELECT 
    ses.id AS session_id,
    ses.date AS session_date,
    ses.status AS session_status,
    
    -- Class info
    c.id AS class_id,
    c.code AS class_code,
    c.name AS class_name,
    co.name AS course_name,
    
    -- Session details
    cs.sequence_no,
    cs.topic,
    cs.student_task,
    cs.skill_set,
    
    -- Time & location
    tst.name AS time_slot_name,
    tst.start_time,
    tst.end_time,
    tst.duration_min,
    
    -- Resource info
    (
        SELECT json_agg(
            json_build_object(
                'resource_id', r.id,
                'resource_name', r.name,
                'resource_type', sr.resource_type,
                'location', r.location,
                'meeting_url', r.meeting_url
            )
        )
        FROM session_resource sr
        INNER JOIN resource r ON r.id = sr.resource_id
        WHERE sr.session_id = ses.id
    ) AS resources,
    
    -- Teachers
    (
        SELECT json_agg(
            json_build_object(
                'teacher_name', ua.full_name,
                'skill', ts.skill,
                'role', ts.role
            )
        )
        FROM teaching_slot ts
        INNER JOIN teacher t ON t.id = ts.teacher_id
        INNER JOIN user_account ua ON ua.id = t.user_account_id
        WHERE ts.session_id = ses.id
    ) AS teachers,
    
    -- Student session record
    ss.attendance_status,
    ss.homework_status,
    
    -- Days until session
    DATE_PART('day', ses.date - CURRENT_DATE) AS days_until

FROM session ses
INNER JOIN "class" c ON c.id = ses.class_id
INNER JOIN course co ON co.id = c.course_id
INNER JOIN enrollment e ON e.class_id = c.id
LEFT JOIN course_session cs ON cs.id = ses.course_session_id
LEFT JOIN time_slot_template tst ON tst.id = ses.time_slot_template_id
LEFT JOIN student_session ss ON ss.session_id = ses.id AND ss.student_id = e.student_id

WHERE e.student_id = :student_id
  AND e.status = 'enrolled'
  AND ses.status = 'planned'
  AND ses.date >= CURRENT_DATE

ORDER BY ses.date ASC, tst.start_time ASC
LIMIT 10;  -- Next 10 sessions


-- 4.5. ABSENCE & MAKEUP TRACKING (Theo dõi vắng mặt và học bù)
WITH absences AS (
    SELECT 
        ses.id AS session_id,
        ses.date AS absence_date,
        c.id AS class_id,
        c.code AS class_code,
        c.name AS class_name,
        cs.sequence_no,
        cs.topic,
        ss.attendance_status,
        ss.note,
        
        -- Check if makeup request exists
        (
            SELECT json_build_object(
                'request_id', sr.id,
                'status', sr.status,
                'makeup_session_id', sr.makeup_session_id,
                'makeup_date', makeup_ses.date,
                'submitted_at', sr.submitted_at,
                'decided_at', sr.decided_at
            )
            FROM student_request sr
            LEFT JOIN session makeup_ses ON makeup_ses.id = sr.makeup_session_id
            WHERE sr.student_id = ss.student_id
              AND sr.target_session_id = ses.id
              AND sr.request_type IN ('absence', 'makeup')
            ORDER BY sr.created_at DESC
            LIMIT 1
        ) AS makeup_request

    FROM student_session ss
    INNER JOIN session ses ON ses.id = ss.session_id
    INNER JOIN "class" c ON c.id = ses.class_id
    LEFT JOIN course_session cs ON cs.id = ses.course_session_id
    
    WHERE ss.student_id = :student_id
      AND ss.attendance_status IN ('absent', 'excused')
      AND ses.status = 'done'
),
makeup_sessions AS (
    SELECT 
        ses.id AS session_id,
        ses.date AS makeup_date,
        c.code AS class_code,
        c.name AS class_name,
        cs.sequence_no,
        cs.topic,
        ss.attendance_status AS makeup_attendance,
        
        -- Original session info
        (
            SELECT json_build_object(
                'original_session_id', orig_ses.id,
                'original_date', orig_ses.date,
                'original_topic', orig_cs.topic
            )
            FROM student_request sr
            INNER JOIN session orig_ses ON orig_ses.id = sr.target_session_id
            LEFT JOIN course_session orig_cs ON orig_cs.id = orig_ses.course_session_id
            WHERE sr.student_id = ss.student_id
              AND sr.makeup_session_id = ses.id
              AND sr.request_type = 'makeup'
              AND sr.status = 'approved'
            LIMIT 1
        ) AS original_session

    FROM student_session ss
    INNER JOIN session ses ON ses.id = ss.session_id
    INNER JOIN "class" c ON c.id = ses.class_id
    LEFT JOIN course_session cs ON cs.id = ses.course_session_id
    
    WHERE ss.student_id = :student_id
      AND ss.is_makeup = TRUE
)
SELECT 
    'absence' AS record_type,
    json_build_object(
        'total_absences', COUNT(*),
        'unresolved_absences', COUNT(*) FILTER (WHERE makeup_request IS NULL OR (makeup_request->>'status')::TEXT NOT IN ('approved', 'waiting_confirm')),
        'pending_makeup_requests', COUNT(*) FILTER (WHERE (makeup_request->>'status')::TEXT IN ('pending', 'waiting_confirm')),
        'completed_makeup', COUNT(*) FILTER (WHERE (makeup_request->>'status')::TEXT = 'approved'),
        'absences', (
            SELECT json_agg(
                json_build_object(
                    'session_id', session_id,
                    'date', absence_date,
                    'class_code', class_code,
                    'class_name', class_name,
                    'topic', topic,
                    'status', attendance_status,
                    'makeup_request', makeup_request
                ) ORDER BY absence_date DESC
            )
            FROM absences
        )
    ) AS absence_data
FROM absences

UNION ALL

SELECT 
    'makeup' AS record_type,
    json_build_object(
        'total_makeup_sessions', COUNT(*),
        'attended_makeup', COUNT(*) FILTER (WHERE makeup_attendance = 'present'),
        'missed_makeup', COUNT(*) FILTER (WHERE makeup_attendance IN ('absent', 'planned')),
        'makeup_sessions', (
            SELECT json_agg(
                json_build_object(
                    'session_id', session_id,
                    'makeup_date', makeup_date,
                    'class_code', class_code,
                    'topic', topic,
                    'attendance', makeup_attendance,
                    'original_session', original_session
                ) ORDER BY makeup_date DESC
            )
            FROM makeup_sessions
        )
    ) AS makeup_data
FROM makeup_sessions;


-- 4.6. ATTENDANCE TREND (Xu hướng điểm danh theo thời gian)
-- Phân tích attendance theo tuần/tháng để tracking performance

WITH weekly_attendance AS (
    SELECT 
        DATE_TRUNC('week', ses.date) AS week_start,
        COUNT(*) AS total_sessions,
        COUNT(*) FILTER (WHERE ss.attendance_status = 'present') AS present_count,
        COUNT(*) FILTER (WHERE ss.attendance_status = 'absent') AS absent_count,
        COUNT(*) FILTER (WHERE ss.attendance_status = 'late') AS late_count,
        ROUND(
            COUNT(*) FILTER (WHERE ss.attendance_status = 'present')::NUMERIC / 
            NULLIF(COUNT(*), 0) * 100, 
            2
        ) AS attendance_rate
    FROM student_session ss
    INNER JOIN session ses ON ses.id = ss.session_id
    WHERE ss.student_id = :student_id
      AND ses.status = 'done'
      AND ses.date >= CURRENT_DATE - INTERVAL '12 weeks'
    GROUP BY DATE_TRUNC('week', ses.date)
)
SELECT 
    week_start,
    TO_CHAR(week_start, 'YYYY-MM-DD') AS week_start_formatted,
    total_sessions,
    present_count,
    absent_count,
    late_count,
    attendance_rate,
    
    -- Trend indicator
    CASE 
        WHEN attendance_rate >= 90 THEN 'excellent'
        WHEN attendance_rate >= 80 THEN 'good'
        WHEN attendance_rate >= 70 THEN 'acceptable'
        ELSE 'needs_improvement'
    END AS performance_level

FROM weekly_attendance
ORDER BY week_start DESC;


-- =========================================
-- 5. DASHBOARD STATISTICS (Thống kê tổng quan)
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

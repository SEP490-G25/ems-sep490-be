Học viên xem chi tiết một buổi học

WITH -- 1. Core Student Session Info
student_session_core AS (
    SELECT
        ss.student_id,
        ss.session_id,
        ss.is_makeup,
        ss.attendance_status,
        ss.homework_status,
        ss.note AS student_note,
        ss.recorded_at,
        -- Student info
        s.student_code,
        s.level AS student_level,
        u_student.full_name AS student_full_name,
        u_student.email AS student_email,
        u_student.phone AS student_phone,
        u_student.dob AS student_dob,
        u_student.gender AS student_gender
    FROM student_session ss
    JOIN student s ON ss.student_id = s.id
    JOIN user_account u_student ON s.user_id = u_student.id
    WHERE ss.student_id = 1 AND ss.session_id = 1
),

-- 2. Session Detail Info
session_detail AS (
    SELECT
        sess.id AS session_id,
        sess.date AS session_date,
        sess.type AS session_type,
        sess.status AS session_status,
        sess.teacher_note,
        sess.created_at AS session_created_at,
        sess.updated_at AS session_updated_at,
        -- Time slot info
        tst.id AS time_slot_id,
        tst.name AS time_slot_name,
        tst.start_time,
        tst.end_time,
        EXTRACT(EPOCH FROM (tst.end_time - tst.start_time)) / 60 AS duration_min,
        -- Class info
        c.id AS class_id,
        c.code AS class_code,
        c.name AS class_name,
        c.modality,
        c.status AS class_status,
        c.start_date AS class_start_date,
        c.planned_end_date AS class_planned_end_date,
        c.actual_end_date AS class_actual_end_date,
        c.schedule_days,
        c.max_capacity AS class_max_capacity,
        -- Branch info
        b.id AS branch_id,
        b.name AS branch_name,
        b.address AS branch_address,
        b.phone AS branch_phone,
        -- Course info
        course.id AS course_id,
        course.code AS course_code,
        course.name AS course_name,
        course.duration_weeks,
        course.session_per_week,
        course.total_hours,
        course.hours_per_session,
        (course.duration_weeks * course.session_per_week) AS total_sessions,
        -- Subject info
        subj.id AS subject_id,
        subj.code AS subject_code,
        subj.name AS subject_name,
        -- Level info
        lvl.id AS level_id,
        lvl.code AS level_code,
        lvl.name AS level_name
    FROM session sess
    JOIN time_slot_template tst ON sess.time_slot_template_id = tst.id
    JOIN "class" c ON sess.class_id = c.id
    JOIN branch b ON c.branch_id = b.id
    JOIN course ON c.course_id = course.id
    JOIN level lvl ON course.level_id = lvl.id
    JOIN subject subj ON lvl.subject_id = subj.id
    WHERE sess.id = 1
),

-- 3. Class Enrollment Count
class_enrollment_count AS (
    SELECT
        class_id,
        COUNT(*) FILTER (WHERE status = 'enrolled') AS enrolled_count
    FROM enrollment
    WHERE class_id = (SELECT class_id FROM session WHERE id = 1)
    GROUP BY class_id
),

-- 4. Course Session Template Info
course_session_info AS (
    SELECT
        cs.id AS course_session_id,
        cs.sequence_no,
        cs.topic,
        cs.student_task,
        cs.skill_set,
        -- Phase info
        cp.id AS phase_id,
        cp.phase_number,
        cp.name AS phase_name,
        cp.learning_focus AS phase_description,
        cp.duration_weeks AS phase_duration_weeks
    FROM course_session cs
    JOIN course_phase cp ON cs.phase_id = cp.id
    WHERE cs.id = (SELECT course_session_id FROM session WHERE id = 1)
),

-- 5. Teachers Info (Multiple teachers possible)
teachers_info AS (
    SELECT
        ts.session_id,
        json_agg(
            json_build_object(
                'teacher_id', t.id,
                'teacher_code', t.employee_code,
                'teacher_name', u_teacher.full_name,
                'teacher_email', u_teacher.email,
                'teacher_phone', u_teacher.phone,
                'status', ts.status,
                'teacher_note', t.note
            )
        ) AS teachers
    FROM teaching_slot ts
    JOIN teacher t ON ts.teacher_id = t.id
    JOIN user_account u_teacher ON t.user_account_id = u_teacher.id
    WHERE ts.session_id = 1
    GROUP BY ts.session_id
),

-- 6. Resources Info (Room/Zoom)
resources_info AS (
    SELECT
        sr.session_id,
        json_agg(
            json_build_object(
                'resource_id', r.id,
                'resource_type', r.resource_type,
                'resource_name', r.name,
                'description', r.description,
                'capacity', COALESCE(r.capacity_override, r.capacity),
                'meeting_url', r.meeting_url,
                'meeting_id', r.meeting_id
            ) ORDER BY r.resource_type, r.name
        ) AS resources
    FROM session_resource sr
    JOIN resource r ON sr.resource_id = r.id
    WHERE sr.session_id = 1
    GROUP BY sr.session_id
),

-- 7. Course Materials
materials_info AS (
    SELECT
        (SELECT course_session_id FROM session WHERE id = 1) as course_session_id,
        json_agg(
            json_build_object(
                'material_id', cm.id,
                'title', cm.title,
                'url', cm.url,
                'material_type', cm.material_type,
                'uploaded_by', u_uploader.full_name,
                'uploaded_at', cm.uploaded_at
            ) ORDER BY cm.uploaded_at DESC
        ) AS materials
    FROM course_material cm
    LEFT JOIN user_account u_uploader ON cm.uploaded_by = u_uploader.id
    WHERE cm.course_session_id = (SELECT course_session_id FROM session WHERE id = 1)
       OR cm.phase_id = (SELECT phase_id FROM course_session WHERE id = (SELECT course_session_id FROM session WHERE id = 1))
       OR cm.course_id = (SELECT course_id FROM "class" WHERE id = (SELECT class_id FROM session WHERE id = 1))
),

-- 8. Learning Outcomes (CLOs)
clo_info AS (
    SELECT
        cscm.course_session_id,
        json_agg(
            json_build_object(
                'clo_id', clo.id,
                'clo_code', clo.code,
                'clo_description', clo.description
            ) ORDER BY clo.code
        ) AS learning_outcomes
    FROM course_session_clo_mapping cscm
    JOIN clo ON cscm.clo_id = clo.id
    WHERE cscm.course_session_id = (SELECT course_session_id FROM session WHERE id = 1)
    GROUP BY cscm.course_session_id
),

-- 9. Current Session Sequence Number
session_sequence AS (
    SELECT
        sess.id,
        ROW_NUMBER() OVER (PARTITION BY sess.class_id ORDER BY sess.date, tst.start_time) AS session_number
    FROM session sess
    JOIN time_slot_template tst ON sess.time_slot_template_id = tst.id
    WHERE sess.class_id = (SELECT class_id FROM session WHERE id = 1)
),

-- 10. Student Feedback for This Phase/Class
student_feedback_info AS (
    SELECT
        sf.student_id,
        (SELECT class_id FROM session WHERE id = 1) as class_id,
        json_agg(
          json_build_object(
            'feedback_id', sf.id,
            'phase_id', sf.phase_id,
            'is_feedback', sf.is_feedback,
            'submitted_at', sf.submitted_at,
            'response_text', sf.response,
            'ratings', (SELECT json_agg(json_build_object('question', fq.question_text, 'rating', sfr.rating)) 
                        FROM student_feedback_response sfr 
                        JOIN feedback_question fq ON sfr.question_id = fq.id
                        WHERE sfr.feedback_id = sf.id)
          )
        ) AS student_feedback
    FROM student_feedback sf
    WHERE sf.student_id = 1 
      AND sf.class_id = (SELECT class_id FROM session WHERE id = 1)
      AND sf.phase_id = (SELECT phase_id FROM course_session WHERE id = (SELECT course_session_id FROM session WHERE id = 1))
    GROUP BY sf.student_id
),

-- 11. Makeup Context (if is_makeup = true)
makeup_context AS (
    SELECT
        sr.makeup_session_id,
        json_build_object(
            'original_session_id', sess_original.id,
            'original_session_date', sess_original.date,
            'original_session_topic', cs_original.topic,
            'original_class_code', c_original.code,
            'absence_request_id', sr.id,
            'absence_reason', sr.request_reason
        ) AS makeup_info
    FROM student_request sr
    JOIN session sess_original ON sr.target_session_id = sess_original.id
    JOIN course_session cs_original ON sess_original.course_session_id = cs_original.id
    JOIN "class" c_original ON sess_original.class_id = c_original.id
    WHERE sr.student_id = 1
      AND sr.makeup_session_id = 1
      AND sr.request_type = 'makeup'
)

-- FINAL ASSEMBLY - STUDENT SESSION DETAIL
SELECT
    -- Core student session
    ssc.student_id,
    ssc.session_id,
    ssc.is_makeup,
    ssc.attendance_status,
    ssc.homework_status,
    ssc.student_note,
    ssc.recorded_at,
    -- Student info
    ssc.student_full_name,
    ssc.student_code,
    ssc.student_level,
    ssc.student_email,
    ssc.student_phone,
    ssc.student_dob,
    ssc.student_gender,
    -- Session details
    sd.session_date,
    sd.session_type,
    sd.session_status,
    sd.teacher_note,
    -- Time slot
    sd.time_slot_name,
    sd.start_time,
    sd.end_time,
    sd.duration_min,
    -- Class info
    sd.class_id,
    sd.class_code,
    sd.class_name,
    sd.modality,
    sd.class_status,
    COALESCE(cec.enrolled_count, 0) AS class_enrolled,
    -- Branch
    sd.branch_name,
    sd.branch_address,
    -- Course
    sd.course_name,
    sd.total_sessions,
    -- Subject & Level
    sd.subject_name,
    sd.level_name,
    -- Course session template
    csi.topic,
    csi.student_task,
    csi.skill_set,
    csi.phase_name,
    csi.phase_description,
    -- Session sequence in class
    ss.session_number,
    -- Teachers (JSON array)
    ti.teachers,
    -- Resources (JSON array)
    ri.resources,
    -- Materials (JSON array)
    mi.materials,
    -- Learning outcomes (JSON array)
    cli.learning_outcomes,
    -- Feedback
    sfi.student_feedback,
    -- Makeup context (if applicable)
    mc.makeup_info
FROM student_session_core ssc
CROSS JOIN session_detail sd
CROSS JOIN course_session_info csi
LEFT JOIN class_enrollment_count cec ON cec.class_id = sd.class_id
LEFT JOIN session_sequence ss ON ss.id = ssc.session_id
LEFT JOIN teachers_info ti ON ti.session_id = ssc.session_id
LEFT JOIN resources_info ri ON ri.session_id = ssc.session_id
LEFT JOIN materials_info mi ON mi.course_session_id = csi.course_session_id
LEFT JOIN clo_info cli ON cli.course_session_id = csi.course_session_id
LEFT JOIN student_feedback_info sfi ON sfi.student_id = ssc.student_id AND sfi.class_id = sd.class_id
LEFT JOIN makeup_context mc ON mc.makeup_session_id = ssc.session_id AND ssc.is_makeup = true;

OUTPUT:
[
  {
    "student_id": 1,
    "session_id": 1,
    "is_makeup": false,
    "attendance_status": "present",
    "homework_status": "completed",
    "student_note": "Active participation",
    "recorded_at": "2025-05-31 00:00:00+00",
    "student_code": "S001",
    "student_level": "Intermediate",
    "student_full_name": "Nguyen Van An",
    "student_email": "student001@gmail.com",
    "student_phone": "+84-911-111-111",
    "student_dob": "1998-03-15",
    "student_gender": null,
    "session_date": "2025-05-30",
    "session_type": "class",
    "session_status": "done",
    "teacher_note": "Great start, students are engaged",
    "session_created_at": "2025-05-30 16:04:33.027916+00",
    "session_updated_at": "2025-05-31 00:00:00+00",
    "time_slot_id": 1,
    "time_slot_name": "Morning Slot 1",
    "start_time": "07:00:00",
    "end_time": "08:30:00",
    "duration_min": 90,
    "class_id": 1,
    "class_code": "A1-GEN-001",
    "class_name": "General English A1 - Morning Class",
    "modality": "offline",
    "class_status": "completed",
    "class_start_date": "2025-05-30",
    "class_planned_end_date": "2025-08-23",
    "class_actual_end_date": "2025-08-23",
    "schedule_days": [
      2,
      4,
      6
    ],
    "class_max_capacity": 15,
    "class_enrolled": 12,
    "class_waitlisted": 0,
    "branch_id": 1,
    "branch_name": "Main Campus",
    "branch_address": "123 Nguyen Trai Street, Thanh Xuan District",
    "branch_phone": "+84-24-3123-4567",
    "course_id": 1,
    "course_code": "ENG-A1-GEN-V1",
    "course_name": "General English A1",
    "duration_weeks": 12,
    "session_per_week": 3,
    "total_hours": 80,
    "hours_per_session": "2.00",
    "total_sessions": 36,
    "subject_id": 1,
    "subject_code": "ENG",
    "subject_name": "English",
    "level_id": 1,
    "level_code": "A1",
    "level_name": "Beginner (A1)",
    "course_session_id": 1,
    "sequence_no": 1,
    "topic": "Greetings and Introductions",
    "student_task": "Practice self-introduction, learn basic greetings",
    "skill_set": "{general,speaking,listening}",
    "phase_id": 1,
    "phase_number": 1,
    "phase_name": "Foundation Phase",
    "phase_description": "Basic greetings, alphabet, numbers, simple present tense",
    "session_number": 1,
    "teachers": [
      {
        "teaching_slot_id": "1-1",
        "teacher_id": 1,
        "teacher_code": "T001",
        "teacher_name": "John Smith",
        "teacher_email": "teacher.john@elc-hanoi.edu.vn",
        "teacher_phone": "+84-903-111-111",
        "skill": "general",
        "status": "completed",
        "teacher_note": "Native speaker, specialized in IELTS Speaking & Writing"
      }
    ],
    "resources": [
      {
        "resource_id": 1,
        "resource_type": "room",
        "resource_name": "Room 101",
        "location": "Floor 1",
        "capacity": 10,
        "meeting_url": null,
        "meeting_id": null,
        "description": "Small classroom for intensive courses"
      }
    ],
    "materials": [
      {
        "material_id": 3,
        "title": "Greetings Vocabulary List",
        "url": "https://drive.google.com/file/greetings-vocab.pdf",
        "uploaded_by": "Nguyen Thi English Leader",
        "created_at": "2025-03-01T16:04:33.027916+00:00"
      },
      {
        "material_id": 4,
        "title": "Self-Introduction Practice Worksheet",
        "url": "https://drive.google.com/file/self-intro-worksheet.pdf",
        "uploaded_by": "Nguyen Thi English Leader",
        "created_at": "2025-03-01T16:04:33.027916+00:00"
      }
    ],
    "learning_outcomes": [
      {
        "clo_id": 1,
        "clo_code": "CLO-A1-01",
        "clo_description": "Use basic greetings and introduce themselves in English"
      },
      {
        "clo_id": 3,
        "clo_code": "CLO-A1-03",
        "clo_description": "Ask and answer basic personal questions about name, age, nationality, and occupation"
      }
    ],
    "student_feedback": null,
    "makeup_info": null
  }
]


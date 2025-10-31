Học viên xem syllabus của lớp đang theo học

-- CLASS DETAIL WITH FULL SYLLABUS FOR STUDENT (Corrected for new schema)
-- Purpose: Get complete class information including course structure, 
--          phases, sessions, materials, CLOs, PLOs for a student's class

-- Parameters: @student_id (e.g., 1) and @class_id (e.g., 1)
-- Note: Parameters have been updated to reflect the seed data. 
-- Student with id=13 does not exist, changed to student_id=1.
-- Class with id=3 is a FOUNDATION class, but student 13 was intermediate.
-- Changed to class_id=1 to match student's enrollment.

WITH student_class AS (
    -- Get student's enrollment info
    SELECT 
        e.id AS enrollment_id,
        e.class_id,
        e.student_id,
        e.status AS enrollment_status,
        e.enrolled_at,
        e.join_session_id,
        s.student_code,
        s.level AS student_level,
        ua.full_name AS student_name,
        ua.email AS student_email
    FROM enrollment e
    JOIN student s ON e.student_id = s.id
    JOIN user_account ua ON s.user_id = ua.id
    WHERE e.student_id = 1  -- @student_id
      AND e.class_id = 1     -- @class_id
),
class_info AS (
    -- Get class basic information
    SELECT 
        c.id AS class_id,
        c.code AS class_code,
        c.name AS class_name,
        c.modality,
        c.start_date,
        c.planned_end_date,
        c.actual_end_date,
        c.schedule_days,
        c.max_capacity,
        c.status AS class_status,
        c.course_id,
        b.id AS branch_id,
        b.name AS branch_name,
        b.address AS branch_address,
        cnt.name AS center_name
    FROM "class" c
    JOIN branch b ON c.branch_id = b.id
    JOIN center cnt ON b.center_id = cnt.id
    WHERE c.id = 1  -- @class_id
),
course_info AS (
    -- Get course information
    SELECT 
        co.id AS course_id,
        co.code AS course_code,
        co.name AS course_name,
        co.description AS course_description,
        co.total_hours,
        co.duration_weeks,
        co.session_per_week,
        co.hours_per_session,
        co.prerequisites,
        co.target_audience,
        co.teaching_methods,
        co.effective_date,
        subj.code AS subject_code,
        subj.name AS subject_name,
        lvl.code AS level_code,
        lvl.name AS level_name,
        lvl.expected_duration_hours
    FROM course co
    JOIN subject subj ON co.subject_id = subj.id
    LEFT JOIN level lvl ON co.level_id = lvl.id
    WHERE co.id IN (SELECT course_id FROM class_info)
),
course_phases_with_sessions AS (
    -- Get all phases and their sessions
    SELECT 
        cp.id AS phase_id,
        cp.course_id,
        cp.phase_number,
        cp.name AS phase_name,
        cp.duration_weeks AS phase_duration_weeks,
        cp.learning_focus,
        cs.id AS course_session_id,
        cs.sequence_no,
        cs.topic,
        cs.student_task,
        cs.skill_set
    FROM course_phase cp
    LEFT JOIN course_session cs ON cp.id = cs.phase_id
    WHERE cp.course_id IN (SELECT course_id FROM class_info)
),
session_clos AS (
    -- Get CLOs mapped to each course session
    SELECT 
        cscm.course_session_id,
        json_agg(
            json_build_object(
                'clo_id', clo.id,
                'clo_code', clo.code,
                'clo_description', clo.description,
                'mapping_status', cscm.status
            ) ORDER BY clo.code
        ) AS clos
    FROM course_session_clo_mapping cscm
    JOIN clo ON cscm.clo_id = clo.id
    WHERE cscm.course_session_id IN (
        SELECT course_session_id FROM course_phases_with_sessions WHERE course_session_id IS NOT NULL
    )
    GROUP BY cscm.course_session_id
),
course_clos_with_plos AS (
    -- Get all CLOs for the course with their mapped PLOs
    SELECT 
        clo.id AS clo_id,
        clo.code AS clo_code,
        clo.description AS clo_description,
        json_agg(
            DISTINCT jsonb_build_object(
                'plo_id', plo.id,
                'plo_code', plo.code,
                'plo_description', plo.description,
                'mapping_status', pcm.status
            )
        ) FILTER (WHERE plo.id IS NOT NULL) AS plos
    FROM clo
    LEFT JOIN plo_clo_mapping pcm ON clo.id = pcm.clo_id
    LEFT JOIN plo ON pcm.plo_id = plo.id
    WHERE clo.course_id IN (SELECT course_id FROM class_info)
    GROUP BY clo.id, clo.code, clo.description
),
course_materials AS (
    -- Get all materials organized by course/phase/session
    SELECT 
        cm.id AS material_id,
        cm.course_id,
        cm.phase_id,
        cm.course_session_id,
        cm.title AS material_title,
        cm.url AS material_url,
        cm.uploaded_at AS material_uploaded_at,
        ua.full_name AS uploaded_by_name
    FROM course_material cm
    LEFT JOIN user_account ua ON cm.uploaded_by = ua.id
    WHERE cm.course_id IN (SELECT course_id FROM class_info)
),
actual_sessions AS (
    -- Get actual sessions created for this class
    SELECT 
        s.id AS session_id,
        s.class_id,
        s.course_session_id,
        s.date,
        s.status AS session_status,
        s.type AS session_type,
        s.teacher_note,
        tst.start_time,
        tst.end_time,
        tst.name AS time_slot_name,
        -- Get assigned teachers
        (
            SELECT json_agg(
                json_build_object(
                    'teacher_id', t.id,
                    'employee_code', t.employee_code,
                    'teacher_name', ua.full_name,
                    'status', ts.status
                )
            )
            FROM teaching_slot ts
            JOIN teacher t ON ts.teacher_id = t.id
            JOIN user_account ua ON t.user_account_id = ua.id
            WHERE ts.session_id = s.id
        ) AS teachers,
        -- Get assigned resources
        (
            SELECT json_agg(
                json_build_object(
                    'resource_id', r.id,
                    'resource_name', r.name,
                    'resource_type', r.resource_type,
                    'description', r.description,
                    'meeting_url', r.meeting_url
                )
            )
            FROM session_resource sr
            JOIN resource r ON sr.resource_id = r.id
            WHERE sr.session_id = s.id
        ) AS resources
    FROM session s
    LEFT JOIN time_slot_template tst ON s.time_slot_template_id = tst.id
    WHERE s.class_id = 1  -- @class_id
      AND s.type = 'class'  -- Only regular class sessions, not makeup/exam
)

-- MAIN QUERY: Combine everything
SELECT 
    -- Student Info
    sc.student_id,
    sc.student_code,
    sc.student_name,
    sc.student_email,
    sc.student_level,
    sc.enrollment_status,
    sc.enrolled_at,
    
    -- Class Info
    ci.class_id,
    ci.class_code,
    ci.class_name,
    ci.modality,
    ci.start_date,
    ci.planned_end_date,
    ci.actual_end_date,
    ci.schedule_days,
    ci.max_capacity,
    ci.class_status,
    ci.branch_name,
    ci.branch_address,
    ci.center_name,
    
    -- Course Info
    coi.course_code,
    coi.course_name,
    coi.course_description,
    coi.total_hours,
    coi.duration_weeks,
    coi.session_per_week,
    coi.hours_per_session,
    coi.prerequisites,
    coi.target_audience,
    coi.teaching_methods,
    coi.subject_code,
    coi.subject_name,
    coi.level_code,
    coi.level_name,
    
    -- Course Structure: Phases with Sessions
    (
        SELECT json_agg(
            json_build_object(
                'phase_id', cpws.phase_id,
                'phase_number', cpws.phase_number,
                'phase_name', cpws.phase_name,
                'phase_duration_weeks', cpws.phase_duration_weeks,
                'learning_focus', cpws.learning_focus,
                'sessions', (
                    SELECT json_agg(
                        json_build_object(
                            'course_session_id', s.course_session_id,
                            'sequence_no', s.sequence_no,
                            'topic', s.topic,
                            'student_task', s.student_task,
                            'skill_set', s.skill_set,
                            'clos', COALESCE(sclo.clos, '[]'::json),
                            'materials', (
                                SELECT json_agg(
                                    json_build_object(
                                        'material_id', cm.material_id,
                                        'title', cm.material_title,
                                        'url', cm.material_url,
                                        'uploaded_at', cm.material_uploaded_at,
                                        'uploaded_by', cm.uploaded_by_name
                                    )
                                )
                                FROM course_materials cm
                                WHERE cm.course_session_id = s.course_session_id
                            ),
                            'actual_session', (
                                SELECT json_build_object(
                                    'session_id', acs.session_id,
                                    'date', acs.date,
                                    'start_time', acs.start_time,
                                    'end_time', acs.end_time,
                                    'time_slot_name', acs.time_slot_name,
                                    'status', acs.session_status,
                                    'type', acs.session_type,
                                    'teacher_note', acs.teacher_note,
                                    'teachers', acs.teachers,
                                    'resources', acs.resources
                                )
                                FROM actual_sessions acs
                                WHERE acs.course_session_id = s.course_session_id
                                LIMIT 1  -- Should be only one per course_session_id per class
                            )
                        ) ORDER BY s.sequence_no
                    )
                    FROM course_phases_with_sessions s
                    LEFT JOIN session_clos sclo ON s.course_session_id = sclo.course_session_id
                    WHERE s.phase_id = cpws.phase_id
                      AND s.course_session_id IS NOT NULL
                )
            ) ORDER BY cpws.phase_number
        )
        FROM (
            SELECT DISTINCT ON (phase_id) 
                phase_id, course_id, phase_number, phase_name, 
                phase_duration_weeks, learning_focus
            FROM course_phases_with_sessions
        ) cpws
    ) AS course_phases,
    
    -- All CLOs with PLO mappings
    (
        SELECT json_agg(
            json_build_object(
                'clo_id', ccwp.clo_id,
                'clo_code', ccwp.clo_code,
                'clo_description', ccwp.clo_description,
                'plos', ccwp.plos
            ) ORDER BY ccwp.clo_code
        )
        FROM course_clos_with_plos ccwp
    ) AS course_learning_outcomes,
    
    -- General course materials (not tied to specific session)
    (
        SELECT json_agg(
            json_build_object(
                'material_id', cm.material_id,
                'title', cm.material_title,
                'url', cm.material_url,
                'uploaded_at', cm.material_uploaded_at,
                'uploaded_by', cm.uploaded_by_name
            )
        )
        FROM course_materials cm
        WHERE cm.course_session_id IS NULL
          AND cm.phase_id IS NULL
    ) AS general_course_materials,
    
    -- Phase-level materials
    (
        SELECT json_agg(
            json_build_object(
                'phase_id', cm.phase_id,
                'material_id', cm.material_id,
                'title', cm.material_title,
                'url', cm.material_url,
                'uploaded_at', cm.material_uploaded_at,
                'uploaded_by', cm.uploaded_by_name
            )
        )
        FROM course_materials cm
        WHERE cm.course_session_id IS NULL
          AND cm.phase_id IS NOT NULL
    ) AS phase_materials

FROM student_class sc
CROSS JOIN class_info ci
CROSS JOIN course_info coi;


OUTPUT:
[
  {
    "student_id": 13,
    "student_code": "S013",
    "student_name": "Ly Van Kien",
    "student_email": "student013@gmail.com",
    "student_level": "Beginner",
    "enrollment_status": "enrolled",
    "enrolled_at": "2025-09-02 16:04:33.027916+00",
    "class_id": 3,
    "class_code": "B1-IELTS-001",
    "class_name": "IELTS Foundation B1 - Afternoon",
    "modality": "offline",
    "start_date": "2025-09-07",
    "planned_end_date": "2025-12-28",
    "actual_end_date": null,
    "schedule_days": [
      2,
      4,
      6
    ],
    "max_capacity": 18,
    "class_status": "ongoing",
    "branch_name": "Main Campus",
    "branch_address": "123 Nguyen Trai Street, Thanh Xuan District",
    "center_name": "English Language Center Hanoi",
    "course_code": "ENG-B1-IELTS-V1",
    "course_name": "IELTS Foundation (B1)",
    "course_description": "IELTS preparation for intermediate learners targeting band 5.0-6.0",
    "total_hours": 120,
    "duration_weeks": 16,
    "session_per_week": 3,
    "hours_per_session": "2.50",
    "prerequisites": "Completed A2 or equivalent (IELTS 4.0-4.5)",
    "target_audience": "Students preparing for IELTS exam, university applicants",
    "teaching_methods": "IELTS-focused tasks, mock tests, skill-building exercises",
    "subject_code": "ENG",
    "subject_name": "English",
    "level_code": "B1",
    "level_name": "Intermediate (B1)",
    "standard_type": "CEFR",
    "course_phases": [
      {
        "phase_id": 4,
        "phase_number": 1,
        "phase_name": "IELTS Introduction & Listening",
        "phase_duration_weeks": 5,
        "learning_focus": "IELTS format, Listening strategies, note-taking skills",
        "phase_sort_order": 1,
        "sessions": [
          {
            "course_session_id": 13,
            "sequence_no": 1,
            "topic": "IELTS Overview & Test Format",
            "student_task": "Understand IELTS structure, scoring system",
            "skill_set": [
              "general"
            ],
            "clos": [
              {
                "clo_id": 6,
                "clo_code": "CLO-IELTS-B1-01",
                "clo_description": "Apply effective listening strategies to understand IELTS listening passages",
                "mapping_status": "active"
              },
              {
                "clo_id": 11,
                "clo_code": "CLO-IELTS-B1-06",
                "clo_description": "Manage time effectively during IELTS test sections",
                "mapping_status": "active"
              }
            ],
            "materials": [
              {
                "material_id": 8,
                "title": "IELTS Test Format Guide",
                "url": "https://drive.google.com/file/ielts-format.pdf",
                "uploaded_at": "2025-03-06T16:04:33.027916+00:00",
                "uploaded_by": "Nguyen Thi English Leader"
              }
            ],
            "actual_session": {
              "session_id": 37,
              "date": "2025-09-07",
              "start_time": "13:00:00",
              "end_time": "14:30:00",
              "time_slot_name": "Afternoon Slot 1",
              "status": "done",
              "type": "class",
              "teacher_note": "Students enthusiastic about IELTS",
              "teachers": [
                {
                  "teacher_id": 4,
                  "employee_code": "T004",
                  "teacher_name": "Emily Davis",
                  "status": "completed"
                },
                {
                  "teacher_id": 7,
                  "employee_code": "T007",
                  "teacher_name": "Le Thi Mai",
                  "status": "completed"
                }
              ],
              "resources": [
                {
                  "resource_id": 4,
                  "resource_name": "Room 201",
                  "resource_type": "room",
                  "location": "Floor 2",
                  "meeting_url": null
                }
              ]
            }
          },
          {
            "course_session_id": 14,
            "sequence_no": 2,
            "topic": "Listening Section 1 - Forms & Details",
            "student_task": "Practice form completion, note-taking",
            "skill_set": [
              "listening"
            ],
            "clos": [
              {
                "clo_id": 6,
                "clo_code": "CLO-IELTS-B1-01",
                "clo_description": "Apply effective listening strategies to understand IELTS listening passages",
                "mapping_status": "active"
              }
            ],
            "materials": [
              {
                "material_id": 9,
                "title": "Listening Section 1 Practice Questions",
                "url": "https://drive.google.com/file/listening-s1-practice.pdf",
                "uploaded_at": "2025-03-07T16:04:33.027916+00:00",
                "uploaded_by": "Nguyen Thi English Leader"
              }
            ],
            "actual_session": {
              "session_id": 38,
              "date": "2025-09-09",
              "start_time": "13:00:00",
              "end_time": "14:30:00",
              "time_slot_name": "Afternoon Slot 1",
              "status": "done",
              "type": "class",
              "teacher_note": null,
              "teachers": [
                {
                  "teacher_id": 4,
                  "employee_code": "T004",
                  "teacher_name": "Emily Davis",
                  "status": "completed"
                }
              ],
              "resources": [
                {
                  "resource_id": 4,
                  "resource_name": "Room 201",
                  "resource_type": "room",
                  "location": "Floor 2",
                  "meeting_url": null
                }
              ]
            }
          },
          {
            "course_session_id": 15,
            "sequence_no": 3,
            "topic": "Listening Section 2 - Monologues",
            "student_task": "Practice with guided tours, presentations",
            "skill_set": [
              "listening"
            ],
            "clos": [
              {
                "clo_id": 6,
                "clo_code": "CLO-IELTS-B1-01",
                "clo_description": "Apply effective listening strategies to understand IELTS listening passages",
                "mapping_status": "active"
              }
            ],
            "materials": null,
            "actual_session": {
              "session_id": 39,
              "date": "2025-09-11",
              "start_time": "13:00:00",
              "end_time": "14:30:00",
              "time_slot_name": "Afternoon Slot 1",
              "status": "done",
              "type": "class",
              "teacher_note": null,
              "teachers": [
                {
                  "teacher_id": 4,
                  "employee_code": "T004",
                  "teacher_name": "Emily Davis",
                  "status": "completed"
                }
              ],
              "resources": [
                {
                  "resource_id": 4,
                  "resource_name": "Room 201",
                  "resource_type": "room",
                  "location": "Floor 2",
                  "meeting_url": null
                }
              ]
            }
          },
          {
            "course_session_id": 16,
            "sequence_no": 4,
            "topic": "Listening Section 3 - Conversations",
            "student_task": "Practice academic discussions",
            "skill_set": [
              "listening"
            ],
            "clos": [
              {
                "clo_id": 6,
                "clo_code": "CLO-IELTS-B1-01",
                "clo_description": "Apply effective listening strategies to understand IELTS listening passages",
                "mapping_status": "active"
              }
            ],
            "materials": null,
            "actual_session": {
              "session_id": 40,
              "date": "2025-09-14",
              "start_time": "13:00:00",
              "end_time": "14:30:00",
              "time_slot_name": "Afternoon Slot 1",
              "status": "done",
              "type": "class",
              "teacher_note": null,
              "teachers": null,
              "resources": null
            }
          },
          {
            "course_session_id": 17,
            "sequence_no": 5,
            "topic": "Listening Section 4 - Lectures",
            "student_task": "Practice with academic lectures",
            "skill_set": [
              "listening"
            ],
            "clos": [
              {
                "clo_id": 6,
                "clo_code": "CLO-IELTS-B1-01",
                "clo_description": "Apply effective listening strategies to understand IELTS listening passages",
                "mapping_status": "active"
              }
            ],
            "materials": null,
            "actual_session": {
              "session_id": 41,
              "date": "2025-09-16",
              "start_time": "13:00:00",
              "end_time": "14:30:00",
              "time_slot_name": "Afternoon Slot 1",
              "status": "done",
              "type": "class",
              "teacher_note": null,
              "teachers": null,
              "resources": null
            }
          },
          {
            "course_session_id": 18,
            "sequence_no": 6,
            "topic": "Listening Practice Test 1",
            "student_task": "Full listening test with timing",
            "skill_set": [
              "listening"
            ],
            "clos": [
              {
                "clo_id": 6,
                "clo_code": "CLO-IELTS-B1-01",
                "clo_description": "Apply effective listening strategies to understand IELTS listening passages",
                "mapping_status": "active"
              },
              {
                "clo_id": 11,
                "clo_code": "CLO-IELTS-B1-06",
                "clo_description": "Manage time effectively during IELTS test sections",
                "mapping_status": "active"
              }
            ],
            "materials": [
              {
                "material_id": 10,
                "title": "Listening Practice Test 1 Audio & Questions",
                "url": "https://drive.google.com/file/listening-test1.zip",
                "uploaded_at": "2025-03-09T16:04:33.027916+00:00",
                "uploaded_by": "Nguyen Thi English Leader"
              }
            ],
            "actual_session": {
              "session_id": 42,
              "date": "2025-09-18",
              "start_time": "13:00:00",
              "end_time": "14:30:00",
              "time_slot_name": "Afternoon Slot 1",
              "status": "done",
              "type": "class",
              "teacher_note": "First mock test - good progress",
              "teachers": null,
              "resources": null
            }
          },
          {
            "course_session_id": 19,
            "sequence_no": 7,
            "topic": "Listening Strategies - Prediction",
            "student_task": "Learn to predict answers",
            "skill_set": [
              "listening"
            ],
            "clos": [],
            "materials": null,
            "actual_session": {
              "session_id": 43,
              "date": "2025-09-21",
              "start_time": "13:00:00",
              "end_time": "14:30:00",
              "time_slot_name": "Afternoon Slot 1",
              "status": "done",
              "type": "class",
              "teacher_note": null,
              "teachers": null,
              "resources": null
            }
          },
          {
            "course_session_id": 20,
            "sequence_no": 8,
            "topic": "Listening Strategies - Keywords",
            "student_task": "Identify keywords and paraphrasing",
            "skill_set": [
              "listening"
            ],
            "clos": [],
            "materials": null,
            "actual_session": {
              "session_id": 44,
              "date": "2025-09-23",
              "start_time": "13:00:00",
              "end_time": "14:30:00",
              "time_slot_name": "Afternoon Slot 1",
              "status": "done",
              "type": "class",
              "teacher_note": null,
              "teachers": null,
              "resources": null
            }
          },
          {
            "course_session_id": 21,
            "sequence_no": 9,
            "topic": "Numbers, Dates, Spellings",
            "student_task": "Master number dictation",
            "skill_set": [
              "listening"
            ],
            "clos": [],
            "materials": null,
            "actual_session": {
              "session_id": 45,
              "date": "2025-09-25",
              "start_time": "13:00:00",
              "end_time": "14:30:00",
              "time_slot_name": "Afternoon Slot 1",
              "status": "done",
              "type": "class",
              "teacher_note": null,
              "teachers": null,
              "resources": null
            }
          },
          {
            "course_session_id": 22,
            "sequence_no": 10,
            "topic": "Multiple Choice Strategies",
            "student_task": "Practice multiple choice questions",
            "skill_set": [
              "listening"
            ],
            "clos": [],
            "materials": null,
            "actual_session": {
              "session_id": 46,
              "date": "2025-09-28",
              "start_time": "13:00:00",
              "end_time": "14:30:00",
              "time_slot_name": "Afternoon Slot 1",
              "status": "done",
              "type": "class",
              "teacher_note": null,
              "teachers": null,
              "resources": null
            }
          },
          {
            "course_session_id": 23,
            "sequence_no": 11,
            "topic": "Matching and Labeling",
            "student_task": "Practice diagram/map labeling",
            "skill_set": [
              "listening"
            ],
            "clos": [],
            "materials": null,
            "actual_session": {
              "session_id": 47,
              "date": "2025-09-30",
              "start_time": "13:00:00",
              "end_time": "14:30:00",
              "time_slot_name": "Afternoon Slot 1",
              "status": "done",
              "type": "class",
              "teacher_note": null,
              "teachers": null,
              "resources": null
            }
          },
          {
            "course_session_id": 24,
            "sequence_no": 12,
            "topic": "Sentence Completion",
            "student_task": "Practice sentence/summary completion",
            "skill_set": [
              "listening"
            ],
            "clos": [],
            "materials": null,
            "actual_session": {
              "session_id": 48,
              "date": "2025-10-02",
              "start_time": "13:00:00",
              "end_time": "14:30:00",
              "time_slot_name": "Afternoon Slot 1",
              "status": "done",
              "type": "class",
              "teacher_note": null,
              "teachers": null,
              "resources": null
            }
          },
          {
            "course_session_id": 25,
            "sequence_no": 13,
            "topic": "Listening Practice Test 2",
            "student_task": "Full listening mock test",
            "skill_set": [
              "listening"
            ],
            "clos": [],
            "materials": null,
            "actual_session": {
              "session_id": 49,
              "date": "2025-10-05",
              "start_time": "13:00:00",
              "end_time": "14:30:00",
              "time_slot_name": "Afternoon Slot 1",
              "status": "done",
              "type": "class",
              "teacher_note": null,
              "teachers": null,
              "resources": null
            }
          },
          {
            "course_session_id": 26,
            "sequence_no": 14,
            "topic": "Review Common Mistakes",
            "student_task": "Analyze errors, improvement strategies",
            "skill_set": [
              "listening"
            ],
            "clos": [],
            "materials": null,
            "actual_session": {
              "session_id": 50,
              "date": "2025-10-07",
              "start_time": "13:00:00",
              "end_time": "14:30:00",
              "time_slot_name": "Afternoon Slot 1",
              "status": "done",
              "type": "class",
              "teacher_note": null,
              "teachers": null,
              "resources": null
            }
          },
          {
            "course_session_id": 27,
            "sequence_no": 15,
            "topic": "Phase 1 Listening Assessment",
            "student_task": "Phase assessment and feedback",
            "skill_set": [
              "listening"
            ],
            "clos": [],
            "materials": null,
            "actual_session": {
              "session_id": 51,
              "date": "2025-10-09",
              "start_time": "13:00:00",
              "end_time": "14:30:00",
              "time_slot_name": "Afternoon Slot 1",
              "status": "done",
              "type": "class",
              "teacher_note": "Phase 1 completed well",
              "teachers": null,
              "resources": null
            }
          }
        ]
      },
      {
        "phase_id": 5,
        "phase_number": 2,
        "phase_name": "Reading & Vocabulary Building",
        "phase_duration_weeks": 6,
        "learning_focus": "Reading techniques, skimming, scanning, academic vocabulary",
        "phase_sort_order": 2,
        "sessions": null
      },
      {
        "phase_id": 6,
        "phase_number": 3,
        "phase_name": "Speaking & Writing Foundation",
        "phase_duration_weeks": 5,
        "learning_focus": "Speaking parts 1-3, Task 1&2 introduction, essay structure",
        "phase_sort_order": 3,
        "sessions": null
      }
    ],
    "course_learning_outcomes": [
      {
        "clo_id": 6,
        "clo_code": "CLO-IELTS-B1-01",
        "clo_description": "Apply effective listening strategies to understand IELTS listening passages",
        "plos": [
          {
            "plo_id": 1,
            "plo_code": "PLO-ENG-01",
            "mapping_status": "active",
            "plo_description": "Demonstrate proficiency in English communication across listening, speaking, reading, and writing skills"
          },
          {
            "plo_id": 5,
            "plo_code": "PLO-ENG-05",
            "mapping_status": "active",
            "plo_description": "Apply English language skills to achieve personal, academic, and professional goals"
          }
        ]
      },
      {
        "clo_id": 7,
        "clo_code": "CLO-IELTS-B1-02",
        "clo_description": "Use reading techniques (skimming, scanning) to comprehend IELTS reading passages",
        "plos": [
          {
            "plo_id": 1,
            "plo_code": "PLO-ENG-01",
            "mapping_status": "active",
            "plo_description": "Demonstrate proficiency in English communication across listening, speaking, reading, and writing skills"
          },
          {
            "plo_id": 2,
            "plo_code": "PLO-ENG-02",
            "mapping_status": "active",
            "plo_description": "Apply critical thinking and analytical skills to interpret and evaluate various types of texts"
          }
        ]
      },
      {
        "clo_id": 8,
        "clo_code": "CLO-IELTS-B1-03",
        "clo_description": "Speak fluently and coherently on familiar topics in IELTS speaking test format",
        "plos": [
          {
            "plo_id": 1,
            "plo_code": "PLO-ENG-01",
            "mapping_status": "active",
            "plo_description": "Demonstrate proficiency in English communication across listening, speaking, reading, and writing skills"
          },
          {
            "plo_id": 3,
            "plo_code": "PLO-ENG-03",
            "mapping_status": "active",
            "plo_description": "Communicate effectively in professional and academic contexts using appropriate language and conventions"
          }
        ]
      },
      {
        "clo_id": 9,
        "clo_code": "CLO-IELTS-B1-04",
        "clo_description": "Write well-structured essays and reports for IELTS Task 1 and Task 2",
        "plos": [
          {
            "plo_id": 1,
            "plo_code": "PLO-ENG-01",
            "mapping_status": "active",
            "plo_description": "Demonstrate proficiency in English communication across listening, speaking, reading, and writing skills"
          },
          {
            "plo_id": 2,
            "plo_code": "PLO-ENG-02",
            "mapping_status": "active",
            "plo_description": "Apply critical thinking and analytical skills to interpret and evaluate various types of texts"
          },
          {
            "plo_id": 3,
            "plo_code": "PLO-ENG-03",
            "mapping_status": "active",
            "plo_description": "Communicate effectively in professional and academic contexts using appropriate language and conventions"
          }
        ]
      },
      {
        "clo_id": 10,
        "clo_code": "CLO-IELTS-B1-05",
        "clo_description": "Expand academic vocabulary relevant to common IELTS topics",
        "plos": [
          {
            "plo_id": 5,
            "plo_code": "PLO-ENG-05",
            "mapping_status": "active",
            "plo_description": "Apply English language skills to achieve personal, academic, and professional goals"
          }
        ]
      },
      {
        "clo_id": 11,
        "clo_code": "CLO-IELTS-B1-06",
        "clo_description": "Manage time effectively during IELTS test sections",
        "plos": [
          {
            "plo_id": 5,
            "plo_code": "PLO-ENG-05",
            "mapping_status": "active",
            "plo_description": "Apply English language skills to achieve personal, academic, and professional goals"
          }
        ]
      }
    ],
    "general_course_materials": null,
    "phase_materials": [
      {
        "phase_id": 4,
        "material_id": 7,
        "title": "IELTS Foundation B1 Course Overview",
        "url": "https://drive.google.com/file/ielts-b1-overview.pdf",
        "uploaded_at": "2025-03-06T16:04:33.027916+00:00",
        "uploaded_by": "Nguyen Thi English Leader"
      },
      {
        "phase_id": 4,
        "material_id": 11,
        "title": "IELTS Vocabulary Bank - Common Topics",
        "url": "https://drive.google.com/file/ielts-vocab.xlsx",
        "uploaded_at": "2025-03-11T16:04:33.027916+00:00",
        "uploaded_by": "Nguyen Thi English Leader"
      }
    ]
  }
]


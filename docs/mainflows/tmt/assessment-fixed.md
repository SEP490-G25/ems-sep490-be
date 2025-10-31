- Luồng nhập điểm đánh giá:
Actors: Teacher, System
Mô tả: Teacher nhập hoặc import điểm cho học viên qua Assessments.
Bảng liên quan:
assessment, score, student, class, enrollment, teaching_slot, session, course_assessment


1. Teacher mở “Assessments”
teaching_slot session class course 
 → System load danh sách lớp mà teacher dạy:
SELECT DISTINCT c.id, c.code, c.name, co.name AS course_name
FROM teaching_slot ts
JOIN session s ON ts.session_id = s.id
JOIN "class" c ON s.class_id = c.id
JOIN course co ON c.course_id = co.id
WHERE ts.teacher_id = :teacher_id -- 1
AND c.status IN ('ongoing','completed');



2. Teacher chọn lớp
assessment score course_assessment
 → System load các assessment của lớp: 
SELECT 
  a.id, 
  ca.name, 
  ca.kind, 
  ca.max_score,
  COUNT(DISTINCT sc.student_id) AS graded_count
FROM assessment a
JOIN course_assessment ca ON a.course_assessment_id = ca.id
LEFT JOIN score sc ON sc.assessment_id = a.id
WHERE a.class_id = :class_id -- 3
GROUP BY a.id, ca.name, ca.kind, ca.max_score;




3. Teacher chọn assessment (Quiz / Midterm / Final)
enrollment student user_account score 
 → System load danh sách học viên:
SELECT 
  st.id, 
  st.student_code, 
  ua.full_name,
  sc.score, 
  sc.feedback, 
  sc.graded_at
FROM enrollment e
JOIN student st ON e.student_id = st.id
JOIN user_account ua ON ua.id = st.user_id
LEFT JOIN score sc ON sc.assessment_id = :assessment_id -- 1
                  AND sc.student_id = st.id
WHERE e.class_id = :class_id -- 1
  AND e.status = 'enrolled';


4. Teacher nhập điểm & feedback
	- Điểm: 0 ≤ score ≤ max_score 
- Feedback: tùy chọn


5. Teacher click “Lưu điểm”
 → System thực hiện UPSERT:


 INSERT INTO score (assessment_id, student_id, score, feedback, graded_by, graded_at)
VALUES (:assessment_id, :student_id, :score, :feedback, :teacher_id, NOW())
ON CONFLICT (assessment_id, student_id) DO UPDATE
  SET score = EXCLUDED.score,
      feedback = EXCLUDED.feedback,
      graded_by = EXCLUDED.graded_by,
      graded_at = NOW();

Validation:
	- score <= max_score AND score >= 0 
- SUM(weight) ≤ 100%


Alternative Flow (Import CSV):
 Teacher upload file (student_code, score, feedback) → System parse & validate → Bulk UPSERT vào score.
Result:
 Điểm được lưu vào bảng score, có thể xem trong báo cáo điểm & theo dõi tiến độ chấm.

---
FLOW: Student View Scores (Xem điểm của tôi)
Actors: Student, System
Mô tả: Học viên xem điểm các bài kiểm tra của lớp mình đang/đã học, theo Ngày / Phase / Toàn khóa, kèm trạng thái đã nộp và điểm trung bình (weighted).
Bảng liên quan: enrollment, "class", course, assessment, score, student, user_account, session, course_session, course_phase, course_assessment
Flow Steps
1. Mở “My Scores” → Chọn lớp (class)

-- Lấy student_id từ user đang đăng nhập
SELECT st.id AS student_id
FROM student st
JOIN user_account ua ON ua.id = st.user_id
WHERE ua.id = :student_user_id; -- 100 (user_id) -> student_id = 1



-- Danh sách lớp học viên đang/đã học
SELECT DISTINCT c.id, c.code, c.name, co.name AS course_name, c.status
FROM enrollment e
JOIN "class" c ON c.id = e.class_id
JOIN course co ON co.id = c.course_id
WHERE e.student_id = :student_id -- 1
  AND e.status IN ('enrolled','transferred','dropped') -- Lấy cả lớp đã từng học
ORDER BY c.name;


2) Chọn phạm vi (scope)
	2a) Theo Ngày (dựa vào ngày thi thực tế)
SELECT DISTINCT DATE(a.actual_date) AS target_date
FROM assessment a
JOIN score sc ON sc.assessment_id = a.id
WHERE a.class_id = :class_id -- 1
  AND sc.student_id = :student_id -- 1
  AND a.actual_date IS NOT NULL
ORDER BY target_date DESC;


	2b) Theo Phase
SELECT cp.id AS phase_id, cp.name AS phase_name, cp.phase_number, cp.duration_weeks
FROM course_phase cp
JOIN course c2 ON c2.id = cp.course_id
JOIN "class" c ON c.course_id = c2.id
WHERE c.id = :class_id -- 1
ORDER BY cp.phase_number;



3) Xem chi tiết bài & điểm 
	3a) Theo Ngày
SELECT 
  a.id, 
  ca.name, 
  ca.kind, 
  ca.max_score,
  sc.score, 
  sc.feedback, 
  sc.graded_at,
  CASE 
    WHEN sc.id IS NULL THEN 'not_submitted' 
    ELSE 'submitted' 
  END AS submit_status
FROM assessment a
JOIN course_assessment ca ON a.course_assessment_id = ca.id
LEFT JOIN score sc 
  ON sc.assessment_id = a.id 
 AND sc.student_id = :student_id -- 1
WHERE a.class_id = :class_id -- 1
  AND DATE(a.actual_date) = :target_date   -- '2025-12-02'
ORDER BY ca.kind, ca.name;


3b) Theo Phase

WITH phase_window AS (
  SELECT 
    MIN(s.date) AS start_date, 
    MAX(s.date) AS end_date
  FROM session s
  JOIN course_session cs ON cs.id = s.course_session_id
  WHERE s.class_id = :class_id -- 1
    AND cs.phase_id = :phase_id -- 1
)
SELECT 
  a.id, 
  ca.name, 
  ca.kind, 
  ca.max_score,
  sc.score, 
  sc.feedback, 
  sc.graded_at,
  CASE 
    WHEN sc.id IS NULL THEN 'not_submitted' 
    ELSE 'submitted' 
  END AS submit_status
FROM assessment a
JOIN course_assessment ca ON a.course_assessment_id = ca.id
LEFT JOIN score sc 
  ON sc.assessment_id = a.id 
 AND sc.student_id = :student_id -- 1
CROSS JOIN phase_window pw
WHERE a.class_id = :class_id -- 1
  AND a.actual_date IS NOT NULL
  AND DATE(a.actual_date) BETWEEN pw.start_date AND pw.end_date
ORDER BY a.actual_date, ca.kind, ca.name;



4) Tổng hợp điểm trung bình (weighted) cho một học viên
	4a) Theo Phase
WITH phase_window AS (
  SELECT 
    MIN(s.date) AS start_date, 
    MAX(s.date) AS end_date
  FROM session s
  JOIN course_session cs ON cs.id = s.course_session_id
  WHERE s.class_id = :class_id -- 1
    AND cs.phase_id = :phase_id -- 1
),
asses AS (
  SELECT a.id, ca.max_score
  FROM assessment a
  JOIN course_assessment ca ON a.course_assessment_id = ca.id
  CROSS JOIN phase_window pw
  WHERE a.class_id = :class_id -- 1
    AND a.actual_date IS NOT NULL
    AND DATE(a.actual_date) BETWEEN pw.start_date AND pw.end_date
),
calc AS (
  SELECT 
    SUM(sc.score)::numeric            AS total_score,
    SUM(a.max_score)::numeric         AS total_max
  FROM asses a
  LEFT JOIN score sc 
    ON sc.assessment_id = a.id 
   AND sc.student_id = :student_id -- 1
)
SELECT 
  ROUND( (total_score / NULLIF(total_max,0)) * 100, 2 ) AS phase_avg_percent
FROM calc;

	4b) Toàn khóa
WITH asses AS (
  SELECT a.id, ca.max_score
  FROM assessment a
  JOIN course_assessment ca ON a.course_assessment_id = ca.id
  WHERE a.class_id = :class_id -- 1
    AND a.actual_date IS NOT NULL
),
calc AS (
  SELECT 
    SUM(sc.score)::numeric AS total_score,
    SUM(a.max_score)::numeric AS total_max
  FROM asses a
  LEFT JOIN score sc 
    ON sc.assessment_id = a.id 
   AND sc.student_id = :student_id -- 1
)
SELECT 
  ROUND( (total_score / NULLIF(total_max,0)) * 100, 2 ) AS course_avg_percent
FROM calc;


5) Tóm tắt nhanh (progress)

WITH phase_window AS (
  SELECT 
    MIN(s.date) AS start_date, 
    MAX(s.date) AS end_date
  FROM session s
  JOIN course_session cs ON cs.id = s.course_session_id
  WHERE s.class_id = :class_id -- 1
    AND cs.phase_id = :phase_id -- 1
),
total AS (
  SELECT COUNT(a.id) AS total_assess
  FROM assessment a
  CROSS JOIN phase_window pw
  WHERE a.class_id = :class_id -- 1
    AND a.actual_date IS NOT NULL
    AND DATE(a.actual_date) BETWEEN pw.start_date AND pw.end_date
),
graded AS (
  SELECT COUNT(DISTINCT sc.assessment_id) AS graded_assess
  FROM score sc
  JOIN assessment a ON a.id = sc.assessment_id
  CROSS JOIN phase_window pw
  WHERE a.class_id = :class_id -- 1
    AND sc.student_id = :student_id -- 1
    AND a.actual_date IS NOT NULL
    AND DATE(a.actual_date) BETWEEN pw.start_date AND pw.end_date
)
SELECT 
  graded_assess,
  total_assess,
  ROUND(graded_assess::numeric / NULLIF(total_assess,0) * 100, 1) AS graded_percent
FROM total, graded;

I. % học viên đạt từng CLO trong một khóa học
Mục tiêu: 
1. Đánh giá hiệu quả giảng dạy
Lớp A1-GEN-001 (General English A1):
- CLO-A1-01 (Greetings): 85% học viên đạt
- CLO-A1-02 (Present tense): 80% học viên đạt
- CLO-A1-03 (Personal questions): 55% học viên đạt -> có vấn đề
=> Từ đó đưa ra hành động các hành động như review lại cách dạy cho CLO-A1-03, kiểm tra đề thi có khó quá không, giáo viên có cần train thêm để dạy CLO này không, CLO này có cần điều chỉnh gì không.
2. Trung tâm Anh ngữ muốn được cấp chứng chỉ quốc tế (Cambridge, IELTS):
→ Phải chứng minh: "80% học viên đạt tất cả CLOs"
→ Báo cáo này là BẰNG CHỨNG.

WITH enrolled_students AS (
    -- Lấy danh sách học viên enrolled trong mỗi class
    SELECT DISTINCT
        c.id AS class_id,
        c.code AS class_code,
        c.name AS class_name,
        c.course_id,
        e.student_id
    FROM class c
    INNER JOIN enrollment e ON c.id = e.class_id
    WHERE e.status = 'enrolled'
),

clo_assessments AS (
    -- Mapping: CLO → Assessment (qua course_assessment)
    SELECT DISTINCT
        clo.id AS clo_id,
        clo.code AS clo_code,
        clo.description AS clo_description,
        clo.course_id,
        ca.id AS course_assessment_id,
        a.id AS assessment_id,
        a.class_id,
        ca.max_score
    FROM clo
    INNER JOIN course_assessment_clo_mapping cacm ON clo.id = cacm.clo_id
    INNER JOIN course_assessment ca ON cacm.course_assessment_id = ca.id
    INNER JOIN assessment a ON ca.id = a.course_assessment_id
    WHERE cacm.status = 'active'
),

student_clo_scores AS (
    -- Tính điểm trung bình của mỗi student cho mỗi CLO
    SELECT
        ca.class_id,
        ca.course_id,
        ca.clo_id,
        ca.clo_code,
        ca.clo_description,
        s.id,
        -- Tính % điểm trung bình: AVG(score / max_score) * 100
        AVG(sc.score / NULLIF(ca.max_score, 0)) * 100 AS avg_score_percentage
    FROM clo_assessments ca
    INNER JOIN score sc ON ca.assessment_id = sc.assessment_id
    INNER JOIN student s ON sc.student_id = s.id
    GROUP BY ca.class_id, ca.course_id, ca.clo_id, ca.clo_code, ca.clo_description, s.id
),

clo_achievement AS (
    -- Xác định học viên nào ĐẠT CLO (>= 70%)
    SELECT
        class_id,
        course_id,
        clo_id,
        clo_code,
        clo_description,
        id,
        avg_score_percentage,
        CASE 
            WHEN avg_score_percentage >= 70 THEN 1 
            ELSE 0 
        END AS is_achieved
    FROM student_clo_scores
),

clo_summary AS (
    -- Tổng hợp: Đếm số học viên đạt và tổng số học viên
    SELECT
        ca.class_id,
        c.code AS class_code,
        c.name AS class_name,
        co.id AS course_id,
        co.code AS course_code,
        co.name AS course_name,
        ca.clo_id,
        ca.clo_code,
        ca.clo_description,
        COUNT(DISTINCT es.student_id) AS total_students,
        COUNT(DISTINCT CASE WHEN ca.is_achieved = 1 THEN ca.id END) AS students_achieved,
        ROUND(
            COUNT(DISTINCT CASE WHEN ca.is_achieved = 1 THEN ca.id END)::NUMERIC / 
            NULLIF(COUNT(DISTINCT es.student_id), 0) * 100, 
            2
        ) AS achievement_percentage,
        ROUND(AVG(ca.avg_score_percentage), 2) AS avg_class_score_percentage
    FROM clo_achievement ca
    INNER JOIN class c ON ca.class_id = c.id
    INNER JOIN course co ON ca.course_id = co.id
    INNER JOIN enrolled_students es ON ca.class_id = es.class_id
    GROUP BY ca.class_id, c.code, c.name, co.id, co.code, co.name, ca.clo_id, ca.clo_code, ca.clo_description
)

-- OUTPUT CHÍNH
SELECT
    class_code,
    class_name,
    course_code,
    course_name,
    clo_code,
    clo_description,
    total_students,
    students_achieved,
    achievement_percentage || '%' AS achievement_rate,
    avg_class_score_percentage || '%' AS avg_score
FROM clo_summary
ORDER BY class_code, clo_code;




II. List all student đạt được bao nhiêu đối với all CLOs
Mục tiêu:
1. Hỗ trợ học viên kịp thời
Tình huống:
Student S003 (Le Van Cuong):
✓ CLO-A1-01: 91% (ĐẠT)
✓ CLO-A1-02: 85% (ĐẠT)
✗ CLO-A1-03: 68% (CHƯA ĐẠT) ← CẦN HỖ TRỢ
✓ CLO-A1-04: 79% (ĐẠT)
✗ CLO-A1-05: 65% (CHƯA ĐẠT) ← CẦN HỖ TRỢ
=> Academic Affair ví dụ như gọi điện cho phụ huynh hoặc hỗ trợ, lên lịch dạy bù cho học viên, track progress 2 tuần sau kiểm tra lại.
2. Cảnh báo sớm
Tuần 6 của khóa 12 tuần:
Student S008 (Bui Thi Ha):
✗ Chưa đạt 4/5 CLOs → NGUY CƠ FAIL
=> Giáo vụ gọi điện ngay cho học viên ngay lập tức để hiểu vấn đề, hay phương pháp học sai, có thể nhận feedback lập tức => ra quyết định đổi sang lớp chậm hơn hoặc là xử lý để đảm bảo học viên không bỏ học vì cảm thấy bất lực. Hoặc là tư vấn định hướng ví dụ kém phần giao tiếp mà gần hết khóa thì có thể tư vấn thêm khóa chỉ nghe và nói giao tiếp

WITH enrolled_students AS (
    SELECT DISTINCT
        c.id AS class_id,
        c.code AS class_code,
        c.course_id,
        e.student_id,
        s.student_code,
        ua.full_name
    FROM class c
    INNER JOIN enrollment e ON c.id = e.class_id
    INNER JOIN student s ON e.student_id = s.id
    INNER JOIN user_account ua ON s.user_id = ua.id
    WHERE e.status = 'enrolled'
),

-- Lấy tất cả CLOs của course
course_clos AS (
    SELECT DISTINCT
        c.id AS class_id,
        c.course_id,
        clo.id AS clo_id,
        clo.code AS clo_code,
        clo.description AS clo_description
    FROM class c
    INNER JOIN clo ON c.course_id = clo.course_id
),

-- Mapping: CLO → Assessments
clo_assessments AS (
    SELECT DISTINCT
        clo.id AS clo_id,
        clo.code AS clo_code,
        clo.description AS clo_description,
        ca.id AS course_assessment_id,
        a.id AS assessment_id,
        a.class_id,
        ca.max_score
    FROM clo
    INNER JOIN course_assessment_clo_mapping cacm ON clo.id = cacm.clo_id
    INNER JOIN course_assessment ca ON cacm.course_assessment_id = ca.id
    INNER JOIN assessment a ON ca.id = a.course_assessment_id
    WHERE cacm.status = 'active'
),

-- Tính điểm trung bình của student cho mỗi CLO
student_clo_scores AS (
    SELECT
        ca.class_id,
        ca.clo_id,
        ca.clo_code,
        ca.clo_description,
        s.id AS student_id,
        COUNT(sc.id) AS total_assessments_taken,
        AVG(sc.score / NULLIF(ca.max_score, 0)) * 100 AS avg_score_percentage
    FROM clo_assessments ca
    INNER JOIN score sc ON ca.assessment_id = sc.assessment_id
    INNER JOIN student s ON sc.student_id = s.id
    GROUP BY ca.class_id, ca.clo_id, ca.clo_code, ca.clo_description, s.id
),

-- Đếm tổng số assessments liên quan đến mỗi CLO
clo_total_assessments AS (
    SELECT
        class_id,
        clo_id,
        COUNT(DISTINCT assessment_id) AS total_assessments_expected
    FROM clo_assessments
    GROUP BY class_id, clo_id
)

-- OUTPUT CHÍNH
SELECT
    es.class_code,
    es.student_code,
    es.full_name,
    cc.clo_code,
    cc.clo_description,
    
    -- Số bài thi đã làm / Tổng số bài thi của CLO
    COALESCE(scs.total_assessments_taken, 0) || '/' || 
    COALESCE(cta.total_assessments_expected, 0) AS assessments_progress,
    
    -- Điểm phần trăm
    CASE 
        WHEN scs.avg_score_percentage IS NULL THEN 'N/A'
        ELSE ROUND(scs.avg_score_percentage, 2) || '%'
    END AS score_percent,
    
    -- Trạng thái đạt/chưa đạt
    CASE 
        WHEN scs.avg_score_percentage IS NULL THEN '⏳ CHƯA CÓ ĐIỂM'
        WHEN scs.total_assessments_taken < cta.total_assessments_expected THEN '⚠️ CHƯA HOÀN THÀNH'
        WHEN scs.avg_score_percentage >= 70 THEN '✓ ĐẠT'
        ELSE '✗ CHƯA ĐẠT'
    END AS achievement_status
    
FROM enrolled_students es
INNER JOIN course_clos cc 
    ON es.class_id = cc.class_id 
    AND es.course_id = cc.course_id
LEFT JOIN student_clo_scores scs 
    ON es.class_id = scs.class_id 
    AND es.student_id = scs.student_id 
    AND cc.clo_id = scs.clo_id
LEFT JOIN clo_total_assessments cta
    ON es.class_id = cta.class_id
    AND cc.clo_id = cta.clo_id
ORDER BY es.class_code, es.student_code, cc.clo_code;






III. So sánh % đạt CLO giữa các lớp cùng course
Mục tiêu:
1. Đánh giá giáo viên
Course: General English A1 (ENG-A1-V1)
Class A1-GEN-001 (Teacher: John Smith):
- CLO-A1-01: 85% đạt 
- CLO-A1-02: 80% đạt 
- CLO-A1-03: 78% đạt 
Class A1-GEN-002 (Teacher: Sarah Johnson):
- CLO-A1-01: 92% đạt EXCELLENT
- CLO-A1-02: 88% đạt EXCELLENT
- CLO-A1-03: 90% đạt EXCELLENT
Class A1-GEN-003 (Teacher: Michael Brown):
- CLO-A1-01: 65% đạt POOR
- CLO-A1-02: 62% đạt POOR
- CLO-A1-03: 58% đạt POOR

Sarah Johnson = Best performer → Yêu cầu share best practices
Michael Brown = Need support → Peer observation với Sarah
=> Tổ chức trainning cho teacher, ví dụ bảo share phương pháp dạy, Michael quan sát giờ dạy của Sarah. Sarah được thưởng teacher of the month. Michael chỉ có 3 tháng để cải thiện performance.
2. Đi điều tra lớp tại sao kém -> vào danh sách lớp của một lớp nhìn thấy danh sách học viên có điểm đầu vào kém -> hiểu được là do nền tảng kém chứ không phải lỗi hoàn toàn của giáo viên => có thể đưa ra quyết định chuyển student sang lớp có course phù hợp hơn.
3. CLOs thất bại ở tất cả các lớp => lỗi tại giáo trình.
4. Phân bổ tài nguyên -> ví dụ như branch A thiếu giáo viên giỏi -> điều chuyển giáo viên giỏi từ branch khác sang.

WITH enrolled_students AS (
    SELECT DISTINCT
        c.id AS class_id,
        c.code AS class_code,
        c.name AS class_name,
        c.course_id,
        c.status AS class_status,
        e.student_id
    FROM class c
    INNER JOIN enrollment e ON c.id = e.class_id
    WHERE e.status = 'enrolled'
),

-- Lấy tất cả CLOs của mỗi course
course_clos AS (
    SELECT DISTINCT
        c.id AS class_id,
        c.code AS class_code,
        c.name AS class_name,
        c.course_id,
        co.code AS course_code,
        co.name AS course_name,
        clo.id AS clo_id,
        clo.code AS clo_code,
        clo.description AS clo_description
    FROM class c
    INNER JOIN course co ON c.course_id = co.id
    INNER JOIN clo ON c.course_id = clo.course_id
),

-- Mapping: CLO → Assessments
clo_assessments AS (
    SELECT DISTINCT
        clo.id AS clo_id,
        ca.id AS course_assessment_id,
        a.id AS assessment_id,
        a.class_id,
        ca.max_score
    FROM clo
    INNER JOIN course_assessment_clo_mapping cacm ON clo.id = cacm.clo_id
    INNER JOIN course_assessment ca ON cacm.course_assessment_id = ca.id
    INNER JOIN assessment a ON ca.id = a.course_assessment_id
    WHERE cacm.status = 'active'
),

-- Tính điểm trung bình của mỗi student cho mỗi CLO
student_clo_scores AS (
    SELECT
        ca.class_id,
        ca.clo_id,
        s.id AS student_id,
        AVG(sc.score / NULLIF(ca.max_score, 0)) * 100 AS avg_score_percentage
    FROM clo_assessments ca
    INNER JOIN score sc ON ca.assessment_id = sc.assessment_id
    INNER JOIN student s ON sc.student_id = s.id
    GROUP BY ca.class_id, ca.clo_id, s.id
    HAVING COUNT(sc.id) > 0  -- Chỉ tính học viên có ít nhất 1 điểm
),

-- Xác định học viên đạt/không đạt CLO
student_clo_achievement AS (
    SELECT
        class_id,
        clo_id,
        student_id,
        avg_score_percentage,
        CASE 
            WHEN avg_score_percentage >= 70 THEN 1 
            ELSE 0 
        END AS is_achieved
    FROM student_clo_scores
),

-- Tổng hợp theo từng lớp và CLO
class_clo_summary AS (
    SELECT
        cc.course_code,
        cc.course_name,
        cc.class_id,
        cc.class_code,
        cc.class_name,
        cc.clo_id,
        cc.clo_code,
        cc.clo_description,
        COUNT(DISTINCT es.student_id) AS total_enrolled_students,
        COUNT(DISTINCT sca.student_id) AS students_with_scores,
        COUNT(DISTINCT CASE WHEN sca.is_achieved = 1 THEN sca.student_id END) AS students_achieved,
        ROUND(
            COUNT(DISTINCT CASE WHEN sca.is_achieved = 1 THEN sca.student_id END)::NUMERIC / 
            NULLIF(COUNT(DISTINCT sca.student_id), 0) * 100, 
            2
        ) AS achievement_percentage,
        ROUND(AVG(sca.avg_score_percentage), 2) AS avg_class_score
    FROM course_clos cc
    INNER JOIN enrolled_students es ON cc.class_id = es.class_id
    LEFT JOIN student_clo_achievement sca 
        ON cc.class_id = sca.class_id 
        AND cc.clo_id = sca.clo_id
        AND es.student_id = sca.student_id
    GROUP BY cc.course_code, cc.course_name, cc.class_id, cc.class_code, 
             cc.class_name, cc.clo_id, cc.clo_code, cc.clo_description
),

-- Tính ranking và statistics
class_clo_ranking AS (
    SELECT
        *,
        -- Ranking: Lớp nào có % đạt CLO cao nhất
        RANK() OVER (
            PARTITION BY course_code, clo_code 
            ORDER BY achievement_percentage DESC NULLS LAST, avg_class_score DESC
        ) AS class_rank,
        -- So sánh với trung bình của course
        ROUND(
            achievement_percentage - 
            AVG(achievement_percentage) OVER (PARTITION BY course_code, clo_code),
            2
        ) AS diff_from_course_avg,
        -- Trung bình % đạt CLO của course
        ROUND(
            AVG(achievement_percentage) OVER (PARTITION BY course_code, clo_code),
            2
        ) AS course_avg_achievement
    FROM class_clo_summary
)

-- OUTPUT: So sánh các lớp
SELECT
    course_code,
    course_name,
    clo_code,
    clo_description,
    class_code,
    class_name,
    total_enrolled_students AS enrolled,
    students_with_scores AS graded,
    students_achieved AS achieved,
    COALESCE(achievement_percentage, 0) || '%' AS achievement_rate,
    COALESCE(avg_class_score, 0) || '%' AS avg_score,
    course_avg_achievement || '%' AS course_avg,
    CASE 
        WHEN diff_from_course_avg > 0 THEN '+' || diff_from_course_avg || '%'
        WHEN diff_from_course_avg < 0 THEN diff_from_course_avg || '%'
        ELSE '0%'
    END AS vs_course_avg,
    class_rank AS rank,
    CASE 
        WHEN class_rank = 1 THEN 'TOP'
        WHEN diff_from_course_avg >= 10 THEN 'XUẤT SẮC'
        WHEN diff_from_course_avg >= 5 THEN 'TỐT'
        WHEN diff_from_course_avg >= -5 THEN 'TRUNG BÌNH'
        WHEN diff_from_course_avg >= -10 THEN 'THẤP'
        ELSE 'RẤT THẤP'
    END AS performance_level
FROM class_clo_ranking
ORDER BY course_code, clo_code, class_rank;




IV. Xu hướng đạt CLOs theo thời gian
Mục tiêu:
1. Đánh giá chất lượng cải tiến.
Timeline: General English A1 - CLO-A1-03 (Personal Questions)
Q1 2024 (Jan-Mar): 55% đạt → Phát hiện: CLO này có vấn đề!
=> ACTION (Apr 2024):
- Thay đổi phương pháp dạy: Thêm role-play activities
- Mua thêm teaching materials 
- Teacher training về interactive teaching
Q2 2024 (Apr-Jun): 65% đạt (+10%) → Cải tiến có tác dụng, nhưng chưa đủ
Q3 2024 (Jul-Sep): 78% đạt (+13%) → Đạt mục tiêu! Keep doing it!
2. Phát hiện suy giảm sớm:
IELTS Listening - CLO-IELTS-01
Jan 2024: 88% 
Feb 2024: 86% (−2%, STABLE)
Mar 2024: 83% (−3%, STABLE)
Apr 2024: 78% (−5%, DECLINING)
May 2024: 70% (−8%, DECLINING)
Jun 2024: 62% (−8%, SIGNIFICANT DECLINE)
=> Kiểm tra: Có phải teacher mới?
Jan-Mar: Teacher John (experienced) - 88% CLO achievement
Apr-Jun: Teacher Emily (new hire) - 70% CLO achievement ← ROOT CAUSE!
3. Phân tích xu hướng theo mùa:
Pattern qua 3 năm (2022-2024):
Q1 (Jan-Mar): 72% đạt (sau Tết, học viên lười)
Q2 (Apr-Jun): 82% đạt (PEAK - học viên motivated)
Q3 (Jul-Sep): 78% đạt (hè nóng, concentration giảm)
Q4 (Oct-Dec): 85% đạt (cuối năm, cố gắng hoàn thành)

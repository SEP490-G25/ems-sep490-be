-- =========================================
-- CLO PROGRESS TRACKING - OPTIMIZED QUERIES
-- =========================================
-- Student Learning Outcome Progress Tracking
-- Optimized for performance and accuracy
-- =========================================

-- =========================================
-- QUERY 1: CLO Progress Overview (All CLOs)
-- =========================================
-- Hiển thị TẤT CẢ CLO từ các courses mà student đã/đang học
-- Bao gồm cả CLO chưa có assessment hoặc chưa có điểm

SELECT 
    co.id AS course_id,
    co.name AS course_name,
    clo.id AS clo_id,
    clo.code AS clo_code,
    clo.description AS clo_description,
    
    -- Count course assessment templates mapped to this CLO
    COUNT(DISTINCT cacm.course_assessment_id) AS related_course_assessments,
    
    -- Count actual assessments in student's classes
    COUNT(DISTINCT CASE WHEN a.id IS NOT NULL THEN a.id END) AS actual_assessments_in_class,
    
    -- Count assessments that have been graded for this student
    COUNT(DISTINCT CASE WHEN sc.id IS NOT NULL THEN a.id END) AS graded_assessments,
    
    -- Simple average (all assessments equal weight)
    CASE 
        WHEN COUNT(DISTINCT CASE WHEN sc.id IS NOT NULL THEN a.id END) > 0 
        THEN ROUND(AVG(sc.score / a.max_score * 10), 2)
        ELSE NULL
    END AS simple_average,
    
    -- Weighted average (using assessment weights)
    CASE 
        WHEN SUM(CASE WHEN sc.id IS NOT NULL THEN COALESCE(a.weight, 1) ELSE 0 END) > 0
        THEN ROUND(
            SUM(CASE WHEN sc.id IS NOT NULL THEN (sc.score / a.max_score * 10 * COALESCE(a.weight, 1)) ELSE 0 END) / 
            NULLIF(SUM(CASE WHEN sc.id IS NOT NULL THEN COALESCE(a.weight, 1) ELSE 0 END), 0),
            2
        )
        ELSE NULL
    END AS weighted_average,
    
    -- Progress status
    CASE 
        WHEN COUNT(DISTINCT cacm.course_assessment_id) = 0 THEN 'not_mapped'
        WHEN COUNT(DISTINCT CASE WHEN a.id IS NOT NULL THEN a.id END) = 0 THEN 'not_created'
        WHEN COUNT(DISTINCT CASE WHEN sc.id IS NOT NULL THEN a.id END) = 0 THEN 'not_graded'
        WHEN COUNT(DISTINCT CASE WHEN sc.id IS NOT NULL THEN a.id END) < COUNT(DISTINCT CASE WHEN a.id IS NOT NULL THEN a.id END) THEN 'partially_graded'
        ELSE 'fully_graded'
    END AS status
    
FROM enrollment e
INNER JOIN "class" c ON e.class_id = c.id
INNER JOIN course co ON c.course_id = co.id
INNER JOIN clo ON clo.course_id = co.id
LEFT JOIN course_assessment_clo_mapping cacm ON cacm.clo_id = clo.id AND cacm.status = 'active'
LEFT JOIN course_assessment ca ON ca.id = cacm.course_assessment_id
LEFT JOIN assessment a ON a.course_assessment_id = ca.id AND a.class_id = c.id
LEFT JOIN score sc ON sc.assessment_id = a.id AND sc.student_id = e.student_id
WHERE e.student_id = 1  -- Replace with actual student_id
  AND e.status IN ('enrolled', 'completed')
GROUP BY co.id, co.name, clo.id, clo.code, clo.description
ORDER BY co.name, clo.code;

-- NOTE: Query này cần join qua enrollment → class vì mục đích là hiển thị
-- TẤT CẢ CLO của courses mà student đã đăng ký (kể cả chưa có điểm)


-- =========================================
-- QUERY 2: CLO Progress - Only Graded (High Performance)
-- =========================================
-- Chỉ hiển thị CLO đã có điểm - BẮT ĐẦU TỪ SCORE
-- Không cần join qua enrollment, class

SELECT 
    co.id AS course_id,
    co.name AS course_name,
    clo.id AS clo_id,
    clo.code AS clo_code,
    clo.description AS clo_description,
    
    -- Count graded assessments
    COUNT(DISTINCT a.id) AS graded_assessments,
    
    -- Simple average
    ROUND(AVG(sc.score / a.max_score * 10), 2) AS simple_average,
    
    -- Weighted average
    ROUND(
        SUM(sc.score / a.max_score * 10 * COALESCE(a.weight, 1)) / 
        NULLIF(SUM(COALESCE(a.weight, 1)), 0),
        2
    ) AS weighted_average,
    
    -- Min/Max scores
    ROUND(MIN(sc.score / a.max_score * 10), 2) AS min_score,
    ROUND(MAX(sc.score / a.max_score * 10), 2) AS max_score,
    
    -- Latest graded date
    MAX(sc.graded_at) AS latest_graded_at

FROM score sc
INNER JOIN assessment a ON sc.assessment_id = a.id
INNER JOIN course_assessment ca ON a.course_assessment_id = ca.id
INNER JOIN course_assessment_clo_mapping cacm ON cacm.course_assessment_id = ca.id AND cacm.status = 'active'
INNER JOIN clo ON cacm.clo_id = clo.id
INNER JOIN course co ON clo.course_id = co.id

WHERE sc.student_id = 1  -- Replace with actual student_id

GROUP BY co.id, co.name, clo.id, clo.code, clo.description
HAVING COUNT(DISTINCT a.id) > 0
ORDER BY co.name, clo.code;

-- PERFORMANCE GAIN: 
-- - Bỏ đi joins: enrollment, class
-- - Bắt đầu từ score (bảng nhỏ, đã có index trên student_id)
-- - Chỉ xử lý data đã có điểm


-- =========================================
-- QUERY 3: CLO Progress Detail - Assessment Breakdown
-- =========================================
-- Chi tiết từng assessment đóng góp vào CLO performance

SELECT 
    co.name AS course_name,
    c.code AS class_code,
    c.name AS class_name,
    clo.code AS clo_code,
    clo.description AS clo_description,
    
    -- Assessment info
    ca.name AS course_assessment_template,
    ca.kind AS assessment_kind,
    a.name AS actual_assessment_name,
    a.max_score AS max_score,
    a.weight AS weight,
    
    -- Student score
    sc.score AS raw_score,
    ROUND(sc.score / a.max_score * 10, 2) AS normalized_score,
    ROUND(sc.score / a.max_score * 10 * COALESCE(a.weight, 1), 2) AS weighted_score,
    
    -- Grading info
    sc.feedback,
    sc.graded_at,
    u.full_name AS graded_by_teacher

FROM score sc
INNER JOIN assessment a ON sc.assessment_id = a.id
INNER JOIN "class" c ON a.class_id = c.id
INNER JOIN course co ON c.course_id = co.id
INNER JOIN course_assessment ca ON a.course_assessment_id = ca.id
INNER JOIN course_assessment_clo_mapping cacm ON cacm.course_assessment_id = ca.id AND cacm.status = 'active'
INNER JOIN clo ON cacm.clo_id = clo.id
LEFT JOIN user_account u ON sc.graded_by = u.id

WHERE sc.student_id = 1  -- Replace with actual student_id
  AND clo.code = 'CLO-A1-01'  -- Filter by specific CLO

ORDER BY c.name, a.created_at;


-- =========================================
-- QUERY 4: CLO Progress by Class
-- =========================================
-- Xem performance của student theo từng CLO trong TỪNG LỚP
-- Hữu ích khi student học nhiều lớp cùng course

SELECT 
    c.id AS class_id,
    c.code AS class_code,
    c.name AS class_name,
    c.status AS class_status,
    co.name AS course_name,
    
    clo.code AS clo_code,
    clo.description AS clo_description,
    
    -- Assessment counts
    COUNT(DISTINCT CASE WHEN a.id IS NOT NULL THEN a.id END) AS total_assessments,
    COUNT(DISTINCT CASE WHEN sc.id IS NOT NULL THEN a.id END) AS graded_assessments,
    
    -- Performance metrics
    CASE 
        WHEN COUNT(DISTINCT CASE WHEN sc.id IS NOT NULL THEN a.id END) > 0 
        THEN ROUND(AVG(sc.score / a.max_score * 10), 2)
        ELSE NULL
    END AS simple_average,
    
    CASE 
        WHEN SUM(CASE WHEN sc.id IS NOT NULL THEN COALESCE(a.weight, 1) ELSE 0 END) > 0
        THEN ROUND(
            SUM(CASE WHEN sc.id IS NOT NULL THEN (sc.score / a.max_score * 10 * COALESCE(a.weight, 1)) ELSE 0 END) / 
            NULLIF(SUM(CASE WHEN sc.id IS NOT NULL THEN COALESCE(a.weight, 1) ELSE 0 END), 0),
            2
        )
        ELSE NULL
    END AS weighted_average

FROM enrollment e
INNER JOIN "class" c ON e.class_id = c.id
INNER JOIN course co ON c.course_id = co.id
INNER JOIN clo ON clo.course_id = co.id
LEFT JOIN course_assessment_clo_mapping cacm ON cacm.clo_id = clo.id AND cacm.status = 'active'
LEFT JOIN course_assessment ca ON ca.id = cacm.course_assessment_id
LEFT JOIN assessment a ON a.course_assessment_id = ca.id AND a.class_id = c.id
LEFT JOIN score sc ON sc.assessment_id = a.id AND sc.student_id = e.student_id

WHERE e.student_id = 1  -- Replace with actual student_id
  AND e.status IN ('enrolled', 'completed')

GROUP BY c.id, c.code, c.name, c.status, co.id, co.name, clo.id, clo.code, clo.description
ORDER BY c.name, clo.code;


-- =========================================
-- QUERY 5: CLO Mastery Summary (Aggregate by Course)
-- =========================================
-- Tổng hợp performance của student theo COURSE
-- Tính % CLO đã đạt (mastery threshold = 7.0/10)

WITH clo_performance AS (
    SELECT 
        co.id AS course_id,
        co.name AS course_name,
        clo.id AS clo_id,
        clo.code AS clo_code,
        
        COUNT(DISTINCT CASE WHEN sc.id IS NOT NULL THEN a.id END) AS graded_count,
        
        CASE 
            WHEN COUNT(DISTINCT CASE WHEN sc.id IS NOT NULL THEN a.id END) > 0 
            THEN ROUND(AVG(sc.score / a.max_score * 10), 2)
            ELSE NULL
        END AS avg_score
        
    FROM enrollment e
    INNER JOIN "class" c ON e.class_id = c.id
    INNER JOIN course co ON c.course_id = co.id
    INNER JOIN clo ON clo.course_id = co.id
    LEFT JOIN course_assessment_clo_mapping cacm ON cacm.clo_id = clo.id AND cacm.status = 'active'
    LEFT JOIN course_assessment ca ON ca.id = cacm.course_assessment_id
    LEFT JOIN assessment a ON a.course_assessment_id = ca.id AND a.class_id = c.id
    LEFT JOIN score sc ON sc.assessment_id = a.id AND sc.student_id = e.student_id
    
    WHERE e.student_id = 1  -- Replace with actual student_id
      AND e.status IN ('enrolled', 'completed')
    
    GROUP BY co.id, co.name, clo.id, clo.code
)
SELECT 
    course_id,
    course_name,
    COUNT(*) AS total_clos,
    COUNT(CASE WHEN avg_score IS NOT NULL THEN 1 END) AS graded_clos,
    COUNT(CASE WHEN avg_score >= 7.0 THEN 1 END) AS mastered_clos,
    ROUND(AVG(avg_score), 2) AS overall_clo_average,
    ROUND(
        COUNT(CASE WHEN avg_score >= 7.0 THEN 1 END)::NUMERIC / 
        NULLIF(COUNT(CASE WHEN avg_score IS NOT NULL THEN 1 END), 0) * 100,
        2
    ) AS mastery_percentage
FROM clo_performance
GROUP BY course_id, course_name
ORDER BY course_name;


-- =========================================
-- QUERY 6: CLO Progress Heatmap Data
-- =========================================
-- Data cho visualization - heatmap showing mastery level
-- Useful for dashboard/charts

SELECT 
    co.name AS course_name,
    clo.code AS clo_code,
    clo.description AS clo_description,
    
    COUNT(DISTINCT CASE WHEN sc.id IS NOT NULL THEN a.id END) AS graded_assessments,
    
    ROUND(AVG(sc.score / a.max_score * 10), 2) AS avg_performance,
    
    -- Mastery level categorization
    CASE 
        WHEN AVG(sc.score / a.max_score * 10) IS NULL THEN 'not_assessed'
        WHEN AVG(sc.score / a.max_score * 10) >= 9.0 THEN 'excellent'
        WHEN AVG(sc.score / a.max_score * 10) >= 8.0 THEN 'very_good'
        WHEN AVG(sc.score / a.max_score * 10) >= 7.0 THEN 'good'
        WHEN AVG(sc.score / a.max_score * 10) >= 6.0 THEN 'satisfactory'
        WHEN AVG(sc.score / a.max_score * 10) >= 5.0 THEN 'needs_improvement'
        ELSE 'poor'
    END AS mastery_level,
    
    -- Color coding for UI (can be used in frontend)
    CASE 
        WHEN AVG(sc.score / a.max_score * 10) IS NULL THEN '#E0E0E0'  -- Gray
        WHEN AVG(sc.score / a.max_score * 10) >= 9.0 THEN '#4CAF50'   -- Green
        WHEN AVG(sc.score / a.max_score * 10) >= 8.0 THEN '#8BC34A'   -- Light Green
        WHEN AVG(sc.score / a.max_score * 10) >= 7.0 THEN '#FFEB3B'   -- Yellow
        WHEN AVG(sc.score / a.max_score * 10) >= 6.0 THEN '#FF9800'   -- Orange
        WHEN AVG(sc.score / a.max_score * 10) >= 5.0 THEN '#FF5722'   -- Deep Orange
        ELSE '#F44336'                                                 -- Red
    END AS color_code

FROM enrollment e
INNER JOIN "class" c ON e.class_id = c.id
INNER JOIN course co ON c.course_id = co.id
INNER JOIN clo ON clo.course_id = co.id
LEFT JOIN course_assessment_clo_mapping cacm ON cacm.clo_id = clo.id AND cacm.status = 'active'
LEFT JOIN course_assessment ca ON ca.id = cacm.course_assessment_id
LEFT JOIN assessment a ON a.course_assessment_id = ca.id AND a.class_id = c.id
LEFT JOIN score sc ON sc.assessment_id = a.id AND sc.student_id = e.student_id

WHERE e.student_id = 1  -- Replace with actual student_id
  AND e.status IN ('enrolled', 'completed')

GROUP BY co.id, co.name, clo.id, clo.code, clo.description
ORDER BY co.name, clo.code;


-- =========================================
-- QUERY 7: Debug Query - Check Mapping Issues
-- =========================================
-- Dùng để debug khi CLO có mapping nhưng không có điểm

SELECT 
    clo.code AS clo_code,
    clo.description AS clo_description,
    
    ca.id AS course_assessment_id,
    ca.name AS course_assessment_name,
    ca.kind AS course_assessment_kind,
    
    a.id AS assessment_id,
    a.name AS actual_assessment_name,
    a.class_id,
    c.code AS class_code,
    
    sc.id AS score_id,
    sc.score AS student_score,
    sc.graded_at

FROM clo
INNER JOIN course_assessment_clo_mapping cacm ON cacm.clo_id = clo.id AND cacm.status = 'active'
INNER JOIN course_assessment ca ON ca.id = cacm.course_assessment_id
LEFT JOIN assessment a ON a.course_assessment_id = ca.id
LEFT JOIN "class" c ON a.class_id = c.id
LEFT JOIN enrollment e ON e.class_id = c.id AND e.student_id = 1 AND e.status IN ('enrolled', 'completed')
LEFT JOIN score sc ON sc.assessment_id = a.id AND sc.student_id = 1

WHERE clo.code = 'CLO-A1-02'  -- Replace with CLO có vấn đề

ORDER BY ca.name, a.class_id, a.name;

-- Kết quả sẽ cho biết:
-- 1. CLO được map với course_assessment nào
-- 2. Course_assessment đó đã được tạo assessment thực tế chưa
-- 3. Assessment đó có trong class của student không
-- 4. Assessment đó đã được chấm điểm chưa


-- =========================================
-- QUERY 8: CLO Progress Timeline
-- =========================================
-- Xem tiến độ CLO theo thời gian

SELECT 
    co.name AS course_name,
    clo.code AS clo_code,
    
    DATE(sc.graded_at) AS graded_date,
    a.name AS assessment_name,
    a.kind AS assessment_kind,
    
    sc.score AS raw_score,
    a.max_score,
    ROUND(sc.score / a.max_score * 10, 2) AS normalized_score,
    
    -- Running average up to this point
    ROUND(AVG(sc.score / a.max_score * 10) OVER (
        PARTITION BY clo.id 
        ORDER BY sc.graded_at 
        ROWS BETWEEN UNBOUNDED PRECEDING AND CURRENT ROW
    ), 2) AS cumulative_average

FROM score sc
INNER JOIN assessment a ON sc.assessment_id = a.id
INNER JOIN "class" c ON a.class_id = c.id
INNER JOIN course co ON c.course_id = co.id
INNER JOIN course_assessment ca ON a.course_assessment_id = ca.id
INNER JOIN course_assessment_clo_mapping cacm ON cacm.course_assessment_id = ca.id AND cacm.status = 'active'
INNER JOIN clo ON cacm.clo_id = clo.id

WHERE sc.student_id = 1  -- Replace with actual student_id
  AND sc.graded_at IS NOT NULL

ORDER BY clo.code, sc.graded_at;


-- =========================================
-- PERFORMANCE NOTES
-- =========================================
/*
1. QUERY 1: Full CLO view - Cần khi cần xem ALL CLOs (including not graded)
   - Use case: Student profile overview, progress dashboard
   - Performance: Medium (requires enrollment/class joins)

2. QUERY 2: Graded only - FASTEST - Recommended for most use cases
   - Use case: Academic reports, GPA calculations
   - Performance: High (starts from score table, indexed)

3. QUERY 3: Detail breakdown - Khi cần drill-down vào specific CLO
   - Use case: Teacher review, student self-assessment detail
   - Performance: Medium (with CLO filter, very fast)

4. QUERY 4: By class - Khi student học nhiều lớp của cùng 1 course
   - Use case: Transfer student, retake analysis
   - Performance: Medium

5. QUERY 5: Mastery summary - Aggregate level for reporting
   - Use case: Academic committee reports, dean's list
   - Performance: Medium (CTE overhead)

6. QUERY 6: Heatmap data - For UI visualization
   - Use case: Dashboard widgets, progress charts
   - Performance: Medium

7. QUERY 7: Debug tool - Troubleshooting only
   - Use case: Admin debugging mapping issues
   - Performance: Fast (with CLO filter)

8. QUERY 8: Timeline - Trend analysis
   - Use case: Learning analytics, intervention planning
   - Performance: Medium (window function overhead)
*/


-- =========================================
-- INDEX RECOMMENDATIONS
-- =========================================
/*
Để tối ưu performance của các queries trên:

CREATE INDEX IF NOT EXISTS idx_score_student_graded ON score(student_id, graded_at);
CREATE INDEX IF NOT EXISTS idx_assessment_course_assessment_class ON assessment(course_assessment_id, class_id);
CREATE INDEX IF NOT EXISTS idx_course_assessment_clo_clo ON course_assessment_clo_mapping(clo_id, status);
CREATE INDEX IF NOT EXISTS idx_course_assessment_clo_ca ON course_assessment_clo_mapping(course_assessment_id, status);
CREATE INDEX IF NOT EXISTS idx_enrollment_student_status ON enrollment(student_id, status);
CREATE INDEX IF NOT EXISTS idx_clo_course ON clo(course_id);

Indexes này đã có trong database schema nhưng cần verify.
*/

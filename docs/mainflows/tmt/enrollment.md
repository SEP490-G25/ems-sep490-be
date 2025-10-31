
Luồng ghi danh học viên vào lớp

GIAI ĐOẠN 1: KHỞI TẠO & XEM DANH SÁCH 
Step 1: Academic Affair truy cập vào trang chi tiết lớp học (Class Detail page)
Tại đây hiển thị thông tin lớp và danh sách học viên đã được ghi danh
SELECT 
    e.id AS enrollment_id,
    e.status AS enrollment_status,
    e.enrolled_at,
    s.id AS student_id,
    s.student_code,
    u.facebook_url,
    u.dob,
    u.full_name,
    u.email,
    u.phone,
    u.address,
    u.gender,
    u.status AS account_status,
    -- Lấy general level từ latest assessment
    (
        SELECT l.name
        FROM replacement_skill_assessment rsa
        JOIN level l ON rsa.level_id = l.id
        WHERE rsa.student_id = s.id
          AND rsa.skill = 'general'
        ORDER BY rsa.assessment_date DESC
        LIMIT 1
    ) AS current_level
FROM enrollment e
INNER JOIN student s ON e.student_id = s.id
INNER JOIN user_account u ON s.user_id = u.id
WHERE e.class_id = 1
  AND e.status = 'enrolled'
ORDER BY e.enrolled_at ASC, u.full_name ASC;



Step 2: Academic Affair Click "Ghi danh học viên"
Giáo vụ nhấn nút "Ghi danh học viên" để bắt đầu quy trình enrollment

Step 3: UI ngay từ lúc fetch class detail ra là đã lấy class_status để disable button đối với các lớp không phải là ongoing hoặc là scheduled

Step 4: System load danh sách tất cả học viên thuộc chi nhánh (branch)
Lấy TẤT CẢ students thuộc branch (không loại trừ students đang học lớp khác)
Đánh dấu students đang có enrollment active để cảnh báo
Hiển thị level matching với course requirement
Sắp xếp ưu tiên students phù hợp nhất
-- Class 1: IELTS Foundation (level_id = 1)
-- Sẽ hiển thị students từ branch_id = 1 (Ha Noi Main Branch)

WITH target_class_info AS (
    SELECT
        c.id,
        c.branch_id,
        c.course_id,
        c.max_capacity,
        c.status,
        co.level_id AS required_level_id,
        l.name AS required_level_name,
        l.description AS required_level_description
    FROM class c
    INNER JOIN course co ON c.course_id = co.id
    LEFT JOIN level l ON co.level_id = l.id
    WHERE c.id = 1  -- IELTS-F-MON-01
),
student_active_enrollments AS (
    SELECT
        e.student_id,
        COUNT(e.id) AS active_class_count,
        STRING_AGG(c.code || ' (' || c.name || ')', ', ' ORDER BY c.code) AS enrolled_class_list,
        BOOL_OR(e.class_id = 1) AS already_enrolled_in_target
    FROM enrollment e
    INNER JOIN class c ON e.class_id = c.id
    WHERE e.status = 'enrolled'
      AND c.status IN ('scheduled', 'ongoing')
    GROUP BY e.student_id
),
student_latest_assessment_raw AS (
    SELECT student_id, MAX(assessment_date) AS latest_assessment_date
    FROM replacement_skill_assessment
    GROUP BY student_id
),
student_latest_assessment AS (
    SELECT
        rsa.student_id,
        MAX(CASE WHEN rsa.skill = 'general' THEN rsa.score END) AS general_score,
        MAX(CASE WHEN rsa.skill = 'listening' THEN rsa.score END) AS listening_score,
        MAX(CASE WHEN rsa.skill = 'reading' THEN rsa.score END) AS reading_score,
        MAX(CASE WHEN rsa.skill = 'writing' THEN rsa.score END) AS writing_score,
        MAX(CASE WHEN rsa.skill = 'speaking' THEN rsa.score END) AS speaking_score,
        MAX(CASE WHEN rsa.skill = 'general' THEN rsa.level_id END) AS general_level_id,
        slar.latest_assessment_date,
        MAX(CASE WHEN rsa.assessment_date = slar.latest_assessment_date 
                 THEN rsa.assessment_type END) AS latest_assessment_type,
        MAX(CASE WHEN rsa.assessment_date = slar.latest_assessment_date 
                 THEN rsa.assessed_by END) AS assessed_by_user_id
    FROM replacement_skill_assessment rsa
    INNER JOIN student_latest_assessment_raw slar
        ON rsa.student_id = slar.student_id
        AND rsa.assessment_date = slar.latest_assessment_date
    GROUP BY rsa.student_id, slar.latest_assessment_date
),
student_level_names AS (
    SELECT
        sla.student_id,
        sla.general_score,
        sla.listening_score,
        sla.reading_score,
        sla.writing_score,
        sla.speaking_score,
        sla.general_level_id,
        sla.latest_assessment_date,
        sla.latest_assessment_type,
        l_general.name AS general_level_name,
        l_general.description AS general_level_description,
        assessed_by_user.full_name AS assessed_by_name
    FROM student_latest_assessment sla
    LEFT JOIN level l_general ON sla.general_level_id = l_general.id
    LEFT JOIN user_account assessed_by_user ON sla.assessed_by_user_id = assessed_by_user.id
)
SELECT
    s.id AS student_id,
    s.student_code,
    u.id AS user_id,
    u.full_name,
    u.email,
    u.phone,
    u.dob,
    u.gender,
    u.address,
    u.facebook_url,
    u.status AS account_status,
    
    COALESCE(sae.active_class_count, 0) AS active_class_count,
    sae.enrolled_class_list,
    COALESCE(sae.already_enrolled_in_target, FALSE) AS already_enrolled_in_target,
    
    sln.general_score,
    sln.listening_score,
    sln.reading_score,
    sln.writing_score,
    sln.speaking_score,
    sln.general_level_id,
    sln.general_level_name,
    sln.general_level_description,
    sln.latest_assessment_date,
    sln.latest_assessment_type,
    sln.assessed_by_name,
    
    tci.required_level_id,
    tci.required_level_name,
    tci.required_level_description,
    
    CASE
        WHEN tci.required_level_id IS NULL THEN TRUE
        WHEN sln.general_level_id IS NULL THEN NULL
        WHEN sln.general_level_id = tci.required_level_id THEN TRUE
        ELSE FALSE
    END AS is_level_matched,
    
    CASE
        WHEN sae.already_enrolled_in_target = TRUE THEN 'already_enrolled'
        WHEN sln.general_level_id IS NULL THEN 'no_assessment'
        WHEN tci.required_level_id IS NOT NULL AND sln.general_level_id != tci.required_level_id THEN 'level_mismatch'
        WHEN sae.active_class_count > 0 THEN 'has_active_enrollment'
        ELSE 'eligible'
    END AS eligibility_status,
    
    (
        CASE WHEN sae.already_enrolled_in_target = TRUE THEN -1000 ELSE 0 END +
        CASE 
            WHEN tci.required_level_id IS NULL THEN 100
            WHEN sln.general_level_id = tci.required_level_id THEN 100
            ELSE 0 
        END +
        CASE WHEN sln.latest_assessment_date IS NOT NULL THEN 50 ELSE 0 END +
        CASE WHEN sae.active_class_count = 0 THEN 30 ELSE 0 END +
        CASE WHEN sln.latest_assessment_date >= CURRENT_DATE - INTERVAL '3 months' THEN 20 ELSE 0 END
    ) AS priority_score

FROM student s
INNER JOIN user_account u ON s.user_id = u.id
INNER JOIN user_branches ub ON u.id = ub.user_id
CROSS JOIN target_class_info tci
LEFT JOIN student_active_enrollments sae ON s.id = sae.student_id
LEFT JOIN student_level_names sln ON s.id = sln.student_id

WHERE ub.branch_id = tci.branch_id
  AND u.status = 'active'
  AND (sae.already_enrolled_in_target IS NULL OR sae.already_enrolled_in_target = FALSE)

ORDER BY priority_score DESC, sln.latest_assessment_date DESC NULLS LAST, u.full_name ASC
LIMIT 20;  -- Giới hạn để test

OUTPUT:
[
  {
    "student_id": 15,
    "student_code": "STD-0015",
    "user_id": 114,
    "full_name": "Bui Van Phong",
    "email": "student.f015@gmail.com",
    "phone": "+84-900-001-015",
    "dob": "2005-11-11",
    "gender": "Male",
    "address": "Ha Noi",
    "facebook_url": null,
    "account_status": "active",
    "active_class_count": 1,
    "enrolled_class_list": "FOUND-F2-2024 (Foundation F2 - Evening Online)",
    "already_enrolled_in_target": false,
    "general_score": 38,
    "listening_score": null,
    "reading_score": null,
    "writing_score": null,
    "speaking_score": null,
    "general_level_id": 1,
    "general_level_name": "IELTS Foundation",
    "general_level_description": "Foundation level for beginners, targeting IELTS band 3.0-4.0",
    "latest_assessment_date": "2024-10-02",
    "latest_assessment_type": "self_assessment",
    "assessed_by_name": null,
    "required_level_id": 1,
    "required_level_name": "IELTS Foundation",
    "required_level_description": "Foundation level for beginners, targeting IELTS band 3.0-4.0",
    "is_level_matched": true,
    "eligibility_status": "has_active_enrollment",
    "priority_score": 150
  },
  {
    "student_id": 51,
    "student_code": "STD-0051",
    "user_id": 150,
    "full_name": "Dao Thi Phuong",
    "email": "student.a008@gmail.com",
    "phone": "+84-900-003-008",
    "dob": "2000-02-21",
    "gender": "Female",
    "address": "Ha Noi",
    "facebook_url": null,
    "account_status": "active",
    "active_class_count": 1,
    "enrolled_class_list": "INTER-I2-2024 (Intermediate I2 - Evening Online)",
    "already_enrolled_in_target": false,
    "general_score": 52,
    "listening_score": 50,
    "reading_score": 55,
    "writing_score": null,
    "speaking_score": null,
    "general_level_id": 2,
    "general_level_name": "IELTS Intermediate",
    "general_level_description": "Intermediate level for developing skills, targeting IELTS band 5.0-6.0",
    "latest_assessment_date": "2024-09-12",
    "latest_assessment_type": "ielts",
    "assessed_by_name": null,
    "required_level_id": 1,
    "required_level_name": "IELTS Foundation",
    "required_level_description": "Foundation level for beginners, targeting IELTS band 3.0-4.0",
    "is_level_matched": false,
    "eligibility_status": "level_mismatch",
    "priority_score": 50
  },
 …
]



Step 5: Academic Affairs xem danh sách học viên có thể ghi danh
Danh sách hiển thị thông tin: họ tên, email, số điện thoại, v.v.

Step 6: System hiển thị 3 options
Hệ thống hiển thị 3 lựa chọn cho giáo vụ:
Option A: Chọn từ danh sách có sẵn
Option B: Thêm học viên mới (thêm lẻ)
Option C: Import CSV

Step 7: Academic Affairs chọn hành động
Giáo vụ quyết định chọn một trong 3 phương thức

GIAI ĐOẠN 2A: OPTION A - CHỌN HỌC VIÊN CÓ SẴN
Step 8A: Academic Affair OPTION A: Chọn student có sẵn từ danh sách

Step 9A: System hiển thị checkbox bên cạnh mỗi học viên trong danh sách
Giáo vụ có thể chọn một hoặc nhiều học viên

Step 10A: Nhảy đến Step 20 (Chọn students từ DS đã update)
Giáo vụ tiến hành chọn các học viên cần ghi danh

GIAI ĐOẠN 2B: OPTION B - THÊM HỌC VIÊN MỚI (LẺ)
Step 8B: Academic Affair OPTION B: Click "Thêm học viên mới"
Giáo vụ chọn phương thức thêm học viên mới từng người

Step 9B: System hiển thị form tạo student mới
Hệ thống mở form nhập thông tin học viên mới
Form bao gồm: Họ tên, email, số điện thoại, ngày sinh, địa chỉ, v.v.

Step 10B: Academic Affair điền form thông tin student

Step 11B: Academic Affair click "Lưu và Thêm vào DS"

Step 12B: System validate input
(hệ thống tự động gọi api)
Hệ thống validate thông tin:
Email unique (không trùng trong hệ thống)
Phone format (đúng định dạng số điện thoại)
Required fields (các trường bắt buộc phải có giá trị)
-- Validate 1: Kiểm tra email đã tồn tại chưa
SELECT COUNT(*) AS email_exists
FROM user_account
WHERE LOWER(email) = LOWER(:email);
-- Nếu email_exists > 0 → Email đã tồn tại, không cho phép tạo mới
-- Nếu email_exists = 0 → Email hợp lệ, tiếp tục
-- Validate 2: Kiểm tra phone đã tồn tại chưa (optional, nếu yêu cầu unique)
SELECT COUNT(*) AS phone_exists
FROM user_account
WHERE phone = :phone;
-- Validate 3: Kiểm tra student_code đã tồn tại chưa (nếu có)
SELECT COUNT(*) AS code_exists
FROM student
WHERE student_code = :studentCode;



Step 13B: System tạo bản ghi student record liên kết với user_account
Gán role STUDENT cho user
WITH new_user AS (
    INSERT INTO user_account (
        email,
        phone,
        full_name,
        dob,
        gender,
        address,
        password_hash,
        status,
        created_at,
        updated_at
    ) VALUES (
        'testuser999@gmail.com',
        '+84-999-888-777',
        'Tran Thi Test 999',
        '2006-08-20'::DATE,
        'female',
        'Ho Chi Minh City, Vietnam',
        '$2a$10$slYQmyNdGzTn7ZLBXBChFOC9f6kFjAqPhccnP6DxlWXx2lPk1C3G6',
        'active',
        CURRENT_TIMESTAMP,
        CURRENT_TIMESTAMP
    )
    ON CONFLICT (email) DO NOTHING
    RETURNING id, email, full_name, phone
),
new_role AS (
    INSERT INTO user_role (user_id, role_id)
    SELECT 
        nu.id, 
        r.id
    FROM new_user nu
    CROSS JOIN (SELECT id FROM role WHERE code = 'STUDENT') r
    ON CONFLICT (user_id, role_id) DO NOTHING
    RETURNING user_id
),
new_branch_assignment AS (
    INSERT INTO user_branches (user_id, branch_id, assigned_at, assigned_by)
    SELECT 
        nu.id,
        1,
        CURRENT_TIMESTAMP,
        4
    FROM new_user nu
    ON CONFLICT (user_id, branch_id) DO NOTHING
    RETURNING user_id, branch_id
),
new_student AS (
    INSERT INTO student (
        user_id,
        student_code,
        level,
        created_at,
        updated_at
    )
    SELECT 
        nu.id,
        'S999',
        'High School',
        CURRENT_TIMESTAMP,
        CURRENT_TIMESTAMP
    FROM new_user nu
    ON CONFLICT (user_id) DO NOTHING
    RETURNING id AS student_id, user_id, student_code
),
new_assessments AS (
    INSERT INTO replacement_skill_assessment (
        student_id,
        skill,
        level_id,
        score,
        assessment_date,
        assessment_type,
        note,
        assessed_by,
        created_at,
        updated_at
    )
    SELECT 
        ns.student_id,
        skill_data.skill,
        skill_data.level_id,
        skill_data.score,
        CURRENT_DATE,
        'placement_test',
        skill_data.note,
        4,
        CURRENT_TIMESTAMP,
        CURRENT_TIMESTAMP
    FROM new_student ns
    CROSS JOIN (
        VALUES 
            ('general'::skill_enum, 2, 48, 'A2 level - Elementary'),
            ('listening'::skill_enum, 2, 45, 'Basic listening skills'),
            ('reading'::skill_enum, 2, 50, 'Good reading comprehension'),
            ('writing'::skill_enum, 2, 42, 'Writing needs practice'),
            ('speaking'::skill_enum, 2, 46, 'Basic conversation ability')
    ) AS skill_data(skill, level_id, score, note)
    RETURNING student_id, skill, score
)
SELECT 
    ns.student_id,
    ns.user_id,
    ns.student_code,
    nu.email,
    nu.full_name,
    nu.phone,
    nba.branch_id,
    COUNT(na.skill) AS assessment_count
FROM new_student ns
JOIN new_user nu ON ns.user_id = nu.id
JOIN new_branch_assignment nba ON ns.user_id = nba.user_id
LEFT JOIN new_assessments na ON ns.student_id = na.student_id
GROUP BY ns.student_id, ns.user_id, ns.student_code, nu.email, nu.full_name, nu.phone, nba.branch_id;


Step 14B: System thêm student mới vào DS khả dụng
Hệ thống thêm học viên mới vào danh sách khả dụng
Refresh danh sách để hiển thị học viên vừa thêm

Step 15B: Chuyển đến Step 20 (Chọn students từ DS đã update)
Giáo vụ tiếp tục chọn học viên (bao gồm cả học viên vừa thêm) để ghi danh

GIAI ĐOẠN 2C: OPTION C - IMPORT CSV
Step 8C: Academic Affair OPTION C: Click "Import CSV"
Giáo vụ chọn phương thức import hàng loạt từ file CSV

Step 9C: Academic Affair upload file CSV

Giáo vụ chọn và upload file CSV chứa thông tin học viên
File CSV phải tuân theo template chuẩn của hệ thống

Step 10C: System parse và validate CSV
Hệ thống đọc và phân tích file CSV
Validate từng dòng:
Format đúng (số cột, định dạng dữ liệu)
Required fields có đủ không
Email/phone đã tồn tại hay chưa

-- Validate từng dòng CSV (trong code, không phải SQL thuần)
-- Nhưng cần check email/phone duplicates batch:
-- Query để check multiple emails một lúc
SELECT email
FROM user_account
WHERE email = ANY(:emailArray);  -- PostgreSQL array parameter


-- Query để check multiple phones một lúc
SELECT phone
FROM user_account
WHERE phone = ANY(:phoneArray);
-- Query để check multiple student codes
SELECT student_code
FROM student
WHERE student_code = ANY(:studentCodeArray);
-- Kết quả: Danh sách emails/phones/codes đã tồn tại
-- Code logic sẽ so sánh với input để đánh dấu warning/error


Step 11C: System hiển thị preview
Hệ thống hiển thị preview kết quả validation:
Valid: Các bản ghi hợp lệ, sẵn sàng import
Warning: Các bản ghi có cảnh báo (ví dụ: email đã tồn tại, sẽ skip)
Error: Các bản ghi lỗi, không thể import (ví dụ: thiếu thông tin bắt buộc)

Step 12C: Academic Affair review preview data
Giáo vụ xem xét kết quả preview
Quyết định có tiếp tục import hay không

Step 13C: Academic Affair click "Import vào DS"
Giáo vụ xác nhận import các bản ghi valid vào hệ thống

Step 14C: System batch CREATE: user_account + student cho valid records
Cũng như tạo lẻ student nhưng backend chạy vòng lặp để thực hiện tạo hàng loạt

Step 15C: System cập nhật danh sách khả dụng với các học viên vừa import
Refresh danh sách để hiển thị đầy đủ

Step 16C: Chuyển đến Step 20 (Chọn students từ DS đã update)
Giáo vụ tiếp tục chọn học viên để ghi danh vào lớp

GIAI ĐOẠN 3: GHI DANH VÀO LỚP (Enrollment Process)
Step 20: Academic Affair chọn students từ DS đã update
Giáo vụ chọn (tick checkbox) các học viên cần ghi danh vào lớp
Có thể chọn một hoặc nhiều học viên

Step 21: Academic Affair click "Ghi danh vào lớp"
Giáo vụ nhấn nút "Ghi danh vào lớp" để xác nhận

Step 22: System lấy danh sách students được chọn
Hệ thống lấy danh sách tất cả học viên đã được tick checkbox

Step 23: System kiểm tra capacity
Hệ thống kiểm tra sức chứa lớp học:
Tính toán: enrolled_count (đã ghi danh) + selected (đang chọn)
So sánh với max_capacity (sức chứa tối đa)
Nếu (enrolled_count + selected) < max_capacity → OK
Nếu (enrolled_count + selected) ≥ max_capacity → Warning
-- Giả sử chọn 3 students: STD-0001, STD-0002, STD-0003
-- Tìm student_id tương ứng:
-- STD-0001 → student_id = 1 (user_id = 100)
-- STD-0002 → student_id = 2 (user_id = 101)
-- STD-0003 → student_id = 3 (user_id = 102)

WITH selected_students AS (
    SELECT student_id FROM (VALUES (1), (2), (3)) AS t(student_id)
),
class_info AS (
    SELECT 
        c.id,
        c.code,
        c.name,
        c.max_capacity,
        c.status,
        COUNT(e.id) FILTER (WHERE e.status = 'enrolled') AS current_enrolled
    FROM class c
    LEFT JOIN enrollment e ON c.id = e.class_id
    WHERE c.id = 1
    GROUP BY c.id, c.code, c.name, c.max_capacity, c.status
),
capacity_analysis AS (
    SELECT 
        ci.id AS class_id,
        ci.code AS class_code,
        ci.name AS class_name,
        ci.max_capacity,
        ci.current_enrolled,
        (SELECT COUNT(*) FROM selected_students) AS selected_count,
        ci.max_capacity - ci.current_enrolled AS available_slots,
        ci.current_enrolled + (SELECT COUNT(*) FROM selected_students) AS total_after_enrollment,
        CASE 
            WHEN ci.current_enrolled + (SELECT COUNT(*) FROM selected_students) <= ci.max_capacity 
            THEN TRUE 
            ELSE FALSE 
        END AS can_enroll_all,
        CASE 
            WHEN ci.current_enrolled + (SELECT COUNT(*) FROM selected_students) <= ci.max_capacity 
            THEN (SELECT COUNT(*) FROM selected_students)
            ELSE GREATEST(ci.max_capacity - ci.current_enrolled, 0)
        END AS enrollable_count,
        CASE 
            WHEN ci.current_enrolled + (SELECT COUNT(*) FROM selected_students) <= ci.max_capacity 
            THEN 0
            ELSE ci.current_enrolled + (SELECT COUNT(*) FROM selected_students) - ci.max_capacity
        END AS waitlist_count
    FROM class_info ci
)
SELECT 
    ca.*,
    CASE 
        WHEN ca.can_enroll_all THEN 'success'
        WHEN ca.available_slots > 0 THEN 'warning'
        ELSE 'error'
    END AS status_code,
    CASE 
        WHEN ca.can_enroll_all THEN 
            'OK: Có thể enroll tất cả ' || ca.selected_count || ' students'
        WHEN ca.available_slots > 0 THEN 
            'WARNING: Chỉ còn ' || ca.available_slots || ' chỗ trống. ' || ca.waitlist_count || ' students sẽ vào waitlist.'
        ELSE 
            'ERROR: Lớp đã full. Tất cả ' || ca.selected_count || ' students sẽ vào waitlist.'
    END AS message
FROM capacity_analysis ca;

OUTPUT:


Step 24: Academic Affair capacity OK?
Giáo vụ quyết định dựa trên kết quả kiểm tra capacity

Step 25a: [YES] Capacity OK → Chuyển đến Step 28
Nếu còn chỗ trống, tiếp tục quy trình enrollment

Step 25b: [NO] Capacity vượt mức → Step 26

Step 26: System hiển thị cảnh báo vượt capacity
Hệ thống hiển thị thông báo cảnh báo
Thông tin: Lớp sẽ vượt sức chứa tối đa X học viên
Yêu cầu giáo vụ xác nhận override

Step 27: Academic Affair override với lý do
Giáo vụ nhập lý do vượt capacity (ví dụ: "Học viên VIP", "Yêu cầu từ ban giám đốc")
Xác nhận override để tiếp tục

Step 28: System kiểm tra schedule conflict
Hệ thống kiểm tra xung đột lịch học:
Xem học viên đã có lớp nào trùng lịch không
So sánh schedule_days và time_slot của lớp hiện tại với các lớp khác mà học viên đã đăng ký
Nếu có conflict → Warning (nhưng vẫn cho phép ghi danh nếu giáo vụ xác nhận)

-- Check xem 3 students có conflict với class_id = 1 không

WITH selected_students AS (
    SELECT student_id FROM (VALUES (1), (2), (3)) AS t(student_id)
),
target_class_sessions AS (
    SELECT 
        s.id AS session_id,
        s.date AS session_date,
        tst.start_time,
        tst.end_time,
        c.code AS class_code
    FROM session s
    INNER JOIN class c ON s.class_id = c.id
    INNER JOIN time_slot_template tst ON s.time_slot_template_id = tst.id
    WHERE s.class_id = 1
      AND s.status = 'planned'
      AND s.date >= CURRENT_DATE
    LIMIT 10  -- Giới hạn để test nhanh
),
existing_student_sessions AS (
    SELECT 
        ss.student_id,
        s.date AS session_date,
        tst.start_time,
        tst.end_time,
        c.code AS class_code
    FROM student_session ss
    INNER JOIN session s ON ss.session_id = s.id
    INNER JOIN class c ON s.class_id = c.id
    INNER JOIN enrollment e ON c.id = e.class_id AND ss.student_id = e.student_id
    INNER JOIN time_slot_template tst ON s.time_slot_template_id = tst.id
    WHERE ss.student_id IN (SELECT student_id FROM selected_students)
      AND ss.attendance_status = 'planned'
      AND s.status = 'planned'
      AND s.date >= CURRENT_DATE
      AND e.status = 'enrolled'
      AND c.status IN ('scheduled', 'ongoing')
),
conflict_detection AS (
    SELECT 
        ess.student_id,
        s.student_code,
        u.full_name,
        ess.session_date,
        ess.class_code AS existing_class,
        tcs.class_code AS target_class,
        ess.start_time AS existing_start,
        ess.end_time AS existing_end,
        tcs.start_time AS target_start,
        tcs.end_time AS target_end,
        CASE 
            WHEN ess.session_date = tcs.session_date 
                 AND ess.start_time = tcs.start_time 
                 AND ess.end_time = tcs.end_time THEN 'exact_match'
            WHEN ess.session_date = tcs.session_date 
                 AND ess.start_time < tcs.end_time 
                 AND tcs.start_time < ess.end_time THEN 'partial_overlap'
            ELSE 'no_conflict'
        END AS conflict_type
    FROM existing_student_sessions ess
    CROSS JOIN target_class_sessions tcs
    INNER JOIN student s ON ess.student_id = s.id
    INNER JOIN user_account u ON s.user_id = u.id
    WHERE ess.session_date = tcs.session_date
      AND ess.start_time < tcs.end_time
      AND tcs.start_time < ess.end_time
),
conflict_summary AS (
    SELECT 
        student_id,
        student_code,
        full_name,
        COUNT(*) AS conflict_count,
        JSON_AGG(
            JSON_BUILD_OBJECT(
                'date', session_date,
                'existing_class', existing_class,
                'existing_time', existing_start || '-' || existing_end,
                'target_class', target_class,
                'target_time', target_start || '-' || target_end,
                'type', conflict_type
            ) ORDER BY session_date
        ) AS conflict_details
    FROM conflict_detection
    GROUP BY student_id, student_code, full_name
)
SELECT 
    ss.student_id,
    s.student_code,
    u.full_name,
    COALESCE(cs.conflict_count, 0) AS conflict_count,
    cs.conflict_details,
    CASE 
        WHEN cs.conflict_count > 0 THEN 'warning'
        ELSE 'success'
    END AS status,
    CASE 
        WHEN cs.conflict_count > 0 THEN 
            'WARNING: Student có ' || cs.conflict_count || ' buổi học bị conflict'
        ELSE 
            'OK: Không có conflict'
    END AS message
FROM selected_students ss
INNER JOIN student s ON ss.student_id = s.id
INNER JOIN user_account u ON s.user_id = u.id
LEFT JOIN conflict_summary cs ON ss.student_id = cs.student_id
ORDER BY 
    CASE WHEN cs.conflict_count > 0 THEN 0 ELSE 1 END,
    s.student_code;




GIAI ĐOẠN 4: XỬ LÝ TRANSACTION (Database Transaction)

Step 29: System BEGIN TRANSACTION
Hệ thống bắt đầu database transaction
Đảm bảo tính toàn vẹn dữ liệu (atomicity)

Step 30: System CREATE enrollments cho các students được chọn
Hệ thống tạo bản ghi enrollment cho từng học viên:
enrollment (class_id, student_id, enrolled_at, status='enrolled')
Cập nhật enrolled_count của lớp học

Step 31: System GENERATE student_session (cho tất cả future sessions của từng student)
Hệ thống sinh các bản ghi student_session:
Lấy tất cả session của lớp có session_date >= today (future sessions)
Với mỗi student được ghi danh:
Tạo student_session (student_id, session_id, attendance_status='planned')
Nếu học viên ghi danh muộn (mid-course), chỉ sinh student_session cho các buổi còn lại
-- THỰC THI TRANSACTION ĐỂ ENROLL 3 STUDENTS VÀO CLASS 1

BEGIN;

WITH selected_students AS (
    SELECT student_id FROM (VALUES (1), (2), (3)) AS t(student_id)
),
new_enrollments AS (
    INSERT INTO enrollment (
        class_id,
        student_id,
        status,
        enrolled_at,
        created_at,
        updated_at
    )
    SELECT 
        1,  -- class_id
        student_id,
        'enrolled'::enrollment_status_enum,
        CURRENT_TIMESTAMP,
        CURRENT_TIMESTAMP,
        CURRENT_TIMESTAMP
    FROM selected_students
    WHERE NOT EXISTS (
        SELECT 1 FROM enrollment e 
        WHERE e.class_id = 1 
          AND e.student_id = selected_students.student_id
          AND e.status = 'enrolled'
    )
    RETURNING id AS enrollment_id, class_id, student_id, status
),
future_sessions AS (
    SELECT 
        s.id AS session_id,
        s.date,
        tst.start_time
    FROM session s
    JOIN time_slot_template tst ON s.time_slot_template_id = tst.id
    WHERE s.class_id = 1
      AND s.status = 'planned'
      AND (
          s.date > CURRENT_DATE
          OR (s.date = CURRENT_DATE AND tst.start_time > CURRENT_TIME)
      )
),
new_student_sessions AS (
    INSERT INTO student_session (
        student_id,
        session_id,
        is_makeup,
        attendance_status
    )
    SELECT 
        ne.student_id,
        fs.session_id,
        FALSE,
        'planned'::attendance_status_enum
    FROM new_enrollments ne
    CROSS JOIN future_sessions fs
    WHERE NOT EXISTS (
        SELECT 1 FROM student_session ss
        WHERE ss.student_id = ne.student_id
          AND ss.session_id = fs.session_id
    )
    RETURNING student_id, session_id
)
SELECT 
    ne.enrollment_id,
    ne.student_id,
    s.student_code,
    u.full_name,
    u.email,
    ne.status AS enrollment_status,
    COUNT(nss.session_id) AS sessions_assigned,
    'Enrolled successfully' AS message
FROM new_enrollments ne
JOIN student s ON ne.student_id = s.id
JOIN user_account u ON s.user_id = u.id
LEFT JOIN new_student_sessions nss ON ne.student_id = nss.student_id
GROUP BY ne.enrollment_id, ne.student_id, s.student_code, u.full_name, u.email, ne.status;

-- COMMIT nếu OK, hoặc ROLLBACK nếu có lỗi
COMMIT;
-- ROLLBACK;


Step 32: System COMMIT TRANSACTION
Hệ thống commit transaction
Lưu tất cả thay đổi vào database
Nếu có lỗi ở bất kỳ bước nào → ROLLBACK toàn bộ

GIAI ĐOẠN 5: THÔNG BÁO & HOÀN TẤT (Notification & Completion)

Step 33: System gửi email welcome cho từng student (async)
Hệ thống gửi email thông báo cho từng học viên (chạy background job):
Thông tin lớp học: Tên lớp, mã lớp, giáo viên, phòng học
Lịch học: Ngày bắt đầu, thời gian, địa điểm
Link login hệ thống
Thông tin tài khoản: Username (email), mật khẩu mặc định (yêu cầu đổi khi đăng nhập đầu tiên)

Step 34: System hiển thị success

Step 35: Academic Affair xem thông báo thành công

Step 36: System update danh sách học viên đã enroll trên UI (refresh)

Step 37:  Academic Affair xem danh sách học viên đã enroll

GIAI ĐOẠN 6: HỌC VIÊN NHẬN THÔNG BÁO (Student Perspective)

Step 38: Student: Học viên nhận email thông báo ghi danh thành công từ hệ thống

Step 39: Student đọc thông tin
Học viên đọc thông tin trong email:
Thông tin lớp học
Lịch học chi tiết
Link đăng nhập hệ thống

Step 40: Student Login hệ thống và đổi password
Học viên truy cập link đăng nhập
Sử dụng email và mật khẩu mặc định để đăng nhập lần đầu
Hệ thống yêu cầu đổi mật khẩu mới

Step 41: Student xem lịch học cá nhân
-- Xem lịch tuần 04-10 Nov 2024 (tuần có sessions thực tế)
WITH student_sessions AS (
    SELECT
        ss.student_id,
        ss.session_id,
        ss.attendance_status,
        ss.is_makeup,
        s.date AS session_date,
        s.type AS session_type,
        s.status AS session_status,
        cs.topic AS session_topic,
        cs.sequence_no,
        tst.name AS time_slot_name,
        tst.start_time,
        tst.end_time,
        c.id AS class_id,
        c.name AS class_name,
        c.code AS class_code,
        c.modality,
        co.name AS course_name,
        b.name AS branch_name,
        b.address AS branch_address,
        r.name AS resource_name,
        r.resource_type,
        r.code AS resource_code,
        r.meeting_url,
        r.meeting_id,
        (
            SELECT u.full_name
            FROM teaching_slot ts
            JOIN teacher t ON ts.teacher_id = t.id
            JOIN user_account u ON t.user_account_id = u.id
            WHERE ts.session_id = s.id
            LIMIT 1
        ) AS teacher_name
    FROM student_session ss
    INNER JOIN session s ON ss.session_id = s.id
    INNER JOIN class c ON s.class_id = c.id
    INNER JOIN enrollment e ON c.id = e.class_id AND ss.student_id = e.student_id
    INNER JOIN course co ON c.course_id = co.id
    INNER JOIN branch b ON c.branch_id = b.id
    LEFT JOIN course_session cs ON s.course_session_id = cs.id
    LEFT JOIN time_slot_template tst ON s.time_slot_template_id = tst.id
    LEFT JOIN session_resource sr ON s.id = sr.session_id
    LEFT JOIN resource r ON sr.resource_id = r.id
    WHERE ss.student_id = 1
      AND s.date BETWEEN '2024-11-04' AND '2024-11-10'
      AND e.status = 'enrolled'
)
SELECT
    session_id,
    session_date,
    EXTRACT(ISODOW FROM session_date) AS day_of_week,
    time_slot_name,
    start_time,
    end_time,
    session_topic,
    sequence_no,
    class_name,
    class_code,
    course_name,
    branch_name,
    modality,
    attendance_status,
    is_makeup,
    resource_type,
    resource_name,
    resource_code,
    CASE
        WHEN resource_type = 'room' THEN 'Room: ' || resource_name || ' (' || resource_code || ') - ' || branch_address
        WHEN resource_type = 'virtual' THEN 'Online: ' || resource_name || COALESCE(' - ID: ' || meeting_id, '')
        ELSE 'TBA - ' || branch_address
    END AS location_display,
    meeting_url,
    teacher_name
FROM student_sessions
ORDER BY day_of_week, start_time, sequence_no;





STUDENT ENROLLMENT
GIAI ĐOẠN 1: KHỞI TẠO & XEM DANH SÁCH (Initialization)
Step 1: [Academic Affairs] truy cập vào trang chi tiết lớp học (Class Detail page)
Tại đây hiển thị thông tin lớp và danh sách học viên đã được ghi danh
SELECT 
    e.id AS enrollment_id,
    e.status AS enrollment_status,
    e.enrolled_at,
    s.id AS student_id,
    s.student_code,
    u.facebook_url,
    u.dob,
    s.level,
    u.full_name,
    u.email,
    u.phone,
    u.address,
    u.status AS account_status
FROM enrollment e
INNER JOIN student s ON e.student_id = s.id
INNER JOIN user_account u ON s.user_id = u.id
WHERE e.class_id = 3  -- Thay bằng :classId trong ứng dụng
  AND e.status IN ('enrolled', 'waitlisted')  -- Chỉ lấy students đang active
ORDER BY e.enrolled_at ASC, u.full_name ASC;


OUTPUT:
[
  {
    "enrollment_id": 26,
    "enrollment_status": "enrolled",
    "enrolled_at": "2025-09-02 15:17:35.132044+00",
    "student_id": 26,
    "student_code": "S026",
    "facebook_url": null,
    "dob": "2001-07-15",
    "level": "Beginner",
    "full_name": "Bach Thi Uyen",
    "email": "student026@gmail.com",
    "phone": "+84-916-111-111",
    "address": "140 Khuc Thua Du, Thanh Xuan, Hanoi",
    "account_status": "active"
  },
  {
    "enrollment_id": 27,
    "enrollment_status": "enrolled",
    "enrolled_at": "2025-09-02 15:17:35.132044+00",
    "student_id": 27,
    "student_code": "S027",
    "facebook_url": null,
    "dob": "1996-03-22",
    "level": "Beginner",
    "full_name": "Chu Van Vinh",
    "email": "student027@gmail.com",
    "phone": "+84-916-222-222",
    "address": "145 Nguy Nhu Kon Tum, Thanh Xuan, Hanoi",
    "account_status": "active"
  },
  …
]


Step 2: [Academic Affairs] Click "Ghi danh học viên"
Giáo vụ nhấn nút "Ghi danh học viên" để bắt đầu quy trình enrollment

Step 3: UI ngay từ lúc fetch class detail ra là đã lấy class_status để disable button đối với các lớp không phải là ongoing hoặc là scheduled

Step 4: [System] load danh sách tất cả học viên thuộc chi nhánh (branch)
Lọc ra những học viên chưa được ghi danh vào lớp nào
Danh sách này sẽ là nguồn để giáo vụ chọn
WITH enrolled_students AS (
    -- Lấy danh sách students đã enroll vào BẤT KỲ lớp nào đang active
    SELECT DISTINCT e.student_id
    FROM enrollment e
    INNER JOIN class c ON e.class_id = c.id
    WHERE e.status IN ('enrolled', 'waitlisted')  -- Đang học hoặc đang chờ
      AND c.status IN ('scheduled', 'ongoing')    -- Lớp đang chạy hoặc sắp chạy
)
SELECT DISTINCT
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
    u.last_login_at,
    s.created_at AS student_created_at,
    -- Thông tin placement test/skill assessment (aggregate các skills)
    MAX(CASE WHEN rsa.skill = 'listening' THEN rsa.score END) AS listening_score,
    MAX(CASE WHEN rsa.skill = 'reading' THEN rsa.score END) AS reading_score,
    MAX(CASE WHEN rsa.skill = 'writing' THEN rsa.score END) AS writing_score,
    MAX(CASE WHEN rsa.skill = 'speaking' THEN rsa.score END) AS speaking_score,
    MAX(CASE WHEN rsa.skill = 'general' THEN rsa.score END) AS general_score,
    MAX(CASE WHEN rsa.skill = 'listening' THEN rsa.level_id END) AS listening_level_id,
    MAX(CASE WHEN rsa.skill = 'reading' THEN rsa.level_id END) AS reading_level_id,
    MAX(CASE WHEN rsa.skill = 'writing' THEN rsa.level_id END) AS writing_level_id,
    MAX(CASE WHEN rsa.skill = 'speaking' THEN rsa.level_id END) AS speaking_level_id,
    MAX(CASE WHEN rsa.skill = 'general' THEN rsa.level_id END) AS general_level_id,
    MAX(rsa.assessment_date) AS latest_assessment_date,
    MAX(rsa.assessment_type) AS assessment_type,
    MAX(rsa.assessed_by) AS assessed_by
FROM student s
INNER JOIN user_account u ON s.user_id = u.id
-- Kiểm tra student thuộc branch của class (qua user_branches)
INNER JOIN user_branches ub ON u.id = ub.user_id
-- LEFT JOIN để lấy thông tin replacement_skill_assessment (có thể không có)
LEFT JOIN replacement_skill_assessment rsa ON s.id = rsa.student_id
-- Loại trừ students đã enroll vào bất kỳ lớp active nào
WHERE s.id NOT IN (SELECT student_id FROM enrolled_students)
  AND u.status = 'active'  -- Chỉ lấy user active
GROUP BY s.id, s.student_code, u.id, u.full_name, u.email, u.phone, u.dob, 
         u.gender, u.address, u.facebook_url, u.status, u.last_login_at, s.created_at
ORDER BY 
    latest_assessment_date DESC NULLS LAST,  -- Ưu tiên students có placement test mới nhất
    u.full_name ASC;


OUTPUT:

[
  {
    "student_id": 68,
    "student_code": "S068",
    "user_id": 93,
    "full_name": "Ly Van Yen",
    "email": "student068@gmail.com",
    "phone": "+84-924-333-333",
    "dob": "1997-04-06",
    "gender": null,
    "address": "350 Hang Thiec, Hoan Kiem, Hanoi",
    "facebook_url": null,
    "account_status": "active",
    "last_login_at": "2025-10-26 15:30:32.84771+00",
    "student_created_at": "2025-10-26 15:30:32.84771+00",
    "listening_score": null,
    "reading_score": null,
    "writing_score": null,
    "speaking_score": null,
    "general_score": 32,
    "listening_level_id": null,
    "reading_level_id": null,
    "writing_level_id": null,
    "speaking_level_id": null,
    "general_level_id": 1,
    "latest_assessment_date": "2025-10-26",
    "assessment_type": "placement_test",
    "assessed_by": 4
  },
  {
    "student_id": 69,
    "student_code": "S069",
    "user_id": 94,
    "full_name": "Mac Thi Anh",
    "email": "student069@gmail.com",
    "phone": "+84-924-444-444",
    "dob": "2000-09-23",
    "gender": null,
    "address": "355 Hang Bac, Hoan Kiem, Hanoi",
    "facebook_url": null,
    "account_status": "active",
    "last_login_at": "2025-10-26 15:30:32.84771+00",
    "student_created_at": "2025-10-26 15:30:32.84771+00",
    "listening_score": null,
    "reading_score": null,
    "writing_score": null,
    "speaking_score": null,
    "general_score": 58,
    "listening_level_id": null,
    "reading_level_id": null,
    "writing_level_id": null,
    "speaking_level_id": null,
    "general_level_id": 3,
    "latest_assessment_date": "2025-10-26",
    "assessment_type": "placement_test",
    "assessed_by": 6
  },
 …
]


Step 5: [Academic Affairs] xem danh sách học viên có thể ghi danh
Danh sách hiển thị thông tin: họ tên, email, số điện thoại, v.v.

Step 6: [System] Hiển thị 3 options
Hệ thống hiển thị 3 lựa chọn cho giáo vụ:
Option A: Chọn từ danh sách có sẵn
Option B: Thêm học viên mới (thêm lẻ)
Option C: Import CSV

Step 7: [Academic Affairs] Chọn hành động
Giáo vụ quyết định chọn một trong 3 phương thức

GIAI ĐOẠN 2A: OPTION A - CHỌN HỌC VIÊN CÓ SẴN
Step 8A: [Academic Affairs] OPTION A: Chọn student có sẵn từ danh sách

Step 9A: [System] hiển thị checkbox bên cạnh mỗi học viên trong danh sách
Giáo vụ có thể chọn một hoặc nhiều học viên

Step 10A: Nhảy đến Step 20 (Chọn students từ DS đã update)
Giáo vụ tiến hành chọn các học viên cần ghi danh

GIAI ĐOẠN 2B: OPTION B - THÊM HỌC VIÊN MỚI (LẺ)
Step 8B: [Academic Affairs] OPTION B: Click "Thêm học viên mới"
Giáo vụ chọn phương thức thêm học viên mới từng người

Step 9B: [System] Hiển thị form tạo student mới
Hệ thống mở form nhập thông tin học viên mới
Form bao gồm: Họ tên, email, số điện thoại, ngày sinh, địa chỉ, v.v.

Step 10B: [Academic Affairs] Điền form thông tin student

Step 11B: [Academic Affairs] Click "Lưu và Thêm vào DS"

Step 12B: [System] Validate input
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


Step 13B: [System] tạo bản ghi student record liên kết với user_account
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


Step 14B: [System] Thêm student mới vào DS khả dụng
Hệ thống thêm học viên mới vào danh sách khả dụng
Refresh danh sách để hiển thị học viên vừa thêm

Step 15B: Chuyển đến Step 20 (Chọn students từ DS đã update)
Giáo vụ tiếp tục chọn học viên (bao gồm cả học viên vừa thêm) để ghi danh

GIAI ĐOẠN 2C: OPTION C - IMPORT CSV
Step 8C: [Academic Affairs] OPTION C: Click "Import CSV"
Giáo vụ chọn phương thức import hàng loạt từ file CSV

Step 9C: [Academic Affairs] Upload file CSV

Giáo vụ chọn và upload file CSV chứa thông tin học viên
File CSV phải tuân theo template chuẩn của hệ thống

Step 10C: [System] Parse và validate CSV
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


Step 11C: [System] Hiển thị preview
Hệ thống hiển thị preview kết quả validation:
Valid: Các bản ghi hợp lệ, sẵn sàng import
Warning: Các bản ghi có cảnh báo (ví dụ: email đã tồn tại, sẽ skip)
Error: Các bản ghi lỗi, không thể import (ví dụ: thiếu thông tin bắt buộc)

Step 12C: [Academic Affairs] Review preview data
Giáo vụ xem xét kết quả preview
Quyết định có tiếp tục import hay không

Step 13C: [Academic Affairs] Click "Import vào DS"
Giáo vụ xác nhận import các bản ghi valid vào hệ thống

Step 14C: [System] Batch CREATE: user_account + student cho valid records
Cũng như tạo lẻ student nhưng backend chạy vòng lặp để thực hiện tạo hàng loạt

Step 15C: [System] cập nhật danh sách khả dụng với các học viên vừa import
Refresh danh sách để hiển thị đầy đủ

Step 16C: Chuyển đến Step 20 (Chọn students từ DS đã update)
Giáo vụ tiếp tục chọn học viên để ghi danh vào lớp

GIAI ĐOẠN 3: GHI DANH VÀO LỚP (Enrollment Process)
Step 20: [Academic Affairs] Chọn students từ DS đã update
Giáo vụ chọn (tick checkbox) các học viên cần ghi danh vào lớp
Có thể chọn một hoặc nhiều học viên

Step 21: [Academic Affairs] Click "Ghi danh vào lớp"
Giáo vụ nhấn nút "Ghi danh vào lớp" để xác nhận

Step 22: [System] Lấy danh sách students được chọn
Hệ thống lấy danh sách tất cả học viên đã được tick checkbox

Step 23: [System] Kiểm tra capacity
Hệ thống kiểm tra sức chứa lớp học:
Tính toán: enrolled_count (đã ghi danh) + selected (đang chọn)
So sánh với max_capacity (sức chứa tối đa)
Nếu (enrolled_count + selected) < max_capacity → OK
Nếu (enrolled_count + selected) ≥ max_capacity → Warning

WITH selected_students AS (
    SELECT UNNEST(ARRAY[51, 52, 53, 54, 55]) AS student_id
),
class_info AS (
    SELECT 
        c.id,
        c.code,
        c.name,
        c.max_capacity,
        c.status,
        COUNT(e.id) FILTER (WHERE e.status = 'enrolled') AS current_enrolled
    FROM public.class c
    LEFT JOIN public.enrollment e ON c.id = e.class_id
    WHERE c.id = 3
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
        (ci.current_enrolled + (SELECT COUNT(*) FROM selected_students)) AS total_after_enrollment,
        CASE 
            WHEN (ci.current_enrolled + (SELECT COUNT(*) FROM selected_students)) <= ci.max_capacity 
            THEN TRUE 
            ELSE FALSE 
        END AS can_enroll_all,
        CASE 
            WHEN (ci.current_enrolled + (SELECT COUNT(*) FROM selected_students)) <= ci.max_capacity 
            THEN (SELECT COUNT(*) FROM selected_students)  -- Tất cả được enroll
            ELSE GREATEST(ci.max_capacity - ci.current_enrolled, 0)  -- Số lượng có thể enroll
        END AS enrollable_count,
        CASE 
            WHEN (ci.current_enrolled + (SELECT COUNT(*) FROM selected_students)) <= ci.max_capacity 
            THEN 0
            ELSE (ci.current_enrolled + (SELECT COUNT(*) FROM selected_students)) - ci.max_capacity
        END AS waitlist_count
    FROM class_info ci
)
SELECT 
    ca.class_id,
    ca.class_code,
    ca.class_name,
    ca.max_capacity,
    ca.current_enrolled,
    ca.selected_count,
    ca.available_slots,
    ca.total_after_enrollment,
    ca.can_enroll_all,
    ca.enrollable_count,
    ca.waitlist_count,
    CASE 
        WHEN ca.can_enroll_all THEN 
            'CÓ THỂ ENROLL: Tất cả ' || ca.selected_count || ' học sinh có thể được ghi danh vào lớp.'
        WHEN ca.available_slots > 0 THEN 
            'ENROLL PARTIAL: Chỉ có thể ghi danh ' || ca.enrollable_count || ' học sinh. ' || 
            ca.waitlist_count || ' học sinh sẽ vào danh sách chờ (waitlist).'
        WHEN ca.available_slots = 0 THEN 
            'LỚP ĐÃ FULL: Không thể ghi danh thêm. Tất cả ' || ca.selected_count || 
            ' học sinh sẽ vào danh sách chờ (waitlist).'
        ELSE 
            'LỖI: Không thể xác định capacity.'
    END AS capacity_message,
    CASE 
        WHEN ca.can_enroll_all THEN 'success'
        WHEN ca.available_slots > 0 THEN 'warning'
        ELSE 'error'
    END AS status_code
FROM capacity_analysis ca;




Step 24: [Academic Affairs] Capacity OK?
Giáo vụ quyết định dựa trên kết quả kiểm tra capacity

Step 25a: [YES] Capacity OK → Chuyển đến Step 28

Nếu còn chỗ trống, tiếp tục quy trình enrollment

Step 25b: [NO] Capacity vượt mức → Step 26

Step 26: [System]  Hiển thị cảnh báo vượt capacity
Hệ thống hiển thị thông báo cảnh báo
Thông tin: Lớp sẽ vượt sức chứa tối đa X học viên
Yêu cầu giáo vụ xác nhận override

Step 27: [Academic Affairs] Override với lý do
Giáo vụ nhập lý do vượt capacity (ví dụ: "Học viên VIP", "Yêu cầu từ ban giám đốc")
Xác nhận override để tiếp tục

Step 28: [System] Kiểm tra schedule conflict
Hệ thống kiểm tra xung đột lịch học:
Xem học viên đã có lớp nào trùng lịch không
So sánh schedule_days và time_slot của lớp hiện tại với các lớp khác mà học viên đã đăng ký
Nếu có conflict → Warning (nhưng vẫn cho phép ghi danh nếu giáo vụ xác nhận)

WITH selected_students AS (
    SELECT UNNEST(ARRAY[51, 52, 53, 54, 55]) AS student_id
),
target_class_info AS (
    -- Lấy thông tin lịch học của class muốn enroll (từ sessions)
    SELECT 
        c.id,
        c.code,
        c.name,
        c.schedule_days,
        MIN(tst.start_time) AS earliest_start_time,
        MAX(tst.end_time) AS latest_end_time,
        STRING_AGG(DISTINCT tst.name, ', ') AS time_slots
    FROM public.class c
    LEFT JOIN public.session s ON c.id = s.class_id
    LEFT JOIN public.time_slot_template tst ON s.time_slot_template_id = tst.id
    WHERE c.id = 3
    GROUP BY c.id, c.code, c.name, c.schedule_days
),
existing_enrollments AS (
    -- Lấy tất cả enrollments hiện tại của selected students
    SELECT 
        e.student_id,
        e.class_id,
        c.code AS class_code,
        c.name AS class_name,
        c.schedule_days,
        MIN(tst.start_time) AS earliest_start_time,
        MAX(tst.end_time) AS latest_end_time,
        STRING_AGG(DISTINCT tst.name, ', ') AS time_slots
    FROM public.enrollment e
    JOIN public.class c ON e.class_id = c.id
    LEFT JOIN public.session s ON c.id = s.class_id
    LEFT JOIN public.time_slot_template tst ON s.time_slot_template_id = tst.id
    WHERE e.student_id IN (SELECT student_id FROM selected_students)
      AND e.status IN ('enrolled', 'waitlisted')
      AND c.status IN ('scheduled', 'ongoing')
    GROUP BY e.student_id, e.class_id, c.code, c.name, c.schedule_days
),
conflict_detection AS (
    -- Phát hiện conflicts: trùng ngày và trùng giờ
    SELECT 
        ee.student_id,
        s.student_code,
        u.full_name,
        ee.class_id AS conflicting_class_id,
        ee.class_code AS conflicting_class_code,
        ee.class_name AS conflicting_class_name,
        ee.schedule_days AS conflicting_schedule_days,
        ee.time_slots AS conflicting_time_slots,
        ee.earliest_start_time AS conflicting_start_time,
        ee.latest_end_time AS conflicting_end_time,
        tci.code AS target_class_code,
        tci.name AS target_class_name,
        tci.schedule_days AS target_schedule_days,
        tci.time_slots AS target_time_slots,
        tci.earliest_start_time AS target_start_time,
        tci.latest_end_time AS target_end_time,
        -- Kiểm tra trùng ngày
        CASE 
            WHEN ee.schedule_days && tci.schedule_days THEN TRUE
            ELSE FALSE 
        END AS has_day_overlap,
        -- Kiểm tra trùng giờ
        CASE 
            WHEN (ee.earliest_start_time < tci.latest_end_time AND tci.earliest_start_time < ee.latest_end_time) THEN TRUE
            ELSE FALSE 
        END AS has_time_overlap
    FROM existing_enrollments ee
    CROSS JOIN target_class_info tci
    JOIN public.student s ON ee.student_id = s.id
    JOIN public.user_account u ON s.user_id = u.id
    WHERE ee.schedule_days && tci.schedule_days  -- Có ngày trùng
      AND (ee.earliest_start_time < tci.latest_end_time AND tci.earliest_start_time < ee.latest_end_time)  -- Giờ trùng
),
students_with_conflicts AS (
    -- Tổng hợp conflicts cho mỗi student
    SELECT 
        student_id,
        student_code,
        full_name,
        COUNT(*) AS conflict_count,
        STRING_AGG(
            conflicting_class_code || ' (' || conflicting_class_name || ') - Days: ' || 
            conflicting_schedule_days::TEXT || ', Times: ' || conflicting_time_slots,
            '; '
            ORDER BY conflicting_class_code
        ) AS conflict_details
    FROM conflict_detection
    GROUP BY student_id, student_code, full_name
),
students_no_conflicts AS (
    -- Students không có conflict
    SELECT 
        ss.student_id,
        s.student_code,
        u.full_name
    FROM selected_students ss
    JOIN public.student s ON ss.student_id = s.id
    JOIN public.user_account u ON s.user_id = u.id
    WHERE ss.student_id NOT IN (SELECT student_id FROM students_with_conflicts)
)
-- Final output: Kết quả kiểm tra conflict
SELECT 
    'CONFLICT' AS status,
    swc.student_id,
    swc.student_code,
    swc.full_name,
    swc.conflict_count,
    swc.conflict_details,
    'WARNING: Học sinh này có lịch học trùng với lớp khác. Bạn có muốn tiếp tục ghi danh?' AS message,
    'warning' AS severity
FROM students_with_conflicts swc

UNION ALL

SELECT 
    'NO_CONFLICT' AS status,
    snc.student_id,
    snc.student_code,
    snc.full_name,
    0 AS conflict_count,
    NULL AS conflict_details,
    'OK: Không có xung đột lịch học.' AS message,
    'success' AS severity
FROM students_no_conflicts snc

ORDER BY status DESC, student_id;





GIAI ĐOẠN 4: XỬ LÝ TRANSACTION (Database Transaction)

Step 29: [System] BEGIN TRANSACTION
Hệ thống bắt đầu database transaction
Đảm bảo tính toàn vẹn dữ liệu (atomicity)

Step 30: [System] CREATE enrollments cho các students được chọn
Hệ thống tạo bản ghi enrollment cho từng học viên:
enrollment (class_id, student_id, enrolled_at, status='enrolled')
Cập nhật enrolled_count của lớp học

Step 31: [System] GENERATE student_session (cho tất cả future sessions của từng student)
Hệ thống sinh các bản ghi student_session:
Lấy tất cả session của lớp có session_date >= today (future sessions)
Với mỗi student được ghi danh:
Tạo student_session (student_id, session_id, attendance_status='planned')
Nếu học viên ghi danh muộn (mid-course), chỉ sinh student_session cho các buổi còn lại

WITH selected_students AS (
    -- Danh sách students được chọn từ UI (giả sử tick checkbox)
    SELECT UNNEST(ARRAY[51, 52, 53, 54, 55]) AS student_id
),
class_info AS (
    -- Lấy thông tin class để validate
    SELECT 
        id,
        code,
        name,
        max_capacity,
        branch_id,
        status,
        (SELECT COUNT(*) FROM public.enrollment WHERE class_id = 3 AND status = 'enrolled') AS current_enrolled
    FROM public.class
    WHERE id = 3
),
-- Get sessions của class này
target_class_sessions AS (
    SELECT 
        s.id AS session_id,
        s.date,
        s.time_slot_template_id,
        tst.start_time,
        tst.end_time
    FROM public.session s
    JOIN public.time_slot_template tst ON s.time_slot_template_id = tst.id
    WHERE s.class_id = 3
      AND s.status = 'planned'
      AND s.date >= CURRENT_DATE
),
-- Kiểm tra schedule conflict: students đã có session trùng lịch chưa?
students_with_conflicts AS (
    SELECT DISTINCT
        ss.student_id,
        COUNT(DISTINCT existing_s.id) AS conflict_count,
        STRING_AGG(DISTINCT existing_c.code, ', ') AS conflicting_classes
    FROM selected_students ss
    -- Lấy các enrollments hiện tại của student
    JOIN public.enrollment existing_e ON ss.student_id = existing_e.student_id
    JOIN public.class existing_c ON existing_e.class_id = existing_c.id
    -- Lấy sessions của các classes đó
    JOIN public.session existing_s ON existing_e.class_id = existing_s.class_id
    JOIN public.time_slot_template existing_tst ON existing_s.time_slot_template_id = existing_tst.id
    -- Check conflict với target class sessions
    JOIN target_class_sessions tcs ON 
        existing_s.date = tcs.date  -- Cùng ngày
        AND existing_s.status IN ('planned', 'done')  -- Session đang active
        AND existing_e.status IN ('enrolled', 'waitlisted')  -- Student đang học
        AND (
            -- Time overlap: (start1 < end2) AND (start2 < end1)
            (existing_tst.start_time < tcs.end_time AND tcs.start_time < existing_tst.end_time)
        )
    GROUP BY ss.student_id
),
-- Students hợp lệ (không conflict)
valid_students AS (
    SELECT student_id 
    FROM selected_students
    WHERE student_id NOT IN (SELECT student_id FROM students_with_conflicts)
),
-- Validate: Check capacity
capacity_check AS (
    SELECT 
        ci.id,
        ci.max_capacity,
        ci.current_enrolled,
        (ci.max_capacity - ci.current_enrolled) AS available_slots,
        (SELECT COUNT(*) FROM valid_students) AS valid_count
    FROM class_info ci
),
-- Step 1: Insert enrollments (chỉ cho valid students)
new_enrollments AS (
    INSERT INTO public.enrollment (
        class_id,
        student_id,
        status,
        enrolled_at,
        created_at,
        updated_at
    )
    SELECT 
        3,  -- class_id
        vs.student_id,
        CASE 
            WHEN (ROW_NUMBER() OVER (ORDER BY vs.student_id) + cc.current_enrolled) <= cc.max_capacity
            THEN 'enrolled'::enrollment_status_enum
            ELSE 'waitlisted'::enrollment_status_enum
        END AS status,
        CURRENT_TIMESTAMP,
        CURRENT_TIMESTAMP,
        CURRENT_TIMESTAMP
    FROM valid_students vs
    CROSS JOIN capacity_check cc
    RETURNING id AS enrollment_id, class_id, student_id, status
),
-- Step 2: Get all sessions của class này
class_sessions AS (
    SELECT 
        id AS session_id, 
        date,
        type,
        status
    FROM public.session
    WHERE class_id = 3
      AND status = 'planned'
      AND date >= CURRENT_DATE
),
-- Step 3: Tạo student_session cho từng student × session
new_student_sessions AS (
    INSERT INTO public.student_session (
        student_id,
        session_id,
        is_makeup,
        attendance_status
    )
    SELECT 
        ne.student_id,
        cs.session_id,
        FALSE,
        'planned'::attendance_status_enum
    FROM new_enrollments ne
    CROSS JOIN class_sessions cs
    WHERE ne.status = 'enrolled'
    RETURNING student_id, session_id
)
-- Final output: Summary với conflict detection
SELECT 
    'ENROLLED' AS result_type,
    ne.enrollment_id,
    ne.student_id,
    s.student_code,
    u.full_name,
    u.email,
    ne.status AS enrollment_status,
    ci.code AS class_code,
    ci.name AS class_name,
    COUNT(nss.session_id) AS sessions_assigned,
    NULL AS conflict_reason
FROM new_enrollments ne
JOIN public.student s ON ne.student_id = s.id
JOIN public.user_account u ON s.user_id = u.id
CROSS JOIN class_info ci
LEFT JOIN new_student_sessions nss ON ne.student_id = nss.student_id
GROUP BY ne.enrollment_id, ne.student_id, s.student_code, u.full_name, u.email, ne.status, ci.code, ci.name








Step 32: [System] COMMIT TRANSACTION
Hệ thống commit transaction
Lưu tất cả thay đổi vào database
Nếu có lỗi ở bất kỳ bước nào → ROLLBACK toàn bộ

GIAI ĐOẠN 5: THÔNG BÁO & HOÀN TẤT (Notification & Completion)

Step 33: [System] Gửi email welcome cho từng student (async)
Hệ thống gửi email thông báo cho từng học viên (chạy background job):
Thông tin lớp học: Tên lớp, mã lớp, giáo viên, phòng học
Lịch học: Ngày bắt đầu, thời gian, địa điểm
Link login hệ thống
Thông tin tài khoản: Username (email), mật khẩu mặc định (yêu cầu đổi khi đăng nhập đầu tiên)

Step 34: [System] Hiển thị success

Step 35: [Academic Affairs] Xem thông báo thành công

Step 36: [System] Update danh sách học viên đã enroll trên UI (refresh)

Step 37: [Academic Affairs] Xem danh sách học viên đã enroll

GIAI ĐOẠN 6: HỌC VIÊN NHẬN THÔNG BÁO (Student Perspective)

Step 38: [Student] Học viên nhận email thông báo ghi danh thành công từ hệ thống

Step 39: [Student] Đọc thông tin
Học viên đọc thông tin trong email:
Thông tin lớp học
Lịch học chi tiết
Link đăng nhập hệ thống

Step 40: [Student] Login hệ thống và đổi password
Học viên truy cập link đăng nhập
Sử dụng email và mật khẩu mặc định để đăng nhập lần đầu
Hệ thống yêu cầu đổi mật khẩu mới

Step 41: [Student] Xem lịch học cá nhân
Học viên xem lịch học của mình trong hệ thống
Danh sách các buổi học (sessions) được tự động sinh sẵn
Kết thúc luồng từ phía học viên

-- Input parameters:
-- :studentId - ID của học viên
-- :weekStartDate - Ngày bắt đầu tuần (Monday), format: 'YYYY-MM-DD'
-- :weekEndDate - Ngày kết thúc tuần (Sunday), format: 'YYYY-MM-DD'

WITH student_classes AS (
    -- Lấy tất cả lớp mà student đang enroll
    SELECT DISTINCT
        c.id AS class_id,
        c.name AS class_name,
        c.class_code,
        c.status AS class_status,
        c.modality,
        e.status AS enrollment_status,
        e.enrolled_at,
        co.name AS course_name,
        co.code AS course_code,
        b.name AS branch_name
    FROM enrollment e
    INNER JOIN class c ON e.class_id = c.id
    INNER JOIN course co ON c.course_id = co.id
    INNER JOIN branch b ON c.branch_id = b.id
    WHERE e.student_id = 1
      AND e.status = 'enrolled'
      AND c.status IN ('scheduled', 'ongoing')
),
student_sessions AS (
    -- Lấy tất cả sessions trong tuần cho student này
    SELECT 
        ss.student_id,
        ss.session_id,
        ss.attendance_status,
        ss.is_makeup,
        ss.homework_status,
        s.date AS session_date,
        EXTRACT(ISODOW FROM s.date) AS day_of_week,  -- 1=Monday, 7=Sunday
        s.type AS session_type,
        s.status AS session_status,
        s.teacher_note,
        cs.topic AS session_topic,
        cs.sequence_no AS session_sequence,
        cs.student_task,
        cs.skill_set,
        tst.name AS time_slot_name,
        tst.start_time,
        tst.end_time,
        tst.duration_min,
        -- Class info
        sc.class_id,
        sc.class_name,
        sc.class_code,
        sc.class_status,
        sc.modality,
        sc.course_name,
        sc.course_code,
        sc.branch_name,
        -- Resource info (room or virtual)
        r.name AS resource_name,
        r.resource_type,
        r.location AS room_location,
        r.meeting_url,
        r.meeting_id,
        -- Teacher info (primary teacher)
        (
            SELECT u.full_name
            FROM teaching_slot ts
            INNER JOIN teacher t ON ts.teacher_id = t.id
            INNER JOIN user_account u ON t.user_account_id = u.id
            WHERE ts.session_id = s.id
              AND ts.role = 'primary'
              AND ts.status = 'scheduled'
            LIMIT 1
        ) AS teacher_name,
        -- Phase info
        cp.name AS phase_name,
        cp.phase_number
    FROM student_session ss
    INNER JOIN session s ON ss.session_id = s.id
    INNER JOIN student_classes sc ON s.class_id = sc.class_id
    LEFT JOIN course_session cs ON s.course_session_id = cs.id
    LEFT JOIN time_slot_template tst ON s.time_slot_template_id = tst.id
    LEFT JOIN course_phase cp ON cs.phase_id = cp.id
    -- Get resource (room or zoom)
    LEFT JOIN session_resource sr ON s.id = sr.session_id
    LEFT JOIN resource r ON sr.resource_id = r.id
    WHERE ss.student_id = :studentId
      AND s.date BETWEEN :weekStartDate AND :weekEndDate
)
SELECT 
    session_id,
    session_date,
    day_of_week,
    CASE day_of_week
        WHEN 1 THEN 'Thứ 2'
        WHEN 2 THEN 'Thứ 3'
        WHEN 3 THEN 'Thứ 4'
        WHEN 4 THEN 'Thứ 5'
        WHEN 5 THEN 'Thứ 6'
        WHEN 6 THEN 'Thứ 7'
        WHEN 7 THEN 'Chủ nhật'
    END AS day_name,
    time_slot_name,
    start_time,
    end_time,
    duration_min,
    -- Session details
    session_topic,
    session_sequence,
    session_type,
    session_status,
    student_task,
    skill_set,
    teacher_note,
    -- Class info
    class_id,
    class_name,
    class_code,
    class_status,
    modality,
    course_name,
    course_code,
    branch_name,
    -- Attendance info
    attendance_status,
    is_makeup,
    homework_status,
    -- Location/Resource info
    CASE 
        WHEN resource_type = 'room' THEN 'Phòng: ' || resource_name || ' (' || room_location || ')'
        WHEN resource_type = 'virtual' THEN 'Online: ' || resource_name
        ELSE 'Chưa xác định'
    END AS location_display,
    resource_type,
    meeting_url,
    meeting_id,
    -- Teacher info
    teacher_name,
    -- Phase info
    phase_name,
    phase_number,
    -- Display badges
    CASE attendance_status
        WHEN 'planned' THEN 'Chưa diễn ra'
        WHEN 'present' THEN 'Có mặt'
        WHEN 'absent' THEN 'Vắng'
        WHEN 'late' THEN 'Muộn'
        WHEN 'excused' THEN 'Có phép'
        WHEN 'remote' THEN 'Học online'
    END AS attendance_badge,
    CASE session_status
        WHEN 'planned' THEN 'badge-primary'
        WHEN 'done' THEN 'badge-success'
        WHEN 'cancelled' THEN 'badge-danger'
    END AS status_badge_class,
    CASE 
        WHEN is_makeup = true THEN 'Buổi bù'
        ELSE ''
    END AS makeup_badge
FROM student_sessions
ORDER BY day_of_week, start_time, session_sequence;

-- Kết quả: Danh sách sessions theo tuần, có đủ thông tin để render calendar view
-- Frontend sẽ group by day_of_week để hiển thị theo từng cột (Thứ 2 → Chủ nhật)



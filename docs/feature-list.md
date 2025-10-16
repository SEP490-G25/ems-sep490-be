EMS (Education Management System) - Feature List
Phiên bản cập nhật dựa trên database schema thực tế và luồng nghiệp vụ đang vận hành

1. User & Role Management
   Mục tiêu: Quản lý danh tính người dùng và phân quyền theo vai trò trong hệ thống multi-tenant (center → branch).
   1.1 User Account Management
   1.1.1 Admin tạo/chỉnh sửa user account (email, phone, full_name, status)
   1.1.2 Admin gán user vào một hoặc nhiều role (VISITOR/STUDENT/TEACHER/ACADEMIC/QA/MANAGER/CENTER_HEAD/ADMIN)
   1.1.3 Admin phân quyền user theo branch (user_branches) để giới hạn phạm vi làm việc
   1.1.4 Hệ thống audit log người gán quyền (assigned_by) và thời điểm (assigned_at)
   1.2 Role-Based Access Control (RBAC)
   1.2.1 Hệ thống kiểm tra quyền truy cập dựa trên role và branch assignment
   1.2.2 Manager có quyền xem/quản lý toàn bộ branch thuộc center của mình
   1.2.3 Center Head có quyền quản lý một branch cụ thể (class, resource, enrollment)
   1.2.4 Academic Staff có quyền vận hành hằng ngày tại branch (ghi danh, tạo lớp, xử lý request)

2. Organization & Infrastructure
   Mục tiêu: Quản lý cấu trúc tổ chức (center → branch), tài nguyên (phòng/zoom), và khung giờ học.
   2.1 Center & Branch Management
   2.1.1 Manager tạo/quản lý thông tin branch (code, name, description, contact)
   2.1.2 Manager tạo/quản lý branch thuộc center (code, name, address, capacity, opening_date, status)
   2.1.3 Hệ thống đảm bảo unique constraint (center_id, branch_code)
   2.2 Resource Management (Unified Room & Zoom)
   2.2.1 Center Head tạo/quản lý resource thuộc branch với hai loại:
   ROOM: physical classroom (location, capacity, equipment)
   VIRTUAL: Zoom/online platform (meeting_url, meeting_id, account_email, license_type, expiry_date)
   2.2.2 Hệ thống cho phép một session gán nhiều resource (session_resource) với capacity_override
   2.2.3 Hệ thống kiểm tra conflict khi gán resource cho session (ngày + giờ trùng lặp)
   2.3 Time Slot Template Management
   2.3.1 Manager tạo/quản lý time_slot_template cho branch (name, start_time, end_time, duration_min)
   2.3.2 Hệ thống sử dụng slot template khi sinh session cho class và khi reschedule
   2.3.3 Đảm bảo mọi session tuân thủ một trong các slot đã định nghĩa (không cho phép giờ tự do)

3. Academic Curriculum Management
   Mục tiêu: Subject Leader xây dựng cấu trúc học thuật (Subject → Level → Course → Phase → Session mẫu) và mapping outcomes (PLO/CLO).
   3.1 Subject & Level Definition
   3.1.1 Subject Leader tạo Subject (code unique, name, description, status)
   3.1.2 Subject Leader tạo Level thuộc Subject (code unique theo subject, name, standard_type, expected_duration_hours, sort_order)
   3.1.3 Hệ thống đảm bảo constraint unique(subject_id, level_code)
   3.2 Course Creation & Versioning
   3.2.1 Subject Leader tạo Course thuộc Subject + Level (code unique, version, total_hours, duration_weeks, session_per_week, hours_per_session)
   3.2.2 Subject Leader điền thông tin chi tiết (description, prerequisites, target_audience, teaching_methods, effective_date)
   3.2.3 Hệ thống lưu hash_checksum để phát hiện thay đổi nội dung
   3.2.4 Subject Leader gửi duyệt course → Manager/Center Head phê duyệt (approved_by_manager, approved_at) hoặc từ chối (rejection_reason)
   3.2.5 Chỉ course đã được duyệt mới được sử dụng để mở lớp
   3.3 Course Structure (Phase & Session Template)
   3.3.1 Subject Leader chia Course thành các Phase (phase_number unique theo course, name, duration_weeks, learning_focus, sort_order)
   3.3.2 Subject Leader tạo Course_Session (buổi mẫu) cho từng Phase:
   sequence_no (thứ tự buổi trong phase)
   topic, student_task
   skill_set[] (general/reading/writing/speaking/listening)
   3.3.3 Constraint: unique(phase_id, sequence_no) đảm bảo không trùng buổi
   3.4 Learning Outcomes Management (PLO & CLO)
   3.4.1 Subject Leader định nghĩa PLO (Program Learning Outcome) ở cấp Subject (code unique theo subject, description)
   3.4.2 Subject Leader định nghĩa CLO (Course Learning Outcome) ở cấp Course (code unique theo course, description)
   3.4.3 Subject Leader mapping PLO ↔ CLO (plo_clo_mapping) với validation: PLO và CLO phải thuộc cùng subject
   3.4.4 Subject Leader mapping CLO ↔ Course_Session (course_session_clo_mapping) để truy vết "buổi nào đạt CLO nào"
   3.4.5 QA sử dụng mapping này để đảm bảo không có CLO nào bị thiếu trong syllabus
   3.5 Course Material Management
   3.5.1 Subject Leader upload tài liệu (course_material) gắn vào:
   Cấp Course (chung cho toàn khóa)
   Cấp Phase (chung cho giai đoạn)
   Cấp Course_Session (theo buổi cụ thể)
   3.5.2 Hệ thống đảm bảo ít nhất một trong {phase_id, course_session_id} phải có giá trị
   3.5.3 Hệ thống lưu thông tin người upload (uploaded_by) và timestamp

4. Teacher Management
   Mục tiêu: Quản lý hồ sơ giáo viên, kỹ năng giảng dạy, lịch rảnh, và khả dụng (availability).
   4.1 Teacher Profile Management
   4.1.1 Admin tạo teacher record (1-1 mapping với user_account, employee_code unique, note)
   4.1.2 Admin/Manager gán teacher vào branch thông qua user_branches
   4.2 Teacher Skill Management
   4.2.1 Manager định nghĩa teacher_skill (teacher_id, skill enum, level 1-5/CEFR)
   4.2.2 Hệ thống sử dụng skill để gợi ý teacher phù hợp khi phân công session (dựa vào course_session.skill_set)
   4.2.3 Constraint: primary key (teacher_id, skill) đảm bảo mỗi kỹ năng chỉ ghi một lần
   4.3 Teacher Availability Management
   4.3.1 Teacher khai báo lịch rảnh thường xuyên (teacher_availability):
   day_of_week (0=Sun..6=Sat)
   start_time, end_time
   note
   4.3.2 Teacher khai báo lịch rảnh ngoại lệ (teacher_availability_override):
   date cụ thể
   start_time, end_time
   is_available (TRUE=rảnh, FALSE=không rảnh)
   reason
   4.3.3 Hệ thống ưu tiên kiểm tra override trước khi kiểm tra availability thường xuyên khi tìm giáo viên thay thế
   4.4 Teacher Overtime (OT) Registration
   4.4.1 Teacher chủ động đăng ký lịch OT (teacher_availability_override với is_available=TRUE)
   4.4.2 Khi có nhu cầu thay thế, hệ thống tìm kiếm giáo viên đã đăng ký OT trong khung giờ đó
   4.4.3 Academic Staff phân công teacher OT → hệ thống tự động tạo teacher_request(type='ot', status='approved') để ghi nhận cho tính lương

5. Class & Session Management
   Mục tiêu: Academic Staff tạo lớp dựa trên course template, sinh tự động session, phân công teacher và resource; Center Head/Manager duyệt.
   5.1 Class Creation
   5.1.1 Academic Staff tạo class mới:
   Chọn branch, course (đã được duyệt)
   Điền code (unique theo branch), name, modality (OFFLINE/ONLINE/HYBRID)
   Chọn start_date, schedule_days[] (mảng ngày học trong tuần), max_capacity
   Trạng thái khởi tạo: status='draft'
   5.1.2 Hệ thống validate:
   Course đã được approved_by_manager
   Code không trùng trong branch (unique constraint)
   Schedule_days hợp lệ (0..6)
   5.2 Auto-Generate Sessions from Course Template
   5.2.1 Hệ thống tự động sinh session dựa trên:
   Course → Phase → Course_Session (buổi mẫu theo sequence_no)
   Class.start_date + schedule_days + time_slot_template (giờ học cho từng ngày)
   5.2.2 Logic sinh session:
   Lấy toàn bộ course_session của course theo thứ tự (phase_number, sequence_no)
   Với mỗi course_session, tính ngày diễn ra dựa trên:
   global_sequence = ROW_NUMBER() của buổi trong toàn bộ course
   week_index = floor((global_sequence - 1) / array_length(schedule_days))
   day_index = ((global_sequence - 1) % array_length(schedule_days)) + 1
   session_date = start_date + offset_to_first(schedule_day) + week_index * 7 ngày
   Lấy start_time/end_time từ mapping time_slot_template theo schedule_day
   Insert vào session với course_session_id tương ứng, type='CLASS', status='planned'
   5.2.3 Hệ thống validate sau khi sinh:
   Mọi session phải nằm trong schedule_days và giờ phải khớp time_slot_template
   Không có session nào bị trùng lặp (nếu có unique constraint)
   5.3 Resource Assignment
   5.3.1 Academic Staff gán resource (phòng/zoom) cho session:
   Hệ thống gợi ý resource còn trống trong khung giờ (query kiểm tra conflict)
   Insert vào session_resource(session_id, resource_type, resource_id, capacity_override)
   5.3.2 Hệ thống kiểm tra conflict:
   Resource không bị trùng giờ với session khác (same date, time overlap)
   Query conflict sử dụng join session_resource + session để phát hiện chồng chéo
   5.4 Teacher Assignment
   5.4.1 Academic Staff phân công teacher cho session:
   Hệ thống gợi ý teacher dựa trên:
   teacher_skill khớp với course_session.skill_set
   teacher_availability/override rảnh trong khung giờ session
   Không bị trùng lịch (check teaching_slot + session)
   Insert vào teaching_slot(session_id, teacher_id, skill, role='primary'/'assistant')
   5.4.2 Hệ thống cho phép một session có nhiều teacher (khác skill) hoặc một teacher dạy nhiều skill
   5.4.3 Constraint: primary key (session_id, teacher_id, skill) đảm bảo không trùng kỹ năng trong một session
   5.5 Class Approval Workflow
   5.5.1 Academic Staff submit class để duyệt (cập nhật submitted_at)
   5.5.2 Manager/Center Head review và phê duyệt (cập nhật approved_by, approved_at, status='scheduled')
   5.5.3 Nếu từ chối, điền rejection_reason và trả lại status='draft'
   5.5.4 Chỉ class đã được approved mới có thể ghi danh học viên
   5.6 Class Schedule Modification (Reschedule)
   5.6.1 Academic Staff/Center Head có thể đổi lịch toàn bộ buổi học của class:
   Chọn khung giờ mới từ time_slot_template
   Chọn ngày hiệu lực (effective_date)
   Chọn target_dow (nếu chỉ đổi một ngày trong tuần) hoặc NULL (đổi tất cả)
   5.6.2 Hệ thống thực thi:
   Update session.start_time, end_time cho tất cả session status='planned' và date >= effective_date
   Ghi log vào session.teacher_note
   5.6.3 Hệ thống validate sau khi đổi:
   Kiểm tra conflict resource/teacher với các lớp khác
   Gợi ý resource/teacher mới nếu có xung đột

6. Enrollment & Student Management
   Mục tiêu: Academic Staff ghi danh học viên vào lớp, sinh tự động lịch cá nhân (student_session), đồng bộ khi có thay đổi.
   6.1 Student Account Creation
   6.1.1 Academic Staff import CSV hoặc tạo học viên lẻ:
   Kiểm tra user_account tồn tại (email/phone)
   Nếu chưa có: tạo user_account → tạo student record (1-1 mapping, student_code unique)
   Nếu đã có user nhưng chưa có student: chỉ tạo student
   6.1.2 Hệ thống gán student vào branch (student.branch_id hoặc user_branches)
   6.2 Enrollment Process
   6.2.1 Academic Staff ghi danh student vào class:
   Input: student_id, class_id
   Validate:
   Class chưa full (enrolled count < max_capacity) – có thể override nếu policy cho phép
   Student chưa bị trùng lịch với lớp khác (check student_session overlap)
   Insert enrollment(class_id, student_id, status='enrolled', enrolled_at=now())
   6.2.2 Hệ thống ghi nhận created_by (user_account của Academic Staff)
   6.3 Auto-Generate Student Schedule
   6.3.1 Sau khi enrollment, hệ thống tự động sinh student_session:
   Clone toàn bộ session của class (status='planned')
   Insert student_session(student_id, session_id, attendance_status='planned', is_makeup=false)
   6.3.2 Đây là lịch cá nhân của học viên, nơi lưu trạng thái điểm danh và cờ make-up
   6.4 Schedule Synchronization
   6.4.1 Khi class thay đổi (đổi lịch, hủy buổi, thêm buổi mới), hệ thống tự động cập nhật student_session của tất cả enrolled students
   6.4.2 Khi teacher thay đổi (substitution), student_session không thay đổi (vì teacher gắn ở teaching_slot)
   6.4.3 Khi resource thay đổi (đổi phòng/zoom), student_session không thay đổi (vì resource gắn ở session_resource)
   6.5 Late Enrollment (Join mid-course)
   6.5.1 Academic Staff có thể ghi danh học viên vào lớp đã chạy (≤ X buổi)
   6.5.2 Hệ thống sinh student_session chỉ cho các buổi tương lai (session.date >= enrollment_date)
   6.5.3 Ghi nhận join_session_id trong enrollment để audit mốc học viên vào lớp

7. Attendance & Session Reporting
   Mục tiêu: Teacher điểm danh học viên, báo cáo buổi học, nhập điểm/nhận xét; hệ thống lock dữ liệu sau thời gian quy định.
   7.1 Attendance Recording
   7.1.1 Teacher mở danh sách học viên của session (query student_session where session_id + attendance_status in planned/present/late)
   7.1.2 Teacher cập nhật attendance_status cho từng học viên:
   present, absent, late, excused, remote
   Ghi note nếu cần (lý do nghỉ, ghi chú riêng)
   recorded_at = timestamp điểm danh
   7.1.3 Hệ thống không cho phép điểm danh trước ngày session.date (validate trên backend)
   7.2 Session Report
   7.2.1 Teacher báo cáo buổi học (session.teacher_note):
   Sĩ số (tự động từ attendance)
   Nội dung đã dạy (tham chiếu course_session.topic)
   Trạng thái buổi (session.status: planned → done/cancelled)
   7.2.2 Hệ thống cập nhật session.status='done' sau khi teacher submit báo cáo
   7.2.3 Manager có thể lock điểm danh sau T giờ để đảm bảo dữ liệu ổn định cho báo cáo
   7.3 Homework & Task Tracking
   7.3.1 Teacher sử dụng link BTVN (course_material) theo buổi
   7.3.2 Teacher đánh dấu hoàn thành BTVN cho từng học viên (có thể mở rộng với bảng riêng nếu cần chi tiết)
   7.4 Score & Feedback Entry
   7.4.1 Teacher nhập điểm cho học viên theo assessment (gắn vào session hoặc phase):
   Insert score(assessment_id, student_id, score, feedback, graded_by=teacher_id, graded_at=now())
   7.4.2 Teacher nhập nhận xét riêng tư (private) theo phase hoặc session (lưu ở trường feedback trong score hoặc riêng)
   7.4.3 Hệ thống hiển thị điểm/nhận xét cho student theo policy (hiển thị ngay hoặc sau khi kết thúc phase)

8. Request & Approval Flows
   Mục tiêu: Xử lý các yêu cầu thay đổi lịch học từ Student và Teacher; Academic Staff/Manager duyệt; hệ thống tự động cập nhật dữ liệu.
   8.1 Student Requests
   8.1.1 Absence Request
   Student tạo student_request(type='absence', target_session_id, reason)
   Academic Staff duyệt → cập nhật student_session.attendance_status='excused'
   Validation: không gửi request cho buổi đã diễn ra
   8.1.2 Make-up Request (Học bù)
   Step 1: Student chọn buổi gốc bị nghỉ (target_session_id)
   Step 2: Hệ thống gợi ý danh sách buổi học bù khả dụng:
   Cùng course_session_id (cùng nội dung)
   Khác class/ngày nhưng cùng branch/modality
   Status='planned', date >= CURRENT_DATE
   Capacity available (có thể override)
   Step 3: Student chọn makeup_session_id và submit request
   Step 4: Academic Staff duyệt:
   Validate: hai buổi có cùng course_session_id
   Validate: makeup_session chưa full (check capacity)
   Cập nhật student_session.attendance_status='excused' cho buổi gốc
   Insert student_session(student_id, makeup_session_id, is_makeup=true, attendance_status='planned')
   Step 5: Teacher của buổi học bù thấy thêm học viên này trong danh sách điểm danh
   8.1.3 Transfer Request (Chuyển lớp)
   Trigger: Student đang học giữa chừng, muốn đổi modality/branch/schedule
   Input: current_class_id, target_class_id, effective_date (ngày bắt đầu chuyển)
   Validation:
   Hai lớp phải cùng course_id (để map phần còn lại theo course_session_id)
   Target class status in (scheduled, ongoing)
   Target class còn chỗ (capacity check)
   Approval flow:
   Academic Staff duyệt → hệ thống thực thi trong 1 transaction:
   Xác định mốc chuyển (cutoff):
   left_session_id = buổi cuối cùng đã học ở lớp A (date < effective_date)
   join_session_id = buổi đầu tiên sẽ học ở lớp B (date >= effective_date)
   Cập nhật enrollment A: status='transferred', left_at=now(), left_session_id
   Tạo enrollment B: status='enrolled', enrolled_at=now(), join_session_id
   Đánh dấu excused các student_session tương lai ở lớp A (date >= effective_date)
   Sinh student_session mới cho lớp B:
   Nếu A và B cùng course_id: map theo course_session_id (phần còn lại)
   Nếu khác course_id: join vào toàn bộ session tương lai của B (không map)
   Edge case: Lớp B đã dạy qua một số buổi mà A chưa học → buổi đó bị thiếu → giải pháp:
   Bỏ qua (student xem lại record)
   Hoặc chuyển muộn hơn (effective_date lùi lại)
   8.1.4 Reschedule Request (Đổi ca trong cùng lớp)
   Student yêu cầu đổi sang một ngày/giờ khác trong cùng lớp (ít phổ biến, có thể kết hợp với make-up)
   8.2 Teacher Requests
   8.2.1 Leave Request (Xin nghỉ)
   Step 1: Teacher tạo teacher_request(type='leave', session_id, reason)
   Step 2: Academic Staff/Manager tìm giải pháp:
   Option A: Tìm giáo viên thay thế
   Hệ thống gợi ý teacher khả dụng (call function find_available_substitute_teachers):
   Skill khớp với course_session.skill_set
   Availability/override rảnh trong khung giờ
   Không trùng lịch dạy
   Ưu tiên teacher đã đăng ký OT
   Academic Staff chọn substitute teacher → Duyệt trong 1 transaction:
   Update teacher_request: status='approved', resolution='Teacher X sẽ dạy thay'
   Insert teacher_request(substitute_teacher_id, type='ot', status='approved') để ghi nhận OT
   Update teaching_slot: teacher_id = substitute_teacher_id
   Option B: Đổi lịch buổi học (reschedule) – xem 8.2.3
   Option C: Hủy buổi học (cancellation) – xem 8.2.4
   Step 3: Nếu không tìm được giải pháp, yêu cầu vẫn ở pending hoặc chuyển sang Option B/C
   8.2.2 Overtime (OT) Request (Nhận dạy thêm)
   Teacher chủ động đăng ký lịch OT (teacher_availability_override với is_available=TRUE)
   Khi có nhu cầu thay thế, hệ thống tự động tìm và gợi ý teacher này
   Academic Staff phân công → hệ thống tự động tạo teacher_request(type='ot', status='approved')
   8.2.3 Reschedule Request (Đổi lịch buổi học)
   Trigger: Không tìm được teacher thay thế hoặc teacher/student yêu cầu đổi lịch
   Input: session_id cũ, new_date, chosen_slot_id (từ time_slot_template)
   Validation query: Tìm slot hợp lệ:
   Slot thuộc branch của lớp
   Teacher không trùng lịch vào khung giờ mới
   Resource không trùng lịch vào khung giờ mới
   Execution transaction:
   Tạo session mới với thông tin từ session cũ + new_date + slot mới
   Chuyển tất cả liên kết sang session mới:
   Update student_session: session_id = new_session_id
   Update teaching_slot: session_id = new_session_id
   Update session_resource: session_id = new_session_id
   Đánh dấu session cũ: status='cancelled'
   Notification: Hệ thống gửi thông báo tới tất cả enrolled students về thay đổi lịch
   8.2.4 Cancellation Request (Hủy buổi học)
   Trigger: Không tìm được teacher thay thế và không thể đổi lịch
   Execution:
   Update session: status='cancelled', teacher_note='Hủy do không có GV'
   Update student_session: attendance_status='excused' cho tất cả học viên
   Notification: Gửi thông báo hàng loạt tới học viên
   8.2.5 Swap Request (Đổi ca với giáo viên khác)
   Teacher A và Teacher B thỏa thuận đổi buổi dạy → cả hai submit request
   Academic Staff duyệt → swap teaching_slot giữa hai session
   8.3 Admin-initiated Request (Giáo vụ tạo thay)
   Academic Staff có thể tạo request thay cho student/teacher khi nhận yêu cầu qua kênh khác (phone, email)
   Hệ thống ghi nhận submitted_by (user_account của Academic Staff) và lý do trong note
   Quy trình duyệt tương tự như request thông thường

9. Assessment & Feedback Management
   Mục tiêu: Quản lý bài kiểm tra offline (nhập điểm từ Excel/manual), feedback học viên, và QA report.
   9.1 Assessment Definition
   9.1.1 Academic Staff/Teacher tạo assessment cho class:
   name (VD: PT1, Midterm, Final)
   kind (quiz/midterm/final/assignment/project/oral/practice/other)
   max_score, weight (% trong tổng điểm)
   session_id (nếu bài kiểm tra gắn vào buổi cụ thể)
   9.1.2 Hệ thống đảm bảo tổng weight không vượt 100% (validate trên backend)
   9.2 Score Entry (Offline)
   9.2.1 Teacher/Academic Staff nhập điểm offline:
   Import CSV từ Excel (assessment_id, student_id, score, feedback)
   Hoặc nhập lẻ trên giao diện
   9.2.2 Insert score(assessment_id, student_id, score, feedback, graded_by=teacher_id, graded_at=now())
   9.2.3 Constraint: unique(assessment_id, student_id) đảm bảo một học viên chỉ có một điểm cho mỗi bài
   9.3 Student Feedback (Voice-of-Student)
   9.3.1 Student đánh giá sau buổi học hoặc sau phase:
   Insert student_feedback(student_id, session_id, phase_id, rating 1-5, comment, submitted_at)
   9.3.2 Hệ thống tổng hợp rating theo lớp/phase để QA và Manager review
   9.3.3 QA sử dụng feedback để đánh giá chất lượng giảng dạy và đề xuất cải thiện
   9.4 QA Report Management
   9.4.1 QA tạo qa_report để ghi nhận vấn đề:
   Scope: class_id, session_id, hoặc phase_id
   report_type (checklist/process/observation/etc.)
   status (open/in_progress/resolved/closed)
   findings (phát hiện vấn đề)
   action_items (hành động khắc phục)
   9.4.2 QA theo dõi tiến độ lớp vs syllabus (session đã dạy vs course_session expected)
   9.4.3 QA đánh giá teacher theo tiêu chí (thái độ, phương pháp, tuân thủ syllabus)
   9.4.4 QA tổng hợp báo cáo định kỳ gửi Manager/Center Head

10. Reporting & Analytics
    Mục tiêu: Cung cấp dashboard và báo cáo cho các cấp quản lý (Manager, Center Head) để theo dõi KPI và ra quyết định.
    10.1 Enrollment Reports
    10.1.1 Tổng quan học viên theo branch/level/term (enrolled/waitlisted/transferred/dropped/completed)
    10.1.2 Fill Rate theo lớp (enrolled count / max_capacity) với cảnh báo thiếu/quá tải
    10.1.3 Funnel "trial → enroll" và late-join statistics (≤ X buổi)
    10.2 Attendance Reports
    10.2.1 Tỷ lệ chuyên cần theo lớp/chi nhánh:
    Attendance Rate = (số lượt present) / (số buổi đã diễn ra) × 100%
    Tổng hợp theo phase và toàn khóa
    Grain: Weekly/Monthly
    10.2.2 Top absences & cảnh báo học viên vượt ngưỡng nghỉ
    10.2.3 Run-sheet hôm nay: danh sách lớp thiếu điểm danh/báo cáo buổi (alert sau T giờ)
    10.3 Class Utilization Reports
    10.3.1 Room/Zoom Utilization = (giờ đã sử dụng) / (giờ khả dụng) theo slot/branch
    10.3.2 Xung đột & thay đổi lịch (số lần reschedule, cancel, make-up theo lớp)
    10.3.3 Overrun/Underrun: lớp kết thúc sớm/trễ so với planned_end_date
    10.4 Teacher Workload Reports
    10.4.1 Tải giảng dạy theo teacher:
    Tổng số giờ dạy = SUM(session end_time - start_time) / 3600
    Số lớp đang dạy, số buổi trong tuần
    Grain: Weekly/Monthly
    10.4.2 Lịch teacher tuần này & cảnh báo conflict
    10.4.3 Teacher request statistics:
    Số lần xin nghỉ/đổi lịch, số lần substitution
    Lead-time thay GV (thời gian từ request → approved)
    Số giờ OT theo teacher
    10.5 Syllabus Progress & CLO Coverage
    10.5.1 Tiến độ lớp vs syllabus:
    % hoàn thành = (số buổi đã dạy) / (tổng số course_session) × 100%
    Danh sách session thiếu/thừa so với template
    10.5.2 Ma trận CLO coverage:
    Mapping session ↔ CLO: kiểm tra có CLO nào chưa được dạy
    Highlight thiếu/phủ CLO theo class
    10.6 Assessment & Attainment Reports
    10.6.1 Phân bố điểm theo lớp/level/skill (histogram, box plot)
    10.6.2 Tỷ lệ đỗ (pass rate) theo assessment kind
    10.6.3 CLO Attainment Summary:
    Điểm trung bình của học viên trên các assessment được map với từng CLO
    So sánh attainment giữa các lớp/cohort
    10.6.4 Correlation: chuyên cần vs kết quả học tập (scatter plot, regression)
    10.7 Feedback & QA Reports
    10.7.1 Feedback rating theo lớp/phase:
    Trung bình rating 1-5, phân bố sao
    Filter: branch, class, teacher, date range
    10.7.2 Xếp hạng teacher theo tháng (tổng hợp QA score + feedback rating + attendance rate của lớp)
    10.7.3 QA report summary: số vấn đề open/resolved, action items pending
    10.8 Request & SLA Reports
    10.8.1 SLA xử lý request:
    Thời gian từ submitted → decided (trung bình, median, max)
    Theo loại request (absence/makeup/transfer/leave/reschedule/ot)
    10.8.2 Thống kê request theo status: pending/approved/rejected/cancelled
    10.8.3 Tỷ lệ chấp thuận/từ chối theo loại request và theo approver
    10.9 Executive Dashboards
    10.9.1 Center Head Dashboard:
    Tổng quan: số branch, số lớp đang chạy, tổng học viên
    Top 3 branch theo enrollment/revenue/attendance rate
    Trend: enrollment, attendance rate, CLO attainment theo tháng
    10.9.2 Manager Dashboard:
    Vận hành ngày: lớp cần điểm danh, conflict cần giải quyết
    Cảnh báo: teacher vượt ngưỡng nghỉ/đổi, lớp fill rate thấp, học viên sắp vượt ngưỡng nghỉ
    Hiệu suất: utilization phòng/zoom, tiến độ lớp vs syllabus
    10.10 Export & Scheduling
    10.10.1 Export CSV/Excel cho tất cả báo cáo
    10.10.2 Lịch gửi báo cáo tự động qua email (daily/weekly) cho Manager/Center Head
    10.10.3 Subscribe to alerts: email/SMS khi có cảnh báo (conflict, low attendance, missing report)

11. System Configuration & Integration
    Mục tiêu: Cấu hình hệ thống, tích hợp dịch vụ bên ngoài, và quản lý audit trail.
    11.1 System Settings
    11.1.1 Admin cấu hình các tham số toàn hệ thống:
    Ngưỡng nghỉ tối đa cho học viên (số buổi)
    Ngưỡng đổi lịch/nghỉ tối đa cho teacher (số lần/tháng)
    Thời gian lock điểm danh sau buổi học (T giờ)
    Lead-time tối thiểu cho request (số ngày trước buổi học)
    Policy capacity override (có cho phép nhét thêm học viên không)
    11.1.2 Admin cấu hình theo branch (có thể khác nhau giữa các branch)
    11.2 Notification Management
    11.2.1 Hệ thống gửi notification qua nhiều kênh:
    Email (class approved, schedule changed, request approved/rejected)
    SMS (urgent: class cancelled, teacher change)
    In-app notification (portal)
    11.2.2 Template quản lý theo loại sự kiện (event-driven)
    11.2.3 User có thể tùy chỉnh notification preferences (email/SMS/push)
    11.3 File Storage Integration (S3/Cloud)
    11.3.1 Course_material upload lên S3, lưu URL vào database
    11.3.2 Import CSV (enrollment, score) upload tạm, parse và validate, sau đó delete
    11.3.3 Export reports lưu tạm trên S3, generate presigned URL cho download
    11.4 Authentication & Authorization
    11.4.1 Tích hợp OAuth2/SSO nếu cần (Google, Microsoft)
    11.4.2 JWT-based authentication cho API
    11.4.3 Multi-factor authentication cho role nhạy cảm (Admin, Center Head)
    11.4.4 Session management: logout khỏi tất cả thiết bị, force logout khi đổi mật khẩu
    11.5 Audit Trail & Logging
    11.5.1 Hệ thống log mọi thao tác quan trọng:
    Enrollment: created_by, enrolled_at
    Class approval: approved_by, approved_at
    Request approval: decided_by, decided_at
    Score entry: graded_by, graded_at
    Teacher/resource assignment: created_by trong các bảng liên quan
    11.5.2 Audit log table (tùy chọn nếu cần chi tiết hơn):
    user_id, action_type, entity_type, entity_id, old_value, new_value, timestamp
    11.5.3 Admin/Manager có thể query audit log để điều tra sự cố
    11.6 Backup & Data Retention
    11.6.1 Daily backup database tự động
    11.6.2 Soft delete policy: không xoá student_session, enrollment quá khứ (chỉ đánh dấu status)
    11.6.3 Archive dữ liệu cũ sau N năm (chuyển sang cold storage)
    11.7 Health Check & Monitoring
    11.7.1 API health check endpoint cho monitoring system
    11.7.2 Alert khi service down, database connection fail, hoặc query chậm
    11.7.3 Performance metrics: response time, throughput, error rate

12. Advanced Features (vNext / Optional)
    Mục tiêu: Các tính năng mở rộng cho giai đoạn sau khi hệ thống ổn định.
    12.1 Auto-Suggest Features
    12.1.1 Hệ thống gợi ý teacher phù hợp khi tạo lớp (dựa trên skill, availability, workload hiện tại)
    12.1.2 Hệ thống gợi ý học viên phù hợp cho make-up session (dựa trên lịch cá nhân, course_session_id)
    12.1.3 Hệ thống gợi ý slot/resource tối ưu khi reschedule (tránh conflict, tối ưu utilization)
    12.2 Waitlist Management
    12.2.1 Student đăng ký vào waitlist khi class đã full
    12.2.2 Hệ thống tự động notify khi có chỗ trống (student drop out)
    12.2.3 Academic Staff duyệt từ waitlist theo thứ tự ưu tiên (first-come-first-serve hoặc policy khác)
    12.3 Payment & Billing Integration
    12.3.1 Tích hợp payment gateway (VNPay, Momo, Stripe)
    12.3.2 Tự động tạo invoice khi enrollment, track payment status
    12.3.3 Refund logic khi student drop/transfer trước deadline
    12.4 Advanced Scheduling Algorithms
    12.4.1 Auto-scheduling: tự động phân công teacher/resource cho toàn bộ term dựa trên constraints và optimization
    12.4.2 Conflict resolution: đề xuất giải pháp tự động khi phát hiện conflict
    12.4.3 Load balancing: cân bằng tải giảng dạy giữa các teacher
    12.5 Mobile App Support
    12.5.1 Student mobile app: xem lịch, điểm danh bằng QR code, nhận notification, xem điểm
    12.5.2 Teacher mobile app: điểm danh nhanh, báo cáo buổi, xem lịch tuần
    12.5.3 Offline mode: sync dữ liệu khi có internet
    12.6 Learning Management System (LMS) Integration
    12.6.1 Sync course_material với LMS (Moodle, Canvas)
    12.6.2 Single sign-on giữa EMS và LMS
    12.6.3 Fetch assignment submission từ LMS về EMS để tracking
    12.7 Video Conferencing Integration
    12.7.1 Tự động tạo Zoom meeting khi session có resource_type='VIRTUAL'
    12.7.2 Embed Zoom link vào student portal, một click để join
    12.7.3 Track attendance từ Zoom report (join time, leave time)
    12.8 AI/ML Features
    12.8.3 Sentiment analysis: phân tích student feedback comment để phát hiện vấn đề

13. Data Migration & Onboarding
    Mục tiêu: Hỗ trợ import dữ liệu từ hệ thống cũ và onboard người dùng mới vào hệ thống.
    13.1 Data Import Tools
    13.1.1 Import wizard cho các entity chính:
    User accounts (CSV: email, phone, full_name, role)
    Students (CSV: student_code, user_email, branch)
    Teachers (CSV: employee_code, user_email, skills)
    Courses (JSON/CSV: structure phức tạp)
    Classes (CSV: code, course_code, branch_code, start_date, schedule_days)
    Enrollments (CSV: student_code, class_code)
    13.1.2 Validation & preview trước khi commit:
    Kiểm tra foreign key tồn tại
    Kiểm tra unique constraint
    Hiển thị lỗi chi tiết (row number, field name, error message)
    13.1.3 Batch import với transaction: rollback toàn bộ nếu có lỗi
    13.1.4 Import log: lưu lại file đã import, số record thành công/thất bại, timestamp
    13.2 User Onboarding
    13.2.1 Welcome email với hướng dẫn kích hoạt tài khoản
    13.2.2 First-time login wizard:
    Student: xem lịch học, cách sử dụng portal
    Teacher: hướng dẫn điểm danh, báo cáo buổi
    Academic Staff: hướng dẫn tạo lớp, ghi danh, xử lý request
    13.2.3 In-app tutorial: tooltips, guided tour cho các chức năng chính
    13.2.4 Help center: FAQ, video tutorials, contact support
    13.3 Bulk Operations
    13.3.1 Academic Staff bulk assign students vào lớp (select multiple students → assign to class)
    13.3.2 Manager bulk assign teachers vào sessions (select multiple sessions → assign teacher)
    13.3.3 Admin bulk update user roles/branches
    13.3.4 Bulk notification: gửi email/SMS tới nhóm users theo filter (role, branch, class)

14. Security & Compliance
    Mục tiêu: Đảm bảo bảo mật dữ liệu và tuân thủ quy định.
    14.1 Data Privacy
    14.1.1 Encryption at rest (database) và in transit (HTTPS/TLS)
    14.1.2 PII data masking: phone/email chỉ hiển thị một phần cho role không có quyền đầy đủ
    14.1.3 GDPR/PDPA compliance:
    User có quyền yêu cầu xóa dữ liệu cá nhân (right to be forgotten)
    Export dữ liệu cá nhân (data portability)
    Consent management cho marketing communication
    14.2 Access Control
    14.2.1 Row-level security: user chỉ xem được dữ liệu thuộc branch của mình (filter theo user_branches)
    14.2.2 Column-level security: sensitive fields (salary, private feedback) chỉ role cao mới thấy
    14.2.3 IP whitelisting cho Admin role (chỉ truy cập từ văn phòng)
    14.2.4 Rate limiting: chống brute force login, API abuse
    14.3 Audit & Compliance Reporting
    14.3.1 Audit trail đầy đủ cho mọi thao tác CRUD trên dữ liệu nhạy cảm (score, enrollment, user)
    14.3.2 Compliance report: tổng hợp truy cập dữ liệu cá nhân theo user/date range
    14.3.3 Security incident log: track failed login, unauthorized access attempt
    14.4 Backup & Disaster Recovery
    14.4.1 Daily automated backup với retention policy (7 daily, 4 weekly, 12 monthly)
    14.4.2 Backup encryption và store ở multiple locations (geo-redundancy)
    14.4.3 Disaster recovery plan: RTO (Recovery Time Objective) < 4h, RPO (Recovery Point Objective) < 1h
    14.4.4 Backup restore test định kỳ (quarterly) để đảm bảo backup hoạt động

Dependencies & Workflow Order
Phase 1 - Foundation (MVP Sprint 1-2):
Module 1: User & Role Management
Module 2: Org & Infrastructure
Module 3.1-3.2: Subject, Level, Course creation
Module 4.1-4.2: Teacher profile & skills
Phase 2 - Core Operations (MVP Sprint 3-5): 5. Module 3.3-3.4: Course structure & outcomes 6. Module 5.1-5.4: Class creation & auto-generate sessions 7. Module 6.1-6.4: Enrollment & schedule sync 8. Module 7.1-7.2: Attendance & session reporting
Phase 3 - Request Flows (MVP Sprint 6-7): 9. Module 8.1.1, 8.2.1: Basic absence & leave requests 10. Module 5.5: Class approval workflow
Phase 4 - Assessment & Basic Reporting (MVP Sprint 8): 11. Module 9.1-9.2: Assessment & score entry 12. Module 10.2.3, 10.9.2: Daily run-sheet & manager dashboard
Phase 5 - Enhancement (vNext): 13. Advanced request flows (make-up, transfer, reschedule, OT) 14. Comprehensive reporting & analytics 15. Notification system 16. QA & feedback management

Notes:
Các feature được thiết kế dựa trên database schema thực tế (session-first design pattern)
Mọi thay đổi lịch học đều thông qua request/approval flow để đảm bảo audit trail
Hệ thống ưu tiên automation (auto-generate session, auto-suggest, auto-sync) để giảm công việc thủ công
Reporting được thiết kế với nguồn dữ liệu là student_session + session (source of truth)

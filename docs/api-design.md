Bản Tài Liệu Thiết Kế API - Hệ Thống EMS (Education Management System)
Thông Tin Tổng Quan
Phiên bản: 1.0
Ngày: 2025
Mục đích: Cung cấp đặc tả API đầy đủ cho hệ thống quản lý đào tạo trung tâm ngoại ngữ, hỗ trợ team Backend implement và team Frontend mock data.
Kiến trúc: RESTful API
Base URL: https://api.ems.example.com/v1
Authentication: Bearer Token (JWT)
Content-Type: application/json

I. AUTHENTICATION & AUTHORIZATION
1.1 Login
POST /auth/login

Request Body:
{
"email": "user@example.com",
"phone": "+84901234567",
"password": "string"
}

Response 200:
{
"access_toke=: "eyJhbGc...",
"refresh_token": "eyJhbGc...",
"expires_in": 3600,
"user": {
"id": 1,
"email": "user@example.com",
"full_name": "Nguyễn Văn A",
"roles": ["STUDENT", "TEACHER"],
"branches": [
{
"id": 1,
"code": "HN-CG",
"name": "Cầu Giấy"
}
]
}
}

1.2 Refresh Token
POST /auth/refresh

Request Body:
{
"refresh_token": "eyJhbGc..."
}

Response 200:
{
"access_token": "eyJhbGc...",
"expires_in": 3600
}

1.3 Logout
POST /auth/logout

Headers: Authorization: Bearer {token}
Response 204: No Content

II. ORGANIZATION & RESOURCES
2.1 Branches
Get All Branches
GET /branches

Query Parameters:
center_id (optional): Filter by center
status (optional): active|inactive|closed|planned
page (optional, default: 1)
limit (optional, default: 20)
Response 200:
{
"data": [
{
"id": 1,
"center_id": 1,
"code": "HN-CG",
"name": "Cầu Giấy",
"address": "123 Đường ABC, Cầu Giấy, Hà Nội",
"location": "21.0285,105.8542",
"phone": "+84243xxxx",
"capacity": 500,
"status": "active",
"opening_date": "2020-01-15",
"created_at": "2020-01-01T00:00:00Z"
}
],
"pagination": {
"current_page": 1,
"total_pages": 5,
"total_items": 100,
"limit": 20
}
}

Get Branch Detail
GET /branches/{id}

Response 200:
{
"id": 1,
"center_id": 1,
"code": "HN-CG",
"name": "Cầu Giấy",
"address": "123 Đường ABC, Cầu Giấy, Hà Nội",
"location": "21.0285,105.8542",
"phone": "+84243xxxx",
"capacity": 500,
"status": "active",
"opening_date": "2020-01-15",
"time_slots": [
{
"id": 1,
"name": "Slot 1 - Sáng",
"start_time": "08:00:00",
"end_time": "10:30:00",
"duration_min": 150
}
],
"resources": [
{
"id": 1,
"resource_type": "ROOM",
"name": "Phòng 101",
"capacity": 25,
"equipment": "Projector, Whiteboard, Air Conditioner"
},
{
"id": 2,
"resource_type": "VIRTUAL",
"name": "Zoom Account 1",
"meeting_url": "https://zoom.us/j/xxxxx",
"license_type": "Pro"
}
],
"created_at": "2020-01-01T00:00:00Z",
"updated_at": "2024-01-01T00:00:00Z"
}

Create Branch (MANAGER, ADMIN)
POST /branches

Request Body:
{
"center_id": 1,
"code": "HN-HK",
"name": "Hoàn Kiếm",
"address": "456 Đường XYZ, Hoàn Kiếm, Hà Nội",
"location": "21.0285,105.8542",
"phone": "+84243yyyy",
"capacity": 300,
"status": "planned",
"opening_date": "2025-03-01"
}

Response 201:
{
"id": 5,
"center_id": 1,
"code": "HN-HK",
"name": "Hoàn Kiếm",
"status": "planned",
"created_at": "2025-01-15T10:30:00Z"
}

2.2 Time Slot Templates
Get Time Slots by Branch
GET /branches/{branch_id}/time-slots

Response 200:
{
"data": [
{
"id": 1,
"branch_id": 1,
"name": "Slot 1 - Sáng",
"start_time": "08:00:00",
"end_time": "10:30:00",
"duration_min": 150,
"created_at": "2020-01-01T00:00:00Z"
},
{
"id": 2,
"branch_id": 1,
"name": "Slot 2 - Chiều",
"start_time": "14:00:00",
"end_time": "16:30:00",
"duration_min": 150,
"created_at": "2020-01-01T00:00:00Z"
}
]
}

Create Time Slot (MANAGER, CENTER_HEAD)
POST /branches/{branch_id}/time-slots

Request Body:
{
"name": "Slot 3 - Tối",
"start_time": "18:00:00",
"end_time": "20:30:00",
"duration_min": 150
}

Response 201:
{
"id": 3,
"branch_id": 1,
"name": "Slot 3 - Tối",
"start_time": "18:00:00",
"end_time": "20:30:00",
"duration_min": 150,
"created_at": "2025-01-15T10:30:00Z"
}

2.3 Resources (Rooms & Virtual)
Get Resources by Branch
GET /branches/{branch_id}/resources

Query Parameters:
resource_type (optional): ROOM|VIRTUAL
available_date (optional): YYYY-MM-DD
available_start_time (optional): HH:MM:SS
available_end_time (optional): HH:MM:SS
Response 200:
{
"data": [
{
"id": 1,
"branch_id": 1,
"resource_type": "ROOM",
"name": "Phòng 101",
"location": "Tầng 1",
"capacity": 25,
"equipment": "Projector, Whiteboard, Air Conditioner",
"is_available": true
},
{
"id": 10,
"branch_id": 1,
"resource_type": "VIRTUAL",
"name": "Zoom Account 1",
"meeting_url": "https://zoom.us/j/xxxxx",
"meeting_id": "123456789",
"account_email": "zoom1@example.com",
"license_type": "Pro",
"expiry_date": "2025-12-31",
"is_available": true
}
]
}

Create Resource (MANAGER, CENTER_HEAD)
POST /branches/{branch_id}/resources

Request Body (ROOM):
{
"resource_type": "ROOM",
"name": "Phòng 201",
"location": "Tầng 2",
"capacity": 30,
"equipment": "Smart TV, Whiteboard, Air Conditioner",
"description": "Phòng học lớn, ánh sáng tốt"
}

Request Body (VIRTUAL):
{
"resource_type": "VIRTUAL",
"name": "Zoom Account 2",
"meeting_url": "https://zoom.us/j/yyyyy",
"meeting_id": "987654321",
"account_email": "zoom2@example.com",
"license_type": "Business",
"expiry_date": "2026-06-30"
}

Response 201:
{
"id": 15,
"branch_id": 1,
"resource_type": "ROOM",
"name": "Phòng 201",
"capacity": 30,
"created_at": "2025-01-15T10:30:00Z"
}


III. ACADEMIC STRUCTURE
3.1 Subjects
Get All Subjects
GET /subjects

Query Parameters:
status (optional): active|inactive
page, limit
Response 200:
{
"data": [
{
"id": 1,
"code": "ENG-GEN",
"name": "English General",
"description": "Chương trình tiếng Anh giao tiếp tổng quát",
"status": "active",
"created_by": 5,
"created_at": "2020-01-01T00:00:00Z",
"levels_count": 6,
"courses_count": 12
}
],
"pagination": {...}
}

Create Subject (SUBJECT_LEADER)
POST /subjects

Request Body:
{
"code": "JPN-GEN",
"name": "Japanese General",
"description": "Chương trình tiếng Nhật tổng quát từ N5 đến N1",
"status": "active"
}

Response 201:
{
"id": 3,
"code": "JPN-GEN",
"name": "Japanese General",
"status": "active",
"created_by": 5,
"created_at": "2025-01-15T10:30:00Z"
}

3.2 Levels
Get Levels by Subject
GET /subjects/{subject_id}/levels

Response 200:
{
"data": [
{
"id": 1,
"subject_id": 1,
"code": "A1",
"name": "Beginner A1",
"standard_type": "CEFR",
"expected_duration_hours": 60,
"sort_order": 1,
"description": "Trình độ sơ cấp",
"created_at": "2020-01-01T00:00:00Z"
},
{
"id": 2,
"subject_id": 1,
"code": "A2",
"name": "Elementary A2",
"standard_type": "CEFR",
"expected_duration_hours": 80,
"sort_order": 2,
"description": "Trình độ cơ bản",
"created_at": "2020-01-01T00:00:00Z"
}
]
}

Create Level (SUBJECT_LEADER)
POST /subjects/{subject_id}/levels

Request Body:
{
"code": "B1",
"name": "Intermediate B1",
"standard_type": "CEFR",
"expected_duration_hours": 100,
"sort_order": 3,
"description": "Trình độ trung cấp"
}

Response 201:
{
"id": 3,
"subject_id": 1,
"code": "B1",
"name": "Intermediate B1",
"created_at": "2025-01-15T10:30:00Z"
}

3.3 Courses
Get All Courses
GET /courses

Query Parameters:
subject_id (optional)
level_id (optional)
status (optional): active|inactive
approved (optional): true|false
page, limit
Response 200:
{
"data": [
{
"id": 1,
"subject_id": 1,
"level_id": 1,
"code": "ENG-A1-V1",
"name": "English A1 Course Version 1",
"version": 1,
"description": "Khóa học tiếng Anh A1 cơ bản",
"total_hours": 60,
"duration_weeks": 12,
"session_per_week": 3,
"hours_per_session": 1.5,
"status": "active",
"approved_by_manager": 10,
"approved_at": "2024-12-01T00:00:00Z",
"created_by": 5,
"created_at": "2024-11-01T00:00:00Z",
"phases_count": 3,
"sessions_count": 36
}
],
"pagination": {...}
}

Get Course Detail
GET /courses/{id}

Response 200:
{
"id": 1,
"subject_id": 1,
"level_id": 1,
"code": "ENG-A1-V1",
"name": "English A1 Course Version 1",
"version": 1,
"description": "Khóa học tiếng Anh A1 cơ bản",
"total_hours": 60,
"duration_weeks": 12,
"session_per_week": 3,
"hours_per_session": 1.5,
"prerequisites": "Không yêu cầu",
"target_audience": "Người mới bắt đầu học tiếng Anh",
"teaching_methods": "Communicative approach, Task-based learning",
"status": "active",
"approved_by_manager": 10,
"approved_at": "2024-12-01T00:00:00Z",
"created_by": 5,
"created_at": "2024-11-01T00:00:00Z",
"phases": [
{
"id": 1,
"phase_number": 1,
"name": "Foundation Phase",
"duration_weeks": 4,
"learning_focus": "Basic greetings, alphabet, numbers",
"sort_order": 1,
"sessions_count": 12
}
],
"clos": [
{
"id": 1,
"code": "CLO-01",
"description": "Có thể giới thiệu bản thân và người khác bằng tiếng Anh"
}
],
"materials": [
{
"id": 1,
"title": "Course Overview Presentation",
"url": "https://storage.example.com/courses/1/overview.pdf",
"phase_id": null,
"uploaded_by": 5,
"created_at": "2024-11-01T00:00:00Z"
}
]
}

Create Course (SUBJECT_LEADER)
POST /courses

Request Body:
{
"subject_id": 1,
"level_id": 1,
"version": 2,
"code": "ENG-A1-V2",
"name": "English A1 Course Version 2",
"description": "Phiên bản cập nhật với phương pháp giảng dạy mới",
"total_hours": 60,
"duration_weeks": 12,
"session_per_week": 3,
"hours_per_session": 1.5,
"prerequisites": "Không yêu cầu",
"target_audience": "Người mới bắt đầu học tiếng Anh",
"teaching_methods": "Communicative approach, Flipped classroom",
"effective_date": "2025-03-01",
"status": "active"
}

Response 201:
{
"id": 15,
"code": "ENG-A1-V2",
"name": "English A1 Course Version 2",
"version": 2,
"status": "active",
"created_by": 5,
"created_at": "2025-01-15T10:30:00Z"
}

Submit Course for Approval (SUBJECT_LEADER)
POST /courses/{id}/submit

Response 200:
{
"id": 15,
"code": "ENG-A1-V2",
"status": "active",
"submitted_at": "2025-01-15T10:30:00Z",
"message": "Course submitted for approval"
}

Approve/Reject Course (MANAGER only - strategic curriculum decision)
POST /courses/{id}/approve

Request Body:
{
"action": "approve",
"rejection_reason": null
}

OR
{
"action": "reject",
"rejection_reason": "Nội dung chưa đầy đủ, cần bổ sung thêm bài tập thực hành"
}

Response 200:
{
"id": 15,
"code": "ENG-A1-V2",
"approved_by_manager": 10,
"approved_at": "2025-01-15T11:00:00Z",
"status": "active"
}

3.4 Course Phases
Get Phases by Course
GET /courses/{course_id}/phases

Response 200:
{
"data": [
{
"id": 1,
"course_id": 1,
"phase_number": 1,
"name": "Foundation Phase",
"duration_weeks": 4,
"learning_focus": "Basic greetings, alphabet, numbers",
"sort_order": 1,
"sessions_count": 12,
"created_at": "2024-11-01T00:00:00Z"
}
]
}

Create Phase (SUBJECT_LEADER)
POST /courses/{course_id}/phases

Request Body:
{
"phase_number": 2,
"name": "Development Phase",
"duration_weeks": 4,
"learning_focus": "Daily conversations, present tense",
"sort_order": 2
}

Response 201:
{
"id": 2,
"course_id": 1,
"phase_number": 2,
"name": "Development Phase",
"created_at": "2025-01-15T10:30:00Z"
}

3.5 Course Sessions (Template)
Get Sessions by Phase
GET /phases/{phase_id}/sessions

Response 200:
{
"data": [
{
"id": 1,
"phase_id": 1,
"sequence_no": 1,
"topic": "Introduction & Greetings",
"student_task": "Practice self-introduction with partners",
"skill_set": ["speaking", "listening"],
"clos": [
{
"id": 1,
"code": "CLO-01",
"description": "Có thể giới thiệu bản thân"
}
],
"materials": [
{
"id": 5,
"title": "Session 1 Slides",
"url": "https://storage.example.com/..."
}
],
"created_at": "2024-11-01T00:00:00Z"
}
]
}

Create Course Session (SUBJECT_LEADER)
POST /phases/{phase_id}/sessions

Request Body:
{
"sequence_no": 2,
"topic": "Alphabet & Numbers",
"student_task": "Complete alphabet worksheet",
"skill_set": ["reading", "writing"]
}

Response 201:
{
"id": 2,
"phase_id": 1,
"sequence_no": 2,
"topic": "Alphabet & Numbers",
"created_at": "2025-01-15T10:30:00Z"
}

3.6 PLO & CLO Management
Get PLOs by Subject
GET /subjects/{subject_id}/plos

Response 200:
{
"data": [
{
"id": 1,
"subject_id": 1,
"code": "PLO-01",
"description": "Giao tiếp hiệu quả trong các tình huống hàng ngày",
"mapped_clos_count": 5
}
]
}

Create PLO (SUBJECT_LEADER)
POST /subjects/{subject_id}/plos

Request Body:
{
"code": "PLO-02",
"description": "Hiểu và sử dụng ngữ pháp cơ bản"
}

Response 201:
{
"id": 2,
"subject_id": 1,
"code": "PLO-02",
"description": "Hiểu và sử dụng ngữ pháp cơ bản"
}

Get CLOs by Course
GET /courses/{course_id}/clos

Response 200:
{
"data": [
{
"id": 1,
"course_id": 1,
"code": "CLO-01",
"description": "Có thể giới thiệu bản thân và người khác",
"mapped_plos": [
{
"id": 1,
"code": "PLO-01",
"description": "Giao tiếp hiệu quả trong các tình huống hàng ngày"
}
],
"mapped_sessions_count": 3
}
]
}

Create CLO (SUBJECT_LEADER)
POST /courses/{course_id}/clos

Request Body:
{
"code": "CLO-02",
"description": "Có thể hỏi và trả lời các câu hỏi đơn giản về thông tin cá nhân"
}

Response 201:
{
"id": 2,
"course_id": 1,
"code": "CLO-02",
"description": "Có thể hỏi và trả lời các câu hỏi đơn giản"
}

Map PLO to CLO (SUBJECT_LEADER)
POST /plos/{plo_id}/clos/{clo_id}/mapping

Request Body:
{
"status": "active"
}

Response 201:
{
"plo_id": 1,
"clo_id": 2,
"status": "active",
"created_at": "2025-01-15T10:30:00Z"
}

Map CLO to Course Session (SUBJECT_LEADER)
POST /course-sessions/{session_id}/clos/{clo_id}/mapping

Request Body:
{
"status": "active"
}

Response 201:
{
"course_session_id": 5,
"clo_id": 2,
"status": "active",
"created_at": "2025-01-15T10:30:00Z"
}

3.7 Course Materials
Upload Material (SUBJECT_LEADER, ACADEMIC_STAFF)
POST /courses/{course_id}/materials

Request Body (multipart/form-data):
file: [binary]
title: "Bài giảng tuần 1"
phase_id: 1 (optional)
course_session_id: 5 (optional)

Response 201:
{
"id": 20,
"course_id": 1,
"phase_id": 1,
"course_session_id": 5,
"title": "Bài giảng tuần 1",
"url": "https://storage.example.com/courses/1/materials/file.pdf",
"uploaded_by": 5,
"created_at": "2025-01-15T10:30:00Z"
}


IV. CLASS MANAGEMENT
4.1 Classes
Get All Classes
GET /classes

Query Parameters:
branch_id (optional)
course_id (optional)
status (optional): draft|scheduled|ongoing|completed|cancelled
modality (optional): OFFLINE|ONLINE|HYBRID
start_date_from (optional): YYYY-MM-DD
start_date_to (optional): YYYY-MM-DD
page, limit
Response 200:
{
"data": [
{
"id": 1,
"branch_id": 1,
"course_id": 1,
"code": "ENG-A1-2025-01",
"name": "English A1 - Sáng T2-T4-T6",
"modality": "OFFLINE",
"start_date": "2025-02-03",
"planned_end_date": "2025-04-25",
"schedule_days": [1, 3, 5],
"max_capacity": 25,
"current_enrollment": 22,
"status": "scheduled",
"created_by": 8,
"approved_by": 10,
"approved_at": "2025-01-10T00:00:00Z",
"created_at": "2025-01-05T00:00:00Z"
}
],
"pagination": {...}
}

Get Class Detail
GET /classes/{id}

Response 200:
{
"id": 1,
"branch": {
"id": 1,
"code": "HN-CG",
"name": "Cầu Giấy"
},
"course": {
"id": 1,
"code": "ENG-A1-V1",
"name": "English A1 Course Version 1",
"total_hours": 60
},
"code": "ENG-A1-2025-01",
"name": "English A1 - Sáng T2-T4-T6",
"modality": "OFFLINE",
"start_date": "2025-02-03",
"planned_end_date": "2025-04-25",
"actual_end_date": null,
"schedule_days": [1, 3, 5],
"schedule_mapping": {
"1": {"start_time": "08:00:00", "end_time": "10:30:00", "slot_id": 1},
"3": {"start_time": "08:00:00", "end_time": "10:30:00", "slot_id": 1},
"5": {"start_time": "08:00:00", "end_time": "10:30:00", "slot_id": 1}
},
"max_capacity": 25,
"current_enrollment": 22,
"status": "scheduled",
"created_by": 8,
"submitted_at": "2025-01-08T00:00:00Z",
"approved_by": 10,
"approved_at": "2025-01-10T00:00:00Z",
"rejection_reason": null,
"created_at": "2025-01-05T00:00:00Z",
"sessions_count": 36,
"sessions_completed": 0
}

Create Class (MANAGER, ACADEMIC_STAFF)
POST /classes

Request Body:
{
"branch_id": 1,
"course_id": 1,
"code": "ENG-A1-2025-02",
"name": "English A1 - Chiều T3-T5-T7",
"modality": "OFFLINE",
"start_date": "2025-02-04",
"schedule_days": [2, 4, 6],
"schedule_mapping": {
"2": {"slot_id": 2},
"4": {"slot_id": 2},
"6": {"slot_id": 2}
},
"max_capacity": 25
}

Response 201:
{
"id": 5,
"code": "ENG-A1-2025-02",
"status": "draft",
"sessions_generated": 36,
"created_by": 8,
"created_at": "2025-01-15T10:30:00Z",
"message": "Class created successfully. 36 sessions generated."
}

#### Submit Class for Approval (ACADEMIC_STAFF)

POST /classes/{id}/submit

**Response 200:**
```json
{
  "id": 5,
  "code": "ENG-A1-2025-02",
  "status": "draft",
  "submitted_at": "2025-01-15T10:35:00Z",
  "message": "Class submitted for approval"
}

Approve/Reject Class (CENTER_HEAD for their branch, MANAGER for cross-branch authority)
POST /classes/{id}/approve

Request Body:
{
  "action": "approve"
}

OR
{
  "action": "reject",
  "rejection_reason": "Trùng phòng 101 với lớp ENG-A2-2025-01 vào thứ 3"
}

Response 200:
{
  "id": 5,
  "code": "ENG-A1-2025-02",
  "status": "scheduled",
  "approved_by": 10,
  "approved_at": "2025-01-15T11:00:00Z",
  "rejection_reason": null
}

Update Class Schedule (ACADEMIC_STAFF with CENTER_HEAD approval)
PUT /classes/{id}/schedule

Request Body:
{
  "effective_from": "2025-02-15",
  "target_dow": 2,
  "new_slot_id": 3,
  "reason": "Thay đổi lịch học theo yêu cầu của học viên"
}

Response 200:
{
  "id": 5,
  "code": "ENG-A1-2025-02",
  "sessions_updated": 28,
  "conflicts_detected": [],
  "message": "Schedule updated successfully for 28 future sessions"
}

Validate Class Schedule (MANAGER, ACADEMIC_STAFF)
POST /classes/{id}/validate

Response 200:
{
  "is_valid": false,
  "conflicts": [
    {
      "type": "resource_conflict",
      "session_id": 15,
      "date": "2025-02-05",
      "start_time": "08:00:00",
      "end_time": "10:30:00",
      "resource_id": 1,
      "resource_name": "Phòng 101",
      "conflicting_class_id": 3,
      "conflicting_class_code": "ENG-A2-2025-01"
    },
    {
      "type": "teacher_conflict",
      "session_id": 20,
      "date": "2025-02-07",
      "start_time": "08:00:00",
      "end_time": "10:30:00",
      "teacher_id": 5,
      "teacher_name": "Nguyễn Thị B",
      "conflicting_session_id": 45
    }
  ],
  "warnings": [
    {
      "type": "capacity_exceeded",
      "session_id": 18,
      "resource_capacity": 25,
      "enrolled_students": 27,
      "message": "Phòng 101 chỉ chứa được 25 học viên nhưng đã có 27 học viên ghi danh"
    }
  ]
}


V. SESSION MANAGEMENT
5.1 Sessions
Get Sessions by Class
GET /classes/{class_id}/sessions

Query Parameters:
date_from (optional): YYYY-MM-DD
date_to (optional): YYYY-MM-DD
status (optional): planned|cancelled|done
type (optional): CLASS|MAKEUP|EXAM|OTHER
page, limit
Response 200:
{
  "data": [
    {
      "id": 1,
      "class_id": 1,
      "course_session_id": 1,
      "date": "2025-02-03",
      "start_time": "08:00:00",
      "end_time": "10:30:00",
      "type": "CLASS",
      "status": "planned",
      "course_session": {
        "sequence_no": 1,
        "topic": "Introduction & Greetings",
        "skill_set": ["speaking", "listening"]
      },
      "teachers": [
        {
          "teacher_id": 5,
          "teacher_name": "Nguyễn Thị B",
          "skill": "speaking",
          "role": "primary"
        }
      ],
      "resources": [
        {
          "resource_id": 1,
          "resource_type": "ROOM",
          "resource_name": "Phòng 101",
          "capacity": 25
        }
      ],
      "attendance_summary": {
        "total_students": 22,
        "present": 0,
        "absent": 0,
        "late": 0,
        "excused": 0,
        "planned": 22
      },
      "teacher_note": null,
      "created_at": "2025-01-10T00:00:00Z"
    }
  ],
  "pagination": {...}
}

Get Session Detail
GET /sessions/{id}

Response 200:
{
  "id": 1,
  "class": {
    "id": 1,
    "code": "ENG-A1-2025-01",
    "name": "English A1 - Sáng T2-T4-T6"
  },
  "course_session": {
    "id": 1,
    "sequence_no": 1,
    "topic": "Introduction & Greetings",
    "student_task": "Practice self-introduction with partners",
    "skill_set": ["speaking", "listening"],
    "materials": [
      {
        "id": 5,
        "title": "Session 1 Slides",
        "url": "https://storage.example.com/..."
      }
    ]
  },
  "date": "2025-02-03",
  "start_time": "08:00:00",
  "end_time": "10:30:00",
  "type": "CLASS",
  "status": "planned",
  "teachers": [
    {
      "teacher_id": 5,
      "teacher_name": "Nguyễn Thị B",
      "skill": "speaking",
      "role": "primary"
    },
    {
      "teacher_id": 8,
      "teacher_name": "Trần Văn C",
      "skill": "listening",
      "role": "assistant"
    }
  ],
  "resources": [
    {
      "resource_id": 1,
      "resource_type": "ROOM",
      "resource_name": "Phòng 101",
      "capacity": 25
    }
  ],
  "students": [
    {
      "student_id": 100,
      "student_code": "HV-2025-001",
      "student_name": "Lê Văn D",
      "is_makeup": false,
      "attendance_status": "planned"
    }
  ],
  "teacher_note": null,
  "created_at": "2025-01-10T00:00:00Z"
}

Update Session (MANAGER, ACADEMIC_STAFF)
PUT /sessions/{id}

Request Body:
{
  "date": "2025-02-04",
  "start_time": "14:00:00",
  "end_time": "16:30:00",
  "status": "planned",
  "teacher_note": "Session rescheduled due to teacher unavailability"
}

Response 200:
{
  "id": 1,
  "date": "2025-02-04",
  "start_time": "14:00:00",
  "end_time": "16:30:00",
  "status": "planned",
  "updated_at": "2025-01-15T10:30:00Z"
}

Cancel Session (MANAGER)
POST /sessions/{id}/cancel

Request Body:
{
  "reason": "Giáo viên bị ốm đột xuất, không tìm được người thay thế"
}

Response 200:
{
  "id": 1,
  "status": "cancelled",
  "teacher_note": "Buổi học đã được hủy do không có giáo viên thay thế. Lý do: Giáo viên bị ốm đột xuất, không tìm được người thay thế",
  "students_notified": 22,
  "updated_at": "2025-01-15T10:30:00Z"
}

5.2 Teacher Assignment
Assign Teacher to Session (MANAGER)
POST /sessions/{session_id}/teachers

Request Body:
{
  "teacher_id": 5,
  "skill": "speaking",
  "role": "primary"
}

Response 201:
{
  "session_id": 1,
  "teacher_id": 5,
  "teacher_name": "Nguyễn Thị B",
  "skill": "speaking",
  "role": "primary",
  "created_at": "2025-01-15T10:30:00Z"
}

Get Available Teachers for Session
GET /sessions/{session_id}/available-teachers

Query Parameters:
skill (optional): general|reading|writing|speaking|listening
Response 200:
{
  "data": [
    {
      "teacher_id": 5,
      "teacher_name": "Nguyễn Thị B",
      "employee_code": "T-HN-001",
      "skills": [
        {
          "skill": "speaking",
          "level": 5
        },
        {
          "skill": "listening",
          "level": 4
        }
      ],
      "availability": {
        "day_of_week": 1,
        "start_time": "08:00:00",
        "end_time": "18:00:00",
        "is_available": true
      },
      "current_sessions_on_date": 0,
      "is_ot_registered": false
    }
  ]
}

Remove Teacher from Session (MANAGER)
DELETE /sessions/{session_id}/teachers/{teacher_id}

Query Parameters:
skill: The skill to remove (required if teacher teaches multiple skills in this session)
Response 204: No Content
5.3 Resource Assignment
Assign Resource to Session (MANAGER)
POST /sessions/{session_id}/resources

Request Body:
{
  "resource_type": "ROOM",
  "resource_id": 1,
  "capacity_override": 27
}

Response 201:
{
  "session_id": 1,
  "resource_type": "ROOM",
  "resource_id": 1,
  "resource_name": "Phòng 101",
  "capacity": 25,
  "capacity_override": 27,
  "created_at": "2025-01-15T10:30:00Z"
}

Get Available Resources for Session
GET /sessions/{session_id}/available-resources

Query Parameters:
resource_type (optional): ROOM|VIRTUAL
Response 200:
{
  "data": [
    {
      "resource_id": 1,
      "resource_type": "ROOM",
      "resource_name": "Phòng 101",
      "capacity": 25,
      "is_available": true,
      "conflicts": []
    },
    {
      "resource_id": 2,
      "resource_type": "ROOM",
      "resource_name": "Phòng 102",
      "capacity": 30,
      "is_available": false,
      "conflicts": [
        {
          "session_id": 45,
          "class_code": "ENG-A2-2025-01",
          "start_time": "08:00:00",
          "end_time": "10:30:00"
        }
      ]
    }
  ]
}

Remove Resource from Session (MANAGER)
DELETE /sessions/{session_id}/resources/{resource_id}

Response 204: No Content

VI. TEACHER OPERATIONS
6.1 Teacher Profile
Get Teacher Profile
GET /teachers/{id}

Response 200:
{
  "id": 5,
  "user_account_id": 25,
  "employee_code": "T-HN-001",
  "full_name": "Nguyễn Thị B",
  "email": "teacher.b@example.com",
  "phone": "+84901234567",
  "note": "Giáo viên có kinh nghiệm 5 năm",
  "skills": [
    {
      "skill": "speaking",
      "level": 5
    },
    {
      "skill": "listening",
      "level": 4
    },
    {
      "skill": "general",
      "level": 5
    }
  ],
  "availability": [
    {
      "id": 1,
      "day_of_week": 1,
      "start_time": "08:00:00",
      "end_time": "18:00:00",
      "note": "Sẵn sàng dạy cả ngày thứ 2"
    },
    {
      "id": 2,
      "day_of_week": 3,
      "start_time": "08:00:00",
      "end_time": "12:00:00",
      "note": "Chỉ dạy buổi sáng thứ 4"
    }
  ],
  "branches": [
    {
      "id": 1,
      "code": "HN-CG",
      "name": "Cầu Giấy"
    }
  ],
  "created_at": "2020-01-01T00:00:00Z"
}

Update Teacher Skills (MANAGER, ADMIN)
PUT /teachers/{id}/skills

Request Body:
{
  "skills": [
    {
      "skill": "speaking",
      "level": 5
    },
    {
      "skill": "listening",
      "level": 5
    },
    {
      "skill": "writing",
      "level": 3
    }
  ]
}

Response 200:
{
  "teacher_id": 5,
  "skills": [
    {
      "skill": "speaking",
      "level": 5
    },
    {
      "skill": "listening",
      "level": 5
    },
    {
      "skill": "writing",
      "level": 3
    }
  ],
  "updated_at": "2025-01-15T10:30:00Z"
}

6.2 Teacher Availability
Get Teacher Availability
GET /teachers/{id}/availability

Query Parameters:
date (optional): YYYY-MM-DD (to include overrides)
Response 200:
{
  "regular_availability": [
    {
      "id": 1,
      "day_of_week": 1,
      "start_time": "08:00:00",
      "end_time": "18:00:00",
      "note": null
    }
  ],
  "overrides": [
    {
      "id": 1,
      "date": "2025-02-10",
      "start_time": "00:00:00",
      "end_time": "23:59:59",
      "is_available": false,
      "reason": "Nghỉ phép"
    },
    {
      "id": 2,
      "date": "2025-02-12",
      "start_time": "18:00:00",
      "end_time": "20:30:00",
      "is_available": true,
      "reason": "Sẵn sàng nhận lớp OT"
    }
  ]
}

Create/Update Regular Availability (TEACHER, MANAGER)
POST /teachers/{id}/availability

Request Body:
{
  "day_of_week": 5,
  "start_time": "14:00:00",
  "end_time": "20:00:00",
  "note": "Chỉ dạy buổi chiều thứ 6"
}

Response 201:
{
  "id": 10,
  "teacher_id": 5,
  "day_of_week": 5,
  "start_time": "14:00:00",
  "end_time": "20:00:00",
  "note": "Chỉ dạy buổi chiều thứ 6",
  "created_at": "2025-01-15T10:30:00Z"
}

Create Availability Override (TEACHER)
POST /teachers/{id}/availability-overrides

Request Body (Leave):
{
  "date": "2025-02-20",
  "start_time": "00:00:00",
  "end_time": "23:59:59",
  "is_available": false,
  "reason": "Nghỉ phép cá nhân"
}

Request Body (OT Registration):
{
  "date": "2025-02-22",
  "start_time": "18:00:00",
  "end_time": "20:30:00",
  "is_available": true,
  "reason": "Sẵn sàng nhận lớp OT"
}

Response 201:
{
  "id": 5,
  "teacher_id": 5,
  "date": "2025-02-22",
  "start_time": "18:00:00",
  "end_time": "20:30:00",
  "is_available": true,
  "reason": "Sẵn sàng nhận lớp OT",
  "created_at": "2025-01-15T10:30:00Z"
}

6.3 Teacher Schedule & Workload
Get Teacher Schedule
GET /teachers/{id}/schedule

Query Parameters:
date_from (required): YYYY-MM-DD
date_to (required): YYYY-MM-DD
Response 200:
{
  "teacher_id": 5,
  "teacher_name": "Nguyễn Thị B",
  "date_from": "2025-02-03",
  "date_to": "2025-02-09",
  "sessions": [
    {
      "session_id": 1,
      "class_id": 1,
      "class_code": "ENG-A1-2025-01",
      "date": "2025-02-03",
      "start_time": "08:00:00",
      "end_time": "10:30:00",
      "skill": "speaking",
      "role": "primary",
      "resource": {
        "type": "ROOM",
        "name": "Phòng 101"
      },
      "status": "planned"
    },
    {
      "session_id": 15,
      "class_id": 1,
      "class_code": "ENG-A1-2025-01",
      "date": "2025-02-05",
      "start_time": "08:00:00",
      "end_time": "10:30:00",
      "skill": "speaking",
      "role": "primary",
      "resource": {
        "type": "ROOM",
        "name": "Phòng 101"
      },
      "status": "planned"
    }
  ],
  "summary": {
    "total_sessions": 6,
    "total_hours": 15.0,
    "by_status": {
      "planned": 6,
      "done": 0,
      "cancelled": 0
    }
  }
}

Get Teacher Workload Report
GET /teachers/{id}/workload

Query Parameters:
date_from (required): YYYY-MM-DD
date_to (required): YYYY-MM-DD
Response 200:
{
  "teacher_id": 5,
  "teacher_name": "Nguyễn Thị B",
  "period": {
    "from": "2025-02-01",
    "to": "2025-02-28"
  },
  "summary": {
    "total_sessions_taught": 24,
    "total_hours": 60.0,
    "classes_count": 3,
    "ot_sessions": 2,
    "ot_hours": 5.0
  },
  "by_class": [
    {
      "class_id": 1,
      "class_code": "ENG-A1-2025-01",
      "sessions_taught": 12,
      "hours": 30.0
    }
  ],
  "by_skill": [
    {
      "skill": "speaking",
      "sessions": 20,
      "hours": 50.0
    },
    {
      "skill": "listening",
      "sessions": 4,
      "hours": 10.0
    }
  ]
}

6.4 Teacher Requests
Get Teacher Requests
GET /teachers/{id}/requests

Query Parameters:
request_type (optional): leave|swap|ot|reschedule
status (optional): pending|approved|rejected|cancelled
date_from (optional): YYYY-MM-DD
date_to (optional): YYYY-MM-DD
page, limit
Response 200:
{
  "data": [
    {
      "id": 1,
      "teacher_id": 5,
      "session_id": 15,
      "session": {
        "date": "2025-02-10",
        "start_time": "08:00:00",
        "end_time": "10:30:00",
        "class_code": "ENG-A1-2025-01"
      },
      "request_type": "leave",
      "status": "pending",
      "note": "Có việc gia đình đột xuất",
      "resolution": null,
      "submitted_at": "2025-02-05T10:00:00Z",
      "submitted_by": 25,
      "decided_by": null,
      "decided_at": null
    }
  ],
  "pagination": {...}
}

Create Teacher Request (TEACHER)
POST /teachers/{id}/requests

Request Body (Leave):
{
  "session_id": 15,
  "request_type": "leave",
  "note": "Có việc gia đình đột xuất, xin nghỉ buổi này"
}

Request Body (Reschedule):
{
  "session_id": 20,
  "request_type": "reschedule",
  "note": "Xin dời lịch buổi này sang tuần sau cùng khung giờ"
}

Response 201:
{
  "id": 10,
  "teacher_id": 5,
  "session_id": 15,
  "request_type": "leave",
  "status": "pending",
  "note": "Có việc gia đình đột xuất, xin nghỉ buổi này",
  "submitted_at": "2025-02-05T10:00:00Z",
  "submitted_by": 25
}

Get Substitute Teachers for Request (ACADEMIC_STAFF, MANAGER)
GET /teacher-requests/{request_id}/substitute-teachers

Response 200:
{
  "request_id": 10,
  "session": {
    "id": 15,
    "date": "2025-02-10",
    "start_time": "08:00:00",
    "end_time": "10:30:00",
    "required_skills": ["speaking"]
  },
  "available_teachers": [
    {
      "teacher_id": 8,
      "teacher_name": "Trần Văn C",
      "employee_code": "T-HN-002",
      "skills": [
        {
          "skill": "speaking",
          "level": 4
        }
      ],
      "is_regular_available": true,
      "is_ot_registered": false,
      "current_sessions_on_date": 1
    },
    {
      "teacher_id": 12,
      "teacher_name": "Phạm Thị D",
      "employee_code": "T-HN-005",
      "skills": [
        {
          "skill": "speaking",
          "level": 5
        }
      ],
      "is_regular_available": false,
      "is_ot_registered": true,
      "current_sessions_on_date": 0
    }
  ]
}

Approve Teacher Request with Substitute (ACADEMIC_STAFF, escalate to CENTER_HEAD if complex)
POST /teacher-requests/{request_id}/approve

Request Body:
{
  "substitute_teacher_id": 8,
  "resolution": "Đã duyệt. GV Trần Văn C sẽ dạy thay."
}

Response 200:
{
  "id": 10,
  "status": "approved",
  "resolution": "Đã duyệt. GV Trần Văn C sẽ dạy thay.",
  "decided_by": 16,
  "decided_at": "2025-02-05T11:00:00Z",
  "substitute_teacher": {
    "teacher_id": 8,
    "teacher_name": "Trần Văn C"
  },
  "ot_request_created": {
    "id": 15,
    "teacher_id": 8,
    "session_id": 15,
    "request_type": "ot",
    "status": "approved"
  }
}

Reject Teacher Request (ACADEMIC_STAFF, CENTER_HEAD)
POST /teacher-requests/{request_id}/reject

Request Body:
{
  "resolution": "Không thể duyệt do không tìm được giáo viên thay thế phù hợp"
}

Response 200:
{
  "id": 10,
  "status": "rejected",
  "resolution": "Không thể duyệt do không tìm được giáo viên thay thế phù hợp",
  "decided_by": 16,
  "decided_at": "2025-02-05T11:00:00Z"
}

Reschedule Session (ACADEMIC_STAFF, MANAGER)
POST /teacher-requests/{request_id}/reschedule

Request Body:
{
  "new_date": "2025-02-17",
  "new_slot_id": 1,
  "resolution": "Đã dời lịch sang thứ 2 tuần sau, cùng khung giờ"
}

Response 200:
{
  "id": 10,
  "status": "approved",
  "resolution": "Đã dời lịch sang thứ 2 tuần sau, cùng khung giờ",
  "decided_by": 16,
  "decided_at": "2025-02-05T11:00:00Z",
  "old_session": {
    "id": 20,
    "status": "cancelled"
  },
  "new_session": {
    "id": 150,
    "date": "2025-02-17",
    "start_time": "08:00:00",
    "end_time": "10:30:00",
    "status": "planned"
  }
}

Get Available Slots for Reschedule (ACADEMIC_STAFF, MANAGER)
GET /sessions/{session_id}/available-slots

Query Parameters:
target_date (required): YYYY-MM-DD
Response 200:
{
  "session_id": 20,
  "target_date": "2025-02-17",
  "branch_id": 1,
  "available_slots": [
    {
      "slot_id": 1,
      "name": "Slot 1 - Sáng",
      "start_time": "08:00:00",
      "end_time": "10:30:00",
      "is_teacher_available": true,
      "is_resource_available": true,
      "conflicts": []
    },
    {
      "slot_id": 2,
      "name": "Slot 2 - Chiều",
      "start_time": "14:00:00",
      "end_time": "16:30:00",
      "is_teacher_available": true,
      "is_resource_available": false,
      "conflicts": [
        {
          "type": "resource_conflict",
          "resource_id": 1,
          "resource_name": "Phòng 101",
          "conflicting_session_id": 75,
          "conflicting_class_code": "ENG-A2-2025-01"
        }
      ]
    }
  ]
}


VII. STUDENT OPERATIONS
7.1 Student Profile
Get Student Profile
GET /students/{id}

Response 200:
{
  "id": 100,
  "user_id": 200,
  "student_code": "HV-2025-001",
  "full_name": "Lê Văn D",
  "email": "student.d@example.com",
  "phone": "+84909876543",
  "branch": {
    "id": 1,
    "code": "HN-CG",
    "name": "Cầu Giấy"
  },
  "current_classes": [
    {
      "class_id": 1,
      "class_code": "ENG-A1-2025-01",
      "class_name": "English A1 - Sáng T2-T4-T6",
      "enrollment_status": "enrolled",
      "enrolled_at": "2025-01-20T00:00:00Z"
    }
  ],
  "created_at": "2025-01-15T00:00:00Z"
}

Create Student (ACADEMIC_STAFF)
POST /students

Request Body:
{
  "full_name": "Hoàng Thị E",
  "email": "student.e@example.com",
  "phone": "+84909111222",
  "branch_id":1 }

**Response 201:**
```json
{
  "id": 105,
  "user_id": 210,
  "student_code": "HV-2025-006",
  "full_name": "Hoàng Thị E",
  "email": "student.e@example.com",
  "phone": "+84909111222",
  "branch_id": 1,
  "created_at": "2025-01-15T10:30:00Z"
}

Bulk Import Students (ACADEMIC_STAFF)
POST /students/import

Request Body (multipart/form-data):
file: [CSV file]
branch_id: 1

CSV Format:
full_name,email,phone
Nguyễn Văn A,student.a@example.com,+84901111111
Trần Thị B,student.b@example.com,+84902222222

Response 200:
{
  "total_rows": 50,
  "successful": 48,
  "failed": 2,
  "students_created": [
    {
      "id": 110,
      "student_code": "HV-2025-011",
      "full_name": "Nguyễn Văn A",
      "email": "student.a@example.com"
    }
  ],
  "errors": [
    {
      "row": 15,
      "full_name": "Duplicate Name",
      "email": "duplicate@example.com",
      "error": "Email already exists"
    }
  ]
}

7.2 Enrollment
Get Student Enrollments
GET /students/{id}/enrollments

Query Parameters:
status (optional): enrolled|waitlisted|transferred|dropped|completed
Response 200:
{
  "data": [
    {
      "id": 1,
      "class": {
        "id": 1,
        "code": "ENG-A1-2025-01",
        "name": "English A1 - Sáng T2-T4-T6",
        "branch_name": "Cầu Giấy",
        "modality": "OFFLINE"
      },
      "status": "enrolled",
      "enrolled_at": "2025-01-20T00:00:00Z",
      "left_at": null,
      "join_session_id": 1,
      "left_session_id": null,
      "attendance_summary": {
        "total_sessions": 36,
        "attended": 0,
        "absent": 0,
        "excused": 0,
        "rate": 0
      }
    }
  ]
}

Enroll Student to Class (ACADEMIC_STAFF)
POST /classes/{class_id}/enrollments

Request Body:
{
  "student_id": 100
}

Response 201:
{
  "id": 15,
  "class_id": 1,
  "student_id": 100,
  "status": "enrolled",
  "enrolled_at": "2025-01-15T10:30:00Z",
  "sessions_generated": 36,
  "message": "Student enrolled successfully. 36 sessions added to student schedule."
}

Error Response 400:
{
  "error": "enrollment_conflict",
  "message": "Student has schedule conflict with existing class",
  "conflicts": [
    {
      "date": "2025-02-03",
      "start_time": "08:00:00",
      "end_time": "10:30:00",
      "existing_class_code": "ENG-A2-2024-05"
    }
  ]
}

Error Response 409:
{
  "error": "capacity_exceeded",
  "message": "Class is at maximum capacity",
  "max_capacity": 25,
  "current_enrollment": 25
}

Bulk Enroll Students (ACADEMIC_STAFF)
POST /classes/{class_id}/enrollments/bulk

Request Body:
{
  "student_ids": [100, 101, 102, 103, 104]
}

Response 200:
{
  "total_students": 5,
  "successful": 4,
  "failed": 1,
  "enrollments_created": [
    {
      "student_id": 100,
      "student_code": "HV-2025-001",
      "enrollment_id": 20
    },
    {
      "student_id": 101,
      "student_code": "HV-2025-002",
      "enrollment_id": 21
    }
  ],
  "errors": [
    {
      "student_id": 103,
      "student_code": "HV-2025-004",
      "error": "schedule_conflict",
      "message": "Student has schedule conflict"
    }
  ]
}

7.3 Student Schedule
Get Student Schedule
GET /students/{id}/schedule

Query Parameters:
date_from (required): YYYY-MM-DD
date_to (required): YYYY-MM-DD
class_id (optional): Filter by specific class
Response 200:
{
  "student_id": 100,
  "student_name": "Lê Văn D",
  "date_from": "2025-02-03",
  "date_to": "2025-02-09",
  "sessions": [
    {
      "student_session_id": 1,
      "session_id": 1,
      "class": {
        "id": 1,
        "code": "ENG-A1-2025-01",
        "name": "English A1 - Sáng T2-T4-T6"
      },
      "date": "2025-02-03",
      "start_time": "08:00:00",
      "end_time": "10:30:00",
      "course_session": {
        "sequence_no": 1,
        "topic": "Introduction & Greetings"
      },
      "teachers": [
        {
          "name": "Nguyễn Thị B",
          "skill": "speaking"
        }
      ],
      "resource": {
        "type": "ROOM",
        "name": "Phòng 101"
      },
      "is_makeup": false,
      "attendance_status": "planned",
      "note": null
    }
  ],
  "summary": {
    "total_sessions": 6,
    "by_status": {
      "planned": 6,
      "present": 0,
      "absent": 0,
      "excused": 0,
      "late": 0
    }
  }
}

7.4 Student Requests
Get Student Requests
GET /students/{id}/requests

Query Parameters:
request_type (optional): absence|makeup|transfer|reschedule
status (optional): pending|approved|rejected|cancelled
page, limit
Response 200:
{
  "data": [
    {
      "id": 1,
      "student_id": 100,
      "current_class": {
        "id": 1,
        "code": "ENG-A1-2025-01"
      },
      "request_type": "absence",
      "status": "pending",
      "target_session": {
        "id": 15,
        "date": "2025-02-10",
        "start_time": "08:00:00",
        "course_session": {
          "topic": "Present Simple Tense"
        }
      },
      "note": "Em bị ốm không thể đến lớp",
      "submitted_at": "2025-02-08T10:00:00Z",
      "submitted_by": 200,
      "decided_by": null,
      "decided_at": null
    }
  ],
  "pagination": {...}
}

Create Absence Request (STUDENT, ACADEMIC_STAFF)
POST /students/{id}/requests/absence

Request Body:
{
  "target_session_id": 15,
  "note": "Em bị ốm không thể đến lớp"
}

Response 201:
{
  "id": 10,
  "student_id": 100,
  "request_type": "absence",
  "target_session_id": 15,
  "status": "pending",
  "note": "Em bị ốm không thể đến lớp",
  "submitted_at": "2025-02-08T10:00:00Z",
  "submitted_by": 200
}

Get Available Makeup Sessions (STUDENT, ACADEMIC_STAFF)
GET /students/{id}/requests/makeup/available-sessions

Query Parameters:
missed_session_id (required): Session ID that student missed
Response 200:
{
  "missed_session": {
    "id": 15,
    "date": "2025-02-10",
    "course_session_id": 5,
    "topic": "Present Simple Tense"
  },
  "available_sessions": [
    {
      "session_id": 45,
      "class": {
        "id": 3,
        "code": "ENG-A1-2025-03",
        "name": "English A1 - Chiều T2-T4-T6"
      },
      "date": "2025-02-11",
      "start_time": "14:00:00",
      "end_time": "16:30:00",
      "course_session_id": 5,
      "same_topic": true,
      "max_capacity": 25,
      "current_enrolled": 20,
      "available_slots": 5,
      "has_capacity": true
    },
    {
      "session_id": 78,
      "class": {
        "id": 5,
        "code": "ENG-A1-2025-05",
        "name": "English A1 - Tối T3-T5"
      },
      "date": "2025-02-12",
      "start_time": "18:00:00",
      "end_time": "20:30:00",
      "course_session_id": 5,
      "same_topic": true,
      "max_capacity": 30,
      "current_enrolled": 30,
      "available_slots": 0,
      "has_capacity": false
    }
  ]
}

Create Makeup Request (STUDENT, ACADEMIC_STAFF)
POST /students/{id}/requests/makeup

Request Body:
{
  "target_session_id": 15,
  "makeup_session_id": 45,
  "note": "Em xin học bù buổi này vào lớp chiều"
}

Response 201:
{
  "id": 12,
  "student_id": 100,
  "request_type": "makeup",
  "target_session_id": 15,
  "makeup_session_id": 45,
  "status": "pending",
  "note": "Em xin học bù buổi này vào lớp chiều",
  "submitted_at": "2025-02-08T10:30:00Z",
  "submitted_by": 200
}

Approve Makeup Request (ACADEMIC_STAFF)
POST /student-requests/{request_id}/approve

Response 200:
{
  "id": 12,
  "status": "approved",
  "decided_by": 16,
  "decided_at": "2025-02-08T11:00:00Z",
  "target_session": {
    "student_session_id": 150,
    "attendance_status": "excused"
  },
  "makeup_session": {
    "student_session_id": 250,
    "session_id": 45,
    "is_makeup": true,
    "attendance_status": "planned"
  },
  "message": "Makeup request approved. Student added to makeup session."
}

Create Transfer Request (STUDENT, ACADEMIC_STAFF)
POST /students/{id}/requests/transfer

Request Body:
{
  "current_class_id": 1,
  "target_class_id": 3,
  "effective_date": "2025-02-15",
  "note": "Em muốn chuyển sang lớp chiều vì có việc buổi sáng"
}

Response 201:
{
  "id": 15,
  "student_id": 100,
  "request_type": "transfer",
  "current_class_id": 1,
  "target_class_id": 3,
  "effective_date": "2025-02-15",
  "status": "pending",
  "note": "Em muốn chuyển sang lớp chiều vì có việc buổi sáng",
  "submitted_at": "2025-02-10T10:00:00Z",
  "submitted_by": 200
}

Validate Transfer Request (ACADEMIC_STAFF)
POST /student-requests/{request_id}/validate-transfer

Response 200:
{
  "is_valid": true,
  "from_class": {
    "id": 1,
    "code": "ENG-A1-2025-01",
    "course_id": 1,
    "status": "ongoing"
  },
  "to_class": {
    "id": 3,
    "code": "ENG-A1-2025-03",
    "course_id": 1,
    "status": "ongoing",
    "has_capacity": true,
    "current_enrollment": 20,
    "max_capacity": 25
  },
  "same_course": true,
  "effective_session": {
    "from_last_done": 8,
    "from_cutoff": 9,
    "to_join": 10
  },
  "remaining_sessions": {
    "from_class": [
      {
        "course_session_id": 5,
        "topic": "Present Simple"
      },
      {
        "course_session_id": 6,
        "topic": "Present Continuous"
      }
    ],
    "to_class_matched": [
      {
        "course_session_id": 5,
        "session_id": 45,
        "date": "2025-02-16"
      },
      {
        "course_session_id": 6,
        "session_id": 50,
        "date": "2025-02-18"
      }
    ],
    "missing_sessions": []
  },
  "warnings": []
}

Error Response 400:
{
  "is_valid": false,
  "errors": [
    {
      "code": "different_course",
      "message": "Cannot transfer between different courses"
    }
  ]
}

Approve Transfer Request (ACADEMIC_STAFF, CENTER_HEAD)
POST /student-requests/{request_id}/approve-transfer

Response 200:
{
  "id": 15,
  "status": "approved",
  "decided_by": 16,
  "decided_at": "2025-02-10T11:00:00Z",
  "from_enrollment": {
    "id": 5,
    "status": "transferred",
    "left_at": "2025-02-10T11:00:00Z",
    "left_session_id": 8
  },
  "to_enrollment": {
    "id": 25,
    "status": "enrolled",
    "enrolled_at": "2025-02-10T11:00:00Z",
    "join_session_id": 10
  },
  "from_class_sessions_updated": 28,
  "to_class_sessions_created": 27,
  "message": "Transfer completed successfully"
}

Reject Student Request (ACADEMIC_STAFF, CENTER_HEAD)
POST /student-requests/{request_id}/reject

Request Body:
{
  "note": "Lớp đích đã đầy, không thể chấp nhận thêm học viên"
}

Response 200:
{
  "id": 15,
  "status": "rejected",
  "decided_by": 16,
  "decided_at": "2025-02-10T11:00:00Z",
  "note": "Lớp đích đã đầy, không thể chấp nhận thêm học viên"
}

7.5 Attendance & Progress
Get Student Attendance Report
GET /students/{id}/attendance

Query Parameters:
class_id (optional): Filter by class
date_from (optional): YYYY-MM-DD
date_to (optional): YYYY-MM-DD
Response 200:
{
  "student_id": 100,
  "student_name": "Lê Văn D",
  "summary": {
    "total_sessions": 36,
    "present": 30,
    "absent": 2,
    "late": 1,
    "excused": 3,
    "attendance_rate": 83.33
  },
  "by_class": [
    {
      "class_id": 1,
      "class_code": "ENG-A1-2025-01",
      "total_sessions": 36,
      "present": 30,
      "absent": 2,
      "late": 1,
      "excused": 3,
      "attendance_rate": 83.33
    }
  ],
  "warnings": [
    {
      "type": "absence_threshold",
      "message": "Student is approaching maximum allowed absences",
      "current_absences": 2,
      "max_allowed": 3
    }
  ]
}

Get Student Progress by Class
GET /students/{id}/classes/{class_id}/progress

Response 200:
{
  "student_id": 100,
  "class": {
    "id": 1,
    "code": "ENG-A1-2025-01",
    "course_name": "English A1 Course Version 1"
  },
  "overall_progress": {
    "sessions_completed": 12,
    "total_sessions": 36,
    "completion_percentage": 33.33,
    "attendance_rate": 91.67
  },
  "by_phase": [
    {
      "phase_id": 1,
      "phase_name": "Foundation Phase",
      "sessions_completed": 12,
      "total_sessions": 12,
      "completion_percentage": 100,
      "attendance_rate": 91.67
    },
    {
      "phase_id": 2,
      "phase_name": "Development Phase",
      "sessions_completed": 0,
      "total_sessions": 12,
      "completion_percentage": 0,
      "attendance_rate": 0
    }
  ],
  "assessments": [
    {
      "assessment_id": 1,
      "name": "Quiz 1",
      "kind": "quiz",
      "max_score": 10,
      "score": 8.5,
      "percentage": 85,
      "graded_at": "2025-02-20T00:00:00Z"
    }
  ],
  "clo_attainment": [
    {
      "clo_id": 1,
      "clo_code": "CLO-01",
      "description": "Có thể giới thiệu bản thân",
      "sessions_completed": 3,
      "sessions_total": 3,
      "is_achieved": true
    }
  ]
}


VIII. ATTENDANCE & SESSION REPORTING
8.1 Attendance Recording (Teacher)
Get Session Attendance List
GET /sessions/{session_id}/attendance

Response 200:
{
  "session_id": 1,
  "class": {
    "id": 1,
    "code": "ENG-A1-2025-01"
  },
  "date": "2025-02-03",
  "start_time": "08:00:00",
  "end_time": "10:30:00",
  "status": "done",
  "students": [
    {
      "student_session_id": 1,
      "student_id": 100,
      "student_code": "HV-2025-001",
      "student_name": "Lê Văn D",
      "is_makeup": false,
      "attendance_status": "present",
      "note": null,
      "recorded_at": "2025-02-03T08:05:00Z"
    },
    {
      "student_session_id": 2,
      "student_id": 101,
      "student_code": "HV-2025-002",
      "student_name": "Nguyễn Thị F",
      "is_makeup": false,
      "attendance_status": "absent",
      "note": "Không đến không báo trước",
      "recorded_at": "2025-02-03T08:10:00Z"
    },
    {
      "student_session_id": 50,
      "student_id": 150,
      "student_code": "HV-2025-050",
      "student_name": "Trần Văn G",
      "is_makeup": true,
      "attendance_status": "present",
      "note": "Học bù từ lớp ENG-A1-2025-05",
      "recorded_at": "2025-02-03T08:05:00Z"
    }
  ],
  "summary": {
    "total_students": 23,
    "present": 20,
    "absent": 2,
    "late": 1,
    "excused": 0,
    "makeup_students": 1
  }
}

Record Attendance (TEACHER)
POST /sessions/{session_id}/attendance

Request Body:
{
  "attendances": [
    {
      "student_id": 100,
      "attendance_status": "present",
      "note": null
    },
    {
      "student_id": 101,
      "attendance_status": "absent",
      "note": "Không đến không báo trước"
    },
    {
      "student_id": 102,
      "attendance_status": "late",
      "note": "Đến muộn 15 phút"
    },
    {
      "student_id": 103,
      "attendance_status": "excused",
      "note": "Đã xin phép trước"
    }
  ]
}

Response 200:
{
  "session_id": 1,
  "total_students": 23,
  "recorded": 23,
  "summary": {
    "present": 20,
    "absent": 2,
    "late": 1,
    "excused": 0
  },
  "recorded_at": "2025-02-03T10:35:00Z",
  "message": "Attendance recorded successfully"
}

Update Single Student Attendance (TEACHER)
PUT /sessions/{session_id}/students/{student_id}/attendance

Request Body:
{
  "attendance_status": "present",
  "note": "Học viên đã đến sau khi điểm danh"
}

Response 200:
{
  "student_session_id": 2,
  "student_id": 101,
  "session_id": 1,
  "attendance_status": "present",
  "note": "Học viên đã đến sau khi điểm danh",
  "recorded_at": "2025-02-03T09:00:00Z"
}

8.2 Session Reporting (Teacher)
Submit Session Report (TEACHER)
POST /sessions/{session_id}/report

Request Body:
{
  "status": "done",
  "actual_start_time": "08:05:00",
  "actual_end_time": "10:25:00",
  "content_covered": "Covered introduction, greetings, and basic self-introduction. All students participated actively.",
  "homework_assigned": "Practice self-introduction at home, complete worksheet page 5-7",
  "notes": "Rất nhiều học viên còn ngại giao tiếp, cần khuyến khích nhiều hơn",
  "next_session_preparation": "Prepare alphabet flashcards"
}

Response 200:
{
  "session_id": 1,
  "status": "done",
  "teacher_note": "Content covered...",
  "reported_at": "2025-02-03T10:35:00Z",
  "message": "Session report submitted successfully"
}

Get Session Report
GET /sessions/{session_id}/report

Response 200:
{
  "session_id": 1,
  "class": {
    "id": 1,
    "code": "ENG-A1-2025-01"
  },
  "date": "2025-02-03",
  "planned_time": {
    "start": "08:00:00",
    "end": "10:30:00"
  },
  "actual_time": {
    "start": "08:05:00",
    "end": "10:25:00"
  },
  "status": "done",
  "course_session": {
    "sequence_no": 1,
    "topic": "Introduction & Greetings",
    "planned_task": "Practice self-introduction with partners"
  },
  "attendance_summary": {
    "total": 23,
    "present": 20,
    "absent": 2,
    "late": 1,
    "attendance_rate": 86.96
  },
  "report": {
    "content_covered": "Covered introduction, greetings, and basic self-introduction. All students participated actively.",
    "homework_assigned": "Practice self-introduction at home, complete worksheet page 5-7",
    "notes": "Rất nhiều học viên còn ngại giao tiếp, cần khuyến khích nhiều hơn",
    "next_session_preparation": "Prepare alphabet flashcards",
    "reported_by": {
      "teacher_id": 5,
      "teacher_name": "Nguyễn Thị B"
    },
    "reported_at": "2025-02-03T10:35:00Z"
  }
}


IX. ASSESSMENTS & SCORES
9.1 Assessments
Get Assessments by Class
GET /classes/{class_id}/assessments

Response 200:
{
  "data": [
    {
      "id": 1,
      "class_id": 1,
      "name": "Quiz 1",
      "kind": "quiz",
      "max_score": 10,
      "weight": 10,
      "session_id": 12,
      "session_date": "2025-02-20",
      "scores_submitted": 20,
      "total_students": 23,
      "created_at": "2025-02-15T00:00:00Z"
    },
    {
      "id": 2,
      "class_id": 1,
      "name": "Midterm Exam",
      "kind": "midterm",
      "max_score": 100,
      "weight": 30,
      "session_id": 18,
      "session_date": "2025-03-15",
      "scores_submitted": 0,
      "total_students": 23,
      "created_at": "2025-02-15T00:00:00Z"
    }
  ]
}

Create Assessment (ACADEMIC_STAFF, TEACHER)
POST /classes/{class_id}/assessments

Request Body:
{
  "name": "Quiz 2",
  "kind": "quiz",
  "max_score": 10,
  "weight": 10,
  "session_id": 24
}

Response 201:
{
  "id": 5,
  "class_id": 1,
  "name": "Quiz 2",
  "kind": "quiz",
  "max_score": 10,
  "weight": 10,
  "session_id": 24,
  "created_at": "2025-02-15T10:30:00Z"
}

9.2 Scores
Get Scores by Assessment
GET /assessments/{assessment_id}/scores

Response 200:
{
  "assessment": {
    "id": 1,
    "name": "Quiz 1",
    "kind": "quiz",
    "max_score": 10,
    "weight": 10
  },
  "scores": [
    {
      "id": 1,
      "student_id": 100,
      "student_code": "HV-2025-001",
      "student_name": "Lê Văn D",
      "score": 8.5,
      "percentage": 85,
      "feedback": "Good work! Keep practicing pronunciation.",
      "graded_by": {
        "teacher_id": 5,
        "teacher_name": "Nguyễn Thị B"
      },
      "graded_at": "2025-02-20T15:00:00Z"
    }
  ],
  "statistics": {
    "total_students": 23,
    "submitted": 20,
    "average": 7.8,
    "highest": 9.5,
    "lowest": 5.0,
    "pass_rate": 85
  }
}

Submit/Update Scores (TEACHER, ACADEMIC_STAFF)
POST /assessments/{assessment_id}/scores

Request Body:
{
  "scores": [
    {
      "student_id": 100,
      "score": 8.5,
      "feedback": "Good work! Keep practicing pronunciation."
    },
    {
      "student_id": 101,
      "score": 7.0,
      "feedback": "Need to improve grammar."
    }
  ]
}

Response 200:
{
  "assessment_id": 1,
  "total_scores": 20,
  "created": 18,
  "updated": 2,
  "graded_by": 5,
  "graded_at": "2025-02-20T15:00:00Z",
  "message": "Scores submitted successfully"
}

Import Scores from CSV (TEACHER, ACADEMIC_STAFF)
POST /assessments/{assessment_id}/scores/import

Request Body (multipart/form-data):
file: [CSV file]

CSV Format:
student_code,score,feedback
HV-2025-001,8.5,Good work
HV-2025-002,7.0,Need improvement

**Response 200:**
{
  "assessment_id": 1,
  "total_rows": 23,
  "successful": 22,
  "failed": 1,
  "scores_created": [
    {
      "student_id": 100,
      "student_code": "HV-2025-001",
      "score": 8.5
    }
  ],
  "errors": [
    {
      "row": 15,
      "student_code": "HV-INVALID",
      "error": "Student not found in this class"
    }
  ],
  "statistics": {
    "average": 7.8,
    "highest": 9.5,
    "lowest": 5.0
  }
}

9.3 Student Feedback
Submit Student Feedback (STUDENT)
POST /students/{id}/feedback

Request Body:
{
  "session_id": 12,
  "phase_id": 1,
  "rating": 5,
  "comment": "Giáo viên dạy rất hay, dễ hiểu. Lớp học vui vẻ và thoải mái."
}

Response 201:
{
  "id": 1,
  "student_id": 100,
  "session_id": 12,
  "phase_id": 1,
  "rating": 5,
  "comment": "Giáo viên dạy rất hay, dễ hiểu. Lớp học vui vẻ và thoải mái.",
  "submitted_at": "2025-02-20T20:00:00Z"
}

Get Feedback by Phase/Class
GET /classes/{class_id}/feedback

Query Parameters:
phase_id (optional)
rating_min (optional): 1-5
page, limit
Response 200:
{
  "data": [
    {
      "id": 1,
      "student": {
        "id": 100,
        "code": "HV-2025-001",
        "name": "Lê Văn D"
      },
      "phase": {
        "id": 1,
        "name": "Foundation Phase"
      },
      "rating": 5,
      "comment": "Giáo viên dạy rất hay, dễ hiểu. Lớp học vui vẻ và thoải mái.",
      "submitted_at": "2025-02-20T20:00:00Z"
    }
  ],
  "statistics": {
    "total_feedbacks": 20,
    "average_rating": 4.5,
    "rating_distribution": {
      "5": 12,
      "4": 6,
      "3": 2,
      "2": 0,
      "1": 0
    }
  },
  "pagination": {...}
}


X. REPORTS & ANALYTICS
10.1 Enrollment Report
Get Enrollment Report
GET /reports/enrollments

Query Parameters:
branch_id (optional)
start_date (required): YYYY-MM-DD
end_date (required): YYYY-MM-DD
level_id (optional)
Response 200:
{
  "period": {
    "start_date": "2025-02-01",
    "end_date": "2025-02-28"
  },
  "summary": {
    "total_active_enrollments": 450,
    "new_enrollments": 80,
    "transfers": 10,
    "dropped": 5,
    "completed": 15
  },
  "by_branch": [
    {
      "branch_id": 1,
      "branch_name": "Cầu Giấy",
      "total_active_enrollments": 200,
      "new_enrollments": 35,
      "fill_rate": 80.0
    },
    {
      "branch_id": 2,
      "branch_name": "Hoàn Kiếm",
      "total_active_enrollments": 150,
      "new_enrollments": 25,
      "fill_rate": 75.0
    }
  ],
  "by_level": [
    {
      "level_id": 1,
      "level_name": "Beginner A1",
      "total_active_enrollments": 180,
      "new_enrollments": 40
    },
    {
      "level_id": 2,
      "level_name": "Elementary A2",
      "total_active_enrollments": 150,
      "new_enrollments": 25
    }
  ]
}

10.2 Attendance Report
Get Attendance Rate Report
GET /reports/attendance

Query Parameters:
branch_id (optional)
class_id (optional)
start_date (required): YYYY-MM-DD
end_date (required): YYYY-MM-DD
Response 200:
{
  "period": {
    "start_date": "2025-02-01",
    "end_date": "2025-02-28"
  },
  "overall": {
    "total_sessions": 500,
    "present_count": 9500,
    "total_expected": 11500,
    "attendance_rate": 82.61
  },
  "by_class": [
    {
      "class_id": 1,
      "class_code": "ENG-A1-2025-01",
      "branch_name": "Cầu Giấy",
      "present_sessions": 240,
      "total_done_sessions": 12,
      "total_expected": 276,
      "attendance_rate": 86.96
    },
    {
      "class_id": 2,
      "class_code": "ENG-A1-2025-02",
      "branch_name": "Cầu Giấy",
      "present_sessions": 200,
      "total_done_sessions": 10,
      "total_expected": 250,
      "attendance_rate": 80.0
    }
  ],
  "lowest_attendance": [
    {
      "class_id": 5,
      "class_code": "ENG-B1-2025-01",
      "attendance_rate": 65.5
    }
  ]
}

10.3 Class Utilization Report
Get Class Utilization Report
GET /reports/class-utilization

Query Parameters:
branch_id (optional)
start_date (required): YYYY-MM-DD
end_date (required): YYYY-MM-DD
Response 200:
{
  "period": {
    "start_date": "2025-02-01",
    "end_date": "2025-02-28"
  },
  "summary": {
    "total_classes": 20,
    "average_utilization": 85.5,
    "over_capacity": 2,
    "under_utilized": 3
  },
  "by_class": [
    {
      "class_id": 1,
      "class_code": "ENG-A1-2025-01",
      "branch_name": "Cầu Giấy",
      "max_capacity": 25,
      "actual_students": 23,
      "utilization_percent": 92.0
    },
    {
      "class_id": 2,
      "class_code": "ENG-A1-2025-02",
      "branch_name": "Cầu Giấy",
      "max_capacity": 25,
      "actual_students": 15,
      "utilization_percent": 60.0
    }
  ],
  "under_utilized_classes": [
    {
      "class_id": 2,
      "class_code": "ENG-A1-2025-02",
      "utilization_percent": 60.0,
      "available_slots": 10
    }
  ]
}

10.4 Teacher Workload Report
Get Teacher Workload Report
GET /reports/teacher-workload

Query Parameters:
branch_id (optional)
teacher_id (optional)
start_date (required): YYYY-MM-DD
end_date (required): YYYY-MM-DD
Response 200:
{
  "period": {
    "start_date": "2025-02-01",
    "end_date": "2025-02-28"
  },
  "summary": {
    "total_teachers": 15,
    "total_hours": 450.0,
    "average_hours_per_teacher": 30.0
  },
  "by_teacher": [
    {
      "teacher_id": 5,
      "teacher_name": "Nguyễn Thị B",
      "employee_code": "T-HN-001",
      "total_sessions_taught": 24,
      "total_hours": 60.0,
      "classes_count": 3,
      "ot_sessions": 2,
      "ot_hours": 5.0,
      "leave_requests": 1,
      "substitutions": 0
    },
    {
      "teacher_id": 8,
      "teacher_name": "Trần Văn C",
      "employee_code": "T-HN-002",
      "total_sessions_taught": 20,
      "total_hours": 50.0,
      "classes_count": 2,
      "ot_sessions": 0,
      "ot_hours": 0,
      "leave_requests": 0,
      "substitutions": 1
    }
  ],
  "top_workload": [
    {
      "teacher_id": 5,
      "teacher_name": "Nguyễn Thị B",
      "total_hours": 60.0
    }
  ]
}

10.5 Resource Utilization Report
Get Resource Utilization Report
GET /reports/resource-utilization

Query Parameters:
branch_id (required)
resource_type (optional): ROOM|VIRTUAL
start_date (required): YYYY-MM-DD
end_date (required): YYYY-MM-DD
Response 200:
{
  "period": {
    "start_date": "2025-02-01",
    "end_date": "2025-02-28"
  },
  "branch": {
    "id": 1,
    "name": "Cầu Giấy"
  },
  "summary": {
    "total_resources": 10,
    "average_utilization": 75.5,
    "total_hours_used": 1200.0,
    "total_hours_available": 1600.0
  },
  "by_resource": [
    {
      "resource_id": 1,
      "resource_type": "ROOM",
      "resource_name": "Phòng 101",
      "capacity": 25,
      "hours_used": 150.0,
      "hours_available": 200.0,
      "utilization_percent": 75.0,
      "sessions_count": 60
    },
    {
      "resource_id": 10,
      "resource_type": "VIRTUAL",
      "resource_name": "Zoom Account 1",
      "hours_used": 120.0,
      "hours_available": 200.0,
      "utilization_percent": 60.0,
      "sessions_count": 48
    }
  ],
  "peak_hours": [
    {
      "time_slot": "08:00-10:30",
      "utilization_percent": 90.0
    },
    {
      "time_slot": "14:00-16:30",
      "utilization_percent": 85.0
    }
  ]
}

10.6 CLO Attainment Report
Get CLO Attainment Report
GET /reports/clo-attainment

Query Parameters:
course_id (optional)
class_id (optional)
Response 200:
{
  "course": {
    "id": 1,
    "code": "ENG-A1-V1",
    "name": "English A1 Course Version 1"
  },
  "class": {
    "id": 1,
    "code": "ENG-A1-2025-01"
  },
  "summary": {
    "total_clos": 8,
    "achieved_clos": 3,
    "in_progress_clos": 3,
    "not_started_clos": 2
  },
  "by_clo": [
    {
      "clo_id": 1,
      "clo_code": "CLO-01",
      "description": "Có thể giới thiệu bản thân và người khác",
      "total_scores_recorded": 23,
      "average_score_attainment": 8.2,
      "max_score": 10,
      "attainment_percent": 82.0,
      "is_achieved": true,
      "mapped_sessions_completed": 3,
      "mapped_sessions_total": 3
    },
    {
      "clo_id": 2,
      "clo_code": "CLO-02",
      "description": "Có thể hỏi và trả lời các câu hỏi đơn giản",
      "total_scores_recorded": 20,
      "average_score_attainment": 7.5,
      "max_score": 10,
      "attainment_percent": 75.0,
      "is_achieved": true,
      "mapped_sessions_completed": 2,
      "mapped_sessions_total": 4
    }
  ],
  "low_attainment_clos": [
    {
      "clo_code": "CLO-05",
      "description": "Có thể viết một đoạn văn ngắn về bản thân",
      "attainment_percent": 65.0
    }
  ]
}

10.7 Feedback Rating Report
Get Feedback Rating Report
GET /reports/feedback-rating

Query Parameters:
branch_id (optional)
class_id (optional)
start_date (required): YYYY-MM-DD
end_date (required): YYYY-MM-DD
Response 200:
{
  "period": {
    "start_date": "2025-02-01",
    "end_date": "2025-02-28"
  },
  "summary": {
    "total_feedbacks": 150,
    "average_rating": 4.5,
    "rating_distribution": {
      "5": 85,
      "4": 45,
      "3": 15,
      "2": 3,
      "1": 2
    }
  },
  "by_class_and_phase": [
    {
      "class_id": 1,
      "class_code": "ENG-A1-2025-01",
      "class_name": "English A1 - Sáng T2-T4-T6",
      "branch_name": "Cầu Giấy",
      "phase_id": 1,
      "phase_name": "Foundation Phase",
      "avg_rating": 4.7,
      "count_feedback": 20,
      "star_5": 15,
      "star_4": 4,
      "star_3": 1,
      "star_2": 0,
      "star_1": 0
    }
  ],
  "top_rated_classes": [
    {
      "class_id": 1,
      "class_code": "ENG-A1-2025-01",
      "avg_rating": 4.7,
      "count_feedback": 20
    }
  ],
  "low_rated_classes": [
    {
      "class_id": 5,
      "class_code": "ENG-B1-2025-01",
      "avg_rating": 3.2,
      "count_feedback": 15
    }
  ]
}

10.8 Daily Operations Report
Get Daily Run-sheet Report
GET /reports/daily-runsheet

Query Parameters:
date (required): YYYY-MM-DD
branch_id (optional)
Response 200:
{
  "date": "2025-02-15",
  "summary": {
    "total_sessions": 25,
    "completed": 20,
    "ongoing": 2,
    "planned": 3,
    "cancelled": 0,
    "missing_attendance": 5,
    "missing_report": 8
  },
  "sessions": [
    {
      "session_id": 50,
      "class_code": "ENG-A1-2025-01",
      "time": "08:00-10:30",
      "status": "done",
      "teacher": "Nguyễn Thị B",
      "resource": "Phòng 101",
      "students_count": 23,
      "attendance_recorded": true,
      "report_submitted": true
    },
    {
      "session_id": 55,
      "class_code": "ENG-A2-2025-02",
      "time": "14:00-16:30",
      "status": "done",
      "teacher": "Trần Văn C",
      "resource": "Zoom Account 1",
      "students_count": 20,
      "attendance_recorded": false,
      "report_submitted": false
    }
  ],
  "alerts": [
    {
      "type": "missing_attendance",
      "session_id": 55,
      "class_code": "ENG-A2-2025-02",
      "teacher": "Trần Văn C",
      "message": "Attendance not recorded after 2 hours"
    },
    {
      "type": "missing_report",
      "session_id": 52,
      "class_code": "ENG-A1-2025-05",
      "teacher": "Phạm Thị D",
      "message": "Session report not submitted"
    }
  ]
}


XI. DASHBOARDS
11.1 Center Head Dashboard
Get Center Head Dashboard
GET /dashboards/center-head

Query Parameters:
branch_id (required)
date (optional, default: today): YYYY-MM-DD
Response 200:
{
  "branch": {
    "id": 1,
    "name": "Cầu Giấy"
  },
  "date": "2025-02-15",
  "overview": {
    "active_classes": 15,
    "total_students": 350,
    "total_teachers": 12,
    "sessions_today": 8
  },
  "today_schedule": {
    "total_sessions": 8,
    "completed": 5,
    "ongoing": 1,
    "upcoming": 2,
    "missing_attendance": 1
  },
  "resource_utilization": {
    "rooms": {
      "total": 5,
      "in_use": 3,
      "utilization_percent": 60.0
    },
    "virtual": {
      "total": 3,
      "in_use": 1,
      "utilization_percent": 33.3
    }
  },
  "attendance_this_week": {
    "average_rate": 85.5,
    "trend": "up",
    "change_percent": 2.5
  },
  "pending_requests": {
    "teacher_requests": 3,
    "student_requests": 8
  },
  "alerts": [
    {
      "type": "low_attendance",
      "class_code": "ENG-B1-2025-01",
      "message": "Class attendance below 70% this week",
      "severity": "warning"
    },
    {
      "type": "missing_report",
      "session_id": 55,
      "class_code": "ENG-A2-2025-02",
      "message": "Session report not submitted after 2 hours",
      "severity": "high"
    }
  ],
  "conflicts": [
    {
      "type": "teacher_conflict",
      "date": "2025-02-16",
      "teacher": "Nguyễn Thị B",
      "sessions": [45, 50]
    }
  ]
}

11.2 Manager Dashboard
Get Manager Dashboard
GET /dashboards/manager

Query Parameters:
center_id (required)
period (optional, default: month): week|month|quarter
Response 200:
{
  "center": {
    "id": 1,
    "name": "Minato Hà Nội"
  },
  "period": "month",
  "date_range": {
    "from": "2025-02-01",
    "to": "2025-02-28"
  },
  "overview": {
    "total_branches": 3,
    "total_classes": 45,
    "total_students": 1050,
    "total_teachers": 35,
    "new_enrollments": 150
  },
  "by_branch": [
    {
      "branch_id": 1,
      "branch_name": "Cầu Giấy",
      "active_classes": 15,
      "students": 350,
      "teachers": 12,
      "attendance_rate": 85.5,
      "fill_rate": 85.0,
      "feedback_rating": 4.5
    },
    {
      "branch_id": 2,
      "branch_name": "Hoàn Kiếm",
      "active_classes": 18,
      "students": 420,
      "teachers": 15,
      "attendance_rate": 82.0,
      "fill_rate": 90.0,
      "feedback_rating": 4.3
    }
  ],
  "kpis": {
    "overall_attendance_rate": 84.2,
    "overall_fill_rate": 87.5,
    "average_feedback_rating": 4.4,
    "completion_rate": 92.0,
    "on_time_completion_rate": 88.0
  },
  "trends": {
    "enrollment": {
      "current": 150,
      "previous": 130,
      "change_percent": 15.4,
      "trend": "up"
    },
    "attendance": {
      "current": 84.2,
      "previous": 82.5,
      "change_percent": 2.1,
      "trend": "up"
    }
  },
  "top_teachers": [
    {
      "teacher_id": 5,
      "teacher_name": "Nguyễn Thị B",
      "feedback_rating": 4.8,
      "classes_taught": 3,
      "total_hours": 60.0
    }
  ],
  "pending_approvals": {
    "courses": 2,
    "classes": 5
  }
}

11.3 Teacher Dashboard
Get Teacher Dashboard
GET /dashboards/teacher

Query Parameters:
teacher_id (required)
date (optional, default: today): YYYY-MM-DD
Response 200:
{
  "teacher": {
    "id": 5,
    "name": "Nguyễn Thị B",
    "employee_code": "T-HN-001"
  },
  "date": "2025-02-15",
  "today_schedule": [
    {
      "session_id": 50,
      "time": "08:00-10:30",
      "class_code": "ENG-A1-2025-01",
      "topic": "Present Simple Tense",
      "resource": "Phòng 101",
      "students_count": 23,
      "status": "done",
      "attendance_recorded": true,
      "report_submitted": true
    },
    {
      "session_id": 55,
      "time": "14:00-16:30",
      "class_code": "ENG-A2-2025-02",
      "topic": "Past Tense Review",
      "resource": "Phòng 102",
      "students_count": 20,
      "status": "planned",
      "attendance_recorded": false,
      "report_submitted": false
    }
  ],
  "this_week_summary": {
    "total_sessions": 12,
    "completed": 8,
    "upcoming": 4,
    "total_hours": 30.0,
    "classes": 3
  },
  "pending_tasks": [
    {
      "type": "attendance",
      "session_id": 48,
      "class_code": "ENG-A1-2025-01",
      "date": "2025-02-13",
      "message": "Attendance not recorded"
    },
    {
      "type": "report",
      "session_id": 49,
      "class_code": "ENG-A1-2025-01",
      "date": "2025-02-14",
      "message": "Session report not submitted"
    },
    {
      "type": "scores",
      "assessment_id": 5,
      "class_code": "ENG-A2-2025-02",
      "message": "5 scores pending for Quiz 2"
    }
  ],
  "my_classes": [
    {
      "class_id": 1,
      "class_code": "ENG-A1-2025-01",
      "students_count": 23,
      "attendance_rate": 86.96,
      "next_session": "2025-02-17 08:00"
    }
  ],
  "requests": {
    "pending": 1,
    "approved": 2,
    "rejected": 0
  }
}

11.4 Student Dashboard
Get Student Dashboard
GET /dashboards/student

Query Parameters:
student_id (required)
Response 200:
{
  "student": {
    "id": 100,
    "code": "HV-2025-001",
    "name": "Lê Văn D"
  },
  "current_classes": [
    {
      "class_id": 1,
      "class_code": "ENG-A1-2025-01",
      "class_name": "English A1 - Sáng T2-T4-T6",
      "teacher": "Nguyễn Thị B",
      "progress": {
        "sessions_completed": 12,
        "total_sessions": 36,
        "completion_percent": 33.3
      },
      "attendance": {
        "present": 10,
        "absent": 1,
        "late": 0,
        "excused": 1,
        "rate": 83.33
      },
      "next_session": {
        "date": "2025-02-17",
        "time": "08:00-10:30",
        "topic": "Future Tense",
        "resource": "Phòng 101"
      }
    }
  ],
  "upcoming_sessions": [
    {
      "session_id": 60,
      "class_code": "ENG-A1-2025-01",
      "date": "2025-02-17",
      "time": "08:00-10:30",
      "topic": "Future Tense",
      "teacher": "Nguyễn Thị B",
      "resource": "Phòng 101"
    }
  ],
  "recent_scores": [
    {
      "assessment_id": 1,
      "assessment_name": "Quiz 1",
      "class_code": "ENG-A1-2025-01",
      "score": 8.5,
      "max_score": 10,
      "percentage": 85,
      "feedback": "Good work! Keep practicing pronunciation.",
      "graded_at": "2025-02-10T15:00:00Z"
    }
  ],
  "alerts": [
    {
      "type": "low_attendance",
      "class_code": "ENG-A1-2025-01",
      "message": "Your attendance rate is below 85%",
      "severity": "warning"
    }
  ],
  "pending_requests": 1
}


XII. COMMON PATTERNS & ERROR HANDLING
12.1 Pagination
All list endpoints support pagination with the following query parameters:
page: integer (default: 1)
limit: integer (default: 20, max: 100)

Pagination response format:
{
  "data": [...],
  "pagination": {
    "current_page": 1,
    "total_pages": 10,
    "total_items": 200,
    "limit": 20,
    "has_next": true,
    "has_prev": false
  }
}

12.2 Sorting
List endpoints support sorting with:
sort_by: field name
sort_order: asc|desc (default: asc)

Example:
GET /classes?sort_by=start_date&sort_order=desc

12.3 Filtering
Most list endpoints support filtering by relevant fields. Use query parameters matching the field names.
Example:
GET /classes?branch_id=1&status=ongoing&modality=OFFLINE

12.4 Error Responses
Standard Error Format
{
  "error": "error_code",
  "message": "Human-readable error message",
  "details": {
    "field": "Additional context"
  },
  "timestamp": "2025-01-15T10:30:00Z"
}

Common HTTP Status Codes
200 OK - Successful GET/PUT request 201 Created - Successful POST request 204 No Content - Successful DELETE request 400 Bad Request - Invalid input data
{
  "error": "validation_error",
  "message": "Invalid input data",
  "details": {
    "email": "Invalid email format",
    "start_date": "Start date must be before end date"
  }
}

401 Unauthorized - Missing or invalid authentication
{
  "error": "unauthorized",
  "message": "Authentication required"
}

403 Forbidden - Insufficient permissions
{
  "error": "forbidden",
  "message": "You do not have permission to perform this action",
  "required_role": "MANAGER"
}

404 Not Found - Resource not found
{
  "error": "not_found",
  "message": "Class with id 999 not found"
}

409 Conflict - Resource conflict
{
  "error": "capacity_exceeded",
  "message": "Class is at maximum capacity",
  "max_capacity": 25,
  "current_enrollment": 25
}

422 Unprocessable Entity - Business logic error
{
  "error": "business_logic_error",
  "message": "Cannot approve class with unresolved conflicts",
  "conflicts": [
    {
      "type": "resource_conflict",
      "session_id": 15,
      "resource_id": 1,
      "message": "Room 101 is already booked for this time slot"
    }
  ]
}

500 Internal Server Error - Server error
{
  "error": "internal_server_error",
  "message": "An unexpected error occurred. Please try again later.",
  "request_id": "req_abc123xyz"
}

12.5 Rate Limiting
API implements rate limiting per user/IP:
Standard users: 1000 requests per hour
Admin users: 5000 requests per hour
Rate limit headers in response:
X-RateLimit-Limit: 1000
X-RateLimit-Remaining: 995
X-RateLimit-Reset: 1642089600

When rate limit exceeded (HTTP 429):
{
  "error": "rate_limit_exceeded",
  "message": "Too many requests. Please try again later.",
  "retry_after": 3600
}


XIII. WEBHOOKS & NOTIFICATIONS
13.1 Webhook Configuration
Register Webhook Endpoint (ADMIN)
POST /webhooks

Request Body:
{
  "url": "https://your-app.com/webhooks/ems",
  "events": [
    "class.approved",
    "enrollment.created",
    "session.completed",
    "attendance.recorded",
    "request.approved"
  ],
  "secret": "your_webhook_secret"
}

Response 201:
{
  "id": 1,
  "url": "https://your-app.com/webhooks/ems",
  "events": ["class.approved", "enrollment.created", ...],
  "active": true,
  "created_at": "2025-01-15T10:30:00Z"
}

13.2 Webhook Event Format
All webhook events follow this format:
{
  "id": "evt_abc123xyz",
  "event": "class.approved",
  "timestamp": "2025-01-15T10:30:00Z",
  "data": {
    "class_id": 5,
    "class_code": "ENG-A1-2025-02",
    "approved_by": 10,
    "approved_at": "2025-01-15T10:30:00Z"
  }
}

13.3 Available Events
Class Events:
class.created
class.approved
class.rejected
class.started
class.completed
class.schedule_changed
Enrollment Events:
enrollment.created
enrollment.transferred
enrollment.completed
Session Events:
session.created
session.cancelled
session.completed
session.rescheduled
Attendance Events:
attendance.recorded
attendance.threshold_warning (student approaching absence limit)
Request Events:
teacher_request.created
teacher_request.approved
teacher_request.rejected
student_request.created
student_request.approved
student_request.rejected
Assessment Events:
assessment.created
scores.submitted

XIV. BATCH OPERATIONS
14.1 Bulk Operations
Bulk Update Session Status
POST /sessions/bulk-update

Request Body:
{
  "session_ids": [1, 2, 3, 4, 5],
  "updates": {
    "status": "cancelled",
    "teacher_note": "Cancelled due to holiday"
  }
}

Response 200:
{
  "total": 5,
  "successful": 5,
  "failed": 0,
  "updated_sessions": [1, 2, 3, 4, 5]
}

Bulk Assign Teacher
POST /teachers/{teacher_id}/bulk-assign

Request Body:
{
  "session_ids": [10, 11, 12, 13],
  "skill": "speaking",
  "role": "primary"
}

Response 200:
{
  "total": 4,
  "successful": 4,
  "failed": 0,
  "conflicts": [],
  "teaching_slots_created": 4
}

14.2 Export Operations
Export Class Data
GET /classes/{class_id}/export

Query Parameters:
format: csv|xlsx|pdf (default: xlsx)
include: attendance|scores|feedback (comma-separated)
Response 200: Binary file download with appropriate Content-Type header
Export Report Data
GET /reports/{report_type}/export

Query Parameters:
format: csv|xlsx|pdf (default: xlsx)
start_date: YYYY-MM-DD
end_date: YYYY-MM-DD
Additional filters based on report type
Response 200: Binary file download

XV. ADVANCED FEATURES
15.1 Search & Global Search
Global Search
GET /search

Query Parameters:
q (required): Search query
type (optional): student|teacher|class|course (comma-separated)
branch_id (optional)
limit (default: 20)
Response 200:
{
  "query": "english",
  "total_results": 45,
  "results": {
    "classes": [
      {
        "id": 1,
        "type": "class",
        "code": "ENG-A1-2025-01",
        "name": "English A1 - Sáng T2-T4-T6",
        "branch": "Cầu Giấy",
        "status": "ongoing",
        "relevance_score": 0.95
      }
    ],
    "courses": [
      {
        "id": 1,
        "type": "course",
        "code": "ENG-A1-V1",
        "name": "English A1 Course Version 1",
        "relevance_score": 0.92
      }
    ],
    "students": [
      {
        "id": 100,
        "type": "student",
        "code": "HV-2025-001",
        "name": "Lê Văn D",
        "relevance_score": 0.45
      }
    ],
    "teachers": []
  }
}

15.2 Conflict Detection
Check Schedule Conflicts
POST /conflicts/check

Request Body:
{
  "type": "session",
  "date": "2025-02-20",
  "start_time": "08:00:00",
  "end_time": "10:30:00",
  "teacher_ids": [5, 8],
  "resource_ids": [1],
  "exclude_session_id": null
}

Response 200:
{
  "has_conflicts": true,
  "conflicts": [
    {
      "type": "teacher_conflict",
      "teacher_id": 5,
      "teacher_name": "Nguyễn Thị B",
      "conflicting_session_id": 45,
      "conflicting_class_code": "ENG-B1-2025-01",
      "time_overlap": {
        "start": "08:00:00",
        "end": "10:30:00"
      }
    },
    {
      "type": "resource_conflict",
      "resource_id": 1,
      "resource_name": "Phòng 101",
      "conflicting_session_id": 50,
      "conflicting_class_code": "ENG-A2-2025-03"
    }
  ],
  "available_alternatives": {
    "teachers": [
      {
        "teacher_id": 12,
        "teacher_name": "Phạm Thị D",
        "is_available": true
      }
    ],
    "resources": [
      {
        "resource_id": 2,
        "resource_name": "Phòng 102",
        "is_available": true
      }
    ]
  }
}

15.3 Audit Logs
Get Audit Logs
GET /audit-logs

Query Parameters:
entity_type (optional): class|session|enrollment|user
entity_id (optional)
action (optional): create|update|delete|approve|reject
user_id (optional): Who performed the action
date_from (optional): YYYY-MM-DD
date_to (optional): YYYY-MM-DD
page, limit
Response 200:
{
  "data": [
    {
      "id": 1234,
      "entity_type": "class",
      "entity_id": 5,
      "action": "approve",
      "performed_by": {
        "user_id": 10,
        "name": "Nguyễn Văn Manager",
        "role": "MANAGER"
      },
      "changes": {
        "status": {
          "old": "draft",
          "new": "scheduled"
        },
        "approved_by": {
          "old": null,
          "new": 10
        }
      },
      "timestamp": "2025-01-15T11:00:00Z",
      "ip_address": "192.168.1.100",
      "user_agent": "Mozilla/5.0..."
    }
  ],
  "pagination": {...}
}

15.4 Notifications
Get User Notifications
GET /notifications

Query Parameters:
status (optional): unread|read|all (default: unread)
type (optional): class|session|request|assessment
page, limit
Response 200:
{
  "data": [
    {
      "id": 1,
      "type": "request_approved",
      "title": "Yêu cầu học bù đã được duyệt",
      "message": "Yêu cầu học bù của bạn cho buổi ngày 2025-02-10 đã được chấp thuận. Bạn sẽ học bù vào lớp ENG-A1-2025-03 ngày 2025-02-12.",
      "status": "unread",
      "priority": "normal",
      "data": {
        "request_id": 12,
        "request_type": "makeup",
        "makeup_session_id": 45
      },
      "created_at": "2025-02-08T11:00:00Z"
    },
    {
      "id": 2,
      "type": "attendance_warning",
      "title": "Cảnh báo tỷ lệ chuyên cần",
      "message": "Tỷ lệ chuyên cần của bạn đang dưới 85%. Vui lòng cố gắng tham gia đầy đủ các buổi học tiếp theo.",
      "status": "unread",
      "priority": "high",
      "data": {
        "class_id": 1,
        "attendance_rate": 83.33,
        "absences": 2
      },
      "created_at": "2025-02-07T18:00:00Z"
    }
  ],
  "summary": {
    "total_unread": 5,
    "by_priority": {
      "high": 2,
      "normal": 3,
      "low": 0
    }
  },
  "pagination": {...}
}

Mark Notification as Read
PUT /notifications/{id}/read

Response 200:
{
  "id": 1,
  "status": "read",
  "read_at": "2025-02-08T12:00:00Z"
}

Mark All Notifications as Read
POST /notifications/mark-all-read

Response 200:
{
  "marked_read": 5,
  "timestamp": "2025-02-08T12:00:00Z"
}


XVI. SYSTEM ADMINISTRATION
16.1 User Management
Get All Users (ADMIN)
GET /users

Query Parameters:
role (optional): Filter by role code
branch_id (optional): Filter by branch
status (optional): active|inactive|pending
page, limit
Response 200:
{
  "data": [
    {
      "id": 25,
      "email": "teacher.b@example.com",
      "phone": "+84901234567",
      "full_name": "Nguyễn Thị B",
      "status": "active",
      "roles": [
        {
          "id": 3,
          "code": "TEACHER",
          "name": "Teacher"
        }
      ],
      "branches": [
        {
          "id": 1,
          "code": "HN-CG",
          "name": "Cầu Giấy"
        }
      ],
      "last_login_at": "2025-02-15T08:00:00Z",
      "created_at": "2020-01-01T00:00:00Z"
    }
  ],
  "pagination": {...}
}

Create User (ADMIN)
POST /users

Request Body:
{
  "email": "newuser@example.com",
  "phone": "+84909999999",
  "full_name": "Người Dùng Mới",
  "role_ids": [2],
  "branch_ids": [1],
  "status": "active"
}

Response 201:
{
  "id": 250,
  "email": "newuser@example.com",
  "full_name": "Người Dùng Mới",
  "status": "active",
  "temporary_password": "TempPass123!",
  "created_at": "2025-01-15T10:30:00Z",
  "message": "User created successfully. Temporary password sent via email."
}

Update User (ADMIN)
PUT /users/{id}

Request Body:
{
  "full_name": "Updated Name",
  "status": "inactive",
  "role_ids": [2, 3],
  "branch_ids": [1, 2]
}

Response 200:
{
  "id": 25,
  "email": "teacher.b@example.com",
  "full_name": "Updated Name",
  "status": "inactive",
  "updated_at": "2025-01-15T10:30:00Z"
}

Reset User Password (ADMIN)
POST /users/{id}/reset-password

Response 200:
{
  "user_id": 25,
  "temporary_password": "NewTemp456!",
  "expires_at": "2025-01-22T10:30:00Z",
  "message": "Password reset successfully. Temporary password sent via email."
}

16.2 Role Management
Get All Roles (ADMIN)
GET /roles

Response 200:
{
  "data": [
    {
      "id": 1,
      "code": "VISITOR",
      "name": "Visitor",
      "description": "Public visitor with read-only access to catalog"
    },
    {
      "id": 2,
      "code": "STUDENT",
      "name": "Student",
      "description": "Enrolled student"
    },
    {
      "id": 3,
      "code": "TEACHER",
      "name": "Teacher",
      "description": "Teaching staff"
    },
    {
      "id": 4,
      "code": "SUBJECT_LEADER",
      "name": "Subject Leader",
      "description": "Academic curriculum designer"
    },
    {
      "id": 5,
      "code": "ACADEMIC_STAFF",
      "name": "Academic Staff",
      "description": "Academic operations staff"
    },
    {
      "id": 6,
      "code": "QA",
      "name": "QA",
      "description": "Quality assurance staff"
    },
    {
      "id": 7,
      "code": "MANAGER",
      "name": "Manager",
      "description": "Branch manager"
    },
    {
      "id": 8,
      "code": "CENTER_HEAD",
      "name": "Center Head",
      "description": "Center director"
    },
    {
      "id": 9,
      "code": "ADMIN",
      "name": "Admin",
      "description": "System administrator"
    }
  ]
}

Assign Role to User (ADMIN)
POST /users/{user_id}/roles

Request Body:
{
  "role_id": 3
}

Response 201:
{
  "user_id": 25,
  "role_id": 3,
  "role_code": "TEACHER",
  "assigned_at": "2025-01-15T10:30:00Z"
}

Remove Role from User (ADMIN)
DELETE /users/{user_id}/roles/{role_id}

Response 204: No Content
16.3 System Configuration
Get System Settings (ADMIN)
GET /system/settings

Response 200:
{
  "general": {
    "system_name": "EMS - Education Management System",
    "timezone": "Asia/Ho_Chi_Minh",
    "date_format": "DD/MM/YYYY",
    "time_format": "HH:mm"
  },
  "attendance": {
    "max_absences_per_course": 3,
    "late_arrival_threshold_minutes": 15,
    "attendance_lock_hours_after_session": 24
  },
  "enrollment": {
    "allow_late_join_sessions": 2,
    "waitlist_enabled": true,
    "auto_enroll_from_waitlist": true
  },
  "requests": {
    "student_request_approval_required": true,
    "teacher_request_approval_required": true,
    "makeup_request_advance_days": 2
  },
  "notifications": {
    "email_enabled": true,
    "sms_enabled": true,
    "push_enabled": true
  }
}

Update System Settings (ADMIN)
PUT /system/settings

Request Body:
{
  "attendance": {
    "max_absences_per_course": 4,
    "late_arrival_threshold_minutes": 10
  }
}

Response 200:
{
  "updated_settings": {
    "attendance.max_absences_per_course": 4,
    "attendance.late_arrival_threshold_minutes": 10
  },
  "updated_at": "2025-01-15T10:30:00Z",
  "updated_by": 1
}

16.4 System Health
Health Check
GET /health

Response 200:
{
  "status": "healthy",
  "timestamp": "2025-02-15T10:30:00Z",
  "version": "1.0.0",
  "services": {
    "database": {
      "status": "up",
      "response_time_ms": 5
    },
    "storage": {
      "status": "up",
      "response_time_ms": 12
    },
    "cache": {
      "status": "up",
      "response_time_ms": 2
    },
    "email": {
      "status": "up",
      "response_time_ms": 50
    }
  },
  "uptime_seconds": 1234567
}


XVII. APPENDIX
17.1 Enumeration Values
session_status_enum
planned: Buổi học được lên lịch nhưng chưa diễn ra
cancelled: Buổi học đã bị hủy
done: Buổi học đã hoàn thành
session_type_enum
CLASS: Buổi học thường
MAKEUP: Buổi học bù
EXAM: Buổi kiểm tra/thi
OTHER: Khác
attendance_status_enum
planned: Dự kiến tham gia
present: Có mặt
absent: Vắng mặt không phép
late: Đến muộn
excused: Vắng có phép
remote: Tham gia từ xa (nếu hybrid)
enrollment_status_enum
enrolled: Đang học
waitlisted: Đang trong danh sách chờ
transferred: Đã chuyển lớp
dropped: Đã bỏ học
completed: Đã hoàn thành
request_status_enum
pending: Đang chờ duyệt
approved: Đã duyệt
rejected: Đã từ chối
cancelled: Đã hủy
teacher_request_type_enum
leave: Xin nghỉ
swap: Đổi ca
ot: Đăng ký làm thêm giờ
reschedule: Xin dời lịch
student_request_type_enum
absence: Xin nghỉ
makeup: Xin học bù
transfer: Xin chuyển lớp
reschedule: Xin đổi lịch
resource_type_enum
ROOM: Phòng học vật lý
VIRTUAL: Phòng học ảo (Zoom, Teams, etc.)
modality_enum
OFFLINE: Học trực tiếp
ONLINE: Học trực tuyến
HYBRID: Kết hợp
skill_enum
general: Tổng hợp
reading: Đọc
writing: Viết
speaking: Nói
listening: Nghe
teaching_role_enum
primary: Giáo viên chính
assistant: Giáo viên phụ
class_status_enum
draft: Đang soạn thảo
scheduled: Đã được duyệt và lên lịch
ongoing: Đang diễn ra
completed: Đã hoàn thành
cancelled: Đã hủy
assessment_kind_enum
quiz: Kiểm tra ngắn
midterm: Kiểm tra giữa kỳ
final: Kiểm tra cuối kỳ
assignment: Bài tập
project: Dự án
oral: Thi nói
practice: Bài thực hành
other: Khác
17.2 Date/Time Formats
Date: YYYY-MM-DD (ISO 8601)
Time: HH:MM:SS (24-hour format)
DateTime: YYYY-MM-DDTHH:MM:SSZ (ISO 8601 with UTC timezone)
17.3 Common HTTP Headers
Request Headers:
Authorization: Bearer {access_token}
Content-Type: application/json
Accept: application/json
X-Request-ID: {unique_request_id}

Response Headers:
Content-Type: application/json
X-Request-ID: {unique_request_id}
X-RateLimit-Limit: 1000
X-RateLimit-Remaining: 995
X-RateLimit-Reset: 1642089600

17.4 Permission Matrix Summary
Role
Class Mgmt
Enrollment
Attendance
Teacher Ops
Reports
System Admin
VISITOR
Read
-
-
-
-
-
STUDENT
Read
Read (Self)
Read (Self)
-
Read (Self)
-
TEACHER
Read
Read
C/U
C/U (Self)
Read
-
SUBJECT_LEADER
Read
-
-
-
Read
-
ACADEMIC_STAFF
C/U
C/U
C/U
Read
Read
-
QA
Read
Read
Read
-
Read
-
MANAGER
C/U/A
C/U/A
C/U/A
A
Read
-
CENTER_HEAD
A
A
Read
A
Read
-
ADMIN
Full
Full
Full
Full
Full
Full

Legend:
C: Create
R: Read
U: Update
D: Delete
A: Approve
Full: All permissions

XVIII. EXAMPLES & USE CASES
18.1 Complete Flow: Creating and Running a Class
Step 1: Create Course (Subject Leader)
POST /courses
{
  "subject_id": 1,
  "level_id": 1,
  "code": "ENG-A1-V2",
  "name": "English A1 Version 2",
  "total_hours": 60,
  "duration_weeks": 12,
  "session_per_week": 3,
  "hours_per_session": 1.5
}

Step 2: Submit for Approval
POST /courses/15/submit

Step 3: Approve Course (Manager - strategic curriculum decision)
POST /courses/15/approve
{
  "action": "approve"
}

Step 4: Create Class (Academic Staff at branch)
POST /classes
{
  "branch_id": 1,
  "course_id": 15,
  "code": "ENG-A1-2025-10",
  "start_date": "2025-03-01",
  "schedule_days": [1, 3, 5],
  "schedule_mapping": {
    "1": {"slot_id": 1},
    "3": {"slot_id": 1},
    "5": {"slot_id": 1}
  },
  "max_capacity": 25
}

Step 5: Assign Teachers
POST /sessions/1/teachers
{
  "teacher_id": 5,
  "skill": "speaking",
  "role": "primary"
}

Step 6: Assign Resources
POST /sessions/1/resources
{
  "resource_type": "ROOM",
  "resource_id": 1
}

Step 7: Validate and Submit Class
POST /classes/5/validate
POST /classes/5/submit

Step 8: Approve Class (Center Head for their branch, Manager for cross-branch)
POST /classes/5/approve
{
  "action": "approve"
}

Step 9: Enroll Students (Academic Staff)
POST /classes/5/enrollments/bulk
{
  "student_ids": [100, 101, 102, ...]
}

Step 10: Teacher Records Attendance
POST /sessions/1/attendance
{
  "attendances": [
    {"student_id": 100, "attendance_status": "present"},
    {"student_id": 101, "attendance_status": "late"}
  ]
}

18.2 Student Makeup Request Flow
Step 1: Student Finds Available Makeup Sessions
GET /students/100/requests/makeup/available-sessions?missed_session_id=15

Step 2: Student Creates Makeup Request
POST /students/100/requests/makeup
{
  "target_session_id": 15,
  "makeup_session_id": 45,
  "note": "Em xin học bù buổi này"
}

Step 3: Academic Staff Approves
POST /student-requests/12/approve

Result: System automatically:
Marks original session as excused
Creates student_session record for makeup session with is_makeup=true
Sends notification to student
18.3 Teacher Leave Request with Substitute
Step 1: Teacher Creates Leave Request
POST /teachers/5/requests
{
  "session_id": 20,
  "request_type": "leave",
  "note": "Có việc gia đình đột xuất"
}

Step 2: Academic Staff Finds Substitutes
GET /teacher-requests/10/substitute-teachers

Step 3: Academic Staff Approves with Substitute
POST /teacher-requests/10/approve
{
  "substitute_teacher_id": 8,
  "resolution": "Đã duyệt. GV Trần Văn C sẽ dạy thay."
}

Result: System automatically:
Approves original leave request
Creates OT request for substitute teacher
Updates teaching_slot to new teacher
Sends notifications to all parties

Document Version: 1.0
Last Updated: 2025-14-10
Maintained by: SEP_G25

-- =========================================
-- EMS-SEP490-BE: Complete Database Schema (PostgreSQL 16)
-- =========================================
-- 1) Drop tables (cascade)
-- 2) Create enum types
-- 3) Create tables (đúng thứ tự phụ thuộc)
-- 4) Create indexes
-- 5) Comments + notice
-- =========================================

-- ========== SECTION 1: DROP EXISTING TABLES ==========
DROP TABLE IF EXISTS teacher_request CASCADE;
DROP TABLE IF EXISTS student_request CASCADE;
DROP TABLE IF EXISTS qa_report CASCADE;
DROP TABLE IF EXISTS student_feedback CASCADE;
DROP TABLE IF EXISTS score CASCADE;
DROP TABLE IF EXISTS assessment CASCADE;
DROP TABLE IF EXISTS course_assessment_clo_mapping CASCADE;
DROP TABLE IF EXISTS course_assessment CASCADE;
DROP TABLE IF EXISTS teacher_availability CASCADE;
DROP TABLE IF EXISTS student_session CASCADE;
DROP TABLE IF EXISTS enrollment CASCADE;
DROP TABLE IF EXISTS teaching_slot CASCADE;
DROP TABLE IF EXISTS session_resource CASCADE;
DROP TABLE IF EXISTS session CASCADE;
DROP TABLE IF EXISTS course_session_clo_mapping CASCADE;
DROP TABLE IF EXISTS plo_clo_mapping CASCADE;
DROP TABLE IF EXISTS clo CASCADE;
DROP TABLE IF EXISTS plo CASCADE;
DROP TABLE IF EXISTS course_material CASCADE;
DROP TABLE IF EXISTS course_session CASCADE;
DROP TABLE IF EXISTS course_phase CASCADE;
DROP TABLE IF EXISTS "class" CASCADE;
DROP TABLE IF EXISTS teacher_skill CASCADE;
DROP TABLE IF EXISTS student CASCADE;
DROP TABLE IF EXISTS teacher CASCADE;
DROP TABLE IF EXISTS user_branches CASCADE;
DROP TABLE IF EXISTS user_role CASCADE;
DROP TABLE IF EXISTS resource CASCADE;
DROP TABLE IF EXISTS time_slot_template CASCADE;
DROP TABLE IF EXISTS course CASCADE;
DROP TABLE IF EXISTS level CASCADE;
DROP TABLE IF EXISTS subject CASCADE;
DROP TABLE IF EXISTS branch CASCADE;
DROP TABLE IF EXISTS center CASCADE;
DROP TABLE IF EXISTS role CASCADE;
DROP TABLE IF EXISTS user_account CASCADE;

-- ========== SECTION 2: ENUM TYPES ==========
DO $$ BEGIN CREATE TYPE session_status_enum AS ENUM ('planned','cancelled','done'); EXCEPTION WHEN duplicate_object THEN NULL; END $$;
DO $$ BEGIN CREATE TYPE session_type_enum   AS ENUM ('class','makeup','exam','other', 'teacher_schedule'); EXCEPTION WHEN duplicate_object THEN NULL; END $$;
DO $$ BEGIN CREATE TYPE attendance_status_enum AS ENUM ('planned','present','absent','late','excused','remote'); EXCEPTION WHEN duplicate_object THEN NULL; END $$;
DO $$ BEGIN CREATE TYPE enrollment_status_enum AS ENUM ('enrolled','waitlisted','transferred','dropped','completed'); EXCEPTION WHEN duplicate_object THEN NULL; END $$;
DO $$ BEGIN CREATE TYPE request_status_enum AS ENUM ('pending','waiting_confirm','approved','rejected','cancelled'); EXCEPTION WHEN duplicate_object THEN NULL; END $$;
DO $$ BEGIN CREATE TYPE teacher_request_type_enum AS ENUM ('leave','swap','ot','reschedule'); EXCEPTION WHEN duplicate_object THEN NULL; END $$;
DO $$ BEGIN CREATE TYPE student_request_type_enum AS ENUM ('absence','makeup','transfer','reschedule'); EXCEPTION WHEN duplicate_object THEN NULL; END $$;
DO $$ BEGIN CREATE TYPE resource_type_enum AS ENUM ('room','virtual'); EXCEPTION WHEN duplicate_object THEN NULL; END $$;
DO $$ BEGIN CREATE TYPE modality_enum AS ENUM ('offline','online','hybrid'); EXCEPTION WHEN duplicate_object THEN NULL; END $$;
DO $$ BEGIN CREATE TYPE skill_enum AS ENUM ('general','reading','writing','speaking','listening'); EXCEPTION WHEN duplicate_object THEN NULL; END $$;
DO $$ BEGIN CREATE TYPE teaching_role_enum AS ENUM ('primary','assistant'); EXCEPTION WHEN duplicate_object THEN NULL; END $$;
DO $$ BEGIN CREATE TYPE branch_status_enum AS ENUM ('active','inactive','closed','planned'); EXCEPTION WHEN duplicate_object THEN NULL; END $$;
DO $$ BEGIN CREATE TYPE class_status_enum AS ENUM ('draft','scheduled','ongoing','completed','cancelled'); EXCEPTION WHEN duplicate_object THEN NULL; END $$;
DO $$ BEGIN CREATE TYPE subject_status_enum AS ENUM ('draft','active','inactive'); EXCEPTION WHEN duplicate_object THEN NULL; END $$;
DO $$ BEGIN CREATE TYPE assessment_kind_enum AS ENUM ('quiz','midterm','final','assignment','project','oral','practice','other'); EXCEPTION WHEN duplicate_object THEN NULL; END $$;
DO $$ BEGIN CREATE TYPE teaching_slot_status_enum AS ENUM ('scheduled','on_leave','substituted','completed','cancelled'); EXCEPTION WHEN duplicate_object THEN NULL; END $$;
DO $$ BEGIN CREATE TYPE homework_status_enum AS ENUM ('completed','incomplete','no_homework'); EXCEPTION WHEN duplicate_object THEN NULL; END $$;
-- ========== SECTION 3: TABLES (ĐÚNG THỨ TỰ) ==========

-- TIER 1: Independent
CREATE TABLE center (
  id BIGSERIAL PRIMARY KEY,
  code VARCHAR(255) NOT NULL UNIQUE,
  name VARCHAR(255) NOT NULL,
  description TEXT,
  phone VARCHAR(50),
  email VARCHAR(255),
  created_at TIMESTAMPTZ DEFAULT CURRENT_TIMESTAMP,
  updated_at TIMESTAMPTZ DEFAULT CURRENT_TIMESTAMP
);

CREATE TABLE role (
  id BIGSERIAL PRIMARY KEY,
  code VARCHAR(50) NOT NULL UNIQUE,
  name VARCHAR(255) NOT NULL
);

CREATE TABLE user_account (
  id BIGSERIAL PRIMARY KEY,
  email VARCHAR(255) UNIQUE,
  phone VARCHAR(50),
  facebook_url VARCHAR(500),
  full_name VARCHAR(255) NOT NULL,
  gender VARCHAR(20),
  dob DATE,
  address TEXT,
  password_hash VARCHAR(255) NOT NULL,
  status VARCHAR(50),
  last_login_at TIMESTAMPTZ,
  created_at TIMESTAMPTZ DEFAULT CURRENT_TIMESTAMP,
  updated_at TIMESTAMPTZ DEFAULT CURRENT_TIMESTAMP
);

-- TIER 2
CREATE TABLE branch (
  id BIGSERIAL PRIMARY KEY,
  center_id BIGINT NOT NULL,
  code VARCHAR(50) NOT NULL,
  name VARCHAR(255) NOT NULL,
  address TEXT,
  location VARCHAR(255),
  phone VARCHAR(50),
  capacity INTEGER,
  status branch_status_enum NOT NULL DEFAULT 'active',
  opening_date DATE,
  created_at TIMESTAMPTZ DEFAULT CURRENT_TIMESTAMP,
  updated_at TIMESTAMPTZ DEFAULT CURRENT_TIMESTAMP,
  CONSTRAINT fk_branch_center FOREIGN KEY(center_id) REFERENCES center(id) ON DELETE CASCADE,
  CONSTRAINT uq_branch_center_code UNIQUE(center_id,code)
);

CREATE TABLE subject (
  id BIGSERIAL PRIMARY KEY,
  code VARCHAR(50) NOT NULL UNIQUE,
  name VARCHAR(255) NOT NULL,
  description TEXT,
  status subject_status_enum NOT NULL DEFAULT 'active',
  created_by BIGINT,
  created_at TIMESTAMPTZ DEFAULT CURRENT_TIMESTAMP,
  updated_at TIMESTAMPTZ DEFAULT CURRENT_TIMESTAMP,
  CONSTRAINT fk_subject_created_by FOREIGN KEY(created_by) REFERENCES user_account(id) ON DELETE SET NULL
);

CREATE TABLE time_slot_template (
  id BIGSERIAL PRIMARY KEY,
  branch_id BIGINT NOT NULL,
  name VARCHAR(255) NOT NULL,
  start_time TIME NOT NULL,
  end_time TIME NOT NULL,
  duration_min INTEGER,
  created_at TIMESTAMPTZ DEFAULT CURRENT_TIMESTAMP,
  updated_at TIMESTAMPTZ DEFAULT CURRENT_TIMESTAMP,
  CONSTRAINT fk_timeslot_branch FOREIGN KEY(branch_id) REFERENCES branch(id) ON DELETE CASCADE
);

CREATE TABLE resource (
  id BIGSERIAL PRIMARY KEY,
  branch_id BIGINT NOT NULL,
  resource_type resource_type_enum NOT NULL,
  name VARCHAR(255) NOT NULL,
  location VARCHAR(255),
  capacity INTEGER,
  description TEXT,
  equipment TEXT,
  meeting_url VARCHAR(500),
  meeting_id VARCHAR(255),
  account_email VARCHAR(255),
  license_type VARCHAR(100),
  expiry_date DATE,
  renewal_date DATE,
  created_by BIGINT,
  created_at TIMESTAMPTZ DEFAULT CURRENT_TIMESTAMP,
  updated_at TIMESTAMPTZ DEFAULT CURRENT_TIMESTAMP,
  CONSTRAINT fk_resource_branch FOREIGN KEY(branch_id) REFERENCES branch(id) ON DELETE CASCADE,
  CONSTRAINT fk_resource_created_by FOREIGN KEY(created_by) REFERENCES user_account(id) ON DELETE SET NULL
);

CREATE TABLE user_role (
  user_id BIGINT NOT NULL,
  role_id BIGINT NOT NULL,
  PRIMARY KEY(user_id,role_id),
  CONSTRAINT fk_userrole_user FOREIGN KEY(user_id) REFERENCES user_account(id) ON DELETE CASCADE,
  CONSTRAINT fk_userrole_role FOREIGN KEY(role_id) REFERENCES role(id) ON DELETE CASCADE
);

CREATE TABLE user_branches (
  user_id BIGINT NOT NULL,
  branch_id BIGINT NOT NULL,
  assigned_at TIMESTAMPTZ DEFAULT CURRENT_TIMESTAMP,
  assigned_by BIGINT,
  PRIMARY KEY(user_id,branch_id),
  CONSTRAINT fk_userbranch_user FOREIGN KEY(user_id) REFERENCES user_account(id) ON DELETE CASCADE,
  CONSTRAINT fk_userbranch_branch FOREIGN KEY(branch_id) REFERENCES branch(id) ON DELETE CASCADE,
  CONSTRAINT fk_userbranch_assigned_by FOREIGN KEY(assigned_by) REFERENCES user_account(id) ON DELETE SET NULL
);

CREATE TABLE teacher (
  id BIGSERIAL PRIMARY KEY,
  user_account_id BIGINT NOT NULL UNIQUE,
  employee_code VARCHAR(50) UNIQUE,
  note TEXT,
  created_at TIMESTAMPTZ DEFAULT CURRENT_TIMESTAMP NOT NULL,
  updated_at TIMESTAMPTZ DEFAULT CURRENT_TIMESTAMP NOT NULL,
  CONSTRAINT fk_teacher_user_account FOREIGN KEY(user_account_id) REFERENCES user_account(id) ON DELETE CASCADE
);

CREATE TABLE student (
  id BIGSERIAL PRIMARY KEY,
  user_id BIGINT NOT NULL UNIQUE,
  student_code VARCHAR(50) UNIQUE,
  level VARCHAR(50),
  created_at TIMESTAMPTZ DEFAULT CURRENT_TIMESTAMP NOT NULL,
  updated_at TIMESTAMPTZ DEFAULT CURRENT_TIMESTAMP NOT NULL,
  CONSTRAINT fk_student_user FOREIGN KEY(user_id) REFERENCES user_account(id) ON DELETE CASCADE
);

-- TIER 3: Curriculum
CREATE TABLE level (
  id BIGSERIAL PRIMARY KEY,
  subject_id BIGINT NOT NULL,
  code VARCHAR(50) NOT NULL,
  name VARCHAR(255) NOT NULL,
  standard_type VARCHAR(100),
  expected_duration_hours INTEGER,
  sort_order INTEGER,
  description TEXT,
  created_at TIMESTAMPTZ DEFAULT CURRENT_TIMESTAMP NOT NULL,
  updated_at TIMESTAMPTZ DEFAULT CURRENT_TIMESTAMP NOT NULL,
  CONSTRAINT fk_level_subject FOREIGN KEY(subject_id) REFERENCES subject(id) ON DELETE CASCADE,
  CONSTRAINT uq_level_subject_code UNIQUE(subject_id,code)
);

CREATE TABLE replacement_skill_assessment (
  id BIGSERIAL PRIMARY KEY,
  student_id BIGINT NOT NULL,
  skill skill_enum NOT NULL,  -- Sử dụng lại enum: general, reading, writing, speaking, listening
  level_id BIGINT,  -- Link đến bảng level (ví dụ: A1, A2, B1, B2...)
  score INTEGER,  -- Điểm số cụ thể (ví dụ: IELTS band score * 10 = 65 cho 6.5)
  assessment_date DATE NOT NULL,  -- Ngày đánh giá
  assessment_type VARCHAR(100),  -- Loại đánh giá: 'placement_test', 'ielts', 'toeic', 'internal_exam', 'self_assessment'
  note TEXT,  -- Ghi chú thêm
  assessed_by BIGINT,  -- Người đánh giá (teacher/academic staff)
  created_at TIMESTAMPTZ DEFAULT CURRENT_TIMESTAMP NOT NULL,
  updated_at TIMESTAMPTZ DEFAULT CURRENT_TIMESTAMP NOT NULL,
  
  CONSTRAINT fk_student_skill_student FOREIGN KEY(student_id) REFERENCES student(id) ON DELETE CASCADE,
  CONSTRAINT fk_student_skill_level FOREIGN KEY(level_id) REFERENCES level(id) ON DELETE SET NULL,
  CONSTRAINT fk_student_skill_assessed_by FOREIGN KEY(assessed_by) REFERENCES user_account(id) ON DELETE SET NULL,
  CONSTRAINT uq_student_skill_assessment UNIQUE(student_id, skill, assessment_date)  -- Một student có thể test lại nhiều lần
);

CREATE TABLE course (
  id BIGSERIAL PRIMARY KEY,
  subject_id BIGINT NOT NULL,
  level_id BIGINT,
  logical_course_code VARCHAR(100),
  version INTEGER,
  code VARCHAR(100) NOT NULL UNIQUE,
  name VARCHAR(255) NOT NULL,
  description TEXT,
  total_hours INTEGER,
  duration_weeks INTEGER,
  session_per_week INTEGER,
  hours_per_session DECIMAL(5,2),
  prerequisites TEXT,
  target_audience TEXT,
  teaching_methods TEXT,
  effective_date DATE,
  status VARCHAR(50),
  approved_by_manager BIGINT,
  approved_at TIMESTAMPTZ,
  rejection_reason TEXT,
  hash_checksum VARCHAR(255),
  created_by BIGINT,
  created_at TIMESTAMPTZ DEFAULT CURRENT_TIMESTAMP,
  updated_at TIMESTAMPTZ DEFAULT CURRENT_TIMESTAMP,
  CONSTRAINT fk_course_subject FOREIGN KEY(subject_id) REFERENCES subject(id) ON DELETE CASCADE,
  CONSTRAINT fk_course_level FOREIGN KEY(level_id) REFERENCES level(id) ON DELETE SET NULL,
  CONSTRAINT fk_course_approved_by FOREIGN KEY(approved_by_manager) REFERENCES user_account(id) ON DELETE SET NULL,
  CONSTRAINT fk_course_created_by FOREIGN KEY(created_by) REFERENCES user_account(id) ON DELETE SET NULL
);

CREATE TABLE course_phase (
  id BIGSERIAL PRIMARY KEY,
  course_id BIGINT NOT NULL,
  phase_number INTEGER NOT NULL,
  name VARCHAR(255),
  duration_weeks INTEGER,
  learning_focus TEXT,
  sort_order INTEGER,
  created_at TIMESTAMPTZ DEFAULT CURRENT_TIMESTAMP NOT NULL,
  updated_at TIMESTAMPTZ DEFAULT CURRENT_TIMESTAMP NOT NULL,
  CONSTRAINT fk_course_phase_course FOREIGN KEY(course_id) REFERENCES course(id) ON DELETE CASCADE,
  CONSTRAINT uq_course_phase_course_number UNIQUE(course_id,phase_number)
);

CREATE TABLE course_session (
  id BIGSERIAL PRIMARY KEY,
  phase_id BIGINT NOT NULL,
  sequence_no INTEGER NOT NULL,
  topic VARCHAR(500),
  student_task TEXT,
  skill_set skill_enum[],
  created_at TIMESTAMPTZ DEFAULT CURRENT_TIMESTAMP NOT NULL,
  updated_at TIMESTAMPTZ DEFAULT CURRENT_TIMESTAMP NOT NULL,
  CONSTRAINT fk_course_session_phase FOREIGN KEY(phase_id) REFERENCES course_phase(id) ON DELETE CASCADE,
  CONSTRAINT uq_course_session_phase_sequence UNIQUE(phase_id,sequence_no)
);

CREATE TABLE course_material (
  id BIGSERIAL PRIMARY KEY,
  course_id BIGINT NOT NULL,
  phase_id BIGINT,
  course_session_id BIGINT,
  title VARCHAR(500) NOT NULL,
  url VARCHAR(1000) NOT NULL,
  uploaded_by BIGINT,
  created_at TIMESTAMPTZ DEFAULT CURRENT_TIMESTAMP NOT NULL,
  updated_at TIMESTAMPTZ DEFAULT CURRENT_TIMESTAMP NOT NULL,
  CONSTRAINT fk_course_material_course FOREIGN KEY(course_id) REFERENCES course(id) ON DELETE CASCADE,
  CONSTRAINT fk_course_material_phase FOREIGN KEY(phase_id) REFERENCES course_phase(id) ON DELETE CASCADE,
  CONSTRAINT fk_course_material_session FOREIGN KEY(course_session_id) REFERENCES course_session(id) ON DELETE CASCADE,
  CONSTRAINT fk_course_material_uploaded_by FOREIGN KEY(uploaded_by) REFERENCES user_account(id) ON DELETE SET NULL
);

CREATE TABLE plo (
  id BIGSERIAL PRIMARY KEY,
  subject_id BIGINT NOT NULL,
  code VARCHAR(50) NOT NULL,
  description TEXT NOT NULL,
  created_at TIMESTAMPTZ DEFAULT CURRENT_TIMESTAMP NOT NULL,
  updated_at TIMESTAMPTZ DEFAULT CURRENT_TIMESTAMP NOT NULL,
  CONSTRAINT fk_plo_subject FOREIGN KEY(subject_id) REFERENCES subject(id) ON DELETE CASCADE,
  CONSTRAINT uq_plo_subject_code UNIQUE(subject_id,code)
);

CREATE TABLE clo (
  id BIGSERIAL PRIMARY KEY,
  course_id BIGINT NOT NULL,
  code VARCHAR(50) NOT NULL,
  description TEXT NOT NULL,
  created_at TIMESTAMPTZ DEFAULT CURRENT_TIMESTAMP NOT NULL,
  updated_at TIMESTAMPTZ DEFAULT CURRENT_TIMESTAMP NOT NULL,
  CONSTRAINT fk_clo_course FOREIGN KEY(course_id) REFERENCES course(id) ON DELETE CASCADE,
  CONSTRAINT uq_clo_course_code UNIQUE(course_id,code)
);

CREATE TABLE plo_clo_mapping (
  plo_id BIGINT NOT NULL,
  clo_id BIGINT NOT NULL,
  status VARCHAR(50),
  PRIMARY KEY (plo_id,clo_id),
  CONSTRAINT fk_plo_clo_plo FOREIGN KEY(plo_id) REFERENCES plo(id) ON DELETE CASCADE,
  CONSTRAINT fk_plo_clo_clo FOREIGN KEY(clo_id) REFERENCES clo(id) ON DELETE CASCADE
);

CREATE TABLE course_session_clo_mapping (
  course_session_id BIGINT NOT NULL,
  clo_id BIGINT NOT NULL,
  status VARCHAR(50),
  PRIMARY KEY (course_session_id,clo_id),
  CONSTRAINT fk_course_session_clo_session FOREIGN KEY(course_session_id) REFERENCES course_session(id) ON DELETE CASCADE,
  CONSTRAINT fk_course_session_clo_clo FOREIGN KEY(clo_id) REFERENCES clo(id) ON DELETE CASCADE
);

CREATE TABLE course_assessment (
  id BIGSERIAL PRIMARY KEY,
  course_id BIGINT NOT NULL,
  name VARCHAR(255) NOT NULL,
  kind assessment_kind_enum NOT NULL,
  skills skill_enum[] NOT NULL,
  max_score DECIMAL(5,2) NOT NULL,
  description TEXT,
  created_at TIMESTAMPTZ DEFAULT CURRENT_TIMESTAMP NOT NULL,
  updated_at TIMESTAMPTZ DEFAULT CURRENT_TIMESTAMP NOT NULL,
  CONSTRAINT fk_course_assessment_course FOREIGN KEY(course_id) REFERENCES course(id) ON DELETE CASCADE
);

CREATE TABLE course_assessment_clo_mapping (
  course_assessment_id BIGINT NOT NULL,
  clo_id BIGINT NOT NULL,
  status VARCHAR(50),
  PRIMARY KEY(course_assessment_id,clo_id),
  CONSTRAINT fk_course_assessment_clo_assessment FOREIGN KEY(course_assessment_id) REFERENCES course_assessment(id) ON DELETE CASCADE,
  CONSTRAINT fk_course_assessment_clo_clo FOREIGN KEY(clo_id) REFERENCES clo(id) ON DELETE CASCADE
);

-- TIER 4: Operations
CREATE TABLE "class" (
  id BIGSERIAL PRIMARY KEY,
  branch_id BIGINT NOT NULL,
  course_id BIGINT NOT NULL,
  code VARCHAR(50) NOT NULL,
  name VARCHAR(255),
  modality modality_enum NOT NULL,
  start_date DATE NOT NULL,
  planned_end_date DATE,
  actual_end_date DATE,
  schedule_days SMALLINT[],
  max_capacity integer,
  status class_status_enum NOT NULL DEFAULT 'draft',
  created_by BIGINT,
  submitted_at TIMESTAMPTZ,
  approved_by BIGINT,
  approved_at TIMESTAMPTZ,
  rejection_reason TEXT,
  created_at TIMESTAMPTZ DEFAULT CURRENT_TIMESTAMP,
  updated_at TIMESTAMPTZ DEFAULT CURRENT_TIMESTAMP,
  CONSTRAINT fk_class_branch FOREIGN KEY(branch_id) REFERENCES branch(id) ON DELETE CASCADE,
  CONSTRAINT fk_class_course FOREIGN KEY(course_id) REFERENCES course(id) ON DELETE CASCADE,
  CONSTRAINT fk_class_created_by FOREIGN KEY(created_by) REFERENCES user_account(id) ON DELETE SET NULL,
  CONSTRAINT fk_class_approved_by FOREIGN KEY(approved_by) REFERENCES user_account(id) ON DELETE SET NULL,
  CONSTRAINT uq_class_branch_code UNIQUE(branch_id,code)
);

CREATE TABLE session (
  id BIGSERIAL PRIMARY KEY,
  class_id BIGINT,
  course_session_id BIGINT,
  time_slot_template_id BIGINT,
  date DATE NOT NULL,
  type session_type_enum NOT NULL DEFAULT 'class',
  status session_status_enum NOT NULL DEFAULT 'planned',
  teacher_note TEXT,
  created_at TIMESTAMPTZ DEFAULT CURRENT_TIMESTAMP NOT NULL,
  updated_at TIMESTAMPTZ DEFAULT CURRENT_TIMESTAMP NOT NULL,
  CONSTRAINT fk_session_class FOREIGN KEY(class_id) REFERENCES "class"(id) ON DELETE CASCADE,
  CONSTRAINT fk_session_course_session FOREIGN KEY(course_session_id) REFERENCES course_session(id) ON DELETE SET NULL,
  CONSTRAINT fk_session_time_slot_template FOREIGN KEY(time_slot_template_id) REFERENCES time_slot_template(id) ON DELETE SET NULL
);

CREATE TABLE teacher_skill (
  teacher_id BIGINT NOT NULL,
  skill skill_enum NOT NULL,
  level SMALLINT,
  PRIMARY KEY(teacher_id,skill),
  CONSTRAINT fk_teacher_skill_teacher FOREIGN KEY(teacher_id) REFERENCES teacher(id) ON DELETE CASCADE
);

CREATE TABLE teacher_availability (
  teacher_id BIGINT NOT NULL,
  time_slot_template_id BIGINT NOT NULL,
  day_of_week SMALLINT NOT NULL CHECK (day_of_week BETWEEN 1 AND 7),
  effective_date DATE,
  note TEXT,
  created_at TIMESTAMPTZ DEFAULT CURRENT_TIMESTAMP NOT NULL,
  updated_at TIMESTAMPTZ DEFAULT CURRENT_TIMESTAMP NOT NULL,
  PRIMARY KEY(teacher_id,time_slot_template_id,day_of_week),
  CONSTRAINT fk_teacher_availability_teacher FOREIGN KEY(teacher_id) REFERENCES teacher(id) ON DELETE CASCADE,
  CONSTRAINT fk_teacher_availability_timeslot FOREIGN KEY(time_slot_template_id) REFERENCES time_slot_template(id) ON DELETE CASCADE
);

CREATE TABLE session_resource (
  session_id BIGINT NOT NULL,
  resource_type resource_type_enum NOT NULL,
  resource_id BIGINT NOT NULL,
  capacity_override INTEGER,
  PRIMARY KEY(session_id,resource_type,resource_id),
  CONSTRAINT fk_session_resource_session FOREIGN KEY(session_id) REFERENCES session(id) ON DELETE CASCADE,
  CONSTRAINT fk_session_resource_resource FOREIGN KEY(resource_id) REFERENCES resource(id) ON DELETE CASCADE
);

CREATE TABLE teaching_slot (
  session_id BIGINT NOT NULL,
  teacher_id BIGINT NOT NULL,
  skill skill_enum NOT NULL,
  role teaching_role_enum NOT NULL,
  status teaching_slot_status_enum NOT NULL DEFAULT 'scheduled',
  PRIMARY KEY(session_id,teacher_id,skill),
  CONSTRAINT fk_teaching_slot_session FOREIGN KEY(session_id) REFERENCES session(id) ON DELETE CASCADE,
  CONSTRAINT fk_teaching_slot_teacher FOREIGN KEY(teacher_id) REFERENCES teacher(id) ON DELETE CASCADE
);

CREATE TABLE enrollment (
  id BIGSERIAL PRIMARY KEY,
  class_id BIGINT NOT NULL,
  student_id BIGINT NOT NULL,
  status enrollment_status_enum NOT NULL DEFAULT 'enrolled',
  enrolled_at TIMESTAMPTZ,
  left_at TIMESTAMPTZ,
  join_session_id BIGINT,
  left_session_id BIGINT,
  created_at TIMESTAMPTZ DEFAULT CURRENT_TIMESTAMP NOT NULL,
  updated_at TIMESTAMPTZ DEFAULT CURRENT_TIMESTAMP NOT NULL,
  CONSTRAINT fk_enrollment_class FOREIGN KEY(class_id) REFERENCES "class"(id) ON DELETE CASCADE,
  CONSTRAINT fk_enrollment_student FOREIGN KEY(student_id) REFERENCES student(id) ON DELETE CASCADE,
  CONSTRAINT fk_enrollment_join_session FOREIGN KEY(join_session_id) REFERENCES session(id) ON DELETE SET NULL,
  CONSTRAINT fk_enrollment_left_session FOREIGN KEY(left_session_id) REFERENCES session(id) ON DELETE SET NULL,
  CONSTRAINT uq_enrollment_class_student UNIQUE(class_id,student_id)
);

CREATE TABLE student_session (
  student_id BIGINT NOT NULL,
  session_id BIGINT NOT NULL,
  is_makeup BOOLEAN DEFAULT false,
  attendance_status attendance_status_enum NOT NULL DEFAULT 'planned',
  homework_status homework_status_enum,
  note TEXT,
  recorded_at TIMESTAMPTZ,
  PRIMARY KEY(student_id,session_id),
  CONSTRAINT fk_student_session_student FOREIGN KEY(student_id) REFERENCES student(id) ON DELETE CASCADE,
  CONSTRAINT fk_student_session_session FOREIGN KEY(session_id) REFERENCES session(id) ON DELETE CASCADE
);

-- TIER 5: Assessment & Feedback
CREATE TABLE assessment (
  id BIGSERIAL PRIMARY KEY,
  class_id BIGINT NOT NULL,
  course_assessment_id BIGINT,
  name VARCHAR(255) NOT NULL,
  kind assessment_kind_enum NOT NULL,
  max_score DECIMAL(5,2) NOT NULL,
  description TEXT,
  created_by BIGINT,
  created_at TIMESTAMPTZ DEFAULT CURRENT_TIMESTAMP NOT NULL,
  updated_at TIMESTAMPTZ DEFAULT CURRENT_TIMESTAMP NOT NULL,
  CONSTRAINT fk_assessment_class FOREIGN KEY(class_id) REFERENCES "class"(id) ON DELETE CASCADE,
  CONSTRAINT fk_assessment_course_assessment FOREIGN KEY(course_assessment_id) REFERENCES course_assessment(id) ON DELETE SET NULL,
  CONSTRAINT fk_assessment_created_by FOREIGN KEY(created_by) REFERENCES user_account(id) ON DELETE SET NULL
);

CREATE TABLE score (
  id BIGSERIAL PRIMARY KEY,
  assessment_id BIGINT NOT NULL,
  student_id BIGINT NOT NULL,
  score DECIMAL(5,2) NOT NULL,
  feedback TEXT,
  graded_by BIGINT,
  graded_at TIMESTAMPTZ,
  CONSTRAINT fk_score_assessment FOREIGN KEY(assessment_id) REFERENCES assessment(id) ON DELETE CASCADE,
  CONSTRAINT fk_score_student FOREIGN KEY(student_id) REFERENCES student(id) ON DELETE CASCADE,
  CONSTRAINT fk_score_graded_by FOREIGN KEY(graded_by) REFERENCES teacher(id) ON DELETE SET NULL,
  CONSTRAINT uq_score_assessment_student UNIQUE(assessment_id,student_id)
);

CREATE TABLE student_feedback (
  id BIGSERIAL PRIMARY KEY,
  student_id BIGINT NOT NULL,
  session_id BIGINT NOT NULL,
  phase_id BIGINT,
  rating SMALLINT CHECK (rating BETWEEN 1 AND 5),
  comment TEXT,
  is_feedback BOOLEAN DEFAULT false,
  submitted_at TIMESTAMPTZ,
  CONSTRAINT fk_student_feedback_student FOREIGN KEY(student_id) REFERENCES student(id) ON DELETE CASCADE,
  CONSTRAINT fk_student_feedback_session FOREIGN KEY(session_id) REFERENCES session(id) ON DELETE CASCADE,
  CONSTRAINT fk_student_feedback_phase FOREIGN KEY(phase_id) REFERENCES course_phase(id) ON DELETE SET NULL
);

CREATE TABLE qa_report (
  id BIGSERIAL PRIMARY KEY,
  class_id BIGINT,
  session_id BIGINT,
  phase_id BIGINT,
  reported_by BIGINT,
  report_type VARCHAR(100),
  status VARCHAR(50),
  findings TEXT,
  action_items TEXT,
  created_at TIMESTAMPTZ DEFAULT CURRENT_TIMESTAMP NOT NULL,
  updated_at TIMESTAMPTZ DEFAULT CURRENT_TIMESTAMP NOT NULL,
  CONSTRAINT fk_qa_report_class FOREIGN KEY(class_id) REFERENCES "class"(id) ON DELETE CASCADE,
  CONSTRAINT fk_qa_report_session FOREIGN KEY(session_id) REFERENCES session(id) ON DELETE CASCADE,
  CONSTRAINT fk_qa_report_phase FOREIGN KEY(phase_id) REFERENCES course_phase(id) ON DELETE SET NULL,
  CONSTRAINT fk_qa_report_reported_by FOREIGN KEY(reported_by) REFERENCES user_account(id) ON DELETE SET NULL
);

-- TIER 6: Requests
CREATE TABLE student_request (
  id BIGSERIAL PRIMARY KEY,
  student_id BIGINT NOT NULL,
  current_class_id BIGINT,
  request_type student_request_type_enum NOT NULL,
  target_class_id BIGINT,
  target_session_id BIGINT,
  makeup_session_id BIGINT,
  effective_date DATE,
  effective_session_id BIGINT,
  status request_status_enum NOT NULL DEFAULT 'pending',
  submitted_at TIMESTAMPTZ,
  submitted_by BIGINT,
  decided_by BIGINT,
  decided_at TIMESTAMPTZ,
  note TEXT,
  created_at TIMESTAMPTZ DEFAULT CURRENT_TIMESTAMP NOT NULL,
  updated_at TIMESTAMPTZ DEFAULT CURRENT_TIMESTAMP NOT NULL,
  CONSTRAINT fk_student_request_student FOREIGN KEY(student_id) REFERENCES student(id) ON DELETE CASCADE,
  CONSTRAINT fk_student_request_current_class FOREIGN KEY(current_class_id) REFERENCES "class"(id) ON DELETE SET NULL,
  CONSTRAINT fk_student_request_target_class FOREIGN KEY(target_class_id) REFERENCES "class"(id) ON DELETE SET NULL,
  CONSTRAINT fk_student_request_target_session FOREIGN KEY(target_session_id) REFERENCES session(id) ON DELETE SET NULL,
  CONSTRAINT fk_student_request_makeup_session FOREIGN KEY(makeup_session_id) REFERENCES session(id) ON DELETE SET NULL,
  CONSTRAINT fk_student_request_effective_session FOREIGN KEY(effective_session_id) REFERENCES session(id) ON DELETE SET NULL,
  CONSTRAINT fk_student_request_submitted_by FOREIGN KEY(submitted_by) REFERENCES user_account(id) ON DELETE SET NULL,
  CONSTRAINT fk_student_request_decided_by FOREIGN KEY(decided_by) REFERENCES user_account(id) ON DELETE SET NULL
);

CREATE TABLE teacher_request (
  id BIGSERIAL PRIMARY KEY,
  teacher_id BIGINT NOT NULL,
  session_id BIGINT,
  request_type teacher_request_type_enum NOT NULL,
  replacement_teacher_id BIGINT,
  new_date DATE,
  new_time_slot_id BIGINT,
  new_resource_id BIGINT,
  reason TEXT,
  resolution TEXT,
  status request_status_enum NOT NULL DEFAULT 'pending',
  submitted_at TIMESTAMPTZ,
  submitted_by BIGINT,
  decided_by BIGINT,
  decided_at TIMESTAMPTZ,
  note TEXT,
  created_at TIMESTAMPTZ DEFAULT CURRENT_TIMESTAMP NOT NULL,
  updated_at TIMESTAMPTZ DEFAULT CURRENT_TIMESTAMP NOT NULL,
  CONSTRAINT fk_teacher_request_teacher FOREIGN KEY(teacher_id) REFERENCES teacher(id) ON DELETE CASCADE,
  CONSTRAINT fk_teacher_request_replacement_teacher FOREIGN KEY(replacement_teacher_id) REFERENCES teacher(id) ON DELETE SET NULL,
  CONSTRAINT fk_teacher_request_session FOREIGN KEY(session_id) REFERENCES session(id) ON DELETE SET NULL,
  CONSTRAINT fk_teacher_request_new_time_slot FOREIGN KEY(new_time_slot_id) REFERENCES time_slot_template(id) ON DELETE SET NULL,
  CONSTRAINT fk_teacher_request_new_resource FOREIGN KEY(new_resource_id) REFERENCES resource(id) ON DELETE SET NULL,
  CONSTRAINT fk_teacher_request_submitted_by FOREIGN KEY(submitted_by) REFERENCES user_account(id) ON DELETE SET NULL,
  CONSTRAINT fk_teacher_request_decided_by FOREIGN KEY(decided_by) REFERENCES user_account(id) ON DELETE SET NULL
);


-- ========== SECTION 4: INDEXES ==========
-- Organization & Infrastructure
CREATE INDEX idx_branch_center          ON branch(center_id);
CREATE INDEX idx_branch_status          ON branch(status);
CREATE INDEX idx_resource_branch        ON resource(branch_id);
CREATE INDEX idx_resource_type          ON resource(resource_type);
CREATE INDEX idx_timeslot_branch        ON time_slot_template(branch_id);

-- People & RBAC
CREATE INDEX idx_user_account_email     ON user_account(email);
CREATE INDEX idx_user_account_status    ON user_account(status);
CREATE INDEX idx_user_role_user         ON user_role(user_id);
CREATE INDEX idx_user_role_role         ON user_role(role_id);
CREATE INDEX idx_user_branch_user       ON user_branches(user_id);
CREATE INDEX idx_user_branch_branch     ON user_branches(branch_id);
CREATE INDEX idx_teacher_user_account   ON teacher(user_account_id);
CREATE INDEX idx_student_user           ON student(user_id);
CREATE INDEX idx_teacher_skill_teacher  ON teacher_skill(teacher_id);

-- Curriculum
CREATE INDEX idx_subject_status                 ON subject(status);
CREATE INDEX idx_level_subject                  ON level(subject_id);
CREATE INDEX idx_course_subject                 ON course(subject_id);
CREATE INDEX idx_course_level                   ON course(level_id);
CREATE INDEX idx_course_status                  ON course(status);
CREATE INDEX idx_course_phase_course            ON course_phase(course_id);
CREATE INDEX idx_course_session_phase           ON course_session(phase_id);
CREATE INDEX idx_course_material_course         ON course_material(course_id);
CREATE INDEX idx_course_material_phase          ON course_material(phase_id);
CREATE INDEX idx_course_material_session        ON course_material(course_session_id);
CREATE INDEX idx_plo_subject                    ON plo(subject_id);
CREATE INDEX idx_clo_course                     ON clo(course_id);
CREATE INDEX idx_course_assessment_course       ON course_assessment(course_id);
CREATE INDEX idx_course_assessment_clo_assess   ON course_assessment_clo_mapping(course_assessment_id);
CREATE INDEX idx_course_assessment_clo_clo      ON course_assessment_clo_mapping(clo_id);

-- Operations
CREATE INDEX idx_class_branch           ON "class"(branch_id);
CREATE INDEX idx_class_course           ON "class"(course_id);
CREATE INDEX idx_class_status           ON "class"(status);
CREATE INDEX idx_class_start_date       ON "class"(start_date);
CREATE INDEX idx_session_class          ON session(class_id);
CREATE INDEX idx_session_course_session ON session(course_session_id);
CREATE INDEX idx_session_time_slot_tpl  ON session(time_slot_template_id);
CREATE INDEX idx_session_date           ON session(date);
CREATE INDEX idx_session_status         ON session(status);
CREATE INDEX idx_session_type           ON session(type);
CREATE INDEX idx_session_resource_sess  ON session_resource(session_id);
CREATE INDEX idx_session_resource_res   ON session_resource(resource_id);
CREATE INDEX idx_teaching_slot_session  ON teaching_slot(session_id);
CREATE INDEX idx_teaching_slot_teacher  ON teaching_slot(teacher_id);
CREATE INDEX idx_teaching_slot_status   ON teaching_slot(status);
CREATE INDEX idx_enrollment_class       ON enrollment(class_id);
CREATE INDEX idx_enrollment_student     ON enrollment(student_id);
CREATE INDEX idx_enrollment_status      ON enrollment(status);
CREATE INDEX idx_student_session_student ON student_session(student_id);
CREATE INDEX idx_student_session_session ON student_session(session_id);
CREATE INDEX idx_student_session_att    ON student_session(attendance_status);
CREATE INDEX idx_teacher_avail_teacher  ON teacher_availability(teacher_id);
CREATE INDEX idx_teacher_avail_slot     ON teacher_availability(time_slot_template_id);
CREATE INDEX idx_teacher_avail_day      ON teacher_availability(day_of_week);

-- Assessment & Feedback
CREATE INDEX idx_assessment_class               ON assessment(class_id);
CREATE INDEX idx_assessment_course_assessment   ON assessment(course_assessment_id);
CREATE INDEX idx_score_assessment               ON score(assessment_id);
CREATE INDEX idx_score_student                  ON score(student_id);
CREATE INDEX idx_student_feedback_student       ON student_feedback(student_id);
CREATE INDEX idx_student_feedback_session       ON student_feedback(session_id);
CREATE INDEX idx_qa_report_class                ON qa_report(class_id);
CREATE INDEX idx_qa_report_session              ON qa_report(session_id);

-- Requests
CREATE INDEX idx_student_request_student        ON student_request(student_id);
CREATE INDEX idx_student_request_status         ON student_request(status);
CREATE INDEX idx_student_request_type           ON student_request(request_type);
CREATE INDEX idx_teacher_request_teacher        ON teacher_request(teacher_id);
CREATE INDEX idx_teacher_request_status         ON teacher_request(status);
CREATE INDEX idx_teacher_request_type           ON teacher_request(request_type);
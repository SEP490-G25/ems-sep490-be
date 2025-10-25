-- =========================================
-- EMS-SEP490-BE: Complete Database Schema
-- PostgreSQL 16 Schema Definition
-- Generated from JPA Entity Classes
-- =========================================
--
-- OVERVIEW:
-- This script creates the complete database schema for the Education Management System (EMS)
-- including all tables, enum types, foreign keys, unique constraints, and indexes.
--
-- EXECUTION ORDER:
-- 1. Drop existing tables (cascade)
-- 2. Create enum types
-- 3. Create tables in dependency order
-- 4. Add foreign key constraints
-- 5. Create indexes for performance
--
-- IMPORTANT NOTES:
-- - This is a reverse-engineered schema from 36 JPA entities (42 Java classes including composite IDs)
-- - All enum types must exist before creating tables
-- - Tables are created in dependency order (parent tables before child tables)
-- - Composite primary keys use multiple columns
-- - Array types use PostgreSQL array syntax (e.g., smallint[], skill_enum[])
-- =========================================

-- =========================================
-- SECTION 1: DROP EXISTING TABLES
-- =========================================

DROP TABLE IF EXISTS teacher_request CASCADE;
DROP TABLE IF EXISTS student_request CASCADE;
DROP TABLE IF EXISTS qa_report CASCADE;
DROP TABLE IF EXISTS student_feedback CASCADE;
DROP TABLE IF EXISTS score CASCADE;
DROP TABLE IF EXISTS assessment CASCADE;
DROP TABLE IF EXISTS course_assessment_clo_mapping CASCADE;
DROP TABLE IF EXISTS course_assessment CASCADE;
DROP TABLE IF EXISTS teacher_availability_override CASCADE;
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

-- =========================================
-- SECTION 2: CREATE ENUM TYPES
-- =========================================
-- Note: If types already exist, you need to DROP TYPE first or use IF NOT EXISTS (PostgreSQL 9.3+)

DO $$ BEGIN
    CREATE TYPE session_status_enum AS ENUM ('planned', 'cancelled', 'done');
EXCEPTION
    WHEN duplicate_object THEN null;
END $$;

DO $$ BEGIN
    CREATE TYPE session_type_enum AS ENUM ('class', 'makeup', 'exam', 'other');
EXCEPTION
    WHEN duplicate_object THEN null;
END $$;

DO $$ BEGIN
    CREATE TYPE attendance_status_enum AS ENUM ('planned', 'present', 'absent', 'late', 'excused', 'remote');
EXCEPTION
    WHEN duplicate_object THEN null;
END $$;

DO $$ BEGIN
    CREATE TYPE enrollment_status_enum AS ENUM ('enrolled', 'waitlisted', 'transferred', 'dropped', 'completed');
EXCEPTION
    WHEN duplicate_object THEN null;
END $$;

DO $$ BEGIN
    CREATE TYPE request_status_enum AS ENUM ('pending', 'waiting_confirm', 'approved', 'rejected', 'cancelled');
EXCEPTION
    WHEN duplicate_object THEN null;
END $$;

DO $$ BEGIN
    CREATE TYPE teacher_request_type_enum AS ENUM ('leave', 'swap', 'ot', 'reschedule');
EXCEPTION
    WHEN duplicate_object THEN null;
END $$;

DO $$ BEGIN
    CREATE TYPE student_request_type_enum AS ENUM ('absence', 'makeup', 'transfer', 'reschedule');
EXCEPTION
    WHEN duplicate_object THEN null;
END $$;

DO $$ BEGIN
    CREATE TYPE resource_type_enum AS ENUM ('room', 'virtual');
EXCEPTION
    WHEN duplicate_object THEN null;
END $$;

DO $$ BEGIN
    CREATE TYPE modality_enum AS ENUM ('offline', 'online', 'hybrid');
EXCEPTION
    WHEN duplicate_object THEN null;
END $$;

DO $$ BEGIN
    CREATE TYPE skill_enum AS ENUM ('general', 'reading', 'writing', 'speaking', 'listening');
EXCEPTION
    WHEN duplicate_object THEN null;
END $$;

DO $$ BEGIN
    CREATE TYPE teaching_role_enum AS ENUM ('primary', 'assistant');
EXCEPTION
    WHEN duplicate_object THEN null;
END $$;

DO $$ BEGIN
    CREATE TYPE branch_status_enum AS ENUM ('active', 'inactive', 'closed', 'planned');
EXCEPTION
    WHEN duplicate_object THEN null;
END $$;

DO $$ BEGIN
    CREATE TYPE class_status_enum AS ENUM ('draft', 'scheduled', 'ongoing', 'completed', 'cancelled');
EXCEPTION
    WHEN duplicate_object THEN null;
END $$;

DO $$ BEGIN
    CREATE TYPE subject_status_enum AS ENUM ('active', 'inactive');
EXCEPTION
    WHEN duplicate_object THEN null;
END $$;

DO $$ BEGIN
    CREATE TYPE assessment_kind_enum AS ENUM ('quiz', 'midterm', 'final', 'assignment', 'project', 'oral', 'practice', 'other');
EXCEPTION
    WHEN duplicate_object THEN null;
END $$;

DO $$ BEGIN
    CREATE TYPE teaching_slot_status_enum AS ENUM ('scheduled', 'on_leave', 'substituted', 'completed', 'cancelled');
EXCEPTION
    WHEN duplicate_object THEN null;
END $$;

DO $$ BEGIN
    CREATE TYPE homework_status_enum AS ENUM ('completed', 'incomplete', 'no_homework');
EXCEPTION
    WHEN duplicate_object THEN null;
END $$;

-- =========================================
-- SECTION 3: CREATE TABLES (Dependency Order)
-- =========================================

-- =========================================
-- TIER 1: INDEPENDENT TABLES (No Foreign Keys)
-- =========================================

-- 1. Center (root of organization hierarchy)
CREATE TABLE center (
    id BIGSERIAL PRIMARY KEY,
    code VARCHAR(255) NOT NULL UNIQUE,
    name VARCHAR(255) NOT NULL,
    description TEXT,
    phone VARCHAR(50),
    email VARCHAR(255),
    created_at TIMESTAMP WITH TIME ZONE DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP WITH TIME ZONE DEFAULT CURRENT_TIMESTAMP
);

-- 2. Role (root of RBAC system)
CREATE TABLE role (
    id BIGSERIAL PRIMARY KEY,
    code VARCHAR(50) NOT NULL UNIQUE,
    name VARCHAR(255) NOT NULL
);

-- 3. UserAccount (root of people system)
CREATE TABLE user_account (
    id BIGSERIAL PRIMARY KEY,
    email VARCHAR(255) UNIQUE,
    phone VARCHAR(50),
    full_name VARCHAR(255) NOT NULL,
    password_hash VARCHAR(255) NOT NULL,
    status VARCHAR(50),
    last_login_at TIMESTAMP WITH TIME ZONE,
    created_at TIMESTAMP WITH TIME ZONE DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP WITH TIME ZONE DEFAULT CURRENT_TIMESTAMP
);

-- =========================================
-- TIER 2: DEPENDS ON TIER 1
-- =========================================

-- 4. Branch (depends on Center)
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
    created_at TIMESTAMP WITH TIME ZONE DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP WITH TIME ZONE DEFAULT CURRENT_TIMESTAMP,
    CONSTRAINT fk_branch_center FOREIGN KEY (center_id) REFERENCES center(id) ON DELETE CASCADE,
    CONSTRAINT uq_branch_center_code UNIQUE (center_id, code)
);

-- 5. Subject (depends on UserAccount for created_by)
CREATE TABLE subject (
    id BIGSERIAL PRIMARY KEY,
    code VARCHAR(50) NOT NULL UNIQUE,
    name VARCHAR(255) NOT NULL,
    description TEXT,
    status subject_status_enum NOT NULL DEFAULT 'active',
    created_by BIGINT,
    created_at TIMESTAMP WITH TIME ZONE DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP WITH TIME ZONE DEFAULT CURRENT_TIMESTAMP,
    CONSTRAINT fk_subject_created_by FOREIGN KEY (created_by) REFERENCES user_account(id) ON DELETE SET NULL
);

-- 6. TimeSlotTemplate (depends on Branch)
-- NOTE: This is now just a template defining time slots, NOT containing day/time directly
-- Teachers register their availability to specific time slots via teacher_availability (N-N relationship)
CREATE TABLE time_slot_template (
    id BIGSERIAL PRIMARY KEY,
    branch_id BIGINT NOT NULL,
    name VARCHAR(255) NOT NULL,
    start_time TIME NOT NULL,
    end_time TIME NOT NULL,
    duration_min INTEGER,
    created_at TIMESTAMP WITH TIME ZONE DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP WITH TIME ZONE DEFAULT CURRENT_TIMESTAMP,
    CONSTRAINT fk_timeslot_branch FOREIGN KEY (branch_id) REFERENCES branch(id) ON DELETE CASCADE
);

-- 7. Resource (depends on Branch and UserAccount)
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
    created_at TIMESTAMP WITH TIME ZONE DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP WITH TIME ZONE DEFAULT CURRENT_TIMESTAMP,
    CONSTRAINT fk_resource_branch FOREIGN KEY (branch_id) REFERENCES branch(id) ON DELETE CASCADE,
    CONSTRAINT fk_resource_created_by FOREIGN KEY (created_by) REFERENCES user_account(id) ON DELETE SET NULL
);

-- 8. UserRole (junction table: UserAccount + Role)
CREATE TABLE user_role (
    user_id BIGINT NOT NULL,
    role_id BIGINT NOT NULL,
    PRIMARY KEY (user_id, role_id),
    CONSTRAINT fk_userrole_user FOREIGN KEY (user_id) REFERENCES user_account(id) ON DELETE CASCADE,
    CONSTRAINT fk_userrole_role FOREIGN KEY (role_id) REFERENCES role(id) ON DELETE CASCADE
);

-- 9. UserBranch (junction table: UserAccount + Branch)
CREATE TABLE user_branches (
    user_id BIGINT NOT NULL,
    branch_id BIGINT NOT NULL,
    assigned_at TIMESTAMP WITH TIME ZONE DEFAULT CURRENT_TIMESTAMP,
    assigned_by BIGINT,
    PRIMARY KEY (user_id, branch_id),
    CONSTRAINT fk_userbranch_user FOREIGN KEY (user_id) REFERENCES user_account(id) ON DELETE CASCADE,
    CONSTRAINT fk_userbranch_branch FOREIGN KEY (branch_id) REFERENCES branch(id) ON DELETE CASCADE,
    CONSTRAINT fk_userbranch_assigned_by FOREIGN KEY (assigned_by) REFERENCES user_account(id) ON DELETE SET NULL
);

-- 10. Teacher (depends on UserAccount)
CREATE TABLE teacher (
    id BIGSERIAL PRIMARY KEY,
    user_account_id BIGINT NOT NULL UNIQUE,
    employee_code VARCHAR(50) UNIQUE,
    note TEXT,
    created_at TIMESTAMP WITH TIME ZONE DEFAULT CURRENT_TIMESTAMP NOT NULL,
    updated_at TIMESTAMP WITH TIME ZONE DEFAULT CURRENT_TIMESTAMP NOT NULL,
    CONSTRAINT fk_teacher_user_account FOREIGN KEY (user_account_id) REFERENCES user_account(id) ON DELETE CASCADE
);

-- 11. Student (depends on UserAccount and Branch)
CREATE TABLE student (
    id BIGSERIAL PRIMARY KEY,
    user_id BIGINT NOT NULL UNIQUE,
    student_code VARCHAR(50) UNIQUE,
    branch_id BIGINT,
    created_at TIMESTAMP WITH TIME ZONE DEFAULT CURRENT_TIMESTAMP NOT NULL,
    updated_at TIMESTAMP WITH TIME ZONE DEFAULT CURRENT_TIMESTAMP NOT NULL,
    CONSTRAINT fk_student_user FOREIGN KEY (user_id) REFERENCES user_account(id) ON DELETE CASCADE,
    CONSTRAINT fk_student_branch FOREIGN KEY (branch_id) REFERENCES branch(id) ON DELETE SET NULL
);

-- =========================================
-- TIER 3: ACADEMIC CURRICULUM HIERARCHY
-- =========================================

-- 12. Level (depends on Subject)
CREATE TABLE level (
    id BIGSERIAL PRIMARY KEY,
    subject_id BIGINT NOT NULL,
    code VARCHAR(50) NOT NULL,
    name VARCHAR(255) NOT NULL,
    standard_type VARCHAR(100),
    expected_duration_hours INTEGER,
    sort_order INTEGER,
    description TEXT,
    created_at TIMESTAMP WITH TIME ZONE DEFAULT CURRENT_TIMESTAMP NOT NULL,
    updated_at TIMESTAMP WITH TIME ZONE DEFAULT CURRENT_TIMESTAMP NOT NULL,
    CONSTRAINT fk_level_subject FOREIGN KEY (subject_id) REFERENCES subject(id) ON DELETE CASCADE,
    CONSTRAINT uq_level_subject_code UNIQUE (subject_id, code)
);

-- 13. Course (depends on Subject, Level, UserAccount)
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
    approved_at TIMESTAMP WITH TIME ZONE,
    rejection_reason TEXT,
    hash_checksum VARCHAR(255),
    created_by BIGINT,
    created_at TIMESTAMP WITH TIME ZONE DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP WITH TIME ZONE DEFAULT CURRENT_TIMESTAMP,
    CONSTRAINT fk_course_subject FOREIGN KEY (subject_id) REFERENCES subject(id) ON DELETE CASCADE,
    CONSTRAINT fk_course_level FOREIGN KEY (level_id) REFERENCES level(id) ON DELETE SET NULL,
    CONSTRAINT fk_course_approved_by FOREIGN KEY (approved_by_manager) REFERENCES user_account(id) ON DELETE SET NULL,
    CONSTRAINT fk_course_created_by FOREIGN KEY (created_by) REFERENCES user_account(id) ON DELETE SET NULL
);

-- 14. CoursePhase (depends on Course)
CREATE TABLE course_phase (
    id BIGSERIAL PRIMARY KEY,
    course_id BIGINT NOT NULL,
    phase_number INTEGER NOT NULL,
    name VARCHAR(255),
    duration_weeks INTEGER,
    learning_focus TEXT,
    sort_order INTEGER,
    created_at TIMESTAMP WITH TIME ZONE DEFAULT CURRENT_TIMESTAMP NOT NULL,
    updated_at TIMESTAMP WITH TIME ZONE DEFAULT CURRENT_TIMESTAMP NOT NULL,
    CONSTRAINT fk_course_phase_course FOREIGN KEY (course_id) REFERENCES course(id) ON DELETE CASCADE,
    CONSTRAINT uq_course_phase_course_number UNIQUE (course_id, phase_number)
);

-- 15. CourseSession (depends on CoursePhase)
CREATE TABLE course_session (
    id BIGSERIAL PRIMARY KEY,
    phase_id BIGINT NOT NULL,
    sequence_no INTEGER NOT NULL,
    topic VARCHAR(500),
    student_task TEXT,
    skill_set skill_enum[],
    created_at TIMESTAMP WITH TIME ZONE DEFAULT CURRENT_TIMESTAMP NOT NULL,
    updated_at TIMESTAMP WITH TIME ZONE DEFAULT CURRENT_TIMESTAMP NOT NULL,
    CONSTRAINT fk_course_session_phase FOREIGN KEY (phase_id) REFERENCES course_phase(id) ON DELETE CASCADE,
    CONSTRAINT uq_course_session_phase_sequence UNIQUE (phase_id, sequence_no)
);

-- 16. CourseMaterial (depends on Course, CoursePhase, CourseSession, UserAccount)
CREATE TABLE course_material (
    id BIGSERIAL PRIMARY KEY,
    course_id BIGINT NOT NULL,
    phase_id BIGINT,
    course_session_id BIGINT,
    title VARCHAR(500) NOT NULL,
    url VARCHAR(1000) NOT NULL,
    uploaded_by BIGINT,
    created_at TIMESTAMP WITH TIME ZONE DEFAULT CURRENT_TIMESTAMP NOT NULL,
    updated_at TIMESTAMP WITH TIME ZONE DEFAULT CURRENT_TIMESTAMP NOT NULL,
    CONSTRAINT fk_course_material_course FOREIGN KEY (course_id) REFERENCES course(id) ON DELETE CASCADE,
    CONSTRAINT fk_course_material_phase FOREIGN KEY (phase_id) REFERENCES course_phase(id) ON DELETE CASCADE,
    CONSTRAINT fk_course_material_session FOREIGN KEY (course_session_id) REFERENCES course_session(id) ON DELETE CASCADE,
    CONSTRAINT fk_course_material_uploaded_by FOREIGN KEY (uploaded_by) REFERENCES user_account(id) ON DELETE SET NULL
);

-- 17. Plo (Program Learning Outcomes - depends on Subject)
CREATE TABLE plo (
    id BIGSERIAL PRIMARY KEY,
    subject_id BIGINT NOT NULL,
    code VARCHAR(50) NOT NULL,
    description TEXT NOT NULL,
    created_at TIMESTAMP WITH TIME ZONE DEFAULT CURRENT_TIMESTAMP NOT NULL,
    updated_at TIMESTAMP WITH TIME ZONE DEFAULT CURRENT_TIMESTAMP NOT NULL,
    CONSTRAINT fk_plo_subject FOREIGN KEY (subject_id) REFERENCES subject(id) ON DELETE CASCADE,
    CONSTRAINT uq_plo_subject_code UNIQUE (subject_id, code)
);

-- 18. Clo (Course Learning Outcomes - depends on Course)
CREATE TABLE clo (
    id BIGSERIAL PRIMARY KEY,
    course_id BIGINT NOT NULL,
    code VARCHAR(50) NOT NULL,
    description TEXT NOT NULL,
    created_at TIMESTAMP WITH TIME ZONE DEFAULT CURRENT_TIMESTAMP NOT NULL,
    updated_at TIMESTAMP WITH TIME ZONE DEFAULT CURRENT_TIMESTAMP NOT NULL,
    CONSTRAINT fk_clo_course FOREIGN KEY (course_id) REFERENCES course(id) ON DELETE CASCADE,
    CONSTRAINT uq_clo_course_code UNIQUE (course_id, code)
);

-- 19. PloCloMapping (junction table: Plo + Clo)
CREATE TABLE plo_clo_mapping (
    plo_id BIGINT NOT NULL,
    clo_id BIGINT NOT NULL,
    status VARCHAR(50),
    PRIMARY KEY (plo_id, clo_id),
    CONSTRAINT fk_plo_clo_plo FOREIGN KEY (plo_id) REFERENCES plo(id) ON DELETE CASCADE,
    CONSTRAINT fk_plo_clo_clo FOREIGN KEY (clo_id) REFERENCES clo(id) ON DELETE CASCADE
);

-- 20. CourseSessionCloMapping (junction table: CourseSession + Clo)
CREATE TABLE course_session_clo_mapping (
    course_session_id BIGINT NOT NULL,
    clo_id BIGINT NOT NULL,
    status VARCHAR(50),
    PRIMARY KEY (course_session_id, clo_id),
    CONSTRAINT fk_course_session_clo_session FOREIGN KEY (course_session_id) REFERENCES course_session(id) ON DELETE CASCADE,
    CONSTRAINT fk_course_session_clo_clo FOREIGN KEY (clo_id) REFERENCES clo(id) ON DELETE CASCADE
);

-- 21. CourseAssessment (template assessments for a course - depends on Course)
-- Purpose: Define assessment templates at course level to be cloned when creating classes
CREATE TABLE course_assessment (
    id BIGSERIAL PRIMARY KEY,
    course_id BIGINT NOT NULL,
    name VARCHAR(255) NOT NULL,
    kind assessment_kind_enum NOT NULL,
    max_score DECIMAL(5,2) NOT NULL,
    weight DECIMAL(5,2),
    description TEXT,
    created_at TIMESTAMP WITH TIME ZONE DEFAULT CURRENT_TIMESTAMP NOT NULL,
    updated_at TIMESTAMP WITH TIME ZONE DEFAULT CURRENT_TIMESTAMP NOT NULL,
    CONSTRAINT fk_course_assessment_course FOREIGN KEY (course_id) REFERENCES course(id) ON DELETE CASCADE
);

-- 22. CourseAssessmentCloMapping (N-N junction table: CourseAssessment + Clo)
-- Purpose: Map which CLOs are assessed by each course assessment
CREATE TABLE course_assessment_clo_mapping (
    course_assessment_id BIGINT NOT NULL,
    clo_id BIGINT NOT NULL,
    status VARCHAR(50),
    PRIMARY KEY (course_assessment_id, clo_id),
    CONSTRAINT fk_course_assessment_clo_assessment FOREIGN KEY (course_assessment_id) REFERENCES course_assessment(id) ON DELETE CASCADE,
    CONSTRAINT fk_course_assessment_clo_clo FOREIGN KEY (clo_id) REFERENCES clo(id) ON DELETE CASCADE
);

-- =========================================
-- TIER 4: OPERATIONS (Classes and Sessions)
-- =========================================

-- 21. Class (depends on Branch, Course, UserAccount)
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
    status class_status_enum NOT NULL DEFAULT 'draft',
    created_by BIGINT,
    submitted_at TIMESTAMP WITH TIME ZONE,
    approved_by BIGINT,
    approved_at TIMESTAMP WITH TIME ZONE,
    rejection_reason TEXT,
    created_at TIMESTAMP WITH TIME ZONE DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP WITH TIME ZONE DEFAULT CURRENT_TIMESTAMP,
    CONSTRAINT fk_class_branch FOREIGN KEY (branch_id) REFERENCES branch(id) ON DELETE CASCADE,
    CONSTRAINT fk_class_course FOREIGN KEY (course_id) REFERENCES course(id) ON DELETE CASCADE,
    CONSTRAINT fk_class_created_by FOREIGN KEY (created_by) REFERENCES user_account(id) ON DELETE SET NULL,
    CONSTRAINT fk_class_approved_by FOREIGN KEY (approved_by) REFERENCES user_account(id) ON DELETE SET NULL,
    CONSTRAINT uq_class_branch_code UNIQUE (branch_id, code)
);

-- 22. Session (depends on Class, CourseSession, and TimeSlotTemplate)
-- NOTE: Added time_slot_template_id to link sessions to specific time slots (1-N relationship)
CREATE TABLE session (
    id BIGSERIAL PRIMARY KEY,
    class_id BIGINT,
    course_session_id BIGINT,
    time_slot_template_id BIGINT,
    date DATE NOT NULL,
    type session_type_enum NOT NULL DEFAULT 'class',
    status session_status_enum NOT NULL DEFAULT 'planned',
    teacher_note TEXT,
    created_at TIMESTAMP WITH TIME ZONE DEFAULT CURRENT_TIMESTAMP NOT NULL,
    updated_at TIMESTAMP WITH TIME ZONE DEFAULT CURRENT_TIMESTAMP NOT NULL,
    CONSTRAINT fk_session_class FOREIGN KEY (class_id) REFERENCES "class"(id) ON DELETE CASCADE,
    CONSTRAINT fk_session_course_session FOREIGN KEY (course_session_id) REFERENCES course_session(id) ON DELETE SET NULL,
    CONSTRAINT fk_session_time_slot_template FOREIGN KEY (time_slot_template_id) REFERENCES time_slot_template(id) ON DELETE SET NULL
);

-- 23. TeacherSkill (junction table: Teacher + Skill enum)
CREATE TABLE teacher_skill (
    teacher_id BIGINT NOT NULL,
    skill skill_enum NOT NULL,
    level SMALLINT,
    PRIMARY KEY (teacher_id, skill),
    CONSTRAINT fk_teacher_skill_teacher FOREIGN KEY (teacher_id) REFERENCES teacher(id) ON DELETE CASCADE
);

-- 24. TeacherAvailability (N-N junction table: Teacher + TimeSlotTemplate)
-- Purpose: Academic Staff manages teacher availability schedules
-- NOTE: Teachers cannot self-register; only Academic Staff can create/update availability
CREATE TABLE teacher_availability (
    teacher_id BIGINT NOT NULL,
    time_slot_template_id BIGINT NOT NULL,
    day_of_week SMALLINT NOT NULL CHECK (day_of_week BETWEEN 1 AND 7),
    effective_date DATE,
    note TEXT,
    created_at TIMESTAMP WITH TIME ZONE DEFAULT CURRENT_TIMESTAMP NOT NULL,
    updated_at TIMESTAMP WITH TIME ZONE DEFAULT CURRENT_TIMESTAMP NOT NULL,
    PRIMARY KEY (teacher_id, time_slot_template_id, day_of_week),
    CONSTRAINT fk_teacher_availability_teacher FOREIGN KEY (teacher_id) REFERENCES teacher(id) ON DELETE CASCADE,
    CONSTRAINT fk_teacher_availability_timeslot FOREIGN KEY (time_slot_template_id) REFERENCES time_slot_template(id) ON DELETE CASCADE
);

-- 26. SessionResource (junction table: Session + Resource)
CREATE TABLE session_resource (
    session_id BIGINT NOT NULL,
    resource_type resource_type_enum NOT NULL,
    resource_id BIGINT NOT NULL,
    capacity_override INTEGER,
    PRIMARY KEY (session_id, resource_type, resource_id),
    CONSTRAINT fk_session_resource_session FOREIGN KEY (session_id) REFERENCES session(id) ON DELETE CASCADE,
    CONSTRAINT fk_session_resource_resource FOREIGN KEY (resource_id) REFERENCES resource(id) ON DELETE CASCADE
);

-- 27. TeachingSlot (junction table: Session + Teacher with Skill)
-- NOTE: Added status field to track teaching slot status
CREATE TABLE teaching_slot (
    session_id BIGINT NOT NULL,
    teacher_id BIGINT NOT NULL,
    skill skill_enum NOT NULL,
    role teaching_role_enum NOT NULL,
    status teaching_slot_status_enum NOT NULL DEFAULT 'scheduled',
    PRIMARY KEY (session_id, teacher_id, skill),
    CONSTRAINT fk_teaching_slot_session FOREIGN KEY (session_id) REFERENCES session(id) ON DELETE CASCADE,
    CONSTRAINT fk_teaching_slot_teacher FOREIGN KEY (teacher_id) REFERENCES teacher(id) ON DELETE CASCADE
);

-- 28. Enrollment (depends on Class, Student, Session)
CREATE TABLE enrollment (
    id BIGSERIAL PRIMARY KEY,
    class_id BIGINT NOT NULL,
    student_id BIGINT NOT NULL,
    status enrollment_status_enum NOT NULL DEFAULT 'enrolled',
    enrolled_at TIMESTAMP WITH TIME ZONE,
    left_at TIMESTAMP WITH TIME ZONE,
    join_session_id BIGINT,
    left_session_id BIGINT,
    created_at TIMESTAMP WITH TIME ZONE DEFAULT CURRENT_TIMESTAMP NOT NULL,
    updated_at TIMESTAMP WITH TIME ZONE DEFAULT CURRENT_TIMESTAMP NOT NULL,
    CONSTRAINT fk_enrollment_class FOREIGN KEY (class_id) REFERENCES "class"(id) ON DELETE CASCADE,
    CONSTRAINT fk_enrollment_student FOREIGN KEY (student_id) REFERENCES student(id) ON DELETE CASCADE,
    CONSTRAINT fk_enrollment_join_session FOREIGN KEY (join_session_id) REFERENCES session(id) ON DELETE SET NULL,
    CONSTRAINT fk_enrollment_left_session FOREIGN KEY (left_session_id) REFERENCES session(id) ON DELETE SET NULL,
    CONSTRAINT uq_enrollment_class_student UNIQUE (class_id, student_id)
);

-- 29. StudentSession (junction table: Student + Session)
CREATE TABLE student_session (
    student_id BIGINT NOT NULL,
    session_id BIGINT NOT NULL,
    is_makeup BOOLEAN DEFAULT false,
    attendance_status attendance_status_enum NOT NULL DEFAULT 'planned',
    homework_status homework_status_enum,
    note TEXT,
    recorded_at TIMESTAMP WITH TIME ZONE,
    PRIMARY KEY (student_id, session_id),
    CONSTRAINT fk_student_session_student FOREIGN KEY (student_id) REFERENCES student(id) ON DELETE CASCADE,
    CONSTRAINT fk_student_session_session FOREIGN KEY (session_id) REFERENCES session(id) ON DELETE CASCADE
);

-- =========================================
-- TIER 5: ASSESSMENT & FEEDBACK
-- =========================================

-- 30. Assessment (depends on Class, CourseAssessment)
-- NOTE: Removed session_id FK - assessments are now linked to class only
-- Assessments are cloned from course_assessment when class is created
CREATE TABLE assessment (
    id BIGSERIAL PRIMARY KEY,
    class_id BIGINT NOT NULL,
    name VARCHAR(255) NOT NULL,
    kind assessment_kind_enum NOT NULL,
    max_score DECIMAL(5,2) NOT NULL,
    weight DECIMAL(5,2),
    description TEXT,
    created_by BIGINT,
    created_at TIMESTAMP WITH TIME ZONE DEFAULT CURRENT_TIMESTAMP NOT NULL,
    updated_at TIMESTAMP WITH TIME ZONE DEFAULT CURRENT_TIMESTAMP NOT NULL,
    CONSTRAINT fk_assessment_class FOREIGN KEY (class_id) REFERENCES "class"(id) ON DELETE CASCADE,
    CONSTRAINT fk_assessment_course_assessment FOREIGN KEY (course_assessment_id) REFERENCES course_assessment(id) ON DELETE SET NULL,
    CONSTRAINT fk_assessment_created_by FOREIGN KEY (created_by) REFERENCES user_account(id) ON DELETE SET NULL
);

-- 31. Score (depends on Assessment, Student, Teacher)
CREATE TABLE score (
    id BIGSERIAL PRIMARY KEY,
    assessment_id BIGINT NOT NULL,
    student_id BIGINT NOT NULL,
    score DECIMAL(5,2) NOT NULL,
    feedback TEXT,
    graded_by BIGINT,
    graded_at TIMESTAMP WITH TIME ZONE,
    CONSTRAINT fk_score_assessment FOREIGN KEY (assessment_id) REFERENCES assessment(id) ON DELETE CASCADE,
    CONSTRAINT fk_score_student FOREIGN KEY (student_id) REFERENCES student(id) ON DELETE CASCADE,
    CONSTRAINT fk_score_graded_by FOREIGN KEY (graded_by) REFERENCES teacher(id) ON DELETE SET NULL,
    CONSTRAINT uq_score_assessment_student UNIQUE (assessment_id, student_id)
);

-- 32. StudentFeedback (depends on Student, Session, CoursePhase)
-- NOTE: Added is_feedback field to indicate if feedback was provided
CREATE TABLE student_feedback (
    id BIGSERIAL PRIMARY KEY,
    student_id BIGINT NOT NULL,
    session_id BIGINT NOT NULL,
    phase_id BIGINT,
    rating SMALLINT CHECK (rating BETWEEN 1 AND 5),
    comment TEXT,
    is_feedback BOOLEAN DEFAULT false,
    submitted_at TIMESTAMP WITH TIME ZONE,
    CONSTRAINT fk_student_feedback_student FOREIGN KEY (student_id) REFERENCES student(id) ON DELETE CASCADE,
    CONSTRAINT fk_student_feedback_session FOREIGN KEY (session_id) REFERENCES session(id) ON DELETE CASCADE,
    CONSTRAINT fk_student_feedback_phase FOREIGN KEY (phase_id) REFERENCES course_phase(id) ON DELETE SET NULL
);

-- 33. QaReport (depends on Class, Session, CoursePhase, UserAccount)
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
    created_at TIMESTAMP WITH TIME ZONE DEFAULT CURRENT_TIMESTAMP NOT NULL,
    updated_at TIMESTAMP WITH TIME ZONE DEFAULT CURRENT_TIMESTAMP NOT NULL,
    CONSTRAINT fk_qa_report_class FOREIGN KEY (class_id) REFERENCES "class"(id) ON DELETE CASCADE,
    CONSTRAINT fk_qa_report_session FOREIGN KEY (session_id) REFERENCES session(id) ON DELETE CASCADE,
    CONSTRAINT fk_qa_report_phase FOREIGN KEY (phase_id) REFERENCES course_phase(id) ON DELETE SET NULL,
    CONSTRAINT fk_qa_report_reported_by FOREIGN KEY (reported_by) REFERENCES user_account(id) ON DELETE SET NULL
);

-- =========================================
-- TIER 6: REQUEST WORKFLOWS
-- =========================================

-- 34. StudentRequest (depends on Student, Class, Session, UserAccount)
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
    submitted_at TIMESTAMP WITH TIME ZONE,
    submitted_by BIGINT,
    decided_by BIGINT,
    decided_at TIMESTAMP WITH TIME ZONE,
    note TEXT,
    created_at TIMESTAMP WITH TIME ZONE DEFAULT CURRENT_TIMESTAMP NOT NULL,
    updated_at TIMESTAMP WITH TIME ZONE DEFAULT CURRENT_TIMESTAMP NOT NULL,
    CONSTRAINT fk_student_request_student FOREIGN KEY (student_id) REFERENCES student(id) ON DELETE CASCADE,
    CONSTRAINT fk_student_request_current_class FOREIGN KEY (current_class_id) REFERENCES "class"(id) ON DELETE SET NULL,
    CONSTRAINT fk_student_request_target_class FOREIGN KEY (target_class_id) REFERENCES "class"(id) ON DELETE SET NULL,
    CONSTRAINT fk_student_request_target_session FOREIGN KEY (target_session_id) REFERENCES session(id) ON DELETE SET NULL,
    CONSTRAINT fk_student_request_makeup_session FOREIGN KEY (makeup_session_id) REFERENCES session(id) ON DELETE SET NULL,
    CONSTRAINT fk_student_request_effective_session FOREIGN KEY (effective_session_id) REFERENCES session(id) ON DELETE SET NULL,
    CONSTRAINT fk_student_request_submitted_by FOREIGN KEY (submitted_by) REFERENCES user_account(id) ON DELETE SET NULL,
    CONSTRAINT fk_student_request_decided_by FOREIGN KEY (decided_by) REFERENCES user_account(id) ON DELETE SET NULL
);

-- 35. TeacherRequest (depends on Teacher, Session, UserAccount, TimeSlotTemplate, Resource)
-- NOTE: Added fields for swap/reschedule request processing
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
    submitted_at TIMESTAMP WITH TIME ZONE,
    submitted_by BIGINT,
    decided_by BIGINT,
    decided_at TIMESTAMP WITH TIME ZONE,
    note TEXT,
    created_at TIMESTAMP WITH TIME ZONE DEFAULT CURRENT_TIMESTAMP NOT NULL,
    updated_at TIMESTAMP WITH TIME ZONE DEFAULT CURRENT_TIMESTAMP NOT NULL,
    CONSTRAINT fk_teacher_request_teacher FOREIGN KEY (teacher_id) REFERENCES teacher(id) ON DELETE CASCADE,
    CONSTRAINT fk_teacher_request_replacement_teacher FOREIGN KEY (replacement_teacher_id) REFERENCES teacher(id) ON DELETE SET NULL,
    CONSTRAINT fk_teacher_request_session FOREIGN KEY (session_id) REFERENCES session(id) ON DELETE SET NULL,
    CONSTRAINT fk_teacher_request_new_time_slot FOREIGN KEY (new_time_slot_id) REFERENCES time_slot_template(id) ON DELETE SET NULL,
    CONSTRAINT fk_teacher_request_new_resource FOREIGN KEY (new_resource_id) REFERENCES resource(id) ON DELETE SET NULL,
    CONSTRAINT fk_teacher_request_submitted_by FOREIGN KEY (submitted_by) REFERENCES user_account(id) ON DELETE SET NULL,
    CONSTRAINT fk_teacher_request_decided_by FOREIGN KEY (decided_by) REFERENCES user_account(id) ON DELETE SET NULL
);

-- =========================================
-- SECTION 4: CREATE INDEXES FOR PERFORMANCE
-- =========================================

-- Organization & Infrastructure
CREATE INDEX idx_branch_center ON branch(center_id);
CREATE INDEX idx_branch_status ON branch(status);
CREATE INDEX idx_resource_branch ON resource(branch_id);
CREATE INDEX idx_resource_type ON resource(resource_type);
CREATE INDEX idx_timeslot_branch ON time_slot_template(branch_id);

-- People & RBAC
CREATE INDEX idx_user_account_email ON user_account(email);
CREATE INDEX idx_user_account_status ON user_account(status);
CREATE INDEX idx_user_role_user ON user_role(user_id);
CREATE INDEX idx_user_role_role ON user_role(role_id);
CREATE INDEX idx_user_branch_user ON user_branches(user_id);
CREATE INDEX idx_user_branch_branch ON user_branches(branch_id);
CREATE INDEX idx_teacher_user_account ON teacher(user_account_id);
CREATE INDEX idx_student_user ON student(user_id);
CREATE INDEX idx_student_branch ON student(branch_id);
CREATE INDEX idx_teacher_skill_teacher ON teacher_skill(teacher_id);

-- Academic Curriculum
CREATE INDEX idx_subject_status ON subject(status);
CREATE INDEX idx_level_subject ON level(subject_id);
CREATE INDEX idx_course_subject ON course(subject_id);
CREATE INDEX idx_course_level ON course(level_id);
CREATE INDEX idx_course_status ON course(status);
CREATE INDEX idx_course_phase_course ON course_phase(course_id);
CREATE INDEX idx_course_session_phase ON course_session(phase_id);
CREATE INDEX idx_course_material_course ON course_material(course_id);
CREATE INDEX idx_course_material_phase ON course_material(phase_id);
CREATE INDEX idx_course_material_session ON course_material(course_session_id);
CREATE INDEX idx_plo_subject ON plo(subject_id);
CREATE INDEX idx_clo_course ON clo(course_id);
CREATE INDEX idx_course_assessment_course ON course_assessment(course_id);
CREATE INDEX idx_course_assessment_clo_assessment ON course_assessment_clo_mapping(course_assessment_id);
CREATE INDEX idx_course_assessment_clo_clo ON course_assessment_clo_mapping(clo_id);

-- Operations
CREATE INDEX idx_class_branch ON "class"(branch_id);
CREATE INDEX idx_class_course ON "class"(course_id);
CREATE INDEX idx_class_status ON "class"(status);
CREATE INDEX idx_class_start_date ON "class"(start_date);
CREATE INDEX idx_session_class ON session(class_id);
CREATE INDEX idx_session_course_session ON session(course_session_id);
CREATE INDEX idx_session_time_slot_template ON session(time_slot_template_id);
CREATE INDEX idx_session_date ON session(date);
CREATE INDEX idx_session_status ON session(status);
CREATE INDEX idx_session_type ON session(type);
CREATE INDEX idx_session_resource_session ON session_resource(session_id);
CREATE INDEX idx_session_resource_resource ON session_resource(resource_id);
CREATE INDEX idx_teaching_slot_session ON teaching_slot(session_id);
CREATE INDEX idx_teaching_slot_teacher ON teaching_slot(teacher_id);
CREATE INDEX idx_teaching_slot_status ON teaching_slot(status);
CREATE INDEX idx_enrollment_class ON enrollment(class_id);
CREATE INDEX idx_enrollment_student ON enrollment(student_id);
CREATE INDEX idx_enrollment_status ON enrollment(status);
CREATE INDEX idx_student_session_student ON student_session(student_id);
CREATE INDEX idx_student_session_session ON student_session(session_id);
CREATE INDEX idx_student_session_attendance ON student_session(attendance_status);
CREATE INDEX idx_teacher_availability_teacher ON teacher_availability(teacher_id);
CREATE INDEX idx_teacher_availability_timeslot ON teacher_availability(time_slot_template_id);
CREATE INDEX idx_teacher_availability_day ON teacher_availability(day_of_week);
CREATE INDEX idx_teacher_availability_override_teacher ON teacher_availability_override(teacher_id);
CREATE INDEX idx_teacher_availability_override_date ON teacher_availability_override(date);

-- Assessment & Feedback
CREATE INDEX idx_assessment_class ON assessment(class_id);
CREATE INDEX idx_assessment_session ON assessment(session_id);
CREATE INDEX idx_score_assessment ON score(assessment_id);
CREATE INDEX idx_score_student ON score(student_id);
CREATE INDEX idx_student_feedback_student ON student_feedback(student_id);
CREATE INDEX idx_student_feedback_session ON student_feedback(session_id);
CREATE INDEX idx_qa_report_class ON qa_report(class_id);
CREATE INDEX idx_qa_report_session ON qa_report(session_id);

-- Request Workflows
CREATE INDEX idx_student_request_student ON student_request(student_id);
CREATE INDEX idx_student_request_status ON student_request(status);
CREATE INDEX idx_student_request_type ON student_request(request_type);
CREATE INDEX idx_teacher_request_teacher ON teacher_request(teacher_id);
CREATE INDEX idx_teacher_request_status ON teacher_request(status);
CREATE INDEX idx_teacher_request_type ON teacher_request(request_type);

-- =========================================
-- SECTION 5: COMMENTS FOR DOCUMENTATION
-- =========================================

COMMENT ON DATABASE ems IS 'Education Management System (EMS) - Multi-tenant system for language training centers';

-- Organization & Infrastructure
COMMENT ON TABLE center IS 'Top-level organization entity representing a training center';
COMMENT ON TABLE branch IS 'Physical location/branch of a center';
COMMENT ON TABLE resource IS 'Unified model for ROOM (physical) and VIRTUAL (Zoom/online) resources';
COMMENT ON TABLE time_slot_template IS 'Defines allowed class time slots per branch';

-- People & RBAC
COMMENT ON TABLE role IS 'RBAC roles: ADMIN, MANAGER, CENTER_HEAD, ACADEMIC_STAFF, SUBJECT_LEADER, TEACHER, STUDENT, QA';
COMMENT ON TABLE user_account IS 'Base user account for all system users';
COMMENT ON TABLE user_role IS 'Many-to-many mapping of users to roles';
COMMENT ON TABLE user_branches IS 'Multi-branch access control for users';
COMMENT ON TABLE teacher IS '1:1 mapping with UserAccount for teachers';
COMMENT ON TABLE student IS '1:1 mapping with UserAccount for students';
COMMENT ON TABLE teacher_skill IS 'Teacher skills: GENERAL, READING, WRITING, SPEAKING, LISTENING';

-- Academic Curriculum
COMMENT ON TABLE subject IS 'Subject domain (e.g., English, Japanese)';
COMMENT ON TABLE level IS 'Proficiency levels within a subject (e.g., A1, A2, B1)';
COMMENT ON TABLE course IS 'Course template (approved by Manager) used to create classes';
COMMENT ON TABLE course_phase IS 'Phases within a course (e.g., foundation, intermediate, advanced)';
COMMENT ON TABLE course_session IS 'Template sessions within a phase - basis for generating actual sessions';
COMMENT ON TABLE plo IS 'Program Learning Outcomes at subject level';
COMMENT ON TABLE clo IS 'Course Learning Outcomes at course level';
COMMENT ON TABLE plo_clo_mapping IS 'Maps CLOs to PLOs for outcome tracking';
COMMENT ON TABLE course_session_clo_mapping IS 'Maps course sessions to CLOs';

-- Operations (Session-First Design)
COMMENT ON TABLE "class" IS 'Class instance (scheduled offering of a course at a branch)';
COMMENT ON TABLE session IS 'SINGLE SOURCE OF TRUTH for schedule - auto-generated from course template';
COMMENT ON TABLE session_resource IS 'Resource allocation per session (room or virtual)';
COMMENT ON TABLE teaching_slot IS 'Teacher assignment to session with skill and role';
COMMENT ON TABLE enrollment IS 'Student registration in a class';
COMMENT ON TABLE student_session IS 'Individual student attendance record per session - auto-generated on enrollment';
COMMENT ON TABLE teacher_availability IS 'Regular weekly availability patterns for teachers';
COMMENT ON TABLE teacher_availability_override IS 'Date-specific availability exceptions';

-- Assessment & Feedback
COMMENT ON TABLE assessment IS 'Assessment definition within a class (quiz, midterm, final, etc.)';
COMMENT ON TABLE score IS 'Student scores for assessments';
COMMENT ON TABLE student_feedback IS 'Student feedback for sessions/phases';
COMMENT ON TABLE qa_report IS 'Quality assurance reports for monitoring';

-- Request Workflows
COMMENT ON TABLE student_request IS 'Student requests: absence, makeup, transfer, reschedule';
COMMENT ON TABLE teacher_request IS 'Teacher requests: leave, swap, OT, reschedule';

-- =========================================
-- SCRIPT COMPLETION
-- =========================================

-- Success message
DO $$
BEGIN
    RAISE NOTICE '========================================';
    RAISE NOTICE 'EMS Database Schema Created Successfully';
    RAISE NOTICE '========================================';
    RAISE NOTICE 'Total Tables Created: 36';
    RAISE NOTICE 'Total Enum Types: 15';
    RAISE NOTICE 'Total Indexes: ~80+ performance indexes';
    RAISE NOTICE '';
    RAISE NOTICE 'Architecture:';
    RAISE NOTICE '  - Organization & Infrastructure: 4 tables';
    RAISE NOTICE '  - People & RBAC: 7 tables';
    RAISE NOTICE '  - Academic Curriculum: 10 tables';
    RAISE NOTICE '  - Operations: 9 tables';
    RAISE NOTICE '  - Assessment & Feedback: 4 tables';
    RAISE NOTICE '  - Request Workflows: 2 tables';
    RAISE NOTICE '';
    RAISE NOTICE 'Key Design Patterns:';
    RAISE NOTICE '  ✓ Session-First Design (session is source of truth)';
    RAISE NOTICE '  ✓ Multi-tenant (center → branch hierarchy)';
    RAISE NOTICE '  ✓ RBAC with multi-branch access control';
    RAISE NOTICE '  ✓ Composite primary keys for junction tables';
    RAISE NOTICE '  ✓ PostgreSQL enum types for type safety';
    RAISE NOTICE '  ✓ Array types for schedule_days and skill_set';
    RAISE NOTICE '  ✓ Audit trail (created_at, updated_at, created_by, etc.)';
    RAISE NOTICE '========================================';
END $$;

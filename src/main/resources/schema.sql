-- =========================================
-- PostgreSQL Enum Types for EMS
-- This script creates enum types before Hibernate creates tables
--
-- QUAN TRỌNG:
-- - File này CHỈ định nghĩa ENUM TYPES, KHÔNG tạo tables
-- - Hibernate sẽ TỰ ĐỘNG tạo/update tables dựa trên entities
-- - Khi thêm enum mới: Chỉ cần thêm dòng CREATE TYPE ở đây
--
-- Note: If types already exist, errors will be ignored due to continue-on-error setting
-- =========================================

CREATE TYPE session_status_enum AS ENUM ('planned','cancelled','done');
CREATE TYPE session_type_enum AS ENUM ('CLASS','MAKEUP','EXAM','OTHER');
CREATE TYPE attendance_status_enum AS ENUM ('planned','present','absent','late','excused','remote');
CREATE TYPE enrollment_status_enum AS ENUM ('enrolled','waitlisted','transferred','dropped','completed');
CREATE TYPE request_status_enum AS ENUM ('pending','approved','rejected','cancelled');
CREATE TYPE teacher_request_type_enum AS ENUM ('leave','swap','ot','reschedule');
CREATE TYPE student_request_type_enum AS ENUM ('absence','makeup','transfer','reschedule');
CREATE TYPE resource_type_enum AS ENUM ('ROOM','VIRTUAL');
CREATE TYPE modality_enum AS ENUM ('OFFLINE','ONLINE','HYBRID');
CREATE TYPE skill_enum AS ENUM ('general','reading','writing','speaking','listening');
CREATE TYPE teaching_role_enum AS ENUM ('primary','assistant');
CREATE TYPE branch_status_enum AS ENUM ('active','inactive','closed','planned');
CREATE TYPE class_status_enum AS ENUM ('draft','scheduled','ongoing','completed','cancelled');
CREATE TYPE subject_status_enum AS ENUM ('active','inactive');
CREATE TYPE assessment_kind_enum AS ENUM ('quiz','midterm','final','assignment','project','oral','practice','other');

-- Example: Nếu bạn thêm enum mới, chỉ cần thêm dòng này:
-- CREATE TYPE payment_status_enum AS ENUM ('pending','completed','failed','refunded');

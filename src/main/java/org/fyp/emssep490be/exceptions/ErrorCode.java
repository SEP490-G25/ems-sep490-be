package org.fyp.emssep490be.exceptions;

import lombok.Getter;

@Getter
public enum ErrorCode {
    // Branch errors (2000-2099)
    BRANCH_NOT_FOUND(2000, "Branch not found"),
    BRANCH_CODE_ALREADY_EXISTS(2001, "Branch code already exists for this center"),
    CENTER_NOT_FOUND(2002, "Center not found"),
    BRANCH_HAS_ACTIVE_CLASSES(2003, "Cannot delete branch with active classes"),

    // TimeSlot errors (2100-2199)
    TIMESLOT_NOT_FOUND(2100, "Time slot not found"),
    TIMESLOT_INVALID_TIME_RANGE(2101, "Start time must be before end time"),
    TIMESLOT_DURATION_MISMATCH(2102, "Calculated duration does not match provided duration"),
    TIMESLOT_OVERLAP(2103, "Time slot overlaps with existing time slot"),

    // Resource errors (2200-2299)
    RESOURCE_NOT_FOUND(2200, "Resource not found"),
    RESOURCE_NAME_ALREADY_EXISTS(2201, "Resource name already exists for this branch"),
    RESOURCE_CONFLICT(2202, "Resource is already booked for the specified time"),
    RESOURCE_INVALID_TYPE(2203, "Invalid resource type"),

    // Subject errors (1200-1219)
    SUBJECT_NOT_FOUND(1201, "Subject not found"),
    SUBJECT_CODE_DUPLICATE(1202, "Subject code already exists"),
    SUBJECT_CODE_INVALID(1203, "Subject code format invalid (must be uppercase alphanumeric with hyphens)"),
    SUBJECT_HAS_LEVELS(1204, "Cannot delete subject with existing levels"),
    SUBJECT_HAS_COURSES(1205, "Cannot delete subject with existing courses"),

    // Level errors (1220-1239)
    LEVEL_NOT_FOUND(1221, "Level not found"),
    LEVEL_CODE_DUPLICATE(1222, "Level code already exists for this subject"),
    LEVEL_HAS_COURSES(1223, "Cannot delete level with existing courses"),
    LEVEL_INVALID_SUBJECT(1224, "Invalid subject ID"),
    LEVEL_SORT_ORDER_DUPLICATE(1225, "Sort order already exists for this subject"),

    // Course errors (1240-1269)
    COURSE_NOT_FOUND(1240, "Course not found"),
    COURSE_ALREADY_EXISTS(1241, "Course already exists with this subject, level, and version"),
    COURSE_CODE_DUPLICATE(1242, "Course code already exists"),
    COURSE_CANNOT_BE_UPDATED(1243, "Course cannot be updated (must be in draft or rejected status)"),
    COURSE_CANNOT_BE_MODIFIED(1244, "Course cannot be modified (not in draft status)"),
    COURSE_IN_USE(1245, "Cannot delete course that is being used by classes"),
    COURSE_ALREADY_SUBMITTED(1246, "Course has already been submitted for approval"),
    COURSE_NOT_SUBMITTED(1247, "Course has not been submitted for approval"),
    COURSE_NO_PHASES(1248, "Course must have at least one phase before submission"),
    INVALID_ACTION(1249, "Invalid approval action (must be 'approve' or 'reject')"),
    REJECTION_REASON_REQUIRED(1250, "Rejection reason is required when rejecting a course"),
    INVALID_TOTAL_HOURS(1251, "Total hours calculation is inconsistent with duration, sessions per week, and hours per session"),

    // CoursePhase errors (1270-1289)
    PHASE_NOT_FOUND(1270, "Course phase not found"),
    PHASE_NUMBER_DUPLICATE(1271, "Phase number already exists for this course"),
    PHASE_HAS_SESSIONS(1272, "Cannot delete phase that has course sessions"),

    // CourseSession errors (1290-1309)
    SESSION_NOT_FOUND(1290, "Course session not found"),
    SESSION_SEQUENCE_DUPLICATE(1291, "Session sequence number already exists for this phase"),
    SESSION_IN_USE(1292, "Cannot delete course session that is being used in actual sessions"),
    INVALID_SKILL_SET(1293, "Invalid skill set value(s)"),

    // User errors (1000-1099)
    USER_NOT_FOUND(1000, "User not found"),
    USER_EMAIL_ALREADY_EXISTS(1001, "Email already exists"),
    USER_PHONE_ALREADY_EXISTS(1002, "Phone number already exists"),
    USER_ALREADY_EXISTS(1003, "User already exists"),
    ROLE_NOT_FOUND(1004, "Role not found"),
    INVALID_PASSWORD(1005, "Invalid password"),
    PASSWORD_MISMATCH(1006, "Old password does not match"),

    // Student errors (1100-1199)
    STUDENT_NOT_FOUND(1100, "Student not found"),
    STUDENT_CODE_ALREADY_EXISTS(1101, "Student code already exists"),

    // Enrollment errors (1200-1299)
    ENROLLMENT_NOT_FOUND(1200, "Enrollment not found"),
    ENROLLMENT_ALREADY_EXISTS(1201, "Student is already enrolled in this class"),
    CLASS_CAPACITY_EXCEEDED(1202, "Class capacity exceeded"),
    CLASS_NOT_AVAILABLE(1203, "Class is not available for enrollment"),
    CANNOT_UNENROLL_COMPLETED_CLASS(1204, "Cannot remove student from completed class"),

    // Class errors (4000-4099)
    CLASS_NOT_FOUND(4000, "Class not found"),

    // Teacher errors (3000-3099)
    TEACHER_NOT_FOUND(3000, "Teacher not found"),
    TEACHER_EMPLOYEE_CODE_ALREADY_EXISTS(3001, "Teacher employee code already exists"),
    TEACHER_SKILL_NOT_FOUND(3002, "Teacher skill not found"),
    TEACHER_AVAILABILITY_NOT_FOUND(3003, "Teacher availability not found"),
    TEACHER_AVAILABILITY_CONFLICT(3004, "Teacher availability conflicts with existing schedule"),
    TEACHER_ALREADY_ASSIGNED_TO_BRANCH(3005, "Teacher is already assigned to this branch"),
    TEACHER_NOT_ASSIGNED_TO_BRANCH(3006, "Teacher is not assigned to this branch"),
    TEACHER_SCHEDULE_NOT_FOUND(3007, "Teacher schedule not found"),
    TEACHER_WORKLOAD_EXCEEDED(3008, "Teacher workload exceeds maximum capacity"),
    // Common errors (9000-9999)
    INVALID_INPUT(9000, "Invalid input provided"),
    INVALID_REQUEST(9001, "Invalid request"),
    INVALID_STATUS(9002, "Invalid status value"),
    UNAUTHORIZED(9401, "Unauthorized access"),
    FORBIDDEN(9403, "Access forbidden"),
    INTERNAL_SERVER_ERROR(9500, "Internal server error");

    private final int code;
    private final String message;

    ErrorCode(int code, String message) {
        this.code = code;
        this.message = message;
    }

}

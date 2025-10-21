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

    // User errors (1000-1099)
    USER_NOT_FOUND(1000, "User not found"),
    USER_EMAIL_ALREADY_EXISTS(1001, "Email already exists"),
    USER_PHONE_ALREADY_EXISTS(1002, "Phone number already exists"),
    ROLE_NOT_FOUND(1003, "Role not found"),
    INVALID_PASSWORD(1004, "Invalid password"),
    PASSWORD_MISMATCH(1005, "Old password does not match"),

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
    INVALID_STATUS(9001, "Invalid status value"),
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

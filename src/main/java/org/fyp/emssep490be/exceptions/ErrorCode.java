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

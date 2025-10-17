package org.fyp.emssep490be.controllers.attendance;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.fyp.emssep490be.dtos.ResponseObject;
import org.fyp.emssep490be.dtos.attendance.RecordAttendanceRequestDTO;
import org.fyp.emssep490be.services.attendance.AttendanceService;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

/**
 * Controller for Attendance operations
 * Base path: /api/v1/sessions/{sessionId}/attendance
 */
@RestController
@RequestMapping("/api/v1/sessions/{sessionId}/attendance")
@RequiredArgsConstructor
public class AttendanceController {

    private final AttendanceService attendanceService;

    @PostMapping
    public ResponseEntity<ResponseObject<Object>> recordAttendance(
            @PathVariable Long sessionId,
            @Valid @RequestBody RecordAttendanceRequestDTO request) {
        // TODO: Implement attendance recording
        return ResponseEntity.ok(new ResponseObject<>(HttpStatus.OK.value(), "Attendance recorded", null));
    }

    @GetMapping
    public ResponseEntity<ResponseObject<Object>> getAttendance(@PathVariable Long sessionId) {
        // TODO: Implement get attendance
        return ResponseEntity.ok(new ResponseObject<>(HttpStatus.OK.value(), "Attendance retrieved", null));
    }
}

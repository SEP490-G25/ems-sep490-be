package org.fyp.emssep490be.controllers.teacher;

import lombok.RequiredArgsConstructor;
import org.fyp.emssep490be.dtos.ResponseObject;
import org.fyp.emssep490be.dtos.teacher.TeacherProfileDTO;
import org.fyp.emssep490be.services.teacher.TeacherService;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

/**
 * Controller for Teacher operations
 * Base path: /api/v1/teachers
 */
@RestController
@RequestMapping("/api/v1/teachers")
@RequiredArgsConstructor
public class TeacherController {

    private final TeacherService teacherService;

    @GetMapping("/{id}")
    public ResponseEntity<ResponseObject<TeacherProfileDTO>> getTeacherProfile(@PathVariable Long id) {
        // TODO: Implement
        return ResponseEntity.ok(new ResponseObject<>(HttpStatus.OK.value(), "Teacher profile retrieved", null));
    }

    @GetMapping("/{id}/schedule")
    public ResponseEntity<ResponseObject<Object>> getTeacherSchedule(
            @PathVariable Long id,
            @RequestParam(required = false) String dateFrom,
            @RequestParam(required = false) String dateTo) {
        // TODO: Implement teacher schedule
        return ResponseEntity.ok(new ResponseObject<>(HttpStatus.OK.value(), "Schedule retrieved", null));
    }

    @GetMapping("/{id}/workload")
    public ResponseEntity<ResponseObject<Object>> getTeacherWorkload(@PathVariable Long id) {
        // TODO: Implement workload calculation
        return ResponseEntity.ok(new ResponseObject<>(HttpStatus.OK.value(), "Workload retrieved", null));
    }
}

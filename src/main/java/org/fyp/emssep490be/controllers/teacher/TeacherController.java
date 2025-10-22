package org.fyp.emssep490be.controllers.teacher;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.fyp.emssep490be.dtos.ResponseObject;
import org.fyp.emssep490be.dtos.teacher.CreateTeacherRequestDTO;
import org.fyp.emssep490be.dtos.teacher.TeacherProfileDTO;
import org.fyp.emssep490be.dtos.teacher.UpdateTeacherRequestDTO;
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
        TeacherProfileDTO profile = teacherService.getTeacherProfile(id);
        return ResponseEntity.ok(new ResponseObject<>(HttpStatus.OK.value(), "Teacher profile retrieved", profile));
    }

    @PostMapping
    public ResponseEntity<ResponseObject<TeacherProfileDTO>> createTeacher(@Valid @RequestBody CreateTeacherRequestDTO request) {
        TeacherProfileDTO profile = teacherService.createTeacher(request);
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(new ResponseObject<>(HttpStatus.CREATED.value(), "Teacher created successfully", profile));
    }

    @PutMapping("/{id}")
    public ResponseEntity<ResponseObject<TeacherProfileDTO>> updateTeacher(@PathVariable Long id, @Valid @RequestBody UpdateTeacherRequestDTO request) {
        TeacherProfileDTO profile = teacherService.updateTeacher(id, request);
        return ResponseEntity.ok(new ResponseObject<>(HttpStatus.OK.value(), "Teacher updated successfully", profile));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<ResponseObject<Void>> deleteTeacher(@PathVariable Long id) {
        teacherService.deleteTeacher(id);
        return ResponseEntity.ok(new ResponseObject<>(HttpStatus.OK.value(), "Teacher deleted successfully", null));
    }

    // TODO: Implement teacher schedule endpoint
    // @GetMapping("/{id}/schedule")
    // public ResponseEntity<ResponseObject<Object>> getTeacherSchedule(
    //         @PathVariable Long id,
    //         @RequestParam(required = false) String dateFrom,
    //         @RequestParam(required = false) String dateTo) {
    //     Object schedule = teacherService.getTeacherSchedule(id, dateFrom, dateTo);
    //     return ResponseEntity.ok(new ResponseObject<>(HttpStatus.OK.value(), "Schedule retrieved", schedule));
    // }

    // TODO: Implement teacher workload endpoint
    // @GetMapping("/{id}/workload")
    // public ResponseEntity<ResponseObject<Object>> getTeacherWorkload(@PathVariable Long id) {
    //     Object workload = teacherService.getTeacherWorkload(id);
    //     return ResponseEntity.ok(new ResponseObject<>(HttpStatus.OK.value(), "Workload retrieved", workload));
    // }
}

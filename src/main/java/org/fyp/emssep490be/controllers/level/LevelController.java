package org.fyp.emssep490be.controllers.level;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.fyp.emssep490be.dtos.ResponseObject;
import org.fyp.emssep490be.dtos.level.CreateLevelRequestDTO;
import org.fyp.emssep490be.dtos.level.LevelDTO;
import org.fyp.emssep490be.dtos.level.UpdateLevelRequestDTO;
import org.fyp.emssep490be.services.level.LevelService;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * Controller for Level management operations
 * Base path: /api/v1/subjects/{subjectId}/levels
 */
@RestController
@RequestMapping("/api/v1/subjects/{subjectId}/levels")
@RequiredArgsConstructor
public class LevelController {

    private final LevelService levelService;

    @GetMapping
    public ResponseEntity<ResponseObject<List<LevelDTO>>> getLevelsBySubject(@PathVariable Long subjectId) {
        // TODO: Implement
        return ResponseEntity.ok(new ResponseObject<>(HttpStatus.OK.value(), "Levels retrieved successfully", null));
    }

    @GetMapping("/{id}")
    public ResponseEntity<ResponseObject<LevelDTO>> getLevelById(@PathVariable Long subjectId, @PathVariable Long id) {
        // TODO: Implement
        return ResponseEntity.ok(new ResponseObject<>(HttpStatus.OK.value(), "Level retrieved successfully", null));
    }

    @PostMapping
    public ResponseEntity<ResponseObject<LevelDTO>> createLevel(
            @PathVariable Long subjectId,
            @Valid @RequestBody CreateLevelRequestDTO request) {
        // TODO: Implement
        return ResponseEntity.status(HttpStatus.CREATED).body(
                new ResponseObject<>(HttpStatus.CREATED.value(), "Level created successfully", null));
    }

    @PutMapping("/{id}")
    public ResponseEntity<ResponseObject<LevelDTO>> updateLevel(
            @PathVariable Long subjectId,
            @PathVariable Long id,
            @Valid @RequestBody UpdateLevelRequestDTO request) {
        // TODO: Implement
        return ResponseEntity.ok(new ResponseObject<>(HttpStatus.OK.value(), "Level updated successfully", null));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteLevel(@PathVariable Long subjectId, @PathVariable Long id) {
        // TODO: Implement
        return ResponseEntity.noContent().build();
    }
}

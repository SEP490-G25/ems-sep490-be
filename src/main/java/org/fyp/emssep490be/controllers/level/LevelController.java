package org.fyp.emssep490be.controllers.level;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.fyp.emssep490be.dtos.ResponseObject;
import org.fyp.emssep490be.dtos.level.CreateLevelRequestDTO;
import org.fyp.emssep490be.dtos.level.LevelDTO;
import org.fyp.emssep490be.dtos.level.UpdateLevelRequestDTO;
import org.fyp.emssep490be.services.level.LevelService;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * Controller for Level management operations
 * Levels are nested under Subjects
 * Base path: /api/v1/subjects/{subjectId}/levels
 */
@RestController
@RequestMapping("/api/v1/subjects/{subjectId}/levels")
@RequiredArgsConstructor
@Validated
@Tag(name = "Levels", description = "Level management APIs (nested under Subjects)")
public class LevelController {

    private final LevelService levelService;

    /**
     * Get all levels for a subject
     * GET /api/v1/subjects/{subjectId}/levels
     *
     * @param subjectId Subject ID
     * @return List of levels ordered by sort order
     */
    @GetMapping
    @PreAuthorize("hasAnyRole('ADMIN', 'MANAGER', 'CENTER_HEAD', 'ACADEMIC_STAFF', 'SUBJECT_LEADER')")
    @Operation(summary = "Get levels by subject", description = "Retrieve all levels for a specific subject, ordered by sort order")
    public ResponseEntity<ResponseObject<List<LevelDTO>>> getLevelsBySubject(@PathVariable Long subjectId) {

        List<LevelDTO> levels = levelService.getLevelsBySubject(subjectId);

        return ResponseEntity.ok(
                ResponseObject.<List<LevelDTO>>builder()
                        .status(HttpStatus.OK.value())
                        .message("Levels retrieved successfully")
                        .data(levels)
                        .build()
        );
    }

    /**
     * Get level by ID
     * GET /api/v1/subjects/{subjectId}/levels/{id}
     *
     * @param subjectId Subject ID
     * @param id Level ID
     * @return Level information
     */
    @GetMapping("/{id}")
    @PreAuthorize("hasAnyRole('ADMIN', 'MANAGER', 'CENTER_HEAD', 'ACADEMIC_STAFF', 'SUBJECT_LEADER')")
    @Operation(summary = "Get level by ID", description = "Retrieve a specific level under a subject")
    public ResponseEntity<ResponseObject<LevelDTO>> getLevelById(
            @PathVariable Long subjectId,
            @PathVariable Long id) {

        LevelDTO level = levelService.getLevelById(subjectId, id);

        return ResponseEntity.ok(
                ResponseObject.<LevelDTO>builder()
                        .status(HttpStatus.OK.value())
                        .message("Level retrieved successfully")
                        .data(level)
                        .build()
        );
    }

    /**
     * Create a new level under a subject
     * POST /api/v1/subjects/{subjectId}/levels
     * Roles: ADMIN, SUBJECT_LEADER
     *
     * @param subjectId Subject ID
     * @param request Level creation data
     * @return Created level information
     */
    @PostMapping
    @PreAuthorize("hasAnyRole('ADMIN', 'SUBJECT_LEADER')")
    @Operation(summary = "Create level", description = "Create a new level under a subject (requires ADMIN or SUBJECT_LEADER role)")
    public ResponseEntity<ResponseObject<LevelDTO>> createLevel(
            @PathVariable Long subjectId,
            @Valid @RequestBody CreateLevelRequestDTO request) {

        LevelDTO createdLevel = levelService.createLevel(subjectId, request);

        return ResponseEntity.status(HttpStatus.CREATED).body(
                ResponseObject.<LevelDTO>builder()
                        .status(HttpStatus.CREATED.value())
                        .message("Level created successfully")
                        .data(createdLevel)
                        .build()
        );
    }

    /**
     * Update an existing level
     * PUT /api/v1/subjects/{subjectId}/levels/{id}
     * Roles: ADMIN, SUBJECT_LEADER
     *
     * @param subjectId Subject ID
     * @param id Level ID
     * @param request Level update data
     * @return Updated level information
     */
    @PutMapping("/{id}")
    @PreAuthorize("hasAnyRole('ADMIN', 'SUBJECT_LEADER')")
    @Operation(summary = "Update level", description = "Update an existing level (requires ADMIN or SUBJECT_LEADER role)")
    public ResponseEntity<ResponseObject<LevelDTO>> updateLevel(
            @PathVariable Long subjectId,
            @PathVariable Long id,
            @Valid @RequestBody UpdateLevelRequestDTO request) {

        LevelDTO updatedLevel = levelService.updateLevel(subjectId, id, request);

        return ResponseEntity.ok(
                ResponseObject.<LevelDTO>builder()
                        .status(HttpStatus.OK.value())
                        .message("Level updated successfully")
                        .data(updatedLevel)
                        .build()
        );
    }

    /**
     * Delete a level
     * DELETE /api/v1/subjects/{subjectId}/levels/{id}
     * Roles: ADMIN
     *
     * @param subjectId Subject ID
     * @param id Level ID
     * @return Success message
     */
    @DeleteMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    @Operation(summary = "Delete level", description = "Delete a level (requires ADMIN role). Cannot delete if level has courses.")
    public ResponseEntity<ResponseObject<Void>> deleteLevel(
            @PathVariable Long subjectId,
            @PathVariable Long id) {

        levelService.deleteLevel(subjectId, id);

        return ResponseEntity.ok(
                ResponseObject.<Void>builder()
                        .status(HttpStatus.OK.value())
                        .message("Level deleted successfully")
                        .data(null)
                        .build()
        );
    }
}

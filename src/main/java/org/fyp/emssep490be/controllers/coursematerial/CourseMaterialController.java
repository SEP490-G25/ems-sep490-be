package org.fyp.emssep490be.controllers.coursematerial;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.fyp.emssep490be.dtos.ResponseObject;
import org.fyp.emssep490be.dtos.coursematerial.CourseMaterialDTO;
import org.fyp.emssep490be.dtos.coursematerial.UploadMaterialRequestDTO;
import org.fyp.emssep490be.services.coursematerial.CourseMaterialService;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

/**
 * Controller for Course Material management operations
 * Base path: /api/v1/courses/{courseId}/materials
 */
@RestController
@RequestMapping("/api/v1/courses/{courseId}/materials")
@RequiredArgsConstructor
public class CourseMaterialController {

    private final CourseMaterialService courseMaterialService;

    /**
     * Upload course material
     * POST /api/v1/courses/{courseId}/materials
     * Material can be associated with course, phase, or session level
     *
     * @param courseId Course ID
     * @param request Upload material request with file and metadata
     * @return Created material DTO
     */
    @PostMapping
    public ResponseEntity<ResponseObject<CourseMaterialDTO>> uploadMaterial(
            @PathVariable Long courseId,
            @Valid @ModelAttribute UploadMaterialRequestDTO request) {
        CourseMaterialDTO uploadedMaterial = courseMaterialService.uploadMaterial(courseId, request);
        return ResponseEntity.status(HttpStatus.CREATED).body(
                new ResponseObject<>(HttpStatus.CREATED.value(), "Material uploaded successfully", uploadedMaterial));
    }

    /**
     * Delete course material
     * DELETE /api/v1/courses/{courseId}/materials/{id}
     * Deletes material record and associated file from storage
     *
     * @param courseId Course ID
     * @param id Material ID
     * @return Success response
     */
    @DeleteMapping("/{id}")
    public ResponseEntity<ResponseObject<Void>> deleteMaterial(@PathVariable Long courseId, @PathVariable Long id) {
        courseMaterialService.deleteMaterial(courseId, id);
        return ResponseEntity.ok(
                new ResponseObject<>(HttpStatus.OK.value(), "Material deleted successfully", null));
    }
}

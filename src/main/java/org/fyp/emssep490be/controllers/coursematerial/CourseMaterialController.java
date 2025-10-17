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

    @PostMapping
    public ResponseEntity<ResponseObject<CourseMaterialDTO>> uploadMaterial(
            @PathVariable Long courseId,
            @Valid @ModelAttribute UploadMaterialRequestDTO request) {
        // TODO: Implement file upload logic
        // - Handle multipart file upload
        // - Store file in storage (S3, local, etc.)
        // - Create course material record
        return ResponseEntity.status(HttpStatus.CREATED).body(
                new ResponseObject<>(HttpStatus.CREATED.value(), "Material uploaded successfully", null));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteMaterial(@PathVariable Long courseId, @PathVariable Long id) {
        // TODO: Implement delete material logic
        return ResponseEntity.noContent().build();
    }
}

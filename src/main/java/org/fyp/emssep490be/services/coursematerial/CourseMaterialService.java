package org.fyp.emssep490be.services.coursematerial;

import org.fyp.emssep490be.dtos.coursematerial.CourseMaterialDTO;
import org.fyp.emssep490be.dtos.coursematerial.UploadMaterialRequestDTO;

public interface CourseMaterialService {
    CourseMaterialDTO uploadMaterial(Long courseId, UploadMaterialRequestDTO request);
    void deleteMaterial(Long courseId, Long id);
}

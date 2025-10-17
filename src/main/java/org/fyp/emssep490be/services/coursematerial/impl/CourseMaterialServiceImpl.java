package org.fyp.emssep490be.services.coursematerial.impl;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.fyp.emssep490be.dtos.coursematerial.CourseMaterialDTO;
import org.fyp.emssep490be.dtos.coursematerial.UploadMaterialRequestDTO;
import org.fyp.emssep490be.repositories.CourseMaterialRepository;
import org.fyp.emssep490be.services.coursematerial.CourseMaterialService;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
@Slf4j
@Transactional
public class CourseMaterialServiceImpl implements CourseMaterialService {

    private final CourseMaterialRepository courseMaterialRepository;

    @Override
    public CourseMaterialDTO uploadMaterial(Long courseId, UploadMaterialRequestDTO request) {
        // TODO: Implement file upload and storage logic
        log.info("Uploading material for course ID: {}", courseId);
        return null;
    }

    @Override
    public void deleteMaterial(Long courseId, Long id) {
        // TODO: Implement delete material logic
        log.info("Deleting material ID: {} for course ID: {}", id, courseId);
    }
}

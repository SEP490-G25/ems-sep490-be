package org.fyp.emssep490be.dtos.coursematerial;

import jakarta.validation.constraints.NotBlank;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.web.multipart.MultipartFile;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class UploadMaterialRequestDTO {

    private MultipartFile file;

    @NotBlank(message = "Title is required")
    private String title;

    private Long phaseId;

    private Long courseSessionId;
}

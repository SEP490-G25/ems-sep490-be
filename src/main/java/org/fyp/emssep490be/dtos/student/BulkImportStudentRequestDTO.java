package org.fyp.emssep490be.dtos.student;

import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.web.multipart.MultipartFile;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class BulkImportStudentRequestDTO {
    
    @NotNull(message = "CSV file is required")
    private MultipartFile file;
    
    @NotNull(message = "Branch ID is required")
    private Long branchId;
}

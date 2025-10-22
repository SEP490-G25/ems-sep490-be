package org.fyp.emssep490be.dtos.student;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class BulkImportStudentResponseDTO {
    private int totalRows;
    private int successful;
    private int failed;
    private List<StudentDTO> studentsCreated;
    private List<ImportError> errors;
    
    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    public static class ImportError {
        private int row;
        private String fullName;
        private String email;
        private String error;
    }
}

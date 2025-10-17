package org.fyp.emssep490be.dtos.session;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDate;
import java.time.LocalTime;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class UpdateSessionRequestDTO {

    private LocalDate date;

    private LocalTime startTime;

    private LocalTime endTime;

    private String status;

    private String teacherNote;
}

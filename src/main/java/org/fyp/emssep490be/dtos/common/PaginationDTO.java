package org.fyp.emssep490be.dtos.common;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class PaginationDTO {

    private Integer currentPage;

    private Integer totalPages;

    private Long totalItems;

    private Integer limit;
}

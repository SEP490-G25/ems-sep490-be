package org.fyp.emssep490be.dtos.studentrequest;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

/**
 * DTO wrapping the search results for available makeup sessions.
 * Includes the list of available sessions and metadata about the search.
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class MakeupSessionSearchResultDTO {

    /**
     * Total number of available makeup sessions found
     */
    private Integer total;

    /**
     * List of available makeup sessions
     */
    private List<AvailableMakeupSessionDTO> makeupSessions;

    /**
     * Information about the target (missed) session for reference
     */
    private SessionBasicDTO targetSessionInfo;
}

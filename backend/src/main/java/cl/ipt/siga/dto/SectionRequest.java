package cl.ipt.siga.dto;

import jakarta.validation.constraints.NotNull;

import java.util.List;

/**
 * @param blocks schedule blocks such as ["L2", "W3"]
 */
public record SectionRequest(
        @NotNull Long subjectId,
        @NotNull Long periodId,
        @NotNull Long teacherId,
        @NotNull Integer capacity,
        @NotNull List<String> blocks) {
}

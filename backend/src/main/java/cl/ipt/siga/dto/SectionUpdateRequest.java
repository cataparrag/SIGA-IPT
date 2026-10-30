package cl.ipt.siga.dto;

import jakarta.validation.constraints.NotNull;

import java.util.List;

/** The subject and period of a section cannot change; only teacher, capacity and schedule. */
public record SectionUpdateRequest(
        @NotNull Long teacherId,
        @NotNull Integer capacity,
        @NotNull List<String> blocks) {
}

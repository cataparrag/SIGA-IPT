package cl.ipt.siga.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

import java.util.List;

/**
 * Only checks that the fields are present. The numeric rules (semester 1-4,
 * SCT 1-7, TEL under 8 hours, prerequisites) are business rules and live in
 * SubjectService, where they are unit tested.
 */
public record SubjectRequest(
        @NotBlank @Size(max = 20) String code,
        @NotBlank String name,
        @NotNull Integer semester,
        @NotNull Integer theoryHours,
        @NotNull Integer exerciseHours,
        @NotNull Integer labHours,
        @NotNull Integer sct,
        List<Long> prerequisiteIds) {
}
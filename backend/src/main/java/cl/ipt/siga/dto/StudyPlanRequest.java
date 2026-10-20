package cl.ipt.siga.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

/**
 * @param identifier e.g. "2021.3"
 * @param current    true to make this plan the current one of its career
 */
public record StudyPlanRequest(
        @NotBlank @Size(max = 20) String identifier,
        @NotNull Boolean current) {
}
package cl.ipt.siga.dto;

import jakarta.validation.constraints.NotNull;

/**
 * Later the teacherId will come from the logged-in user (Keycloak token).
 */
public record GradeRequest(@NotNull Long teacherId, @NotNull Double finalGrade) {
}

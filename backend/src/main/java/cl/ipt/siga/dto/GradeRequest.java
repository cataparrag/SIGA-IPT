package cl.ipt.siga.dto;

import jakarta.validation.constraints.NotNull;

/** The teacher is the logged-in user (taken from the Keycloak token). */
public record GradeRequest(@NotNull Double finalGrade) {
}

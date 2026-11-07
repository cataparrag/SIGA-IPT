package cl.ipt.siga.dto;

import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;

import java.util.List;

/**
 * The sections the student wants to take in a period, all in one request,
 * so the minimum/maximum and schedule rules are checked on the whole selection.
 * The student is the logged-in user (taken from the Keycloak token).
 */
public record EnrollmentRequest(
        @NotNull Long periodId,
        @NotEmpty List<Long> sectionIds) {
}

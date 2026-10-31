package cl.ipt.siga.dto;

import jakarta.validation.constraints.NotNull;

public record MatriculationRequest(@NotNull Long studentId) {
}

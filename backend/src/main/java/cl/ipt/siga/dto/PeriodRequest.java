package cl.ipt.siga.dto;

import jakarta.validation.constraints.NotNull;

public record PeriodRequest(@NotNull Integer year, @NotNull Integer semester) {
}

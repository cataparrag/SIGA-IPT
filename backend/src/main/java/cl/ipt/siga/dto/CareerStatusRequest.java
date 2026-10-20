package cl.ipt.siga.dto;

import cl.ipt.siga.model.CareerStatus;
import jakarta.validation.constraints.NotNull;

public record CareerStatusRequest(@NotNull CareerStatus status) {
}
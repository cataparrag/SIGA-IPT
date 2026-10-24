package cl.ipt.siga.dto;

import cl.ipt.siga.model.StudentStatus;
import jakarta.validation.constraints.NotNull;

public record StudentStatusRequest(@NotNull StudentStatus status) {
}

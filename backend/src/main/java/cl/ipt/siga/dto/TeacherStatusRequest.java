package cl.ipt.siga.dto;

import cl.ipt.siga.model.TeacherStatus;
import jakarta.validation.constraints.NotNull;

public record TeacherStatusRequest(@NotNull TeacherStatus status) {
}

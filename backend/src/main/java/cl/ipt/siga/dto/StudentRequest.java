package cl.ipt.siga.dto;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;

/**
 * @param studyPlanId optional. On creation it is ignored: the student always
 *                    gets the current plan of the career. On update it allows
 *                    moving the student to another plan of the same career.
 */
public record StudentRequest(
        @NotBlank @Pattern(regexp = "^\\d{7,8}-[\\dkK]$", message = "debe tener el formato 12345678-9")
        String run,
        @NotBlank String fullName,
        @NotBlank @Email String email,
        @NotNull Long careerId,
        Long studyPlanId) {
}

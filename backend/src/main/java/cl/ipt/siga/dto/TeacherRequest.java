package cl.ipt.siga.dto;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;

public record TeacherRequest(
        @NotBlank @Pattern(regexp = "^\\d{7,8}-[\\dkK]$", message = "debe tener el formato 12345678-9")
        String run,
        @NotBlank String fullName,
        @NotBlank @Email String email,
        @NotBlank String professionalTitle,
        @NotBlank String academicDegree) {
}

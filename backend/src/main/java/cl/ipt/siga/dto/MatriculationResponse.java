package cl.ipt.siga.dto;

import cl.ipt.siga.model.Matriculation;

import java.time.LocalDate;

public record MatriculationResponse(
        Long id,
        Long studentId,
        String studentRun,
        String studentName,
        Long periodId,
        String periodCode,
        LocalDate registeredAt) {

    public static MatriculationResponse from(Matriculation matriculation) {
        return new MatriculationResponse(
                matriculation.getId(),
                matriculation.getStudent().getId(),
                matriculation.getStudent().getRun(),
                matriculation.getStudent().getFullName(),
                matriculation.getPeriod().getId(),
                matriculation.getPeriod().getCode(),
                matriculation.getRegisteredAt());
    }
}

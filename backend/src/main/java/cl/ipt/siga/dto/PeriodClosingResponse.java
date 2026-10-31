package cl.ipt.siga.dto;

import cl.ipt.siga.model.PeriodStatus;

import java.util.List;

/** Summary of a period closing: records created and students whose status changed. */
public record PeriodClosingResponse(
        String periodCode,
        PeriodStatus status,
        int recordsCreated,
        List<String> eliminatedStudents,
        List<String> graduatedStudents) {
}

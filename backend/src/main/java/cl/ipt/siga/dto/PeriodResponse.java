package cl.ipt.siga.dto;

import cl.ipt.siga.model.AcademicPeriod;
import cl.ipt.siga.model.PeriodStatus;

public record PeriodResponse(Long id, String code, int year, int semester, PeriodStatus status) {

    public static PeriodResponse from(AcademicPeriod period) {
        return new PeriodResponse(
                period.getId(),
                period.getCode(),
                period.getYear(),
                period.getSemester(),
                period.getStatus());
    }
}

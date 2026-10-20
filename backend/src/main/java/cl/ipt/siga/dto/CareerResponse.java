package cl.ipt.siga.dto;

import cl.ipt.siga.model.Career;
import cl.ipt.siga.model.CareerStatus;

public record CareerResponse(
        Long id,
        String code,
        String name,
        String description,
        int durationSemesters,
        CareerStatus status) {

    public static CareerResponse from(Career career) {
        return new CareerResponse(
                career.getId(),
                career.getCode(),
                career.getName(),
                career.getDescription(),
                Career.DURATION_SEMESTERS,
                career.getStatus());
    }
}
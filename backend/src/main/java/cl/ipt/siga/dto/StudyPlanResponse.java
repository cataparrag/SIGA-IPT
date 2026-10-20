package cl.ipt.siga.dto;

import cl.ipt.siga.model.StudyPlan;
import cl.ipt.siga.model.StudyPlanStatus;

public record StudyPlanResponse(
        Long id,
        String identifier,
        StudyPlanStatus status,
        Long careerId,
        String careerCode) {

    public static StudyPlanResponse from(StudyPlan plan) {
        return new StudyPlanResponse(
                plan.getId(),
                plan.getIdentifier(),
                plan.getStatus(),
                plan.getCareer().getId(),
                plan.getCareer().getCode());
    }
}
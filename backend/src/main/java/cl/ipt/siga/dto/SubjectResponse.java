package cl.ipt.siga.dto;

import cl.ipt.siga.model.Subject;

import java.util.List;

public record SubjectResponse(
        Long id,
        String code,
        String name,
        int semester,
        String tel,
        int totalHours,
        int sct,
        Long studyPlanId,
        List<String> prerequisiteCodes) {

    public static SubjectResponse from(Subject subject) {
        List<String> prerequisiteCodes = subject.getPrerequisites().stream()
                .map(Subject::getCode)
                .sorted()
                .toList();
        String tel = subject.getTheoryHours() + "-" + subject.getExerciseHours() + "-" + subject.getLabHours();
        return new SubjectResponse(
                subject.getId(),
                subject.getCode(),
                subject.getName(),
                subject.getSemester(),
                tel,
                subject.getTotalHours(),
                subject.getSct(),
                subject.getStudyPlan().getId(),
                prerequisiteCodes);
    }
}
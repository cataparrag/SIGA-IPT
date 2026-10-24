package cl.ipt.siga.dto;

import cl.ipt.siga.model.Student;
import cl.ipt.siga.model.StudentStatus;

public record StudentResponse(
        Long id,
        String run,
        String fullName,
        String email,
        StudentStatus status,
        Long careerId,
        String careerCode,
        Long studyPlanId,
        String studyPlanIdentifier) {

    public static StudentResponse from(Student student) {
        return new StudentResponse(
                student.getId(),
                student.getRun(),
                student.getFullName(),
                student.getEmail(),
                student.getStatus(),
                student.getCareer().getId(),
                student.getCareer().getCode(),
                student.getStudyPlan().getId(),
                student.getStudyPlan().getIdentifier());
    }
}

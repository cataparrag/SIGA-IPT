package cl.ipt.siga.dto;

import cl.ipt.siga.model.Enrollment;
import cl.ipt.siga.model.EnrollmentResult;

import java.time.LocalDateTime;

public record EnrollmentResponse(
        Long id,
        Long studentId,
        String studentRun,
        String studentName,
        Long sectionId,
        String subjectCode,
        String subjectName,
        int subjectSemester,
        String periodCode,
        LocalDateTime enrollmentDate,
        Double finalGrade,
        EnrollmentResult result) {

    public static EnrollmentResponse from(Enrollment enrollment) {
        return new EnrollmentResponse(
                enrollment.getId(),
                enrollment.getStudent().getId(),
                enrollment.getStudent().getRun(),
                enrollment.getStudent().getFullName(),
                enrollment.getSection().getId(),
                enrollment.getSection().getSubject().getCode(),
                enrollment.getSection().getSubject().getName(),
                enrollment.getSection().getSubject().getSemester(),
                enrollment.getSection().getPeriod().getCode(),
                enrollment.getEnrollmentDate(),
                enrollment.getFinalGrade(),
                enrollment.getResult());
    }
}

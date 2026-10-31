package cl.ipt.siga.dto;

import cl.ipt.siga.model.AcademicRecord;
import cl.ipt.siga.model.EnrollmentResult;

public record AcademicRecordResponse(
        Long id,
        String periodCode,
        String subjectCode,
        String subjectName,
        int subjectSemester,
        int sct,
        double finalGrade,
        EnrollmentResult result) {

    public static AcademicRecordResponse from(AcademicRecord record) {
        return new AcademicRecordResponse(
                record.getId(),
                record.getPeriodCode(),
                record.getSubjectCode(),
                record.getSubjectName(),
                record.getSubjectSemester(),
                record.getSct(),
                record.getFinalGrade(),
                record.getResult());
    }
}

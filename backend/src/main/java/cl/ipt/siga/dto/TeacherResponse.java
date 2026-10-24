package cl.ipt.siga.dto;

import cl.ipt.siga.model.Teacher;
import cl.ipt.siga.model.TeacherStatus;

public record TeacherResponse(
        Long id,
        String run,
        String fullName,
        String email,
        String professionalTitle,
        String academicDegree,
        TeacherStatus status) {

    public static TeacherResponse from(Teacher teacher) {
        return new TeacherResponse(
                teacher.getId(),
                teacher.getRun(),
                teacher.getFullName(),
                teacher.getEmail(),
                teacher.getProfessionalTitle(),
                teacher.getAcademicDegree(),
                teacher.getStatus());
    }
}

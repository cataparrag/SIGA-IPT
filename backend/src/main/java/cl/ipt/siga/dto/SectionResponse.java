package cl.ipt.siga.dto;

import cl.ipt.siga.model.ScheduleBlocks;
import cl.ipt.siga.model.Section;

import java.util.List;

public record SectionResponse(
        Long id,
        Long subjectId,
        String subjectCode,
        String subjectName,
        int subjectSemester,
        Long periodId,
        String periodCode,
        Long teacherId,
        String teacherName,
        int capacity,
        long enrolledCount,
        long availableSeats,
        List<String> blocks) {

    public static SectionResponse from(Section section, long enrolledCount) {
        return new SectionResponse(
                section.getId(),
                section.getSubject().getId(),
                section.getSubject().getCode(),
                section.getSubject().getName(),
                section.getSubject().getSemester(),
                section.getPeriod().getId(),
                section.getPeriod().getCode(),
                section.getTeacher().getId(),
                section.getTeacher().getFullName(),
                section.getCapacity(),
                enrolledCount,
                Math.max(0, section.getCapacity() - enrolledCount),
                ScheduleBlocks.sorted(section.getBlocks()));
    }
}

package cl.ipt.siga.controller;

import cl.ipt.siga.dto.EnrollmentRequest;
import cl.ipt.siga.dto.EnrollmentResponse;
import cl.ipt.siga.dto.GradeRequest;
import cl.ipt.siga.dto.SectionResponse;
import cl.ipt.siga.security.CurrentUser;
import cl.ipt.siga.service.EnrollmentService;
import cl.ipt.siga.service.GradeService;
import cl.ipt.siga.service.SectionService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api")
@RequiredArgsConstructor
public class EnrollmentController {

    private final EnrollmentService enrollmentService;
    private final GradeService gradeService;
    private final SectionService sectionService;
    private final CurrentUser currentUser;

    /** Enrolls the logged-in student in several sections at once. */
    @PostMapping("/enrollments")
    public ResponseEntity<List<EnrollmentResponse>> enroll(@Valid @RequestBody EnrollmentRequest request) {
        Long studentId = currentUser.requireStudentId();
        return ResponseEntity.status(HttpStatus.CREATED).body(enrollmentService.enroll(studentId, request));
    }

    /** Cancels an enrollment and frees its seat. */
    @DeleteMapping("/enrollments/{id}")
    public ResponseEntity<Void> cancel(@PathVariable Long id) {
        currentUser.checkStudentAccess(enrollmentService.getEnrollment(id).getStudent().getId());
        enrollmentService.cancel(id);
        return ResponseEntity.noContent().build();
    }

    /** Registers or modifies the final grade (E7). */
    @PutMapping("/enrollments/{id}/grade")
    public EnrollmentResponse registerGrade(@PathVariable Long id, @Valid @RequestBody GradeRequest request) {
        return gradeService.registerGrade(id, currentUser.requireTeacherId(), request.finalGrade());
    }

    @GetMapping("/students/{studentId}/enrollments")
    public List<EnrollmentResponse> findByStudent(@PathVariable Long studentId,
                                                  @RequestParam(required = false) Long periodId) {
        currentUser.checkStudentAccess(studentId);
        return enrollmentService.findByStudent(studentId, periodId);
    }

    /** Sections the student can choose in a period. */
    @GetMapping("/students/{studentId}/available-sections")
    public List<SectionResponse> findAvailableSections(@PathVariable Long studentId, @RequestParam Long periodId) {
        currentUser.checkStudentAccess(studentId);
        return enrollmentService.findAvailableSections(studentId, periodId);
    }

    @GetMapping("/sections/{sectionId}/enrollments")
    public List<EnrollmentResponse> findBySection(@PathVariable Long sectionId) {
        currentUser.checkTeacherAccess(sectionService.getSection(sectionId).getTeacher().getId());
        return enrollmentService.findBySection(sectionId);
    }

    @GetMapping("/periods/{periodId}/enrollments")
    public List<EnrollmentResponse> findByPeriod(@PathVariable Long periodId) {
        return enrollmentService.findByPeriod(periodId);
    }
}

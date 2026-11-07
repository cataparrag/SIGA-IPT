package cl.ipt.siga.controller;

import cl.ipt.siga.dto.AcademicRecordResponse;
import cl.ipt.siga.security.CurrentUser;
import cl.ipt.siga.service.AcademicRecordService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/api/students")
@RequiredArgsConstructor
public class AcademicRecordController {

    private final AcademicRecordService academicRecordService;
    private final CurrentUser currentUser;

    @GetMapping("/{studentId}/academic-record")
    public List<AcademicRecordResponse> findByStudent(@PathVariable Long studentId) {
        currentUser.checkStudentAccess(studentId);
        return academicRecordService.findByStudent(studentId);
    }
}

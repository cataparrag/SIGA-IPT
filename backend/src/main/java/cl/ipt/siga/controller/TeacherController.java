package cl.ipt.siga.controller;

import cl.ipt.siga.dto.TeacherRequest;
import cl.ipt.siga.dto.TeacherResponse;
import cl.ipt.siga.dto.TeacherStatusRequest;
import cl.ipt.siga.security.CurrentUser;
import cl.ipt.siga.service.TeacherService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/teachers")
@RequiredArgsConstructor
public class TeacherController {

    private final TeacherService teacherService;
    private final CurrentUser currentUser;

    @GetMapping
    public List<TeacherResponse> findAll() {
        return teacherService.findAll();
    }

    @GetMapping("/{id}")
    public TeacherResponse findById(@PathVariable Long id) {
        currentUser.checkTeacherAccess(id);
        return teacherService.findById(id);
    }

    @PostMapping
    public ResponseEntity<TeacherResponse> create(@Valid @RequestBody TeacherRequest request) {
        return ResponseEntity.status(HttpStatus.CREATED).body(teacherService.create(request));
    }

    @PutMapping("/{id}")
    public TeacherResponse update(@PathVariable Long id, @Valid @RequestBody TeacherRequest request) {
        return teacherService.update(id, request);
    }

    @PutMapping("/{id}/status")
    public TeacherResponse changeStatus(@PathVariable Long id, @Valid @RequestBody TeacherStatusRequest request) {
        return teacherService.changeStatus(id, request.status());
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> delete(@PathVariable Long id) {
        teacherService.delete(id);
        return ResponseEntity.noContent().build();
    }
}

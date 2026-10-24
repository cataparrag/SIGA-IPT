package cl.ipt.siga.controller;

import cl.ipt.siga.dto.SubjectRequest;
import cl.ipt.siga.dto.SubjectResponse;
import cl.ipt.siga.service.SubjectService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api")
@RequiredArgsConstructor
public class SubjectController {

    private final SubjectService subjectService;

    @GetMapping("/study-plans/{studyPlanId}/subjects")
    public List<SubjectResponse> findByStudyPlan(@PathVariable Long studyPlanId) {
        return subjectService.findByStudyPlan(studyPlanId);
    }

    @PostMapping("/study-plans/{studyPlanId}/subjects")
    public ResponseEntity<SubjectResponse> create(@PathVariable Long studyPlanId,
                                                  @Valid @RequestBody SubjectRequest request) {
        return ResponseEntity.status(HttpStatus.CREATED).body(subjectService.create(studyPlanId, request));
    }

    @GetMapping("/subjects/{id}")
    public SubjectResponse findById(@PathVariable Long id) {
        return subjectService.findById(id);
    }

    @PutMapping("/subjects/{id}")
    public SubjectResponse update(@PathVariable Long id, @Valid @RequestBody SubjectRequest request) {
        return subjectService.update(id, request);
    }

    @DeleteMapping("/subjects/{id}")
    public ResponseEntity<Void> delete(@PathVariable Long id) {
        subjectService.delete(id);
        return ResponseEntity.noContent().build();
    }
}
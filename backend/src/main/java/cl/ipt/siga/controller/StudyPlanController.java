package cl.ipt.siga.controller;

import cl.ipt.siga.dto.StudyPlanRequest;
import cl.ipt.siga.dto.StudyPlanResponse;
import cl.ipt.siga.service.StudyPlanService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api")
@RequiredArgsConstructor
public class StudyPlanController {

    private final StudyPlanService studyPlanService;

    @GetMapping("/careers/{careerId}/study-plans")
    public List<StudyPlanResponse> findByCareer(@PathVariable Long careerId) {
        return studyPlanService.findByCareer(careerId);
    }

    @PostMapping("/careers/{careerId}/study-plans")
    public ResponseEntity<StudyPlanResponse> create(@PathVariable Long careerId,
                                                    @Valid @RequestBody StudyPlanRequest request) {
        return ResponseEntity.status(HttpStatus.CREATED).body(studyPlanService.create(careerId, request));
    }

    @GetMapping("/study-plans/{id}")
    public StudyPlanResponse findById(@PathVariable Long id) {
        return studyPlanService.findById(id);
    }

    @PutMapping("/study-plans/{id}/current")
    public StudyPlanResponse setCurrent(@PathVariable Long id) {
        return studyPlanService.setCurrent(id);
    }

    @DeleteMapping("/study-plans/{id}")
    public ResponseEntity<Void> delete(@PathVariable Long id) {
        studyPlanService.delete(id);
        return ResponseEntity.noContent().build();
    }
}
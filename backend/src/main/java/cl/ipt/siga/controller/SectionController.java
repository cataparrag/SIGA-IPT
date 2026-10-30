package cl.ipt.siga.controller;

import cl.ipt.siga.dto.SectionRequest;
import cl.ipt.siga.dto.SectionResponse;
import cl.ipt.siga.dto.SectionUpdateRequest;
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
public class SectionController {

    private final SectionService sectionService;

    /** Academic offer: all sections of a period. */
    @GetMapping("/periods/{periodId}/sections")
    public List<SectionResponse> findByPeriod(@PathVariable Long periodId) {
        return sectionService.findByPeriod(periodId);
    }

    @GetMapping("/teachers/{teacherId}/sections")
    public List<SectionResponse> findByTeacher(@PathVariable Long teacherId) {
        return sectionService.findByTeacher(teacherId);
    }

    @GetMapping("/sections/{id}")
    public SectionResponse findById(@PathVariable Long id) {
        return sectionService.findById(id);
    }

    @PostMapping("/sections")
    public ResponseEntity<SectionResponse> create(@Valid @RequestBody SectionRequest request) {
        return ResponseEntity.status(HttpStatus.CREATED).body(sectionService.create(request));
    }

    @PutMapping("/sections/{id}")
    public SectionResponse update(@PathVariable Long id, @Valid @RequestBody SectionUpdateRequest request) {
        return sectionService.update(id, request);
    }

    @DeleteMapping("/sections/{id}")
    public ResponseEntity<Void> delete(@PathVariable Long id) {
        sectionService.delete(id);
        return ResponseEntity.noContent().build();
    }
}

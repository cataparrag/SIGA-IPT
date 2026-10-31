package cl.ipt.siga.controller;

import cl.ipt.siga.dto.PeriodClosingResponse;
import cl.ipt.siga.service.PeriodClosingService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/periods")
@RequiredArgsConstructor
public class PeriodClosingController {

    private final PeriodClosingService periodClosingService;

    @PostMapping("/{periodId}/close")
    public PeriodClosingResponse close(@PathVariable Long periodId) {
        return periodClosingService.close(periodId);
    }
}

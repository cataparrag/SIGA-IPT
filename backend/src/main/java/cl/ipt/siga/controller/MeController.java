package cl.ipt.siga.controller;

import cl.ipt.siga.dto.MeResponse;
import cl.ipt.siga.model.Student;
import cl.ipt.siga.model.Teacher;
import cl.ipt.siga.security.CurrentUser;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/me")
@RequiredArgsConstructor
public class MeController {

    private final CurrentUser currentUser;

    @GetMapping
    public MeResponse me() {
        return new MeResponse(
                currentUser.name(),
                currentUser.email(),
                currentUser.roles(),
                currentUser.student().map(Student::getId).orElse(null),
                currentUser.teacher().map(Teacher::getId).orElse(null));
    }
}

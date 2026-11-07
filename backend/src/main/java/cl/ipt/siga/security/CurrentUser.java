package cl.ipt.siga.security;

import cl.ipt.siga.exception.ForbiddenException;
import cl.ipt.siga.model.Student;
import cl.ipt.siga.model.Teacher;
import cl.ipt.siga.repository.StudentRepository;
import cl.ipt.siga.repository.TeacherRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.security.oauth2.server.resource.authentication.JwtAuthenticationToken;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Objects;
import java.util.Optional;

/**
 * The logged-in user, read from the Keycloak token. Students and teachers are linked
 * to their Keycloak user by email: SIGA IPT never stores passwords.
 */
@Component
@RequiredArgsConstructor
public class CurrentUser {

    private final StudentRepository studentRepository;
    private final TeacherRepository teacherRepository;

    public Jwt token() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        if (authentication instanceof JwtAuthenticationToken jwtAuthentication) {
            return jwtAuthentication.getToken();
        }
        throw new ForbiddenException("No hay un usuario autenticado");
    }

    public String email() {
        return token().getClaimAsString("email");
    }

    public String name() {
        String name = token().getClaimAsString("name");
        return name != null ? name : email();
    }

    public List<String> roles() {
        return SecurityContextHolder.getContext().getAuthentication().getAuthorities().stream()
                .map(authority -> authority.getAuthority())
                .filter(authority -> authority.startsWith("ROLE_"))
                .map(authority -> authority.substring("ROLE_".length()))
                .toList();
    }

    public boolean hasRole(String role) {
        return roles().contains(role);
    }

    public Optional<Student> student() {
        return studentRepository.findByEmail(email());
    }

    public Optional<Teacher> teacher() {
        return teacherRepository.findByEmail(email());
    }

    public Long requireStudentId() {
        return student().map(Student::getId)
                .orElseThrow(() -> new ForbiddenException("Tu usuario no está asociado a un estudiante"));
    }

    public Long requireTeacherId() {
        return teacher().map(Teacher::getId)
                .orElseThrow(() -> new ForbiddenException("Tu usuario no está asociado a un docente"));
    }

    /** The administrator can see any student; a student only themselves. */
    public void checkStudentAccess(Long studentId) {
        if (hasRole("ADMIN")) {
            return;
        }
        if (!Objects.equals(requireStudentId(), studentId)) {
            throw new ForbiddenException("Solo puedes consultar tu propia información académica");
        }
    }

    /** The administrator can see any teacher; a teacher only themselves. */
    public void checkTeacherAccess(Long teacherId) {
        if (hasRole("ADMIN")) {
            return;
        }
        if (!Objects.equals(requireTeacherId(), teacherId)) {
            throw new ForbiddenException("Solo puedes consultar tus propias secciones");
        }
    }
}

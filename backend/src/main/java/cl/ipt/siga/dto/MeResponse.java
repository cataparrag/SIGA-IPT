package cl.ipt.siga.dto;

import java.util.List;

/** The logged-in user as the frontend needs it. studentId/teacherId are null when not applicable. */
public record MeResponse(String name, String email, List<String> roles, Long studentId, Long teacherId) {
}

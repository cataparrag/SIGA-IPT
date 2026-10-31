package cl.ipt.siga.exception;

/** The user is not allowed to perform the operation (e.g. not the section's teacher). Mapped to HTTP 403. */
public class ForbiddenException extends RuntimeException {
    public ForbiddenException(String message) {
        super(message);
    }
}

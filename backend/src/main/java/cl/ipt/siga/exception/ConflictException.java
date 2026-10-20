package cl.ipt.siga.exception;

/** The request conflicts with existing data (duplicates, dependencies). Mapped to HTTP 409. */
public class ConflictException extends RuntimeException {
    public ConflictException(String message) {
        super(message);
    }
}
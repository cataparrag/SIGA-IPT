package cl.ipt.siga.exception;

/** The request data breaks a business rule (e.g. SCT out of range). Mapped to HTTP 400. */
public class BadRequestException extends RuntimeException {
    public BadRequestException(String message) {
        super(message);
    }
}
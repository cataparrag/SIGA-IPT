package cl.ipt.siga.model;

public enum StudentStatus {
    REGULAR,
    POSTPONED,
    TEMPORARY_WITHDRAWAL,
    GRADUATED,
    ELIMINATED;

    /** GRADUATED and ELIMINATED are only set by the period closing process. */
    public boolean isManuallyAssignable() {
        return this == REGULAR || this == POSTPONED || this == TEMPORARY_WITHDRAWAL;
    }
}

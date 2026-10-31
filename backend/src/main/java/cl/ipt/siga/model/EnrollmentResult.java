package cl.ipt.siga.model;

public enum EnrollmentResult {
    APPROVED,
    FAILED;

    public static final double PASSING_GRADE = 4.0;

    /** A final grade of 4.0 or higher approves the subject. */
    public static EnrollmentResult fromGrade(double grade) {
        return grade >= PASSING_GRADE ? APPROVED : FAILED;
    }
}

package de.ceskilia.schoolbot.school.verification;

public enum VerificationResult {

    // todo: translation?

    SUCCEED(false),
    ALREADY_VERIFIED(false),
    CANNOT_VERIFY(true),
    FAILED_USERNAME(true),
    FAILED_PASSWORD(true),
    FAILED(true);

    private final boolean fail;

    VerificationResult(boolean fail) {
        this.fail = fail;
    }

    public boolean isFail() {
        return fail;
    }

}

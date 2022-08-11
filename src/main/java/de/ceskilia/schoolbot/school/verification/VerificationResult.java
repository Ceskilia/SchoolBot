package de.ceskilia.schoolbot.school.verification;

import org.jetbrains.annotations.NotNull;

public enum VerificationResult {

    SUCCEED("Erfolgreich",false),
    ALREADY_VERIFIED("Bereits verifiziert",false),
    WRONG_USERNAME("Falscher Name",true),
    WRONG_PASSWORD("Falsches Password",true),
    WRONG_DATA("Falsche Daten",true);

    private final String translation;
    private final boolean fail;

    VerificationResult(@NotNull String translation, boolean fail) {
        this.translation = translation;
        this.fail = fail;
    }

    public @NotNull String getTranslation() {
        return translation;
    }

    public boolean isFail() {
        return fail;
    }

}

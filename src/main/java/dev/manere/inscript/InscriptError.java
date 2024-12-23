package dev.manere.inscript;

import org.jetbrains.annotations.NotNull;

public class InscriptError extends RuntimeException {
    public InscriptError(final @NotNull Throwable details) {
        super(details);
    }

    public InscriptError(final @NotNull String details) {
        super(details);
    }
}

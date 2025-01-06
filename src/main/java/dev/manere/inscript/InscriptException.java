package dev.manere.inscript;

import org.jetbrains.annotations.NotNull;

public class InscriptException extends RuntimeException {
    public InscriptException(final @NotNull Throwable details) {
        super(details);
    }

    public InscriptException(final @NotNull String details) {
        super(details);
    }

    public InscriptException() {
        super();
    }
}

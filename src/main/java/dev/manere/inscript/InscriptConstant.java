package dev.manere.inscript;

import com.google.errorprone.annotations.CanIgnoreReturnValue;
import org.jetbrains.annotations.NotNull;

public class InscriptConstant<T> {
    private T value;

    public InscriptConstant(final @NotNull T value) {
        this.value = value;
    }

    @NotNull
    @CanIgnoreReturnValue
    public InscriptConstant<T> edit(final @NotNull T value) {
        this.value = value;
        return this;
    }

    @NotNull
    public T getValue() {
        return value;
    }

    @NotNull
    @Override
    public String toString() {
        return getValue().toString();
    }
}

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
    public InscriptConstant<T> set(final @NotNull T value) {
        this.value = value;
        return this;
    }

    @NotNull
    public T get() {
        return value;
    }

    @NotNull
    @Override
    public String toString() {
        return get().toString();
    }
}

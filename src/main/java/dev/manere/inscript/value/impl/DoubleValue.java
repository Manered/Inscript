package dev.manere.inscript.value.impl;

import dev.manere.inscript.value.InlineValue;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public class DoubleValue implements InlineValue<Double> {
    @Override
    public boolean matches(final @NotNull String text) {
        if (!text.endsWith("D")) return false;

        final String other = text.replaceAll("D", "");
        try {
            Double.valueOf(other);
            return true;
        } catch (final NumberFormatException e) {
            return false;
        }
    }

    @Override
    public @Nullable Double deserialize(final @NotNull String text) {
        try {
            return Double.valueOf(text.replaceAll("D", ""));
        } catch (NumberFormatException e) {
            return -1.0;
        }
    }

    @Override
    public @Nullable String serialize(final @NotNull Double aDouble) {
        return aDouble + "D";
    }
}

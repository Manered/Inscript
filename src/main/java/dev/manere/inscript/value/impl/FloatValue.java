package dev.manere.inscript.value.impl;

import dev.manere.inscript.value.InlineValue;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public class FloatValue implements InlineValue<Float> {
    @Override
    public boolean matches(final @NotNull String text) {
        if (!text.endsWith("F")) return false;

        final String other = text.substring(0, text.length() - 1);
        try {
            Float.valueOf(other);
            return true;
        } catch (final NumberFormatException e) {
            return false;
        }
    }

    @Override
    public @Nullable Float deserialize(final @NotNull String text) {
        try {
            return Float.valueOf(text.substring(0, text.length() - 1));
        } catch (NumberFormatException e) {
            return -1.0F;
        }
    }

    @Override
    public @Nullable String serialize(final @NotNull Float aFloat) {
        return aFloat + "F";
    }
}

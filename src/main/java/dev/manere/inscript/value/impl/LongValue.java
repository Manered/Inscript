package dev.manere.inscript.value.impl;

import dev.manere.inscript.value.InlineValue;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public class LongValue implements InlineValue<Long> {
    @Override
    public boolean matches(final @NotNull String text) {
        if (!text.endsWith("L")) return false;

        final String other = text.substring(0, text.length() - 1);
        try {
            Long.valueOf(other);
            return true;
        } catch (final NumberFormatException e) {
            return false;
        }
    }

    @Override
    public @Nullable Long deserialize(final @NotNull String text) {
        try {
            return Long.valueOf(text.substring(0, text.length() - 1));
        } catch (NumberFormatException e) {
            return -1L;
        }
    }

    @Override
    public @Nullable String serialize(final @NotNull Long aLong) {
        return aLong + "L";
    }
}

package dev.manere.inscript.value.impl;

import dev.manere.inscript.value.InlineValue;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public class IntegerValue implements InlineValue<Integer> {
    @Override
    public boolean matches(final @NotNull String text) {
        try {
            Integer.valueOf(text);
            return true;
        } catch (final NumberFormatException e) {
            return false;
        }
    }

    @Override
    public @Nullable Integer deserialize(final @NotNull String text) {
        try {
            return Integer.valueOf(text);
        } catch (NumberFormatException e) {
            return -1;
        }
    }

    @Override
    public @Nullable String serialize(final @NotNull Integer integer) {
        return String.valueOf(integer);
    }
}
